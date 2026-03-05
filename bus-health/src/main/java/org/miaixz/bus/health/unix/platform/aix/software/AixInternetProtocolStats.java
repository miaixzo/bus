/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.aix.software;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatProtocol;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_protocol_t;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class AixInternetProtocolStats extends AbstractInternetProtocolStats {

    private final Supplier<perfstat_protocol_t[]> ipstats = Memoizer
            .memoize(PerfstatProtocol::queryProtocols, Memoizer.defaultExpiration());

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        for (perfstat_protocol_t stat : ipstats.get()) {
            if ("tcp".equals(Native.toString(stat.name))) {
                return new InternetProtocolStats.TcpStats(stat.u.tcp.established, stat.u.tcp.initiated,
                        stat.u.tcp.accepted, stat.u.tcp.dropped, stat.u.tcp.dropped, stat.u.tcp.opackets,
                        stat.u.tcp.ipackets, 0L, stat.u.tcp.ierrors, 0L);
            }
        }
        return new InternetProtocolStats.TcpStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        for (perfstat_protocol_t stat : ipstats.get()) {
            if ("udp".equals(Native.toString(stat.name))) {
                return new InternetProtocolStats.UdpStats(stat.u.udp.opackets, stat.u.udp.ipackets,
                        stat.u.udp.no_socket, stat.u.udp.ierrors);
            }
        }
        return new InternetProtocolStats.UdpStats(0L, 0L, 0L, 0L);
    }

}
