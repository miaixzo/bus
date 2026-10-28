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
package org.miaixz.bus.core.lang.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Caches mappings from generic type variables to actual types discovered along the generic declaration path selected by
 * {@link TypeKit#toParameterizedType(Type)}. Generic-array classes are derived without inserting them into the shared
 * cached mapping.
 *
 * @author Kimi Liu
 */
public class ActualTypeMapper {

    /**
     * A weak concurrent map to cache the mapping between a generic type and its resolved actual type arguments. The
     * keys are generic types (e.g., ParameterizedType), and the values are maps from TypeVariable to their actual Type.
     * Both keys and mapping objects are weakly held and may be reclaimed independently.
     */
    private static final WeakConcurrentMap<Type, Map<Type, Type>> CACHE = new WeakConcurrentMap<>();

    /**
     * Creates an instance; generic type-variable resolution remains available through the static API.
     */
    public ActualTypeMapper() {
        // No initialization required.
    }

    /**
     * Retrieves the mapping between generic type variables and their actual types for a given type. The result is
     * cached for reuse while its weak references remain live. The returned map is the shared mutable cache value, not a
     * defensive copy; callers must not modify it concurrently with resolution.
     *
     * @param type The non-null type containing generic parameters to be resolved.
     * @return The shared map where keys are generic type variables and values are their actual types.
     * @throws NullPointerException If the context type is null.
     */
    public static Map<Type, Type> get(final Type type) {
        return CACHE.computeIfAbsent(type, (key) -> createTypeMap(type));
    }

    /**
     * Retrieves the mapping between generic type variable names (as strings) and their actual types for a given type.
     *
     * @param type The type containing generic parameters to be resolved.
     * @return A map where keys are generic type variable names (e.g., "T") and values are their actual types.
     */
    public static Map<String, Type> getStringKeyMap(final Type type) {
        return Convert.toMap(String.class, Type.class, get(type));
    }

    /**
     * Retrieves the actual type corresponding to a given generic type variable within the context of a specific type.
     * If the type variable does not have a corresponding actual type, {@code null} is returned.
     *
     * @param type         The context type (e.g., a class or parameterized type) from which to resolve the type
     *                     variable.
     * @param typeVariable The generic type variable (e.g., 'T' in {@code List<T>}).
     * @return The actual type (e.g., {@code String.class}) corresponding to the type variable, or {@code null} if not
     *         found.
     */
    public static Type getActualType(final Type type, final TypeVariable<?> typeVariable) {
        final Map<Type, Type> typeTypeMap = get(type);
        Type result = typeTypeMap.get(typeVariable);
        // Follow any type-variable links already present in the shared mapping.
        while (result instanceof TypeVariable) {
            result = typeTypeMap.get(result);
        }
        return result;
    }

    /**
     * Retrieves an existing array mapping, or derives an array class when the component's mapping is a concrete class.
     * The derived array class is returned without mutating the shared mapping. If neither case applies, {@code null} is
     * returned.
     *
     * @param type             The context type (e.g., a class or parameterized type) from which to resolve the generic
     *                         array type.
     * @param genericArrayType The generic array type (e.g., {@code T[]}).
     * @return The actual array type (e.g., {@code String[].class}) corresponding to the generic array type, or
     *         {@code null} if not found.
     */
    public static Type getActualType(final Type type, final GenericArrayType genericArrayType) {
        final Map<Type, Type> typeTypeMap = get(type);
        Type actualType = typeTypeMap.get(genericArrayType);
        if (actualType == null) {
            // Resolve the actual type of the generic array component type
            final Type componentType = typeTypeMap.get(genericArrayType.getGenericComponentType());
            if (componentType instanceof Class) {
                // The shared cached mapping need not be mutated to derive an array class.
                actualType = ArrayKit.getArrayType((Class<?>) componentType);
            }
        }

        return actualType;
    }

    /**
     * Resolves each type-variable element against the context mapping and copies other type elements unchanged. In
     * particular, this overload does not derive generic-array classes for non-type-variable elements.
     *
     * @param type          The actual type's declaring class, which holds the mapping of generic parameters to actual
     *                      types.
     * @param typeVariables An array of generic type variables for which to find the actual types.
     * @return A new array in input order; unresolved type variables become null and other elements remain unchanged.
     */
    public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
        // Resolve only type-variable inputs; preserve other type elements unchanged.
        final Type[] result = new Type[typeVariables.length];
        for (int i = 0; i < typeVariables.length; i++) {
            result[i] = (typeVariables[i] instanceof TypeVariable)
                    ? getActualType(type, (TypeVariable<?>) typeVariables[i])
                    : typeVariables[i];
        }
        return result;
    }

    /**
     * Builds mappings along the parameterized declaration path selected by {@link TypeKit#toParameterizedType(Type)}.
     * At each step, concrete type arguments are recorded for the raw type's parameters, while arguments that are
     * themselves type variables are skipped. The next step examines that raw type. This is not an exhaustive traversal
     * of every superclass and interface branch, and it does not add derived generic-array mappings.
     *
     * @param type The type (e.g., a class or parameterized type) to analyze for generic type mappings.
     * @return A map where keys are generic type variables and values are their actual types.
     */
    private static Map<Type, Type> createTypeMap(Type type) {
        final Map<Type, Type> typeMap = new HashMap<>();

        // Traverse the inheritance hierarchy to find the mapping between generic variables and actual types.
        while (null != type) {
            final ParameterizedType parameterizedType = TypeKit.toParameterizedType(type);
            if (null == parameterizedType) {
                break;
            }
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            final Type[] typeParameters = rawType.getTypeParameters();

            Type value;
            for (int i = 0; i < typeParameters.length; i++) {
                value = typeArguments[i];
                // Skip cases where a generic variable maps to another generic variable.
                if (!(value instanceof TypeVariable)) {
                    typeMap.put(typeParameters[i], value);
                }
            }

            type = rawType;
        }

        return typeMap;
    }

}
