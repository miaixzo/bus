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

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cursor.Cursor;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Resolves generic types, based on the source code of MyBatis 3. It adds the {@code resolveMapperTypes} method to
 * support resolving generic types of interfaces. This class primarily handles the runtime resolution of generic type
 * arguments to their actual concrete types by traversing the type hierarchy. Original source from
 * https://github.com/mybatis/mybatis-3
 *
 * @author Kimi Liu
 */
public class GenericTypeResolver {

    /**
     * Cache for resolved generic types of mapper interfaces (Class -> Actual Type Arguments).
     */
    private static final Map<Class<?>, Type[]> RESOLVED_MAPPER_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic types of the interface where a specific method is declared. Uses nested maps for
     * zero-allocation lookups: Method -> (Type -> Type[])
     */
    private static final Map<Method, Map<Type, Type[]>> RESOLVED_METHOD_MAPPER_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic field types (Field -> (Source Type -> Actual Type)).
     */
    private static final Map<Field, Map<Type, Type>> RESOLVED_FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic method return types. Uses nested maps for zero-allocation lookups: Method -> (Type ->
     * Type)
     */
    private static final Map<Method, Map<Type, Type>> RESOLVED_RETURN_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic method parameter types. Uses nested maps for zero-allocation lookups: Method -> (Type
     * -> Type[])
     */
    private static final Map<Method, Map<Type, Type[]>> RESOLVED_PARAM_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Initializes the resolver used to derive concrete mapper return types from generic method signatures.
     */
    public GenericTypeResolver() {
        // No initialization required.
    }

    /**
     * Gets the actual return type of a method, taking into account generics, collections, arrays, and MyBatis
     * annotations.
     *
     * @param method  The method to analyze.
     * @param srcType The class where the method is being invoked or the generic context class.
     * @return The actual concrete {@code Class} representing the return type.
     */
    public static Class<?> getReturnType(Method method, Class<?> srcType) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = resolveReturnType(method, srcType);

