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
package org.miaixz.bus.mapper.builder;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cursor.Cursor;
import org.miaixz.bus.core.lang.Optional;

/**
 * A utility class for resolving generic types, based on the source code of MyBatis 3. It adds the
 * {@code resolveMapperTypes} method to support resolving generic types of interfaces. This class primarily handles the
 * runtime resolution of generic type arguments to their actual concrete types by traversing the type hierarchy.
 * Original source from https://github.com/mybatis/mybatis-3
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericTypeResolver {

    /**
     * Cache for resolved generic types of mapper interfaces (Class -> Actual Type Arguments).
     */
    private static final Map<Class<?>, Type[]> RESOLVED_MAPPER_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic types of the interface where a specific method is declared (Method -> Actual Type
     * Arguments).
     */
    private static final Map<Method, Type[]> RESOLVED_METHOD_MAPPER_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic field types (Field -> Actual Type).
     */
    private static final Map<Field, Type> RESOLVED_FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic method return types (Method -> Actual Return Type).
     */
    private static final Map<Method, Type> RESOLVED_RETURN_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * Cache for resolved generic method parameter types (Method -> Actual Parameter Types Array).
     */
    private static final Map<Method, Type[]> RESOLVED_PARAM_TYPES_CACHE = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    public GenericTypeResolver() {
        // Private constructor for utility class
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
                    Collections.addAll(result, ((ParameterizedType) type).getActualTypeArguments());
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
        // Use cache to prevent repeated calculations
        return RESOLVED_METHOD_MAPPER_TYPES_CACHE.computeIfAbsent(method, key -> {
            Class<?> declaringClass = key.getDeclaringClass();
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
        // Use cache to prevent repeated calculations
        return RESOLVED_FIELD_TYPE_CACHE.computeIfAbsent(field, key -> {
            Type fieldType = key.getGenericType();
            Class<?> declaringClass = key.getDeclaringClass();
            // Delegate to the core type resolution method
            return resolveType(fieldType, srcType, declaringClass);
        });
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
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            // For ParameterizedType (e.g., List<String>), return the raw type (List.class)
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable<?>) {
            // For TypeVariable (e.g., T), return the first upper bound (e.g., Serializable or Object)
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return (Class<?>) bounds[0];
        } else if (type instanceof GenericArrayType) {
            // For GenericArrayType (e.g., T[]), resolve the component type and return the array class
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            if (componentType instanceof Class) {
                return Array.newInstance((Class<?>) componentType, 0).getClass();
            } else {
                Class<?> componentClass = resolveTypeToClass(componentType);
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return Object.class;
    }

    /**
     * Resolves the generic return type of a method to its actual concrete type based on the source type context.
     *
     * @param method  The method whose return type needs resolution.
     * @param srcType The source type (the class where the method is declared, possibly parameterized).
     * @return The actual resolved {@code Type} of the method's return value.
     */
    public static Type resolveReturnType(Method method, Type srcType) {
        // Use cache to prevent repeated calculations
        return RESOLVED_RETURN_TYPE_CACHE.computeIfAbsent(method, key -> {
            Type returnType = key.getGenericReturnType();
            Class<?> declaringClass = key.getDeclaringClass();
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
        // Use cache to prevent repeated calculations
        return RESOLVED_PARAM_TYPES_CACHE.computeIfAbsent(method, key -> {
            Type[] paramTypes = key.getGenericParameterTypes();
            Class<?> declaringClass = key.getDeclaringClass();
            Type[] result = new Type[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                // Resolve each parameter type against the source type context
                result[i] = resolveType(paramTypes[i], srcType, declaringClass);
            }
            return result;
        });
    }

    /**
     * Core method for resolving a generic type by delegating to specific resolution methods based on the type's
     * concrete implementation (TypeVariable, ParameterizedType, GenericArrayType, or Class).
     *
     * @param type           The type to resolve.
     * @param srcType        The source type, providing the context for resolution (e.g., {@code MyClass<String>}).
     * @param declaringClass The class where the original generic type was declared.
     * @return The resolved actual {@code Type}.
     */
    public static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
            return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        } else {
            // Return Class or non-generic types directly
            return type;
        }
    }

    /**
     * Resolves the component type of a {@link GenericArrayType}.
     *
     * @param genericArrayType The generic array type (e.g., T[]).
     * @param srcType          The source type context.
     * @param declaringClass   The class where the generic type was declared.
     * @return The resolved actual array type or a new {@code GenericArrayType} implementation if the component is still
     *         generic.
     */
    private static Type resolveGenericArrayType(
            GenericArrayType genericArrayType,
            Type srcType,
            Class<?> declaringClass) {
        Type componentType = genericArrayType.getGenericComponentType();
        Type resolvedComponentType = null;

        // Recursively resolve the component type
        if (componentType instanceof TypeVariable) {
            resolvedComponentType = resolveTypeVar((TypeVariable<?>) componentType, srcType, declaringClass);
        } else if (componentType instanceof GenericArrayType) {
            resolvedComponentType = resolveGenericArrayType((GenericArrayType) componentType, srcType, declaringClass);
        } else if (componentType instanceof ParameterizedType) {
            resolvedComponentType = resolveParameterizedType(
                    (ParameterizedType) componentType,
                    srcType,
                    declaringClass);
        }

        // If component type is now concrete, return the actual array Class
        if (resolvedComponentType instanceof Class) {
            return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
        }
        // Otherwise, return a custom GenericArrayType implementation with the resolved component
        else {
            return new GenericArrayTypes(resolvedComponentType);
        }
    }

    /**
     * Resolves the type arguments of a {@link ParameterizedType}.
     *
     * @param parameterizedType The parameterized type (e.g., List<T>).
     * @param srcType           The source type context.
     * @param declaringClass    The class where the generic type was declared.
     * @return The resolved parameterized type with concrete arguments or a new {@code ParameterizedType}
     *         implementation.
     */
    private static ParameterizedType resolveParameterizedType(
            ParameterizedType parameterizedType,
            Type srcType,
            Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        Type[] args = new Type[typeArgs.length];

        // Iterate through all type arguments and resolve them recursively
        for (int i = 0; i < typeArgs.length; i++) {
            if (typeArgs[i] instanceof TypeVariable) {
                args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof ParameterizedType) {
                args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof WildcardType) {
                args[i] = resolveWildcardType((WildcardType) typeArgs[i], srcType, declaringClass);
            } else {
                args[i] = typeArgs[i]; // Concrete type
            }
        }
        // Return a custom ParameterizedType implementation with resolved arguments
        return new ParameterizedTypes(rawType, null, args);
    }

    /**
     * Resolves the bounds of a {@link WildcardType}.
     *
     * @param wildcardType   The wildcard type (e.g., ? extends Number).
     * @param srcType        The source type context.
     * @param declaringClass The class where the generic type was declared.
     * @return The resolved wildcard type with resolved bounds or a new {@code WildcardType} implementation.
     */
    private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
        // Resolve the lower bounds
        Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
        // Resolve the upper bounds
        Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
        // Return a custom WildcardType implementation with resolved bounds
        return new WildcardTypes(lowerBounds, upperBounds);
    }

    /**
     * Helper method to recursively resolve all types in an array of bounds (used by {@code resolveWildcardType}).
     *
     * @param bounds         The array of bounds.
     * @param srcType        The source type context.
     * @param declaringClass The class where the generic type was declared.
     * @return The array of resolved bounds.
     */
    private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass) {
        Type[] result = new Type[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] instanceof TypeVariable) {
                result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof ParameterizedType) {
                result[i] = resolveParameterizedType((ParameterizedType) bounds[i], srcType, declaringClass);
            } else if (bounds[i] instanceof WildcardType) {
                result[i] = resolveWildcardType((WildcardType) bounds[i], srcType, declaringClass);
            } else {
                result[i] = bounds[i];
            }
        }
        return result;
    }

    /**
     * Resolves a {@link TypeVariable} (e.g., 'T') to its actual concrete type by traversing the class/interface
     * hierarchy from the {@code srcType} up to the {@code declaringClass}.
     *
     * @param typeVar        The type variable to resolve.
     * @param srcType        The source type (the concrete class/interface providing the type argument).
     * @param declaringClass The class where the type variable was originally defined.
     * @return The resolved actual {@code Type}. Returns the first bound or {@code Object.class} if not found.
     * @throws IllegalArgumentException if {@code srcType} is neither a Class nor a ParameterizedType.
     */
    private static Type resolveTypeVar(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass) {
        Type result;
        Class<?> clazz;
        if (srcType instanceof Class) {
            clazz = (Class<?>) srcType;
        } else if (srcType instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) srcType).getRawType();
        } else {
            throw new IllegalArgumentException(
                    "The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
        }

        // Base case: If the current class is the declaring class, return the type variable's bound (or Object)
        if (clazz == declaringClass) {
            Type[] bounds = typeVar.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }

        // Recursively check superclass hierarchy
        Type superclass = clazz.getGenericSuperclass();
        result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
        if (result != null) {
            return result;
        }

        // Recursively check super-interface hierarchy
        Type[] superInterfaces = clazz.getGenericInterfaces();
        for (Type superInterface : superInterfaces) {
            result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
            if (result != null) {
                return result;
            }
        }
        // If not found, return Object.class (default bound)
        return Object.class;
    }

    /**
     * Scans a superclass or super-interface type to find the actual type argument corresponding to a
     * {@link TypeVariable}.
     *
     * @param typeVar        The type variable being searched for.
     * @param srcType        The original source type.
     * @param declaringClass The class where the type variable was declared.
     * @param clazz          The current class being scanned.
     * @param superclass     The superclass or interface type to analyze.
     * @return The resolved actual type if found, otherwise {@code null}.
     */
    private static Type scanSuperTypes(
            TypeVariable<?> typeVar,
            Type srcType,
            Class<?> declaringClass,
            Class<?> clazz,
            Type superclass) {
        if (superclass instanceof ParameterizedType parentAsType) {
            // Case 1: Supertype is parameterized (e.g., MyClass<String> extends Base<String>)
            Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
            TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters();

            // If the source type is also parameterized, translate the parent's type variables using the source's
            // arguments
            if (srcType instanceof ParameterizedType) {
                parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
            }

            // If the superclass is the declaring class, find the matching type argument
            if (declaringClass == parentAsClass) {
                for (int i = 0; i < parentTypeVars.length; i++) {
                    if (typeVar.equals(parentTypeVars[i])) {
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }

            // If the declaring class is further up the hierarchy, recurse
            if (declaringClass.isAssignableFrom(parentAsClass)) {
                return resolveTypeVar(typeVar, parentAsType, declaringClass);
            }
        } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
            // Case 2: Supertype is raw (e.g., MyClass<String> extends Base) and Base is in the hierarchy
            return resolveTypeVar(typeVar, superclass, declaringClass);
        }
        return null;
    }

    /**
     * Translates type variables in a parent's {@link ParameterizedType} using the concrete type arguments provided by
     * the child's {@link ParameterizedType}.
     *
     * @param srcType    The child's parameterized type (e.g., {@code Child<C>}).
     * @param srcClass   The child's raw class (e.g., {@code Child.class}).
     * @param parentType The parent's parameterized type (e.g., {@code Parent<T, K>}).
     * @return The translated parameterized type where parent type variables are replaced by child's concrete types.
     */
    private static ParameterizedType translateParentTypeVars(
            ParameterizedType srcType,
            Class<?> srcClass,
            ParameterizedType parentType) {
        Type[] parentTypeArgs = parentType.getActualTypeArguments();
        Type[] srcTypeArgs = srcType.getActualTypeArguments();
        TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters();
        Type[] newParentArgs = new Type[parentTypeArgs.length];
        boolean noChange = true;

        for (int i = 0; i < parentTypeArgs.length; i++) {
            if (parentTypeArgs[i] instanceof TypeVariable) {
                // If a parent's argument is a TypeVariable (e.g., T in Parent<T>),
                // find which child's TypeVariable it maps to and use the child's concrete argument
                for (int j = 0; j < srcTypeVars.length; j++) {
                    if (srcTypeVars[j].equals(parentTypeArgs[i])) {
                        noChange = false;
                        newParentArgs[i] = srcTypeArgs[j];
                    }
                }
            } else {
                newParentArgs[i] = parentTypeArgs[i];
            }
        }

        // Return the original type if no variables were translated, otherwise return the new ParameterizedType
        // implementation
        return noChange ? parentType : new ParameterizedTypes((Class<?>) parentType.getRawType(), null, newParentArgs);
    }

    /**
     * An implementation of the standard Java reflection {@link ParameterizedType} interface. This custom implementation
     * is used to hold resolved generic type arguments.
     */
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
         * Constructs a new ParameterizedTypes instance.
         *
         * @param rawType             The raw type.
         * @param ownerType           The owner type (can be null).
         * @param actualTypeArguments The actual type arguments.
         */
        public ParameterizedTypes(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
            super();
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        /**
         * Gets the actual type arguments.
         *
         * @return The array of actual type arguments.
         */
        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        /**
         * Gets the owner type.
         *
         * @return The owner type.
         */
        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        /**
         * Gets the raw type.
         *
         * @return The raw type.
         */
        @Override
        public Type getRawType() {
            return rawType;
        }

        /**
         * Returns a string representation of this parameterized type, mainly for debugging purposes.
         *
         * @return A string representation.
         */
        @Override
        public String toString() {
            return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments="
                    + Arrays.toString(actualTypeArguments) + "]";
        }
    }

    /**
     * An implementation of the standard Java reflection {@link WildcardType} interface. This custom implementation is
     * used to hold resolved wildcard bounds.
     */
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
         * Constructs a new WildcardTypes instance.
         *
         * @param lowerBounds The array of lower bounds.
         * @param upperBounds The array of upper bounds.
         */
        WildcardTypes(Type[] lowerBounds, Type[] upperBounds) {
            super();
            this.lowerBounds = lowerBounds;
            this.upperBounds = upperBounds;
        }

        /**
         * Gets the lower bounds.
         *
         * @return The array of lower bounds.
         */
        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }

        /**
         * Gets the upper bounds.
         *
         * @return The array of upper bounds.
         */
        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }

        // Omitted toString(), hashCode(), equals() for brevity, but should typically be included in production code
    }

    /**
     * An implementation of the standard Java reflection {@link GenericArrayType} interface. This custom implementation
     * is used to hold a resolved generic component type.
     */
    public static class GenericArrayTypes implements GenericArrayType {

        /**
         * The generic component type (e.g., {@code T} in {@code T[]}).
         */
        private final Type genericComponentType;

        /**
         * Constructs a new GenericArrayTypes instance.
         *
         * @param genericComponentType The generic component type.
         */
        GenericArrayTypes(Type genericComponentType) {
            super();
            this.genericComponentType = genericComponentType;
        }

        /**
         * Gets the generic component type.
         *
         * @return The generic component type.
         */
        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }

    }

}
