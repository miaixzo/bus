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
package org.miaixz.bus.core.date.formatter;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化输出接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DatePrinter extends DateMotd {

    /**
     * 格式化日期表示的毫秒数
     *
     * @param millis 日期毫秒数
     * @return 格式化的字符串
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
     * 格式化 {@link Calendar} 对象
     *
     * @param calendar {@link Calendar}
     * @return 格式化后的字符串
     */
    String format(Calendar calendar);

    /**
     * 将毫秒{@code long}值格式化为提供的{@code Appendable}
     *
     * @param millis 要格式化的毫秒值
     * @param buf    要格式化为的缓冲区
     * @param <B>    附加类类型，通常是StringBuilder或StringBuffer
     * @return 指定的字符串缓冲区
     */
    <B extends Appendable> B format(long millis, B buf);

    /**
     * 使用{@code GregorianCalendar}将{@code Date}对象格式化为提供的{@code Appendable}
     *
     * @param date 格式的日期
     * @param buf  要格式化为的缓冲区
     * @param <B>  附加类类型，通常是StringBuilder或StringBuffer
     * @return 指定的字符串缓冲区
     */
    <B extends Appendable> B format(Date date, B buf);

    /**
     * 将{@code Calendar}对象格式化为提供的{@code Appendable}
     * 日历上设置的时区仅用于调整时间偏移。解析器构造期间指定的时区将确定格式化字符串中使用的时区
     *
     * @param calendar 要格式化的日历
     * @param buf      要格式化为的缓冲区
     * @param <B>      附加类类型，通常是StringBuilder或StringBuffer
     * @return 指定的字符串缓冲区
     */
    <B extends Appendable> B format(Calendar calendar, B buf);

}
