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
 * Utility to query Open Hardware Monitor WMI data for Sensors
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class OhmSensor {

    private static final String SENSOR = "Sensor";

    /**
     * Queries the sensor value of an hardware identifier and sensor type.
     *
     * @param h          An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @param identifier The identifier whose value to query.
     * @param sensorType The type of sensor to query.
     * @return The sensor value.
     */
    public static WmiResult<ValueProperty> querySensorValue(WmiQueryHandler h, String identifier, String sensorType) {
        String sb = SENSOR + " WHERE Parent = \"" + identifier + "\" AND SensorType=\"" + sensorType + '\"';
        WmiQuery<ValueProperty> ohmSensorQuery = new WmiQuery<>(WmiKit.OHM_NAMESPACE, sb, ValueProperty.class);
        return h.queryWMI(ohmSensorQuery, false);
    }

    /**
     * Sensor value property
     */
    public enum ValueProperty {
        VALUE
    }

}
