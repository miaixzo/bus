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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;

/**
 * Sensors
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class OpenBsdSensors extends AbstractSensors {

    private final Supplier<Triplet<Double, int[], Double>> tempFanVolts = Memoizer
            .memoize(OpenBsdSensors::querySensors, Memoizer.defaultExpiration());

    private static Triplet<Double, int[], Double> querySensors() {
        double volts = 0d;
        List<Double> cpuTemps = new ArrayList<>();
        List<Double> allTemps = new ArrayList<>();
        List<Integer> fanRPMs = new ArrayList<>();
        for (String line : Executor.runNative("systat -ab sensors")) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 1) {
                if (split[0].contains("cpu")) {
                    if (split[0].contains("temp0")) {
                        cpuTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                    } else if (split[0].contains("volt0")) {
                        volts = Parsing.parseDoubleOrDefault(split[1], 0d);
                    }
                } else if (split[0].contains("temp0")) {
                    allTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                } else if (split[0].contains("fan")) {
                    fanRPMs.add(Parsing.parseIntOrDefault(split[1], 0));
                }
            }
        }
        // Prefer cpu temps
        double temp = cpuTemps.isEmpty() ? listAverage(allTemps) : listAverage(cpuTemps);
        // Collect all fans
        int[] fans = new int[fanRPMs.size()];
        for (int i = 0; i < fans.length; i++) {
            fans[i] = fanRPMs.get(i);
        }
        return Triplet.of(temp, fans, volts);
    }

    private static double listAverage(List<Double> doubles) {
        double sum = 0d;
        int count = 0;
        for (Double d : doubles) {
            if (!d.isNaN()) {
                sum += d;
                count++;
            }
        }
        return count > 0 ? sum / count : 0d;
    }

    @Override
    public double queryCpuTemperature() {
        return tempFanVolts.get().getLeft();
    }

    @Override
    public int[] queryFanSpeeds() {
        return tempFanVolts.get().getMiddle();
    }

    @Override
    public double queryCpuVoltage() {
        return tempFanVolts.get().getRight();
    }

}
