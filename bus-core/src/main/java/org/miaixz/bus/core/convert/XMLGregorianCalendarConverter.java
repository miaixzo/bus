/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
