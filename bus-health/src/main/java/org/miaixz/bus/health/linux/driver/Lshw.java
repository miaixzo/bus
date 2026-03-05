/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
