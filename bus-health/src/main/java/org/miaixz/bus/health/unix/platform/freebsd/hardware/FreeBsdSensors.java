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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.Locale;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.unix.jna.FreeBsdLibc;

import com.sun.jna.Memory;
import com.sun.jna.platform.unix.LibCAPI.size_t;

/**
 * Sensors from coretemp
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class FreeBsdSensors extends AbstractSensors {

    /*
     * If user has loaded coretemp module via kldload coretemp, sysctl call will return temperature
     *
     * @return Temperature if successful, otherwise NaN
     */
    private static double queryKldloadCoretemp() {
        String name = "dev.cpu.%d.temperature";
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(FreeBsdLibc.INT_SIZE)) {
            int cpu = 0;
            double sumTemp = 0d;
            try (Memory p = new Memory(size.longValue())) {
                while (0 == FreeBsdLibc.INSTANCE
                        .sysctlbyname(String.format(Locale.ROOT, name, cpu), p, size, null, size_t.ZERO)) {
                    sumTemp += p.getInt(0) / 10d - 273.15;
                    cpu++;
                }
            }
            return cpu > 0 ? sumTemp / cpu : Double.NaN;
        }
    }

    @Override
    public double queryCpuTemperature() {
        return queryKldloadCoretemp();
    }

    @Override
    public int[] queryFanSpeeds() {
        // Nothing known on FreeBSD for this.
        return new int[0];
    }

    @Override
    public double queryCpuVoltage() {
        // Nothing known on FreeBSD for this.
        return 0d;
    }

}
