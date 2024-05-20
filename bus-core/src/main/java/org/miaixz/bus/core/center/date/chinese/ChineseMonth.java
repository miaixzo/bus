/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.center.date.chinese;

/**
 * 农历月份表示
 * 规范参考：<a href="https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E107EA4DE9725EDF819F33C60A44B296">GB/T 33661-2017</a> 的6.2 农历月的命名法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChineseMonth {

    private static final String[] MONTH_NAME = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    private static final String[] MONTH_NAME_TRADITIONAL = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "寒", "冬", "腊"};

    /**
     * 当前农历月份是否为闰月
     *
     * @param year  农历年
     * @param month 农历月
     * @return 是否为闰月
     */
    public static boolean isLeapMonth(final int year, final int month) {
        return month == LunarInfo.leapMonth(year);
    }

    /**
     * 获得农历月称呼
     * 当为传统表示时，表示为二月，腊月，或者润正月等
     * 当为非传统表示时，二月，十二月，或者润一月等
     *
     * @param isLeapMonth   是否闰月
     * @param month         月份，从1开始，如果是闰月，应传入需要显示的月份
     * @param isTraditional 是否传统表示，例如一月传统表示为正月
     * @return 返回农历月份称呼
     */
    public static String getChineseMonthName(final boolean isLeapMonth, final int month, final boolean isTraditional) {
        return (isLeapMonth ? "闰" : "") + (isTraditional ? MONTH_NAME_TRADITIONAL : MONTH_NAME)[month - 1] + "月";
    }

}
