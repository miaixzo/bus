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

import org.miaixz.bus.core.center.date.culture.parts.MonthParts;

/**
 * Represents a month in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarMonth extends MonthParts {

    /**
     * Chinese month names (1月 to 12月).
     */
    public static final String[] NAMES = { "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月" };

    /**
     * Number of days in each month for a non-leap year.
     */
    public static final int[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /**
     * Constructs a SolarMonth instance.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @throws IllegalArgumentException if the month is invalid
     */
    public SolarMonth(int year, int month) {
        validate(year, month);
        this.year = year;
        this.month = month;
    }

    /**
     * Creates a SolarMonth from year and month.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @return a new SolarMonth instance
     */
    public static SolarMonth fromYm(int year, int month) {
        return new SolarMonth(year, month);
    }

    /**
     * Validates the year and month.
     *
     * @param year  the year
     * @param month the month
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(String.format("illegal solar month: %d", month));
        }
        SolarYear.validate(year);
    }

    /**
     * Gets the solar year containing this month.
     *
     * @return the SolarYear
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(year);
    }

    /**
     * Gets the number of days in this month. Note: October 1582 has only 21 days due to the Gregorian calendar reform.
     *
     * @return the number of days in this month
     */
    public int getDayCount() {
        if (1582 == year && 10 == month) {
            return 21;
        }
        int d = DAYS[getIndexInYear()];
        // Leap year adds one day to February
        if (2 == month && getSolarYear().isLeap()) {
            d++;
        }
        return d;
    }

    /**
     * Gets the index of this month within the year (0-11, where 0=January).
     *
     * @return the month index within the year
     */
    public int getIndexInYear() {
        return month - 1;
    }

    /**
     * Gets the solar quarter containing this month.
     *
     * @return the SolarQuarter
     */
    public SolarQuarter getSeason() {
        return SolarQuarter.fromIndex(year, getIndexInYear() / 3);
    }

    /**
     * Gets the number of weeks in this month.
     *
     * @param start the start day of week (1=Monday, 2=Tuesday, ..., 0=Sunday)
     * @return the number of weeks in this month
     */
    public int getWeekCount(int start) {
        return (int) Math
                .ceil((indexOf(SolarDay.fromYmd(year, month, 1).getWeek().getIndex() - start, 7) + getDayCount()) / 7D);
    }

    /**
     * Gets the Chinese name of this month (e.g., "1月", "2月").
     *
     * @return the Chinese month name
     */
    public String getName() {
        return NAMES[getIndexInYear()];
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
     * Gets the next or previous month.
     *
     * @param n the number of months to move (positive for forward, negative for backward)
     * @return the SolarMonth n months from this one
     */
    public SolarMonth next(int n) {
        int i = month - 1 + n;
        return fromYm((year * 12 + i) / 12, indexOf(i, 12) + 1);
    }

    /**
     * Gets the list of weeks in this month.
     *
     * @param start the start day of week (1=Monday, 2=Tuesday, ..., 0=Sunday)
     * @return a list of SolarWeek objects in this month
     */
    public List<SolarWeek> getWeeks(int start) {
        int size = getWeekCount(start);
        List<SolarWeek> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(SolarWeek.fromYm(year, month, i, start));
        }
        return l;
    }

    /**
     * Gets the list of days in this month.
     *
     * @return a list of SolarDay objects in this month
     */
    public List<SolarDay> getDays() {
        int size = getDayCount();
        List<SolarDay> l = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            l.add(SolarDay.fromYmd(year, month, i));
        }
        return l;
    }

    /**
     * Gets the first day of this month.
     *
     * @return the first SolarDay of this month
     */
    public SolarDay getFirstDay() {
        return SolarDay.fromYmd(year, month, 1);
    }

}
