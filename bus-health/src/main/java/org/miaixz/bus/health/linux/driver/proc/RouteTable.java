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
package org.miaixz.bus.health.linux.driver.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.NetworkParams.IPRoute;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Reads the Linux routing table from {@code /proc/net/route} and {@code /proc/net/ipv6_route}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class RouteTable {

    /**
     * Creates a new RouteTable instance.
     */
    private RouteTable() {
        // No initialization required.
    }

    /**
     * Queries and parses {@code /proc/net/route}.
     *
     * @param ifIndexByName Interface name to index map.
     * @return The parsed IPv4 routes.
     */
    public static List<IPRoute> queryIpv4Routes(Map<String, Integer> ifIndexByName) {
        return parseIpv4Routes(Builder.readFile(ProcPath.ROUTE), ifIndexByName);
    }

    /**
     * Parses {@code /proc/net/route} content.
     *
     * @param lines         The file lines.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed IPv4 routes.
     */
    static List<IPRoute> parseIpv4Routes(List<String> lines, Map<String, Integer> ifIndexByName) {
        List<IPRoute> routes = new ArrayList<>();
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim(), Normal.__1);
            if (fields.length < Normal._11 || !isHexWord(fields[Normal._1], Normal._8)) {
                continue;
            }
            byte[] destination = Parsing.parseIntToIP((int) Parsing.hexStringToLong(fields[Normal._1], Normal._0));
            int flags = Parsing.hexStringToInt(fields[Normal._3], Normal._0);
            boolean isGateway = (flags & Normal._2) != Normal._0;
            int prefixLength = Parsing.netmaskToPrefixLength(
                    Parsing.parseIntToIP((int) Parsing.hexStringToLong(fields[Normal._7], Normal._0)));
            byte[] gateway = isGateway
                    ? Parsing.parseIntToIP((int) Parsing.hexStringToLong(fields[Normal._2], Normal._0))
                    : Normal.EMPTY_BYTE_ARRAY;
            String interfaceName = fields[Normal._0];
            routes.add(
                    new IPRoute(destination, prefixLength, gateway, interfaceName,
                            indexOfInterface(interfaceName, ifIndexByName),
                            Parsing.parseLongOrDefault(fields[Normal._6], Normal.__1), isGateway,
                            (flags & Normal._4) != Normal._0 || prefixLength == Normal._32));
        }
        return routes;
    }

    /**
     * Queries and parses {@code /proc/net/ipv6_route}.
     *
     * @param ifIndexByName Interface name to index map.
     * @return The parsed IPv6 routes.
     */
    public static List<IPRoute> queryIpv6Routes(Map<String, Integer> ifIndexByName) {
        return parseIpv6Routes(Builder.readFile(ProcPath.IPV6_ROUTE), ifIndexByName);
    }

    /**
     * Parses {@code /proc/net/ipv6_route} content.
     *
     * @param lines         The file lines.
     * @param ifIndexByName Interface name to index map.
     * @return The parsed IPv6 routes.
     */
    static List<IPRoute> parseIpv6Routes(List<String> lines, Map<String, Integer> ifIndexByName) {
        List<IPRoute> routes = new ArrayList<>();
        for (String line : lines) {
            String[] fields = Pattern.SPACES_PATTERN.split(line.trim(), Normal.__1);
            if (fields.length < Normal._10 || !isHexWord(fields[Normal._0], Normal._32)) {
                continue;
            }
            byte[] destination = Parsing.hexStringToByteArray(fields[Normal._0]);
            long flags = Parsing.hexStringToLong(fields[Normal._8], Normal._0);
            boolean isGateway = (flags & Normal._2) != Normal._0;
            int prefixLength = Parsing.hexStringToInt(fields[Normal._1], Normal.__1);
            byte[] nextHop = Parsing.hexStringToByteArray(fields[Normal._4]);
            byte[] gateway = isGateway && nextHop.length == Normal._16 ? nextHop : Normal.EMPTY_BYTE_ARRAY;
            String interfaceName = fields[Normal._9];
            routes.add(
                    new IPRoute(destination, prefixLength, gateway, interfaceName,
                            indexOfInterface(interfaceName, ifIndexByName),
                            Parsing.hexStringToLong(fields[Normal._5], Normal.__1), isGateway,
                            (flags & Normal._4) != Normal._0 || prefixLength == Normal._128));
        }
        return routes;
    }

    /**
     * Tests whether a token has a fixed-length hexadecimal shape.
     *
     * @param token  The token.
     * @param length The required length.
     * @return {@code true} if the token matches.
     */
    private static boolean isHexWord(String token, int length) {
        if (token.length() != length) {
            return false;
        }
        for (int i = Normal._0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c < Symbol.C_ZERO || c > Symbol.C_NINE) && (c < Symbol.C_LOWER_A || c > Symbol.C_LOWER_F)
                    && (c < Symbol.C_UPPER_A || c > Symbol.C_UPPER_F)) {
                return false;
            }
        }
        return true;
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

}
