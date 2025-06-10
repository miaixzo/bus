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
 * 日期格式化器缓存类，提供线程安全的格式化器实例管理。
 *
 * @param <F> 格式化器类型，继承自{@link Format}
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FormatCache<F extends Format> {

    /**
     * 无日期或无时间样式，用于与DateFormat.SHORT或DateFormat.LONG相同的参数
     */
    private static final ConcurrentMap<Tuple, String> C_DATE_TIME_INSTANCE_CACHE = new ConcurrentHashMap<>(7);
    /**
     * 格式化器实例缓存
     */
    private final ConcurrentMap<Tuple, F> cInstanceCache = new ConcurrentHashMap<>(7);

    /**
     * 根据指定的日期和时间样式以及地域获取日期/时间格式模式。
     *
     * @param dateStyle 日期样式：FULL、LONG、MEDIUM或SHORT，null表示无日期
     * @param timeStyle 时间样式：FULL、LONG、MEDIUM或SHORT，null表示无时间
     * @param locale    非空地域
     * @return 本地化的标准日期/时间格式模式
     * @throws IllegalArgumentException 如果地域没有定义日期/时间模式
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
     * 使用默认的模式、时区和地域获取缓存中的格式化器实例。
     *
     * @return 日期/时间格式化器
     */
    public F getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, null, null);
    }

    /**
     * 根据指定的模式、时区和地域获取格式化器实例，支持缓存。
     *
     * @param pattern  非空日期格式，与{@link java.text.SimpleDateFormat}格式兼容
     * @param timeZone 时区，默认为当前时区
     * @param locale   地域，默认为当前地域
     * @return 格式化器实例
     * @throws IllegalArgumentException 如果pattern为空或无效
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
                format = previousValue;
            }
        }
        return format;
    }

    /**
     * 创建格式化器实例。
     *
     * @param pattern  非空日期格式，与{@link java.text.SimpleDateFormat}格式兼容
     * @param timeZone 时区，默认为当前时区
     * @param locale   地域，默认为当前地域
     * @return 格式化器实例
     * @throws IllegalArgumentException 如果pattern为空或无效
     */
    abstract protected F createInstance(String pattern, TimeZone timeZone, Locale locale);

    /**
     * 根据指定的日期和时间样式、时区和地域获取格式化器实例。
     *
     * @param dateStyle 日期样式：FULL、LONG、MEDIUM或SHORT，null表示无日期
     * @param timeStyle 时间样式：FULL、LONG、MEDIUM或SHORT，null表示无时间
     * @param timeZone  可选时区，覆盖格式化日期的时区，null表示使用默认地域
     * @param locale    可选地域，覆盖系统地域
     * @return 本地化的标准日期/时间格式化器
     * @throws IllegalArgumentException 如果地域没有定义日期/时间模式
     */
    F getDateTimeInstance(final Integer dateStyle, final Integer timeStyle, final TimeZone timeZone, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
        return getInstance(pattern, timeZone, locale);
    }

    /**
     * 根据指定的日期样式、时区和地域获取日期格式化器实例。
     *
     * @param dateStyle 日期样式：FULL、LONG、MEDIUM或SHORT
     * @param timeZone  可选时区，覆盖格式化日期的时区，null表示使用默认地域
     * @param locale    可选地域，覆盖系统地域
     * @return 本地化的标准日期格式化器
     * @throws IllegalArgumentException 如果地域没有定义日期模式
     */
    F getDateInstance(final int dateStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(dateStyle, null, timeZone, locale);
    }

    /**
     * 根据指定的时间样式、时区和地域获取时间格式化器实例。
     *
     * @param timeStyle 时间样式：FULL、LONG、MEDIUM或SHORT
     * @param timeZone  可选时区，覆盖格式化时间的时区，null表示使用默认地域
     * @param locale    可选地域，覆盖系统地域
     * @return 本地化的标准时间格式化器
     * @throws IllegalArgumentException 如果地域没有定义时间模式
     */
    F getTimeInstance(final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return getDateTimeInstance(null, timeStyle, timeZone, locale);
    }

}