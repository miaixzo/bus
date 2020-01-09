/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.windows.WmiQueryHandler;
import org.aoju.bus.health.common.windows.WmiUtils;
import org.aoju.bus.health.hardware.AbstractComputerSystem;
import org.aoju.bus.health.hardware.Baseboard;
import org.aoju.bus.health.hardware.Firmware;

import java.util.function.Supplier;

/**
 * Hardware data obtained from WMI.
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
final class WindowsComputerSystem extends AbstractComputerSystem {

    private final WmiQueryHandler wmiQueryHandler = WmiQueryHandler.createInstance();
    private final Supplier<ManufacturerModel> manufacturerModel = Memoizer.memoize(this::queryManufacturerModel);
    private final Supplier<String> serialNumber = Memoizer.memoize(this::querySystemSerialNumber);

    @Override
    public String getManufacturer() {
        return manufacturerModel.get().manufacturer;
    }

    @Override
    public String getModel() {
        return manufacturerModel.get().model;
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

    private ManufacturerModel queryManufacturerModel() {
        String manufacturer = null;
        String model = null;
        WmiQuery<ComputerSystemProperty> computerSystemQuery = new WmiQuery<>("Win32_ComputerSystem",
                ComputerSystemProperty.class);
        WmiResult<ComputerSystemProperty> win32ComputerSystem = wmiQueryHandler.queryWMI(computerSystemQuery);
        if (win32ComputerSystem.getResultCount() > 0) {
            manufacturer = WmiUtils.getString(win32ComputerSystem, ComputerSystemProperty.MANUFACTURER, 0);
            model = WmiUtils.getString(win32ComputerSystem, ComputerSystemProperty.MODEL, 0);
        }
        return new ManufacturerModel(manufacturer, model);
    }

    private String querySystemSerialNumber() {
        String result;
        if ((result = querySerialFromBios()) == null && (result = querySerialFromCsProduct()) == null) {
            return Builder.UNKNOWN;
        }
        return StringUtils.isBlank(result) ? Builder.UNKNOWN : result;
    }

    private String querySerialFromBios() {
        String result = null;
        WmiQuery<BiosProperty> serialNumQuery = new WmiQuery<>("Win32_BIOS where PrimaryBIOS=true", BiosProperty.class);
        WmiResult<BiosProperty> serialNum = wmiQueryHandler.queryWMI(serialNumQuery);
        if (serialNum.getResultCount() > 0) {
            result = WmiUtils.getString(serialNum, BiosProperty.SERIALNUMBER, 0);
        }
        return result;
    }

    private String querySerialFromCsProduct() {
        String result = null;
        WmiQuery<ComputerSystemProductProperty> identifyingNumberQuery = new WmiQuery<>("Win32_ComputerSystemProduct",
                ComputerSystemProductProperty.class);
        WmiResult<ComputerSystemProductProperty> identifyingNumber = wmiQueryHandler.queryWMI(identifyingNumberQuery);
        if (identifyingNumber.getResultCount() > 0) {
            result = WmiUtils.getString(identifyingNumber, ComputerSystemProductProperty.IDENTIFYINGNUMBER, 0);
        }
        return result;
    }

    enum ComputerSystemProperty {
        MANUFACTURER, MODEL
    }

    enum BiosProperty {
        SERIALNUMBER
    }

    enum ComputerSystemProductProperty {
        IDENTIFYINGNUMBER
    }

    private static final class ManufacturerModel {
        private final String manufacturer;
        private final String model;

        private ManufacturerModel(String manufacturer, String model) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.model = StringUtils.isBlank(model) ? Builder.UNKNOWN : model;
        }
    }
}
