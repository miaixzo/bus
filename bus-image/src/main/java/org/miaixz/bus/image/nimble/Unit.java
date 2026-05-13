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
package org.miaixz.bus.image.nimble;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Measurement units used by image calibration and measurement tools.
 * <p>
 * This is the non-UI portion of the Weasis unit model, adapted to the bus-image package structure and using plain
 * display labels instead of desktop resource bundles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Unit {

    /**
     * The pixel value.
     */
    PIXEL(-5, "Pixel", "px", 1.0, UnitSystem.DIGITAL),

    /**
     * The nanometer value.
     */
    NANOMETER(-2, "Nanometer", "nm", 1.0E-09, UnitSystem.METRIC),
    /**
     * The micrometer value.
     */
    MICROMETER(-1, "Micrometer", "um", 1.0E-06, UnitSystem.METRIC),
    /**
     * The millimeter value.
     */
    MILLIMETER(0, "Millimeter", "mm", 1.0E-03, UnitSystem.METRIC),
    /**
     * Constant for the centimeter value.
     */
    CENTIMETER(1, "Centimeter", "cm", 1.0E-02, UnitSystem.METRIC),
    /**
     * Constant for the meter value.
     */
    METER(2, "Meter", "m", 1.0, UnitSystem.METRIC),
    /**
     * The kilometer value.
     */
    KILOMETER(3, "Kilometer", "km", 1.0E+03, UnitSystem.METRIC),

    /**
     * The microinch value.
     */
    MICROINCH(9, "Microinch", "uin", 2.54E-08, UnitSystem.IMPERIAL),
    /**
     * The milliinch value.
     */
    MILLIINCH(10, "Milliinch", "mil", 2.54E-05, UnitSystem.IMPERIAL),
    /**
     * Constant for the inch value.
     */
    INCH(11, "Inch", "in", 2.54E-02, UnitSystem.IMPERIAL),
    /**
     * Constant for the feet value.
     */
    FEET(12, "Foot", "ft", 3.048E-01, UnitSystem.IMPERIAL),
    /**
     * Constant for the yard value.
     */
    YARD(13, "Yard", "yd", 9.144E-01, UnitSystem.IMPERIAL),
    /**
     * Constant for the mile value.
     */
    MILE(14, "Mile", "mi", 1.609344E+03, UnitSystem.IMPERIAL);

    /**
     * The id to unit value.
     */
    private static final Map<Integer, Unit> ID_TO_UNIT = Arrays.stream(values())
            .collect(Collectors.toMap(Unit::getId, unit -> unit));

    /**
     * The name to unit value.
     */
    private static final Map<String, Unit> NAME_TO_UNIT = Arrays.stream(values())
            .collect(Collectors.toMap(Unit::getFullName, unit -> unit));

    /**
     * The abbrev to unit value.
     */
    private static final Map<String, Unit> ABBREV_TO_UNIT = Arrays.stream(values())
            .collect(Collectors.toMap(Unit::getAbbreviation, unit -> unit));

    /**
     * The id value.
     */
    private final int id;

    /**
     * The full name value.
     */
    private final String fullName;

    /**
     * The abbreviation value.
     */
    private final String abbreviation;

    /**
     * The factor to meters value.
     */
    private final double factorToMeters;

    /**
     * The system value.
     */
    private final UnitSystem system;

    /**
     * Creates a new instance.
     *
     * @param id             the id.
     * @param fullName       the full name.
     * @param abbreviation   the abbreviation.
     * @param factorToMeters the factor to meters.
     * @param system         the system.
     */
    Unit(int id, String fullName, String abbreviation, double factorToMeters, UnitSystem system) {
        this.id = id;
        this.fullName = fullName;
        this.abbreviation = abbreviation;
        this.factorToMeters = factorToMeters;
        this.system = system;
    }

    /**
     * Gets the id.
     *
     * @return the id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the full name.
     *
     * @return the full name.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the abbreviation.
     *
     * @return the abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets the factor to meters.
     *
     * @return the factor to meters.
     */
    public double getFactorToMeters() {
        return factorToMeters;
    }

    /**
     * Gets the system.
     *
     * @return the system.
     */
    public UnitSystem getSystem() {
        return system;
    }

    /**
     * Gets the conversion ratio.
     *
     * @param calibrationRatio the calibration ratio.
     * @return the conversion ratio.
     */
    public double getConversionRatio(double calibrationRatio) {
        return calibrationRatio / factorToMeters;
    }

    /**
     * Executes the convert to operation.
     *
     * @param value      the value.
     * @param targetUnit the target unit.
     * @return the operation result.
     */
    public double convertTo(double value, Unit targetUnit) {
        if (this == targetUnit) {
            return value;
        }
        if (this == PIXEL || targetUnit == PIXEL) {
            throw new IllegalArgumentException("Cannot convert between pixel and physical units without calibration");
        }
        return toMeters(value) / targetUnit.factorToMeters;
    }

    /**
     * Converts this value to meters.
     *
     * @param value the value.
     * @return the operation result.
     */
    public double toMeters(double value) {
        return value * factorToMeters;
    }

    /**
     * Executes the from meters operation.
     *
     * @param meters the meters.
     * @return the operation result.
     */
    public double fromMeters(double meters) {
        return meters / factorToMeters;
    }

    /**
     * Gets the next larger unit.
     *
     * @return the next larger unit.
     */
    public Optional<Unit> getNextLargerUnit() {
        return findAdjacentUnit(id + 1);
    }

    /**
     * Gets the next smaller unit.
     *
     * @return the next smaller unit.
     */
    public Optional<Unit> getNextSmallerUnit() {
        return findAdjacentUnit(id - 1);
    }

    /**
     * Finds the adjacent unit.
     *
     * @param targetId the target id.
     * @return the operation result.
     */
    private Optional<Unit> findAdjacentUnit(int targetId) {
        return Arrays.stream(values()).filter(unit -> unit.system == system && unit.id == targetId).findFirst();
    }

    /**
     * Gets the units in same system.
     *
     * @return the units in same system.
     */
    public List<Unit> getUnitsInSameSystem() {
        return Arrays.stream(values()).filter(unit -> unit.system == system)
                .sorted(Comparator.comparingInt(Unit::getId)).toList();
    }

    /**
     * Gets the by id.
     *
     * @param id the id.
     * @return the by id.
     */
    public static Unit getById(int id) {
        return ID_TO_UNIT.getOrDefault(id, PIXEL);
    }

    /**
     * Gets the by name.
     *
     * @param name the name.
     * @return the by name.
     */
    public static Optional<Unit> getByName(String name) {
        return Optional.ofNullable(NAME_TO_UNIT.get(name));
    }

    /**
     * Gets the by abbreviation.
     *
     * @param abbreviation the abbreviation.
     * @return the by abbreviation.
     */
    public static Optional<Unit> getByAbbreviation(String abbreviation) {
        return Optional.ofNullable(ABBREV_TO_UNIT.get(abbreviation));
    }

    /**
     * Gets the physical units.
     *
     * @return the physical units.
     */
    public static List<Unit> getPhysicalUnits() {
        return filtered(unit -> unit != PIXEL);
    }

    /**
     * Gets the metric units.
     *
     * @return the metric units.
     */
    public static List<Unit> getMetricUnits() {
        return getUnitsBySystem(UnitSystem.METRIC);
    }

    /**
     * Gets the imperial units.
     *
     * @return the imperial units.
     */
    public static List<Unit> getImperialUnits() {
        return getUnitsBySystem(UnitSystem.IMPERIAL);
    }

    /**
     * Gets the units by system.
     *
     * @param system the system.
     * @return the units by system.
     */
    public static List<Unit> getUnitsBySystem(UnitSystem system) {
        return filtered(unit -> unit.system == system);
    }

    /**
     * Executes the filtered operation.
     *
     * @param filter the filter.
     * @return the operation result.
     */
    private static List<Unit> filtered(Predicate<Unit> filter) {
        return Arrays.stream(values()).filter(filter).sorted(Comparator.comparingInt(Unit::getId)).toList();
    }

    /**
     * Finds the best unit.
     *
     * @param valueInMeters   the value in meters.
     * @param preferredSystem the preferred system.
     * @return the operation result.
     */
    public static Unit findBestUnit(double valueInMeters, UnitSystem preferredSystem) {
        return getUnitsBySystem(preferredSystem).stream().filter(unit -> unit != PIXEL)
                .min(Comparator.comparingDouble(unit -> score(unit.fromMeters(valueInMeters)))).orElse(METER);
    }

    /**
     * Executes the score operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static double score(double value) {
        double abs = Math.abs(value);
        if (abs == 0.0) {
            return 0.0;
        }
        double log = Math.abs(Math.log10(abs));
        return abs >= 0.1 && abs <= 1000.0 ? log : log + 10.0;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return fullName;
    }

    /**
     * Defines the UnitSystem values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum UnitSystem {

        /**
         * Constant for the metric value.
         */
        METRIC("Metric System"),
        /**
         * Constant for the imperial value.
         */
        IMPERIAL("Imperial System"),
        /**
         * Constant for the digital value.
         */
        DIGITAL("Digital Units");

        /**
         * The display name value.
         */
        private final String displayName;

        /**
         * Creates a new instance.
         *
         * @param displayName the display name.
         */
        UnitSystem(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Gets the display name.
         *
         * @return the display name.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return displayName;
        }

    }

}
