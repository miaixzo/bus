/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.reflect.ActualTypeMapperPool;
import org.miaixz.bus.core.lang.reflect.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;

/**
 * 针对 {@link Type} 的工具类封装
 * 最主要功能包括：
 *
 * <pre>
 * 1. 获取方法的参数和返回值类型（包括Type和Class）
 * 2. 获取泛型参数类型（包括对象的泛型参数或集合元素的泛型类型）
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TypeKit {

    /**
     * 常见的基础对象类型
     */
    private static final Class[] BASE_TYPE_CLASS = new Class[]{String.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class, Object.class, Class.class};

    /**
     * 是否为 map class 类型
     *
     * @param clazz 对象类型
     * @return 是否为 map class
     */
    public static boolean isMap(final Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * 是否为 数组 class 类型
     *
     * @param clazz 对象类型
     * @return 是否为 数组 class
     */
    public static boolean isArray(final Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * 是否为 Collection class 类型
     *
     * @param clazz 对象类型
     * @return 是否为 Collection class
     */
    public static boolean isCollection(final Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 是否为 Iterable class 类型
     *
     * @param clazz 对象类型
     * @return 是否为 数组 class
     */
    public static boolean isIterable(final Class<?> clazz) {
        return Iterable.class.isAssignableFrom(clazz);
    }

    /**
     * 是否为基本类型
     * 1. 8大基本类型
     * 2. 常见的值类型
     *
     * @param clazz 对象类型
     * @return 是否为基本类型
     */
    public static boolean isBase(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        for (Class baseClazz : BASE_TYPE_CLASS) {
            if (baseClazz.equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为抽象类
     *
     * @param clazz 类
     * @return 是否为抽象类
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是抽象类或者接口
     *
     * @param clazz 类信息
     * @return 是否
     */
    public static boolean isAbstractOrInterface(Class<?> clazz) {
        return isAbstract(clazz) || clazz.isInterface();
    }

    /**
     * 是否为标准的类
     * 这个类必须：
     *
     * <pre>
     * 0、不为 null
     * 1、非接口
     * 2、非抽象类
     * 3、非Enum枚举
     * 4、非数组
     * 5、非注解
     * 6、非原始类型(int, long等)
     * 7、非集合 Iterable
     * 8、非 Map.clas
     * 9、非 JVM 生成类
     * </pre>
     *
     * @param clazz 类
     * @return 是否为标准类
     */
    public static boolean isJavaBean(Class<?> clazz) {
        return null != clazz && !clazz.isInterface() && !isAbstract(clazz) && !clazz.isEnum() && !clazz.isArray() && !clazz.isAnnotation() && !clazz.isSynthetic() && !clazz.isPrimitive() && !isIterable(clazz) && !isMap(clazz);
    }

    /**
     * 判断一个类是JDK 自带的类型
     * jdk 自带的类,classLoader 是为空的
     *
     * @param clazz 类
     * @return 是否为 java 类
     */
    public static boolean isJdk(Class<?> clazz) {
        return null != clazz && null == clazz.getClassLoader();
    }

    /**
     * 检查subject类型是否可以按照Java泛型规则隐式转换为目标类型.
     * 如果这两种类型都是{@link Class}对象，
     * 则该方法返回{@link ClassKit#isAssignable(Class, Class)}的结果
     *
     * @param type   要分配给目标类型的主题类型
     * @param toType 目标类型
     * @return 如果{@code type}可赋值给{@code toType}，则{@code true}.
     */
    public static boolean isAssignable(final Type type, final Type toType) {
        return isAssignable(type, toType, null);
    }

    /**
     * 检查subject类型是否可以按照Java泛型规则隐式转换为目标类型
     *
     * @param type           要分配给目标类型的主题类型
     * @param toType         目标类型
     * @param typeVarAssigns 类型变量赋值的可选映射
     * @return 如果{@code type}可赋值给{@code toType}，则{@code true}.
     */
    private static boolean isAssignable(final Type type, final Type toType, final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (null == toType || toType instanceof Class<?>) {
            return isAssignable(type, (Class<?>) toType);
        }

        if (toType instanceof ParameterizedType) {
            return isAssignable(type, (ParameterizedType) toType, typeVarAssigns);
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
     * 检查subject类型是否可以按照Java泛型规则隐式转换为目标类
     *
     * @param type    要分配给目标类型的主题类型
     * @param toClass 目标类
     * @return 如果{@code type}可赋值给{@code toClass}，则{@code true}
     */
    private static boolean isAssignable(final Type type, final Class<?> toClass) {
        if (null == type) {
            return null == toClass || !toClass.isPrimitive();
        }

        // 只有一个null类型可以被赋值给null类型，
        // 而null类型会导致前一个返回true
        if (null == toClass) {
            return false;
        }

        // 所有类型都可以分配给自己
        if (toClass.equals(type)) {
            return true;
        }

        if (type instanceof Class<?>) {
            // 只是比较两个类
            return ClassKit.isAssignable((Class<?>) type, toClass);
        }

        if (type instanceof ParameterizedType) {
            // 只需将原始类型与类进行比较
            return isAssignable(getRawType((ParameterizedType) type), toClass);
        }

        // *
        if (type instanceof TypeVariable<?>) {
            // 如果任何边界都可以分配给类，那么类型也可以分配给类.
            for (final Type bound : ((TypeVariable<?>) type).getBounds()) {
                if (isAssignable(bound, toClass)) {
                    return true;
                }
            }

            return false;
        }

        // 可以为泛型数组类型分配的类只有类对象和数组类
        if (type instanceof GenericArrayType) {
            return toClass.equals(Object.class) || toClass.isArray() && isAssignable(((GenericArrayType) type).getGenericComponentType(), toClass.getComponentType());
        }

        if (type instanceof WildcardType) {
            return false;
        }

        throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * 检查主题类型是否可以按照Java泛型规则隐式转换为目标参数化类型
     *
     * @param type                要分配给目标类型的主题类型
     * @param toParameterizedType 目标参数化类型
     * @param typeVarAssigns      带有类型变量的映射
     * @return 如果{@code type}可分配给{@code toType}，则{@code true}
     */
    private static boolean isAssignable(final Type type, final ParameterizedType toParameterizedType, final Map<TypeVariable<?>, Type> typeVarAssigns) {
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
        final Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(toParameterizedType, toClass, typeVarAssigns);

        for (final TypeVariable<?> var : toTypeVarAssigns.keySet()) {
            final Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
            final Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);

            if (null == toTypeArg && fromTypeArg instanceof Class) {
                continue;
            }

            if (null != fromTypeArg && !toTypeArg.equals(fromTypeArg) && !(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg, typeVarAssigns))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 了解指定的类型是否表示数组类型
     *
     * @param type 要检查的类型
     * @return 如果{@code type}是数组类或{@link GenericArrayType}，则为{@code true}
     */
    public static boolean isArrayType(final Type type) {
        return type instanceof GenericArrayType || type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    /**
     * 是否未知类型
     * type为null或者{@link TypeVariable} 都视为未知类型
     *
     * @param type Type类型
     * @return 是否未知类型
     */
    public static boolean isUnknown(Type type) {
        return null == type || type instanceof TypeVariable;
    }

    /**
     * 获得Type对应的原始类
     *
     * @param type {@link Type}
     * @return 原始类，如果无法获取原始类，返回{@code null}
     */
    public static Class<?> getClass(final Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
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
            }
        }
        return null;
    }

    /**
     * 获取字段对应的Type类型
     * 方法优先获取GenericType，获取不到则获取Type
     *
     * @param field 字段
     * @return {@link Type}，可能为{@code null}
     */
    public static Type getType(final Field field) {
        if (null == field) {
            return null;
        }
        return field.getGenericType();
    }

    /**
     * 获得字段的泛型类型
     *
     * @param clazz     Bean类
     * @param fieldName 字段名
     * @return 字段的泛型类型
     */
    public static Type getFieldType(final Class<?> clazz, final String fieldName) {
        return getType(FieldKit.getField(clazz, fieldName));
    }

    /**
     * 获得Field对应的原始类
     *
     * @param field {@link Field}
     * @return 原始类，如果无法获取原始类，返回{@code null}
     */
    public static Class<?> getClass(final Field field) {
        return null == field ? null : field.getType();
    }

    /**
     * 获取方法的第一个参数类型
     * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
     *
     * @param method 方法
     * @return {@link Type}，可能为{@code null}
     */
    public static Type getFirstParamType(final Method method) {
        return getParamType(method, 0);
    }

    /**
     * 获取方法的第一个参数类
     *
     * @param method 方法
     * @return 第一个参数类型，可能为{@code null}
     */
    public static Class<?> getFirstParamClass(final Method method) {
        return getParamClass(method, 0);
    }

    /**
     * 获取方法的参数类型
     * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
     *
     * @param method 方法
     * @param index  第几个参数的索引，从0开始计数
     * @return {@link Type}，可能为{@code null}
     */
    public static Type getParamType(final Method method, final int index) {
        final Type[] types = getParamTypes(method);
        if (null != types && types.length > index) {
            return types[index];
        }
        return null;
    }

    /**
     * 获取方法的参数类
     *
     * @param method 方法
     * @param index  第几个参数的索引，从0开始计数
     * @return 参数类，可能为{@code null}
     */
    public static Class<?> getParamClass(final Method method, final int index) {
        final Class<?>[] classes = getParamClasses(method);
        if (null != classes && classes.length > index) {
            return classes[index];
        }
        return null;
    }

    /**
     * 获取方法的参数类型列表
     * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
     *
     * @param method 方法
     * @return {@link Type}列表，可能为{@code null}
     * @see Method#getGenericParameterTypes()
     * @see Method#getParameterTypes()
     */
    public static Type[] getParamTypes(final Method method) {
        return null == method ? null : method.getGenericParameterTypes();
    }

    /**
     * 解析方法的参数类型列表
     * 依赖jre\lib\rt.jar
     *
     * @param method t方法
     * @return 参数类型类列表
     * @see Method#getGenericParameterTypes
     * @see Method#getParameterTypes
     */
    public static Class<?>[] getParamClasses(final Method method) {
        return null == method ? null : method.getParameterTypes();
    }

    /**
     * 获取方法的返回值类型
     * 获取方法的GenericReturnType
     *
     * @param method 方法
     * @return {@link Type}，可能为{@code null}
     * @see Method#getGenericReturnType()
     * @see Method#getReturnType()
     */
    public static Type getReturnType(final Method method) {
        return null == method ? null : method.getGenericReturnType();
    }

    /**
     * 解析方法的返回类型类列表
     *
     * @param method 方法
     * @return 返回值类型的类
     * @see Method#getGenericReturnType
     * @see Method#getReturnType
     */
    public static Class<?> getReturnClass(final Method method) {
        return null == method ? null : method.getReturnType();
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param type 被检查的类型，必须是已经确定泛型类型的类型
     * @return {@link Type}，可能为{@code null}
     */
    public static Type getTypeArgument(final Type type) {
        return getTypeArgument(type, 0);
    }

    /**
     * 获得给定类的泛型参数
     *
     * @param type  被检查的类型，必须是已经确定泛型类型的类
     * @param index 泛型类型的索引号，即第几个泛型类型
     * @return {@link Type}
     */
    public static Type getTypeArgument(final Type type, final int index) {
        final Type[] typeArguments = getTypeArguments(type);
        if (null != typeArguments && typeArguments.length > index) {
            return typeArguments[index];
        }
        return null;
    }

    /**
     * 获得指定类型中所有泛型参数类型，例如：
     * <pre>
     * class A&lt;T&gt;
     * class B extends A&lt;String&gt;
     * </pre>
     * 通过此方法，传入B.class即可得到String
     *
     * @param type 指定类型
     * @return 所有泛型参数类型
     */
    public static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        }

        final ParameterizedType parameterizedType = toParameterizedType(type);
        return (null == parameterizedType) ? null : parameterizedType.getActualTypeArguments();
    }

    /**
     * 检索此参数化类型的所有类型参数，包括所有者层次结构参数
     *
     * @param type 指定要从中获取参数的主题参数化类型
     * @return 带有类型参数的{@code Map}.
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final ParameterizedType type) {
        return getTypeArguments(type, getRawType(type), null);
    }

    /**
     * 获取基于子类型的类/接口的类型参数
     *
     * @param type    用于确定{@code toClass}的类型参数的类型
     * @param toClass 类型参数将根据子类型{@code type}确定的类
     * @return 带有类型参数的{@code Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final Type type, final Class<?> toClass) {
        return getTypeArguments(type, toClass, null);
    }

    /**
     * 在{@code toClass}的上下文中返回{@code type}的类型参数的映射
     *
     * @param type              问题类型
     * @param toClass           类
     * @param subtypeVarAssigns 带有类型变量的映射
     * @return 带有类型参数的{@code Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final Type type, final Class<?> toClass, final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (type instanceof Class<?>) {
            return getTypeArguments((Class<?>) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof ParameterizedType) {
            return getTypeArguments((ParameterizedType) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof GenericArrayType) {
            return getTypeArguments(((GenericArrayType) type).getGenericComponentType(), toClass.isArray() ? toClass.getComponentType() : toClass, subtypeVarAssigns);
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
     * 在{@code toClass}的上下文中返回类的类型参数的映射
     *
     * @param cls               要确定类型参数的类
     * @param toClass           上下文类
     * @param subtypeVarAssigns 带有类型变量的映射
     * @return 带有类型参数的{@code Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls, final Class<?> toClass, final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (!isAssignable(cls, toClass)) {
            return null;
        }

        if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
                return new HashMap<>();
            }
            cls = ClassKit.primitiveToWrapper(cls);
        }

        final HashMap<TypeVariable<?>, Type> typeVarAssigns = null == subtypeVarAssigns ? new HashMap<>() : new HashMap<>(subtypeVarAssigns);

        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }

        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * 在{@code toClass}的上下文中返回参数化类型的类型参数的映射
     *
     * @param parameterizedType 参数化类型
     * @param toClass           类
     * @param subtypeVarAssigns 带有类型变量的映射
     * @return 带有类型参数的{@code Map}
     */
    public static Map<TypeVariable<?>, Type> getTypeArguments(final ParameterizedType parameterizedType, final Class<?> toClass, final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        final Class<?> cls = getRawType(parameterizedType);

        if (!isAssignable(cls, toClass)) {
            return null;
        }

        final Type ownerType = parameterizedType.getOwnerType();
        Map<TypeVariable<?>, Type> typeVarAssigns;

        if (ownerType instanceof ParameterizedType) {
            final ParameterizedType parameterizedOwnerType = (ParameterizedType) ownerType;
            typeVarAssigns = getTypeArguments(parameterizedOwnerType, getRawType(parameterizedOwnerType), subtypeVarAssigns);
        } else {
            typeVarAssigns = null == subtypeVarAssigns ? new HashMap<>() : new HashMap<>(subtypeVarAssigns);
        }

        final Type[] typeArgs = parameterizedType.getActualTypeArguments();
        final TypeVariable<?>[] typeParams = cls.getTypeParameters();

        for (int i = 0; i < typeParams.length; i++) {
            final Type typeArg = typeArgs[i];
            typeVarAssigns.put(typeParams[i], typeVarAssigns.containsKey(typeArg) ? typeVarAssigns.get(typeArg) : typeArg);
        }

        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }

        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * 获取与{@code superClass} 指定的超类最接近的父类
     *
     * @param cls        类
     * @param superClass 超类
     * @return 父类型
     */
    public static Type getClosestParentType(final Class<?> cls, final Class<?> superClass) {
        if (superClass.isInterface()) {
            final Type[] interfaceTypes = cls.getGenericInterfaces();
            Type genericInterface = null;

            for (final Type midType : interfaceTypes) {
                Class<?> midClass;

                if (midType instanceof ParameterizedType) {
                    midClass = getRawType((ParameterizedType) midType);
                } else if (midType instanceof Class<?>) {
                    midClass = (Class<?>) midType;
                } else {
                    throw new IllegalStateException("Unexpected generic" + " interface type found: " + midType);
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
     * 将{@link Type} 转换为{@link ParameterizedType}
     * {@link ParameterizedType}用于获取当前类或父类中泛型参数化后的类型
     * 一般用于获取泛型参数具体的参数类型，例如：
     * <pre>
     * class A&lt;T&gt;
     * class B extends A&lt;String&gt;
     * </pre>
     * 通过此方法，传入B.class即可得到B{@link ParameterizedType}，从而获取到String
     *
     * @param type {@link Type}
     * @return {@link ParameterizedType}
     */
    public static ParameterizedType toParameterizedType(final Type type) {
        return toParameterizedType(type, 0);
    }

    /**
     * 将{@link Type} 转换为{@link ParameterizedType}
     * {@link ParameterizedType}用于获取当前类或父类中泛型参数化后的类型
     * 一般用于获取泛型参数具体的参数类型，例如：
     *
     * <pre>{@code
     *   class A<T>
     *   class B extends A<String>;
     * }</pre>
     * 通过此方法，传入B.class即可得到B对应的{@link ParameterizedType}，从而获取到String
     *
     * @param type           {@link Type}
     * @param interfaceIndex 实现的第几个接口
     * @return {@link ParameterizedType}
     */
    public static ParameterizedType toParameterizedType(final Type type, final int interfaceIndex) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        }

        if (type instanceof Class) {
            final ParameterizedType[] generics = getGenerics((Class<?>) type);
            if (generics.length > interfaceIndex) {
                return generics[interfaceIndex];
            }
        }

        return null;
    }

    /**
     * 获取指定类所有泛型父类和泛型接口
     * <ul>
     *     <li>指定类及其所有的泛型父类</li>
     *     <li>指定类实现的直接泛型接口</li>
     * </ul>
     *
     * @param clazz 类
     * @return 泛型父类或接口数组
     */
    public static ParameterizedType[] getGenerics(final Class<?> clazz) {
        final List<ParameterizedType> result = ListKit.of(false);
        // 泛型父类（父类及祖类优先级高）
        final Type genericSuper = clazz.getGenericSuperclass();
        if (null != genericSuper && !Object.class.equals(genericSuper)) {
            final ParameterizedType parameterizedType = toParameterizedType(genericSuper);
            if (null != parameterizedType) {
                result.add(parameterizedType);
            }
        }

        // 泛型接口
        final Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (ArrayKit.isNotEmpty(genericInterfaces)) {
            for (final Type genericInterface : genericInterfaces) {
                final ParameterizedType parameterizedType = toParameterizedType(genericInterface);
                if (null != parameterizedType) {
                    result.add(parameterizedType);
                }
            }
        }
        return result.toArray(new ParameterizedType[0]);
    }

    /**
     * 指定泛型数组中是否含有泛型变量
     *
     * @param types 泛型数组
     * @return 是否含有泛型变量
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
     * 获取泛型变量和泛型实际类型的对应关系Map
     * 例如：
     * <pre>
     *     E    java.lang.Integer
     * </pre>
     *
     * @param clazz 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public static Map<Type, Type> getTypeMap(final Class<?> clazz) {
        return ActualTypeMapperPool.get(clazz);
    }

    /**
     * 获得泛型字段对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     *
     * @param type  实际类型明确的类
     * @param field 字段
     * @return 实际类型，可能为Class等
     */
    public static Type getActualType(final Type type, final Field field) {
        if (null == field) {
            return null;
        }
        return getActualType(ObjectKit.defaultIfNull(type, field.getDeclaringClass()), field.getGenericType());
    }

    /**
     * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     * 此方法可以处理：
     *
     * <pre>
     *     1. 泛型化对象，类似于Map&lt;User, Key&lt;Long&gt;&gt;
     *     2. 泛型变量，类似于T
     * </pre>
     *
     * @param type         类
     * @param typeVariable 泛型变量，例如T等
     * @return 实际类型，可能为Class等
     */
    public static Type getActualType(final Type type, final Type typeVariable) {
        if (typeVariable instanceof ParameterizedType) {
            return getActualType(type, (ParameterizedType) typeVariable);
        }

        if (typeVariable instanceof TypeVariable) {
            return ActualTypeMapperPool.getActualType(type, (TypeVariable<?>) typeVariable);
        }

        // 没有需要替换的泛型变量，原样输出
        return typeVariable;
    }

    /**
     * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     * 此方法可以处理复杂的泛型化对象，类似于Map&lt;User, Key&lt;Long&gt;&gt;
     *
     * @param type              类
     * @param parameterizedType 泛型变量，例如List&lt;T&gt;等
     * @return 实际类型，可能为Class等
     */
    public static Type getActualType(final Type type, ParameterizedType parameterizedType) {
        // 字段类型为泛型参数类型，解析对应泛型类型为真实类型，类似于List<T> a
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        // 泛型对象中含有未被转换的泛型变量
        if (TypeKit.hasTypeVariable(actualTypeArguments)) {
            actualTypeArguments = getActualTypes(type, parameterizedType.getActualTypeArguments());
            if (ArrayKit.isNotEmpty(actualTypeArguments)) {
                // 替换泛型变量为实际类型，例如List<T>变为List<String>
                parameterizedType = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
            }
        }

        return parameterizedType;
    }

    /**
     * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     *
     * @param type          类
     * @param typeVariables 泛型变量数组，例如T等
     * @return 实际类型数组，可能为Class等
     */
    public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
        return ActualTypeMapperPool.getActualTypes(type, typeVariables);
    }

    /**
     * 如果{@link TypeVariable#getBounds()}返回一个空数组,
     * 则返回一个包含{@link Object} 唯一类型的数组.
     * 否则返回{@link TypeVariable#getBounds()}
     * 传递给{@link #normalizeUpperBounds}的结果
     *
     * @param typeVariable 类型变量
     * @return 包含类型变量边界的非空数组.
     */
    public static Type[] getImplicitBounds(final TypeVariable<?> typeVariable) {
        Assert.notNull(typeVariable, "typeVariable is null");
        final Type[] bounds = typeVariable.getBounds();

        return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
    }

    /**
     * 如果{@link WildcardType#getUpperBounds()}返回一个空数组，
     * 则返回一个包含{@link Object}唯一值的数组否则，
     * 它将返回传递给{@link #normalizeUpperBounds}的
     * {@link WildcardType#getUpperBounds()}的结果
     *
     * @param wildcardType 通配符类型
     * @return 包含通配符类型下界的非空数组.
     */
    public static Type[] getImplicitUpperBounds(final WildcardType wildcardType) {
        Assert.notNull(wildcardType, "wildcardType is null");
        final Type[] bounds = wildcardType.getUpperBounds();
        return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
    }

    /**
     * 该方法在类型变量类型和通配符类型中去除冗余的上界类型
     *
     * @param bounds 表示{@link WildcardType}或
     *               {@link TypeVariable}的上界的类型数组.
     * @return 包含来自{@code bounds}的值减去冗余类型的数组.
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
     * 将传入的类型转换为{@link Class}对象方便的类型检查方法.
     *
     * @param parameterizedType 要转换的类型
     * @return 对应的{@code Class}对象
     */
    private static Class<?> getRawType(final ParameterizedType parameterizedType) {
        final Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?>)) {
            throw new IllegalStateException("Wait... What!? Type of rawType: " + rawType);
        }
        return (Class<?>) rawType;
    }

    /**
     * 在{@code typeVarAssigns}中查找{@code var}
     *
     * @param var            要查找的类型变量
     * @param typeVarAssigns 用于查找的map
     * @return 如果某个变量不在映射中，则返回{@code null}
     */
    private static Type unrollVariableAssignments(TypeVariable<?> var, final Map<TypeVariable<?>, Type> typeVarAssigns) {
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
