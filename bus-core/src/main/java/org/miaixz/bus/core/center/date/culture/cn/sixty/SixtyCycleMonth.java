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
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * 干支月
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixtyCycleMonth extends Loops {
    /**
     * 干支年
     */
    protected SixtyCycleYear year;

    /**
     * 月柱
     */
    protected SixtyCycle month;

    /**
     * 初始化
     *
     * @param year  干支年
     * @param month 月柱
     */
    public SixtyCycleMonth(SixtyCycleYear year, SixtyCycle month) {
        this.year = year;
        this.month = month;
    }

    /**
     * 从年和月索引初始化
     *
     * @param year  年
     * @param index 月索引
     * @return 干支月
     */
    public static SixtyCycleMonth fromIndex(int year, int index) {
        return SixtyCycleYear.fromYear(year).getFirstMonth().next(index);
    }

    /**
     * 干支年
     *
     * @return 干支年
     */
    public SixtyCycleYear getSixtyCycleYear() {
        return year;
    }

    /**
     * 年柱
     *
     * @return 年柱
     */
    public SixtyCycle getYear() {
        return year.getSixtyCycle();
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        return month;
    }

    public String getName() {
        return String.format("%s月", month);
    }

    @Override
    public String toString() {
        return String.format("%s%s", year, getName());
    }

    public SixtyCycleMonth next(int n) {
        return new SixtyCycleMonth(SixtyCycleYear.fromYear((year.getYear() * 12 + getIndexInYear() + n) / 12),
                month.next(n));
    }

    /**
     * 位于当年的索引(0-11)，寅月为0，依次类推
     *
     * @return 索引
     */
    public int getIndexInYear() {
        return month.getEarthBranch().next(-2).getIndex();
    }

    /**
     * 九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        int index = month.getEarthBranch().getIndex();
        if (index < 2) {
            index += 3;
        }
        return NineStar.fromIndex(27 - getYear().getEarthBranch().getIndex() % 3 * 3 - index);
    }

    /**
     * 太岁方位
     *
     * @return 方位
     */
    public Direction getJupiterDirection() {
        int n = new int[] { 7, -1, 1, 3 }[month.getEarthBranch().next(-2).getIndex() % 4];
        return n == -1 ? month.getHeavenStem().getDirection() : Direction.fromIndex(n);
    }

    /**
     * 首日（节令当天）
     *
     * @return 干支日
     */
    public SixtyCycleDay getFirstDay() {
        return SixtyCycleDay.fromSolarDay(
                SolarTerms.fromIndex(year.getYear(), 3 + getIndexInYear() * 2).getJulianDay().getSolarDay());
    }

    /**
     * 本月的干支日列表
     *
     * @return 干支日列表
     */
    public List<SixtyCycleDay> getDays() {
        List<SixtyCycleDay> l = new ArrayList<>();
        SixtyCycleDay d = getFirstDay();
        while (d.getSixtyCycleMonth().equals(this)) {
            l.add(d);
            d = d.next(1);
        }
        return l;
    }
}
