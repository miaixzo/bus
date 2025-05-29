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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.miaixz.bus.core.center.date.format.DatePattern;
import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 线程安全的日期格式化类，替代 {@link java.text.SimpleDateFormat}，用于将 {@link Date} 格式化为字符串。
 * 参考 Apache Commons Lang 3.5。
 *
 * @author Kimi Liu
 * @see FastDateParser
 * @since Java 17+
 */
public class FastDatePrinter extends SimpleDatePrinter implements FormatPrinter {

    @Serial
    private static final long serialVersionUID = 2852292276382L;

    /**
     * 日期格式化模式对象
     */
    private final DatePattern datePattern;

    /**
     * 缓存的 Calendar 对象队列，用于减少对象创建
     */
    private final Queue<Calendar> queue;

    /**
     * 构造方法，初始化日期格式化器。
     *
     * @param pattern  {@link java.text.SimpleDateFormat} 兼容的日期格式
     * @param timeZone 非空时区对象
     * @param locale   非空地域对象
     */
    public FastDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        super(pattern, timeZone, locale);
        this.datePattern = new DatePattern(pattern, locale, timeZone);
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 格式化对象为字符串，支持 {@link Date}、{@link Calendar} 或 {@link Long}（毫秒）。
     *
     * @param obj 要格式化的对象
     * @return 格式化后的字符串
     * @throws IllegalArgumentException 如果对象类型不支持
     */
    public String format(final Object obj) {
        if (obj instanceof Date) {
            return format((Date) obj);
        } else if (obj instanceof Calendar) {
            return format((Calendar) obj);
        } else if (obj instanceof Long) {
            return format(((Long) obj).longValue());
        } else {
            throw new IllegalArgumentException("Unknown class: " + (obj == null ? "<null>" : obj.getClass().getName()));
        }
    }

    /**
     * 格式化日期对象为字符串。
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    @Override
    public String format(final Date date) {
        return format(date.getTime());
    }

    /**
     * 格式化毫秒时间戳为字符串。
     *
     * @param millis 毫秒时间戳
     * @return 格式化后的字符串
     */
    @Override
    public String format(final long millis) {
        return format(millis, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    /**
     * 格式化日历对象为字符串。
     *
     * @param calendar 日历对象
     * @return 格式化后的字符串
     */
    @Override
    public String format(final Calendar calendar) {
        return format(calendar, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    /**
     * 格式化日期对象到指定缓冲区。
     *
     * @param date 日期对象
     * @param buf  输出缓冲区
     * @param <B>  Appendable 类型
     * @return 格式化后的缓冲区
     */
    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return format(date.getTime(), buf);
    }

    /**
     * 格式化毫秒时间戳到指定缓冲区。
     *
     * @param millis 毫秒时间戳
     * @param buf    输出缓冲区
     * @param <B>    Appendable 类型
     * @return 格式化后的缓冲区
     */
    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return applyRules(millis, buf);
    }

    /**
     * 格式化日历对象到指定缓冲区。
     *
     * @param calendar 日历对象
     * @param buf      输出缓冲区
     * @param <B>      Appendable 类型
     * @return 格式化后的缓冲区
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
     * 将时间戳格式化为 Appendable，使用缓存的 Calendar 对象以减少创建开销。
     *
     * @param millis 时间戳
     * @param buf    待拼接的 Appendable
     * @param <B>    Appendable 类型
     * @return 拼接后的 Appendable
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
     * 估算格式化后的日期字符串最大长度。
     *
     * @return 最大长度估算值
     */
    public int getMaxLengthEstimate() {
        return datePattern.getEstimateLength();
    }

}