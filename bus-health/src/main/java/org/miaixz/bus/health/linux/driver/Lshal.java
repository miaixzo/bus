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
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to read info from {@code lshal}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Lshal {

    /**
     * Constructs a new {@code Lshal} instance.
     */
    public Lshal() {
        // No initialization required.
    }

    /**
     * Query the serial number from lshal
     *
     * @return The serial number if available, null otherwise
     */
    public static String querySerialNumber() {
        // if lshal command available (HAL deprecated in newer linuxes)
        return querySerialNumber(Executor.runNative("lshal"));
    }

    /**
     * Parse the serial number from lshal output.
     *
     * @param lines output of {@code lshal}
     * @return The serial number if available, null otherwise
     */
    static String querySerialNumber(List<String> lines) {
        String marker = "system.hardware.serial =";
        for (String checkLine : lines) {
            if (checkLine.contains(marker)) {
                return Parsing.getSingleQuoteStringValue(checkLine);
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
        return queryUUID(Executor.runNative("lshal"));
    }

    /**
     * Parse the UUID from lshal output.
     *
     * @param lines output of {@code lshal}
     * @return The UUID if available, null otherwise
     */
    static String queryUUID(List<String> lines) {
        String marker = "system.hardware.uuid =";
        for (String checkLine : lines) {
            if (checkLine.contains(marker)) {
                return Parsing.getSingleQuoteStringValue(checkLine);
            }
        }
        return null;
    }

}
