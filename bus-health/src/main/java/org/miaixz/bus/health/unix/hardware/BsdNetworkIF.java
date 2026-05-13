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
     * The timeStamp value.
     */
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
        return 0;
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
