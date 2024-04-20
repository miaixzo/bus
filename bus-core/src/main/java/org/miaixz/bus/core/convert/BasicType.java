/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基本变量类型的枚举
 * 基本类型枚举包括原始类型和包装类型
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
     * integer
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
     * character
     */
    CHARACTER,
    /**
     * string
     */
    STRING;

    /**
     * 包装类型为Key,原始类型为Value,例如： Integer.class = int.class.
     */
    public static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new ConcurrentHashMap<>(8);
    /**
     * 原始类型为Key,包装类型为Value,例如： int.class = Integer.class.
     */
    public static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new ConcurrentHashMap<>(8);

    static {
        WRAPPER_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_PRIMITIVE_MAP.put(Byte.class, byte.class);
        WRAPPER_PRIMITIVE_MAP.put(Character.class, char.class);
        WRAPPER_PRIMITIVE_MAP.put(Double.class, double.class);
        WRAPPER_PRIMITIVE_MAP.put(Float.class, float.class);
        WRAPPER_PRIMITIVE_MAP.put(Integer.class, int.class);
        WRAPPER_PRIMITIVE_MAP.put(Long.class, long.class);
        WRAPPER_PRIMITIVE_MAP.put(Short.class, short.class);

        for (final Map.Entry<Class<?>, Class<?>> entry : WRAPPER_PRIMITIVE_MAP.entrySet()) {
            PRIMITIVE_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 原始类转为包装类,非原始类返回原类
     *
     * @param clazz 原始类
     * @return 包装类
     */
    public static Class<?> wrap(Class<?> clazz) {
        if (null == clazz || false == clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = PRIMITIVE_WRAPPER_MAP.get(clazz);
        return (null == result) ? clazz : result;
    }

    /**
     * 包装类转为原始类,非包装类返回原类
     *
     * @param clazz 包装类
     * @return 原始类
     */
    public static Class<?> unWrap(Class<?> clazz) {
        if (null == clazz || clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = WRAPPER_PRIMITIVE_MAP.get(clazz);
        return (null == result) ? clazz : result;
    }

}
