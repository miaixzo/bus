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

import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化输出接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface FormatPrinter extends DatePrinter {

    /**
     * 格式化日期表示的毫秒数
     *
     * @param millis 日期毫秒数
     * @return the formatted string
     */
    String format(long millis);

    /**
     * 使用 {@code GregorianCalendar} 格式化 {@code Date}
     *
     * @param date 日期 {@link Date}
     * @return 格式化后的字符串
     */
    String format(Date date);

    /**
     * Formats a {@code Calendar} object. 格式化 {@link Calendar}
     *
     * @param calendar {@link Calendar}
     * @return 格式化后的字符串
     */
    String format(Calendar calendar);

    /**
     * Formats a millisecond {@code long} value into the supplied {@code Appendable}.
     *
     * @param millis the millisecond value to format
     * @param buf    the buffer to format into
     * @param <B>    the Appendable class type, usually StringBuilder or StringBuffer.
     * @return the specified string buffer
     */
    <B extends Appendable> B format(long millis, B buf);

    /**
     * Formats a {@code Date} object into the supplied {@code Appendable} using a {@code GregorianCalendar}.
     *
     * @param date the date to format
     * @param buf  the buffer to format into
     * @param <B>  the Appendable class type, usually StringBuilder or StringBuffer.
     * @return the specified string buffer
     */
    <B extends Appendable> B format(Date date, B buf);

    /**
     * Formats a {@code Calendar} object into the supplied {@code Appendable}. The TimeZone set on the Calendar is only
     * used to adjust the time offset. The TimeZone specified during the construction of the Parser will determine the
     * TimeZone used in the formatted string.
     *
     * @param calendar the calendar to format
     * @param buf      the buffer to format into
     * @param <B>      the Appendable class type, usually StringBuilder or StringBuffer.
     * @return the specified string buffer
     */
    <B extends Appendable> B format(Calendar calendar, B buf);

}
