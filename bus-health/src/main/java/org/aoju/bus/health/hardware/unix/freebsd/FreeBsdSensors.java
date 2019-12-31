/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.health.common.unix.freebsd.FreeBsdLibc;
import org.aoju.bus.health.hardware.AbstractSensors;

/**
 * <p>
 * FreeBsdSensors class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class FreeBsdSensors extends AbstractSensors {

    @Override
    public double queryCpuTemperature() {
        return queryKldloadCoretemp();
    }

    private double queryKldloadCoretemp() {
        String name = "dev.cpu.%d.temperature";
        IntByReference size = new IntByReference(FreeBsdLibc.INT_SIZE);
        Pointer p = new Memory(size.getValue());
        int cpu = 0;
        double sumTemp = 0d;
        while (0 == FreeBsdLibc.INSTANCE.sysctlbyname(String.format(name, cpu), p, size, null, 0)) {
            sumTemp += p.getInt(0) / 10d - 273.15;
            cpu++;
        }
        return cpu > 0 ? sumTemp / cpu : Double.NaN;
    }

    @Override
    public int[] queryFanSpeeds() {
        return Normal.EMPTY_INT_ARRAY;
    }


    @Override
    public double queryCpuVoltage() {
        return 0d;
    }
}
