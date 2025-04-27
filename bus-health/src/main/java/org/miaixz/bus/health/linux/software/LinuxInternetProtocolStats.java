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
package org.miaixz.bus.health.linux.software;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class LinuxInternetProtocolStats extends AbstractInternetProtocolStats {

    private final String tcpColon = "Tcp:";
    private final String udpColon = "Udp:";
    private final String udp6 = "Udp6";

    private enum TcpStat {
        RtoAlgorithm, RtoMin, RtoMax, MaxConn, ActiveOpens, PassiveOpens, AttemptFails, EstabResets, CurrEstab, InSegs,
        OutSegs, RetransSegs, InErrs, OutRsts, InCsumErrors;
    }

    private enum UdpStat {
        OutDatagrams, InDatagrams, NoPorts, InErrors, RcvbufErrors, SndbufErrors, InCsumErrors, IgnoredMulti, MemErrors;
    }

    @Override
    public TcpStats getTCPv4Stats() {
        byte[] fileBytes = Builder.readAllBytes(ProcPath.SNMP, true);
        List<String> lines = Parsing.parseByteArrayToStrings(fileBytes);
        Map<TcpStat, Long> tcpData = new EnumMap<>(TcpStat.class);

        for (int line = 0; line < lines.size() - 1; line += 2) {
            if (lines.get(line).startsWith(tcpColon) && lines.get(line + 1).startsWith(tcpColon)) {
                Map<TcpStat, String> parsedData = Parsing.stringToEnumMap(TcpStat.class,
                        lines.get(line + 1).substring(tcpColon.length()).trim(), ' ');
                for (Map.Entry<TcpStat, String> entry : parsedData.entrySet()) {
                    tcpData.put(entry.getKey(), Parsing.parseLongOrDefault(entry.getValue(), 0L));
                }
                break;
            }
        }

        return new TcpStats(tcpData.getOrDefault(TcpStat.CurrEstab, 0L), tcpData.getOrDefault(TcpStat.ActiveOpens, 0L),
                tcpData.getOrDefault(TcpStat.PassiveOpens, 0L), tcpData.getOrDefault(TcpStat.AttemptFails, 0L),
                tcpData.getOrDefault(TcpStat.EstabResets, 0L), tcpData.getOrDefault(TcpStat.OutSegs, 0L),
                tcpData.getOrDefault(TcpStat.InSegs, 0L), tcpData.getOrDefault(TcpStat.RetransSegs, 0L),
                tcpData.getOrDefault(TcpStat.InErrs, 0L), tcpData.getOrDefault(TcpStat.OutRsts, 0L));
    }

    @Override
    public UdpStats getUDPv4Stats() {
        byte[] fileBytes = Builder.readAllBytes(ProcPath.SNMP, true);
        List<String> lines = Parsing.parseByteArrayToStrings(fileBytes);
        Map<UdpStat, Long> udpData = new EnumMap<>(UdpStat.class);

        for (int line = 0; line < lines.size() - 1; line += 2) {
            if (lines.get(line).startsWith(udpColon) && lines.get(line + 1).startsWith(udpColon)) {
                Map<UdpStat, String> parsedData = Parsing.stringToEnumMap(UdpStat.class,
                        lines.get(line + 1).substring(udpColon.length()).trim(), ' ');
                for (Map.Entry<UdpStat, String> entry : parsedData.entrySet()) {
                    udpData.put(entry.getKey(), Parsing.parseLongOrDefault(entry.getValue(), 0L));
                }
                break;
            }
        }

        return new UdpStats(udpData.getOrDefault(UdpStat.OutDatagrams, 0L),
                udpData.getOrDefault(UdpStat.InDatagrams, 0L), udpData.getOrDefault(UdpStat.NoPorts, 0L),
                udpData.getOrDefault(UdpStat.InErrors, 0L));
    }

    @Override
    public UdpStats getUDPv6Stats() {
        byte[] fileBytes = Builder.readAllBytes(ProcPath.SNMP6, true);
        List<String> lines = Parsing.parseByteArrayToStrings(fileBytes);
        long inDatagrams = 0;
        long noPorts = 0;
        long inErrors = 0;
        long outDatagrams = 0;
        int foundUDPv6StatsCount = 0;

        // Traverse bottom-to-top for efficiency as the /etc/proc/snmp6 file follows sequential format -> ip6, icmp6,
        // udp6, udplite6 stats
        for (int line = lines.size() - 1; line >= 0 && foundUDPv6StatsCount < 4; line--) {
            if (lines.get(line).startsWith(udp6)) {
                String[] parts = lines.get(line).split("\\s+");
                switch (parts[0]) {
                case "Udp6InDatagrams":
                    inDatagrams = Parsing.parseLongOrDefault(parts[1], 0L);
                    foundUDPv6StatsCount++;
                    break;
                case "Udp6NoPorts":
                    noPorts = Parsing.parseLongOrDefault(parts[1], 0L);
                    foundUDPv6StatsCount++;
                    break;
                case "Udp6InErrors":
                    inErrors = Parsing.parseLongOrDefault(parts[1], 0L);
                    foundUDPv6StatsCount++;
                    break;
                case "Udp6OutDatagrams":
                    outDatagrams = Parsing.parseLongOrDefault(parts[1], 0L);
                    foundUDPv6StatsCount++;
                    break;
                default:
                    break;
                }
            }
        }

        return new UdpStats(inDatagrams, noPorts, inErrors, outDatagrams);
    }

    @Override
    public List<InternetProtocolStats.IPConnection> getConnections() {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        Map<Long, Integer> pidMap = ProcessStat.querySocketToPidMap();
        conns.addAll(queryConnections("tcp", 4, pidMap));
        conns.addAll(queryConnections("tcp", 6, pidMap));
        conns.addAll(queryConnections("udp", 4, pidMap));
        conns.addAll(queryConnections("udp", 6, pidMap));
        return conns;
    }

    private static List<IPConnection> queryConnections(String protocol, int ipver, Map<Long, Integer> pidMap) {
        List<IPConnection> conns = new ArrayList<>();
        for (String s : Builder.readFile(ProcPath.NET + "/" + protocol + (ipver == 6 ? "6" : ""))) {
            if (s.indexOf(':') >= 0) {
                String[] split = Pattern.SPACES_PATTERN.split(s.trim());
                if (split.length > 9) {
                    Pair<byte[], Integer> lAddr = parseIpAddr(split[1]);
                    Pair<byte[], Integer> fAddr = parseIpAddr(split[2]);
                    TcpState state = stateLookup(Parsing.hexStringToInt(split[3], 0));
                    Pair<Integer, Integer> txQrxQ = parseHexColonHex(split[4]);
                    long inode = Parsing.parseLongOrDefault(split[9], 0);
                    conns.add(new IPConnection(protocol + ipver, lAddr.getLeft(), lAddr.getRight(), fAddr.getLeft(),
                            fAddr.getRight(), state, txQrxQ.getLeft(), txQrxQ.getRight(),
                            pidMap.getOrDefault(inode, -1)));
                }
            }
        }
        return conns;
    }

    private static Pair<byte[], Integer> parseIpAddr(String s) {
        int colon = s.indexOf(':');
        if (colon > 0 && colon < s.length()) {
            byte[] first = ByteKit.hexStringToByteArray(s.substring(0, colon));
            // Bytes are in __be32 endianness. we must invert each set of 4 bytes
            for (int i = 0; i + 3 < first.length; i += 4) {
                byte tmp = first[i];
                first[i] = first[i + 3];
                first[i + 3] = tmp;
                tmp = first[i + 1];
                first[i + 1] = first[i + 2];
                first[i + 2] = tmp;
            }
            int second = Parsing.hexStringToInt(s.substring(colon + 1), 0);
            return new Pair<>(first, second);
        }
        return new Pair<>(new byte[0], 0);
    }

    private static Pair<Integer, Integer> parseHexColonHex(String s) {
        int colon = s.indexOf(':');
        if (colon > 0 && colon < s.length()) {
            int first = Parsing.hexStringToInt(s.substring(0, colon), 0);
            int second = Parsing.hexStringToInt(s.substring(colon + 1), 0);
            return new Pair<>(first, second);
        }
        return new Pair<>(0, 0);
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

}
