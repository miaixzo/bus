/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.Resolver;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Converts an object to a {@link java.util.Calendar}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CalendarConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852265956002L;

    /**
     * The date format pattern.
     */
    private String format;

    /**
     * Constructs a new {@code CalendarConverter} with no specific format.
     */
    public CalendarConverter() {
        this(null);
    }

    /**
     * Constructs a new {@code CalendarConverter} with the specified format.
     *
     * @param format The date format pattern. If {@code null}, a default format will be used.
     */
    public CalendarConverter(final String format) {
        this.format = format;
    }

    /**
     * Gets the date format pattern.
     *
     * @return The date format pattern.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the date format pattern.
     *
     * @param format The date format pattern.
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    /**
     * Internally converts the given value to a {@link java.util.Calendar}.
     *
     * @param targetClass The target class, which should be {@link java.util.Calendar}.
     * @param value       The value to be converted.
     * @return The converted {@link java.util.Calendar} object.
     */
    @Override
    protected java.util.Calendar convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Date) {
            return Calendar.calendar((Date) value);
        }

        if (value instanceof Long) {
            return Calendar.calendar((Long) value);
        }

        if (value instanceof XMLGregorianCalendar) {
            return Calendar.calendar((XMLGregorianCalendar) value);
        }

        final String valueStr = convertToString(value);
        return Calendar
                .calendar(StringKit.isBlank(format) ? Resolver.parse(valueStr) : Resolver.parse(valueStr, format));
    }

}
