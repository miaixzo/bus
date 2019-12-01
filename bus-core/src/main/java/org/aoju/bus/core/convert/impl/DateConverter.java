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
import org.aoju.bus.core.date.DateTime;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换器
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class DateConverter extends AbstractConverter<Date> {

    private Class<? extends Date> targetType;
    /**
     * 日期格式化
     */
    private String format;

    /**
     * 构造
     *
     * @param targetType 目标类型
     */
    public DateConverter(Class<? extends Date> targetType) {
        this.targetType = targetType;
    }

    /**
     * 构造
     *
     * @param targetType 目标类型
     * @param format     日期格式
     */
    public DateConverter(Class<? extends Date> targetType, String format) {
        this.targetType = targetType;
        this.format = format;
    }

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
    protected Date convertInternal(Object value) {
        long mills = -1;
        if (value instanceof Calendar) {
            // Handle Calendar
            mills = ((Calendar) value).getTimeInMillis();
        } else if (value instanceof Long) {
            // Handle Long
            // 此处使用自动拆装箱
            mills = (Long) value;
        } else {
            // 统一按照字符串处理
            final String valueStr = convertToStr(value);
            try {
                mills = StringUtils.isBlank(format) ? DateUtils.parse(valueStr).getTime() : DateUtils.parse(valueStr, format).getTime();
            } catch (Exception e) {
                // Ignore Exception
            }
        }

        if (mills < 0) {
            return null;
        }

        // 返回指定类型
        if (Date.class == targetType) {
            return new Date(mills);
        }
        if (DateTime.class == targetType) {
            return new DateTime(mills);
        } else if (java.sql.Date.class == targetType) {
            return new java.sql.Date(mills);
        } else if (java.sql.Time.class == targetType) {
            return new java.sql.Time(mills);
        } else if (java.sql.Timestamp.class == targetType) {
            return new java.sql.Timestamp(mills);
        }

        throw new UnsupportedOperationException(StringUtils.format("Unsupport Date type: {}", this.targetType.getName()));
    }

}
