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
import org.miaixz.bus.core.center.date.culture.en.Quarter;

/**
 * Represents a quarter in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarQuarter extends Loops {

    /**
     * The solar year this quarter belongs to.
     */
    protected SolarYear year;

    /**
     * The index of the quarter within the year, 0-3.
     */
    protected int index;

    /**
     * Constructs a {@code SolarQuarter} with the given year and index.
     *
     * @param year  The year.
     * @param index The index of the quarter, 0-3.
     * @throws IllegalArgumentException if the index is out of valid range.
     */
    public SolarQuarter(int year, int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException(String.format("illegal solar season index: %d", index));
        }
        this.year = SolarYear.fromYear(year);
        this.index = index;
    }

    /**
     * Creates a {@code SolarQuarter} instance from the given year and index.
     *
     * @param year  The year.
     * @param index The index of the quarter.
     * @return A new {@link SolarQuarter} instance.
     */
    public static SolarQuarter fromIndex(int year, int index) {
        return new SolarQuarter(year, index);
    }

    /**
     * Gets the solar year this quarter belongs to.
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
     * Gets the index of the quarter within the year, 0-3.
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the name of this solar quarter.
     *
     * @return The name of this solar quarter.
     */
    public String getName() {
        return Quarter.getName(index);
    }

    @Override
    public String toString() {
        return year + getName();
    }

    /**
     * Gets the solar quarter after a specified number of quarters.
     *
     * @param n The number of quarters to add.
     * @return The {@link SolarQuarter} after {@code n} quarters.
     */
    public SolarQuarter next(int n) {
        int i = index + n;
        return fromIndex((getYear() * 4 + i) / 4, indexOf(i, 4));
    }

    /**
     * Gets a list of all months in this solar quarter. A quarter has 3 months.
     *
     * @return A list of {@link SolarMonth} objects for this quarter.
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(3);
        int y = getYear();
        for (int i = 1; i < 4; i++) {
            l.add(SolarMonth.fromYm(y, index * 3 + i));
        }
        return l;
    }

}
