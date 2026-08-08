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
package org.miaixz.bus.extra.json;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Objects;

/**
 * Immutable, framework-independent options for JSON deserialization.
 *
 * @param typeFilter allow-list applied to concrete classes found in the requested Java type
 * @author Kimi Liu
 * @since Java 21+
 */
public record JsonReadOptions(JsonTypeFilter typeFilter) {

    /**
     * Creates validated read options.
     *
     * @param typeFilter allow-list applied to target classes
     */
    public JsonReadOptions {
        typeFilter = Objects.requireNonNull(typeFilter, "typeFilter");
    }

    /**
     * Creates permissive options for compatibility with existing provider methods.
     *
     * @return permissive read options
     */
    public static JsonReadOptions defaults() {
        return new JsonReadOptions(JsonTypeFilter.always());
    }

    /**
     * Validates every concrete class reachable from the requested Java type.
     *
     * @param type requested deserialization type
     * @throws IllegalArgumentException if the type is {@code null} or rejected by the configured allow-list
     */
    public void validate(Type type) {
        Objects.requireNonNull(type, "type");
        validateType(type);
    }

    /**
     * Recursively validates classes, parameterized arguments, arrays, and wildcard bounds.
     *
     * @param type current type node
     * @throws IllegalArgumentException if a concrete class is rejected
     */
    private void validateType(Type type) {
        if (type instanceof Class<?> clazz) {
            Class<?> candidate = clazz.isArray() ? clazz.getComponentType() : clazz;
            if (!candidate.isPrimitive() && !typeFilter.accept(candidate)) {
                throw new IllegalArgumentException("JSON target type is not allowed: " + candidate.getName());
            }
            return;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            validateType(parameterizedType.getRawType());
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                validateType(argument);
            }
            return;
        }
        if (type instanceof GenericArrayType genericArrayType) {
            validateType(genericArrayType.getGenericComponentType());
            return;
        }
        if (type instanceof WildcardType wildcardType) {
            for (Type bound : wildcardType.getUpperBounds()) {
                validateType(bound);
            }
            for (Type bound : wildcardType.getLowerBounds()) {
                validateType(bound);
            }
        }
    }

}
