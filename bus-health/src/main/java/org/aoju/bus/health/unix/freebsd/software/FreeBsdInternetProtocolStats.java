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
 ********************************************************************************/
package org.aoju.bus.health.unix.freebsd.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.InternetProtocolStats;
import org.aoju.bus.health.unix.CLibrary;
import org.aoju.bus.health.unix.freebsd.BsdSysctl;

import java.util.List;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.defaultExpiration;
import static org.aoju.bus.health.Memoize.memoize;

/**
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
@ThreadSafe
public class FreeBsdInternetProtocolStats implements InternetProtocolStats {

    private Supplier<Pair<Long, Long>> establishedv4v6 = memoize(FreeBsdInternetProtocolStats::queryTcpnetstat,
            defaultExpiration());
    private Supplier<CLibrary.Tcpstat> tcpstat = memoize(FreeBsdInternetProtocolStats::queryTcpstat, defaultExpiration());
    private Supplier<CLibrary.Udpstat> udpstat = memoize(FreeBsdInternetProtocolStats::queryUdpstat, defaultExpiration());

    private static CLibrary.Tcpstat queryTcpstat() {
        CLibrary.Tcpstat tcpstat = new CLibrary.Tcpstat();
        BsdSysctl.sysctl("net.inet.tcp.stats", tcpstat);
        return tcpstat;
    }

    private static CLibrary.Udpstat queryUdpstat() {
        CLibrary.Udpstat udpstat = new CLibrary.Udpstat();
        BsdSysctl.sysctl("net.inet.udp.stats", udpstat);
        return udpstat;
    }

    private static Pair<Long, Long> queryTcpnetstat() {
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

    @Override
    public TcpStats getTCPv4Stats() {
        CLibrary.Tcpstat tcp = tcpstat.get();
        return new TcpStats(establishedv4v6.get().getLeft(), Builder.unsignedIntToLong(tcp.tcps_connattempt),
                Builder.unsignedIntToLong(tcp.tcps_accepts), Builder.unsignedIntToLong(tcp.tcps_conndrops),
                Builder.unsignedIntToLong(tcp.tcps_drops),
                Builder.unsignedIntToLong(tcp.tcps_snd_swcsum - tcp.tcps_sndrexmitpack),
                Builder.unsignedIntToLong(tcp.tcps_rcv_swcsum), Builder.unsignedIntToLong(tcp.tcps_sndrexmitpack),
                Builder.unsignedIntToLong(
                        tcp.tcps_rcvbadsum + tcp.tcps_rcvbadoff + tcp.tcps_rcvmemdrop + tcp.tcps_rcvshort),
                0L);
    }

    @Override
    public TcpStats getTCPv6Stats() {
        return new TcpStats(establishedv4v6.get().getRight(), 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public UdpStats getUDPv4Stats() {
        CLibrary.Udpstat stat = udpstat.get();
        return new UdpStats(Builder.unsignedIntToLong(stat.udps_snd_swcsum),
                Builder.unsignedIntToLong(stat.udps_rcv_swcsum), Builder.unsignedIntToLong(stat.udps_noportmcast),
                Builder.unsignedIntToLong(stat.udps_hdrops + stat.udps_badsum + stat.udps_badlen));
    }

    @Override
    public UdpStats getUDPv6Stats() {
        CLibrary.Udpstat stat = udpstat.get();
        return new UdpStats(Builder.unsignedIntToLong(stat.udps_snd6_swcsum),
                Builder.unsignedIntToLong(stat.udps_rcv6_swcsum), 0L, 0L);
    }

}
