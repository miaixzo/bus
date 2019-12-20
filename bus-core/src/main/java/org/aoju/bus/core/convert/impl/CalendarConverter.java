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
package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换器
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class CalendarConverter extends AbstractConverter<Calendar> {

    /**
     * 日期格式化
     */
    private String format;

    /**
     * 获取日期格式
     *
     * @return 设置日期格式
     */
    public String getFormat() {
        return format;
    }

    /**
     * 设置日期格式
     *
     * @param format 日期格式
     */
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    protected Calendar convertInternal(Object value) {
        // Handle Date
        if (value instanceof Date) {
            return DateUtils.calendar((Date) value);
        }

        // Handle Long
        if (value instanceof Long) {
            //此处使用自动拆装箱
            return DateUtils.calendar((Long) value);
        }

        final String valueStr = convertToStr(value);
        return DateUtils.calendar(StringUtils.isBlank(format) ? DateUtils.parse(valueStr) : DateUtils.parse(valueStr, format));
    }

}
