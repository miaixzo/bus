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
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Week;
import org.miaixz.bus.core.center.date.culture.parts.WeekParts;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a week in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarWeek extends WeekParts {

    /**
     * Constructs a LunarWeek instance.
     *
     * @param year  the lunar year
     * @param month the lunar month
     * @param index the week index (0-5)
     * @param start the starting day of week (1-7 for Monday-Sunday, 0 for Sunday)
     */
    public LunarWeek(int year, int month, int index, int start) {
        validate(year, month, index, start);
        this.year = year;
        this.month = month;
        this.index = index;
        this.start = start;
    }

    /**
     * Creates a LunarWeek from year, month, index, and start day.
     *
     * @param year  the lunar year
     * @param month the lunar month
     * @param index the week index (0-5)
     * @param start the starting day of week (1-7 for Monday-Sunday, 0 for Sunday)
     * @return a new LunarWeek instance
     */
    public static LunarWeek fromYm(int year, int month, int index, int start) {
        return new LunarWeek(year, month, index, start);
    }

    /**
     * Validates the lunar year, month, week index, and start day.
     *
     * @param year  the lunar year
     * @param month the lunar month
     * @param index the week index
     * @param start the starting day of week
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public static void validate(int year, int month, int index, int start) {
        WeekParts.validate(index, start);
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (index >= m.getWeekCount(start)) {
            throw new IllegalArgumentException(String.format("illegal lunar week index: %d in month: %s", index, m));
        }
    }

    /**
     * Gets the lunar month for this week.
     *
     * @return the lunar month
     */
    public LunarMonth getLunarMonth() {
        return LunarMonth.fromYm(year, month);
    }

    /**
     * Gets the name of this week (e.g., "第一周", "第二周").
     *
     * @return the week name
     */
    public String getName() {
        return Week.WHICH[index];
    }

    @Override
    public String toString() {
        return getLunarMonth() + getName();
    }

    /**
     * Gets the lunar week that is n weeks after this week.
     *
     * @param n the number of weeks to advance (can be negative)
     * @return the lunar week after n weeks
     */
    public LunarWeek next(int n) {
        if (n == 0) {
            return fromYm(getYear(), getMonth(), index, start);
        }
        int d = index + n;
        LunarMonth m = getLunarMonth();
        if (n > 0) {
            int weekCount = m.getWeekCount(start);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d += 1;
                }
                weekCount = m.getWeekCount(start);
            }
        } else {
            while (d < 0) {
                if (m.getFirstDay().getWeek().getIndex() != start) {
                    d -= 1;
                }
                m = m.next(-1);
                d += m.getWeekCount(start);
            }
        }
        return fromYm(m.getYear(), m.getMonthWithLeap(), d, start);
    }

    /**
     * Gets the first day of this week.
     *
     * @return the first lunar day of this week
     */
    public LunarDay getFirstDay() {
        LunarDay firstDay = LunarDay.fromYmd(getYear(), getMonth(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start, 7));
    }

    /**
     * Gets the list of lunar days in this week.
     *
     * @return the list of 7 lunar days
     */
    public List<LunarDay> getDays() {
        List<LunarDay> l = new ArrayList<>(7);
        LunarDay d = getFirstDay();
        l.add(d);
        for (int i = 1; i < 7; i++) {
            l.add(d.next(i));
        }
        return l;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LunarWeek && getFirstDay().equals(((LunarWeek) o).getFirstDay());
    }

}
