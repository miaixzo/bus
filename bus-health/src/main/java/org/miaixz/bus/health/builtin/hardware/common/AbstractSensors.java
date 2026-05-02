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

    /**
     * The cpuTemperature value.
     */
    private final Supplier<Double> cpuTemperature = Memoizer
            .memoize(this::queryCpuTemperature, Memoizer.defaultExpiration());

    /**
     * The fanSpeeds value.
     */
    private final Supplier<int[]> fanSpeeds = Memoizer.memoize(this::queryFanSpeeds, Memoizer.defaultExpiration());

    /**
     * The cpuVoltage value.
     */
    private final Supplier<Double> cpuVoltage = Memoizer.memoize(this::queryCpuVoltage, Memoizer.defaultExpiration());

    /**
     * Returns the cpu temperature.
     *
     * @return the get cpu temperature result
     */
    @Override
    public double getCpuTemperature() {
        return cpuTemperature.get();
    }

    /**
     * Queries the cpu temperature.
     *
     * @return the query cpu temperature result
     */
    protected abstract double queryCpuTemperature();

    /**
     * Returns the fan speeds.
     *
     * @return the get fan speeds result
     */
    @Override
    public int[] getFanSpeeds() {
        return fanSpeeds.get();
    }

    /**
     * Queries the fan speeds.
     *
     * @return the query fan speeds result
     */
    protected abstract int[] queryFanSpeeds();

    /**
     * Returns the cpu voltage.
     *
     * @return the get cpu voltage result
     */
    @Override
    public double getCpuVoltage() {
        return cpuVoltage.get();
    }

    /**
     * Queries the cpu voltage.
     *
     * @return the query cpu voltage result
     */
    protected abstract double queryCpuVoltage();

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        String sb = "CPU Temperature=" + getCpuTemperature() + "C, " + "Fan Speeds=" + Arrays.toString(getFanSpeeds())
                + ", " + "CPU Voltage=" + getCpuVoltage();
        return sb;
    }

}
