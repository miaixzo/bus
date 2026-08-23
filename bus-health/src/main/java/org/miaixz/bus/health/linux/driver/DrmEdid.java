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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Reads EDID data from the Linux DRM (Direct Rendering Manager) subsystem. The kernel exposes raw EDID bytes for each
 * connected display at {@code /sys/class/drm/card<N>-<connector>/edid}, which works regardless of whether X11 or
 * Wayland is in use.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class DrmEdid {

    /**
     * Creates a new DrmEdid instance.
     */
    public DrmEdid() {
        // No initialization required.
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
        List<Triplet<String, Integer, byte[]>> data = getDisplayData(drmDir);
        List<byte[]> edids = new ArrayList<>(data.size());
        for (Triplet<String, Integer, byte[]> display : data) {
            edids.add(display.getRight());
        }
        return Collections.unmodifiableList(edids);
    }

    /**
     * Reads display data from {@code /sys/class/drm} for all connected displays.
     *
     * @return a list of connector name, connector identifier, and EDID byte array triplets
     */
    public static List<Triplet<String, Integer, byte[]>> getDisplayData() {
        return getDisplayData(new File(SysPath.DRM));
    }

    /**
     * Reads display data from the given DRM directory.
     *
     * @param drmDir the directory containing card connector subdirectories
     * @return a list of connector name, connector identifier, and EDID byte array triplets
     */
    static List<Triplet<String, Integer, byte[]>> getDisplayData(File drmDir) {
        if (!drmDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] connectors = drmDir.listFiles(f -> f.isDirectory() && f.getName().matches("card\\d+-.+"));
        if (connectors == null || connectors.length == Normal._0) {
            return Collections.emptyList();
        }
        List<Triplet<String, Integer, byte[]>> displays = new ArrayList<>();
        for (File connector : connectors) {
            File statusFile = new File(connector, "status");
            if (statusFile.exists()) {
                String status = Builder.getStringFromFile(statusFile.getPath()).trim();
                if (!"connected".equals(status)) {
                    continue;
                }
            }
            File edidFile = new File(connector, "edid");
            if (edidFile.exists() && edidFile.length() >= Normal._128) {
                byte[] edid = Builder.readAllBytes(edidFile.getPath(), false);
                if (edid.length >= Normal._128) {
                    String directoryName = connector.getName();
                    String connectorName = directoryName.substring(directoryName.indexOf(Symbol.C_MINUS) + Normal._1);
                    int connectorId = Normal.__1;
                    File connectorIdFile = new File(connector, "connector_id");
                    if (connectorIdFile.exists()) {
                        connectorId = Parsing.parseIntOrDefault(
                                Builder.getStringFromFile(connectorIdFile.getPath()).trim(),
                                Normal.__1);
                    }
                    displays.add(Triplet.of(connectorName, connectorId, edid));
                }
            }
        }
        return Collections.unmodifiableList(displays);
    }

}
