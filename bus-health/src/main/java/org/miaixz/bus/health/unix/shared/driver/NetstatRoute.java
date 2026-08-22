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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.NetworkParams.IPRoute;

/**
 * Queries and parses routing tables from {@code netstat}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class NetstatRoute {

    /**
     * Interface name shape used to reject miscounted columns.
     */
    private static final java.util.regex.Pattern INTERFACE_NAME = java.util.regex.Pattern
            .compile("[A-Za-z][A-Za-z0-9._:@-]*");

    /**
     * Column headers under which each platform prints the interface name.
     */
    private static final String[] INTERFACE_HEADERS = { "Netif", "Iface", "Interface", "If", "Device" };

    /**
     * Creates a new NetstatRoute instance.
     */
    private NetstatRoute() {
        // No initialization required.
    }

    /**
     * Queries both address families from a BSD-derived {@code netstat}.
     *
     * @param ipv4Command        The command listing the IPv4 table.
     * @param ipv6Command        The command listing the IPv6 table.
     * @param defaultIfNameIndex The default interface-name token index.
     * @param ifIndexByName      Interface name to index map.
     * @return The parsed routes for both families.
     */
    public static List<IPRoute> queryRoutes(
            String ipv4Command,
            String ipv6Command,
            int defaultIfNameIndex,
            Map<String, Integer> ifIndexByName) {
        List<IPRoute> routes = new ArrayList<>(queryRoutes(ipv4Command, false, defaultIfNameIndex, ifIndexByName));
        routes.addAll(queryRoutes(ipv6Command, true, defaultIfNameIndex, ifIndexByName));
        return routes;
    }

    /**
     * Queries and parses a BSD-derived {@code netstat} routing table.
     *
     * @param command            The command to run.
     * @param ipv6               Whether the command selects the IPv6 table.
     * @param defaultIfNameIndex The default interface-name token index.
     * @param ifIndexByName      Interface name to index map.
     * @return The parsed routes.
     */
    public static List<IPRoute> queryRoutes(
            String command,
            boolean ipv6,
            int defaultIfNameIndex,
            Map<String, Integer> ifIndexByName) {
        return parseRoutes(Executor.runNative(command), ipv6, defaultIfNameIndex, ifIndexByName);
    }

    /**
     * Parses a BSD-derived {@code netstat} routing table.
     *
     * @param netstat            The netstat output.
     * @param ipv6               Whether this is an IPv6 table.
     * @param defaultIfNameIndex The default interface-name token index.
     * @param ifIndexByName      Interface name to index map.
     * @return The parsed routes.
     */
    static List<IPRoute> parseRoutes(
            List<String> netstat,
            boolean ipv6,
            int defaultIfNameIndex,
            Map<String, Integer> ifIndexByName) {
        int ifNameIndex = defaultIfNameIndex;
        int metricIndex = Normal.__1;
        List<IPRoute> routes = new ArrayList<>();
        for (String line : netstat) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim(), Normal.__1);
            if (fields.length < Normal._3) {
                continue;
            }
            if (isColumnHeader(fields[Normal._0])) {
                int headerIfIndex = indexOfAny(fields, INTERFACE_HEADERS);
                if (headerIfIndex >= Normal._0) {
                    ifNameIndex = headerIfIndex;
                }
                metricIndex = indexOfAny(fields, "Prio");
                continue;
            }
            IPRoute route = parseBsdRow(fields, ipv6, ifNameIndex, metricIndex, ifIndexByName);
            if (route != null) {
                routes.add(route);
            }
        }
        return routes;
    }

    /**
     * Parses a BSD-style route row.
     *
     * @param fields        The row fields.
     * @param ipv6          Whether the row belongs to IPv6.
     * @param ifNameIndex   The interface-name field index.
     * @param metricIndex   The metric field index.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed route, or {@code null}.
     */
    private static IPRoute parseBsdRow(
            String[] fields,
            boolean ipv6,
            int ifNameIndex,
            int metricIndex,
            Map<String, Integer> ifIndexByName) {
        if (!Parsing.isRouteFlags(fields[Normal._2])) {
            return null;
        }
        Pair<byte[], Integer> dest = Parsing.parseRouteDestination(fields[Normal._0], ipv6);
        byte[] destination = dest.getLeft();
        if (destination.length == Normal._0) {
            return null;
        }
        String flags = fields[Normal._2];
        boolean isGateway = flags.indexOf('G') >= Normal._0;
        boolean hostFlag = flags.indexOf('H') >= Normal._0;
        int prefixLength = resolvePrefixLength(dest.getRight(), hostFlag, destination.length);
        byte[] gateway = isGateway ? parseAddress(fields[Normal._1], ipv6) : Normal.EMPTY_BYTE_ARRAY;
        String interfaceName = readInterfaceName(fields, ifNameIndex);
        long metric = metricIndex >= Normal._0 && metricIndex < fields.length
                ? Parsing.parseLongOrDefault(fields[metricIndex], Normal.__1)
                : Normal.__1;
        return new IPRoute(destination, prefixLength, gateway, interfaceName,
                indexOfInterface(interfaceName, ifIndexByName), metric, isGateway,
                hostFlag || prefixLength == destination.length * Normal._8);
    }

    /**
     * Queries both address families from a Solaris {@code netstat -rnv}.
     *
     * @param ipv4Command   The command listing the IPv4 table.
     * @param ipv6Command   The command listing the IPv6 table.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed routes for both families.
     */
    public static List<IPRoute> querySolarisRoutes(
            String ipv4Command,
            String ipv6Command,
            Map<String, Integer> ifIndexByName) {
        List<IPRoute> routes = new ArrayList<>(querySolarisRoutes(ipv4Command, false, ifIndexByName));
        routes.addAll(querySolarisRoutes(ipv6Command, true, ifIndexByName));
        return routes;
    }

    /**
     * Queries and parses a Solaris {@code netstat -rnv} routing table.
     *
     * @param command       The command to run.
     * @param ipv6          Whether the command selects the IPv6 table.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed routes.
     */
    public static List<IPRoute> querySolarisRoutes(String command, boolean ipv6, Map<String, Integer> ifIndexByName) {
        return parseSolarisRoutes(Executor.runNative(command), ipv6, ifIndexByName);
    }

    /**
     * Parses the Solaris {@code netstat -rnv} routing table.
     *
     * @param netstat       The netstat output.
     * @param ipv6          Whether this is an IPv6 table.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed routes.
     */
    static List<IPRoute> parseSolarisRoutes(List<String> netstat, boolean ipv6, Map<String, Integer> ifIndexByName) {
        int gatewayIndex = ipv6 ? Normal._1 : Normal._2;
        int deviceIndex = gatewayIndex + Normal._1;
        List<IPRoute> routes = new ArrayList<>();
        for (String line : netstat) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim(), Normal.__1);
            if (fields.length < deviceIndex + Normal._1) {
                continue;
            }
            int flagsIndex = findFlagsFromRight(fields, deviceIndex);
            if (flagsIndex < Normal._0) {
                continue;
            }
            Pair<byte[], Integer> dest = Parsing.parseRouteDestination(fields[Normal._0], ipv6);
            byte[] destination = dest.getLeft();
            if (destination.length == Normal._0) {
                continue;
            }
            String flags = fields[flagsIndex];
            boolean isGateway = flags.indexOf('G') >= Normal._0;
            boolean hostFlag = flags.indexOf('H') >= Normal._0;
            int prefixLength = dest.getRight();
            if (prefixLength < Normal._0 && !ipv6) {
                prefixLength = Parsing.netmaskToPrefixLength(fields[Normal._1]);
            }
            prefixLength = resolvePrefixLength(prefixLength, hostFlag, destination.length);
            byte[] gateway = isGateway ? parseAddress(fields[gatewayIndex], ipv6) : Normal.EMPTY_BYTE_ARRAY;
            String interfaceName = flagsIndex == deviceIndex + Normal._3 ? readInterfaceName(fields, deviceIndex)
                    : Normal.EMPTY;
            routes.add(
                    new IPRoute(destination, prefixLength, gateway, interfaceName,
                            indexOfInterface(interfaceName, ifIndexByName), Normal.__1, isGateway,
                            hostFlag || prefixLength == destination.length * Normal._8));
        }
        return routes;
    }

    /**
     * Locates the flags column by scanning right to left.
     *
     * @param fields The fields.
     * @param floor  The lowest index to scan.
     * @return The flags index, or {@code -1}.
     */
    private static int findFlagsFromRight(String[] fields, int floor) {
        for (int i = fields.length - Normal._1; i >= floor; i--) {
            if (Parsing.isRouteFlags(fields[i])) {
                return i;
            }
        }
        return Normal.__1;
    }

    /**
     * Resolves a prefix length from route flags.
     *
     * @param statedPrefix The stated prefix.
     * @param hostFlag     Whether the host flag is present.
     * @param addressBytes The address byte count.
     * @return The resolved prefix.
     */
    private static int resolvePrefixLength(int statedPrefix, boolean hostFlag, int addressBytes) {
        if (statedPrefix >= Normal._0) {
            return statedPrefix;
        }
        return hostFlag ? addressBytes * Normal._8 : Normal.__1;
    }

    /**
     * Parses an address token.
     *
     * @param token The address token.
     * @param ipv6  Whether to parse as IPv6.
     * @return The parsed address bytes.
     */
    private static byte[] parseAddress(String token, boolean ipv6) {
        return ipv6 ? Parsing.parseIpv6AddressToBytes(token) : Parsing.parseIpv4AddressToBytes(token);
    }

    /**
     * Reads a valid interface name field.
     *
     * @param fields The fields.
     * @param index  The field index.
     * @return The interface name, or an empty string.
     */
    private static String readInterfaceName(String[] fields, int index) {
        if (index < Normal._0 || index >= fields.length) {
            return Normal.EMPTY;
        }
        String token = fields[index];
        return INTERFACE_NAME.matcher(token).matches() ? token : Normal.EMPTY;
    }

    /**
     * Looks up an interface index by name.
     *
     * @param name          The interface name.
     * @param ifIndexByName Interface name to index map.
     * @return The interface index, or {@code -1}.
     */
    private static int indexOfInterface(String name, Map<String, Integer> ifIndexByName) {
        if (name.isEmpty()) {
            return Normal.__1;
        }
        Integer index = ifIndexByName.get(name);
        return index == null ? Normal.__1 : index;
    }

    /**
     * Tests whether a row is a column header.
     *
     * @param firstToken The first token.
     * @return {@code true} if the token is a route table header.
     */
    private static boolean isColumnHeader(String firstToken) {
        return "Destination".equals(firstToken) || "Destination/Mask".equals(firstToken);
    }

    /**
     * Finds the first field index matching any candidate.
     *
     * @param fields     The fields.
     * @param candidates The candidate values.
     * @return The matching index, or {@code -1}.
     */
    private static int indexOfAny(String[] fields, String... candidates) {
        for (int i = Normal._0; i < fields.length; i++) {
            for (String candidate : candidates) {
                if (candidate.equals(fields[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

}
