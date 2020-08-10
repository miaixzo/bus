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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.collection.ArrayIterator;
import org.aoju.bus.core.collection.EnumerationIter;
import org.aoju.bus.core.collection.IteratorEnumeration;
import org.aoju.bus.core.compare.PropertyCompare;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.Matcher;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合相关工具类
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public class CollKit {

    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     * @see MapKit#isEmpty(Map)
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return MapKit.isEmpty(map);
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IterKit#isEmpty(Iterable)
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        return IterKit.isEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     * @see IterKit#isEmpty(Iterator)
     */
    public static boolean isEmpty(Iterator<?> Iterator) {
        return IterKit.isEmpty(Iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isEmpty(Enumeration<?> enumeration) {
        return null == enumeration || false == enumeration.hasMoreElements();
    }

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return false == isEmpty(collection);
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     * @see MapKit#isNotEmpty(Map)
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapKit.isNotEmpty(map);
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IterKit#isNotEmpty(Iterable)
     */
    public static boolean isNotEmpty(Iterable<?> iterable) {
        return IterKit.isNotEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     * @see IterKit#isNotEmpty(Iterator)
     */
    public static boolean isNotEmpty(Iterator<?> Iterator) {
        return IterKit.isNotEmpty(Iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isNotEmpty(Enumeration<?> enumeration) {
        return null != enumeration && enumeration.hasMoreElements();
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iterable 被检查的Iterable对象,如果为{@code null} 返回true
     * @return 是否包含{@code null}元素
     * @see IterKit#hasNull(Iterable)
     */
    public static boolean hasNull(Iterable<?> iterable) {
        return IterKit.hasNull(iterable);
    }

    /**
     * 两个集合的并集
     * 针对一个集合中存在多个相同元素的情况,计算两个集合中此元素的个数,保留最多的个数
     * 例如：集合1：[a, b, c, c, c],集合2：[a, b, c, c]
     * 结果：[a, b, c, c, c],此结果中只保留了三个c
     *
     * @param <T>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 并集的集合, 返回 {@link ArrayList}
     */
    public static <T> Collection<T> union(final Collection<T> coll1, final Collection<T> coll2) {
        final ArrayList<T> list = new ArrayList<>();
        if (isEmpty(coll1)) {
            list.addAll(coll2);
        } else if (isEmpty(coll2)) {
            list.addAll(coll1);
        } else {
            final Map<T, Integer> map1 = countMap(coll1);
            final Map<T, Integer> map2 = countMap(coll2);
            final Set<T> elts = newHashSet(coll2);
            elts.addAll(coll1);
            int m;
            for (T t : elts) {
                m = Math.max(Convert.toInt(map1.get(t), 0), Convert.toInt(map2.get(t), 0));
                for (int i = 0; i < m; i++) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * 多个集合的并集
     * 针对一个集合中存在多个相同元素的情况,计算两个集合中此元素的个数,保留最多的个数
     * 例如：集合1：[a, b, c, c, c],集合2：[a, b, c, c]
     * 结果：[a, b, c, c, c],此结果中只保留了三个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合, 返回 {@link ArrayList}
     */
    public static <T> Collection<T> union(final Collection<T> coll1, final Collection<T> coll2, final Collection<T>... otherColls) {
        Collection<T> union = union(coll1, coll2);
        for (Collection<T> coll : otherColls) {
            union = union(union, coll);
        }
        return union;
    }

    /**
     * 多个集合的非重复并集，类似于SQL中的“UNION DISTINCT”
     * 针对一个集合中存在多个相同元素的情况，只保留一个
     * 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c]
     * 结果：[a, b, c]，此结果中只保留了一个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合，返回 {@link LinkedHashSet}
     */
    public static <T> Set<T> unionDistinct(Collection<T> coll1, Collection<T> coll2, Collection<T>... otherColls) {
        final Set<T> result;
        if (isEmpty(coll1)) {
            result = new LinkedHashSet<>();
        } else {
            result = new LinkedHashSet<>(coll1);
        }

        if (isNotEmpty(coll2)) {
            result.addAll(coll2);
        }

        if (ArrayKit.isNotEmpty(otherColls)) {
            for (Collection<T> otherColl : otherColls) {
                result.addAll(otherColl);
            }
        }

        return result;
    }

    /**
     * 多个集合的完全并集，类似于SQL中的“UNION ALL”
     * 针对一个集合中存在多个相同元素的情况，保留全部元素
     * 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c]
     * 结果：[a, b, c, c, c, a, b, c, c]
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合，返回 {@link ArrayList}
     */
    public static <T> List<T> unionAll(Collection<T> coll1, Collection<T> coll2, Collection<T>... otherColls) {
        final List<T> result;
        if (isEmpty(coll1)) {
            result = new ArrayList<>();
        } else {
            result = new ArrayList<>(coll1);
        }

        if (isNotEmpty(coll2)) {
            result.addAll(coll2);
        }

        if (ArrayKit.isNotEmpty(otherColls)) {
            for (Collection<T> otherColl : otherColls) {
                result.addAll(otherColl);
            }
        }

        return result;
    }

    /**
     * 两个集合的交集
     * 针对一个集合中存在多个相同元素的情况,计算两个集合中此元素的个数,保留最少的个数
     * 例如：集合1：[a, b, c, c, c],集合2：[a, b, c, c]
     * 结果：[a, b, c, c],此结果中只保留了两个c
     *
     * @param <T>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 交集的集合, 返回 {@link ArrayList}
     */
    public static <T> Collection<T> intersection(final Collection<T> coll1, final Collection<T> coll2) {
        final List<T> list = new ArrayList<>();
        if (isNotEmpty(coll1) && isNotEmpty(coll2)) {
            final Map<T, Integer> map1 = countMap(coll1);
            final Map<T, Integer> map2 = countMap(coll2);
            final Set<T> elts = newHashSet(coll2);
            int m;
            for (T t : elts) {
                m = Math.min(Convert.toInt(map1.get(t), 0), Convert.toInt(map2.get(t), 0));
                for (int i = 0; i < m; i++) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * 多个集合的交集
     * 针对一个集合中存在多个相同元素的情况,计算两个集合中此元素的个数,保留最少的个数
     * 例如：集合1：[a, b, c, c, c],集合2：[a, b, c, c]
     * 结果：[a, b, c, c],此结果中只保留了两个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合, 返回 {@link ArrayList}
     */
    public static <T> Collection<T> intersection(final Collection<T> coll1, final Collection<T> coll2, final Collection<T>... otherColls) {
        Collection<T> intersection = intersection(coll1, coll2);
        if (isEmpty(intersection)) {
            return intersection;
        }
        for (Collection<T> coll : otherColls) {
            intersection = intersection(intersection, coll);
            if (isEmpty(intersection)) {
                return intersection;
            }
        }
        return intersection;
    }

    /**
     * 多个集合的交集
     * 针对一个集合中存在多个相同元素的情况，只保留一个
     * 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c]
     * 结果：[a, b, c],此结果中只保留了一个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合，返回 {@link LinkedHashSet}
     */
    public static <T> Set<T> intersectOne(Collection<T> coll1, Collection<T> coll2, Collection<T>... otherColls) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return new LinkedHashSet<>();
        }

        final Set<T> result = new LinkedHashSet<>(coll1);

        if (ArrayKit.isNotEmpty(otherColls)) {
            for (Collection<T> otherColl : otherColls) {
                if (isNotEmpty(otherColl)) {
                    result.retainAll(otherColl);
                } else {
                    return new LinkedHashSet<>();
                }
            }
        }
        result.retainAll(coll2);
        return result;
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
     *
     * <pre>
     *     subtract([1,2,3,4],[2,3,4,5]) -  [1]
     * </pre>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   元素类型
     * @return 单差集
     */
    public static <T> Collection<T> subtract(Collection<T> coll1, Collection<T> coll2) {
        final Collection<T> result = ObjectKit.clone(coll1);
        result.removeAll(coll2);
        return result;
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素
     * 例如：
     * <pre>
     *     subtractToList([1,2,3,4],[2,3,4,5]) - [1]
     * </pre>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   元素类型
     * @return 单差集
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2) {
        if (isEmpty(coll1)) {
            return Collections.emptyList();
        }
        if (isEmpty(coll2)) {
            if (null == coll1) {
                return new LinkedList<>();
            }
            return new ArrayList<>(coll1);
        }

        final List<T> result = new LinkedList<>();
        Set<T> set = new HashSet<>(coll2);
        for (T t : coll1) {
            if (false == set.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 两个集合的差集
     * 针对一个集合中存在多个相同元素的情况,计算两个集合中此元素的个数,保留两个集合中此元素个数差的个数
     * 例如：集合1：[a, b, c, c, c],集合2：[a, b, c, c]
     * 结果：[c],此结果中只保留了一个
     * 任意一个集合为空,返回另一个集合
     * 两个集合无交集则返回两个集合的组合
     *
     * @param <T>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 差集的集合, 返回 {@link ArrayList}
     */
    public static <T> Collection<T> disjunction(final Collection<T> coll1, final Collection<T> coll2) {
        if (isEmpty(coll1)) {
            return coll2;
        }
        if (isEmpty(coll2)) {
            return coll1;
        }

        final ArrayList<T> result = new ArrayList<>();
        final Map<T, Integer> map1 = countMap(coll1);
        final Map<T, Integer> map2 = countMap(coll2);
        final Set<T> elts = newHashSet(coll2);
        elts.addAll(coll1);
        int m;
        for (T t : elts) {
            m = Math.abs(Convert.toInt(map1.get(t), 0) - Convert.toInt(map2.get(t), 0));
            for (int i = 0; i < m; i++) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 检查给定数组是否包含给定元素
     *
     * @param array   数组要检查的数组
     * @param element 要查找的元素
     * @param <T>     通用标签
     * @return 如果集合为空(null或者空), 返回{@code false},否则找到元素返回{@code true}
     */
    public static <T> boolean contains(T[] array, final T element) {
        if (array == null) {
            return false;
        }
        return Arrays.stream(array).anyMatch(x -> ObjectKit.nullSafeEquals(x, element));
    }

    /**
     * 判断指定集合是否包含指定值,如果集合为空(null或者空),返回{@code false},否则找到元素返回{@code true}
     *
     * @param collection 集合
     * @param value      需要查找的值
     * @return 如果集合为空(null或者空), 返回{@code false},否则找到元素返回{@code true}
     */
    public static boolean contains(final Collection<?> collection, Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

    /**
     * 检查给定的迭代器是否包含给定的元素.
     *
     * @param iterator 要检查的迭代器
     * @param element  要查找的元素
     * @return {@code true} 如果找到, {@code false} 否则返回
     */
    public static boolean contains(final Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectKit.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查给定枚举是否包含给定元素.
     *
     * @param enumeration 要检查的枚举
     * @param element     要查找的元素
     * @return {@code true} 如果找到, {@code false} 否则返回
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectKit.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 自定义函数判断集合是否包含某类值
     *
     * @param collection  集合
     * @param containFunc 自定义判断函数
     * @param <T>         值类型
     * @return 是否包含自定义规则的值
     */
    public static <T> boolean contains(Collection<T> collection, Predicate<? super T> containFunc) {
        if (isEmpty(collection)) {
            return false;
        }
        for (T t : collection) {
            if (containFunc.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 其中一个集合在另一个集合中是否至少包含一个元素,既是两个集合是否至少有一个共同的元素
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 其中一个集合在另一个集合中是否至少包含一个元素
     * @see #intersection
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        if (coll1.size() < coll2.size()) {
            for (Object object : coll1) {
                if (coll2.contains(object)) {
                    return true;
                }
            }
        } else {
            for (Object object : coll2) {
                if (coll1.contains(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 集合1中是否包含集合2中所有的元素，即集合2是否为集合1的子集
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 集合1中是否包含集合2中所有的元素
     */
    public static boolean containsAll(Collection<?> coll1, Collection<?> coll2) {
        if (isEmpty(coll1)) {
            return isEmpty(coll2);
        }

        if (isEmpty(coll2)) {
            return true;
        }

        if (coll1.size() < coll2.size()) {
            return false;
        }

        for (Object object : coll2) {
            if (false == coll1.contains(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map}
     * 所谓元素计数就是假如这个集合中某个元素出现了n次,那将这个元素做为key,n做为value
     * 例如：[a,b,c,c,c] 得到：
     * a: 1
     * b: 1
     * c: 3
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link Map}
     * @see IterKit#countMap(Iterable)
     */
    public static <T> Map<T, Integer> countMap(Iterable<T> collection) {
        return IterKit.countMap(collection);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串
     * 如果集合元素为数组、{@link Iterable}或{@link Iterator},则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @return 连接后的字符串
     * @see IterKit#join(Iterable, CharSequence)
     */
    public static <T> String join(Iterable<T> iterable, CharSequence conjunction) {
        return IterKit.join(iterable, conjunction);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串
     * 如果集合元素为数组、{@link Iterable}或{@link Iterator},则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterator    集合
     * @param conjunction 分隔符
     * @return 连接后的字符串
     * @see IterKit#join(Iterator, CharSequence)
     */
    public static <T> String join(Iterator<T> iterator, CharSequence conjunction) {
        return IterKit.join(iterator, conjunction);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @param prefix      每个元素添加的前缀，null表示不添加
     * @param suffix      每个元素添加的后缀，null表示不添加
     * @return 连接后的字符串
     */
    public static <T> String join(Iterable<T> iterable, CharSequence conjunction, String prefix, String suffix) {
        if (null == iterable) {
            return null;
        }
        return IterKit.join(iterable.iterator(), conjunction, prefix, suffix);
    }

    /**
     * 切取部分数据
     * 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(Stack<T> surplusAlaDatas, int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return null;
        }

        final List<T> currentAlaDatas = new ArrayList<>();
        int size = surplusAlaDatas.size();
        // 切割
        if (size > partSize) {
            for (int i = 0; i < partSize; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        } else {
            for (int i = 0; i < size; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        }
        return currentAlaDatas;
    }

    /**
     * 切取部分数据
     * 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(Deque<T> surplusAlaDatas, int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return null;
        }

        final List<T> currentAlaDatas = new ArrayList<>();
        int size = surplusAlaDatas.size();
        // 切割
        if (size > partSize) {
            for (int i = 0; i < partSize; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        } else {
            for (int i = 0; i < size; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        }
        return currentAlaDatas;
    }

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     * @see MapKit#newHashMap()
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return MapKit.newHashMap();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param size    初始大小,由于默认负载因子0.75,传入的size会实际初始大小为size / 0.75
     * @param isOrder Map的Key是否有序,有序返回 {@link LinkedHashMap},否则返回 {@link HashMap}
     * @return HashMap对象
     * @see MapKit#newHashMap(int, boolean)
     */
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
        return MapKit.newHashMap(size, isOrder);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param size 初始大小,由于默认负载因子0.75,传入的size会实际初始大小为size / 0.75
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return MapKit.newHashMap(size);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param ts  元素数组
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(T... ts) {
        return newHashSet(false, ts);
    }

    /**
     * 新建一个LinkedHashSet
     *
     * @param <T> 集合元素类型
     * @param ts  元素数组
     * @return HashSet对象
     */
    public static <T> LinkedHashSet<T> newLinkedHashSet(T... ts) {
        return (LinkedHashSet<T>) newHashSet(true, ts);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>      集合元素类型
     * @param isSorted 是否有序,有序返回 {@link LinkedHashSet},否则返回 {@link HashSet}
     * @param ts       元素数组
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, T... ts) {
        if (null == ts) {
            return isSorted ? new LinkedHashSet<>() : new HashSet<>();
        }
        int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        HashSet<T> set = isSorted ? new LinkedHashSet<>(initialCapacity) : new HashSet<>(initialCapacity);
        for (T t : ts) {
            set.add(t);
        }
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(Collection<T> collection) {
        return newHashSet(false, collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param isSorted   是否有序,有序返回 {@link LinkedHashSet},否则返回{@link HashSet}
     * @param collection 集合,用于初始化Set
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Collection<T> collection) {
        return isSorted ? new LinkedHashSet<>(collection) : new HashSet<>(collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>      集合元素类型
     * @param isSorted 是否有序,有序返回 {@link LinkedHashSet},否则返回{@link HashSet}
     * @param iter     {@link Iterator}
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Iterator<T> iter) {
        if (null == iter) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<>() : new HashSet<>();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param isSorted   是否有序,有序返回 {@link LinkedHashSet},否则返回{@link HashSet}
     * @param enumration {@link Enumeration}
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Enumeration<T> enumration) {
        if (null == enumration) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<>() : new HashSet<>();
        while (enumration.hasMoreElements()) {
            set.add(enumration.nextElement());
        }
        return set;
    }

    /**
     * 新建一个空List
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @return List对象
     */
    public static <T> List<T> list(boolean isLinked) {
        return isLinked ? new LinkedList<>() : new ArrayList<>();
    }

    /**
     * 新建一个List
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param values   数组
     * @return List对象
     */
    public static <T> List<T> list(boolean isLinked, T... values) {
        if (ArrayKit.isEmpty(values)) {
            return list(isLinked);
        }
        List<T> arrayList = isLinked ? new LinkedList<>() : new ArrayList<>(values.length);
        for (T t : values) {
            arrayList.add(t);
        }
        return arrayList;
    }

    /**
     * 新建一个List
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param collection 集合
     * @return List对象
     */
    public static <T> List<T> list(boolean isLinked, Collection<T> collection) {
        if (null == collection) {
            return list(isLinked);
        }
        return isLinked ? new LinkedList<>(collection) : new ArrayList<>(collection);
    }

    /**
     * 新建一个List
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iterable {@link Iterable}
     * @return List对象
     */
    public static <T> List<T> list(boolean isLinked, Iterable<T> iterable) {
        if (null == iterable) {
            return list(isLinked);
        }
        return list(isLinked, iterable.iterator());
    }

    /**
     * 新建一个ArrayList
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iter     {@link Iterator}
     * @return ArrayList对象
     */
    public static <T> List<T> list(boolean isLinked, Iterator<T> iter) {
        final List<T> list = list(isLinked);
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * 新建一个List
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     */
    public static <T> List<T> list(boolean isLinked, Enumeration<T> enumration) {
        final List<T> list = list(isLinked);
        if (null != enumration) {
            while (enumration.hasMoreElements()) {
                list.add(enumration.nextElement());
            }
        }
        return list;
    }

    /**
     * 新建一个ArrayList
     *
     * @param <T>    集合元素类型
     * @param values 数组
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(T... values) {
        return (ArrayList<T>) list(false, values);
    }

    /**
     * 数组转为ArrayList
     *
     * @param <T>    集合元素类型
     * @param values 数组
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> toList(T... values) {
        return newArrayList(values);
    }

    /**
     * 新建一个ArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Collection<T> collection) {
        return (ArrayList<T>) list(false, collection);
    }

    /**
     * 新建一个ArrayList
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
        return (ArrayList<T>) list(false, iterable);
    }

    /**
     * 新建一个ArrayList
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>  集合元素类型
     * @param iter {@link Iterator}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Iterator<T> iter) {
        return (ArrayList<T>) list(false, iter);
    }

    /**
     * 新建一个ArrayList
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>        集合元素类型
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Enumeration<T> enumration) {
        return (ArrayList<T>) list(false, enumration);
    }

    /**
     * 新建LinkedList
     *
     * @param values 数组
     * @param <T>    类型
     * @return LinkedList
     */
    public static <T> LinkedList<T> newLinkedList(T... values) {
        return (LinkedList<T>) list(true, values);
    }

    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link CopyOnWriteArrayList}
     */
    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(Collection<T> collection) {
        return (null == collection) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(collection));
    }

    /**
     * 新建{@link BlockingQueue}
     * 在队列为空时,获取元素的线程会等待队列变为非空 当队列满时,存储元素的线程会等待队列可用
     *
     * @param <T>      对象
     * @param capacity 容量
     * @param isLinked 是否为链表形式
     * @return {@link BlockingQueue}
     */
    public static <T> BlockingQueue<T> newBlockingQueue(int capacity, boolean isLinked) {
        BlockingQueue<T> queue;
        if (isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }

    /**
     * 创建新的集合对象
     *
     * @param <T>            对象
     * @param collectionType 集合类型
     * @return 集合类型对应的实例
     */
    public static <T> Collection<T> create(Class<?> collectionType) {
        Collection<T> list;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            // 抽象集合默认使用ArrayList
            list = new ArrayList<>();
        }

        // Set
        else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>();
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>();
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>();
        } else if (collectionType.isAssignableFrom(EnumSet.class)) {
            list = (Collection<T>) EnumSet.noneOf((Class<Enum>) ClassKit.getTypeArgument(collectionType));
        }

        // List
        else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        }

        // Others,直接实例化
        else {
            try {
                list = (Collection<T>) ReflectKit.newInstance(collectionType);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
        return list;
    }

    /**
     * 创建Map
     * 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K>     map键类型
     * @param <V>     map值类型
     * @param mapType map类型
     * @return {@link Map}实例
     * @see MapKit#createMap(Class)
     */
    public static <K, V> Map<K, V> createMap(Class<?> mapType) {
        return MapKit.createMap(mapType);
    }

    /**
     * 去重集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link ArrayList}
     */
    public static <T> ArrayList<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        } else if (collection instanceof Set) {
            return new ArrayList<>(collection);
        } else {
            return new ArrayList<>(new LinkedHashSet<>(collection));
        }
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置(包含)
     * @param end   结束位置(不包含)
     * @return 截取后的数组, 当开始位置超过最大时, 返回空的List
     */
    public static <T> List<T> sub(List<T> list, int start, int end) {
        return sub(list, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置(包含)
     * @param end   结束位置(不包含)
     * @param step  步进
     * @return 截取后的数组, 当开始位置超过最大时, 返回空的List
     */
    public static <T> List<T> sub(List<T> list, int start, int end, int step) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return new ArrayList<>(0);
        }

        final int size = list.size();
        if (start < 0) {
            start += size;
        }
        if (end < 0) {
            end += size;
        }
        if (start == size) {
            return new ArrayList<>(0);
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > size) {
            if (start >= size) {
                return new ArrayList<>(0);
            }
            end = size;
        }

        if (step <= 1) {
            return list.subList(start, end);
        }

        final List<T> result = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * 截取集合的部分
     *
     * @param <T>        集合元素类型
     * @param collection 被截取的数组
     * @param start      开始位置(包含)
     * @param end        结束位置(不包含)
     * @return 截取后的数组, 当开始位置超过最大时, 返回null
     */
    public static <T> List<T> sub(Collection<T> collection, int start, int end) {
        return sub(collection, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置(包含)
     * @param end   结束位置(不包含)
     * @param step  步进
     * @return 截取后的数组, 当开始位置超过最大时, 返回空集合
     */
    public static <T> List<T> sub(Collection<T> list, int start, int end, int step) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        return sub(new ArrayList<>(list), start, end, step);
    }

    /**
     * 对集合按照指定长度分段,每一个段为单独的集合,返回这个集合的列表
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param size       每个段的长度
     * @return 分段列表
     */
    public static <T> List<List<T>> split(Collection<T> collection, int size) {
        final List<List<T>> result = new ArrayList<>();
        if (CollKit.isEmpty(collection)) {
            return result;
        }

        ArrayList<T> subList = new ArrayList<>(size);
        for (T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param editor     编辑器接口
     * @return 过滤后的数组
     */
    public static <T> Collection<T> filter(Collection<T> collection, Editor<T> editor) {
        Collection<T> collection2 = ObjectKit.clone(collection);
        try {
            collection2.clear();
        } catch (UnsupportedOperationException e) {
            // 克隆后的对象不支持清空,说明为不可变集合对象,使用默认的ArrayList保存结果
            collection2 = new ArrayList<>();
        }

        T modified;
        for (T t : collection) {
            modified = editor.edit(t);
            if (null != modified) {
                collection2.add(modified);
            }
        }
        return collection2;
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>    集合元素类型
     * @param list   集合
     * @param editor 编辑器接口
     * @return 过滤后的数组
     */
    public static <T> List<T> filter(List<T> list, Editor<T> editor) {
        final List<T> list2 = (list instanceof LinkedList) ? new LinkedList<>() : new ArrayList<>(list.size());
        T modified;
        for (T t : list) {
            modified = editor.edit(t);
            if (null != modified) {
                list2.add(modified);
            }
        }
        return list2;
    }

    /**
     * 过滤集合，此方法在原集合上直接修改
     * 通过实现Filter接口，完成元素的过滤，这个Filter实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，{@link Filter#accept(Object)}方法返回false的对象将被使用{@link Iterator#remove()}方法移除
     * </pre>
     *
     * @param <T>    集合类型
     * @param <E>    集合元素类型
     * @param iter   集合
     * @param filter 过滤器接口
     * @return 编辑后的集合
     */
    public static <T extends Iterable<E>, E> T filter(T iter, Filter<E> filter) {
        if (null == iter) {
            return null;
        }

        filter(iter.iterator(), filter);

        return iter;
    }

    /**
     * 过滤集合，此方法在原集合上直接修改
     * 通过实现Filter接口，完成元素的过滤，这个Filter实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，{@link Filter#accept(Object)}方法返回false的对象将被使用{@link Iterator#remove()}方法移除
     * </pre>
     *
     * @param <E>    集合元素类型
     * @param iter   集合
     * @param filter 过滤器接口
     * @return 编辑后的集合
     */
    public static <E> Iterator<E> filter(Iterator<E> iter, Filter<E> filter) {
        if (null == iter || null == filter) {
            return iter;
        }

        while (iter.hasNext()) {
            if (false == filter.accept(iter.next())) {
                iter.remove();
            }
        }
        return iter;
    }

    /**
     * 去除{@code null} 元素
     *
     * @param <T>        对象
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T> Collection<T> removeNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 去掉集合中的多个元素
     *
     * @param <T>         对象
     * @param collection  集合
     * @param elesRemoved 被去掉的元素数组
     * @return 原集合
     */
    public static <T> Collection<T> removeAny(Collection<T> collection, T... elesRemoved) {
        collection.removeAll(newHashSet(elesRemoved));
        return collection;
    }

    /**
     * 去除{@code null}或者"" 元素
     *
     * @param <T>        对象
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T extends CharSequence> Collection<T> removeEmpty(Collection<T> collection) {
        return filter(collection, (Filter<T>) t -> false == StringKit.isEmpty(t));
    }

    /**
     * 去除{@code null}或者""或者空白字符串 元素
     *
     * @param <T>        对象
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T extends CharSequence> Collection<T> removeBlank(Collection<T> collection) {
        return filter(collection, (Filter<T>) t -> false == StringKit.isBlank(t));
    }

    /**
     * 通过Editor抽取集合元素中的某些值返回为新列表
     * 例如提供的是一个Bean列表,通过Editor接口实现获取某个字段值,返回这个字段值组成的新列表
     *
     * @param collection 原集合
     * @param editor     编辑器
     * @return 抽取后的新列表
     */
    public static List<Object> extract(Iterable<?> collection, Editor<Object> editor) {
        return extract(collection, editor, false);
    }

    /**
     * 通过Editor抽取集合元素中的某些值返回为新列表
     * 例如提供的是一个Bean列表,通过Editor接口实现获取某个字段值,返回这个字段值组成的新列表
     *
     * @param collection 原集合
     * @param editor     编辑器
     * @param ignoreNull 是否忽略空值
     * @return 抽取后的新列表
     * @see #map(Iterable, Function, boolean)
     */
    public static List<Object> extract(Iterable<?> collection, Editor<Object> editor, boolean ignoreNull) {
        return map(collection, editor::edit, ignoreNull);
    }

    /**
     * 通过func自定义一个规则,此规则将原集合中的元素转换成新的元素,生成新的列表返回
     * 例如提供的是一个Bean列表,通过Function接口实现获取某个字段值,返回这个字段值组成的新列表
     *
     * @param <T>        输入类型
     * @param <R>        结果类型
     * @param collection 原集合
     * @param func       编辑函数
     * @param ignoreNull 是否忽略空值
     * @return 抽取后的新列表
     */
    public static <T, R> List<R> map(Iterable<T> collection, Function<T, R> func, boolean ignoreNull) {
        final List<R> fieldValueList = new ArrayList<>();
        if (null == collection) {
            return fieldValueList;
        }

        R value;
        for (T bean : collection) {
            value = func.apply(bean);
            if (null == value && ignoreNull) {
                continue;
            }
            fieldValueList.add(value);
        }
        return fieldValueList;
    }

    /**
     * 获取给定Bean列表中指定字段名对应字段值的列表
     * 列表元素支持Bean与Map
     *
     * @param collection Bean集合或Map集合
     * @param fieldName  字段名或map的键
     * @return 字段值列表
     */
    public static List<Object> getFieldValues(Iterable<?> collection, final String fieldName) {
        return extract(collection, bean -> {
            if (bean instanceof Map) {
                return ((Map<?, ?>) bean).get(fieldName);
            } else {
                return ReflectKit.getFieldValue(bean, fieldName);
            }
        });
    }

    /**
     * 查找第一个匹配元素对象
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param filter     过滤器,满足过滤条件的第一个元素将被返回
     * @return 满足过滤条件的第一个元素
     */
    public static <T> T findOne(Iterable<T> collection, Filter<T> filter) {
        if (null != collection) {
            for (T t : collection) {
                if (filter.accept(t)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * 查找第一个匹配元素对象
     * 如果集合元素是Map,则比对键和值是否相同,相同则返回
     * 如果为普通Bean,则通过反射比对元素字段名对应的字段值是否相同,相同则返回
     * 如果给定字段值参数是{@code null} 且元素对象中的字段值也为{@code null}则认为相同
     *
     * @param <T>        集合元素类型
     * @param collection 集合,集合元素可以是Bean或者Map
     * @param fieldName  集合元素对象的字段名或map的键
     * @param fieldValue 集合元素对象的字段值或map的值
     * @return 满足条件的第一个元素
     */
    public static <T> T findOneByField(Iterable<T> collection, final String fieldName, final Object fieldValue) {
        return findOne(collection, new Filter<T>() {
            @Override
            public boolean accept(T t) {
                if (t instanceof Map) {
                    final Map<?, ?> map = (Map<?, ?>) t;
                    final Object value = map.get(fieldName);
                    return ObjectKit.equal(value, fieldValue);
                }

                // 普通Bean
                final Object value = ReflectKit.getFieldValue(t, fieldName);
                return ObjectKit.equal(value, fieldValue);
            }
        });
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param editor 编辑器接口
     * @return 过滤后的Map
     * @see MapKit#filter(Map, Editor)
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Editor<Entry<K, V>> editor) {
        return MapKit.filter(map, editor);
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param filter 编辑器接口
     * @return 过滤后的Map
     * @see MapKit#filter(Map, Filter)
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Filter<Entry<K, V>> filter) {
        return MapKit.filter(map, filter);
    }

    /**
     * 集合中匹配规则的数量
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @param matcher  匹配器,为空则全部匹配
     * @return 匹配数量
     */
    public static <T> int count(Iterable<T> iterable, Matcher<T> matcher) {
        int count = 0;
        if (null != iterable) {
            for (T t : iterable) {
                if (null == matcher || matcher.match(t)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 映射键值(参考Python的zip()函数)
     * 例如：
     * keys = a,b,c,d
     * values = 1,2,3,4
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同,则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @param isOrder   是否有序
     * @return Map
     */
    public static Map<String, String> zip(String keys, String values, String delimiter, boolean isOrder) {
        return ArrayKit.zip(StringKit.split(keys, delimiter), StringKit.split(values, delimiter), isOrder);
    }

    /**
     * 映射键值(参考Python的zip()函数),返回Map无序
     * 例如：
     * keys = a,b,c,d
     * values = 1,2,3,4
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同,则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @return Map
     */
    public static Map<String, String> zip(String keys, String values, String delimiter) {
        return zip(keys, values, delimiter, false);
    }

    /**
     * 映射键值(参考Python的zip()函数)
     * 例如：
     * keys = [a,b,c,d]
     * values = [1,2,3,4]
     * 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同,则只对应最短部分
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return Map
     */
    public static <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
        if (isEmpty(keys) || isEmpty(values)) {
            return null;
        }

        final List<K> keyList = new ArrayList<>(keys);
        final List<V> valueList = new ArrayList<>(values);

        final int size = Math.min(keys.size(), values.size());
        final Map<K, V> map = new HashMap<>((int) (size / 0.75));
        for (int i = 0; i < size; i++) {
            map.put(keyList.get(i), valueList.get(i));
        }

        return map;
    }

    /**
     * 将Entry集合转换为HashMap
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param entryIter entry集合
     * @return Map
     * @see IterKit#toMap(Iterable)
     */
    public static <K, V> HashMap<K, V> toMap(Iterable<Entry<K, V>> entryIter) {
        return IterKit.toMap(entryIter);
    }

    /**
     * 将数组转换为Map(HashMap),支持数组元素类型为：
     *
     * <pre>
     * Map.Entry
     * 长度大于1的数组(取前两个值),如果不满足跳过此元素
     * Iterable 长度也必须大于1(取前两个值),如果不满足跳过此元素
     * Iterator 长度也必须大于1(取前两个值),如果不满足跳过此元素
     * </pre>
     *
     * <pre>
     * Map&lt;Object, Object&gt; colorMap = toMap(new String[][] {{
     *     {"RED", "#FF0000"},
     *     {"GREEN", "#00FF00"},
     *     {"BLUE", "#0000FF"}});
     * </pre>
     * <p>
     * 参考：commons-lang
     *
     * @param array 数组 元素类型为Map.Entry、数组、Iterable、Iterator
     * @return {@link HashMap}
     * @see MapKit#of(Object[])
     */
    public static HashMap<Object, Object> toMap(Object[] array) {
        return MapKit.of(array);
    }

    /**
     * 将集合转换为排序后的TreeSet
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> TreeSet<T> toTreeSet(Collection<T> collection, Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<>(comparator);
        for (T t : collection) {
            treeSet.add(t);
        }
        return treeSet;
    }

    /**
     * Iterator转换为Enumeration
     * <p>
     * Adapt the specified <code>Iterator</code> to the <code>Enumeration</code> interface.
     *
     * @param <E>  集合元素类型
     * @param iter {@link Iterator}
     * @return {@link Enumeration}
     */
    public static <E> Enumeration<E> asEnumeration(Iterator<E> iter) {
        return new IteratorEnumeration<>(iter);
    }

    /**
     * Enumeration转换为Iterator
     * <p>
     * Adapt the specified <code>Enumeration</code> to the <code>Iterator</code> interface
     *
     * @param <E> 集合元素类型
     * @param e   {@link Enumeration}
     * @return {@link Iterator}
     * @see IterKit#asIterator(Enumeration)
     */
    public static <E> Iterator<E> asIterator(Enumeration<E> e) {
        return IterKit.asIterator(e);
    }

    /**
     * {@link Iterator} 转为 {@link Iterable}
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return {@link Iterable}
     * @see IterKit#asIterable(Iterator)
     */
    public static <E> Iterable<E> asIterable(final Iterator<E> iter) {
        return IterKit.asIterable(iter);
    }

    /**
     * {@link Iterable}转为{@link Collection}
     * 首先尝试强转,强转失败则构建一个新的{@link ArrayList}
     *
     * @param <E>      集合元素类型
     * @param iterable {@link Iterable}
     * @return {@link Collection} 或者 {@link ArrayList}
     */
    public static <E> Collection<E> toCollection(Iterable<E> iterable) {
        return (iterable instanceof Collection) ? (Collection<E>) iterable : newArrayList(iterable.iterator());
    }

    /**
     * 行转列,合并相同的键,值合并为列表
     * 将Map列表中相同key的值组成列表做为Map的value
     * 是{@link #toMapList(Map)}的逆方法
     * 比如传入数据：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param mapList Map列表
     * @return Map
     * @see MapKit#toListMap(Iterable)
     */
    public static <K, V> Map<K, List<V>> toListMap(Iterable<? extends Map<K, V>> mapList) {
        return MapKit.toListMap(mapList);
    }

    /**
     * 列转行 将Map中值列表分别按照其位置与key组成新的map
     * 是{@link #toListMap(Iterable)}的逆方法
     * 比如传入数据：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param listMap 列表Map
     * @return Map列表
     * @see MapKit#toMapList(Map)
     */
    public static <K, V> List<Map<K, V>> toMapList(Map<K, ? extends Iterable<V>> listMap) {
        return MapKit.toMapList(listMap);
    }

    /**
     * 将指定对象全部加入到集合中
     * 提供的对象如果为集合类型,会自动转换为目标元素类型
     *
     * @param <T>        元素类型
     * @param collection 被加入的集合
     * @param value      对象,可能为Iterator、Iterable、Enumeration、Array
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Object value) {
        return addAll(collection, value, TypeKit.getTypeArgument(collection.getClass()));
    }

    /**
     * 将指定对象全部加入到集合中
     * 提供的对象如果为集合类型,会自动转换为目标元素类型
     *
     * @param <T>         元素类型
     * @param collection  被加入的集合
     * @param value       对象,可能为Iterator、Iterable、Enumeration、Array,或者与集合元素类型一致
     * @param elementType 元素类型,为空时,使用Object类型来接纳所有类型
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Object value, Type elementType) {
        if (null == collection || null == value) {
            return collection;
        }
        if (TypeKit.isUnknow(elementType)) {
            // 元素类型为空时，使用Object类型来接纳所有类型
            elementType = Object.class;
        }

        Iterator iter;
        if (value instanceof Iterator) {
            iter = (Iterator) value;
        } else if (value instanceof Iterable) {
            iter = ((Iterable) value).iterator();
        } else if (value instanceof Enumeration) {
            iter = new EnumerationIter<>((Enumeration) value);
        } else if (ArrayKit.isArray(value)) {
            iter = new ArrayIterator<>(value);
        } else if (value instanceof CharSequence) {
            // String按照逗号分隔的列表对待
            final String ArrayStr = StringKit.unWrap((CharSequence) value, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT);
            iter = StringKit.splitTrim(ArrayStr, Symbol.C_COMMA).iterator();
        } else {
            // 其它类型按照单一元素处理
            iter = CollKit.newArrayList(value).iterator();
        }

        final ConverterRegistry convert = ConverterRegistry.getInstance();
        while (iter.hasNext()) {
            collection.add(convert.convert(elementType, iter.next()));
        }

        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterator   要加入的{@link Iterator}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterator<T> iterator) {
        if (null != collection && null != iterator) {
            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterable   要加入的内容{@link Iterable}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterable<T> iterable) {
        return addAll(collection, iterable.iterator());
    }

    /**
     * 加入全部
     *
     * @param <T>         集合元素类型
     * @param collection  被加入的集合 {@link Collection}
     * @param enumeration 要加入的内容{@link Enumeration}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Enumeration<T> enumeration) {
        if (null != collection && null != enumeration) {
            while (enumeration.hasMoreElements()) {
                collection.add(enumeration.nextElement());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param values     要加入的内容数组
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, T[] values) {
        if (null != collection && null != values) {
            for (T value : values) {
                collection.add(value);
            }
        }
        return collection;
    }

    /**
     * 将字符串加入List中
     *
     * @param list        列表
     * @param part        被加入的部分
     * @param isTrim      是否去除两端空白符
     * @param ignoreEmpty 是否略过空字符串(空字符串不做为一个元素)
     * @return 列表
     */
    public static List<String> addAll(List<String> list, String part, boolean isTrim, boolean ignoreEmpty) {
        if (isTrim) {
            part = StringKit.trim(part);
        }
        if (false == ignoreEmpty || false == part.isEmpty()) {
            list.add(part);
        }
        return list;
    }

    /**
     * 将另一个列表中的元素加入到列表中,如果列表中已经存在此元素则忽略之
     *
     * @param <T>       集合元素类型
     * @param list      列表
     * @param otherList 其它列表
     * @return 此列表
     */
    public static <T> List<T> addAllIfNotContains(List<T> list, List<T> otherList) {
        for (T t : otherList) {
            if (false == list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 获取集合中指定下标的元素值,下标可以为负数,例如-1表示最后一个元素
     * 如果元素越界,返回null
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param index      下标,支持负数
     * @return 元素值
     */
    public static <T> T get(Collection<T> collection, int index) {
        if (null == collection) {
            return null;
        }

        final int size = collection.size();
        if (0 == size) {
            return null;
        }

        if (index < 0) {
            index += size;
        }

        // 检查越界
        if (index >= size) {
            return null;
        }

        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            return list.get(index);
        } else {
            int i = 0;
            for (T t : collection) {
                if (i > index) {
                    break;
                } else if (i == index) {
                    return t;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * 获取集合中指定多个下标的元素值,下标可以为负数,例如-1表示最后一个元素
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param indexes    下标,支持负数
     * @return 元素值列表
     */
    public static <T> List<T> getAny(Collection<T> collection, int... indexes) {
        final int size = collection.size();
        final ArrayList<T> result = new ArrayList<>();
        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add(list.get(index));
            }
        } else {
            Object[] array = collection.toArray();
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add((T) array[index]);
            }
        }
        return result;
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return 第一个元素
     * @see IterKit#getFirst(Iterable)
     */
    public static <T> T getFirst(Iterable<T> iterable) {
        return IterKit.getFirst(iterable);
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return 第一个元素
     * @see IterKit#getFirst(Iterator)
     */
    public static <T> T getFirst(Iterator<T> iterator) {
        return IterKit.getFirst(iterator);
    }

    /**
     * 获取集合的最后一个元素
     *
     * @param <T>        集合元素类型
     * @param collection {@link Collection}
     * @return 最后一个元素
     */
    public static <T> T getLast(Collection<T> collection) {
        return get(collection, -1);
    }

    /**
     * 获得{@link Iterable}对象的元素类型(通过第一个非空元素判断)
     *
     * @param iterable {@link Iterable}
     * @return 元素类型, 当列表为空或元素全部为null时, 返回null
     * @see IterKit#getElementType(Iterable)
     */
    public static Class<?> getElementType(Iterable<?> iterable) {
        return IterKit.getElementType(iterable);
    }

    /**
     * 获得{@link Iterator}对象的元素类型(通过第一个非空元素判断)
     *
     * @param iterator {@link Iterator}
     * @return 元素类型, 当列表为空或元素全部为null时, 返回null
     * @see IterKit#getElementType(Iterator)
     */
    public static Class<?> getElementType(Iterator<?> iterator) {
        return IterKit.getElementType(iterator);
    }

    /**
     * 从Map中获取指定键列表对应的值列表
     * 如果key在map中不存在或key对应值为null,则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, K... keys) {
        final ArrayList<V> values = new ArrayList<>();
        for (K k : keys) {
            values.add(map.get(k));
        }
        return values;
    }

    /**
     * 从Map中获取指定键列表对应的值列表
     * 如果key在map中不存在或key对应值为null,则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterable<K> keys) {
        return valuesOfKeys(map, keys.iterator());
    }

    /**
     * 从Map中获取指定键列表对应的值列表
     * 如果key在map中不存在或key对应值为null,则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterator<K> keys) {
        final ArrayList<V> values = new ArrayList<>();
        while (keys.hasNext()) {
            values.add(map.get(keys.next()));
        }
        return values;
    }

    /**
     * 对指定List分页取值
     *
     * @param <T>      集合元素类型
     * @param pageNo   页码,从1开始计数,0和1效果相同
     * @param pageSize 每页的条目数
     * @param list     列表
     * @return 分页后的段落内容
     */
    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        if (isEmpty(list)) {
            return new ArrayList<>(0);
        }

        int resultSize = list.size();
        // 每页条目数大于总数直接返回所有
        if (resultSize <= pageSize) {
            if (pageNo <= 1) {
                return Collections.unmodifiableList(list);
            } else {
                // 越界直接返回空
                return new ArrayList<>(0);
            }
        }
        final int[] startEnd = transToStartEnd(pageNo, pageSize);
        if (startEnd[1] > resultSize) {
            startEnd[1] = resultSize;
        }
        return list.subList(startEnd[0], startEnd[1]);
    }

    /**
     * 对2个对象按照指定属性名称进行排序
     *
     * @param name 属性名称
     * @param asc  true升序,false降序
     * @param a    对象
     * @param b    对象
     * @return the object
     * @throws Exception 异常
     */
    private static <E> int sort(final String name, final boolean asc, E a, E b) throws Exception {

        Object value1 = forceGetFieldValue(a, name);
        Object value2 = forceGetFieldValue(b, name);
        String str1 = value1.toString();
        String str2 = value2.toString();
        if (value1 instanceof Number && value2 instanceof Number) {
            int maxlen = Math.max(str1.length(), str2.length());
            str1 = MathKit.addZero((Number) value1, maxlen);
            str2 = MathKit.addZero((Number) value2, maxlen);
        } else if (value1 instanceof Date && value2 instanceof Date) {
            long time1 = ((Date) value1).getTime();
            long time2 = ((Date) value2).getTime();
            int maxlen = Long.toString(Math.max(time1, time2)).length();
            str1 = MathKit.addZero(time1, maxlen);
            str2 = MathKit.addZero(time2, maxlen);
        }
        if (asc) {
            return str1.compareTo(str2);
        }
        return str2.compareTo(str1);
    }

    /**
     * 排序集合,排序不会修改原集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<? super T> comparator) {
        List<T> list = new ArrayList<>(collection);
        list.sort(comparator);
        return list;
    }

    /**
     * 针对List排序,排序会修改原List
     *
     * @param <T>  元素类型
     * @param list 被排序的List
     * @param c    {@link Comparator}
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(List<T> list, Comparator<? super T> c) {
        list.sort(c);
        return list;
    }

    /**
     * 排序Map
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        Map
     * @param comparator Entry比较器
     * @return {@link TreeMap}
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
        final TreeMap<K, V> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * 对list的元素按照多个属性名称排序,
     * list元素的属性可以是数字(byte、short、int、long、float、double等,支持正数、负数、0)、char、String、java.util.Date
     *
     * @param <E>  对象
     * @param list 集合
     * @param name list元素的属性名称
     * @param asc  true升序,false降序
     */
    public static <E> void sort(List<E> list, final boolean asc, final String... name) {
        Collections.sort(list, (a, b) -> {
            int ret = 0;
            try {
                for (int i = 0; i < name.length; i++) {
                    ret = sort(name[i], asc, a, b);
                    if (0 != ret) {
                        break;
                    }
                }
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
            return ret;
        });
    }

    /**
     * 给list的每个属性都指定是升序还是降序
     *
     * @param <E>  对象
     * @param list 集合
     * @param name 参数数组
     * @param type 每个属性对应的升降序数组, true升序,false降序
     */
    public static <E> void sort(List<E> list, final String[] name, final boolean[] type) {
        if (name.length != type.length) {
            throw new RuntimeException("属性数组元素个数和升降序数组元素个数不相等");
        }
        Collections.sort(list, (a, b) -> {
            int ret = 0;
            try {
                for (int i = 0; i < name.length; i++) {
                    ret = sort(name[i], type[i], a, b);
                    if (0 != ret) {
                        break;
                    }
                }
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
            return ret;
        });
    }

    /**
     * 将多个集合排序并显示不同的段落(分页)
     * 实现分页取局部
     *
     * @param <T>        集合元素类型
     * @param pageNo     页码,从1开始计数,0和1效果相同
     * @param pageSize   每页的条目数
     * @param comparator 比较器
     * @param colls      集合数组
     * @return 分页后的段落内容
     */
    public static <T> List<T> sortPageAll(int pageNo, int pageSize, Comparator<T> comparator, Collection<T>... colls) {
        final List<T> list = new ArrayList<>(pageNo * pageSize);
        for (Collection<T> coll : colls) {
            list.addAll(coll);
        }
        if (null != comparator) {
            list.sort(comparator);
        }

        return page(pageNo, pageSize, list);
    }

    /**
     * 根据Bean的属性排序
     *
     * @param <T>        元素类型
     * @param collection 集合，会被转换为List
     * @param property   属性名
     * @return 排序后的List
     */
    public static <T> List<T> sortByProperty(Collection<T> collection, String property) {
        return sort(collection, new PropertyCompare<>(property));
    }

    /**
     * 根据Bean的属性排序
     *
     * @param <T>      元素类型
     * @param list     List
     * @param property 属性名
     * @return 排序后的List
     */
    public static <T> List<T> sortByProperty(List<T> list, String property) {
        return sort(list, new PropertyCompare<>(property));
    }

    /**
     * 通过Entry排序,可以按照键排序,也可以按照值排序,亦或者两者综合排序
     *
     * @param <K>             键类型
     * @param <V>             值类型
     * @param entryCollection Entry集合
     * @param comparator      {@link Comparator}
     * @return {@link LinkedList}
     */
    public static <K, V> LinkedHashMap<K, V> sortToMap(Collection<Entry<K, V>> entryCollection, Comparator<Entry<K, V>> comparator) {
        List<Entry<K, V>> list = new LinkedList<>(entryCollection);
        list.sort(comparator);

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 通过Entry排序,可以按照键排序,也可以按照值排序,亦或者两者综合排序
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        被排序的Map
     * @param comparator {@link Comparator}
     * @return {@link LinkedList}
     */
    public static <K, V> LinkedHashMap<K, V> sortByEntry(Map<K, V> map, Comparator<Entry<K, V>> comparator) {
        return sortToMap(map.entrySet(), comparator);
    }

    /**
     * 将Set排序(根据Entry的值)
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param collection 被排序的{@link Collection}
     * @return 排序后的Set
     */
    public static <K, V> List<Entry<K, V>> sortEntryToList(Collection<Entry<K, V>> collection) {
        List<Entry<K, V>> list = new LinkedList<>(collection);
        list.sort((o1, o2) -> {
            V v1 = o1.getValue();
            V v2 = o2.getValue();

            if (v1 instanceof Comparable) {
                return ((Comparable) v1).compareTo(v2);
            } else {
                return v1.toString().compareTo(v2.toString());
            }
        });
        return list;
    }

    /**
     * 分组,按照{@link Hash}接口定义的hash算法,集合中的元素放入hash值对应的子列表中
     *
     * @param <T>        元素类型
     * @param collection 被分组的集合
     * @param hash       Hash值算法,决定元素放在第几个分组的规则
     * @return 分组后的集合
     */
    public static <T> List<List<T>> group(Collection<T> collection, Hash<T> hash) {
        final List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        if (null == hash) {
            // 默认hash算法,按照元素的hashCode分组
            hash = t -> null == t ? 0 : t.hashCode();
        }

        int index;
        List<T> subList;
        for (T t : collection) {
            index = hash.hash(t);
            if (result.size() - 1 < index) {
                while (result.size() - 1 < index) {
                    result.add(null);
                }
                result.set(index, newArrayList(t));
            } else {
                subList = result.get(index);
                if (null == subList) {
                    result.set(index, newArrayList(t));
                } else {
                    subList.add(t);
                }
            }
        }
        return result;
    }

    /**
     * 根据元素的指定字段名分组，非Bean都放在第一个分组中
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param fieldName  元素Bean中的字段名，非Bean都放在第一个分组中
     * @return 分组列表
     */
    public static <T> List<List<T>> groupByField(Collection<T> collection, final String fieldName) {
        return group(collection, new Hash<T>() {
            private final List<Object> fieldNameList = new ArrayList<>();

            @Override
            public int hash(T t) {
                if (null == t || false == BeanKit.isBean(t.getClass())) {
                    // 非Bean放在同一子分组中
                    return 0;
                }
                final Object value = ReflectKit.getFieldValue(t, fieldName);
                int hash = fieldNameList.indexOf(value);
                if (hash < 0) {
                    fieldNameList.add(value);
                    return fieldNameList.size() - 1;
                } else {
                    return hash;
                }
            }
        });
    }

    /**
     * 循环遍历 {@link Iterator},使用{@link Consumer} 接受遍历的每条数据,并针对每条数据做处理
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @param consumer {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Iterator<T> iterator, Consumer<T> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(iterator.next(), index);
            index++;
        }
    }

    /**
     * 循环遍历 {@link Enumeration},使用{@link Consumer} 接受遍历的每条数据,并针对每条数据做处理
     *
     * @param <T>         集合元素类型
     * @param enumeration {@link Enumeration}
     * @param consumer    {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Enumeration<T> enumeration, Consumer<T> consumer) {
        int index = 0;
        while (enumeration.hasMoreElements()) {
            consumer.accept(enumeration.nextElement(), index);
            index++;
        }
    }

    /**
     * 循环遍历Map,使用{@link KVConsumer} 接受遍历的每条数据,并针对每条数据做处理
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        {@link Map}
     * @param kvConsumer {@link KVConsumer} 遍历的每条数据处理器
     */
    public static <K, V> void forEach(Map<K, V> map, KVConsumer<K, V> kvConsumer) {
        int index = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            kvConsumer.accept(entry.getKey(), entry.getValue(), index);
            index++;
        }
    }

    /**
     * 反序给定List,会在原List基础上直接修改
     *
     * @param <T>  元素类型
     * @param list 被反转的List
     * @return 反转后的List
     */
    public static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    /**
     * 反序给定List,会创建一个新的List,原List数据不变
     *
     * @param <T>   元素类型
     * @param list  被反转的List
     * @param clone 是否克隆
     * @return 反转后的List
     */
    public static <T> List<T> reverse(List<T> list, boolean clone) {
        if (!clone) {
            return reverse(list);
        }
        final List<T> list2 = ObjectKit.clone(list);
        return reverse(list2);
    }

    /**
     * 获取指定对象的指定属性值(去除private,protected的限制)
     *
     * @param obj       属性名称所在的对象
     * @param fieldName 属性名称
     * @return the object
     * @throws Exception 异常
     */
    public static Object forceGetFieldValue(Object obj, String fieldName) throws Exception {
        Field field = ClassKit.getField(obj.getClass(), fieldName);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            // 如果是private,protected修饰的属性,需要修改为可以访问的
            field.setAccessible(true);
            obj = field.get(obj);
            // 还原private,protected属性的访问性质
            field.setAccessible(accessible);
            return obj;
        }
        return field.get(obj);
    }

    /**
     * 设置或增加元素 当index小于List的长度时,替换指定位置的值,否则在尾部追加
     *
     * @param <T>     对象
     * @param list    List列表
     * @param index   位置
     * @param element 新元素
     * @return 原List
     */
    public static <T> List<T> setOrAppend(List<T> list, int index, T element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            list.add(element);
        }
        return list;
    }

    /**
     * 将页数和每页条目数转换为开始位置和结束位置
     * 此方法用于不包括结束位置的分页方法
     * 例如：
     * 页码：1,每页10 =  [0, 10]
     * 页码：2,每页10 =  [10, 20]
     *
     * @param pageNo   页码(从1计数)
     * @param pageSize 每页条目数
     * @return 第一个数为开始位置, 第二个数为结束位置
     */
    public static int[] transToStartEnd(int pageNo, int pageSize) {
        if (pageNo < 1) {
            pageNo = 1;
        }

        if (pageSize < 1) {
            pageSize = 0;
        }

        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize;

        return new int[]{start, end};
    }

    /**
     * 根据总数计算总页数
     *
     * @param totalCount 总数
     * @param pageSize   每页数
     * @return 总页数
     */
    public static int totalPage(int totalCount, int pageSize) {
        if (pageSize == 0) {
            return 0;
        }
        return totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
    }

    /**
     * 分页彩虹算法
     * 通过传入的信息,生成一个分页列表显示
     *
     * @param currentPage  当前页
     * @param pageCount    总页数
     * @param displayCount 每屏展示的页数
     * @return 分页条
     */
    public static int[] rainbow(int currentPage, int pageCount, int displayCount) {
        boolean isEven = displayCount % 2 == 0;
        int left = displayCount / 2;
        int right = displayCount / 2;

        int length = displayCount;
        if (isEven) {
            right++;
        }
        if (pageCount < displayCount) {
            length = pageCount;
        }
        int[] result = new int[length];
        if (pageCount >= displayCount) {
            if (currentPage <= left) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + 1;
                }
            } else if (currentPage > pageCount - right) {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + pageCount - displayCount + 1;
                }
            } else {
                for (int i = 0; i < result.length; i++) {
                    result[i] = i + currentPage - left + (isEven ? 1 : 0);
                }
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = i + 1;
            }
        }
        return result;

    }

    /**
     * 分页彩虹算法(默认展示10页)
     *
     * @param currentPage 当前页
     * @param pageCount   总页数
     * @return 分页条
     */
    public static int[] rainbow(int currentPage, int pageCount) {
        return rainbow(currentPage, pageCount, 10);
    }

    /**
     * 找到第一个不为 null 的元素
     *
     * @param list 列表
     * @param <T>  泛型
     * @return 不为 null 的元素
     */
    public static <T> Optional<T> firstNotNullElem(Collection<T> list) {
        if (isEmpty(list)) {
            return Optional.empty();
        }

        for (T elem : list) {
            if (ObjectKit.isNotNull(elem)) {
                return Optional.of(elem);
            }
        }
        return Optional.empty();
    }

    /**
     * Concatenates 2 arrays
     *
     * @param <T>   对象
     * @param one   数组1
     * @param other 数组2
     * @param clazz 数组类
     * @return 新数组
     */
    public static <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        T[] target = (T[]) Array.newInstance(clazz, one.length + other.length);
        System.arraycopy(one, 0, target, 0, one.length);
        System.arraycopy(other, 0, target, one.length, other.length);
        return target;
    }

    /**
     * 不可变 Set
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> Set<E> ofImmutableSet(E... es) {
        Objects.requireNonNull(es, "args es is null.");
        return Arrays.stream(es).collect(Collectors.toSet());
    }

    /**
     * 不可变 List
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> List<E> ofImmutableList(E... es) {
        Objects.requireNonNull(es, "args es is null.");
        return Arrays.stream(es).collect(Collectors.toList());
    }

    /**
     * 获取指定Map列表中所有的Key
     *
     * @param <K>           键类型
     * @param mapCollection Map列表
     * @return key集合
     */
    public static <K> Set<K> keySet(Collection<Map<K, ?>> mapCollection) {
        if (isEmpty(mapCollection)) {
            return new HashSet<>();
        }
        final HashSet<K> set = new HashSet<>(mapCollection.size() * 16);
        for (Map<K, ?> map : mapCollection) {
            set.addAll(map.keySet());
        }

        return set;
    }

    /**
     * 获取指定Map列表中所有的Value
     *
     * @param <V>           值类型
     * @param mapCollection Map列表
     * @return Value集合
     */
    public static <V> List<V> values(Collection<Map<?, V>> mapCollection) {
        final List<V> values = new ArrayList<>();
        for (Map<?, V> map : mapCollection) {
            values.addAll(map.values());
        }

        return values;
    }

    /**
     * 取最大值
     *
     * @param <T>  元素类型
     * @param coll 集合
     * @return 最大值
     * @see Collections#max(Collection)
     */
    public static <T extends Comparable<? super T>> T max(Collection<T> coll) {
        return Collections.max(coll);
    }

    /**
     * 取最大值
     *
     * @param <T>  元素类型
     * @param coll 集合
     * @return 最大值
     * @see Collections#min(Collection)
     */
    public static <T extends Comparable<? super T>> T min(Collection<T> coll) {
        return Collections.min(coll);
    }

    /**
     * 获取匹配规则定义中匹配到元素的所有位置
     * 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param matcher    匹配器，为空则全部匹配
     * @return 位置数组
     */
    public static <T> int[] indexOfAll(Collection<T> collection, Matcher<T> matcher) {
        final List<Integer> indexList = new ArrayList<>();
        if (null != collection) {
            int index = 0;
            for (T t : collection) {
                if (null == matcher || matcher.match(t)) {
                    indexList.add(index);
                }
                index++;
            }
        }
        return Convert.convert(int[].class, indexList);
    }

    /**
     * 清除一个或多个集合内的元素，每个集合调用clear()方法
     *
     * @param collections 一个或多个集合
     */
    public static void clear(Collection<?>... collections) {
        for (Collection<?> collection : collections) {
            if (isNotEmpty(collection)) {
                collection.clear();
            }
        }
    }

    /**
     * 填充List，以达到最小长度
     *
     * @param list   列表
     * @param minLen 最小长度
     * @param padObj 填充的对象
     * @param <T>    集合元素类型
     */
    public static <T> void padLeft(List<T> list, int minLen, T padObj) {
        Objects.requireNonNull(list);
        if (list.isEmpty()) {
            padRight(list, minLen, padObj);
            return;
        }
        for (int i = list.size(); i < minLen; i++) {
            list.add(0, padObj);
        }
    }

    /**
     * 填充List，以达到最小长度
     *
     * @param list   列表
     * @param minLen 最小长度
     * @param padObj 填充的对象
     * @param <T>    集合元素类型
     */
    public static <T> void padRight(Collection<T> list, int minLen, T padObj) {
        Objects.requireNonNull(list);
        for (int i = list.size(); i < minLen; i++) {
            list.add(padObj);
        }
    }

    /**
     * 针对一个参数做相应的操作
     *
     * @param <T> 处理参数类型
     * @author Kimi Liu
     */
    public interface Consumer<T> {
        /**
         * 接受并处理一个参数
         *
         * @param value 参数值
         * @param index 参数在集合中的索引
         */
        void accept(T value, int index);
    }

    /**
     * 针对两个参数做相应的操作,例如Map中的KEY和VALUE
     *
     * @param <K> KEY类型
     * @param <V> VALUE类型
     * @author Kimi Liu
     */
    public interface KVConsumer<K, V> {
        /**
         * 接受并处理一对参数
         *
         * @param key   键
         * @param value 值
         * @param index 参数在集合中的索引
         */
        void accept(K key, V value, int index);
    }

    /**
     * Hash计算接口
     *
     * @param <T> 被计算hash的对象类型
     */
    public interface Hash<T> {
        /**
         * 计算Hash值
         *
         * @param t 对象
         * @return hash
         */
        int hash(T t);
    }

}
