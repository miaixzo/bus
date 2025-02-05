/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.driver.disk;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.Statfs;

/**
 * Utility to query fsstat
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Fsstat {

    /**
     * Query fsstat to map partitions to mount points
     *
     * @return A map with partitions as the key and mount points as the value
     */
    public static Map<String, String> queryPartitionToMountMap() {
        Map<String, String> mountPointMap = new HashMap<>();

        // Use statfs to get size of mounted file systems
        int numfs = queryFsstat(null, 0, 0);
        // Get data on file system
        Statfs s = new Statfs();
        // Create array to hold results
        Statfs[] fs = (Statfs[]) s.toArray(numfs);
        // Write file system data to array
        queryFsstat(fs, numfs * fs[0].size(), SystemB.MNT_NOWAIT);

        // Iterate all mounted file systems
        for (Statfs f : fs) {
            String mntFrom = Native.toString(f.f_mntfromname, Charset.UTF_8);
            mountPointMap.put(mntFrom.replace("/dev/", Normal.EMPTY), Native.toString(f.f_mntonname, Charset.UTF_8));
        }
        return mountPointMap;
    }

    private static int queryFsstat(Statfs[] buf, int bufsize, int flags) {
        return SystemB.INSTANCE.getfsstat64(buf, bufsize, flags);
    }

}
