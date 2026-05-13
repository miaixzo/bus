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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Sensors from WMI or Open Hardware Monitor
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class LinuxSensors extends AbstractSensors {

    /**
     * Configuration property for prioritizing hwmon temperature sensors by name. Common sensor names include:
     * <ul>
     * <li>coretemp: Intel CPU temperature</li>
     * <li>k10temp: AMD CPU temperature (K10+ cores)</li>
     * <li>zenpower: AMD Zen CPU temperature</li>
     * <li>k8temp: AMD K8 CPU temperature</li>
     * <li>via-cputemp: VIA CPU temperature</li>
     * </ul>
     */
    public static final String HWMON_NAME_PRIORITY_CONFIG = Config._LINUX_HWMON_NAME_PRIORITY;

    /**
     * The THERMAL_ZONE_TYPE_PRIORITY_CONFIG constant.
     */
    public static final String THERMAL_ZONE_TYPE_PRIORITY_CONFIG = Config._LINUX_THERMAL_ZONE_TYPE_PRIORITY;

    /**
     * The HWMON_NAME_PRIORITY constant.
     */
    private static final List<String> HWMON_NAME_PRIORITY = Stream.of(
            Config.get(HWMON_NAME_PRIORITY_CONFIG, "coretemp,k10temp,zenpower,k8temp,via-cputemp,acpitz").split(","))
            .filter((s) -> !s.isEmpty()).collect(Collectors.toList());

    /**
     * The THERMAL_ZONE_TYPE_PRIORITY constant.
     */
    private static final List<String> THERMAL_ZONE_TYPE_PRIORITY = Stream
            .of(Config.get(THERMAL_ZONE_TYPE_PRIORITY_CONFIG, "cpu-thermal,x86_pkg_temp").split(","))
            .filter((s) -> !s.isEmpty()).collect(Collectors.toList());

    /**
     * The TYPE constant.
     */
    private static final String TYPE = "type";

    /**
     * The NAME constant.
     */
    private static final String NAME = "/name";
    // Possible sensor types. See sysfs documentation for others, e.g. current
    /**
     * The TEMP constant.
     */
    private static final String TEMP = "temp";

    /**
     * The FAN constant.
     */
    private static final String FAN = "fan";

    /**
     * The VOLTAGE constant.
     */
    private static final String VOLTAGE = "in";
    // Compile pattern for "temp<digits>_input"
    /**
     * The INPUT_SUFFIX constant.
     */
    private static final String INPUT_SUFFIX = "_input";

    /**
     * The TEMP_INPUT_PATTERN constant.
     */
    private static final Pattern TEMP_INPUT_PATTERN = Pattern.compile("^" + TEMP + "\\d+" + INPUT_SUFFIX + "$");

    // Base path constants
    /**
     * The HWMON constant.
     */
    private static final String HWMON = "hwmon";

    /**
     * The THERMAL_ZONE constant.
     */
    private static final String THERMAL_ZONE = "thermal_zone";

    /**
     * The hwmonPath value.
     */
    private final String hwmonPath;

    /**
     * The thermalZonePath value.
     */
    private final String thermalZonePath;

    /**
     * The isPi value.
     */
    private final boolean isPi;

    // Map from sensor to path. Built by constructor, so thread safe
    /**
     * The sensorsMap value.
     */
    private final Map<String, String> sensorsMap = new HashMap<>();

    /**
     * <p>
     * Constructor for LinuxSensors.
     * </p>
     */
    LinuxSensors() {
        this(SysPath.HWMON + HWMON, SysPath.THERMAL + THERMAL_ZONE, queryCpuTemperatureFromVcGenCmd() > 0);
    }

    /**
     * Creates a new LinuxSensors instance.
     *
     * @param hwmonBasePath       the hwmon base path
     * @param thermalZoneBasePath the thermal zone base path
     * @param isPi                the is pi
     */
    LinuxSensors(String hwmonBasePath, String thermalZoneBasePath, boolean isPi) {
        this.hwmonPath = hwmonBasePath;
        this.thermalZonePath = thermalZoneBasePath;
        this.isPi = isPi;
        if (!isPi) {
            populateSensorsMapFromHwmon();
            // if no temperature sensor is found in hwmon, try thermal_zone
            if (!this.sensorsMap.containsKey(TEMP)) {
                populateSensorsMapFromThermalZone();
            }
        }
    }

    /**
     * Retrieves temperature from Raspberry Pi
     *
     * @return The temperature on a Pi, 0 otherwise
     */
    private static double queryCpuTemperatureFromVcGenCmd() {
        String tempStr = Executor.getFirstAnswer("vcgencmd measure_temp");
        // temp=50.8'C
        if (tempStr.startsWith("temp=")) {
            return Parsing.parseDoubleOrDefault(tempStr.replaceAll("[^\\d|\\.]+", Normal.EMPTY), 0d);
        }
        return 0d;
    }

    /**
     * Retrieves voltage from Raspberry Pi
     *
     * @return The temperature on a Pi, 0 otherwise
     */
    private static double queryCpuVoltageFromVcGenCmd() {
        // For raspberry pi
        String voltageStr = Executor.getFirstAnswer("vcgencmd measure_volts core");
        // volt=1.20V
        if (voltageStr.startsWith("volt=")) {
            return Parsing.parseDoubleOrDefault(voltageStr.replaceAll("[^\\d|\\.]+", Normal.EMPTY), 0d);
        }
        return 0d;
    }

    /**
     * Find all sensor files in a specific path and adds them to the sensorsMap
     *
     * @param sensorPath       A string containing the sensor path
     * @param sensor           A string containing the sensor
     * @param sensorFileFilter A FileFilter for detecting valid sensor files
     */
    private void getSensorFilesFromPath(String sensorPath, String sensor, FileFilter sensorFileFilter) {
        getSensorFilesFromPath(sensorPath, sensor, sensorFileFilter, (files) -> 0);
    }

    /**
     * Find all sensor files in a specific path and adds them to the sensorsMap
     *
     * @param sensorPath       A string containing the sensor path
     * @param sensor           A string containing the sensor
     * @param sensorFileFilter A FileFilter for detecting valid sensor files
     * @param prioritizer      A callback to prioritize between multiple sensors
     */
    private void getSensorFilesFromPath(
            String sensorPath,
            String sensor,
            FileFilter sensorFileFilter,
            ToIntFunction<File[]> prioritizer) {
        String selectedPath = null;
        int selectedPriority = Integer.MAX_VALUE;

        int i = 0;
        while (Paths.get(sensorPath + i).toFile().isDirectory()) {
            String path = sensorPath + i;
            File dir = new File(path);
            File[] matchingFiles = dir.listFiles(sensorFileFilter);

            if (matchingFiles != null && matchingFiles.length > 0) {
                int priority = prioritizer.applyAsInt(matchingFiles);

                if (priority < selectedPriority) {
                    selectedPriority = priority;
                    selectedPath = path;
                }
            }
            i++;
        }

        if (selectedPath != null) {
            this.sensorsMap.put(sensor, String.format(Locale.ROOT, "%s/%s", selectedPath, sensor));
        }
    }

    /*
     * Iterate over all hwmon* directories and look for sensor files, e.g., /sys/class/hwmon/hwmon0/temp1_input
     */
    /**
     * Handles the populate sensors map from hwmon operation.
     */
    private void populateSensorsMapFromHwmon() {
        String selectedTempPath = null;
        int selectedPriority = Integer.MAX_VALUE;

        int i = 0;
        while (Paths.get(hwmonPath + i).toFile().isDirectory()) {
            String path = hwmonPath + i;

            // Read the name file
            String sensorName = Builder.getStringFromFile(path + NAME).trim();

            // Check if this is a temperature sensor with valid readings
            File dir = new File(path);
            File[] tempInputs = dir.listFiles((d, name) -> TEMP_INPUT_PATTERN.matcher(name).matches());

            if (tempInputs != null && tempInputs.length > 0) {
                int priority = HWMON_NAME_PRIORITY.indexOf(sensorName);
                if (priority >= 0 && priority < selectedPriority) {
                    // Check if we can read at least one valid temperature
                    for (File tempInput : tempInputs) {
                        long temp = Builder.getLongFromFile(tempInput.getPath());
                        if (temp > 0) {
                            selectedPriority = priority;
                            selectedTempPath = path;
                            break;
                        }
                    }
                }
            }

            i++;
        }

        if (selectedTempPath != null) {
            this.sensorsMap.put(TEMP, selectedTempPath + "/temp");
        }

        for (String sensor : new String[] { FAN, VOLTAGE }) {
            final String sensorPrefix = sensor;
            getSensorFilesFromPath(hwmonPath, sensor, f -> {
                try {
                    return f.getName().startsWith(sensorPrefix) && f.getName().endsWith(INPUT_SUFFIX)
                            && Builder.getIntFromFile(f.getCanonicalPath()) > 0;
                } catch (IOException e) {
                    return false;
                }
            });
        }
    }

    /*
     * Iterate over all thermal_zone* directories and look for sensor files, e.g., /sys/class/thermal/thermal_zone0/temp
     */
    /**
     * Handles the populate sensors map from thermal zone operation.
     */
    private void populateSensorsMapFromThermalZone() {
        getSensorFilesFromPath(
                thermalZonePath,
                TEMP,
                f -> f.getName().equals(TYPE) || f.getName().equals(TEMP),
                files -> Stream.of(files).filter(f -> TYPE.equals(f.getName())).findFirst().map(File::getPath)
                        .map(Builder::getStringFromFile).map(THERMAL_ZONE_TYPE_PRIORITY::indexOf)
                        .filter((index) -> index >= 0).orElse(THERMAL_ZONE_TYPE_PRIORITY.size()));
    }

    /**
     * Queries the cpu temperature.
     *
     * @return the query cpu temperature result
     */
    @Override
    public double queryCpuTemperature() {
        if (isPi) {
            return queryCpuTemperatureFromVcGenCmd();
        }
        String tempStr = this.sensorsMap.get(TEMP);
        if (tempStr != null) {
            long millidegrees = 0;
            if (tempStr.contains(HWMON)) {
                // First attempt should be CPU temperature at index 1, if available
                millidegrees = Builder.getLongFromFile(String.format(Locale.ROOT, "%s1%s", tempStr, INPUT_SUFFIX));
                // Should return a single line of millidegrees Celsius
                if (millidegrees > 0) {
                    return millidegrees / 1000d;
                }
                // If temp1_input doesn't exist, iterate over temp2..temp6_input
                // and average
                long sum = 0;
                int count = 0;
                for (int i = 2; i <= 6; i++) {
                    millidegrees = Builder
                            .getLongFromFile(String.format(Locale.ROOT, "%s%d%s", tempStr, i, INPUT_SUFFIX));
                    if (millidegrees > 0) {
                        sum += millidegrees;
                        count++;
                    }
                }
                if (count > 0) {
                    return sum / (count * 1000d);
                }
            } else if (tempStr.contains(THERMAL_ZONE)) {
                // If temp2..temp6_input doesn't exist, try thermal_zone0
                millidegrees = Builder.getLongFromFile(tempStr);
                // Should return a single line of millidegrees Celsius
                if (millidegrees > 0) {
                    return millidegrees / 1000d;
                }
            }
        }
        return 0d;
    }

    /**
     * Queries the fan speeds.
     *
     * @return the query fan speeds result
     */
    @Override
    public int[] queryFanSpeeds() {
        if (!isPi) {
            String fanStr = this.sensorsMap.get(FAN);
            if (fanStr != null) {
                List<Integer> speeds = new ArrayList<>();
                int fan = 1;
                for (;;) {
                    String fanPath = String.format(Locale.ROOT, "%s%d%s", fanStr, fan, INPUT_SUFFIX);
                    if (!new File(fanPath).exists()) {
                        // No file found, we've reached max fans
                        break;
                    }
                    // Should return a single line of RPM
                    speeds.add(Builder.getIntFromFile(fanPath));
                    // Done reading data for current fan, read next fan
                    fan++;
                }
                int[] fanSpeeds = new int[speeds.size()];
                for (int i = 0; i < speeds.size(); i++) {
                    fanSpeeds[i] = speeds.get(i);
                }
                return fanSpeeds;
            }
        }
        return new int[0];
    }

    /**
     * Queries the cpu voltage.
     *
     * @return the query cpu voltage result
     */
    @Override
    public double queryCpuVoltage() {
        if (isPi) {
            return queryCpuVoltageFromVcGenCmd();
        }
        String voltageStr = this.sensorsMap.get(VOLTAGE);
        if (voltageStr != null) {
            // Should return a single line of millivolt
            return Builder.getIntFromFile(String.format(Locale.ROOT, "%s1%s", voltageStr, INPUT_SUFFIX)) / 1000d;
        }
        return 0d;
    }

}
