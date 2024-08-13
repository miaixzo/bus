/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.convert;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * 日期转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CalendarConverter extends AbstractConverter {

    private static final long serialVersionUID = -1L;

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
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    protected Calendar convertInternal(final Class<?> targetClass, final Object value) {
        // Handle Date
        if (value instanceof Date) {
            return org.miaixz.bus.core.center.date.Calendar.calendar((Date) value);
        }

        // Handle Long
        if (value instanceof Long) {
            // 此处使用自动拆装箱
            return org.miaixz.bus.core.center.date.Calendar.calendar((Long) value);
        }

        if (value instanceof XMLGregorianCalendar) {
            return org.miaixz.bus.core.center.date.Calendar.calendar((XMLGregorianCalendar) value);
        }

        final String valueStr = convertToString(value);
        return org.miaixz.bus.core.center.date.Calendar
                .calendar(StringKit.isBlank(format) ? org.miaixz.bus.core.center.date.Calendar.parse(valueStr)
                        : org.miaixz.bus.core.center.date.Calendar.parse(valueStr, format));
    }

}
