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
package org.aoju.bus.health.hardware.mac;

import com.sun.jna.platform.mac.IOKit.IOConnect;
import org.aoju.bus.health.common.mac.SmcUtils;
import org.aoju.bus.health.hardware.AbstractSensors;

/**
 * <p>
 * MacSensors class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class MacSensors extends AbstractSensors {

    private int numFans = 0;

    @Override
    public double queryCpuTemperature() {
        IOConnect conn = SmcUtils.smcOpen();
        double temp = SmcUtils.smcGetFloat(conn, SmcUtils.SMC_KEY_CPU_TEMP);
        SmcUtils.smcClose(conn);
        if (temp > 0d) {
            return temp;
        }
        return 0d;
    }

    @Override
    public int[] queryFanSpeeds() {
        // If we don't have fan # try to get it
        IOConnect conn = SmcUtils.smcOpen();
        if (this.numFans == 0) {
            this.numFans = (int) SmcUtils.smcGetLong(conn, SmcUtils.SMC_KEY_FAN_NUM);
        }
        int[] fanSpeeds = new int[this.numFans];
        for (int i = 0; i < this.numFans; i++) {
            fanSpeeds[i] = (int) SmcUtils.smcGetFloat(conn, String.format(SmcUtils.SMC_KEY_FAN_SPEED, i));
        }
        SmcUtils.smcClose(conn);
        return fanSpeeds;
    }

    @Override
    public double queryCpuVoltage() {
        IOConnect conn = SmcUtils.smcOpen();
        double volts = SmcUtils.smcGetFloat(conn, SmcUtils.SMC_KEY_CPU_VOLTAGE) / 1000d;
        SmcUtils.smcClose(conn);
        return volts;
    }

}
