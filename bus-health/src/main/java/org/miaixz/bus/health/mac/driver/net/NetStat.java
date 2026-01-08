/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.driver.net;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableSizeTByReference;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.IFmsgHdr;
import com.sun.jna.platform.mac.SystemB.IFmsgHdr2;
import com.sun.jna.platform.unix.LibCAPI.size_t;

/**
 * Utility to query NetStat on macOS.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class NetStat {

    /**
     * Control Network (CTL_NET) MIB value.
     */
    private static final int CTL_NET = 4;
    /**
     * Protocol Family Route (PF_ROUTE) MIB value.
     */
    private static final int PF_ROUTE = 17;
    /**
     * Net Route Interface List 2 (NET_RT_IFLIST2) MIB value.
     */
    private static final int NET_RT_IFLIST2 = 6;
    /**
     * Routing Message Interface Info 2 (RTM_IFINFO2) message type.
     */
    private static final int RTM_IFINFO2 = 0x12;

    /**
     * Retrieves network interface data.
     *
     * @param index If positive, limit the map to only return data for this interface index. If negative, returns data
     *              for all indices.
     * @return A map of {@link IFdata} objects indexed by the interface index, encapsulating the stats.
     */
    public static Map<Integer, IFdata> queryIFdata(int index) {
        // Ported from source code of "netstat -ir". See
        // https://opensource.apple.com/source/network_cmds/network_cmds-457/netstat.tproj/if.c
        Map<Integer, IFdata> data = new HashMap<>();
        // Get buffer of all interface information
        int[] mib = { CTL_NET, PF_ROUTE, 0, 0, NET_RT_IFLIST2, 0 };
        try (CloseableSizeTByReference len = new CloseableSizeTByReference()) {
            if (0 != SystemB.INSTANCE.sysctl(mib, 6, null, len, null, size_t.ZERO)) {
                Logger.error("Didn't get buffer length for IFLIST2");
                return data;
            }
            try (Memory buf = new Memory(len.longValue())) {
                if (0 != SystemB.INSTANCE.sysctl(mib, 6, buf, len, null, size_t.ZERO)) {
                    Logger.error("Didn't get buffer for IFLIST2");
                    return data;
                }
                final long now = System.currentTimeMillis();

                // Iterate offset from buf's pointer up to limit of buf
                int lim = (int) (buf.size() - new IFmsgHdr().size());
                int offset = 0;
                while (offset < lim) {
                    // Get pointer to current native part of buf
                    Pointer p = buf.share(offset);
                    // Cast pointer to if_msghdr
                    IFmsgHdr ifm = new IFmsgHdr(p);
                    ifm.read();
                    // Advance next
                    offset += ifm.ifm_msglen;
                    // Skip messages which are not the right format
                    if (ifm.ifm_type == RTM_IFINFO2) {
                        // Cast pointer to if_msghdr2
                        IFmsgHdr2 if2m = new IFmsgHdr2(p);
                        if2m.read();
                        if (index < 0 || index == if2m.ifm_index) {
                            data.put(
                                    (int) if2m.ifm_index,
                                    new IFdata(0xff & if2m.ifm_data.ifi_type, if2m.ifm_data.ifi_opackets,
                                            if2m.ifm_data.ifi_ipackets, if2m.ifm_data.ifi_obytes,
                                            if2m.ifm_data.ifi_ibytes, if2m.ifm_data.ifi_oerrors,
                                            if2m.ifm_data.ifi_ierrors, if2m.ifm_data.ifi_collisions,
                                            if2m.ifm_data.ifi_iqdrops, if2m.ifm_data.ifi_baudrate, now));
                            if (index >= 0) {
                                return data;
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    /**
     * Class to encapsulate network interface data for method return.
     */
    @Immutable
    public static class IFdata {

        private final int ifType;
        private final long oPackets;
        private final long iPackets;
        private final long oBytes;
        private final long iBytes;
        private final long oErrors;
        private final long iErrors;
        private final long collisions;
        private final long iDrops;
        private final long speed;
        private final long timeStamp;

        /**
         * Constructs an {@code IFdata} object.
         *
         * @param ifType     The interface type.
         * @param oPackets   The number of outgoing packets.
         * @param iPackets   The number of incoming packets.
         * @param oBytes     The number of outgoing bytes.
         * @param iBytes     The number of incoming bytes.
         * @param oErrors    The number of outgoing errors.
         * @param iErrors    The number of incoming errors.
         * @param collisions The number of collisions.
         * @param iDrops     The number of incoming packet drops.
         * @param speed      The interface speed.
         * @param timeStamp  The timestamp when this data was captured.
         */
        IFdata(int ifType, // NOSONAR squid:S00107
                long oPackets, long iPackets, long oBytes, long iBytes, long oErrors, long iErrors, long collisions,
                long iDrops, long speed, long timeStamp) {
            this.ifType = ifType;
            this.oPackets = oPackets & 0xffffffffL;
            this.iPackets = iPackets & 0xffffffffL;
            this.oBytes = oBytes & 0xffffffffL;
            this.iBytes = iBytes & 0xffffffffL;
            this.oErrors = oErrors & 0xffffffffL;
            this.iErrors = iErrors & 0xffffffffL;
            this.collisions = collisions & 0xffffffffL;
            this.iDrops = iDrops & 0xffffffffL;
            this.speed = speed & 0xffffffffL;
            this.timeStamp = timeStamp;
        }

        /**
         * Gets the interface type.
         *
         * @return The interface type.
         */
        public int getIfType() {
            return ifType;
        }

        /**
         * Gets the number of outgoing packets.
         *
         * @return The number of outgoing packets.
         */
        public long getOPackets() {
            return oPackets;
        }

        /**
         * Gets the number of incoming packets.
         *
         * @return The number of incoming packets.
         */
        public long getIPackets() {
            return iPackets;
        }

        /**
         * Gets the number of outgoing bytes.
         *
         * @return The number of outgoing bytes.
         */
        public long getOBytes() {
            return oBytes;
        }

        /**
         * Gets the number of incoming bytes.
         *
         * @return The number of incoming bytes.
         */
        public long getIBytes() {
            return iBytes;
        }

        /**
         * Gets the number of outgoing errors.
         *
         * @return The number of outgoing errors.
         */
        public long getOErrors() {
            return oErrors;
        }

        /**
         * Gets the number of incoming errors.
         *
         * @return The number of incoming errors.
         */
        public long getIErrors() {
            return iErrors;
        }

        /**
         * Gets the number of collisions.
         *
         * @return The number of collisions.
         */
        public long getCollisions() {
            return collisions;
        }

        /**
         * Gets the number of incoming packet drops.
         *
         * @return The number of incoming packet drops.
         */
        public long getIDrops() {
            return iDrops;
        }

        /**
         * Gets the interface speed.
         *
         * @return The interface speed.
         */
        public long getSpeed() {
            return speed;
        }

        /**
         * Gets the timestamp when this data was captured.
         *
         * @return The timestamp in milliseconds.
         */
        public long getTimeStamp() {
            return timeStamp;
        }
    }

}
