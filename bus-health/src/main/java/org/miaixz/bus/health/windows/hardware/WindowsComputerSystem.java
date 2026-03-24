/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.windows.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
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

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Hardware data obtained from WMI.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class WindowsComputerSystem extends AbstractComputerSystem {

    private final Supplier<Pair<String, String>> manufacturerModel = Memoizer
            .memoize(WindowsComputerSystem::queryManufacturerModel);
    private final Supplier<Pair<String, String>> serialNumberUUID = Memoizer
            .memoize(WindowsComputerSystem::querySystemSerialNumberUUID);

    private static Pair<String, String> queryManufacturerModel() {
        String manufacturer = null;
        String model = null;
        WmiResult<ComputerSystemProperty> win32ComputerSystem = Win32ComputerSystem.queryComputerSystem();
        if (win32ComputerSystem.getResultCount() > 0) {
            manufacturer = WmiKit.getString(win32ComputerSystem, ComputerSystemProperty.MANUFACTURER, 0);
            model = WmiKit.getString(win32ComputerSystem, ComputerSystemProperty.MODEL, 0);
        }
        return Pair.of(
                StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model);
    }

    private static Pair<String, String> querySystemSerialNumberUUID() {
        String serialNumber = null;
        String uuid = null;
        WmiResult<ComputerSystemProductProperty> win32ComputerSystemProduct = Win32ComputerSystemProduct
                .queryIdentifyingNumberUUID();
        if (win32ComputerSystemProduct.getResultCount() > 0) {
            serialNumber = WmiKit
                    .getString(win32ComputerSystemProduct, ComputerSystemProductProperty.IDENTIFYINGNUMBER, 0);
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
