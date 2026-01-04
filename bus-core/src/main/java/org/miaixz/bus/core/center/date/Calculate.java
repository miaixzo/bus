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
package org.miaixz.bus.core.center.date;

import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.*;

/**
 * Date calculation class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calculate extends Resolver {

    /**
     * Constructs a new Calculate. Utility class constructor for static access.
     */
    public Calculate() {
    }

    /**
     * Checks if the current date is within the specified date range. The begin and end dates can be interchanged.
     *
     * @param date      The date to check.
     * @param beginDate The inclusive start date.
     * @param endDate   The inclusive end date.
     * @return {@code true} if within range, {@code false} otherwise.
     */
    public static boolean isIn(
            final TemporalAccessor date,
            final TemporalAccessor beginDate,
            final TemporalAccessor endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * Checks if the current date is within the specified date range. The begin and end dates can be interchanged. The
     * {@code includeBegin} and {@code includeEnd} parameters control whether the date range is open or closed. For
     * example, if {@code includeBegin=true} and {@code includeEnd=false}, this method checks if date ∈ (beginDate,
     * endDate].
     *
     * @param date         The date to check.
     * @param beginDate    The start date.
     * @param endDate      The end date.
     * @param includeBegin {@code true} if the time range includes the start date.
     * @param includeEnd   {@code true} if the time range includes the end date.
     * @return {@code true} if within range, {@code false} otherwise.
     */
    public static boolean isIn(
            final TemporalAccessor date,
            final TemporalAccessor beginDate,
            final TemporalAccessor endDate,
            final boolean includeBegin,
            final boolean includeEnd) {
        if (date == null || beginDate == null || endDate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        final long thisMills = toEpochMilli(date);
        final long beginMills = toEpochMilli(beginDate);
        final long endMills = toEpochMilli(endDate);
        final long rangeMin = Math.min(beginMills, endMills);
        final long rangeMax = Math.max(beginMills, endMills);

        // First, check if date ∈ (beginDate, endDate)
        boolean isIn = rangeMin < thisMills && thisMills < rangeMax;

        // If not, then check if it's on the boundary of the time range
        if (!isIn && includeBegin) {
            isIn = thisMills == rangeMin;
        }

        if (!isIn && includeEnd) {
            isIn = thisMills == rangeMax;
        }

        return isIn;
    }

    /**
     * Checks if two time periods overlap. Overlap means whether the two time periods have an intersection. Note that
     * when time periods overlap, such as:
     * <ul>
     * <li>This method does not correct for start time being less than end time.</li>
     * <li>When realStartTime and realEndTime or startTime and endTime are equal, it degenerates to checking if the
     * interval contains a point.</li>
     * <li>When realStartTime, realEndTime, startTime, and endTime are all equal, it degenerates to checking if points
     * are equal.</li>
     * </ul>
     * See <a href="https://www.ics.uci.edu/~alspaugh/cls/shr/allen.html">For accurate interval relationships, refer to
     * Allen's Interval Algebra</a>
     *
     * @param realStartTime The start time of the first period.
     * @param realEndTime   The end time of the first period.
     * @param startTime     The start time of the second period.
     * @param endTime       The end time of the second period.
     * @return {@code true} if the time periods overlap, {@code false} otherwise.
     */
    public static boolean isOverlap(
            final ChronoLocalDateTime<?> realStartTime,
            final ChronoLocalDateTime<?> realEndTime,
            final ChronoLocalDateTime<?> startTime,
            final ChronoLocalDateTime<?> endTime) {
        // x > b || a > y means no intersection
        // So the logic for intersection is !(x > b || a > y)
        // According to De Morgan's laws, this simplifies to x <= b && a <= y, i.e., realStartTime <= endTime &&
        // startTime <= realEndTime
        return realStartTime.compareTo(endTime) <= 0 && startTime.compareTo(realEndTime) <= 0;
    }

    /**
     * Compares two dates to check if they are the same day.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return {@code true} if both dates are the same day, {@code false} otherwise.
     */
    public static boolean isSameDay(final ChronoLocalDateTime<?> date1, final ChronoLocalDateTime<?> date2) {
        return date1 != null && date2 != null && date1.toLocalDate().isEqual(date2.toLocalDate());
    }

    /**
     * Compares two dates to check if they are the same day.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return {@code true} if both dates are the same day, {@code false} otherwise.
     */
    public static boolean isSameDay(final ChronoLocalDate date1, final ChronoLocalDate date2) {
        return date1 != null && date2 != null && date1.isEqual(date2);
    }

    /**
     * Checks if the given {@link LocalDateTime} is a weekend (Saturday or Sunday).
     *
     * @param localDateTime The {@link LocalDateTime} to check.
     * @return {@code true} if it's a weekend, {@code false} otherwise.
     */
    public static boolean isWeekend(final LocalDateTime localDateTime) {
        return isWeekend(localDateTime.toLocalDate());
    }

    /**
     * Checks if the given {@link LocalDate} is a weekend (Saturday or Sunday).
     *
     * @param localDate The {@link LocalDate} to check.
     * @return {@code true} if it's a weekend, {@code false} otherwise.
     */
    public static boolean isWeekend(final LocalDate localDate) {
        final DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return DayOfWeek.SATURDAY == dayOfWeek || DayOfWeek.SUNDAY == dayOfWeek;
    }

    /**
     * Checks if the given year is a leap year.
     *
     * @param year The year to check.
     * @return {@code true} if it's a leap year, {@code false} otherwise.
     */
    public static boolean isLeapYear(final int year) {
        return Year.isLeap(year);
    }

    /**
     * Date offset, adds different values based on the field (offset modifies the passed object).
     *
     * @param time   The {@link LocalDateTime} object.
     * @param number The offset amount; positive for future, negative for past.
     * @param field  The offset unit, see {@link ChronoUnit}, cannot be null.
     * @return The offset date and time.
     */
    public static LocalDateTime offset(final LocalDateTime time, final long number, final TemporalUnit field) {
        return offset(time, number, field);
    }

    /**
     * Date offset, adds different values based on the field (offset modifies the passed object).
     *
     * @param <T>    The date type, such as LocalDate or LocalDateTime.
     * @param time   The {@link Temporal} object.
     * @param number The offset amount; positive for future, negative for past.
     * @param field  The offset unit, see {@link ChronoUnit}, cannot be null.
     * @return The offset date and time.
     */
    public static <T extends Temporal> T offset(final T time, final long number, final TemporalUnit field) {
        if (null == time) {
            return null;
        }

        return (T) time.plus(number, field);
    }

    /**
     * Modifies to the beginning of the day, e.g., 2020-02-02 00:00:00,000.
     *
     * @param time The date and time.
     * @return The beginning of the day.
     */
    public static LocalDateTime beginOfDay(final LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    /**
     * Modifies to the beginning of the day, e.g., 2020-02-02 00:00:00,000.
     *
     * @param date The date.
     * @return The beginning of the day.
     */
    public static LocalDateTime beginOfDay(final LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Modifies to the end of the day.
     * <ul>
     * <li>Milliseconds not truncated: 2020-02-02 23:59:59,999</li>
     * <li>Milliseconds truncated: 2020-02-02 23:59:59,000</li>
     * </ul>
     *
     * @param time                The date and time.
     * @param truncateMillisecond Whether to truncate milliseconds to zero.
     * @return The end of the day.
     */
    public static LocalDateTime endOfDay(final LocalDateTime time, final boolean truncateMillisecond) {
        return time.with(max(truncateMillisecond));
    }

    /**
     * Modifies to the end of the day.
     * <ul>
     * <li>Milliseconds not truncated: 2024-10-01 23:59:59,999</li>
     * <li>Milliseconds truncated: 2024-10-01 23:59:59,000</li>
     * </ul>
     *
     * @param date                The date.
     * @param truncateMillisecond Whether to truncate milliseconds to zero.
     * @return The end of the day.
     */
    public static LocalDateTime endOfDay(final LocalDate date, final boolean truncateMillisecond) {
        return LocalDateTime.of(date, max(truncateMillisecond));
    }

    /**
     * Modifies to the beginning of the month, e.g., 2024-10-01 00:00:00,000.
     *
     * @param time The date and time.
     * @return The beginning of the month.
     */
    public static LocalDateTime beginOfMonth(final LocalDateTime time) {
        return beginOfDay(beginOfMonth(time.toLocalDate()));
    }

    /**
     * Modifies to the beginning of the month, e.g., 2024-10-01 00:00:00,000.
     *
     * @param date The date.
     * @return The beginning of the month.
     */
    public static LocalDate beginOfMonth(final LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Modifies to the end of the month.
     *
     * @param time                The date and time.
     * @param truncateMillisecond Whether to truncate milliseconds to zero.
     * @return The end of the month.
     */
    public static LocalDateTime endOfMonth(final LocalDateTime time, final boolean truncateMillisecond) {
        return endOfDay(endOfMonth(time.toLocalDate()), truncateMillisecond);
    }

    /**
     * Modifies to the end of the month.
     *
     * @param date The date.
     * @return The end of the month.
     */
    public static LocalDate endOfMonth(final LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Modifies to the beginning of the year, e.g., 2025-01-01 00:00:00,000.
     *
     * @param time The date and time.
     * @return The beginning of the year.
     */
    public static LocalDateTime beginOfYear(final LocalDateTime time) {
        return beginOfDay(beginOfYear(time.toLocalDate()));
    }

    /**
     * Modifies to the beginning of the year, e.g., 2024-10-01 00:00:00,000.
     *
     * @param date The date.
     * @return The beginning of the year.
     */
    public static LocalDate beginOfYear(final LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Modifies to the end of the year.
     *
     * @param time                The date and time.
     * @param truncateMillisecond Whether to truncate milliseconds to zero.
     * @return The end of the year.
     */
    public static LocalDateTime endOfYear(final LocalDateTime time, final boolean truncateMillisecond) {
        return endOfDay(endOfYear(time.toLocalDate()), truncateMillisecond);
    }

    /**
     * Modifies to the end of the year.
     *
     * @param date The date.
     * @return The end of the year.
     */
    public static LocalDate endOfYear(final LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Gets the {@link Week} value corresponding to the {@link LocalDate}.
     *
     * @param localDate The date {@link LocalDate}.
     * @return The {@link Week} enum.
     */
    public static Week dayOfWeek(final LocalDate localDate) {
        return Week.of(localDate.getDayOfWeek());
    }

    /**
     * Gets the week number of the year for the specified date. For example:
     * <ul>
     * <li>If the first day of the year is Monday, the first week starts from the first day, with no week zero.</li>
     * <li>If the second day of the year is Monday, the first week starts from the second day, and the first day is in
     * week zero.</li>
     * <li>If the 4th day of the year is Monday, the first week starts from the 4th day, and days 1 to 3 are in week
     * zero.</li>
     * <li>If the 5th day of the year is Monday, the second week starts from the 5th day, and days 1 to 4 are in the
     * first week.</li>
     * </ul>
     *
     * @param date The date (e.g., {@link LocalDate} or {@link LocalDateTime}).
     * @return The week number of the year.
     */
    public static int weekOfYear(final TemporalAccessor date) {
        return get(date, WeekFields.ISO.weekOfYear());
    }

    /**
     * Gets the maximum time, with an option to truncate milliseconds.
     * <ul>
     * <li>If {@code truncateMillisecond} is {@code false}, returns the maximum time value: 23:59:59,999</li>
     * <li>If {@code truncateMillisecond} is {@code true}, returns the maximum time value: 23:59:59,000</li>
     * </ul>
     *
     * @param truncateMillisecond Whether to truncate milliseconds to zero.
     * @return The maximum {@link LocalTime} value.
     */
    public static LocalTime max(final boolean truncateMillisecond) {
        return truncateMillisecond ? MAX_HMS : LocalTime.MAX;
    }

    /**
     * Offsets the date to a specified day of the week.
     *
     * @param temporal   The date or date-time.
     * @param dayOfWeek  The target day of the week.
     * @param <T>        The date type, such as LocalDate or LocalDateTime.
     * @param isPrevious {@code true} to offset backward, {@code false} to offset forward.
     * @return The offset date.
     */
    public <T extends Temporal> T offset(final T temporal, final DayOfWeek dayOfWeek, final boolean isPrevious) {
        return (T) temporal
                .with(isPrevious ? TemporalAdjusters.previous(dayOfWeek) : TemporalAdjusters.next(dayOfWeek));
    }

}
