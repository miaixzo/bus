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
package org.miaixz.bus.health.unix.shared.driver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.NetworkParams.IPRoute;

/**
 * Parses routing table dumps returned by {@code NET_RT_DUMP}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class RouteTableDump {

    /**
     * Platform-specific routing message layouts.
     */
    public enum Layout {

        /**
         * macOS routing message layout.
         */
        MACOS(92, -1, 8, 4, -1, 4, 8, 30, 0x20000, 0x400),

        /**
         * FreeBSD routing message layout.
         */
        FREEBSD(152, -1, 8, 4, -1, 8, 8, 28, 0, 0),

        /**
         * DragonFly BSD routing message layout.
         */
        DRAGONFLY(152, -1, 8, 4, -1, 8, 11, 28, 0x20000, 0x400),

        /**
         * NetBSD routing message layout.
         */
        NETBSD(120, -1, 8, 4, -1, 8, 9, 24, 0, 0),

        /**
         * OpenBSD routing message layout.
         */
        OPENBSD(96, 4, 16, 6, 10, 8, 15, 24, 0, 0);

        /**
         * Header size in bytes.
         */
        private final int headerSize;

        /**
         * Header-length field offset.
         */
        private final int hdrLenOffset;

        /**
         * Flags field offset.
         */
        private final int flagsOffset;

        /**
         * Interface index field offset.
         */
        private final int indexOffset;

        /**
         * Priority field offset.
         */
        private final int priorityOffset;

        /**
         * Sockaddr padding unit.
         */
        private final int paddingUnit;

        /**
         * Maximum RTAX index.
         */
        private final int rtaxMax;

        /**
         * IPv6 address family value.
         */
        private final int afInet6;

        /**
         * Cloned route flag.
         */
        private final int clonedFlag;

        /**
         * Link-info route flag.
         */
        private final int linkInfoFlag;

        /**
         * Creates a new routing dump layout.
         *
         * @param headerSize     The header size.
         * @param hdrLenOffset   The header-length offset.
         * @param flagsOffset    The flags offset.
         * @param indexOffset    The interface index offset.
         * @param priorityOffset The priority offset.
         * @param paddingUnit    The sockaddr padding unit.
         * @param rtaxMax        The maximum RTAX index.
         * @param afInet6        The IPv6 address family value.
         * @param clonedFlag     The cloned route flag.
         * @param linkInfoFlag   The link-info route flag.
         */
        Layout(int headerSize, int hdrLenOffset, int flagsOffset, int indexOffset, int priorityOffset, int paddingUnit,
                int rtaxMax, int afInet6, int clonedFlag, int linkInfoFlag) {
            this.headerSize = headerSize;
            this.hdrLenOffset = hdrLenOffset;
            this.flagsOffset = flagsOffset;
            this.indexOffset = indexOffset;
            this.priorityOffset = priorityOffset;
            this.paddingUnit = paddingUnit;
            this.rtaxMax = rtaxMax;
            this.afInet6 = afInet6;
            this.clonedFlag = clonedFlag;
            this.linkInfoFlag = linkInfoFlag;
        }
    }

    /**
     * Creates a new RouteTableDump instance.
     */
    private RouteTableDump() {
        // No initialization required.
    }

    /**
     * Parses a routing table dump.
     *
     * @param buffer      The bytes returned by {@code sysctl}.
     * @param layout      The platform layout.
     * @param ifNameByIdx Interface names by index.
     * @return The parsed routes.
     */
    public static List<IPRoute> parse(byte[] buffer, Layout layout, Map<Integer, String> ifNameByIdx) {
        List<IPRoute> routes = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder());
        int offset = Normal._0;
        while (offset < buffer.length) {
            int msgLen = buffer.length - offset < layout.headerSize ? Normal._0
                    : bb.getShort(offset + Normal._0) & Normal._65535;
            if (msgLen < layout.headerSize || offset + msgLen > buffer.length
                    || !addressesFit(bb, buffer, offset, msgLen, layout)) {
                routes.clear();
                return routes;
            }
            if ((buffer[offset + Normal._3] & 0xFF) == Normal._4) {
                IPRoute route = parseMessage(bb, buffer, offset, msgLen, layout, ifNameByIdx);
                if (route != null) {
                    routes.add(route);
                }
            }
            offset += msgLen;
        }
        return routes;
    }

    /**
     * Checks whether the message addresses fit inside the message bounds.
     *
     * @param bb     The byte buffer.
     * @param buffer The raw buffer.
     * @param offset The message offset.
     * @param msgLen The message length.
     * @param layout The platform layout.
     * @return {@code true} if the addresses fit.
     */
    private static boolean addressesFit(ByteBuffer bb, byte[] buffer, int offset, int msgLen, Layout layout) {
        int headerSize = layout.hdrLenOffset < Normal._0 ? layout.headerSize
                : bb.getShort(offset + layout.hdrLenOffset) & Normal._65535;
        if (headerSize < layout.headerSize || headerSize > msgLen) {
            return false;
        }
        int addrs = bb.getInt(offset + Normal._12);
        int end = offset + msgLen;
        int sa = offset + headerSize;
        for (int rtax = Normal._0; rtax < layout.rtaxMax; rtax++) {
            if ((addrs & (Normal._1 << rtax)) == Normal._0) {
                continue;
            }
            if (sa >= end || sa + Normal._2 > end) {
                return false;
            }
            int saLen = buffer[sa] & 0xFF;
            if (saLen > end - sa) {
                return false;
            }
            sa += saLen == Normal._0 ? layout.paddingUnit : roundUp(saLen, layout.paddingUnit);
            if (sa > end) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses one routing message.
     *
     * @param bb          The byte buffer.
     * @param buffer      The raw buffer.
     * @param offset      The message offset.
     * @param msgLen      The message length.
     * @param layout      The platform layout.
     * @param ifNameByIdx Interface names by index.
     * @return The parsed route, or {@code null}.
     */
    private static IPRoute parseMessage(
            ByteBuffer bb,
            byte[] buffer,
            int offset,
            int msgLen,
            Layout layout,
            Map<Integer, String> ifNameByIdx) {
        int addrs = bb.getInt(offset + Normal._12);
        int flags = bb.getInt(offset + layout.flagsOffset);
        if (layout.clonedFlag != Normal._0 && (flags & layout.clonedFlag) != Normal._0
                && (flags & layout.linkInfoFlag) == Normal._0) {
            return null;
        }
        int index = bb.getShort(offset + layout.indexOffset) & Normal._65535;
        int headerSize = layout.hdrLenOffset < Normal._0 ? layout.headerSize
                : bb.getShort(offset + layout.hdrLenOffset) & Normal._65535;

        byte[] destination = Normal.EMPTY_BYTE_ARRAY;
        byte[] gateway = Normal.EMPTY_BYTE_ARRAY;
        int maskStart = Normal.__1;
        int maskLen = Normal._0;

        int end = offset + msgLen;
        int sa = offset + headerSize;
        for (int rtax = Normal._0; rtax < layout.rtaxMax && sa < end; rtax++) {
            if ((addrs & (Normal._1 << rtax)) == Normal._0) {
                continue;
            }
            int saLen = buffer[sa] & 0xFF;
            int saFamily = buffer[sa + Normal._1] & 0xFF;
            if (rtax == Normal._0) {
                destination = readAddress(buffer, sa, saLen, saFamily, layout);
            } else if (rtax == Normal._1) {
                gateway = readAddress(buffer, sa, saLen, saFamily, layout);
            } else if (rtax == Normal._2) {
                maskStart = sa;
                maskLen = saLen;
            }
            sa += saLen == Normal._0 ? layout.paddingUnit : roundUp(saLen, layout.paddingUnit);
        }

        if (destination.length == Normal._0) {
            return null;
        }
        boolean isGateway = (flags & Normal._2) != Normal._0;
        int prefixLength = prefixFromMask(buffer, maskStart, maskLen, destination.length);
        boolean isHost = (flags & Normal._4) != Normal._0 || prefixLength == destination.length * Normal._8;
        long metric = layout.priorityOffset < Normal._0 ? Normal.__1 : buffer[offset + layout.priorityOffset] & 0xFFL;
        String name = ifNameByIdx.get(index);
        return new IPRoute(destination, prefixLength, isGateway ? gateway : Normal.EMPTY_BYTE_ARRAY,
                name == null ? Normal.EMPTY : name, index, metric, isGateway, isHost);
    }

    /**
     * Reads an IP address from a sockaddr.
     *
     * @param buffer   The raw buffer.
     * @param sa       The sockaddr offset.
     * @param saLen    The sockaddr length.
     * @param saFamily The address family.
     * @param layout   The platform layout.
     * @return The address bytes, or an empty array.
     */
    private static byte[] readAddress(byte[] buffer, int sa, int saLen, int saFamily, Layout layout) {
        if (saFamily == Normal._2 && saLen >= Normal._8) {
            return Arrays.copyOfRange(buffer, sa + Normal._4, sa + Normal._8);
        }
        if (saFamily == layout.afInet6 && saLen >= Normal._24) {
            byte[] address = Arrays.copyOfRange(buffer, sa + Normal._8, sa + Normal._24);
            clearEmbeddedScope(address);
            return address;
        }
        return Normal.EMPTY_BYTE_ARRAY;
    }

    /**
     * Clears an embedded interface scope from scoped IPv6 addresses.
     *
     * @param address The address bytes.
     */
    private static void clearEmbeddedScope(byte[] address) {
        if (address.length != Normal._16) {
            return;
        }
        boolean linkLocalUnicast = (address[Normal._0] & 0xFF) == 0xFE && (address[Normal._1] & 0xC0) == 0x80;
        boolean scopedMulticast = (address[Normal._0] & 0xFF) == 0xFF && (address[Normal._1] & 0x0F) <= Normal._2;
        if (linkLocalUnicast || scopedMulticast) {
            address[Normal._2] = Normal._0;
            address[Normal._3] = Normal._0;
        }
    }

    /**
     * Derives a prefix length from a truncated netmask sockaddr.
     *
     * @param buffer       The raw buffer.
     * @param maskStart    The mask sockaddr offset.
     * @param maskLen      The mask sockaddr length.
     * @param addressBytes The destination address byte count.
     * @return The prefix length.
     */
    private static int prefixFromMask(byte[] buffer, int maskStart, int maskLen, int addressBytes) {
        if (maskStart < Normal._0) {
            return addressBytes * Normal._8;
        }
        int addrOffset = addressBytes == Normal._4 ? Normal._4 : Normal._8;
        int available = Math.min(maskLen, buffer.length - maskStart);
        byte[] mask = new byte[addressBytes];
        for (int i = Normal._0; i < addressBytes && addrOffset + i < available; i++) {
            mask[i] = buffer[maskStart + addrOffset + i];
        }
        return Parsing.netmaskToPrefixLength(mask);
    }

    /**
     * Rounds a length up to the next padding unit.
     *
     * @param len  The length.
     * @param unit The padding unit.
     * @return The rounded length.
     */
    private static int roundUp(int len, int unit) {
        return Normal._1 + ((len - Normal._1) | (unit - Normal._1));
    }

}
