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
package org.miaixz.bus.core.center.date.format.parser;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * 全局正则日期解析器
 * 通过使用预定义或自定义的正则规则，解析日期字符串
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GlobalRegexDateParser {

    private static final String yearRegex = "(?<year>\\d{2,4})";
    private static final String monthRegex = "(?<month>\\w{3,9})";
    private static final String dayRegex = "(?<day>\\d{1,2})(?:th)?";
    // 周的正则，匹配：Mon, Tue, Wed, Thu, Fri, Sat, Sun，或 Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    // 日期中一般出现在头部，可选
    private static final String weekRegexWithSuff = "(?<week>[mwfts][oeruha][ndieut](\\w{3,6})?\\W+)?";
    // hh:mm:ss.SSSSZ hh:mm:ss.SSSS hh:mm:ss hh:mm
    private static final String timeRegexWithPre = "(" +
            "\\W+(at\\s)?(?<hour>\\d{1,2})" +
            ":(?<minute>\\d{1,2})" +
            "(:(?<second>\\d{1,2}))?" +
            "(?:[.,](?<ns>\\d{1,9}))?(?<zero>z)?" +
            "(\\s?(?<m>[ap]m))?" +
            ")?";
    // 时区，类似： +08:00 +0800 +08，可选
    private static final String zoneOffsetRegexWithPre = "(\\s?(?<zoneOffset>[-+]\\d{1,2}:?(?:\\d{2})?))?";
    // 时区名称，类似： CST UTC (CST)，可选
    private static final String zoneNameRegexWithPre = "(\\s[(]?(?<zoneName>[a-z ]+)[)]?)?";
    private static final String zoneNameIgnoreRegexWithPre = "(\\s[(]?(?<zoneNameIgnore>[a-z ]+)[)]?)?";

    private static final RegexDateParser PARSER;

    static {
        // 月开头，类似：May 8
        final String dateRegexMonthFirst = monthRegex + "\\W+" + dayRegex;
        // 日开头，类似：02-Jan
        final String dateRegexDayFirst = dayRegex + "\\W+" + monthRegex;
        // 时区拼接，类似：
        // GMT+0100 (GMT Daylight Time)
        // +0200 (CEST)
        // GMT+0100
        // MST
        final String zoneRegex = zoneNameRegexWithPre + zoneOffsetRegexWithPre + zoneNameIgnoreRegexWithPre;

        PARSER = RegexDateParser.of(
                // 年开头

                //【周月日年时】类似：May 8, 2009，时间部分可选，类似：5:57:51，5:57:51 +08:00
                weekRegexWithSuff + dateRegexMonthFirst + "\\W+" + yearRegex + timeRegexWithPre + zoneRegex,
                //【周月日时年】类似：Mon Jan 2 15:04:05 MST 2006
                weekRegexWithSuff + dateRegexMonthFirst + timeRegexWithPre + zoneRegex + "\\W+" + yearRegex,
                //【周日月年时】类似：Monday, 02-Jan-06 15:04:05 MST
                weekRegexWithSuff + dateRegexDayFirst + "\\W+" + yearRegex + timeRegexWithPre + zoneRegex
        );
    }

    /**
     * 解析日期，此方法线程安全
     *
     * @param source 日期字符串
     * @return 日期
     */
    public static Date parse(final CharSequence source) {
        return PARSER.parse(source);
    }

    /**
     * 新增自定义日期正则
     *
     * @param regex 日期正则
     */
    synchronized public static void registerRegex(final String regex) {
        PARSER.addRegex(regex);
    }

    /**
     * 新增自定义日期正则
     *
     * @param pattern 日期正则
     */
    synchronized public static void registerPattern(final Pattern pattern) {
        PARSER.addPattern(pattern);
    }

}