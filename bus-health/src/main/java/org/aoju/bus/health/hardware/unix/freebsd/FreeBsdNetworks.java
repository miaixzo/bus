/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.hardware.AbstractNetworks;
import org.aoju.bus.health.hardware.NetworkIF;

/**
 * <p>
 * FreeBsdNetworks class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class FreeBsdNetworks extends AbstractNetworks {

    /**
     * Updates interface network statistics on the given interface. Statistics
     * include packets and bytes sent and received, and interface speed.
     *
     * @param netIF The interface on which to update statistics
     */
    public static void updateNetworkStats(NetworkIF netIF) {
        String stats = Command.getAnswerAt("netstat -bI " + netIF.getName(), 1);
        netIF.setTimeStamp(System.currentTimeMillis());
        String[] split = Builder.whitespaces.split(stats);
        if (split.length < 12) {
            // No update
            return;
        }
        netIF.setBytesSent(Builder.parseUnsignedLongOrDefault(split[10], 0L));
        netIF.setBytesRecv(Builder.parseUnsignedLongOrDefault(split[7], 0L));
        netIF.setPacketsSent(Builder.parseUnsignedLongOrDefault(split[8], 0L));
        netIF.setPacketsRecv(Builder.parseUnsignedLongOrDefault(split[4], 0L));
        netIF.setOutErrors(Builder.parseUnsignedLongOrDefault(split[9], 0L));
        netIF.setInErrors(Builder.parseUnsignedLongOrDefault(split[5], 0L));
    }
}
