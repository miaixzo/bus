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

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;

/**
 * Utility to query Open Hardware Monitor WMI data for Hardware
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class OhmHardware {

    /**
     * Prevents instantiation of utility class.
     */
    private OhmHardware() {
    }

    /**
     * The WMI namespace for Open Hardware Monitor.
     */
    public static final String OHM_NAMESPACE = WmiKit.OHM_NAMESPACE;

    /**
     * The WMI class name for hardware.
     */
    public static final String HARDWARE = "Hardware";

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
        WmiQuery<IdentifierProperty> cpuIdentifierQuery = new WmiQuery<>(OHM_NAMESPACE,
                buildHardwareWmiClassNameWithWhere(typeToQuery, typeName), IdentifierProperty.class);
        return h.queryWMI(cpuIdentifierQuery, false);
    }

    /**
     * Builds the WMI class name with WHERE clause for hardware identifier queries.
     *
     * @param typeToQuery which type to filter based on
     * @param typeName    the name of the type
     * @return the WMI class name with WHERE clause
     */
    public static String buildHardwareWmiClassNameWithWhere(String typeToQuery, String typeName) {
        StringBuilder sb = new StringBuilder(HARDWARE);
        sb.append(" WHERE ").append(typeToQuery).append("Type=\"").append(typeName).append('"');
        return sb.toString();
    }

    /**
     * HW Identifier Property
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IdentifierProperty {
        /**
         * Executes the identifier operation.
         */
        IDENTIFIER

    }

}
