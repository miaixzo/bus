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

import lombok.Getter;
import lombok.Setter;

/**
 * Unified Temporal workflow binding and runtime options.
 * <p>
 * This class is not a direct model of {@link io.temporal.client.WorkflowOptions}. It is the shared options object used
 * to bind Temporal connection, workflow publication target, subscriber worker runtime, activity retry, health check,
 * reconnect, and shutdown parameters.
 * <p>
 * The root object stores only grouped sections. Compatibility accessors are kept to protect existing publisher and
 * subscriber contracts while delegating all values to the grouped sections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class WorkflowBindingOptions {

    /**
     * Temporal connection options.
     */
    private Connection connection = new Connection();

    /**
     * Temporal workflow binding options.
     */
    private Binding binding = new Binding();

    /**
     * Temporal runtime options.
     */
    private Runtime runtime = new Runtime();

    /**
     * Creates workflow binding options with default section values.
     */
    public WorkflowBindingOptions() {
        ensureSections();
    }

    /**
     * Creates default workflow binding options.
     *
     * @return default workflow binding options
     */
    public static WorkflowBindingOptions defaults() {
        return new WorkflowBindingOptions();
    }

    /**
     * Creates workflow binding options with workflow target fields.
     *
     * @param taskQueue    task queue name
     * @param workflowType workflow type name
     * @return workflow binding options
     */
    public static WorkflowBindingOptions of(String taskQueue, String workflowType) {
        WorkflowBindingOptions options = defaults();
        options.getBinding().setTaskQueue(taskQueue);
        options.getBinding().setWorkflowType(workflowType);
        return options;
    }

    /**
     * Returns the Temporal connection section.
     *
     * @return Temporal connection section
     */
    public Connection getConnection() {
        ensureSections();
        return connection;
    }

    /**
     * Returns the Temporal workflow binding section.
     *
     * @return Temporal workflow binding section
     */
    public Binding getBinding() {
        ensureSections();
        return binding;
    }

    /**
     * Returns the Temporal runtime section.
     *
     * @return Temporal runtime section
     */
    public Runtime getRuntime() {
        ensureSections();
        return runtime;
    }

    /**
     * Returns the Temporal service endpoint.
     *
     * @return Temporal service endpoint
     */
    public String getEndpoint() {
        return getConnection().getEndpoint();
    }

    /**
     * Updates the Temporal service endpoint.
     *
     * @param endpoint Temporal service endpoint
     */
    public void setEndpoint(String endpoint) {
        getConnection().setEndpoint(endpoint);
    }

    /**
     * Returns the Temporal namespace.
     *
     * @return Temporal namespace
     */
    public String getNamespace() {
        return getConnection().getNamespace();
    }

    /**
     * Updates the Temporal namespace.
     *
     * @param namespace Temporal namespace
     */
    public void setNamespace(String namespace) {
        getConnection().setNamespace(namespace);
    }

    /**
     * Returns the Temporal client identity.
     *
     * @return Temporal client identity
     */
    public String getIdentity() {
        return getConnection().getIdentity();
    }

    /**
     * Updates the Temporal client identity.
     *
     * @param identity Temporal client identity
     */
    public void setIdentity(String identity) {
        getConnection().setIdentity(identity);
    }

    /**
     * Returns the Temporal task queue.
     *
     * @return Temporal task queue
     */
    public String getTaskQueue() {
        return getBinding().getTaskQueue();
    }

    /**
     * Updates the Temporal task queue.
     *
     * @param taskQueue Temporal task queue
     */
    public void setTaskQueue(String taskQueue) {
        getBinding().setTaskQueue(taskQueue);
    }

    /**
     * Returns the Temporal workflow type.
     *
     * @return Temporal workflow type
     */
    public String getWorkflowType() {
        return getBinding().getWorkflowType();
    }

    /**
     * Updates the Temporal workflow type.
     *
     * @param workflowType Temporal workflow type
     */
    public void setWorkflowType(String workflowType) {
        getBinding().setWorkflowType(workflowType);
    }

    /**
     * Returns the explicit Temporal workflow identifier.
     *
     * @return explicit Temporal workflow identifier
     */
    public String getWorkflowId() {
        return getBinding().getWorkflowId();
    }

    /**
     * Updates the explicit Temporal workflow identifier.
     *
     * @param workflowId explicit Temporal workflow identifier
     */
    public void setWorkflowId(String workflowId) {
        getBinding().setWorkflowId(workflowId);
    }

    /**
     * Returns the stable key used to generate deterministic workflow identifiers.
     *
     * @return stable workflow key
     */
    public String getStableKey() {
        return getBinding().getStableKey();
    }

    /**
     * Updates the stable key used to generate deterministic workflow identifiers.
     *
     * @param stableKey stable workflow key
     */
    public void setStableKey(String stableKey) {
        getBinding().setStableKey(stableKey);
    }

    /**
     * Returns configured maximum worker concurrency.
     *
     * @return configured maximum worker concurrency
     */
    public int getMaxConcurrent() {
        return getRuntime().getWorker().getMaxConcurrent();
    }

    /**
     * Updates maximum worker concurrency.
     *
     * @param maxConcurrent maximum worker concurrency
     */
    public void setMaxConcurrent(int maxConcurrent) {
        getRuntime().getWorker().setMaxConcurrent(maxConcurrent);
    }

    /**
     * Resolves maximum worker concurrency.
     *
     * @return effective maximum worker concurrency
     */
    public int resolveMaxConcurrent() {
        int value = getRuntime().getWorker().getMaxConcurrent();
        return value <= 0 ? 4 : value;
    }

    /**
     * Resolves workflow execution timeout days.
     *
     * @return effective workflow execution timeout days
     */
    public int resolveWorkflowExecutionTimeoutDays() {
        int value = getRuntime().getWorkflow().getExecutionTimeoutDays();
        return value <= 0 ? 1 : value;
    }

    /**
     * Resolves workflow run timeout hours.
     *
     * @return effective workflow run timeout hours
     */
    public int resolveWorkflowRunTimeoutHours() {
        int value = getRuntime().getWorkflow().getRunTimeoutHours();
        return value <= 0 ? 12 : value;
    }

    /**
     * Resolves workflow task timeout minutes.
     *
     * @return effective workflow task timeout minutes
     */
    public int resolveWorkflowTaskTimeoutMinutes() {
        int value = getRuntime().getWorkflow().getTaskTimeoutMinutes();
        return value <= 0 ? 6 : value;
    }

    /**
     * Resolves activity start-to-close timeout hours.
     *
     * @return effective timeout hours
     */
    public int resolveActivityStartToCloseHours() {
        int value = getRuntime().getActivity().getStartToCloseHours();
        return value <= 0 ? 12 : value;
    }

    /**
     * Resolves activity schedule-to-start timeout minutes.
     *
     * @return effective timeout minutes
     */
    public int resolveActivityScheduleToStartMinutes() {
        int value = getRuntime().getActivity().getScheduleToStartMinutes();
        return value <= 0 ? 6 : value;
    }

    /**
     * Resolves activity heartbeat timeout seconds.
     *
     * @return effective heartbeat timeout seconds
     */
    public int resolveActivityHeartbeatTimeoutSeconds() {
        int value = getRuntime().getActivity().getHeartbeatTimeoutSeconds();
        return value <= 0 ? 300 : value;
    }

    /**
     * Resolves activity retry initial interval seconds.
     *
     * @return effective initial interval seconds
     */
    public int resolveActivityRetryInitialIntervalSeconds() {
        int value = getRuntime().getRetry().getInitialIntervalSeconds();
        return value <= 0 ? 180 : value;
    }

    /**
     * Resolves activity retry maximum interval seconds.
     *
     * @return effective maximum interval seconds
     */
    public int resolveActivityRetryMaxIntervalSeconds() {
        int value = getRuntime().getRetry().getMaxIntervalSeconds();
        return value <= 0 ? 600 : value;
    }

    /**
     * Resolves activity retry backoff coefficient.
     *
     * @return effective backoff coefficient
     */
    public double resolveActivityRetryBackoffCoefficient() {
        double value = getRuntime().getRetry().getBackoffCoefficient();
        return value <= 0D ? 2.0D : value;
    }

    /**
     * Resolves activity retry maximum attempts.
     *
     * @return effective maximum attempts
     */
    public int resolveActivityRetryMaxAttempts() {
        int value = getRuntime().getRetry().getMaxAttempts();
        return value <= 0 ? 3 : value;
    }

    /**
     * Resolves per-worker activity start rate.
     *
     * @return non-negative rate where zero means unlimited
     */
    public double resolveMaxWorkerActivitiesPerSecond() {
        double value = getRuntime().getWorker().getMaxWorkerActivitiesPerSecond();
        return value < 0D ? 0D : value;
    }

    /**
     * Resolves task-queue activity start rate.
     *
     * @return non-negative rate where zero means unlimited
     */
    public double resolveMaxTaskQueueActivitiesPerSecond() {
        double value = getRuntime().getWorker().getMaxTaskQueueActivitiesPerSecond();
        return value < 0D ? 0D : value;
    }

    /**
     * Resolves workflow task poller count.
     *
     * @return effective workflow task poller count
     */
    public int resolveMaxWorkflowTaskPollers() {
        int value = getRuntime().getWorker().getMaxWorkflowTaskPollers();
        return value <= 0 ? 2 : value;
    }

    /**
     * Resolves activity task poller count.
     *
     * @return effective activity task poller count
     */
    public int resolveMaxActivityTaskPollers() {
        int value = getRuntime().getWorker().getMaxActivityTaskPollers();
        return value <= 0 ? 2 : value;
    }

    /**
     * Resolves workflow cache size.
     *
     * @return effective workflow cache size
     */
    public int resolveWorkflowCacheSize() {
        int value = getRuntime().getWorker().getWorkflowCacheSize();
        return value <= 0 ? 128 : value;
    }

    /**
     * Resolves maximum workflow thread count.
     *
     * @return effective maximum workflow thread count
     */
    public int resolveMaxWorkflowThreadCount() {
        int value = getRuntime().getWorker().getMaxWorkflowThreadCount();
        return value <= 0 ? 128 : value;
    }

    /**
     * Returns whether automatic worker reconnect is enabled.
     *
     * @return {@code true} when automatic worker reconnect is enabled
     */
    public boolean isWorkerReconnectEnabled() {
        return getRuntime().getRecovery().isReconnectEnabled();
    }

    /**
     * Updates whether automatic worker reconnect is enabled.
     *
     * @param workerReconnectEnabled automatic worker reconnect switch
     */
    public void setWorkerReconnectEnabled(boolean workerReconnectEnabled) {
        getRuntime().getRecovery().setReconnectEnabled(workerReconnectEnabled);
    }

    /**
     * Resolves worker health check interval seconds.
     *
     * @return effective health interval seconds
     */
    public int resolveWorkerHealthIntervalSeconds() {
        int value = getRuntime().getRecovery().getHealthIntervalSeconds();
        return value <= 0 ? 30 : value;
    }

    /**
     * Resolves worker health failure threshold.
     *
     * @return effective failure threshold
     */
    public int resolveWorkerHealthFailureThreshold() {
        int value = getRuntime().getRecovery().getHealthFailureThreshold();
        return value <= 0 ? 3 : value;
    }

    /**
     * Resolves initial reconnect backoff seconds.
     *
     * @return effective initial backoff seconds
     */
    public int resolveWorkerReconnectInitialBackoffSeconds() {
        int value = getRuntime().getRecovery().getReconnectInitialBackoffSeconds();
        return value <= 0 ? 5 : value;
    }

    /**
     * Resolves maximum reconnect backoff seconds.
     *
     * @return effective maximum backoff seconds
     */
    public int resolveWorkerReconnectMaxBackoffSeconds() {
        int value = getRuntime().getRecovery().getReconnectMaxBackoffSeconds();
        return value <= 0 ? 60 : value;
    }

    /**
     * Resolves worker health probe timeout seconds.
     *
     * @return effective probe timeout seconds
     */
    public int resolveWorkerHealthProbeTimeoutSeconds() {
        int value = getRuntime().getRecovery().getHealthProbeTimeoutSeconds();
        return value <= 0 ? 5 : value;
    }

    /**
     * Resolves shutdown wait seconds.
     *
     * @return effective shutdown wait seconds
     */
    public int resolveShutdownAwaitSeconds() {
        int value = getRuntime().getRecovery().getShutdownAwaitSeconds();
        return value <= 0 ? 10 : value;
    }

    /**
     * Ensures all option sections are non-null.
     */
    private void ensureSections() {
        if (connection == null) {
            connection = new Connection();
        }
        if (binding == null) {
            binding = new Binding();
        }
        if (runtime == null) {
            runtime = new Runtime();
        }
        runtime.ensureSections();
    }

    /**
     * Temporal connection options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Connection {

        /**
         * Temporal service endpoint.
         */
        String endpoint;

        /**
         * Temporal namespace.
         */
        String namespace;

        /**
         * Temporal client identity.
         */
        String identity;

        /**
         * Creates connection options.
         */
        public Connection() {
            // No initialization required.
        }

    }

    /**
     * Temporal workflow target binding options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Binding {

        /**
         * Temporal task queue name.
         */
        String taskQueue;

        /**
         * Temporal workflow type name.
         */
        String workflowType;

        /**
         * Explicit workflow identifier.
         */
        String workflowId;

        /**
         * Stable key used to generate deterministic workflow identifiers.
         */
        String stableKey;

        /**
         * Creates workflow target binding options.
         */
        public Binding() {
            // No initialization required.
        }

    }

    /**
     * Temporal runtime options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Runtime {

        /**
         * Workflow timeout options.
         */
        Workflow workflow = new Workflow();

        /**
         * Activity timeout options.
         */
        Activity activity = new Activity();

        /**
         * Activity retry options.
         */
        Retry retry = new Retry();

        /**
         * Worker capacity options.
         */
        Worker worker = new Worker();

        /**
         * Worker recovery options.
         */
        Recovery recovery = new Recovery();

        /**
         * Creates runtime options.
         */
        public Runtime() {
            ensureSections();
        }

        /**
         * Ensures all runtime subsections are non-null.
         */
        private void ensureSections() {
            if (workflow == null) {
                workflow = new Workflow();
            }
            if (activity == null) {
                activity = new Activity();
            }
            if (retry == null) {
                retry = new Retry();
            }
            if (worker == null) {
                worker = new Worker();
            }
            if (recovery == null) {
                recovery = new Recovery();
            }
        }

    }

    /**
     * Workflow timeout options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Workflow {

        /**
         * Workflow execution timeout in days.
         */
        int executionTimeoutDays = 1;

        /**
         * Workflow run timeout in hours.
         */
        int runTimeoutHours = 12;

        /**
         * Workflow task timeout in minutes.
         */
        int taskTimeoutMinutes = 6;

        /**
         * Creates workflow timeout options.
         */
        public Workflow() {
            // No initialization required.
        }

    }

    /**
     * Activity timeout options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Activity {

        /**
         * Activity start-to-close timeout in hours.
         */
        int startToCloseHours = 12;

        /**
         * Activity schedule-to-start timeout in minutes.
         */
        int scheduleToStartMinutes = 6;

        /**
         * Activity heartbeat timeout in seconds.
         */
        int heartbeatTimeoutSeconds = 300;

        /**
         * Creates activity timeout options.
         */
        public Activity() {
            // No initialization required.
        }

    }

    /**
     * Activity retry options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Retry {

        /**
         * Activity initial retry interval in seconds.
         */
        int initialIntervalSeconds = 180;

        /**
         * Activity maximum retry interval in seconds.
         */
        int maxIntervalSeconds = 600;

        /**
         * Activity retry backoff coefficient.
         */
        double backoffCoefficient = 2.0D;

        /**
         * Activity maximum retry attempts.
         */
        int maxAttempts = 3;

        /**
         * Creates activity retry options.
         */
        public Retry() {
            // No initialization required.
        }

    }

    /**
     * Worker capacity options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Worker {

        /**
         * Maximum worker concurrency.
         */
        int maxConcurrent = 4;

        /**
         * Per-worker activity start rate.
         */
        double maxWorkerActivitiesPerSecond = 0D;

        /**
         * Task-queue activity start rate.
         */
        double maxTaskQueueActivitiesPerSecond = 0D;

        /**
         * Workflow task poller count.
         */
        int maxWorkflowTaskPollers = 2;

        /**
         * Activity task poller count.
         */
        int maxActivityTaskPollers = 2;

        /**
         * Workflow cache size.
         */
        int workflowCacheSize = 128;

        /**
         * Maximum workflow thread count.
         */
        int maxWorkflowThreadCount = 128;

        /**
         * Creates worker capacity options.
         */
        public Worker() {
            // No initialization required.
        }

    }

    /**
     * Worker recovery options.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class Recovery {

        /**
         * Whether automatic worker reconnect is enabled.
         */
        boolean reconnectEnabled = true;

        /**
         * Worker health check interval in seconds.
         */
        int healthIntervalSeconds = 30;

        /**
         * Consecutive worker health check failure threshold.
         */
        int healthFailureThreshold = 3;

        /**
         * Initial reconnect backoff in seconds.
         */
        int reconnectInitialBackoffSeconds = 5;

        /**
         * Maximum reconnect backoff in seconds.
         */
        int reconnectMaxBackoffSeconds = 60;

        /**
         * Worker health probe timeout in seconds.
         */
        int healthProbeTimeoutSeconds = 5;

        /**
         * Worker shutdown wait time in seconds.
         */
        int shutdownAwaitSeconds = 10;

        /**
         * Creates worker recovery options.
         */
        public Recovery() {
            // No initialization required.
        }

    }

}
