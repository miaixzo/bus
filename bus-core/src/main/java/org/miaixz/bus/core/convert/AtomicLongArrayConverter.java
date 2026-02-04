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
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Converts an object to an {@link AtomicLongArray}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicLongArrayConverter extends AbstractConverter {

    /**
     * Constructs a new AtomicLongArrayConverter. Utility class constructor for static access.
     */
    public AtomicLongArrayConverter() {
    }

    @Serial
    private static final long serialVersionUID = 2852265081792L;

    /**
     * Internally converts the given value to an {@link AtomicLongArray}.
     *
     * @param targetClass The target class, which should be {@link AtomicLongArray}.
     * @param value       The value to be converted.
     * @return The converted {@link AtomicLongArray} object.
     */
    @Override
    protected AtomicLongArray convertInternal(final Class<?> targetClass, final Object value) {
        return new AtomicLongArray(Convert.convert(long[].class, value));
    }

}
