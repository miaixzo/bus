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
import java.lang.reflect.Type;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.center.date.Resolver;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Converter for Date objects
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852267950550L;

    /**
     * Singleton instance
     */
    public static final DateConverter INSTANCE = new DateConverter();

    /**
     * Date format pattern
     */
    private String format;

    /**
     * Constructs a new DateConverter
     */
    public DateConverter() {
        this(null);
    }

    /**
     * Constructs a new DateConverter with specified format
     *
     * @param format the date format pattern, {@code null} means no format defined
     */
    public DateConverter(final String format) {
        this.format = format;
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
     * @param format the date format pattern
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Date.class.isAssignableFrom(rawType);
    }

    @Override
    protected java.util.Date convertInternal(final Class<?> targetClass, final Object value) {
        if (value == null || (value instanceof CharSequence && StringKit.isBlank(value.toString()))) {
            return null;
        }
        if (value instanceof TemporalAccessor) {
            return wrap(targetClass, DateKit.date((TemporalAccessor) value));
        } else if (value instanceof Calendar) {
            return wrap(targetClass, DateKit.date((Calendar) value));
        } else if (null == this.format && value instanceof Number) {
            return wrap(targetClass, ((Number) value).longValue());
        } else {
            // Process uniformly as string
            final String values = convertToString(value);
            final Date date = StringKit.isBlank(this.format) //
                    ? Resolver.parse(values) //
                    : Resolver.parse(values, this.format);
            if (null != date) {
                return wrap(targetClass, date);
            }
        }

        throw new ConvertException("Can not support {}:[{}] to {}", value.getClass().getName(), value,
                targetClass.getName());
    }

    /**
     * Converts java.util.Date to subtype
     *
     * @param date the Date
     * @return the target type object
     */
    private java.util.Date wrap(final Class<?> targetClass, final Date date) {
        if (targetClass == date.getClass()) {
            return date;
        }

        return wrap(targetClass, date.getTime());
    }

    /**
     * Converts timestamp to subtype, supporting:
     * <ul>
     * <li>{@link java.util.Date}</li>
     * <li>{@link DateTime}</li>
     * <li>{@link java.sql.Date}</li>
     * <li>{@link java.sql.Time}</li>
     * <li>{@link java.sql.Timestamp}</li>
     * </ul>
     *
     * @param mills the timestamp in milliseconds
     * @return the target type object
     */
    private java.util.Date wrap(final Class<?> targetClass, final long mills) {
        // Return specified type
        if (java.util.Date.class == targetClass) {
            return new java.util.Date(mills);
        }
        if (DateTime.class == targetClass) {
            return DateKit.date(mills);
        }

        final String dateClassName = targetClass.getName();
        if (dateClassName.startsWith("java.sql.")) {
            // To solve the problem that users do not introduce java.sql module in JDK9+ modular projects,
            // add judgment here. If targetClass is a class of java.sql, it means this module has been introduced
            return DateKit.SQL.wrap(targetClass, mills);
        }

        throw new ConvertException("Unsupported target Date type: {}", targetClass.getName());
    }

}
