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
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.windows.WmiQueryHandler;
import org.aoju.bus.health.common.windows.WmiUtils;
import org.aoju.bus.health.hardware.AbstractBaseboard;

import java.util.function.Supplier;

/**
 * Baseboard data obtained from WMI
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
final class WindowsBaseboard extends AbstractBaseboard {

    private final Supplier<WmiStrings> wmi = Memoizer.memoize(this::queryWmi);

    @Override
    public String getManufacturer() {
        return wmi.get().manufacturer;
    }

    @Override
    public String getModel() {
        return wmi.get().model;
    }

    @Override
    public String getVersion() {
        return wmi.get().version;
    }

    @Override
    public String getSerialNumber() {
        return wmi.get().serialNumber;
    }

    private WmiStrings queryWmi() {
        WmiQuery<BaseboardProperty> baseboardQuery = new WmiQuery<>("Win32_BaseBoard", BaseboardProperty.class);
        WmiResult<BaseboardProperty> win32BaseBoard = WmiQueryHandler.createInstance().queryWMI(baseboardQuery);
        if (win32BaseBoard.getResultCount() > 0) {
            return new WmiStrings(WmiUtils.getString(win32BaseBoard, BaseboardProperty.MANUFACTURER, 0),
                    WmiUtils.getString(win32BaseBoard, BaseboardProperty.MODEL, 0),
                    WmiUtils.getString(win32BaseBoard, BaseboardProperty.VERSION, 0),
                    WmiUtils.getString(win32BaseBoard, BaseboardProperty.SERIALNUMBER, 0));
        }
        return new WmiStrings(Builder.UNKNOWN, Builder.UNKNOWN, Builder.UNKNOWN, Builder.UNKNOWN);
    }

    enum BaseboardProperty {
        MANUFACTURER, MODEL, VERSION, SERIALNUMBER;
    }

    private static final class WmiStrings {
        private final String manufacturer;
        private final String model;
        private final String version;
        private final String serialNumber;

        private WmiStrings(String manufacturer, String model, String version, String serialNumber) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.model = StringUtils.isBlank(model) ? Builder.UNKNOWN : model;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.serialNumber = StringUtils.isBlank(serialNumber) ? Builder.UNKNOWN : serialNumber;
        }
    }

}