        // Handle resolved type if it's a concrete Class
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                // If it's an array, get the component type (e.g., String[] -> String)
                returnType = returnType.getComponentType();
            }
            if (void.class.equals(returnType)) {
                // Handle MyBatis @ResultType annotation for void methods (e.g., for bulk operations)
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        }
        // Handle resolved type if it's a ParameterizedType (e.g., List<T>, Map<K, V>)
        else if (resolvedReturnType instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();

            // Handle Collections (List, Set, Collection) and Cursor types: return the type of the element.
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // Handle nested generics (e.g., List<List<String>>)
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        // Handle array component types (e.g., List<byte[]>)
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter)
                                .getGenericComponentType();
                        // Construct Class<?> for the array type
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
            // Handle Map types with @MapKey annotation: return the value type.
            else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1]; // Value type is the second argument
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // Handle nested generics (e.g., Map<String, List<Integer>>)
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            }
            // Handle Optional<T> types: return the enclosed type T.
            else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0]; // Enclosed type is the first argument
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    /**
     * Resolves the generic types of a mapper interface (and its super-interfaces) to find the actual type arguments
     * provided by the implementation.
     *
     * @param srcType The interface type (usually a MyBatis Mapper interface).
     * @return An array of actual type arguments for the generic parameters of the interface.
     */
    public static Type[] resolveMapperTypes(Class<?> srcType) {
        // Use cache to prevent repeated calculations
        return RESOLVED_MAPPER_TYPES_CACHE.computeIfAbsent(srcType, key -> {
            Type[] types = key.getGenericInterfaces();
            List<Type> result = new ArrayList<>();
            for (Type type : types) {
                if (type instanceof Class) {
                    // Recursively resolve types of non-parameterized super-interfaces
                    result.addAll(Arrays.asList(resolveMapperTypes((Class<?>) type)));
                } else if (type instanceof ParameterizedType) {
                    // Add actual type arguments from parameterized super-interfaces
                    for (Type argument : ((ParameterizedType) type).getActualTypeArguments()) {
                        result.add(resolveType(argument, key, key));
                    }
                }
            }
            return result.toArray(new Type[0]);
        });
    }

    /**
     * Resolves the actual type arguments for the type variables defined in the interface where a method is declared,
     * based on the concrete type provided by {@code srcType}.
     *
     * @param method  The method declared in a generic interface.
     * @param srcType The concrete implementation type or context type.
     * @return An array of actual type arguments corresponding to the method's declaring interface's type parameters.
     */
    public static Type[] resolveMapperTypes(Method method, Type srcType) {
        // Use nested maps for zero-allocation lookups
        return RESOLVED_METHOD_MAPPER_TYPES_CACHE.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(srcType, k -> {
                    Class<?> declaringClass = method.getDeclaringClass();
                    TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
                    Type[] result = new Type[typeParameters.length];
                    for (int i = 0; i < typeParameters.length; i++) {
                        // Resolve each type variable (T, K, V, etc.) against the source type context
                        result[i] = resolveType(typeParameters[i], srcType, declaringClass);
                    }
                    return result;
                });
    }

    /**
     * Resolves the generic type of a field to its actual concrete type based on the source type context.
     *
     * @param field   The field whose type needs resolution.
     * @param srcType The source type (the class containing the field, possibly parameterized).
     * @return The actual resolved {@code Type} of the field.
     */
    public static Type resolveFieldType(Field field, Type srcType) {
        return RESOLVED_FIELD_TYPE_CACHE.computeIfAbsent(field, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(srcType, key -> resolveType(field.getGenericType(), key, field.getDeclaringClass()));
    }

    /**
     * Resolves the actual class of a field's type by calling {@code resolveTypeToClass} after generic resolution.
     *
     * @param field   The field whose type needs resolution.
     * @param srcType The source type (the class containing the field, possibly parameterized).
     * @return The actual concrete {@code Class} of the field's type.
     */
    public static Class<?> resolveFieldClass(Field field, Type srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        // 1. Resolve generics
        Type type = resolveType(fieldType, srcType, declaringClass);
        // 2. Convert resolved Type to Class
        return resolveTypeToClass(type);
    }

    /**
     * Converts a {@link Type} (which can be a Class, ParameterizedType, TypeVariable, or GenericArrayType) to its
     * corresponding raw {@link Class}.
     *
     * @param type The type to convert.
     * @return The corresponding raw class. Returns {@code Object.class} if the resolution fails or is ambiguous.
     */
    public static Class<?> resolveTypeToClass(Type type) {
        return resolveTypeToClass(type, new HashSet<>());
    }

    /**
     * Converts a type to its raw class while preventing recursive bounds from overflowing the stack.
     *
     * @param type     the type to convert
     * @param visiting types on the current resolution path, used to detect recursive bounds
     * @return the raw class, or {@code Object.class} for a cycle or an unsupported type
     */
    private static Class<?> resolveTypeToClass(Type type, Set<Type> visiting) {
        if (!visiting.add(type)) {
            return Object.class;
        }
        try {
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return resolveTypeToClass(((ParameterizedType) type).getRawType(), visiting);
            } else if (type instanceof TypeVariable<?>) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                return bounds.length == 0 ? Object.class : resolveTypeToClass(bounds[0], visiting);
            } else if (type instanceof WildcardType) {
                return resolveTypeToClass(((WildcardType) type).getUpperBounds()[0], visiting);
            } else if (type instanceof GenericArrayType) {
                return Array.newInstance(
                        resolveTypeToClass(((GenericArrayType) type).getGenericComponentType(), visiting),
                        0).getClass();
            }
            return Object.class;
        } finally {
            visiting.remove(type);
        }
    }

    /**
     * Resolves the generic return type of a method to its actual concrete type based on the source type context.
     *
     * @param method  The method whose return type needs resolution.
     * @param srcType The source type (the class where the method is declared, possibly parameterized).
     * @return The actual resolved {@code Type} of the method's return value.
     */
    public static Type resolveReturnType(Method method, Type srcType) {
        // Use nested maps for zero-allocation lookups
        return RESOLVED_RETURN_TYPE_CACHE.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(srcType, k -> {
                    Type returnType = method.getGenericReturnType();
                    Class<?> declaringClass = method.getDeclaringClass();
                    // Delegate to the core type resolution method
                    return resolveType(returnType, srcType, declaringClass);
                });
    }

    /**
     * Resolves the generic parameter types of a method to their actual concrete types based on the source type context.
     *
     * @param method  The method whose parameter types need resolution.
     * @param srcType The source type (the class where the method is declared, possibly parameterized).
     * @return An array of actual resolved {@code Type}s for the method's parameters.
     */
    public static Type[] resolveParamTypes(Method method, Type srcType) {
        // Use nested maps for zero-allocation lookups
        return RESOLVED_PARAM_TYPES_CACHE.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(srcType, k -> {
                    Type[] paramTypes = method.getGenericParameterTypes();
                    Class<?> declaringClass = method.getDeclaringClass();
                    Type[] result = new Type[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        // Resolve each parameter type against the source type context
                        result[i] = resolveType(paramTypes[i], srcType, declaringClass);
                    }
                    return result;
                });
    }

    /**
     * Resolves a generic type against bindings collected from the complete source hierarchy, including method-level
     * upper bounds and nested type arguments.
     *
     * @param type           The type to resolve.
     * @param srcType        The source type, providing the context for resolution (e.g., {@code MyClass<String>}).
     * @param declaringClass The original declaring class, retained for API compatibility; bindings use {@code srcType}.
     * @return The resolved actual {@code Type}.
     * @throws IllegalArgumentException if the source is neither a class nor a parameterized type
     */
    public static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (!(srcType instanceof Class<?>) && !(srcType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("The source type must be Class or ParameterizedType: " + srcType);
        }
        Map<TypeVariable<?>, Type> bindings = new HashMap<>();
        collectBindings(srcType, bindings, new HashSet<>());
        return substitute(type, bindings, new HashSet<>(), true);
    }

    /**
     * Collects bindings throughout the source hierarchy before resolving method-level upper bounds. Keeping the
     * complete context allows S extends T to resolve T from the concrete mapper even when S belongs to a method.
     *
     * @param source   the source hierarchy type, including any owner type
     * @param bindings the accumulated bindings from type variables to actual arguments
     * @param visited  raw classes already traversed, used to avoid repeated hierarchy visits
     */
    private static void collectBindings(Type source, Map<TypeVariable<?>, Type> bindings, Set<Class<?>> visited) {
        Class<?> raw;
        if (source instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) source;
            if (parameterized.getOwnerType() != null) {
                collectBindings(parameterized.getOwnerType(), bindings, visited);
            }
            raw = (Class<?>) parameterized.getRawType();
            TypeVariable<?>[] variables = raw.getTypeParameters();
            Type[] arguments = parameterized.getActualTypeArguments();
            for (int i = 0; i < variables.length; i++) {
                Type argument = substitute(arguments[i], bindings, new HashSet<>(), false);
                if (!variables[i].equals(argument)) {
                    bindings.put(variables[i], argument);
                }
            }
        } else if (source instanceof Class<?>) {
            raw = (Class<?>) source;
        } else {
            return;
        }
        if (!visited.add(raw)) {
            return;
        }
        if (raw.getGenericSuperclass() != null) {
            collectBindings(raw.getGenericSuperclass(), bindings, visited);
        }
        for (Type parent : raw.getGenericInterfaces()) {
            collectBindings(parent, bindings, visited);
        }
    }

    /**
     * Substitutes nested arguments, owners, arrays and wildcard bounds. Upper bounds are followed only after the
     * hierarchy has been collected; recursive bounds retain the variable at the cycle instead of overflowing.
     *
     * @param type          the type whose variables and nested arguments are substituted
     * @param bindings      the bindings collected from the concrete source hierarchy
     * @param visiting      type variables on the current substitution path
     * @param resolveBounds whether unbound variables should be resolved through their upper bounds
     * @return the substituted type; a variable is retained when a recursive bound is encountered
     */
    private static Type substitute(
            Type type,
            Map<TypeVariable<?>, Type> bindings,
            Set<TypeVariable<?>> visiting,
            boolean resolveBounds) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> variable = (TypeVariable<?>) type;
            if (!visiting.add(variable)) {
                return variable;
            }
            try {
                Type bound = bindings.get(variable);
                if (bound != null && !bound.equals(variable)) {
                    return substitute(bound, bindings, visiting, resolveBounds);
                }
                Type[] bounds = variable.getBounds();
                return resolveBounds && bounds.length > 0 ? substitute(bounds[0], bindings, visiting, true) : variable;
            } finally {
                visiting.remove(variable);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) type;
            Type owner = parameterized.getOwnerType();
            Type[] arguments = parameterized.getActualTypeArguments();
            Type[] resolved = substitute(arguments, bindings, visiting, resolveBounds);
            Type resolvedOwner = owner == null ? null : substitute(owner, bindings, visiting, resolveBounds);
            return Arrays.equals(arguments, resolved) && Objects.equals(owner, resolvedOwner) ? type
                    : new ParameterizedTypes((Class<?>) parameterized.getRawType(), resolvedOwner, resolved);
        } else if (type instanceof GenericArrayType) {
            Type component = ((GenericArrayType) type).getGenericComponentType();
            Type resolved = substitute(component, bindings, visiting, resolveBounds);
            return resolved instanceof Class<?> ? Array.newInstance((Class<?>) resolved, 0).getClass()
                    : Objects.equals(component, resolved) ? type : new GenericArrayTypes(resolved);
        } else if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            Type[] lower = substitute(wildcard.getLowerBounds(), bindings, visiting, resolveBounds);
            Type[] upper = substitute(wildcard.getUpperBounds(), bindings, visiting, resolveBounds);
            return Arrays.equals(lower, wildcard.getLowerBounds()) && Arrays.equals(upper, wildcard.getUpperBounds())
                    ? type
                    : new WildcardTypes(lower, upper);
        }
        return type;
    }

    /**
     * Substitutes each type using the same hierarchy bindings and recursive-bound guard.
     *
     * @param types         the types to substitute in declaration order
     * @param bindings      the bindings collected from the concrete source hierarchy
     * @param visiting      type variables on the current substitution path
     * @param resolveBounds whether unbound variables should be resolved through their upper bounds
     * @return a new array of substituted types in the original order
     */
    private static Type[] substitute(
            Type[] types,
            Map<TypeVariable<?>, Type> bindings,
            Set<TypeVariable<?>> visiting,
            boolean resolveBounds) {
        Type[] result = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = substitute(types[i], bindings, visiting, resolveBounds);
        }
        return result;
    }

    /**
     * An implementation of the standard Java reflection {@link ParameterizedType} interface. This custom implementation
     * is used to hold resolved generic type arguments.
     *
     * @author Kimi Liu
     */
    @Getter
    public static class ParameterizedTypes implements ParameterizedType {

        /**
         * The raw type (e.g., {@code List.class} for {@code List<String>}).
         */
        private final Class<?> rawType;

        /**
         * The owner type (usually {@code null} for static contexts like fields or methods).
         */
        private final Type ownerType;

        /**
         * The actual type arguments (e.g., {@code [String.class]} for {@code List<String>}).
         */
        private final Type[] actualTypeArguments;

        /**
         * Creates a parameterized type descriptor.
         *
         * @param rawType             the raw class
         * @param ownerType           the owning type
         * @param actualTypeArguments the actual type arguments
         */
        public ParameterizedTypes(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        /**
         * Returns a string representation of this parameterized type, mainly for debugging purposes.
         *
         * @return A string representation.
         */
        @Override
        public String toString() {
            return "ParameterizedTypes [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments="
                    + Arrays.toString(actualTypeArguments) + Symbol.BRACKET_RIGHT;
        }

    }

    /**
     * An implementation of the standard Java reflection {@link WildcardType} interface. This custom implementation is
     * used to hold resolved wildcard bounds.
     *
     * @author Kimi Liu
     */
    @Getter
    public static class WildcardTypes implements WildcardType {

        /**
         * The array of lower bounds (types preceded by {@code super}).
         */
        private final Type[] lowerBounds;

        /**
         * The array of upper bounds (types preceded by {@code extends} or default to {@code Object}).
         */
        private final Type[] upperBounds;

        /**
         * Creates a wildcard type descriptor.
         *
         * @param lowerBounds lower type bounds
         * @param upperBounds upper type bounds
         */
        public WildcardTypes(Type[] lowerBounds, Type[] upperBounds) {
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
        }

        // Omitted toString(), hashCode(), equals() for brevity, but should typically be included in production code

    }

    /**
     * An implementation of the standard Java reflection {@link GenericArrayType} interface. This custom implementation
     * is used to hold a resolved generic component type.
     *
     * @author Kimi Liu
     */
    @Getter
    public static class GenericArrayTypes implements GenericArrayType {

        /**
         * The generic component type (e.g., {@code T} in {@code T[]}).
         */
        private final Type genericComponentType;

        /**
         * Creates a generic array type descriptor.
         *
         * @param genericComponentType the generic component type
         */
        public GenericArrayTypes(Type genericComponentType) {
            this.genericComponentType = genericComponentType;
        }

    }

}
