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
import org.miaixz.bus.metrics.bridge.NetworkSnapshot;
import org.miaixz.bus.metrics.bridge.SnapshotCache;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Binds cumulative network-interface and connection metrics.
 *
 * @author Kimi Liu
 */
public final class NetworkHostMetrics implements MetricBinder {

    /**
     * Attribute identifying a logical network interface.
     */
    private static final AttributeDescriptor<String> INTERFACE = AttributeDescriptor
            .string("network.interface.name", true);
    /**
     * Attribute identifying a system network device.
     */
    private static final AttributeDescriptor<String> DEVICE = AttributeDescriptor.string("system.device", true);
    /**
     * Attribute distinguishing receive and transmit directions.
     */
    private static final AttributeDescriptor<String> DIRECTION = AttributeDescriptor.string("network.io.direction");
    /**
     * Attribute identifying a transport protocol.
     */
    private static final AttributeDescriptor<String> TRANSPORT = AttributeDescriptor.string("network.transport");
    /**
     * Attribute identifying a transport connection state.
     */
    private static final AttributeDescriptor<String> CONNECTION_STATE = AttributeDescriptor
            .string("network.connection.state");

    /**
     * Cached source for network counters and connection aggregates.
     */
    private final SnapshotCache<NetworkSnapshot> cache;
    /**
     * Whether transport connection metrics are enabled.
     */
    private final boolean connections;
    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;

    /**
     * Creates a network binder.
     *
     * @param cache       network snapshot cache
     * @param connections whether connection metrics are enabled
     */
    public NetworkHostMetrics(SnapshotCache<NetworkSnapshot> cache, boolean connections) {
        this.cache = Objects.requireNonNull(cache, "Network snapshot cache must not be null");
        this.connections = connections;
    }

    /**
     * Creates an observable counter descriptor.
     *
     * @param name       metric name
     * @param unit       metric unit
     * @param attributes ordered attribute schema
     * @return metric descriptor
     */
    private static MetricDescriptor descriptor(String name, String unit, List<AttributeDescriptor<?>> attributes) {
        return MetricDescriptor.of(name, InstrumentKind.COUNTER, NumberKind.LONG, unit, "", attributes);
    }

    /**
     * Creates semantic network-interface attributes.
     *
     * @param name      interface name
     * @param direction traffic direction
     * @return immutable attributes
     */
    private static Attributes interfaceAttributes(String name, String direction) {
        return Attributes.builder().put(INTERFACE, name).put(DIRECTION, direction).build();
    }

    /**
     * Creates system device attributes for error and drop counters.
     *
     * @param name      device name
     * @param direction traffic direction
     * @return immutable attributes
     */
    private static Attributes deviceAttributes(String name, String direction) {
        return Attributes.builder().put(DEVICE, name).put(DIRECTION, direction).build();
    }

    @Override
    public synchronized void bind(Provider provider) {
        if (registrations != null) {
            throw new IllegalStateException("Network host metrics are already bound");
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            group.add(
                    provider.registerObservable(
                            descriptor("system.network.io", "By", List.of(INTERFACE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshot -> snapshot.interfaces().forEach(network -> {
                                measurement.recordLong(
                                        network.bytesReceived(),
                                        interfaceAttributes(network.name(), "receive"));
                                measurement.recordLong(
                                        network.bytesTransmitted(),
                                        interfaceAttributes(network.name(), "transmit"));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor("system.network.packet.count", "{packet}", List.of(DEVICE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshot -> snapshot.interfaces().forEach(network -> {
                                measurement.recordLong(
                                        network.packetsReceived(),
                                        deviceAttributes(network.name(), "receive"));
                                measurement.recordLong(
                                        network.packetsTransmitted(),
                                        deviceAttributes(network.name(), "transmit"));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor("system.network.packet.dropped", "{packet}", List.of(INTERFACE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshot -> snapshot.interfaces().forEach(network -> {
                                measurement.recordLong(
                                        network.packetsDroppedReceived(),
                                        interfaceAttributes(network.name(), "receive"));
                                measurement.recordLong(
                                        network.packetsDroppedTransmitted(),
                                        interfaceAttributes(network.name(), "transmit"));
                            }))));
            group.add(
                    provider.registerObservable(
                            descriptor("system.network.errors", "{error}", List.of(INTERFACE, DIRECTION)),
                            measurement -> cache.get().ifPresent(snapshot -> snapshot.interfaces().forEach(network -> {
                                measurement.recordLong(
                                        network.errorsReceived(),
                                        interfaceAttributes(network.name(), "receive"));
                                measurement.recordLong(
                                        network.errorsTransmitted(),
                                        interfaceAttributes(network.name(), "transmit"));
                            }))));
            if (connections) {
                group.add(
                        provider.registerObservable(
                                MetricDescriptor.of(
                                        "system.network.connection.count",
                                        InstrumentKind.UP_DOWN_COUNTER,
                                        NumberKind.LONG,
                                        "{connection}",
                                        "",
                                        List.of(TRANSPORT, CONNECTION_STATE)),
                                measurement -> cache.get().ifPresent(
                                        snapshot -> snapshot.connections().forEach(
                                                connection -> measurement.recordLong(
                                                        connection.count(),
                                                        Attributes.builder().put(TRANSPORT, connection.transport())
                                                                .put(CONNECTION_STATE, connection.state()).build())))));
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
