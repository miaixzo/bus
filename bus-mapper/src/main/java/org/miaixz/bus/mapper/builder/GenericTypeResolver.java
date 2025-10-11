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

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cursor.Cursor;
import org.miaixz.bus.core.lang.Optional;

/**
 * A utility class for resolving generic types, based on the source code of MyBatis 3. It adds the
 * {@code resolveMapperTypes} method to support resolving generic types of interfaces. Original source from
 * https://github.com/mybatis/mybatis-3
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericTypeResolver {

    /**
     * Private constructor to prevent instantiation.
     */
    public GenericTypeResolver() {

    }

    /**
     * Gets the actual return type of a method.
     *
     * @param method  The method.
     * @param srcType The class where the method is declared.
     * @return The actual return type of the method.
     */
    public static Class<?> getReturnType(Method method, Class<?> srcType) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = resolveReturnType(method, srcType);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType parameterizedType) {
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter)
                                .getGenericComponentType();
                        // support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0];
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    /**
     * Resolves the generic types of a mapper interface.
     *
     * @param srcType The interface type.
     * @return An array of actual type arguments for the generic parameters of the interface.
     */
    public static Type[] resolveMapperTypes(Class<?> srcType) {
        Type[] types = srcType.getGenericInterfaces();
        List<Type> result = new ArrayList<>();
        for (Type type : types) {
            if (type instanceof Class) {
                result.addAll(Arrays.asList(resolveMapperTypes((Class<?>) type)));
            } else if (type instanceof ParameterizedType) {
                Collections.addAll(result, ((ParameterizedType) type).getActualTypeArguments());
            }
        }
        return result.toArray(new Type[] {});
    }

    /**
     * Resolves the generic types of the interface where a method is declared.
     *
     * @param method  The method.
     * @param srcType The interface type.
     * @return An array of actual type arguments for the generic parameters of the interface.
     */
    public static Type[] resolveMapperTypes(Method method, Type srcType) {
        Class<?> declaringClass = method.getDeclaringClass();
        TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
        Type[] result = new Type[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            result[i] = resolveType(typeParameters[i], srcType, declaringClass);
        }
        return result;
    }

    /**
     * Resolves the generic type of a field.
     *
     * @param field   The field.
     * @param srcType The source type.
     * @return The actual type of the field.
     */
    public static Type resolveFieldType(Field field, Type srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        return resolveType(fieldType, srcType, declaringClass);
    }

    /**
     * Resolves the actual class of a field's type.
     *
     * @param field   The field.
     * @param srcType The source type.
     * @return The actual class of the field's type.
     */
    public static Class<?> resolveFieldClass(Field field, Type srcType) {
        Type fieldType = field.getGenericType();
        Class<?> declaringClass = field.getDeclaringClass();
        Type type = resolveType(fieldType, srcType, declaringClass);
        return resolveTypeToClass(type);
    }

    /**
     * Converts a {@link Type} to its corresponding {@link Class}.
     *
     * @param type The type to convert.
     * @return The corresponding class.
     */
    public static Class<?> resolveTypeToClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable<?>) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return (Class<?>) bounds[0];
        } else if (type instanceof GenericArrayType) {
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
     * Resolves the return type of a method.
     *
     * @param method  The method.
     * @param srcType The source type.
     * @return The actual return type of the method.
     */
    public static Type resolveReturnType(Method method, Type srcType) {
        Type returnType = method.getGenericReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        return resolveType(returnType, srcType, declaringClass);
    }

    /**
     * Resolves the parameter types of a method.
     *
     * @param method  The method.
     * @param srcType The source type.
     * @return An array of actual parameter types for the method.
     */
    public static Type[] resolveParamTypes(Method method, Type srcType) {
        Type[] paramTypes = method.getGenericParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Type[] result = new Type[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            result[i] = resolveType(paramTypes[i], srcType, declaringClass);
        }
        return result;
    }

    /**
     * Resolves a generic type.
     *
     * @param type           The type to resolve.
     * @param srcType        The source type.
     * @param declaringClass The declaring class.
     * @return The resolved actual type.
     */
    public static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
        if (type instanceof TypeVariable) {
            return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
        } else if (type instanceof ParameterizedType) {
            return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
        } else if (type instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
        } else {
            return type;
        }
    }

    /**
     * Resolves a generic array type.
     *
     * @param genericArrayType The generic array type.
     * @param srcType          The source type.
     * @param declaringClass   The declaring class.
     * @return The resolved actual type.
     */
    private static Type resolveGenericArrayType(
            GenericArrayType genericArrayType,
            Type srcType,
            Class<?> declaringClass) {
        Type componentType = genericArrayType.getGenericComponentType();
        Type resolvedComponentType = null;
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
        if (resolvedComponentType instanceof Class) {
            return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
        } else {
            return new GenericArrayTypes(resolvedComponentType);
        }
    }

    /**
     * Resolves a parameterized type.
     *
     * @param parameterizedType The parameterized type.
     * @param srcType           The source type.
     * @param declaringClass    The declaring class.
     * @return The resolved parameterized type.
     */
    private static ParameterizedType resolveParameterizedType(
            ParameterizedType parameterizedType,
            Type srcType,
            Class<?> declaringClass) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type[] typeArgs = parameterizedType.getActualTypeArguments();
        Type[] args = new Type[typeArgs.length];
        for (int i = 0; i < typeArgs.length; i++) {
            if (typeArgs[i] instanceof TypeVariable) {
                args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof ParameterizedType) {
                args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], srcType, declaringClass);
            } else if (typeArgs[i] instanceof WildcardType) {
                args[i] = resolveWildcardType((WildcardType) typeArgs[i], srcType, declaringClass);
            } else {
                args[i] = typeArgs[i];
            }
        }
        return new ParameterizedTypes(rawType, null, args);
    }

    /**
     * Resolves a wildcard type.
     *
     * @param wildcardType   The wildcard type.
     * @param srcType        The source type.
     * @param declaringClass The declaring class.
     * @return The resolved wildcard type.
     */
    private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
        Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
        Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
        return new WildcardTypes(lowerBounds, upperBounds);
    }

    /**
     * Resolves the bounds of a wildcard type.
     *
     * @param bounds         The array of bounds.
     * @param srcType        The source type.
     * @param declaringClass The declaring class.
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
     * Resolves a type variable.
     *
     * @param typeVar        The type variable.
     * @param srcType        The source type.
     * @param declaringClass The declaring class.
     * @return The resolved actual type.
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

        if (clazz == declaringClass) {
            Type[] bounds = typeVar.getBounds();
            if (bounds.length > 0) {
                return bounds[0];
            }
            return Object.class;
        }

        Type superclass = clazz.getGenericSuperclass();
        result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
        if (result != null) {
            return result;
        }

        Type[] superInterfaces = clazz.getGenericInterfaces();
        for (Type superInterface : superInterfaces) {
            result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
            if (result != null) {
                return result;
            }
        }
        return Object.class;
    }

    /**
     * Scans superclasses and interfaces to resolve a type variable.
     *
     * @param typeVar        The type variable.
     * @param srcType        The source type.
     * @param declaringClass The declaring class.
     * @param clazz          The current class.
     * @param superclass     The superclass or interface type.
     * @return The resolved actual type.
     */
    private static Type scanSuperTypes(
            TypeVariable<?> typeVar,
            Type srcType,
            Class<?> declaringClass,
            Class<?> clazz,
            Type superclass) {
        if (superclass instanceof ParameterizedType parentAsType) {
            Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
            TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters();
            if (srcType instanceof ParameterizedType) {
                parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
            }
            if (declaringClass == parentAsClass) {
                for (int i = 0; i < parentTypeVars.length; i++) {
                    if (typeVar.equals(parentTypeVars[i])) {
                        return parentAsType.getActualTypeArguments()[i];
                    }
                }
            }
            if (declaringClass.isAssignableFrom(parentAsClass)) {
                return resolveTypeVar(typeVar, parentAsType, declaringClass);
            }
        } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
            return resolveTypeVar(typeVar, superclass, declaringClass);
        }
        return null;
    }

    /**
     * Translates type variables from a parent type.
     *
     * @param srcType    The source parameterized type.
     * @param srcClass   The source class.
     * @param parentType The parent parameterized type.
     * @return The translated parameterized type.
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
        return noChange ? parentType : new ParameterizedTypes((Class<?>) parentType.getRawType(), null, newParentArgs);
    }

    /**
     * An implementation of {@link ParameterizedType}.
     */
    public static class ParameterizedTypes implements ParameterizedType {

        /**
         * The raw type.
         */
        private final Class<?> rawType;

        /**
         * The owner type.
         */
        private final Type ownerType;

        /**
         * The actual type arguments.
         */
        private final Type[] actualTypeArguments;

        /**
         * Constructs a new ParameterizedTypes instance.
         *
         * @param rawType             The raw type.
         * @param ownerType           The owner type.
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
         * Returns a string representation of this parameterized type.
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
     * An implementation of {@link WildcardType}.
     */
    public static class WildcardTypes implements WildcardType {

        /**
         * The lower bounds.
         */
        private final Type[] lowerBounds;

        /**
         * The upper bounds.
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
    }

    /**
     * An implementation of {@link GenericArrayType}.
     */
    public static class GenericArrayTypes implements GenericArrayType {

        /**
         * The generic component type.
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
