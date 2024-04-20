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
package org.miaixz.bus.cron.pattern.matcher;

import org.miaixz.bus.core.lang.Fields;

import java.util.List;

/**
 * 每月第几天匹配
 * 考虑每月的天数不同，且存在闰年情况，日匹配单独使用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DayOfMonthMatcher extends BoolArrayMatcher {

    /**
     * 构造
     *
     * @param intValueList 匹配的日值
     */
    public DayOfMonthMatcher(List<Integer> intValueList) {
        super(intValueList);
    }

    /**
     * 是否为本月最后一天，规则如下：
     * <pre>
     * 1、闰年2月匹配是否为29
     * 2、其它月份是否匹配最后一天的日期（可能为30或者31）
     * </pre>
     *
     * @param value      被检查的值
     * @param month      月份，从1开始
     * @param isLeapYear 是否闰年
     * @return 是否为本月最后一天
     */
    private static boolean isLastDayOfMonth(int value, int month, boolean isLeapYear) {
        return value == Fields.Month.getLastDay(month - 1, isLeapYear);
    }

    /**
     * 给定的日期是否匹配当前匹配器
     *
     * @param value      被检查的值，此处为日
     * @param month      实际的月份，从1开始
     * @param isLeapYear 是否闰年
     * @return 是否匹配
     */
    public boolean match(int value, int month, boolean isLeapYear) {
        return (super.test(value) // 在约定日范围内的某一天
                // 匹配器中用户定义了最后一天（31表示最后一天）
                || (value > 27 && test(31) && isLastDayOfMonth(value, month, isLeapYear)));
    }

}
