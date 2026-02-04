/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.aix.driver;

import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query lscfg
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Lscfg {

    private Lscfg() {
    }

    /**
     * Query {@code lscfg -vp} to get all hardware devices
     *
     * @return A list of the output
     */
    public static List<String> queryAllDevices() {
        return Executor.runNative("lscfg -vp");
    }

    /**
     * Parse the output of {@code lscfg -vp} to get backplane info
     *
     * @param lscfg The output of a previous call to {@code lscfg -vp}
     * @return A triplet with backplane model, serial number, and version
     */
    public static Triplet<String, String, String> queryBackplaneModelSerialVersion(List<String> lscfg) {
        final String planeMarker = "WAY BACKPLANE";
        final String modelMarker = "Part Number";
        final String serialMarker = "Serial Number";
        final String versionMarker = "Version";
        final String locationMarker = "Physical Location";

        // 1 WAY BACKPLANE :
        // Serial Number...............YL10243490FB
        // Part Number.................80P4315
        // Customer Card ID Number.....26F4
        // CCIN Extender...............1
        // FRU Number.................. 80P4315
        // Version.....................RS6K
        // Hardware Location Code......U0.1-P1
        // Physical Location: U0.1-P1

        String model = null;
        String serialNumber = null;
        String version = null;
        boolean planeFlag = false;
        for (final String checkLine : lscfg) {
            if (!planeFlag && checkLine.contains(planeMarker)) {
                planeFlag = true;
            } else if (planeFlag) {
                if (checkLine.contains(modelMarker)) {
                    model = Parsing.removeLeadingDots(checkLine.split(modelMarker)[1].trim());
                } else if (checkLine.contains(serialMarker)) {
                    serialNumber = Parsing.removeLeadingDots(checkLine.split(serialMarker)[1].trim());
                } else if (checkLine.contains(versionMarker)) {
                    version = Parsing.removeLeadingDots(checkLine.split(versionMarker)[1].trim());
                } else if (checkLine.contains(locationMarker)) {
                    break;
                }
            }
        }
        return Triplet.of(model, serialNumber, version);
    }

    /**
     * Query {@code lscfg -vl device} to get hardware info
     *
     * @param device The disk to get the model and serial from
     * @return A pair containing the model and serial number for the device, or null if not found
     */
    public static Pair<String, String> queryModelSerial(String device) {
        String modelMarker = "Machine Type and Model";
        String serialMarker = "Serial Number";
        String model = null;
        String serial = null;
        for (String s : Executor.runNative("lscfg -vl " + device)) {
            // Default model to description at end of first line
            if (model == null && s.contains(device)) {
                String locDesc = s.split(device)[1].trim();
                int idx = locDesc.indexOf(Symbol.C_SPACE);
                if (idx > 0) {
                    model = locDesc.substring(idx).trim();
                }
            }
            if (s.contains(modelMarker)) {
                model = Parsing.removeLeadingDots(s.split(modelMarker)[1].trim());
            } else if (s.contains(serialMarker)) {
                serial = Parsing.removeLeadingDots(s.split(serialMarker)[1].trim());
            }
        }
        return Pair.of(model, serial);
    }

}
