/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Converter for date objects
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XMLGregorianCalendarConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852272916958L;

    private final DatatypeFactory datatypeFactory;
    /**
     * Date format pattern
     */
    private String format;

    /**
     * Constructs a new XMLGregorianCalendarConverter
     */
    public XMLGregorianCalendarConverter() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (final DatatypeConfigurationException e) {
            throw new DateException(e);
        }
    }

    /**
     * Gets the date format pattern
     *
     * @return the date format pattern
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the date format pattern
     *
     * @param format the date format pattern to set
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Converts the given value to an XMLGregorianCalendar.
     * <p>
     * Supports conversion from GregorianCalendar, Date, Calendar, Long (milliseconds), and string representations.
     * </p>
     *
     * @param targetClass the target class (should be XMLGregorianCalendar.class)
     * @param value       the value to convert
     * @return the converted XMLGregorianCalendar object
     * @throws ConvertException if conversion fails
     */
    @Override
    protected XMLGregorianCalendar convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof GregorianCalendar) {
            return datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) value);
        }

        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        // Handle Date
        if (value instanceof Date) {
            gregorianCalendar.setTime((Date) value);
        } else if (value instanceof Calendar calendar) {
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
