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
package org.miaixz.bus.starter.tempus;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowBindingOptions;

/**
 * Temporal framework configuration properties.
 * <p>
 * The {@code enabled} field is a Spring Boot starter lifecycle switch. Temporal connection, target, workflow, activity,
 * retry, worker, and recovery options are inherited from {@link WorkflowBindingOptions}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.TEMPUS)
public class TempusProperties extends WorkflowBindingOptions {

    /**
     * Whether the Temporal worker is enabled.
     */
    private boolean enabled = false;

    /**
     * Creates Temporal configuration properties.
     */
    public TempusProperties() {
        super();
    }

    /**
     * Validates connection, binding, timeout, retry, and worker bounds after binding.
     */
    public void validate() {
        if (blank(getEndpoint()) || blank(getNamespace()) || blank(getTaskQueue())) {
            throw new IllegalArgumentException("Enabled bus.tempus requires endpoint, namespace and task-queue");
        }
        Runtime runtime = getRuntime();
        if (runtime.getWorkflow().getExecutionTimeoutDays() <= 0 || runtime.getWorkflow().getRunTimeoutHours() <= 0
                || runtime.getWorkflow().getTaskTimeoutMinutes() <= 0
                || runtime.getActivity().getStartToCloseHours() <= 0
                || runtime.getActivity().getScheduleToStartMinutes() <= 0
                || runtime.getActivity().getHeartbeatTimeoutSeconds() <= 0
                || runtime.getRetry().getInitialIntervalSeconds() <= 0
                || runtime.getRetry().getMaxIntervalSeconds() <= 0 || runtime.getRetry().getMaxAttempts() <= 0
                || runtime.getRetry().getBackoffCoefficient() <= 0D || runtime.getWorker().getMaxConcurrent() <= 0
                || runtime.getWorker().getMaxWorkflowTaskPollers() <= 0
                || runtime.getWorker().getMaxActivityTaskPollers() <= 0
                || runtime.getWorker().getWorkflowCacheSize() <= 0
                || runtime.getWorker().getMaxWorkflowThreadCount() <= 0
                || runtime.getRecovery().getHealthIntervalSeconds() <= 0
                || runtime.getRecovery().getHealthFailureThreshold() <= 0
                || runtime.getRecovery().getReconnectInitialBackoffSeconds() <= 0
                || runtime.getRecovery().getReconnectMaxBackoffSeconds() <= 0
                || runtime.getRecovery().getHealthProbeTimeoutSeconds() <= 0
                || runtime.getRecovery().getShutdownAwaitSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "bus.tempus timeouts, retry limits and worker capacities must be positive");
        }
    }

    /**
     * Returns whether a configuration string is absent or blank.
     *
     * @param value configuration string
     * @return {@code true} when the string is absent or blank
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * @return masked diagnostic representation
     */
    @Override
    public String toString() {
        return "TempusProperties[enabled=" + enabled + ", endpoint=" + getEndpoint() + ", namespace=" + getNamespace()
                + ", taskQueue=" + getTaskQueue() + ", credentials=***]";
    }

}
