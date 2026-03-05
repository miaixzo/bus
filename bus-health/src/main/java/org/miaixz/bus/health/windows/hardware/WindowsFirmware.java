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
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios;
import org.miaixz.bus.health.windows.driver.wmi.Win32Bios.BiosProperty;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

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
