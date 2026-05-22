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
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.reflection.TypeParameterResolver;

/**
 * Resolves actual mapper method return and parameter classes, including generic signatures.
 * <p>
 * This helper centralizes MyBatis generic type resolution for starter AOT hint registration. It does not depend on
 * Spring and can therefore be reused by any mapper runtime integration that already has a mapper interface and method.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MapperMethodTypeResolver {

    /**
     * Prevents instantiation of this utility class.
     */
    private MapperMethodTypeResolver() {
        // Utility class; do not instantiate.
    }

    /**
     * Resolves a mapper method return class.
     * <p>
     * Array return types are normalized to their component type so AOT reflection hints target the entity element type.
     *
     * @param mapperInterface mapper interface
     * @param method          mapper method
     * @return resolved return class
     */
    public static Class<?> resolveReturnClass(Class<?> mapperInterface, Method method) {
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
        return typeToClass(resolvedReturnType, method.getReturnType());
    }

    /**
     * Resolves mapper method parameter classes.
     * <p>
     * Generic parameters are resolved against the mapper interface before being reduced to concrete classes for
     * reflection hint registration.
     *
     * @param mapperInterface mapper interface
     * @param method          mapper method
     * @return resolved parameter classes
     */
    public static Set<Class<?>> resolveParameterClasses(Class<?> mapperInterface, Method method) {
        return Stream.of(TypeParameterResolver.resolveParamTypes(method, mapperInterface))
                .map(x -> typeToClass(x, x instanceof Class ? (Class<?>) x : Object.class)).collect(Collectors.toSet());
    }

    /**
     * Converts a resolved generic {@link Type} into the concrete class that should be registered for reflection.
     * <p>
     * For parameterized maps, the value type is preferred because mapper methods commonly use map values as payload
     * objects; for other parameterized types, the first actual argument is used. When no concrete class can be derived,
     * the supplied fallback is returned.
     *
     * @param src      resolved generic type
     * @param fallback fallback class to use when the type cannot be reduced
     * @return concrete class for reflection registration
     */
    private static Class<?> typeToClass(Type src, Class<?> fallback) {
        Class<?> result = null;
        if (src instanceof Class<?>) {
            if (((Class<?>) src).isArray()) {
                result = ((Class<?>) src).getComponentType();
            } else {
                result = (Class<?>) src;
            }
        } else if (src instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) src;
            int index = parameterizedType.getRawType() instanceof Class
                    && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
                    && parameterizedType.getActualTypeArguments().length > 1 ? 1 : 0;
            Type actualType = parameterizedType.getActualTypeArguments()[index];
            result = typeToClass(actualType, fallback);
        }
        if (result == null) {
            result = fallback;
        }
        return result;
    }

}
