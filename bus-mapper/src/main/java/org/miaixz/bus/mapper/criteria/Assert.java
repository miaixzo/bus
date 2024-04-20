/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
package org.miaixz.bus.mapper.criteria;

import java.util.Collection;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Assert {

    public static void isTrue(boolean expression, String errorMsg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static void isNull(Object object, String errorMsg) throws IllegalArgumentException {
        if (object != null) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public static <T> T notNull(T object, String errorMsg) throws NullPointerException {
        if (object == null) {
            throw new NullPointerException(errorMsg);
        }
        return object;
    }

    public static String notEmpty(String text, String errorMsg) throws IllegalArgumentException {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException(errorMsg);
        }
        return text;
    }

    public static Object[] notEmpty(Object[] array, String errorMsg) throws IllegalArgumentException {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(errorMsg);
        }
        return array;
    }

    public static <T> Collection<T> notEmpty(Collection<T> collection, String errorMsg) throws IllegalArgumentException {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(errorMsg);
        }
        return collection;
    }

    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String errorMsg) throws IllegalArgumentException {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException(errorMsg);
        }
        return map;
    }

}
