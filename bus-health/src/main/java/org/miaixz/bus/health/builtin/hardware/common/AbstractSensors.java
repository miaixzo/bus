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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.Arrays;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Sensors;

/**
 * Sensors from WMI or Open Hardware Monitor
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractSensors implements Sensors {

    private final Supplier<Double> cpuTemperature = Memoizer
            .memoize(this::queryCpuTemperature, Memoizer.defaultExpiration());

    private final Supplier<int[]> fanSpeeds = Memoizer.memoize(this::queryFanSpeeds, Memoizer.defaultExpiration());

    private final Supplier<Double> cpuVoltage = Memoizer.memoize(this::queryCpuVoltage, Memoizer.defaultExpiration());

    @Override
    public double getCpuTemperature() {
        return cpuTemperature.get();
    }

    protected abstract double queryCpuTemperature();

    @Override
    public int[] getFanSpeeds() {
        return fanSpeeds.get();
    }

    protected abstract int[] queryFanSpeeds();

    @Override
    public double getCpuVoltage() {
        return cpuVoltage.get();
    }

    protected abstract double queryCpuVoltage();

    @Override
    public String toString() {
        String sb = "CPU Temperature=" + getCpuTemperature() + "C, " + "Fan Speeds=" + Arrays.toString(getFanSpeeds())
                + ", " + "CPU Voltage=" + getCpuVoltage();
        return sb;
    }

}
