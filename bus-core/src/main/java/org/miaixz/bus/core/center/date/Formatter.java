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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.Era;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;

import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * Date formatting and parsing utility. Supports various date and time formats including milliseconds, microseconds, and
 * nanoseconds for precise time. Examples: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm:ss.SSS, yyyy-MM-dd HH:mm:ss.SSSSSS,
 * yyyy-MM-dd HH:mm:ss.SSSSSSSSS, yyyy-MM-dd'T'HH:mm:ss.SSSZ, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Formatter {

    /**
     * Month format {@link FormatBuilder}: yyyy-MM
     */
    public static final FormatBuilder NORM_MONTH_FORMAT = FormatBuilder.getInstance(Fields.NORM_MONTH);
    /**
     * Month format {@link DateTimeFormatter}: yyyy-MM
     */
    public static final DateTimeFormatter NORM_MONTH_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_MONTH);
    /**
     * Simple month format {@link FormatBuilder}: yyyyMM
     */
    public static final FormatBuilder SIMPLE_MONTH_FORMAT = FormatBuilder.getInstance(Fields.SIMPLE_MONTH);
    /**
     * Simple month format {@link DateTimeFormatter}: yyyyMM
     */
    public static final DateTimeFormatter SIMPLE_MONTH_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.SIMPLE_MONTH);
    /**
     * Standard date format {@link FormatBuilder}: yyyy-MM-dd
     */
    public static final FormatBuilder NORM_DATE_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATE);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyy-MM-dd
     */
    public static final DateTimeFormatter NORM_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_DATE);
    /**
     * Standard hour and minute format {@link FormatBuilder}: HH:mm
     */
    public static final FormatBuilder NORM_HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(Fields.NORM_HOUR_MINUTE);
    /**
     * Standard hour and minute format {@link DateTimeFormatter}: HH:mm
     */
    public static final DateTimeFormatter NORM_HOUR_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_HOUR_MINUTE);
    /**
     * Standard time format {@link FormatBuilder}: HH:mm:ss
     */
    public static final FormatBuilder NORM_TIME_FORMAT = FormatBuilder.getInstance(Fields.NORM_TIME);
    /**
     * Standard time format {@link DateTimeFormatter}: HH:mm:ss
     */
    public static final DateTimeFormatter NORM_TIME_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.NORM_TIME);
    /**
     * Standard date and time format, accurate to minute {@link FormatBuilder}: yyyy-MM-dd HH:mm
     */
    public static final FormatBuilder NORM_DATETIME_MINUTE_FORMAT = FormatBuilder
            .getInstance(Fields.NORM_DATETIME_MINUTE);
    /**
     * Standard date and time format, accurate to minute {@link DateTimeFormatter}: yyyy-MM-dd HH:mm
     */
    public static final DateTimeFormatter NORM_DATETIME_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_MINUTE);
    /**
     * Standard date and time format, accurate to second {@link FormatBuilder}: yyyy-MM-dd HH:mm:ss
     */
    public static final FormatBuilder NORM_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATETIME);
    /**
     * Standard date and time format, accurate to second {@link DateTimeFormatter}: yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter NORM_DATETIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME);
    /**
     * Standard date and time format, accurate to millisecond {@link FormatBuilder}: yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final FormatBuilder NORM_DATETIME_MS_FORMAT = FormatBuilder.getInstance(Fields.NORM_DATETIME_MS);
    /**
     * Standard date and time format, accurate to millisecond {@link DateTimeFormatter}: yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final DateTimeFormatter NORM_DATETIME_MS_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_MS);
    /**
     * ISO8601 date and time format, accurate to millisecond {@link FormatBuilder}: yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final FormatBuilder NORM_DATETIME_COMMA_MS_FORMAT = FormatBuilder
            .getInstance(Fields.NORM_DATETIME_COMMA_MS);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final DateTimeFormatter NORM_DATETIME_COMMA_MS_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.NORM_DATETIME_COMMA_MS);
    /**
     * Standard date format {@link FormatBuilder}: MM月dd日
     */
    public static final FormatBuilder CN_MONTH_FORMAT = FormatBuilder.getInstance(Fields.CN_MONTH);
    /**
     * Standard date format {@link DateTimeFormatter}: MM月dd日
     */
    public static final DateTimeFormatter CN_MONTH_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.CN_MONTH);
    /**
     * Standard date format {@link FormatBuilder}: yyyy年MM月dd日
     */
    public static final FormatBuilder CN_DATE_FORMAT = FormatBuilder.getInstance(Fields.CN_DATE);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyy年MM月dd日
     */
    public static final DateTimeFormatter CN_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.CN_DATE);
    /**
     * Standard date format {@link FormatBuilder}: yyyy年MM月dd日HH时mm分ss秒
     */
    public static final FormatBuilder CN_DATE_TIME_FORMAT = FormatBuilder.getInstance(Fields.CN_DATE_TIME);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyy年MM月dd日HH时mm分ss秒
     */
    public static final DateTimeFormatter CN_DATE_TIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.CN_DATE_TIME);
    /**
     * Standard date format {@link FormatBuilder}: yyyyMMdd
     */
    public static final FormatBuilder PURE_DATE_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATE);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyyMMdd
     */
    public static final DateTimeFormatter PURE_DATE_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.PURE_DATE);
    /**
     * Standard hour and minute format {@link FormatBuilder}: HHmm
     */
    public static final FormatBuilder PPURE_HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(Fields.PURE_HOUR_MINUTE);
    /**
     * Standard hour and minute format {@link DateTimeFormatter}: HHmm
     */
    public static final DateTimeFormatter PURE_HOUR_MINUTE_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.PURE_HOUR_MINUTE);
    /**
     * Standard time format {@link FormatBuilder}: HHmmss
     */
    public static final FormatBuilder PURE_TIME_FORMAT = FormatBuilder.getInstance(Fields.PURE_TIME);
    /**
     * Standard time format {@link DateTimeFormatter}: HHmmss
     */
    public static final DateTimeFormatter PURE_TIME_FORMATTER = FormatBuilder.getDateTimeInstance(Fields.PURE_TIME);
    /**
     * Standard date and time format {@link FormatBuilder}: yyyyMMddHHmmss
     */
    public static final FormatBuilder PURE_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATETIME);
    /**
     * Standard date and time format {@link DateTimeFormatter}: yyyyMMddHHmmss
     */
    public static final DateTimeFormatter PURE_DATETIME_FORMATTER = FormatBuilder
            .getDateTimeInstance(Fields.PURE_DATETIME);
    /**
     * Standard date and time format, accurate to millisecond {@link FormatBuilder}: yyyyMMddHHmmssSSS
     */
    public static final FormatBuilder PURE_DATETIME_MS_FORMAT = FormatBuilder.getInstance(Fields.PURE_DATETIME_MS);
    /**
     * Format wildcard: {@link FormatBuilder} yyyyMMddHHmmss.SSS
     */
    public static final FormatBuilder PURE_DATETIME_TIP_FORMAT = FormatBuilder
            .getInstance(Fields.PURE_DATETIME_TIP_PATTERN);
    /**
     * Standard date format {@link DateTimeFormatter}: yyyyMMddHHmmssSSS. See
     * https://stackoverflow.com/questions/22588051/is-java-time-failing-to-parse-fraction-of-second JDK 8 bug at:
     * https://bugs.openjdk.java.net/browse/JDK-8031085
     */
    public static final DateTimeFormatter PURE_DATETIME_MS_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern(Fields.PURE_DATETIME).appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();
    /**
     * HTTP header date and time format {@link FormatBuilder}: EEE, dd MMM yyyy HH:mm:ss GMT
     */
    public static final FormatBuilder HTTP_DATETIME_FORMAT_GMT = FormatBuilder
            .getInstance(Fields.HTTP_DATETIME, TimeZone.getTimeZone("GMT"), Locale.US);
    /**
     * HTTP header date and time format {@link FormatBuilder}: EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final FormatBuilder HTTP_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.HTTP_DATETIME, Locale.US);
    /**
     * JDK date and time format {@link FormatBuilder}: EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final FormatBuilder JDK_DATETIME_FORMAT = FormatBuilder.getInstance(Fields.JDK_DATETIME, Locale.US);
    /**
     * ISO8601 date and time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss
     */
    public static final FormatBuilder ISO8601_FORMAT = FormatBuilder.getInstance(Fields.ISO8601);
    /**
     * UTC time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final FormatBuilder ISO8601_MS_FORMAT = FormatBuilder.getInstance(Fields.ISO8601_MS);
    /**
     * ISO8601 time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static final FormatBuilder UTC_FORMAT = FormatBuilder.getInstance(Fields.UTC, ZoneKit.ZONE_UTC);
    /**
     * ISO8601 time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ssZ, where Z indicates a time offset, e.g., +0800
     */
    public static final FormatBuilder ISO8601_WITH_ZONE_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_WITH_ZONE_OFFSET);
    /**
     * ISO8601 time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final FormatBuilder ISO8601_WITH_XXX_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_WITH_XXX_OFFSET);
    /**
     * ISO8601 time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final FormatBuilder UTC_MS_FORMAT = FormatBuilder.getInstance(Fields.UTC_MS, ZoneKit.ZONE_UTC);
    /**
     * ISO8601 time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final FormatBuilder ISO8601_MS_WITH_ZONE_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_MS_WITH_ZONE_OFFSET);
    /**
     * UTC time {@link FormatBuilder}: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final FormatBuilder ISO8601_MS_WITH_XXX_OFFSET_FORMAT = FormatBuilder
            .getInstance(Fields.ISO8601_MS_WITH_XXX_OFFSET);
    /**
     * Maximum time for hour, minute, second only.
     */
    public static final LocalTime MAX_HMS = LocalTime.of(23, 59, 59);

    /**
     * Converts the specified date string to a Unix timestamp.
     *
     * @param text The date string to convert, e.g., "yyyy-MM-dd HH:mm:ss".
     * @return The Unix timestamp in milliseconds.
     */
    public static long format(String text) {
        return NORM_DATETIME_FORMAT.parse(text).getTime();

    }

    /**
     * Converts a Unix timestamp to a date string.
     *
     * @param timestamp The Unix timestamp in milliseconds.
     * @return The date string in "yyyy-MM-dd HH:mm:ss" format.
     */
    public static String format(long timestamp) {
        return NORM_DATETIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Converts a Unix timestamp to a date string with a specified format.
     *
     * @param timestamp The Unix timestamp in milliseconds.
     * @param format    The format string, e.g., "yyyy-MM-dd".
     * @return The formatted date string.
     */
    public static String format(long timestamp, String format) {
        return new SimpleDateFormat(format).format(new Date(timestamp));
    }

    /**
     * Converts the specified date string with a given format to a Unix timestamp.
     *
     * @param text   The date string to convert.
     * @param format The format string, e.g., "yyyy-MM-dd HH:mm:ss".
     * @return The Unix timestamp in milliseconds.
     * @throws InternalException if parsing fails.
     */
    public static long format(String text, String format) {
        try {
            return new SimpleDateFormat(format).parse(text).getTime();
        } catch (ParseException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Formats a {@link LocalDateTime} object to "yyyy-MM-dd HH:mm:ss" format.
     *
     * @param localDateTime The {@link LocalDateTime} to format.
     * @return The formatted string.
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, Fields.NORM_DATETIME);
    }

    /**
     * Formats a {@link LocalDateTime} object according to a specific format.
     *
     * @param localDateTime The {@link LocalDateTime} to format.
     * @param format        The date format string; common formats can be found in {@link Fields}.
     * @return The formatted string.
     */
    public static String format(final LocalDateTime localDateTime, final String format) {
        return Formatter.format(localDateTime, format);
    }

    /**
     * Formats a {@link TemporalAccessor} object into a string using a specified {@link DateTimeFormatter}. If the input
     * is a {@link Month}, {@link Month#toString()} is called.
     *
     * @param time      The {@link TemporalAccessor} object.
     * @param formatter The date formatter; predefined formats can be found in {@link DateTimeFormatter}.
     * @return The formatted string.
     * @throws UnsupportedTemporalTypeException if the temporal accessor does not support the required fields for
     *                                          formatting.
     */
    public static String format(final TemporalAccessor time, DateTimeFormatter formatter) {
        if (null == time) {
            return null;
        }

        if (time instanceof Month) {
            return time.toString();
        }

        if (null == formatter) {
            formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }

        try {
            return formatter.format(time);
        } catch (final UnsupportedTemporalTypeException e) {
            if (time instanceof LocalDate && e.getMessage().contains("HourOfDay")) {
                // If LocalDate is passed but the format requires time, convert to LocalDateTime and retry
                return formatter.format(((LocalDate) time).atStartOfDay());
            } else if (time instanceof LocalTime && e.getMessage().contains("YearOfEra")) {
                // If LocalTime is passed but the format requires date, convert to LocalDateTime and retry
                return formatter.format(((LocalTime) time).atDate(LocalDate.now()));
            } else if (time instanceof Instant) {
                // Instant has no time zone information, assign default time zone
                return formatter.format(((Instant) time).atZone(ZoneId.systemDefault()));
            }
            throw e;
        }
    }

    /**
     * Formats a {@link TemporalAccessor} object into a string using a specified format string. If the input is a
     * {@link DayOfWeek}, {@link java.time.Month}, or {@link Era}, {@link TemporalAccessor} is called.
     *
     * @param time   The {@link TemporalAccessor} object.
     * @param format The date format string.
     * @return The formatted string.
     */
    public static String format(final TemporalAccessor time, final String format) {
        if (null == time) {
            return null;
        }

        if (time instanceof DayOfWeek || time instanceof java.time.Month || time instanceof Era
                || time instanceof MonthDay) {
            return time.toString();
        }

        // Check custom formatters
        final FormatManager formatManager = FormatManager.getInstance();
        if (formatManager.isCustomFormat(format)) {
            return formatManager.format(time, format);
        }

        final DateTimeFormatter formatter = StringKit.isBlank(format) ? null : DateTimeFormatter.ofPattern(format);

        return format(time, formatter);
    }

    /**
     * Formats an original time string from one pattern to another.
     *
     * @param text        The original time string.
     * @param srcPattern  The original time pattern.
     * @param destPattern The target time pattern.
     * @return The formatted string if successful, otherwise an empty string.
     */
    public static String format(String text, String srcPattern, String destPattern) {
        try {
            SimpleDateFormat srcSdf = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstSdf = new SimpleDateFormat(destPattern);
            return dstSdf.format(srcSdf.parse(text));
        } catch (ParseException e) {
            return Normal.EMPTY;
        }
    }

    /**
     * Formats a {@link ChronoLocalDateTime} object to "yyyy-MM-dd HH:mm:ss" format.
     *
     * @param time The {@link ChronoLocalDateTime} to format.
     * @return The formatted string.
     */
    public static String format(final ChronoLocalDateTime<?> time) {
        return format(time, NORM_DATETIME_FORMATTER);
    }

    /**
     * Formats a {@link ChronoLocalDate} object to "yyyy-MM-dd" format.
     *
     * @param date The {@link ChronoLocalDate} to format.
     * @return The formatted string.
     */
    public static String format(final ChronoLocalDate date) {
        return format(date, NORM_DATE_FORMATTER);
    }

    /**
     * Returns a function that formats a {@link TemporalAccessor} using the given {@link DateTimeFormatter}.
     *
     * @param dateTimeFormatter The {@link DateTimeFormatter} to use for formatting.
     * @return A function that formats a {@link TemporalAccessor} to a string.
     */
    public static Function<TemporalAccessor, String> format(final DateTimeFormatter dateTimeFormatter) {
        return LambdaKit.toFunction(Formatter::format, dateTimeFormatter);
    }

    /**
     * Creates a new {@link SimpleDateFormat} instance. Note that this object is not thread-safe! This object defaults
     * to strict parsing mode, meaning that an incorrect format will throw an error during parsing.
     *
     * @param pattern The format pattern.
     * @return A new {@link SimpleDateFormat} instance.
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern) {
        return newSimpleFormat(pattern, null, null);
    }

    /**
     * Creates a new {@link SimpleDateFormat} instance. Note that this object is not thread-safe! This object defaults
     * to strict parsing mode, meaning that an incorrect format will throw an error during parsing.
     *
     * @param pattern  The format pattern.
     * @param locale   The {@link Locale}; {@code null} indicates the default locale.
     * @param timeZone The {@link TimeZone}; {@code null} indicates the default time zone.
     * @return A new {@link SimpleDateFormat} instance.
     */
    public static SimpleDateFormat newSimpleFormat(final String pattern, final Locale locale, final TimeZone timeZone) {
        return newSimpleFormat(pattern, locale, timeZone, Keys.getBoolean(Keys.DATE_LENIENT, false));
    }

    /**
     * Creates a new {@link SimpleDateFormat} instance. Note that this object is not thread-safe! This object defaults
     * to strict parsing mode, meaning that an incorrect format will throw an error during parsing.
     *
     * @param pattern  The format pattern.
     * @param locale   The {@link Locale}; {@code null} indicates the default locale.
     * @param timeZone The {@link TimeZone}; {@code null} indicates the default time zone.
     * @param lenient  Whether to use lenient parsing.
     * @return A new {@link SimpleDateFormat} instance.
     */
    public static SimpleDateFormat newSimpleFormat(
            final String pattern,
            Locale locale,
            final TimeZone timeZone,
            final boolean lenient) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        if (null != timeZone) {
            format.setTimeZone(timeZone);
        }
        format.setLenient(lenient);
        return format;
    }

}
