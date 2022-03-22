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
import org.aoju.bus.cron.pattern.matcher.MatcherTable;

import java.util.List;

/**
 * 定时任务表达式解析器，用于将表达式字符串解析为{@link MatcherTable}
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class CronPatternParser {

    private static final ValueParser SECOND_VALUE_PARSER = new SecondValueParser();
    private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();
    private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();
    private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();
    private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();
    private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();
    private static final ValueParser YEAR_VALUE_PARSER = new YearValueParser();
    private MatcherTable matcherTable;

    /**
     * 创建表达式解析器
     *
     * @return CronPatternParser
     */
    public static CronPatternParser create() {
        return new CronPatternParser();
    }

    /**
     * 解析表达式到匹配表中
     *
     * @param cronPattern 复合表达式
     * @return {@link MatcherTable}
     */
    public MatcherTable parse(String cronPattern) {
        parseGroupPattern(cronPattern);
        return this.matcherTable;
    }

    /**
     * 解析复合任务表达式，格式为：
     * <pre>
     *     cronA | cronB | ...
     * </pre>
     *
     * @param groupPattern 复合表达式
     */
    private void parseGroupPattern(String groupPattern) {
        final List<String> patternList = StringKit.split(groupPattern, '|');
        matcherTable = new MatcherTable(patternList.size());
        for (String pattern : patternList) {
            parseSinglePattern(pattern);
        }
    }

    /**
     * 解析单一定时任务表达式
     *
     * @param pattern 表达式
     */
    private void parseSinglePattern(String pattern) {
        final String[] parts = pattern.split("\\s");

        int offset = 0;// 偏移量用于兼容Quartz表达式，当表达式有6或7项时，第一项为秒
        if (parts.length == 6 || parts.length == 7) {
            offset = 1;
        } else if (parts.length != 5) {
            throw new CrontabException("Pattern [{}] is invalid, it must be 5-7 parts!", pattern);
        }

        // 秒，如果不支持秒的表达式，则第一位按照表达式生成时间的秒数赋值，表示整分匹配
        final String secondPart = (1 == offset) ? parts[0] : String.valueOf(DateKit.date().second());
        parseToTable(SECOND_VALUE_PARSER, secondPart);

        // 分
        parseToTable(MINUTE_VALUE_PARSER, parts[offset]);

        // 时
        parseToTable(HOUR_VALUE_PARSER, parts[1 + offset]);

        // 天
        parseToTable(DAY_OF_MONTH_VALUE_PARSER, parts[2 + offset]);

        // 月
        parseToTable(MONTH_VALUE_PARSER, parts[3 + offset]);

        // 周
        parseToTable(DAY_OF_WEEK_VALUE_PARSER, parts[4 + offset]);

        // 年
        if (parts.length == 7) {// 支持年的表达式
            parseToTable(YEAR_VALUE_PARSER, parts[6]);
        } else {// 不支持年的表达式，全部匹配
            matcherTable.yearMatchers.add(new AlwaysTrueValueMatcher());
        }
    }

    /**
     * 将表达式解析后加入到{@link #matcherTable}中
     *
     * @param valueParser 表达式解析器
     * @param patternPart 表达式部分
     */
    private void parseToTable(ValueParser valueParser, String patternPart) {
        valueParser.parseTo(this.matcherTable, patternPart);
    }

}
