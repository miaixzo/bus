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
package org.miaixz.bus.mapper.parsing;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.builder.ClassMetaResolver;
import org.miaixz.bus.mapper.builder.GenericTypeResolver;

/**
 * An abstract entity class finder that determines the corresponding entity class type based on generics from return
 * values, parameters, and interface generic arguments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class SchemaTypeParser implements ClassMetaResolver {

    /**
     * Finds the entity class corresponding to the current method by checking the method's return value, parameters, and
     * interface generics in order.
     *
     * @param mapperType   The Mapper interface, which cannot be null.
     * @param mapperMethod The Mapper interface method, which can be null.
     * @return An {@link Optional} containing the entity class type.
     */
    @Override
    public Optional<Class<?>> findClass(Class<?> mapperType, Method mapperMethod) {
        // First, check the return value.
        Optional<Class<?>> optionalClass;
        if (mapperMethod != null) {
            optionalClass = getClassByMapperMethodReturnType(mapperType, mapperMethod);
            if (optionalClass.isPresent()) {
                return optionalClass;
            }
            // Then, check the parameters.
            optionalClass = getClassByMapperMethodParamTypes(mapperType, mapperMethod);
            if (optionalClass.isPresent()) {
                return optionalClass;
            }
            // Finally, get it from the interface generics.
            optionalClass = getClassByMapperMethodAndMapperType(mapperType, mapperMethod);
            if (optionalClass.isPresent()) {
                return optionalClass;
            }
        }
        return getClassByMapperType(mapperType);
    }

    /**
     * Gets the entity class based on the method's return type.
     *
     * @param mapperType   The Mapper interface.
     * @param mapperMethod The method.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByMapperMethodReturnType(Class<?> mapperType, Method mapperMethod) {
        Class<?> returnType = GenericTypeResolver.getReturnType(mapperMethod, mapperType);
        return isClass(returnType) ? Optional.of(returnType) : Optional.empty();
    }

    /**
     * Gets the entity class based on the method's parameter types.
     *
     * @param mapperType   The Mapper interface.
     * @param mapperMethod The method.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByMapperMethodParamTypes(Class<?> mapperType, Method mapperMethod) {
        return getClassByTypes(GenericTypeResolver.resolveParamTypes(mapperMethod, mapperType));
    }

    /**
     * Gets the entity class from the generics of the interface where the method is defined. This is only applicable to
     * methods defined in a generic interface.
     *
     * @param mapperType   The Mapper interface.
     * @param mapperMethod The method.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByMapperMethodAndMapperType(Class<?> mapperType, Method mapperMethod) {
        return getClassByTypes(GenericTypeResolver.resolveMapperTypes(mapperMethod, mapperType));
    }

    /**
     * Gets the entity class from the interface generics. This has the lowest priority and is independent of the
     * currently executing method.
     *
     * @param mapperType The Mapper interface.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByMapperType(Class<?> mapperType) {
        return getClassByTypes(GenericTypeResolver.resolveMapperTypes(mapperType));
    }

    /**
     * Gets a potential entity class type from a single {@link Type}.
     *
     * @param type The type.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByType(Type type) {
        if (type instanceof Class) {
            return Optional.of((Class<?>) type);
        } else if (type instanceof GenericTypeResolver.ParameterizedTypes) {
            return getClassByTypes(((GenericTypeResolver.ParameterizedTypes) type).getActualTypeArguments());
        } else if (type instanceof GenericTypeResolver.WildcardTypes) {
            Optional<Class<?>> optionalClass = getClassByTypes(
                    ((GenericTypeResolver.WildcardTypes) type).getLowerBounds());
            if (optionalClass.isPresent()) {
                return optionalClass;
            }
            return getClassByTypes(((GenericTypeResolver.WildcardTypes) type).getUpperBounds());
        } else if (type instanceof GenericTypeResolver.GenericArrayTypes) {
            return getClassByType(((GenericTypeResolver.GenericArrayTypes) type).getGenericComponentType());
        }
        return Optional.empty();
    }

    /**
     * Iterates through an array of types to find a potential entity class type.
     *
     * @param types The array of types.
     * @return An {@link Optional} containing the entity class type.
     */
    protected Optional<Class<?>> getClassByTypes(Type[] types) {
        for (Type type : types) {
            Optional<Class<?>> optionalClass = getClassByType(type);
            if (optionalClass.isPresent() && isClass(optionalClass.getOrNull())) {
                return optionalClass;
            }
        }
        return Optional.empty();
    }

}
