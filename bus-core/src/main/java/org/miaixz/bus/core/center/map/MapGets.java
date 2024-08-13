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
package org.miaixz.bus.core.center.map;

import java.util.Date;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.reflect.TypeReference;

/**
 * Map的getXXX封装，提供针对通用型的value按照所需类型获取值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapGets extends MapValidator {

    /**
     * 获取Map指定key的值，并转换为字符串
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static String getString(final Map<?, ?> map, final Object key) {
        return get(map, key, String.class);
    }

    /**
     * 获取Map指定key的值，并转换为字符串
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static String getString(final Map<?, ?> map, final Object key, final String defaultValue) {
        return get(map, key, String.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Integer
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Integer getInt(final Map<?, ?> map, final Object key) {
        return get(map, key, Integer.class);
    }

    /**
     * 获取Map指定key的值，并转换为Integer
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Integer getInt(final Map<?, ?> map, final Object key, final Integer defaultValue) {
        return get(map, key, Integer.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Double
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Double getDouble(final Map<?, ?> map, final Object key) {
        return get(map, key, Double.class);
    }

    /**
     * 获取Map指定key的值，并转换为Double
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Double getDouble(final Map<?, ?> map, final Object key, final Double defaultValue) {
        return get(map, key, Double.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Float
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Float getFloat(final Map<?, ?> map, final Object key) {
        return get(map, key, Float.class);
    }

    /**
     * 获取Map指定key的值，并转换为Float
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Float getFloat(final Map<?, ?> map, final Object key, final Float defaultValue) {
        return get(map, key, Float.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Short
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Short getShort(final Map<?, ?> map, final Object key) {
        return get(map, key, Short.class);
    }

    /**
     * 获取Map指定key的值，并转换为Short
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Short getShort(final Map<?, ?> map, final Object key, final Short defaultValue) {
        return get(map, key, Short.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Bool
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Boolean getBoolean(final Map<?, ?> map, final Object key) {
        return get(map, key, Boolean.class);
    }

    /**
     * 获取Map指定key的值，并转换为Bool
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Boolean getBoolean(final Map<?, ?> map, final Object key, final Boolean defaultValue) {
        return get(map, key, Boolean.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Character
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Character getChar(final Map<?, ?> map, final Object key) {
        return get(map, key, Character.class);
    }

    /**
     * 获取Map指定key的值，并转换为Character
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Character getChar(final Map<?, ?> map, final Object key, final Character defaultValue) {
        return get(map, key, Character.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为Long
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Long getLong(final Map<?, ?> map, final Object key) {
        return get(map, key, Long.class);
    }

    /**
     * 获取Map指定key的值，并转换为Long
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Long getLong(final Map<?, ?> map, final Object key, final Long defaultValue) {
        return get(map, key, Long.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为{@link Date}
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Date getDate(final Map<?, ?> map, final Object key) {
        return get(map, key, Date.class);
    }

    /**
     * 获取Map指定key的值，并转换为{@link Date}
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Date getDate(final Map<?, ?> map, final Object key, final Date defaultValue) {
        return get(map, key, Date.class, defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>  目标值类型
     * @param map  Map
     * @param key  键
     * @param type 值类型
     * @return 值
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final Class<T> type) {
        return get(map, key, type, null);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final Class<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型，此方法在转换失败后不抛异常，返回null。
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T getQuietly(final Map<?, ?> map, final Object key, final Class<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>  目标值类型
     * @param map  Map
     * @param key  键
     * @param type 值类型
     * @return 值
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final TypeReference<T> type) {
        return get(map, key, type, null);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final TypeReference<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型，转换失败后返回null，不抛异常
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T getQuietly(final Map<?, ?> map, final Object key, final TypeReference<T> type,
            final T defaultValue) {
        return null == map ? defaultValue : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

}
