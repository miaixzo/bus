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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.unix.freebsd.drivers;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to query geom part list
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
@ThreadSafe
public final class GeomDiskList {

    private static final String GEOM_DISK_LIST = "geom disk list";

    private GeomDiskList() {
    }

    /**
     * Queries disk data using geom
     *
     * @return A map with disk name as the key and a Triple of model, serial, and
     * size as the value
     */
    public static Map<String, Triple<String, String, Long>> queryDisks() {
        // Map of device name to disk, to be returned
        Map<String, Triple<String, String, Long>> diskMap = new HashMap<>();
        // Parameters needed.
        String diskName = null; // Non-null identifies a valid partition
        String descr = Normal.UNKNOWN;
        String ident = Normal.UNKNOWN;
        long mediaSize = 0L;

        List<String> geom = Executor.runNative(GEOM_DISK_LIST);
        for (String line : geom) {
            line = line.trim();
            // Marks the DiskStore device
            if (line.startsWith("Geom name:")) {
                // Save any previous disk in the map
                if (diskName != null) {
                    diskMap.put(diskName, Triple.of(descr, ident, mediaSize));
                    descr = Normal.UNKNOWN;
                    ident = Normal.UNKNOWN;
                    mediaSize = 0L;
                }
                // Now use new diskName
                diskName = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
            }
            // If we don't have a valid store, don't bother parsing anything
            if (diskName != null) {
                line = line.trim();
                if (line.startsWith("Mediasize:")) {
                    String[] split = RegEx.SPACES.split(line);
                    if (split.length > 1) {
                        mediaSize = Builder.parseLongOrDefault(split[1], 0L);
                    }
                }
                if (line.startsWith("descr:")) {
                    descr = line.replace("descr:", Normal.EMPTY).trim();
                }
                if (line.startsWith("ident:")) {
                    ident = line.replace("ident:", Normal.EMPTY).replace("(null)", Normal.EMPTY).trim();
                }
            }
        }
        if (diskName != null) {
            diskMap.put(diskName, Triple.of(descr, ident, mediaSize));
        }
        return diskMap;
    }

}
