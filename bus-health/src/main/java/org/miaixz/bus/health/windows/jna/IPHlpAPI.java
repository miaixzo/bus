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
package org.miaixz.bus.health.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

import org.miaixz.bus.core.lang.Normal;

/**
 * Extends JNA IP Helper API mapping with routing table calls.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface IPHlpAPI extends com.sun.jna.platform.win32.IPHlpAPI {

    /**
     * The IP Helper API instance.
     */
    IPHlpAPI INSTANCE = Native.load("IPHlpAPI", IPHlpAPI.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * A union of {@code SOCKADDR_IN}, {@code SOCKADDR_IN6}, and {@code ADDRESS_FAMILY}.
     */
    @FieldOrder({ "si_family", "port", "ipv4AddrOrFlowInfo", "ipv6Addr", "scopeId" })
    class SOCKADDR_INET extends Structure {

        /**
         * Creates a new socket address structure.
         */
        public SOCKADDR_INET() {
            // No initialization required.
        }

        /**
         * Address family.
         */
        public short si_family;

        /**
         * Port value.
         */
        public short port;

        /**
         * IPv4 address or IPv6 flow info.
         */
        public int ipv4AddrOrFlowInfo;

        /**
         * IPv6 address bytes.
         */
        public byte[] ipv6Addr = new byte[Normal._16];

        /**
         * IPv6 scope ID.
         */
        public int scopeId;
    }

    /**
     * An address paired with a prefix length.
     */
    @FieldOrder({ "Prefix", "PrefixLength" })
    class IP_ADDRESS_PREFIX extends Structure {

        /**
         * Creates a new address prefix structure.
         */
        public IP_ADDRESS_PREFIX() {
            // No initialization required.
        }

        /**
         * Prefix address.
         */
        public SOCKADDR_INET Prefix = new SOCKADDR_INET();

        /**
         * Prefix length.
         */
        public byte PrefixLength;
    }

    /**
     * A single IP forwarding table row.
     */
    @FieldOrder({ "InterfaceLuid", "InterfaceIndex", "DestinationPrefix", "NextHop", "SitePrefixLength",
            "ValidLifetime", "PreferredLifetime", "Metric", "Protocol", "Loopback", "AutoconfigureAddress", "Publish",
            "Immortal", "Age", "Origin" })
    class MIB_IPFORWARD_ROW2 extends Structure {

        /**
         * Interface LUID.
         */
        public long InterfaceLuid;

        /**
         * Interface index.
         */
        public int InterfaceIndex;

        /**
         * Destination prefix.
         */
        public IP_ADDRESS_PREFIX DestinationPrefix = new IP_ADDRESS_PREFIX();

        /**
         * Next hop.
         */
        public SOCKADDR_INET NextHop = new SOCKADDR_INET();

        /**
         * Site prefix length.
         */
        public byte SitePrefixLength;

        /**
         * Valid lifetime.
         */
        public int ValidLifetime;

        /**
         * Preferred lifetime.
         */
        public int PreferredLifetime;

        /**
         * Route metric.
         */
        public int Metric;

        /**
         * Route protocol.
         */
        public int Protocol;

        /**
         * Loopback flag.
         */
        public byte Loopback;

        /**
         * Autoconfigure address flag.
         */
        public byte AutoconfigureAddress;

        /**
         * Publish flag.
         */
        public byte Publish;

        /**
         * Immortal flag.
         */
        public byte Immortal;

        /**
         * Route age.
         */
        public int Age;

        /**
         * Route origin.
         */
        public int Origin;

        /**
         * Creates a new forwarding row.
         */
        public MIB_IPFORWARD_ROW2() {
            super();
        }

        /**
         * Creates a new forwarding row from native memory.
         *
         * @param p The native pointer.
         */
        public MIB_IPFORWARD_ROW2(Pointer p) {
            super(p);
        }
    }

    /**
     * Retrieves the IP route entries on the local computer.
     *
     * @param family The address family.
     * @param table  Receives the allocated routing table pointer.
     * @return {@code NO_ERROR} on success.
     */
    int GetIpForwardTable2(short family, PointerByReference table);

    /**
     * Frees a table allocated by the IP Helper API.
     *
     * @param memory The table pointer to free.
     */
    void FreeMibTable(Pointer memory);

}
