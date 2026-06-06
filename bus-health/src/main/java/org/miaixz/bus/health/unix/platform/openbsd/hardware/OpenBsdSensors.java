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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.unix.platform.bsd.Systat;

/**
 * Sensors
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class OpenBsdSensors extends AbstractSensors {

    /**
     * The tempFanVolts value.
     */
    private final Supplier<Triplet<Double, int[], Double>> tempFanVolts = Memoizer
            .memoize(Systat::querySensors, Memoizer.defaultExpiration());

    /**
     * Queries the cpu temperature.
     *
     * @return the query cpu temperature result
     */
    @Override
    public double queryCpuTemperature() {
        return tempFanVolts.get().getLeft();
    }

    /**
     * Queries the fan speeds.
     *
     * @return the query fan speeds result
     */
    @Override
    public int[] queryFanSpeeds() {
        return tempFanVolts.get().getMiddle();
    }

    /**
     * Queries the cpu voltage.
     *
     * @return the query cpu voltage result
     */
    @Override
    public double queryCpuVoltage() {
        return tempFanVolts.get().getRight();
    }

}
