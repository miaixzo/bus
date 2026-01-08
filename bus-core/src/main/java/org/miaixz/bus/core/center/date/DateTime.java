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
package org.miaixz.bus.core.center.date;

import java.io.Serial;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.parser.DateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * A wrapper for {@link Date} that provides enhanced functionality and is timezone-aware. This class extends
 * {@link Date} and overrides the {@code toString()} method to return a "yyyy-MM-dd HH:mm:ss" formatted string by
 * default. It maintains a timezone, defaulting to the system's current timezone.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateTime extends Date {

    @Serial
    private static final long serialVersionUID = 2852233950292L;

    private static boolean useJdkToStringStyle = false;
    /**
     * Whether this object is mutable.
     */
    private boolean mutable = true;
    /**
     * The first day of the week, defaulting to Monday.
     */
    private Week firstDayOfWeek = Week.MONDAY;
    /**
     * The timezone for this DateTime object.
     */
    private transient TimeZone timeZone;
    /**
     * The minimum number of days in the first week.
     */
    private int minimalDaysInFirstWeek;

    /**
     * Constructs a new {@code DateTime} object representing the current time in the default timezone.
     */
    public DateTime() {
        this(TimeZone.getDefault());
    }

    /**
     * Constructs a new {@code DateTime} object representing the current time in the specified timezone.
     *
     * @param timeZone The timezone. If {@code null}, the default timezone is used.
     */
    public DateTime(final TimeZone timeZone) {
        this(System.currentTimeMillis(), timeZone);
    }

    /**
     * Constructs a new {@code DateTime} object from an existing {@link Date}.
     *
     * @param date The date.
     */
    public DateTime(final Date date) {
        this(date, (date instanceof DateTime) ? ((DateTime) date).timeZone : TimeZone.getDefault());
    }

    /**
     * Constructs a new {@code DateTime} object from an existing {@link Date} and timezone.
     *
     * @param date     The date. If {@code null}, the current time is used.
     * @param timeZone The timezone. If {@code null}, the default timezone is used.
     */
    public DateTime(final Date date, final TimeZone timeZone) {
        this(ObjectKit.defaultIfNull(date, Date::new).getTime(), timeZone);
    }

    /**
     * Constructs a new {@code DateTime} object from a {@link java.util.Calendar}.
     *
     * @param calendar The calendar, which cannot be {@code null}.
     */
    public DateTime(final java.util.Calendar calendar) {
        this(calendar.getTime(), calendar.getTimeZone());
        this.setFirstDayOfWeek(Week.of(calendar.getFirstDayOfWeek()));
    }

    /**
     * Constructs a new {@code DateTime} object from an {@link Instant}.
     *
     * @param instant The instant, which cannot be {@code null}.
     */
    public DateTime(final Instant instant) {
        this(instant.toEpochMilli());
    }

    /**
     * Constructs a new {@code DateTime} object from an {@link Instant} and {@link ZoneId}.
     *
     * @param instant The instant.
     * @param zoneId  The zone ID.
     */
    public DateTime(final Instant instant, final ZoneId zoneId) {
        this(instant.toEpochMilli(), ZoneKit.toTimeZone(zoneId));
    }

    /**
     * Constructs a new {@code DateTime} object from a {@link TemporalAccessor}.
     *
     * @param temporalAccessor The temporal accessor.
     */
    public DateTime(final TemporalAccessor temporalAccessor) {
        this(Converter.toInstant(temporalAccessor));
    }

    /**
     * Constructs a new {@code DateTime} object from a {@link ZonedDateTime}.
     *
     * @param zonedDateTime The zoned date-time object.
     */
    public DateTime(final ZonedDateTime zonedDateTime) {
        this(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    /**
     * Constructs a new {@code DateTime} object from a timestamp in milliseconds.
     *
     * @param timeMillis The timestamp in milliseconds since the epoch.
     */
    public DateTime(final long timeMillis) {
        this(timeMillis, TimeZone.getDefault());
    }

    /**
     * Constructs a new {@code DateTime} object from a timestamp and timezone.
     *
     * @param timeMillis The timestamp in milliseconds since the epoch.
     * @param timeZone   The timezone.
     */
    public DateTime(final long timeMillis, final TimeZone timeZone) {
        super(timeMillis);
        this.timeZone = ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault);
    }

    /**
     * Constructs a new {@code DateTime} object by parsing a date string. It supports various common formats.
     *
     * @param date The date string.
     */
    public DateTime(final CharSequence date) {
        this(DateKit.parse(date));
    }

    /**
     * Constructs a new {@code DateTime} object by parsing a date string with a specific format.
     *
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     * 
     * @param date   The date string.
     * @param format The format pattern (see {@link Fields}).
     */
    public DateTime(final CharSequence date, final String format) {
        this(parse(date, format));
    }

    /**
     * Constructs a new {@code DateTime} object using a {@link DateFormat}.
     *
     * @param date   The date string.
     * @param format The {@link DateFormat} to use for parsing.
     */
    public DateTime(final CharSequence date, final DateFormat format) {
        this(parse(date, format), format.getTimeZone());
    }

    /**
     * Constructs a new {@code DateTime} object using a {@link DateTimeFormatter}.
     *
     * @param date      The date string.
     * @param formatter The {@link DateTimeFormatter} to use for parsing.
     */
    public DateTime(final CharSequence date, final DateTimeFormatter formatter) {
        this(Converter.toInstant(formatter.parse(date)), formatter.getZone());
    }

    /**
     * Constructs a new {@code DateTime} object using a {@link DateParser}.
     *
     * @param date       The date string.
     * @param dateParser The {@link DateParser} to use.
     */
    public DateTime(final CharSequence date, final PositionDateParser dateParser) {
        this(date, dateParser, Keys.getBoolean(Keys.DATE_LENIENT, false));
    }

    /**
     * Constructs a new {@code DateTime} object using a {@link DateParser} and lenient mode setting.
     *
     * @param date    The date string.
     * @param parser  The {@link DateParser} to use.
     * @param lenient Whether parsing should be lenient.
     */
    public DateTime(final CharSequence date, final PositionDateParser parser, final boolean lenient) {
        this(parse(date, parser, lenient));
    }

    /**
     * Creates a new {@code DateTime} object representing the current time.
     *
     * @return A new {@code DateTime} object representing the current time.
     */
    public static DateTime now() {
        return new DateTime();
    }

    /**
     * Creates a {@code DateTime} from a timestamp in milliseconds.
     *
     * @param timeMillis The timestamp.
     * @return A new {@code DateTime} instance.
     */
    public static DateTime of(final long timeMillis) {
        return new DateTime(timeMillis);
    }

    /**
     * Converts a standard {@link Date} to a {@code DateTime}.
     *
     * @param date The JDK {@link Date}.
     * @return A new {@code DateTime} instance.
     */
    public static DateTime of(final Date date) {
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return new DateTime(date);
    }

    /**
     * Converts a {@link java.util.Calendar} to a {@code DateTime}, preserving the timezone.
     *
     * @param calendar The {@link java.util.Calendar}.
     * @return A new {@code DateTime} instance.
     */
    public static DateTime of(final java.util.Calendar calendar) {
        return new DateTime(calendar);
    }

    /**
     * Creates a {@code DateTime} by parsing a string with a specific format.
     *
     * @param date   The date string.
     * @param format The format pattern.
     * @return A new {@code DateTime} instance.
     */
    public static DateTime of(final String date, final String format) {
        return new DateTime(date, format);
    }

    /**
     * Sets the global default string representation style for {@code DateTime} objects.
     *
     * @param customUseJdkToStringStyle If {@code true}, {@code toString()} will use the default {@link Date#toString()}
     *                                  format. If {@code false} (default), it will use "yyyy-MM-dd HH:mm:ss".
     */
    public static void setUseJdkToStringStyle(final boolean customUseJdkToStringStyle) {
        useJdkToStringStyle = customUseJdkToStringStyle;
    }

    /**
     * Parses a date string using a format pattern.
     * 
     * @param date   the date string
     * @param format the format pattern
     * @return the parsed {@link Date}
     */
    private static Date parse(final CharSequence date, final String format) {
        final FormatManager formatManager = FormatManager.getInstance();
        return formatManager.isCustomFormat(format) ? formatManager.parse(date, format)
                : parse(date, Formatter.newSimpleFormat(format));
    }

    /**
     * Parses a date string using a {@link DateFormat}.
     * 
     * @param date   the date string
     * @param format the date format
     * @return the parsed {@link Date}
     */
    private static Date parse(final CharSequence date, final DateFormat format) {
        Assert.notBlank(date, "Date String must be not blank!");
        try {
            return format.parse(date.toString());
        } catch (final Exception e) {
            final String pattern = (format instanceof SimpleDateFormat) ? ((SimpleDateFormat) format).toPattern()
                    : format.toString();
            throw new DateException(StringKit.format("Parse [{}] with format [{}] error!", date, pattern), e);
        }
    }

    /**
     * Parses a date string using a {@link DateParser}.
     * 
     * @param date    the date string
     * @param parser  the date parser
     * @param lenient whether parsing should be lenient
     * @return the parsed {@link java.util.Calendar}
     */
    private static java.util.Calendar parse(
            final CharSequence date,
            final PositionDateParser parser,
            final boolean lenient) {
        final java.util.Calendar calendar = Calendar.parse(date, lenient, parser);
        calendar.setFirstDayOfWeek(Week.MONDAY.getCode());
        return calendar;
    }

    /**
     * Offsets this date and time by a given amount.
     *
     * @param datePart The part of the date to modify (e.g., {@link Various#YEAR}).
     * @param offset   The amount to add (can be negative).
     * @return This object if mutable, or a new {@code DateTime} instance if immutable.
     */
    public DateTime offset(final Various datePart, final int offset) {
        if (Various.ERA == datePart) {
            throw new IllegalArgumentException("ERA is not supported for offset!");
        }

        final java.util.Calendar cal = toCalendar();
        cal.add(datePart.getValue(), offset);

        final DateTime dt = mutable ? this : ObjectKit.clone(this);
        return dt.setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * Offsets this date and time, returning a new instance regardless of mutability.
     *
     * @param datePart The part of the date to modify.
     * @param offset   The amount to add.
     * @return A new {@code DateTime} instance with the offset applied.
     */
    public DateTime offsetNew(final Various datePart, final int offset) {
        final java.util.Calendar cal = toCalendar();
        cal.add(datePart.getValue(), offset);
        return ObjectKit.clone(this).setTimeInternal(cal.getTimeInMillis());
    }

    /**
     * Gets a specific part of the date (e.g., year, month).
     *
     * @param field The date part to get (e.g., {@link Various#YEAR}).
     * @return The value of the specified field.
     */
    public int getField(final Various field) {
        return getField(field.getValue());
    }

    /**
     * Gets a specific part of the date using a {@link java.util.Calendar} field constant.
     *
     * @param field The {@link java.util.Calendar} field constant.
     * @return The value of the specified field.
     */
    public int getField(final int field) {
        return toCalendar().get(field);
    }

    /**
     * Sets a specific part of the date (e.g., year, month).
     *
     * @param field The date part to set (e.g., {@link Various#YEAR}).
     * @param value The new value.
     * @return This object if mutable, or a new instance if immutable.
     */
    public DateTime setField(final Various field, final int value) {
        return setField(field.getValue(), value);
    }

    /**
     * Sets a specific part of the date using a {@link java.util.Calendar} field constant.
     *
     * @param field The {@link java.util.Calendar} field constant.
     * @param value The new value.
     * @return This object if mutable, or a new instance if immutable.
     */
    public DateTime setField(final int field, final int value) {
        final java.util.Calendar calendar = toCalendar();
        calendar.set(field, value);

        DateTime dt = this;
        if (!mutable) {
            dt = ObjectKit.clone(this);
        }
        return dt.setTimeInternal(calendar.getTimeInMillis());
    }

    /**
     * Sets the time of this {@code DateTime} object.
     *
     * @param time the new time in milliseconds since the epoch.
     * @throws DateException if this object is immutable.
     */
    @Override
    public void setTime(final long time) {
        if (mutable) {
            super.setTime(time);
        } else {
            throw new DateException("This DateTime object is immutable!");
        }
    }

    /**
     * Gets the year part of the date.
     *
     * @return The year.
     */
    public int year() {
        return getField(Various.YEAR);
    }

    /**
     * Gets the quarter of the year (1-4).
     *
     * @return The quarter.
     */
    public int quarter() {
        return month() / 3 + 1;
    }

    /**
     * Gets the quarter of the year as a {@link Quarter} enum.
     *
     * @return The quarter enum.
     */
    public Quarter quarterEnum() {
        return Quarter.of(quarter());
    }

    /**
     * Gets the month of the year, 0-indexed (0 for January).
     *
     * @return The month (0-11).
     */
    public int month() {
        return getField(Various.MONTH);
    }

    /**
     * Gets the month of the year, 1-indexed (1 for January).
     *
     * @return The month (1-12).
     */
    public int monthBaseOne() {
        return month() + 1;
    }

    /**
     * Gets the month of the year, 1-indexed (1 for January).
     *
     * @return The month (1-12).
     * @deprecated Use {@link #monthBaseOne()} for clarity.
     */
    @Deprecated
    public int monthStartFromOne() {
        return month() + 1;
    }

    /**
     * Gets the month of the year as a {@link Month} enum.
     *
     * @return The month enum.
     */
    public Month monthEnum() {
        return Month.of(month());
    }

    /**
     * Gets the week number within the current year.
     *
     * @return The week of the year.
     */
    public int weekOfYear() {
        return getField(Various.WEEK_OF_YEAR);
    }

    /**
     * Gets the week number within the current month.
     *
     * @return The week of the month.
     */
    public int weekOfMonth() {
        return getField(Various.WEEK_OF_MONTH);
    }

    /**
     * Gets the day of the month (1-31).
     *
     * @return The day of the month.
     */
    public int dayOfMonth() {
        return getField(Various.DAY_OF_MONTH);
    }

    /**
     * Gets the day of the year (1-366).
     *
     * @return The day of the year.
     */
    public int dayOfYear() {
        return getField(Various.DAY_OF_YEAR);
    }

    /**
     * Gets the day of the week (1 for Sunday, 2 for Monday, etc.).
     *
     * @return The day of the week.
     */
    public int dayOfWeek() {
        return getField(Various.DAY_OF_WEEK);
    }

    /**
     * Gets the day of the week within the current month (e.g., 2 for the second Tuesday in the month).
     *
     * @return The day of the week in the month.
     */
    public int dayOfWeekInMonth() {
        return getField(Various.DAY_OF_WEEK_IN_MONTH);
    }

    /**
     * Gets the day of the week as a {@link Week} enum.
     *
     * @return The day of the week enum.
     */
    public Week dayOfWeekEnum() {
        return Week.of(dayOfWeek());
    }

    /**
     * Gets the hour of the day.
     *
     * @param is24HourClock If true, returns the hour in 24-hour format (0-23). Otherwise, in 12-hour format (0-11 for
     *                      AM/PM).
     * @return The hour.
     */
    public int hour(final boolean is24HourClock) {
        return getField(is24HourClock ? Various.HOUR_OF_DAY : Various.HOUR);
    }

    /**
     * Gets the minute of the hour (0-59).
     *
     * @return The minute.
     */
    public int minute() {
        return getField(Various.MINUTE);
    }

    /**
     * Gets the second of the minute (0-59).
     *
     * @return The second.
     */
    public int second() {
        return getField(Various.SECOND);
    }

    /**
     * Gets the millisecond of the second (0-999).
     *
     * @return The millisecond.
     */
    public int millisecond() {
        return getField(Various.MILLISECOND);
    }

    /**
     * Checks if the time is in the morning (AM).
     *
     * @return {@code true} if it is AM.
     */
    public boolean isAM() {
        return java.util.Calendar.AM == getField(Various.AM_PM);
    }

    /**
     * Checks if the time is in the afternoon/evening (PM).
     *
     * @return {@code true} if it is PM.
     */
    public boolean isPM() {
        return java.util.Calendar.PM == getField(Various.AM_PM);
    }

    /**
     * Checks if the day is a weekend (Saturday or Sunday).
     *
     * @return {@code true} if it is a weekend.
     */
    public boolean isWeekend() {
        final int dayOfWeek = dayOfWeek();
        return java.util.Calendar.SATURDAY == dayOfWeek || java.util.Calendar.SUNDAY == dayOfWeek;
    }

    /**
     * Checks if the year is a leap year.
     *
     * @return {@code true} if it is a leap year.
     */
    public boolean isLeapYear() {
        return Year.isLeap(year());
    }

    /**
     * Converts this object to a {@link java.util.Calendar} using the default locale.
     *
     * @return A {@link java.util.Calendar} instance.
     */
    public java.util.Calendar toCalendar() {
        return toCalendar(Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Converts this object to a {@link java.util.Calendar} using the specified locale.
     *
     * @param locale The locale.
     * @return A {@link java.util.Calendar} instance.
     */
    public java.util.Calendar toCalendar(final Locale locale) {
        return toCalendar(this.timeZone, locale);
    }

    /**
     * Converts this object to a {@link java.util.Calendar} using the specified timezone.
     *
     * @param zone The timezone.
     * @return A {@link java.util.Calendar} instance.
     */
    public java.util.Calendar toCalendar(final TimeZone zone) {
        return toCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    /**
     * Converts this object to a {@link java.util.Calendar}.
     *
     * @param zone   The timezone.
     * @param locale The locale.
     * @return A {@link java.util.Calendar} instance.
     */
    public java.util.Calendar toCalendar(final TimeZone zone, Locale locale) {
        locale = ObjectKit.defaultIfNull(locale, () -> Locale.getDefault(Locale.Category.FORMAT));
        final java.util.Calendar cal = (null != zone) ? java.util.Calendar.getInstance(zone, locale)
                : java.util.Calendar.getInstance(locale);
        cal.setFirstDayOfWeek(firstDayOfWeek.getCode());
        if (minimalDaysInFirstWeek > 0) {
            cal.setMinimalDaysInFirstWeek(minimalDaysInFirstWeek);
        }
        cal.setTime(this);
        return cal;
    }

    /**
     * Converts this object to a standard JDK {@link Date} for compatibility.
     *
     * @return A new {@link Date} instance.
     */
    public Date toJdkDate() {
        return new Date(this.getTime());
    }

    /**
     * Converts this object to a {@link LocalDateTime}.
     *
     * @return A {@link LocalDateTime} instance.
     */
    public LocalDateTime toLocalDateTime() {
        return Converter.of(this.toInstant());
    }

    /**
     * Calculates the duration between this date and another.
     *
     * @param date The date to compare against.
     * @return A {@link Between} object representing the duration.
     */
    public Between between(final Date date) {
        return new Between(this, date);
    }

    /**
     * Calculates the duration between this date and another in a specific unit.
     *
     * @param date   The date to compare against.
     * @param chrono The chronological unit for measuring the time difference (e.g., {@link Chrono#MILLISECOND},
     *               {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR}, {@link Chrono#DAY}).
     * @return The duration in the specified unit.
     */
    public long between(final Date date, final Chrono chrono) {
        return new Between(this, date).between(chrono);
    }

    /**
     * Checks if this date is within the specified date range (inclusive).
     *
     * @param beginDate The start date of the range.
     * @param endDate   The end date of the range.
     * @return {@code true} if this date is within the range.
     */
    public boolean isIn(final Date beginDate, final Date endDate) {
        return isIn(this, beginDate, endDate);
    }

    /**
     * Checks if a date is within a specified date range (inclusive).
     *
     * @param date      The date to check.
     * @param beginDate The start date of the range.
     * @param endDate   The end date of the range.
     * @return {@code true} if the date is within the range.
     */
    public boolean isIn(final Date date, final Date beginDate, final Date endDate) {
        return isIn(date, beginDate, endDate, true, true);
    }

    /**
     * Checks if a date is within a specified date range, with optional inclusivity.
     *
     * @param date         The date to check.
     * @param beginDate    The start date of the range.
     * @param endDate      The end date of the range.
     * @param includeBegin Whether the start date is inclusive.
     * @param includeEnd   Whether the end date is inclusive.
     * @return {@code true} if the date is within the specified range.
     */
    public boolean isIn(
            final Date date,
            final Date beginDate,
            final Date endDate,
            final boolean includeBegin,
            final boolean includeEnd) {
        if (date == null || beginDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        final long thisMills = date.getTime();
        final long beginMills = beginDate.getTime();
        final long endMills = endDate.getTime();
        final long rangeMin = Math.min(beginMills, endMills);
        final long rangeMax = Math.max(beginMills, endMills);

        boolean isIn = thisMills > rangeMin && thisMills < rangeMax;
        if (!isIn && includeBegin) {
            isIn = thisMills == rangeMin;
        }
        if (!isIn && includeEnd) {
            isIn = thisMills == rangeMax;
        }
        return isIn;
    }

    /**
     * Checks if this date is before the specified date.
     *
     * @param date The date to compare with.
     * @return {@code true} if this date is before the specified date.
     */
    public boolean isBefore(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null!");
        }
        return compareTo(date) < 0;
    }

    /**
     * Checks if this date is before or equal to the specified date.
     *
     * @param date The date to compare with.
     * @return {@code true} if this date is before or equal to the specified date.
     */
    public boolean isBeforeOrEquals(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null!");
        }
        return compareTo(date) <= 0;
    }

    /**
     * Checks if this date is after the specified date.
     *
     * @param date The date to compare with.
     * @return {@code true} if this date is after the specified date.
     */
    public boolean isAfter(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null!");
        }
        return compareTo(date) > 0;
    }

    /**
     * Checks if this date is after or equal to the specified date.
     *
     * @param date The date to compare with.
     * @return {@code true} if this date is after or equal to the specified date.
     */
    public boolean isAfterOrEquals(final Date date) {
        if (null == date) {
            throw new NullPointerException("Date to compare is null!");
        }
        return compareTo(date) >= 0;
    }

    /**
     * Checks if this object is mutable.
     *
     * @return {@code true} if this object is mutable.
     */
    public boolean isMutable() {
        return mutable;
    }

    /**
     * Sets whether this object is mutable. If set to immutable, methods like {@code offset} and {@code setField} will
     * return a new instance.
     *
     * @param mutable {@code true} to make the object mutable.
     * @return this {@code DateTime} instance for chaining.
     */
    public DateTime setMutable(final boolean mutable) {
        this.mutable = mutable;
        return this;
    }

    /**
     * Gets the first day of the week.
     *
     * @return The first day of the week.
     */
    public Week getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    /**
     * Sets the first day of the week. This affects methods like {@link #weekOfMonth()} and {@link #weekOfYear()}.
     *
     * @param firstDayOfWeek The first day of the week.
     * @return this {@code DateTime} instance for chaining.
     */
    public DateTime setFirstDayOfWeek(final Week firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        return this;
    }

    /**
     * Gets the timezone of this object.
     *
     * @return The timezone.
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * Sets the timezone of this object.
     *
     * @param timeZone The new timezone.
     * @return this {@code DateTime} instance for chaining.
     */
    public DateTime setTimeZone(final TimeZone timeZone) {
        this.timeZone = ObjectKit.defaultIfNull(timeZone, TimeZone::getDefault);
        return this;
    }

    /**
     * Gets the {@link ZoneId} corresponding to this object's timezone.
     *
     * @return The {@link ZoneId}.
     */
    public ZoneId getZoneId() {
        return this.timeZone.toZoneId();
    }

    /**
     * Sets the minimum number of days required in the first week of a year or month.
     *
     * @param minimalDaysInFirstWeek The minimum number of days.
     * @return this {@code DateTime} instance for chaining.
     */
    public DateTime setMinimalDaysInFirstWeek(final int minimalDaysInFirstWeek) {
        this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
        return this;
    }

    /**
     * Checks if this date is the last day of the month.
     *
     * @return {@code true} if this date is the last day of the month.
     */
    public boolean isLastDayOfMonth() {
        return dayOfMonth() == getLastDayOfMonth();
    }

    /**
     * Gets the last day of the current month.
     *
     * @return The day number.
     */
    public int getLastDayOfMonth() {
        return monthEnum().getLastDay(isLeapYear());
    }

    /**
     * Returns a string representation of this date. The format depends on the global setting from
     * {@link #setUseJdkToStringStyle(boolean)}.
     *
     * @return The formatted date string.
     */
    @Override
    public String toString() {
        if (useJdkToStringStyle) {
            return super.toString();
        }
        return toString(this.timeZone);
    }

    /**
     * Returns the date formatted as "yyyy-MM-dd HH:mm:ss" in the system's default timezone.
     *
     * @return The formatted date string.
     */
    public String toStringDefaultTimeZone() {
        return toString(TimeZone.getDefault());
    }

    /**
     * Returns the date formatted as "yyyy-MM-dd HH:mm:ss" in the specified timezone.
     *
     * @param timeZone The timezone.
     * @return The formatted date string.
     */
    public String toString(final TimeZone timeZone) {
        if (null != timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATETIME, null, timeZone));
        }
        return toString(Formatter.NORM_DATETIME_FORMAT);
    }

    /**
     * Returns the date formatted as "yyyy-MM-dd".
     *
     * @return The "yyyy-MM-dd" formatted string.
     */
    public String toDateString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_DATE, null, timeZone));
        }
        return toString(Formatter.NORM_DATE_FORMAT);
    }

    /**
     * Returns the time formatted as "HH:mm:ss".
     *
     * @return The "HH:mm:ss" formatted string.
     */
    public String toTimeString() {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(Fields.NORM_TIME, null, timeZone));
        }
        return toString(Formatter.NORM_TIME_FORMAT);
    }

    /**
     * Formats this date to a string using the specified pattern.
     *
     * @param format The format pattern (e.g., "yyyy-MM-dd").
     * @return The formatted string.
     */
    public String toString(final String format) {
        if (null != this.timeZone) {
            return toString(Formatter.newSimpleFormat(format, null, timeZone));
        }
        return toString(FormatBuilder.getInstance(format));
    }

    /**
     * Formats this date using the specified formatter.
     *
     * @param format A {@link FormatPrinter} or {@link FormatBuilder}.
     * @return The formatted string.
     */
    public String toString(final FormatPrinter format) {
        return format.format(this);
    }

    /**
     * Formats this date using the specified {@link DateFormat}.
     *
     * @param format A {@link SimpleDateFormat} or other {@link DateFormat}.
     * @return The formatted string.
     */
    public String toString(final DateFormat format) {
        return format.format(this);
    }

    /**
     * Returns the date formatted as "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return The standard date format string with milliseconds.
     */
    public String toMsString() {
        return toString(Formatter.NORM_DATETIME_MS_FORMAT);
    }

    /**
     * Sets the internal time value for this object.
     *
     * @param time The timestamp in milliseconds.
     * @return this {@code DateTime} instance.
     */
    private DateTime setTimeInternal(final long time) {
        super.setTime(time);
        return this;
    }

}
