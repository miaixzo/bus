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
package org.miaixz.bus.metrics.builtin;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Records framework-neutral application startup metrics through a Bus metrics provider.
 *
 * @author Kimi Liu
 */
public class StartupMetrics {

    /**
     * Total application startup duration metric.
     */
    public static final String STARTUP_DURATION = "application.startup.duration";
    /**
     * Application startup count metric.
     */
    public static final String STARTUP_COUNT = "application.startup.count";
    /**
     * Application startup-stage duration metric.
     */
    public static final String STARTUP_STAGE_DURATION = "application.startup.stage.duration";

    /**
     * Constructs a new StartupMetrics instance.
     */
    public StartupMetrics() {
        // No initialization required.
    }

    /**
     * Records one startup through the active provider.
     *
     * @param durationMillis total startup duration in milliseconds
     * @param stages         completed startup stages
     */
    public static void record(long durationMillis, List<StartupStage> stages) {
        record(Metrics.getProvider(), durationMillis, stages);
    }

    /**
     * Records one startup through an explicitly selected provider.
     *
     * @param provider       metrics provider
     * @param durationMillis total startup duration in milliseconds
     * @param stages         completed startup stages
     */
    public static void record(Provider provider, long durationMillis, List<StartupStage> stages) {
        Objects.requireNonNull(provider, "provider");
        if (durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must not be negative");
        }
        List<StartupStage> startupStages = List.copyOf(stages == null ? List.of() : stages);
        provider.counter(STARTUP_COUNT).increment();
        provider.timer(STARTUP_DURATION).record(durationMillis, TimeUnit.MILLISECONDS);
        for (StartupStage stage : startupStages) {
            provider.timer(STARTUP_STAGE_DURATION, Tag.of("stage", stage.name()))
                    .record(stage.durationMillis(), TimeUnit.MILLISECONDS);
        }
    }

}
