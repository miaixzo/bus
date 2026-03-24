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
import java.time.ZoneId;
import java.util.TimeZone;

import org.miaixz.bus.core.xyz.ZoneKit;

/**
 * Converter for TimeZone objects
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TimeZoneConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852272228100L;

    /**
     * Singleton instance
     */
    public static final TimeZoneConverter INSTANCE = new TimeZoneConverter();

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType the target type
     * @param rawType    the raw class of the target type
     * @param value      the value to be converted
     * @return {@code true} if the target type is assignable from TimeZone
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return TimeZone.class.isAssignableFrom(rawType);
    }

    /**
     * Converts the given value to a TimeZone.
     * <p>
     * Supports conversion from ZoneId and string representations.
     * </p>
     *
     * @param targetClass the target class (should be TimeZone.class)
     * @param value       the value to convert
     * @return the converted TimeZone object
     */
    @Override
    protected TimeZone convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof ZoneId) {
            return ZoneKit.getTimeZone((ZoneId) value);
        }
        return ZoneKit.getTimeZone(convertToString(value));
    }

}
