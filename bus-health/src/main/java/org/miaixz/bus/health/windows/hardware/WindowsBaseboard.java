/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
 * @since Java 17+
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
