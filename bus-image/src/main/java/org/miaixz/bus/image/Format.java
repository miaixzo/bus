/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.DatePrecision;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Format extends java.text.Format {

    private static final long serialVersionUID = -1L;

    private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v' };
    private static final int LONG_BYTES = 8;
    private static final DateTimeFormatter DA_PARSER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .optionalStart().appendLiteral('.').optionalEnd().appendValue(MONTH_OF_YEAR, 2).optionalStart()
            .appendLiteral('.').optionalEnd().appendValue(DAY_OF_MONTH, 2).toFormatter();
    private static final DateTimeFormatter DA_FORMATTER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2).toFormatter();
    private static final DateTimeFormatter TM_PARSER = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
            .optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart().optionalStart().appendLiteral(':').optionalEnd().appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart().appendFraction(NANO_OF_SECOND, 0, 6, true).toFormatter();
    private static final DateTimeFormatter TM_FORMATTER = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2).appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 6, 6, true)
            .toFormatter();
    private static final DateTimeFormatter DT_PARSER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .optionalStart().appendValue(MONTH_OF_YEAR, 2).optionalStart().appendValue(DAY_OF_MONTH, 2).optionalStart()
            .appendValue(HOUR_OF_DAY, 2).optionalStart().appendValue(MINUTE_OF_HOUR, 2).optionalStart()
            .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendFraction(NANO_OF_SECOND, 0, 6, true).optionalEnd()
            .optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalStart()
            .appendOffset("+HHMM", "+0000").toFormatter();
    private static final DateTimeFormatter DT_FORMATTER = new DateTimeFormatterBuilder().appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2).appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2).appendValue(SECOND_OF_MINUTE, 2).appendFraction(NANO_OF_SECOND, 6, 6, true)
            .optionalStart().appendOffset("+HHMM", "+0000").toFormatter();
    /**
     * Conversion from old to new Time API
     */
    private static final DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter defaultTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
    private static final DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM);
    private static TimeZone cachedTimeZone;

    private final String pattern;
    private final int[][] tagPaths;
    private final int[] index;
    private final int[] offsets;
    private final Object[] dateTimeOffsets;
    private final UnaryOperator<String>[] slices;
    private final Type[] types;
    private final MessageFormat format;

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

    public static Format valueOf(String s) {
        return s != null ? new Format(s) : null;
    }

    private static Calendar cal(TimeZone tz) {
        Calendar cal = (tz != null) ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.clear();
        return cal;
    }

    private static Calendar cal(TimeZone tz, Date date) {
        Calendar cal = (tz != null) ? new GregorianCalendar(tz) : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(Calendar.MILLISECOND, -1);
    }

    public static String formatDA(TimeZone tz, Date date) {
        return formatDA(tz, date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(TimeZone tz, Date date, StringBuilder toAppendTo) {
        return formatDT(cal(tz, date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    public static String formatTM(TimeZone tz, Date date) {
        return formatTM(tz, date, new DatePrecision());
    }

    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return formatTM(cal(tz, date), new StringBuilder(10), precision.lastField).toString();
    }

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

    public static String formatDT(TimeZone tz, Date date) {
        return formatDT(tz, date, new DatePrecision());
    }

    public static String formatDT(TimeZone tz, Date date, DatePrecision precision) {
        return formatDT(tz, date, new StringBuilder(23), precision).toString();
    }

    public static StringBuilder formatDT(TimeZone tz, Date date, StringBuilder toAppendTo, DatePrecision precision) {
        Calendar cal = cal(tz, date);
        formatDT(cal, toAppendTo, precision.lastField);
        if (precision.includeTimezone) {
            int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
            appendZZZZZ(offset, toAppendTo);
        }
        return toAppendTo;
    }

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
     * 返回指定时区的格式{@code (+|i)HHMM}的UTC时区偏移量，不涉及夏令时(DST).
     *
     * @param tz 时区
     * @return 来自UTC的时区偏移量，格式为{@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz) {
        return appendZZZZZ(tz.getRawOffset(), new StringBuilder(5)).toString();
    }

    /**
     * 在指定日期以指定时区的格式{@code(+|i)HHMM}从UTC返回时区偏移量 如果未指定日期，则考虑当前日期为夏令时
     *
     * @param tz   时区
     * @param date 日期或{@code null}
     * @return 来自UTC的时区偏移量，格式为{@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz, Date date) {
        return appendZZZZZ(tz.getOffset(date == null ? System.currentTimeMillis() : date.getTime()),
                new StringBuilder(5)).toString();
    }

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

    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000)
            toAppendTo.append('0');
        appendXXX(i, toAppendTo);
    }

    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100)
            toAppendTo.append('0');
        appendXX(i, toAppendTo);
    }

    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10)
            toAppendTo.append('0');
        toAppendTo.append(i);
    }

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

    public static Date parseDA(TimeZone tz, String s) {
        return parseDA(tz, s, false);
    }

    public static Date parseDA(TimeZone tz, String s, boolean ceil) {
        Calendar cal = cal(tz);
        int length = s.length();
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4)) && s.charAt(7) == s.charAt(4)))
            throw new IllegalArgumentException(s);
        int pos = 0;
        cal.set(Calendar.YEAR, parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100 + parseDigit(s, pos++) * 10
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

                    int fraction = 1_00_000_000;
                    int d;
                    for (int i = 1; i < n; i++) {
                        d = parseDigit(s, pos++);
                        nanos += d * fraction;
                        fraction /= 10;
                    }
                    // TODO this is not accurate as the precision can also be higher (microsecond)
                    precision.lastField = Calendar.MILLISECOND;
                }
            }
        }
        return LocalTime.of(hour, minute, second, nanos);
    }

    public static Date parseTM(TimeZone tz, String s, DatePrecision precision) {
        return parseTM(tz, s, false, precision);
    }

    public static Date parseTM(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
        return parseTM(cal(tz), truncateTimeZone(s), ceil, precision);
    }

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

    private static int parseDigit(String s, int index) {
        int d = s.charAt(index) - '0';
        if (d < 0 || d > 9)
            throw new IllegalArgumentException(s);
        return d;
    }

    public static Date parseDT(TimeZone tz, String s, DatePrecision precision) {
        return parseDT(tz, s, false, precision);
    }

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
        int month = 0;
        int day = 0;
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

    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || (tz = safeTimeZone(s)) == null)
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        return tz;
    }

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

    private static TimeZone safeTimeZone(String s) {
        String tzid = tzid(s);
        if (tzid == null)
            return null;

        TimeZone tz = cachedTimeZone;
        if (tz == null || !tz.getID().equals(tzid))
            cachedTimeZone = tz = TimeZone.getTimeZone(tzid);

        return tz;
    }

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
        cal.set(precision.lastField = Calendar.YEAR, parseDigit(s, pos++) * 1000 + parseDigit(s, pos++) * 100
                + parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
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

    public static LocalDate parseDA(String value) {
        return LocalDate.from(DA_PARSER.parse(value.trim()));
    }

    public static String formatDA(Temporal value) {
        return DA_FORMATTER.format(value);
    }

    public static LocalTime parseTM(String value) {
        return LocalTime.from(TM_PARSER.parse(value.trim()));
    }

    public static LocalTime parseTMMax(String value) {
        return parseTM(value).plusNanos(nanosToAdd(value));
    }

    public static String formatTM(Temporal value) {
        return TM_FORMATTER.format(value);
    }

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

    public static LocalDateTime dateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        if (time == null) {
            return date.atStartOfDay();
        }
        return LocalDateTime.of(date, time);
    }

    private static int getMonth(TemporalAccessor temporal) {
        return temporal.isSupported(MONTH_OF_YEAR) ? temporal.get(MONTH_OF_YEAR) : 1;
    }

    public static Temporal parseDTMax(String value) {
        int length = lengthWithoutZone(value);
        return length > 8 ? parseDT(value).plus(nanosToAdd(length - 8), ChronoUnit.NANOS)
                : parseDT(value).plus(1, yearsMonthsDays(length)).minus(1, ChronoUnit.NANOS);
    }

    public static String formatDT(Temporal value) {
        return DT_FORMATTER.format(value);
    }

    public static String truncateTM(String value, int maxLength) {
        if (maxLength < 2)
            throw new IllegalArgumentException("maxLength %d < 2" + maxLength);

        return truncate(value, value.length(), maxLength, 8);
    }

    public static String truncateDT(String value, int maxLength) {
        if (maxLength < 4)
            throw new IllegalArgumentException("maxLength %d < 4" + maxLength);

        int index = indexOfZone(value);
        return index < 0 ? truncate(value, value.length(), maxLength, 16)
                : truncate(value, index, maxLength, 16) + value.substring(index);
    }

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

    private static long nanosToAdd(int length) {
        return switch (length) {
        case 2 -> 3599999999999L;
        case 4 -> 59999999999L;
        case 6, 7 -> 999999999L;
        case 8 -> 99999999L;
        case 9 -> 9999999L;
        case 10 -> 999999L;
        case 11 -> 99999L;
        case 12 -> 9999L;
        case 13 -> 999L;
        default -> throw new IllegalArgumentException("length: " + length);
        };
    }

    private static ChronoUnit yearsMonthsDays(int length) {
        return switch (length) {
        case 4 -> ChronoUnit.YEARS;
        case 6 -> ChronoUnit.MONTHS;
        case 8 -> ChronoUnit.DAYS;
        default -> throw new IllegalArgumentException("length: " + length);
        };
    }

    private static int lengthWithoutZone(String value) {
        int index = indexOfZone(value);
        return index < 0 ? value.length() : index;
    }

    private static int indexOfZone(String value) {
        int index = value.length() - 5;
        return index >= 4 && isSign(value.charAt(index)) ? index : -1;
    }

    private static boolean isSign(char ch) {
        return ch == '+' || ch == '-';
    }

    private static String truncate(String value, int length, int maxLength, int fractionPos) {
        return value.substring(0, adjustMaxLength(Math.min(length, maxLength), fractionPos));
    }

    private static int adjustMaxLength(int maxLength, int fractionPos) {
        return maxLength < fractionPos ? maxLength & ~1 : maxLength;
    }

    /**
     * Convert date or time object to display date in String with FormatStyle.MEDIUM
     *
     * @param date the date or time object
     * @return the time to display with FormatStyle.MEDIUM
     */
    public static String formatDateTime(TemporalAccessor date) {
        return formatDateTime(date, Locale.getDefault());
    }

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

    public static LocalDate toLocalDate(Date date) {
        if (date != null) {
            LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return datetime.toLocalDate();
        }
        return null;
    }

    public static LocalTime toLocalTime(Date date) {
        if (date != null) {
            LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return datetime.toLocalTime();
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    public static Date dateTime(Date date, Date time) {
        if (time == null) {
            return date;
        } else if (date == null) {
            return time;
        }
        Calendar calendarA = Calendar.getInstance();
        calendarA.setTime(date);

        Calendar calendarB = Calendar.getInstance();
        calendarB.setTime(time);

        calendarA.set(Calendar.HOUR_OF_DAY, calendarB.get(Calendar.HOUR_OF_DAY));
        calendarA.set(Calendar.MINUTE, calendarB.get(Calendar.MINUTE));
        calendarA.set(Calendar.SECOND, calendarB.get(Calendar.SECOND));
        calendarA.set(Calendar.MILLISECOND, calendarB.get(Calendar.MILLISECOND));

        return calendarA.getTime();
    }

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
                case number:
                case date:
                case time:
                case choice:
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
                case uuid:
                case uid:
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

    @Override
    public StringBuffer format(Object object, StringBuffer result, FieldPosition pos) {
        return format.format(toArgs((Attributes) object), result, pos);
    }

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

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return pattern;
    }

    private enum Type {
        none {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getString(tag, index, "");
            }
        },
        upper {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getString(tag, index, "").toUpperCase();
            }
        },
        slice {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> slice) {
                return slice.apply(attrs.getString(tag, index));
            }
        },
        number {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        offset {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return Integer.toString(attrs.getInt(tag, index, 0) + offset);
            }
        },
        date {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
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
        time {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
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
        choice {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        hash {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? Tag.toHexString(s.hashCode()) : null;
            }
        },
        md5 {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? getMD5String(s) : null;
            }
        },
        urlencoded {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                String s = attrs.getString(tag, index);
                return s != null ? URLEncoder.encode(s, StandardCharsets.UTF_8) : null;
            }
        },
        rnd {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return Tag.toHexString(ThreadLocalRandom.current().nextInt());
            }
        },
        uuid {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return UUID.randomUUID();
            }
        },
        uid {
            @Override
            Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                    UnaryOperator<String> splice) {
                return UID.createUID();
            }
        };

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

        static long toLong(byte[] ba, int offset) {
            long l = 0;
            for (int i = offset, len = offset + LONG_BYTES; i < len; i++) {
                l |= ba[i] & 0xFF;
                l <<= 8;
            }
            return l;
        }

        abstract Object toArg(Attributes attrs, int tag, int index, int offset, Object dateTimeOffset,
                UnaryOperator<String> splice);

        String getMD5String(String s) {
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8));
                return toString32(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                return s;
            }
        }
    }

    private class Slice implements UnaryOperator<String> {
        final int beginIndex;
        final int endIndex;

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

        @Override
        public String apply(String s) {
            try {
                int l = s.length();
                return endIndex == 0 ? s.substring(beginIndex < 0 ? Math.max(0, l + beginIndex) : beginIndex)
                        : s.substring(beginIndex < 0 ? Math.max(0, l + beginIndex) : beginIndex,
                                endIndex < 0 ? l + endIndex : Math.min(l, endIndex));
            } catch (RuntimeException e) {
                return "";
            }
        }
    }

}
