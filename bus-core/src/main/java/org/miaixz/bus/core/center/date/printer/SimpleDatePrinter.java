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
 * 抽象日期基本信息类，提供日期格式、时区和地域信息。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleDatePrinter implements DatePrinter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852292363363L;

    /**
     * 日期格式模式
     */
    protected final String pattern;

    /**
     * 时区
     */
    protected final TimeZone timeZone;

    /**
     * 地域
     */
    protected final Locale locale;

    /**
     * 构造方法，初始化日期格式化信息。
     *
     * @param pattern  {@link java.text.SimpleDateFormat} 兼容的日期格式
     * @param timeZone 非空时区对象
     * @param locale   非空地域对象
     */
    protected SimpleDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    /**
     * 获取日期格式模式。
     *
     * @return 日期格式模式字符串
     */
    @Override
    public String getPattern() {
        return pattern;
    }

    /**
     * 获取时区。
     *
     * @return 时区对象
     */
    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * 获取地域。
     *
     * @return 地域对象
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * 判断是否与另一个对象相等。
     *
     * @param object 要比较的对象
     * @return 如果相等返回 true
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof SimpleDatePrinter)) {
            return false;
        }
        final SimpleDatePrinter other = (SimpleDatePrinter) object;
        return pattern.equals(other.pattern) && timeZone.equals(other.timeZone) && locale.equals(other.locale);
    }

    /**
     * 获取对象的哈希码。
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "SimpleDatePrinter[" + pattern + Symbol.COMMA + locale + Symbol.COMMA + timeZone.getID() + "]";
    }

}