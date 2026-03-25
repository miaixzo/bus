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

import org.miaixz.bus.core.lang.Charset;

/**
 * Converts an object to a {@link java.nio.charset.Charset}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CharsetConverter extends AbstractConverter {

    /**
     * Constructs a new CharsetConverter. Utility class constructor for static access.
     */
    public CharsetConverter() {
    }

    @Serial
    private static final long serialVersionUID = 2852266502058L;

    /**
     * Internally converts the given value to a {@link java.nio.charset.Charset}.
     *
     * @param targetClass The target class, which should be {@link java.nio.charset.Charset}.
     * @param value       The value to be converted, typically a string representing the charset name.
     * @return The converted {@link java.nio.charset.Charset} object.
     */
    @Override
    protected java.nio.charset.Charset convertInternal(final Class<?> targetClass, final Object value) {
        return Charset.charset(convertToString(value));
    }

}
