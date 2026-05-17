/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.health.linux.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Utility to read EDID data from the Linux DRM (Direct Rendering Manager) subsystem. The kernel exposes raw EDID bytes
 * for each connected display at {@code /sys/class/drm/card<N>-<connector>/edid}, which works regardless of whether X11
 * or Wayland is in use.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class DrmEdid {

    /**
     * Creates a new DrmEdid instance.
     */
    private DrmEdid() {

    }

    /**
     * Reads EDID byte arrays from {@code /sys/class/drm} for all connected displays.
     *
     * @return a list of EDID byte arrays, or an empty list if none are found
     */
    public static List<byte[]> getEdidArrays() {
        return getEdidArrays(new File(SysPath.DRM));
    }

    /**
     * Reads EDID byte arrays from the given DRM directory.
     *
     * @param drmDir the directory containing card connector subdirectories
     * @return a list of EDID byte arrays, or an empty list if none are found
     */
    static List<byte[]> getEdidArrays(File drmDir) {
        if (!drmDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] connectors = drmDir.listFiles(f -> f.isDirectory() && f.getName().matches("card\\d+-.+"));
        if (connectors == null || connectors.length == 0) {
            return Collections.emptyList();
        }
        List<byte[]> displays = new ArrayList<>();
        for (File connector : connectors) {
            File statusFile = new File(connector, "status");
            if (statusFile.exists()) {
                String status = Builder.getStringFromFile(statusFile.getPath()).trim();
                if (!"connected".equals(status)) {
                    continue;
                }
            }
            File edidFile = new File(connector, "edid");
            if (edidFile.exists() && edidFile.length() >= 128) {
                byte[] edid = Builder.readAllBytes(edidFile.getPath(), false);
                if (edid.length >= 128) {
                    displays.add(edid);
                }
            }
        }
        return displays;
    }

}
