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
package org.miaixz.bus.health.windows.hardware;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.IPHlpAPI;
import com.sun.jna.platform.win32.VersionHelpers;

/**
 * WindowsNetworks class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class WindowsNetworkIF extends AbstractNetworkIF {

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    private static final byte CONNECTOR_PRESENT_BIT = 0b00000100;

    private int ifType;
    private int ndisPhysicalMediumType;
    private boolean connectorPresent;
    private long bytesRecv;
    private long bytesSent;
    private long packetsRecv;
    private long packetsSent;
    private long inErrors;
    private long outErrors;
    private long inDrops;
    private long collisions;
    private long speed;
    private long timeStamp;
    private String ifAlias;
    private NetworkIF.IfOperStatus ifOperStatus;

    public WindowsNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint);
        updateAttributes();
    }

    /**
     * Gets all network interfaces on this machine
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        List<NetworkIF> ifList = new ArrayList<>();
        for (NetworkInterface ni : getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new WindowsNetworkIF(ni));
            } catch (InstantiationException e) {
                Logger.debug("Network Interface Instantiation failed: {}", e.getMessage());
            }
        }
        return ifList;
    }

    @Override
    public int getIfType() {
        return this.ifType;
    }

    @Override
    public int getNdisPhysicalMediumType() {
        return this.ndisPhysicalMediumType;
    }

    @Override
    public boolean isConnectorPresent() {
        return this.connectorPresent;
    }

    @Override
    public long getBytesRecv() {
        return this.bytesRecv;
    }

    @Override
    public long getBytesSent() {
        return this.bytesSent;
    }

    @Override
    public long getPacketsRecv() {
        return this.packetsRecv;
    }

    @Override
    public long getPacketsSent() {
        return this.packetsSent;
    }

    @Override
    public long getInErrors() {
        return this.inErrors;
    }

    @Override
    public long getOutErrors() {
        return this.outErrors;
    }

    @Override
    public long getInDrops() {
        return this.inDrops;
    }

    @Override
    public long getCollisions() {
        return this.collisions;
    }

    @Override
    public long getSpeed() {
        return this.speed;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public String getIfAlias() {
        return ifAlias;
    }

    @Override
    public NetworkIF.IfOperStatus getIfOperStatus() {
        return ifOperStatus;
    }

    @Override
    public boolean updateAttributes() {
        // MIB_IFROW2 requires Vista (6.0) or later.
        if (IS_VISTA_OR_GREATER) {
            // Create new MIB_IFROW2 and set index to this interface index
            try (Struct.CloseableMibIfRow2 ifRow = new Struct.CloseableMibIfRow2()) {
                ifRow.InterfaceIndex = queryNetworkInterface().getIndex();
                if (0 != IPHlpAPI.INSTANCE.GetIfEntry2(ifRow)) {
                    // Error, abort
                    Logger.error(
                            "Failed to retrieve data for interface {}, {}",
                            queryNetworkInterface().getIndex(),
                            getName());
                    return false;
                }
                this.ifType = ifRow.Type;
                this.ndisPhysicalMediumType = ifRow.PhysicalMediumType;
                this.connectorPresent = (ifRow.InterfaceAndOperStatusFlags & CONNECTOR_PRESENT_BIT) > 0;
                this.bytesSent = ifRow.OutOctets;
                this.bytesRecv = ifRow.InOctets;
                this.packetsSent = ifRow.OutUcastPkts;
                this.packetsRecv = ifRow.InUcastPkts;
                this.outErrors = ifRow.OutErrors;
                this.inErrors = ifRow.InErrors;
                this.collisions = ifRow.OutDiscards; // closest proxy
                this.inDrops = ifRow.InDiscards; // closest proxy
                this.speed = ifRow.ReceiveLinkSpeed;
                this.ifAlias = Native.toString(ifRow.Alias);
                this.ifOperStatus = NetworkIF.IfOperStatus.byValue(ifRow.OperStatus);
            }
        } else {
            // Create new MIB_IFROW and set index to this interface index
            try (Struct.CloseableMibIfRow ifRow = new Struct.CloseableMibIfRow()) {
                ifRow.dwIndex = queryNetworkInterface().getIndex();
                if (0 != IPHlpAPI.INSTANCE.GetIfEntry(ifRow)) {
                    // Error, abort
                    Logger.error(
                            "Failed to retrieve data for interface {}, {}",
                            queryNetworkInterface().getIndex(),
                            getName());
                    return false;
                }
                this.ifType = ifRow.dwType;
                // These are unsigned ints. Widen them to longs.
                this.bytesSent = Parsing.unsignedIntToLong(ifRow.dwOutOctets);
                this.bytesRecv = Parsing.unsignedIntToLong(ifRow.dwInOctets);
                this.packetsSent = Parsing.unsignedIntToLong(ifRow.dwOutUcastPkts);
                this.packetsRecv = Parsing.unsignedIntToLong(ifRow.dwInUcastPkts);
                this.outErrors = Parsing.unsignedIntToLong(ifRow.dwOutErrors);
                this.inErrors = Parsing.unsignedIntToLong(ifRow.dwInErrors);
                this.collisions = Parsing.unsignedIntToLong(ifRow.dwOutDiscards); // closest proxy
                this.inDrops = Parsing.unsignedIntToLong(ifRow.dwInDiscards); // closest proxy
                this.speed = Parsing.unsignedIntToLong(ifRow.dwSpeed);
                this.ifAlias = Normal.EMPTY; // not supported by MIB_IFROW
                this.ifOperStatus = NetworkIF.IfOperStatus.UNKNOWN; // not supported
            }
        }
        this.timeStamp = System.currentTimeMillis();
        return true;
    }

}
