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
 * Default Temporal workflow transport based on Temporal SDK service stubs.
 * <p>
 * This transport intentionally avoids direct gRPC imports and keeps transport details inside the Temporal SDK layer.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultWorkflowTransport implements WorkflowTransport {

    /**
     * Temporal service stubs class name.
     */
    private static final String WORKFLOW_SERVICE_STUBS_CLASS = "io.temporal.serviceclient.WorkflowServiceStubs";

    /**
     * Temporal service stubs options class name.
     */
    private static final String WORKFLOW_SERVICE_STUBS_OPTIONS_CLASS = "io.temporal.serviceclient.WorkflowServiceStubsOptions";

    /**
     * Creates a default workflow transport.
     */
    public DefaultWorkflowTransport() {
        // No initialization required.
    }

    /**
     * Creates a Temporal transport handle from the binding endpoint.
     *
     * @param binding Temporal binding configuration
     * @return opaque transport handle
     */
    @Override
    public Object create(Binding binding) {
        String rawEndpoint = binding == null ? null : binding.getEndpoint();
        WorkflowEndpointParser.Endpoint endpoint = WorkflowEndpointParser.resolve(rawEndpoint);
        Logger.info(
                true,
                "Tempus",
                "Temporal service stubs creation started: rawEndpoint={}, target={}, namespace={}, taskQueue={}",
                endpoint.rawEndpoint(),
                endpoint.target(),
                binding == null ? null : binding.getNamespace(),
                binding == null ? null : binding.getTaskQueue());
        Object builder = newServiceStubsOptionsBuilder();
        if (StringKit.isNotBlank(endpoint.target())) {
            MethodKit.invoke(builder, "setTarget", endpoint.target());
            MethodKit.invoke(builder, "setEnableHttps", endpoint.enableHttps());
        }
        Object options = MethodKit.invoke(builder, "build");
        Object serviceStubs = newServiceStubs(options);
        Logger.info(
                false,
                "Tempus",
                "Temporal service stubs creation completed: rawEndpoint={}, target={}, stubsType={}",
                endpoint.rawEndpoint(),
                endpoint.target(),
                serviceStubs.getClass().getName());
        return serviceStubs;
    }

    /**
     * Creates a workflow client from a transport handle.
     *
     * @param handle opaque transport handle
     * @return workflow client
     */
    @Override
    public WorkflowClient client(Object handle) {
        requireWorkflowServiceStubs(handle);
        Logger.debug(
                true,
                "Tempus",
                "Temporal workflow client creation started: mode=default, stubsType={}",
                handle.getClass().getName());
        WorkflowClient client = clientWithoutBinding(handle);
        Logger.debug(false, "Tempus", "Temporal workflow client creation completed: mode=default");
        return client;
    }

    /**
     * Creates a workflow client from a transport handle and binding metadata.
     *
     * @param handle  opaque transport handle
     * @param binding Temporal binding configuration
     * @return workflow client
     */
    @Override
    public WorkflowClient client(Object handle, Binding binding) {
        requireWorkflowServiceStubs(handle);
        Assert.notNull(binding, "binding must not be null");
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");
        Logger.debug(
                true,
                "Tempus",
                "Temporal workflow client creation started: mode=binding, namespace={}, identity={}, stubsType={}",
                binding.getNamespace(),
                binding.getIdentity(),
                handle.getClass().getName());
        try {
            if (StringKit.hasText(binding.getNamespace())) {
                try {
                    WorkflowClientOptions.Builder builder = WorkflowClientOptions.newBuilder();
                    builder.setNamespace(binding.getNamespace());
                    if (StringKit.hasText(binding.getIdentity())) {
                        builder.setIdentity(binding.getIdentity());
                    }

                    Method factoryMethod = findWorkflowClientFactoryWithOptions(handle.getClass());
                    WorkflowClient client = MethodKit
                            .invokeStatic(factoryMethod, handle, builder.validateAndBuildWithDefaults());
                    Logger.debug(
                            false,
                            "Tempus",
                            "Temporal workflow client creation completed: mode=binding, namespace={}, identity={}",
                            binding.getNamespace(),
                            binding.getIdentity());
                    return client;
                } catch (IllegalStateException ignore) {
                    Logger.debug(
                            false,
                            "Tempus",
                            "Temporal workflow client creation with options skipped: stubsType={}, reason=noCompatibleFactory",
                            handle.getClass().getName());
                }
            }

            WorkflowClient client = clientWithoutBinding(handle);
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal workflow client creation completed: mode=binding, namespace={}, identity={}, fallback=true",
                    binding.getNamespace(),
                    binding.getIdentity());
            return client;
        } catch (RuntimeException e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Temporal workflow client creation failed: stubsType={}, endpoint={}, namespace={}, identity={}, exception={}",
                    handle.getClass().getName(),
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getClass().getSimpleName());
            throw new IllegalStateException("Failed to create WorkflowClient with binding", e);
        }
    }

    /**
     * Gets the current transport state from a transport handle.
     *
     * @param handle opaque transport handle
     * @return current transport state
     */
    @Override
    public String state(Object handle) {
        if (handle == null) {
            Logger.debug(false, "Tempus", "Workflow transport state skipped: reason=noStubs");
            return shutdownTransportState();
        }
        try {
            Method rawChannelMethod = handle.getClass().getMethod("getRawChannel");
            Object channel = rawChannelMethod.invoke(handle);
            if (channel == null) {
                Logger.debug(false, "Tempus", "Workflow transport state skipped: reason=noChannel");
                return shutdownTransportState();
            }
            Method stateMethod = channel.getClass().getMethod("getState", boolean.class);
            Object state = stateMethod.invoke(channel, true);
            String transportState = state == null ? unknownTransportState() : String.valueOf(state);
            Logger.debug(false, "Tempus", "Workflow transport state read: state={}", transportState);
            return transportState;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Throwable rootCause = ExceptionKit.getRootCause(e);
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow transport state skipped: stubsType={}, reason=unsupported, exception={}",
                    handle.getClass().getName(),
                    rootCause.getClass().getSimpleName());
            return unknownTransportState();
        }
    }

    /**
     * Probes the Temporal service with a lightweight system information request.
     *
     * @param handle         opaque transport handle
     * @param timeoutSeconds probe timeout in seconds
     * @return {@code true} when the service responds successfully
     */
    @Override
    public boolean health(Object handle, long timeoutSeconds) {
        if (handle == null) {
            Logger.warn(false, "Tempus", "Workflow service stubs health check skipped: reason=noStubs");
            return false;
        }
        long deadlineSeconds = timeoutSeconds <= 0 ? 5L : timeoutSeconds;
        try {
            Object blockingStub = handle.getClass().getMethod("blockingStub").invoke(handle);
            Object deadlineStub = blockingStub.getClass().getMethod("withDeadlineAfter", long.class, TimeUnit.class)
                    .invoke(blockingStub, deadlineSeconds, TimeUnit.SECONDS);
            Class<?> requestType = Class.forName(systemInfoRequestClassName());
            Object request = requestType.getMethod("getDefaultInstance").invoke(null);
            deadlineStub.getClass().getMethod("getSystemInfo", requestType).invoke(deadlineStub, request);
            Logger.debug(false, "Tempus", "Workflow service stubs health check completed: healthy=true");
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Throwable rootCause = ExceptionKit.getRootCause(e);
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Workflow service stubs health check failed: stubsType={}, exception={}, rootException={}",
                    handle.getClass().getName(),
                    e.getClass().getSimpleName(),
                    rootCause.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Shuts down the specified transport handle.
     *
     * @param handle opaque transport handle
     */
    @Override
    public void shutdown(Object handle) {
        if (handle == null) {
            return;
        }

        Logger.debug(
                true,
                "Tempus",
                "Workflow service stubs shutdown started: stubsType={}",
                handle.getClass().getName());

        try {
            invokeNoArgIfPresent(handle, "shutdown");
            boolean terminated = invokeAwaitTerminationIfPresent(handle, 5L, TimeUnit.SECONDS);
            if (!terminated) {
                Logger.warn(
                        false,
                        "Tempus",
                        "Workflow service stubs shutdown timeout: stubsType={}, forceShutdown=true",
                        handle.getClass().getName());
                invokeNoArgIfPresent(handle, "shutdownNow");
                invokeAwaitTerminationIfPresent(handle, 2L, TimeUnit.SECONDS);
            }

            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow service stubs shutdown completed: stubsType={}",
                    handle.getClass().getName());
        } catch (RuntimeException e) {
            if (ExceptionKit.isCausedBy(e, InterruptedException.class)) {
                Thread.currentThread().interrupt();
            }
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow service stubs shutdown failed: stubsType={}, exception={}",
                    handle.getClass().getName(),
                    e.getClass().getSimpleName());
            throw new IllegalStateException("Failed to shut down WorkflowServiceStubs", e);
        }
    }

    /**
     * Creates a workflow client from a transport handle without binding-level options.
     *
     * @param handle opaque transport handle
     * @return workflow client
     */
    private WorkflowClient clientWithoutBinding(Object handle) {
        Assert.notNull(handle, "handle must not be null");
        Logger.debug(
                true,
                "Tempus",
                "Workflow client creation from service stubs started: stubsType={}",
                handle.getClass().getName());

        Method factoryMethod = findWorkflowClientFactory(handle.getClass());
        WorkflowClient client = MethodKit.invokeStatic(factoryMethod, handle);
        Logger.debug(
                false,
                "Tempus",
                "Workflow client creation from service stubs completed: stubsType={}",
                handle.getClass().getName());
        return client;
    }

    /**
     * Finds a compatible {@link WorkflowClient#newInstance} factory method for the specified service stubs type.
     *
     * @param serviceStubsType runtime service stubs type
     * @return matched factory method
     */
    private static Method findWorkflowClientFactory(Class<?> serviceStubsType) {
        for (Method candidate : MethodKit.getPublicMethods(WorkflowClient.class)) {
            if (!"newInstance".equals(candidate.getName()) || candidate.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = candidate.getParameterTypes()[0];
            if (parameterType.isAssignableFrom(serviceStubsType)) {
                Logger.debug(
                        false,
                        "Tempus",
                        "Workflow client factory lookup completed: stubsType={}, mode=singleArg",
                        serviceStubsType.getName());
                return candidate;
            }
        }
        Logger.warn(
                false,
                "Tempus",
                "Workflow client factory lookup failed: stubsType={}, mode=singleArg",
                serviceStubsType.getName());
        throw new IllegalStateException("No WorkflowClient.newInstance method accepts " + serviceStubsType.getName());
    }

    /**
     * Finds a compatible two-argument {@link WorkflowClient#newInstance} factory method for the specified service stubs
     * type.
     *
     * @param serviceStubsType runtime service stubs type
     * @return matched factory method
     */
    private static Method findWorkflowClientFactoryWithOptions(Class<?> serviceStubsType) {
        for (Method candidate : MethodKit.getPublicMethods(WorkflowClient.class)) {
            if (!"newInstance".equals(candidate.getName()) || candidate.getParameterCount() != 2) {
                continue;
            }
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes[0].isAssignableFrom(serviceStubsType)
                    && WorkflowClientOptions.class.isAssignableFrom(parameterTypes[1])) {
                Logger.debug(
                        false,
                        "Tempus",
                        "Workflow client factory lookup completed: stubsType={}, mode=withOptions",
                        serviceStubsType.getName());
                return candidate;
            }
        }
        Logger.warn(
                false,
                "Tempus",
                "Workflow client factory lookup failed: stubsType={}, mode=withOptions",
                serviceStubsType.getName());
        throw new IllegalStateException(
                "No WorkflowClient.newInstance(stubs, options) method accepts " + serviceStubsType.getName());
    }

    /**
     * Creates a Temporal service stubs options builder through reflection.
     *
     * @return service stubs options builder
     */
    private static Object newServiceStubsOptionsBuilder() {
        try {
            Class<?> optionsType = Class.forName(WORKFLOW_SERVICE_STUBS_OPTIONS_CLASS);
            return MethodKit.invokeStatic(optionsType.getMethod("newBuilder"));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Temporal WorkflowServiceStubsOptions is not available", e);
        }
    }

    /**
     * Creates Temporal service stubs through reflection.
     *
     * @param options service stubs options
     * @return service stubs handle
     */
    private static Object newServiceStubs(Object options) {
        try {
            Class<?> serviceStubsType = Class.forName(WORKFLOW_SERVICE_STUBS_CLASS);
            return MethodKit.invokeStatic(serviceStubsType.getMethod("newServiceStubs", options.getClass()), options);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Temporal WorkflowServiceStubs is not available", e);
        }
    }

    /**
     * Validates and casts a service stubs handle.
     *
     * @param serviceStubs service stubs handle
     */
    private static void requireWorkflowServiceStubs(Object serviceStubs) {
        try {
            Class<?> serviceStubsType = Class.forName(WORKFLOW_SERVICE_STUBS_CLASS);
            Assert.isTrue(
                    serviceStubsType.isInstance(serviceStubs),
                    "serviceStubs must be a WorkflowServiceStubs instance");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Temporal WorkflowServiceStubs is not available", e);
        }
    }

    /**
     * Returns the shutdown transport state name.
     *
     * @return shutdown transport state name
     */
    private static String shutdownTransportState() {
        return WorkflowTransportState.SHUTDOWN.value();
    }

    /**
     * Returns the unknown transport state name.
     *
     * @return unknown transport state name
     */
    private static String unknownTransportState() {
        return WorkflowTransportState.UNKNOWN.value();
    }

    /**
     * Returns the Temporal system information request class name.
     *
     * @return Temporal system information request class name
     */
    private static String systemInfoRequestClassName() {
        return "io.temporal.api.workflowservice.v1.GetSystemInfoRequest";
    }

    /**
     * Invokes a no-argument method when it exists on the target object.
     *
     * @param target     target object
     * @param methodName method name
     */
    private static void invokeNoArgIfPresent(Object target, String methodName) {
        invokeIfPresent(target, methodName, new Class<?>[0]);
    }

    /**
     * Invokes a method when it exists on the target object and treats a missing method as an optional capability.
     *
     * @param target         target object
     * @param methodName     method name
     * @param parameterTypes method parameter types
     * @param args           invocation arguments
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
     * Waits for termination when the service stubs implementation exposes {@code awaitTermination(long, TimeUnit)}.
     *
     * @param target  target object
     * @param timeout timeout value
     * @param unit    timeout unit
     * @return {@code true} when the method is absent or termination succeeds, otherwise {@code false}
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
