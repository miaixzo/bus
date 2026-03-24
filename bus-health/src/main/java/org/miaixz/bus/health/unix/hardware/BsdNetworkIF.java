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
package org.miaixz.bus.health.unix.hardware;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.common.AbstractNetworkIF;
import org.miaixz.bus.logger.Logger;

/**
 * BsdNetworkIF applicable to FreeBSD and OpenBSD.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class BsdNetworkIF extends AbstractNetworkIF {

    private long bytesRecv;
    private long bytesSent;
    private long packetsRecv;
    private long packetsSent;
    private long inErrors;
    private long outErrors;
    private long inDrops;
    private long collisions;
    private long timeStamp;

    /**
     * Constructs a new {@code BsdNetworkIF} object.
     *
     * @param netint The {@link NetworkInterface} object.
     * @throws InstantiationException If the network interface cannot be properly initialized.
     */
    public BsdNetworkIF(NetworkInterface netint) throws InstantiationException {
        super(netint);
        updateAttributes();
    }

    /**
     * Gets all network interfaces on this machine.
     *
     * @param includeLocalInterfaces include local interfaces in the result
     * @return A list of {@link NetworkIF} objects representing the interfaces
     */
    public static List<NetworkIF> getNetworks(boolean includeLocalInterfaces) {
        List<NetworkIF> ifList = new ArrayList<>();
        for (NetworkInterface ni : getNetworkInterfaces(includeLocalInterfaces)) {
            try {
                ifList.add(new BsdNetworkIF(ni));
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
        return 0;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public boolean updateAttributes() {
        String stats = Executor.getAnswerAt("netstat -bI " + getName(), 1);
        this.timeStamp = System.currentTimeMillis();
        String[] split = Pattern.SPACES_PATTERN.split(stats);
        if (split.length < 12) {
            // No update
            return false;
        }
        this.bytesSent = Parsing.parseUnsignedLongOrDefault(split[10], 0L);
        this.bytesRecv = Parsing.parseUnsignedLongOrDefault(split[7], 0L);
        this.packetsSent = Parsing.parseUnsignedLongOrDefault(split[8], 0L);
        this.packetsRecv = Parsing.parseUnsignedLongOrDefault(split[4], 0L);
        this.outErrors = Parsing.parseUnsignedLongOrDefault(split[9], 0L);
        this.inErrors = Parsing.parseUnsignedLongOrDefault(split[5], 0L);
        this.collisions = Parsing.parseUnsignedLongOrDefault(split[11], 0L);
        this.inDrops = Parsing.parseUnsignedLongOrDefault(split[6], 0L);
        return true;
    }

}
