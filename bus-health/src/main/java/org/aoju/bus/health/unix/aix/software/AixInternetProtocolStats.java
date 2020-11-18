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
package org.aoju.bus.health.unix.aix.software;

import com.sun.jna.Native;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.software.InternetProtocolStats;
import org.aoju.bus.health.unix.aix.Perfstat;
import org.aoju.bus.health.unix.aix.drivers.perfstat.PerfstatProtocol;

import java.util.function.Supplier;

/**
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
@ThreadSafe
public class AixInternetProtocolStats implements InternetProtocolStats {

    private Supplier<Perfstat.perfstat_protocol_t[]> ipstats = Memoize.memoize(PerfstatProtocol::queryProtocols, Memoize.defaultExpiration());

    @Override
    public TcpStats getTCPv4Stats() {
        for (Perfstat.perfstat_protocol_t stat : ipstats.get()) {
            if ("tcp".equals(Native.toString(stat.name))) {
                return new TcpStats(stat.u.tcp.established, stat.u.tcp.initiated, stat.u.tcp.accepted,
                        stat.u.tcp.dropped, stat.u.tcp.dropped, stat.u.tcp.opackets, stat.u.tcp.ipackets, 0L,
                        stat.u.tcp.ierrors, 0L);
            }
        }
        return new TcpStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public TcpStats getTCPv6Stats() {
        // Stats are no different for inet6
        return new TcpStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public UdpStats getUDPv4Stats() {
        for (Perfstat.perfstat_protocol_t stat : ipstats.get()) {
            if ("udp".equals(Native.toString(stat.name))) {
                return new UdpStats(stat.u.udp.opackets, stat.u.udp.ipackets, stat.u.udp.no_socket, stat.u.udp.ierrors);
            }
        }
        return new UdpStats(0L, 0L, 0L, 0L);
    }

    @Override
    public UdpStats getUDPv6Stats() {
        // Stats are no different for inet6
        return new UdpStats(0L, 0L, 0L, 0L);
    }

}
