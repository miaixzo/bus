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
package org.miaixz.bus.metrics.bridge;

import java.util.concurrent.TimeUnit;

import org.miaixz.bus.metrics.Metrics;

/**
 * Helper for recording bus-tempus job execution metrics. Wrap {@code AbstractActivityHandler.execute()} calls to get
 * job SLA tracking.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TempusMetrics {

    /** Private constructor; this is a static utility class. */
    private TempusMetrics() {

    }

    /**
     * Record a completed job execution.
     *
     * @param jobName    logical job name (used as tag)
     * @param durationMs measured execution duration in milliseconds
     * @param success    true if job completed normally
     */
    public static void recordExecution(String jobName, long durationMs, boolean success) {
        Metrics.timer("tempus.job.duration", "job", jobName).record(durationMs, TimeUnit.MILLISECONDS);
        Metrics.meter("tempus.job.executions", "job", jobName, "result", success ? "success" : "fail").increment();
    }

    /**
     * Convenience wrapper that times an execution block.
     *
     * @param jobName the job name
     * @param action  the action to run and measure
     */
    public static void timed(String jobName, Runnable action) {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            action.run();
            success = true;
        } finally {
            recordExecution(jobName, System.currentTimeMillis() - start, success);
        }
    }

}
