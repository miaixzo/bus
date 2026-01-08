/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.xyz;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.date.*;
import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * Date and time utility class.
 *
 * @author Kimi Liu
 * @see Fields Utility class for common date formats.
 * @since Java 17+
 */
public class DateKit extends Calendar {

    /**
     * Constructs a new DateKit. Utility class constructor for static access.
     */
    private DateKit() {
    }

    /**
     * Checks if the time is in the morning (AM).
     *
     * @param date The date.
     * @return `true` if it is AM.
     */
    public static boolean isAM(final Date date) {
        return DateTime.of(date).isAM();
    }

    /**
     * Checks if the time is in the afternoon (PM).
     *
     * @param date The date.
     * @return `true` if it is PM.
     */
    public static boolean isPM(final Date date) {
        return DateTime.of(date).isPM();
    }

    /**
     * Checks if two dates represent the same instant in time.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return `true` if the timestamps are identical.
     */
    public static boolean isSameTime(final Date date1, final Date date2) {
        return date1.compareTo(date2) == 0;
    }

    /**
     * Checks if two dates are on the same day.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return `true` if they are on the same day.
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendar.isSameDay(calendar(date1), calendar(date2));
    }

    /**
     * Checks if two dates are in the same week.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @param isMon `true` if Monday is the first day of the week, `false` if Sunday is.
     * @return `true` if they are in the same week.
     */
    public static boolean isSameWeek(final Date date1, final Date date2, final boolean isMon) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendar.isSameWeek(calendar(date1), calendar(date2), isMon);
    }

    /**
     * Checks if two dates are in the same month.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return `true` if they are in the same month.
     */
    public static boolean isSameMonth(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return Calendar.isSameMonth(calendar(date1), calendar(date2));
    }

    /**
     * Checks if a date is within a specified range (inclusive).
     *
     * @param date      The date to check.
     * @param beginDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return `true` if the date is within the range.
     */
    public static boolean isIn(final Date date, final Date beginDate, final Date endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * Checks if a date is within a specified range, with control over endpoint inclusion.
     *
     * @param date         The date to check.
     * @param beginDate    The start date of the range.
     * @param endDate      The end date of the range.
     * @param includeBegin `true` to include the start date in the range.
     * @param includeEnd   `true` to include the end date in the range.
     * @return `true` if the date is within the specified range.
     */
    public static boolean isIn(
            final Date date,
            final Date beginDate,
            final Date endDate,
            final boolean includeBegin,
            final boolean includeEnd) {
        return new DateTime().isIn(date, beginDate, endDate, includeBegin, includeEnd);
    }

    /**
     * Gets the current date and time as a {@link DateTime} object.
     *
     * @return The current date and time.
     */

    public static DateTime now() {
        return new DateTime();
    }

    /**
     * Gets the start of the current day (00:00:00).
     *
     * @return The start of today.
     */
    public static DateTime today() {
        return new DateTime(beginOfDay(java.util.Calendar.getInstance()));
    }

    /**
     * Gets the current time, truncated to seconds precision (milliseconds set to 0).
     *
     * @return The current time without milliseconds.
     */
    public static DateTime dateSecond() {
        return beginOfSecond(now());
    }

    /**
     * Converts a {@link Date} to a {@link DateTime} object.
     *
     * @param date The {@link Date}. If `null`, returns `null`.
     * @return The corresponding {@link DateTime} object.
     */
    public static DateTime date(final Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return dateNew(date);
    }

    /**
     * Converts an {@link XMLGregorianCalendar} to a {@link DateTime} object.
     *
     * @param date The {@link XMLGregorianCalendar}. If `null`, returns `null`.
     * @return The corresponding {@link DateTime} object.
     */
    public static DateTime date(final XMLGregorianCalendar date) {
        if (date == null) {
            return null;
        }
        return date(date.toGregorianCalendar());
    }

    /**
     * Creates a new {@link DateTime} object from an existing {@link Date}.
     *
     * @param date The {@link Date}. If `null`, returns `null`.
     * @return A new {@link DateTime} object.
     */
    public static DateTime dateNew(final Date date) {
        if (date == null) {
            return null;
        }
        return new DateTime(date);
    }

    /**
     * Creates a new {@link DateTime} object from a {@link Date} and a {@link TimeZone}.
     *
     * @param date     The {@link Date}. If `null`, returns `null`.
     * @param timeZone The time zone. If `null`, the default time zone is used.
     * @return A new {@link DateTime} object.
     */
    public static DateTime date(final Date date, final TimeZone timeZone) {
        if (date == null) {
            return null;
        }
        return new DateTime(date, timeZone);
    }

    /**
     * Converts a `long` timestamp (milliseconds) to a {@link DateTime} object.
     *
     * @param date The timestamp in milliseconds.
     * @return A {@link DateTime} object.
     */
    public static DateTime date(final long date) {
        return new DateTime(date);
    }

    /**
     * Converts a {@link java.util.Calendar} to a {@link DateTime} object.
     *
     * @param calendar The {@link java.util.Calendar}. If `null`, returns `null`.
     * @return A new {@link DateTime} object.
     */
    public static DateTime date(final java.util.Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new DateTime(calendar);
    }

    /**
     * Converts a {@link TemporalAccessor} to a {@link DateTime} object.
     *
     * @param temporalAccessor The {@link TemporalAccessor} (e.g., {@link LocalDateTime}). If `null`, returns `null`.
     * @return A new {@link DateTime} object.
     */
    public static DateTime date(final TemporalAccessor temporalAccessor) {
        if (temporalAccessor == null) {
            return null;
        }
        return new DateTime(temporalAccessor);
    }

    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time as a timestamp.
     */
    public static long current() {
        return System.currentTimeMillis();
    }

    /**
     * Gets the current time in seconds since the Unix epoch (January 1, 1970).
     *
     * @return The current time in seconds.
     */
    public static long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Formats the current time as "yyyy-MM-dd HH:mm:ss".
     *
     * @return The formatted string.
     */
    public static String formatNow() {
        return formatDateTime(new DateTime());
    }

    /**
     * Formats the current date as "yyyy-MM-dd".
     *
     * @return The formatted string.
     */
    public static String formatToday() {
        return formatDate(new DateTime());
    }

    /**
     * Gets the year part of a date.
     *
     * @param date The date.
     * @return The year.
     */
    public static int year(final Date date) {
        return DateTime.of(date).year();
    }

    /**
     * Gets the quarter of the year for a given date (1-4).
     *
     * @param date The date.
     * @return The quarter (1-4).
     */
    public static int quarter(final Date date) {
        return DateTime.of(date).quarter();
    }

    /**
     * Gets the quarter enum for a given date.
     *
     * @param date The date.
     * @return The {@link Quarter} enum.
     */
    public static Quarter quarterEnum(final Date date) {
        return DateTime.of(date).quarterEnum();
    }

    /**
     * Gets the month part of a date (0-11).
     *
     * @param date The date.
     * @return The month (0-11).
     */
    public static int month(final Date date) {
        return DateTime.of(date).month();
    }

    /**
     * Gets the month enum for a given date.
     *
     * @param date The date.
     * @return The {@link Month} enum.
     */
    public static Month monthEnum(final Date date) {
        return DateTime.of(date).monthEnum();
    }

    /**
     * Gets the week of the year for a given date.
     *
     * @param date The date.
     * @return The week of the year.
     * @see DateTime#setFirstDayOfWeek(Week)
     */
    public static int weekOfYear(final Date date) {
        return DateTime.of(date).weekOfYear();
    }

    /**
     * Gets the week of the month for a given date.
     *
     * @param date The date.
     * @return The week of the month.
     */
    public static int weekOfMonth(final Date date) {
        return DateTime.of(date).weekOfMonth();
    }

    /**
     * Gets the day of the month for a given date.
     *
     * @param date The date.
     * @return The day of the month.
     */
    public static int dayOfMonth(final Date date) {
        return DateTime.of(date).dayOfMonth();
    }

    /**
     * Gets the day of the year for a given date.
     *
     * @param date The date.
     * @return The day of the year.
     */
    public static int dayOfYear(final Date date) {
        return DateTime.of(date).dayOfYear();
    }

    /**
     * Gets the day of the week for a given date (1 for Sunday, 2 for Monday, etc.).
     *
     * @param date The date.
     * @return The day of the week.
     */
    public static int dayOfWeek(final Date date) {
        return DateTime.of(date).dayOfWeek();
    }

    /**
     * Gets the day of the week enum for a given date.
     *
     * @param date The date.
     * @return The {@link Week} enum.
     */
    public static Week dayOfWeekEnum(final Date date) {
        return DateTime.of(date).dayOfWeekEnum();
    }

    /**
     * Checks if the given date is a weekend (Saturday or Sunday).
     *
     * @param date The date to check.
     * @return `true` if it is a weekend.
     */
    public static boolean isWeekend(final Date date) {
        final Week week = dayOfWeekEnum(date);
        return Week.SATURDAY == week || Week.SUNDAY == week;
    }

    /**
     * Gets the hour part of a date.
     *
     * @param date          The date.
     * @param is24HourClock If true, returns the hour in 24-hour format.
     * @return The hour.
     */
    public static int hour(final Date date, final boolean is24HourClock) {
        return DateTime.of(date).hour(is24HourClock);
    }

    /**
     * Gets the minute part of a date.
     *
     * @param date The date.
     * @return The minute.
     */
    public static int minute(final Date date) {
        return DateTime.of(date).minute();
    }

    /**
     * Gets the second part of a date.
     *
     * @param date The date.
     * @return The second.
     */
    public static int second(final Date date) {
        return DateTime.of(date).second();
    }

    /**
     * Gets the millisecond part of a date.
     *
     * @param date The date.
     * @return The millisecond.
     */
    public static int millisecond(final Date date) {
        return DateTime.of(date).millisecond();
    }

    /**
     * Gets the current year.
     *
     * @return The current year.
     */
    public static int thisYear() {
        return year(now());
    }

    /**
     * Gets the current month value.
     *
     * @return The current month (0-11).
     */
    public static int thisMonth() {
        return month(now());
    }

    /**
     * Gets the current month as a {@link Month} enum.
     *
     * @return The current month as a {@link Month} enum.
     */
    public static Month thisMonthEnum() {
        return monthEnum(now());
    }

    /**
     * Gets the current week of the year.
     *
     * @return The current week of the year.
     */
    public static int thisWeekOfYear() {
        return weekOfYear(now());
    }

    /**
     * Gets the current week of the month.
     *
     * @return The current week of the month.
     */
    public static int thisWeekOfMonth() {
        return weekOfMonth(now());
    }

    /**
     * Gets the current day of the month.
     *
     * @return The current day of the month.
     */
    public static int thisDayOfMonth() {
        return dayOfMonth(now());
    }

    /**
     * Gets the current day of the week.
     *
     * @return The current day of the week.
     */
    public static int thisDayOfWeek() {
        return dayOfWeek(now());
    }

    /**
     * Gets the current day of the week as a {@link Week} enum.
     *
     * @return The current day of the week as a {@link Week} enum.
     */
    public static Week thisDayOfWeekEnum() {
        return dayOfWeekEnum(now());
    }

    /**
     * Gets the current hour.
     *
     * @param is24HourClock If true, returns the hour in 24-hour format.
     * @return The current hour.
     */
    public static int thisHour(final boolean is24HourClock) {
        return hour(now(), is24HourClock);
    }

    /**
     * Gets the current minute.
     *
     * @return The current minute.
     */
    public static int thisMinute() {
        return minute(now());
    }

    /**
     * Gets the current second.
     *
     * @return The current second.
     */
    public static int thisSecond() {
        return second(now());
    }

    /**
     * Gets the current millisecond.
     *
     * @return The current millisecond.
     */
    public static int thisMillisecond() {
        return millisecond(now());
    }

    /**
     * Gets the year and quarter in the format "yyyyQ" (e.g., 20132 for 2013 Q2).
     *
     * @param date The date.
     * @return The year and quarter string.
     */
    public static String yearAndQuarter(final Date date) {
        return yearAndQuarter(calendar(date));
    }

    /**
     * Formats a date according to a specified pattern.
     *
     * @param date   The date to format.
     * @param format The format pattern (e.g., {@link Fields#NORM_DATETIME}).
     * @return The formatted string.
     */
    public static String format(final Date date, final String format) {
        if (null == date || StringKit.isBlank(format)) {
            return null;
        }
        final FormatManager formatManager = FormatManager.getInstance();
        if (formatManager.isCustomFormat(format)) {
            return formatManager.format(date, format);
        }
        TimeZone timeZone = null;
        if (date instanceof DateTime) {
            timeZone = ((DateTime) date).getTimeZone();
        }
        return format(date, FormatBuilder.getInstance(format, timeZone));
    }

    /**
     * Formats a date using a {@link FormatPrinter}.
     *
     * @param date   The date to format.
     * @param format The {@link FormatPrinter}.
     * @return The formatted string.
     */
    public static String format(final Date date, final FormatPrinter format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * Formats a date using a {@link DateFormat}.
     *
     * @param date   The date to format.
     * @param format The {@link DateFormat}.
     * @return The formatted string.
     */
    public static String format(final Date date, final DateFormat format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * Formats a date using a {@link DateTimeFormatter}.
     *
     * @param date   The date to format.
     * @param format The {@link DateTimeFormatter}.
     * @return The formatted string.
     */
    public static String format(final Date date, final DateTimeFormatter format) {
        if (null == format || null == date) {
            return null;
        }
        return Formatter.format(date.toInstant(), format);
    }

    /**
     * Formats a date as "yyyy-MM-dd HH:mm:ss".
     *
     * @param date The date to format.
     * @return The formatted string.
     */
    public static String formatDateTime(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_DATETIME_FORMAT.format(date);
    }

    /**
     * Formats a date as "yyyy-MM-dd".
     *
     * @param date The date to format.
     * @return The formatted string.
     */
    public static String formatDate(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_DATE_FORMAT.format(date);
    }

    /**
     * Formats a date as "HH:mm:ss".
     *
     * @param date The date to format.
     * @return The formatted string.
     */
    public static String formatTime(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.NORM_TIME_FORMAT.format(date);
    }

    /**
     * Formats a date to the standard HTTP format (RFC 1123).
     *
     * @param date The date to format.
     * @return The HTTP formatted date string.
     */
    public static String formatHttpDate(final Date date) {
        if (null == date) {
            return null;
        }
        return Formatter.HTTP_DATETIME_FORMAT_GMT.format(date);
    }

    /**
     * Formats a date into a Chinese date format.
     *
     * @param date        The date to format.
     * @param isUppercase If true, uses Chinese uppercase numerals.
     * @param withTime    If true, includes the time part.
     * @return The formatted Chinese date string.
     */
    public static String formatChineseDate(final Date date, final boolean isUppercase, final boolean withTime) {
        if (null == date) {
            return null;
        }
        if (!isUppercase) {
            return (withTime ? Formatter.CN_DATE_TIME_FORMAT : Formatter.CN_DATE_FORMAT).format(date);
        }
        return Calendar.formatChineseDate(Calendar.calendar(date), withTime);
    }

    /**
     * Truncates a date to the beginning of a specified time field.
     *
     * @param date    The {@link Date}.
     * @param various The time field to truncate to (e.g., {@link Various#SECOND}).
     * @return The truncated {@link DateTime}.
     */
    public static DateTime truncate(final Date date, final Various various) {
        return new DateTime(truncate(calendar(date), various));
    }

    /**
     * Rounds a date to the nearest specified time field.
     *
     * @param date    The {@link Date}.
     * @param various The time field to round.
     * @return The rounded {@link DateTime}.
     */
    public static DateTime round(final Date date, final Various various) {
        return new DateTime(round(calendar(date), various));
    }

    /**
     * Moves a date to the ceiling of a specified time field.
     *
     * @param date     The {@link Date}.
     * @param various  The time field.
     * @param truncate If true, truncates milliseconds to zero.
     * @return The ceiling {@link DateTime}.
     */
    public static DateTime ceiling(final Date date, final Various various, final boolean truncate) {
        return new DateTime(ceiling(calendar(date), various, truncate));
    }

    /**
     * Gets the start of the second for a given date (milliseconds set to 0).
     *
     * @param date The date.
     * @return The start of the second.
     */
    public static DateTime beginOfSecond(final Date date) {
        return new DateTime(beginOfSecond(calendar(date)));
    }

    /**
     * Gets the end of the second for a given date (milliseconds set to 999).
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0 instead of 999.
     * @return The end of the second.
     */
    public static DateTime endOfSecond(final Date date, final boolean truncate) {
        return new DateTime(endOfSecond(calendar(date), truncate));
    }

    /**
     * Gets the start of the hour for a given date.
     *
     * @param date The date.
     * @return The start of the hour.
     */
    public static DateTime beginOfHour(final Date date) {
        return new DateTime(beginOfHour(calendar(date)));
    }

    /**
     * Gets the end of the hour for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the hour.
     */
    public static DateTime endOfHour(final Date date, final boolean truncate) {
        return new DateTime(endOfHour(calendar(date), truncate));
    }

    /**
     * Gets the start of the minute for a given date.
     *
     * @param date The date.
     * @return The start of the minute.
     */
    public static DateTime beginOfMinute(final Date date) {
        return new DateTime(beginOfMinute(calendar(date)));
    }

    /**
     * Gets the end of the minute for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the minute.
     */
    public static DateTime endOfMinute(final Date date, final boolean truncate) {
        return new DateTime(endOfMinute(calendar(date), truncate));
    }

    /**
     * Gets the start of the day for a given date.
     *
     * @param date The date.
     * @return The start of the day.
     */
    public static DateTime beginOfDay(final Date date) {
        return new DateTime(beginOfDay(calendar(date)));
    }

    /**
     * Gets the end of the day for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the day.
     */
    public static DateTime endOfDay(final Date date, final boolean truncate) {
        return new DateTime(endOfDay(calendar(date), truncate));
    }

    /**
     * Gets the start of the week for a given date (Monday is the first day).
     *
     * @param date The date.
     * @return The start of the week.
     */
    public static DateTime beginOfWeek(final Date date) {
        return new DateTime(beginOfWeek(calendar(date)));
    }

    /**
     * Gets the start of the week for a given date.
     *
     * @param date               The date.
     * @param isMondayAsFirstDay If true, Monday is the first day; otherwise, Sunday is.
     * @return The start of the week.
     */
    public static DateTime beginOfWeek(final Date date, final boolean isMondayAsFirstDay) {
        return new DateTime(beginOfWeek(calendar(date), isMondayAsFirstDay));
    }

    /**
     * Gets the end of the week for a given date.
     *
     * @param date              The date.
     * @param isSundayAsLastDay If true, Sunday is the last day; otherwise, Saturday is.
     * @param truncate          If true, sets milliseconds to 0.
     * @return The end of the week.
     */
    public static DateTime endOfWeek(final Date date, final boolean isSundayAsLastDay, final boolean truncate) {
        return new DateTime(endOfWeek(calendar(date), isSundayAsLastDay, truncate));
    }

    /**
     * Gets the start of the month for a given date.
     *
     * @param date The date.
     * @return The start of the month.
     */
    public static DateTime beginOfMonth(final Date date) {
        return new DateTime(beginOfMonth(calendar(date)));
    }

    /**
     * Gets the end of the month for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the month.
     */
    public static DateTime endOfMonth(final Date date, final boolean truncate) {
        return new DateTime(endOfMonth(calendar(date), truncate));
    }

    /**
     * Gets the start of the quarter for a given date.
     *
     * @param date The date.
     * @return The start of the quarter.
     */
    public static DateTime beginOfQuarter(final Date date) {
        return new DateTime(beginOfQuarter(calendar(date)));
    }

    /**
     * Gets the end of the quarter for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the quarter.
     */
    public static DateTime endOfQuarter(final Date date, final boolean truncate) {
        return new DateTime(endOfQuarter(calendar(date), truncate));
    }

    /**
     * Gets the start of the year for a given date.
     *
     * @param date The date.
     * @return The start of the year.
     */
    public static DateTime beginOfYear(final Date date) {
        return new DateTime(beginOfYear(calendar(date)));
    }

    /**
     * Gets the end of the year for a given date.
     *
     * @param date     The date.
     * @param truncate If true, sets milliseconds to 0.
     * @return The end of the year.
     */
    public static DateTime endOfYear(final Date date, final boolean truncate) {
        return new DateTime(endOfYear(calendar(date), truncate));
    }

    /**
     * Gets yesterday's date.
     *
     * @return Yesterday's date.
     */
    public static DateTime yesterday() {
        return offsetDay(new DateTime(), -1);
    }

    /**
     * Gets tomorrow's date.
     *
     * @return Tomorrow's date.
     */
    public static DateTime tomorrow() {
        return offsetDay(new DateTime(), 1);
    }

    /**
     * Gets last week's date.
     *
     * @return Last week's date.
     */
    public static DateTime lastWeek() {
        return offsetWeek(new DateTime(), -1);
    }

    /**
     * Gets next week's date.
     *
     * @return Next week's date.
     */
    public static DateTime nextWeek() {
        return offsetWeek(new DateTime(), 1);
    }

    /**
     * Gets last month's date.
     *
     * @return Last month's date.
     */
    public static DateTime lastMonth() {
        return offsetMonth(new DateTime(), -1);
    }

    /**
     * Gets next month's date.
     *
     * @return Next month's date.
     */
    public static DateTime nextMonth() {
        return offsetMonth(new DateTime(), 1);
    }

    /**
     * Offsets a date by a number of milliseconds.
     *
     * @param date   The date.
     * @param offset The offset in milliseconds.
     * @return The new date.
     */
    public static DateTime offsetMillisecond(final Date date, final int offset) {
        return offset(date, Various.MILLISECOND, offset);
    }

    /**
     * Offsets a date by a number of seconds.
     *
     * @param date   The date.
     * @param offset The offset in seconds.
     * @return The new date.
     */
    public static DateTime offsetSecond(final Date date, final int offset) {
        return offset(date, Various.SECOND, offset);
    }

    /**
     * Offsets a date by a number of minutes.
     *
     * @param date   The date.
     * @param offset The offset in minutes.
     * @return The new date.
     */
    public static DateTime offsetMinute(final Date date, final int offset) {
        return offset(date, Various.MINUTE, offset);
    }

    /**
     * Offsets a date by a number of hours.
     *
     * @param date   The date.
     * @param offset The offset in hours.
     * @return The new date.
     */
    public static DateTime offsetHour(final Date date, final int offset) {
        return offset(date, Various.HOUR_OF_DAY, offset);
    }

    /**
     * Offsets a date by a number of days.
     *
     * @param date   The date.
     * @param offset The offset in days.
     * @return The new date.
     */
    public static DateTime offsetDay(final Date date, final int offset) {
        return offset(date, Various.DAY_OF_YEAR, offset);
    }

    /**
     * Offsets a date by a number of weeks.
     *
     * @param date   The date.
     * @param offset The offset in weeks.
     * @return The new date.
     */
    public static DateTime offsetWeek(final Date date, final int offset) {
        return offset(date, Various.WEEK_OF_YEAR, offset);
    }

    /**
     * Offsets a date by a number of months.
     *
     * @param date   The date.
     * @param offset The offset in months.
     * @return The new date.
     */
    public static DateTime offsetMonth(final Date date, final int offset) {
        return offset(date, Various.MONTH, offset);
    }

    /**
     * Offsets a date by a number of years.
     *
     * @param date   The date.
     * @param offset The offset in years.
     * @return The new date.
     */
    public static DateTime offsetYear(final Date date, final int offset) {
        return offset(date, Various.YEAR, offset);
    }

    /**
     * Offsets a date by a specified amount in a specified field.
     *
     * @param date    The base date.
     * @param various The time unit to offset (e.g., hour, day, month).
     * @param offset  The amount to offset.
     * @return The new date.
     */
    public static DateTime offset(final Date date, final Various various, final int offset) {
        if (date == null) {
            return null;
        }
        return dateNew(date).offset(various, offset);
    }

    /**
     * Calculates the absolute difference between two dates in a specified unit.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param chrono    The chronological unit for measuring the time difference (e.g., {@link Chrono#MILLISECOND},
     *                  {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR}, {@link Chrono#DAY}).
     * @return The difference in the specified unit.
     */
    public static long between(final Date beginDate, final Date endDate, final Chrono chrono) {
        return between(beginDate, endDate, chrono, true);
    }

    /**
     * Calculates the difference between two dates in a specified unit.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param chrono    The chronological unit for measuring the time difference (e.g., {@link Chrono#MILLISECOND},
     *                  {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR}, {@link Chrono#DAY}).
     * @param isAbs     If true, returns the absolute difference; otherwise, returns signed difference.
     * @return The difference in the specified unit.
     */
    public static long between(final Date beginDate, final Date endDate, final Chrono chrono, final boolean isAbs) {
        return new Between(beginDate, endDate, isAbs).between(chrono);
    }

    /**
     * Calculates the difference between two dates in milliseconds.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @return The difference in milliseconds.
     */
    public static long betweenMs(final Date beginDate, final Date endDate) {
        return new Between(beginDate, endDate).between(Chrono.MILLISECOND);
    }

    /**
     * Calculates the difference between two dates in days.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param isReset   If true, truncates both dates to the start of the day before comparing.
     * @return The difference in days.
     */
    public static long betweenDay(Date beginDate, Date endDate, final boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Chrono.DAY);
    }

    /**
     * Calculates the difference between two dates in weeks.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param isReset   If true, truncates both dates to the start of the day before comparing.
     * @return The difference in weeks.
     */
    public static long betweenWeek(Date beginDate, Date endDate, final boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Chrono.WEEK);
    }

    /**
     * Calculates the difference between two dates in months.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param isReset   If true, resets the time part of the dates.
     * @return The difference in months.
     */
    public static long betweenMonth(final Date beginDate, final Date endDate, final boolean isReset) {
        return new Between(beginDate, endDate).betweenMonth(isReset);
    }

    /**
     * Calculates the difference between two dates in years.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param isReset   If true, resets the time part of the dates.
     * @return The difference in years.
     */
    public static long betweenYear(final Date beginDate, final Date endDate, final boolean isReset) {
        return new Between(beginDate, endDate).betweenYear(isReset);
    }

    /**
     * Formats a time interval into a human-readable string.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @param chrono    The level of precision, representing the smallest chronological unit to display (e.g.,
     *                  {@link Chrono#MILLISECOND}, {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR},
     *                  {@link Chrono#DAY}).
     * @return The formatted string (e.g., "XX days XX hours XX minutes XX seconds").
     */
    public static String formatBetween(final Date beginDate, final Date endDate, final Chrono chrono) {
        return formatBetween(between(beginDate, endDate, Chrono.MILLISECOND), chrono);
    }

    /**
     * Formats a time interval into a human-readable string with millisecond precision.
     *
     * @param beginDate The start date.
     * @param endDate   The end date.
     * @return The formatted string.
     */
    public static String formatBetween(final Date beginDate, final Date endDate) {
        return formatBetween(between(beginDate, endDate, Chrono.MILLISECOND));
    }

    /**
     * Formats a duration in milliseconds into a human-readable string.
     *
     * @param betweenMs The duration in milliseconds.
     * @param chrono    The level of precision, representing the smallest chronological unit to display (e.g.,
     *                  {@link Chrono#MILLISECOND}, {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR},
     *                  {@link Chrono#DAY}).
     * @return The formatted string.
     */
    public static String formatBetween(final long betweenMs, final Chrono chrono) {
        return FormatPeriod.of(betweenMs, chrono).format();
    }

    /**
     * Formats a duration in milliseconds into a human-readable string with millisecond precision.
     *
     * @param betweenMs The duration in milliseconds.
     * @return The formatted string.
     */
    public static String formatBetween(final long betweenMs) {
        return FormatPeriod.of(betweenMs, Chrono.MILLISECOND).format();
    }

    /**
     * Calculates the elapsed time in nanoseconds since a previous time.
     *
     * @param preTime The previous time in nanoseconds.
     * @return The elapsed time in nanoseconds.
     */
    public static long spendNt(final long preTime) {
        return System.nanoTime() - preTime;
    }

    /**
     * Calculates the elapsed time in milliseconds since a previous time.
     *
     * @param preTime The previous time in milliseconds.
     * @return The elapsed time in milliseconds.
     */
    public static long spendMs(final long preTime) {
        return System.currentTimeMillis() - preTime;
    }

    /**
     * Creates a {@link StopWatch} for timing code execution.
     *
     * @return A new {@link StopWatch}.
     */
    public static StopWatch createStopWatch() {
        return new StopWatch();
    }

    /**
     * Creates a {@link StopWatch} with a specific ID.
     *
     * @param id The ID for the stopwatch.
     * @return A new {@link StopWatch}.
     */
    public static StopWatch createStopWatch(final String id) {
        return new StopWatch(id);
    }

    /**
     * Calculates the legal age from a birthday string.
     *
     * @param birthDay The birthday string.
     * @return The age.
     */
    public static int ageOfNow(final String birthDay) {
        return ageOfNow(parse(birthDay));
    }

    /**
     * Calculates the legal age (in full years) from a birth date.
     *
     * @param birthDay The birth date.
     * @return The age.
     */
    public static int ageOfNow(final Date birthDay) {
        return age(birthDay, now());
    }

    /**
     * Calculates the age as of a specific comparison date.
     *
     * @param birthday      The birth date.
     * @param dateToCompare The date to compare against.
     * @return The age.
     */
    public static int age(final Date birthday, Date dateToCompare) {
        if (null == dateToCompare) {
            dateToCompare = now();
        }
        return age(birthday.getTime(), dateToCompare.getTime());
    }

    /**
     * Converts a time string (HH:mm:ss) to seconds.
     *
     * @param timeStr The time string.
     * @return The total number of seconds.
     */
    public static int timeToSecond(final String timeStr) {
        if (StringKit.isEmpty(timeStr)) {
            return 0;
        }
        final List<String> hms = CharsBacker.split(timeStr, Symbol.COLON, 3, true, true);
        final int lastIndex = hms.size() - 1;
        int result = 0;
        for (int i = lastIndex; i >= 0; i--) {
            result += (int) (Integer.parseInt(hms.get(i)) * Math.pow(60, (lastIndex - i)));
        }
        return result;
    }

    /**
     * Converts a number of seconds to a time string (HH:mm:ss).
     *
     * @param seconds The number of seconds.
     * @return The formatted time string.
     */
    public static String secondToTime(final int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a positive number!");
        }
        final int hour = seconds / 3600;
        final int other = seconds % 3600;
        final int minute = other / 60;
        final int second = other % 60;
        final StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        sb.append(Symbol.COLON);
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        sb.append(Symbol.COLON);
        if (second < 10) {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
    }

    /**
     * Creates a date range generator.
     *
     * @param start The start date (inclusive).
     * @param end   The end date.
     * @param unit  The stepping unit.
     * @return A {@link Boundary} object.
     */
    public static Boundary range(final Date start, final Date end, final Various unit) {
        return new Boundary(start, end, unit);
    }

    /**
     * Finds the intersection of two date ranges.
     *
     * @param start The first date range.
     * @param end   The second date range.
     * @return A list of dates that are in both ranges.
     */
    public static List<DateTime> rangeContains(final Boundary start, final Boundary end) {
        final List<DateTime> startDateTimes = ListKit.of((Iterable<DateTime>) start);
        final List<DateTime> endDateTimes = ListKit.of((Iterable<DateTime>) end);
        return startDateTimes.stream().filter(endDateTimes::contains).collect(Collectors.toList());
    }

    /**
     * Finds the difference between two date ranges (end - start).
     *
     * @param start The first date range.
     * @param end   The second date range.
     * @return A list of dates that are in the `end` range but not in the `start` range.
     */
    public static List<DateTime> rangeNotContains(final Boundary start, final Boundary end) {
        final List<DateTime> startDateTimes = ListKit.of((Iterable<DateTime>) start);
        final List<DateTime> endDateTimes = ListKit.of((Iterable<DateTime>) end);
        return endDateTimes.stream().filter(item -> !startDateTimes.contains(item)).collect(Collectors.toList());
    }

    /**
     * Iterates over a date range and applies a function to each date.
     *
     * @param start The start date (inclusive).
     * @param end   The end date.
     * @param unit  The stepping unit.
     * @param func  The function to apply.
     * @param <T>   The result type of the function.
     * @return A list of the results.
     */
    public static <T> List<T> rangeFunc(
            final Date start,
            final Date end,
            final Various unit,
            final Function<Date, T> func) {
        if (start == null || end == null || start.after(end)) {
            return Collections.emptyList();
        }
        final ArrayList<T> list = new ArrayList<>();
        for (final DateTime date : range(start, end, unit)) {
            list.add(func.apply(date));
        }
        return list;
    }

    /**
     * Iterates over a date range and applies a consumer to each date.
     *
     * @param start    The start date (inclusive).
     * @param end      The end date.
     * @param unit     The stepping unit.
     * @param consumer The consumer to apply.
     */
    public static void rangeConsume(
            final Date start,
            final Date end,
            final Various unit,
            final Consumer<Date> consumer) {
        if (start == null || end == null || start.after(end)) {
            return;
        }
        range(start, end, unit).forEach(consumer);
    }

    /**
     * Generates a list of dates within a range with a specific stepping unit.
     *
     * @param start The start date.
     * @param end   The end date.
     * @param unit  The stepping unit.
     * @return A list of {@link DateTime} objects.
     */
    public static List<DateTime> rangeToList(final Date start, final Date end, final Various unit) {
        return ListKit.of((Iterable<DateTime>) range(start, end, unit));
    }

    /**
     * Generates a list of dates within a range with a specific stepping unit and step size.
     *
     * @param start The start date.
     * @param end   The end date.
     * @param unit  The stepping unit.
     * @param step  The step size.
     * @return A list of {@link DateTime} objects.
     */
    public static List<DateTime> rangeToList(final Date start, final Date end, final Various unit, final int step) {
        return ListKit.of(new Boundary(start, end, unit, step));
    }

    /**
     * Gets the astrological zodiac for a given month and day.
     *
     * @param month The month (0-11).
     * @param day   The day.
     * @return The zodiac name.
     */
    public static String getZodiac(final int month, final int day) {
        return Zodiac.getName(month, day);
    }

    /**
     * Gets the Chinese Zodiac for a given year.
     *
     * @param year The year.
     * @return The Zodiac name.
     */
    public static String getCNZodiac(final int year) {
        return org.miaixz.bus.core.center.date.culture.Zodiac.getName(year);
    }

    /**
     * Null-safe date comparison. `null` values are sorted last.
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return The comparison result.
     */
    public static int compare(final Date date1, final Date date2) {
        return CompareKit.compare(date1, date2);
    }

    /**
     * Null-safe date comparison, comparing only the parts specified by the format string.
     *
     * @param date1  The first date.
     * @param date2  The second date.
     * @param format The format pattern to use for comparison (e.g., "yyyy-MM-dd").
     * @return The comparison result.
     */
    public static int compare(Date date1, Date date2, final String format) {
        if (format != null) {
            if (date1 != null) {
                date1 = parse(format(date1, format), format);
            }
            if (date2 != null) {
                date2 = parse(format(date2, format), format);
            }
        }
        return CompareKit.compare(date1, date2);
    }

    /**
     * Converts nanoseconds to milliseconds.
     *
     * @param duration The duration in nanoseconds.
     * @return The duration in milliseconds.
     */
    public static long nanosToMillis(final long duration) {
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }

    /**
     * Converts nanoseconds to seconds.
     *
     * @param duration The duration in nanoseconds.
     * @return The duration in seconds.
     */
    public static double nanosToSeconds(final long duration) {
        return duration / 1_000_000_000.0;
    }

    /**
     * Converts a {@link Date} to a {@link java.util.Calendar}.
     *
     * @param date The date.
     * @return The calendar.
     */
    public static java.util.Calendar toCalendar(final Date date) {
        return Calendar.calendar(date);
    }

    /**
     * Converts a {@link Date} to an {@link Instant}.
     *
     * @param date The date.
     * @return The instant.
     */
    public static Instant toInstant(final Date date) {
        return null == date ? null : date.toInstant();
    }

    /**
     * Converts a {@link TemporalAccessor} to an {@link Instant}.
     *
     * @param temporalAccessor The temporal accessor.
     * @return The instant.
     */
    public static Instant toInstant(final TemporalAccessor temporalAccessor) {
        return Calculate.toInstant(temporalAccessor);
    }

    /**
     * Converts an {@link Instant} to a {@link LocalDateTime} using the system default time zone.
     *
     * @param instant The instant.
     * @return The {@link LocalDateTime}.
     */
    public static LocalDateTime toLocalDateTime(final Instant instant) {
        return Resolver.of(instant);
    }

    /**
     * Converts a {@link Date} to a {@link LocalDateTime} using the system default time zone.
     *
     * @param date The date.
     * @return The {@link LocalDateTime}.
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        return of(date);
    }

    /**
     * Gets the total number of days in a given year.
     *
     * @param year The year.
     * @return The number of days.
     */
    public static int lengthOfYear(final int year) {
        return Year.of(year).length();
    }

    /**
     * Gets the total number of days in a given month.
     *
     * @param month      The month (1-12).
     * @param isLeapYear Whether it is a leap year.
     * @return The number of days.
     */
    public static int lengthOfMonth(final int month, final boolean isLeapYear) {
        return java.time.Month.of(month).length(isLeapYear);
    }

    /**
     * Converts a {@link Date} to a {@link LocalDateTime} using the default time zone.
     *
     * @param date The date.
     * @return The {@link LocalDateTime}.
     */
    public static LocalDateTime of(final Date date) {
        if (null == date) {
            return null;
        }
        if (date instanceof DateTime) {
            return of(date.toInstant(), ((DateTime) date).getZoneId());
        }
        return of(date.toInstant());
    }

    /**
     * Gets the short name for a {@link TimeUnit}.
     *
     * @param unit The time unit.
     * @return The short name.
     */
    public static String getShortName(final TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";

            case MICROSECONDS:
                return "μs";

            case MILLISECONDS:
                return "ms";

            case SECONDS:
                return "s";

            case MINUTES:
                return "min";

            case HOURS:
                return "h";

            default:
                return unit.name().toLowerCase();
        }
    }

    /**
     * Checks if two time intervals overlap.
     *
     * @param realStartTime The start time of the first interval.
     * @param realEndTime   The end time of the first interval.
     * @param startTime     The start time of the second interval.
     * @param endTime       The end time of the second interval.
     * @return `true` if the intervals overlap.
     */
    public static boolean isOverlap(
            final Date realStartTime,
            final Date realEndTime,
            final Date startTime,
            final Date endTime) {
        return realStartTime.compareTo(endTime) <= 0 && startTime.compareTo(realEndTime) <= 0;
    }

    /**
     * Checks if the given date is the last day of its month.
     *
     * @param date The {@link Date}.
     * @return `true` if it is the last day of the month.
     */
    public static boolean isLastDayOfMonth(final Date date) {
        return date(date).isLastDayOfMonth();
    }

    /**
     * Gets the last day of the month for a given date.
     *
     * @param date The {@link Date}.
     * @return The last day of the month.
     */
    public static int getLastDayOfMonth(final Date date) {
        return date(date).getLastDayOfMonth();
    }

    /**
     * Checks if the current date is within a specified range (inclusive).
     *
     * @param beginDate The start date of the range (inclusive).
     * @param endDate   The end date of the range (inclusive).
     * @return `true` if the current date is within the range.
     */
    public boolean isIn(final Date beginDate, final Date endDate) {
        return new DateTime().isIn(beginDate, endDate);
    }

    /**
     * Utility class for `java.sql` date/time types.
     */
    public static class SQL {

        /**
         * Constructs a new SQL. Utility class constructor for static access.
         */
        private SQL() {
        }

        /**
         * Converts a {@link Date} to a {@link Timestamp}.
         *
         * @param date The date.
         * @return The {@link Timestamp}.
         */
        public static Timestamp timestamp(final Date date) {
            Assert.notNull(date);
            return new Timestamp(date.getTime());
        }

        /**
         * Converts a {@link Date} to a {@link java.sql.Date}.
         *
         * @param date The date.
         * @return The {@link java.sql.Date}.
         */
        public static java.sql.Date date(final Date date) {
            Assert.notNull(date);
            return new java.sql.Date(date.getTime());
        }

        /**
         * Converts a {@link Date} to a {@link java.sql.Time}.
         *
         * @param date The date.
         * @return The {@link java.sql.Time}.
         */
        public static java.sql.Time time(final Date date) {
            Assert.notNull(date);
            return new java.sql.Time(date.getTime());
        }

        /**
         * Wraps a millisecond timestamp into a specific `java.sql.Date` subtype.
         *
         * @param <T>         The target date type.
         * @param targetClass The target class.
         * @param mills       The timestamp in milliseconds.
         * @return An instance of the target date type.
         */
        public static <T extends Date> T wrap(final Class<?> targetClass, final long mills) {
            if (java.sql.Date.class == targetClass) {
                return (T) new java.sql.Date(mills);
            }
            if (java.sql.Time.class == targetClass) {
                return (T) new java.sql.Time(mills);
            }
            if (Timestamp.class == targetClass) {
                return (T) new Timestamp(mills);
            }
            throw new UnsupportedOperationException(
                    StringKit.format("Unsupported target Date type: {}", targetClass.getName()));
        }
    }

}
