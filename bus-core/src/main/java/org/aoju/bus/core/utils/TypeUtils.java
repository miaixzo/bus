/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.builder.Builder;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.map.TableMap;

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
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class TypeUtils {

    /**
     * 常见的基础对象类型
     */
    private static final Class[] BASE_TYPE_CLASS = new Class[]{
            String.class, Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class, Object.class, Class.class
    };

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
        return isAbstract(clazz)
                || clazz.isInterface();
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
     * 6、非原始类型（int, long等）
     * 7、非集合 Iterable
     * 8、非 Map.clas
     * 9、非 JVM 生成类
     * </pre>
     *
     * @param clazz 类
     * @return 是否为标准类
     */
    public static boolean isJavaBean(Class<?> clazz) {
        return null != clazz
                && !clazz.isInterface()
                && !isAbstract(clazz)
                && !clazz.isEnum()
                && !clazz.isArray()
                && !clazz.isAnnotation()
                && !clazz.isSynthetic()
                && !clazz.isPrimitive()
                && !isIterable(clazz)
                && !isMap(clazz);
    }

    /**
     * 判断一个类是JDK 自带的类型
     * jdk 自带的类,classLoader 是为空的
     *
     * @param clazz 类
     * @return 是否为 java 类
     */
    public static boolean isJdk(Class<?> clazz) {
        return clazz != null && clazz.getClassLoader() == null;
    }

    /**
     * 判断是否为Bean对象
     * 判定方法是是否存在只有一个参数的setXXX方法
     *
     * @param clazz 待测试类
     * @return 是否为Bean对象
     */
    public static boolean isBean(Class<?> clazz) {
        if (isJavaBean(clazz)) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1 && method.getName().startsWith("set")) {
                    // 检测包含标准的setXXX方法即视为标准的JavaBean
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获得Type对应的原始类
     *
     * @param type {@link Type}
     * @return 原始类, 如果无法获取原始类, 返回{@code null}
     */
    public static Class<?> getClass(Type type) {
        if (null != type) {
            if (type instanceof Class) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            } else if (type instanceof TypeVariable) {
                return (Class<?>) ((TypeVariable<?>) type).getBounds()[0];
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
     * 获取方法的第一个参数类型<br>
     * 优先获取方法的GenericParameterTypes,如果获取不到,则获取ParameterTypes
     *
     * @param method 方法
     * @return {@link Type},可能为{@code null}
     * @since 3.1.2
     */
    public static Type getFirstParamType(Method method) {
        return getParamType(method, 0);
    }

    /**
     * 获取方法的第一个参数类
     *
     * @param method 方法
     * @return 第一个参数类型, 可能为{@code null}
     * @since 3.1.2
     */
    public static Class<?> getFirstParamClass(Method method) {
        return getParamClass(method, 0);
    }

    /**
     * 获取方法的参数类型<br>
     * 优先获取方法的GenericParameterTypes,如果获取不到,则获取ParameterTypes
     *
     * @param method 方法
     * @param index  第几个参数的索引,从0开始计数
     * @return {@link Type},可能为{@code null}
     */
    public static Type getParamType(Method method, int index) {
        Type[] types = getParamTypes(method);
        if (null != types && types.length > index) {
            return types[index];
        }
        return null;
    }

    /**
     * 获取方法的参数类
     *
     * @param method 方法
     * @param index  第几个参数的索引,从0开始计数
     * @return 参数类, 可能为{@code null}
     * @since 3.1.2
     */
    public static Class<?> getParamClass(Method method, int index) {
        Class<?>[] classes = getParamClasses(method);
        if (null != classes && classes.length > index) {
            return classes[index];
        }
        return null;
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param type 被检查的类型,必须是已经确定泛型类型的类型
     * @return {@link Type},可能为{@code null}
     */
    public static Type getTypeArgument(Type type) {
        return getTypeArgument(type, 0);
    }

    /**
     * 获得给定类的泛型参数
     *
     * @param type  被检查的类型,必须是已经确定泛型类型的类
     * @param index 泛型类型的索引号,既第几个泛型类型
     * @return {@link Type}
     */
    public static Type getTypeArgument(Type type, int index) {
        final Type[] typeArguments = getTypeArguments(type);
        if (null != typeArguments && typeArguments.length > index) {
            return typeArguments[index];
        }
        return null;
    }


    /**
     * 将{@link Type} 转换为{@link ParameterizedType}<br>
     * {@link ParameterizedType}用于获取当前类或父类中泛型参数化后的类型<br>
     * 一般用于获取泛型参数具体的参数类型,例如：
     *
     * <pre>
     * class A&lt;T&gt;
     * class B extends A&lt;String&gt;
     * </pre>
     * <p>
     * 通过此方法,传入B.class即可得到B{@link ParameterizedType},从而获取到String
     *
     * @param type {@link Type}
     * @return {@link ParameterizedType}
     */
    public static ParameterizedType toParameterizedType(Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        } else if (type instanceof Class) {
            return toParameterizedType(((Class<?>) type).getGenericSuperclass());
        }
        return null;
    }

    /**
     * 获取指定泛型变量对应的真实类型<br>
     * 由于子类中泛型参数实现和父类（接口）中泛型定义位置是一一对应的,因此可以通过对应关系找到泛型实现类型<br>
     * 使用此方法注意：
     *
     * <pre>
     * 1. superClass必须是clazz的父类或者clazz实现的接口
     * 2. typeVariable必须在superClass中声明
     * </pre>
     *
     * @param actualType      真实类型所在类,此类中记录了泛型参数对应的实际类型
     * @param typeDefineClass 泛型变量声明所在类或接口,此类中定义了泛型类型
     * @param typeVariables   泛型变量,需要的实际类型对应的泛型参数
     * @return 给定泛型参数对应的实际类型, 如果无对应类型, 返回null
     */
    public static Type[] getActualTypes(Type actualType, Class<?> typeDefineClass, Type... typeVariables) {
        if (false == typeDefineClass.isAssignableFrom(getClass(actualType))) {
            throw new IllegalArgumentException("Parameter [superClass] must be assignable from [clazz]");
        }

        // 泛型参数标识符列表
        final TypeVariable<?>[] typeVars = typeDefineClass.getTypeParameters();
        if (ArrayUtils.isEmpty(typeVars)) {
            return null;
        }
        // 实际类型列表
        final Type[] actualTypeArguments = getTypeArguments(actualType);
        if (ArrayUtils.isEmpty(actualTypeArguments)) {
            return null;
        }

        int size = Math.min(actualTypeArguments.length, typeVars.length);
        final Map<TypeVariable<?>, Type> tableMap = new TableMap<>(typeVars, actualTypeArguments);

        // 查找方法定义所在类或接口中此泛型参数的位置
        final Type[] result = new Type[size];
        for (int i = 0; i < typeVariables.length; i++) {
            result[i] = (typeVariables[i] instanceof TypeVariable) ? tableMap.get(typeVariables[i]) : typeVariables[i];
        }
        return result;
    }

    /**
     * 获取指定泛型变量对应的真实类型<br>
     * 由于子类中泛型参数实现和父类（接口）中泛型定义位置是一一对应的,因此可以通过对应关系找到泛型实现类型<br>
     * 使用此方法注意：
     *
     * <pre>
     * 1. superClass必须是clazz的父类或者clazz实现的接口
     * 2. typeVariable必须在superClass中声明
     * </pre>
     *
     * @param actualType      真实类型所在类,此类中记录了泛型参数对应的实际类型
     * @param typeDefineClass 泛型变量声明所在类或接口,此类中定义了泛型类型
     * @param typeVariable    泛型变量,需要的实际类型对应的泛型参数
     * @return 给定泛型参数对应的实际类型
     */
    public static Type getActualType(Type actualType, Class<?> typeDefineClass, Type typeVariable) {
        Type[] types = getActualTypes(actualType, typeDefineClass, typeVariable);
        if (ArrayUtils.isNotEmpty(types)) {
            return types[0];
        }
        return null;
    }

    /**
     * 是否未知类型<br>
     * type为null或者{@link TypeVariable} 都视为未知类型
     *
     * @param type Type类型
     * @return 是否未知类型
     */
    public static boolean isUnknow(Type type) {
        return null == type || type instanceof TypeVariable;
    }

    /**
     * 指定泛型数组中是否含有泛型变量
     *
     * @param types 泛型数组
     * @return 是否含有泛型变量
     */
    public static boolean hasTypeVeriable(Type... types) {
        for (Type type : types) {
            if (type instanceof TypeVariable) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取列表字段对应的类型
     *
     * @param field 字段
     * @return 返回对应的 class 类型
     */
    public static Class getListType(Field field) {
        ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
        Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
        return (Class) listActualTypeArguments[0];
    }

    /**
     * 是否为通配符泛型
     *
     * @param type 类型
     * @return 是否
     */
    public static boolean isWildcardGenericType(final Type type) {
        final Class clazz = type.getClass();
        return WildcardTypeImpl.class.equals(clazz);
    }

    /**
     * 是否为列表
     *
     * @param clazz 类型
     * @return 结果
     */
    public static boolean isList(final Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    /**
     * 是否为 set
     *
     * @param clazz 类型
     * @return 结果
     */
    public static boolean isSet(final Class clazz) {
        return Set.class.isAssignableFrom(clazz);
    }

    /**
     * 获取字段对应的Type类型
     * 方法优先获取GenericType,获取不到则获取Type
     *
     * @param field 字段
     * @return {@link Type},可能为{@code null}
     */
    public static Type getType(Field field) {
        if (null == field) {
            return null;
        }
        Type type = field.getGenericType();
        if (null == type) {
            type = field.getType();
        }
        return type;
    }

    /**
     * 获得Field对应的原始类
     *
     * @param field {@link Field}
     * @return 原始类, 如果无法获取原始类, 返回{@code null}
     * @since 3.1.9
     */
    public static Class<?> getClass(Field field) {
        return null == field ? null : field.getType();
    }

    /**
     * 获取方法的参数类型列表
     * 优先获取方法的GenericParameterTypes,如果获取不到,则获取ParameterTypes
     *
     * @param method 方法
     * @return {@link Type}列表,可能为{@code null}
     * @see Method#getGenericParameterTypes()
     * @see Method#getParameterTypes()
     */
    public static Type[] getParamTypes(Method method) {
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
     * @since 3.1.9
     */
    public static Class<?>[] getParamClasses(Method method) {
        return null == method ? null : method.getParameterTypes();
    }

    /**
     * 获取方法的返回值类型
     * 获取方法的GenericReturnType
     *
     * @param method 方法
     * @return {@link Type},可能为{@code null}
     * @see Method#getGenericReturnType()
     * @see Method#getReturnType()
     */
    public static Type getReturnType(Method method) {
        return null == method ? null : method.getGenericReturnType();
    }

    /**
     * 解析方法的返回类型类列表
     *
     * @param method 方法
     * @return 返回值类型的类
     * @see Method#getGenericReturnType
     * @see Method#getReturnType
     * @since 3.1.9
     */
    public static Class<?> getReturnClass(Method method) {
        return null == method ? null : method.getReturnType();
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param clazz 被检查的类,必须是已经确定泛型类型的类
     * @return {@link Type},可能为{@code null}
     */
    public static Type getTypeArgument(Class<?> clazz) {
        return getTypeArgument(clazz, 0);
    }

    /**
     * 获得给定类的泛型参数
     *
     * @param clazz 被检查的类,必须是已经确定泛型类型的类
     * @param index 泛型类型的索引号,既第几个泛型类型
     * @return {@link Type}
     */
    public static Type getTypeArgument(Class<?> clazz, int index) {
        Type type = clazz;
        if (false == (type instanceof ParameterizedType)) {
            type = clazz.getGenericSuperclass();
        }
        return getTypeArgument(type, index);
    }

    /**
     * 获得指定类型中所有泛型参数类型
     *
     * @param type 指定类型
     * @return 所有泛型参数类型
     */
    public static Type[] getTypeArguments(Type type) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType genericSuperclass = (ParameterizedType) type;
            return genericSuperclass.getActualTypeArguments();
        }
        return null;
    }

    /**
     * 检查subject类型是否可以按照Java泛型规则隐式转换为目标类型.
     * 如果这两种类型都是{@link Class}对象，
     * 则该方法返回{@link ClassUtils#isAssignable(Class, Class)}的结果
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
    private static boolean isAssignable(final Type type, final Type toType,
                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (toType == null || toType instanceof Class<?>) {
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
        if (type == null) {
            // 与ClassUtils.isAssignable()行为的一致性
            return toClass == null || !toClass.isPrimitive();
        }

        // 只有一个null类型可以被赋值给null类型，
        // 而null类型会导致前一个返回true
        if (toClass == null) {
            return false;
        }

        // 所有类型都可以分配给自己
        if (toClass.equals(type)) {
            return true;
        }

        if (type instanceof Class<?>) {
            // 只是比较两个类
            return ClassUtils.isAssignable((Class<?>) type, toClass);
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
            return toClass.equals(Object.class)
                    || toClass.isArray()
                    && isAssignable(((GenericArrayType) type).getGenericComponentType(), toClass
                    .getComponentType());
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
    private static boolean isAssignable(final Type type, final ParameterizedType toParameterizedType,
                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type == null) {
            return true;
        }
        if (toParameterizedType == null) {
            return false;
        }
        if (toParameterizedType.equals(type)) {
            return true;
        }

        final Class<?> toClass = getRawType(toParameterizedType);
        final Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments(type, toClass, null);

        if (fromTypeVarAssigns == null) {
            return false;
        }
        if (fromTypeVarAssigns.isEmpty()) {
            return true;
        }
        final Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(toParameterizedType,
                toClass, typeVarAssigns);

        for (final TypeVariable<?> var : toTypeVarAssigns.keySet()) {
            final Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
            final Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);

            if (toTypeArg == null && fromTypeArg instanceof Class) {
                continue;
            }

            if (fromTypeArg != null
                    && !toTypeArg.equals(fromTypeArg)
                    && !(toTypeArg instanceof WildcardType && isAssignable(fromTypeArg, toTypeArg,
                    typeVarAssigns))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在{@code typeVarAssigns}中查找{@code var}
     *
     * @param var            要查找的类型变量
     * @param typeVarAssigns 用于查找的map
     * @return 如果某个变量不在映射中，则返回{@code null}
     * @since 3.2.0
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

    /**
     * 检查类型是否可以按照Java泛型规则隐式转换为目标通配符类型.
     *
     * @param type           要分配给目标类型的主题类型
     * @param typeVarAssigns 带有类型变量的映射
     * @return 如果{@code type}可分配给{@code toGenericArrayType}，则{@code true}.
     */
    private static boolean isAssignable(final Type type, final GenericArrayType toGenericArrayType,
                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type == null) {
            return true;
        }
        if (toGenericArrayType == null) {
            return false;
        }
        if (toGenericArrayType.equals(type)) {
            return true;
        }

        final Type toComponentType = toGenericArrayType.getGenericComponentType();

        if (type instanceof Class<?>) {
            final Class<?> cls = (Class<?>) type;
            return cls.isArray()
                    && isAssignable(cls.getComponentType(), toComponentType, typeVarAssigns);
        }

        if (type instanceof GenericArrayType) {
            return isAssignable(((GenericArrayType) type).getGenericComponentType(),
                    toComponentType, typeVarAssigns);
        }

        if (type instanceof WildcardType) {
            for (final Type bound : getImplicitUpperBounds((WildcardType) type)) {
                if (isAssignable(bound, toGenericArrayType)) {
                    return true;
                }
            }
            return false;
        }

        if (type instanceof TypeVariable<?>) {
            for (final Type bound : getImplicitBounds((TypeVariable<?>) type)) {
                if (isAssignable(bound, toGenericArrayType)) {
                    return true;
                }
            }
            return false;
        }

        if (type instanceof ParameterizedType) {
            return false;
        }

        throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * 检查类型是否可以按照Java泛型规则隐式转换为目标通配符类型.
     *
     * @param type           要分配给目标类型的主题类型
     * @param toWildcardType 目标通配符类型
     * @param typeVarAssigns 带有类型变量的映射
     * @return 如果{@code type}可分配给{@code toWildcardType}，则{@code true}.
     */
    private static boolean isAssignable(final Type type, final WildcardType toWildcardType,
                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type == null) {
            return true;
        }
        if (toWildcardType == null) {
            return false;
        }
        if (toWildcardType.equals(type)) {
            return true;
        }

        final Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
        final Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);

        if (type instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) type;
            final Type[] upperBounds = getImplicitUpperBounds(wildcardType);
            final Type[] lowerBounds = getImplicitLowerBounds(wildcardType);

            for (Type toBound : toUpperBounds) {
                toBound = substituteTypeVariables(toBound, typeVarAssigns);
                for (final Type bound : upperBounds) {
                    if (!isAssignable(bound, toBound, typeVarAssigns)) {
                        return false;
                    }
                }
            }

            for (Type toBound : toLowerBounds) {
                toBound = substituteTypeVariables(toBound, typeVarAssigns);
                for (final Type bound : lowerBounds) {
                    if (!isAssignable(toBound, bound, typeVarAssigns)) {
                        return false;
                    }
                }
            }
            return true;
        }

        for (final Type toBound : toUpperBounds) {
            if (!isAssignable(type, substituteTypeVariables(toBound, typeVarAssigns),
                    typeVarAssigns)) {
                return false;
            }
        }

        for (final Type toBound : toLowerBounds) {
            if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns), type,
                    typeVarAssigns)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查主题类型是否可以按照Java泛型规则隐式转换为目标类型变量
     *
     * @param type           要分配给目标类型的主题类型
     * @param toTypeVariable 目标类型变量
     * @param typeVarAssigns 带有类型变量的映射
     * @return 如果{@code type}可赋值给{@code toTypeVariable}，则{@code true}.
     */
    private static boolean isAssignable(final Type type, final TypeVariable<?> toTypeVariable,
                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type == null) {
            return true;
        }
        if (toTypeVariable == null) {
            return false;
        }
        if (toTypeVariable.equals(type)) {
            return true;
        }

        if (type instanceof TypeVariable<?>) {
            final Type[] bounds = getImplicitBounds((TypeVariable<?>) type);

            for (final Type bound : bounds) {
                if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
                    return true;
                }
            }
        }

        if (type instanceof Class<?> || type instanceof ParameterizedType
                || type instanceof GenericArrayType || type instanceof WildcardType) {
            return false;
        }

        throw new IllegalStateException("found an unhandled type: " + type);
    }

    /**
     * 在{@code typevarassignments}中查找{@code type}的映射
     *
     * @param type           要替换的类型
     * @param typeVarAssigns 带有类型变量的映射
     * @return 取代类型
     */
    private static Type substituteTypeVariables(final Type type, final Map<TypeVariable<?>, Type> typeVarAssigns) {
        if (type instanceof TypeVariable<?> && typeVarAssigns != null) {
            final Type replacementType = typeVarAssigns.get(type);

            if (replacementType == null) {
                throw new IllegalArgumentException("missing assignment type for type variable "
                        + type);
            }
            return replacementType;
        }
        return type;
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
    private static Map<TypeVariable<?>, Type> getTypeArguments(final Type type, final Class<?> toClass,
                                                               final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (type instanceof Class<?>) {
            return getTypeArguments((Class<?>) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof ParameterizedType) {
            return getTypeArguments((ParameterizedType) type, toClass, subtypeVarAssigns);
        }
        if (type instanceof GenericArrayType) {
            return getTypeArguments(((GenericArrayType) type).getGenericComponentType(), toClass
                    .isArray() ? toClass.getComponentType() : toClass, subtypeVarAssigns);
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
     * 在{@code toClass}的上下文中返回参数化类型的类型参数的映射
     *
     * @param parameterizedType 参数化类型
     * @param toClass           类
     * @param subtypeVarAssigns 带有类型变量的映射
     * @return 带有类型参数的{@code Map}
     */
    private static Map<TypeVariable<?>, Type> getTypeArguments(
            final ParameterizedType parameterizedType, final Class<?> toClass,
            final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        final Class<?> cls = getRawType(parameterizedType);

        if (!isAssignable(cls, toClass)) {
            return null;
        }

        final Type ownerType = parameterizedType.getOwnerType();
        Map<TypeVariable<?>, Type> typeVarAssigns;

        if (ownerType instanceof ParameterizedType) {
            final ParameterizedType parameterizedOwnerType = (ParameterizedType) ownerType;
            typeVarAssigns = getTypeArguments(parameterizedOwnerType,
                    getRawType(parameterizedOwnerType), subtypeVarAssigns);
        } else {
            typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>()
                    : new HashMap<>(subtypeVarAssigns);
        }

        final Type[] typeArgs = parameterizedType.getActualTypeArguments();
        final TypeVariable<?>[] typeParams = cls.getTypeParameters();

        for (int i = 0; i < typeParams.length; i++) {
            final Type typeArg = typeArgs[i];
            typeVarAssigns.put(typeParams[i], typeVarAssigns.containsKey(typeArg) ? typeVarAssigns
                    .get(typeArg) : typeArg);
        }

        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }

        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * 在{@code toClass}的上下文中返回类的类型参数的映射
     *
     * @param cls               要确定类型参数的类
     * @param toClass           上下文类
     * @param subtypeVarAssigns 带有类型变量的映射
     * @return 带有类型参数的{@code Map}
     */
    private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls, final Class<?> toClass,
                                                               final Map<TypeVariable<?>, Type> subtypeVarAssigns) {
        if (!isAssignable(cls, toClass)) {
            return null;
        }

        if (cls.isPrimitive()) {
            if (toClass.isPrimitive()) {
                return new HashMap<>();
            }
            cls = ClassUtils.primitiveToWrapper(cls);
        }

        final HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>()
                : new HashMap<>(subtypeVarAssigns);

        if (toClass.equals(cls)) {
            return typeVarAssigns;
        }

        return getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
    }

    /**
     * 尝试基于超参数化类型的类型参数确定类/接口的类型参数
     *
     * @param cls       要确定类型参数的类
     * @param superType 要从中确定{@code cls}的类型参数的超类型
     * @return 类型赋值的{@code Map}，可以确定继承层次结构中
     * 从{@code type}到{@code toClass}的每个类型中的类型变量.
     */
    public static Map<TypeVariable<?>, Type> determineTypeArguments(final Class<?> cls,
                                                                    final ParameterizedType superType) {
        Assert.notNull(cls, "cls is null");
        Assert.notNull(superType, "superType is null");

        final Class<?> superClass = getRawType(superType);

        if (!isAssignable(cls, superClass)) {
            return null;
        }

        if (cls.equals(superClass)) {
            return getTypeArguments(superType, superClass, null);
        }

        final Type midType = getClosestParentType(cls, superClass);

        if (midType instanceof Class<?>) {
            return determineTypeArguments((Class<?>) midType, superType);
        }

        final ParameterizedType midParameterizedType = (ParameterizedType) midType;
        final Class<?> midClass = getRawType(midParameterizedType);
        final Map<TypeVariable<?>, Type> typeVarAssigns = determineTypeArguments(midClass, superType);
        mapTypeVariablesToArguments(cls, midParameterizedType, typeVarAssigns);

        return typeVarAssigns;
    }

    /**
     * 执行类型变量的映射
     *
     * @param <T>               泛型类型
     * @param cls               类
     * @param parameterizedType 参数化类型
     * @param typeVarAssigns    Map信息
     */
    private static <T> void mapTypeVariablesToArguments(final Class<T> cls,
                                                        final ParameterizedType parameterizedType,
                                                        final Map<TypeVariable<?>, Type> typeVarAssigns) {
        final Type ownerType = parameterizedType.getOwnerType();

        if (ownerType instanceof ParameterizedType) {
            mapTypeVariablesToArguments(cls, (ParameterizedType) ownerType, typeVarAssigns);
        }

        final Type[] typeArgs = parameterizedType.getActualTypeArguments();
        final TypeVariable<?>[] typeVars = getRawType(parameterizedType).getTypeParameters();
        final List<TypeVariable<Class<T>>> typeVarList = Arrays.asList(cls
                .getTypeParameters());

        for (int i = 0; i < typeArgs.length; i++) {
            final TypeVariable<?> typeVar = typeVars[i];
            final Type typeArg = typeArgs[i];

            if (typeVarList.contains(typeArg)
                    && typeVarAssigns.containsKey(typeVar)) {
                typeVarAssigns.put((TypeVariable<?>) typeArg, typeVarAssigns.get(typeVar));
            }
        }
    }

    /**
     * 获取与{@code superClass} 指定的超类最接近的父类
     *
     * @param cls        类
     * @param superClass 超类
     * @return 父类型
     */
    private static Type getClosestParentType(final Class<?> cls, final Class<?> superClass) {
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
                    throw new IllegalStateException("Unexpected generic"
                            + " interface type found: " + midType);
                }

                if (isAssignable(midClass, superClass)
                        && isAssignable(genericInterface, (Type) midClass)) {
                    genericInterface = midType;
                }
            }

            if (genericInterface != null) {
                return genericInterface;
            }
        }

        return cls.getGenericSuperclass();
    }

    /**
     * 检查给定值是否可以按照Java泛型规则分配给目标类型
     *
     * @param value 要检查的值
     * @param type  目标类型
     * @return 如果{@code value}是{@code type}的实例，则{@code true}.
     */
    public static boolean isInstance(final Object value, final Type type) {
        if (type == null) {
            return false;
        }

        return value == null ? !(type instanceof Class<?>) || !((Class<?>) type).isPrimitive()
                : isAssignable(value.getClass(), type, null);
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
     * 则返回一个包含{@link Object}唯一值的数组。否则，
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
     * 如果{@link WildcardType#getLowerBounds()}返回一个空数组，
     * 则返回一个包含单个值{@code null}的数组。否则，
     * 它将返回{@link WildcardType#getLowerBounds()}的结果.
     *
     * @param wildcardType 通配符类型
     * @return 包含通配符类型下界的非空数组.
     */
    public static Type[] getImplicitLowerBounds(final WildcardType wildcardType) {
        Assert.notNull(wildcardType, "wildcardType is null");
        final Type[] bounds = wildcardType.getLowerBounds();

        return bounds.length == 0 ? new Type[]{null} : bounds;
    }

    /**
     * 确定指定的类型，是否满类型变量的边界
     *
     * @param typeVarAssigns 指定分配给类型变量的潜在类型，而不是{@code null}.
     * @return 是否可以将类型分配给它们各自的类型变量.
     */
    public static boolean typesSatisfyVariables(final Map<TypeVariable<?>, Type> typeVarAssigns) {
        Assert.notNull(typeVarAssigns, "typeVarAssigns is null");
        for (final Map.Entry<TypeVariable<?>, Type> entry : typeVarAssigns.entrySet()) {
            final TypeVariable<?> typeVar = entry.getKey();
            final Type type = entry.getValue();

            for (final Type bound : getImplicitBounds(typeVar)) {
                if (!isAssignable(type, substituteTypeVariables(bound, typeVarAssigns),
                        typeVarAssigns)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将传入的类型转换为{@link Class}对象。方便的类型检查方法.
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
     * 根据上下文获取Java类型的原始类型
     * 主要用于{@link TypeVariable}s和{@link GenericArrayType}
     *
     * @param type          类型
     * @param assigningType 要解析的类型
     * @return 如果不能解析类型，则解析{@link Class}对象或{@code null}
     */
    public static Class<?> getRawType(final Type type, final Type assigningType) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getRawType((ParameterizedType) type);
        }

        if (type instanceof TypeVariable<?>) {
            if (assigningType == null) {
                return null;
            }

            final Object genericDeclaration = ((TypeVariable<?>) type).getGenericDeclaration();

            if (!(genericDeclaration instanceof Class<?>)) {
                return null;
            }

            final Map<TypeVariable<?>, Type> typeVarAssigns = getTypeArguments(assigningType,
                    (Class<?>) genericDeclaration);

            if (typeVarAssigns == null) {
                return null;
            }

            // 获取分配给该类型变量的参数
            final Type typeArgument = typeVarAssigns.get(type);

            if (typeArgument == null) {
                return null;
            }

            // 获取此类型变量的参数
            return getRawType(typeArgument, assigningType);
        }

        if (type instanceof GenericArrayType) {
            // 获取原始组件类型
            final Class<?> rawComponentType = getRawType(((GenericArrayType) type)
                    .getGenericComponentType(), assigningType);

            // 从原始组件类型创建数组类型并返回其类
            return Array.newInstance(rawComponentType, 0).getClass();
        }

        // 这不是要找的方法
        if (type instanceof WildcardType) {
            return null;
        }

        throw new IllegalArgumentException("unknown type: " + type);
    }

    /**
     * 了解指定的类型是否表示数组类型。
     *
     * @param type 要检查的类型
     * @return 如果{@code type}是数组类或{@link GenericArrayType}，则为{@code true}
     */
    public static boolean isArrayType(final Type type) {
        return type instanceof GenericArrayType || type instanceof Class<?> && ((Class<?>) type).isArray();
    }

    /**
     * 获取数组组件类型为{@code type}.
     *
     * @param type 要检查的类型
     * @return 如果类型不是数组类型，则为null
     */
    public static Type getArrayComponentType(final Type type) {
        if (type instanceof Class<?>) {
            final Class<?> clazz = (Class<?>) type;
            return clazz.isArray() ? clazz.getComponentType() : null;
        }
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        }
        return null;
    }

    /**
     * 获取一个表示{@code type}的类型，该类型具有“展开”的变量赋值。
     *
     * @param typeArguments 参数 {@link TypeUtils#getTypeArguments(Type, Class)}
     * @param type          变量赋值的类型
     * @return Type
     * @since 3.2.0
     */
    public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments, final Type type) {
        if (typeArguments == null) {
            typeArguments = Collections.emptyMap();
        }
        if (containsTypeVariables(type)) {
            if (type instanceof TypeVariable<?>) {
                return unrollVariables(typeArguments, typeArguments.get(type));
            }
            if (type instanceof ParameterizedType) {
                final ParameterizedType p = (ParameterizedType) type;
                final Map<TypeVariable<?>, Type> parameterizedTypeArguments;
                if (p.getOwnerType() == null) {
                    parameterizedTypeArguments = typeArguments;
                } else {
                    parameterizedTypeArguments = new HashMap<>(typeArguments);
                    parameterizedTypeArguments.putAll(getTypeArguments(p));
                }
                final Type[] args = p.getActualTypeArguments();
                for (int i = 0; i < args.length; i++) {
                    final Type unrolled = unrollVariables(parameterizedTypeArguments, args[i]);
                    if (unrolled != null) {
                        args[i] = unrolled;
                    }
                }
                return parameterizeWithOwner(p.getOwnerType(), (Class<?>) p.getRawType(), args);
            }
            if (type instanceof WildcardType) {
                final WildcardType wild = (WildcardType) type;
                return wildcardType().withUpperBounds(unrollBounds(typeArguments, wild.getUpperBounds()))
                        .withLowerBounds(unrollBounds(typeArguments, wild.getLowerBounds())).build();
            }
        }
        return type;
    }

    /**
     * 局部辅助方法来展开类型界限数组中的变量.
     *
     * @param typeArguments 参数 {@link Map}
     * @param bounds        绑定
     * @return {@code bounds}，任何变量都可以重新赋值
     * @since 3.2.0
     */
    private static Type[] unrollBounds(final Map<TypeVariable<?>, Type> typeArguments, final Type[] bounds) {
        Type[] result = bounds;
        int i = 0;
        for (; i < result.length; i++) {
            final Type unrolled = unrollVariables(typeArguments, result[i]);
            if (unrolled == null) {
                result = ArrayUtils.remove(result, i--);
            } else {
                result[i] = unrolled;
            }
        }
        return result;
    }

    /**
     * 递归比较与{@code type}关联的任何类型参数是否绑定到变量.
     *
     * @param type 检查类型变量的类型
     * @return boolean
     * @since 3.2.0
     */
    public static boolean containsTypeVariables(final Type type) {
        if (type instanceof TypeVariable<?>) {
            return true;
        }
        if (type instanceof Class<?>) {
            return ((Class<?>) type).getTypeParameters().length > 0;
        }
        if (type instanceof ParameterizedType) {
            for (final Type arg : ((ParameterizedType) type).getActualTypeArguments()) {
                if (containsTypeVariables(arg)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof WildcardType) {
            final WildcardType wild = (WildcardType) type;
            return containsTypeVariables(getImplicitLowerBounds(wild)[0])
                    || containsTypeVariables(getImplicitUpperBounds(wild)[0]);
        }
        return false;
    }

    /**
     * 创建参数化类型实例.
     *
     * @param raw           用于创建参数化类型实例的原始类
     * @param typeArguments 用于参数化的类型
     * @return {@link ParameterizedType}
     * @since 3.2.0
     */
    public static final ParameterizedType parameterize(final Class<?> raw, final Type... typeArguments) {
        return parameterizeWithOwner(null, raw, typeArguments);
    }

    /**
     * 创建参数化类型实例.
     *
     * @param raw             用于创建参数化类型实例的原始类
     * @param typeArgMappings 用于参数化的类型
     * @return {@link ParameterizedType}
     * @since 3.2.0
     */
    public static final ParameterizedType parameterize(final Class<?> raw,
                                                       final Map<TypeVariable<?>, Type> typeArgMappings) {
        Assert.notNull(raw, "raw class is null");
        Assert.notNull(typeArgMappings, "typeArgMappings is null");
        return parameterizeWithOwner(null, raw, extractTypeArgumentsFrom(typeArgMappings, raw.getTypeParameters()));
    }

    /**
     * 创建参数化类型实例.
     *
     * @param owner         类型
     * @param raw           用于创建参数化类型实例的原始类
     * @param typeArguments 用于参数化的类型
     * @return {@link ParameterizedType}
     * @since 3.2.0
     */
    public static final ParameterizedType parameterizeWithOwner(final Type owner, final Class<?> raw,
                                                                final Type... typeArguments) {
        Assert.notNull(raw, "raw class is null");
        final Type useOwner;
        if (raw.getEnclosingClass() == null) {
            Assert.isTrue(owner == null, "no owner allowed for top-level %s", raw);
            useOwner = null;
        } else if (owner == null) {
            useOwner = raw.getEnclosingClass();
        } else {
            Assert.isTrue(isAssignable(owner, raw.getEnclosingClass()),
                    "%s is invalid owner type for parameterized %s", owner, raw);
            useOwner = owner;
        }
        Assert.noNullElements(typeArguments, "null type argument at index %s");
        Assert.isTrue(raw.getTypeParameters().length == typeArguments.length,
                "invalid number of type parameters specified: expected %d, got %d", raw.getTypeParameters().length,
                typeArguments.length);

        return new ParameterizedTypeImpl(raw, useOwner, typeArguments);
    }

    /**
     * 创建参数化类型实例.
     *
     * @param owner           类型
     * @param raw             用于创建参数化类型实例的原始类
     * @param typeArgMappings 用于参数化的映射
     * @return {@link ParameterizedType}
     * @since 3.2.0
     */
    public static final ParameterizedType parameterizeWithOwner(final Type owner, final Class<?> raw,
                                                                final Map<TypeVariable<?>, Type> typeArgMappings) {
        Assert.notNull(raw, "raw class is null");
        Assert.notNull(typeArgMappings, "typeArgMappings is null");
        return parameterizeWithOwner(owner, raw, extractTypeArgumentsFrom(typeArgMappings, raw.getTypeParameters()));
    }

    /**
     * 辅助方法，用于为参数化类型建立形式参数.
     *
     * @param mappings  包含作业的map
     * @param variables 键映射
     * @return 对应于指定键的映射值的数组
     */
    private static Type[] extractTypeArgumentsFrom(final Map<TypeVariable<?>, Type> mappings, final TypeVariable<?>[] variables) {
        final Type[] result = new Type[variables.length];
        int index = 0;
        for (final TypeVariable<?> var : variables) {
            Assert.isTrue(mappings.containsKey(var), "missing argument mapping for %s", toString(var));
            result[index++] = mappings.get(var);
        }
        return result;
    }

    /**
     * 获取{@link WildcardTypeBuilder}.
     *
     * @return {@link WildcardTypeBuilder}
     * @since 3.2.0
     */
    public static WildcardTypeBuilder wildcardType() {
        return new WildcardTypeBuilder();
    }

    /**
     * 创建泛型数组类型实例.
     *
     * @param componentType 数组元素的类型
     * @return {@link GenericArrayType}
     * @since 3.2.0
     */
    public static GenericArrayType genericArrayType(final Type componentType) {
        return new GenericArrayTypeImpl(Assert.notNull(componentType, "componentType is null"));
    }

    /**
     * 检查类型是否相等
     *
     * @param t1 第一个比较对象
     * @param t2 第二个比较对象
     * @return 是否相等 true/false
     * @since 3.2.0
     */
    public static boolean equals(final Type t1, final Type t2) {
        if (Objects.equals(t1, t2)) {
            return true;
        }
        if (t1 instanceof ParameterizedType) {
            return equals((ParameterizedType) t1, t2);
        }
        if (t1 instanceof GenericArrayType) {
            return equals((GenericArrayType) t1, t2);
        }
        if (t1 instanceof WildcardType) {
            return equals((WildcardType) t1, t2);
        }
        return false;
    }

    /**
     * 比较{@code p}是否等于{@code t}
     *
     * @param p 第一个比较对象
     * @param t 第二个比较对象
     * @return 是否相等 true/false
     * @since 3.2.0
     */
    private static boolean equals(final ParameterizedType p, final Type t) {
        if (t instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) t;
            if (equals(p.getRawType(), other.getRawType()) && equals(p.getOwnerType(), other.getOwnerType())) {
                return equals(p.getActualTypeArguments(), other.getActualTypeArguments());
            }
        }
        return false;
    }

    /**
     * 比较{@code a}是否等于{@code t}
     *
     * @param a 第一个比较对象
     * @param t 第二个比较对象
     * @return 是否相等 true/false
     * @since 3.2.0
     */
    private static boolean equals(final GenericArrayType a, final Type t) {
        return t instanceof GenericArrayType
                && equals(a.getGenericComponentType(), ((GenericArrayType) t).getGenericComponentType());
    }

    /**
     * 比较{@code t}是否等于{@code w}
     *
     * @param w 第一个比较对象
     * @param t 第二个比较对象
     * @return 是否相等 true/false
     * @since 3.2.0
     */
    private static boolean equals(final WildcardType w, final Type t) {
        if (t instanceof WildcardType) {
            final WildcardType other = (WildcardType) t;
            return equals(getImplicitLowerBounds(w), getImplicitLowerBounds(other))
                    && equals(getImplicitUpperBounds(w), getImplicitUpperBounds(other));
        }
        return false;
    }

    /**
     * 比较{@code t1}是否等于{@code t2}
     *
     * @param t1 第一个比较对象
     * @param t2 第二个比较对象
     * @return 是否相等 true/false
     * @since 3.2.0
     */
    private static boolean equals(final Type[] t1, final Type[] t2) {
        if (t1.length == t2.length) {
            for (int i = 0; i < t1.length; i++) {
                if (!equals(t1[i], t2[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 将给定的类型表示为java风格的字符串.
     *
     * @param type 创建字符串表示的类型
     * @return 字符串
     * @since 3.2.0
     */
    public static String toString(final Type type) {
        Assert.notNull(type);
        if (type instanceof Class<?>) {
            return classToString((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return parameterizedTypeToString((ParameterizedType) type);
        }
        if (type instanceof WildcardType) {
            return wildcardTypeToString((WildcardType) type);
        }
        if (type instanceof TypeVariable<?>) {
            return typeVariableToString((TypeVariable<?>) type);
        }
        if (type instanceof GenericArrayType) {
            return genericArrayTypeToString((GenericArrayType) type);
        }
        throw new IllegalArgumentException(ObjectUtils.identityToString(type));
    }

    /**
     * 格式化一个{@link TypeVariable}，包括它的{@link GenericDeclaration}
     *
     * @param var 创建字符串表示的类型变量，而不是{@code null}
     * @return 字符串
     * @since 3.2.0
     */
    public static String toLongString(final TypeVariable<?> var) {
        Assert.notNull(var, "var is null");
        final StringBuilder buf = new StringBuilder();
        final GenericDeclaration d = var.getGenericDeclaration();
        if (d instanceof Class<?>) {
            Class<?> c = (Class<?>) d;
            while (true) {
                if (c.getEnclosingClass() == null) {
                    buf.insert(0, c.getName());
                    break;
                }
                buf.insert(0, c.getSimpleName()).insert(0, Symbol.C_DOT);
                c = c.getEnclosingClass();
            }
        } else if (d instanceof Type) {
            buf.append(toString((Type) d));
        } else {
            buf.append(d);
        }
        return buf.append(Symbol.C_COLON).append(typeVariableToString(var)).toString();
    }

    /**
     * 将{@link Class}格式化为{@link String}
     *
     * @param c {@code Class} 格式化内容
     * @return 字符串
     * @since 3.2.0
     */
    private static String classToString(final Class<?> c) {
        if (c.isArray()) {
            return toString(c.getComponentType()) + Symbol.BRACKET;
        }

        final StringBuilder buf = new StringBuilder();

        if (c.getEnclosingClass() != null) {
            buf.append(classToString(c.getEnclosingClass())).append(Symbol.C_DOT).append(c.getSimpleName());
        } else {
            buf.append(c.getName());
        }
        if (c.getTypeParameters().length > 0) {
            buf.append(Symbol.C_LT);
            appendAllTo(buf, ", ", c.getTypeParameters());
            buf.append(Symbol.C_GT);
        }
        return buf.toString();
    }

    /**
     * 将{@link TypeVariable}格式化为{@link String}
     *
     * @param v {@code TypeVariable} 格式化内容
     * @return 字符串
     * @since 3.2.0
     */
    private static String typeVariableToString(final TypeVariable<?> v) {
        final StringBuilder buf = new StringBuilder(v.getName());
        final Type[] bounds = v.getBounds();
        if (bounds.length > 0 && !(bounds.length == 1 && Object.class.equals(bounds[0]))) {
            buf.append(" extends ");
            appendAllTo(buf, " & ", v.getBounds());
        }
        return buf.toString();
    }

    /**
     * 将{@link ParameterizedType}格式化为{@link String}
     *
     * @param p {@code ParameterizedType}格式化内容
     * @return 字符串
     * @since 3.2.0
     */
    private static String parameterizedTypeToString(final ParameterizedType p) {
        final StringBuilder buf = new StringBuilder();

        final Type useOwner = p.getOwnerType();
        final Class<?> raw = (Class<?>) p.getRawType();

        if (useOwner == null) {
            buf.append(raw.getName());
        } else {
            if (useOwner instanceof Class<?>) {
                buf.append(((Class<?>) useOwner).getName());
            } else {
                buf.append(useOwner.toString());
            }
            buf.append(Symbol.C_DOT).append(raw.getSimpleName());
        }

        final int[] recursiveTypeIndexes = findRecursiveTypes(p);

        if (recursiveTypeIndexes.length > 0) {
            appendRecursiveTypes(buf, recursiveTypeIndexes, p.getActualTypeArguments());
        } else {
            appendAllTo(buf.append(Symbol.C_LT), ", ", p.getActualTypeArguments()).append(Symbol.C_GT);
        }

        return buf.toString();
    }

    private static void appendRecursiveTypes(final StringBuilder buf, final int[] recursiveTypeIndexes, final Type[] argumentTypes) {
        for (int i = 0; i < recursiveTypeIndexes.length; i++) {
            appendAllTo(buf.append(Symbol.C_LT), ", ", argumentTypes[i].toString()).append(Symbol.C_GT);
        }

        final Type[] argumentsFiltered = ArrayUtils.removeAll(argumentTypes, recursiveTypeIndexes);

        if (argumentsFiltered.length > 0) {
            appendAllTo(buf.append(Symbol.C_LT), ", ", argumentsFiltered).append(Symbol.C_GT);
        }
    }

    private static int[] findRecursiveTypes(final ParameterizedType p) {
        final Type[] filteredArgumentTypes = Arrays.copyOf(p.getActualTypeArguments(), p.getActualTypeArguments().length);
        int[] indexesToRemove = {};
        for (int i = 0; i < filteredArgumentTypes.length; i++) {
            if (filteredArgumentTypes[i] instanceof TypeVariable<?>) {
                if (containsVariableTypeSameParametrizedTypeBound(((TypeVariable<?>) filteredArgumentTypes[i]), p)) {
                    indexesToRemove = ArrayUtils.add(indexesToRemove, i);
                }
            }
        }
        return indexesToRemove;
    }

    private static boolean containsVariableTypeSameParametrizedTypeBound(final TypeVariable<?> typeVariable, final ParameterizedType p) {
        return ArrayUtils.contains(typeVariable.getBounds(), p);
    }

    private static String wildcardTypeToString(final WildcardType w) {
        final StringBuilder buf = new StringBuilder().append(Symbol.C_QUESTION_MARK);
        final Type[] lowerBounds = w.getLowerBounds();
        final Type[] upperBounds = w.getUpperBounds();
        if (lowerBounds.length > 1 || lowerBounds.length == 1 && lowerBounds[0] != null) {
            appendAllTo(buf.append(" super "), " & ", lowerBounds);
        } else if (upperBounds.length > 1 || upperBounds.length == 1 && !Object.class.equals(upperBounds[0])) {
            appendAllTo(buf.append(" extends "), " & ", upperBounds);
        }
        return buf.toString();
    }

    /**
     * 按照指定的分隔符追加内容.
     *
     * @param buf   目的地
     * @param sep   分隔符
     * @param types 要添加的内容
     * @return 操作后的buf
     * @since 3.2.0
     */
    private static <T> StringBuilder appendAllTo(final StringBuilder buf, final String sep, final T... types) {
        Assert.notEmpty(Assert.noNullElements(types));
        if (types.length > 0) {
            buf.append(toString(types[0]));
            for (int i = 1; i < types.length; i++) {
                buf.append(sep).append(toString(types[i]));
            }
        }
        return buf;
    }

    private static <T> String toString(final T object) {
        return object instanceof Type ? toString((Type) object) : object.toString();
    }

    private static String genericArrayTypeToString(final GenericArrayType g) {
        return String.format("%s[]", toString(g.getGenericComponentType()));
    }

    public static class WildcardTypeBuilder implements Builder<WildcardType> {

        private Type[] upperBounds;
        private Type[] lowerBounds;

        private WildcardTypeBuilder() {
        }

        public WildcardTypeBuilder withUpperBounds(final Type... bounds) {
            this.upperBounds = bounds;
            return this;
        }

        public WildcardTypeBuilder withLowerBounds(final Type... bounds) {
            this.lowerBounds = bounds;
            return this;
        }


        @Override
        public WildcardType build() {
            return new WildcardTypeImpl(upperBounds, lowerBounds);
        }
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {

        private final Type componentType;

        private GenericArrayTypeImpl(final Type componentType) {
            this.componentType = componentType;
        }


        @Override
        public Type getGenericComponentType() {
            return componentType;
        }


        @Override
        public String toString() {
            return TypeUtils.toString(this);
        }


        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof GenericArrayType && TypeUtils.equals(this, (GenericArrayType) obj);
        }


        @Override
        public int hashCode() {
            int result = 67 << 4;
            result |= componentType.hashCode();
            return result;
        }
    }

    private static final class ParameterizedTypeImpl implements ParameterizedType {

        private final Class<?> raw;
        private final Type useOwner;
        private final Type[] typeArguments;

        /**
         * 构造函数
         *
         * @param raw           类型
         * @param useOwner      要使用的所有者类型
         * @param typeArguments 正式的类型参数
         */
        private ParameterizedTypeImpl(final Class<?> raw, final Type useOwner, final Type[] typeArguments) {
            this.raw = raw;
            this.useOwner = useOwner;
            this.typeArguments = Arrays.copyOf(typeArguments, typeArguments.length, Type[].class);
        }


        @Override
        public Type getRawType() {
            return raw;
        }


        @Override
        public Type getOwnerType() {
            return useOwner;
        }


        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }


        @Override
        public String toString() {
            return TypeUtils.toString(this);
        }


        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof ParameterizedType && TypeUtils.equals(this, ((ParameterizedType) obj));
        }


        @Override
        public int hashCode() {
            int result = 71 << 4;
            result |= raw.hashCode();
            result <<= 4;
            result |= Objects.hashCode(useOwner);
            result <<= 8;
            result |= Arrays.hashCode(typeArguments);
            return result;
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {

        private static final Type[] EMPTY_BOUNDS = new Type[0];
        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        private WildcardTypeImpl(final Type[] upperBounds, final Type[] lowerBounds) {
            this.upperBounds = ObjectUtils.defaultIfNull(upperBounds, EMPTY_BOUNDS);
            this.lowerBounds = ObjectUtils.defaultIfNull(lowerBounds, EMPTY_BOUNDS);
        }


        @Override
        public Type[] getUpperBounds() {
            return upperBounds.clone();
        }


        @Override
        public Type[] getLowerBounds() {
            return lowerBounds.clone();
        }


        @Override
        public String toString() {
            return TypeUtils.toString(this);
        }


        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof WildcardType && TypeUtils.equals(this, (WildcardType) obj);
        }


        @Override
        public int hashCode() {
            int result = 73 << 8;
            result |= Arrays.hashCode(upperBounds);
            result <<= 8;
            result |= Arrays.hashCode(lowerBounds);
            return result;
        }
    }

}
