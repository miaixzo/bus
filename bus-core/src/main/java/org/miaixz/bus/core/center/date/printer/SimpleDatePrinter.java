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

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

import org.miaixz.bus.core.lang.Symbol;

/**
 * 抽象日期基本信息类，包括日期格式、时区、本地化等信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleDatePrinter implements DatePrinter, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * The pattern
     */
    protected final String pattern;
    /**
     * The time zone
     */
    protected final TimeZone timeZone;
    /**
     * The locale
     */
    protected final Locale locale;

    /**
     * 构造，内部使用
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 非空时区{@link TimeZone}
     * @param locale   非空{@link Locale} 日期地理位置
     */
    protected SimpleDatePrinter(final String pattern, final TimeZone timeZone, final Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean equals(final Object object) {
        if (object instanceof FastDatePrinter == false) {
            return false;
        }
        final SimpleDatePrinter other = (SimpleDatePrinter) object;
        return pattern.equals(other.pattern) && timeZone.equals(other.timeZone) && locale.equals(other.locale);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }

    @Override
    public String toString() {
        return "SimpleDatePrinter[" + pattern + Symbol.COMMA + locale + Symbol.COMMA + timeZone.getID() + "]";
    }

}
