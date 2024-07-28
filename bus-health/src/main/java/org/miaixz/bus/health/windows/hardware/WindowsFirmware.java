/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios.BiosProperty;

import java.util.function.Supplier;

/**
 * Firmware data obtained from WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class WindowsFirmware extends AbstractFirmware {

    private final Supplier<Tuple> manufNameDescVersRelease = Memoizer
            .memoize(WindowsFirmware::queryManufNameDescVersRelease);

    private static Tuple queryManufNameDescVersRelease() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;
        WmiResult<BiosProperty> win32BIOS = Win32Bios.queryBiosInfo();
        if (win32BIOS.getResultCount() > 0) {
            manufacturer = WmiKit.getString(win32BIOS, BiosProperty.MANUFACTURER, 0);
            name = WmiKit.getString(win32BIOS, BiosProperty.NAME, 0);
            description = WmiKit.getString(win32BIOS, BiosProperty.DESCRIPTION, 0);
            version = WmiKit.getString(win32BIOS, BiosProperty.VERSION, 0);
            releaseDate = WmiKit.getDateString(win32BIOS, BiosProperty.RELEASEDATE, 0);
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(name) ? Normal.UNKNOWN : name,
                StringKit.isBlank(description) ? Normal.UNKNOWN : description,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    @Override
    public String getManufacturer() {
        return manufNameDescVersRelease.get().get(0);
    }

    @Override
    public String getName() {
        return manufNameDescVersRelease.get().get(1);
    }

    @Override
    public String getDescription() {
        return manufNameDescVersRelease.get().get(2);
    }

    @Override
    public String getVersion() {
        return manufNameDescVersRelease.get().get(3);
    }

    @Override
    public String getReleaseDate() {
        return manufNameDescVersRelease.get().get(4);
    }

}
