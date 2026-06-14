/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiQueryHandler;

/**
 * Utility to query WMI class {@code Win32_BIOS}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Win32Bios {

    /**
     * Prevents instantiation of utility class.
     */
    private Win32Bios() {
    }

    /**
     * The WMI class name with WHERE clause for primary BIOS.
     */
    public static final String WIN32_BIOS_WHERE_PRIMARY_BIOS_TRUE = "Win32_BIOS where PrimaryBIOS=true";

    /**
     * Queries the BIOS serial number.
     *
     * @return Assigned serial number of the software element.
     */
    public static WmiResult<BiosSerialProperty> querySerialNumber() {
        WmiQuery<BiosSerialProperty> serialNumQuery = new WmiQuery<>(WIN32_BIOS_WHERE_PRIMARY_BIOS_TRUE,
                BiosSerialProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(serialNumQuery);
    }

    /**
     * Queries the BIOS description.
     *
     * @return BIOS name, description, and related fields.
     */
    public static WmiResult<BiosProperty> queryBiosInfo() {
        WmiQuery<BiosProperty> biosQuery = new WmiQuery<>(WIN32_BIOS_WHERE_PRIMARY_BIOS_TRUE, BiosProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(biosQuery);
    }

    /**
     * Serial number property.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum BiosSerialProperty {
        /**
         * The SERIALNUMBER WMI property.
         */
        SERIALNUMBER

    }

    /**
     * BIOS description properties.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum BiosProperty {
        /**
         * The MANUFACTURER WMI property.
         */
        MANUFACTURER,
        /**
         * The NAME WMI property.
         */
        NAME,
        /**
         * The DESCRIPTION WMI property.
         */
        DESCRIPTION,
        /**
         * The VERSION WMI property.
         */
        VERSION,
        /**
         * The RELEASEDATE WMI property.
         */
        RELEASEDATE

    }

}
