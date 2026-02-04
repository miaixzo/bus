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

import org.miaixz.bus.core.xyz.BooleanKit;

/**
 * Converts an object to a {@link Boolean}.
 * <p>
 * The conversion rules are as follows:
 * <ul>
 * <li>A numeric value of 0 is treated as {@code false}, while any other number is {@code true}.</li>
 * <li>For string values, the conversion is delegated to {@link BooleanKit#toBoolean(String)}.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BooleanConverter extends AbstractConverter {

    /**
     * Constructs a new BooleanConverter. Utility class constructor for static access.
     */
    public BooleanConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final BooleanConverter INSTANCE = new BooleanConverter();
    @Serial
    private static final long serialVersionUID = 2852265810501L;

    /**
     * Internally converts the given value to a {@link Boolean}.
     *
     * @param targetClass The target class, which should be {@link Boolean}.
     * @param value       The value to be converted.
     * @return The converted {@link Boolean} object.
     */
    @Override
    protected Boolean convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Number) {
            // A value of 0 is false, other numbers are true.
            return 0 != ((Number) value).doubleValue();
        }
        // For other types, convert to a string and then to a boolean.
        return BooleanKit.toBoolean(convertToString(value));
    }

}
