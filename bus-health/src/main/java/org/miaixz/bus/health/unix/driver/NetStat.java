/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
 * Utility to query TCP connections
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class NetStat {

    private NetStat() {
    }

    /**
     * Query netstat to obtain number of established TCP connections
     *
     * @return A pair with number of established IPv4 and IPv6 connections
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
     * Query netstat to all TCP and UDP connections
     *
     * @return A list of TCP and UDP connections
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
                    connections.add(new InternetProtocolStats.IPConnection(type, local.getLeft(), local.getRight(),
                            foreign.getLeft(), foreign.getRight(),
                            state == null ? InternetProtocolStats.TcpState.NONE
                                    : InternetProtocolStats.TcpState.valueOf(state),
                            Parsing.parseIntOrDefault(split[2], 0), Parsing.parseIntOrDefault(split[1], 0), -1));
                }
            }
        }
        return connections;
    }

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
     * Gets TCP stats via {@code netstat -s}. Used for Linux and OpenBSD formats
     *
     * @param netstatStr The command string
     * @return The statistics
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
     * Gets UDP stats via {@code netstat -s}. Used for Linux and OpenBSD formats
     *
     * @param netstatStr The command string
     * @return The statistics
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
