/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.tempus.temporal.workflow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.DynamicActivity;
import io.temporal.worker.TypeAlreadyRegisteredException;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkflowImplementationOptions;
import io.temporal.workflow.ActivityStub;
import io.temporal.workflow.DynamicWorkflow;
import io.temporal.workflow.Workflow;

/**
 * Adapts Temporal workflow and activity registration for native-image friendly Jackson 3 only runtimes.
 * <p>
 * Temporal SDK 1.35 still contains several internal metadata paths that directly reference Jackson 2 classes. This
 * adapter keeps the public Temporal worker model unchanged while routing workflow and activity registration through
 * dynamic dispatch and lightweight proxy code that does not require Jackson 2 on the runtime classpath.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class NativeWorkflowAdapter {

    /**
     * Utility class constructor.
     */
    private NativeWorkflowAdapter() {
        // No initialization required.
    }

    /**
     * Registers activity implementations with a Temporal worker.
     * <p>
     * Bypasses Temporal 1.35 metadata validation methods that hard-link Jackson 2 while preserving the same activity
     * type naming rules used by Temporal.
     *
     * @param worker     Temporal worker
     * @param activities activity implementations
     * @throws TypeAlreadyRegisteredException if an activity type has already been registered on the worker
     * @throws IllegalStateException          if Temporal's internal worker structures cannot be accessed
     */
    public static void registerActivitiesImplementations(Worker worker, Object... activities) {
        try {
            Object activityWorker = field(worker, "activityWorker");
            Object taskHandler = field(activityWorker, "taskHandler");

            Map<String, Object> registered = (Map<String, Object>) field(taskHandler, "activities");
            for (Object activity : activities) {
                if (activity instanceof DynamicActivity) {
                    worker.registerActivitiesImplementations(activity);
                    continue;
                }
                for (Map.Entry<String, Method> entry : activityMethods(activity).entrySet()) {
                    if (registered.containsKey(entry.getKey())) {
                        throw new TypeAlreadyRegisteredException(entry.getKey(),
                                "\"" + entry.getKey() + "\" activity type is already registered with the worker");
                    }
                    registered.put(entry.getKey(), activityExecutor(taskHandler, activity, entry.getValue()));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register Temporal activities", e);
        }
    }

    /**
     * Registers a single-argument workflow using Temporal dynamic workflow dispatch.
     * <p>
     * Temporal 1.35 loads an internal utility class that hard-links Jackson 2 while scanning POJO workflow metadata.
     * This path keeps the public workflow type unchanged and delegates execution to the original handler without
     * loading the Jackson 2-bound metadata utility.
     *
     * @param worker       Temporal worker
     * @param workflowType Temporal workflow type
     * @param requestType  workflow request type
     * @param factory      creates a workflow invocation function inside Temporal workflow context
     * @param <R>          workflow request type
     * @throws NullPointerException if any required argument is {@code null}
     */
    public static <R> void registerWorkflowImplementationFactory(
            Worker worker,
            String workflowType,
            Class<R> requestType,
            Supplier<? extends Function<R, ?>> factory) {
        Objects.requireNonNull(worker, "worker must not be null");
        Objects.requireNonNull(workflowType, "workflowType must not be null");
        Objects.requireNonNull(requestType, "requestType must not be null");
        Objects.requireNonNull(factory, "factory must not be null");
        worker.registerWorkflowImplementationFactory(
                DynamicWorkflow.class,
                ignored -> new DynamicWorkflowHandler<>(workflowType, requestType, factory.get()),
                WorkflowImplementationOptions.getDefaultInstance());
    }

    /**
     * Creates a typed activity stub without Temporal's Jackson 2-bound activity invocation handler.
     *
     * @param activityClass activity interface
     * @param options       activity options
     * @param <A>           activity interface type
     * @return typed activity stub
     * @throws NullPointerException     if {@code activityClass} is {@code null}
     * @throws IllegalArgumentException if invoked methods are not declared on an {@link ActivityInterface}
     */
    public static <A> A newActivityStub(Class<A> activityClass, ActivityOptions options) {
        Objects.requireNonNull(activityClass, "activityClass must not be null");
        ActivityStub stub = Workflow.newUntypedActivityStub(options);
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return objectMethod(proxy, method, args);
            }
            ActivityInterface activityInterface = activityInterface(activityClass, method);
            String activityName = activityTypeName(activityInterface, method);
            Object result = stub.execute(
                    activityName,
                    method.getReturnType(),
                    method.getGenericReturnType(),
                    args == null ? new Object[0] : args);
            return defaultValue(result, method.getReturnType());
        };
        Object proxy = Proxy
                .newProxyInstance(activityClass.getClassLoader(), new Class<?>[] { activityClass }, handler);
        return activityClass.cast(proxy);
    }

    /**
     * Resolves all Temporal activity methods declared by the implementation's {@link ActivityInterface} interfaces.
     * <p>
     * The returned map uses the final Temporal activity type name as the key, including {@link ActivityMethod#name()}
     * overrides and {@link ActivityInterface#namePrefix()} based names. Interface inheritance is traversed explicitly
     * so inherited activity contracts are registered with the worker as well.
     *
     * @param activity activity implementation instance
     * @return ordered map of activity type name to Java method
     */
    private static Map<String, Method> activityMethods(Object activity) {
        Map<String, Method> methods = new LinkedHashMap<>();
        Set<Class<?>> seen = new LinkedHashSet<>();
        List<Class<?>> interfaces = new ArrayList<>();
        for (Class<?> type = activity.getClass(); type != null; type = type.getSuperclass()) {
            for (Class<?> interfaceType : type.getInterfaces()) {
                interfaces.add(interfaceType);
            }
        }
        for (int i = 0; i < interfaces.size(); i++) {
            Class<?> interfaceType = interfaces.get(i);
            if (!seen.add(interfaceType)) {
                continue;
            }
            for (Class<?> parent : interfaceType.getInterfaces()) {
                interfaces.add(parent);
            }
            ActivityInterface activityInterface = interfaceType.getAnnotation(ActivityInterface.class);
            if (activityInterface == null) {
                continue;
            }
            for (Method method : interfaceType.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                methods.put(activityTypeName(activityInterface, method), method);
            }
        }
        return methods;
    }

    /**
     * Creates Temporal's internal POJO activity executor for the supplied implementation method.
     * <p>
     * This mirrors the SDK's normal executor construction while avoiding the metadata validation path that requires
     * Jackson 2. The data converter, interceptors, context propagators and execution context factory are reused from
     * the existing worker task handler.
     *
     * @param taskHandler Temporal internal activity task handler
     * @param activity    activity implementation instance
     * @param method      activity method to execute
     * @return Temporal internal activity executor
     * @throws ReflectiveOperationException if the expected Temporal internal constructor or fields are unavailable
     */
    private static Object activityExecutor(Object taskHandler, Object activity, Method method)
            throws ReflectiveOperationException {
        Constructor<?> constructor = Class
                .forName("io.temporal.internal.activity.ActivityTaskExecutors$POJOActivityImplementation")
                .getDeclaredConstructor(
                        Method.class,
                        Object.class,
                        Class.forName("io.temporal.common.converter.DataConverter"),
                        List.class,
                        Class.forName("[Lio.temporal.common.interceptors.WorkerInterceptor;"),
                        Class.forName("io.temporal.internal.activity.ActivityExecutionContextFactory"));
        constructor.setAccessible(true);
        return constructor.newInstance(
                method,
                activity,
                field(taskHandler, "dataConverter"),
                field(taskHandler, "contextPropagators"),
                field(taskHandler, "interceptors"),
                field(taskHandler, "executionContextFactory"));
    }

    /**
     * Resolves the Temporal activity type name for a method.
     * <p>
     * Explicit {@link ActivityMethod#name()} values have priority. Otherwise the name is built from the activity
     * interface prefix and the capitalized Java method name, matching Temporal's default naming rule.
     *
     * @param activityInterface activity interface annotation that defines the naming prefix
     * @param method            activity method
     * @return Temporal activity type name
     */
    private static String activityTypeName(ActivityInterface activityInterface, Method method) {
        ActivityMethod activityMethod = method.getAnnotation(ActivityMethod.class);
        if (activityMethod != null && !activityMethod.name().isEmpty()) {
            return activityMethod.name();
        }
        String name = method.getName();
        return activityInterface.namePrefix() + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Locates the {@link ActivityInterface} annotation that applies to an activity method.
     * <p>
     * The method declaring interface is checked first because that is the normal Temporal contract location. The
     * activity class itself is used as a fallback for implementations that place the annotation directly on the class.
     *
     * @param activityClass activity interface or implementation class used by the proxy
     * @param method        invoked activity method
     * @return resolved activity interface annotation
     * @throws IllegalArgumentException if neither the method declaration nor activity class is annotated
     */
    private static ActivityInterface activityInterface(Class<?> activityClass, Method method) {
        ActivityInterface activityInterface = method.getDeclaringClass().getAnnotation(ActivityInterface.class);
        if (activityInterface != null) {
            return activityInterface;
        }
        activityInterface = activityClass.getAnnotation(ActivityInterface.class);
        if (activityInterface != null) {
            return activityInterface;
        }
        throw new IllegalArgumentException("Activity method is not declared on an @ActivityInterface: " + method);
    }

    /**
     * Converts a {@code null} activity result into the JVM default value for primitive return types.
     * <p>
     * Temporal's untyped stub returns boxed values. This method keeps proxy calls compatible with primitive activity
     * method signatures when the underlying result is absent.
     *
     * @param value activity execution result
     * @param type  declared Java return type
     * @return original result, or the primitive default value when required
     */
    private static Object defaultValue(Object value, Class<?> type) {
        if (value != null || !type.isPrimitive()) {
            return value;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        return null;
    }

    /**
     * Handles {@link Object} methods invoked on a generated activity proxy.
     * <p>
     * Activity method calls are dispatched to Temporal, but {@code toString}, {@code hashCode} and {@code equals} must
     * be answered locally to keep proxy identity behavior stable.
     *
     * @param proxy  generated activity proxy
     * @param method invoked {@link Object} method
     * @param args   invocation arguments
     * @return local result for the invoked {@link Object} method
     * @throws UnsupportedOperationException if an unexpected {@link Object} method is invoked
     */
    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        if ("toString".equals(name)) {
            return proxy.getClass().getName();
        }
        if ("hashCode".equals(name)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(name)) {
            return proxy == args[0];
        }
        throw new UnsupportedOperationException("Unsupported Object method: " + name);
    }

    /**
     * Reads a private field from a Temporal internal object.
     * <p>
     * The adapter intentionally keeps this reflective access in one place because it depends on Temporal SDK internals.
     *
     * @param target object that owns the field
     * @param name   field name
     * @return field value
     * @throws ReflectiveOperationException if the field cannot be found or read
     */
    private static Object field(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

}
