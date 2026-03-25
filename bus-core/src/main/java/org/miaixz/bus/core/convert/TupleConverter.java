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

import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.tuple.Tuple;

/**
 * Converter for {@link Tuple} objects
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TupleConverter implements Converter {

    /**
     * Singleton instance
     */
    public static final TupleConverter INSTANCE = new TupleConverter();

    /**
     * Converts the given value to a Tuple.
     * <p>
     * Converts the value to an Object array first, then creates a Tuple from the array.
     * </p>
     *
     * @param targetType the target type (should be Tuple.class)
     * @param value      the value to convert
     * @return a Tuple containing the converted array elements
     * @throws ConvertException if conversion fails
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        return Tuple.of(ArrayConverter.INSTANCE.convert(Object[].class, value));
    }

}
