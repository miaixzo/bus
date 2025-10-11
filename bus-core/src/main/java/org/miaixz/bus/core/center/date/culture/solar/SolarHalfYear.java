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

import org.miaixz.bus.core.center.date.culture.Loops;

/**
 * Represents a half-year in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarHalfYear extends Loops {

    /**
     * Names of solar half-years.
     */
    public static final String[] NAMES = { "上半年", "下半年" };

    /**
     * The solar year this half-year belongs to.
     */
    protected SolarYear year;

    /**
     * The index of the half-year within the year, 0-1.
     */
    protected int index;

    /**
     * Constructs a {@code SolarHalfYear} with the given year and index.
     *
     * @param year  The year.
     * @param index The index of the half-year, 0-1.
     * @throws IllegalArgumentException if the index is out of valid range.
     */
    public SolarHalfYear(int year, int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException(String.format("illegal solar half year index: %d", index));
        }
        this.year = SolarYear.fromYear(year);
        this.index = index;
    }

    /**
     * Creates a {@code SolarHalfYear} instance from the given year and index.
     *
     * @param year  The year.
     * @param index The index of the half-year.
     * @return A new {@link SolarHalfYear} instance.
     */
    public static SolarHalfYear fromIndex(int year, int index) {
        return new SolarHalfYear(year, index);
    }

    /**
     * Gets the solar year this half-year belongs to.
     *
     * @return The {@link SolarYear}.
     */
    public SolarYear getSolarYear() {
        return year;
    }

    /**
     * Gets the year number.
     *
     * @return The year number.
     */
    public int getYear() {
        return year.getYear();
    }

    /**
     * Gets the index of the half-year within the year, 0-1.
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the name of this solar half-year.
     *
     * @return The name of this solar half-year.
     */
    public String getName() {
        return NAMES[index];
    }

    @Override
    public String toString() {
        return year + getName();
    }

    /**
     * Gets the solar half-year after a specified number of half-years.
     *
     * @param n The number of half-years to add.
     * @return The {@link SolarHalfYear} after {@code n} half-years.
     */
    public SolarHalfYear next(int n) {
        int i = index + n;
        return fromIndex((getYear() * 2 + i) / 2, indexOf(i, 2));
    }

    /**
     * Gets a list of all months in this solar half-year. A half-year has 6 months.
     *
     * @return A list of {@link SolarMonth} objects for this half-year.
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(6);
        int y = getYear();
        for (int i = 1; i < 7; i++) {
            l.add(SolarMonth.fromYm(y, index * 6 + i));
        }
        return l;
    }

    /**
     * Gets a list of all quarters in this solar half-year. A half-year has 2 quarters.
     *
     * @return A list of {@link SolarQuarter} objects for this half-year.
     */
    public List<SolarQuarter> getSeasons() {
        List<SolarQuarter> l = new ArrayList<>(2);
        int y = getYear();
        for (int i = 0; i < 2; i++) {
            l.add(SolarQuarter.fromIndex(y, index * 2 + i));
        }
        return l;
    }

}
