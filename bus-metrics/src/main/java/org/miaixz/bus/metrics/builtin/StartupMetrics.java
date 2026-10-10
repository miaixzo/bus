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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

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
     * Startup stage name attribute.
     */
    private static final AttributeDescriptor<String> STAGE = AttributeDescriptor.string("stage");

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
     * @throws IllegalArgumentException if the total startup duration is negative
     */
    public static void record(Provider provider, long durationMillis, List<StartupStage> stages) {
        Objects.requireNonNull(provider, "provider");
        if (durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must not be negative");
        }
        List<StartupStage> startupStages = List.copyOf(stages == null ? List.of() : stages);
        Map<String, StartupStage> uniqueStages = new LinkedHashMap<>();
        startupStages.forEach(stage -> uniqueStages.putIfAbsent(stage.name(), stage));
        Attributes empty = Attributes.empty();
        provider.counter(
                descriptor(STARTUP_COUNT, InstrumentKind.COUNTER, NumberKind.LONG, "{startup}", List.of()),
                empty).increment();
        provider.timer(descriptor(STARTUP_DURATION, InstrumentKind.TIMER, NumberKind.DOUBLE, "s", List.of()), empty)
                .record(durationMillis, TimeUnit.MILLISECONDS);
        for (StartupStage stage : uniqueStages.values()) {
            Attributes attributes = Attributes.of(STAGE, stage.name());
            provider.timer(
                    descriptor(STARTUP_STAGE_DURATION, InstrumentKind.TIMER, NumberKind.DOUBLE, "s", List.of(STAGE)),
                    attributes).record(stage.durationMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Creates an application startup metric descriptor.
     *
     * @param name       metric name
     * @param kind       instrument kind
     * @param number     numeric kind
     * @param unit       metric unit
     * @param attributes ordered attribute schema
     * @return metric descriptor
     */
    private static MetricDescriptor descriptor(
            String name,
            InstrumentKind kind,
            NumberKind number,
            String unit,
            List<AttributeDescriptor<?>> attributes) {
        return MetricDescriptor.of(name, kind, number, unit, "", attributes);
    }

}
