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
