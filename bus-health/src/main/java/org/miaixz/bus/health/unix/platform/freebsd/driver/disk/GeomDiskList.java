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
package org.miaixz.bus.health.unix.platform.freebsd.driver.disk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query geom part list
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class GeomDiskList {

    private static final String GEOM_DISK_LIST = "geom disk list";

    /**
     * Queries disk data using geom
     *
     * @return A map with disk name as the key and a Triplet of model, serial, and size as the value
     */
    public static Map<String, Triplet<String, String, Long>> queryDisks() {
        // Map of device name to disk, to be returned
        Map<String, Triplet<String, String, Long>> diskMap = new HashMap<>();
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
                    diskMap.put(diskName, Triplet.of(descr, ident, mediaSize));
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
                    String[] split = Pattern.SPACES_PATTERN.split(line);
                    if (split.length > 1) {
                        mediaSize = Parsing.parseLongOrDefault(split[1], 0L);
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
            diskMap.put(diskName, Triplet.of(descr, ident, mediaSize));
        }
        return diskMap;
    }

}
