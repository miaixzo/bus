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
 * A date parser that uses a list of regular expressions to parse date strings. It iterates through the defined
 * patterns, and the first one that matches is used to extract date components.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852256787361L;

    /**
     * An array of multipliers for parsing nanoseconds of varying lengths.
     */
    private static final int[] NSS = { 100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1 };

    /**
     * A regular expression for matching timezone offsets (e.g., +08:00, -0700).
     */
    private static final Pattern ZONE_OFFSET_PATTERN = Pattern.compile("[-+]\\d{1,2}:?(?:\\d{2})?");

    /**
     * A {@link WordTree} for efficiently matching timezone names (e.g., 'Asia/Shanghai').
     */
    private static final WordTree ZONE_TREE = WordTree.of(TimeZone.getAvailableIDs());

    /**
     * A list of regular expression patterns to try for parsing.
     */
    private final List<Pattern> patterns;

    /**
     * Whether to prefer parsing ambiguous dates (e.g., 01/02/2023) as month/day first.
     */
    private boolean preferMonthFirst;

    /**
     * Constructs a new {@code RegexDateParser}.
     *
     * @param patterns A list of regular expression patterns.
     */
    public RegexDateParser(final List<Pattern> patterns) {
        this.patterns = patterns;
    }

    /**
     * Creates a new {@code RegexDateParser} from one or more regular expression strings (case-insensitive).
     *
     * @param regexes The regular expression strings.
     * @return A new {@code RegexDateParser} instance.
     */
    public static RegexDateParser of(final String... regexes) {
        final List<Pattern> patternList = new ArrayList<>(regexes.length);
        for (final String regex : regexes) {
            patternList.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
        return new RegexDateParser(patternList);
    }

    /**
     * Creates a new {@code RegexDateParser} from one or more {@link Pattern} objects.
     *
     * @param patterns The regular expression patterns.
     * @return A new {@code RegexDateParser} instance.
     */
    public static RegexDateParser of(final Pattern... patterns) {
        return new RegexDateParser(ListKit.of(patterns));
    }

    /**
     * Parses a purely numeric date string based on its length.
     *
     * @param number      The numeric string.
     * @param dateBuilder The {@link DateBuilder} to populate.
     */
    private static void parseNumberDate(final String number, final DateBuilder dateBuilder) {
        final int length = number.length();
        switch (length) {
            case 4 -> // yyyy
                    dateBuilder.setYear(Integer.parseInt(number));
            case 6 -> { // yyyyMM
                dateBuilder.setYear(parseInt(number, 0, 4));
                dateBuilder.setMonth(parseInt(number, 4, 6));
            }
            case 8 -> { // yyyyMMdd
                dateBuilder.setYear(parseInt(number, 0, 4));
                dateBuilder.setMonth(parseInt(number, 4, 6));
                dateBuilder.setDay(parseInt(number, 6, 8));
            }
            case 14 -> { // yyyyMMddhhmmss
                dateBuilder.setYear(parseInt(number, 0, 4));
                dateBuilder.setMonth(parseInt(number, 4, 6));
                dateBuilder.setDay(parseInt(number, 6, 8));
                dateBuilder.setHour(parseInt(number, 8, 10));
                dateBuilder.setMinute(parseInt(number, 10, 12));
                dateBuilder.setSecond(parseInt(number, 12, 14));
            }
            case 10 -> // unixtime(10)
                    dateBuilder.setUnixsecond(parseLong(number));
            case 13 -> // millisecond(13)
                    dateBuilder.setMillisecond(parseLong(number));
            case 16 -> { // microsecond(16)
                dateBuilder.setUnixsecond(parseLong(number.substring(0, 10)));
                dateBuilder.setNanosecond(parseInt(number, 10, 16));
            }
            case 19 -> { // nanosecond(19)
                dateBuilder.setUnixsecond(parseLong(number.substring(0, 10)));
                dateBuilder.setNanosecond(parseInt(number, 10, 19));
            }
        }
    }

    /**
     * Parses a 2 or 4 digit year string. 2-digit years are interpreted relative to the current century.
     *
     * @param year The year string.
     * @return The parsed year.
     * @throws DateException if the year is invalid.
     */
    private static int parseYear(final String year) {
        final int length = year.length();
        return switch (length) {
            case 4 -> Integer.parseInt(year);
            case 2 -> {
                final int num = Integer.parseInt(year);
                yield (num > 50 ? 1900 : 2000) + num;
            }
            default -> throw new DateException("Invalid year: [{}]", year);
        };
    }

    /**
     * Parses a string that could represent a day or a month (e.g., in "dd/mm" or "mm/dd" format).
     *
     * @param dayOrMonth       The string to parse.
     * @param dateBuilder      The {@link DateBuilder} to populate.
     * @param preferMonthFirst If true, ambiguous formats like "01/02" will be treated as month/day.
     */
    private static void parseDayOrMonth(
            final String dayOrMonth,
            final DateBuilder dateBuilder,
            final boolean preferMonthFirst) {
        final char next = dayOrMonth.charAt(1);
        final int a, b;
        if (!Character.isDigit(next)) { // d/m
            a = parseInt(dayOrMonth, 0, 1);
            b = parseInt(dayOrMonth, 2, dayOrMonth.length());
        } else { // dd/mm
            a = parseInt(dayOrMonth, 0, 2);
            b = parseInt(dayOrMonth, 3, dayOrMonth.length());
        }

        if (a > 31 || b > 31 || a == 0 || b == 0 || (a > 12 && b > 12)) {
            throw new DateException("Invalid DayOrMonth: {}", dayOrMonth);
        }

        if (b > 12 || (preferMonthFirst && a <= 12)) {
            dateBuilder.setMonth(a);
            dateBuilder.setDay(b);
        } else {
            dateBuilder.setMonth(b);
            dateBuilder.setDay(a);
        }
    }

    /**
     * Parses a month string, which can be a number (1-12) or a name (e.g., "January", "Jan").
     *
     * @param month The month string.
     * @return The parsed month number (1-12).
     * @throws DateException if the month is invalid.
     */
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

    /**
     * Parses a week string (e.g., "Monday", "Mon").
     *
     * @param week The week string.
     * @return The parsed week number (1-7).
     */
    private static int parseWeek(final String week) {
        return Week.of(week).getIsoValue();
    }

    /**
     * Parses a number string, ensuring it falls within a specified range.
     *
     * @param numberStr  The number string.
     * @param minInclude The minimum allowed value (inclusive).
     * @param maxInclude The maximum allowed value (inclusive).
     * @return The parsed integer.
     * @throws DateException if the number is invalid or out of range.
     */
    private static int parseNumberLimit(final String numberStr, final int minInclude, final int maxInclude) {
        try {
            final int numberInt = Integer.parseInt(numberStr);
            if (numberInt >= minInclude && numberInt <= maxInclude) {
                return numberInt;
            }
        } catch (final NumberFormatException ignored) {
            // fall through to throw exception
        }
        throw new DateException("Invalid number: [{}]", numberStr);
    }

    /**
     * Parses a string as a long.
     *
     * @param numberStr The number string.
     * @return The parsed long.
     * @throws DateException if the string is not a valid long.
     */
    private static long parseLong(final String numberStr) {
        try {
            return Long.parseLong(numberStr);
        } catch (final NumberFormatException ignored) {
            // fall through to throw exception
        }
        throw new DateException("Invalid long: [{}]", numberStr);
    }

    /**
     * Parses a substring as an integer.
     *
     * @param numberStr The source string.
     * @param from      The starting index.
     * @param to        The ending index.
     * @return The parsed integer.
     * @throws DateException if the substring is not a valid integer.
     */
    private static int parseInt(final String numberStr, final int from, final int to) {
        try {
            return Integer.parseInt(numberStr.substring(from, to));
        } catch (final NumberFormatException ignored) {
            // fall through to throw exception
        }
        throw new DateException("Invalid int: [{}]", numberStr);
    }

    /**
     * Parses a nanosecond string.
     *
     * @param ns The nanosecond string.
     * @return The parsed nanosecond value.
     */
    private static int parseNano(final String ns) {
        return NSS[ns.length() - 1] * Integer.parseInt(ns);
    }

    /**
     * Parses a timezone string, which could be an offset or a name.
     *
     * @param zone        The timezone string.
     * @param dateBuilder The {@link DateBuilder} to populate.
     */
    private static void parseZone(final String zone, final DateBuilder dateBuilder) {
        final String zoneOffset = PatternKit.getGroup0(ZONE_OFFSET_PATTERN, zone);
        if (StringKit.isNotBlank(zoneOffset)) {
            dateBuilder.setFlag(true);
            dateBuilder.setZoneOffset(parseZoneOffset(zoneOffset));
            return;
        }
        final String zoneName = ZONE_TREE.match(zone);
        if (StringKit.isNotBlank(zoneName)) {
            dateBuilder.setFlag(true);
            dateBuilder.setZone(TimeZone.getTimeZone(zoneName));
        }
    }

    /**
     * Parses a timezone offset string (e.g., '+08', '+8:00', '+08:00', '+0800').
     *
     * @param zoneOffset The timezone offset string.
     * @return The offset in minutes.
     */
    private static int parseZoneOffset(final String zoneOffset) {
        int from = 0;
        final int to = zoneOffset.length();
        final boolean neg = '-' == zoneOffset.charAt(from);
        from++;
        final int hour;
        if (from + 2 <= to && Character.isDigit(zoneOffset.charAt(from + 1))) {
            hour = parseInt(zoneOffset, from, from + 2);
            from += 2;
        } else {
            hour = parseInt(zoneOffset, from, from + 1);
            from += 1;
        }
        if (from + 3 <= to && zoneOffset.charAt(from) == ':') {
            from++;
        }
        int minute = 0;
        if (from + 2 <= to) {
            minute = parseInt(zoneOffset, from, from + 2);
        }
        return (hour * 60 + minute) * (neg ? -1 : 1);
    }

    /**
     * Sets whether to prefer parsing ambiguous d/M formats as month/day.
     *
     * @param preferMonthFirst If true, parse as mm/dd; otherwise, dd/mm.
     */
    public void setPreferMonthFirst(final boolean preferMonthFirst) {
        this.preferMonthFirst = preferMonthFirst;
    }

    /**
     * Adds a custom regular expression for parsing (case-insensitive).
     *
     * @param regex The regular expression string.
     * @return this {@code RegexDateParser} instance for chaining.
     */
    public RegexDateParser addRegex(final String regex) {
        return addPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }

    /**
     * Adds a custom {@link Pattern} for parsing.
     *
     * @param pattern The regular expression pattern.
     * @return this {@code RegexDateParser} instance for chaining.
     */
    public RegexDateParser addPattern(final Pattern pattern) {
        this.patterns.add(pattern);
        return this;
    }

    /**
     * Parses a date string using the configured regular expressions.
     *
     * @param source The date string to parse.
     * @return The parsed {@link Date} object.
     * @throws DateException if no configured pattern matches the string.
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        Assert.notBlank(source, "Date source must not be blank!");
        return parseToBuilder(source).toDate();
    }

    /**
     * Parses a date string into a {@link DateBuilder}.
     *
     * @param source The date string.
     * @return The populated {@link DateBuilder}.
     * @throws DateException if no pattern matches.
     */
    private DateBuilder parseToBuilder(final CharSequence source) throws DateException {
        final DateBuilder dateBuilder = DateBuilder.of();
        for (final Pattern pattern : this.patterns) {
            Matcher matcher = pattern.matcher(source);
            if (matcher.matches()) {
                parse(matcher, dateBuilder);
                return dateBuilder;
            }
        }
        throw new DateException("No valid pattern found for date string: [{}]", source);
    }

    /**
     * Parses the named groups from a successful regex match and populates a {@link DateBuilder}.
     *
     * @param matcher     The successful regex matcher.
     * @param dateBuilder The date builder to populate.
     * @throws DateException if parsing a group fails.
     */
    private void parse(final Matcher matcher, final DateBuilder dateBuilder) throws DateException {
        final String number = PatternKit.group(matcher, "number");
        if (StringKit.isNotEmpty(number)) {
            parseNumberDate(number, dateBuilder);
            return;
        }
        final String millisecond = PatternKit.group(matcher, "millisecond");
        if (StringKit.isNotEmpty(millisecond)) {
            dateBuilder.setMillisecond(parseLong(millisecond));
            return;
        }

        Optional.ofNullable(PatternKit.group(matcher, "year")).ifPresent(year -> dateBuilder.setYear(parseYear(year)));
        Optional.ofNullable(PatternKit.group(matcher, "dayOrMonth"))
                .ifPresent(dayOrMonth -> parseDayOrMonth(dayOrMonth, dateBuilder, preferMonthFirst));
        Optional.ofNullable(PatternKit.group(matcher, "month"))
                .ifPresent(month -> dateBuilder.setMonth(parseMonth(month)));
        Optional.ofNullable(PatternKit.group(matcher, "week")).ifPresent(week -> dateBuilder.setWeek(parseWeek(week)));
        Optional.ofNullable(PatternKit.group(matcher, "day"))
                .ifPresent(day -> dateBuilder.setDay(parseNumberLimit(day, 1, 31)));
        Optional.ofNullable(PatternKit.group(matcher, "hour"))
                .ifPresent(hour -> dateBuilder.setHour(parseNumberLimit(hour, 0, 23)));
        Optional.ofNullable(PatternKit.group(matcher, "minute"))
                .ifPresent(minute -> dateBuilder.setMinute(parseNumberLimit(minute, 0, 59)));
        Optional.ofNullable(PatternKit.group(matcher, "second"))
                .ifPresent(second -> dateBuilder.setSecond(parseNumberLimit(second, 0, 59)));
        Optional.ofNullable(PatternKit.group(matcher, "nanosecond"))
                .ifPresent(ns -> dateBuilder.setNanosecond(parseNano(ns)));
        Optional.ofNullable(PatternKit.group(matcher, "m")).ifPresent(m -> {
            if (CharKit.equals('p', m.charAt(0), true)) {
                dateBuilder.setPm(true);
            } else {
                dateBuilder.setAm(true);
            }
        });

        Optional.ofNullable(PatternKit.group(matcher, "zero")).ifPresent(zero -> {
            dateBuilder.setFlag(true);
            dateBuilder.setZoneOffset(0);
        });

        Optional.ofNullable(PatternKit.group(matcher, "zone")).ifPresent(zone -> parseZone(zone, dateBuilder));
        Optional.ofNullable(PatternKit.group(matcher, "zoneOffset")).ifPresent(zoneOffset -> {
            dateBuilder.setFlag(true);
            dateBuilder.setZoneOffset(parseZoneOffset(zoneOffset));
        });

        Optional.ofNullable(PatternKit.group(matcher, "unixsecond"))
                .ifPresent((unixsecond) -> dateBuilder.setUnixsecond(parseLong(unixsecond)));
    }

}
