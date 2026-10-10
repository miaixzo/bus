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
import org.miaixz.bus.metrics.bridge.ContainerSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds container metrics only when the source reports a container snapshot.
 *
 * @author Kimi Liu
 */
public final class ContainerHostMetrics implements MetricBinder {

    /**
     * Cached optional container snapshot source.
     */
    private final SnapshotCache<Optional<ContainerSnapshot>> cache;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a container binder.
     *
     * @param cache container snapshot cache
     */
    public ContainerHostMetrics(SnapshotCache<Optional<ContainerSnapshot>> cache) {
        this.cache = Objects.requireNonNull(cache, "Container snapshot cache must not be null");
    }

    /**
     * Creates a container metric descriptor.
     *
     * @param name   metric name
     * @param kind   instrument kind
     * @param number numeric representation
     * @param unit   metric unit
     * @return metric descriptor
     */
    private static MetricDescriptor descriptor(String name, InstrumentKind kind, NumberKind number, String unit) {
        return MetricDescriptor.of(name, kind, number, unit, "", List.of());
    }

    @Override
    public synchronized void bind(Provider provider) {
        if (registrations != null) {
            throw new IllegalStateException("Container host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor("container.cpu.time", InstrumentKind.COUNTER, NumberKind.DOUBLE, "s"),
                            measurement -> current().ifPresent(
                                    snapshot -> measurement.recordDouble(snapshot.cpuSeconds(), Attributes.empty()))));
            group.add(
                    provider.registerObservable(
                            descriptor("container.memory.usage", InstrumentKind.UP_DOWN_COUNTER, NumberKind.LONG, "By"),
                            measurement -> current().ifPresent(
                                    snapshot -> measurement
                                            .recordLong(snapshot.memoryUsageBytes(), Attributes.empty()))));
            group.commit();
            registrations = group;
        } catch (RuntimeException exception) {
            group.close();
            throw exception;
        }
    }

    /**
     * Returns the cached container snapshot.
     *
     * @return container data when the process is containerized
     */
    private Optional<ContainerSnapshot> current() {
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
