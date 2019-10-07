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
package org.aoju.bus.core.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基本变量类型的枚举
 * 基本类型枚举包括原始类型和包装类型
 *
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public enum BasicType {

    BYTE, SHORT, INT, INTEGER, LONG, DOUBLE, FLOAT, BOOLEAN, CHAR, CHARACTER, STRING;

    /**
     * 包装类型为Key，原始类型为Value，例如： Integer.class =》 int.class.
     */
    public static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new ConcurrentHashMap<>(8);
    /**
     * 原始类型为Key，包装类型为Value，例如： int.class =》 Integer.class.
     */
    public static final Map<Class<?>, Class<?>> primitiveWrapperMap = new ConcurrentHashMap<>(8);

    static {
        wrapperPrimitiveMap.put(Boolean.class, boolean.class);
        wrapperPrimitiveMap.put(Byte.class, byte.class);
        wrapperPrimitiveMap.put(Character.class, char.class);
        wrapperPrimitiveMap.put(Double.class, double.class);
        wrapperPrimitiveMap.put(Float.class, float.class);
        wrapperPrimitiveMap.put(Integer.class, int.class);
        wrapperPrimitiveMap.put(Long.class, long.class);
        wrapperPrimitiveMap.put(Short.class, short.class);

        for (Map.Entry<Class<?>, Class<?>> entry : wrapperPrimitiveMap.entrySet()) {
            primitiveWrapperMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 原始类转为包装类，非原始类返回原类
     *
     * @param clazz 原始类
     * @return 包装类
     */
    public static Class<?> wrap(Class<?> clazz) {
        if (null == clazz || false == clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = primitiveWrapperMap.get(clazz);
        return (null == result) ? clazz : result;
    }

    /**
     * 包装类转为原始类，非包装类返回原类
     *
     * @param clazz 包装类
     * @return 原始类
     */
    public static Class<?> unWrap(Class<?> clazz) {
        if (null == clazz || clazz.isPrimitive()) {
            return clazz;
        }
        Class<?> result = wrapperPrimitiveMap.get(clazz);
        return (null == result) ? clazz : result;
    }

}
