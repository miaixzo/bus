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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query Open Hardware Monitor WMI data for Hardware
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class OhmHardware {

    private static final String HARDWARE = "Hardware";

    /**
     * Queries the hardware identifiers for a monitored type.
     *
     * @param h           An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @param typeToQuery which type to filter based on
     * @param typeName    the name of the type
     * @return The sensor value.
     */
    public static WmiResult<IdentifierProperty> queryHwIdentifier(
            WmiQueryHandler h,
            String typeToQuery,
            String typeName) {
        WmiQuery<IdentifierProperty> cpuIdentifierQuery = new WmiQuery<>(WmiKit.OHM_NAMESPACE,
                HARDWARE + " WHERE " + typeToQuery + "Type=\"" + typeName + '\"', IdentifierProperty.class);
        return h.queryWMI(cpuIdentifierQuery, false);
    }

    /**
     * HW Identifier Property
     */
    public enum IdentifierProperty {
        IDENTIFIER
    }

}
