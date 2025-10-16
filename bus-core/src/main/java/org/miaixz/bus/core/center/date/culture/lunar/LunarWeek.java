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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Week;

/**
 * Represents a week in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarWeek extends Loops {

    /**
     * The lunar month this week belongs to.
     */
    protected LunarMonth month;

    /**
     * The index of the week within the month, 0-5.
     */
    protected int index;

    /**
     * The starting day of the week.
     */
    protected Week start;

    /**
     * Initializes a new LunarWeek instance.
     *
     * @param year  The year.
     * @param month The month.
     * @param index The index of the week, 0-5.
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @throws IllegalArgumentException if the index or start day is out of valid range.
     */
    public LunarWeek(int year, int month, int index, int start) {
        if (index < 0 || index > 5) {
            throw new IllegalArgumentException(String.format("illegal lunar week index: %d", index));
        }
        if (start < 0 || start > 6) {
            throw new IllegalArgumentException(String.format("illegal lunar week start: %d", start));
        }
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (index >= m.getWeekCount(start)) {
            throw new IllegalArgumentException(String.format("illegal lunar week index: %d in month: %s", index, m));
        }
        this.month = m;
        this.index = index;
        this.start = Week.fromIndex(start);
    }

    /**
     * Creates a new LunarWeek instance from year, month, week index, and start day.
     *
     * @param year  The year.
     * @param month The month.
     * @param index The index of the week, 0-5.
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @return A new {@link LunarWeek} instance.
     */
    public static LunarWeek fromYm(int year, int month, int index, int start) {
        return new LunarWeek(year, month, index, start);
    }

    /**
     * Gets the lunar month this week belongs to.
     *
     * @return The {@link LunarMonth}.
     */
    public LunarMonth getLunarMonth() {
        return month;
    }

    /**
     * Gets the year of this lunar week.
     *
     * @return The year.
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * Gets the month of this lunar week, with negative value indicating a leap month.
     *
     * @return The month.
     */
    public int getMonth() {
        return month.getMonthWithLeap();
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
     * Gets the lunar week after a specified number of weeks.
     *
     * @param n The number of weeks to add.
     * @return The {@link LunarWeek} after {@code n} weeks.
     */
    public LunarWeek next(int n) {
        int startIndex = start.getIndex();
        if (n == 0) {
            return fromYm(getYear(), getMonth(), index, startIndex);
        }
        int d = index + n;
        LunarMonth m = month;
        if (n > 0) {
            int weekCount = m.getWeekCount(startIndex);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (!LunarDay.fromYmd(m.getYear(), m.getMonthWithLeap(), 1).getWeek().equals(start)) {
                    d += 1;
                }
                weekCount = m.getWeekCount(startIndex);
            }
        } else {
            while (d < 0) {
                if (!LunarDay.fromYmd(m.getYear(), m.getMonthWithLeap(), 1).getWeek().equals(start)) {
                    d -= 1;
                }
                m = m.next(-1);
                d += m.getWeekCount(startIndex);
            }
        }
        return fromYm(m.getYear(), m.getMonthWithLeap(), d, startIndex);
    }

    /**
     * Gets the first day of this week.
     *
     * @return The first {@link LunarDay} of this week.
     */
    public LunarDay getFirstDay() {
        LunarDay firstDay = LunarDay.fromYmd(getYear(), getMonth(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start.getIndex(), 7));
    }

    /**
     * Gets a list of lunar days in this week.
     *
     * @return A list of {@link LunarDay} objects for this week.
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
