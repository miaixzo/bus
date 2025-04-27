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
package org.miaixz.bus.core.convert;

import java.util.HashMap;
import java.util.Set;

import org.miaixz.bus.core.center.map.BiMap;

/**
 * 基本变量类型的枚举 基本类型枚举包括原始类型和包装类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BasicType {

    /**
     * byte
     */
    BYTE,
    /**
     * short
     */
    SHORT,
    /**
     * int
     */
    INT,
    /**
     * {@link Integer}
     */
    INTEGER,
    /**
     * long
     */
    LONG,
    /**
     * double
     */
    DOUBLE,
    /**
     * float
     */
    FLOAT,
    /**
     * boolean
     */
    BOOLEAN,
    /**
     * char
     */
    CHAR,
    /**
     * {@link Character}
     */
    CHARACTER,
    /**
     * {@link String}
     */
    STRING;

    /**
     * 包装类型为Key，原始类型为Value，例如： Integer.class => int.class.
     */
    private static final BiMap<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new BiMap<>(new HashMap<>(8, 1));

    static {
        WRAPPER_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_PRIMITIVE_MAP.put(Byte.class, byte.class);
        WRAPPER_PRIMITIVE_MAP.put(Character.class, char.class);
        WRAPPER_PRIMITIVE_MAP.put(Double.class, double.class);
        WRAPPER_PRIMITIVE_MAP.put(Float.class, float.class);
        WRAPPER_PRIMITIVE_MAP.put(Integer.class, int.class);
        WRAPPER_PRIMITIVE_MAP.put(Long.class, long.class);
        WRAPPER_PRIMITIVE_MAP.put(Short.class, short.class);
    }

    /**
     * 原始类转为包装类，非原始类返回原类
     *
     * @param clazz 原始类
     * @return 包装类
     */
    public static Class<?> wrap(final Class<?> clazz) {
        return wrap(clazz, false);
    }

    /**
     * 原始类转为包装类，非原始类返回原类
     *
     * @param clazz           原始类
     * @param errorReturnNull 如果没有对应类的原始类型，是否返回{@code null}，{@code true}返回{@code null}，否则返回原class
     * @return 包装类
     */
    public static Class<?> wrap(final Class<?> clazz, final boolean errorReturnNull) {
        if (null == clazz || !clazz.isPrimitive()) {
            return clazz;
        }
        final Class<?> result = WRAPPER_PRIMITIVE_MAP.getInverse().get(clazz);
        return (null == result) ? errorReturnNull ? null : clazz : result;
    }

    /**
     * 包装类转为原始类，非包装类返回原类
     *
     * @param clazz 包装类
     * @return 原始类
     */
    public static Class<?> unWrap(final Class<?> clazz) {
        if (null == clazz || clazz.isPrimitive()) {
            return clazz;
        }
        final Class<?> result = WRAPPER_PRIMITIVE_MAP.get(clazz);
        return (null == result) ? clazz : result;
    }

    /**
     * 是否为包装类型
     *
     * @param clazz 类
     * @return 是否为包装类型
     */
    public static boolean isPrimitiveWrapper(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    /**
     * 获取所有原始类型
     *
     * @return 所有原始类型
     */
    public static Set<Class<?>> getPrimitiveSet() {
        return WRAPPER_PRIMITIVE_MAP.getInverse().keySet();
    }

    /**
     * 获取所有原始类型
     *
     * @return 所有原始类型
     */
    public static Set<Class<?>> getWrapperSet() {
        return WRAPPER_PRIMITIVE_MAP.keySet();
    }

}
