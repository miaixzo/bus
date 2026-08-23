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
package org.miaixz.bus.health.mac;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Interprets values reported by macOS SMC sensor keys.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class SmcSensorValues {

    /**
     * SMC data type reporting a fixed-point value with two fractional bits.
     */
    private static final String DATATYPE_FPE2 = "fpe2";

    /**
     * Lowest reading accepted as a plausible CPU voltage, in volts.
     */
    public static final double MIN_PLAUSIBLE_VOLTAGE = 0.2;

    /**
     * Creates a new SmcSensorValues instance.
     */
    public SmcSensorValues() {
        // No initialization required.
    }

    /**
     * Converts a fan speed reading to RPM.
     *
     * @param reading The raw reading.
     * @return The speed in RPM, never negative.
     */
    public static int toRpm(double reading) {
        if (Double.isNaN(reading) || reading <= 0d) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(reading));
    }

    /**
     * Tests whether a reading is plausible as a CPU voltage.
     *
     * @param volts The reading to test, in volts.
     * @return {@code true} if the reading is plausible.
     */
    public static boolean isPlausibleVoltage(double volts) {
        return volts >= MIN_PLAUSIBLE_VOLTAGE;
    }

    /**
     * Converts a voltage reading to volts according to the SMC data type.
     *
     * @param raw      The decoded reading.
     * @param dataType The key data type.
     * @return The reading in volts.
     */
    public static double scaleVoltage(double raw, String dataType) {
        return DATATYPE_FPE2.equals(dataType) ? raw / 1000d : raw;
    }

}
