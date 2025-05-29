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
 * 线程安全的日期格式化类，替代{@link java.text.SimpleDateFormat}。
 *
 * <p>
 * 可通过以下静态方法获取实例： {@link #getInstance(String, TimeZone, Locale)}, {@link #getDateInstance(int, TimeZone, Locale)},
 * {@link #getTimeInstance(int, TimeZone, Locale)}, {@link #getDateTimeInstance(int, int, TimeZone, Locale)}
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatBuilder extends Format implements PositionDateParser, FormatPrinter {

    @Serial
    private static final long serialVersionUID = 2852291286713L;

    private static final FormatCache<FormatBuilder> CACHE = new FormatCache<>() {
        @Override
        protected FormatBuilder createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
            return new FormatBuilder(pattern, timeZone, locale);
        }
    };

    private final FastDatePrinter printer;
    private final FastDateParser parser;

    /**
     * 构造函数，初始化日期格式化器。
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat}兼容的日期格式
     * @param timeZone 非空时区
     * @param locale   日期地域
     * @throws NullPointerException 如果pattern、timeZone或locale为null
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * 构造函数，初始化日期格式化器，允许指定世纪起始时间。
     *
     * @param pattern      使用{@link java.text.SimpleDateFormat}兼容的日期格式
     * @param timeZone     非空时区
     * @param locale       日期地域
     * @param centuryStart 两位的默认世纪起始时间，若为null则为当前时间前80年
     * @throws NullPointerException 如果pattern、timeZone或locale为null
     */
    protected FormatBuilder(final String pattern, final TimeZone timeZone, final Locale locale,
            final Date centuryStart) {
        printer = new FastDatePrinter(pattern, timeZone, locale);
        parser = new FastDateParser(pattern, timeZone, locale, centuryStart);
    }

    /**
     * 获取默认格式和地域的FormatBuilder实例。
     *
     * @return FormatBuilder实例
     */
    public static FormatBuilder getInstance() {
        return CACHE.getInstance();
    }

    /**
     * 获取指定格式的FormatBuilder实例，使用默认时区和地域。
     *
     * @param pattern 日期格式
     * @return FormatBuilder实例
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static FormatBuilder getInstance(final String pattern) {
        return CACHE.getInstance(pattern, null, null);
    }

    /**
     * 获取指定格式和时区的FormatBuilder实例。
     *
     * @param pattern  日期格式
     * @param timeZone 时区
     * @return FormatBuilder实例
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone) {
        return CACHE.getInstance(pattern, timeZone, null);
    }

    /**
     * 获取指定格式和地域的FormatBuilder实例。
     *
     * @param pattern 日期格式
     * @param locale  地域
     * @return FormatBuilder实例
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static FormatBuilder getInstance(final String pattern, final Locale locale) {
        return CACHE.getInstance(pattern, null, locale);
    }

    /**
     * 获取指定格式、时区和地域的FormatBuilder实例。
     *
     * @param pattern  日期格式
     * @param timeZone 时区
     * @param locale   地域
     * @return FormatBuilder实例
     * @throws IllegalArgumentException 如果日期格式无效
     */
    public static FormatBuilder getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return CACHE.getInstance(pattern, timeZone, locale);
    }

    /**
     * 获取指定日期样式的FormatBuilder实例。
     *
     * @param style 日期样式（FULL、LONG、MEDIUM或SHORT）
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateInstance(final int style) {
        return CACHE.getDateInstance(style, null, null);
    }

    /**
     * 获取指定日期样式和地域的FormatBuilder实例。
     *
     * @param style  日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param locale 地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateInstance(final int style, final Locale locale) {
        return CACHE.getDateInstance(style, null, locale);
    }

    /**
     * 获取指定日期样式和时区的FormatBuilder实例。
     *
     * @param style    日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone 时区
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone) {
        return CACHE.getDateInstance(style, timeZone, null);
    }

    /**
     * 获取指定日期样式、时区和地域的FormatBuilder实例。
     *
     * @param style    日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone 时区
     * @param locale   地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getDateInstance(style, timeZone, locale);
    }

    /**
     * 获取指定时间样式的FormatBuilder实例。
     *
     * @param style 时间样式（FULL、LONG、MEDIUM或SHORT）
     * @return FormatBuilder实例
     */
    public static FormatBuilder getTimeInstance(final int style) {
        return CACHE.getTimeInstance(style, null, null);
    }

    /**
     * 获取指定时间样式和地域的FormatBuilder实例。
     *
     * @param style  时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param locale 地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getTimeInstance(final int style, final Locale locale) {
        return CACHE.getTimeInstance(style, null, locale);
    }

    /**
     * 获取指定时间样式和时区的FormatBuilder实例。
     *
     * @param style    时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone 时区
     * @return FormatBuilder实例
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone) {
        return CACHE.getTimeInstance(style, timeZone, null);
    }

    /**
     * 获取指定时间样式、时区和地域的FormatBuilder实例。
     *
     * @param style    时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone 时区
     * @param locale   地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getTimeInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return CACHE.getTimeInstance(style, timeZone, locale);
    }

    /**
     * 获取指定日期和时间样式的FormatBuilder实例。
     *
     * @param dateStyle 日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeStyle 时间样式（FULL、LONG、MEDIUM或SHORT）
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, null);
    }

    /**
     * 获取指定日期、时间样式和地域的FormatBuilder实例。
     *
     * @param dateStyle 日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeStyle 时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param locale    地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }

    /**
     * 获取指定日期、时间样式和时区的FormatBuilder实例。
     *
     * @param dateStyle 日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeStyle 时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone  时区
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone) {
        return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
    }

    /**
     * 获取指定日期、时间样式、时区和地域的FormatBuilder实例。
     *
     * @param dateStyle 日期样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeStyle 时间样式（FULL、LONG、MEDIUM或SHORT）
     * @param timeZone  时区
     * @param locale    地域
     * @return FormatBuilder实例
     */
    public static FormatBuilder getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone,
            final Locale locale) {
        return CACHE.getDateTimeInstance(dateStyle, timeStyle, timeZone, locale);
    }

    /**
     * 创建DateTimeFormatter，使用系统默认时区和地域。
     *
     * @param pattern 日期格式
     * @return DateTimeFormatter实例
     */
    public static DateTimeFormatter getDateTimeInstance(final String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(ZoneId.systemDefault());
    }

    /**
     * 格式化对象到字符串缓冲区。
     *
     * @param object     要格式化的对象
     * @param toAppendTo 目标字符串缓冲区
     * @param pos        字段位置
     * @return 格式化后的字符串缓冲区
     */
    @Override
    public StringBuffer format(final Object object, final StringBuffer toAppendTo, final FieldPosition pos) {
        return toAppendTo.append(printer.format(object));
    }

    /**
     * 格式化毫秒时间。
     *
     * @param millis 毫秒时间
     * @return 格式化后的字符串
     */
    @Override
    public String format(final long millis) {
        return printer.format(millis);
    }

    /**
     * 格式化日期对象。
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    @Override
    public String format(final Date date) {
        return printer.format(date);
    }

    /**
     * 格式化日历对象。
     *
     * @param calendar 日历对象
     * @return 格式化后的字符串
     */
    @Override
    public String format(final Calendar calendar) {
        return printer.format(calendar);
    }

    /**
     * 格式化毫秒时间到指定缓冲区。
     *
     * @param millis 毫秒时间
     * @param buf    输出缓冲区
     * @param <B>    Appendable类型
     * @return 格式化后的缓冲区
     */
    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return printer.format(millis, buf);
    }

    /**
     * 格式化日期对象到指定缓冲区。
     *
     * @param date 日期对象
     * @param buf  输出缓冲区
     * @param <B>  Appendable类型
     * @return 格式化后的缓冲区
     */
    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return printer.format(date, buf);
    }

    /**
     * 格式化日历对象到指定缓冲区。
     *
     * @param calendar 日历对象
     * @param buf      输出缓冲区
     * @param <B>      Appendable类型
     * @return 格式化后的缓冲区
     */
    @Override
    public <B extends Appendable> B format(final Calendar calendar, final B buf) {
        return printer.format(calendar, buf);
    }

    /**
     * 解析日期字符串。
     *
     * @param source 日期字符串
     * @return 解析后的日期对象
     * @throws DateException 如果解析失败
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        return parser.parse(source);
    }

    /**
     * 解析日期字符串到日历对象。
     *
     * @param source   日期字符串
     * @param pos      解析位置
     * @param calendar 日历对象
     * @return 如果解析成功返回true
     */
    @Override
    public boolean parse(final CharSequence source, final ParsePosition pos, final Calendar calendar) {
        return parser.parse(source, pos, calendar);
    }

    /**
     * 解析日期字符串到对象。
     *
     * @param source 日期字符串
     * @param pos    解析位置
     * @return 解析后的对象
     */
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return parser.parse(source, pos);
    }

    /**
     * 获取日期格式模式。
     *
     * @return 日期格式模式
     */
    @Override
    public String getPattern() {
        return printer.getPattern();
    }

    /**
     * 获取时区。
     *
     * @return 时区
     */
    @Override
    public TimeZone getTimeZone() {
        return printer.getTimeZone();
    }

    /**
     * 获取地域。
     *
     * @return 地域
     */
    @Override
    public Locale getLocale() {
        return printer.getLocale();
    }

    /**
     * 估算格式化后的最大字符串长度。
     *
     * @return 最大长度估算
     */
    public int getMaxLengthEstimate() {
        return printer.getMaxLengthEstimate();
    }

    /**
     * 获取与当前格式兼容的DateTimeFormatter。
     *
     * @return DateTimeFormatter实例
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
     * 判断是否与另一个对象相等。
     *
     * @param object 要比较的对象
     * @return 如果相等返回true
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof FormatBuilder)) {
            return false;
        }
        final FormatBuilder other = (FormatBuilder) object;
        return printer.equals(other.printer);
    }

    /**
     * 获取对象的哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return printer.hashCode();
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "FastFormat[" + printer.getPattern() + Symbol.COMMA + printer.getLocale() + Symbol.COMMA
                + printer.getTimeZone().getID() + "]";
    }

}