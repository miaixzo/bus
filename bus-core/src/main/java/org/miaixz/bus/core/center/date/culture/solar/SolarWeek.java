/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Week;

import java.util.ArrayList;
import java.util.List;

/**
 * 公历周
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarWeek extends Loops {

    /**
     * 月
     */
    protected SolarMonth month;

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

    public static SolarWeek fromYm(int year, int month, int index, int start) {
        return new SolarWeek(year, month, index, start);
    }

    /**
     * 公历月
     *
     * @return 公历月
     */
    public SolarMonth getSolarMonth() {
        return month;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * 月
     *
     * @return 月
     */
    public int getMonth() {
        return month.getMonth();
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
     * 位于当年的索引
     *
     * @return 索引
     */
    public int getIndexInYear() {
        int i = 0;
        SolarDay firstDay = getFirstDay();
        // 今年第1周
        SolarWeek w = SolarWeek.fromYm(getYear(), 1, 0, start.getIndex());
        while (!w.getFirstDay().equals(firstDay)) {
            w = w.next(1);
            i++;
        }
        return i;
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

    public SolarWeek next(int n) {
        int startIndex = start.getIndex();
        if (n == 0) {
            return fromYm(getYear(), getMonth(), index, startIndex);
        }
        int d = index + n;
        SolarMonth m = month;
        if (n > 0) {
            int weekCount = m.getWeekCount(startIndex);
            while (d >= weekCount) {
                d -= weekCount;
                m = m.next(1);
                if (!SolarDay.fromYmd(m.getYear(), m.getMonth(), 1).getWeek().equals(start)) {
                    d += 1;
                }
                weekCount = m.getWeekCount(startIndex);
            }
        } else {
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
     * 本周第1天
     *
     * @return 公历日
     */
    public SolarDay getFirstDay() {
        SolarDay firstDay = SolarDay.fromYmd(getYear(), getMonth(), 1);
        return firstDay.next(index * 7 - indexOf(firstDay.getWeek().getIndex() - start.getIndex(), 7));
    }

    /**
     * 本周公历日列表
     *
     * @return 公历日列表
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
