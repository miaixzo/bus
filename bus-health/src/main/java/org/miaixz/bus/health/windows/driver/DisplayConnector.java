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
import java.util.Optional;
import java.util.function.LongToIntFunction;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.builtin.hardware.DisplayMode;
import org.miaixz.bus.health.builtin.hardware.DisplayModeImpl;

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
     * Tests whether an output technology connects a display built into the device.
     *
     * @param outputTechnology the Windows display output technology value
     * @return {@code true} for LVDS, embedded DisplayPort, embedded UDI, and internal connections
     */
    public static boolean isBuiltIn(int outputTechnology) {
        switch (outputTechnology) {
            case Normal._6:
            case Normal._11:
            case Normal._13:
            case Normal._1 << Normal._31:
                return true;

            default:
                return false;
        }
    }

    /**
     * Reads an active path's current mode from the buffers filled by {@code QueryDisplayConfig}. Windows desktop
     * coordinates use physical pixels, so the logical and native pixel sizes are equal.
     *
     * @param path      reads an integer at an offset within the path structure
     * @param modes     reads an integer at an offset within the mode array
     * @param modeCount the number of entries in the mode array
     * @return the current mode, or {@code null} if no valid source mode is available
     */
    public static DisplayMode readMode(LongToIntFunction path, LongToIntFunction modes, int modeCount) {
        int index = path.applyAsInt(Normal._12);
        if (index < Normal._0 || index >= modeCount) {
            return null;
        }
        long base = (long) index * Normal._64;
        if (modes.applyAsInt(base) != Normal._1) {
            return null;
        }
        int width = modes.applyAsInt(base + Normal._16);
        int height = modes.applyAsInt(base + Normal._20);
        if (width <= Normal._0 || height <= Normal._0) {
            return null;
        }
        int x = modes.applyAsInt(base + Normal._28);
        int y = modes.applyAsInt(base + Normal._32);
        int rotation = rotationDegrees(path.applyAsInt(Normal._40));
        double refreshRate = refreshRate(path.applyAsInt(Normal._48), path.applyAsInt(Normal._52));
        return new DisplayModeImpl(x, y, width, height, width, height, refreshRate, rotation);
    }

    /**
     * Converts a Windows display rotation value to clockwise degrees.
     *
     * @param rotation the Windows rotation value
     * @return 0, 90, 180, or 270 degrees
     */
    private static int rotationDegrees(int rotation) {
        switch (rotation) {
            case Normal._2:
                return Normal._90;

            case Normal._3:
                return Normal._90 * Normal._2;

            case Normal._4:
                return Normal._90 * Normal._3;

            default:
                return Normal._0;
        }
    }

    /**
     * Converts an unsigned Windows rational refresh rate to hertz.
     *
     * @param numerator   the unsigned numerator
     * @param denominator the unsigned denominator
     * @return the refresh rate, or 0 when the denominator is zero
     */
    private static double refreshRate(int numerator, int denominator) {
        if (denominator == Normal._0) {
            return Normal._0;
        }
        return (double) Integer.toUnsignedLong(numerator) / Integer.toUnsignedLong(denominator);
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

    /**
     * Describes a physical connector and the mode currently driven through it.
     */
    @Immutable
    public static final class Connector {

        /** The human-readable connector name. */
        private final String name;

        /** Whether the connector belongs to a built-in display. */
        private final boolean builtIn;

        /** The current mode, or {@code null}. */
        private final DisplayMode mode;

        /**
         * Constructs a connector description.
         *
         * @param outputTechnology  the Windows output technology value
         * @param connectorInstance the connector instance number
         * @param mode              the current mode, or {@code null}
         */
        public Connector(int outputTechnology, int connectorInstance, DisplayMode mode) {
            this.name = connectorName(outputTechnology, connectorInstance);
            this.builtIn = DisplayConnector.isBuiltIn(outputTechnology);
            this.mode = mode;
        }

        /**
         * Gets the connector name.
         *
         * @return the connector name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Reports whether the display is built in.
         *
         * @return {@code true} for a built-in display
         */
        public boolean isBuiltIn() {
            return this.builtIn;
        }

        /**
         * Gets the current mode.
         *
         * @return the current mode, or an empty optional
         */
        public Optional<DisplayMode> getMode() {
            return Optional.ofNullable(this.mode);
        }

    }

}
