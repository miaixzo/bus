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

    /**
     * The IS_VISTA_OR_GREATER constant.
     */
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
    /**
     * The CONNECTOR_PRESENT_BIT constant.
     */
    private static final byte CONNECTOR_PRESENT_BIT = 0b00000100;

    /**
     * The ifType value.
     */
    private int ifType;
    /**
     * The ndisPhysicalMediumType value.
     */
    private int ndisPhysicalMediumType;
    /**
     * The connectorPresent value.
     */
    private boolean connectorPresent;
    /**
     * The bytesRecv value.
     */
    private long bytesRecv;
    /**
     * The bytesSent value.
     */
    private long bytesSent;
    /**
     * The packetsRecv value.
     */
    private long packetsRecv;
    /**
     * The packetsSent value.
     */
    private long packetsSent;
    /**
     * The inErrors value.
     */
    private long inErrors;
    /**
     * The outErrors value.
     */
    private long outErrors;
    /**
     * The inDrops value.
     */
    private long inDrops;
    /**
     * The collisions value.
     */
    private long collisions;
    /**
     * The speed value.
     */
    private long speed;
    /**
     * The timeStamp value.
     */
    private long timeStamp;
    /**
     * The ifAlias value.
     */
    private String ifAlias;
    /**
     * The ifOperStatus value.
     */
    private NetworkIF.IfOperStatus ifOperStatus;

    /**
     * Creates a new WindowsNetworkIF instance.
     *
     * @param netint the netint
     * @throws InstantiationException if the instantiation exception condition occurs
     */
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
                Logger.debug(false, "Health", "Network Interface Instantiation failed: {}", e.getMessage());
            }
        }
        return ifList;
    }

    /**
     * Returns the if type.
     *
     * @return the get if type result
     */
    @Override
    public int getIfType() {
        return this.ifType;
    }

    /**
     * Returns the ndis physical medium type.
     *
     * @return the get ndis physical medium type result
     */
    @Override
    public int getNdisPhysicalMediumType() {
        return this.ndisPhysicalMediumType;
    }

    /**
     * Returns whether the connector present condition is true.
     *
     * @return the is connector present result
     */
    @Override
    public boolean isConnectorPresent() {
        return this.connectorPresent;
    }

    /**
     * Returns the bytes recv.
     *
     * @return the get bytes recv result
     */
    @Override
    public long getBytesRecv() {
        return this.bytesRecv;
    }

    /**
     * Returns the bytes sent.
     *
     * @return the get bytes sent result
     */
    @Override
    public long getBytesSent() {
        return this.bytesSent;
    }

    /**
     * Returns the packets recv.
     *
     * @return the get packets recv result
     */
    @Override
    public long getPacketsRecv() {
        return this.packetsRecv;
    }

    /**
     * Returns the packets sent.
     *
     * @return the get packets sent result
     */
    @Override
    public long getPacketsSent() {
        return this.packetsSent;
    }

    /**
     * Returns the in errors.
     *
     * @return the get in errors result
     */
    @Override
    public long getInErrors() {
        return this.inErrors;
    }

    /**
     * Returns the out errors.
     *
     * @return the get out errors result
     */
    @Override
    public long getOutErrors() {
        return this.outErrors;
    }

    /**
     * Returns the in drops.
     *
     * @return the get in drops result
     */
    @Override
    public long getInDrops() {
        return this.inDrops;
    }

    /**
     * Returns the collisions.
     *
     * @return the get collisions result
     */
    @Override
    public long getCollisions() {
        return this.collisions;
    }

    /**
     * Returns the speed.
     *
     * @return the get speed result
     */
    @Override
    public long getSpeed() {
        return this.speed;
    }

    /**
     * Returns the time stamp.
     *
     * @return the get time stamp result
     */
    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Returns the if alias.
     *
     * @return the get if alias result
     */
    @Override
    public String getIfAlias() {
        return ifAlias;
    }

    /**
     * Returns the if oper status.
     *
     * @return the get if oper status result
     */
    @Override
    public NetworkIF.IfOperStatus getIfOperStatus() {
        return ifOperStatus;
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        // MIB_IFROW2 requires Vista (6.0) or later.
        if (IS_VISTA_OR_GREATER) {
            // Create new MIB_IFROW2 and set index to this interface index
            try (Struct.CloseableMibIfRow2 ifRow = new Struct.CloseableMibIfRow2()) {
                ifRow.InterfaceIndex = queryNetworkInterface().getIndex();
                if (0 != IPHlpAPI.INSTANCE.GetIfEntry2(ifRow)) {
                    // Error, abort
                    Logger.error(false, "Health",
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
                    Logger.error(false, "Health",
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
