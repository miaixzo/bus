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
package org.miaixz.bus.health.windows.driver;

import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Maps Windows display configuration data to physical connector names.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class DisplayConnector {

    /**
     * Prevents instantiation.
     */
    private DisplayConnector() {
    }

    /**
     * Names the connector a display is attached through.
     *
     * @param outputTechnology  the output technology value
     * @param connectorInstance the connector instance number
     * @return a connector name such as {@code HDMI} or {@code DisplayPort-1}
     */
    public static String connectorName(int outputTechnology, int connectorInstance) {
        String base = technologyName(outputTechnology);
        return connectorInstance > Normal._0 ? base + Symbol.MINUS + connectorInstance : base;
    }

    /**
     * Gets a human-readable technology name.
     *
     * @param outputTechnology the output technology value
     * @return the technology name
     */
    private static String technologyName(int outputTechnology) {
        switch (outputTechnology) {
            case Normal._0:
                return "VGA";

            case Normal._1:
                return "S-Video";

            case Normal._2:
                return "Composite";

            case Normal._3:
                return "Component";

            case Normal._4:
                return "DVI";

            case Normal._5:
                return "HDMI";

            case Normal._6:
                return "LVDS";

            case Normal._9:
                return "SDI";

            case Normal._10:
                return "DisplayPort";

            case Normal._11:
                return "eDP";

            case Normal._12:
            case Normal._13:
                return "UDI";

            case Normal._14:
                return "SDTV";

            case Normal._15:
                return "Miracast";

            case Normal._1 << Normal._31:
                return "Internal";

            default:
                return "Other";
        }
    }

    /**
     * Normalizes a monitor device interface path for case-insensitive matching.
     *
     * @param devicePath the device interface path
     * @return the lower-case path, or {@link Normal#UNKNOWN} if the input is blank
     */
    public static String normalizePath(String devicePath) {
        if (StringKit.isBlank(devicePath)) {
            return Normal.UNKNOWN;
        }
        return devicePath.toLowerCase(Locale.ROOT);
    }

}
