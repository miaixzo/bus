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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.builder.DateBuilder;
import org.miaixz.bus.core.center.date.culture.en.Month;
import org.miaixz.bus.core.center.date.culture.en.Week;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.text.dfa.WordTree;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 使用正则列表方式的日期解析器 通过定义若干的日期正则，遍历匹配到给定正则后，按照正则方式解析为日期
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852291957886L;

    private static final int[] NSS = { 100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1 };
    private static final Pattern ZONE_OFFSET_PATTERN = Pattern.compile("[-+]\\d{1,2}:?(?:\\d{2})?");
    private static final WordTree ZONE_TREE = WordTree.of(TimeZone.getAvailableIDs());
    private final List<Pattern> patterns;
    private boolean preferMonthFirst;

    /**
     * 构造
     *
     * @param patterns 正则列表
     */
    public RegexDateParser(final List<Pattern> patterns) {
        this.patterns = patterns;
    }

    /**
     * 根据给定的正则列表
     *
     * @param regexes 正则列表，默认忽略大小写
     * @return this
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
     * @return this
     */
    public static RegexDateParser of(final Pattern... patterns) {
        return new RegexDateParser(ListKit.of(patterns));
    }

    /**
     * 解析纯数字型的日期
     *
     * @param number      纯数字
     * @param dateBuilder {@link DateBuilder}
     */
    private static void parseNumberDate(final String number, final DateBuilder dateBuilder) {
        final int length = number.length();
        switch (length) {
        case 4:
            // yyyy
            dateBuilder.setYear(Integer.parseInt(number));
            break;
        case 6:
            // yyyyMM
            dateBuilder.setYear(parseInt(number, 0, 4));
            dateBuilder.setMonth(parseInt(number, 4, 6));
            break;
        case 8:
            // yyyyMMdd
            dateBuilder.setYear(parseInt(number, 0, 4));
            dateBuilder.setMonth(parseInt(number, 4, 6));
            dateBuilder.setDay(parseInt(number, 6, 8));
            break;
        case 14:
            dateBuilder.setYear(parseInt(number, 0, 4));
            dateBuilder.setMonth(parseInt(number, 4, 6));
            dateBuilder.setDay(parseInt(number, 6, 8));
            dateBuilder.setHour(parseInt(number, 8, 10));
            dateBuilder.setMinute(parseInt(number, 10, 12));
            dateBuilder.setSecond(parseInt(number, 12, 14));
            break;
        case 10:
            // unixtime(10)
            dateBuilder.setUnixsecond(parseLong(number));
            break;
        case 13:
            // millisecond(13)
            dateBuilder.setMillisecond(parseLong(number));
            break;
        case 16:
            // microsecond(16)
            dateBuilder.setUnixsecond(parseLong(number.substring(0, 10)));
            dateBuilder.setNanosecond(parseInt(number, 10, 16));
            break;
        case 19:
            // nanosecond(19)
            dateBuilder.setUnixsecond(parseLong(number.substring(0, 10)));
            dateBuilder.setNanosecond(parseInt(number, 10, 19));
            break;
        }
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

    /**
     * 解析日期中的日或月，类似于dd/mm或mm/dd格式
     *
     * @param dayOrMonth       日期中的日或月
     * @param dateBuilder      {@link DateBuilder}
     * @param preferMonthFirst 是否月份在前
     */
    private static void parseDayOrMonth(final String dayOrMonth, final DateBuilder dateBuilder,
            final boolean preferMonthFirst) {
        final char next = dayOrMonth.charAt(1);
        final int a;
        final int b;
        if (next < '0' || next > '9') {
            // d/m
            a = parseInt(dayOrMonth, 0, 1);
            b = parseInt(dayOrMonth, 2, dayOrMonth.length());
        } else {
            // dd/mm
            a = parseInt(dayOrMonth, 0, 2);
            b = parseInt(dayOrMonth, 3, dayOrMonth.length());
        }

        if (a > 31 || b > 31 || a == 0 || b == 0 || (a > 12 && b > 12)) {
            throw new DateException("Invalid DayOrMonth : {}", dayOrMonth);
        }

        if (b > 12 || (preferMonthFirst && a <= 12)) {
            dateBuilder.setMonth(a);
            dateBuilder.setDay(b);
        } else {
            dateBuilder.setMonth(b);
            dateBuilder.setDay(a);
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
     * 解析时区，包括时区偏移和时区名称
     *
     * @param zone        时区
     * @param dateBuilder 日期时间对象
     */
    private static void parseZone(final String zone, final DateBuilder dateBuilder) {
        // 检查是否直接定义了时区偏移
        final String zoneOffset = PatternKit.getGroup0(ZONE_OFFSET_PATTERN, zone);
        if (StringKit.isNotBlank(zoneOffset)) {
            dateBuilder.setFlag(true);
            dateBuilder.setZoneOffset(parseZoneOffset(zoneOffset));
            return;
        }

        // 检查是否定义了时区名称
        final String zoneName = ZONE_TREE.match(zone);
        if (StringKit.isNotBlank(zoneName)) {
            dateBuilder.setFlag(true);
            dateBuilder.setZone(TimeZone.getTimeZone(zoneName));
        }
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

    /**
     * 当用户传入的月和日无法判定默认位置时，设置默认的日期格式为dd/mm还是mm/dd
     *
     * @param preferMonthFirst {@code true}默认为mm/dd，否则dd/mm
     */
    public void setPreferMonthFirst(final boolean preferMonthFirst) {
        this.preferMonthFirst = preferMonthFirst;
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
        this.patterns.add(pattern);
        return this;
    }

    @Override
    public Date parse(final CharSequence source) throws DateException {
        Assert.notBlank(source, "Date source must be not blank!");
        return parseToBuilder(source).toDate();
    }

    /**
     * 解析日期
     *
     * @param source 日期字符串
     * @return DateBuilder
     * @throws DateException 日期解析异常
     */
    private DateBuilder parseToBuilder(final CharSequence source) throws DateException {
        final DateBuilder dateBuilder = DateBuilder.of();
        Matcher matcher;
        for (final Pattern pattern : this.patterns) {
            matcher = pattern.matcher(source);
            if (matcher.matches()) {
                parse(matcher, dateBuilder);
                return dateBuilder;
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
    private void parse(final Matcher matcher, final DateBuilder dateBuilder) throws DateException {

        // 纯数字格式
        final String number = PatternKit.group(matcher, "number");
        if (StringKit.isNotEmpty(number)) {
            parseNumberDate(number, dateBuilder);
            return;
        }

        // 毫秒时间戳
        final String millisecond = PatternKit.group(matcher, "millisecond");
        if (StringKit.isNotEmpty(millisecond)) {
            dateBuilder.setMillisecond(parseLong(millisecond));
            return;
        }

        // year
        Optional.ofNullable(PatternKit.group(matcher, "year"))
                .ifPresent((year) -> dateBuilder.setYear(parseYear(year)));
        // dayOrMonth, dd/mm or mm/dd
        Optional.ofNullable(PatternKit.group(matcher, "dayOrMonth"))
                .ifPresent((dayOrMonth) -> parseDayOrMonth(dayOrMonth, dateBuilder, preferMonthFirst));
        // month
        Optional.ofNullable(PatternKit.group(matcher, "month"))
                .ifPresent((month) -> dateBuilder.setMonth(parseMonth(month)));
        // week
        Optional.ofNullable(PatternKit.group(matcher, "week"))
                .ifPresent((week) -> dateBuilder.setWeek(parseWeek(week)));
        // day
        Optional.ofNullable(PatternKit.group(matcher, "day"))
                .ifPresent((day) -> dateBuilder.setDay(parseNumberLimit(day, 1, 31)));
        // hour
        Optional.ofNullable(PatternKit.group(matcher, "hour"))
                .ifPresent((hour) -> dateBuilder.setHour(parseNumberLimit(hour, 0, 23)));
        // minute
        Optional.ofNullable(PatternKit.group(matcher, "minute"))
                .ifPresent((minute) -> dateBuilder.setMinute(parseNumberLimit(minute, 0, 59)));
        // second
        Optional.ofNullable(PatternKit.group(matcher, "second"))
                .ifPresent((second) -> dateBuilder.setSecond(parseNumberLimit(second, 0, 59)));
        // nanoseconds
        Optional.ofNullable(PatternKit.group(matcher, "nanosecond"))
                .ifPresent((ns) -> dateBuilder.setNanosecond(parseNano(ns)));
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
            dateBuilder.setZoneOffset(0);
        });

        // zone（包括可时区名称、时区偏移等信息，综合解析）
        Optional.ofNullable(PatternKit.group(matcher, "zone")).ifPresent((zoneOffset) -> {
            parseZone(zoneOffset, dateBuilder);
        });

        // zone offset
        Optional.ofNullable(PatternKit.group(matcher, "zoneOffset")).ifPresent((zoneOffset) -> {
            dateBuilder.setFlag(true);
            dateBuilder.setZoneOffset(parseZoneOffset(zoneOffset));
        });

        // unix时间戳，可能有NS
        Optional.ofNullable(PatternKit.group(matcher, "unixsecond")).ifPresent((unixsecond) -> {
            dateBuilder.setUnixsecond(parseLong(unixsecond));
        });
    }

}
