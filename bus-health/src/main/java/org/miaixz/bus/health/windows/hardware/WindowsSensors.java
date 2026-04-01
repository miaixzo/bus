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
package org.miaixz.bus.health.windows.hardware;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;
import org.miaixz.bus.health.windows.driver.wmi.*;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Sensors from WMI or Open Hardware Monitor
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class WindowsSensors extends AbstractSensors {

    private static final String COM_EXCEPTION_MSG = "COM exception: {}";

    private static final String REFLECT_EXCEPTION_MSG = "Reflect exception: {}";

    private static final String JLIBREHARDWAREMONITOR_PACKAGE = "io.github.pandalxb.jlibrehardwaremonitor";

    @Override
    public double queryCpuTemperature() {
        // Attempt to fetch value from Open Hardware Monitor if it is running,
        // as it will give the most accurate results and the time to query (or
        // attempt) is trivial
        double tempC = getTempFromOHM();
        if (tempC > 0d) {
            return tempC;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        tempC = getTempFromLHM();
        if (tempC > 0d) {
            return tempC;
        }

        // If we get this far, OHM is not running. Try from WMI
        tempC = getTempFromWMI();

        // Other fallbacks to WMI are unreliable so we omit them
        // Win32_TemperatureProbe is the official location but is not currently
        // populated and is "reserved for future use"
        return tempC;
    }

    private static double getTempFromOHM() {
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getOhmSensors(
                "Hardware",
                "CPU",
                "Temperature",
                (h, ohmHardware) -> {
                    String cpuIdentifier = WmiKit.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
                    if (!cpuIdentifier.isEmpty()) {
                        return OhmSensor.querySensorValue(h, cpuIdentifier, "Temperature");
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

    private static double getTempFromLHM() {
        return getAverageValueFromLHM(
                "CPU",
                "Temperature",
                (name, value) -> !name.contains("Max") && !name.contains("Average") && value > 0);
    }

    private static double getTempFromWMI() {
        double tempC = 0d;
        long tempK = 0L;
        WmiResult<MSAcpiThermalZoneTemperature.TemperatureProperty> result = MSAcpiThermalZoneTemperature
                .queryCurrentTemperature();
        if (result.getResultCount() > 0) {
            Logger.debug("Found Temperature data in WMI");
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

    @Override
    public int[] queryFanSpeeds() {
        // Attempt to fetch value from Open Hardware Monitor if it is running
        int[] fanSpeeds = getFansFromOHM();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        fanSpeeds = getFansFromLHM();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // If we get this far, OHM is not running.
        // Try to get from conventional WMI
        fanSpeeds = getFansFromWMI();
        if (fanSpeeds.length > 0) {
            return fanSpeeds;
        }

        // Default
        return new int[0];
    }

    private static int[] getFansFromOHM() {
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getOhmSensors("Hardware", "CPU", "Fan", (h, ohmHardware) -> {
            String cpuIdentifier = WmiKit.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
            if (!cpuIdentifier.isEmpty()) {
                return OhmSensor.querySensorValue(h, cpuIdentifier, "Fan");
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
        return new int[0];
    }

    private static int[] getFansFromLHM() {
        List<?> sensors = getLhmSensors("SuperIO", "Fan");
        if (sensors == null || sensors.isEmpty()) {
            return new int[0];
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
                    Logger.warn(REFLECT_EXCEPTION_MSG, e.getMessage());
                    return false;
                }
            }).mapToInt(sensor -> {
                try {
                    return (int) (double) getValueMethod.invoke(sensor);
                } catch (Exception e) {
                    Logger.warn(REFLECT_EXCEPTION_MSG, e.getMessage());
                    return 0;
                }
            }).toArray();
        } catch (Exception e) {
            Logger.warn(REFLECT_EXCEPTION_MSG, e.getMessage());
        }
        return new int[0];
    }

    private static int[] getFansFromWMI() {
        WmiResult<Win32Fan.SpeedProperty> fan = Win32Fan.querySpeed();
        if (fan.getResultCount() > 1) {
            Logger.debug("Found Fan data in WMI");
            int[] fanSpeeds = new int[fan.getResultCount()];
            for (int i = 0; i < fan.getResultCount(); i++) {
                fanSpeeds[i] = (int) WmiKit.getUint64(fan, Win32Fan.SpeedProperty.DESIREDSPEED, i);
            }
            return fanSpeeds;
        }
        return new int[0];
    }

    @Override
    public double queryCpuVoltage() {
        // Attempt to fetch value from Open Hardware Monitor if it is running
        double volts = getVoltsFromOHM();
        if (volts > 0d) {
            return volts;
        }

        // Fetch value from library LibreHardwareMonitorLib.dll(.NET 4.7.2 and above) or OpenHardwareMonitorLib.dll(.NET
        // 2.0)
        // without applications running
        volts = getVoltsFromLHM();
        if (volts > 0d) {
            return volts;
        }

        // If we get this far, OHM is not running.
        // Try to get from conventional WMI
        volts = getVoltsFromWMI();

        return volts;
    }

    private static double getVoltsFromOHM() {
        WmiResult<OhmSensor.ValueProperty> ohmSensors = getOhmSensors(
                "Sensor",
                "Voltage",
                "Voltage",
                (h, ohmHardware) -> {
                    // Look for identifier containing "cpu"
                    String cpuIdentifier = null;
                    for (int i = 0; i < ohmHardware.getResultCount(); i++) {
                        String id = WmiKit.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, i);
                        if (id.toLowerCase(Locale.ROOT).contains("cpu")) {
                            cpuIdentifier = id;
                            break;
                        }
                    }
                    // If none found, just get the first one
                    if (cpuIdentifier == null) {
                        cpuIdentifier = WmiKit.getString(ohmHardware, OhmHardware.IdentifierProperty.IDENTIFIER, 0);
                    }
                    // Now fetch sensor
                    return OhmSensor.querySensorValue(h, cpuIdentifier, "Voltage");
                });
        if (ohmSensors != null && ohmSensors.getResultCount() > 0) {
            return WmiKit.getFloat(ohmSensors, OhmSensor.ValueProperty.VALUE, 0);
        }
        return 0d;
    }

    private static double getVoltsFromLHM() {
        return getAverageValueFromLHM(
                "SuperIO",
                "Voltage",
                (name, value) -> name.toLowerCase(Locale.ROOT).contains("vcore") && value > 0);
    }

    private static double getVoltsFromWMI() {
        WmiResult<Win32Processor.VoltProperty> voltage = Win32Processor.queryVoltage();
        if (voltage.getResultCount() > 1) {
            Logger.debug("Found Voltage data in WMI");
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

    private static WmiResult<OhmSensor.ValueProperty> getOhmSensors(
            String typeToQuery,
            String typeName,
            String sensorType,
            BiFunction<WmiQueryHandler, WmiResult<OhmHardware.IdentifierProperty>, WmiResult<OhmSensor.ValueProperty>> querySensorFunction) {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        WmiResult<OhmSensor.ValueProperty> ohmSensors = null;
        try {
            comInit = h.initCOM();
            WmiResult<OhmHardware.IdentifierProperty> ohmHardware = OhmHardware
                    .queryHwIdentifier(h, typeToQuery, typeName);
            if (ohmHardware.getResultCount() > 0) {
                Logger.debug("Found {} data in Open Hardware Monitor", sensorType);
                ohmSensors = querySensorFunction.apply(h, ohmHardware);
            }
        } catch (COMException e) {
            Logger.warn(COM_EXCEPTION_MSG, e.getMessage());
        } finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
        return ohmSensors;
    }

    private static double getAverageValueFromLHM(
            String hardwareType,
            String sensorType,
            BiFunction<String, Double, Boolean> sensorValidFunction) {
        List<?> sensors = getLhmSensors(hardwareType, sensorType);
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
                if (sensorValidFunction.apply(name, value)) {
                    sum += value;
                    validCount++;
                }
            }
            return validCount > 0 ? sum / validCount : 0;
        } catch (Exception e) {
            Logger.warn(REFLECT_EXCEPTION_MSG, e.getMessage());
        }
        return 0;
    }

    private static List<?> getLhmSensors(String hardwareType, String sensorType) {
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
        } catch (ClassNotFoundException e) {
            Logger.trace("jLibreHardwareMonitor not available: {}", e.getMessage());
        } catch (Exception e) {
            Logger.warn(REFLECT_EXCEPTION_MSG, e.getMessage());
        }
        return Collections.emptyList();
    }

}
