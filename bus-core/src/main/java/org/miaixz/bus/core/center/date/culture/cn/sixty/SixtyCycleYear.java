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
import org.miaixz.bus.core.center.date.culture.cn.Twenty;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;

/**
 * 干支年
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixtyCycleYear extends Loops {

    /**
     * 年
     */
    protected int year;

    public SixtyCycleYear(int year) {
        if (year < -1 || year > 9999) {
            throw new IllegalArgumentException(String.format("illegal sixty cycle year: %d", year));
        }
        this.year = year;
    }

    /**
     * 从年初始化
     *
     * @param year 年，支持-1到9999年
     * @return 干支年
     */
    public static SixtyCycleYear fromYear(int year) {
        return new SixtyCycleYear(year);
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
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromIndex(year - 4);
    }

    public String getName() {
        return String.format("%s年", getSixtyCycle());
    }

    /**
     * 运
     *
     * @return 运
     */
    public Twenty getTwenty() {
        return Twenty.fromIndex((int) Math.floor((year - 1864) / 20D));
    }

    /**
     * 九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        return NineStar.fromIndex(63 + getTwenty().getSixty().getIndex() * 3 - getSixtyCycle().getIndex());
    }

    /**
     * 太岁方位
     *
     * @return 方位
     */
    public Direction getJupiterDirection() {
        return Direction.fromIndex(
                new int[] { 0, 7, 7, 2, 3, 3, 8, 1, 1, 6, 0, 0 }[getSixtyCycle().getEarthBranch().getIndex()]);
    }

    /**
     * 推移
     *
     * @param n 推移年数
     * @return 干支年
     */
    public SixtyCycleYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * 首月（依据五虎遁和正月起寅的规律）
     *
     * @return 干支月
     */
    public SixtyCycleMonth getFirstMonth() {
        HeavenStem h = HeavenStem.fromIndex((getSixtyCycle().getHeavenStem().getIndex() + 1) * 2);
        return new SixtyCycleMonth(this, SixtyCycle.fromName(h.getName() + "寅"));
    }

    /**
     * 干支月列表
     *
     * @return 干支月列表
     */
    public List<SixtyCycleMonth> getMonths() {
        List<SixtyCycleMonth> l = new ArrayList<>();
        SixtyCycleMonth m = getFirstMonth();
        l.add(m);
        for (int i = 1; i < 12; i++) {
            l.add(m.next(i));
        }
        return l;
    }

}
