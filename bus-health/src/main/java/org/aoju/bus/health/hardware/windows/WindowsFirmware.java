/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.windows.WmiQueryHandler;
import org.aoju.bus.health.common.windows.WmiUtils;
import org.aoju.bus.health.hardware.AbstractFirmware;

import java.util.function.Supplier;

/**
 * Firmware data obtained from WMI
 *
 * @author Kimi Liu
 * @version 5.6.8
 * @since JDK 1.8+
 */
final class WindowsFirmware extends AbstractFirmware {

    private final Supplier<WmiStrings> wmi = Memoizer.memoize(this::queryWmi);

    @Override
    public String getManufacturer() {
        return wmi.get().manufacturer;
    }

    @Override
    public String getName() {
        return wmi.get().name;
    }

    @Override
    public String getDescription() {
        return wmi.get().description;
    }

    @Override
    public String getVersion() {
        return wmi.get().version;
    }

    @Override
    public String getReleaseDate() {
        return wmi.get().releaseDate;
    }

    private WmiStrings queryWmi() {
        WmiQuery<BiosProperty> biosQuery = new WmiQuery<>("Win32_BIOS where PrimaryBIOS=true", BiosProperty.class);
        WmiResult<BiosProperty> win32BIOS = WmiQueryHandler.createInstance().queryWMI(biosQuery);
        if (win32BIOS.getResultCount() > 0) {
            return new WmiStrings(WmiUtils.getString(win32BIOS, BiosProperty.MANUFACTURER, 0),
                    WmiUtils.getString(win32BIOS, BiosProperty.NAME, 0),
                    WmiUtils.getString(win32BIOS, BiosProperty.DESCRIPTION, 0),
                    WmiUtils.getString(win32BIOS, BiosProperty.VERSION, 0),
                    WmiUtils.getDateString(win32BIOS, BiosProperty.RELEASEDATE, 0));
        }
        return new WmiStrings(Builder.UNKNOWN, Builder.UNKNOWN, Builder.UNKNOWN, Builder.UNKNOWN,
                Builder.UNKNOWN);
    }

    enum BiosProperty {
        MANUFACTURER, NAME, DESCRIPTION, VERSION, RELEASEDATE;
    }

    private static final class WmiStrings {

        private final String releaseDate;
        private final String manufacturer;
        private final String version;
        private final String name;
        private final String description;

        private WmiStrings(String releaseDate, String manufacturer, String version, String name, String description) {
            this.releaseDate = StringUtils.isBlank(releaseDate) ? Builder.UNKNOWN : releaseDate;
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.name = StringUtils.isBlank(name) ? Builder.UNKNOWN : name;
            this.description = StringUtils.isBlank(description) ? Builder.UNKNOWN : description;
        }

    }

}
