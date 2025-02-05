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
package org.miaixz.bus.core.center.date.format.parser;

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import org.miaixz.bus.core.center.date.printer.DatePrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * 带有{@link ParsePosition}的日期解析接口，用于解析日期字符串为 {@link Date} 对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PositionDateParser extends DateParser, DatePrinter {

    /**
     * 将日期字符串解析并转换为 {@link Date} 对象 等价于 {@link java.text.DateFormat#parse(String, ParsePosition)}
     *
     * @param source 日期字符串
     * @param pos    {@link ParsePosition}
     * @return {@link Date}
     */
    default Date parse(final CharSequence source, final ParsePosition pos) {
        return parseCalendar(source, pos, Keys.getBoolean(Keys.DATE_LENIENT, false)).getTime();
    }

    /**
     * 根据给定格式更新{@link Calendar} 解析成功后，{@link ParsePosition#getIndex()}更新成转换到的位置
     * 失败则{@link ParsePosition#getErrorIndex()}更新到解析失败的位置
     *
     * @param source   被转换的日期字符串
     * @param pos      定义开始转换的位置，转换结束后更新转换到的位置，{@code null}表示忽略，从第一个字符开始转换
     * @param calendar 解析并更新的{@link Calendar}
     * @return 解析成功返回 {@code true}，否则返回{@code false}
     */
    boolean parse(CharSequence source, ParsePosition pos, Calendar calendar);

    /**
     * 将日期字符串解析并转换为 {@link Calendar} 对象
     *
     * @param source  日期字符串
     * @param pos     {@link ParsePosition}
     * @param lenient 是否宽容模式
     * @return {@link Calendar}
     */
    default Calendar parseCalendar(final CharSequence source, final ParsePosition pos, final boolean lenient) {
        Assert.notBlank(source, "Date str must be not blank!");
        final Calendar calendar = Calendar.getInstance(getTimeZone(), getLocale());
        calendar.clear();
        calendar.setLenient(lenient);

        if (parse(source.toString(), pos, calendar)) {
            return calendar;
        }

        throw new DateException("Parse [{}] with format [{}] error, at: {}", source, getPattern(), pos.getErrorIndex());
    }

}
