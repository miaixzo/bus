/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;

/**
 * Sensors from prtpicl
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class SolarisSensors extends AbstractSensors {

    @Override
    public double queryCpuTemperature() {
        double maxTemp = 0d;
        // Return max found temp
        for (String line : Executor.runNative("/usr/sbin/prtpicl -v -c temperature-sensor")) {
            if (line.trim().startsWith("Temperature:")) {
                int temp = Parsing.parseLastInt(line, 0);
                if (temp > maxTemp) {
                    maxTemp = temp;
                }
            }
        }
        // If it's in millidegrees:
        if (maxTemp > 1000) {
            maxTemp /= 1000;
        }
        return maxTemp;
    }

    @Override
    public int[] queryFanSpeeds() {
        List<Integer> speedList = new ArrayList<>();
        // Return max found temp
        for (String line : Executor.runNative("/usr/sbin/prtpicl -v -c fan")) {
            if (line.trim().startsWith("Speed:")) {
                speedList.add(Parsing.parseLastInt(line, 0));
            }
        }
        int[] fans = new int[speedList.size()];
        for (int i = 0; i < speedList.size(); i++) {
            fans[i] = speedList.get(i);
        }
        return fans;
    }

    @Override
    public double queryCpuVoltage() {
        double voltage = 0d;
        for (String line : Executor.runNative("/usr/sbin/prtpicl -v -c voltage-sensor")) {
            if (line.trim().startsWith("Voltage:")) {
                voltage = Parsing.parseDoubleOrDefault(line.replace("Voltage:", Normal.EMPTY).trim(), 0d);
                break;
            }
        }
        return voltage;
    }

}
