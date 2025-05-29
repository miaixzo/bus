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
 * 日期格式化输出接口，定义日期格式化方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface FormatPrinter extends DatePrinter {

    /**
     * 格式化毫秒时间戳为字符串。
     *
     * @param millis 毫秒时间戳
     * @return 格式化后的字符串
     */
    String format(long millis);

    /**
     * 格式化日期对象为字符串，使用 {@code GregorianCalendar}。
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    String format(Date date);

    /**
     * 格式化日历对象为字符串。
     *
     * @param calendar 日历对象
     * @return 格式化后的字符串
     */
    String format(Calendar calendar);

    /**
     * 格式化毫秒时间戳到指定的输出缓冲区。
     *
     * @param millis 毫秒时间戳
     * @param buf    输出缓冲区
     * @param <B>    Appendable 类型，通常为 StringBuilder 或 StringBuffer
     * @return 格式化后的缓冲区
     */
    <B extends Appendable> B format(long millis, B buf);

    /**
     * 格式化日期对象到指定的输出缓冲区，使用 {@code GregorianCalendar}。
     *
     * @param date 日期对象
     * @param buf  输出缓冲区
     * @param <B>  Appendable 类型，通常为 StringBuilder 或 StringBuffer
     * @return 格式化后的缓冲区
     */
    <B extends Appendable> B format(Date date, B buf);

    /**
     * 格式化日历对象到指定的输出缓冲区，优先使用构造时指定的时区。
     *
     * @param calendar 日历对象
     * @param buf      输出缓冲区
     * @param <B>      Appendable 类型，通常为 StringBuilder 或 StringBuffer
     * @return 格式化后的缓冲区
     */
    <B extends Appendable> B format(Calendar calendar, B buf);

}