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
package org.miaixz.bus.health.unix.aix.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;

/**
 * Sensors not available except counting fans from lscfg
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class AixSensors extends AbstractSensors {

    /**
     * The lscfg value.
     */
    private final Supplier<List<String>> lscfg;

    /**
     * Creates a new AixSensors instance.
     *
     * @param lscfg the lscfg
     */
    AixSensors(Supplier<List<String>> lscfg) {
        this.lscfg = lscfg;
    }

    /**
     * Queries the cpu temperature.
     *
     * @return the query cpu temperature result
     */
    @Override
    public double queryCpuTemperature() {
        // Not available in general without specialized software
        return 0d;
    }

    /**
     * Queries the fan speeds.
     *
     * @return the query fan speeds result
     */
    @Override
    public int[] queryFanSpeeds() {
        // Speeds are not available in general without specialized software
        // We can count fans from lscfg and return an appropriate sized array of zeroes.
        int fans = 0;
        for (String s : lscfg.get()) {
            if (s.contains("Air Mover")) {
                fans++;
            }
        }
        return new int[fans];
    }

    /**
     * Queries the cpu voltage.
     *
     * @return the query cpu voltage result
     */
    @Override
    public double queryCpuVoltage() {
        // Not available in general without specialized software
        return 0d;
    }

}
