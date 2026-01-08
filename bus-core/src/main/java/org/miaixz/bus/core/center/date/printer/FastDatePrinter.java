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
package org.miaixz.bus.core.center.date.printer;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.miaixz.bus.core.center.date.format.DatePattern;
import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A thread-safe date formatting class that can be used as a replacement for {@link java.text.SimpleDateFormat}. It is
 * used to format a {@link Date} into a string. This implementation is inspired by Apache Commons Lang's
 * {@code FastDatePrinter}.
 *
 * @author Kimi Liu
 * @see FastDateParser
 * @since Java 17+
 */
public class FastDatePrinter extends SimpleDatePrinter implements FormatPrinter {

    @Serial
    private static final long serialVersionUID = 2852257551539L;

    /**
     * The parsed date pattern used for formatting.
     */
    private final DatePattern datePattern;

    /**
     * A queue of cached {@link Calendar} objects to reduce object creation overhead.
     */
    private final Queue<Calendar> queue;

    /**
     * Constructs a new {@code FastDatePrinter}.
     *
     * @param pattern  A {@link java.text.SimpleDateFormat} compatible date format pattern.
     * @param timeZone The timezone to use, not null.
     * @param locale   The locale to use, not null.
     */
    public FastDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        super(pattern, timeZone, locale);
        this.datePattern = new DatePattern(pattern, locale, timeZone);
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Formats a {@link Date}, {@link Calendar}, or {@link Long} (milliseconds) object into a date string.
     *
     * @param obj The object to format.
     * @return The formatted string.
     * @throws IllegalArgumentException if the object type is not supported.
     */
    public String format(final Object obj) {
        if (obj instanceof Date) {
            return format((Date) obj);
        } else if (obj instanceof Calendar) {
            return format((Calendar) obj);
        } else if (obj instanceof Long) {
            return format((Long) obj);
        } else {
            throw new IllegalArgumentException("Unknown class: " + (obj == null ? "<null>" : obj.getClass().getName()));
        }
    }

    /**
     * Formats a {@link Date} object into a date string.
     *
     * @param date The date object.
     * @return The formatted date string.
     */
    @Override
    public String format(final Date date) {
        return format(date.getTime());
    }

    /**
     * Formats a millisecond timestamp into a date string.
     *
     * @param millis The timestamp in milliseconds.
     * @return The formatted date string.
     */
    @Override
    public String format(final long millis) {
        return format(millis, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    /**
     * Formats a {@link Calendar} object into a date string.
     *
     * @param calendar The calendar object.
     * @return The formatted date string.
     */
    @Override
    public String format(final Calendar calendar) {
        return format(calendar, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    /**
     * Formats a {@link Date} object into the provided {@link Appendable} buffer.
     *
     * @param date The date object.
     * @param buf  The buffer to append to.
     * @param <B>  The type of the Appendable.
     * @return The buffer with the formatted date.
     */
    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return format(date.getTime(), buf);
    }

    /**
     * Formats a millisecond timestamp into the provided {@link Appendable} buffer.
     *
     * @param millis The timestamp in milliseconds.
     * @param buf    The buffer to append to.
     * @param <B>    The type of the Appendable.
     * @return The buffer with the formatted date.
     */
    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return applyRules(millis, buf);
    }

    /**
     * Formats a {@link Calendar} object into the provided {@link Appendable} buffer.
     *
     * @param calendar The calendar object.
     * @param buf      The buffer to append to.
     * @param <B>      The type of the Appendable.
     * @return The buffer with the formatted date.
     */
    @Override
    public <B extends Appendable> B format(Calendar calendar, final B buf) {
        if (!calendar.getTimeZone().equals(timeZone)) {
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(timeZone);
        }
        return datePattern.applyRules(calendar, buf);
    }

    /**
     * Applies the formatting rules to a timestamp, appending the result to a buffer. This method uses a cached
     * {@link Calendar} instance to reduce object creation overhead.
     *
     * @param millis The timestamp in milliseconds.
     * @param buf    The {@link Appendable} to write to.
     * @param <B>    The type of the Appendable.
     * @return The provided {@link Appendable}.
     */
    private <B extends Appendable> B applyRules(final long millis, final B buf) {
        Calendar calendar = queue.poll();
        if (calendar == null) {
            calendar = Calendar.getInstance(timeZone, locale);
        }
        calendar.setTimeInMillis(millis);
        final B b = datePattern.applyRules(calendar, buf);
        queue.offer(calendar);
        return b;
    }

    /**
     * Gets an estimate of the maximum length that the formatted date string could have.
     *
     * @return The estimated maximum length.
     */
    public int getMaxLengthEstimate() {
        return datePattern.getEstimateLength();
    }

}
