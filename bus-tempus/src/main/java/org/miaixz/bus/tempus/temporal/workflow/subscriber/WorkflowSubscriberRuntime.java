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
package org.miaixz.bus.tempus.temporal.workflow.subscriber;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.core.lang.EnumValue;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Temporal worker subscriber runtime state.
 * <p>
 * This object only stores Temporal runtime resources and lifecycle state. It does not hold business parsing objects.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class WorkflowSubscriberRuntime {

    /**
     * Creates a worker subscriber runtime state.
     */
    public WorkflowSubscriberRuntime() {
        // No initialization required.
    }

    /**
     * Temporal workflow transport handle.
     */
    private Object transportHandle;

    /**
     * Temporal workflow client.
     */
    private WorkflowClient workflowClient;

    /**
     * Temporal worker factory.
     */
    private WorkerFactory workerFactory;

    /**
     * Temporal worker.
     */
    private Worker worker;

    /**
     * Lifecycle state.
     */
    private EnumValue.Lifecycle state = EnumValue.Lifecycle.UNKNOWN;

    /**
     * Startup time.
     */
    private Instant startedAt;

    /**
     * Last healthy time.
     */
    private Instant lastHealthyAt;

    /**
     * Last failure time.
     */
    private Instant lastFailureAt;

    /**
     * Consecutive failure count.
     */
    private int consecutiveFailures;

    /**
     * Reconnect attempt count.
     */
    private int reconnectAttempts;

    /**
     * Whether reconnect has been scheduled.
     */
    private boolean reconnectScheduled;

    /**
     * Marks the runtime as starting.
     */
    public void markStarting() {
        state = EnumValue.Lifecycle.STARTING;
        startedAt = Instant.now();
    }

    /**
     * Marks the runtime as running.
     */
    public void markRunning() {
        state = EnumValue.Lifecycle.RUNNING;
        consecutiveFailures = 0;
        reconnectScheduled = false;
        lastHealthyAt = Instant.now();
    }

    /**
     * Marks the runtime as failed and increments the consecutive failure counter.
     *
     * @param cause failure cause reported by the caller
     */
    public void markFailure(Throwable cause) {
        state = EnumValue.Lifecycle.ERROR;
        consecutiveFailures++;
        lastFailureAt = Instant.now();
    }

    /**
     * Marks reconnect as scheduled.
     */
    public void markReconnectScheduled() {
        reconnectScheduled = true;
        reconnectAttempts++;
    }

    /**
     * Marks the runtime as stopping.
     */
    public void markStopping() {
        state = EnumValue.Lifecycle.STOPPING;
    }

    /**
     * Marks the runtime as stopped.
     */
    public void markStopped() {
        state = EnumValue.Lifecycle.STOPPED;
        reconnectScheduled = false;
    }

    /**
     * Checks whether the runtime is still active.
     *
     * @return {@code true} when the runtime is active
     */
    public boolean isRunning() {
        return state == EnumValue.Lifecycle.RUNNING && workerFactory != null && !workerFactory.isShutdown();
    }

}
