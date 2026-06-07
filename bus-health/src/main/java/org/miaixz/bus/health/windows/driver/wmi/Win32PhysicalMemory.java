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
 * Utility to query WMI class {@code Win32_PhysicalMemory}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Win32PhysicalMemory {

    /**
     * Prevents instantiation of utility class.
     */
    private Win32PhysicalMemory() {
    }

    /**
     * The WMI class name.
     */
    public static final String WIN32_PHYSICAL_MEMORY = "Win32_PhysicalMemory";

    /**
     * Queries physical memory info for Win10 and later.
     *
     * @return Information regarding physical memory.
     */
    public static WmiResult<PhysicalMemoryProperty> queryPhysicalMemory() {
        WmiQuery<PhysicalMemoryProperty> physicalMemoryQuery = new WmiQuery<>(WIN32_PHYSICAL_MEMORY,
                PhysicalMemoryProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(physicalMemoryQuery);
    }

    /**
     * Queries physical memory info for Win8 and earlier.
     *
     * @return Information regarding physical memory.
     */
    public static WmiResult<PhysicalMemoryPropertyWin8> queryPhysicalMemoryWin8() {
        WmiQuery<PhysicalMemoryPropertyWin8> physicalMemoryQuery = new WmiQuery<>(WIN32_PHYSICAL_MEMORY,
                PhysicalMemoryPropertyWin8.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(physicalMemoryQuery);
    }

    /**
     * Physical Memory properties for Win10 and later.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PhysicalMemoryProperty {
        /**
         * The BANKLABEL WMI property.
         */
        BANKLABEL,
        /**
         * The CAPACITY WMI property.
         */
        CAPACITY,
        /**
         * The SPEED WMI property.
         */
        SPEED,
        /**
         * The MANUFACTURER WMI property.
         */
        MANUFACTURER,
        /**
         * The PARTNUMBER WMI property.
         */
        PARTNUMBER,
        /**
         * The SMBIOSMEMORYTYPE WMI property.
         */
        SMBIOSMEMORYTYPE,
        /**
         * The SERIALNUMBER WMI property.
         */
        SERIALNUMBER

    }

    /**
     * Physical Memory properties for Win8 and earlier.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum PhysicalMemoryPropertyWin8 {
        /**
         * The BANKLABEL WMI property.
         */
        BANKLABEL,
        /**
         * The CAPACITY WMI property.
         */
        CAPACITY,
        /**
         * The SPEED WMI property.
         */
        SPEED,
        /**
         * The MANUFACTURER WMI property.
         */
        MANUFACTURER,
        /**
         * The MEMORYTYPE WMI property.
         */
        MEMORYTYPE,
        /**
         * The PARTNUMBER WMI property.
         */
        PARTNUMBER,
        /**
         * The SERIALNUMBER WMI property.
         */
        SERIALNUMBER

    }

}
