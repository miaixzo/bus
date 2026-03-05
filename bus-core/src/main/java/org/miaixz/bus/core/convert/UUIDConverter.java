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
import java.util.UUID;

/**
 * Converter for UUID objects
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UUIDConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852272763653L;

    /**
     * Converts the given value to a UUID.
     * <p>
     * Converts the value to a string first, then uses {@link UUID#fromString} to create the UUID.
     * </p>
     *
     * @param targetClass the target class (should be UUID.class)
     * @param value       the value to convert
     * @return the converted UUID object
     */
    @Override
    protected UUID convertInternal(final Class<?> targetClass, final Object value) {
        return UUID.fromString(convertToString(value));
    }

}
