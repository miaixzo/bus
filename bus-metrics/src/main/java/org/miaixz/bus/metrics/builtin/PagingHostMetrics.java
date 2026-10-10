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
import org.miaixz.bus.metrics.bridge.PagingSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds paging-space usage and cumulative paging operations.
 *
 * @author Kimi Liu
 */
public final class PagingHostMetrics implements MetricBinder {

    /**
     * Attribute distinguishing used and free paging space.
     */
    private static final AttributeDescriptor<String> STATE = AttributeDescriptor.string("system.paging.state");
    /**
     * Attribute distinguishing page-in and page-out operations.
     */
    private static final AttributeDescriptor<String> DIRECTION = AttributeDescriptor.string("system.paging.direction");

    /**
     * Cached source for paging values.
     */
    private final SnapshotCache<PagingSnapshot> cache;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a paging binder.
     *
     * @param cache paging snapshot cache
     */
    public PagingHostMetrics(SnapshotCache<PagingSnapshot> cache) {
        this.cache = Objects.requireNonNull(cache, "Paging snapshot cache must not be null");
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
            throw new IllegalStateException("Paging host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.paging.usage",
                                    InstrumentKind.UP_DOWN_COUNTER,
                                    NumberKind.LONG,
                                    "By",
                                    List.of(STATE)),
                            measurement -> cache.get().ifPresent(snapshot -> {
                                measurement.recordLong(snapshot.usedBytes(), Attributes.of(STATE, "used"));
                                measurement.recordLong(snapshot.freeBytes(), Attributes.of(STATE, "free"));
                            })));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.paging.utilization",
                                    InstrumentKind.GAUGE,
                                    NumberKind.DOUBLE,
                                    "1",
                                    List.of(STATE)),
                            measurement -> cache.get().ifPresent(snapshot -> {
                                if (snapshot.totalBytes() > 0) {
                                    measurement.recordDouble(
                                            (double) snapshot.usedBytes() / snapshot.totalBytes(),
                                            Attributes.of(STATE, "used"));
                                    measurement.recordDouble(
                                            (double) snapshot.freeBytes() / snapshot.totalBytes(),
                                            Attributes.of(STATE, "free"));
                                }
                            })));
            group.add(
                    provider.registerObservable(
                            descriptor(
                                    "system.paging.operations",
                                    InstrumentKind.COUNTER,
                                    NumberKind.LONG,
                                    "{operation}",
                                    List.of(DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshot -> {
                                snapshot.pageIns().ifPresent(
                                        value -> measurement.recordLong(value, Attributes.of(DIRECTION, "in")));
                                snapshot.pageOuts().ifPresent(
                                        value -> measurement.recordLong(value, Attributes.of(DIRECTION, "out")));
                            })));
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
