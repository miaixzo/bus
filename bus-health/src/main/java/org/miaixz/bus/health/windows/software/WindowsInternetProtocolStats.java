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
package org.miaixz.bus.health.windows.software;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.WinError;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class WindowsInternetProtocolStats extends AbstractInternetProtocolStats {

    private static final IPHlpAPI IPHLP = IPHlpAPI.INSTANCE;

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    private static List<InternetProtocolStats.IPConnection> queryTCPv4Connections() {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        // Get size needed
        try (ByRef.CloseableIntByReference sizePtr = new ByRef.CloseableIntByReference()) {
            int ret = IPHLP.GetExtendedTcpTable(null, sizePtr, false, IPHlpAPI.AF_INET,
                    IPHlpAPI.TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);
            // Get buffer and populate table
            int size = sizePtr.getValue();
            Memory buf = new Memory(size);
            do {
                ret = IPHLP.GetExtendedTcpTable(buf, sizePtr, false, IPHlpAPI.AF_INET,
                        IPHlpAPI.TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);
                if (ret == WinError.ERROR_INSUFFICIENT_BUFFER) {
                    size = sizePtr.getValue();
                    buf.close();
                    buf = new Memory(size);
                }
            } while (ret == WinError.ERROR_INSUFFICIENT_BUFFER);
            IPHlpAPI.MIB_TCPTABLE_OWNER_PID tcpTable = new IPHlpAPI.MIB_TCPTABLE_OWNER_PID(buf);
            for (int i = 0; i < tcpTable.dwNumEntries; i++) {
                IPHlpAPI.MIB_TCPROW_OWNER_PID row = tcpTable.table[i];
                conns.add(new InternetProtocolStats.IPConnection("tcp4", Parsing.parseIntToIP(row.dwLocalAddr),
                        Parsing.bigEndian16ToLittleEndian(row.dwLocalPort), Parsing.parseIntToIP(row.dwRemoteAddr),
                        Parsing.bigEndian16ToLittleEndian(row.dwRemotePort), stateLookup(row.dwState), 0, 0,
                        row.dwOwningPid));
            }
            buf.close();
        }
        return conns;
    }

    private static List<InternetProtocolStats.IPConnection> queryTCPv6Connections() {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        // Get size needed
        try (ByRef.CloseableIntByReference sizePtr = new ByRef.CloseableIntByReference()) {
            int ret = IPHLP.GetExtendedTcpTable(null, sizePtr, false, IPHlpAPI.AF_INET6,
                    IPHlpAPI.TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);
            // Get buffer and populate table
            int size = sizePtr.getValue();
            Memory buf = new Memory(size);
            do {
                ret = IPHLP.GetExtendedTcpTable(buf, sizePtr, false, IPHlpAPI.AF_INET6,
                        IPHlpAPI.TCP_TABLE_CLASS.TCP_TABLE_OWNER_PID_ALL, 0);
                if (ret == WinError.ERROR_INSUFFICIENT_BUFFER) {
                    size = sizePtr.getValue();
                    buf.close();
                    buf = new Memory(size);
                }
            } while (ret == WinError.ERROR_INSUFFICIENT_BUFFER);
            IPHlpAPI.MIB_TCP6TABLE_OWNER_PID tcpTable = new IPHlpAPI.MIB_TCP6TABLE_OWNER_PID(buf);
            for (int i = 0; i < tcpTable.dwNumEntries; i++) {
                IPHlpAPI.MIB_TCP6ROW_OWNER_PID row = tcpTable.table[i];
                conns.add(new InternetProtocolStats.IPConnection("tcp6", row.LocalAddr,
                        Parsing.bigEndian16ToLittleEndian(row.dwLocalPort), row.RemoteAddr,
                        Parsing.bigEndian16ToLittleEndian(row.dwRemotePort), stateLookup(row.State), 0, 0,
                        row.dwOwningPid));
            }
            buf.close();
        }
        return conns;
    }

    private static List<InternetProtocolStats.IPConnection> queryUDPv4Connections() {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        // Get size needed
        try (ByRef.CloseableIntByReference sizePtr = new ByRef.CloseableIntByReference()) {
            int ret = IPHLP.GetExtendedUdpTable(null, sizePtr, false, IPHlpAPI.AF_INET,
                    IPHlpAPI.UDP_TABLE_CLASS.UDP_TABLE_OWNER_PID, 0);
            // Get buffer and populate table
            int size = sizePtr.getValue();
            Memory buf = new Memory(size);
            do {
                ret = IPHLP.GetExtendedUdpTable(buf, sizePtr, false, IPHlpAPI.AF_INET,
                        IPHlpAPI.UDP_TABLE_CLASS.UDP_TABLE_OWNER_PID, 0);
                if (ret == WinError.ERROR_INSUFFICIENT_BUFFER) {
                    size = sizePtr.getValue();
                    buf.close();
                    buf = new Memory(size);
                }
            } while (ret == WinError.ERROR_INSUFFICIENT_BUFFER);
            IPHlpAPI.MIB_UDPTABLE_OWNER_PID udpTable = new IPHlpAPI.MIB_UDPTABLE_OWNER_PID(buf);
            for (int i = 0; i < udpTable.dwNumEntries; i++) {
                IPHlpAPI.MIB_UDPROW_OWNER_PID row = udpTable.table[i];
                conns.add(new InternetProtocolStats.IPConnection("udp4", Parsing.parseIntToIP(row.dwLocalAddr),
                        Parsing.bigEndian16ToLittleEndian(row.dwLocalPort), new byte[0], 0,
                        InternetProtocolStats.TcpState.NONE, 0, 0, row.dwOwningPid));
            }
            buf.close();
        }
        return conns;
    }

    private static List<InternetProtocolStats.IPConnection> queryUDPv6Connections() {
        List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
        // Get size needed
        try (ByRef.CloseableIntByReference sizePtr = new ByRef.CloseableIntByReference()) {
            int ret = IPHLP.GetExtendedUdpTable(null, sizePtr, false, IPHlpAPI.AF_INET6,
                    IPHlpAPI.UDP_TABLE_CLASS.UDP_TABLE_OWNER_PID, 0);
            // Get buffer and populate table
            int size = sizePtr.getValue();
            Memory buf = new Memory(size);
            do {
                ret = IPHLP.GetExtendedUdpTable(buf, sizePtr, false, IPHlpAPI.AF_INET6,
                        IPHlpAPI.UDP_TABLE_CLASS.UDP_TABLE_OWNER_PID, 0);
                if (ret == WinError.ERROR_INSUFFICIENT_BUFFER) {
                    size = sizePtr.getValue();
                    buf.close();
                    buf = new Memory(size);
                }
            } while (ret == WinError.ERROR_INSUFFICIENT_BUFFER);
            IPHlpAPI.MIB_UDP6TABLE_OWNER_PID udpTable = new IPHlpAPI.MIB_UDP6TABLE_OWNER_PID(buf);
            for (int i = 0; i < udpTable.dwNumEntries; i++) {
                IPHlpAPI.MIB_UDP6ROW_OWNER_PID row = udpTable.table[i];
                conns.add(new InternetProtocolStats.IPConnection("udp6", row.ucLocalAddr,
                        Parsing.bigEndian16ToLittleEndian(row.dwLocalPort), new byte[0], 0,
                        InternetProtocolStats.TcpState.NONE, 0, 0, row.dwOwningPid));
            }
        }
        return conns;
    }

    private static InternetProtocolStats.TcpState stateLookup(int state) {
        switch (state) {
        case 1:
        case 12:
            return InternetProtocolStats.TcpState.CLOSED;
        case 2:
            return InternetProtocolStats.TcpState.LISTEN;
        case 3:
            return InternetProtocolStats.TcpState.SYN_SENT;
        case 4:
            return InternetProtocolStats.TcpState.SYN_RECV;
        case 5:
            return InternetProtocolStats.TcpState.ESTABLISHED;
        case 6:
            return InternetProtocolStats.TcpState.FIN_WAIT_1;
        case 7:
            return InternetProtocolStats.TcpState.FIN_WAIT_2;
        case 8:
            return InternetProtocolStats.TcpState.CLOSE_WAIT;
        case 9:
            return InternetProtocolStats.TcpState.CLOSING;
        case 10:
            return InternetProtocolStats.TcpState.LAST_ACK;
        case 11:
            return InternetProtocolStats.TcpState.TIME_WAIT;
        default:
            return InternetProtocolStats.TcpState.UNKNOWN;
        }
    }

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        try (Struct.CloseableMibTcpStats stats = new Struct.CloseableMibTcpStats()) {
            IPHLP.GetTcpStatisticsEx(stats, IPHlpAPI.AF_INET);
            return new InternetProtocolStats.TcpStats(stats.dwCurrEstab, stats.dwActiveOpens, stats.dwPassiveOpens,
                    stats.dwAttemptFails, stats.dwEstabResets, stats.dwOutSegs, stats.dwInSegs, stats.dwRetransSegs,
                    stats.dwInErrs, stats.dwOutRsts);
        }
    }

    @Override
    public InternetProtocolStats.TcpStats getTCPv6Stats() {
        try (Struct.CloseableMibTcpStats stats = new Struct.CloseableMibTcpStats()) {
            IPHLP.GetTcpStatisticsEx(stats, IPHlpAPI.AF_INET6);
            return new InternetProtocolStats.TcpStats(stats.dwCurrEstab, stats.dwActiveOpens, stats.dwPassiveOpens,
                    stats.dwAttemptFails, stats.dwEstabResets, stats.dwOutSegs, stats.dwInSegs, stats.dwRetransSegs,
                    stats.dwInErrs, stats.dwOutRsts);
        }
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        try (Struct.CloseableMibUdpStats stats = new Struct.CloseableMibUdpStats()) {
            IPHLP.GetUdpStatisticsEx(stats, IPHlpAPI.AF_INET);
            return new InternetProtocolStats.UdpStats(stats.dwOutDatagrams, stats.dwInDatagrams, stats.dwNoPorts,
                    stats.dwInErrors);
        }
    }

    @Override
    public List<InternetProtocolStats.IPConnection> getConnections() {
        if (IS_VISTA_OR_GREATER) {
            List<InternetProtocolStats.IPConnection> conns = new ArrayList<>();
            conns.addAll(queryTCPv4Connections());
            conns.addAll(queryTCPv6Connections());
            conns.addAll(queryUDPv4Connections());
            conns.addAll(queryUDPv6Connections());
            return conns;
        }
        return Collections.emptyList();
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv6Stats() {
        try (Struct.CloseableMibUdpStats stats = new Struct.CloseableMibUdpStats()) {
            IPHLP.GetUdpStatisticsEx(stats, IPHlpAPI.AF_INET6);
            return new InternetProtocolStats.UdpStats(stats.dwOutDatagrams, stats.dwInDatagrams, stats.dwNoPorts,
                    stats.dwInErrors);
        }
    }

}
