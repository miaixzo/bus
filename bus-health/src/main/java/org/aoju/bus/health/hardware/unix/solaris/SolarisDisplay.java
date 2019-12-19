/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.solaris;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.hardware.AbstractDisplay;
import org.aoju.bus.health.hardware.Display;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A Display
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class SolarisDisplay extends AbstractDisplay {

    /**
     * <p>
     * Constructor for SolarisDisplay.
     * </p>
     *
     * @param edid an array of {@link byte} objects.
     */
    public SolarisDisplay(byte[] edid) {
        super(edid);
        Logger.debug("Initialized SolarisDisplay");
    }

    /**
     * Gets Display Information
     *
     * @return An array of Display objects representing monitors, etc.
     */
    public static Display[] getDisplays() {
        List<String> xrandr = Command.runNative("xrandr --verbose");
        // xrandr reports edid in multiple lines. After seeing a line containing
        // EDID, read subsequent lines of hex until 256 characters are reached
        if (xrandr.isEmpty()) {
            return new Display[0];
        }
        List<Display> displays = new ArrayList<>();
        StringBuilder sb = null;
        for (String s : xrandr) {
            if (s.contains("EDID")) {
                sb = new StringBuilder();
            } else if (sb != null) {
                sb.append(s.trim());
                if (sb.length() < 256) {
                    continue;
                }
                String edidStr = sb.toString();
                Logger.debug("Parsed EDID: {}", edidStr);
                byte[] edid = Builder.hexStringToByteArray(edidStr);
                if (edid.length >= 128) {
                    displays.add(new SolarisDisplay(edid));
                }
                sb = null;
            }
        }

        return displays.toArray(new Display[0]);
    }
}
