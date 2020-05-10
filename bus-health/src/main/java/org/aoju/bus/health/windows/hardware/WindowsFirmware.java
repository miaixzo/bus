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
import org.aoju.bus.core.lang.tuple.Quintet;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.builtin.hardware.AbstractFirmware;
import org.aoju.bus.health.windows.WmiQuery;
import org.aoju.bus.health.windows.drivers.Win32Bios;
import org.aoju.bus.health.windows.drivers.Win32Bios.BiosProperty;

import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.memoize;

/**
 * Firmware data obtained from WMI
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
@Immutable
final class WindowsFirmware extends AbstractFirmware {

    private final Supplier<Quintet<String, String, String, String, String>> manufNameDescVersRelease = memoize(
            WindowsFirmware::queryManufNameDescVersRelease);

    private static Quintet<String, String, String, String, String> queryManufNameDescVersRelease() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;
        WmiResult<BiosProperty> win32BIOS = Win32Bios.queryBiosInfo();
        if (win32BIOS.getResultCount() > 0) {
            manufacturer = WmiQuery.getString(win32BIOS, BiosProperty.MANUFACTURER, 0);
            name = WmiQuery.getString(win32BIOS, BiosProperty.NAME, 0);
            description = WmiQuery.getString(win32BIOS, BiosProperty.DESCRIPTION, 0);
            version = WmiQuery.getString(win32BIOS, BiosProperty.VERSION, 0);
            releaseDate = WmiQuery.getDateString(win32BIOS, BiosProperty.RELEASEDATE, 0);
        }
        return new Quintet<>(StringUtils.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringUtils.isBlank(name) ? Normal.UNKNOWN : name,
                StringUtils.isBlank(description) ? Normal.UNKNOWN : description,
                StringUtils.isBlank(version) ? Normal.UNKNOWN : version,
                StringUtils.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    @Override
    public String getManufacturer() {
        return manufNameDescVersRelease.get().getA();
    }

    @Override
    public String getName() {
        return manufNameDescVersRelease.get().getB();
    }

    @Override
    public String getDescription() {
        return manufNameDescVersRelease.get().getC();
    }

    @Override
    public String getVersion() {
        return manufNameDescVersRelease.get().getD();
    }

    @Override
    public String getReleaseDate() {
        return manufNameDescVersRelease.get().getE();
    }

}
