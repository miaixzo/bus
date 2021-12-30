/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.linux.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.health.builtin.hardware.AbstractDisplay;
import org.aoju.bus.health.builtin.hardware.Display;
import org.aoju.bus.health.unix.Xrandr;
import org.aoju.bus.logger.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Display
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@Immutable
final class LinuxDisplay extends AbstractDisplay {

    /**
     * Constructor for LinuxDisplay.
     *
     * @param edid a byte array representing a display EDID
     */
    LinuxDisplay(byte[] edid) {
        super(edid);
        Logger.debug("Initialized LinuxDisplay");
    }

    /**
     * Gets Display Information
     *
     * @return An array of Display objects representing monitors, etc.
     */
    public static List<Display> getDisplays() {
        return Xrandr.getEdidArrays().stream().map(LinuxDisplay::new).collect(Collectors.toList());
    }

}
