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
package org.miaixz.bus.cron.temporal.worker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import io.temporal.client.WorkflowClient;

/**
 * Creates Temporal service stub handles for specific endpoints.
 * <p>
 * Implementations may return any object representing the underlying Temporal service stubs. The default methods on this
 * interface use reflection so callers can work with Temporal service stubs without importing
 * {@code io.temporal.serviceclient} from this module.
 */
public interface WorkflowServiceStubsProvider {

    /**
     * Creates a service stub handle for the specified Temporal server endpoint.
     *
     * @param endpoint the Temporal server endpoint
     * @return an opaque service stub handle
     */
    Object createServiceStubs(String endpoint);

    /**
     * Creates a workflow client backed by the specified service stub handle.
     *
     * @param serviceStubs the opaque service stub handle
     * @return the workflow client
     */
    default WorkflowClient createWorkflowClient(Object serviceStubs) {
        if (serviceStubs == null) {
            throw new IllegalArgumentException("serviceStubs must not be null");
        }
        try {
            Method factoryMethod = findWorkflowClientFactory(serviceStubs.getClass());
            return (WorkflowClient) factoryMethod.invoke(null, serviceStubs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to create WorkflowClient from service stubs", e);
        }
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
        try {
            invokeNoArg(serviceStubs, "shutdown");
            invoke(
                    serviceStubs,
                    "awaitTermination",
                    new Class<?>[] { long.class, TimeUnit.class },
                    5L,
                    TimeUnit.SECONDS);
        } catch (IllegalAccessException | InvocationTargetException e) {
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
        for (Method method : WorkflowClient.class.getMethods()) {
            if (!"newInstance".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType.isAssignableFrom(serviceStubsType)) {
                return method;
            }
        }
        throw new IllegalStateException("No WorkflowClient.newInstance method accepts " + serviceStubsType.getName());
    }

    /**
     * Invokes a no-argument method on the specified service stub handle.
     *
     * @param target     the target object
     * @param methodName the method name
     * @throws IllegalAccessException    if method access fails
     * @throws InvocationTargetException if the method throws an exception
     */
    private static void invokeNoArg(Object target, String methodName)
            throws IllegalAccessException, InvocationTargetException {
        invoke(target, methodName, new Class<?>[0]);
    }

    /**
     * Invokes a method on the specified service stub handle.
     *
     * @param target         the target object
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the invocation arguments
     * @throws IllegalAccessException    if method access fails
     * @throws InvocationTargetException if the method throws an exception
     */
    private static void invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.invoke(target, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Method " + methodName + " not found on service stubs type " + target.getClass().getName(), e);
        }
    }

}
