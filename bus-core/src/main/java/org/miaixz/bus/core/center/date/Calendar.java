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

import java.text.ParsePosition;
import java.time.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.parser.DateParser;
import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.math.ChineseNumberFormatter;
import org.miaixz.bus.core.xyz.CompareKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utility class for encapsulating and manipulating {@link java.util.Calendar} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calendar extends Calculate {

    /**
     * Constructs a new Calendar. Utility class constructor for static access.
     */
    public Calendar() {
    }

    /**
     * Checks if the given calendar represents an AM time.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return {@code true} if it's AM, {@code false} otherwise.
     */
    public static boolean isAM(final java.util.Calendar calendar) {
        return java.util.Calendar.AM == calendar.get(java.util.Calendar.AM_PM);
    }

    /**
     * Checks if the given calendar represents a PM time.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return {@code true} if it's PM, {@code false} otherwise.
     */
    public static boolean isPM(final java.util.Calendar calendar) {
        return java.util.Calendar.PM == calendar.get(java.util.Calendar.AM_PM);
    }

    /**
     * Compares two dates to check if they fall on the same day.
     *
     * @param cal1 The first date.
     * @param cal2 The second date.
     * @return {@code true} if both dates are on the same day, {@code false} otherwise.
     */
    public static boolean isSameDay(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // Unify time zones
            cal2 = calendar(cal2, cal1.getTimeZone());
        }
        return isSameYear(cal1, cal2)
                && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    /**
     * Compares two dates to check if they fall within the same week. The same week means that ERA, year, month, and
     * week are all consistent.
     *
     * @param cal1  The first date.
     * @param cal2  The second date.
     * @param isMon {@code true} if Monday is considered the first day of the week (e.g., in China), {@code false} if
     *              Sunday is considered the first day of the week (e.g., in the US).
     * @return {@code true} if both dates are in the same week, {@code false} otherwise.
     * @throws IllegalArgumentException if either calendar is null.
     */
    public static boolean isSameWeek(java.util.Calendar cal1, java.util.Calendar cal2, final boolean isMon) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        // Prevent modification of original Calendar objects before comparison
        cal1 = (java.util.Calendar) cal1.clone();

        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // Unify time zones
            cal2 = calendar(cal2, cal1.getTimeZone());
        } else {
            cal2 = (java.util.Calendar) cal2.clone();
        }

        // Set both dates to the first day of their respective weeks for comparison
        // If the two dates are the same after this, they are in the same week.
        if (isMon) {
            cal1.setFirstDayOfWeek(java.util.Calendar.MONDAY);
            cal1.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
            cal2.setFirstDayOfWeek(java.util.Calendar.MONDAY);
            cal2.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        } else {
            cal1.setFirstDayOfWeek(java.util.Calendar.SUNDAY);
            cal1.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
            cal2.setFirstDayOfWeek(java.util.Calendar.SUNDAY);
            cal2.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
        }
        return isSameDay(cal1, cal2);
    }

    /**
     * Compares two dates to check if they fall within the same month. The same month means that ERA, year, and month
     * are all consistent.
     *
     * @param cal1 The first date.
     * @param cal2 The second date.
     * @return {@code true} if both dates are in the same month, {@code false} otherwise.
     */
    public static boolean isSameMonth(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // Unify time zones
            cal2 = calendar(cal2, cal1.getTimeZone());
        }

        return isSameYear(cal1, cal2) && cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH);
    }

    /**
     * Compares two dates to check if they fall within the same year. The same year means that ERA and year are all
     * consistent.
     *
     * @param cal1 The first date.
     * @param cal2 The second date.
     * @return {@code true} if both dates are in the same year, {@code false} otherwise.
     * @throws IllegalArgumentException if either calendar is null.
     */
    public static boolean isSameYear(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // Unify time zones
            cal2 = calendar(cal2, cal1.getTimeZone());
        }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                && cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA);
    }

    /**
     * Checks if the timestamps of two {@link java.util.Calendar} objects are identical. This method compares the
     * millisecond timestamps of the two Calendar objects.
     *
     * @param date1 The first calendar.
     * @param date2 The second calendar.
     * @return {@code true} if the timestamps are identical. If both are {@code null}, returns {@code true}. If one is
     *         {@code null} and the other is not, returns {@code false}.
     */
    public static boolean isSameInstant(final java.util.Calendar date1, final java.util.Calendar date2) {
        if (null == date1) {
            return null == date2;
        }
        if (null == date2) {
            return false;
        }

        return date1.getTimeInMillis() == date2.getTimeInMillis();
    }

    /**
     * Checks if the given calendar represents the first day of its month.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return {@code true} if it's the first day of the month, {@code false} otherwise.
     */
    public static boolean isFirstDayOfMonth(final java.util.Calendar calendar) {
        return 1 == calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /**
     * Checks if the given calendar represents the last day of its month.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return {@code true} if it's the last day of the month, {@code false} otherwise.
     */
    public static boolean isLastDayOfMonth(final java.util.Calendar calendar) {
        return calendar.get(java.util.Calendar.DAY_OF_MONTH) == calendar
                .getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
    }

    /**
     * Creates a {@link java.util.Calendar} object initialized to the current time in the default time zone.
     *
     * @return A new {@link java.util.Calendar} instance.
     */
    public static java.util.Calendar calendar() {
        return java.util.Calendar.getInstance();
    }

    /**
     * Converts a {@link Date} object to a {@link java.util.Calendar} object.
     *
     * @param date The date object to convert.
     * @return A {@link java.util.Calendar} instance representing the given date.
     */
    public static java.util.Calendar calendar(final Date date) {
        if (date instanceof DateTime) {
            return ((DateTime) date).toCalendar();
        } else {
            return calendar(date.getTime());
        }
    }

    /**
     * Converts an {@link XMLGregorianCalendar} object to a {@link java.util.Calendar} object.
     *
     * @param calendar The XMLGregorianCalendar object to convert.
     * @return A {@link java.util.Calendar} instance representing the given XMLGregorianCalendar.
     */
    public static java.util.Calendar calendar(final XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar();
    }

    /**
     * Converts a millisecond timestamp to a {@link java.util.Calendar} object, using the current default time zone.
     *
     * @param millis The millisecond timestamp.
     * @return A {@link java.util.Calendar} instance representing the given timestamp.
     */
    public static java.util.Calendar calendar(final long millis) {
        return calendar(millis, TimeZone.getDefault());
    }

    /**
     * Converts a millisecond timestamp to a {@link java.util.Calendar} object, using the specified time zone.
     *
     * @param millis   The millisecond timestamp.
     * @param timeZone The time zone to use.
     * @return A {@link java.util.Calendar} instance representing the given timestamp in the specified time zone.
     */
    public static java.util.Calendar calendar(final long millis, final TimeZone timeZone) {
        final java.util.Calendar cal = java.util.Calendar.getInstance(timeZone);
        cal.setTimeInMillis(millis);
        return cal;
    }

    /**
     * Converts a {@link java.util.Calendar} object to a new {@link java.util.Calendar} object in the specified time
     * zone.
     *
     * @param calendar The original calendar.
     * @param timeZone The new time zone.
     * @return A new {@link java.util.Calendar} instance in the specified time zone.
     */
    public static java.util.Calendar calendar(java.util.Calendar calendar, final TimeZone timeZone) {
        // Convert to a unified time zone, e.g., UTC
        calendar = (java.util.Calendar) calendar.clone();
        calendar.setTimeZone(timeZone);
        return calendar;
    }

    /**
     * Truncates the given calendar to the beginning of a specified time field. For example, if {@code Various#SECOND}
     * is specified, the seconds field and all smaller fields (milliseconds) will be set to their minimum possible value
     * (0).
     *
     * @param calendar The {@link java.util.Calendar} object to truncate.
     * @param various  The time field to truncate to, e.g., {@link Various#SECOND}.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar truncate(final java.util.Calendar calendar, final Various various) {
        return Modifier.modify(calendar, various.getValue(), Modify.TRUNCATE);
    }

    /**
     * Rounds the given calendar to the nearest value of a specified time field.
     *
     * @param calendar The {@link java.util.Calendar} object to round.
     * @param various  The time field to round, e.g., {@link Various#MINUTE}.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar round(final java.util.Calendar calendar, final Various various) {
        return Modifier.modify(calendar, various.getValue(), Modify.ROUND);
    }

    /**
     * Sets the given calendar to the end of a specified time field. Optionally, milliseconds can be truncated to 0.
     *
     * <p>
     * Sometimes, the millisecond part must be 0 (e.g., in MySQL databases), so this option is provided.
     * 
     *
     * @param calendar The {@link java.util.Calendar} object to modify.
     * @param various  The time field to set to the end of.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar ceiling(
            final java.util.Calendar calendar,
            final Various various,
            final boolean truncate) {
        return Modifier.modify(calendar, various.getValue(), Modify.CEILING, truncate);
    }

    /**
     * Sets the given calendar to the beginning of the second, ignoring milliseconds.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfSecond(final java.util.Calendar calendar) {
        return truncate(calendar, Various.SECOND);
    }

    /**
     * Sets the given calendar to the end of the second, setting milliseconds to 999.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfSecond(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.SECOND, truncate);
    }

    /**
     * Sets the given calendar to the beginning of the hour.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfHour(final java.util.Calendar calendar) {
        return truncate(calendar, Various.HOUR_OF_DAY);
    }

    /**
     * Sets the given calendar to the end of the hour.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfHour(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.HOUR_OF_DAY, truncate);
    }

    /**
     * Sets the given calendar to the beginning of the minute.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfMinute(final java.util.Calendar calendar) {
        return truncate(calendar, Various.MINUTE);
    }

    /**
     * Sets the given calendar to the end of the minute.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfMinute(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.MINUTE, truncate);
    }

    /**
     * Sets the given calendar to the beginning of the day.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfDay(final java.util.Calendar calendar) {
        return truncate(calendar, Various.DAY_OF_MONTH);
    }

    /**
     * Sets the given calendar to the end of the day.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfDay(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.DAY_OF_MONTH, truncate);
    }

    /**
     * Sets the given calendar to the beginning of its current week, with Monday as the first day of the week.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfWeek(final java.util.Calendar calendar) {
        return beginOfWeek(calendar, true);
    }

    /**
     * Sets the given calendar to the beginning of its current week.
     *
     * @param calendar           The {@link java.util.Calendar} object.
     * @param isMondayAsFirstDay {@code true} if Monday is the first day of the week, {@code false} if Sunday is.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfWeek(final java.util.Calendar calendar, final boolean isMondayAsFirstDay) {
        calendar.setFirstDayOfWeek(isMondayAsFirstDay ? java.util.Calendar.MONDAY : java.util.Calendar.SUNDAY);
        // WEEK_OF_MONTH is the upper bound field (exclusive), the actual adjustment is DAY_OF_MONTH
        return truncate(calendar, Various.WEEK_OF_MONTH);
    }

    /**
     * Sets the given calendar to the end of its current week.
     *
     * @param calendar          The {@link java.util.Calendar} object.
     * @param isSundayAsLastDay {@code true} if Sunday is the last day of the week, {@code false} if Saturday is.
     * @param truncate          {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfWeek(
            final java.util.Calendar calendar,
            final boolean isSundayAsLastDay,
            final boolean truncate) {
        calendar.setFirstDayOfWeek(isSundayAsLastDay ? java.util.Calendar.MONDAY : java.util.Calendar.SUNDAY);
        // WEEK_OF_MONTH is the upper bound field (exclusive), the actual adjustment is DAY_OF_MONTH
        return ceiling(calendar, Various.WEEK_OF_MONTH, truncate);
    }

    /**
     * Sets the given calendar to the beginning of its current month.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfMonth(final java.util.Calendar calendar) {
        return truncate(calendar, Various.MONTH);
    }

    /**
     * Sets the given calendar to the end of its current month.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfMonth(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.MONTH, truncate);
    }

    /**
     * Sets the given calendar to the beginning of its current quarter.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfQuarter(final java.util.Calendar calendar) {
        calendar.set(java.util.Calendar.MONTH, calendar.get(Various.MONTH.getValue()) / 3 * 3);
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return beginOfDay(calendar);
    }

    /**
     * Sets the given calendar to the end of its current quarter.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfQuarter(final java.util.Calendar calendar, final boolean truncate) {
        final int year = calendar.get(java.util.Calendar.YEAR);
        final int month = calendar.get(Various.MONTH.getValue()) / 3 * 3 + 2;

        final java.util.Calendar resultCal = java.util.Calendar.getInstance(calendar.getTimeZone());
        resultCal.set(year, month, Month.of(month).getLastDay(isLeapYear(year)));

        return endOfDay(resultCal, truncate);
    }

    /**
     * Sets the given calendar to the beginning of its current year.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar beginOfYear(final java.util.Calendar calendar) {
        return truncate(calendar, Various.YEAR);
    }

    /**
     * Sets the given calendar to the end of its current year.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param truncate {@code true} to truncate milliseconds to 0, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar endOfYear(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.YEAR, truncate);
    }

    /**
     * Gets the year and quarter of the specified date. Format: [20131] represents the first quarter of 2013.
     *
     * @param cal The date calendar.
     * @return The year and quarter, formatted as "YYYYQ" (e.g., "20131").
     */
    public static String yearAndQuarter(final java.util.Calendar cal) {
        return StringKit.builder().append(cal.get(java.util.Calendar.YEAR))
                .append(cal.get(java.util.Calendar.MONTH) / 3 + 1).toString();
    }

    /**
     * Gets the minimum value for a specified date field. For example, the minimum value for minutes is 0.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param various  The date field, e.g., {@link Various#MINUTE}.
     * @return The minimum value for the specified field.
     * @see java.util.Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(final java.util.Calendar calendar, final Various various) {
        return getBeginValue(calendar, various.getValue());
    }

    /**
     * Gets the minimum value for a specified date field. For example, the minimum value for minutes is 0.
     *
     * @param calendar  The {@link java.util.Calendar} object.
     * @param dateField The date field constant from {@link java.util.Calendar}.
     * @return The minimum value for the specified field.
     * @see java.util.Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(final java.util.Calendar calendar, final int dateField) {
        if (java.util.Calendar.DAY_OF_WEEK == dateField) {
            return calendar.getFirstDayOfWeek();
        }
        return calendar.getActualMinimum(dateField);
    }

    /**
     * Gets the maximum value for a specified date field. For example, the maximum value for minutes is 59.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param various  The date field, e.g., {@link Various#MINUTE}.
     * @return The maximum value for the specified field.
     * @see java.util.Calendar#getActualMaximum(int)
     */
    public static int getEndValue(final java.util.Calendar calendar, final Various various) {
        return getEndValue(calendar, various.getValue());
    }

    /**
     * Gets the maximum value for a specified date field. For example, the maximum value for minutes is 59.
     *
     * @param calendar  The {@link java.util.Calendar} object.
     * @param dateField The date field constant from {@link java.util.Calendar}.
     * @return The maximum value for the specified field.
     * @see java.util.Calendar#getActualMaximum(int)
     */
    public static int getEndValue(final java.util.Calendar calendar, final int dateField) {
        if (java.util.Calendar.DAY_OF_WEEK == dateField) {
            return (calendar.getFirstDayOfWeek() + 6) % 7;
        }
        return calendar.getActualMaximum(dateField);
    }

    /**
     * Gets a specific part of the date from the given calendar. For example, to get the year, use
     * {@code getField(calendar, Various.YEAR)}.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param field    The enum representing the date part to retrieve, e.g., {@link Various}.
     * @return The value of the specified date part.
     */
    public static int getField(final java.util.Calendar calendar, final Various field) {
        return Assert.notNull(calendar).get(Assert.notNull(field).getValue());
    }

    /**
     * Converts a {@link java.util.Calendar} object to an {@link Instant} object.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The corresponding {@link Instant} object, or {@code null} if the input calendar is {@code null}.
     */
    public static Instant toInstant(final java.util.Calendar calendar) {
        return null == calendar ? null : calendar.toInstant();
    }

    /**
     * Converts a {@link java.util.Calendar} object to a {@link LocalDateTime} object, using the system default time
     * zone.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @return The corresponding {@link LocalDateTime} object, or {@code null} if the input calendar is {@code null}.
     */
    public static LocalDateTime toLocalDateTime(final java.util.Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * Null-safe comparison of two {@link java.util.Calendar} objects. A {@code null} calendar is considered less than
     * any non-null calendar.
     *
     * @param calendar1 The first calendar.
     * @param calendar2 The second calendar.
     * @return A negative integer, zero, or a positive integer as the first calendar is less than, equal to, or greater
     *         than the second.
     */
    public static int compare(final java.util.Calendar calendar1, final java.util.Calendar calendar2) {
        return CompareKit.compare(calendar1, calendar2);
    }

    /**
     * Formats the specified {@link java.util.Calendar} time into a pure Chinese representation. For example:
     * 
     * <pre>
     *     2018-02-24 12:13:14 converts to 二〇一八年二月二十四日 (if withTime is false)
     *     2018-02-24 12:13:14 converts to 二〇一八年二月二十四日十二时十三分十四秒 (if withTime is true)
     * </pre>
     *
     * @param calendar The {@link java.util.Calendar} object to format.
     * @param withTime {@code true} to include the time part, {@code false} otherwise.
     * @return The formatted Chinese date string.
     */
    public static String formatChineseDate(final java.util.Calendar calendar, final boolean withTime) {
        final StringBuilder result = StringKit.builder();

        // Year
        final String year = String.valueOf(calendar.get(java.util.Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(ChineseNumberFormatter.formatChar(year.charAt(i), false));
        }
        result.append('年');

        // Month
        final int month = calendar.get(java.util.Calendar.MONTH) + 1;
        result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(month));
        result.append('月');

        // Day
        final int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(day));
        result.append('日');

        // Replace '0' with '〇' only for year, month, day. '0' in time part does not need replacement.
        final String temp = result.toString().replace(Symbol.C_UL_ZERO, '〇');
        result.delete(0, result.length());
        result.append(temp);

        if (withTime) {
            // Hour
            final int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(hour));
            result.append('时');
            // Minute
            final int minute = calendar.get(java.util.Calendar.MINUTE);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(minute));
            result.append('分');
            // Second
            final int second = calendar.get(java.util.Calendar.SECOND);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(second));
            result.append('秒');
        }

        return result.toString();
    }

    /**
     * Parses a date-time string using a given array of date formats. The provided date formats will be tried one by one
     * until parsing succeeds. Returns a {@link java.util.Calendar} object, otherwise throws a {@link DateException}.
     * Method adapted from Apache Commons-Lang3.
     *
     * @param text          The date-time string, must not be null.
     * @param parsePatterns An array of date-time formats to try, must not be null, see SimpleDateFormat.
     * @return The parsed Calendar object.
     * @throws IllegalArgumentException if the date string or pattern array is null.
     * @throws DateException            if no suitable date pattern is found.
     */
    public static java.util.Calendar parseByPatterns(final CharSequence text, final String... parsePatterns)
            throws DateException {
        return parseByPatterns(text, null, parsePatterns);
    }

    /**
     * Parses a date-time string using a given array of date formats. The provided date formats will be tried one by one
     * until parsing succeeds. Returns a {@link java.util.Calendar} object, otherwise throws a {@link DateException}.
     * Method adapted from Apache Commons-Lang3.
     *
     * @param text          The date-time string, must not be null.
     * @param locale        The locale to use; if {@code null}, {@link Locale#getDefault()} is used.
     * @param parsePatterns An array of date-time formats to try, must not be null, see SimpleDateFormat.
     * @return The parsed Calendar object.
     * @throws IllegalArgumentException if the date string or pattern array is null.
     * @throws DateException            if no suitable date pattern is found.
     */
    public static java.util.Calendar parseByPatterns(
            final CharSequence text,
            final Locale locale,
            final String... parsePatterns) throws DateException {
        return parseByPatterns(text, locale, true, parsePatterns);
    }

    /**
     * Parses a date-time string using a given array of date formats. The provided date formats will be tried one by one
     * until parsing succeeds. Returns a {@link java.util.Calendar} object, otherwise throws a {@link DateException}.
     * Method adapted from Apache Commons-Lang3.
     *
     * @param text          The date-time string, must not be null.
     * @param locale        The locale to use; if {@code null}, {@link Locale#getDefault()} is used.
     * @param lenient       {@code true} to use lenient parsing, {@code false} for strict parsing.
     * @param parsePatterns An array of date-time formats to try, must not be null, see SimpleDateFormat.
     * @return The parsed Calendar object.
     * @throws IllegalArgumentException if the date string or pattern array is null.
     * @throws DateException            if no suitable date pattern is found.
     * @see java.util.Calendar#isLenient()
     */
    public static java.util.Calendar parseByPatterns(
            final CharSequence text,
            final Locale locale,
            final boolean lenient,
            final String... parsePatterns) throws DateException {
        if (text == null || parsePatterns == null) {
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }

        final TimeZone tz = TimeZone.getDefault();
        final Locale lcl = ObjectKit.defaultIfNull(locale, Locale.getDefault());
        final ParsePosition pos = new ParsePosition(0);
        final java.util.Calendar calendar = java.util.Calendar.getInstance(tz, lcl);
        calendar.setLenient(lenient);

        final FormatManager formatManager = FormatManager.getInstance();
        for (final String parsePattern : parsePatterns) {
            if (formatManager.isCustomParse(parsePattern)) {
                final Date parse = formatManager.parse(text, parsePattern);
                if (null == parse) {
                    continue;
                }
                calendar.setTime(parse);
                return calendar;
            }

            final FastDateParser fdp = new FastDateParser(parsePattern, tz, lcl);
            calendar.clear();
            try {
                if (fdp.parse(text, pos, calendar) && pos.getIndex() == text.length()) {
                    return calendar;
                }
            } catch (final IllegalArgumentException ignore) {
                // Lenient handling prevents the calendar from being set
            }
            pos.setIndex(0);
        }

        throw new DateException("Unable to parse the date: {}", text);
    }

    /**
     * Parses a string into a {@link java.util.Calendar} using the specified {@link DateParser}.
     *
     * @param text    The date string.
     * @param lenient {@code true} for lenient parsing, {@code false} for strict parsing.
     * @param parser  The {@link DateParser} to use.
     * @return The parsed {@link java.util.Calendar} object, or {@code null} if parsing fails.
     * @throws DateException if parsing fails.
     */
    public static java.util.Calendar parse(
            final CharSequence text,
            final boolean lenient,
            final PositionDateParser parser) {
        Assert.notNull(parser, "Parser must be not null!");
        return parser.parseCalendar(text, null, lenient);
    }

    /**
     * Calculates the age based on a birth date.
     *
     * @param birthDay The birth date as a {@link LocalDate}.
     * @return The age in years.
     * @throws DateTimeException if the birth date is after the current date.
     */
    public static int age(LocalDate birthDay) {
        Period period = Period.between(birthDay, LocalDate.now());
        if (period.getYears() < 0) {
            throw new DateTimeException("birthDay is after now!");
        } else {
            return period.getYears();
        }
    }

    /**
     * Calculates the age based on a birth date.
     *
     * @param birthDay The birth date as a {@link LocalDateTime}.
     * @return The age in years.
     */
    public static int age(LocalDateTime birthDay) {
        return age(birthDay.toLocalDate());
    }

    /**
     * Calculates the age (full years) relative to a comparison date. This is commonly used to calculate the age of a
     * specific birthday in a given year. According to Article 2 of the "Interpretation of Several Issues Concerning the
     * Application of Law in the Trial of Criminal Cases Involving Minors" by the Supreme People's Court, the "full age"
     * stipulated in Article 17 of the Criminal Law is calculated based on the Gregorian calendar year, month, and day,
     * starting from the second day after the birthday.
     * <ul>
     * <li>Born on 2022-03-01, then relative to 2023-03-01, the full age is 0. It becomes 1 year old relative to
     * 2023-03-02.</li>
     * <li>Born on 1999-02-28, then relative to 2000-02-29, the full age is 1.</li>
     * </ul>
     *
     * @param birthday      The birth date as a {@link java.util.Calendar}.
     * @param dateToCompare The date to compare against.
     * @return The age in years.
     */
    public static int age(final java.util.Calendar birthday, final java.util.Calendar dateToCompare) {
        return age(birthday.getTimeInMillis(), dateToCompare.getTimeInMillis());
    }

    /**
     * Calculates the age (full years) relative to a comparison date. This is commonly used to calculate the age of a
     * specific birthday in a given year. According to Article 2 of the "Interpretation of Several Issues Concerning the
     * Application of Law in the Trial of Criminal Cases Involving Minors" by the Supreme People's Court, the "full age"
     * stipulated in Article 17 of the Criminal Law is calculated based on the Gregorian calendar year, month, and day,
     * starting from the second day after the birthday.
     * <ul>
     * <li>Born on 2022-03-01, then relative to 2023-03-01, the full age is 0. It becomes 1 year old relative to
     * 2023-03-02.</li>
     * <li>Born on 1999-02-28, then relative to 2000-02-29, the full age is 1.</li>
     * </ul>
     *
     * @param birthday      The birth date in milliseconds.
     * @param dateToCompare The comparison date in milliseconds.
     * @return The age in years.
     * @throws IllegalArgumentException if the birth date is after the comparison date.
     */
    protected static int age(final long birthday, final long dateToCompare) {
        if (birthday > dateToCompare) {
            throw new IllegalArgumentException("Birthday is after dateToCompare!");
        }

        final java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(dateToCompare);

        final int year = cal.get(java.util.Calendar.YEAR);
        final int month = cal.get(java.util.Calendar.MONTH);
        final int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);

        // Reuse cal
        cal.setTimeInMillis(birthday);
        int age = year - cal.get(java.util.Calendar.YEAR);
        // If current date is the birth year, age is 0
        if (age == 0) {
            return 0;
        }

        final int monthBirth = cal.get(java.util.Calendar.MONTH);
        if (month == monthBirth) {
            final int dayOfMonthBirth = cal.get(java.util.Calendar.DAY_OF_MONTH);
            // Legal birthday itself does not count towards age, calculation starts from the second day
            if (dayOfMonth <= dayOfMonthBirth) {
                // If birthday is in the current month, but the current day has not reached the birthday, decrement age
                age--;
            }
        } else if (month < monthBirth) {
            // If the current month has not reached the birth month, decrement age
            age--;
        }

        return age;
    }

}
