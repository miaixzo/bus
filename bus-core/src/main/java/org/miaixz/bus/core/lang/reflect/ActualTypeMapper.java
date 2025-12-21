/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Cache for mapping relationships between generic type variables and their actual types. This class provides utility
 * methods to resolve actual type arguments for generic types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ActualTypeMapper {

    /**
     * Constructs a new ActualTypeMapper. Utility class constructor for static access.
     */
    private ActualTypeMapper() {
    }

    /**
     * A weak concurrent map to cache the mapping between a generic type and its resolved actual type arguments. The
     * keys are generic types (e.g., ParameterizedType), and the values are maps from TypeVariable to their actual Type.
     */
    private static final WeakConcurrentMap<Type, Map<Type, Type>> CACHE = new WeakConcurrentMap<>();

    /**
     * Retrieves the mapping between generic type variables and their actual types for a given type. The result is
     * cached for performance.
     *
     * @param type The type containing generic parameters to be resolved.
     * @return A map where keys are generic type variables and values are their actual types.
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
        // Recursively resolve if the result is still a TypeVariable (e.g., A extends B, B extends C)
        while (result instanceof TypeVariable) {
            result = typeTypeMap.get(result);
        }
        return result;
    }

    /**
     * Retrieves the actual type corresponding to a given generic array type within the context of a specific type. If
     * the generic array type does not have a corresponding actual type, {@code null} is returned.
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
                actualType = ArrayKit.getArrayType((Class<?>) componentType);
                typeTypeMap.put(genericArrayType, actualType);
            }
        }

        return actualType;
    }

    /**
     * Retrieves the actual types corresponding to an array of generic type variables within the context of a specific
     * type. The mapping between generic parameters in a subclass and those in a superclass (or interface) is
     * one-to-one.
     *
     * @param type          The actual type's declaring class, which holds the mapping of generic parameters to actual
     *                      types.
     * @param typeVariables An array of generic type variables for which to find the actual types.
     * @return An array of actual types corresponding to the given generic type variables. If a type variable has no
     *         corresponding actual type, the corresponding position in the array will be {@code null}.
     */
    public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
        // Find the position of this generic parameter in the method definition's declaring class or interface.
        final Type[] result = new Type[typeVariables.length];
        for (int i = 0; i < typeVariables.length; i++) {
            result[i] = (typeVariables[i] instanceof TypeVariable)
                    ? getActualType(type, (TypeVariable<?>) typeVariables[i])
                    : typeVariables[i];
        }
        return result;
    }

    /**
     * Creates a map of all generic type variables to their actual types for a given type. This method traverses the
     * inheritance hierarchy to resolve all generic type arguments.
     * <p>
     * The mapping process considers two main scenarios:
     * <ol>
     * <li>A superclass defines a type variable, and a subclass specifies its actual type.</li>
     * <li>A superclass defines a type variable, and a subclass inherits this variable, leaving its resolution to a
     * further subclass, and so on.</li>
     * </ol>
     * This method adds all such relationships at each level of the hierarchy to the map. When looking up an actual
     * type, if the mapped value is still a type variable, the lookup continues recursively until an actual type is
     * found or {@code null} is returned. If the input type is not a {@code Class} (e.g., {@code TypeReference}), it
     * extracts the actual generic object class from its generic parameters and processes it as a class.
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
