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
package org.miaixz.bus.core.center.date.culture.solar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.parts.YearParts;

/**
 * Represents a half-year in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SolarHalfYear extends YearParts {

    /**
     * Chinese names for half-years.
     */
    public static final String[] NAMES = { "上半年", "下半年" };

    /**
     * The index of the half-year (0 for first half, 1 for second half).
     */
    protected int index;

    /**
     * Constructs a SolarHalfYear instance.
     *
     * @param year  the year (1-9999)
     * @param index the half-year index (0 or 1)
     * @throws IllegalArgumentException if the index is not 0 or 1
     */
    public SolarHalfYear(int year, int index) {
        validate(year, index);
        this.year = year;
        this.index = index;
    }

    /**
     * Creates a SolarHalfYear from year and index.
     *
     * @param year  the year (1-9999)
     * @param index the half-year index (0 or 1)
     * @return a new SolarHalfYear instance
     */
    public static SolarHalfYear fromIndex(int year, int index) {
        return new SolarHalfYear(year, index);
    }

    /**
     * Validates the year and index.
     *
     * @param year  the year
     * @param index the half-year index
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(int year, int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException(String.format("illegal solar half year index: %d", index));
        }
        SolarYear.validate(year);
    }

    /**
     * Gets the solar year containing this half-year.
     *
     * @return the SolarYear
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(year);
    }

    /**
     * Gets the index of this half-year (0 or 1).
     *
     * @return the half-year index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the Chinese name of this half-year.
     *
     * @return the Chinese name
     */
    public String getName() {
        return NAMES[index];
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getSolarYear() + getName();
    }

    /**
     * Gets the next or previous half-year.
     *
     * @param n the number of half-years to move (positive for forward, negative for backward)
     * @return the SolarHalfYear n half-years from this one
     */
    public SolarHalfYear next(int n) {
        int i = index + n;
        return fromIndex((year * 2 + i) / 2, indexOf(i, 2));
    }

    /**
     * Gets the list of months in this half-year. Each half-year contains 6 months.
     *
     * @return a list of 6 SolarMonth objects
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(6);
        for (int i = 1; i < 7; i++) {
            l.add(SolarMonth.fromYm(year, index * 6 + i));
        }
        return l;
    }

    /**
     * Gets the list of quarters in this half-year. Each half-year contains 2 quarters.
     *
     * @return a list of 2 SolarQuarter objects
     */
    public List<SolarQuarter> getQuarters() {
        List<SolarQuarter> l = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            l.add(SolarQuarter.fromIndex(year, index * 2 + i));
        }
        return l;
    }

}
