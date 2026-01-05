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
package org.miaixz.bus.core.center.date.format;

import java.io.Serial;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.printer.FastDatePrinter;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * A thread-safe date formatter that replaces {@link java.text.SimpleDateFormat}.
 *
 * <p>
 * Instances can be obtained through the following static methods: {@link #getInstance(String, TimeZone, Locale)},
 * {@link #getDateInstance(int, TimeZone, Locale)}, {@link #getTimeInstance(int, TimeZone, Locale)},
 * {@link #getDateTimeInstance(int, int, TimeZone, Locale)}
 * 
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatBuilder extends Format implements PositionDateParser, FormatPrinter {

    @Serial
    private static final long serialVersionUID = 2852255307380L;

    private static final FormatCache<FormatBuilder> CACHE = new FormatCache<>() {

        /**
         * Createinstance method.
         *
         * @return the FormatBuilder value
         */
        @Override
        protected FormatBuilder createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
            return new FormatBuilder(pattern, timeZone, locale);
        }
    };

    private final FastDatePrinter printer;
    private final FastDateParser parser;

    /**
     * Constructs a {@code FormatBuilder} with the given pattern, time zone, and locale.
     *
     * @param pattern  The date format pattern compatible with {@link java.text.SimpleDateFormat}.
     * @param timeZone The non-null time zone.
     * @param locale   The date locale.
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * Constructs a {@code FormatBuilder} with the given pattern, time zone, locale, and century start date.
     *
     * @param pattern      The date format pattern compatible with {@link java.text.SimpleDateFormat}.
     * @param timeZone     The non-null time zone.
     * @param locale       The date locale.
     * @param centuryStart The default century start date for two-digit years; if null, it defaults to 80 years before
     *                     the current time.
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale,
            final Date centuryStart) {
        printer = new FastDatePrinter(pattern, timeZone, locale);
        parser = new FastDateParser(pattern, timeZone, locale, centuryStart);
    }

    /**
     * Gets a {@code FormatBuilder} instance with default format and locale.
     *
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getInstance() {
        return CACHE.getInstance();
    }

    /**
     * Gets a {@code FormatBuilder} instance with the specified pattern, using the default time zone and locale.
     *
     * @param pattern The date format pattern.
     * @return A {@code FormatBuilder} instance.
     * @throws IllegalArgumentException if the date format pattern is invalid.
     */
    public static FormatBuilder getInstance(final String pattern) {
        return CACHE.getInstance(pattern, null, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance with the specified pattern and time zone, using the default locale.
     *
     * @param pattern  The date format pattern.
     * @param timeZone The time zone.
     * @return A {@code FormatBuilder} instance.
     * @throws IllegalArgumentException if the date format pattern is invalid.
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone) {
        return CACHE.getInstance(pattern, timeZone, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance with the specified pattern and locale, using the default time zone.
     *
     * @param pattern The date format pattern.
     * @param locale  The locale.
     * @return A {@code FormatBuilder} instance.
     * @throws IllegalArgumentException if the date format pattern is invalid.
     */
    public static FormatBuilder getInstance(final String pattern, final Locale locale) {
        return CACHE.getInstance(pattern, null, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance with the specified pattern, time zone, and locale.
     *
     * @param pattern  The date format pattern.
     * @param timeZone The time zone.
     * @param locale   The locale.
     * @return A {@code FormatBuilder} instance.
     * @throws IllegalArgumentException if the date format pattern is invalid.
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return CACHE.getInstance(pattern, timeZone, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date style, using the default time zone and locale.
     *
     * @param style The date style (FULL, LONG, MEDIUM, or SHORT).
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateInstance(final int style) {
        return CACHE.getDateInstance(style, null, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date style and locale, using the default time zone.
     *
     * @param style  The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param locale The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateInstance(final int style, final Locale locale) {
        return CACHE.getDateInstance(style, null, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date style and time zone, using the default locale.
     *
     * @param style    The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone The time zone.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone) {
        return CACHE.getDateInstance(style, timeZone, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date style, time zone, and locale.
     *
     * @param style    The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone The time zone.
     * @param locale   The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getDateInstance(style, timeZone, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified time style, using the default time zone and locale.
     *
     * @param style The time style (FULL, LONG, MEDIUM, or SHORT).
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getTimeInstance(final int style) {
        return CACHE.getTimeInstance(style, null, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified time style and locale, using the default time zone.
     *
     * @param style  The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param locale The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getTimeInstance(final int style, final Locale locale) {
        return CACHE.getTimeInstance(style, null, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified time style and time zone, using the default locale.
     *
     * @param style    The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone The time zone.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone) {
        return CACHE.getTimeInstance(style, timeZone, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified time style, time zone, and locale.
     *
     * @param style    The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone The time zone.
     * @param locale   The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getTimeInstance(style, timeZone, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date and time styles, using the default time zone and
     * locale.
     *
     * @param dateStyle The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeStyle The time style (FULL, LONG, MEDIUM, or SHORT).
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date and time styles and locale, using the default time
     * zone.
     *
     * @param dateStyle The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeStyle The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param locale    The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date and time styles and time zone, using the default
     * locale.
     *
     * @param dateStyle The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeStyle The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone  The time zone.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone) {
        return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
    }

    /**
     * Gets a {@code FormatBuilder} instance for the specified date and time styles, time zone, and locale.
     *
     * @param dateStyle The date style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeStyle The time style (FULL, LONG, MEDIUM, or SHORT).
     * @param timeZone  The time zone.
     * @param locale    The locale.
     * @return A {@code FormatBuilder} instance.
     */
    public static FormatBuilder getDateTimeInstance(
            final int dateStyle,
            final int timeStyle,
            final TimeZone timeZone,
            final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, timeZone, locale);
    }

    /**
     * Creates a {@code DateTimeFormatter} instance, using the system default time zone and locale.
     *
     * @param pattern The date format pattern.
     * @return A {@code DateTimeFormatter} instance.
     */
    public static DateTimeFormatter getDateTimeInstance(final String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(ZoneId.systemDefault());
    }

    /**
     * Formats an object into a string buffer.
     *
     * @param object     The object to format.
     * @param toAppendTo The string buffer to append to.
     * @param pos        The field position.
     * @return The formatted string buffer.
     */
    @Override
    public StringBuffer format(final Object object, final StringBuffer toAppendTo, final FieldPosition pos) {
        return toAppendTo.append(printer.format(object));
    }

    /**
     * Formats a millisecond timestamp.
     *
     * @param millis The millisecond timestamp.
     * @return The formatted string.
     */
    @Override
    public String format(final long millis) {
        return printer.format(millis);
    }

    /**
     * Formats a date object.
     *
     * @param date The date object.
     * @return The formatted string.
     */
    @Override
    public String format(final Date date) {
        return printer.format(date);
    }

    /**
     * Formats a calendar object.
     *
     * @param calendar The calendar object.
     * @return The formatted string.
     */
    @Override
    public String format(final Calendar calendar) {
        return printer.format(calendar);
    }

    /**
     * Formats a millisecond timestamp into the specified buffer.
     *
     * @param millis The millisecond timestamp.
     * @param buf    The output buffer.
     * @param <B>    The type of {@code Appendable}.
     * @return The formatted buffer.
     */
    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return printer.format(millis, buf);
    }

    /**
     * Formats a date object into the specified buffer.
     *
     * @param date The date object.
     * @param buf  The output buffer.
     * @param <B>  The type of {@code Appendable}.
     * @return The formatted buffer.
     */
    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return printer.format(date, buf);
    }

    /**
     * Formats a calendar object into the specified buffer.
     *
     * @param calendar The calendar object.
     * @param buf      The output buffer.
     * @param <B>      The type of {@code Appendable}.
     * @return The formatted buffer.
     */
    @Override
    public <B extends Appendable> B format(final Calendar calendar, final B buf) {
        return printer.format(calendar, buf);
    }

    /**
     * Parses a date string.
     *
     * @param source The date string.
     * @return The parsed date object.
     * @throws DateException if parsing fails.
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        return parser.parse(source);
    }

    /**
     * Parses a date string into a calendar object.
     *
     * @param source   The date string.
     * @param pos      The parse position.
     * @param calendar The calendar object.
     * @return {@code true} if parsing is successful.
     */
    @Override
    public boolean parse(final CharSequence source, final ParsePosition pos, final Calendar calendar) {
        return parser.parse(source, pos, calendar);
    }

    /**
     * Parses a date string into an object.
     *
     * @param source The date string.
     * @param pos    The parse position.
     * @return The parsed object.
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return parser.parse(source, pos);
    }

    /**
     * Gets the date format pattern.
     *
     * @return The date format pattern.
     */
    @Override
    public String getPattern() {
        return printer.getPattern();
    }

    /**
     * Gets the time zone.
     *
     * @return The time zone.
     */
    @Override
    public TimeZone getTimeZone() {
        return printer.getTimeZone();
    }

    /**
     * Gets the locale.
     *
     * @return The locale.
     */
    @Override
    public Locale getLocale() {
        return printer.getLocale();
    }

    /**
     * Estimates the maximum length of the formatted string.
     *
     * @return The estimated maximum length.
     */
    public int getMaxLengthEstimate() {
        return printer.getMaxLengthEstimate();
    }

    /**
     * Gets a {@code DateTimeFormatter} compatible with the current format.
     *
     * @return A {@code DateTimeFormatter} instance.
     */
    public DateTimeFormatter getDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getPattern());
        if (this.getLocale() != null) {
            formatter = formatter.withLocale(this.getLocale());
        }
        if (this.getTimeZone() != null) {
            formatter = formatter.withZone(this.getTimeZone().toZoneId());
        }
        return formatter;
    }

    /**
     * Checks if this object is equal to another object.
     *
     * @param object The object to compare.
     * @return {@code true} if the objects are equal.
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof FormatBuilder other)) {
            return false;
        }
        return printer.equals(other.printer);
    }

    /**
     * Gets the hash code of this object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return printer.hashCode();
    }

    /**
     * Returns the string representation of this object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "FastFormat[" + printer.getPattern() + Symbol.COMMA + printer.getLocale() + Symbol.COMMA
                + printer.getTimeZone().getID() + "]";
    }

}
