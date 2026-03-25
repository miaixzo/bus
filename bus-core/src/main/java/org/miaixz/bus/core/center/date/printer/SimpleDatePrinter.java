/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
