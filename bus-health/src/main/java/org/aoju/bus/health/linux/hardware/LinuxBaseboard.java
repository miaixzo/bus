/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.linux.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Quartet;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractBaseboard;
import org.aoju.bus.health.linux.drivers.CpuInfo;
import org.aoju.bus.health.linux.drivers.Sysfs;

import java.util.function.Supplier;

/**
 * Baseboard data obtained by sysfs
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
@Immutable
final class LinuxBaseboard extends AbstractBaseboard {

    private final Supplier<Quartet<String, String, String, String>> manufacturerModelVersionSerial = Memoize.memoize(
            CpuInfo::queryBoardInfo);
    private final Supplier<String> manufacturer = Memoize.memoize(this::queryManufacturer);
    private final Supplier<String> model = Memoize.memoize(this::queryModel);
    private final Supplier<String> version = Memoize.memoize(this::queryVersion);
    private final Supplier<String> serialNumber = Memoize.memoize(this::querySerialNumber);

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    private String queryManufacturer() {
        String result;
        if (null == (result = Sysfs.queryBoardVendor())
                && null == (result = manufacturerModelVersionSerial.get().getA())) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryModel() {
        String result;
        if (null == (result = Sysfs.queryBoardModel())
                && null == (result = manufacturerModelVersionSerial.get().getB())) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryVersion() {
        String result;
        if (null == (result = Sysfs.queryBoardVersion())
                && null == (result = manufacturerModelVersionSerial.get().getC())) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String querySerialNumber() {
        String result;
        if (null == (result = Sysfs.queryBoardSerial())
                && null == (result = manufacturerModelVersionSerial.get().getD())) {
            return Normal.UNKNOWN;
        }
        return result;
    }

}
