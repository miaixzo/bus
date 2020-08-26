/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.mac.drivers;

import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.Statfs;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to query fsstat
 *
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
@ThreadSafe
public final class Fsstat {

    private Fsstat() {
    }

    /**
     * Query fsstat to map partitions to mount points
     *
     * @return A map with partitions as the key and mount points as the value
     */
    public static Map<String, String> queryPartitionToMountMap() {
        Map<String, String> mountPointMap = new HashMap<>();
        // Use statfs to populate mount point map
        int numfs = SystemB.INSTANCE.getfsstat64(null, 0, 0);
        // Create array to hold results
        Statfs[] fs = new Statfs[numfs];
        // Fill array with results
        SystemB.INSTANCE.getfsstat64(fs, numfs * new Statfs().size(), SystemB.MNT_NOWAIT);
        // Iterate all mounted file systems
        for (Statfs f : fs) {
            String mntFrom = new String(f.f_mntfromname, StandardCharsets.UTF_8).trim();
            mountPointMap.put(mntFrom.replace("/dev/", Normal.EMPTY), new String(f.f_mntonname, StandardCharsets.UTF_8).trim());
        }
        return mountPointMap;
    }

}
