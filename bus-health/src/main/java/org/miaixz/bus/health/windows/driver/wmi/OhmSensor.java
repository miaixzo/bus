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
 * Utility to query Open Hardware Monitor WMI data for Sensors
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class OhmSensor {

    /**
     * Prevents instantiation of utility class.
     */
    private OhmSensor() {
        // No initialization required.
    }

    /**
     * The WMI namespace for Open Hardware Monitor.
     */
    public static final String OHM_NAMESPACE = WmiKit.OHM_NAMESPACE;

    /**
     * The WMI class name for sensors.
     */
    public static final String SENSOR = "Sensor";

    /**
     * Queries the sensor value of an hardware identifier and sensor type.
     *
     * @param h          An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @param identifier The identifier whose value to query.
     * @param sensorType The type of sensor to query.
     * @return The sensor value.
     */
    public static WmiResult<ValueProperty> querySensorValue(WmiQueryHandler h, String identifier, String sensorType) {
        WmiQuery<ValueProperty> ohmSensorQuery = new WmiQuery<>(OHM_NAMESPACE,
                buildSensorWmiClassNameWithWhere(identifier, sensorType), ValueProperty.class);
        return h.queryWMI(ohmSensorQuery, false);
    }

    /**
     * Builds the WMI class name with WHERE clause for sensor value queries.
     *
     * @param identifier The identifier whose value to query
     * @param sensorType The type of sensor to query
     * @return the WMI class name with WHERE clause
     */
    public static String buildSensorWmiClassNameWithWhere(String identifier, String sensorType) {
        StringBuilder sb = new StringBuilder(SENSOR);
        sb.append(" WHERE Parent = \"").append(identifier);
        sb.append("\" AND SensorType = \"").append(sensorType).append('"');
        return sb.toString();
    }

    /**
     * Sensor value property
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ValueProperty {
        /**
         * Executes the value operation.
         */
        VALUE

    }

}
