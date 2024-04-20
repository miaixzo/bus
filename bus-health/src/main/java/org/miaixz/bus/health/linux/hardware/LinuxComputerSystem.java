/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
package org.miaixz.bus.health.linux.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.Memoize;
import org.miaixz.bus.health.builtin.hardware.AbstractComputerSystem;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.linux.drivers.*;
import org.miaixz.bus.health.linux.drivers.proc.CpuInfo;

import java.util.function.Supplier;

/**
 * Hardware data obtained from sysfs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxComputerSystem extends AbstractComputerSystem {

    private final Supplier<String> manufacturer = Memoize.memoize(LinuxComputerSystem::queryManufacturer);

    private final Supplier<String> model = Memoize.memoize(LinuxComputerSystem::queryModel);

    private final Supplier<String> serialNumber = Memoize.memoize(LinuxComputerSystem::querySerialNumber);

    private final Supplier<String> uuid = Memoize.memoize(LinuxComputerSystem::queryUUID);

    private static String queryManufacturer() {
        String result;
        if ((result = Sysfs.querySystemVendor()) == null && (result = CpuInfo.queryCpuManufacturer()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String queryModel() {
        String result;
        if ((result = Sysfs.queryProductModel()) == null && (result = DeviceTree.queryModel()) == null
                && (result = Lshw.queryModel()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String querySerialNumber() {
        String result;
        if ((result = Sysfs.queryProductSerial()) == null && (result = Dmidecode.querySerialNumber()) == null
                && (result = Lshal.querySerialNumber()) == null && (result = Lshw.querySerialNumber()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String queryUUID() {
        String result;
        if ((result = Sysfs.queryUUID()) == null && (result = Dmidecode.queryUUID()) == null
                && (result = Lshal.queryUUID()) == null && (result = Lshw.queryUUID()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    @Override
    public String getHardwareUUID() {
        return uuid.get();
    }

    @Override
    public Firmware createFirmware() {
        return new LinuxFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new LinuxBaseboard();
    }

}
