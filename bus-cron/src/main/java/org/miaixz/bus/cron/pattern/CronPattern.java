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
package org.miaixz.bus.cron.pattern;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.culture.en.Week;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CompareKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cron.pattern.matcher.PatternMatcher;
import org.miaixz.bus.cron.pattern.parser.PatternParser;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a cron pattern, inspired by the Linux crontab format. The expression is a string of 5, 6, or 7 fields
 * separated by spaces.
 * <p>
 * The standard 5-field format is:
 * <ol>
 * <li><strong>Minute:</strong> 0-59</li>
 * <li><strong>Hour:</strong> 0-23</li>
 * <li><strong>Day of Month:</strong> 1-31. 'L' represents the last day of the month.</li>
 * <li><strong>Month:</strong> 1-12. Case-insensitive aliases are supported (e.g., "jan", "feb", "mar").</li>
 * <li><strong>Day of Week:</strong> 0-6 (Sunday=0 or 7). Case-insensitive aliases are supported (e.g., "sun", "mon").
 * 'L' can be used to mean the last day of the week (Saturday).</li>
 * </ol>
 * <p>
 * For compatibility with Quartz, 6-field and 7-field formats are also supported:
 * <ul>
 * <li>A 6-field expression includes a <strong>Second</strong> field (0-59) at the beginning.</li>
 * <li>A 7-field expression adds a <strong>Year</strong> field (1970-2099) at the end.</li>
 * </ul>
 * <p>
 * A task is triggered when the current time matches the pattern. <strong>Note:</strong> The second field is only
 * matched if the scheduler's `isMatchSecond` property is set to {@code true}. By default, second matching is disabled.
 * <p>
 * Each field can have the following formats:
 * <ul>
 * <li><strong>*</strong>: Matches all possible values for the field.</li>
 * <li><strong>?</strong>: (Question mark) Same as '*', it matches any value. Used for Day of Month or Day of Week.</li>
 * <li><strong>*&#47;2</strong>: An interval. For example, in the minute field, it means every 2 minutes.</li>
 * <li><strong>2-8</strong>: A range. For example, in the minute field, it means minutes 2, 3, 4, 5, 6, 7, and 8.</li>
 * <li><strong>2,3,5,8</strong>: A list of specific values.</li>
 * <li><strong>cronA | cronB</strong>: Multiple cron expressions can be combined with a pipe symbol.</li>
 * </ul>
 * <p>
 * Operator precedence within a field is: Interval (/) > Range (-) > List (,). For example, in {@code 2,3,6/3}, the '/'
 * has higher precedence, so it's interpreted as {@code 2,3,(6/3)}, which is equivalent to {@code 2,3,6}.
 * <p>
 * <strong>Examples:</strong>
 * <ul>
 * <li>{@code 5 * * * *}: At minute 5 of every hour (e.g., 00:05, 01:05).</li>
 * <li>{@code * * * * *}: Every minute.</li>
 * <li>{@code *&#47;2 * * * *}: Every 2 minutes.</li>
 * <li>{@code * 12 * * *}: Every minute during the 12th hour (12:00, 12:01, etc.).</li>
 * <li>{@code 59 11 * * 1,2}: At 11:59 on every Monday and Tuesday.</li>
 * <li>{@code 3-18&#47;5 * * * *}: Every 5 minutes between minute 3 and 18 (e.g., 0:03, 0:08, 0:13, 0:18).</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronPattern {

    private final String pattern;
    private final List<PatternMatcher> matchers;

    /**
     * Constructs a new CronPattern by parsing the given expression.
     *
     * @param pattern The cron expression string.
     */
    public CronPattern(final String pattern) {
        this.pattern = pattern;
        this.matchers = PatternParser.parse(pattern);
    }

    /**
     * Parses a cron expression string into a {@link CronPattern} object.
     *
     * @param pattern The cron expression string.
     * @return A new {@link CronPattern} instance.
     */
    public static CronPattern of(final String pattern) {
        return new CronPattern(pattern);
    }

    /**
     * Finds the first matching date after a given start date.
     *
     * @param pattern The cron pattern.
     * @param start   The start date.
     * @return The first matching {@link Date}.
     */
    public static Date nextDateAfter(final CronPattern pattern, final Date start) {
        return DateKit.date(pattern.nextMatchAfter(Calendar.calendar(start)));
    }

    /**
     * Finds all dates matching the cron expression after a given start date, up to a specified count. The search is
     * limited to the year of the start date.
     *
     * @param patternStr The cron expression string.
     * @param start      The start date.
     * @param count      The maximum number of matching dates to find.
     * @return A list of matching dates.
     */
    public static List<Date> matchedDates(final String patternStr, final Date start, final int count) {
        return matchedDates(patternStr, start, DateKit.endOfYear(start, false), count);
    }

    /**
     * Finds all dates matching the cron expression within a given date range.
     *
     * @param patternStr The cron expression string.
     * @param start      The start of the date range.
     * @param end        The end of the date range.
     * @param count      The maximum number of matching dates to find.
     * @return A list of matching dates.
     */
    public static List<Date> matchedDates(final String patternStr, final Date start, final Date end, final int count) {
        return matchedDates(patternStr, start.getTime(), end.getTime(), count);
    }

    /**
     * Finds all dates matching the cron expression within a given time range.
     *
     * @param patternStr The cron expression string.
     * @param start      The start of the time range in milliseconds.
     * @param end        The end of the time range in milliseconds.
     * @param count      The maximum number of matching dates to find.
     * @return A list of matching dates.
     */
    public static List<Date> matchedDates(final String patternStr, final long start, final long end, final int count) {
        return matchedDates(new CronPattern(patternStr), start, end, count);
    }

    /**
     * Finds all dates matching the cron pattern within a given time range.
     *
     * @param pattern The cron pattern.
     * @param start   The start of the time range in milliseconds.
     * @param end     The end of the time range in milliseconds.
     * @param count   The maximum number of matching dates to find.
     * @return A list of matching dates.
     */
    public static List<Date> matchedDates(
            final CronPattern pattern,
            final long start,
            final long end,
            final int count) {
        Assert.isTrue(start < end, "Start date is later than end !");

        final List<Date> result = new ArrayList<>(count);

        java.util.Calendar calendar = pattern.nextMatchAfter(Calendar.calendar(start));
        while (calendar.getTimeInMillis() < end) {
            result.add(DateKit.date(calendar));
            if (result.size() >= count) {
                break;
            }
            calendar = pattern.nextMatchAfter(calendar);
        }

        return result;
    }

    /**
     * Gets the time fields from a {@link LocalDateTime} object.
     *
     * @param dateTime      The {@link LocalDateTime} instance.
     * @param isMatchSecond Whether to include the second field. If {@code false}, the second value will be -1.
     * @return An array of time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     */
    static int[] getFields(final LocalDateTime dateTime, final boolean isMatchSecond) {
        final int second = isMatchSecond ? dateTime.getSecond() : -1;
        final int minute = dateTime.getMinute();
        final int hour = dateTime.getHour();
        final int dayOfMonth = dateTime.getDayOfMonth();
        final int month = dateTime.getMonthValue(); // Month is 1-based
        final int dayOfWeek = Week.of(dateTime.getDayOfWeek()).getCode() - 1; // Day of week is 0-based (0=Sunday)
        final int year = dateTime.getYear();
        return new int[] { second, minute, hour, dayOfMonth, month, dayOfWeek, year };
    }

    /**
     * Gets the time fields from a {@link java.util.Calendar} object.
     *
     * @param calendar      The {@link java.util.Calendar} instance.
     * @param isMatchSecond Whether to include the second field. If {@code false}, the second value will be -1.
     * @return An array of time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     */
    static int[] getFields(final java.util.Calendar calendar, final boolean isMatchSecond) {
        final int second = isMatchSecond ? calendar.get(java.util.Calendar.SECOND) : -1;
        final int minute = calendar.get(java.util.Calendar.MINUTE);
        final int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        final int dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        final int monthBase1 = calendar.get(java.util.Calendar.MONTH) + 1; // Month is 1-based
        final int dayOfWeekBase0 = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1; // Day of week is 0-based
        // (0=Sunday)
        final int year = calendar.get(java.util.Calendar.YEAR);
        return new int[] { second, minute, hour, dayOfMonth, monthBase1, dayOfWeekBase0, year };
    }

    /**
     * Checks if a given time matches the cron expression.
     *
     * @param millis        The time in milliseconds.
     * @param isMatchSecond Whether to match the second field.
     * @return {@code true} if the time matches, {@code false} otherwise.
     */
    public boolean match(final long millis, final boolean isMatchSecond) {
        return match(TimeZone.getDefault(), millis, isMatchSecond);
    }

    /**
     * Checks if a given time in a specific time zone matches the cron expression.
     *
     * @param timezone      The time zone.
     * @param millis        The time in milliseconds.
     * @param isMatchSecond Whether to match the second field.
     * @return {@code true} if the time matches, {@code false} otherwise.
     */
    public boolean match(final TimeZone timezone, final long millis, final boolean isMatchSecond) {
        final GregorianCalendar calendar = new GregorianCalendar(timezone);
        calendar.setTimeInMillis(millis);
        return match(calendar, isMatchSecond);
    }

    /**
     * Checks if a given {@link java.util.Calendar} instance matches the cron expression.
     *
     * @param calendar      The calendar instance.
     * @param isMatchSecond Whether to match the second field.
     * @return {@code true} if the time matches, {@code false} otherwise.
     */
    public boolean match(final java.util.Calendar calendar, final boolean isMatchSecond) {
        return match(getFields(calendar, isMatchSecond));
    }

    /**
     * Checks if a given {@link LocalDateTime} instance matches the cron expression.
     *
     * @param dateTime      The {@link LocalDateTime} instance.
     * @param isMatchSecond Whether to match the second field.
     * @return {@code true} if the time matches, {@code false} otherwise.
     */
    public boolean match(final LocalDateTime dateTime, final boolean isMatchSecond) {
        return match(getFields(dateTime, isMatchSecond));
    }

    /**
     * Starting from the current time, returns the next matching time. If the current time matches, returns directly.
     *
     * @return Timestamp of the next matching time
     */
    public long nextMatchFromNow() {
        return nextMatch(java.util.Calendar.getInstance()).getTimeInMillis();
    }

    /**
     * Returns the next matching time after the given calendar time. If the given time already matches, it will find the
     * next occurrence.
     *
     * @param calendar The calendar instance representing the start time.
     * @return A new {@link java.util.Calendar} instance for the next matching time.
     */
    public java.util.Calendar nextMatchAfter(java.util.Calendar calendar) {
        // If the provided time already matches, add one second to find the *next* match.
        if (match(calendar, true)) {
            final java.util.Calendar newCalendar = java.util.Calendar.getInstance(calendar.getTimeZone());
            newCalendar.setTimeInMillis(calendar.getTimeInMillis() + 1000);
            calendar = newCalendar;
        }

        return nextMatch(calendar);
    }

    /**
     * Starting from the specified timestamp, returns the next matching time. If the current time matches, returns
     * directly.
     *
     * @param millis Timestamp
     * @return Timestamp of the next matching time
     */
    public long nextMatch(long millis) {
        return nextMatch(Calendar.calendar(millis)).getTimeInMillis();
    }

    /**
     * Returns the next matching time. If the given time matches the pattern, the time itself is returned.
     *
     * @param calendar The calendar instance representing the start time.
     * @return A new {@link java.util.Calendar} instance for the next matching time.
     */
    public java.util.Calendar nextMatch(final java.util.Calendar calendar) {
        java.util.Calendar next = nextMatchAfter(getFields(calendar, true), calendar.getTimeZone());
        if (match(next, true)) {
            return next;
        }

        // If no match is found, advance to the beginning of the next day and try again.
        next.set(java.util.Calendar.DAY_OF_MONTH, next.get(java.util.Calendar.DAY_OF_MONTH) + 1);
        next = Calendar.beginOfDay(next);
        return nextMatch(next);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CronPattern that = (CronPattern) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public String toString() {
        return this.pattern;
    }

    /**
     * Checks if the given time fields match any of the sub-patterns.
     *
     * @param fields The time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     * @return {@code true} if a match is found, {@code false} otherwise.
     */
    private boolean match(final int[] fields) {
        for (final PatternMatcher matcher : matchers) {
            if (matcher.match(fields)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the next earliest matching date and time from all sub-patterns.
     *
     * @param values The time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     * @param zone   The time zone.
     * @return A {@link java.util.Calendar} instance for the earliest next match, with milliseconds set to 0.
     */
    private java.util.Calendar nextMatchAfter(final int[] values, final TimeZone zone) {
        java.util.Calendar minMatch = null;
        for (final PatternMatcher matcher : matchers) {
            final java.util.Calendar nextMatch = matcher.nextMatchAfter(values, zone);
            if (null == minMatch) {
                minMatch = nextMatch;
            } else {
                minMatch = CompareKit.min(minMatch, nextMatch);
            }
        }
        // Return the earliest match found.
        return minMatch;
    }

}
