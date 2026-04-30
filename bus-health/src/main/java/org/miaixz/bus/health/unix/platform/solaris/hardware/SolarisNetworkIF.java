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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;

/**
 * SolarisNetworks class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SolarisNetworkIF extends AbstractNetworkIF {

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
     * Creates a new SolarisNetworkIF instance.
     *
     * @param netint the netint
     * @throws InstantiationException if the instantiation exception condition occurs
     */
    public SolarisNetworkIF(NetworkInterface netint) throws InstantiationException {
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
                ifList.add(new SolarisNetworkIF(ni));
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
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        // Initialize to a sane default value
        this.timeStamp = System.currentTimeMillis();
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return updateAttributes2();
        }
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup("link", -1, getName());
            if (ksp == null) { // Solaris 10 compatibility
                ksp = kc.lookup(null, -1, getName());
            }
            if (ksp != null && kc.read(ksp)) {
                this.bytesSent = KstatKit.dataLookupLong(ksp, "obytes64");
                this.bytesRecv = KstatKit.dataLookupLong(ksp, "rbytes64");
                this.packetsSent = KstatKit.dataLookupLong(ksp, "opackets64");
                this.packetsRecv = KstatKit.dataLookupLong(ksp, "ipackets64");
                this.outErrors = KstatKit.dataLookupLong(ksp, "oerrors");
                this.inErrors = KstatKit.dataLookupLong(ksp, "ierrors");
                this.collisions = KstatKit.dataLookupLong(ksp, "collisions");
                this.inDrops = KstatKit.dataLookupLong(ksp, "dl_idrops");
                this.speed = KstatKit.dataLookupLong(ksp, "ifspeed");
                // Snap time in ns; convert to ms
                this.timeStamp = ksp.ks_snaptime / 1_000_000L;
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the attributes2.
     *
     * @return the update attributes2 result
     */
    private boolean updateAttributes2() {
        Object[] results = KstatKit.queryKstat2(
                "kstat:/net/link/" + getName() + "/0",
                "obytes64",
                "rbytes64",
                "opackets64",
                "ipackets64",
                "oerrors",
                "ierrors",
                "collisions",
                "dl_idrops",
                "ifspeed",
                "snaptime");
        if (results[results.length - 1] == null) {
            return false;
        }
        this.bytesSent = results[0] == null ? 0L : (long) results[0];
        this.bytesRecv = results[1] == null ? 0L : (long) results[1];
        this.packetsSent = results[2] == null ? 0L : (long) results[2];
        this.packetsRecv = results[3] == null ? 0L : (long) results[3];
        this.outErrors = results[4] == null ? 0L : (long) results[4];
        this.collisions = results[5] == null ? 0L : (long) results[5];
        this.inDrops = results[6] == null ? 0L : (long) results[6];
        this.speed = results[7] == null ? 0L : (long) results[7];
        // Snap time in ns; convert to ms
        this.timeStamp = (long) results[8] / 1_000_000L;
        return true;
    }

}
