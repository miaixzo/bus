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
 * Represents a quarter in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SolarQuarter extends YearParts {

    /**
     * Chinese quarter names.
     */
    public static final String[] NAMES = { "一季度", "二季度", "三季度", "四季度" };

    /**
     * The index of the quarter (0-3).
     */
    protected int index;

    /**
     * Validates the year and quarter index.
     *
     * @param year  the year
     * @param index the quarter index (0-3)
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(int year, int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException(String.format("illegal solar quarter index: %d", index));
        }
        SolarYear.validate(year);
    }

    /**
     * Constructs a SolarQuarter instance.
     *
     * @param year  the year (1-9999)
     * @param index the quarter index (0-3)
     * @throws IllegalArgumentException if the index is not 0-3
     */
    public SolarQuarter(int year, int index) {
        validate(year, index);
        this.year = year;
        this.index = index;
    }

    /**
     * Creates a SolarQuarter from year and index.
     *
     * @param year  the year (1-9999)
     * @param index the quarter index (0-3)
     * @return a new SolarQuarter instance
     */
    public static SolarQuarter fromIndex(int year, int index) {
        return new SolarQuarter(year, index);
    }

    /**
     * Gets the solar year containing this quarter.
     *
     * @return the SolarYear
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(year);
    }

    /**
     * Gets the index of this quarter (0-3).
     *
     * @return the quarter index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the Chinese name of this quarter.
     *
     * @return the Chinese quarter name
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
     * Gets the next or previous quarter.
     *
     * @param n the number of quarters to move (positive for forward, negative for backward)
     * @return the SolarQuarter n quarters from this one
     */
    public SolarQuarter next(int n) {
        int i = index + n;
        return fromIndex((year * 4 + i) / 4, indexOf(i, 4));
    }

    /**
     * Gets the list of months in this quarter. Each quarter contains 3 months.
     *
     * @return a list of 3 SolarMonth objects
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(3);
        for (int i = 1; i < 4; i++) {
            l.add(SolarMonth.fromYm(year, index * 3 + i));
        }
        return l;
    }

}
