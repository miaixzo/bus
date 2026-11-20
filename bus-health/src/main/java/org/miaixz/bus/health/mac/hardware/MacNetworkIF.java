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
package org.miaixz.bus.health.mac.hardware;

import java.net.NetworkInterface;
import java.util.List;
import java.util.Map;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.health.mac.driver.net.NetStat;
import org.miaixz.bus.health.mac.jna.SystemConfiguration;

/**
 * <p>
 * MacNetworkIF class.
 * </p>
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class MacNetworkIF extends AbstractNetworkIF {

    /**
     * The ifType value.
     */
    private volatile int ifType;

    /**
     * The bytesRecv value.
     */
    private volatile long bytesRecv;

    /**
     * The bytesSent value.
     */
    private volatile long bytesSent;

    /**
     * The packetsRecv value.
     */
    private volatile long packetsRecv;

    /**
     * The packetsSent value.
     */
    private volatile long packetsSent;

    /**
     * The inErrors value.
     */
    private volatile long inErrors;

    /**
     * The outErrors value.
     */
    private volatile long outErrors;

    /**
     * The inDrops value.
     */
    private volatile long inDrops;

    /**
     * The collisions value.
     */
    private volatile long collisions;

    /**
     * The speed value.
     */
    private volatile long speed;

    /**
     * The timeStamp value.
     */
    private volatile long timeStamp;

    /**
     * Constructs a new {@code MacNetworkIF} object.
     *
     * @param netint The {@link NetworkInterface} object.
     * @param data   A map of network interface statistics with the index as the key.
     * @throws InstantiationException If the network interface cannot be properly initialized.
     */
    public MacNetworkIF(NetworkInterface netint, Map<Integer, NetStat.IFdata> data) throws InstantiationException {
        super(netint, queryIfDisplayName(netint));
        updateNetworkStats(data);
    }

    /**
     * Queries the display name for a given network interface.
     *
     * @param netint The {@link NetworkInterface} to query.
     * @return The localized display name of the network interface, or its BSD name if the display name cannot be found.
     */
    private static String queryIfDisplayName(NetworkInterface netint) {
        String name = netint.getName();
        CFArrayRef ifArray = SystemConfiguration.INSTANCE.SCNetworkInterfaceCopyAll();
        if (ifArray != null) {
            try {
                int count = ifArray.getCount();
                for (int i = 0; i < count; i++) {
                    Pointer pNetIf = ifArray.getValueAtIndex(i);
                    SystemConfiguration.SCNetworkInterfaceRef scNetIf = new SystemConfiguration.SCNetworkInterfaceRef(
                            pNetIf);
                    CFStringRef cfName = SystemConfiguration.INSTANCE.SCNetworkInterfaceGetBSDName(scNetIf);
                    if (cfName != null && name.equals(cfName.stringValue())) {
                        CFStringRef cfDisplayName = SystemConfiguration.INSTANCE
                                .SCNetworkInterfaceGetLocalizedDisplayName(scNetIf);
                        if (cfDisplayName != null) {
                            return cfDisplayName.stringValue();
                        }
                    }
                }
            } finally {
                ifArray.release();
            }
        }
        return name;
    }

    /**
     * Gets all network interfaces on this machine.
     *
     * @param includeLocalInterfaces Whether to include local (loopback) interfaces in the result.
     * @return A list of {@link NetworkIF} objects representing the interfaces.
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        // One time fetch of stats
        final Map<Integer, NetStat.IFdata> data = NetStat.queryIFdata(-1);
        return getNetworks(includeLocalInterfaces, ni -> new MacNetworkIF(ni, data));
    }

    /**
     * (Windows, macOS) The NDIS Interface Type. NDIS interface types are registered with the Internet Assigned Numbers
     * Authority (IANA), which publishes a list of interface types periodically in the Assigned Numbers RFC, or in a
     * derivative of it that is specific to Internet network management number assignments. (Linux) ARP Protocol
     * hardware identifiers defined in {@code include/uapi/linux/if_arp.h}
     *
     * @return the ifType
     */
    @Override
    public int getIfType() {
        return this.ifType;
    }

    /**
     * Getter for the field <code>bytesRecv</code>.
     *
     * @return The Bytes Received. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getBytesRecv() {
        return this.bytesRecv;
    }

    /**
     * Getter for the field <code>bytesSent</code>.
     *
     * @return The Bytes Sent. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     *         To update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getBytesSent() {
        return this.bytesSent;
    }

    /**
     * Getter for the field <code>packetsRecv</code>.
     *
     * @return The Packets Received. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getPacketsRecv() {
        return this.packetsRecv;
    }

    /**
     * Getter for the field <code>packetsSent</code>.
     *
     * @return The Packets Sent. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     *         To update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getPacketsSent() {
        return this.packetsSent;
    }

    /**
     * Getter for the field <code>inErrors</code>.
     *
     * @return Input Errors. This value is set when the {@link NetworkIF} is instantiated and may not be up to date. To
     *         update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getInErrors() {
        return this.inErrors;
    }

    /**
     * Getter for the field <code>outErrors</code>.
     *
     * @return The Output Errors. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    @Override
    public long getOutErrors() {
        return this.outErrors;
    }

    /**
     * Getter for the field <code>inDrops</code>.
     *
     * @return Incoming/Received dropped packets. On Windows, returns discarded incoming packets. This value is set when
     *         the {@link NetworkIF} is instantiated and may not be up to date. To update this value, execute the
     *         {@link #updateAttributes()} method
     */
    @Override
    public long getInDrops() {
        return this.inDrops;
    }

    /**
     * Getter for the field <code>collisions</code>.
     *
     * @return Packet collisions. On Windows, returns discarded outgoing packets. This value is set when the
     *         {@link NetworkIF} is instantiated and may not be up to date. To update this value, execute the
     *         {@link #updateAttributes()} method
     */
    @Override
    public long getCollisions() {
        return this.collisions;
    }

    /**
     * Getter for the field <code>speed</code>.
     *
     * @return The speed of the network interface in bits per second. This value is set when the {@link NetworkIF} is
     *         instantiated and may not be up to date. To update this value, execute the {@link #updateAttributes()}
     *         method
     */
    @Override
    public long getSpeed() {
        return this.speed;
    }

    /**
     * Getter for the field <code>timeStamp</code>.
     *
     * @return Returns the timeStamp.
     */
    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Updates interface network statistics on this interface. Statistics include packets and bytes sent and received,
     * and interface speed.
     *
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    @Override
    public synchronized boolean updateAttributes() {
        int index = queryNetworkInterface().getIndex();
        return updateNetworkStats(NetStat.queryIFdata(index));
    }

    /**
     * Updates interface network statistics on the given interface. Statistics include packets and bytes sent and
     * received, and interface speed.
     *
     * @param data A map of network interface statistics with the index as the key.
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    private boolean updateNetworkStats(Map<Integer, NetStat.IFdata> data) {
        int index = queryNetworkInterface().getIndex();
        if (data.containsKey(index)) {
            NetStat.IFdata ifData = data.get(index);
            // Update data
            this.ifType = ifData.getIfType();
            this.bytesSent = ifData.getOBytes();
            this.bytesRecv = ifData.getIBytes();
            this.packetsSent = ifData.getOPackets();
            this.packetsRecv = ifData.getIPackets();
            this.outErrors = ifData.getOErrors();
            this.inErrors = ifData.getIErrors();
            this.collisions = ifData.getCollisions();
            this.inDrops = ifData.getIDrops();
            this.speed = ifData.getSpeed();
            this.timeStamp = ifData.getTimeStamp();
            return true;
        }
        return false;
    }

}
