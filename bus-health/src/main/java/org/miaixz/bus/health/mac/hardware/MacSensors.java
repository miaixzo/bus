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
package org.miaixz.bus.health.mac.hardware;

import java.util.List;

import com.sun.jna.platform.mac.IOKit.IOConnect;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.mac.SmcKit;
import org.miaixz.bus.health.mac.SmcSensorValues;

/**
 * <p>
 * MacSensors class.
 * </p>
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class MacSensors extends AbstractSensors {

    /**
     * Queries the CPU temperature from the SMC (System Management Controller).
     *
     * @return The CPU temperature in Celsius, or 0.0 if unable to retrieve.
     */
    @Override
    public double queryCpuTemperature() {
        IOConnect conn = SmcKit.smcOpen();
        if (conn == null) {
            return 0d;
        }
        try {
            double temp = SmcKit.smcGetFirstTemperature(conn, SmcKit.SMC_KEYS_CPU_TEMP_AGGREGATE_AS);
            if (temp <= 0d) {
                temp = SmcKit.smcGetFirstTemperature(conn, SmcKit.SMC_KEYS_CPU_TEMP_AS);
            }
            if (temp <= 0d) {
                double intelTemp = SmcKit.smcGetFloat(conn, SmcKit.SMC_KEY_CPU_TEMP);
                temp = SmcKit.isPlausibleTemperature(intelTemp) ? intelTemp : 0d;
            }
            return temp;
        } finally {
            SmcKit.smcClose(conn);
        }
    }

    /**
     * Queries the fan speeds from the SMC.
     *
     * @return An array of fan speeds in RPM, or an empty array if no fans are found or unable to retrieve.
     */
    @Override
    public int[] queryFanSpeeds() {
        List<String> keys = SmcKit.getFanSpeedKeys();
        IOConnect conn = SmcKit.smcOpen();
        if (conn == null) {
            return new int[keys.size()];
        }
        try {
            int[] fanSpeeds = new int[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                fanSpeeds[i] = SmcSensorValues.toRpm(SmcKit.smcGetFloat(conn, keys.get(i)));
            }
            return fanSpeeds;
        } finally {
            SmcKit.smcClose(conn);
        }
    }

    /**
     * Queries the CPU voltage from the SMC.
     *
     * @return The CPU voltage in Volts, or 0.0 if unable to retrieve.
     */
    @Override
    public double queryCpuVoltage() {
        IOConnect conn = SmcKit.smcOpen();
        if (conn == null) {
            return 0d;
        }
        try {
            return SmcKit.smcGetFirstVoltage(conn, SmcKit.getCpuVoltageKeys());
        } finally {
            SmcKit.smcClose(conn);
        }
    }

}
