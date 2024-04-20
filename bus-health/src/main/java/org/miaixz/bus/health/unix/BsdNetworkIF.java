/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.unix;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.hardware.AbstractNetworkIF;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.logger.Logger;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

/**
 * BsdNetworkIF applicable to FreeBSD and OpenBSD.
 *
 * @author Kimi Liu
 * @since Java 17+
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

    public BsdNetworkIF(NetworkInterface netint) throws InstantiationException {
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
        String[] split = RegEx.SPACES.split(stats);
        if (split.length < 12) {
            // No update
            return false;
        }
        this.bytesSent = Builder.parseUnsignedLongOrDefault(split[10], 0L);
        this.bytesRecv = Builder.parseUnsignedLongOrDefault(split[7], 0L);
        this.packetsSent = Builder.parseUnsignedLongOrDefault(split[8], 0L);
        this.packetsRecv = Builder.parseUnsignedLongOrDefault(split[4], 0L);
        this.outErrors = Builder.parseUnsignedLongOrDefault(split[9], 0L);
        this.inErrors = Builder.parseUnsignedLongOrDefault(split[5], 0L);
        this.collisions = Builder.parseUnsignedLongOrDefault(split[11], 0L);
        this.inDrops = Builder.parseUnsignedLongOrDefault(split[6], 0L);
        return true;
    }

}
