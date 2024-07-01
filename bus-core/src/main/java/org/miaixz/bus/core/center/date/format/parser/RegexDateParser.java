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

import org.miaixz.bus.core.center.date.builder.DateBuilder;
import org.miaixz.bus.core.center.date.culture.en.Month;
import org.miaixz.bus.core.center.date.culture.en.Week;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用正则列表方式的日期解析器
 * 通过定义若干的日期正则，遍历匹配到给定正则后，按照正则方式解析为日期
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexDateParser implements DateParser, Serializable {

    private static final long serialVersionUID = -1L;

    private static final int[] NSS = {100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1};


    private final List<Pattern> pattern;

    /**
     * 构造
     *
     * @param pattern 正则表达式
     */
    public RegexDateParser(final List<Pattern> pattern) {
        this.pattern = pattern;
    }

    /**
     * 根据给定的正则列表
     *
     * @param regexes 正则列表，默认忽略大小写
     * @return RegexListDateParser
     */
    public static RegexDateParser of(final String... regexes) {
        final List<Pattern> patternList = new ArrayList<>(regexes.length);
        for (final String regex : regexes) {
            patternList.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
        return new RegexDateParser(patternList);
    }

    /**
     * 根据给定的正则列表
     *
     * @param patterns 正则列表
     * @return {@link RegexDateParser}
     */
    public static RegexDateParser of(final Pattern... patterns) {
        return new RegexDateParser(ListKit.of(patterns));
    }

    /**
     * 新增自定义日期正则
     *
     * @param regex 日期正则
     * @return this
     */
    public RegexDateParser addRegex(final String regex) {
        // 日期正则忽略大小写
        return addPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * 新增自定义日期正则
     *
     * @param pattern 日期正则
     * @return this
     */
    public RegexDateParser addPattern(final Pattern pattern) {
        this.pattern.add(pattern);
        return this;
    }

    @Override
    public Date parse(final CharSequence source) throws DateException {
        final DateBuilder dateBuilder = DateBuilder.of();
        Matcher matcher;
        for (final Pattern pattern : this.pattern) {
            matcher = pattern.matcher(source);
            if (matcher.matches()) {
                parse(matcher, dateBuilder);
                return dateBuilder.toDate();
            }
        }

        throw new DateException("No valid pattern for date string: [{}]", source);
    }

    /**
     * 解析日期
     *
     * @param matcher 正则匹配器
     * @throws DateException 日期解析异常
     */
    private static void parse(final Matcher matcher, final DateBuilder dateBuilder) throws DateException {
        // 毫秒时间戳
        final String millisecond = PatternKit.group(matcher, "millisecond");
        if (StringKit.isNotEmpty(millisecond)) {
            dateBuilder.setMillisecond(parseLong(millisecond));
            return;
        }

        // year
        Optional.ofNullable(PatternKit.group(matcher, "year")).ifPresent((year) -> dateBuilder.setYear(parseYear(year)));
        // month
        Optional.ofNullable(PatternKit.group(matcher, "month")).ifPresent((month) -> dateBuilder.setMonth(parseMonth(month)));
        // week
        Optional.ofNullable(PatternKit.group(matcher, "week")).ifPresent((week) -> dateBuilder.setWeek(parseWeek(week)));
        // day
        Optional.ofNullable(PatternKit.group(matcher, "day")).ifPresent((day) -> dateBuilder.setDay(parseNumberLimit(day, 1, 31)));
        // hour
        Optional.ofNullable(PatternKit.group(matcher, "hour")).ifPresent((hour) -> dateBuilder.setHour(parseNumberLimit(hour, 0, 23)));
        // minute
        Optional.ofNullable(PatternKit.group(matcher, "minute")).ifPresent((minute) -> dateBuilder.setMinute(parseNumberLimit(minute, 0, 59)));
        // second
        Optional.ofNullable(PatternKit.group(matcher, "second")).ifPresent((second) -> dateBuilder.setSecond(parseNumberLimit(second, 0, 59)));
        // ns
        Optional.ofNullable(PatternKit.group(matcher, "ns")).ifPresent((ns) -> dateBuilder.setNanosecond(parseNano(ns)));
        // am or pm
        Optional.ofNullable(PatternKit.group(matcher, "m")).ifPresent((m) -> {
            if (CharKit.equals('p', m.charAt(0), true)) {
                dateBuilder.setPm(true);
            } else {
                dateBuilder.setAm(true);
            }
        });

        // zero zone offset
        Optional.ofNullable(PatternKit.group(matcher, "zero")).ifPresent((zero) -> {
            dateBuilder.setFlag(true);
            dateBuilder.setOffset(0);
        });

        // zone offset
        Optional.ofNullable(PatternKit.group(matcher, "zoneOffset")).ifPresent((zoneOffset) -> {
            dateBuilder.setFlag(true);
            dateBuilder.setOffset(parseZoneOffset(zoneOffset));
        });

        // zone name
        Optional.ofNullable(PatternKit.group(matcher, "zoneName")).ifPresent((zoneOffset) -> {
            // 暂时不支持解析
        });

        // unix时间戳
        Optional.ofNullable(PatternKit.group(matcher, "unixsecond")).ifPresent((unixsecond) -> {
            dateBuilder.setTimestamp(parseLong(unixsecond));
        });
    }

    private static int parseYear(final String year) {
        final int length = year.length();
        switch (length) {
            case 4:
                return Integer.parseInt(year);
            case 2:
                final int num = Integer.parseInt(year);
                return (num > 50 ? 1900 : 2000) + num;
            default:
                throw new DateException("Invalid year: [{}]", year);
        }
    }

    private static int parseMonth(final String month) {
        try {
            final int monthInt = Integer.parseInt(month);
            if (monthInt > 0 && monthInt < 13) {
                return monthInt;
            }
        } catch (final NumberFormatException e) {
            return Month.of(month).getIsoValue();
        }

        throw new DateException("Invalid month: [{}]", month);
    }

    private static int parseWeek(final String week) {
        return Week.of(week).getIsoValue();
    }

    private static int parseNumberLimit(final String numberStr, final int minInclude, final int maxInclude) {
        try {
            final int monthInt = Integer.parseInt(numberStr);
            if (monthInt >= minInclude && monthInt <= maxInclude) {
                return monthInt;
            }
        } catch (final NumberFormatException ignored) {
        }
        throw new DateException("Invalid number: [{}]", numberStr);
    }

    private static long parseLong(final String numberStr) {
        try {
            return Long.parseLong(numberStr);
        } catch (final NumberFormatException ignored) {
        }
        throw new DateException("Invalid long: [{}]", numberStr);
    }

    private static int parseInt(final String numberStr, final int from, final int to) {
        try {
            return Integer.parseInt(numberStr.substring(from, to));
        } catch (final NumberFormatException ignored) {
        }
        throw new DateException("Invalid int: [{}]", numberStr);
    }

    private static int parseNano(final String ns) {
        return NSS[ns.length() - 1] * Integer.parseInt(ns);
    }

    /**
     * 解析时区偏移，类似于'+08', '+8:00', '+08:00', '+0800'
     *
     * @param zoneOffset 时区偏移
     * @return 偏移量
     */
    private static int parseZoneOffset(final String zoneOffset) {
        int from = 0;
        final int to = zoneOffset.length();
        final boolean neg = '-' == zoneOffset.charAt(from);
        from++;

        // 解析小时
        final int hour;
        if (from + 2 <= to && Character.isDigit(zoneOffset.charAt(from + 1))) {
            hour = parseInt(zoneOffset, from, from + 2);
            from += 2;
        } else {
            hour = parseInt(zoneOffset, from, from + 1);
            from += 1;
        }
        // 可选跳过‘:’
        if (from + 3 <= to && zoneOffset.charAt(from) == ':') {
            from++;
        }
        // 解析分钟
        int minute = 0;
        if (from + 2 <= to) {
            minute = parseInt(zoneOffset, from, from + 2);
        }
        return (hour * 60 + minute) * (neg ? -1 : 1);
    }

}
