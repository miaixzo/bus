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
package org.miaixz.bus.core.center.date.culture.en;

import java.time.temporal.ChronoField;

import org.miaixz.bus.core.lang.Assert;

/**
 * Enumeration for quarters of a year.
 *
 * @author Kimi Liu
 * @see #Q1
 * @see #Q2
 * @see #Q3
 * @see #Q4
 * @since Java 17+
 */
public enum Quarter {

    /**
     * First quarter
     */
    Q1(1, "一季度"),
    /**
     * Second quarter
     */
    Q2(2, "二季度"),
    /**
     * Third quarter
     */
    Q3(3, "三季度"),
    /**
     * Fourth quarter
     */
    Q4(4, "四季度");

    /**
     * Array of all {@link Quarter} enum constants.
     */
    private static final Quarter[] ENUMS = Quarter.values();

    /**
     * The code of the quarter.
     */
    private final int code;
    /**
     * The name of the quarter.
     */
    private final String name;

    /**
     * Constructor for Quarter enum.
     *
     * @param code The code of the quarter.
     * @param name The name of the quarter.
     */
    Quarter(final int code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Converts an int value representing a quarter to a {@link Quarter} enum object.
     *
     * @param intValue The int value representing the quarter.
     * @return The corresponding {@link Quarter} enum.
     * @see #Q1
     * @see #Q2
     * @see #Q3
     * @see #Q4
     */
    public static Quarter of(final int intValue) {
        switch (intValue) {
            case 1:
                return Q1;

            case 2:
                return Q2;

            case 3:
                return Q3;

            case 4:
                return Q4;

            default:
                return null;
        }
    }

    /**
     * Gets the name of the quarter.
     *
     * @param code The code of the quarter.
     * @return The name of the quarter.
     */
    public static String getName(int code) {
        return ENUMS[code].name;
    }

    /**
     * Returns the corresponding quarter based on the given month value.
     *
     * @param monthValue The month value, ranging from 1 to 12.
     * @return The corresponding quarter.
     * @throws IllegalArgumentException If the month value is not within the valid range (1 to 12).
     */
    public static Quarter fromMonth(final int monthValue) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(monthValue);
        return of(computeQuarterValueInternal(monthValue));
    }

    /**
     * Returns the corresponding quarter based on the given {@link Month}.
     *
     * @param month The {@link Month}.
     * @return The corresponding quarter.
     */
    public static Quarter fromMonth(final Month month) {
        Assert.notNull(month);
        final int monthValue = month.getValue();
        return of(computeQuarterValueInternal(monthValue));
    }

    /**
     * Computes the quarter value corresponding to the given month value.
     *
     * @param monthValue The month value, ranging from 1 to 12.
     * @return The corresponding quarter value.
     */
    private static int computeQuarterValueInternal(final int monthValue) {
        return (monthValue - 1) / 3 + 1;
    }

    /**
     * Gets the code of the quarter.
     *
     * @return The code of the quarter.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Gets the name of the quarter.
     *
     * @return The name of the quarter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the first month of this quarter.
     *
     * @return The first month of this quarter.
     */
    public Month firstMonth() {
        return Month.of(this.code * 3 - 3);
    }

    /**
     * Gets the last month of this quarter.
     *
     * @return The last month of this quarter.
     */
    public Month lastMonth() {
        return Month.of(this.code * 3 - 1);
    }

}
