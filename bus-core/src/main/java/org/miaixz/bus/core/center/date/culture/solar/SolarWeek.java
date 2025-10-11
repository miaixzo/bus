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
import org.miaixz.bus.core.center.date.culture.cn.Week;

/**
 * Represents a week in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarWeek extends Loops {

    /**
     * The solar month this week belongs to.
     */
    protected SolarMonth month;

    /**
     * The index of the week within the month, 0-5.
     */
    protected int index;

    /**
     * The starting day of the week.
     */
    protected Week start;

    /**
     * Constructs a {@code SolarWeek} with the given year, month, week index, and start day.
     *
     * @param year  The year.
     * @param month The month.
     * @param index The index of the week, 0-5.
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @throws IllegalArgumentException if the index or start day is out of valid range.
     */
    public SolarWeek(int year, int month, int index, int start) {
        if (index < 0 || index > 5) {
            throw new IllegalArgumentException(String.format("illegal solar week index: %d", index));
        }
        if (start < 0 || start > 6) {
            throw new IllegalArgumentException(String.format("illegal solar week start: %d", start));
        }
        SolarMonth m = SolarMonth.fromYm(year, month);
        if (index >= m.getWeekCount(start)) {
            throw new IllegalArgumentException(String.format("illegal solar week index: %d in month: %s", index, m));
        }
        this.month = m;
        this.index = index;
        this.start = Week.fromIndex(start);
    }

    /**
     * Creates a {@code SolarWeek} instance from the given year, month, week index, and start day.
     *
     * @param year  The year.
     * @param month The month.
     * @param index The index of the week, 0-5.
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @return A new {@link SolarWeek} instance.
     */
    public static SolarWeek fromYm(int year, int month, int index, int start) {
        return new SolarWeek(year, month, index, start);
    }

    /**
     * Gets the solar month this week belongs to.
     *
     * @return The {@link SolarMonth}.
     */
    public SolarMonth getSolarMonth() {
        return month;
    }

    /**
     * Gets the year of this solar week.
     *
     * @return The year.
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * Gets the month of this solar week.
     *
     * @return The month.
     */
    public int getMonth() {
        return month.getMonth();
    }

    /**
     * Gets the index of the week within the month, 0-5.
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the index of this week within the year.
     *
     * @return The index within the year.
     */
    public int getIndexInYear() {
        int i = 0;
        SolarDay firstDay = getFirstDay();
        // The first week of the year
        SolarWeek w = SolarWeek.fromYm(getYear(), 1, 0, start.getIndex());
        while (!w.getFirstDay().equals(firstDay)) {
            w = w.next(1);
            i++;
        }
        return i;
    }

    /**
     * Gets the starting day of the week.
     *
     * @return The {@link Week} representing the start day.
     */
    public Week getStart() {
        return start;
    }

    /**
     * Gets the Chinese name of the week.
     *
     * @return The name of the week.
     */
    public String getName() {
        return Week.WHICH[index];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    /**
     * Gets the solar week after a specified number of weeks.
     *
     * @param n The number of weeks to add.
     * @return The {@link SolarWeek} after {@code n} weeks.
     */
    public SolarWeek next(int n) {
        int startIndex = start.getIndex();
        int d = index;
        SolarMonth m = month;
        if (n > 0) {
            d += n;
            int weekCount = m.getWeekCount(startIndex);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (!SolarDay.fromYmd(m.getYear(), m.getMonth(), 1).getWeek().equals(start)) {
                    d += 1;
                }
                weekCount = m.getWeekCount(startIndex);
            }
        } else if (n < 0) {
            d += n;
            while (d < 0) {
                if (!SolarDay.fromYmd(m.getYear(), m.getMonth(), 1).getWeek().equals(start)) {
                    d -= 1;
                }
                m = m.next(-1);
                d += m.getWeekCount(startIndex);
            }
        }
        return fromYm(m.getYear(), m.getMonth(), d, startIndex);
    }

    /**
     * Gets the first day of this week.
     *
     * @return The first {@link SolarDay} of this week.
     */
    public SolarDay getFirstDay() {
        SolarDay firstDay = SolarDay.fromYmd(getYear(), getMonth(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start.getIndex(), 7));
    }

    /**
     * Gets a list of all solar days in this week.
     *
     * @return A list of {@link SolarDay} objects for this week.
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

    @Override
    public boolean equals(Object o) {
        return o instanceof SolarWeek && getFirstDay().equals(((SolarWeek) o).getFirstDay());
    }

}
