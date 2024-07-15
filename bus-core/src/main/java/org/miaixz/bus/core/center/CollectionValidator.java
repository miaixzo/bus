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
package org.miaixz.bus.core.center;

import org.miaixz.bus.core.center.array.ArrayValidator;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Collection检查工具类，提供字对象的blank和empty等检查
 * <ul>
 *     <li>empty定义：{@code null} or 空字对象：{@code ""}</li>
 *     <li>blank定义：{@code null} or 空字对象：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionValidator {

    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IteratorKit#isEmpty(Iterable)
     */
    public static boolean isEmpty(final Iterable<?> iterable) {
        return IteratorKit.isEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param iterator Iterator对象
     * @return 是否为空
     * @see IteratorKit#isEmpty(Iterator)
     */
    public static boolean isEmpty(final Iterator<?> iterator) {
        return IteratorKit.isEmpty(iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isEmpty(final Enumeration<?> enumeration) {
        return null == enumeration || !enumeration.hasMoreElements();
    }

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     * @see MapKit#isEmpty(Map)
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return MapKit.isEmpty(map);
    }

    /**
     * 如果给定集合为空，返回默认集合
     *
     * @param <T>               集合类型
     * @param <E>               集合元素类型
     * @param collection        集合
     * @param defaultCollection 默认数组
     * @return 非空（empty）的原集合或默认集合
     */
    public static <T extends Collection<E>, E> T defaultIfEmpty(final T collection, final T defaultCollection) {
        return isEmpty(collection) ? defaultCollection : collection;
    }

    /**
     * 如果给定集合为空，返回默认集合
     *
     * @param <T>             集合类型
     * @param <E>             集合元素类型
     * @param collection      集合
     * @param handler         非空的处理函数
     * @param defaultSupplier 默认值懒加载函数
     * @return 非空（empty）的原集合或默认集合
     */
    public static <T extends Collection<E>, E> T defaultIfEmpty(final T collection, final Function<T, T> handler, final Supplier<? extends T> defaultSupplier) {
        return isEmpty(collection) ? defaultSupplier.get() : handler.apply(collection);
    }

    /**
     * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合
     * 空集合使用{@link Collections#emptySet()}
     *
     * @param <T> 集合元素类型
     * @param set 提供的集合，可能为null
     * @return 原集合，若为null返回空集合
     */
    public static <T> Set<T> emptyIfNull(final Set<T> set) {
        return ObjectKit.defaultIfNull(set, Collections.emptySet());
    }

    /**
     * 如果提供的集合为{@code null}，返回一个不可变的默认空集合，否则返回原集合
     * 空集合使用{@link Collections#emptyList()}
     *
     * @param <T>  集合元素类型
     * @param list 提供的集合，可能为null
     * @return 原集合，若为null返回空集合
     */
    public static <T> List<T> emptyIfNull(final List<T> list) {
        return ObjectKit.defaultIfNull(list, Collections.emptyList());
    }

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IteratorKit#isNotEmpty(Iterable)
     */
    public static boolean isNotEmpty(final Iterable<?> iterable) {
        return IteratorKit.isNotEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param iterator Iterator对象
     * @return 是否为空
     * @see IteratorKit#isNotEmpty(Iterator)
     */
    public static boolean isNotEmpty(final Iterator<?> iterator) {
        return IteratorKit.isNotEmpty(iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isNotEmpty(final Enumeration<?> enumeration) {
        return null != enumeration && enumeration.hasMoreElements();
    }

    /**
     * 是否包含{@code null}元素
     * <ul>
     *     <li>集合为{@code null}，返回{@code true}</li>
     *     <li>集合为空集合，即元素个数为0，返回{@code false}</li>
     *     <li>集合中元素为""，返回{@code false}</li>
     * </ul>
     *
     * @param iterable 被检查的Iterable对象，如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     * @see IteratorKit#hasNull(Iterator)
     */
    public static boolean hasNull(final Iterable<?> iterable) {
        return IteratorKit.hasNull(IteratorKit.getIter(iterable));
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     * @see MapKit#isNotEmpty(Map)
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return MapKit.isNotEmpty(map);
    }

    /**
     * 判断subCollection是否为collection的子集合，不考虑顺序，只考虑元素数量。
     * <ul>
     *     <li>如果两个集合为同一集合或，则返回true</li>
     *     <li>如果两个集合元素都相同，则返回true（无论顺序相同与否）</li>
     * </ul>
     *
     * @param subCollection 第一个Iterable对象，即子集合。
     * @param collection    第二个Iterable对象，可以为任何实现了Iterable接口的集合。
     * @return 如果subCollection是collection的子集合，则返回true；否则返回false。
     */
    public static boolean isSub(final Collection<?> subCollection, final Collection<?> collection) {
        if (size(subCollection) > size(collection)) {
            return false;
        }
        return IteratorKit.isSub(subCollection, collection);
    }

    /**
     * 判断两个{@link Collection} 是否元素和顺序相同，返回{@code true}的条件是：
     * <ul>
     *     <li>两个{@link Collection}必须长度相同</li>
     *     <li>两个{@link Collection}元素相同index的对象必须equals，满足{@link Objects#equals(Object, Object)}</li>
     * </ul>
     *
     * @param list1 列表1
     * @param list2 列表2
     * @return 是否相同
     */
    public static boolean isEqualList(final Collection<?> list1, final Collection<?> list2) {
        return equals(list1, list2, false);
    }

    /**
     * 判断两个{@link Iterable}中的元素是否相同，可选是否判断顺序
     * 当满足下列情况时返回{@code true}：
     * <ul>
     *     <li>两个{@link Iterable}都为{@code null}；</li>
     *     <li>两个{@link Iterable}满足{@code coll1 == coll2}；</li>
     *     <li>如果忽略顺序，则计算两个集合中元素和数量是否相同</li>
     *     <li>如果不忽略顺序，两个{@link Iterable}所有具有相同下标的元素皆满足{@link Objects#equals(Object, Object)}；</li>
     * </ul>
     *
     * @param coll1       集合1
     * @param coll2       集合2
     * @param ignoreOrder 是否忽略顺序
     * @return 是否相同
     */
    public static boolean equals(final Collection<?> coll1, final Collection<?> coll2, final boolean ignoreOrder) {
        if (size(coll1) != size(coll2)) {
            return false;
        }

        return IteratorKit.equals(coll1, coll2, ignoreOrder);
    }

    /**
     * 获取Collection或者iterator的大小，此方法可以处理的对象类型如下：
     * <ul>
     *   <li>Collection - the collection size</li>
     *   <li>Map - the map size</li>
     *   <li>Array - the array size</li>
     *   <li>Iterator - the number of elements remaining in the iterator</li>
     *   <li>Enumeration - the number of elements remaining in the enumeration</li>
     * </ul>
     *
     * @param object 可以为空的对象
     * @return 如果object为空则返回0
     * @throws IllegalArgumentException 参数object不是Collection或者iterator
     */
    public static int size(final Object object) {
        if (object == null) {
            return 0;
        }

        // 优先判断使用频率较高的类型
        if (object instanceof Collection<?>) {
            return ((Collection<?>) object).size();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>) object).size();
        } else if (object instanceof Iterable<?>) {
            return IteratorKit.size((Iterable<?>) object);
        } else if (object instanceof Iterator<?>) {
            return IteratorKit.size((Iterator<?>) object);
        } else if (object instanceof Enumeration<?>) {
            int total = 0;
            final Enumeration<?> it = (Enumeration<?>) object;
            while (it.hasMoreElements()) {
                total++;
                it.nextElement();
            }
            return total;
        } else if (ArrayValidator.isArray(object)) {
            return ArrayValidator.length(object);
        } else {
            throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
        }
    }

}
