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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.*;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.reflect.ActualTypeMapper;
import org.miaixz.bus.core.lang.reflect.ParameterizedType;
import org.miaixz.bus.core.lang.reflect.TypeReference;

/**
 * Utility class for {@link Type}. Main functions include:
 * 
 * <pre>
 * 1. Getting parameter and return types of methods.
 * 2. Getting generic parameter types.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TypeKit {

    /**
     * Common base object types.
     */
    private static final Class<?>[] BASE_TYPE_CLASS = new Class[] { String.class, Boolean.class, Character.class,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class, Object.class,
            Class.class };

    /**
     * Checks if a class is a Map type.
     *
     * @param clazz The class.
     * @return `true` if it is a Map.
     */
    public static boolean isMap(final Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is an array type.
     *
     * @param clazz The class.
     * @return `true` if it is an array.
     */
    public static boolean isArray(final Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * Checks if a class is a Collection type.
     *
     * @param clazz The class.
     * @return `true` if it is a Collection.
     */
    public static boolean isCollection(final Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is an Iterable type.
     *
     * @param clazz The class.
     * @return `true` if it is an Iterable.
     */
    public static boolean isIterable(final Class<?> clazz) {
        return Iterable.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is a base type (primitive or common value type).
     *
     * @param clazz The class.
     * @return `true` if it is a base type.
     */
    public static boolean isBase(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        for (Class<?> baseClazz : BASE_TYPE_CLASS) {
            if (baseClazz.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class is abstract.
     *
     * @param clazz The class.
     * @return `true` if it is abstract.
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Checks if a class is abstract or an interface.
     *
     * @param clazz The class.
     * @return `true` if it is abstract or an interface.
     */
    public static boolean isAbstractOrInterface(Class<?> clazz) {
        return isAbstract(clazz) || clazz.isInterface();
    }

    /**
     * Checks if a class is a standard JavaBean.
     *
     * @param clazz The class.
     * @return `true` if it is a standard JavaBean.
     */
    public static boolean isJavaBean(Class<?> clazz) {
        return null != clazz && !clazz.isInterface() && !isAbstract(clazz) && !clazz.isEnum() && !clazz.isArray()
                && !clazz.isAnnotation() && !clazz.isSynthetic() && !clazz.isPrimitive() && !isIterable(clazz)
                && !isMap(clazz);
    }

    /**
     * Checks if a class is a built-in JDK type.
     *
     * @param clazz The class.
     * @return `true` if it is a JDK class.
     */
    public static boolean isJdk(Class<?> clazz) {
        return null != clazz && null == clazz.getClassLoader();
    }

    /**
     * Checks if a subject type can be implicitly converted to a target type according to Java generics rules.
     *
     * @param type   The subject type.
     * @param toType The target type.
     * @return `true` if `type` is assignable to `toType`.
     */
    public static boolean isAssignable(final Type type, final Type toType) {
        return isAssignable(type, toType, null);
    }

    /**
     * Checks if a subject type can be implicitly converted to a target type.
     *
     * @param type           The subject type.
     * @param toType         The target type.
     * @param typeVarAssigns An optional map of type variable assignments.
     * @return `true` if assignable.
     */
    private static boolean isAssignable(
            final Type type,
            final Type toType,
            final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (null == toType || toType instanceof Class<?>) {
            return isAssignable(type, (Class<?>) toType);
        }
        if (toType instanceof java.lang.reflect.ParameterizedType) {
            return isAssignable(type, (java.lang.reflect.ParameterizedType) toType, typeVarAssigns);
        }
        if (toType instanceof GenericArrayType) {
            return isAssignable(type, (GenericArrayType) toType, typeVarAssigns);
        }
        if (toType instanceof WildcardType) {
            return isAssignable(type, (WildcardType) toType, typeVarAssigns);
        }
        if (toType instanceof TypeVariable<?>) {
            return isAssignable(type, (TypeVariable<?>) toType, typeVarAssigns);
        }
        throw new IllegalStateException("found an unhandled type: " + toType);
    }

    /**
     * Checks if a subject type can be implicitly converted to a target class.
     *
     * @param type    The subject type.
     * @param toClass The target class.
     * @return `true` if assignable.
     */
    private static boolean isAssignable(final Type type, final Class<?> toClass) {
        if (null == type) {
            return null == toClass || !toClass.isPrimitive();
        }
        if (null == toClass) {
            return false;
        }
        if (toClass.equals(type)) {
            return true;
        }
        if (type instanceof Class<?>) {
            return ClassKit.isAssignable((Class<?>) type, toClass);
        }
        if (type instanceof java.lang.reflect.ParameterizedType) {
            return isAssignable(getRawType((java.lang.reflect.ParameterizedType) type), toClass);
        }
        if (type instanceof TypeVariable<?>) {
            for (final Type bound : ((TypeVariable<?>) type).getBounds()) {
                if (isAssignable(bound, toClass)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return toClass.equals(Object.class) || toClass.isArray()
                    && isAssignable(((GenericArrayType) type).getGenericComponentType(), toClass.getComponentType());
        }
        if (type instanceof WildcardType) {
            return false;
        }
        throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * Checks if a subject type can be implicitly converted to a target parameterized type.
     *
     * @param type                The subject type.
     * @param toParameterizedType The target parameterized type.
     * @param typeVarAssigns      A map of type variable assignments.
     * @return `true` if assignable.
     */
    private static boolean isAssignable(
            final Type type,
            final java.lang.reflect.ParameterizedType toParameterizedType,
            final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (null == type) {
            return true;
        }
        if (null == toParameterizedType) {
            return false;
        }
        if (toParameterizedType.equals(type)) {
            return true;
        }

        final Class<?> toClass = getRawType(toParameterizedType);
        final Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments(type, toClass, null);

        if (null == fromTypeVarAssigns) {
            return false;
        }
        if (fromTypeVarAssigns.isEmpty()) {
            return true;
        }
        final Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(
                toParameterizedType,
                toClass,
                typeVarAssigns);

        for (final TypeVariable<?> var : toTypeVarAssigns.keySet()) {
            final Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
            final Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);

            if (null == toTypeArg && fromTypeArg instanceof Class) {
                continue;
            }

            if (null != fromTypeArg && !toTypeArg.equals(fromTypeArg)
                    && !(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg, typeVarAssigns))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a type represents an array type.
     *
     * @param type The type to check.
     * @return `true` if it's an array class or `GenericArrayType`.
     */
    public static boolean isArrayType(final Type type) {
        return type instanceof GenericArrayType || type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    /**
     * Checks if a type is unknown (`null` or `TypeVariable`).
     *
     * @param type The type.
     * @return `true` if the type is unknown.
     */
    public static boolean isUnknown(Type type) {
        return null == type || type instanceof TypeVariable;
    }

    /**
     * Gets the raw class from a `Type`.
     *
     * @param type The `Type`.
     * @return The raw class, or `null`.
     */
    public static Class<?> getClass(final Type type) {
        if (null == type) {
            return null;
        }

        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof java.lang.reflect.ParameterizedType) {
            return (Class<?>) ((java.lang.reflect.ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            final Type[] bounds = ((TypeVariable<?>) type).getBounds();
            if (bounds.length == 1) {
                return getClass(bounds[0]);
            }
        } else if (type instanceof WildcardType) {
            final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(getClass(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeReference) {
            return getClass(((TypeReference<?>) type).getType());
        }

        throw new IllegalArgumentException("Unsupported Type: " + type.getClass().getName());
    }

    /**
     * Gets the generic type of a field.
     *
     * @param field The field.
     * @return The {@link Type}, or `null`.
     */
    public static Type getType(final Field field) {
        if (null == field) {
            return null;
        }
        return field.getGenericType();
    }

    /**
     * Gets the generic type of a field by name.
     *
     * @param clazz     The class.
     * @param fieldName The field name.
     * @return The field's generic type.
     */
    public static Type getFieldType(final Class<?> clazz, final String fieldName) {
        return getType(FieldKit.getField(clazz, fieldName));
    }

    /**
     * Gets the raw class of a field.
     *
     * @param field The `Field`.
     * @return The raw class.
     */
    public static Class<?> getClass(final Field field) {
        return null == field ? null : field.getType();
    }

    /**
     * Gets the first parameter's generic type for a method.
     *
     * @param method The method.
     * @return The {@link Type}, or `null`.
     */
    public static Type getFirstParamType(final Method method) {
        return getParamType(method, 0);
    }

    /**
     * Gets the first parameter's class for a method.
     *
     * @param method The method.
     * @return The first parameter's class.
     */
    public static Class<?> getFirstParamClass(final Method method) {
        return getParamClass(method, 0);
    }

    /**
     * Gets a parameter's generic type by index for a method.
     *
     * @param method The method.
     * @param index  The parameter index.
     * @return The {@link Type}, or `null`.
     */
    public static Type getParamType(final Method method, final int index) {
        final Type[] types = getParamTypes(method);
        if (null != types && types.length > index) {
            return types[index];
        }
        return null;
    }

    /**
     * Gets a parameter's class by index for a method.
     *
     * @param method The method.
     * @param index  The parameter index.
     * @return The parameter's class.
     */
    public static Class<?> getParamClass(final Method method, final int index) {
        final Class<?>[] classes = getParamClasses(method);
        if (null != classes && classes.length > index) {
            return classes[index];
        }
        return null;
    }

    /**
     * Gets the generic parameter types for a method.
     *
     * @param method The method.
     * @return An array of {@link Type}s.
     */
    public static Type[] getParamTypes(final Method method) {
        return null == method ? null : method.getGenericParameterTypes();
    }

    /**
     * Gets the parameter classes for a method.
     *
     * @param method The method.
     * @return An array of parameter classes.
     */
    public static Class<?>[] getParamClasses(final Method method) {
        return null == method ? null : method.getParameterTypes();
    }

    /**
     * Gets the generic return type of a method.
     *
     * @param method The method.
     * @return The return {@link Type}.
     */
    public static Type getReturnType(final Method method) {
        return null == method ? null : method.getGenericReturnType();
    }

    /**
     * Gets the return class of a method.
     *
     * @param method The method.
     * @return The return class.
     */
    public static Class<?> getReturnClass(final Method method) {
        return null == method ? null : method.getReturnType();
    }

    /**
     * Gets the first generic type argument of a given type.
     *
     * @param type The type to inspect.
     * @return The first generic argument as a {@link Type}.
     */
    public static Type getTypeArgument(final Type type) {
        return getTypeArgument(type, 0);
    }

    /**
     * Gets the generic type argument of a given type at a specific index.
     *
     * @param type  The type to inspect.
     * @param index The index of the generic argument.
     * @return The generic argument as a {@link Type}.
     */
    public static Type getTypeArgument(final Type type, final int index) {
        final Type[] typeArguments = getTypeArguments(type);
        if (null != typeArguments && typeArguments.length > index) {
            return typeArguments[index];
        }
        return null;
    }

    /**
     * Gets all generic type arguments of a given type.
     *
     * @param type The type.
     * @return An array of all generic type arguments.
     */
    public static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        }
        final java.lang.reflect.ParameterizedType parameterizedType = toParameterizedType(type);
        return (null == parameterizedType) ? null : parameterizedType.getActualTypeArguments();
    }

    /**
     * Retrieves all type arguments of this parameterized type, including owner hierarchy arguments.
     *
     * @param type The parameterized type.
     * @return A map of type variables to their assigned types.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final java.lang.reflect.ParameterizedType type) {
        return getTypeArguments(type, getRawType(type), null);
    }

    /**
     * Gets the type arguments of a class/interface based on a subtype.
     *
     * @param type    The subtype.
     * @param toClass The class whose type arguments are to be determined.
     * @return A map of type variables to their assigned types.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final Type type, final Class<?> toClass) {
        return getTypeArguments(type, toClass, null);
    }

    /**
     * Returns a map of the type arguments of `type` in the context of `toClass`.
     *
     * @param type              The type in question.
     * @param toClass           The context class.
     * @param subtypeVarAssigns A map of type variable assignments.
     * @return A map of type parameters.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(
            final Type type,
            final Class<?> toClass,
            final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (type instanceof Class<?>) {
            return getTypeArguments((Class<?>) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof java.lang.reflect.ParameterizedType) {
            return getTypeArguments((java.lang.reflect.ParameterizedType) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof GenericArrayType) {
            return getTypeArguments(
                    ((GenericArrayType) type).getGenericComponentType(),
                    toClass.isArray() ? toClass.getComponentType() : toClass,
                    subtypeVarAssigns);
        }
        if (type instanceof WildcardType) {
            for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
                if (isAssignable(bound, toClass)) {
                    return getTypeArguments(bound, toClass, subtypeVarAssigns);
                }
            }
            return null;
        }

        if (type instanceof TypeVariable<?>) {
            for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
                if (isAssignable(bound, toClass)) {
                    return getTypeArguments(bound, toClass, subtypeVarAssigns);
                }
            }
            return null;
        }
        throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * Returns a map of the type arguments of a class in the context of `toClass`.
     *
     * @param cls               The class whose type arguments are to be determined.
     * @param toClass           The context class.
     * @param subtypeVarAssigns A map of type variable assignments.
     * @return A map of type parameters.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(
            Class<?> cls,
            final Class<?> toClass,
            final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (!isAssignable(cls, toClass)) {
            return null;
        }
        if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
                return new HashMap<>();
            }
            cls = ClassKit.primitiveToWrapper(cls);
        }
        final HashMap<TypeVariable<?>, Type> typeVarAssigns = null == subtypeVarAssigns ? new HashMap<>()
                : new HashMap<>(subtypeVarAssigns);
        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }
        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * Returns a map of the type arguments of a parameterized type in the context of `toClass`.
     *
     * @param parameterizedType The parameterized type.
     * @param toClass           The context class.
     * @param subtypeVarAssigns A map of type variable assignments.
     * @return A map of type parameters.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(
            final java.lang.reflect.ParameterizedType parameterizedType,
            final Class<?> toClass,
            final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        final Class<?> cls = getRawType(parameterizedType);
        if (!isAssignable(cls, toClass)) {
            return null;
        }
        final Type ownerType = parameterizedType.getOwnerType();
        Map<TypeVariable<?>, Type> typeVarAssigns;
        if (ownerType instanceof java.lang.reflect.ParameterizedType) {
            final java.lang.reflect.ParameterizedType parameterizedOwnerType = (java.lang.reflect.ParameterizedType) ownerType;
            typeVarAssigns = getTypeArguments(
                    parameterizedOwnerType,
                    getRawType(parameterizedOwnerType),
                    subtypeVarAssigns);
        } else {
            typeVarAssigns = null == subtypeVarAssigns ? new HashMap<>() : new HashMap<>(subtypeVarAssigns);
        }
        final Type[] typeArgs = parameterizedType.getActualTypeArguments();
        final TypeVariable<?>[] typeParams = cls.getTypeParameters();
        for (int i = 0; i < typeParams.length; i++) {
            final Type typeArg = typeArgs[i];
            typeVarAssigns
                    .put(typeParams[i], typeVarAssigns.containsKey(typeArg) ? typeVarAssigns.get(typeArg) : typeArg);
        }
        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }
        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * Gets the closest parent type to `superClass`.
     *
     * @param cls        The class.
     * @param superClass The superclass.
     * @return The parent type.
     */
    public static Type getClosestParentType(final Class<?> cls, final Class<?> superClass) {
        if (superClass.isInterface()) {
            final Type[] interfaceTypes = cls.getGenericInterfaces();
            Type genericInterface = null;
            for (final Type midType : interfaceTypes) {
                Class<?> midClass;
                if (midType instanceof java.lang.reflect.ParameterizedType) {
                    midClass = getRawType((java.lang.reflect.ParameterizedType) midType);
                } else if (midType instanceof Class<?>) {
                    midClass = (Class<?>) midType;
                } else {
                    throw new IllegalStateException("Unexpected generic interface type found: " + midType);
                }
                if (isAssignable(midClass, superClass) && isAssignable(genericInterface, (Type) midClass)) {
                    genericInterface = midType;
                }
            }
            if (null != genericInterface) {
                return genericInterface;
            }
        }
        return cls.getGenericSuperclass();
    }

    /**
     * Converts a {@link Type} to a {@link java.lang.reflect.ParameterizedType}.
     *
     * @param type The `Type`.
     * @return The `ParameterizedType`.
     */
    public static java.lang.reflect.ParameterizedType toParameterizedType(final Type type) {
        return toParameterizedType(type, 0);
    }

    /**
     * Converts a {@link Type} to a {@link java.lang.reflect.ParameterizedType}.
     *
     * @param type           The `Type`.
     * @param interfaceIndex The index of the implemented interface.
     * @return The `ParameterizedType`.
     */
    public static java.lang.reflect.ParameterizedType toParameterizedType(Type type, final int interfaceIndex) {
        if (type instanceof TypeReference) {
            type = ((TypeReference<?>) type).getType();
        }
        if (type instanceof java.lang.reflect.ParameterizedType) {
            return (java.lang.reflect.ParameterizedType) type;
        }
        if (type instanceof Class) {
            final java.lang.reflect.ParameterizedType[] generics = getGenerics((Class<?>) type);
            if (generics.length > interfaceIndex) {
                return generics[interfaceIndex];
            }
        }
        return null;
    }

    /**
     * Gets all generic superclasses and interfaces for a class.
     *
     * @param clazz The class.
     * @return An array of `ParameterizedType`.
     */
    public static java.lang.reflect.ParameterizedType[] getGenerics(final Class<?> clazz) {
        final List<java.lang.reflect.ParameterizedType> result = ListKit.of(false);
        final Type genericSuper = clazz.getGenericSuperclass();
        if (null != genericSuper && !Object.class.equals(genericSuper)) {
            final java.lang.reflect.ParameterizedType parameterizedType = toParameterizedType(genericSuper);
            if (null != parameterizedType) {
                result.add(parameterizedType);
            }
        }
        final Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (ArrayKit.isNotEmpty(genericInterfaces)) {
            for (final Type genericInterface : genericInterfaces) {
                final java.lang.reflect.ParameterizedType parameterizedType = toParameterizedType(genericInterface);
                if (null != parameterizedType) {
                    result.add(parameterizedType);
                }
            }
        }
        return result.toArray(new java.lang.reflect.ParameterizedType[0]);
    }

    /**
     * Checks if an array of types contains a `TypeVariable`.
     *
     * @param types The array of types.
     * @return `true` if it contains a `TypeVariable`.
     */
    public static boolean hasTypeVariable(final Type... types) {
        for (final Type type : types) {
            if (type instanceof TypeVariable) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a map of generic type variables to their actual types for a class.
     *
     * @param clazz The class to analyze.
     * @return A map of generic type assignments.
     */
    public static Map<Type, Type> getTypeMap(final Class<?> clazz) {
        return ActualTypeMapper.get(clazz);
    }

    /**
     * Gets the actual generic type of a field.
     *
     * @param type  The containing type.
     * @param field The field.
     * @return The actual type.
     */
    public static Type getActualType(final Type type, final Field field) {
        if (null == field) {
            return null;
        }
        return getActualType(ObjectKit.defaultIfNull(type, field.getDeclaringClass()), field.getGenericType());
    }

    /**
     * Gets the actual type corresponding to a generic type variable.
     *
     * @param type         The containing type.
     * @param typeVariable The type variable.
     * @return The actual type.
     */
    public static Type getActualType(final Type type, final Type typeVariable) {
        if (typeVariable instanceof java.lang.reflect.ParameterizedType) {
            return getActualType(type, (java.lang.reflect.ParameterizedType) typeVariable);
        }
        if (typeVariable instanceof TypeVariable) {
            return getActualType(type, (TypeVariable<?>) typeVariable);
        }
        if (typeVariable instanceof GenericArrayType) {
            return ActualTypeMapper.getActualType(type, (GenericArrayType) typeVariable);
        }
        return typeVariable;
    }

    /**
     * Gets the actual type corresponding to a generic type variable.
     *
     * @param type         The containing type.
     * @param typeVariable The type variable.
     * @return The actual type.
     */
    public static Type getActualType(final Type type, final TypeVariable<?> typeVariable) {
        return ObjectKit.defaultIfNull(ActualTypeMapper.getActualType(type, typeVariable), typeVariable);
    }

    /**
     * Gets the actual types for a parameterized type.
     *
     * @param type              The containing type.
     * @param parameterizedType The parameterized type.
     * @return The actual type.
     */
    public static Type getActualType(final Type type, java.lang.reflect.ParameterizedType parameterizedType) {
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (TypeKit.hasTypeVariable(actualTypeArguments)) {
            actualTypeArguments = getActualTypes(type, parameterizedType.getActualTypeArguments());
            if (ArrayKit.isNotEmpty(actualTypeArguments)) {
                parameterizedType = new ParameterizedType(actualTypeArguments, parameterizedType.getOwnerType(),
                        parameterizedType.getRawType());
            }
        }
        return parameterizedType;
    }

    /**
     * Gets the actual types for an array of type variables.
     *
     * @param type          The containing type.
     * @param typeVariables The array of type variables.
     * @return An array of actual types.
     */
    public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
        return ActualTypeMapper.getActualTypes(type, typeVariables);
    }

    /**
     * Gets the implicit bounds of a type variable.
     *
     * @param typeVariable The type variable.
     * @return An array of bound types.
     */
    public static Type[] getImplicitBounds(final TypeVariable<?> typeVariable) {
        Assert.notNull(typeVariable, "typeVariable is null");
        final Type[] bounds = typeVariable.getBounds();
        return bounds.length == 0 ? new Type[] { Object.class } : normalizeUpperBounds(bounds);
    }

    /**
     * Gets the implicit upper bounds of a wildcard type.
     *
     * @param wildcardType The wildcard type.
     * @return An array of upper bound types.
     */
    public static Type[] getImplicitUpperBounds(final WildcardType wildcardType) {
        Assert.notNull(wildcardType, "wildcardType is null");
        final Type[] bounds = wildcardType.getUpperBounds();
        return bounds.length == 0 ? new Type[] { Object.class } : normalizeUpperBounds(bounds);
    }

    /**
     * Normalizes an array of upper bounds by removing redundant types.
     *
     * @param bounds The array of bound types.
     * @return The normalized array of bounds.
     */
    public static Type[] normalizeUpperBounds(final Type[] bounds) {
        Assert.notNull(bounds, "null value specified for bounds array");
        if (bounds.length < 2) {
            return bounds;
        }
        final Set<Type> types = new HashSet<>(bounds.length);
        for (final Type type1 : bounds) {
            boolean subtypeFound = false;
            for (final Type type2 : bounds) {
                if (type1 != type2 && isAssignable(type2, type1, null)) {
                    subtypeFound = true;
                    break;
                }
            }
            if (!subtypeFound) {
                types.add(type1);
            }
        }
        return types.toArray(new Type[types.size()]);
    }

    /**
     * Gets the raw type from a `ParameterizedType`.
     *
     * @param parameterizedType The parameterized type.
     * @return The raw `Class`.
     */
    private static Class<?> getRawType(final java.lang.reflect.ParameterizedType parameterizedType) {
        final Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>)) {
            throw new IllegalStateException("Wait... What!? Type of rawType: " + rawType);
        }
        return (Class<?>) rawType;
    }

    /**
     * Unrolls variable assignments to find the ultimate type.
     *
     * @param var            The type variable.
     * @param typeVarAssigns The map of assignments.
     * @return The resolved type.
     */
    private static Type unrollVariableAssignments(
            TypeVariable<?> var,
            final Map<TypeVariable<?>, Type> typeVarAssigns) {
        Type result;
        do {
            result = typeVarAssigns.get(var);
            if (result instanceof TypeVariable<?> && !result.equals(var)) {
                var = (TypeVariable<?>) result;
                continue;
            }
            break;
        } while (true);
        return result;
    }

}
