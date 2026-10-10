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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;

/**
 * Queries TCP connections on Unix-based systems.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class NetStat {

    /**
     * Creates a new NetStat instance.
     */
    public NetStat() {
        // No initialization required.
    }

    /**
     * Query netstat to obtain the number of established TCP connections.
     *
     * @return A {@link Pair} where the left element is the number of established IPv4 connections and the right element
     *         is the number of established IPv6 connections.
     */
    public static Pair<Long, Long> queryTcpnetstat() {
        return queryTcpnetstat(Executor.runNative("netstat -n -p tcp"));
    }

    /**
     * Parse netstat output to count established TCP connections.
     *
     * @param lines output of {@code netstat -n -p tcp}
     * @return A {@link Pair} where the left element is the number of established IPv4 connections and the right element
     *         is the number of established IPv6 connections.
     */
    static Pair<Long, Long> queryTcpnetstat(List<String> lines) {
        long tcp4 = 0L;
        long tcp6 = 0L;
        for (String s : lines) {
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
     * Query netstat for all TCP and UDP connections, including listening and unconnected sockets.
     *
     * @return A list of {@link InternetProtocolStats.IPConnection} objects representing TCP and UDP connections.
     */
    public static List<InternetProtocolStats.IPConnection> queryNetstat() {
        return queryNetstat(Executor.runNative("netstat -an"));
    }

    /**
     * Parse netstat output for TCP and UDP connections.
     *
     * @param lines output of {@code netstat -an}
     * @return A list of {@link InternetProtocolStats.IPConnection} objects representing TCP and UDP connections.
     */
    static List<InternetProtocolStats.IPConnection> queryNetstat(List<String> lines) {
        List<InternetProtocolStats.IPConnection> connections = new ArrayList<>();
        for (String s : lines) {
            String[] split;
            if (s.startsWith(Protocol.TCP.name) || s.startsWith(Protocol.UDP.name)) {
                split = Pattern.SPACES_PATTERN.split(s);
                if (split.length >= 5) {
                    String type = split[0];
                    Pair<byte[], Integer> local = parseIP(split[3]);
                    Pair<byte[], Integer> foreign = parseIP(split[4]);
                    connections.add(
                            new InternetProtocolStats.IPConnection(type, local.getLeft(), local.getRight(),
                                    foreign.getLeft(), foreign.getRight(),
                                    split.length == Normal._6 ? parseTcpState(split[Normal._5])
                                            : InternetProtocolStats.TcpState.NONE,
                                    Parsing.parseIntOrDefault(split[Normal._2], Normal._0),
                                    Parsing.parseIntOrDefault(split[Normal._1], Normal._0), Normal.__1));
                }
            }
        }
        return connections;
    }

    /**
     * Query Solaris netstat for all TCP and UDP connections, including listening and unconnected sockets.
     *
     * @return A list of {@link InternetProtocolStats.IPConnection} objects representing TCP and UDP connections.
     */
    public static List<InternetProtocolStats.IPConnection> querySolarisNetstat() {
        return querySolarisNetstat(Executor.runNative("netstat -an"));
    }

    /**
     * Parse Solaris netstat output for TCP and UDP connections. Solaris groups connections into sections headed by
     * protocol and address family, such as {@code TCP: IPv4}, and each row starts with the local address.
     *
     * @param lines output of {@code netstat -an}
     * @return A list of {@link InternetProtocolStats.IPConnection} objects representing TCP and UDP connections.
     */
    static List<InternetProtocolStats.IPConnection> querySolarisNetstat(List<String> lines) {
        List<InternetProtocolStats.IPConnection> connections = new ArrayList<>();
        String type = null;
        for (String value : lines) {
            String line = value.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] split = Pattern.SPACES_PATTERN.split(line, Normal.__1);
            if (split[Normal._0].indexOf(Symbol.C_DOT) < Normal._0) {
                // Every row starts with an address.port; anything else is a section heading, column heading, or rule.
                type = solarisSectionType(line, type);
                continue;
            }
            if (type == null) {
                continue;
            }
            Pair<byte[], Integer> local = parseIP(split[Normal._0]);
            // An unconnected UDP socket leaves the remote address blank, putting the state in the second field.
            Pair<byte[], Integer> foreign = split.length > Normal._1
                    && split[Normal._1].indexOf(Symbol.C_DOT) >= Normal._0 ? parseIP(split[Normal._1])
                            : Pair.of(Normal.EMPTY_BYTE_ARRAY, Normal._0);
            if (type.startsWith(Protocol.TCP.name)) {
                // Local Address, Remote Address, Swind, Send-Q, Rwind, Recv-Q, State, and If on IPv6.
                if (split.length >= Normal._7) {
                    connections.add(
                            new InternetProtocolStats.IPConnection(type, local.getLeft(), local.getRight(),
                                    foreign.getLeft(), foreign.getRight(), parseTcpState(split[Normal._6]),
                                    Parsing.parseIntOrDefault(split[Normal._3], Normal._0),
                                    Parsing.parseIntOrDefault(split[Normal._5], Normal._0), Normal.__1));
                }
            } else if (split.length >= Normal._3) {
                // Local Address, Remote Address, State, buffer sizes, and overflow counts; UDP has no queues.
                connections.add(
                        new InternetProtocolStats.IPConnection(type, local.getLeft(), local.getRight(),
                                foreign.getLeft(), foreign.getRight(), InternetProtocolStats.TcpState.NONE, Normal._0,
                                Normal._0, Normal.__1));
            }
        }
        return connections;
    }

    /**
     * Resolves the connection type associated with a Solaris netstat section heading.
     *
     * @param line the current heading or separator line
     * @param type the current connection type, or {@code null} outside a supported section
     * @return the resolved connection type, or {@code null} outside a supported section
     */
    private static String solarisSectionType(String line, String type) {
        switch (line) {
            case "TCP: IPv4":
                return "tcp4";

            case "TCP: IPv6":
                return "tcp6";

            case "UDP: IPv4":
                return "udp4";

            case "UDP: IPv6":
                return "udp6";

            default:
                // Column headings and rules stay in the current section; any other heading leaves it.
                return line.startsWith("Local Address") || line.startsWith(Symbol.MINUS) ? type : null;
        }
    }

    /**
     * Maps a native netstat TCP state to the public connection-state enumeration.
     *
     * @param state the native TCP state name
     * @return the corresponding TCP state, or {@link InternetProtocolStats.TcpState#UNKNOWN}
     */
    private static InternetProtocolStats.TcpState parseTcpState(String state) {
        switch (state) {
            case "SYN_RCVD":
            case "SYN_RECEIVED":
                return InternetProtocolStats.TcpState.SYN_RECV;

            case "CLOSED":
            case "LISTEN":
            case "SYN_SENT":
            case "ESTABLISHED":
            case "FIN_WAIT_1":
            case "FIN_WAIT_2":
            case "CLOSE_WAIT":
            case "CLOSING":
            case "LAST_ACK":
            case "TIME_WAIT":
                return InternetProtocolStats.TcpState.valueOf(state);

            default:
                // Solaris also reports IDLE and BOUND, which have no equivalent.
                return InternetProtocolStats.TcpState.UNKNOWN;
        }
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
        int portPos = s.lastIndexOf(Symbol.C_DOT);
        if (portPos > 0 && s.length() > portPos) {
            int port = Parsing.parseIntOrDefault(s.substring(portPos + 1), 0);
            String ip = s.substring(0, portPos);
            if (Symbol.STAR.equals(ip)) {
                // Any address; not a host name to look up.
                return Pair.of(Normal.EMPTY_BYTE_ARRAY, port);
            }
            try {
                // Try to parse existing IP
                return Pair.of(toAddressBytes(ip), port);
            } catch (UnknownHostException e) {
                try {
                    // Try again with trailing ::
                    if (ip.endsWith(Symbol.COLON) && ip.contains(Symbol.COLON + Symbol.COLON)) {
                        ip = ip + Symbol.ZERO;
                    } else if (ip.endsWith(Symbol.COLON) || ip.contains(Symbol.COLON + Symbol.COLON)) {
                        ip = ip + ":0";
                    } else {
                        ip = ip + "::0";
                    }
                    return Pair.of(toAddressBytes(ip), port);
                } catch (UnknownHostException e2) {
                    return Pair.of(Normal.EMPTY_BYTE_ARRAY, port);
                }
            }
        }
        return Pair.of(Normal.EMPTY_BYTE_ARRAY, 0);
    }

    /**
     * Parses an IP address while preserving the 16-byte form of an IPv4-mapped IPv6 address.
     *
     * @param ip the textual IP address
     * @return the address bytes
     * @throws UnknownHostException if the address cannot be parsed
     */
    private static byte[] toAddressBytes(String ip) throws UnknownHostException {
        byte[] address = InetAddress.getByName(ip).getAddress();
        // InetAddress collapses an IPv4-mapped IPv6 address to four bytes, although the socket is IPv6.
        if (address.length == Normal._4 && ip.indexOf(Symbol.C_COLON) >= 0) {
            byte[] mapped = new byte[Normal._16];
            mapped[Normal._10] = (byte) 0xff;
            mapped[Normal._11] = (byte) 0xff;
            System.arraycopy(address, 0, mapped, Normal._12, Normal._4);
            return mapped;
        }
        return address;
    }

    /**
     * Gets TCP stats via {@code netstat -s}. Used for Linux and OpenBSD formats.
     *
     * @param netstatStr The command string.
     * @return The TCP statistics.
     */
    public static InternetProtocolStats.TcpStats queryTcpStats(String netstatStr) {
        return queryTcpStats(Executor.runNative(netstatStr));
    }

    /**
     * Parse TCP statistics from netstat output.
     *
     * @param netstat output lines from {@code netstat -s}
     * @return The TCP statistics.
     */
    static InternetProtocolStats.TcpStats queryTcpStats(List<String> netstat) {
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
        return queryUdpStats(Executor.runNative(netstatStr));
    }

    /**
     * Parse UDP statistics from netstat output.
     *
     * @param netstat output lines from {@code netstat -s}
     * @return The UDP statistics.
     */
    static InternetProtocolStats.UdpStats queryUdpStats(List<String> netstat) {
        long datagramsSent = 0;
        long datagramsReceived = 0;
        long datagramsNoPort = 0;
        long datagramsReceivedErrors = 0;
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
                    case "with no checksum":
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
