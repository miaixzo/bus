/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.health.mac.software;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.software.InternetProtocolStats;
import org.aoju.bus.health.mac.Sysctl;
import org.aoju.bus.health.unix.CLibrary;

import java.util.List;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.defaultExpiration;
import static org.aoju.bus.health.Memoize.memoize;

/**
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
@ThreadSafe
public class MacInternetProtocolStats implements InternetProtocolStats {

    private boolean isElevated;
    private Supplier<Pair<Long, Long>> establishedv4v6 = memoize(MacInternetProtocolStats::queryTcpnetstat,
            defaultExpiration());
    private Supplier<CLibrary.Tcpstat> tcpstat = memoize(MacInternetProtocolStats::queryTcpstat, defaultExpiration());
    private Supplier<CLibrary.Udpstat> udpstat = memoize(MacInternetProtocolStats::queryUdpstat, defaultExpiration());
    // With elevated permissions use tcpstat only
    // Backup estimate get ipstat and subtract off udp
    private Supplier<CLibrary.Ipstat> ipstat = memoize(MacInternetProtocolStats::queryIpstat, defaultExpiration());
    private Supplier<CLibrary.Ip6stat> ip6stat = memoize(MacInternetProtocolStats::queryIp6stat, defaultExpiration());

    public MacInternetProtocolStats(boolean elevated) {
        this.isElevated = elevated;
    }

    private static CLibrary.Tcpstat queryTcpstat() {
        CLibrary.Tcpstat tcpstat = new CLibrary.Tcpstat();
        Sysctl.sysctl("net.inet.tcp.stats", tcpstat);
        return tcpstat;
    }

    private static CLibrary.Ipstat queryIpstat() {
        CLibrary.Ipstat ipstat = new CLibrary.Ipstat();
        Sysctl.sysctl("net.inet.ip.stats", ipstat);
        return ipstat;
    }

    private static CLibrary.Ip6stat queryIp6stat() {
        CLibrary.Ip6stat ip6stat = new CLibrary.Ip6stat();
        Sysctl.sysctl("net.inet6.ip6.stats", ip6stat);
        return ip6stat;
    }

    private static CLibrary.Udpstat queryUdpstat() {
        CLibrary.Udpstat udpstat = new CLibrary.Udpstat();
        Sysctl.sysctl("net.inet.udp.stats", udpstat);
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
        if (this.isElevated) {
            return new TcpStats(establishedv4v6.get().getLeft(), Builder.unsignedIntToLong(tcp.tcps_connattempt),
                    Builder.unsignedIntToLong(tcp.tcps_accepts), Builder.unsignedIntToLong(tcp.tcps_conndrops),
                    Builder.unsignedIntToLong(tcp.tcps_drops),
                    Builder.unsignedIntToLong(tcp.tcps_snd_swcsum - tcp.tcps_sndrexmitpack),
                    Builder.unsignedIntToLong(tcp.tcps_rcv_swcsum),
                    Builder.unsignedIntToLong(tcp.tcps_sndrexmitpack), Builder.unsignedIntToLong(
                    tcp.tcps_rcvbadsum + tcp.tcps_rcvbadoff + tcp.tcps_rcvmemdrop + tcp.tcps_rcvshort),
                    0L);
        }
        CLibrary.Ipstat ip = ipstat.get();
        CLibrary.Udpstat udp = udpstat.get();
        return new TcpStats(establishedv4v6.get().getLeft(), Builder.unsignedIntToLong(tcp.tcps_connattempt),
                Builder.unsignedIntToLong(tcp.tcps_accepts), Builder.unsignedIntToLong(tcp.tcps_conndrops),
                Builder.unsignedIntToLong(tcp.tcps_drops),
                Math.max(0L,
                        Builder.unsignedIntToLong(ip.ips_snd_swcsum - udp.udps_snd_swcsum - tcp.tcps_sndrexmitpack)),
                Math.max(0L, Builder.unsignedIntToLong(ip.ips_rcv_swcsum - udp.udps_rcv_swcsum)),
                Builder.unsignedIntToLong(tcp.tcps_sndrexmitpack),
                Math.max(0L, Builder.unsignedIntToLong(ip.ips_badsum + ip.ips_tooshort + ip.ips_toosmall
                        + ip.ips_badhlen + ip.ips_badlen - udp.udps_hdrops + udp.udps_badsum + udp.udps_badlen)),
                0L);
    }

    @Override
    public TcpStats getTCPv6Stats() {
        CLibrary.Ip6stat ip6 = ip6stat.get();
        CLibrary.Udpstat udp = udpstat.get();
        return new TcpStats(establishedv4v6.get().getRight(), 0L, 0L, 0L, 0L,
                ip6.ip6s_localout - Builder.unsignedIntToLong(udp.udps_snd6_swcsum),
                ip6.ip6s_total - Builder.unsignedIntToLong(udp.udps_rcv6_swcsum), 0L, 0L, 0L);
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
