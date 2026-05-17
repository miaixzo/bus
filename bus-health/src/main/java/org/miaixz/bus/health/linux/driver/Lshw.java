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

import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to read info from {@code lshw}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Lshw {

    /**
     * The MODEL constant.
     */
    private static final String MODEL;

    /**
     * The SERIAL constant.
     */
    private static final String SERIAL;

    /**
     * The UUID constant.
     */
    private static final String UUID;

    static {
        Triplet<String, String, String> info = parseSystemInfo(Executor.runPrivilegedNative("lshw -C system"));
        MODEL = info.getLeft();
        SERIAL = info.getMiddle();
        UUID = info.getRight();
    }

    /**
     * Parse model, serial number, and UUID from lshw system output.
     *
     * @param lines output of {@code lshw -C system}
     * @return triplet of (model, serial, uuid), with null for missing values
     */
    static Triplet<String, String, String> parseSystemInfo(List<String> lines) {
        String model = null;
        String serial = null;
        String uuid = null;

        String modelMarker = "product:";
        String serialMarker = "serial:";
        String uuidMarker = "uuid:";

        for (String checkLine : lines) {
            if (checkLine.contains(modelMarker)) {
                model = checkLine.split(modelMarker)[1].trim();
            } else if (checkLine.contains(serialMarker)) {
                serial = checkLine.split(serialMarker)[1].trim();
            } else if (checkLine.contains(uuidMarker)) {
                uuid = checkLine.split(uuidMarker)[1].trim();
            }
        }
        return Triplet.of(model, serial, uuid);
    }

    /**
     * Creates a new Lshw instance.
     */
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
        return queryCpuCapacity(Executor.runPrivilegedNative("lshw -class processor"));
    }

    /**
     * Parse the CPU capacity (max frequency) from lshw processor output.
     *
     * @param lines output of {@code lshw -class processor}
     * @return The CPU capacity (max frequency) if available, -1 otherwise
     */
    static long queryCpuCapacity(List<String> lines) {
        String capacityMarker = "capacity:";
        for (String checkLine : lines) {
            if (checkLine.contains(capacityMarker)) {
                return Parsing.parseHertz(checkLine.split(capacityMarker)[1].trim());
            }
        }
        return -1L;
    }

}
