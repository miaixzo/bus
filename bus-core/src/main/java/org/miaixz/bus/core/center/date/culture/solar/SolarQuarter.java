/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.solar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.parts.YearParts;

/**
 * Represents a quarter in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
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
