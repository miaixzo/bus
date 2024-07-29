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
 * 公历半年
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarHalfYear extends Loops {

    public static final String[] NAMES = { "上半年", "下半年" };

    /**
     * 年
     */
    protected SolarYear year;

    /**
     * 索引，0-1
     */
    protected int index;

    /**
     * 初始化
     *
     * @param year  年
     * @param index 索引，0-1
     */
    public SolarHalfYear(int year, int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException(String.format("illegal solar half year index: %d", index));
        }
        this.year = SolarYear.fromYear(year);
        this.index = index;
    }

    public static SolarHalfYear fromIndex(int year, int index) {
        return new SolarHalfYear(year, index);
    }

    /**
     * 公历年
     *
     * @return 公历年
     */
    public SolarYear getSolarYear() {
        return year;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return year.getYear();
    }

    /**
     * 索引
     *
     * @return 索引，0-1
     */
    public int getIndex() {
        return index;
    }

    public String getName() {
        return NAMES[index];
    }

    @Override
    public String toString() {
        return year + getName();
    }

    public SolarHalfYear next(int n) {
        if (n == 0) {
            return fromIndex(getYear(), index);
        }
        int i = index + n;
        int y = getYear() + i / 2;
        i %= 2;
        if (i < 0) {
            i += 2;
            y -= 1;
        }
        return fromIndex(y, i);
    }

    /**
     * 月份列表
     *
     * @return 月份列表，半年有6个月。
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(6);
        int y = getYear();
        for (int i = 1; i < 7; i++) {
            l.add(SolarMonth.fromYm(y, index * 6 + i));
        }
        return l;
    }

    /**
     * 季度列表
     *
     * @return 季度列表，半年有2个季度。
     */
    public List<SolarQuarter> getSeasons() {
        List<SolarQuarter> l = new ArrayList<>(2);
        int y = getYear();
        for (int i = 0; i < 2; i++) {
            l.add(SolarQuarter.fromIndex(y, index * 2 + i));
        }
        return l;
    }

}
