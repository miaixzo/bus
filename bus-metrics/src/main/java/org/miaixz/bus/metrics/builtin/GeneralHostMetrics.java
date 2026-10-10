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
import org.miaixz.bus.metrics.bridge.GeneralSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds general host metrics.
 *
 * @author Kimi Liu
 */
public final class GeneralHostMetrics implements MetricBinder {

    /**
     * Attribute used to distinguish normalized process states.
     */
    private static final AttributeDescriptor<String> PROCESS_STATE = AttributeDescriptor.string("process.state");

    /**
     * Cached source for system uptime and process-state counts.
     */
    private final SnapshotCache<GeneralSnapshot> cache;
    /**
     * Whether process-state series are enabled.
     */
    private final boolean stateCounts;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a binder.
     *
     * @param cache       general host snapshot cache
     * @param stateCounts whether process state metrics are enabled
     */
    public GeneralHostMetrics(SnapshotCache<GeneralSnapshot> cache, boolean stateCounts) {
        this.cache = Objects.requireNonNull(cache, "General snapshot cache must not be null");
        this.stateCounts = stateCounts;
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
            throw new IllegalStateException("General host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor("system.uptime", InstrumentKind.GAUGE, NumberKind.DOUBLE, "s", List.of()),
                            measurement -> cache.get().ifPresent(
                                    snapshot -> measurement
                                            .recordDouble(snapshot.uptimeSeconds(), Attributes.empty()))));
            if (stateCounts) {
                group.add(
                        provider.registerObservable(
                                descriptor(
                                        "system.process.count",
                                        InstrumentKind.UP_DOWN_COUNTER,
                                        NumberKind.LONG,
                                        "{process}",
                                        List.of(PROCESS_STATE)),
                                measurement -> cache.get().ifPresent(
                                        snapshot -> snapshot.processCountsByState().forEach(
                                                (state, count) -> measurement
                                                        .recordLong(count, Attributes.of(PROCESS_STATE, state))))));
            }
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
