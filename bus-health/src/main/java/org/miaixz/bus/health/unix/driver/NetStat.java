/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.driver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;

/**
 * Utility to query TCP connections on Unix-based systems.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class NetStat {

    private NetStat() {
    }

    /**
     * Query netstat to obtain the number of established TCP connections.
     *
     * @return A {@link Pair} where the left element is the number of established IPv4 connections and the right element
     *         is the number of established IPv6 connections.
     */
    public static Pair<Long, Long> queryTcpnetstat() {
        long tcp4 = 0L;
        long tcp6 = 0L;
        List<String> activeConns = Executor.runNative("netstat -n -p tcp");
        for (String s : activeConns) {
            if (s.endsWith("ESTABLISHED")) {
                if (s.startsWith("tcp4")) {
                    tcp4++;
                } else if (s.startsWith("tcp6")) {
                    tcp6++;
                }
            }
        }
        return Pair.of(tcp4, tcp6);
    }

    /**
     * Query netstat to get all TCP and UDP connections.
     *
     * @return A list of {@link InternetProtocolStats.IPConnection} objects representing TCP and UDP connections.
     */
    public static List<InternetProtocolStats.IPConnection> queryNetstat() {
        List<InternetProtocolStats.IPConnection> connections = new ArrayList<>();
        List<String> activeConns = Executor.runNative("netstat -n");
        for (String s : activeConns) {
            String[] split;
            if (s.startsWith("tcp") || s.startsWith("udp")) {
                split = Pattern.SPACES_PATTERN.split(s);
                if (split.length >= 5) {
                    String state = (split.length == 6) ? split[5] : null;
                    // Substitution if required
                    if ("SYN_RCVD".equals(state)) {
                        state = "SYN_RECV";
                    }
                    String type = split[0];
                    Pair<byte[], Integer> local = parseIP(split[3]);
                    Pair<byte[], Integer> foreign = parseIP(split[4]);
                    connections.add(
                            new InternetProtocolStats.IPConnection(type, local.getLeft(), local.getRight(),
                                    foreign.getLeft(), foreign.getRight(),
                                    state == null ? InternetProtocolStats.TcpState.NONE
                                            : InternetProtocolStats.TcpState.valueOf(state),
                                    Parsing.parseIntOrDefault(split[2], 0), Parsing.parseIntOrDefault(split[1], 0),
                                    -1));
                }
            }
        }
        return connections;
    }

    /**
     * Parses an IP address and port from a string.
     *
     * @param s The string to parse (e.g., "73.169.134.6.9599").
     * @return A {@link Pair} where the left element is the IP address as a byte array and the right element is the port
     *         number.
     */
    private static Pair<byte[], Integer> parseIP(String s) {
        // 73.169.134.6.9599 to 73.169.134.6 port 9599
        // or
        // 2001:558:600a:a5.123 to 2001:558:600a:a5 port 123
        int portPos = s.lastIndexOf('.');
        if (portPos > 0 && s.length() > portPos) {
            int port = Parsing.parseIntOrDefault(s.substring(portPos + 1), 0);
            String ip = s.substring(0, portPos);
            try {
                // Try to parse existing IP
                return Pair.of(InetAddress.getByName(ip).getAddress(), port);
            } catch (UnknownHostException e) {
                try {
                    // Try again with trailing ::
                    if (ip.endsWith(Symbol.COLON) && ip.contains(Symbol.COLON + Symbol.COLON)) {
                        ip = ip + "0";
                    } else if (ip.endsWith(Symbol.COLON) || ip.contains(Symbol.COLON + Symbol.COLON)) {
                        ip = ip + ":0";
                    } else {
                        ip = ip + "::0";
                    }
                    return Pair.of(InetAddress.getByName(ip).getAddress(), port);
                } catch (UnknownHostException e2) {
                    return Pair.of(new byte[0], port);
                }
            }
        }
        return Pair.of(new byte[0], 0);
    }

    /**
     * Gets TCP stats via {@code netstat -s}. Used for Linux and OpenBSD formats.
     *
     * @param netstatStr The command string.
     * @return The TCP statistics.
     */
    public static InternetProtocolStats.TcpStats queryTcpStats(String netstatStr) {
        long connectionsEstablished = 0;
        long connectionsActive = 0;
        long connectionsPassive = 0;
        long connectionFailures = 0;
        long connectionsReset = 0;
        long segmentsSent = 0;
        long segmentsReceived = 0;
        long segmentsRetransmitted = 0;
        long inErrors = 0;
        long outResets = 0;
        List<String> netstat = Executor.runNative(netstatStr);
        for (String s : netstat) {
            String[] split = s.trim().split(Symbol.SPACE, 2);
            if (split.length == 2) {
                switch (split[1]) {
                    case "connections established":
                    case "connection established (including accepts)":
                    case "connections established (including accepts)":
                        connectionsEstablished = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "active connection openings":
                        connectionsActive = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "passive connection openings":
                        connectionsPassive = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "failed connection attempts":
                    case "bad connection attempts":
                        connectionFailures = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "connection resets received":
                    case "dropped due to RST":
                        connectionsReset = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "segments sent out":
                    case "packet sent":
                    case "packets sent":
                        segmentsSent = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "segments received":
                    case "packet received":
                    case "packets received":
                        segmentsReceived = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "segments retransmitted":
                        segmentsRetransmitted = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "bad segments received":
                    case "discarded for bad checksum":
                    case "discarded for bad checksums":
                    case "discarded for bad header offset field":
                    case "discarded for bad header offset fields":
                    case "discarded because packet too short":
                    case "discarded for missing IPsec protection":
                        inErrors += Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "resets sent":
                        outResets = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    default:
                        // handle special case variable strings
                        if (split[1].contains("retransmitted") && split[1].contains("data packet")) {
                            segmentsRetransmitted += Parsing.parseLongOrDefault(split[0], 0L);
                        }
                        break;
                }

            }

        }
        return new InternetProtocolStats.TcpStats(connectionsEstablished, connectionsActive, connectionsPassive,
                connectionFailures, connectionsReset, segmentsSent, segmentsReceived, segmentsRetransmitted, inErrors,
                outResets);
    }

    /**
     * Gets UDP stats via {@code netstat -s}. Used for Linux and OpenBSD formats.
     *
     * @param netstatStr The command string.
     * @return The UDP statistics.
     */
    public static InternetProtocolStats.UdpStats queryUdpStats(String netstatStr) {
        long datagramsSent = 0;
        long datagramsReceived = 0;
        long datagramsNoPort = 0;
        long datagramsReceivedErrors = 0;
        List<String> netstat = Executor.runNative(netstatStr);
        for (String s : netstat) {
            String[] split = s.trim().split(Symbol.SPACE, 2);
            if (split.length == 2) {
                switch (split[1]) {
                    case "packets sent":
                    case "datagram output":
                    case "datagrams output":
                        datagramsSent = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "packets received":
                    case "datagram received":
                    case "datagrams received":
                        datagramsReceived = Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "packets to unknown port received":
                    case "dropped due to no socket":
                    case "broadcast/multicast datagram dropped due to no socket":
                    case "broadcast/multicast datagrams dropped due to no socket":
                        datagramsNoPort += Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    case "packet receive errors":
                    case "with incomplete header":
                    case "with bad data length field":
                    case "with bad checksum":
                    case "woth no checksum":
                        datagramsReceivedErrors += Parsing.parseLongOrDefault(split[0], 0L);
                        break;

                    default:
                        break;
                }
            }
        }
        return new InternetProtocolStats.UdpStats(datagramsSent, datagramsReceived, datagramsNoPort,
                datagramsReceivedErrors);
    }

}
