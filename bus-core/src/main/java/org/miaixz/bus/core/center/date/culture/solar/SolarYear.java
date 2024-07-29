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

import java.util.ArrayList;
import java.util.List;

/**
 * 公历年
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarYear extends Loops {

    /**
     * 年
     */
    protected int year;

    public SolarYear(int year) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException(String.format("illegal solar year: %d", year));
        }
        this.year = year;
    }

    /**
     * 从年初始化
     *
     * @param year 年，支持1到9999年
     * @return 公历年
     */
    public static SolarYear fromYear(int year) {
        return new SolarYear(year);
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return year;
    }

    /**
     * 天数（1582年355天，平年365天，闰年366天）
     *
     * @return 天数
     */
    public int getDayCount() {
        if (1582 == year) {
            return 355;
        }
        return isLeap() ? 366 : 365;
    }

    /**
     * 是否闰年(1582年以前，使用儒略历，能被4整除即为闰年。以后采用格里历，四年一闰，百年不闰，四百年再闰。)
     *
     * @return true/false
     */
    public boolean isLeap() {
        if (year < 1600) {
            return year % 4 == 0;
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public String getName() {
        return String.format("%d年", year);
    }

    public SolarYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * 月份列表
     *
     * @return 月份列表，1年有12个月。
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(12);
        for (int i = 1; i < 13; i++) {
            l.add(SolarMonth.fromYm(year, i));
        }
        return l;
    }

    /**
     * 季度列表
     *
     * @return 季度列表，1年有4个季度。
     */
    public List<SolarQuarter> getSeasons() {
        List<SolarQuarter> l = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            l.add(SolarQuarter.fromIndex(year, i));
        }
        return l;
    }

    /**
     * 半年列表
     *
     * @return 半年列表，1年有2个半年。
     */
    public List<SolarHalfYear> getHalfYears() {
        List<SolarHalfYear> l = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            l.add(SolarHalfYear.fromIndex(year, i));
        }
        return l;
    }

}
