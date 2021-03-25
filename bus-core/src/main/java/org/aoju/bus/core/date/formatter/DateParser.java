/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.date.formatter;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期解析接口,用于解析日期字符串为 {@link Date} 对象
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public interface DateParser extends DateMotd {

    /**
     * 将日期字符串解析并转换为  {@link Date} 对象
     * 等价于 {@link java.text.DateFormat#parse(String)}
     *
     * @param source 日期字符串
     * @return {@link Date}
     * @throws ParseException 转换异常,被转换的字符串格式错误
     */
    Date parse(String source) throws ParseException;

    /**
     * 将日期字符串解析并转换为  {@link Date} 对象
     * 等价于 {@link java.text.DateFormat#parse(String, ParsePosition)}
     *
     * @param source 日期字符串
     * @param pos    {@link ParsePosition}
     * @return {@link Date}
     */
    Date parse(String source, ParsePosition pos);

    /**
     * 根据给定格式转换日期字符串
     * 使用解析的字段更新日历。成功之后，将更新ParsePosition索引，以指示消耗了多少源文本。并不是所有的源文本都需要使用
     * 在解析失败时，ParsePosition错误索引将更新为源文本的偏移量，该偏移量与提供的格式不匹配
     *
     * @param source   被转换的日期字符串
     * @param pos      定义开始转换的位置,转换结束后更新转换到的位置
     * @param calendar 用于设置已解析字段的日历
     * @return 如果源已被解析(pos parsePosition 已更新);否则为false(并更新 pos errorIndex)
     * @throws IllegalArgumentException 当日历被设置为不宽松，并且已解析字段超出范围时
     */
    boolean parse(String source, ParsePosition pos, Calendar calendar);

    /**
     * 将日期字符串解析并转换为  {@link Date} 对象
     *
     * @param source 应该解析其开头的字符串
     * @return 日期对象
     * @throws ParseException 如果无法解析指定字符串的开头
     * @see java.text.DateFormat#parseObject(String)
     */
    Object parseObject(String source) throws ParseException;

    /**
     * 根据 {@link ParsePosition} 给定将日期字符串解析并转换为  {@link Date} 对象
     *
     * @param source 应该解析其开头的字符串
     * @param pos    解析的位置
     * @return 日期对象
     * @see java.text.DateFormat#parseObject(String, ParsePosition)
     */
    Object parseObject(String source, ParsePosition pos);

}
