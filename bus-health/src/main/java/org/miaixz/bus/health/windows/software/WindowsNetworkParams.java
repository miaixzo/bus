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
package org.miaixz.bus.health.windows.software;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.IPHlpAPI.FIXED_INFO;
import com.sun.jna.platform.win32.IPHlpAPI.IP_ADDR_STRING;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.NetworkParams;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.windows.jna.IPHlpAPI.MIB_IPFORWARD_ROW2;
import org.miaixz.bus.health.windows.jna.IPHlpAPI.SOCKADDR_INET;
import org.miaixz.bus.logger.Logger;

/**
 * WindowsNetworkParams class.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class WindowsNetworkParams extends AbstractNetworkParams {

    /**
     * A route row read from the IP Helper API.
     */
    public static final class RouteRow {

        /**
         * Destination prefix address bytes.
         */
        public byte[] destination = Normal.EMPTY_BYTE_ARRAY;

        /**
         * Destination prefix length.
         */
        public int prefixLength = -1;

        /**
         * Next hop address bytes.
         */
        public byte[] nextHop = Normal.EMPTY_BYTE_ARRAY;

        /**
         * Outgoing interface index.
         */
        public int interfaceIndex = -1;

        /**
         * Route metric.
         */
        public long metric = -1L;
    }

    /**
     * Parses the ipv4 route.
     *
     * @return the parse ipv4 route result
     */
    private static String parseIpv4Route() {
        List<String> lines = Executor.runNative("route print -4 0.0.0.0");
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim());
            if (fields.length > 2 && "0.0.0.0".equals(fields[0])) {
                return fields[2];
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Parses the ipv6 route.
     *
     * @return the parse ipv6 route result
     */
    private static String parseIpv6Route() {
        List<String> lines = Executor.runNative("route print -6 ::/0");
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim());
            if (fields.length > 3 && "::/0".equals(fields[2])) {
                return fields[3];
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Returns the host name.
     *
     * @return the get host name result
     */
    @Override
    public String getHostName() {
        try {
            return Kernel32Util.getComputerName();
        } catch (Win32Exception e) {
            return super.getHostName();
        }
    }

    /**
     * Returns the ipv4 default gateway.
     *
     * @return the get ipv4 default gateway result
     */
    @Override
    public String getIpv4DefaultGateway() {
        return parseIpv4Route();
    }

    /**
     * Returns the ipv6 default gateway.
     *
     * @return the get ipv6 default gateway result
     */
    @Override
    public String getIpv6DefaultGateway() {
        return parseIpv6Route();
    }

    /**
     * Returns the routing table.
     *
     * @return the routing table
     */
    @Override
    public List<NetworkParams.IPRoute> getRoutes() {
        List<RouteRow> rows = queryRouteRows();
        List<NetworkParams.IPRoute> routes = new ArrayList<>(rows.size());
        if (rows.isEmpty()) {
            return routes;
        }
        Map<Integer, String> namesByIndex = queryInterfaceNameByIndex();
        for (RouteRow row : rows) {
            int addressBits = row.destination.length * 8;
            if (addressBits != 32 && addressBits != 128) {
                continue;
            }
            boolean isGateway = !isUnspecified(row.nextHop);
            String interfaceName = namesByIndex.get(row.interfaceIndex);
            routes.add(
                    new NetworkParams.IPRoute(row.destination, row.prefixLength,
                            isGateway ? row.nextHop : Normal.EMPTY_BYTE_ARRAY,
                            interfaceName == null ? Normal.EMPTY : interfaceName, row.interfaceIndex, row.metric,
                            isGateway, row.prefixLength == addressBits));
        }
        return routes;
    }

    /**
     * Tests whether an address is unspecified.
     *
     * @param address The address bytes.
     * @return {@code true} if all bytes are zero.
     */
    private static boolean isUnspecified(byte[] address) {
        for (byte b : address) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Queries route rows from the IP Helper API.
     *
     * @return The route rows.
     */
    protected List<RouteRow> queryRouteRows() {
        try (ByRef.CloseablePointerByReference tableRef = new ByRef.CloseablePointerByReference()) {
            int ret = org.miaixz.bus.health.windows.jna.IPHlpAPI.INSTANCE
                    .GetIpForwardTable2((short) IPHlpAPI.AF_UNSPEC, tableRef);
            if (ret != WinError.NO_ERROR) {
                Logger.error(false, "Health", "Failed to get the IP forward table. Error code: {}", ret);
                return new ArrayList<>();
            }
            Pointer table = tableRef.getValue();
            try {
                return readRows(table);
            } finally {
                org.miaixz.bus.health.windows.jna.IPHlpAPI.INSTANCE.FreeMibTable(table);
            }
        }
    }

    /**
     * Reads route rows from a native routing table.
     *
     * @param table The native routing table pointer.
     * @return The route rows.
     */
    private static List<RouteRow> readRows(Pointer table) {
        int numEntries = table.getInt(0);
        if (numEntries <= 0) {
            return new ArrayList<>();
        }
        int rowSize = new MIB_IPFORWARD_ROW2().size();
        List<RouteRow> rows = new ArrayList<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            MIB_IPFORWARD_ROW2 row = Structure
                    .newInstance(MIB_IPFORWARD_ROW2.class, table.share(8L + (long) i * rowSize));
            row.read();
            RouteRow out = new RouteRow();
            out.destination = addressBytes(row.DestinationPrefix.Prefix);
            out.prefixLength = row.DestinationPrefix.PrefixLength & 0xff;
            out.nextHop = addressBytes(row.NextHop);
            out.interfaceIndex = row.InterfaceIndex;
            out.metric = Parsing.unsignedIntToLong(row.Metric);
            rows.add(out);
        }
        return rows;
    }

    /**
     * Reads address bytes from a socket address.
     *
     * @param address The socket address.
     * @return The address bytes.
     */
    private static byte[] addressBytes(SOCKADDR_INET address) {
        if (address.si_family == IPHlpAPI.AF_INET) {
            return Parsing.parseIntToIP(address.ipv4AddrOrFlowInfo);
        } else if (address.si_family == IPHlpAPI.AF_INET6) {
            return Arrays.copyOf(address.ipv6Addr, 16);
        }
        return Normal.EMPTY_BYTE_ARRAY;
    }

    /**
     * Returns the domain name.
     *
     * @return the get domain name result
     */
    @Override
    public String getDomainName() {
        char[] buffer = new char[256];
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference(buffer.length)) {
            if (!Kernel32.INSTANCE.GetComputerNameEx(Normal._3, buffer, bufferSize)) {
                Logger.error(
                        false,
                        "Health",
                        "Failed to get dns domain name. Error code: {}",
                        Kernel32.INSTANCE.GetLastError());
                return Normal.EMPTY;
            }
        }
        return Native.toString(buffer);
    }

    /**
     * Returns the dns servers.
     *
     * @return the get dns servers result
     */
    @Override
    public String[] getDnsServers() {
        try (ByRef.CloseableIntByReference bufferSize = new ByRef.CloseableIntByReference()) {
            int ret = IPHlpAPI.INSTANCE.GetNetworkParams(null, bufferSize);
            if (ret != WinError.ERROR_BUFFER_OVERFLOW) {
                Logger.error(false, "Health", "Failed to get network parameters buffer size. Error code: {}", ret);
                return Normal.EMPTY_STRING_ARRAY;
            }

            try (Memory buffer = new Memory(bufferSize.getValue())) {
                ret = IPHlpAPI.INSTANCE.GetNetworkParams(buffer, bufferSize);
                if (ret != 0) {
                    Logger.error(false, "Health", "Failed to get network parameters. Error code: {}", ret);
                    return Normal.EMPTY_STRING_ARRAY;
                }
                FIXED_INFO fixedInfo = new FIXED_INFO(buffer);

                List<String> list = new ArrayList<>();
                IP_ADDR_STRING dns = fixedInfo.DnsServerList;
                while (dns != null) {
                    // a char array of size 16.
                    // This array holds an IPv4 address in dotted decimal notation.
                    String addr = Native.toString(dns.IpAddress.String, Charset.US_ASCII);
                    int nullPos = addr.indexOf(0);
                    if (nullPos != -1) {
                        addr = addr.substring(0, nullPos);
                    }
                    list.add(addr);
                    dns = dns.Next;
                }
                return list.toArray(Normal.EMPTY_STRING_ARRAY);
            }
        }
    }

}
