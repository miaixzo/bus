/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.format;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tuple.Tuple;

/**
 * Date formatter cache class, providing thread-safe management of formatter instances.
 *
 * @param <F> The formatter type, extending from {@link Format}.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FormatCache<F extends Format> {

    /**
     * Constructs a new FormatCache. Utility class constructor for static access.
     */
    protected FormatCache() {
    }

    /**
     * Cache for date/time instance patterns, used for parameters similar to DateFormat.SHORT or DateFormat.LONG.
     */
    private static final ConcurrentMap<Tuple, String> C_DATE_TIME_INSTANCE_CACHE = new ConcurrentHashMap<>(7);
    /**
     * Cache for formatter instances.
     */
    private final ConcurrentMap<Tuple, F> cInstanceCache = new ConcurrentHashMap<>(7);

    /**
     * Retrieves the date/time format pattern based on the specified date and time styles and locale.
     *
     * @param dateStyle The date style: FULL, LONG, MEDIUM, or SHORT; {@code null} means no date.
     * @param timeStyle The time style: FULL, LONG, MEDIUM, or SHORT; {@code null} means no time.
     * @param locale    The non-null locale.
     * @return The localized standard date/time format pattern.
     * @throws IllegalArgumentException if the locale does not define a date/time pattern.
     */
    static String getPatternForStyle(final Integer dateStyle, final Integer timeStyle, final Locale locale) {
        final Tuple key = new Tuple(dateStyle, timeStyle, locale);
        String pattern = C_DATE_TIME_INSTANCE_CACHE.get(key);
        if (pattern == null) {
            try {
                final DateFormat formatter;
                if (dateStyle == null) {
                    formatter = DateFormat.getTimeInstance(timeStyle, locale);
                } else if (timeStyle == null) {
                    formatter = DateFormat.getDateInstance(dateStyle, locale);
                } else {
                    formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                }
                pattern = ((SimpleDateFormat) formatter).toPattern();
                final String previous = C_DATE_TIME_INSTANCE_CACHE.putIfAbsent(key, pattern);
                if (previous != null) {
                    pattern = previous;
                }
            } catch (final ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    /**
     * Retrieves a cached formatter instance using default pattern, time zone, and locale.
     *
     * @return A date/time formatter.
     */
    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, null, null);
    }

    /**
     * Retrieves a formatter instance based on the specified pattern, time zone, and locale, supporting caching.
     *
     * @param pattern  The non-null date format, compatible with {@link java.text.SimpleDateFormat} format.
     * @param timeZone The time zone; defaults to the current time zone if {@code null}.
     * @param locale   The locale; defaults to the current locale if {@code null}.
     * @return The formatter instance.
     * @throws IllegalArgumentException if the pattern is blank or invalid.
     */
    public F getInstance(final String pattern, TimeZone timeZone, Locale locale) {
        Assert.notBlank(pattern, "pattern must not be blank");
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final Tuple key = new Tuple(pattern, timeZone, locale);
        F format = cInstanceCache.get(key);
        if (format == null) {
            format = createInstance(pattern, timeZone, locale);
            final F previous = cInstanceCache.putIfAbsent(key, format);
            if (previous != null) {
                format = previous;
            }
        }
        return format;
    }

    /**
     * Creates a formatter instance.
     *
     * @param pattern  The non-null date format, compatible with {@link java.text.SimpleDateFormat} format.
     * @param timeZone The time zone; defaults to the current time zone if {@code null}.
     * @param locale   The locale; defaults to the current locale if {@code null}.
     * @return The formatter instance.
     * @throws IllegalArgumentException if the pattern is blank or invalid.
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * Retrieves a formatter instance based on the specified date and time styles, time zone, and locale.
     *
     * @param dateStyle The date style: FULL, LONG, MEDIUM, or SHORT; {@code null} means no date.
     * @param timeStyle The time style: FULL, LONG, MEDIUM, or SHORT; {@code null} means no time.
     * @param timeZone  The optional time zone, overriding the time zone for formatting dates; {@code null} means use
     *                  default locale.
     * @param locale    The optional locale, overriding the system locale.
     * @return The localized standard date/time formatter.
     * @throws IllegalArgumentException if the locale does not define a date/time pattern.
     */
    F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    /**
     * Retrieves a date formatter instance based on the specified date style, time zone, and locale.
     *
     * @param dateStyle The date style: FULL, LONG, MEDIUM, or SHORT.
     * @param timeZone  The optional time zone, overriding the time zone for formatting dates; {@code null} means use
     *                  default locale.
     * @param locale    The optional locale, overriding the system locale.
     * @return The localized standard date formatter.
     * @throws IllegalArgumentException if the locale does not define a date pattern.
     */
    F getDateInstance(final int dateStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(dateStyle, null, timeZone, locale);
    }

    /**
     * Retrieves a time formatter instance based on the specified time style, time zone, and locale.
     *
     * @param timeStyle The time style: FULL, LONG, MEDIUM, or SHORT.
     * @param timeZone  The optional time zone, overriding the time zone for formatting times; {@code null} means use
     *                  default locale.
     * @param locale    The optional locale, overriding the system locale.
     * @return The localized standard time formatter.
     * @throws IllegalArgumentException if the locale does not define a time pattern.
     */
    F getTimeInstance(final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(null, timeStyle, timeZone, locale);
    }

}
