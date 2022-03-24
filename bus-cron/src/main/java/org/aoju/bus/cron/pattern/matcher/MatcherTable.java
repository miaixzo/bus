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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间匹配表，用于存放定时任务表达式解析后的结构信息
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class MatcherTable {

    /**
     * 秒字段匹配列表
     */
    public final List<DateTimeMatcher> matchers;

    /**
     * 构造
     *
     * @param size 表达式个数，用于表示复合表达式中单个表达式个数
     */
    public MatcherTable(int size) {
        matchers = new ArrayList<>(size);
    }

    public LocalDateTime nextMatchAfter(int second, int minute, int hour, int dayOfMonth, int month, int dayOfWeek, int year) {
        return null;
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
        for (DateTimeMatcher matcher : matchers) {
            if (matcher.match(second, minute, hour, dayOfMonth, month, dayOfWeek, year)) {
                return true;
            }
        }
        return false;
    }

}
