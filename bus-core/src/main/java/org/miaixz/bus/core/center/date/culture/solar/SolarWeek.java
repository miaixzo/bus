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

import org.miaixz.bus.core.center.date.culture.Week;
import org.miaixz.bus.core.center.date.culture.parts.WeekParts;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a week in the Gregorian calendar.
 * <p>
 * A week can start on any day of the week (configurable), and belongs to a specific month and year. Weeks are numbered
 * starting from 0 within each month.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarWeek extends WeekParts {

    /**
     * Constructs a SolarWeek instance.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @param index the week index within the month (0-based)
     * @param start the start day of week (1=Monday, 2=Tuesday, ..., 0=Sunday)
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public SolarWeek(int year, int month, int index, int start) {
        validate(year, month, index, start);
        this.year = year;
        this.month = month;
        this.index = index;
        this.start = start;
    }

    /**
     * Creates a SolarWeek from year, month, index, and start day.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @param index the week index within the month (0-based)
     * @param start the start day of week (1=Monday, 2=Tuesday, ..., 0=Sunday)
     * @return a new SolarWeek instance
     */
    public static SolarWeek fromYm(int year, int month, int index, int start) {
        return new SolarWeek(year, month, index, start);
    }

    /**
     * Validates the week parameters.
     *
     * @param year  the year
     * @param month the month
     * @param index the week index
     * @param start the start day of week
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(int year, int month, int index, int start) {
        WeekParts.validate(index, start);
        SolarMonth m = SolarMonth.fromYm(year, month);
        if (index >= m.getWeekCount(start)) {
            throw new IllegalArgumentException(String.format("illegal solar week index: %d in month: %s", index, m));
        }
    }

    /**
     * Gets the solar month containing this week.
     *
     * @return the SolarMonth
     */
    public SolarMonth getSolarMonth() {
        return SolarMonth.fromYm(year, month);
    }

    /**
     * Gets the index of this week within the year (0-based). Counts from the first week of the year.
     *
     * @return the week index within the year
     */
    public int getIndexInYear() {
        int i = 0;
        SolarDay firstDay = getFirstDay();
        // First week of this year
        SolarWeek w = SolarWeek.fromYm(year, 1, 0, start);
        while (!w.getFirstDay().equals(firstDay)) {
            w = w.next(1);
            i++;
        }
        return i;
    }

    /**
     * Gets the Chinese name of this week (e.g., "第一周", "第二周").
     *
     * @return the Chinese week name
     */
    public String getName() {
        return Week.WHICH[index];
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return getSolarMonth() + getName();
    }

    /**
     * Gets the next or previous week.
     *
     * @param n the number of weeks to move (positive for forward, negative for backward)
     * @return the SolarWeek n weeks from this one
     */
    public SolarWeek next(int n) {
        int d = index;
        SolarMonth m = getSolarMonth();
        if (n > 0) {
            d += n;
            int weekCount = m.getWeekCount(start);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d += 1;
                }
                weekCount = m.getWeekCount(start);
            }
        } else if (n < 0) {
            d += n;
            while (d < 0) {
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d -= 1;
                }
                m = m.next(-1);
                d += m.getWeekCount(start);
            }
        }
        return fromYm(m.getYear(), m.getMonth(), d, start);
    }

    /**
     * Gets the first day of this week.
     *
     * @return the first SolarDay of this week
     */
    public SolarDay getFirstDay() {
        SolarDay firstDay = SolarDay.fromYmd(getYear(), getMonth(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start, 7));
    }

    /**
     * Gets the list of days in this week.
     *
     * @return a list of 7 SolarDay objects in this week
     */
    public List<SolarDay> getDays() {
        List<SolarDay> l = new ArrayList<>(7);
        SolarDay d = getFirstDay();
        l.add(d);
        for (int i = 1; i < 7; i++) {
            l.add(d.next(i));
        }
        return l;
    }

    /**
     * Checks if this object equals another object.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof SolarWeek && getFirstDay().equals(((SolarWeek) o).getFirstDay());
    }

}
