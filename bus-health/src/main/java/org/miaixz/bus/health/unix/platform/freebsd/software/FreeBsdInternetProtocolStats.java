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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import java.util.function.Supplier;

import com.sun.jna.Memory;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.unix.driver.NetStat;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class FreeBsdInternetProtocolStats extends AbstractInternetProtocolStats {

    /**
     * Constructs a new FreeBsdInternetProtocolStats instance.
     */
    public FreeBsdInternetProtocolStats() {
        // No initialization required.
    }

    /**
     * The establishedv4v6 value.
     */
    private final Supplier<Pair<Long, Long>> establishedv4v6 = Memoizer
            .memoize(NetStat::queryTcpnetstat, Memoizer.defaultExpiration());

    /**
     * The tcpstat value.
     */
    private final Supplier<CLibrary.BsdTcpstat> tcpstat = Memoizer
            .memoize(FreeBsdInternetProtocolStats::queryTcpstat, Memoizer.defaultExpiration());

    /**
     * The udpstat value.
     */
    private final Supplier<CLibrary.BsdUdpstat> udpstat = Memoizer
            .memoize(FreeBsdInternetProtocolStats::queryUdpstat, Memoizer.defaultExpiration());

    /**
     * Queries the tcpstat.
     *
     * @return the query tcpstat result
     */
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

    /**
     * Queries the udpstat.
     *
     * @return the query udpstat result
     */
    private static CLibrary.BsdUdpstat queryUdpstat() {
        CLibrary.BsdUdpstat ut = new CLibrary.BsdUdpstat();
        try (Memory m = BsdSysctlKit.sysctl("net.inet.udp.stats")) {
            if (m != null && m.size() >= 84) {
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

    /**
     * Returns the tc pv4 stats.
     *
     * @return the get tc pv4 stats result
     */
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

    /**
     * Returns the ud pv4 stats.
     *
     * @return the get ud pv4 stats result
     */
    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        CLibrary.BsdUdpstat stat = udpstat.get();
        return new InternetProtocolStats.UdpStats(Parsing.unsignedIntToLong(stat.udps_opackets),
                Parsing.unsignedIntToLong(stat.udps_ipackets), Parsing.unsignedIntToLong(stat.udps_noportmcast),
                Parsing.unsignedIntToLong(stat.udps_hdrops + stat.udps_badsum + stat.udps_badlen));
    }

    /**
     * Returns the ud pv6 stats.
     *
     * @return the get ud pv6 stats result
     */
    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        CLibrary.BsdUdpstat stat = udpstat.get();
        return new InternetProtocolStats.UdpStats(Parsing.unsignedIntToLong(stat.udps_snd6_swcsum),
                Parsing.unsignedIntToLong(stat.udps_rcv6_swcsum), 0L, 0L);
    }

}
