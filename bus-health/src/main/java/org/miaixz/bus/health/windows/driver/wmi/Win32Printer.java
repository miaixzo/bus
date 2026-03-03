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
package org.miaixz.bus.health.windows.driver.wmi;

import java.util.Objects;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.miaixz.bus.health.windows.WmiQueryHandler;

/**
 * Utility to query WMI class {@code Win32_Printer}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Win32Printer {

    private static final String WIN32_PRINTER = "Win32_Printer";

    /**
     * Printer properties from WMI
     */
    public enum PrinterProperty {
        NAME, DRIVERNAME, PRINTERSTATUS, DETECTEDERRORSTATE, DEFAULT, LOCAL, PORTNAME, DESCRIPTION;
    }

    private Win32Printer() {
    }

    /**
     * Queries printer information.
     *
     * @return Information regarding printers
     */
    public static WmiResult<PrinterProperty> queryPrinters() {
        WmiQuery<PrinterProperty> printerQuery = new WmiQuery<>(WIN32_PRINTER, PrinterProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(printerQuery);
    }

}
