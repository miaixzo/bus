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
package org.miaixz.bus.image;

import static java.time.temporal.ChronoField.*;

import java.io.Serial;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.DatePrecision;

/**
 * A formatter that creates strings by substituting placeholders with values from DICOM attributes. This class extends
 * {@link java.text.Format} and uses a {@link MessageFormat} engine underneath. It supports a custom pattern language
 * for accessing DICOM tags, applying formatting types (like dates, numbers, UID generation), and performing string
 * manipulations. Also provides a rich set of static utility methods for parsing and formatting DICOM date-time strings
 * (DA, TM, DT).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Format extends java.text.Format {

    @Serial
    private static final long serialVersionUID = 2852253785600L;

    /**
     * Characters for a custom base-32 encoding used in MD5 hashing.
     */
    private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v' };
    /**
     * Number of bytes in a Java long primitive type.
     */
    private static final int LONG_BYTES = 8;
    /**
     * Parser for DICOM Date (DA) strings. Handles "YYYYMMDD" and "YYYY.MM.DD".
     */
    private static final DateTimeFormatter DA_PARSER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .optionalStart().appendLiteral('.').optionalEnd().appendValue(MONTH_OF_YEAR, 2).optionalStart()
            .appendLiteral('.').optionalEnd().appendValue(DAY_OF_MONTH, 2).toFormatter();
    /**
     * Formatter for DICOM Date (DA) strings to "YYYYMMDD".
     */
    private static final DateTimeFormatter DA_FORMATTER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2).toFormatter();
    /**
     * Parser for DICOM Time (TM) strings. Handles "HHMMSS.FFFFFF".
     */
    private static final DateTimeFormatter TM_PARSER = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
            .optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart().appendFraction(NANO_OF_SECOND, 0, 6, true).toFormatter();
    /**
     * Formatter for DICOM Time (TM) strings to "HHMMSS.FFFFFF".
     */
    private static final DateTimeFormatter TM_FORMATTER = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2).appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 6, 6, true)
            .toFormatter();
    /**
     * Parser for DICOM DateTime (DT) strings. Handles "YYYYMMDDHHMMSS.FFFFFF&ZZXX".
     */
    private static final DateTimeFormatter DT_PARSER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .optionalStart().appendValue(MONTH_OF_YEAR, 2).optionalStart().appendValue(DAY_OF_MONTH, 2).optionalStart()
            .appendValue(HOUR_OF_DAY, 2).optionalStart().appendValue(MINUTE_OF_HOUR, 2).optionalStart()
            .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 0, 6, true).optionalEnd()
            .optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalStart()
            .appendOffset("+HHMM", "+0000").toFormatter();
    /**
     * Formatter for DICOM DateTime (DT) strings to "YYYYMMDDHHMMSS.FFFFFF&ZZXX".
     */
    private static final DateTimeFormatter DT_FORMATTER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2).appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2).appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 6, 6, true)
            .optionalStart().appendOffset("+HHMM", "+0000").toFormatter();
    /**
     * A default date formatter for localized display (e.g., "Jan 1, 2025").
     */
    private static final DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    /**
     * A default time formatter for localized display (e.g., "10:30:00 AM").
     */
    private static final DateTimeFormatter defaultTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    /**
     * A default date-time formatter for localized display.
     */
    private static final DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM);
    /**
     * A cache for the last used TimeZone to avoid repeated lookups.
     */
    private static TimeZone cachedTimeZone;

    /**
     * The original format pattern string.
     */
    private final String pattern;
    /**
     * An array of parsed DICOM tag paths from the pattern.
     */
    private final int[][] tagPaths;
    /**
     * An array of value indices for multi-valued attributes.
     */
    private final int[] index;
    /**
     * An array of numeric offsets for number formatting.
     */
    private final int[] offsets;
    /**
     * An array of Period or Duration objects for date/time calculations.
     */
    private final Object[] dateTimeOffsets;
    /**
     * An array of string slice operators.
     */
    private final UnaryOperator<String>[] slices;
    /**
     * An array of format types for each placeholder in the pattern.
     */
    private final Type[] types;
    /**
     * The underlying MessageFormat instance that performs the final formatting.
     */
    private final MessageFormat format;

    /**
     * Constructs a new Format object by parsing the given pattern string.
     *
     * @param pattern The format pattern string.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public Format(String pattern) {
        List<String> tokens = tokenize(pattern);
        int n = tokens.size() / 2;
        this.pattern = pattern;
        this.tagPaths = new int[n][];
        this.index = new int[n];
        this.types = new Type[n];
        this.offsets = new int[n];
        this.dateTimeOffsets = new Object[n];
        this.slices = new UnaryOperator[n];
        this.format = buildMessageFormat(tokens);
    }

    /**
     * A factory method to create a Format object from a string.
     *
     * @param s The format pattern string.
     * @return a new {@code Format} instance, or {@code null} if the input is null.
     */
    public static Format valueOf(String s) {
        return s != null ? new Format(s) : null;
    }

    /**
     * Creates a new Calendar instance for a given TimeZone, with all fields cleared.
     *
     * @param tz The TimeZone, or null for the default timezone.
     * @return A cleared Calendar instance.
     */
    private static Calendar cal(TimeZone tz) {
        Calendar cal = (tz != null) ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.clear();
        return cal;
    }

    /**
     * Creates a new Calendar instance for a given TimeZone and initializes it with a Date.
     *
     * @param tz   The TimeZone, or null for the default timezone.
     * @param date The Date to initialize the calendar with.
     * @return An initialized Calendar instance.
     */
    private static Calendar cal(TimeZone tz, Date date) {
        Calendar cal = (tz != null) ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    /**
     * Rounds up a Calendar to the end of the specified field (e.g., end of day, end of month).
     *
     * @param cal   The Calendar to modify.
     * @param field The Calendar field to round up (e.g., {@code Calendar.DAY_OF_MONTH}).
     */
    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(Calendar.MILLISECOND, -1);
    }

    /**
     * Formats a Date into a DICOM DA (Date) string "YYYYMMDD".
     *
     * @param tz   The TimeZone.
     * @param date The Date to format.
     * @return The formatted DA string.
     */
    public static String formatDA(TimeZone tz, Date date) {
        return formatDA(tz, date, new StringBuilder(8)).toString();
    }

    /**
     * Formats a Date into a DICOM DA (Date) string and appends it to a StringBuilder.
     *
     * @param tz         The TimeZone.
     * @param date       The Date to format.
     * @param toAppendTo The StringBuilder to append the result to.
     * @return The StringBuilder with the appended DA string.
     */
    public static StringBuilder formatDA(TimeZone tz, Date date, StringBuilder toAppendTo) {
        return formatDT(cal(tz, date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    /**
     * Formats a Date into a DICOM TM (Time) string "HHMMSS.sss".
     *
     * @param tz   The TimeZone.
     * @param date The Date to format.
     * @return The formatted TM string.
     */
    public static String formatTM(TimeZone tz, Date date) {
        return formatTM(tz, date, new DatePrecision());
    }

    /**
     * Formats a Date into a DICOM TM (Time) string with a specific precision.
     *
     * @param tz        The TimeZone.
     * @param date      The Date to format.
     * @param precision The precision to use for formatting.
     * @return The formatted TM string.
     */
    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return formatTM(cal(tz, date), new StringBuilder(10), precision.lastField).toString();
    }

    /**
     * Private helper to format a Calendar into a TM string.
     */
    private static StringBuilder formatTM(Calendar cal, StringBuilder toAppendTo, int lastField) {
        appendXX(cal.get(Calendar.HOUR_OF_DAY), toAppendTo);
        if (lastField > Calendar.HOUR_OF_DAY) {
            appendXX(cal.get(Calendar.MINUTE), toAppendTo);
            if (lastField > Calendar.MINUTE) {
                appendXX(cal.get(Calendar.SECOND), toAppendTo);
                if (lastField > Calendar.SECOND) {
                    toAppendTo.append(Symbol.C_DOT);
                    appendXXX(cal.get(Calendar.MILLISECOND), toAppendTo);
                }
            }
        }
        return toAppendTo;
    }

    /**
     * Formats a Date into a DICOM DT (DateTime) string.
     *
     * @param tz   The TimeZone.
     * @param date The Date to format.
     * @return The formatted DT string.
     */
    public static String formatDT(TimeZone tz, Date date) {
        return formatDT(tz, date, new DatePrecision());
    }

    /**
     * Formats a Date into a DICOM DT (DateTime) string with a specific precision.
     *
     * @param tz        The TimeZone.
     * @param date      The Date to format.
     * @param precision The precision to use for formatting.
     * @return The formatted DT string.
     */
    public static String formatDT(TimeZone tz, Date date, DatePrecision precision) {
        return formatDT(tz, date, new StringBuilder(23), precision).toString();
    }

    /**
     * Formats a Date into a DICOM DT (DateTime) string and appends it to a StringBuilder.
     *
     * @param tz         The TimeZone.
     * @param date       The Date to format.
     * @param toAppendTo The StringBuilder to append the result to.
     * @param precision  The precision to use for formatting.
     * @return The StringBuilder with the appended DT string.
     */
    public static StringBuilder formatDT(TimeZone tz, Date date, StringBuilder toAppendTo, DatePrecision precision) {
        Calendar cal = cal(tz, date);
        formatDT(cal, toAppendTo, precision.lastField);
        if (precision.includeTimezone) {
            int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
            appendZZZZZ(offset, toAppendTo);
        }
        return toAppendTo;
    }

    /**
     * Appends a timezone offset in milliseconds to a StringBuilder in "+HHMM" format.
     */
    private static StringBuilder appendZZZZZ(int offset, StringBuilder sb) {
        if (offset < 0) {
            offset = -offset;
            sb.append(Symbol.C_MINUS);
        } else
            sb.append(Symbol.C_PLUS);
        int min = offset / 60000;
        appendXX(min / 60, sb);
        appendXX(min % 60, sb);
        return sb;
    }

    /**
     * Returns the raw UTC timezone offset of a given TimeZone in (+|-)HHMM format, without daylight saving.
     *
     * @param tz The timezone.
     * @return The timezone offset from UTC in (+|-)HHMM format.
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz) {
        return appendZZZZZ(tz.getRawOffset(), new StringBuilder(5)).toString();
    }

    /**
     * Returns the UTC timezone offset of a given TimeZone on a specific date in (+|-)HHMM format, accounting for
     * daylight saving.
     *
     * @param tz   The timezone.
     * @param date The date to consider for DST, or null for the current date.
     * @return The timezone offset from UTC in (+|-)HHMM format.
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz, Date date) {
        return appendZZZZZ(
                tz.getOffset(date == null ? System.currentTimeMillis() : date.getTime()),
                new StringBuilder(5)).toString();
    }

    /**
     * Private helper to format a Calendar into a DT string up to a specified precision.
     */
    private static StringBuilder formatDT(Calendar cal, StringBuilder toAppendTo, int lastField) {
        appendXXXX(cal.get(Calendar.YEAR), toAppendTo);
        if (lastField > Calendar.YEAR) {
            appendXX(cal.get(Calendar.MONTH) + 1, toAppendTo);
            if (lastField > Calendar.MONTH) {
                appendXX(cal.get(Calendar.DAY_OF_MONTH), toAppendTo);
                if (lastField > Calendar.DAY_OF_MONTH) {
                    formatTM(cal, toAppendTo, lastField);
                }
            }
        }
        return toAppendTo;
    }

    /**
     * Appends a four-digit integer to a StringBuilder, padding with leading zeros.
     */
    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000)
            toAppendTo.append('0');
        appendXXX(i, toAppendTo);
    }

    /**
     * Appends a three-digit integer to a StringBuilder, padding with leading zeros.
     */
    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100)
            toAppendTo.append('0');
        appendXX(i, toAppendTo);
    }

    /**
     * Appends a two-digit integer to a StringBuilder, padding with a leading zero.
     */
    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10)
            toAppendTo.append('0');
        toAppendTo.append(i);
    }

    /**
     * Parses a DICOM DA string into a {@link LocalDate}.
     *
     * @param s The DA string (e.g., "20251009" or "2025.10.09").
     * @return The parsed {@link LocalDate}.
     * @throws IllegalArgumentException if the string is not a valid DA format.
     */
    public static LocalDate parseLocalDA(String s) {
        int length = s.length();
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4)) && s.charAt(7) == s.charAt(4)))
            throw new IllegalArgumentException(s);
        int pos = 0;
        int year = parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100 + parseDigit(s, pos++) * 10
                + parseDigit(s, pos++);
        if (length == 10)
            pos++;
        int month = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
        if (length == 10)
            pos++;
        int dayOfMonth = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);

        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Parses a DICOM DA string into a legacy {@link Date}.
     *
     * @param tz The timezone to interpret the date in.
     * @param s  The DA string.
     * @return The parsed {@link Date}.
     */
    public static Date parseDA(TimeZone tz, String s) {
        return parseDA(tz, s, false);
    }

    /**
     * Parses a DICOM DA string into a legacy {@link Date}, optionally rounding to the end of the day.
     *
     * @param tz   The timezone to interpret the date in.
     * @param s    The DA string.
     * @param ceil If true, the resulting date is set to the last millisecond of that day.
     * @return The parsed {@link Date}.
     */
    public static Date parseDA(TimeZone tz, String s, boolean ceil) {
        Calendar cal = cal(tz);
        int length = s.length();
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4)) && s.charAt(7) == s.charAt(4)))
            throw new IllegalArgumentException(s);
        int pos = 0;
        cal.set(
                Calendar.YEAR,
                parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100 + parseDigit(s, pos++) * 10
                        + parseDigit(s, pos++));
        if (length == 10)
            pos++;
        cal.set(Calendar.MONTH, parseDigit(s, pos++) * 10 + parseDigit(s, pos++) - 1);
        if (length == 10)
            pos++;
        cal.set(Calendar.DAY_OF_MONTH, parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
        if (ceil)
            ceil(cal, Calendar.DAY_OF_MONTH);
        return cal.getTime();
    }

    /**
     * Parses a DICOM TM string into a {@link LocalTime}, capturing the precision.
     *
     * @param s         The TM string (e.g., "103000.500").
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link LocalTime}.
     * @throws IllegalArgumentException if the string is not a valid TM format.
     */
    public static LocalTime parseLocalTM(String s, DatePrecision precision) {
        int length = s.length();
        int pos = 0;
        if (pos + 2 > length)
            throw new IllegalArgumentException(s);

        precision.lastField = Calendar.HOUR_OF_DAY;
        int hour = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
        int minute = 0;
        int second = 0;
        int nanos = 0;

        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);

            precision.lastField = Calendar.MINUTE;
            minute = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);

            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);

                precision.lastField = Calendar.SECOND;
                second = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
                int n = length - pos;
                if (n > 0) {
                    if (s.charAt(pos++) != '.')
                        throw new IllegalArgumentException(s);

                    int fraction = 1_000_000_000;
                    for (int i = 1; i < n; i++) {
                        int d = parseDigit(s, pos++);
                        nanos += d * (fraction /= 10);
                    }
                    precision.lastField = Calendar.MILLISECOND;
                }
            }
        }
        return LocalTime.of(hour, minute, second, nanos);
    }

    /**
     * Parses a DICOM TM string into a legacy {@link Date}.
     *
     * @param tz        The timezone.
     * @param s         The TM string.
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link Date}.
     */
    public static Date parseTM(TimeZone tz, String s, DatePrecision precision) {
        return parseTM(tz, s, false, precision);
    }

    /**
     * Parses a DICOM TM string into a legacy {@link Date}, optionally rounding up.
     *
     * @param tz        The timezone.
     * @param s         The TM string.
     * @param ceil      If true, rounds up to the end of the last specified time unit.
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link Date}.
     */
    public static Date parseTM(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
        return parseTM(cal(tz), truncateTimeZone(s), ceil, precision);
    }

    /**
     * Truncates the timezone part from a date-time string.
     */
    private static String truncateTimeZone(String s) {
        int length = s.length();
        if (length > 4) {
            char sign = s.charAt(length - 5);
            if (sign == '+' || sign == '-') {
                return s.substring(0, length - 5);
            }
        }
        return s;
    }

    /**
     * Private helper to parse a TM string into a Calendar.
     */
    private static Date parseTM(Calendar cal, String s, boolean ceil, DatePrecision precision) {
        int length = s.length();
        int pos = 0;
        if (pos + 2 > length)
            throw new IllegalArgumentException(s);

        cal.set(precision.lastField = Calendar.HOUR_OF_DAY, parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);

            cal.set(precision.lastField = Calendar.MINUTE, parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(precision.lastField = Calendar.SECOND, parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
                int n = length - pos;
                if (n > 0) {
                    if (s.charAt(pos++) != '.')
                        throw new IllegalArgumentException(s);

                    int d, millis = 0;
                    for (int i = 1; i < n; ++i) {
                        d = parseDigit(s, pos++);
                        if (i < 4)
                            millis += d;
                        else if (i == 4 & d > 4) // round up
                            millis++;
                        if (i < 3)
                            millis *= 10;
                    }
                    for (int i = n; i < 3; ++i)
                        millis *= 10;
                    cal.set(precision.lastField = Calendar.MILLISECOND, millis);
                    return cal.getTime();
                }
            }
        }
        if (ceil)
            ceil(cal, precision.lastField);
        return cal.getTime();
    }

    /**
     * Parses a single character into a digit.
     */
    private static int parseDigit(String s, int index) {
        int d = s.charAt(index) - '0';
        if (d < 0 || d > 9)
            throw new IllegalArgumentException(s);
        return d;
    }

    /**
     * Parses a DICOM DT string into a legacy {@link Date}.
     *
     * @param tz        The default timezone.
     * @param s         The DT string.
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link Date}.
     */
    public static Date parseDT(TimeZone tz, String s, DatePrecision precision) {
        return parseDT(tz, s, false, precision);
    }

    /**
     * Parses a DICOM DT string into a modern {@link Temporal} object.
     *
     * @param s         The DT string.
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link Temporal}, which may be a {@link LocalDateTime} or {@link ZonedDateTime}.
     */
    public static Temporal parseTemporalDT(String s, DatePrecision precision) {
        int length = s.length();
        ZoneOffset offset = safeZoneOffset(s);
        if (offset != null) {
            precision.includeTimezone = true;
            length -= 5;
        }

        int pos = 0;
        if (pos + 4 > length)
            throw new IllegalArgumentException(s);

        precision.lastField = Calendar.YEAR;
        int year = parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100 + parseDigit(s, pos++) * 10
                + parseDigit(s, pos++);
        int month = 1;
        int day = 1;
        LocalTime time = null;

        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);
            precision.lastField = Calendar.MONTH;
            month = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                precision.lastField = Calendar.DAY_OF_MONTH;
                day = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
                if (pos < length)
                    time = parseLocalTM(s.substring(pos, length), precision);
            }
        }

        if (time == null)
            time = LocalTime.of(0, 0);
        LocalDateTime dateTime = LocalDate.of(year, month, day).atTime(time);

        if (offset != null) {
            return dateTime.atOffset(offset);
        }

        return dateTime;
    }

    /**
     * Parses a timezone offset string (+HHMM or -HHMM) into a {@link TimeZone}.
     *
     * @param s The timezone offset string.
     * @return The corresponding {@link TimeZone}.
     * @throws IllegalArgumentException if the format is invalid.
     */
    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || (tz = safeTimeZone(s)) == null)
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        return tz;
    }

    /**
     * Safely parses a timezone offset string into a {@link ZoneOffset}.
     *
     * @param s The string which may end with a timezone offset.
     * @return The parsed {@link ZoneOffset}, or null if not present or invalid.
     */
    private static ZoneOffset safeZoneOffset(String s) {
        int length = s.length();
        if (length < 5) {
            return null;
        }

        int pos = length - 5;
        char sign = s.charAt(pos++);
        if (sign != '+' && sign != '-') {
            return null;
        }

        try {
            int hour = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);
            int minute = parseDigit(s, pos++) * 10 + parseDigit(s, pos++);

            if (sign == '-') {
                hour *= -1;
            }

            return ZoneOffset.ofHoursMinutes(hour, minute);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Safely parses a timezone offset string into a {@link TimeZone}.
     *
     * @param s The string which may end with a timezone offset.
     * @return The parsed {@link TimeZone}, or null if not present or invalid.
     */
    private static TimeZone safeTimeZone(String s) {
        String tzid = tzid(s);
        if (tzid == null)
            return null;

        TimeZone tz = cachedTimeZone;
        if (tz == null || !tz.getID().equals(tzid))
            cachedTimeZone = tz = TimeZone.getTimeZone(tzid);

        return tz;
    }

    /**
     * Converts a timezone offset string (+HHMM) into a GMT-based TimeZone ID ("GMT+HH:MM").
     */
    private static String tzid(String s) {
        int length = s.length();
        if (length > 4) {
            char[] tzid = { 'G', 'M', 'T', 0, 0, 0, Symbol.C_COLON, 0, 0 };
            s.getChars(length - 5, length - 2, tzid, 3);
            s.getChars(length - 2, length, tzid, 7);
            if ((tzid[3] == '+' || tzid[3] == '-') && Character.isDigit(tzid[4]) && Character.isDigit(tzid[5])
                    && Character.isDigit(tzid[7]) && Character.isDigit(tzid[8])) {
                return new String(tzid);
            }
        }
        return null;
    }

    /**
     * Parses a DICOM DT string into a legacy {@link Date}, optionally rounding up.
     *
     * @param tz        The default timezone.
     * @param s         The DT string.
     * @param ceil      If true, rounds up to the end of the last specified time unit.
     * @param precision A {@link DatePrecision} object to store the detected precision.
     * @return The parsed {@link Date}.
     */
    public static Date parseDT(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
        int length = s.length();
        TimeZone tz1 = safeTimeZone(s);
        if (precision.includeTimezone = tz1 != null) {
            length -= 5;
            tz = tz1;
        }
        Calendar cal = cal(tz);
        int pos = 0;
        if (pos + 4 > length)
            throw new IllegalArgumentException(s);
        cal.set(
                precision.lastField = Calendar.YEAR,
                parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100 + parseDigit(s, pos++) * 10
                        + parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);
            cal.set(precision.lastField = Calendar.MONTH, parseDigit(s, pos++) * 10 + parseDigit(s, pos++) - 1);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(precision.lastField = Calendar.DAY_OF_MONTH, parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
                if (pos < length)
                    return parseTM(cal, s.substring(pos, length), ceil, precision);
            }
        }
        if (ceil)
            ceil(cal, precision.lastField);
        return cal.getTime();
    }

    /**
     * Parses a DICOM DA string into a {@link LocalDate} using a formatter.
     *
     * @param value The DA string.
     * @return The parsed {@link LocalDate}.
     */
    public static LocalDate parseDA(String value) {
        return LocalDate.from(DA_PARSER.parse(value.trim()));
    }

    /**
     * Formats a {@link Temporal} object into a DICOM DA string.
     *
     * @param value The temporal object (e.g., LocalDate).
     * @return The formatted DA string.
     */
    public static String formatDA(Temporal value) {
        return DA_FORMATTER.format(value);
    }

    /**
     * Parses a DICOM TM string into a {@link LocalTime} using a formatter.
     *
     * @param value The TM string.
     * @return The parsed {@link LocalTime}.
     */
    public static LocalTime parseTM(String value) {
        return LocalTime.from(TM_PARSER.parse(value.trim()));
    }

    /**
     * Parses a DICOM TM string and returns a {@link LocalTime} representing the end of the specified time range.
     *
     * @param value The TM string.
     * @return The parsed {@link LocalTime} adjusted to the maximum value of its precision.
     */
    public static LocalTime parseTMMax(String value) {
        return parseTM(value).plusNanos(nanosToAdd(value));
    }

    /**
     * Formats a {@link Temporal} object into a DICOM TM string.
     *
     * @param value The temporal object (e.g., LocalTime).
     * @return The formatted TM string.
     */
    public static String formatTM(Temporal value) {
        return TM_FORMATTER.format(value);
    }

    /**
     * Parses a DICOM DT string into a {@link Temporal} object using a formatter.
     *
     * @param value The DT string.
     * @return The parsed {@link Temporal}, which may be a {@link LocalDateTime} or {@link ZonedDateTime}.
     */
    public static Temporal parseDT(String value) {
        TemporalAccessor temporal = DT_PARSER.parse(value.trim());
        LocalDate date = temporal.isSupported(DAY_OF_MONTH) ? LocalDate.from(temporal)
                : LocalDate.of(temporal.get(YEAR), getMonth(temporal), 1);
        LocalTime time = temporal.isSupported(HOUR_OF_DAY) ? LocalTime.from(temporal) : LocalTime.MIN;
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return temporal.isSupported(OFFSET_SECONDS)
                ? ZonedDateTime.of(dateTime, ZoneOffset.ofTotalSeconds(temporal.get(OFFSET_SECONDS)))
                : dateTime;
    }

    /**
     * Combines a {@link LocalDate} and a {@link LocalTime} into a {@link LocalDateTime}.
     *
     * @param date The date part. Can be null.
     * @param time The time part. If null, the start of the day is assumed.
     * @return The combined {@link LocalDateTime}, or null if the date is null.
     */
    public static LocalDateTime dateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        if (time == null) {
            return date.atStartOfDay();
        }
        return LocalDateTime.of(date, time);
    }

    /**
     * Combines two integer tags (for date and time) into a single long value for use as a key.
     *
     * @param tagDate The date tag.
     * @param tagTime The time tag.
     * @return A long value combining both tags.
     */
    public static long combineTags(int tagDate, int tagTime) {
        return ((long) tagDate << 32) | (tagTime & 0xFFFFFFFFL);
    }

    /**
     * Creates a legacy {@link Date} object by combining date and time values from two separate DICOM attributes.
     *
     * @param dcm     The DICOM attributes dataset.
     * @param tagDate The tag for the date attribute.
     * @param tagTime The tag for the time attribute.
     * @return The combined {@link Date} object, or null if dcm is null.
     */
    public static Date dateTime(Attributes dcm, int tagDate, int tagTime) {
        if (dcm == null) {
            return null;
        }

        return dcm.getDate(combineTags(tagDate, tagTime));
    }

    /**
     * Creates a legacy {@link Date} object by combining separate date and time objects.
     *
     * @param tz                   The time zone.
     * @param date                 The date object.
     * @param time                 The time object.
     * @param acceptNullDateOrTime If false, returns null if either date or time is null.
     * @return The combined {@link Date} object.
     */
    public static Date dateTime(TimeZone tz, Date date, Date time, boolean acceptNullDateOrTime) {
        if (!acceptNullDateOrTime && (date == null || time == null)) {
            return null;
        }
        Calendar calendar = tz == null || date == null ? Calendar.getInstance() : Calendar.getInstance(tz);

        Calendar datePart = Calendar.getInstance();
        datePart.setTime(date == null ? new Date(0) : date);
        calendar.set(Calendar.YEAR, datePart.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, datePart.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, datePart.get(Calendar.DAY_OF_MONTH));

        Calendar timePart = Calendar.getInstance();
        timePart.setTime(time == null ? new Date(0) : time);
        calendar.set(Calendar.HOUR_OF_DAY, timePart.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timePart.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timePart.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, timePart.get(Calendar.MILLISECOND));

        return calendar.getTime();
    }

    /**
     * Gets the month from a temporal accessor, defaulting to 1 if not present.
     */
    private static int getMonth(TemporalAccessor temporal) {
        return temporal.isSupported(MONTH_OF_YEAR) ? temporal.get(MONTH_OF_YEAR) : 1;
    }

    /**
     * Parses a DICOM DT string and returns a {@link Temporal} representing the end of the specified time range.
     *
     * @param value The DT string.
     * @return The parsed {@link Temporal} adjusted to the maximum value of its precision.
     */
    public static Temporal parseDTMax(String value) {
        int length = lengthWithoutZone(value);
        return length > 8 ? parseDT(value).plus(nanosToAdd(length - 8), ChronoUnit.NANOS)
                : parseDT(value).plus(1, yearsMonthsDays(length)).minus(1, ChronoUnit.NANOS);
    }

    /**
     * Formats a {@link Temporal} object into a DICOM DT string.
     *
     * @param value The temporal object.
     * @return The formatted DT string.
     */
    public static String formatDT(Temporal value) {
        return DT_FORMATTER.format(value);
    }

    /**
     * Truncates a DICOM TM string to a specified maximum length.
     *
     * @param value     The TM string.
     * @param maxLength The maximum allowed length.
     * @return The truncated string.
     */
    public static String truncateTM(String value, int maxLength) {
        if (maxLength < 2)
            throw new IllegalArgumentException("maxLength " + maxLength + " < 2");

        return truncate(value, value.length(), maxLength, 8);
    }

    /**
     * Truncates a DICOM DT string to a specified maximum length, preserving the timezone.
     *
     * @param value     The DT string.
     * @param maxLength The maximum allowed length.
     * @return The truncated string.
     */
    public static String truncateDT(String value, int maxLength) {
        if (maxLength < 4)
            throw new IllegalArgumentException("maxLength " + maxLength + " < 4");

        int index = indexOfZone(value);
        return index < 0 ? truncate(value, value.length(), maxLength, 16)
                : truncate(value, index, maxLength, 16) + value.substring(index);
    }

    /**
     * Calculates nanoseconds to add to a time string to get to the end of its precision.
     */
    private static long nanosToAdd(String tm) {
        int length = tm.length();
        int index = tm.lastIndexOf(':');
        if (index > 0) {
            length--;
            if (index > 4)
                length--;
        }
        return nanosToAdd(length);
    }

    /**
     * Calculates nanoseconds to add based on the length of the time part.
     */
    private static long nanosToAdd(int length) {
        return switch (length) {
            case 2 -> 3599999999999L; // HH -> end of hour
            case 4 -> 59999999999L; // HHMM -> end of minute
            case 6, 7 -> 999999999L; // HHMMSS -> end of second
            case 8 -> 99999999L; // .F
            case 9 -> 9999999L; // .FF
            case 10 -> 999999L; // .FFF (ms)
            case 11 -> 99999L;
            case 12 -> 9999L;
            case 13 -> 999L;
            default -> throw new IllegalArgumentException("length: " + length);
        };
    }

    /**
     * Returns the appropriate ChronoUnit (YEARS, MONTHS, DAYS) based on the length of a partial DA string.
     */
    private static ChronoUnit yearsMonthsDays(int length) {
        return switch (length) {
            case 4 -> ChronoUnit.YEARS;
            case 6 -> ChronoUnit.MONTHS;
            case 8 -> ChronoUnit.DAYS;
            default -> throw new IllegalArgumentException("length: " + length);
        };
    }

    /**
     * Returns the length of a DT string, excluding the timezone part.
     */
    private static int lengthWithoutZone(String value) {
        int index = indexOfZone(value);
        return index < 0 ? value.length() : index;
    }

    /**
     * Finds the starting index of the timezone part in a DT string.
     */
    private static int indexOfZone(String value) {
        int index = value.length() - 5;
        return index >= 4 && isSign(value.charAt(index)) ? index : -1;
    }

    /**
     * Checks if a character is a '+' or '-' sign.
     */
    private static boolean isSign(char ch) {
        return ch == '+' || ch == '-';
    }

    /**
     * Private helper to truncate a string to a max length.
     */
    private static String truncate(String value, int length, int maxLength, int fractionPos) {
        return value.substring(0, adjustMaxLength(Math.min(length, maxLength), fractionPos));
    }

    /**
     * Adjusts max length for time values to avoid cutting in the middle of a component.
     */
    private static int adjustMaxLength(int maxLength, int fractionPos) {
        return maxLength < fractionPos ? maxLength & ~1 : maxLength;
    }

    /**
     * Converts a temporal object (like LocalDate, LocalTime) to a medium-style localized string.
     *
     * @param date The temporal object.
     * @return The formatted string.
     */
    public static String formatDateTime(TemporalAccessor date) {
        return formatDateTime(date, Locale.getDefault());
    }

    /**
     * Converts a temporal object to a medium-style string for a specific locale.
     *
     * @param date   The temporal object.
     * @param locale The locale for formatting.
     * @return The formatted string.
     */
    public static String formatDateTime(TemporalAccessor date, Locale locale) {
        if (date instanceof LocalDate) {
            return defaultDateFormatter.withLocale(locale).format(date);
        } else if (date instanceof LocalTime) {
            return defaultTimeFormatter.withLocale(locale).format(date);
        } else if (date instanceof LocalDateTime || date instanceof ZonedDateTime) {
            return defaultDateTimeFormatter.withLocale(locale).format(date);
        } else if (date instanceof Instant) {
            return defaultDateTimeFormatter.withLocale(locale).format(((Instant) date).atZone(ZoneId.systemDefault()));
        }
        return "";
    }

    /**
     * Converts a legacy {@link Date} to a {@link LocalDate}.
     *
     * @param date The date to convert.
     * @return The converted {@link LocalDate}, or null if input is null.
     */
    public static LocalDate toLocalDate(Date date) {
        if (date != null) {
            LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return datetime.toLocalDate();
        }
        return null;
    }

    /**
     * Converts a legacy {@link Date} to a {@link LocalTime}.
     *
     * @param date The date to convert.
     * @return The converted {@link LocalTime}, or null if input is null.
     */
    public static LocalTime toLocalTime(Date date) {
        if (date != null) {
            LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return datetime.toLocalTime();
        }
        return null;
    }

    /**
     * Converts a legacy {@link Date} to a {@link LocalDateTime}.
     *
     * @param date The date to convert.
     * @return The converted {@link LocalDateTime}, or null if input is null.
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    /**
     * Parses an XML Schema dateTime string (ISO 8601) into a {@link GregorianCalendar}.
     *
     * @param s The XML dateTime string.
     * @return The parsed {@link GregorianCalendar}.
     * @throws DatatypeConfigurationException if the JAXP factory cannot be configured.
     * @throws IllegalArgumentException       if the input string is invalid.
     */
    public static GregorianCalendar parseXmlDateTime(CharSequence s) throws DatatypeConfigurationException {
        if (!StringKit.hasText(s.toString())) {
            throw new IllegalArgumentException("Input CharSequence cannot be null or empty");
        }
        String val = s.toString().trim();
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(val).toGregorianCalendar();
    }

    /**
     * Tokenizes the format pattern string into literal parts and placeholder parts.
     *
     * @param s The pattern string.
     * @return A list of tokens.
     */
    private List<String> tokenize(String s) {
        List<String> result = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(s, "{}", true);
        String tk;
        char delim;
        char prevDelim = '}';
        int level = 0;
        StringBuilder sb = new StringBuilder();
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            delim = tk.charAt(0);
            if (delim == '{') {
                if (level++ == 0) {
                    if (prevDelim == '}')
                        result.add("");
                } else {
                    sb.append(delim);
                }
            } else if (delim == '}') {
                if (--level == 0) {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else if (level > 0) {
                    sb.append(delim);
                } else
                    throw new IllegalArgumentException(s);
            } else {
                if (level == 0)
                    result.add(tk);
                else
                    sb.append(tk);
            }
            prevDelim = delim;
        }
        return result;
    }

    /**
     * Builds the internal state of the formatter by parsing the tokens from the pattern.
     *
     * @param tokens The list of tokens.
     * @return The configured {@link MessageFormat} instance.
     */
    private MessageFormat buildMessageFormat(List<String> tokens) {
        StringBuilder formatBuilder = new StringBuilder(pattern.length());
        int j = 0;
        for (int i = 0; i < tagPaths.length; i++) {
            formatBuilder.append(tokens.get(j++)).append('{').append(i);
            String tagStr = tokens.get(j++);
            int typeStart = tagStr.indexOf(Symbol.C_COMMA) + 1;
            boolean rnd = tagStr.startsWith("rnd");
            if (!rnd && !tagStr.startsWith("now")) {
                int tagStrLen = typeStart != 0 ? typeStart - 1 : tagStr.length();

                int indexStart = tagStr.charAt(tagStrLen - 1) == Symbol.C_BRACKET_RIGHT
                        ? tagStr.lastIndexOf(Symbol.C_BRACKET_LEFT, tagStrLen - 3) + 1
                        : 0;
                try {
                    tagPaths[i] = Tag.parseTagPath(tagStr.substring(0, indexStart != 0 ? indexStart - 1 : tagStrLen));
                    if (indexStart != 0)
                        index[i] = Integer.parseInt(tagStr.substring(indexStart, tagStrLen - 1));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(pattern);
                }
            }
            if (typeStart != 0) {
                int typeEnd = tagStr.indexOf(Symbol.C_COMMA, typeStart);
                if (typeEnd < 0)
                    typeEnd = tagStr.length();
                try {
                    if (tagStr.startsWith("date", typeStart)) {
                        types[i] = Type.date;
                        if (typeStart + 4 < typeEnd) {
                            dateTimeOffsets[i] = Period.parse(tagStr.substring(typeStart + 4, typeEnd));
                        }
                    } else if (tagStr.startsWith("time", typeStart)) {
                        types[i] = Type.time;
                        if (typeStart + 4 < typeEnd) {
                            dateTimeOffsets[i] = Duration.parse(tagStr.substring(typeStart + 4, typeEnd));
                        }
                    } else {
                        types[i] = Type.valueOf(tagStr.substring(typeStart, typeEnd));
                    }
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    throw new IllegalArgumentException(pattern);
                }
                switch (types[i]) {
                    case number, date, time, choice:
                        formatBuilder.append(',').append(types[i]).append(tagStr.substring(typeEnd));
                        break;

                    case offset:
                        try {
                            offsets[i] = Integer.parseInt(tagStr.substring(typeEnd + 1));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(pattern);
                        }
                    case slice:
                        try {
                            slices[i] = new Slice(tagStr.substring(typeEnd + 1));
                        } catch (RuntimeException e) {
                            throw new IllegalArgumentException(pattern);
                        }
                }
            } else {
                types[i] = Type.none;
            }
            if (rnd) {
                switch (types[i]) {
                    case none:
                        types[i] = Type.rnd;
                    case uuid, uid:
                        break;

                    default:
                        throw new IllegalArgumentException(pattern);
                }
            }
            formatBuilder.append('}');
        }
        if (j < tokens.size())
            formatBuilder.append(tokens.get(j));
        try {
            return new MessageFormat(formatBuilder.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pattern);
        }
    }

    /**
     * Formats an object to produce a string.
     *
     * @param object The object to format, must be an {@link Attributes} instance.
     * @param result A {@link StringBuffer} to append the resulting text to.
     * @param pos    A {@link FieldPosition} tracking the formatting.
     * @return The string buffer passed in as {@code result}.
     */
    @Override
    public StringBuffer format(Object object, StringBuffer result, FieldPosition pos) {
        return format.format(toArgs((Attributes) object), result, pos);
    }

    /**
     * Converts a DICOM Attributes object into an array of arguments for the MessageFormat.
     *
     * @param attrs The DICOM attributes.
     * @return An array of objects to be formatted.
     */
    private Object[] toArgs(Attributes attrs) {
        Object[] args = new Object[tagPaths.length];
        outer: for (int i = 0; i < args.length; i++) {
            Attributes item = attrs;
            int tag = 0;
            int[] tagPath = tagPaths[i];
            if (tagPath != null) { // !now
                int last = tagPath.length - 1;
                tag = tagPath[last];
                for (int j = 0; j < last; j++) {
                    item = item.getNestedDataset(tagPath[j]);
                    if (item == null)
                        continue outer;
                }
            }
            args[i] = types[i].toArg(item, tag, index[i], offsets[i], dateTimeOffsets[i], slices[i]);
        }
        return args;
    }

    /**
     * Parsing is not supported by this formatter.
     *
     * @param source The string to parse.
     * @param pos    The parse position.
     * @return Throws {@link UnsupportedOperationException}.
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the original pattern string.
     *
     * @return the pattern string.
     */
    @Override
    public String toString() {
        return pattern;
    }

    /**
     * Defines the various formatting types supported by the pattern language.
     */
    private enum Type {

        /** No special formatting, returns the raw string value. */
        none {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getString(tag, index, "");
            }
        },
        /** Converts the string value to uppercase. */
        upper {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getString(tag, index, "").toUpperCase();
            }
        },
        /** Extracts a substring from the value. */
        slice {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> slice) {
                return slice.apply(attrs.getString(tag, index));
            }
        },
        /** Formats the value as a number. */
        number {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        /** Adds a numeric offset to an integer value. */
        offset {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return Integer.toString(attrs.getInt(tag, index, 0) + offset);
            }
        },
        /** Formats the value as a date, with optional period offset. */
        date {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                Date date = tag != 0 ? attrs.getDate(tag, index) : new Date();
                if (!(dateTimeOffset instanceof Period dateOffset))
                    return date;
                Calendar cal = Calendar.getInstance(attrs.getTimeZone());
                cal.setTime(date);
                cal.add(Calendar.YEAR, dateOffset.getYears());
                cal.add(Calendar.MONTH, dateOffset.getMonths());
                cal.add(Calendar.DAY_OF_MONTH, dateOffset.getDays());
                return cal.getTime();
            }
        },
        /** Formats the value as a time, with optional duration offset. */
        time {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                Date date = tag != 0 ? attrs.getDate(tag, index) : new Date();
                if (!(dateTimeOffset instanceof Duration timeOffset))
                    return date;
                Calendar cal = Calendar.getInstance(attrs.getTimeZone());
                cal.setTime(date);
                cal.add(Calendar.SECOND, (int) timeOffset.getSeconds());
                return cal.getTime();
            }
        },
        /** Formats a numeric value using a choice pattern. */
        choice {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        /** Computes the hash code of the string value. */
        hash {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? Tag.toHexString(s.hashCode()) : null;
            }
        },
        /** Computes the MD5 hash of the string value. */
        md5 {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? getMD5String(s) : null;
            }
        },
        /** URL-encodes the string value. */
        urlencoded {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? URLEncoder.encode(s, Charset.UTF_8) : null;
            }
        },
        /** Generates a random integer and formats it as hex. */
        rnd {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return Tag.toHexString(ThreadLocalRandom.current().nextInt());
            }
        },
        /** Generates a random UUID. */
        uuid {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return UUID.randomUUID();
            }
        },
        /** Generates a new DICOM UID. */
        uid {

            @Override
            Object toArg(
                    Attributes attrs,
                    int tag,
                    int index,
                    int offset,
                    Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return UID.createUID();
            }
        };

        /**
         * Converts a 16-byte array into a 26-character base-32 string.
         *
         * @param ba The byte array (must be 16 bytes).
         * @return The base-32 encoded string.
         */
        static String toString32(byte[] ba) {
            long l1 = toLong(ba, 0);
            long l2 = toLong(ba, LONG_BYTES);
            char[] ca = new char[26];
            for (int i = 0; i < 12; i++) {
                ca[i] = CHARS[(int) l1 & 0x1f];
                l1 = l1 >>> 5;
            }
            l1 = l1 | (l2 & 1) << 4;
            ca[12] = CHARS[(int) l1 & 0x1f];
            l2 = l2 >>> 1;
            for (int i = 13; i < 26; i++) {
                ca[i] = CHARS[(int) l2 & 0x1f];
                l2 = l2 >>> 5;
            }

            return new String(ca);
        }

        /**
         * Converts a portion of a byte array into a long.
         *
         * @param ba     The byte array.
         * @param offset The starting offset.
         * @return The resulting long value.
         */
        static long toLong(byte[] ba, int offset) {
            long l = 0;
            for (int i = offset, len = offset + LONG_BYTES; i < len; i++) {
                l |= ba[i] & 0xFF;
                if (i < len - 1)
                    l <<= 8;
            }
            return l;
        }

        /**
         * Abstract method to convert a DICOM attribute value to an argument for MessageFormat.
         *
         * @param attrs          The DICOM attributes.
         * @param tag            The DICOM tag to access.
         * @param index          The value index for multi-valued attributes.
         * @param offset         A numeric offset.
         * @param dateTimeOffset A date/time offset.
         * @param splice         A string slice operator.
         * @return The object to be used as an argument for formatting.
         */
        abstract Object toArg(
                Attributes attrs,
                int tag,
                int index,
                int offset,
                Object dateTimeOffset,
                UnaryOperator<String> splice);

        /**
         * Computes the MD5 hash of a string and returns it in a custom base-32 format.
         *
         * @param s The string to hash.
         * @return The hashed string.
         */
        String getMD5String(String s) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(s == null ? new byte[0] : s.getBytes(Charset.UTF_8));
                return toString32(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                return s;
            }
        }
    }

    /**
     * An operator for extracting a slice (substring) from a string.
     */
    private class Slice implements UnaryOperator<String> {

        /**
         * The starting index of the slice.
         */
        final int beginIndex;
        /**
         * The ending index of the slice.
         */
        final int endIndex;

        /**
         * Constructs a Slice operator from a string representation (e.g., "1,5" or "-10").
         *
         * @param s The string defining the slice indices.
         */
        public Slice(String s) {
            String[] ss = Builder.split(s, ',');
            if (ss.length == 1) {
                beginIndex = Integer.parseInt(ss[0]);
                endIndex = 0;
            } else if (ss.length == 2) {
                endIndex = Integer.parseInt(ss[1]);
                beginIndex = endIndex != 0 ? Integer.parseInt(ss[0]) : 0;
            } else {
                throw new IllegalArgumentException(s);
            }
        }

        /**
         * Applies the slice operation to a given string.
         *
         * @param s The string to slice.
         * @return The resulting substring, or an empty string on error.
         */
        @Override
        public String apply(String s) {
            try {
                int l = s.length();
                return endIndex == 0 ? s.substring(beginIndex < 0 ? Math.max(0, l + beginIndex) : beginIndex)
                        : s.substring(
                                beginIndex < 0 ? Math.max(0, l + beginIndex) : beginIndex,
                                endIndex < 0 ? l + endIndex : Math.min(l, endIndex));
            } catch (RuntimeException e) {
                return "";
            }
        }
    }

}
