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
package org.miaixz.bus.health.unix.freebsd.software;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.sun.jna.Memory;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.unix.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.shared.driver.NetStat;

/**
 * FreeBSD Internet protocol statistics read from 64-bit kernel structures with a command fallback for unknown layouts.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class FreeBsdInternetProtocolStats extends AbstractInternetProtocolStats {

    /**
     * TCP statistics sysctl name.
     */
    private static final String TCPSTAT_SYSCTL = "net.inet.tcp.stats";
    /**
     * UDP statistics sysctl name.
     */
    private static final String UDPSTAT_SYSCTL = "net.inet.udp.stats";
    /**
     * Minimum size of the FreeBSD 10 through 13 TCP statistics structure.
     */
    private static final int TCPSTAT_MIN_SIZE = 880;
    /**
     * Maximum size of a TCP structure before FreeBSD 14 inserted {@code tcps_progdrops}.
     */
    private static final int TCPSTAT_MAX_SIZE_BEFORE_PROGDROPS = 904;
    /**
     * Minimum size of a TCP structure containing {@code tcps_progdrops}.
     */
    private static final int TCPSTAT_MIN_SIZE_WITH_PROGDROPS = 984;
    /**
     * Legacy offset of TCP packets sent.
     */
    private static final int TCPS_SNDPACK = 136;
    /**
     * Legacy offset of retransmitted TCP packets.
     */
    private static final int TCPS_SNDREXMITPACK = 152;
    /**
     * Legacy offset of TCP packets received.
     */
    private static final int TCPS_RCVPACK = 224;
    /**
     * Legacy offset of TCP bad-checksum packets.
     */
    private static final int TCPS_RCVBADSUM = 240;
    /**
     * Legacy offset of TCP bad-offset packets.
     */
    private static final int TCPS_RCVBADOFF = 248;
    /**
     * Legacy offset of short TCP packets.
     */
    private static final int TCPS_RCVSHORT = 264;
    /**
     * Minimum supported UDP statistics structure size.
     */
    private static final int UDPSTAT_SIZE = 112;

    /**
     * Memoized established IPv4 and IPv6 connection counts.
     */
    private final SupplierX<Pair<Long, Long>> established = Memoizer
            .memoize(NetStat::queryTcpnetstat, Memoizer.defaultExpiration());
    /**
     * Memoized IPv4 TCP statistics.
     */
    private final SupplierX<TcpStats> tcpv4 = Memoizer.memoize(this::queryTcpv4Stats, Memoizer.defaultExpiration());
    /**
     * Memoized IPv4 UDP statistics.
     */
    private final SupplierX<UdpStats> udpv4 = Memoizer.memoize(this::queryUdpv4Stats, Memoizer.defaultExpiration());

    /**
     * Constructs a FreeBSD Internet protocol statistics provider.
     */
    public FreeBsdInternetProtocolStats() {
        // No initialization required.
    }

    /**
     * Returns memoized IPv4 TCP statistics obtained from the FreeBSD kernel structure, with a netstat command fallback
     * when the structure layout is unsupported.
     *
     * @return the cached IPv4 TCP counters and established connection count
     */
    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        return tcpv4.get();
    }

    /**
     * Returns memoized IPv4 UDP statistics obtained from the FreeBSD kernel structure, with a netstat command fallback
     * when the structure layout is unsupported.
     *
     * @return the cached IPv4 UDP datagram and error counters
     */
    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        return udpv4.get();
    }

    /**
     * Returns zero-valued IPv6 UDP statistics because this provider does not collect a separate IPv6 UDP snapshot.
     *
     * @return an IPv6 UDP statistics object whose counters are all zero
     */
    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        return new InternetProtocolStats.UdpStats(0L, 0L, 0L, 0L);
    }

    /**
     * Queries IPv4 TCP statistics with a command fallback.
     */
    private TcpStats queryTcpv4Stats() {
        long establishedCount = established.get().getLeft();
        TcpStats stats = parseTcpstat(querySysctl(TCPSTAT_SYSCTL), establishedCount);
        return stats == null ? NetStat.queryTcpStats("netstat -s -p tcp", establishedCount) : stats;
    }

    /**
     * Queries IPv4 UDP statistics with a command fallback.
     */
    private UdpStats queryUdpv4Stats() {
        UdpStats stats = parseUdpstat(querySysctl(UDPSTAT_SYSCTL));
        return stats == null ? NetStat.queryUdpStats("netstat -s -p udp") : stats;
    }

    /**
     * Reads a sysctl value as raw bytes.
     *
     * @param name the sysctl name
     * @return the raw value, or an empty array
     */
    private static byte[] querySysctl(String name) {
        try (Memory memory = BsdSysctlKit.sysctl(name)) {
            return memory == null ? Normal.EMPTY_BYTE_ARRAY : memory.getByteArray(0, (int) memory.size());
        }
    }

    /**
     * Parses a FreeBSD {@code struct tcpstat}.
     *
     * @param value            the raw structure
     * @param establishedCount the current established connection count
     * @return parsed statistics, or {@code null} for an unknown layout
     */
    static TcpStats parseTcpstat(byte[] value, long establishedCount) {
        int shift;
        if (value.length >= TCPSTAT_MIN_SIZE_WITH_PROGDROPS) {
            shift = Normal._8;
        } else if (value.length >= TCPSTAT_MIN_SIZE && value.length <= TCPSTAT_MAX_SIZE_BEFORE_PROGDROPS) {
            shift = Normal._0;
        } else {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.nativeOrder());
        return new TcpStats(establishedCount, buffer.getLong(Normal._0), buffer.getLong(Normal._8),
                buffer.getLong(Normal._32), buffer.getLong(Normal._24), buffer.getLong(TCPS_SNDPACK + shift),
                buffer.getLong(TCPS_RCVPACK + shift), buffer.getLong(TCPS_SNDREXMITPACK + shift),
                buffer.getLong(TCPS_RCVBADSUM + shift) + buffer.getLong(TCPS_RCVBADOFF + shift)
                        + buffer.getLong(TCPS_RCVSHORT + shift),
                0L);
    }

    /**
     * Parses a FreeBSD {@code struct udpstat}.
     *
     * @param value the raw structure
     * @return parsed statistics, or {@code null} for an unknown layout
     */
    static UdpStats parseUdpstat(byte[] value) {
        if (value.length < UDPSTAT_SIZE) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(value).order(ByteOrder.nativeOrder());
        return new UdpStats(buffer.getLong(Normal._80), buffer.getLong(Normal._0),
                buffer.getLong(Normal._40) + buffer.getLong(Normal._48) + buffer.getLong(Normal._96),
                buffer.getLong(Normal._8) + buffer.getLong(Normal._16) + buffer.getLong(Normal._32));
    }

}
