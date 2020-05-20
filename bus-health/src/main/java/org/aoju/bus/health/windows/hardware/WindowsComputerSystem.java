/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
 ********************************************************************************/
package org.aoju.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.builtin.hardware.AbstractComputerSystem;
import org.aoju.bus.health.builtin.hardware.Baseboard;
import org.aoju.bus.health.builtin.hardware.Firmware;
import org.aoju.bus.health.windows.WmiQuery;
import org.aoju.bus.health.windows.drivers.Win32Bios;
import org.aoju.bus.health.windows.drivers.Win32Bios.BiosSerialProperty;
import org.aoju.bus.health.windows.drivers.Win32ComputerSystem;
import org.aoju.bus.health.windows.drivers.Win32ComputerSystem.ComputerSystemProperty;
import org.aoju.bus.health.windows.drivers.Win32ComputerSystemProduct;
import org.aoju.bus.health.windows.drivers.Win32ComputerSystemProduct.ComputerSystemProductProperty;

import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.memoize;

/**
 * Hardware data obtained from WMI.
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
@Immutable
final class WindowsComputerSystem extends AbstractComputerSystem {

    private final Supplier<Pair<String, String>> manufacturerModel = memoize(
            WindowsComputerSystem::queryManufacturerModel);
    private final Supplier<String> serialNumber = memoize(WindowsComputerSystem::querySystemSerialNumber);

    private static Pair<String, String> queryManufacturerModel() {
        String manufacturer = null;
        String model = null;
        WmiResult<ComputerSystemProperty> win32ComputerSystem = Win32ComputerSystem.queryComputerSystem();
        if (win32ComputerSystem.getResultCount() > 0) {
            manufacturer = WmiQuery.getString(win32ComputerSystem, ComputerSystemProperty.MANUFACTURER, 0);
            model = WmiQuery.getString(win32ComputerSystem, ComputerSystemProperty.MODEL, 0);
        }
        return Pair.of(StringUtils.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringUtils.isBlank(model) ? Normal.UNKNOWN : model);
    }

    private static String querySystemSerialNumber() {
        String result;
        if (((result = querySerialFromBios()) != null || (result = querySerialFromCsProduct()) != null)
                && !StringUtils.isBlank(result)) {
            return result;
        }
        return Normal.UNKNOWN;
    }

    private static String querySerialFromBios() {
        WmiResult<BiosSerialProperty> serialNum = Win32Bios.querySerialNumber();
        if (serialNum.getResultCount() > 0) {
            return WmiQuery.getString(serialNum, BiosSerialProperty.SERIALNUMBER, 0);
        }
        return null;
    }

    private static String querySerialFromCsProduct() {
        WmiResult<ComputerSystemProductProperty> identifyingNumber = Win32ComputerSystemProduct
                .queryIdentifyingNumber();
        if (identifyingNumber.getResultCount() > 0) {
            return WmiQuery.getString(identifyingNumber, ComputerSystemProductProperty.IDENTIFYINGNUMBER, 0);
        }
        return null;
    }

    @Override
    public String getManufacturer() {
        return manufacturerModel.get().getLeft();
    }

    @Override
    public String getModel() {
        return manufacturerModel.get().getRight();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    @Override
    public Firmware createFirmware() {
        return new WindowsFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new WindowsBaseboard();
    }

}
