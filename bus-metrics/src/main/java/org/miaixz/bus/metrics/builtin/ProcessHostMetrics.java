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
import java.util.Optional;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.bridge.HostMetricCapabilities;
import org.miaixz.bus.metrics.bridge.ProcessSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds metrics for the current process only.
 *
 * @author Kimi Liu
 */
public final class ProcessHostMetrics implements MetricBinder {

    /**
     * Attribute distinguishing user and system CPU time.
     */
    private static final AttributeDescriptor<String> CPU_MODE = AttributeDescriptor.string("cpu.mode");
    /**
     * Attribute distinguishing process read and write I/O.
     */
    private static final AttributeDescriptor<String> IO_DIRECTION = AttributeDescriptor.string("disk.io.direction");
    /**
     * Attribute distinguishing process page-fault types.
     */
    private static final AttributeDescriptor<String> FAULT_TYPE = AttributeDescriptor
            .string("system.paging.fault.type");
    /**
     * Attribute distinguishing voluntary and involuntary context switches.
     */
    private static final AttributeDescriptor<String> SWITCH_TYPE = AttributeDescriptor
            .string("process.context_switch.type");

    /**
     * Cached current-process snapshot source.
     */
    private final SnapshotCache<Optional<ProcessSnapshot>> cache;
    /**
     * Platform capability declaration used to choose supported series.
     */
    private final HostMetricCapabilities capabilities;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a current-process binder.
     *
     * @param cache        current-process snapshot cache
     * @param capabilities platform-declared metric capabilities
     */
    public ProcessHostMetrics(SnapshotCache<Optional<ProcessSnapshot>> cache, HostMetricCapabilities capabilities) {
        this.cache = Objects.requireNonNull(cache, "Process snapshot cache must not be null");
        this.capabilities = Objects.requireNonNull(capabilities, "Host capabilities must not be null");
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
            throw new IllegalStateException("Process host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "process.cpu.time",
                                    InstrumentKind.COUNTER,
                                    NumberKind.DOUBLE,
                                    "s",
                                    List.of(CPU_MODE)),
                            measurement -> current().ifPresent(snapshot -> {
                                measurement.recordDouble(
                                        snapshot.userTimeMillis() / 1000.0,
                                        Attributes.of(CPU_MODE, "user"));
                                measurement.recordDouble(
                                        snapshot.kernelTimeMillis() / 1000.0,
                                        Attributes.of(CPU_MODE, "system"));
                            })));
            scalarLong(group, provider, "process.memory.usage", "By", ProcessSnapshot::residentBytes);
            scalarLong(group, provider, "process.memory.virtual", "By", ProcessSnapshot::virtualBytes);
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "process.disk.io",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    List.of(IO_DIRECTION)),
                            measurement -> current().ifPresent(snapshot -> {
                                measurement.recordLong(snapshot.readBytes(), Attributes.of(IO_DIRECTION, "read"));
                                measurement.recordLong(snapshot.writeBytes(), Attributes.of(IO_DIRECTION, "write"));
                            })));
            scalarLong(group, provider, "process.thread.count", "{thread}", ProcessSnapshot::threadCount);
            if (capabilities.unixFileDescriptors()) {
                optionalLong(
                        group,
                        provider,
                        "process.unix.file_descriptor.count",
                        InstrumentKind.UP_DOWN_COUNTER,
                        "{file_descriptor}",
                        snapshot -> snapshot.openFiles());
            }
            if (capabilities.windowsHandles()) {
                optionalLong(
                        group,
                        provider,
                        "process.windows.handle.count",
                        InstrumentKind.UP_DOWN_COUNTER,
                        "{handle}",
                        snapshot -> snapshot.handles());
            }
            bindFaults(group, provider);
            if (capabilities.contextSwitchTypes()) {
                group.add(
                        provider.registerObservable(
                                descriptor(
                                        "process.context_switches",
                                        InstrumentKind.COUNTER,
                                        NumberKind.LONG,
                                        "{context_switch}",
                                        List.of(SWITCH_TYPE)),
                                measurement -> current().ifPresent(snapshot -> {
                                    snapshot.voluntarySwitches().ifPresent(
                                            value -> measurement
                                                    .recordLong(value, Attributes.of(SWITCH_TYPE, "voluntary")));
                                    snapshot.involuntarySwitches().ifPresent(
                                            value -> measurement
                                                    .recordLong(value, Attributes.of(SWITCH_TYPE, "involuntary")));
                                })));
            }
            group.add(
                    provider.registerObservable(
                            descriptor("process.uptime", InstrumentKind.GAUGE, NumberKind.DOUBLE, "s", List.of()),
                            measurement -> current().ifPresent(
                                    snapshot -> measurement
                                            .recordDouble(snapshot.uptimeMillis() / 1000.0, Attributes.empty()))));
            group.commit();
            registrations = group;
        } catch (RuntimeException exception) {
            group.close();
            throw exception;
        }
    }

    /**
     * Registers the page-fault shape supported by the current platform.
     *
     * @param group    transactional registration group
     * @param provider target provider
     */
    private void bindFaults(RegistrationGroup group, Provider provider) {
        if (capabilities.pagingFaults() == HostMetricCapabilities.PagingFaultShape.SPLIT) {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "process.paging.faults",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "{fault}",
                                    List.of(FAULT_TYPE)),
                            measurement -> current().ifPresent(snapshot -> {
                                snapshot.majorFaults().ifPresent(
                                        value -> measurement.recordLong(value, Attributes.of(FAULT_TYPE, "major")));
                                snapshot.minorFaults().ifPresent(
                                        value -> measurement.recordLong(value, Attributes.of(FAULT_TYPE, "minor")));
                            })));
        } else if (capabilities.pagingFaults() == HostMetricCapabilities.PagingFaultShape.TOTAL) {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "process.paging.faults",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "{fault}",
                                    List.of()),
                            measurement -> current().ifPresent(snapshot -> {
                                long total = snapshot.majorFaults().orElse(0) + snapshot.minorFaults().orElse(0);
                                measurement.recordLong(total, Attributes.empty());
                            })));
        }
    }

    /**
     * Registers an observable scalar derived from the current process.
     *
     * @param group      transactional registration group
     * @param provider   target provider
     * @param descriptor metric descriptor
     * @param value      value extractor
     */
    private void scalarLong(
            RegistrationGroup group,
            Provider provider,
            String name,
            String unit,
            java.util.function.ToLongFunction<ProcessSnapshot> getter) {
        group.add(
                provider.registerObservable(
                        descriptor(name, InstrumentKind.UP_DOWN_COUNTER, NumberKind.LONG, unit, List.of()),
                        measurement -> current().ifPresent(
                                snapshot -> measurement.recordLong(getter.applyAsLong(snapshot), Attributes.empty()))));
    }

    /**
     * Registers an observable scalar that may be unavailable on the platform.
     *
     * @param group      transactional registration group
     * @param provider   target provider
     * @param descriptor metric descriptor
     * @param value      optional value extractor
     * @param attributes series attributes
     */
    private void optionalLong(
            RegistrationGroup group,
            Provider provider,
            String name,
            InstrumentKind kind,
            String unit,
            java.util.function.Function<ProcessSnapshot, java.util.OptionalLong> getter) {
        group.add(
                provider.registerObservable(
                        descriptor(name, kind, NumberKind.LONG, unit, List.of()),
                        measurement -> current().ifPresent(
                                snapshot -> getter.apply(snapshot)
                                        .ifPresent(value -> measurement.recordLong(value, Attributes.empty())))));
    }

    /**
     * Returns the cached current-process snapshot.
     *
     * @return current process when it still exists
     */
    private Optional<ProcessSnapshot> current() {
        return cache.get().flatMap(value -> value);
    }

    @Override
    public synchronized void close() {
        if (registrations != null) {
            registrations.close();
            registrations = null;
        }
    }

}
