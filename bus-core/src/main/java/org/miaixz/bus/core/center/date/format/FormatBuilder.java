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

import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.center.date.printer.FastDatePrinter;
import org.miaixz.bus.core.center.date.printer.FormatPrinter;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.exception.DateException;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * FastFormat 是一个线程安全的 {@link java.text.SimpleDateFormat} 实现。
 *
 * <p>
 * 通过以下静态方法获得此对象:
 * {@link #getInstance(String, TimeZone, Locale)}
 * {@link #getDateInstance(int, TimeZone, Locale)}
 * {@link #getTimeInstance(int, TimeZone, Locale)}
 * {@link #getDateTimeInstance(int, int, TimeZone, Locale)}
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatBuilder extends Format implements PositionDateParser, FormatPrinter {

    /**
     * FULL locale dependent date or time style.
     */
    public static final int FULL = DateFormat.FULL;
    /**
     * LONG locale dependent date or time style.
     */
    public static final int LONG = DateFormat.LONG;
    /**
     * MEDIUM locale dependent date or time style.
     */
    public static final int MEDIUM = DateFormat.MEDIUM;
    /**
     * SHORT locale dependent date or time style.
     */
    public static final int SHORT = DateFormat.SHORT;

    private static final long serialVersionUID = -1L;

    private static final FormatCache<FormatBuilder> CACHE = new FormatCache<>() {
        @Override
        protected FormatBuilder createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
            return new FormatBuilder(pattern, timeZone, locale);
        }
    };

    private final FastDatePrinter printer;
    private final FastDateParser parser;

    /**
     * 构造
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 非空时区 {@link TimeZone}
     * @param locale   {@link Locale} 日期地理位置
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * 构造
     *
     * @param pattern      使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone     非空时区 {@link TimeZone}
     * @param locale       {@link Locale} 日期地理位置
     * @param centuryStart The start of the 100 year period to use as the "default century" for 2 digit year parsing. If centuryStart is null, defaults to now - 80 years
     * @throws NullPointerException if pattern, timeZone, or locale is null.
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        printer = new FastDatePrinter(pattern, timeZone, locale);
        parser = new FastDateParser(pattern, timeZone, locale, centuryStart);
    }

    /**
     * 获得 FastDateFormat实例，使用默认格式和地区
     *
     * @return FastFormat
     */
    public static FormatBuilder getInstance() {
        return CACHE.getInstance();
    }

    /**
     * 获得 FastFormat 实例，使用默认地区
     * 支持缓存
     *
     * @param pattern 使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @return FastFormat
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FormatBuilder getInstance(final String pattern) {
        return CACHE.getInstance(pattern, null, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 时区{@link TimeZone}
     * @return FastFormat
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone) {
        return CACHE.getInstance(pattern, timeZone, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param pattern 使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param locale  {@link Locale} 日期地理位置
     * @return FastFormat
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FormatBuilder getInstance(final String pattern, final Locale locale) {
        return CACHE.getInstance(pattern, null, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 时区{@link TimeZone}
     * @param locale   {@link Locale} 日期地理位置
     * @return FastFormat
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return CACHE.getInstance(pattern, timeZone, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style date style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateInstance(final int style) {
        return CACHE.getDateInstance(style, null, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style  date style: FULL, LONG, MEDIUM, or SHORT
     * @param locale {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateInstance(final int style, final Locale locale) {
        return CACHE.getDateInstance(style, null, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style    date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone 时区{@link TimeZone}
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone) {
        return CACHE.getDateInstance(style, timeZone, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style    date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone 时区{@link TimeZone}
     * @param locale   {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getDateInstance(style, timeZone, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style time style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getTimeInstance(final int style) {
        return CACHE.getTimeInstance(style, null, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style  time style: FULL, LONG, MEDIUM, or SHORT
     * @param locale {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getTimeInstance(final int style, final Locale locale) {
        return CACHE.getTimeInstance(style, null, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style    time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of formatted time
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone) {
        return CACHE.getTimeInstance(style, timeZone, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param style    time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of formatted time
     * @param locale   {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getTimeInstance(style, timeZone, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param locale    {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  时区{@link TimeZone}
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone) {
        return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
    }

    /**
     * 获得 FastFormat 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  时区{@link TimeZone}
     * @param locale    {@link Locale} 日期地理位置
     * @return 本地化 FastFormat
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, timeZone, locale);
    }

    /**
     * 创建并为 {@link DateTimeFormatter} 赋予默认时区和位置信息，默认值为系统默认值。
     *
     * @param pattern 日期格式
     * @return {@link DateTimeFormatter}
     */
    public static DateTimeFormatter getDateTimeInstance(final String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(ZoneId.systemDefault());
    }

    @Override
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        return toAppendTo.append(printer.format(obj));
    }

    @Override
    public String format(final long millis) {
        return printer.format(millis);
    }

    @Override
    public String format(final Date date) {
        return printer.format(date);
    }

    @Override
    public String format(final Calendar calendar) {
        return printer.format(calendar);
    }

    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return printer.format(millis, buf);
    }

    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return printer.format(date, buf);
    }

    @Override
    public <B extends Appendable> B format(final Calendar calendar, final B buf) {
        return printer.format(calendar, buf);
    }

    @Override
    public Date parse(final String source) throws DateException {
        return parser.parse(source);
    }

    @Override
    public Date parse(final String source, final ParsePosition pos) {
        return parser.parse(source, pos);
    }

    @Override
    public boolean parse(final String source, final ParsePosition pos, final Calendar calendar) {
        return parser.parse(source, pos, calendar);
    }

    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return parser.parse(source, pos);
    }

    @Override
    public String getPattern() {
        return printer.getPattern();
    }

    @Override
    public TimeZone getTimeZone() {
        return printer.getTimeZone();
    }

    @Override
    public Locale getLocale() {
        return printer.getLocale();
    }

    /**
     * 估算生成的日期字符串长度
     * 实际生成的字符串长度小于或等于此值
     *
     * @return 日期字符串长度
     */
    public int getMaxLengthEstimate() {
        return printer.getMaxLengthEstimate();
    }

    /**
     * 便捷获取 DateTimeFormatter
     * 由于 {@link Fields} 很大一部分的格式没有提供 {@link DateTimeFormatter},因此这里提供快捷获取方式
     *
     * @return DateTimeFormatter
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

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FormatBuilder == false) {
            return false;
        }
        final FormatBuilder other = (FormatBuilder) obj;
        // no need to check parser, as it has same invariants as printer
        return printer.equals(other.printer);
    }

    @Override
    public int hashCode() {
        return printer.hashCode();
    }

    @Override
    public String toString() {
        return "FastFormat[" + printer.getPattern() + "," + printer.getLocale() + "," + printer.getTimeZone().getID() + "]";
    }

}
