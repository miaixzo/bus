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
package org.miaixz.bus.core.center.date.printer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Abstract base class for date information, providing date format, time zone, and locale information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleDatePrinter implements DatePrinter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852258036368L;

    /**
     * The date format pattern.
     */
    protected final String pattern;

    /**
     * The time zone.
     */
    protected final TimeZone timeZone;

    /**
     * The locale.
     */
    protected final Locale locale;

    /**
     * Constructs a {@code SimpleDatePrinter} with the given pattern, time zone, and locale.
     *
     * @param pattern  The date format pattern compatible with {@link java.text.SimpleDateFormat}.
     * @param timeZone The non-null time zone object.
     * @param locale   The non-null locale object.
     */
    protected SimpleDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    /**
     * Gets the date format pattern.
     *
     * @return The date format pattern string.
     */
    @Override
    public String getPattern() {
        return pattern;
    }

    /**
     * Gets the time zone.
     *
     * @return The {@link TimeZone} object.
     */
    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Gets the locale.
     *
     * @return The {@link Locale} object.
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Checks if this object is equal to another object.
     *
     * @param object The object to compare.
     * @return {@code true} if the objects are equal.
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof FastDatePrinter other)) {
            return false;
        }
        return pattern.equals(other.pattern) && timeZone.equals(other.timeZone) && locale.equals(other.locale);
    }

    /**
     * Gets the hash code of this object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }

    /**
     * Returns the string representation of this object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "SimpleDatePrinter[" + pattern + Symbol.COMMA + locale + Symbol.COMMA + timeZone.getID() + "]";
    }

}
