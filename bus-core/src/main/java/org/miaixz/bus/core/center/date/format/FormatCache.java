/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date.format;

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.tuple.Tuple;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

/**
 * 日期格式化器缓存
 *
 * @param <F> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FormatCache<F extends Format> {

    /**
     * No date or no time. Used in same parameters as DateFormat.SHORT or DateFormat.LONG
     */
    private static final ConcurrentMap<Tuple, String> C_DATE_TIME_INSTANCE_CACHE = new SafeConcurrentHashMap<>(7);
    private final ConcurrentMap<Tuple, F> cInstanceCache = new SafeConcurrentHashMap<>(7);

    /**
     * Gets a date/time format for the specified styles and locale.
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param locale    The non-null locale of the desired format
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
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
                    // even though it doesn't matter if another thread put the pattern
                    // it's still good practice to return the String instance that is
                    // actually in the ConcurrentMap
                    pattern = previous;
                }
            } catch (final ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return pattern;
    }

    /**
     * 使用默认的pattern、timezone和locale获得缓存中的实例
     *
     * @return a date/time format
     */
    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, null, null);
    }

    /**
     * 使用 pattern, time zone and locale 获得对应的 格式化器
     *
     * @param pattern  非空日期格式，使用与 {@link java.text.SimpleDateFormat}相同格式
     * @param timeZone 时区，默认当前时区
     * @param locale   地区，默认使用当前地区
     * @return 格式化器
     * @throws IllegalArgumentException pattern 无效或{@code null}
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
            final F previousValue = cInstanceCache.putIfAbsent(key, format);
            if (previousValue != null) {
                // another thread snuck in and did the same work
                // we should return the instance that is in ConcurrentMap
                format = previousValue;
            }
        }
        return format;
    }

    /**
     * 创建格式化器
     *
     * @param pattern  非空日期格式，使用与 {@link java.text.SimpleDateFormat}相同格式
     * @param timeZone 时区，默认当前时区
     * @param locale   地区，默认使用当前地区
     * @return 格式化器
     * @throws IllegalArgumentException pattern 无效或{@code null}
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * Gets a date/time format instance using the specified style, time zone and locale.
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT, null indicates no date in format
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT, null indicates no time in format
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    /**
     * Gets a date format instance using the specified style, time zone and locale.
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    F getDateInstance(final int dateStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(dateStyle, null, timeZone, locale);
    }

    /**
     * Gets a time format instance using the specified style, time zone and locale.
     *
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  optional time zone, overrides time zone of formatted date, null means use default Locale
     * @param locale    optional locale, overrides system locale
     * @return a localized standard date/time format
     * @throws IllegalArgumentException if the Locale has no date/time pattern defined
     */
    F getTimeInstance(final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(null, timeStyle, timeZone, locale);
    }

}
