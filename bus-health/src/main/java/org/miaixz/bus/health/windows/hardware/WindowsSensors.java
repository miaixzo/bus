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
package org.miaixz.bus.health.windows.hardware;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import org.miaixz.bus.core.center.function.BiFunctionX;
import org.miaixz.bus.core.center.function.BiPredicateX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;
import org.miaixz.bus.health.windows.driver.wmi.*;
import org.miaixz.bus.logger.Logger;

/**
 * Windows sensors queried from hardware-monitor WMI namespaces, the optional {@code jLibreHardwareMonitor} library, or
 * WMI.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class WindowsSensors extends AbstractSensors {

    /**
     * The COM_EXCEPTION_MSG constant.
     */
    private static final String COM_EXCEPTION_MSG = "COM exception: {}";

    /**
     * The REFLECT_EXCEPTION_MSG constant.
     */
    private static final String REFLECT_EXCEPTION_MSG = "Reflect exception: {}";

    /**
     * The JLIBREHARDWAREMONITOR_PACKAGE constant.
     */
    private static final String JLIBREHARDWAREMONITOR_PACKAGE = "io.github.pandalxb.jlibrehardwaremonitor";

    /**
     * Open Hardware Monitor's hardware type for a processor.
     */
    private static final String OHM_CPU = "CPU";

    /**
     * Libre Hardware Monitor's hardware type for a processor.
     */
    private static final String LHM_CPU = "Cpu";

    /**
     * The temperature sensor type.
     */
    private static final String TEMPERATURE = "Temperature";

    /**
     * The fan sensor type.
     */
    private static final String FAN = "Fan";

    /**
     * The voltage sensor type.
     */
    private static final String VOLTAGE = "Voltage";

    /**
     * Whether the optional jLibreHardwareMonitor dependency is available.
     */
    private static final boolean LHM_JAR_PRESENT = isLhmJarPresent();

    /**
     * Tests whether the optional jLibreHardwareMonitor dependency is available without initializing it.
     *
     * @return {@code true} when the dependency is available
     */
    private static boolean isLhmJarPresent() {
        try {
            Class.forName(
                    JLIBREHARDWAREMONITOR_PACKAGE + ".config.ComputerConfig",
                    false,
                    WindowsSensors.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            Logger.debug(
                    false,
                    "Health",
                    "jLibreHardwareMonitor is not on the class path. Sensor data will come from other sources.");
            return false;
        }
    }

    /**
     * Returns the temperature from a hardware monitor's WMI namespace.
     *
     * @param namespace   the hardware-monitor WMI namespace
     * @param cpuTypeName the monitor-specific CPU hardware type
     * @return the CPU temperature, or zero if unavailable
     */
    private static double getTempFromMonitorWmi(String namespace, String cpuTypeName) {
        if (HardwareMonitorDisabled.isWmiDisabled(namespace)) {
            return 0;
        }
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getHardwareMonitorSensors(
                namespace,
                OhmHardware.HARDWARE,
                cpuTypeName,
                TEMPERATURE,
                (h, ohmHardware) -> {
                    String cpuIdentifier = selectOhmCpuIdentifier(ohmHardware);
                    if (!cpuIdentifier.isEmpty()) {
                        return OhmSensor.querySensorValue(h, namespace, cpuIdentifier, TEMPERATURE);
                    }
                    return null;
                });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            double sum = 0;
            for (int i = 0; i < ohmSensors.getResultCount(); i++) {
                sum += WmiKit.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, i);
            }
            return sum / ohmSensors.getResultCount();
        }
        return 0;
    }

    /**
     * Returns the temperature from the optional jLibreHardwareMonitor dependency.
     *
     * @return the CPU temperature, or zero if unavailable
     */
    private static double getTempFromLhmJar() {
        return getAverageValueFromLhmJar(
                "CPU",
                TEMPERATURE,
                (name, value) -> !name.contains("Max") && !name.contains("Average") && value > 0);
    }

    /**
     * Returns the temp from wmi.
     *
     * @return the get temp from wmi result
     */
    private static double getTempFromWMI() {
        double tempC = 0d;
        long tempK = 0L;
        WmiResult<MSAcpiThermalZoneTemperature.TemperatureProperty> result = MSAcpiThermalZoneTemperature
                .queryCurrentTemperature();
        if (result.getResultCount() > 0) {
            Logger.debug(false, "Health", "Found Temperature data in WMI");
            tempK = WmiKit
                    .getUint32asLong(result, MSAcpiThermalZoneTemperature.TemperatureProperty.CURRENTTEMPERATURE, 0);
        }
        if (tempK > 2732L) {
            tempC = tempK / 10d - 273.15;
        } else if (tempK > 274L) {
            tempC = tempK - 273d;
        }
        return Math.max(tempC, +0.0);
    }

    /**
     * Returns fan speeds from a hardware monitor's WMI namespace.
     *
     * @param namespace   the hardware-monitor WMI namespace
     * @param cpuTypeName the monitor-specific CPU hardware type
     * @return fan speeds, or an empty array if unavailable
     */
    private static int[] getFansFromMonitorWmi(String namespace, String cpuTypeName) {
        if (HardwareMonitorDisabled.isWmiDisabled(namespace)) {
            return Normal.EMPTY_INT_ARRAY;
        }
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getHardwareMonitorSensors(
                namespace,
                OhmHardware.HARDWARE,
                cpuTypeName,
                FAN,
                (h, ohmHardware) -> {
                    String cpuIdentifier = selectOhmCpuIdentifier(ohmHardware);
                    if (!cpuIdentifier.isEmpty()) {
                        return OhmSensor.querySensorValue(h, namespace, cpuIdentifier, FAN);
                    }
                    return null;
                });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            int[] fanSpeeds = new int[ohmSensors.getResultCount()];
            for (int i = 0; i < ohmSensors.getResultCount(); i++) {
                fanSpeeds[i] = (int) WmiKit.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, i);
            }
            return fanSpeeds;
        }
        return Normal.EMPTY_INT_ARRAY;
    }

    /**
     * Returns fan speeds from the optional jLibreHardwareMonitor dependency.
     *
     * @return fan speeds, or an empty array if unavailable
     */
    private static int[] getFansFromLhmJar() {
        List<?> sensors = queryLhmJarSensors("SuperIO", FAN);
        if (sensors == null || sensors.isEmpty()) {
            return Normal.EMPTY_INT_ARRAY;
        }

        try {
            // The sensor object is confirmed to contain the getValue method.
            Class<?> sensorClass = Class.forName(JLIBREHARDWAREMONITOR_PACKAGE + ".model.Sensor");
            Method getValueMethod = sensorClass.getMethod("getValue");

            return sensors.stream().filter(sensor -> {
                try {
                    double value = (double) getValueMethod.invoke(sensor);
                    return value > 0;
                } catch (Exception e) {
                    Logger.warn(false, "Health", REFLECT_EXCEPTION_MSG, e.getClass().getSimpleName());
                    return false;
                }
            }).mapToInt(sensor -> {
                try {
                    return (int) (double) getValueMethod.invoke(sensor);
                } catch (Exception e) {
                    Logger.warn(false, "Health", REFLECT_EXCEPTION_MSG, e.getClass().getSimpleName());
                    return 0;
                }
            }).toArray();
        } catch (Exception e) {
            Logger.warn(false, "Health", REFLECT_EXCEPTION_MSG, e.getClass().getSimpleName());
        }
        return Normal.EMPTY_INT_ARRAY;
    }

    /**
     * Returns the fans from wmi.
     *
     * @return the get fans from wmi result
     */
    private static int[] getFansFromWMI() {
        WmiResult<Win32Fan.SpeedProperty> fan = Win32Fan.querySpeed();
        if (fan.getResultCount() > 0) {
            Logger.debug(false, "Health", "Found Fan data in WMI");
            int[] fanSpeeds = new int[fan.getResultCount()];
            for (int i = 0; i < fan.getResultCount(); i++) {
                fanSpeeds[i] = (int) WmiKit.getUint64(fan, Win32Fan.SpeedProperty.DESIREDSPEED, i);
            }
            return fanSpeeds;
        }
        return Normal.EMPTY_INT_ARRAY;
    }

    /**
     * Returns CPU voltage from a hardware monitor's WMI namespace.
     *
     * @param namespace   the hardware-monitor WMI namespace
     * @param cpuTypeName the monitor-specific CPU hardware type
     * @return CPU voltage, or zero if unavailable
     */
    private static double getVoltsFromMonitorWmi(String namespace, String cpuTypeName) {
        if (HardwareMonitorDisabled.isWmiDisabled(namespace)) {
            return 0d;
        }
        // Find the processor hardware first, then query its voltage sensors. The Hardware table has no SensorType
        // property, so querying it directly for SensorType="Voltage" can never match.
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getHardwareMonitorSensors(
                namespace,
                OhmHardware.HARDWARE,
                cpuTypeName,
                VOLTAGE,
                (h, ohmHardware) -> {
                    String cpuIdentifier = selectOhmCpuIdentifier(ohmHardware);
                    if (!cpuIdentifier.isEmpty()) {
                        return OhmSensor.querySensorValue(h, namespace, cpuIdentifier, VOLTAGE);
                    }
                    return null;
                });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            return WmiKit.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, 0);
        }
        return 0d;
    }

    /**
     * Returns CPU voltage from the optional jLibreHardwareMonitor dependency.
     *
     * @return CPU voltage, or zero if unavailable
     */
    private static double getVoltsFromLhmJar() {
        return getAverageValueFromLhmJar(
                "SuperIO",
                VOLTAGE,
                (name, value) -> name.toLowerCase(Locale.ROOT).contains("vcore") && value > 0);
    }

    /**
     * Returns the volts from wmi.
     *
     * @return the get volts from wmi result
     */
    private static double getVoltsFromWMI() {
        WmiResult<Win32Processor.VoltProperty> voltage = Win32Processor.queryVoltage();
        if (voltage.getResultCount() > 0) {
            Logger.debug(false, "Health", "Found Voltage data in WMI");
            int decivolts = WmiKit.getUint16(voltage, Win32Processor.VoltProperty.CURRENTVOLTAGE, 0);
            // If the eighth bit is set, bits 0-6 contain the voltage
            // multiplied by 10. If the eighth bit is not set, then the bit
            // setting in VoltageCaps represents the voltage value.
            if (decivolts > 0) {
                if ((decivolts & 0x80) == 0) {
                    decivolts = WmiKit.getUint32(voltage, Win32Processor.VoltProperty.VOLTAGECAPS, 0);
                    // This value is really a bit setting, not decivolts
                    if ((decivolts & 0x1) > 0) {
                        return 5.0;
                    } else if ((decivolts & 0x2) > 0) {
                        return 3.3;
                    } else if ((decivolts & 0x4) > 0) {
                        return 2.9;
                    }
                } else {
                    // Value from bits 0-6, divided by 10
                    return (decivolts & 0x7F) / 10d;
                }
            }
        }
        return 0d;
    }

    /**
     * Selects the first identifier from an already filtered hardware-monitor result.
     *
     * @param ohmHardware the hardware identifier result
     * @return the selected identifier, or an empty string if no identifier is available
     */
    private static String selectOhmCpuIdentifier(WmiResult<OhmHardware.IdentifierProperty> ohmHardware) {
        return WmiKit.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
    }

    /**
     * Returns sensors from a hardware monitor's WMI namespace.
     *
     * @param namespace           the hardware-monitor WMI namespace
     * @param typeToQuery         the type to query
     * @param typeName            the type name
     * @param sensorType          the sensor type
     * @param querySensorFunction the query sensor function
     * @return the hardware-monitor sensor result, or {@code null} if unavailable
     */
    private static WmiResult<OhmSensor.ValueProperty> getHardwareMonitorSensors(
            String namespace,
            String typeToQuery,
            String typeName,
            String sensorType,
            BiFunctionX<WmiQueryHandler, WmiResult<OhmHardware.IdentifierProperty>, WmiResult<OhmSensor.ValueProperty>> querySensorFunction) {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        WmiResult<OhmSensor.ValueProperty> ohmSensors = null;
        try {
            comInit = h.initCOM();
            WmiResult<OhmHardware.IdentifierProperty> ohmHardware = OhmHardware
                    .queryHwIdentifier(h, namespace, typeToQuery, typeName);
            if (ohmHardware.getResultCount() > 0) {
                if (OhmHardware.OHM_NAMESPACE.equals(namespace)) {
                    Logger.debug(false, "Health", "Found {} data in Open Hardware Monitor", sensorType);
                } else {
                    Logger.debug(false, "Health", "Found {} data in Libre Hardware Monitor", sensorType);
                }
                ohmSensors = querySensorFunction.apply(h, ohmHardware);
            }
        } catch (COMException e) {
            Logger.warn(false, "Health", COM_EXCEPTION_MSG, e.getClass().getSimpleName());
        } finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
        return ohmSensors;
    }

    /**
     * Returns the average value from the optional jLibreHardwareMonitor dependency.
     *
     * @param hardwareType        the hardware type
     * @param sensorType          the sensor type
     * @param sensorValidFunction the sensor valid function
     * @return the get average value from lhm result
     */
    private static double getAverageValueFromLhmJar(
            String hardwareType,
            String sensorType,
            BiPredicateX<String, Double> sensorValidFunction) {
        List<?> sensors = queryLhmJarSensors(hardwareType, sensorType);
        if (sensors == null || sensors.isEmpty()) {
            return 0;
        }

        try {
            // The sensor object is confirmed to contain the getName and getValue methods.
            Class<?> sensorClass = Class.forName(JLIBREHARDWAREMONITOR_PACKAGE + ".model.Sensor");
            Method getNameMethod = sensorClass.getMethod("getName");
            Method getValueMethod = sensorClass.getMethod("getValue");

            double sum = 0;
            int validCount = 0;
            for (Object sensor : sensors) {
                String name = (String) getNameMethod.invoke(sensor);
                double value = (double) getValueMethod.invoke(sensor);
                if (sensorValidFunction.test(name, value)) {
                    sum += value;
                    validCount++;
                }
            }
            return validCount > 0 ? sum / validCount : 0;
        } catch (Exception e) {
            Logger.warn(false, "Health", REFLECT_EXCEPTION_MSG, e.getClass().getSimpleName());
        }
        return 0;
    }

    /**
     * Returns sensors from the optional jLibreHardwareMonitor dependency.
     *
     * @param hardwareType the hardware type
     * @param sensorType   the sensor type
     * @return the get lhm sensors result
     */
    private static List<?> queryLhmJarSensors(String hardwareType, String sensorType) {
        if (!LHM_JAR_PRESENT) {
            return Collections.emptyList();
        }
        try {
            Class<?> computerConfigClass = Class.forName(JLIBREHARDWAREMONITOR_PACKAGE + ".config.ComputerConfig");
            Class<?> libreHardwareManagerClass = Class
                    .forName(JLIBREHARDWAREMONITOR_PACKAGE + ".manager.LibreHardwareManager");

            Method computerConfigGetInstanceMethod = computerConfigClass.getMethod("getInstance");
            Object computerConfigInstance = computerConfigGetInstanceMethod.invoke(null);

            Method setEnabledMethod = computerConfigClass.getMethod("setCpuEnabled", boolean.class);
            setEnabledMethod.invoke(computerConfigInstance, true);
            setEnabledMethod = computerConfigClass.getMethod("setMotherboardEnabled", boolean.class);
            setEnabledMethod.invoke(computerConfigInstance, true);

            Method libreHardwareManagerGetInstanceMethod = libreHardwareManagerClass
                    .getMethod("getInstance", computerConfigClass);

            Object instance = libreHardwareManagerGetInstanceMethod.invoke(null, computerConfigInstance);

            Method querySensorsMethod = libreHardwareManagerClass.getMethod("querySensors", String.class, String.class);
            return (List<?>) querySensorsMethod.invoke(instance, hardwareType, sensorType);
        } catch (Exception e) {
            Logger.warn(false, "Health", REFLECT_EXCEPTION_MSG, e.getClass().getSimpleName());
        }
        return Collections.emptyList();
    }

    /**
     * Queries the CPU temperature.
     *
     * @return the query cpu temperature result
     */
    @Override
    public double queryCpuTemperature() {
        // Attempt to fetch value from Open Hardware Monitor if it is running,
        // as it will give the most accurate results and the time to query (or
        // attempt) is trivial
        double tempC = getTempFromMonitorWmi(OhmHardware.OHM_NAMESPACE, OHM_CPU);
        if (tempC > 0d) {
            return tempC;
        }

        // Then Libre Hardware Monitor, the maintained successor, which publishes the same schema
        tempC = getTempFromMonitorWmi(LhmSensor.LHM_NAMESPACE, LHM_CPU);
        if (tempC > 0d) {
            return tempC;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        tempC = getTempFromLhmJar();
        if (tempC > 0d) {
            return tempC;
        }

        // If no hardware monitor supplied a value, try conventional WMI
        tempC = getTempFromWMI();

        // Other fallbacks to WMI are unreliable so we omit them
        // Win32_TemperatureProbe is the official location but is not currently
        // populated and is "reserved for future use"
        return tempC;
    }

    /**
     * Queries the fan speeds.
     *
     * @return the query fan speeds result
     */
    @Override
    public int[] queryFanSpeeds() {
        // Attempt to fetch value from Open Hardware Monitor if it is running
        int[] fanSpeeds = getFansFromMonitorWmi(OhmHardware.OHM_NAMESPACE, OHM_CPU);
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // Then Libre Hardware Monitor, the maintained successor, which publishes the same schema
        fanSpeeds = getFansFromMonitorWmi(LhmSensor.LHM_NAMESPACE, LHM_CPU);
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        fanSpeeds = getFansFromLhmJar();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // If no hardware monitor supplied a value, try conventional WMI
        fanSpeeds = getFansFromWMI();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // Default
        return Normal.EMPTY_INT_ARRAY;
    }

    /**
     * Queries the CPU voltage.
     *
     * @return the query cpu voltage result
     */
    @Override
    public double queryCpuVoltage() {
        // Attempt to fetch value from Open Hardware Monitor if it is running
        double volts = getVoltsFromMonitorWmi(OhmHardware.OHM_NAMESPACE, OHM_CPU);
        if (volts > 0d) {
            return volts;
        }

        // Then Libre Hardware Monitor, the maintained successor, which publishes the same schema
        volts = getVoltsFromMonitorWmi(LhmSensor.LHM_NAMESPACE, LHM_CPU);
        if (volts > 0d) {
            return volts;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        volts = getVoltsFromLhmJar();
        if (volts > 0d) {
            return volts;
        }

        // If no hardware monitor supplied a value, try conventional WMI
        volts = getVoltsFromWMI();

        return volts;
    }

}
