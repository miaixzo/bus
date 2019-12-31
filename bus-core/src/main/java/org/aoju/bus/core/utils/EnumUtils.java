/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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


import org.aoju.bus.core.lang.Assert;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 枚举工具类
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class EnumUtils {

    private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
    private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
    private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";
    private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";

    /**
     * 指定类是否为Enum类
     *
     * @param clazz 类
     * @return 是否为Enum类
     */
    public static boolean isEnum(Class<?> clazz) {
        Assert.notNull(clazz);
        return clazz.isEnum();
    }

    /**
     * 指定类是否为Enum类
     *
     * @param obj 类
     * @return 是否为Enum类
     */
    public static boolean isEnum(Object obj) {
        Assert.notNull(obj);
        return obj.getClass().isEnum();
    }

    /**
     * Enum对象转String，调用{@link Enum#name()} 方法
     *
     * @param e Enum
     * @return name值
     * @since 4.1.13
     */
    public static String toString(Enum<?> e) {
        return null != e ? e.name() : null;
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}
     *
     * @param <E>       枚举类型泛型
     * @param enumClass 枚举类
     * @param value     值
     * @return 枚举值
     * @since 4.1.13
     */
    public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value) {
        return Enum.valueOf(enumClass, value);
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}<br>
     * 如果无枚举值，返回默认值
     *
     * @param <E>          枚举类型泛型
     * @param enumClass    枚举类
     * @param value        值
     * @param defaultValue 无对应枚举值返回的默认值
     * @return 枚举值
     * @since 4.5.18
     */
    public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value, E defaultValue) {
        return ObjectUtils.defaultIfNull(fromStringQuietly(enumClass, value), defaultValue);
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}，转换失败返回{@code null} 而非报错
     *
     * @param <E>       枚举类型泛型
     * @param enumClass 枚举类
     * @param value     值
     * @return 枚举值
     * @since 4.5.18
     */
    public static <E extends Enum<E>> E fromStringQuietly(Class<E> enumClass, String value) {
        if (null == enumClass || StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return fromString(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 模糊匹配转换为枚举，给定一个值，匹配枚举中定义的所有字段名（包括name属性），一旦匹配到返回这个枚举对象，否则返回null
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param value     值
     * @return 匹配到的枚举对象，未匹配到返回null
     */
    public static <E extends Enum<E>> E likeValueOf(Class<E> enumClass, Object value) {
        if (value instanceof CharSequence) {
            value = value.toString().trim();
        }

        final Field[] fields = ReflectUtils.getFields(enumClass);
        final Enum<?>[] enums = enumClass.getEnumConstants();
        String fieldName;
        for (Field field : fields) {
            fieldName = field.getName();
            if (field.getType().isEnum() || "ENUM$VALUES".equals(fieldName) || "ordinal".equals(fieldName)) {
                // 跳过一些特殊字段
                continue;
            }
            for (Enum<?> enumObj : enums) {
                if (ObjectUtils.equal(value, ReflectUtils.getFieldValue(enumObj, field))) {
                    return (E) enumObj;
                }
            }
        }
        return null;
    }

    /**
     * 枚举类中所有枚举对象的name列表
     *
     * @param clazz 枚举类
     * @return name列表
     */
    public static List<String> getNames(Class<? extends Enum<?>> clazz) {
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final List<String> list = new ArrayList<>(enums.length);
        for (Enum<?> e : enums) {
            list.add(e.name());
        }
        return list;
    }

    /**
     * 获得枚举类中各枚举对象下指定字段的值
     *
     * @param clazz     枚举类
     * @param fieldName 字段名，最终调用getXXX方法
     * @return 字段值列表
     */
    public static List<Object> getFieldValues(Class<? extends Enum<?>> clazz, String fieldName) {
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final List<Object> list = new ArrayList<>(enums.length);
        for (Enum<?> e : enums) {
            list.add(ReflectUtils.getFieldValue(e, fieldName));
        }
        return list;
    }

    /**
     * 获得枚举类中所有的字段名<br>
     * 除用户自定义的字段名，也包括“name”字段，例如：
     *
     * <pre>
     *   EnumUtil.getFieldNames(Color.class) == ["name", "index"]
     * </pre>
     *
     * @param clazz 枚举类
     * @return 字段名列表
     * @since 4.1.20
     */
    public static List<String> getFieldNames(Class<? extends Enum<?>> clazz) {
        final List<String> names = new ArrayList<>();
        final Field[] fields = ReflectUtils.getFields(clazz);
        String name;
        for (Field field : fields) {
            name = field.getName();
            if (field.getType().isEnum() || name.contains("$VALUES") || "ordinal".equals(name)) {
                continue;
            }
            if (false == names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * 获取枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序<br>
     * 结果中键为枚举名，值为枚举对象
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @return 枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序
     * @since 4.0.2
     */
    public static <E extends Enum<E>> LinkedHashMap<String, E> getEnumMap(final Class<E> enumClass) {
        final LinkedHashMap<String, E> map = new LinkedHashMap<>();
        for (final E e : enumClass.getEnumConstants()) {
            map.put(e.name(), e);
        }
        return map;
    }

    /**
     * 获得枚举名对应指定字段值的Map<br>
     * 键为枚举名，值为字段值
     *
     * @param clazz     枚举类
     * @param fieldName 字段名，最终调用getXXX方法
     * @return 枚举名对应指定字段值的Map
     */
    public static Map<String, Object> getNameFieldMap(Class<? extends Enum<?>> clazz, String fieldName) {
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final Map<String, Object> map = MapUtils.newHashMap(enums.length);
        for (Enum<?> e : enums) {
            map.put(e.name(), ReflectUtils.getFieldValue(e, fieldName));
        }
        return map;
    }

    /**
     * 判断某个值是存在枚举中
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param val       需要查找的值
     * @return 是否存在
     */
    public static <E extends Enum<E>> boolean contains(final Class<E> enumClass, String val) {
        return getEnumMap(enumClass).containsKey(val);
    }

    /**
     * 判断某个值是不存在枚举中
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param val       需要查找的值
     * @return 是否不存在
     */
    public static <E extends Enum<E>> boolean notContains(final Class<E> enumClass, String val) {
        return false == contains(enumClass, val);
    }

    /**
     * 忽略大小检查某个枚举值是否匹配指定值
     *
     * @param e   枚举值
     * @param val 需要判断的值
     * @return 是非匹配
     */
    public static boolean equalsIgnoreCase(final Enum<?> e, String val) {
        return StringUtils.equalsIgnoreCase(toString(e), val);
    }

    /**
     * 检查某个枚举值是否匹配指定值
     *
     * @param e   枚举值
     * @param val 需要判断的值
     * @return 是非匹配
     */
    public static boolean equals(final Enum<?> e, String val) {
        return StringUtils.equals(toString(e), val);
    }

    /**
     * 获取枚举的{@code List}
     *
     * @param <E>       枚举的类型
     * @param enumClass 要查询的枚举的类
     * @return 枚举的可修改列表，从不为空
     */
    public static <E extends Enum<E>> List<E> getEnumList(final Class<E> enumClass) {
        return new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
    }

    /**
     * 检查指定的名称是否是类的有效枚举
     *
     * @param <E>       枚举的类型
     * @param enumClass 要查询的枚举的类
     * @param enumName  枚举名null返回false
     * @return 如果枚举名有效，则为真，否则为假
     */
    public static <E extends Enum<E>> boolean isValidEnum(final Class<E> enumClass, final String enumName) {
        return getEnum(enumClass, enumName) != null;
    }

    /**
     * 检查指定的名称是否是类的有效枚举
     *
     * @param <E>       枚举的类型
     * @param enumClass 要查询的枚举的类
     * @param enumName  枚举名null返回false
     * @return 如果枚举名有效，则为真，否则为假
     */
    public static <E extends Enum<E>> boolean isValidEnumIgnoreCase(final Class<E> enumClass, final String enumName) {
        return getEnumIgnoreCase(enumClass, enumName) != null;
    }

    /**
     * 获取类的枚举，如果没有找到，则返回{@code null}
     *
     * @param <E>       枚举的类型
     * @param enumClass 要查询的枚举的类
     * @param enumName  枚举名
     * @return 枚举，如果没有找到则为空
     */
    public static <E extends Enum<E>> E getEnum(final Class<E> enumClass, final String enumName) {
        if (enumName == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, enumName);
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 获取类的枚举，如果没有找到，则返回{@code null}
     *
     * @param <E>       枚举的类型
     * @param enumClass 要查询的枚举的类
     * @param enumName  枚举名
     * @return 枚举，如果没有找到则为空
     */
    public static <E extends Enum<E>> E getEnumIgnoreCase(final Class<E> enumClass, final String enumName) {
        if (enumName == null || !enumClass.isEnum()) {
            return null;
        }
        for (final E each : enumClass.getEnumConstants()) {
            if (each.name().equalsIgnoreCase(enumName)) {
                return each;
            }
        }
        return null;
    }

    /**
     * 创建Enum的给定子集的长位向量表示
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param values    long[]表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @see #generateBitVectors(Class, Iterable)
     * @since 3.0.1
     */
    public static <E extends Enum<E>> long generateBitVector(final Class<E> enumClass, final Iterable<? extends E> values) {
        checkBitVectorable(enumClass);
        Assert.notNull(values);
        long total = 0;
        for (final E constant : values) {
            Assert.isTrue(constant != null, NULL_ELEMENTS_NOT_PERMITTED);
            total |= 1L << constant.ordinal();
        }
        return total;
    }

    /**
     * 根据需要使用任意数量的{@code long}创建Enum的给定子集的位向量表示
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param values    long[]表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @since 3.2.0
     */
    public static <E extends Enum<E>> long[] generateBitVectors(final Class<E> enumClass, final Iterable<? extends E> values) {
        asEnum(enumClass);
        Assert.notNull(values);
        final EnumSet<E> condensed = EnumSet.noneOf(enumClass);
        for (final E constant : values) {
            Assert.isTrue(constant != null, NULL_ELEMENTS_NOT_PERMITTED);
            condensed.add(constant);
        }
        final long[] result = new long[(enumClass.getEnumConstants().length - 1) / Long.SIZE + 1];
        for (final E value : condensed) {
            result[value.ordinal() / Long.SIZE] |= 1L << (value.ordinal() % Long.SIZE);
        }
        ArrayUtils.reverse(result);
        return result;
    }

    /**
     * 创建给定Enum值数组的长位向量表示
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param values    long[]表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @since 3.0.1
     */
    public static <E extends Enum<E>> long generateBitVector(final Class<E> enumClass, final E... values) {
        Assert.noNullElements(values);
        return generateBitVector(enumClass, Arrays.asList(values));
    }

    /**
     * 根据需要使用任意数量的{@code long}创建Enum的给定子集的位向量表示
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param values    long[]表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @since 3.2.0
     */
    public static <E extends Enum<E>> long[] generateBitVectors(final Class<E> enumClass, final E... values) {
        asEnum(enumClass);
        Assert.noNullElements(values);
        final EnumSet<E> condensed = EnumSet.noneOf(enumClass);
        Collections.addAll(condensed, values);
        final long[] result = new long[(enumClass.getEnumConstants().length - 1) / Long.SIZE + 1];
        for (final E value : condensed) {
            result[value.ordinal() / Long.SIZE] |= 1L << (value.ordinal() % Long.SIZE);
        }
        ArrayUtils.reverse(result);
        return result;
    }

    /**
     * 将{@link EnumUtils#generateBitVector}创建的长值转换为它所表示的enum值集
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param value     long表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @since 3.0.1
     */
    public static <E extends Enum<E>> EnumSet<E> processBitVector(final Class<E> enumClass, final long value) {
        checkBitVectorable(enumClass).getEnumConstants();
        return processBitVectors(enumClass, value);
    }

    /**
     * 将{@link EnumUtils#generateBitVectors}
     * 创建的{@code long[]}转换为它所表示的一组enum值
     *
     * @param enumClass 我们使用的enum类，而不是{@code null}
     * @param values    long[]表示一组enum值，最右边是最不重要的数字，
     *                  而不是{@code null}
     * @param <E>       枚举的类型
     * @return 一组枚举值
     * @since 3.2.0
     */
    public static <E extends Enum<E>> EnumSet<E> processBitVectors(final Class<E> enumClass, final long... values) {
        final EnumSet<E> results = EnumSet.noneOf(asEnum(enumClass));
        final long[] lvalues = ArrayUtils.clone(Assert.notNull(values));
        ArrayUtils.reverse(lvalues);
        for (final E constant : enumClass.getEnumConstants()) {
            final int block = constant.ordinal() / Long.SIZE;
            if (block < lvalues.length && (lvalues[block] & 1L << (constant.ordinal() % Long.SIZE)) != 0) {
                results.add(constant);
            }
        }
        return results;
    }

    /**
     * 验证{@code enumClass}与{@code long}中的注册的是否兼容.
     *
     * @param <E>       枚举的类型
     * @param enumClass 检查
     * @return {@code enumClass}
     * @since 3.0.1
     */
    private static <E extends Enum<E>> Class<E> checkBitVectorable(final Class<E> enumClass) {
        final E[] constants = asEnum(enumClass).getEnumConstants();
        Assert.isTrue(constants.length <= Long.SIZE, CANNOT_STORE_S_S_VALUES_IN_S_BITS,
                Integer.valueOf(constants.length), enumClass.getSimpleName(), Integer.valueOf(Long.SIZE));

        return enumClass;
    }

    /**
     * 验证(@code enumClass)
     *
     * @param <E>       枚举的类型
     * @param enumClass 检查
     * @return {@code enumClass}
     * @since 3.2.0
     */
    private static <E extends Enum<E>> Class<E> asEnum(final Class<E> enumClass) {
        Assert.notNull(enumClass, ENUM_CLASS_MUST_BE_DEFINED);
        Assert.isTrue(enumClass.isEnum(), S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE, enumClass);
        return enumClass;
    }

}
