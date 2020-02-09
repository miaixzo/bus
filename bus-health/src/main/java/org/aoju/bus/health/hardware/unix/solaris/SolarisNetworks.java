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
package org.aoju.bus.health.hardware.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.aoju.bus.health.common.unix.solaris.KstatUtils;
import org.aoju.bus.health.common.unix.solaris.KstatUtils.KstatChain;
import org.aoju.bus.health.hardware.AbstractNetworks;
import org.aoju.bus.health.hardware.NetworkIF;

/**
 * <p>
 * SolarisNetworks class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class SolarisNetworks extends AbstractNetworks {

    /**
     * Updates interface network statistics on the given interface. Statistics
     * include packets and bytes sent and received, and interface speed.
     *
     * @param netIF The interface on which to update statistics
     */
    public static void updateNetworkStats(NetworkIF netIF) {
        try (KstatChain kc = KstatUtils.openChain()) {
            Kstat ksp = kc.lookup("link", -1, netIF.getName());
            if (ksp == null) { // Solaris 10 compatibility
                ksp = kc.lookup(null, -1, netIF.getName());
            }
            if (ksp != null && kc.read(ksp)) {
                netIF.setBytesSent(KstatUtils.dataLookupLong(ksp, "obytes64"));
                netIF.setBytesRecv(KstatUtils.dataLookupLong(ksp, "rbytes64"));
                netIF.setPacketsSent(KstatUtils.dataLookupLong(ksp, "opackets64"));
                netIF.setPacketsRecv(KstatUtils.dataLookupLong(ksp, "ipackets64"));
                netIF.setOutErrors(KstatUtils.dataLookupLong(ksp, "oerrors"));
                netIF.setInErrors(KstatUtils.dataLookupLong(ksp, "ierrors"));
                netIF.setSpeed(KstatUtils.dataLookupLong(ksp, "ifspeed"));
                // Snap time in ns; convert to ms
                netIF.setTimeStamp(ksp.ks_snaptime / 1_000_000L);
            }
        }
    }

}
