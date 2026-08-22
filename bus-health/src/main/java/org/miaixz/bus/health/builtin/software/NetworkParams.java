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
package org.miaixz.bus.health.builtin.software;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;

/**
 * NetworkParams presents network parameters of running OS, such as DNS, host name etc.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public interface NetworkParams {

    /**
     * Gets the HostName of the machine executing OSHI.
     *
     * @return the hostname
     */
    String getHostName();

    /**
     * Gets the Domain Name of the machine executing OSHI.
     *
     * @return the domain name
     */
    String getDomainName();

    /**
     * Gets the DNS Servers configured for this machine.
     *
     * @return the DNS servers
     */
    String[] getDnsServers();

    /**
     * Gets the default gateway(routing destination for 0.0.0.0/0) for IPv4 connections.
     *
     * @return default gateway for IPv4, or empty string if not defined.
     */
    String getIpv4DefaultGateway();

    /**
     * Gets default gateway(routing destination for ::/0) for IPv6 connections.
     *
     * @return default gateway for IPv6, or empty string if not defined.
     */
    String getIpv6DefaultGateway();

    /**
     * Gets the operating system routing table, containing both IPv4 and IPv6 routes.
     *
     * @return A list of {@link IPRoute} objects, or an empty list if the routing table could not be read.
     */
    default List<IPRoute> getRoutes() {
        return Collections.emptyList();
    }

    /**
     * A single entry in the operating system routing table.
     */
    @Immutable
    final class IPRoute {

        /**
         * Destination network address bytes.
         */
        private final byte[] destination;

        /**
         * Destination prefix length.
         */
        private final int prefixLength;

        /**
         * Gateway address bytes.
         */
        private final byte[] gateway;

        /**
         * Outgoing interface name.
         */
        private final String interfaceName;

        /**
         * Outgoing interface index.
         */
        private final int interfaceIndex;

        /**
         * Route metric.
         */
        private final long metric;

        /**
         * Whether this route uses a gateway.
         */
        private final boolean isGateway;

        /**
         * Whether this route targets a single host.
         */
        private final boolean isHost;

        /**
         * Constructs a new IPRoute instance.
         *
         * @param destination    The destination network address bytes, four for IPv4 or sixteen for IPv6.
         * @param prefixLength   The number of leading bits in the destination network mask, or {@code -1}.
         * @param gateway        The next hop address bytes, or an empty array for a directly attached route.
         * @param interfaceName  The name of the outgoing interface, or an empty string if not published.
         * @param interfaceIndex The index of the outgoing interface, or {@code -1} if not published.
         * @param metric         The route metric or priority, or {@code -1} if not published.
         * @param isGateway      Whether the route forwards through the gateway.
         * @param isHost         Whether the route is to a single host.
         */
        public IPRoute(byte[] destination, int prefixLength, byte[] gateway, String interfaceName, int interfaceIndex,
                long metric, boolean isGateway, boolean isHost) {
            this.destination = Arrays.copyOf(destination, destination.length);
            this.prefixLength = prefixLength;
            this.gateway = Arrays.copyOf(gateway, gateway.length);
            this.interfaceName = interfaceName;
            this.interfaceIndex = interfaceIndex;
            this.metric = metric;
            this.isGateway = isGateway;
            this.isHost = isHost;
        }

        /**
         * Gets the destination network address.
         *
         * @return The destination address.
         */
        public byte[] getDestination() {
            return Arrays.copyOf(destination, destination.length);
        }

        /**
         * Gets the number of leading bits in the destination network mask.
         *
         * @return The prefix length, or {@code -1} when unavailable.
         */
        public int getPrefixLength() {
            return prefixLength;
        }

        /**
         * Gets the next hop address that traffic matching this route is forwarded to.
         *
         * @return The gateway address, or an empty array for directly attached routes.
         */
        public byte[] getGateway() {
            return Arrays.copyOf(gateway, gateway.length);
        }

        /**
         * Gets the name of the interface traffic matching this route leaves by.
         *
         * @return The interface name, or an empty string if unavailable.
         */
        public String getInterfaceName() {
            return interfaceName;
        }

        /**
         * Gets the index of the interface traffic matching this route leaves by.
         *
         * @return The interface index, matching {@link NetworkIF#getIndex()}, or {@code -1}.
         */
        public int getInterfaceIndex() {
            return interfaceIndex;
        }

        /**
         * Gets the cost of this route.
         *
         * @return The metric, or {@code -1} when unavailable.
         */
        public long getMetric() {
            return metric;
        }

        /**
         * Tests whether traffic matching this route is forwarded through {@link #getGateway()}.
         *
         * @return {@code true} for a gateway route.
         */
        public boolean isGateway() {
            return isGateway;
        }

        /**
         * Tests whether this route is to a single host.
         *
         * @return {@code true} for a host route.
         */
        public boolean isHost() {
            return isHost;
        }

        /**
         * Returns a string representation of the route.
         *
         * @return A string representation of the route.
         */
        @Override
        public String toString() {
            return "IPRoute [destination=" + addressToString(destination) + "/" + prefixLength + ", gateway="
                    + addressToString(gateway) + ", interfaceName=" + interfaceName + ", interfaceIndex="
                    + interfaceIndex + ", metric=" + metric + ", isGateway=" + isGateway + ", isHost=" + isHost + "]";
        }

        /**
         * Converts an address byte array to a display string.
         *
         * @param address The address bytes.
         * @return The display string.
         */
        private static String addressToString(byte[] address) {
            if (address.length > 0) {
                try {
                    return InetAddress.getByAddress(address).getHostAddress();
                } catch (UnknownHostException e) {
                    // Cannot happen for a length of 4 or 16.
                }
            }
            return "*";
        }
    }

}
