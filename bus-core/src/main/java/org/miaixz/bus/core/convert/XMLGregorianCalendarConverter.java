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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 日期转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XMLGregorianCalendarConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852280392105L;

    private final DatatypeFactory datatypeFactory;
    /**
     * 日期格式化
     */
    private String format;

    /**
     * 构造
     */
    public XMLGregorianCalendarConverter() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (final DatatypeConfigurationException e) {
            throw new DateException(e);
        }
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
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    protected XMLGregorianCalendar convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof GregorianCalendar) {
            return datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) value);
        }

        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        // Handle Date
        if (value instanceof Date) {
            gregorianCalendar.setTime((Date) value);
        } else if (value instanceof Calendar) {
            final Calendar calendar = (Calendar) value;
            gregorianCalendar.setTimeZone(calendar.getTimeZone());
            gregorianCalendar.setFirstDayOfWeek(calendar.getFirstDayOfWeek());
            gregorianCalendar.setLenient(calendar.isLenient());
            gregorianCalendar.setTimeInMillis(calendar.getTimeInMillis());
        } else if (value instanceof Long) {
            gregorianCalendar.setTimeInMillis((Long) value);
        } else {
            final String values = convertToString(value);
            final Date date = StringKit.isBlank(format) ? DateKit.parse(values) : DateKit.parse(values, format);
            if (null == date) {
                throw new ConvertException("Unsupported date value: " + value);
            }
            gregorianCalendar.setTime(date);
        }
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

}
