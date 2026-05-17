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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jna.platform.linux.Udev;
import com.sun.jna.platform.linux.Udev.UdevContext;
import com.sun.jna.platform.linux.Udev.UdevDevice;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.health.linux.SysPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;
import org.miaixz.bus.logger.Logger;

/**
 * LinuxNetworks class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LinuxNetworkIF extends AbstractNetworkIF {

    /**
     * The ifType value.
     */
    private int ifType;

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
    private String ifAlias = Normal.EMPTY;

    /**
     * The ifOperStatus value.
     */
    private NetworkIF.IfOperStatus ifOperStatus = NetworkIF.IfOperStatus.UNKNOWN;

    /**
     * Creates a new LinuxNetworkIF instance.
     *
     * @param netint the netint
     * @throws InstantiationException if the instantiation exception condition occurs
     */
    public LinuxNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint, queryIfModel(netint));
        updateAttributes();
    }

    /**
     * Queries the if model.
     *
     * @param netint the netint
     * @return the query if model result
     */
    private static String queryIfModel(NetworkInterface netint) {
        String name = netint.getName();
        if (!LinuxOperatingSystem.HAS_UDEV) {
            return queryIfModelFromSysfs(name);
        }
        UdevContext udev = Udev.INSTANCE.udev_new();
        if (udev != null) {
            try {
                UdevDevice device = udev.deviceNewFromSyspath(SysPath.NET + name);
                if (device != null) {
                    try {
                        String devVendor = device.getPropertyValue("ID_VENDOR_FROM_DATABASE");
                        String devModel = device.getPropertyValue("ID_MODEL_FROM_DATABASE");
                        if (!StringKit.isBlank(devModel)) {
                            if (!StringKit.isBlank(devVendor)) {
                                return devVendor + Symbol.SPACE + devModel;
                            }
                            return devModel;
                        }
                    } finally {
                        device.unref();
                    }
                }
            } finally {
                udev.unref();
            }
        }
        return name;
    }

    /**
     * Queries the if model from sysfs.
     *
     * @param name the name
     * @return the query if model from sysfs result
     */
    private static String queryIfModelFromSysfs(String name) {
        Map<String, String> uevent = Builder.getKeyValueMapFromFile(SysPath.NET + name + "/uevent", Symbol.EQUAL);
        String devVendor = uevent.get("ID_VENDOR_FROM_DATABASE");
        String devModel = uevent.get("ID_MODEL_FROM_DATABASE");
        if (!StringKit.isBlank(devModel)) {
            if (!StringKit.isBlank(devVendor)) {
                return devVendor + Symbol.SPACE + devModel;
            }
            return devModel;
        }
        return name;
    }

    /**
     * Gets network interfaces on this machine
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        List<NetworkIF> ifList = new ArrayList<>();
        for (NetworkInterface ni : getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new LinuxNetworkIF(ni));
            } catch (InstantiationException e) {
                Logger.debug(
                        false,
                        "Health",
                        "Network Interface Instantiation failed: {}",
                        e.getClass().getSimpleName());
            }
        }
        return ifList;
    }

    /**
     * Parses the if oper status.
     *
     * @param operState the oper state
     * @return the parse if oper status result
     */
    private static NetworkIF.IfOperStatus parseIfOperStatus(String operState) {
        switch (operState) {
            case "up":
                return NetworkIF.IfOperStatus.UP;

            case "down":
                return NetworkIF.IfOperStatus.DOWN;

            case "testing":
                return NetworkIF.IfOperStatus.TESTING;

            case "dormant":
                return NetworkIF.IfOperStatus.DORMANT;

            case "notpresent":
                return NetworkIF.IfOperStatus.NOT_PRESENT;

            case "lowerlayerdown":
                return NetworkIF.IfOperStatus.LOWER_LAYER_DOWN;

            case "unknown":
            default:
                return NetworkIF.IfOperStatus.UNKNOWN;
        }
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
        String name = SysPath.NET + getName();
        try {
            File ifDir = new File(name + "/statistics");
            if (!ifDir.isDirectory()) {
                return false;
            }
        } catch (SecurityException e) {
            return false;
        }

        this.timeStamp = System.currentTimeMillis();
        this.ifType = Builder.getIntFromFile(name + "/type");
        this.connectorPresent = Builder.getIntFromFile(name + "/carrier") > 0;
        this.bytesSent = Builder.getUnsignedLongFromFile(name + "/statistics/tx_bytes");
        this.bytesRecv = Builder.getUnsignedLongFromFile(name + "/statistics/rx_bytes");
        this.packetsSent = Builder.getUnsignedLongFromFile(name + "/statistics/tx_packets");
        this.packetsRecv = Builder.getUnsignedLongFromFile(name + "/statistics/rx_packets");
        this.outErrors = Builder.getUnsignedLongFromFile(name + "/statistics/tx_errors");
        this.inErrors = Builder.getUnsignedLongFromFile(name + "/statistics/rx_errors");
        this.collisions = Builder.getUnsignedLongFromFile(name + "/statistics/collisions");
        this.inDrops = Builder.getUnsignedLongFromFile(name + "/statistics/rx_dropped");
        long speedMbps = Builder.getUnsignedLongFromFile(name + "/speed");
        // speed may be -1 from file.
        this.speed = speedMbps < 0 ? 0 : speedMbps * 1000000L;
        this.ifAlias = Builder.getStringFromFile(name + "/ifalias");
        this.ifOperStatus = parseIfOperStatus(Builder.getStringFromFile(name + "/operstate"));

        return true;
    }

}
