/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.text;


import org.aoju.bus.core.lang.exception.InstrumentException;

import java.util.Map;

/**
 * 查找字符串值的字符串键.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public abstract class StrLookup<V> {

    /**
     * 基于系统属性的查找.
     */
    private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP = new SystemPropertiesStrLookup();

    protected StrLookup() {
        super();
    }

    /**
     * 返回一个新的查找,该查找使用当前的副本
     * 如果安全管理器阻塞了对系统属性的访问,
     * 则null将阻塞每次查找都返回
     *
     * @return 使用系统属性返回查找, 而不是null
     */
    public static StrLookup<String> systemPropertiesLookup() {
        return SYSTEM_PROPERTIES_LOOKUP;
    }

    /**
     * 返回使用映射查找值的查找
     * 如果映射为null,那么每次查找都会返回null
     * 使用toString()将映射结果对象转换为字符串
     *
     * @param <V> 查找支持的值的类型
     * @param map 映射键值的映射,可以为空
     * @return 使用映射的查找, 而不是null
     */
    public static <V> StrLookup<V> mapLookup(final Map<String, V> map) {
        return new MapStrLookup<>(map);
    }

    /**
     * 查找字符串值的字符串键
     *
     * @param key 要查找的键可以为空
     * @return 匹配值, 如果没有匹配则为空
     */
    public abstract String lookup(String key);

    /**
     * 使用映射的查找实现.
     */
    static class MapStrLookup<V> extends StrLookup<V> {

        /**
         * Map键是变量名和值.
         */
        private final Map<String, V> map;

        /**
         * 创建由映射支持的新实例.
         *
         * @param map 键到值的映射可以为空
         */
        MapStrLookup(final Map<String, V> map) {
            this.map = map;
        }

        /**
         * 使用映射查找字符串值的字符串键
         * 如果映射为null,则返回null
         * 使用toString()将映射结果对象转换为字符串
         *
         * @param key 要查找的键可以为空
         * @return 匹配值, 如果没有匹配则为空
         */
        @Override
        public String lookup(final String key) {
            if (map == null) {
                return null;
            }
            final Object obj = map.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }

    /**
     * 基于系统属性的查找实现s.
     */
    private static class SystemPropertiesStrLookup extends StrLookup<String> {

        @Override
        public String lookup(String key) {
            if (key.length() > 0) {
                try {
                    return System.getProperty(key);
                } catch (SecurityException e) {
                    throw new InstrumentException(e);
                }
            }
            return null;
        }
    }

}
