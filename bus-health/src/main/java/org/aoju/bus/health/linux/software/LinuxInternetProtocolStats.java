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
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.InternetProtocolStats;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
@ThreadSafe
public class LinuxInternetProtocolStats implements InternetProtocolStats {

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

    @Override
    public TcpStats getTCPv4Stats() {
        return getTcpStats("netstat -st4");
    }

    @Override
    public TcpStats getTCPv6Stats() {
        // "netstat -st6" returns the same as -st4
        return new TcpStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public UdpStats getUDPv4Stats() {
        return getUdpStats("netstat -su4");
    }

    @Override
    public UdpStats getUDPv6Stats() {
        return getUdpStats("netstat -su6");
    }

}
