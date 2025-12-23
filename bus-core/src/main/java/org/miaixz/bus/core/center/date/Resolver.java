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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.format.parser.RegisterDateParser;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Date parsing utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Resolver extends Converter {

    /**
     * Constructs a new Resolver. Utility class constructor for static access.
     */
    public Resolver() {
    }

    /**
     * Converts a date string to a {@link DateTime} object. Supported formats include:
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
     * <li>yyyy-MM-dd HH:mm:ss.SSSSSS</li>
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
     * @param date The date string.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date) {
        return (DateTime) RegisterDateParser.INSTANCE.parse(date);
    }

    /**
     * Constructs a DateTime object from a date string and a {@link DateFormat} object.
     *
     * @param date   The date string.
     * @param format The formatter {@link SimpleDateFormat}.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final DateFormat format) {
        return new DateTime(date, format);
    }

    /**
     * Constructs a DateTime object from a date string and a {@link PositionDateParser} object.
     *
     * @param date   The date string.
     * @param parser The formatter, {@link FormatBuilder}.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final PositionDateParser parser) {
        return new DateTime(date, parser);
    }

    /**
     * Constructs a DateTime object from a date string, a {@link PositionDateParser} object, and a lenient flag.
     *
     * @param date    The date string.
     * @param parser  The formatter, {@link FormatBuilder}.
     * @param lenient Whether to use lenient parsing.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final PositionDateParser parser, final boolean lenient) {
        return new DateTime(date, parser, lenient);
    }

    /**
     * Constructs a DateTime object from a date string and a {@link DateTimeFormatter} object.
     *
     * @param date      The date string.
     * @param formatter The formatter, {@link DateTimeFormatter}.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final DateTimeFormatter formatter) {
        return new DateTime(date, formatter);
    }

    /**
     * Converts a date string of a specific format to a {@link DateTime} object.
     *
     * @param date   The date string in a specific format.
     * @param format The format, e.g., "yyyy-MM-dd".
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final String format) {
        return new DateTime(date, format);
    }

    /**
     * Converts a date string of a specific format and locale to a {@link DateTime} object.
     *
     * @param date   The date string in a specific format.
     * @param format The format, e.g., "yyyy-MM-dd".
     * @param locale The locale information.
     * @return The {@link DateTime} object.
     */
    public static DateTime parse(final CharSequence date, final String format, final Locale locale) {
        final FormatManager formatManager = FormatManager.getInstance();
        if (formatManager.isCustomFormat(format)) {
            // Custom formatter ignores Locale
            return new DateTime(formatManager.parse(date, format));
        }
        return new DateTime(date, Formatter.newSimpleFormat(format, locale, null));
    }

    /**
     * Parses a date-time string using a given array of date formats. The provided date formats will be tried one by one
     * until parsing succeeds. Returns a {@link DateTime} object, otherwise throws a {@link DateException}.
     *
     * @param date          The date-time string, must not be null.
     * @param parsePatterns An array of date-time formats to try, must not be null, see SimpleDateFormat.
     * @return The parsed {@link DateTime} object.
     * @throws IllegalArgumentException if the date string or pattern array is null.
     * @throws DateException            if none of the date patterns were suitable.
     */
    public static DateTime parse(final String date, final String... parsePatterns) throws DateException {
        return date(Calendar.parseByPatterns(date, parsePatterns));
    }

    /**
     * Parses a date-time string into a {@link LocalDateTime} object.
     *
     * @param date   The date-time string.
     * @param format The date format, e.g., "yyyy-MM-dd HH:mm:ss,SSS".
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime parseTime(CharSequence date, final String format) {
        if (StringKit.isBlank(date)) {
            return null;
        }

        final FormatManager formatManager = FormatManager.getInstance();
        if (formatManager.isCustomFormat(format)) {
            return of(formatManager.parse(date, format));
        }

        DateTimeFormatter formatter = null;
        if (StringKit.isNotBlank(format)) {
            // Fix the issue where yyyyMMddHHmmssSSS format cannot be parsed
            if (StringKit.startWithIgnoreEquals(format, Fields.PURE_DATETIME) && format.endsWith("S")) {
                // Number of zeros to pad
                final int paddingWidth = 3 - (format.length() - Fields.PURE_DATETIME.length());
                if (paddingWidth > 0) {
                    // Unify yyyyMMddHHmmssS, yyyyMMddHHmmssSS dates to yyyyMMddHHmmssSSS format, padded with 0s
                    date += StringKit.repeat('0', paddingWidth);
                }
                formatter = Formatter.PURE_DATETIME_MS_FORMATTER;
            } else {
                formatter = DateTimeFormatter.ofPattern(format);
            }
        }

        return parseTime(date, formatter);
    }

    /**
     * Parses a date-time string into a {@link LocalDateTime} object. Supports date-time, date, and time formats. If the
     * formatter is {@code null}, {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} is used.
     *
     * @param date      The date-time string.
     * @param formatter The date formatter; predefined formats can be found in {@link DateTimeFormatter}.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime parseTime(final CharSequence date, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        if (null == formatter) {
            return LocalDateTime.parse(date);
        }

        return of(formatter.parse(date));
    }

    /**
     * Parses a date string into a {@link LocalDate} object.
     *
     * @param date   The date string.
     * @param format The date format, e.g., "yyyy-MM-dd".
     * @return The {@link LocalDate} object.
     */
    public static LocalDate parseDate(final CharSequence date, final String format) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        return parseDate(date, DateTimeFormatter.ofPattern(format));
    }

    /**
     * Parses a date-time string into a {@link LocalDate} object. Supports date formats only.
     *
     * @param date      The date-time string.
     * @param formatter The date formatter; predefined formats can be found in {@link DateTimeFormatter}.
     * @return The {@link LocalDate} object.
     */
    public static LocalDate parseDate(final CharSequence date, final DateTimeFormatter formatter) {
        if (StringKit.isBlank(date)) {
            return null;
        }
        if (null == formatter) {
            return LocalDate.parse(date);
        }

        return ofDate(formatter.parse(date));
    }

    /**
     * Parses a date-time string into a {@link LocalDate} object. Only supports "yyyy-MM-dd'T'HH:mm:ss" format, e.g.,
     * "2007-12-03T10:15:30".
     *
     * @param date The date-time string.
     * @return The {@link LocalDate} object.
     */
    public static LocalDate parseDateByISO(final CharSequence date) {
        return parseDate(date, (DateTimeFormatter) null);
    }

    /**
     * Parses a date-time string into a {@link LocalDateTime} object. Supports:
     * <ul>
     * <li>{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} "yyyy-MM-dd'T'HH:mm:ss" format, e.g.,
     * "2007-12-03T10:15:30"</li>
     * <li>"yyyy-MM-dd HH:mm:ss"</li>
     * </ul>
     *
     * @param date The date-time string.
     * @return The {@link LocalDateTime} object.
     */
    public static LocalDateTime parseTimeByISO(final CharSequence date) {
        if (StringKit.contains(date, 'T')) {
            return parseTime(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return parseTime(date, Formatter.NORM_DATETIME_FORMATTER);
        }
    }

    /**
     * Normalizes a date string. By default, it processes date-time formats separated by spaces, where the part before
     * the space is the date and after is the time. Replaces the following characters with "-":
     * 
     * <pre>
     * "."
     * "/"
     * "年" (year)
     * "月" (month)
     * </pre>
     * <p>
     * Removes the following characters:
     * 
     * <pre>
     * "日" (day)
     * </pre>
     * <p>
     * Replaces the following characters with ":":
     * 
     * <pre>
     * "时" (hour)
     * "分" (minute)
     * "秒" (second)
     * </pre>
     * <p>
     * Removes the trailing ":" if it exists (when milliseconds are not present).
     *
     * @param date The date-time string.
     * @return The normalized date string.
     */
    public static String normalize(final CharSequence date) {
        if (StringKit.isBlank(date)) {
            return StringKit.toString(date);
        }

        // Process date and time separately
        final List<String> dateAndTime = CharsBacker.splitTrim(date, Symbol.SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // Not a format that can be standardized
            return StringKit.toString(date);
        }

        final StringBuilder builder = StringKit.builder();

        // Date part (replace "\", "/", ".", "年", "月" with "-")
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", Symbol.MINUS);
        datePart = StringKit.removeSuffix(datePart, "日");
        builder.append(datePart);

        // Time part
        if (size == 2) {
            builder.append(Symbol.C_SPACE);
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", Symbol.COLON);
            timePart = StringKit.removeSuffix(timePart, Symbol.COLON);
            // Replace comma in ISO8601 with dot
            timePart = timePart.replace(Symbol.C_COMMA, '.');
            builder.append(timePart);
        }

        return builder.toString();
    }

}
