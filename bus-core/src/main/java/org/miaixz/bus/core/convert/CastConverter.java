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
import java.io.Serializable;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.exception.ConvertException;

/**
 * A converter that performs a direct cast if the value is already an instance of the target type. This serves as an
 * optimization to bypass unnecessary conversion logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CastConverter implements MatcherConverter, Serializable {

    /**
     * Constructs a new CastConverter. Utility class constructor for static access.
     */
    private CastConverter() {
    }

    /**
     * Singleton instance.
     */
    public static final CastConverter INSTANCE = new CastConverter();
    @Serial
    private static final long serialVersionUID = 2852266109781L;

    /**
     * Checks if the value is already an instance of the raw target type.
     *
     * @param targetType The target type.
     * @param rawType    The raw class of the target type.
     * @param value      The value to be checked.
     * @return {@code true} if the value is an instance of the raw type, {@code false} otherwise.
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isInstance(value);
    }

    /**
     * Returns the value directly without any conversion, as it is already of a compatible type.
     *
     * @param targetType The target type.
     * @param value      The value to be "converted".
     * @return The original value.
     * @throws ConvertException This exception is not thrown in this implementation.
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        // No conversion logic is needed, as the value is already of the target type
        // or a subclass/implementation thereof.
        return value;
    }

}
