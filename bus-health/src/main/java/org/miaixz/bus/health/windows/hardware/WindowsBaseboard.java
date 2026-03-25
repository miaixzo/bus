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
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32BaseBoard;
import org.miaixz.bus.health.windows.driver.wmi.Win32BaseBoard.BaseBoardProperty;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Baseboard data obtained from WMI
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class WindowsBaseboard extends AbstractBaseboard {

    private final Supplier<Tuple> manufModelVersSerial = Memoizer.memoize(WindowsBaseboard::queryManufModelVersSerial);

    private static Tuple queryManufModelVersSerial() {
        String manufacturer = null;
        String model = null;
        String version = null;
        String serialNumber = null;
        WmiResult<BaseBoardProperty> win32BaseBoard = Win32BaseBoard.queryBaseboardInfo();
        if (win32BaseBoard.getResultCount() > 0) {
            manufacturer = WmiKit.getString(win32BaseBoard, BaseBoardProperty.MANUFACTURER, 0);
            model = WmiKit.getString(win32BaseBoard, BaseBoardProperty.MODEL, 0);
            String product = WmiKit.getString(win32BaseBoard, BaseBoardProperty.PRODUCT, 0);
            if (!StringKit.isBlank(product)) {
                model = StringKit.isBlank(model) ? product : (model + " (" + product + ")");
            }
            version = WmiKit.getString(win32BaseBoard, BaseBoardProperty.VERSION, 0);
            serialNumber = WmiKit.getString(win32BaseBoard, BaseBoardProperty.SERIALNUMBER, 0);
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber);
    }

    @Override
    public String getManufacturer() {
        return manufModelVersSerial.get().get(0);
    }

    @Override
    public String getModel() {
        return manufModelVersSerial.get().get(1);
    }

    @Override
    public String getVersion() {
        return manufModelVersSerial.get().get(2);
    }

    @Override
    public String getSerialNumber() {
        return manufModelVersSerial.get().get(3);
    }

}
