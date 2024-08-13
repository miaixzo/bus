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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.unix.driver.NetStat;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

import com.sun.jna.Memory;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class FreeBsdInternetProtocolStats extends AbstractInternetProtocolStats {

    private final Supplier<Pair<Long, Long>> establishedv4v6 = Memoizer.memoize(NetStat::queryTcpnetstat,
            Memoizer.defaultExpiration());
    private final Supplier<CLibrary.BsdTcpstat> tcpstat = Memoizer.memoize(FreeBsdInternetProtocolStats::queryTcpstat,
            Memoizer.defaultExpiration());
    private final Supplier<CLibrary.BsdUdpstat> udpstat = Memoizer.memoize(FreeBsdInternetProtocolStats::queryUdpstat,
            Memoizer.defaultExpiration());

    private static CLibrary.BsdTcpstat queryTcpstat() {
        CLibrary.BsdTcpstat ft = new CLibrary.BsdTcpstat();
        try (Memory m = BsdSysctlKit.sysctl("net.inet.tcp.stats")) {
            if (m != null && m.size() >= 128) {
                ft.tcps_connattempt = m.getInt(0);
                ft.tcps_accepts = m.getInt(4);
                ft.tcps_drops = m.getInt(12);
                ft.tcps_conndrops = m.getInt(16);
                ft.tcps_sndpack = m.getInt(64);
                ft.tcps_sndrexmitpack = m.getInt(72);
                ft.tcps_rcvpack = m.getInt(104);
                ft.tcps_rcvbadsum = m.getInt(112);
                ft.tcps_rcvbadoff = m.getInt(116);
                ft.tcps_rcvmemdrop = m.getInt(120);
                ft.tcps_rcvshort = m.getInt(124);
            }
        }
        return ft;
    }

    private static CLibrary.BsdUdpstat queryUdpstat() {
        CLibrary.BsdUdpstat ut = new CLibrary.BsdUdpstat();
        try (Memory m = BsdSysctlKit.sysctl("net.inet.udp.stats")) {
            if (m != null && m.size() >= 1644) {
                ut.udps_ipackets = m.getInt(0);
                ut.udps_hdrops = m.getInt(4);
                ut.udps_badsum = m.getInt(8);
                ut.udps_badlen = m.getInt(12);
                ut.udps_opackets = m.getInt(36);
                ut.udps_noportmcast = m.getInt(48);
                ut.udps_rcv6_swcsum = m.getInt(64);
                ut.udps_snd6_swcsum = m.getInt(80);
            }
        }
        return ut;
    }

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        CLibrary.BsdTcpstat tcp = tcpstat.get();
        return new InternetProtocolStats.TcpStats(establishedv4v6.get().getLeft(),
                Parsing.unsignedIntToLong(tcp.tcps_connattempt), Parsing.unsignedIntToLong(tcp.tcps_accepts),
                Parsing.unsignedIntToLong(tcp.tcps_conndrops), Parsing.unsignedIntToLong(tcp.tcps_drops),
                Parsing.unsignedIntToLong(tcp.tcps_sndpack), Parsing.unsignedIntToLong(tcp.tcps_rcvpack),
                Parsing.unsignedIntToLong(tcp.tcps_sndrexmitpack), Parsing.unsignedIntToLong(
                        tcp.tcps_rcvbadsum + tcp.tcps_rcvbadoff + tcp.tcps_rcvmemdrop + tcp.tcps_rcvshort),
                0L);
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        CLibrary.BsdUdpstat stat = udpstat.get();
        return new InternetProtocolStats.UdpStats(Parsing.unsignedIntToLong(stat.udps_opackets),
                Parsing.unsignedIntToLong(stat.udps_ipackets), Parsing.unsignedIntToLong(stat.udps_noportmcast),
                Parsing.unsignedIntToLong(stat.udps_hdrops + stat.udps_badsum + stat.udps_badlen));
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        CLibrary.BsdUdpstat stat = udpstat.get();
        return new InternetProtocolStats.UdpStats(Parsing.unsignedIntToLong(stat.udps_snd6_swcsum),
                Parsing.unsignedIntToLong(stat.udps_rcv6_swcsum), 0L, 0L);
    }
}
