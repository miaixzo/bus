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
package org.miaixz.bus.health.unix.platform.bsd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PowerSource;

/**
 * Parses output from the BSD {@code systat -ab sensors} command.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Systat {

    /**
     * The command used to query BSD sensor information.
     */
    private static final String SYSTAT_AB_SENSORS = "systat -ab sensors";

    /**
     * Creates a new Systat instance.
     */
    private Systat() {
        // No initialization required.
    }

    /**
     * Runs {@code systat -ab sensors} and returns its raw output.
     *
     * @return the raw sensor lines
     */
    public static List<String> querySensorLines() {
        return Executor.runNative(SYSTAT_AB_SENSORS);
    }

    /**
     * Runs {@code systat -ab sensors} and returns CPU temperature, fan speeds, and CPU voltage.
     *
     * @return the parsed sensor values
     */
    public static Triplet<Double, int[], Double> querySensors() {
        return parseSensors(querySensorLines());
    }

    /**
     * Parses sensor output into CPU temperature, fan speeds, and CPU voltage.
     *
     * @param systatLines the raw sensor lines
     * @return the parsed sensor values
     */
    public static Triplet<Double, int[], Double> parseSensors(List<String> systatLines) {
        double volts = 0d;
        List<Double> cpuTemps = new ArrayList<>();
        List<Double> allTemps = new ArrayList<>();
        List<Integer> fanRPMs = new ArrayList<>();
        for (String line : systatLines) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 1) {
                if (split[0].contains("cpu")) {
                    if (split[0].contains("temp0")) {
                        cpuTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                    } else if (split[0].contains("volt0")) {
                        volts = Parsing.parseDoubleOrDefault(split[1], 0d);
                    }
                } else if (split[0].contains("temp0")) {
                    allTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                } else if (split[0].contains("fan")) {
                    fanRPMs.add(Parsing.parseIntOrDefault(split[1], 0));
                }
            }
        }
        double temp = cpuTemps.isEmpty() ? listAverage(allTemps) : listAverage(cpuTemps);
        int[] fans = new int[fanRPMs.size()];
        for (int i = 0; i < fans.length; i++) {
            fans[i] = fanRPMs.get(i);
        }
        return Triplet.of(temp, fans, volts);
    }

    /**
     * Runs {@code systat -ab sensors} and returns distinct power-source names.
     *
     * @return the power-source names
     */
    public static Set<String> queryPowerSourceNames() {
        return parsePowerSourceNames(querySensorLines());
    }

    /**
     * Parses sensor output and returns distinct power-source names.
     *
     * @param systatLines the raw sensor lines
     * @return the power-source names
     */
    public static Set<String> parsePowerSourceNames(List<String> systatLines) {
        Set<String> psNames = new HashSet<>();
        for (String line : systatLines) {
            if (line.contains(".amphour") || line.contains(".watthour")) {
                int dot = line.indexOf('.');
                psNames.add(line.substring(0, dot));
            }
        }
        return psNames;
    }

    /**
     * Runs {@code systat -ab sensors} and returns battery fields for the named power source.
     *
     * @param name the power-source name
     * @return the parsed battery fields
     */
    public static BatteryFields queryBatteryFields(String name) {
        return parseBatteryFields(name, querySensorLines());
    }

    /**
     * Parses battery fields for the named power source.
     *
     * @param name        the power-source name
     * @param systatLines the raw sensor lines
     * @return the parsed battery fields
     */
    public static BatteryFields parseBatteryFields(String name, List<String> systatLines) {
        double voltage = -1d;
        double amperage = 0d;
        double temperature = 0d;
        PowerSource.CapacityUnits capacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int currentCapacity = 0;
        int maxCapacity = 1;
        int designCapacity = 1;
        for (String line : systatLines) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 1 && split[0].startsWith(name)) {
                if (split[0].contains("volt0") || (split[0].contains("volt") && line.contains("current"))) {
                    voltage = Parsing.parseDoubleOrDefault(split[1], -1d);
                } else if (split[0].contains("current0")) {
                    amperage = Parsing.parseDoubleOrDefault(split[1], 0d);
                } else if (split[0].contains("temp0")) {
                    temperature = Parsing.parseDoubleOrDefault(split[1], 0d);
                } else if (split[0].contains("watthour") || split[0].contains("amphour")) {
                    capacityUnits = split[0].contains("watthour") ? PowerSource.CapacityUnits.MWH
                            : PowerSource.CapacityUnits.MAH;
                    if (line.contains("remaining")) {
                        currentCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    } else if (line.contains("full")) {
                        maxCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    } else if (line.contains("new") || line.contains("design")) {
                        designCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    }
                }
            }
        }
        return new BatteryFields(voltage, amperage, temperature, capacityUnits, currentCapacity, maxCapacity,
                designCapacity);
    }

    /**
     * Returns the average of the non-NaN values.
     *
     * @param values the values to average
     * @return the average value
     */
    private static double listAverage(List<Double> values) {
        double sum = 0d;
        int count = 0;
        for (Double value : values) {
            if (!value.isNaN()) {
                sum += value;
                count++;
            }
        }
        return count > 0 ? sum / count : 0d;
    }

    /**
     * Holds battery sensor fields parsed from {@code systat -ab sensors}.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @ThreadSafe
    public static final class BatteryFields {

        /**
         * The voltage value.
         */
        private final double voltage;

        /**
         * The amperage value.
         */
        private final double amperage;

        /**
         * The temperature value.
         */
        private final double temperature;

        /**
         * The capacity units value.
         */
        private final PowerSource.CapacityUnits capacityUnits;

        /**
         * The current capacity value.
         */
        private final int currentCapacity;

        /**
         * The maximum capacity value.
         */
        private final int maxCapacity;

        /**
         * The design capacity value.
         */
        private final int designCapacity;

        /**
         * Creates a new BatteryFields instance.
         *
         * @param voltage         the voltage
         * @param amperage        the amperage
         * @param temperature     the temperature
         * @param capacityUnits   the capacity units
         * @param currentCapacity the current capacity
         * @param maxCapacity     the maximum capacity
         * @param designCapacity  the design capacity
         */
        private BatteryFields(double voltage, double amperage, double temperature,
                PowerSource.CapacityUnits capacityUnits, int currentCapacity, int maxCapacity, int designCapacity) {
            this.voltage = voltage;
            this.amperage = amperage;
            this.temperature = temperature;
            this.capacityUnits = capacityUnits;
            this.currentCapacity = currentCapacity;
            this.maxCapacity = maxCapacity;
            this.designCapacity = designCapacity;
        }

        /**
         * Returns the voltage.
         *
         * @return the voltage
         */
        public double getVoltage() {
            return voltage;
        }

        /**
         * Returns the amperage.
         *
         * @return the amperage
         */
        public double getAmperage() {
            return amperage;
        }

        /**
         * Returns the temperature.
         *
         * @return the temperature
         */
        public double getTemperature() {
            return temperature;
        }

        /**
         * Returns the capacity units.
         *
         * @return the capacity units
         */
        public PowerSource.CapacityUnits getCapacityUnits() {
            return capacityUnits;
        }

        /**
         * Returns the current capacity.
         *
         * @return the current capacity
         */
        public int getCurrentCapacity() {
            return currentCapacity;
        }

        /**
         * Returns the maximum capacity.
         *
         * @return the maximum capacity
         */
        public int getMaxCapacity() {
            return maxCapacity;
        }

        /**
         * Returns the design capacity.
         *
         * @return the design capacity
         */
        public int getDesignCapacity() {
            return designCapacity;
        }

    }

}
