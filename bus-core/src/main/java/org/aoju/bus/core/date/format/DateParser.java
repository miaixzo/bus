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

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期解析接口，用于解析日期字符串为 {@link Date} 对象
 * Thanks to Apache Commons Lang 3.5
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface DateParser extends DateBasic {

    /**
     * 将日期字符串解析并转换为  {@link Date} 对象
     * 等价于 {@link java.text.DateFormat#parse(String)}
     *
     * @param source 日期字符串
     * @return {@link Date}
     * @throws ParseException 转换异常，被转换的字符串格式错误。
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
     * Updates the Calendar with parsed fields. Upon success, the ParsePosition index is updated to indicate how much of the source text was consumed.
     * Not all source text needs to be consumed.
     * Upon parse failure, ParsePosition error index is updated to the offset of the source text which does not match the supplied format.
     *
     * @param source   被转换的日期字符串
     * @param pos      定义开始转换的位置，转换结束后更新转换到的位置
     * @param calendar The calendar into which to set parsed fields.
     * @return true, if source has been parsed (pos parsePosition is updated); otherwise false (and pos errorIndex is updated)
     * @throws IllegalArgumentException when Calendar has been set to be not lenient, and a parsed field is out of range.
     */
    boolean parse(String source, ParsePosition pos, Calendar calendar);

    /**
     * 将日期字符串解析并转换为  {@link Date} 对象
     *
     * @param source A String whose beginning should be parsed.
     * @return a java.utils.Date object
     * @throws ParseException if the beginning of the specified string cannot be parsed.
     * @see java.text.DateFormat#parseObject(String)
     */
    Object parseObject(String source) throws ParseException;

    /**
     * 根据 {@link ParsePosition} 给定将日期字符串解析并转换为  {@link Date} 对象
     *
     * @param source A String whose beginning should be parsed.
     * @param pos    the parse position
     * @return a java.utils.Date object
     * @see java.text.DateFormat#parseObject(String, ParsePosition)
     */
    Object parseObject(String source, ParsePosition pos);

}
