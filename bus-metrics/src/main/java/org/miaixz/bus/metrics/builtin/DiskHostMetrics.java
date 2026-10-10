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
import org.miaixz.bus.metrics.bridge.DiskSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds per-device cumulative disk metrics.
 *
 * @author Kimi Liu
 */
public final class DiskHostMetrics implements MetricBinder {

    /**
     * Attribute identifying a disk device.
     */
    private static final AttributeDescriptor<String> DEVICE = AttributeDescriptor.string("system.device", true);
    /**
     * Attribute distinguishing read and write operations.
     */
    private static final AttributeDescriptor<String> DIRECTION = AttributeDescriptor.string("disk.io.direction");

    /**
     * Cached source for disk counters.
     */
    private final SnapshotCache<List<DiskSnapshot>> cache;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a disk binder.
     *
     * @param cache disk snapshot cache
     */
    public DiskHostMetrics(SnapshotCache<List<DiskSnapshot>> cache) {
        this.cache = Objects.requireNonNull(cache, "Disk snapshot cache must not be null");
    }

    /**
     * Creates disk attributes for one direction.
     *
     * @param device    disk device
     * @param direction I/O direction
     * @return immutable disk attributes
     */
    private static Attributes attributes(String device, String direction) {
        return Attributes.builder().put(DEVICE, device).put(DIRECTION, direction).build();
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
            throw new IllegalStateException("Disk host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.disk.io",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    List.of(DEVICE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshots -> snapshots.forEach(snapshot -> {
                                snapshot.readBytes().ifPresent(
                                        value -> measurement.recordLong(value, attributes(snapshot.device(), "read")));
                                snapshot.writeBytes().ifPresent(
                                        value -> measurement.recordLong(value, attributes(snapshot.device(), "write")));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.disk.operations",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "{operation}",
                                    List.of(DEVICE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshots -> snapshots.forEach(snapshot -> {
                                snapshot.reads().ifPresent(
                                        value -> measurement.recordLong(value, attributes(snapshot.device(), "read")));
                                snapshot.writes().ifPresent(
                                        value -> measurement.recordLong(value, attributes(snapshot.device(), "write")));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.disk.io_time",
                                    InstrumentKind.COUNTER,
                                    NumberKind.DOUBLE,
                                    "s",
                                    List.of(DEVICE)),
                            measurement -> cache.get().ifPresent(
                                    snapshots -> snapshots.forEach(
                                            snapshot -> snapshot.transferTimeMillis().ifPresent(
                                                    value -> measurement.recordDouble(
                                                            value / 1000.0,
                                                            Attributes.of(DEVICE, snapshot.device())))))));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.disk.limit",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    List.of(DEVICE)),
                            measurement -> cache.get().ifPresent(
                                    snapshots -> snapshots.forEach(
                                            snapshot -> snapshot.sizeBytes().ifPresent(
                                                    value -> measurement.recordLong(
                                                            value,
                                                            Attributes.of(DEVICE, snapshot.device())))))));
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
