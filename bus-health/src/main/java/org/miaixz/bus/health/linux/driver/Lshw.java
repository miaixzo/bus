/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.linux.driver;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.IdGroup;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to read info from {@code lshw}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Lshw {

    private static final String MODEL;
    private static final String SERIAL;
    private static final String UUID;

    static {
        String model = null;
        String serial = null;
        String uuid = null;

        if (IdGroup.isElevated()) {
            String modelMarker = "product:";
            String serialMarker = "serial:";
            String uuidMarker = "uuid:";

            for (String checkLine : Executor.runNative("lshw -C system")) {
                if (checkLine.contains(modelMarker)) {
                    model = checkLine.split(modelMarker)[1].trim();
                } else if (checkLine.contains(serialMarker)) {
                    serial = checkLine.split(serialMarker)[1].trim();
                } else if (checkLine.contains(uuidMarker)) {
                    uuid = checkLine.split(uuidMarker)[1].trim();
                }
            }
        }
        MODEL = model;
        SERIAL = serial;
        UUID = uuid;
    }

    private Lshw() {
    }

    /**
     * Query the model from lshw
     *
     * @return The model if available, null otherwise
     */
    public static String queryModel() {
        return MODEL;
    }

    /**
     * Query the serial number from lshw
     *
     * @return The serial number if available, null otherwise
     */
    public static String querySerialNumber() {
        return SERIAL;
    }

    /**
     * Query the UUID from lshw
     *
     * @return The UUID if available, null otherwise
     */
    public static String queryUUID() {
        return UUID;
    }

    /**
     * Query the CPU capacity (max frequency) from lshw
     *
     * @return The CPU capacity (max frequency) if available, -1 otherwise
     */
    public static long queryCpuCapacity() {
        String capacityMarker = "capacity:";
        for (String checkLine : Executor.runNative("lshw -class processor")) {
            if (checkLine.contains(capacityMarker)) {
                return Parsing.parseHertz(checkLine.split(capacityMarker)[1].trim());
            }
        }
        return -1L;
    }

}
