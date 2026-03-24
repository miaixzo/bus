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
 * Utility to query WMI class {@code Win32_LogicalDisk}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Win32LogicalDisk {

    private static final String WIN32_LOGICAL_DISK = "Win32_LogicalDisk";

    /**
     * Queries logical disk information
     *
     * @param nameToMatch an optional string to filter match, null otherwise
     * @param localOnly   Whether to only search local drives
     * @return Logical Disk Information
     */
    public static WmiResult<LogicalDiskProperty> queryLogicalDisk(String nameToMatch, boolean localOnly) {
        StringBuilder wmiClassName = new StringBuilder(WIN32_LOGICAL_DISK);
        boolean where = false;
        if (localOnly) {
            wmiClassName.append(" WHERE DriveType = 2 OR DriveType = 3 OR DriveType = 6");
            where = true;
        }
        if (nameToMatch != null) {
            wmiClassName.append(where ? " AND" : " WHERE").append(" Name=\"").append(nameToMatch).append('\"');
        }
        WmiQuery<LogicalDiskProperty> logicalDiskQuery = new WmiQuery<>(wmiClassName.toString(),
                LogicalDiskProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(logicalDiskQuery);
    }

    /**
     * Logical disk properties.
     */
    public enum LogicalDiskProperty {
        ACCESS, DESCRIPTION, DRIVETYPE, FILESYSTEM, FREESPACE, NAME, PROVIDERNAME, SIZE, VOLUMENAME
    }

}
