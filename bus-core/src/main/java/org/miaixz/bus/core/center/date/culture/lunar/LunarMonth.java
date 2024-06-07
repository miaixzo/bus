/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.fetus.FetusMonth;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

import java.util.ArrayList;
import java.util.List;

/**
 * 农历月
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarMonth extends Loops {

    public static final String[] NAMES = {
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "腊月"
    };

    /**
     * 农历年
     */
    protected LunarYear year;

    /**
     * 月
     */
    protected int month;

    /**
     * 是否闰月
     */
    protected boolean leap;

    /**
     * 天数
     */
    protected int dayCount;

    /**
     * 位于当年的索引，0-12
     */
    protected int indexInYear;

    /**
     * 初一的儒略日
     */
    protected JulianDay firstJulianDay;

    public LunarMonth(int year, int month) {
        LunarYear currentYear = LunarYear.fromYear(year);
        int currentLeapMonth = currentYear.getLeapMonth();
        if (month == 0 || month > 12 || month < -12) {
            throw new IllegalArgumentException(String.format("illegal lunar month: %d", month));
        }
        boolean leap = month < 0;
        int m = Math.abs(month);
        if (leap && m != currentLeapMonth) {
            throw new IllegalArgumentException(String.format("illegal leap month %d in lunar year %d", m, year));
        }

        // 冬至
        SolarTerms dongZhi = SolarTerms.fromIndex(year, 0);
        double dongZhiJd = dongZhi.getCursoryJulianDay();

        // 冬至前的初一，今年首朔的日月黄经差
        double w = Galaxy.calcShuo(dongZhiJd);
        if (w > dongZhiJd) {
            w -= 29.53;
        }

        // 计算正月初一的偏移
        LunarYear prevYear = LunarYear.fromYear(year - 1);
        int prevLeapMonth = prevYear.getLeapMonth();

        // 正常情况正月初一为第3个朔日，但有些特殊的
        int offset = 2;
        if (year > 8 && year < 24) {
            offset = 1;
        } else if (prevLeapMonth > 10 && year != 239 && year != 240) {
            offset = 3;
        }

        // 位于当年的索引
        int index = m - 1;
        if (leap || (currentLeapMonth > 0 && m > currentLeapMonth)) {
            index += 1;
        }
        indexInYear = index;

        // 本月初一
        w += 29.5306 * (offset + index);
        double firstDay = Galaxy.calcShuo(w);
        firstJulianDay = JulianDay.fromJulianDay(JulianDay.J2000 + firstDay);
        // 本月天数 = 下月初一 - 本月初一
        dayCount = (int) (Galaxy.calcShuo(w + 29.5306) - firstDay);
        this.year = currentYear;
        this.month = m;
        this.leap = leap;
    }

    /**
     * 从农历年月初始化
     *
     * @param year  农历年
     * @param month 农历月，闰月为负
     * @return 农历月
     */
    public static LunarMonth fromYm(int year, int month) {
        return new LunarMonth(year, month);
    }

    /**
     * 农历年
     *
     * @return 农历年
     */
    public LunarYear getYear() {
        return year;
    }

    /**
     * 月
     *
     * @return 月
     */
    public int getMonth() {
        return month;
    }

    /**
     * 月
     *
     * @return 月，当月为闰月时，返回负数
     */
    public int getMonthWithLeap() {
        return leap ? -month : month;
    }

    /**
     * 天数(大月30天，小月29天)
     *
     * @return 天数
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * 位于当年的索引(0-12)
     *
     * @return 索引
     */
    public int getIndexInYear() {
        return indexInYear;
    }

    /**
     * 农历季节
     *
     * @return 农历季节
     */
    public LunarSeason getSeason() {
        return LunarSeason.fromIndex(month - 1);
    }

    /**
     * 初一的儒略日
     *
     * @return 儒略日
     */
    public JulianDay getFirstJulianDay() {
        return firstJulianDay;
    }

    /**
     * 是否闰月
     *
     * @return true/false
     */
    public boolean isLeap() {
        return leap;
    }

    /**
     * 周数
     *
     * @param start 起始星期，1234560分别代表星期一至星期天
     * @return 周数
     */
    public int getWeekCount(int start) {
        return (int) Math.ceil((indexOf(firstJulianDay.getWeek().getIndex() - start, 7) + getDayCount()) / 7D);
    }

    /**
     * 依据国家标准《农历的编算和颁行》GB/T 33661-2017中农历月的命名方法。
     *
     * @return 名称
     */
    public String getName() {
        return (leap ? "闰" : "") + NAMES[month - 1];
    }

    @Override
    public String toString() {
        return year + getName();
    }

    public LunarMonth next(int n) {
        if (n == 0) {
            return fromYm(year.getYear(), getMonthWithLeap());
        }
        int m = indexInYear + 1 + n;
        LunarYear y = year;
        int leapMonth = y.getLeapMonth();
        int monthSize = 12 + (leapMonth > 0 ? 1 : 0);
        boolean forward = n > 0;
        int add = forward ? 1 : -1;
        while (forward ? (m > monthSize) : (m <= 0)) {
            if (forward) {
                m -= monthSize;
            }
            y = y.next(add);
            leapMonth = y.getLeapMonth();
            monthSize = 12 + (leapMonth > 0 ? 1 : 0);
            if (!forward) {
                m += monthSize;
            }
        }
        boolean leap = false;
        if (leapMonth > 0) {
            if (m == leapMonth + 1) {
                leap = true;
            }
            if (m > leapMonth) {
                m--;
            }
        }
        return fromYm(y.getYear(), leap ? -m : m);
    }

    /**
     * 获取本月的农历日列表
     *
     * @return 农历日列表
     */
    public List<LunarDay> getDays() {
        int size = getDayCount();
        int y = year.getYear();
        int m = getMonthWithLeap();
        List<LunarDay> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(LunarDay.fromYmd(y, m, i + 1));
        }
        return l;
    }

    /**
     * 获取本月的农历周列表
     *
     * @param start 星期几作为一周的开始，1234560分别代表星期一至星期天
     * @return 周列表
     */
    public List<LunarWeek> getWeeks(int start) {
        int size = getWeekCount(start);
        int y = year.getYear();
        int m = getMonthWithLeap();
        List<LunarWeek> l = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            l.add(LunarWeek.fromYm(y, m, i, start));
        }
        return l;
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromName(HeavenStem.fromIndex((year.getSixtyCycle().getHeavenStem().getIndex() + 1) * 2 + indexInYear).getName() + EarthBranch.fromIndex(indexInYear + 2).getName());
    }

    /**
     * 九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        return NineStar.fromIndex(27 - year.getSixtyCycle().getEarthBranch().getIndex() % 3 * 3 - getSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * 太岁方位
     *
     * @return 方位
     */
    public Direction getJupiterDirection() {
        SixtyCycle sixtyCycle = getSixtyCycle();
        int n = new int[]{7, -1, 1, 3}[sixtyCycle.getEarthBranch().next(-2).getIndex() % 4];
        return n == -1 ? sixtyCycle.getHeavenStem().getDirection() : Direction.fromIndex(n);
    }

    /**
     * 逐月胎神
     *
     * @return 逐月胎神
     */
    public FetusMonth getFetus() {
        return FetusMonth.fromLunarMonth(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LunarMonth)) {
            return false;
        }
        LunarMonth target = (LunarMonth) o;
        return year.equals(target.getYear()) && getMonthWithLeap() == target.getMonthWithLeap();
    }

}
