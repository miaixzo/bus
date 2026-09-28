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
package org.miaixz.bus.health.mac.driver;

import java.util.List;
import java.util.Optional;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.DisplayMode;
import org.miaixz.bus.health.builtin.hardware.DisplayModeImpl;

/**
 * Describes an active display reported by CoreGraphics and matches it to an IOKit display using EDID identity fields.
 *
 * @author Kimi Liu
 */
@Immutable
public final class CoreGraphicsDisplay {

    /** The EDID manufacturer identifier. */
    private final int vendor;

    /** The EDID product identifier. */
    private final int model;

    /** The EDID serial number. */
    private final int serial;

    /** Whether CoreGraphics marks the display as built in. */
    private final boolean builtIn;

    /** Whether CoreGraphics marks the display as the main display. */
    private final boolean main;

    /** The current mode, or {@code null}. */
    private final DisplayMode mode;

    /**
     * Constructs a CoreGraphics display snapshot.
     *
     * @param vendor  the display vendor number
     * @param model   the display model number
     * @param serial  the display serial number
     * @param builtIn whether the display is built in
     * @param main    whether the display is the main display
     * @param mode    the current mode, or {@code null}
     */
    public CoreGraphicsDisplay(int vendor, int model, int serial, boolean builtIn, boolean main, DisplayMode mode) {
        this.vendor = vendor;
        this.model = model;
        this.serial = serial;
        this.builtIn = builtIn;
        this.main = main;
        this.mode = mode;
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
     * Reports whether the display is the main display.
     *
     * @return {@code true} for the main display
     */
    public boolean isMain() {
        return this.main;
    }

    /**
     * Gets the current display mode.
     *
     * @return the current mode, or an empty optional
     */
    public Optional<DisplayMode> getMode() {
        return Optional.ofNullable(this.mode);
    }

    /**
     * Builds a display mode from CoreGraphics values.
     *
     * @param x           the desktop x origin in points
     * @param y           the desktop y origin in points
     * @param width       the logical width in the current orientation
     * @param height      the logical height in the current orientation
     * @param pixelWidth  the unrotated native pixel width
     * @param pixelHeight the unrotated native pixel height
     * @param refreshRate the refresh rate in hertz
     * @param rotation    the clockwise rotation in degrees
     * @return the normalized display mode
     */
    public static DisplayMode toMode(
            double x,
            double y,
            double width,
            double height,
            long pixelWidth,
            long pixelHeight,
            double refreshRate,
            double rotation) {
        int degrees = (int) Math.round(rotation);
        boolean quarterTurn = Math.abs(degrees) % (Normal._90 * Normal._2) == Normal._90;
        int currentPixelWidth = (int) (quarterTurn ? pixelHeight : pixelWidth);
        int currentPixelHeight = (int) (quarterTurn ? pixelWidth : pixelHeight);
        return new DisplayModeImpl((int) Math.round(x), (int) Math.round(y), (int) Math.round(width),
                (int) Math.round(height), currentPixelWidth, currentPixelHeight, refreshRate, degrees);
    }

    /**
     * Finds the unique CoreGraphics display matching an EDID's vendor, product, and serial values.
     *
     * @param displays active CoreGraphics displays
     * @param edid     the IOKit display EDID
     * @return the unique matching display, or an empty optional
     */
    public static Optional<CoreGraphicsDisplay> matchEdid(List<CoreGraphicsDisplay> displays, byte[] edid) {
        if (edid.length < Normal._16) {
            return Optional.empty();
        }
        int vendor = Byte.toUnsignedInt(edid[Normal._8]) << Normal._8 | Byte.toUnsignedInt(edid[Normal._9]);
        int model = Byte.toUnsignedInt(edid[Normal._10]) | Byte.toUnsignedInt(edid[Normal._11]) << Normal._8;
        int serial = Byte.toUnsignedInt(edid[Normal._12]) | Byte.toUnsignedInt(edid[Normal._13]) << Normal._8
                | Byte.toUnsignedInt(edid[Normal._14]) << Normal._16
                | Byte.toUnsignedInt(edid[Normal._15]) << Normal._24;
        CoreGraphicsDisplay match = null;
        for (CoreGraphicsDisplay display : displays) {
            if (display.vendor == vendor && display.model == model && display.serial == serial) {
                if (match != null) {
                    return Optional.empty();
                }
                match = display;
            }
        }
        return Optional.ofNullable(match);
    }

    /**
     * Finds the unique built-in CoreGraphics display.
     *
     * @param displays active CoreGraphics displays
     * @return the unique built-in display, or an empty optional
     */
    public static Optional<CoreGraphicsDisplay> matchBuiltIn(List<CoreGraphicsDisplay> displays) {
        CoreGraphicsDisplay match = null;
        for (CoreGraphicsDisplay display : displays) {
            if (display.builtIn) {
                if (match != null) {
                    return Optional.empty();
                }
                match = display;
            }
        }
        return Optional.ofNullable(match);
    }

}
