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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatNetInterface;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_netinterface_t;

/**
 * AIXNetworks class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class AixNetworkIF extends AbstractNetworkIF {

    private final Supplier<perfstat_netinterface_t[]> netstats;
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

    public AixNetworkIF(NetworkInterface netint, Supplier<perfstat_netinterface_t[]> netstats)
            throws InstantiationException {
        super(netint);
        this.netstats = netstats;
        updateAttributes();
    }

    /**
     * Gets all network interfaces on this machine
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        Supplier<perfstat_netinterface_t[]> netstats = Memoizer
                .memoize(PerfstatNetInterface::queryNetInterfaces, Memoizer.defaultExpiration());
        List<NetworkIF> ifList = new ArrayList<>();
        for (NetworkInterface ni : getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new AixNetworkIF(ni, netstats));
            } catch (InstantiationException e) {
                Logger.debug("Network Interface Instantiation failed: {}", e.getMessage());
            }
        }
        return ifList;
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
    public boolean updateAttributes() {
        perfstat_netinterface_t[] stats = netstats.get();
        long now = System.currentTimeMillis();
        for (perfstat_netinterface_t stat : stats) {
            String name = Native.toString(stat.name);
            if (name.equals(this.getName())) {
                this.bytesSent = stat.obytes;
                this.bytesRecv = stat.ibytes;
                this.packetsSent = stat.opackets;
                this.packetsRecv = stat.ipackets;
                this.outErrors = stat.oerrors;
                this.inErrors = stat.ierrors;
                this.collisions = stat.collisions;
                this.inDrops = stat.if_iqdrops;
                this.speed = stat.bitrate;
                this.timeStamp = now;
                return true;
            }
        }
        return false;
    }

}
