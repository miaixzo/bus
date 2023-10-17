/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.linux.drivers;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;

/**
 * Utility to read info from {@code lshal}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Lshal {

    /**
     * Query the serial number from lshal
     *
     * @return The serial number if available, null otherwise
     */
    public static String querySerialNumber() {
        // if lshal command available (HAL deprecated in newer linuxes)
        String marker = "system.hardware.serial =";
        for (String checkLine : Executor.runNative("lshal")) {
            if (checkLine.contains(marker)) {
                return Builder.getSingleQuoteStringValue(checkLine);
            }
        }
        return null;
    }

    /**
     * Query the UUID from lshal
     *
     * @return The UUID if available, null otherwise
     */
    public static String queryUUID() {
        // if lshal command available (HAL deprecated in newer linuxes)
        String marker = "system.hardware.uuid =";
        for (String checkLine : Executor.runNative("lshal")) {
            if (checkLine.contains(marker)) {
                return Builder.getSingleQuoteStringValue(checkLine);
            }
        }
        return null;
    }

}
