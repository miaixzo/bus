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
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query LibreHardwareMonitor WMI sensor data for GPU metrics.
 *
 * <p>
 * LHM publishes sensor data to {@code ROOT\LibreHardwareMonitor} when it is running. This class queries the
 * {@code Sensor} table filtered by hardware parent identifier and sensor type.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LhmSensor {

    private static final String SENSOR = "Sensor";

    /**
     * Sensor properties returned by LHM WMI queries.
     */
    public enum LhmSensorProperty {
        NAME, VALUE, PARENT;
    }

    /**
     * LHM Hardware properties.
     */
    public enum LhmHardwareProperty {
        IDENTIFIER, NAME;
    }

    private LhmSensor() {
    }

    /**
     * Queries all sensors of a given type belonging to a specific hardware parent.
     *
     * @param parent     the LHM hardware identifier (e.g. {@code /gpu-nvidia/0})
     * @param sensorType the sensor type string (e.g. {@code "Load"}, {@code "SmallData"})
     * @return WMI result containing NAME, VALUE, and PARENT columns
     */
    public static WmiResult<LhmSensorProperty> querySensors(String parent, String sensorType) {
        StringBuilder sb = new StringBuilder(SENSOR);
        sb.append(" WHERE Parent=\"").append(parent);
        sb.append("\" AND SensorType=\"").append(sensorType).append('"');
        WmiQuery<LhmSensorProperty> query = new WmiQuery<>(WmiKit.LHM_NAMESPACE, sb.toString(),
                LhmSensorProperty.class);
        WmiQueryHandler handler = WmiQueryHandler.createInstance();
        Objects.requireNonNull(handler, "WmiQueryHandler.createInstance() returned null for LhmSensor queries");
        return handler.queryWMI(query, true);
    }

    /**
     * Queries all GPU hardware entries from LHM to discover parent identifiers.
     *
     * @return WMI result with IDENTIFIER and NAME columns for all GPU hardware entries
     */
    public static WmiResult<LhmHardwareProperty> queryGpuHardware() {
        WmiQuery<LhmHardwareProperty> query = new WmiQuery<>(WmiKit.LHM_NAMESPACE,
                "Hardware WHERE HardwareType=\"GpuNvidia\" OR HardwareType=\"GpuAmd\" OR HardwareType=\"GpuIntel\"",
                LhmHardwareProperty.class);
        WmiQueryHandler handler = WmiQueryHandler.createInstance();
        Objects.requireNonNull(
                handler,
                "WmiQueryHandler.createInstance() returned null for LhmSensor hardware queries");
        return handler.queryWMI(query, true);
    }

}
