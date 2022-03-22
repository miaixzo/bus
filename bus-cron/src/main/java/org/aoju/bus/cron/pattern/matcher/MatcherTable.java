/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.cron.pattern.matcher;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间匹配表，用于存放定时任务表达式解析后的结构信息
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class MatcherTable {

    /**
     * 秒字段匹配列表
     */
    public final List<ValueMatcher> secondMatchers;
    /**
     * 分字段匹配列表
     */
    public final List<ValueMatcher> minuteMatchers;
    /**
     * 时字段匹配列表
     */
    public final List<ValueMatcher> hourMatchers;
    /**
     * 每月几号字段匹配列表
     */
    public final List<ValueMatcher> dayOfMonthMatchers;
    /**
     * 月字段匹配列表
     */
    public final List<ValueMatcher> monthMatchers;
    /**
     * 星期字段匹配列表
     */
    public final List<ValueMatcher> dayOfWeekMatchers;
    /**
     * 年字段匹配列表
     */
    public final List<ValueMatcher> yearMatchers;
    /**
     * 匹配器个数，取决于复合任务表达式中的单一表达式个数
     */
    public int matcherSize;

    /**
     * 构造
     *
     * @param size 表达式个数，用于表示复合表达式中单个表达式个数
     */
    public MatcherTable(int size) {
        matcherSize = size;
        secondMatchers = new ArrayList<>(size);
        minuteMatchers = new ArrayList<>(size);
        hourMatchers = new ArrayList<>(size);
        dayOfMonthMatchers = new ArrayList<>(size);
        monthMatchers = new ArrayList<>(size);
        dayOfWeekMatchers = new ArrayList<>(size);
        yearMatchers = new ArrayList<>(size);
    }

    /**
     * 是否匹配日（指定月份的第几天）
     *
     * @param matcher    {@link ValueMatcher}
     * @param dayOfMonth 日
     * @param month      月
     * @param isLeapYear 是否闰年
     * @return 是否匹配
     */
    private static boolean isMatchDayOfMonth(ValueMatcher matcher, int dayOfMonth, int month, boolean isLeapYear) {
        return ((matcher instanceof DayOfMonthValueMatcher)
                ? ((DayOfMonthValueMatcher) matcher).match(dayOfMonth, month, isLeapYear)
                : matcher.match(dayOfMonth));
    }

    /**
     * 是否匹配指定的日期时间位置
     *
     * @param matchers 匹配器列表
     * @param index    位置
     * @param value    被匹配的值
     * @return 是否匹配s
     */
    private static boolean isMatch(List<ValueMatcher> matchers, int index, int value) {
        return (matchers.size() <= index) || matchers.get(index).match(value);
    }

    /**
     * 给定时间是否匹配定时任务表达式
     *
     * @param second     秒数，-1表示不匹配此项
     * @param minute     分钟
     * @param hour       小时
     * @param dayOfMonth 天
     * @param month      月
     * @param dayOfWeek  周几
     * @param year       年
     * @return 如果匹配返回 {@code true}, 否则返回 {@code false}
     */
    public boolean match(int second, int minute, int hour, int dayOfMonth, int month, int dayOfWeek, int year) {
        for (int i = 0; i < matcherSize; i++) {
            boolean eval = ((second < 0) || secondMatchers.get(i).match(second)) // 匹配秒（非秒匹配模式下始终返回true）
                    && minuteMatchers.get(i).match(minute)// 匹配分
                    && hourMatchers.get(i).match(hour)// 匹配时
                    && isMatchDayOfMonth(dayOfMonthMatchers.get(i), dayOfMonth, month, Year.isLeap(year))// 匹配日
                    && monthMatchers.get(i).match(month) // 匹配月
                    && dayOfWeekMatchers.get(i).match(dayOfWeek)// 匹配周
                    && isMatch(yearMatchers, i, year);// 匹配年
            if (eval) {
                return true;
            }
        }
        return false;
    }

}
