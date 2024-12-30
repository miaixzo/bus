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
package org.miaixz.bus.core.xyz;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.iterator.*;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.text.StringJoiner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link Iterable} 和 {@link Iterator} 相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IteratorKit extends IteratorValidator {

    /**
     * 获取{@link Iterator}
     *
     * @param iterable {@link Iterable}
     * @param <T>      元素类型
     * @return 当iterable为null返回{@code null}，否则返回对应的{@link Iterator}
     */
    public static <T> Iterator<T> getIter(final Iterable<T> iterable) {
        return null == iterable ? null : iterable.iterator();
    }

    /**
     * 字段值与列表值对应的Map，常用于元素对象中有唯一ID时需要按照这个ID查找对象的情况 例如：车牌号 = 车
     *
     * @param <K>       字段名对应值得类型，不确定请使用Object
     * @param <V>       对象类型
     * @param iter      对象列表
     * @param fieldName 字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <K, V> Map<K, V> fieldValueMap(final Iterator<V> iter, final String fieldName) {
        return MapKit.putAll(new HashMap<>(), iter, (value) -> (K) FieldKit.getFieldValue(value, fieldName));
    }

    /**
     * 两个字段值组成新的Map
     *
     * @param <K>               字段名对应值得类型，不确定请使用Object
     * @param <V>               值类型，不确定使用Object
     * @param iter              对象列表
     * @param fieldNameForKey   做为键的字段名（会通过反射获取其值）
     * @param fieldNameForValue 做为值的字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <K, V> Map<K, V> fieldValueAsMap(final Iterator<?> iter, final String fieldNameForKey,
            final String fieldNameForValue) {
        return MapKit.putAll(new HashMap<>(), iter, (value) -> (K) FieldKit.getFieldValue(value, fieldNameForKey),
                (value) -> (V) FieldKit.getFieldValue(value, fieldNameForValue));
    }

    /**
     * 获取指定Bean列表中某个字段，生成新的列表
     *
     * @param <R>       返回元素类型
     * @param <V>       对象类型
     * @param iterable  对象列表
     * @param fieldName 字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <V, R> List<R> fieldValueList(final Iterable<V> iterable, final String fieldName) {
        return fieldValueList(getIter(iterable), fieldName);
    }

    /**
     * 获取指定Bean列表中某个字段，生成新的列表
     *
     * @param <R>       返回元素类型
     * @param <V>       对象类型
     * @param iter      对象列表
     * @param fieldName 字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <V, R> List<R> fieldValueList(final Iterator<V> iter, final String fieldName) {
        final List<R> result = new ArrayList<>();
        if (null != iter) {
            V value;
            while (iter.hasNext()) {
                value = iter.next();
                result.add((R) FieldKit.getFieldValue(value, fieldName));
            }
        }
        return result;
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterator    集合
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static <T> String join(final Iterator<T> iterator, final CharSequence conjunction) {
        return StringJoiner.of(conjunction).append(iterator).toString();
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterator    集合
     * @param conjunction 分隔符
     * @param prefix      每个元素添加的前缀，null表示不添加
     * @param suffix      每个元素添加的后缀，null表示不添加
     * @return 连接后的字符串
     */
    public static <T> String join(final Iterator<T> iterator, final CharSequence conjunction, final String prefix,
            final String suffix) {
        return StringJoiner.of(conjunction, prefix, suffix)
                // 每个元素都添加前后缀
                .setWrapElement(true).append(iterator).toString();
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterator    集合
     * @param conjunction 分隔符
     * @param func        集合元素转换器，将元素转换为字符串
     * @return 连接后的字符串
     */
    public static <T> String join(final Iterator<T> iterator, final CharSequence conjunction,
            final Function<T, ? extends CharSequence> func) {
        if (null == iterator) {
            return null;
        }

        return StringJoiner.of(conjunction).append(iterator, func).toString();
    }

    /**
     * 将键列表和值列表转换为Map 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。 如果值多于键，忽略多余的值。
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return 标题内容Map
     */
    public static <K, V> Map<K, V> toMap(final Iterable<K> keys, final Iterable<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * 将键列表和值列表转换为Map 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。 如果值多于键，忽略多余的值。
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder 是否有序
     * @return 标题内容Map
     */
    public static <K, V> Map<K, V> toMap(final Iterable<K> keys, final Iterable<V> values, final boolean isOrder) {
        return toMap(null == keys ? null : keys.iterator(), null == values ? null : values.iterator(), isOrder);
    }

    /**
     * 将键列表和值列表转换为Map 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。 如果值多于键，忽略多余的值。
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return 标题内容Map
     */
    public static <K, V> Map<K, V> toMap(final Iterator<K> keys, final Iterator<V> values) {
        return toMap(keys, values, false);
    }

    /**
     * 将键列表和值列表转换为Map 以键为准，值与键位置需对应。如果键元素数多于值元素，多余部分值用null代替。 如果值多于键，忽略多余的值。
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder 是否有序
     * @return 标题内容Map
     */
    public static <K, V> Map<K, V> toMap(final Iterator<K> keys, final Iterator<V> values, final boolean isOrder) {
        final Map<K, V> resultMap = MapKit.newHashMap(isOrder);
        if (isNotEmpty(keys)) {
            while (keys.hasNext()) {
                resultMap.put(keys.next(), (null != values && values.hasNext()) ? values.next() : null);
            }
        }
        return resultMap;
    }

    /**
     * 将列表转成值为List的HashMap
     *
     * @param iterable  值列表
     * @param keyMapper Map的键映射
     * @param <K>       键类型
     * @param <V>       值类型
     * @return HashMap
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<V> iterable, final Function<V, K> keyMapper) {
        return toListMap(iterable, keyMapper, v -> v);
    }

    /**
     * 将列表转成值为List的HashMap
     *
     * @param iterable    值列表
     * @param keyMapper   Map的键映射
     * @param valueMapper Map中List的值映射
     * @param <T>         列表值类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return HashMap
     */
    public static <T, K, V> Map<K, List<V>> toListMap(final Iterable<T> iterable, final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        return toListMap(MapKit.newHashMap(), iterable, keyMapper, valueMapper);
    }

    /**
     * 将列表转成值为List的Map集合
     *
     * @param resultMap   结果Map，可自定义结果Map类型
     * @param iterable    值列表
     * @param keyMapper   Map的键映射
     * @param valueMapper Map中List的值映射
     * @param <T>         列表值类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return HashMap
     */
    public static <T, K, V> Map<K, List<V>> toListMap(Map<K, List<V>> resultMap, final Iterable<T> iterable,
            final Function<T, K> keyMapper, final Function<T, V> valueMapper) {
        if (null == resultMap) {
            resultMap = MapKit.newHashMap();
        }
        if (ObjectKit.isNull(iterable)) {
            return resultMap;
        }

        for (final T value : iterable) {
            resultMap.computeIfAbsent(keyMapper.apply(value), k -> new ArrayList<>()).add(valueMapper.apply(value));
        }

        return resultMap;
    }

    /**
     * 将列表转成HashMap
     *
     * @param iterable  值列表
     * @param keyMapper Map的键映射
     * @param <K>       键类型
     * @param <V>       值类型
     * @return HashMap
     */
    public static <K, V> Map<K, V> toMap(final Iterable<V> iterable, final Function<V, K> keyMapper) {
        return toMap(iterable, keyMapper, Function.identity());
    }

    /**
     * 将列表转成HashMap
     *
     * @param iterable    值列表
     * @param keyMapper   Map的键映射
     * @param valueMapper Map的值映射
     * @param <T>         列表值类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return HashMap
     */
    public static <T, K, V> Map<K, V> toMap(final Iterable<T> iterable, final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        return MapKit.putAll(MapKit.newHashMap(), iterable, keyMapper, valueMapper);
    }

    /**
     * Enumeration转换为Iterator Adapt the specified {@code Enumeration} to the {@code Iterator} interface
     *
     * @param <E> 集合元素类型
     * @param e   {@link Enumeration}
     * @return {@link Iterator}
     */
    public static <E> Iterator<E> asIterator(final Enumeration<E> e) {
        return new EnumerationIterator<>(Objects.requireNonNull(e));
    }

    /**
     * {@link Iterator} 转为 {@link Iterable}, 但是仅可使用一次
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return {@link Iterable}
     */
    public static <E> Iterable<E> asIterable(final Iterator<E> iter) {
        return () -> iter;
    }

    /**
     * 获得{@link Iterable}对象的元素类型（通过第一个非空元素判断） 注意，此方法至少会调用多次next方法
     *
     * @param iterable {@link Iterable}
     * @return 元素类型，当列表为空或元素全部为null时，返回null
     */
    public static Class<?> getElementType(final Iterable<?> iterable) {
        return getElementType(getIter(iterable));
    }

    /**
     * 获得{@link Iterator}对象的元素类型（通过第一个非空元素判断） 注意，此方法至少会调用多次next方法
     *
     * @param iterator {@link Iterator}，为 {@code null}返回{@code null}
     * @return 元素类型，当列表为空或元素全部为{@code null}时，返回{@code null}
     */
    public static Class<?> getElementType(final Iterator<?> iterator) {
        if (null == iterator) {
            return null;
        }
        final Object ele = getFirstNoneNull(iterator);
        return null == ele ? null : ele.getClass();
    }

    /**
     * 编辑，此方法产生一个新{@link ArrayList} 编辑过程通过传入的Editor实现来返回需要的元素内容，这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，如果返回null表示这个元素对象抛弃
     * 2、修改元素对象，返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>    集合元素类型
     * @param iter   集合
     * @param editor 编辑器接口, {@code null}表示不编辑
     * @return 过滤后的集合
     */
    public static <T> List<T> edit(final Iterator<T> iter, final UnaryOperator<T> editor) {
        final List<T> result = new ArrayList<>();
        if (null == iter) {
            return result;
        }

        T modified;
        while (iter.hasNext()) {
            modified = (null == editor) ? iter.next() : editor.apply(iter.next());
            if (null != modified) {
                result.add(modified);
            }
        }
        return result;
    }

    /**
     * 移除集合中满足条件的所有元素，此方法在原集合上直接修改 通过实现{@link Predicate}接口，完成元素的移除，可以实现以下功能：
     *
     * <pre>
     * 1、移除指定对象，{@link Predicate#test(Object)}方法返回{@code
     * true
     * }的对象将被使用{@link Iterator#remove()}方法移除。
     * </pre>
     *
     * @param <E>       集合元素类型
     * @param iter      集合
     * @param predicate 过滤器接口，删除{@link Predicate#test(Object)}为{@code true}的元素
     * @return 编辑后的集合
     */
    public static <E> Iterator<E> remove(final Iterator<E> iter, final Predicate<E> predicate) {
        if (null == iter || null == predicate) {
            return iter;
        }

        while (iter.hasNext()) {
            if (predicate.test(iter.next())) {
                iter.remove();
            }
        }
        return iter;
    }

    /**
     * 过滤{@link Iterator}并将过滤后满足条件的元素添加到List中
     *
     * @param <E>       元素类型
     * @param iter      {@link Iterator}
     * @param predicate 过滤器，{@link Predicate#test(Object)}为{@code true}保留
     * @return ArrayList
     */
    public static <E> List<E> filterToList(final Iterator<E> iter, final Predicate<E> predicate) {
        return ListKit.of(filtered(iter, predicate));
    }

    /**
     * 获取一个新的 {@link FilterIterator}，用于过滤指定元素
     *
     * @param iterator  被包装的 {@link Iterator}
     * @param predicate 过滤断言，{@link Predicate#test(Object)}为{@code true}保留元素。
     * @param <E>       元素类型
     * @return {@link FilterIterator}
     */
    public static <E> FilterIterator<E> filtered(final Iterator<? extends E> iterator,
            final Predicate<? super E> predicate) {
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * 返回一个空Iterator
     *
     * @param <T> 元素类型
     * @return 空Iterator
     * @see Collections#emptyIterator()
     */
    public static <T> Iterator<T> empty() {
        return Collections.emptyIterator();
    }

    /**
     * 按照给定函数，转换{@link Iterator}为另一种类型的{@link Iterator}
     *
     * @param <F>      源元素类型
     * @param <T>      目标元素类型
     * @param iterator 源{@link Iterator}
     * @param function 转换函数
     * @return 转换后的{@link Iterator}
     */
    public static <F, T> Iterator<T> trans(final Iterator<F> iterator,
            final Function<? super F, ? extends T> function) {
        return new TransIterator<>(iterator, function);
    }

    /**
     * 返回 Iterable 对象的元素数量
     *
     * @param iterable Iterable对象
     * @return Iterable对象的元素数量
     */
    public static int size(final Iterable<?> iterable) {
        if (null == iterable) {
            return 0;
        }

        if (iterable instanceof Collection<?>) {
            return ((Collection<?>) iterable).size();
        } else {
            return size(iterable.iterator());
        }
    }

    /**
     * 返回 Iterator 对象的元素数量
     *
     * @param iterator Iterator对象
     * @return Iterator对象的元素数量
     */
    public static int size(final Iterator<?> iterator) {
        int size = 0;
        if (iterator != null) {
            while (iterator.hasNext()) {
                iterator.next();
                size++;
            }
        }
        return size;
    }

    /**
     * 清空指定{@link Iterator}，此方法遍历后调用{@link Iterator#remove()}移除每个元素
     *
     * @param iterator {@link Iterator}
     */
    public static void clear(final Iterator<?> iterator) {
        if (null != iterator) {
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    /**
     * 遍历{@link Iterator} 当consumer为{@code null}表示不处理，但是依旧遍历{@link Iterator}
     *
     * @param iterator {@link Iterator}
     * @param consumer 节点消费，{@code null}表示不处理
     * @param <E>      元素类型
     */
    public static <E> void forEach(final Iterator<E> iterator, final Consumer<? super E> consumer) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                final E element = iterator.next();
                if (null != consumer) {
                    consumer.accept(element);
                }
            }
        }
    }

    /**
     * 循环遍历 {@link Iterator}，使用{@link BiConsumerX} 接受遍历的每条数据，并针对每条数据做处理，支持index
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @param consumer {@link BiConsumerX} 遍历的每条数据处理器
     */
    public static <T> void forEach(final Iterator<T> iterator, final BiConsumerX<Integer, T> consumer) {
        if (iterator == null) {
            return;
        }
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(index, iterator.next());
            index++;
        }
    }

    /**
     * 拼接 {@link Iterator}为字符串
     *
     * @param iterator {@link Iterator}
     * @param <E>      元素类型
     * @return 字符串
     */
    public static <E> String toString(final Iterator<E> iterator) {
        return toString(iterator, Convert::toStringOrNull);
    }

    /**
     * 拼接 {@link Iterator}为字符串
     *
     * @param iterator  {@link Iterator}
     * @param transFunc 元素转字符串函数
     * @param <E>       元素类型
     * @return 字符串
     */
    public static <E> String toString(final Iterator<E> iterator, final Function<? super E, String> transFunc) {
        return toString(iterator, transFunc, ", ", "[", "]");
    }

    /**
     * 拼接 {@link Iterator}为字符串
     *
     * @param iterator  {@link Iterator}
     * @param transFunc 元素转字符串函数
     * @param delimiter 分隔符
     * @param prefix    前缀
     * @param suffix    后缀
     * @param <E>       元素类型
     * @return 字符串
     */
    public static <E> String toString(final Iterator<E> iterator, final Function<? super E, String> transFunc,
            final String delimiter, final String prefix, final String suffix) {
        final StringJoiner stringJoiner = StringJoiner.of(delimiter, prefix, suffix);
        stringJoiner.append(iterator, transFunc);
        return stringJoiner.toString();
    }

    /**
     * 从给定的对象中获取可能存在的{@link Iterator}，规则如下：
     * <ul>
     * <li>null - null</li>
     * <li>Iterator - 直接返回</li>
     * <li>Enumeration - {@link EnumerationIterator}</li>
     * <li>Collection - 调用{@link Collection#iterator()}</li>
     * <li>Map - Entry的{@link Iterator}</li>
     * <li>Dictionary - values (elements) enumeration returned as iterator</li>
     * <li>array - {@link ArrayIterator}</li>
     * <li>NodeList - {@link NodeListIterator}</li>
     * <li>Node - 子节点</li>
     * <li>object with iterator() public method，通过反射访问</li>
     * <li>object - 单对象的{@link ArrayIterator}</li>
     * </ul>
     *
     * @param object 可以获取{@link Iterator}的对象
     * @return {@link Iterator}，如果提供对象为{@code null}，返回{@code null}
     */
    public static Iterator<?> getIter(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Iterator) {
            return (Iterator<?>) object;
        } else if (object instanceof Iterable) {
            return ((Iterable<?>) object).iterator();
        } else if (ArrayKit.isArray(object)) {
            return new ArrayIterator<>(object);
        } else if (object instanceof Enumeration) {
            return new EnumerationIterator<>((Enumeration<?>) object);
        } else if (object instanceof Map) {
            return ((Map<?, ?>) object).entrySet().iterator();
        } else if (object instanceof NodeList) {
            return new NodeListIterator((NodeList) object);
        } else if (object instanceof Node) {
            // 遍历子节点
            return new NodeListIterator(((Node) object).getChildNodes());
        } else if (object instanceof Dictionary) {
            return new EnumerationIterator<>(((Dictionary<?, ?>) object).elements());
        }

        // 反射获取
        try {
            final Object iterator = MethodKit.invoke(object, "iterator");
            if (iterator instanceof Iterator) {
                return (Iterator<?>) iterator;
            }
        } catch (final RuntimeException ignore) {
            // ignore
        }
        return new ArrayIterator<>(new Object[] { object });
    }

}
