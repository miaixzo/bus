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
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query WMI class {@code Win32_OperatingSystem}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Win32OperatingSystem {

    private static final String WIN32_OPERATING_SYSTEM = "Win32_OperatingSystem";

    /**
     * Queries the Computer System.
     *
     * @return Computer System Manufacturer and Model
     */
    public static WmiResult<OSVersionProperty> queryOsVersion() {
        WmiQuery<OSVersionProperty> osVersionQuery = new WmiQuery<>(WIN32_OPERATING_SYSTEM, OSVersionProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(osVersionQuery);
    }

    /**
     * Operating System properties
     */
    public enum OSVersionProperty {
        VERSION, PRODUCTTYPE, BUILDNUMBER, CSDVERSION, SUITEMASK
    }

}
