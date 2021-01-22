/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.linux.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.AbstractInternetProtocolStats;
import org.aoju.bus.health.builtin.software.InternetProtocolStats;
import org.aoju.bus.health.linux.ProcPath;
import org.aoju.bus.health.linux.drivers.ProcessStat;
import org.aoju.bus.health.unix.NetStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
@ThreadSafe
public class LinuxInternetProtocolStats extends AbstractInternetProtocolStats {

    private static TcpStats getTcpStats(String netstatStr) {
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
                        connectionsEstablished = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "active connection openings":
                        connectionsActive = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "passive connection openings":
                        connectionsPassive = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "failed connection attempts":
                        connectionFailures = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "connection resets received":
                        connectionsReset = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "segments sent out":
                        segmentsSent = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "segments received":
                        segmentsReceived = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "segments retransmitted":
                        segmentsRetransmitted = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "bad segments received":
                        inErrors = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "resets sent":
                        outResets = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    default:
                        break;
                }

            }
        }
        return new TcpStats(connectionsEstablished, connectionsActive, connectionsPassive, connectionFailures,
                connectionsReset, segmentsSent, segmentsReceived, segmentsRetransmitted, inErrors, outResets);
    }

    private static UdpStats getUdpStats(String netstatStr) {
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
                        datagramsSent = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "packets received":
                        datagramsReceived = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "packets to unknown port received":
                        datagramsNoPort = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    case "packet receive errors":
                        datagramsReceivedErrors = Builder.parseLongOrDefault(split[0], 0L);
                        break;
                    default:
                        break;
                }
            }
        }
        return new UdpStats(datagramsSent, datagramsReceived, datagramsNoPort, datagramsReceivedErrors);
    }

    private static List<IPConnection> queryConnections(String protocol, int ipver, Map<Integer, Integer> pidMap) {
        List<IPConnection> conns = new ArrayList<>();
        for (String s : FileKit.readLines(ProcPath.NET + "/" + protocol + (ipver == 6 ? "6" : ""))) {
            if (s.indexOf(':') >= 0) {
                String[] split = RegEx.SPACES.split(s.trim());
                if (split.length > 9) {
                    Pair<byte[], Integer> lAddr = parseIpAddr(split[1]);
                    Pair<byte[], Integer> fAddr = parseIpAddr(split[2]);
                    TcpState state = stateLookup(Builder.hexStringToInt(split[3], 0));
                    Pair<Integer, Integer> txQrxQ = parseHexColonHex(split[4]);
                    int inode = Builder.parseIntOrDefault(split[9], 0);
                    conns.add(new IPConnection(protocol + ipver, lAddr.getLeft(), lAddr.getRight(), fAddr.getLeft(), fAddr.getRight(),
                            state, txQrxQ.getLeft(), txQrxQ.getRight(), pidMap.getOrDefault(inode, -1)));
                }
            }
        }
        return conns;
    }

    private static Pair<byte[], Integer> parseIpAddr(String s) {
        int colon = s.indexOf(':');
        if (colon > 0 && colon < s.length()) {
            byte[] first = Builder.hexStringToByteArray(s.substring(0, colon));
            // Bytes are in __be32 endianness. we must invert each set of 4 bytes
            for (int i = 0; i + 3 < first.length; i += 4) {
                byte tmp = first[i];
                first[i] = first[i + 3];
                first[i + 3] = tmp;
                tmp = first[i + 1];
                first[i + 1] = first[i + 2];
                first[i + 2] = tmp;
            }
            int second = Builder.hexStringToInt(s.substring(colon + 1), 0);
            return Pair.of(first, second);
        }
        return Pair.of(new byte[0], 0);
    }

    private static Pair<Integer, Integer> parseHexColonHex(String s) {
        int colon = s.indexOf(':');
        if (colon > 0 && colon < s.length()) {
            int first = Builder.hexStringToInt(s.substring(0, colon), 0);
            int second = Builder.hexStringToInt(s.substring(colon + 1), 0);
            return Pair.of(first, second);
        }
        return Pair.of(0, 0);
    }

    private static InternetProtocolStats.TcpState stateLookup(int state) {
        switch (state) {
            case 0x01:
                return InternetProtocolStats.TcpState.ESTABLISHED;
            case 0x02:
                return InternetProtocolStats.TcpState.SYN_SENT;
            case 0x03:
                return InternetProtocolStats.TcpState.SYN_RECV;
            case 0x04:
                return InternetProtocolStats.TcpState.FIN_WAIT_1;
            case 0x05:
                return InternetProtocolStats.TcpState.FIN_WAIT_2;
            case 0x06:
                return InternetProtocolStats.TcpState.TIME_WAIT;
            case 0x07:
                return InternetProtocolStats.TcpState.CLOSED;
            case 0x08:
                return InternetProtocolStats.TcpState.CLOSE_WAIT;
            case 0x09:
                return InternetProtocolStats.TcpState.LAST_ACK;
            case 0x0A:
                return InternetProtocolStats.TcpState.LISTEN;
            case 0x0B:
                return InternetProtocolStats.TcpState.CLOSING;
            case 0x00:
            default:
                return InternetProtocolStats.TcpState.UNKNOWN;
        }
    }

    @Override
    public TcpStats getTCPv4Stats() {
        return NetStat.queryTcpStats("netstat -st4");
    }

    @Override
    public UdpStats getUDPv4Stats() {
        return NetStat.queryUdpStats("netstat -su4");
    }

    @Override
    public UdpStats getUDPv6Stats() {
        return NetStat.queryUdpStats("netstat -su6");
    }

    @Override
    public List<IPConnection> getConnections() {
        List<IPConnection> conns = new ArrayList<>();
        Map<Integer, Integer> pidMap = ProcessStat.querySocketToPidMap();
        conns.addAll(queryConnections("tcp", 4, pidMap));
        conns.addAll(queryConnections("tcp", 6, pidMap));
        conns.addAll(queryConnections("udp", 4, pidMap));
        conns.addAll(queryConnections("udp", 6, pidMap));
        return conns;
    }

}
