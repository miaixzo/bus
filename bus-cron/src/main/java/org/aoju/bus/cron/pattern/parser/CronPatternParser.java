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
package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.CrontabException;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.cron.pattern.matcher.AlwaysTrueValueMatcher;
import org.aoju.bus.cron.pattern.matcher.DateTimeMatcher;
import org.aoju.bus.cron.pattern.matcher.MatcherTable;
import org.aoju.bus.cron.pattern.matcher.ValueMatcher;

import java.util.List;

/**
 * 定时任务表达式解析器，用于将表达式字符串解析为{@link MatcherTable}
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class CronPatternParser {

    private static final ValueParser SECOND_VALUE_PARSER = new SecondValueParser();
    private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();
    private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();
    private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();
    private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();
    private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();
    private static final ValueParser YEAR_VALUE_PARSER = new YearValueParser();

    /**
     * 解析表达式到匹配表中
     *
     * @param cronPattern 复合表达式
     * @return {@link MatcherTable}
     */
    public static MatcherTable parse(String cronPattern) {
        return parseGroupPattern(cronPattern);
    }

    /**
     * 解析复合任务表达式，格式为：
     * <pre>
     *     cronA | cronB | ...
     * </pre>
     *
     * @param groupPattern 复合表达式
     * @return {@link MatcherTable}
     */
    private static MatcherTable parseGroupPattern(String groupPattern) {
        final List<String> patternList = StringKit.split(groupPattern, '|');
        final MatcherTable matcherTable = new MatcherTable(patternList.size());
        for (String pattern : patternList) {
            matcherTable.matchers.add(parseSinglePattern(pattern));
        }
        return matcherTable;
    }

    /**
     * 解析单一定时任务表达式
     *
     * @param pattern 表达式
     * @return {@link DateTimeMatcher}
     */
    private static DateTimeMatcher parseSinglePattern(String pattern) {
        final String[] parts = pattern.split("\\s");

        int offset = 0;// 偏移量用于兼容Quartz表达式，当表达式有6或7项时，第一项为秒
        if (parts.length == 6 || parts.length == 7) {
            offset = 1;
        } else if (parts.length != 5) {
            throw new CrontabException("Pattern [{}] is invalid, it must be 5-7 parts!", pattern);
        }

        // 秒，如果不支持秒的表达式，则第一位按照表达式生成时间的秒数赋值，表示整分匹配
        final String secondPart = (1 == offset) ? parts[0] : String.valueOf(DateKit.date().second());

        // 年
        ValueMatcher yearMatcher;
        if (parts.length == 7) {// 支持年的表达式
            yearMatcher = YEAR_VALUE_PARSER.parseAsValueMatcher(parts[6]);
        } else {// 不支持年的表达式，全部匹配
            yearMatcher = AlwaysTrueValueMatcher.INSTANCE;
        }

        return new DateTimeMatcher(
                // 秒
                SECOND_VALUE_PARSER.parseAsValueMatcher(secondPart),
                // 分
                MINUTE_VALUE_PARSER.parseAsValueMatcher(parts[offset]),
                // 时
                HOUR_VALUE_PARSER.parseAsValueMatcher(parts[1 + offset]),
                // 天
                DAY_OF_MONTH_VALUE_PARSER.parseAsValueMatcher(parts[2 + offset]),
                // 月
                MONTH_VALUE_PARSER.parseAsValueMatcher(parts[3 + offset]),
                // 周
                DAY_OF_WEEK_VALUE_PARSER.parseAsValueMatcher(parts[4 + offset]),
                // 年
                yearMatcher
        );
    }

}
