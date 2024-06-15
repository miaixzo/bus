/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.driver.proc.ProcessStat;
import org.miaixz.bus.health.unix.driver.NetStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class LinuxInternetProtocolStats extends AbstractInternetProtocolStats {

    private static List<InternetProtocolStats.IPConnection> queryConnections(String protocol, int ipver, Map<Long, Integer> pidMap) {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        for (String s : Builder.readFile(ProcPath.NET + "/" + protocol + (ipver == 6 ? "6" : Normal.EMPTY))) {
            if (s.indexOf(Symbol.C_COLON) >= 0) {
                String[] split = Pattern.SPACES_PATTERN.split(s.trim());
                if (split.length > 9) {
                    Pair<byte[], Integer> lAddr = parseIpAddr(split[1]);
                    Pair<byte[], Integer> fAddr = parseIpAddr(split[2]);
                    InternetProtocolStats.TcpState state = stateLookup(Parsing.hexStringToInt(split[3], 0));
                    Pair<Integer, Integer> txQrxQ = parseHexColonHex(split[4]);
                    long inode = Parsing.parseLongOrDefault(split[9], 0);
                    conns.add(new InternetProtocolStats.IPConnection(protocol + ipver, lAddr.getLeft(), lAddr.getRight(), fAddr.getLeft(), fAddr.getRight(),
                            state, txQrxQ.getLeft(), txQrxQ.getRight(), pidMap.getOrDefault(inode, -1)));
                }
            }
        }
        return conns;
    }

    private static Pair<byte[], Integer> parseIpAddr(String s) {
        int colon = s.indexOf(Symbol.C_COLON);
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
            return Pair.of(first, second);
        }
        return Pair.of(new byte[0], 0);
    }

    private static Pair<Integer, Integer> parseHexColonHex(String s) {
        int colon = s.indexOf(Symbol.C_COLON);
        if (colon > 0 && colon < s.length()) {
            int first = Parsing.hexStringToInt(s.substring(0, colon), 0);
            int second = Parsing.hexStringToInt(s.substring(colon + 1), 0);
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
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        return NetStat.queryTcpStats("netstat -st4");
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        return NetStat.queryUdpStats("netstat -su4");
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        return NetStat.queryUdpStats("netstat -su6");
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
}
