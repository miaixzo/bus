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
package org.miaixz.bus.health.mac.hardware;

import java.util.Locale;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.mac.SmcKit;

import com.sun.jna.platform.mac.IOKit.IOConnect;

/**
 * <p>
 * MacSensors class.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class MacSensors extends AbstractSensors {

    /**
     * The number of fans, initialized to 0 and determined once.
     */
    private int numFans = 0;

    /**
     * Queries the CPU temperature from the SMC (System Management Controller).
     *
     * @return The CPU temperature in Celsius, or 0.0 if unable to retrieve.
     */
    @Override
    public double queryCpuTemperature() {
        IOConnect conn = SmcKit.smcOpen();
        double temp = SmcKit.smcGetFloat(conn, SmcKit.SMC_KEY_CPU_TEMP);
        SmcKit.smcClose(conn);
        if (temp > 0d) {
            return temp;
        }
        return 0d;
    }

    /**
     * Queries the fan speeds from the SMC.
     *
     * @return An array of fan speeds in RPM, or an empty array if no fans are found or unable to retrieve.
     */
    @Override
    public int[] queryFanSpeeds() {
        // If we don't have fan # try to get it
        IOConnect conn = SmcKit.smcOpen();
        if (this.numFans == 0) {
            this.numFans = (int) SmcKit.smcGetLong(conn, SmcKit.SMC_KEY_FAN_NUM);
        }
        int[] fanSpeeds = new int[this.numFans];
        for (int i = 0; i < this.numFans; i++) {
            fanSpeeds[i] = (int) SmcKit.smcGetFloat(conn, String.format(Locale.ROOT, SmcKit.SMC_KEY_FAN_SPEED, i));
        }
        SmcKit.smcClose(conn);
        return fanSpeeds;
    }

    /**
     * Queries the CPU voltage from the SMC.
     *
     * @return The CPU voltage in Volts, or 0.0 if unable to retrieve.
     */
    @Override
    public double queryCpuVoltage() {
        IOConnect conn = SmcKit.smcOpen();
        double volts = SmcKit.smcGetFloat(conn, SmcKit.SMC_KEY_CPU_VOLTAGE) / 1000d;
        SmcKit.smcClose(conn);
        return volts;
    }

}
