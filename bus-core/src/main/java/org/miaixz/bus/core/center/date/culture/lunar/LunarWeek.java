/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              ~
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

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Week;

import java.util.ArrayList;
import java.util.List;

/**
 * 农历周
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarWeek extends Loops {

    /**
     * 月
     */
    protected LunarMonth month;

    /**
     * 索引，0-5
     */
    protected int index;

    /**
     * 起始星期
     */
    protected Week start;

    /**
     * 初始化
     *
     * @param year  年
     * @param month 月
     * @param index 索引，0-5
     * @param start 起始星期，1234560分别代表星期一至星期天
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

    public static LunarWeek fromYm(int year, int month, int index, int start) {
        return new LunarWeek(year, month, index, start);
    }

    /**
     * 月
     *
     * @return 月
     */
    public LunarMonth getMonth() {
        return month;
    }

    /**
     * 索引
     *
     * @return 索引，0-5
     */
    public int getIndex() {
        return index;
    }

    /**
     * 起始星期
     *
     * @return 星期
     */
    public Week getStart() {
        return start;
    }

    public String getName() {
        return Week.WHICH[index];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    public LunarWeek next(int n) {
        int startIndex = start.getIndex();
        if (n == 0) {
            return fromYm(month.getYear().getYear(), month.getMonthWithLeap(), index, startIndex);
        }
        int d = index + n;
        LunarMonth m = month;
        int weeksInMonth = m.getWeekCount(startIndex);
        boolean forward = n > 0;
        int add = forward ? 1 : -1;
        while (forward ? (d >= weeksInMonth) : (d < 0)) {
            if (forward) {
                d -= weeksInMonth;
            }
            if (!forward) {
                if (!LunarDay.fromYmd(m.getYear().getYear(), m.getMonthWithLeap(), 1).getWeek().equals(start)) {
                    d += add;
                }
            }
            m = m.next(add);
            if (forward) {
                if (!LunarDay.fromYmd(m.getYear().getYear(), m.getMonthWithLeap(), 1).getWeek().equals(start)) {
                    d += add;
                }
            }
            weeksInMonth = m.getWeekCount(startIndex);
            if (!forward) {
                d += weeksInMonth;
            }
        }
        return fromYm(m.getYear().getYear(), m.getMonthWithLeap(), d, startIndex);
    }

    /**
     * 本周第1天
     *
     * @return 农历日
     */
    public LunarDay getFirstDay() {
        LunarMonth m = getMonth();
        LunarDay firstDay = LunarDay.fromYmd(m.getYear().getYear(), m.getMonthWithLeap(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start.getIndex(), 7));
    }

    /**
     * 本周农历日列表
     *
     * @return 农历日列表
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

}
