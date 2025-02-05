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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;

/**
 * 转换工具类，提供集合、Map等向上向下转换工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CastKit {

    /**
     * 将指定对象强制转换为指定类型
     *
     * @param <T>   目标类型
     * @param value 被转换的对象
     * @return 转换后的对象
     */
    public static <T> T cast(final Object value) {
        return (T) value;
    }

    /**
     * 将指定对象强制转换为指定类型
     *
     * @param <T>        目标类型
     * @param targetType 指定目标类型
     * @param value      被转换的对象
     * @return 转换后的对象
     */
    public static <T> T castTo(final Class<T> targetType, final Object value) {
        return Assert.notNull(targetType).cast(value);
    }

    /**
     * 泛型集合向上转型。例如将Collection&lt;Integer&gt;转换为Collection&lt;Number&gt;
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 转换后的集合
     */
    public static <T> Collection<T> castUp(final Collection<? extends T> collection) {
        return (Collection<T>) collection;
    }

    /**
     * 泛型集合向下转型。例如将Collection&lt;Number&gt;转换为Collection&lt;Integer&gt;
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 转换后的集合
     */
    public static <T> Collection<T> castDown(final Collection<? super T> collection) {
        return (Collection<T>) collection;
    }

    /**
     * 泛型集合向上转型。例如将Set&lt;Integer&gt;转换为Set&lt;Number&gt;
     *
     * @param set 集合
     * @param <T> 泛型
     * @return 泛化集合
     */
    public static <T> Set<T> castUp(final Set<? extends T> set) {
        return (Set<T>) set;
    }

    /**
     * 泛型集合向下转型。例如将Set&lt;Number&gt;转换为Set&lt;Integer&gt;
     *
     * @param set 集合
     * @param <T> 泛型子类
     * @return 泛化集合
     */
    public static <T> Set<T> castDown(final Set<? super T> set) {
        return (Set<T>) set;
    }

    /**
     * 泛型接口向上转型。例如将List&lt;Integer&gt;转换为List&lt;Number&gt;
     *
     * @param list 集合
     * @param <T>  泛型的父类
     * @return 泛化集合
     */
    public static <T> List<T> castUp(final List<? extends T> list) {
        return (List<T>) list;
    }

    /**
     * 泛型集合向下转型。例如将List&lt;Number&gt;转换为List&lt;Integer&gt;
     *
     * @param list 集合
     * @param <T>  泛型的子类
     * @return 泛化集合
     */
    public static <T> List<T> castDown(final List<? super T> list) {
        return (List<T>) list;
    }

    /**
     * 泛型集合向下转型。例如将Map&lt;Integer, Integer&gt;转换为Map&lt;Number,Number&gt;
     *
     * @param map 集合
     * @param <K> 泛型父类
     * @param <V> 泛型父类
     * @return 泛化集合
     */
    public static <K, V> Map<K, V> castUp(final Map<? extends K, ? extends V> map) {
        return (Map<K, V>) map;
    }

    /**
     * 泛型集合向下转型。例如将Map&lt;Number,Number&gt;转换为Map&lt;Integer, Integer&gt;
     *
     * @param map 集合
     * @param <K> 泛型子类
     * @param <V> 泛型子类
     * @return 泛化集合
     */
    public static <K, V> Map<K, V> castDown(final Map<? super K, ? super V> map) {
        return (Map<K, V>) map;
    }

}
