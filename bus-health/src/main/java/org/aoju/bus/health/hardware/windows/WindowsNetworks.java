/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_IFROW;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_IF_ROW2;
import com.sun.jna.platform.win32.VersionHelpers;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.hardware.AbstractNetworks;
import org.aoju.bus.health.hardware.NetworkIF;
import org.aoju.bus.logger.Logger;

/**
 * <p>
 * WindowsNetworks class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public class WindowsNetworks extends AbstractNetworks {

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    /**
     * Updates interface network statistics on the given interface. Statistics
     * include packets and bytes sent and received, and interface speed.
     *
     * @param netIF The interface on which to update statistics
     */
    public static void updateNetworkStats(NetworkIF netIF) {
        if (IS_VISTA_OR_GREATER) {
            MIB_IF_ROW2 ifRow = new MIB_IF_ROW2();
            ifRow.InterfaceIndex = netIF.queryNetworkInterface().getIndex();
            if (0 != IPHlpAPI.INSTANCE.GetIfEntry2(ifRow)) {
                Logger.error("Failed to retrieve data for interface {}, {}", netIF.queryNetworkInterface().getIndex(),
                        netIF.getName());
                return;
            }
            netIF.setBytesSent(ifRow.OutOctets);
            netIF.setBytesRecv(ifRow.InOctets);
            netIF.setPacketsSent(ifRow.OutUcastPkts);
            netIF.setPacketsRecv(ifRow.InUcastPkts);
            netIF.setOutErrors(ifRow.OutErrors);
            netIF.setInErrors(ifRow.InErrors);
            netIF.setSpeed(ifRow.ReceiveLinkSpeed);
        } else {
            MIB_IFROW ifRow = new MIB_IFROW();
            ifRow.dwIndex = netIF.queryNetworkInterface().getIndex();
            if (0 != IPHlpAPI.INSTANCE.GetIfEntry(ifRow)) {
                // Error, abort
                Logger.error("Failed to retrieve data for interface {}, {}", netIF.queryNetworkInterface().getIndex(),
                        netIF.getName());
                return;
            }
            netIF.setBytesSent(Builder.unsignedIntToLong(ifRow.dwOutOctets));
            netIF.setBytesRecv(Builder.unsignedIntToLong(ifRow.dwInOctets));
            netIF.setPacketsSent(Builder.unsignedIntToLong(ifRow.dwOutUcastPkts));
            netIF.setPacketsRecv(Builder.unsignedIntToLong(ifRow.dwInUcastPkts));
            netIF.setOutErrors(Builder.unsignedIntToLong(ifRow.dwOutErrors));
            netIF.setInErrors(Builder.unsignedIntToLong(ifRow.dwInErrors));
            netIF.setSpeed(Builder.unsignedIntToLong(ifRow.dwSpeed));
        }
        netIF.setTimeStamp(System.currentTimeMillis());
    }

}
