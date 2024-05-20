/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios.BiosSerialProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32ComputerSystem;
import org.miaixz.bus.health.windows.driver.wmi.Win32ComputerSystem.ComputerSystemProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32ComputerSystemProduct;
import org.miaixz.bus.health.windows.driver.wmi.Win32ComputerSystemProduct.ComputerSystemProductProperty;

import java.util.function.Supplier;

/**
 * Hardware data obtained from WMI.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class WindowsComputerSystem extends AbstractComputerSystem {

    private final Supplier<Pair<String, String>> manufacturerModel = Memoizer.memoize(
            WindowsComputerSystem::queryManufacturerModel);
    private final Supplier<Pair<String, String>> serialNumberUUID = Memoizer.memoize(
            WindowsComputerSystem::querySystemSerialNumberUUID);

    private static Pair<String, String> queryManufacturerModel() {
        String manufacturer = null;
        String model = null;
        WmiResult<ComputerSystemProperty> win32ComputerSystem = Win32ComputerSystem.queryComputerSystem();
        if (win32ComputerSystem.getResultCount() > 0) {
            manufacturer = WmiKit.getString(win32ComputerSystem, ComputerSystemProperty.MANUFACTURER, 0);
            model = WmiKit.getString(win32ComputerSystem, ComputerSystemProperty.MODEL, 0);
        }
        return Pair.of(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model);
    }

    private static Pair<String, String> querySystemSerialNumberUUID() {
        String serialNumber = null;
        String uuid = null;
        WmiResult<ComputerSystemProductProperty> win32ComputerSystemProduct = Win32ComputerSystemProduct
                .queryIdentifyingNumberUUID();
        if (win32ComputerSystemProduct.getResultCount() > 0) {
            serialNumber = WmiKit.getString(win32ComputerSystemProduct,
                    ComputerSystemProductProperty.IDENTIFYINGNUMBER, 0);
            uuid = WmiKit.getString(win32ComputerSystemProduct, ComputerSystemProductProperty.UUID, 0);
        }
        if (StringKit.isBlank(serialNumber)) {
            serialNumber = querySerialFromBios();
        }
        if (StringKit.isBlank(serialNumber)) {
            serialNumber = Normal.UNKNOWN;
        }
        if (StringKit.isBlank(uuid)) {
            uuid = Normal.UNKNOWN;
        }
        return Pair.of(serialNumber, uuid);
    }

    private static String querySerialFromBios() {
        WmiResult<BiosSerialProperty> serialNum = Win32Bios.querySerialNumber();
        if (serialNum.getResultCount() > 0) {
            return WmiKit.getString(serialNum, BiosSerialProperty.SERIALNUMBER, 0);
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
        return serialNumberUUID.get().getLeft();
    }

    @Override
    public String getHardwareUUID() {
        return serialNumberUUID.get().getRight();
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
