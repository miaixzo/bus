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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Binding;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;

/**
 * Creates Temporal service stub handles for specific endpoints.
 * <p>
 * Implementations may return any object representing the underlying Temporal service stubs. The default methods on this
 * interface use reflection so callers can work with Temporal service stubs without importing
 * {@code io.temporal.serviceclient} from this module.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * Creates a service stub handle for the specified Temporal configuration.
     * <p>
     * Default implementation delegates to {@link #createServiceStubs(String)}.
     *
     * @param binding temporal configuration
     * @return an opaque service stub handle
     */
    default Object createServiceStubs(Binding binding) {
        if (binding == null) {
            throw new IllegalArgumentException("binding must not be null");
        }
        if (binding.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }

        Logger.debug(
                "Creating service stubs, endpoint: {}, namespace: {}, identity: {}",
                binding.getEndpoint(),
                binding.getNamespace(),
                binding.getIdentity());

        try {
            Object stubs = createServiceStubs(binding.getEndpoint());
            Logger.debug(
                    "Created service stubs successfully, endpoint: {}, stubsType: {}",
                    binding.getEndpoint(),
                    stubs != null ? stubs.getClass().getName() : null);
            return stubs;
        } catch (Exception e) {
            Logger.error(
                    "Failed to create service stubs, endpoint: {}, namespace: {}, identity: {}, error: {}",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

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
        Logger.debug("Creating workflow client from service stubs, stubsType: {}", serviceStubs.getClass().getName());

        try {
            Method factoryMethod = findWorkflowClientFactory(serviceStubs.getClass());
            WorkflowClient client = (WorkflowClient) factoryMethod.invoke(null, serviceStubs);
            Logger.debug("Created workflow client from service stubs successfully");
            return client;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Logger.error(
                    "Failed to create WorkflowClient from service stubs, stubsType: {}, error: {}",
                    serviceStubs.getClass().getName(),
                    e.getMessage(),
                    e);
            throw new IllegalStateException("Failed to create WorkflowClient from service stubs", e);
        }
    }

    /**
     * Creates a workflow client backed by the specified service stub handle and temporal configuration.
     *
     * @param serviceStubs the opaque service stub handle
     * @param binding      temporal configuration (optional)
     * @return the workflow client
     */
    default WorkflowClient createWorkflowClient(Object serviceStubs, Binding binding) {
        if (serviceStubs == null) {
            throw new IllegalArgumentException("serviceStubs must not be null");
        }

        Logger.debug(
                "Creating workflow client, endpoint: {}, namespace: {}, identity: {}",
                binding != null ? binding.getEndpoint() : null,
                binding != null ? binding.getNamespace() : null,
                binding != null ? binding.getIdentity() : null);

        // Avoid direct dependency on Temporal serviceclient/grpc. If the runtime type supports a
        // WorkflowClient.newInstance(stubs, options) overload, use it; otherwise fall back to the
        // 1-arg overload.
        if (binding != null && StringKit.hasText(binding.getNamespace())) {
            try {
                WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder();
                builder.setNamespace(binding.getNamespace());
                if (StringKit.hasText(binding.getIdentity())) {
                    builder.setIdentity(binding.getIdentity());
                }

                Method factoryMethod = findWorkflowClientFactoryWithOptions(serviceStubs.getClass());
                WorkflowClient client = (WorkflowClient) factoryMethod
                        .invoke(null, serviceStubs, builder.validateAndBuildWithDefaults());
                Logger.debug("Created workflow client with options successfully");
                return client;
            } catch (IllegalStateException ignore) {
                // No compatible 2-arg overload; fall back to the 1-arg variant.
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.error(
                        "Failed to create WorkflowClient with options, stubsType: {}, error: {}",
                        serviceStubs.getClass().getName(),
                        e.getMessage(),
                        e);
                throw new IllegalStateException("Failed to create WorkflowClient with options", e);
            }
        }

        return createWorkflowClient(serviceStubs);
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

        Logger.debug("Shutting down service stubs, stubsType: {}", serviceStubs.getClass().getName());

        try {
            invokeNoArg(serviceStubs, "shutdown");
            invoke(
                    serviceStubs,
                    "awaitTermination",
                    new Class<?>[] { long.class, TimeUnit.class },
                    5L,
                    TimeUnit.SECONDS);

            Logger.debug("Service stubs shutdown completed, stubsType: {}", serviceStubs.getClass().getName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            Logger.error(
                    "Failed to shut down WorkflowServiceStubs, stubsType: {}, error: {}",
                    serviceStubs.getClass().getName(),
                    e.getMessage(),
                    e);
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

    private static Method findWorkflowClientFactoryWithOptions(Class<?> serviceStubsType) {
        for (Method method : WorkflowClient.class.getMethods()) {
            if (!"newInstance".equals(method.getName()) || method.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes[0].isAssignableFrom(serviceStubsType)
                    && WorkflowClientOptions.class.isAssignableFrom(parameterTypes[1])) {
                return method;
            }
        }
        throw new IllegalStateException(
                "No WorkflowClient.newInstance(stubs, options) method accepts " + serviceStubsType.getName());
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
