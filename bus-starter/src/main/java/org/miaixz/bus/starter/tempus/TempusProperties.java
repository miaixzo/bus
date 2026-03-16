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

import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Temporal framework-level configuration properties.
 * <p>
 * Binds to {@code bus.tempus.*} configuration keys, covering connection, worker concurrency, workflow/activity timeouts
 * and retry parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.TEMPUS)
public class TempusProperties {

    /**
     * Whether to enable the Temporal worker.
     */
    private boolean enabled = false;

    /**
     * Temporal server address (host:port).
     */
    private String endpoint;

    /**
     * Temporal namespace.
     */
    private String namespace;

    /**
     * Temporal client identity (optional).
     */
    private String identity;

    /**
     * Temporal task queue name.
     */
    private String taskQueue;

    /**
     * Workflow type name.
     */
    private String workflowType;

    /**
     * Maximum worker concurrency.
     */
    private int maxConcurrent = 4;

    // -------------------------------------------------------------------------
    // Workflow timeouts
    // -------------------------------------------------------------------------

    /**
     * Workflow task timeout in minutes. Maps to setWorkflowTaskTimeout.
     */
    private int workflowTaskTimeoutMinutes = 6;

    // -------------------------------------------------------------------------
    // Activity timeouts
    // -------------------------------------------------------------------------

    /**
     * Maximum duration from activity start to close, in hours. Maps to setStartToCloseTimeout.
     */
    private int activityStartToCloseHours = 12;

    /**
     * Maximum wait time from activity schedule to start, in minutes. Maps to setScheduleToStartTimeout.
     */
    private int activityScheduleToStartMinutes = 6;

    /**
     * Activity heartbeat timeout in seconds. Maps to setHeartbeatTimeout.
     */
    private int activityHeartbeatTimeoutSeconds = 60;

    // -------------------------------------------------------------------------
    // Activity retry
    // -------------------------------------------------------------------------

    /**
     * Initial retry interval in seconds. Maps to setInitialInterval.
     */
    private int activityRetryInitialIntervalSeconds = 180;

    /**
     * Maximum retry interval in seconds. Maps to setMaximumInterval.
     */
    private int activityRetryMaxIntervalSeconds = 600;

    /**
     * Retry backoff coefficient. Maps to setBackoffCoefficient.
     */
    private double activityRetryBackoffCoefficient = 2.0;

    /**
     * Maximum number of retry attempts. Maps to setMaximumAttempts.
     */
    private int activityRetryMaxAttempts = 3;

}
