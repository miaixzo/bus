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

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A converter with matching capability. Determines whether the target object meets the conditions. If met, converts;
 * otherwise skips. Implementing this interface can also directly convert without determining assertions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MatcherConverter extends Converter {

    /**
     * Determines whether the object to be converted matches the current converter. If met, converts; otherwise skips.
     *
     * @param targetType the target type of the conversion, cannot be {@code null}
     * @param rawType    the target raw type, consistent with this parameter when targetType is Class, cannot be
     *                   {@code null}
     * @param value      the value to be converted
     * @return whether it matches
     */
    boolean match(Type targetType, Class<?> rawType, Object value);

    /**
     * Determines whether the object to be converted matches the current converter. If met, converts; otherwise skips.
     *
     * @param targetType the target type of the conversion
     * @param value      the value to be converted
     * @return whether it matches
     */
    default boolean match(final Type targetType, final Object value) {
        return match(targetType, TypeKit.getClass(targetType), value);
    }

}
