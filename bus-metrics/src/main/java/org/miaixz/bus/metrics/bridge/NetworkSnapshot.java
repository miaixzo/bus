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

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Immutable network-interface and transport-connection snapshot.
 *
 * @param interfaces  interface counters
 * @param connections connection aggregates
 * @author Kimi Liu
 */
public record NetworkSnapshot(List<InterfaceSnapshot> interfaces, List<ConnectionSnapshot> connections) {

    /**
     * Defensively copies and orders the snapshot.
     */
    public NetworkSnapshot {
        if (interfaces == null || connections == null) {
            throw new IllegalArgumentException("Network snapshot lists must not be null");
        }
        interfaces = interfaces.stream().sorted(Comparator.comparing(InterfaceSnapshot::name)).toList();
        connections = connections.stream()
                .sorted(Comparator.comparing(ConnectionSnapshot::transport).thenComparing(ConnectionSnapshot::state))
                .toList();
    }

    /**
     * Cumulative counters for one network interface.
     *
     * @param name                      interface name
     * @param bytesReceived             cumulative received bytes
     * @param bytesTransmitted          cumulative transmitted bytes
     * @param packetsReceived           cumulative received packets
     * @param packetsTransmitted        cumulative transmitted packets
     * @param packetsDroppedReceived    cumulative receive drops
     * @param packetsDroppedTransmitted cumulative transmit drops
     * @param errorsReceived            cumulative receive errors
     * @param errorsTransmitted         cumulative transmit errors
     */
    public record InterfaceSnapshot(String name, long bytesReceived, long bytesTransmitted, long packetsReceived,
            long packetsTransmitted, long packetsDroppedReceived, long packetsDroppedTransmitted, long errorsReceived,
            long errorsTransmitted) {

        /**
         * Validates interface identity and counters.
         */
        public InterfaceSnapshot {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Network interface name must not be blank");
            }
            name = name.trim();
            long[] values = { bytesReceived, bytesTransmitted, packetsReceived, packetsTransmitted,
                    packetsDroppedReceived, packetsDroppedTransmitted, errorsReceived, errorsTransmitted };
            for (long value : values) {
                if (value < 0) {
                    throw new IllegalArgumentException("Network counters must not be negative");
                }
            }
        }
    }

    /**
     * Aggregated connection count for a transport/state pair.
     *
     * @param transport normalized transport
     * @param state     normalized connection state
     * @param count     matching connection count
     */
    public record ConnectionSnapshot(String transport, String state, long count) {

        /**
         * Supported transport dimensions.
         */
        private static final Set<String> TRANSPORTS = Set.of("tcp", "udp", "unix", "pipe", "quic", "other");
        /**
         * Supported connection-state dimensions.
         */
        private static final Set<String> STATES = Set.of(
                "closed",
                "close_wait",
                "closing",
                "established",
                "fin_wait_1",
                "fin_wait_2",
                "last_ack",
                "listen",
                "syn_received",
                "syn_sent",
                "time_wait",
                "other");

        /**
         * Validates normalized connection dimensions.
         */
        public ConnectionSnapshot {
            if (!TRANSPORTS.contains(transport) || !STATES.contains(state) || count < 0) {
                throw new IllegalArgumentException("Invalid network connection snapshot");
            }
        }
    }

}
