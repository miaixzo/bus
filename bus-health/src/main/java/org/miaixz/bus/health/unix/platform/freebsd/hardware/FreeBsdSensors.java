/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import com.sun.jna.Memory;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.unix.jna.FreeBsdLibc;

import java.util.Locale;

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
                while (0 == FreeBsdLibc.INSTANCE.sysctlbyname(String.format(Locale.ROOT, name, cpu), p, size, null,
                        size_t.ZERO)) {
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
