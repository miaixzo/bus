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
package org.miaixz.bus.tempus.temporal.worker;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Binding;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;

/**
 * Creates Temporal service stub handles for specific endpoints.
 * <p>
 * Implementations may return any object representing the underlying Temporal service stubs. The default methods on this
 * interface use reflection so callers can work with Temporal service stubs through opaque handles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowServiceStubsProvider {

    /**
     * Caches the compatible single-argument {@link WorkflowClient#newInstance} factory method by runtime service stub
     * type.
     */
    Map<Class<?>, Method> WORKFLOW_CLIENT_FACTORY_CACHE = new ConcurrentHashMap<>();

    /**
     * Caches the compatible two-argument {@link WorkflowClient#newInstance} factory method by runtime service stub
     * type.
     */
    Map<Class<?>, Method> WORKFLOW_CLIENT_FACTORY_WITH_OPTIONS_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a service stub handle for the specified Temporal configuration.
     * <p>
     * This is the primary entry point because bindings may carry transport-related metadata beyond the endpoint.
     *
     * @param binding temporal configuration
     * @return an opaque service stub handle
     */
    Object createServiceStubs(Binding binding);

    /**
     * Creates a workflow client backed by the specified service stub handle and temporal configuration.
     *
     * @param serviceStubs the opaque service stub handle
     * @param binding      temporal configuration (optional)
     * @return the workflow client
     */
    default WorkflowClient createWorkflowClient(Object serviceStubs, Binding binding) {
        Assert.notNull(serviceStubs, "serviceStubs must not be null");
        Assert.notNull(binding, "binding must not be null");
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");

        Logger.debug(
                true,
                "Tempus",
                "Workflow client creation with binding started: endpoint={}, namespace={}, identity={}, stubsType={}",
                binding.getEndpoint(),
                binding.getNamespace(),
                binding.getIdentity(),
                serviceStubs.getClass().getName());

        try {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow client options creation started: endpoint={}, namespace={}, identity={}",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity());

            if (StringKit.hasText(binding.getNamespace())) {
                try {
                    WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder();
                    builder.setNamespace(binding.getNamespace());
                    if (StringKit.hasText(binding.getIdentity())) {
                        builder.setIdentity(binding.getIdentity());
                    }

                    Method factoryMethod = findWorkflowClientFactoryWithOptions(serviceStubs.getClass());
                    WorkflowClient client = MethodKit
                            .invokeStatic(factoryMethod, serviceStubs, builder.validateAndBuildWithDefaults());
                    Logger.debug(
                            false,
                            "Tempus",
                            "Workflow client creation with options completed: endpoint={}, namespace={}, identity={}",
                            binding.getEndpoint(),
                            binding.getNamespace(),
                            binding.getIdentity());
                    return client;
                } catch (IllegalStateException ignore) {
                    // No compatible 2-arg overload; fall back to the low-level bridge.
                    Logger.debug(
                            false,
                            "Tempus",
                            "Workflow client creation with options skipped: stubsType={}, reason=noCompatibleFactory",
                            serviceStubs.getClass().getName());
                }
            }

            WorkflowClient client = createWorkflowClientWithoutBinding(serviceStubs);
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow client creation with binding completed: endpoint={}, namespace={}, identity={}, fallback=true",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity());
            return client;
        } catch (RuntimeException e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow client creation with binding failed: stubsType={}, endpoint={}, namespace={}, identity={}, exception={}",
                    serviceStubs.getClass().getName(),
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getClass().getSimpleName());
            throw new IllegalStateException("Failed to create WorkflowClient with binding", e);
        }
    }

    /**
     * Creates a workflow client backed by the specified service stub handle.
     * <p>
     * This low-level path omits binding-specific client options and should only be used when the caller explicitly
     * intends to create a client from an already prepared service-stub handle.
     *
     * @param serviceStubs the opaque service stub handle
     * @return the workflow client
     */
    default WorkflowClient createWorkflowClient(Object serviceStubs) {
        Assert.notNull(serviceStubs, "serviceStubs must not be null");
        return createWorkflowClientWithoutBinding(serviceStubs);
    }

    /**
     * Creates a workflow client from service stubs without binding-aware client options.
     *
     * @param serviceStubs the opaque service stub handle
     * @return the workflow client
     */
    private WorkflowClient createWorkflowClientWithoutBinding(Object serviceStubs) {
        Assert.notNull(serviceStubs, "serviceStubs must not be null");
        Logger.debug(
                true,
                "Tempus",
                "Workflow client creation from service stubs started: stubsType={}",
                serviceStubs.getClass().getName());

        Method factoryMethod = findWorkflowClientFactory(serviceStubs.getClass());
        WorkflowClient client = MethodKit.invokeStatic(factoryMethod, serviceStubs);
        Logger.debug(
                false,
                "Tempus",
                "Workflow client creation from service stubs completed: stubsType={}",
                serviceStubs.getClass().getName());
        return client;
    }

    /**
     * Shuts down the specified service stub handle.
     *
     * @param serviceStubs the opaque service stub handle
     */
    default void shutdownServiceStubs(Object serviceStubs) {
        if (serviceStubs == null) {
            return;
        }

        Logger.debug(
                true,
                "Tempus",
                "Workflow service stubs shutdown started: stubsType={}",
                serviceStubs.getClass().getName());

        try {
            invokeNoArgIfPresent(serviceStubs, "shutdown");
            boolean terminated = invokeAwaitTerminationIfPresent(serviceStubs, 5L, TimeUnit.SECONDS);
            if (!terminated) {
                Logger.warn(
                        false,
                        "Tempus",
                        "Workflow service stubs shutdown timeout: stubsType={}, forceShutdown=true",
                        serviceStubs.getClass().getName());
                invokeNoArgIfPresent(serviceStubs, "shutdownNow");
                invokeAwaitTerminationIfPresent(serviceStubs, 2L, TimeUnit.SECONDS);
            }

            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow service stubs shutdown completed: stubsType={}",
                    serviceStubs.getClass().getName());
        } catch (RuntimeException e) {
            if (ExceptionKit.isCausedBy(e, InterruptedException.class)) {
                Thread.currentThread().interrupt();
            }
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow service stubs shutdown failed: stubsType={}, exception={}",
                    serviceStubs.getClass().getName(),
                    e.getClass().getSimpleName());
            throw new IllegalStateException("Failed to shut down WorkflowServiceStubs", e);
        }
    }

    /**
     * Locates a compatible {@link WorkflowClient#newInstance} factory method for the given service stub type.
     *
     * @param serviceStubsType the runtime service stub type
     * @return the matching factory method
     */
    private static Method findWorkflowClientFactory(Class<?> serviceStubsType) {
        Method method = WORKFLOW_CLIENT_FACTORY_CACHE.computeIfAbsent(serviceStubsType, type -> {
            for (Method candidate : MethodKit.getPublicMethods(WorkflowClient.class)) {
                if (!"newInstance".equals(candidate.getName()) || candidate.getParameterCount() != 1) {
                    continue;
                }
                Class<?> parameterType = candidate.getParameterTypes()[0];
                if (parameterType.isAssignableFrom(type)) {
                    return candidate;
                }
            }
            return null;
        });
        if (method == null) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Workflow client factory lookup failed: stubsType={}, mode=singleArg",
                    serviceStubsType.getName());
            throw new IllegalStateException(
                    "No WorkflowClient.newInstance method accepts " + serviceStubsType.getName());
        }
        Logger.debug(
                false,
                "Tempus",
                "Workflow client factory lookup completed: stubsType={}, mode=singleArg",
                serviceStubsType.getName());
        return method;
    }

    /**
     * Locates a compatible two-argument {@link WorkflowClient#newInstance} factory method for the given service stub
     * type.
     *
     * @param serviceStubsType the runtime service stub type
     * @return the matching factory method
     */
    private static Method findWorkflowClientFactoryWithOptions(Class<?> serviceStubsType) {
        Method method = WORKFLOW_CLIENT_FACTORY_WITH_OPTIONS_CACHE.computeIfAbsent(serviceStubsType, type -> {
            for (Method candidate : MethodKit.getPublicMethods(WorkflowClient.class)) {
                if (!"newInstance".equals(candidate.getName()) || candidate.getParameterCount() != 2) {
                    continue;
                }
                Class<?>[] parameterTypes = candidate.getParameterTypes();
                if (parameterTypes[0].isAssignableFrom(type)
                        && WorkflowClientOptions.class.isAssignableFrom(parameterTypes[1])) {
                    return candidate;
                }
            }
            return null;
        });
        if (method == null) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Workflow client factory lookup failed: stubsType={}, mode=withOptions",
                    serviceStubsType.getName());
            throw new IllegalStateException(
                    "No WorkflowClient.newInstance(stubs, options) method accepts " + serviceStubsType.getName());
        }
        Logger.debug(
                false,
                "Tempus",
                "Workflow client factory lookup completed: stubsType={}, mode=withOptions",
                serviceStubsType.getName());
        return method;
    }

    /**
     * Invokes a no-argument method on the specified service stub handle.
     *
     * @param target     the target object
     * @param methodName the method name
     */
    private static void invokeNoArg(Object target, String methodName) {
        invoke(target, methodName, new Class<?>[0]);
    }

    /**
     * Invokes a no-argument method on the target object when the method is available.
     *
     * @param target     the target object
     * @param methodName the method name
     */
    private static void invokeNoArgIfPresent(Object target, String methodName) {
        invokeIfPresent(target, methodName, new Class<?>[0]);
    }

    /**
     * Invokes a method on the specified service stub handle.
     *
     * @param target         the target object
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the invocation arguments
     */
    private static void invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = MethodKit.getPublicMethod(target.getClass(), false, methodName, parameterTypes);
        if (method == null) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Workflow service stubs method invocation failed: stubsType={}, method={}",
                    target.getClass().getName(),
                    methodName);
            throw new IllegalStateException(
                    "Method " + methodName + " not found on service stubs type " + target.getClass().getName());
        }
        Logger.debug(
                true,
                "Tempus",
                "Workflow service stubs method invocation started: stubsType={}, method={}",
                target.getClass().getName(),
                methodName);
        MethodKit.invoke(target, method, args);
        Logger.debug(
                false,
                "Tempus",
                "Workflow service stubs method invocation completed: stubsType={}, method={}",
                target.getClass().getName(),
                methodName);
    }

    /**
     * Invokes a method on the target object when the method exists; missing methods are treated as optional.
     *
     * @param target         the target object
     * @param methodName     the method name
     * @param parameterTypes the method parameter types
     * @param args           the invocation arguments
     */
    private static void invokeIfPresent(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Method method = MethodKit.getPublicMethod(target.getClass(), false, methodName, parameterTypes);
        if (method != null) {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow service stubs optional method invocation started: stubsType={}, method={}",
                    target.getClass().getName(),
                    methodName);
            MethodKit.invoke(target, method, args);
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow service stubs optional method invocation completed: stubsType={}, method={}",
                    target.getClass().getName(),
                    methodName);
        } else {
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow service stubs optional method skipped: stubsType={}, method={}, reason=missing",
                    target.getClass().getName(),
                    methodName);
        }
    }

    /**
     * Invokes {@code awaitTermination(long, TimeUnit)} when the service stub implementation exposes it.
     *
     * @param target  the target object
     * @param timeout the timeout value
     * @param unit    the timeout unit
     * @return {@code true} when the method is absent or indicates successful termination; {@code false} otherwise
     */
    private static boolean invokeAwaitTerminationIfPresent(Object target, long timeout, TimeUnit unit) {
        Method method = MethodKit
                .getPublicMethod(target.getClass(), false, "awaitTermination", long.class, TimeUnit.class);
        if (method == null) {
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow service stubs await termination skipped: stubsType={}, reason=missingMethod",
                    target.getClass().getName());
            return true;
        }
        Logger.debug(
                true,
                "Tempus",
                "Workflow service stubs await termination started: stubsType={}, timeout={}, unit={}",
                target.getClass().getName(),
                timeout,
                unit);
        Object result = MethodKit.invoke(target, method, timeout, unit);
        boolean terminated = !(result instanceof Boolean value) || value;
        Logger.debug(
                false,
                "Tempus",
                "Workflow service stubs await termination completed: stubsType={}, timeout={}, unit={}, terminated={}",
                target.getClass().getName(),
                timeout,
                unit,
                terminated);
        return terminated;
    }

}
