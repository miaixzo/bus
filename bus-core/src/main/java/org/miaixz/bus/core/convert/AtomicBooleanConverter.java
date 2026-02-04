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
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.xyz.BooleanKit;

/**
 * Converts an object to an {@link AtomicBoolean}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicBooleanConverter extends AbstractConverter {

    /**
     * Constructs a new AtomicBooleanConverter. Utility class constructor for static access.
     */
    public AtomicBooleanConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final AtomicBooleanConverter INSTANCE = new AtomicBooleanConverter();
    @Serial
    private static final long serialVersionUID = 2852263652262L;

    /**
     * Internally converts the given value to an {@link AtomicBoolean}.
     *
     * @param targetClass The target class, which should be {@link AtomicBoolean}.
     * @param value       The value to be converted.
     * @return The converted {@link AtomicBoolean} object.
     */
    @Override
    protected AtomicBoolean convertInternal(final Class<?> targetClass, final Object value) {
        if (value instanceof Boolean) {
            return new AtomicBoolean((Boolean) value);
        }
        final String values = convertToString(value);
        return new AtomicBoolean(BooleanKit.toBoolean(values));
    }

}
