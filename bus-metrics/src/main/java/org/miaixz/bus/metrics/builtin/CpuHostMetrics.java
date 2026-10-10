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

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.bridge.CpuSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds CPU topology and cumulative per-logical-CPU values.
 *
 * @author Kimi Liu
 */
public final class CpuHostMetrics implements MetricBinder {

    /**
     * Attribute identifying a logical processor.
     */
    private static final AttributeDescriptor<Long> LOGICAL_NUMBER = AttributeDescriptor
            .longValue("cpu.logical_number", true);
    /**
     * Attribute identifying a CPU time mode.
     */
    private static final AttributeDescriptor<String> MODE = AttributeDescriptor.string("cpu.mode");

    /**
     * Cached source for CPU topology and counters.
     */
    private final SnapshotCache<CpuSnapshot> cache;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a CPU binder.
     *
     * @param cache CPU snapshot cache
     */
    public CpuHostMetrics(SnapshotCache<CpuSnapshot> cache) {
        this.cache = Objects.requireNonNull(cache, "CPU snapshot cache must not be null");
    }

    /**
     * Creates a descriptor in the Bus instrumentation scope.
     *
     * @param name       metric name
     * @param kind       instrument kind
     * @param numberKind numeric representation
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

    @Override
    public synchronized void bind(Provider provider) {
        if (registrations != null) {
            throw new IllegalStateException("CPU host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.cpu.physical.count",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "{cpu}",
                                    List.of()),
                            measurement -> cache.get().ifPresent(
                                    snapshot -> measurement.recordLong(snapshot.physicalCount(), Attributes.empty()))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.cpu.logical.count",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "{cpu}",
                                    List.of()),
                            measurement -> cache.get().ifPresent(
                                    snapshot -> measurement.recordLong(snapshot.logicalCount(), Attributes.empty()))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.cpu.time",
                                    InstrumentKind.COUNTER,
                                    NumberKind.DOUBLE,
                                    "s",
                                    List.of(LOGICAL_NUMBER, MODE)),
                            measurement -> cache.get()
                                    .ifPresent(
                                            snapshot -> snapshot.times().forEach(
                                                    time -> measurement.recordDouble(
                                                            time.seconds(),
                                                            Attributes.builder()
                                                                    .put(LOGICAL_NUMBER, time.logicalNumber())
                                                                    .put(MODE, time.mode()).build())))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.cpu.frequency",
                                    InstrumentKind.GAUGE,
                                    NumberKind.LONG,
                                    "Hz",
                                    List.of(LOGICAL_NUMBER)),
                            measurement -> cache.get().ifPresent(
                                    snapshot -> snapshot.frequencies().forEach(
                                            frequency -> measurement.recordLong(
                                                    frequency.hertz(),
                                                    Attributes.of(LOGICAL_NUMBER, frequency.logicalNumber()))))));
            group.commit();
            registrations = group;
        } catch (RuntimeException exception) {
            group.close();
            throw exception;
        }
    }

    @Override
    public synchronized void close() {
        if (registrations != null) {
            registrations.close();
            registrations = null;
        }
    }

}
