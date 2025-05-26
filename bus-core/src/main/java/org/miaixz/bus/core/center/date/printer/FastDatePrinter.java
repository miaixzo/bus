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
 * {@link java.text.SimpleDateFormat} 的线程安全版本，用于将 {@link Date} 格式化输出 Thanks to Apache Commons Lang 3.5
 *
 * @author Kimi Liu
 * @see FastDateParser
 * @since Java 17+
 */
public class FastDatePrinter extends SimpleDatePrinter implements FormatPrinter {

    @Serial
    private static final long serialVersionUID = 2852292276382L;

    private final DatePattern datePattern;
    /**
     * 缓存的Calendar对象，用于减少对象创建。参考tomcat的ConcurrentDateFormat
     */
    private final Queue<Calendar> queue;

    /**
     * 构造，内部使用<br>
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 非空时区{@link TimeZone}
     * @param locale   非空{@link Locale} 日期地理位置
     */
    public FastDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        super(pattern, timeZone, locale);
        this.datePattern = new DatePattern(pattern, locale, timeZone);
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Formats a {@code Date}, {@code Calendar} or {@code Long} (milliseconds) object.
     *
     * @param obj the object to format
     * @return The formatted value.
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

    @Override
    public String format(final Date date) {
        return format(date.getTime());
    }

    @Override
    public String format(final long millis) {
        return format(millis, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    @Override
    public String format(final Calendar calendar) {
        return format(calendar, StringKit.builder(datePattern.getEstimateLength())).toString();
    }

    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        return format(date.getTime(), buf);
    }

    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        return applyRules(millis, buf);
    }

    @Override
    public <B extends Appendable> B format(Calendar calendar, final B buf) {
        // do not pass in calendar directly, this will cause TimeZone of FastDatePrinter to be ignored
        if (!calendar.getTimeZone().equals(timeZone)) {
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(timeZone);
        }
        return datePattern.applyRules(calendar, buf);
    }

    /**
     * 根据规则将时间戳转换为Appendable，复用Calendar对象，避免创建新对象
     *
     * @param millis 时间戳
     * @param buf    待拼接的 Appendable
     * @return buf 拼接后的Appendable
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
     * 估算生成的日期字符串长度 实际生成的字符串长度小于或等于此值
     *
     * @return 日期字符串长度
     */
    public int getMaxLengthEstimate() {
        return datePattern.getEstimateLength();
    }

}
