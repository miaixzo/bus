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

    /**
     * Match method.
     *
     * @return the boolean value
     */
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
