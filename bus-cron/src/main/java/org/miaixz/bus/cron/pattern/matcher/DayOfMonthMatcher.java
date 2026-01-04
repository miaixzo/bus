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
package org.miaixz.bus.cron.pattern.matcher;

import org.miaixz.bus.core.center.date.Month;

import java.util.List;

/**
 * A matcher for the day-of-month field in a cron expression. This class handles the complexity of matching days,
 * considering that the number of days varies per month and the existence of leap years. It has special logic for the
 * 'L' (last day of the month) token.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DayOfMonthMatcher extends BoolArrayMatcher {

    /**
     * Constructs a new DayOfMonthMatcher.
     *
     * @param intValueList A list of integer values for the days to match.
     */
    public DayOfMonthMatcher(final List<Integer> intValueList) {
        super(intValueList);
    }

    /**
     * Gets the last day of the given month.
     *
     * @param month      The month (1-based).
     * @param isLeapYear Whether it is a leap year.
     * @return The last day of the month.
     */
    private static int getLastDay(final int month, final boolean isLeapYear) {
        return Month.getLastDay(month - 1, isLeapYear);
    }

    /**
     * Checks if the given day of the month matches this matcher for a specific month and year.
     *
     * @param value      The day of the month to check.
     * @param month      The actual month (1-based).
     * @param isLeapYear Whether it is a leap year.
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    public boolean match(final int value, final int month, final boolean isLeapYear) {
        return (super.test(value) // Matches a specific day defined in the list
                // Or matches the last day of the month if 'L' (represented by 31) was specified.
                || matchLastDay(value, getLastDay(month, isLeapYear)));
    }

    /**
     * Gets the next matching value at or after the given value for a specific month. If the expression contains 'L'
     * (last day), this method correctly handles the varying number of days in each month.
     *
     * @param value      The value to start searching from.
     * @param month      The month (1-based).
     * @param isLeapYear Whether it is a leap year.
     * @return The next matching value.
     */
    public int nextAfter(int value, final int month, final boolean isLeapYear) {
        final int minValue = getMinValue(month, isLeapYear);
        if (value > minValue) {
            final boolean[] bValues = this.bValues;
            while (value < bValues.length) {
                if (bValues[value]) {
                    if (31 == value) {
                        // If the value is 31 (representing 'L'), return the actual last day of the month.
                        return getLastDay(month, isLeapYear);
                    }
                    return value;
                }
                value++;
            }
        }

        // Returns the minimum value in two cases:
        // 1. The given value is less than the minimum value, so the next match is the minimum value.
        // 2. The given value is greater than the maximum value, so the next match is the minimum value of the next
        // cycle.
        return minValue;
    }

    /**
     * Gets the minimum matching value for the given month. If 'L' is specified, this will return the actual last day of
     * the given month.
     *
     * @param month      The month (1-based).
     * @param isLeapYear Whether it is a leap year.
     * @return The minimum matching value.
     */
    public int getMinValue(final int month, final boolean isLeapYear) {
        final int minValue = super.getMinValue();
        if (31 == minValue) {
            // If the user specified 'L' (represented by 31), the minimum value is the last day of the month.
            return getLastDay(month, isLeapYear);
        }
        return minValue;
    }

    /**
     * Checks if the value matches the last day of the month. This is true if the expression included 'L' (represented
     * by 31) and the value is the actual last day of the given month.
     *
     * @param value   The day value to check.
     * @param lastDay The actual last day of the current month.
     * @return {@code true} if it's a match for the last day, {@code false} otherwise.
     */
    private boolean matchLastDay(final int value, final int lastDay) {
        // The value > 27 is an optimization.
        return value > 27 && test(31) && value == lastDay;
    }

}
