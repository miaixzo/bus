/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.date.format;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>
 * FastDateFormat 是一个线程安全的 {@link java.text.SimpleDateFormat} 实现。
 * </p>
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
 * @version 5.2.2
 * @since JDK 1.8+
 */
public class FastDateFormat extends Format implements DateParser, DatePrinter {

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
    private static final long serialVersionUID = 8097890768636183236L;
    private static final FormatCache<FastDateFormat> cache = new FormatCache<FastDateFormat>() {
        @Override
        protected FastDateFormat createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
            return new FastDateFormat(pattern, timeZone, locale);
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
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale) {
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
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        printer = new FastDatePrinter(pattern, timeZone, locale);
        parser = new FastDateParser(pattern, timeZone, locale, centuryStart);
    }

    /**
     * 获得 {@link FastDateFormat} 实例，使用默认格式和地区
     *
     * @return {@link FastDateFormat}
     */
    public static FastDateFormat getInstance() {
        return cache.getInstance();
    }

    /**
     * 获得 {@link FastDateFormat} 实例，使用默认地区
     * 支持缓存
     *
     * @param pattern 使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @return {@link FastDateFormat}
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FastDateFormat getInstance(final String pattern) {
        return cache.getInstance(pattern, null, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 时区{@link TimeZone}
     * @return {@link FastDateFormat}
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone) {
        return cache.getInstance(pattern, timeZone, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param pattern 使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param locale  {@link Locale} 日期地理位置
     * @return {@link FastDateFormat}
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FastDateFormat getInstance(final String pattern, final Locale locale) {
        return cache.getInstance(pattern, null, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 时区{@link TimeZone}
     * @param locale   {@link Locale} 日期地理位置
     * @return {@link FastDateFormat}
     * @throws IllegalArgumentException 日期格式问题
     */
    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return cache.getInstance(pattern, timeZone, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style date style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateInstance(final int style) {
        return cache.getDateInstance(style, null, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style  date style: FULL, LONG, MEDIUM, or SHORT
     * @param locale {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateInstance(final int style, final Locale locale) {
        return cache.getDateInstance(style, null, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style    date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone 时区{@link TimeZone}
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateInstance(final int style, final TimeZone timeZone) {
        return cache.getDateInstance(style, timeZone, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style    date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone 时区{@link TimeZone}
     * @param locale   {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return cache.getDateInstance(style, timeZone, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style time style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getTimeInstance(final int style) {
        return cache.getTimeInstance(style, null, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style  time style: FULL, LONG, MEDIUM, or SHORT
     * @param locale {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getTimeInstance(final int style, final Locale locale) {
        return cache.getTimeInstance(style, null, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style    time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of formatted time
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getTimeInstance(final int style, final TimeZone timeZone) {
        return cache.getTimeInstance(style, timeZone, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param style    time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone optional time zone, overrides time zone of formatted time
     * @param locale   {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getTimeInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return cache.getTimeInstance(style, timeZone, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle) {
        return cache.getDateTimeInstance(dateStyle, timeStyle, null, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param locale    {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final Locale locale) {
        return cache.getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  时区{@link TimeZone}
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone) {
        return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
    }

    /**
     * 获得 {@link FastDateFormat} 实例
     * 支持缓存
     *
     * @param dateStyle date style: FULL, LONG, MEDIUM, or SHORT
     * @param timeStyle time style: FULL, LONG, MEDIUM, or SHORT
     * @param timeZone  时区{@link TimeZone}
     * @param locale    {@link Locale} 日期地理位置
     * @return 本地化 {@link FastDateFormat}
     */
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return cache.getDateTimeInstance(dateStyle, timeStyle, timeZone, locale);
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
    public Date parse(final String source) throws ParseException {
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
        return parser.parseObject(source, pos);
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

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FastDateFormat == false) {
            return false;
        }
        final FastDateFormat other = (FastDateFormat) obj;
        // no need to check parser, as it has same invariants as printer
        return printer.equals(other.printer);
    }

    @Override
    public int hashCode() {
        return printer.hashCode();
    }

    @Override
    public String toString() {
        return "FastDateFormat[" + printer.getPattern() + "," + printer.getLocale() + "," + printer.getTimeZone().getID() + "]";
    }

}
