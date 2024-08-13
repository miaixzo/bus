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
package org.miaixz.bus.core.center.stream;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.mutable.MutableInt;
import org.miaixz.bus.core.lang.mutable.MutableObject;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollectorKit;

/**
 * {@link WrappedStream}的扩展，用于为实现类提供更多终端操作方法的增强接口， 该接口提供的方法，返回值类型都不为{@link Stream}。
 *
 * @param <T> 流中的元素类型
 * @param <S> {@link TerminableWrappedStream}的实现类类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TerminableWrappedStream<T, S extends TerminableWrappedStream<T, S>> extends WrappedStream<T, S> {

    /**
     * 转换为{@link ArrayList}
     *
     * @return 集合
     * @see #toColl(Supplier)
     */
    default List<T> toList() {
        return this.toColl(ArrayList::new);
    }

    /**
     * 换为不可变集合
     *
     * @return 集合
     * @see #toColl(Supplier)
     */
    default List<T> toUnmodifiableList() {
        return Collections.unmodifiableList(this.toList());
    }

    /**
     * 转换为HashSet
     *
     * @return 集合
     * @see #toColl(Supplier)
     */
    default Set<T> toSet() {
        return this.toColl(HashSet::new);
    }

    /**
     * 换为不可变集合
     *
     * @return 集合
     * @see #toColl(Supplier)
     */
    default Set<T> toUnmodifiableSet() {
        return Collections.unmodifiableSet(this.toSet());
    }

    /**
     * 转换成集合
     *
     * @param collectionFactory 集合工厂(可以是集合构造器)
     * @param <C>               集合类型
     * @return 集合
     */
    default <C extends Collection<T>> C toColl(final Supplier<C> collectionFactory) {
        Objects.requireNonNull(collectionFactory);
        return unwrap().collect(Collectors.toCollection(collectionFactory));
    }

    /**
     * 转换为map，key为给定操作执行后的返回值,value为当前元素
     *
     * @param keyMapper 指定的key操作
     * @param <K>       key类型
     * @return map
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    default <K> Map<K, T> toMap(final Function<? super T, ? extends K> keyMapper) {
        return this.toMap(keyMapper, Function.identity());
    }

    /**
     * 转换为map，data,value为给定操作执行后的返回值
     *
     * @param keyMapper   指定的key操作
     * @param valueMapper 指定value操作
     * @param <K>         key类型
     * @param <U>         value类型
     * @return map
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    default <K, U> Map<K, U> toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper) {
        return this.toMap(keyMapper, valueMapper, (l, r) -> r);
    }

    /**
     * 转换为不可变map，data,value为给定操作执行后的返回值
     *
     * @param keyMapper   指定的key操作
     * @param valueMapper 指定value操作
     * @param <K>         key类型
     * @param <U>         value类型
     * @return map
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    default <K, U> Map<K, U> toUnmodifiableMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper) {
        return Collections.unmodifiableMap(this.toMap(keyMapper, valueMapper));
    }

    /**
     * 转换为map，data,value为给定操作执行后的返回值
     *
     * @param keyMapper     指定的key操作
     * @param valueMapper   指定value操作
     * @param mergeFunction 合并操作
     * @param <K>           key类型
     * @param <U>           value类型
     * @return map
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    default <K, U> Map<K, U> toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction) {
        return this.toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * 转换为不可变map，data,value为给定操作执行后的返回值
     *
     * @param keyMapper     指定的key操作
     * @param valueMapper   指定value操作
     * @param mergeFunction 合并操作
     * @param <K>           key类型
     * @param <U>           value类型
     * @return map
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     */
    default <K, U> Map<K, U> toUnmodifiableMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction) {
        return Collections.unmodifiableMap(this.toMap(keyMapper, valueMapper, mergeFunction, HashMap::new));
    }

    /**
     * 转换为map，data,value为给定操作执行后的返回值
     *
     * @param keyMapper     指定的key操作
     * @param valueMapper   指定value操作
     * @param mergeFunction 合并操作
     * @param mapSupplier   map工厂
     * @param <K>           key类型
     * @param <U>           value类型
     * @param <M>           map类型
     * @return map
     */
    default <K, U, M extends Map<K, U>> M toMap(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction,
            final Supplier<M> mapSupplier) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        Objects.requireNonNull(mergeFunction);
        Objects.requireNonNull(mapSupplier);
        return unwrap().collect(CollectorKit.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }

    /**
     * 转换为map，key为下标,value为元素
     *
     * @return map
     */
    default Map<Integer, T> toIdxMap() {
        return toIdxMap(Function.identity());
    }

    /**
     * 转换为map，key为下标,value为给定操作执行后的返回值
     *
     * @param valueMapper 指定value操作
     * @param <U>         value类型
     * @return map
     */
    default <U> Map<Integer, U> toIdxMap(final Function<? super T, ? extends U> valueMapper) {
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        return EasyStream.of(sequential().toList()).toMap(e -> index.incrementAndGet(), valueMapper, (l, r) -> r);
    }

    /**
     * 与给定的可迭代对象转换成map，key为现有元素，value为给定可迭代对象迭代的元素 至少包含全部的key，如果对应位置上的value不存在，则为null
     *
     * @param other 可迭代对象
     * @param <R>   可迭代对象迭代的元素类型
     * @return map，key为现有元素，value为给定可迭代对象迭代的元素; 至少包含全部的key，如果对应位置上的value不存在，则为null; 如果key重复, 则保留最后一个关联的value;
     */
    default <R> Map<T, R> toZip(final Iterable<R> other) {
        Objects.requireNonNull(other);
        // value对象迭代器
        final Iterator<R> iterator = Optional.ofNullable(other).map(Iterable::iterator)
                .orElseGet(Collections::emptyIterator);
        if (this.isParallel()) {
            final List<T> keyList = toList();
            final Map<T, R> map = new HashMap<>(keyList.size());
            for (final T key : keyList) {
                map.put(key, iterator.hasNext() ? iterator.next() : null);
            }
            return map;
        } else {
            return this.toMap(Function.identity(), e -> iterator.hasNext() ? iterator.next() : null);
        }
    }

    /**
     * 获取与给定断言匹配的第一个元素
     *
     * @param predicate 断言
     * @return 与给定断言匹配的第一个元素
     */
    default java.util.Optional<T> findFirst(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return unwrap().filter(predicate).findFirst();
    }

    /**
     * 获取与给定断言匹配的第一个元素的下标，并行流下标永远为-1
     *
     * @param predicate 断言
     * @return 与给定断言匹配的第一个元素的下标，如果不存在则返回-1
     */
    default int findFirstIdx(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        unwrap().filter(e -> {
            index.increment();
            return predicate.test(e);
        }).findFirst();// 此处只做计数，不需要值
        return index.get();
    }

    /**
     * 获取最后一个元素
     *
     * @return 最后一个元素
     */
    default java.util.Optional<T> findLast() {
        final MutableObject<T> last = new MutableObject<>(null);
        spliterator().forEachRemaining(last::set);
        return java.util.Optional.ofNullable(last.get());
    }

    /**
     * 获取与给定断言匹配的最后一个元素
     *
     * @param predicate 断言
     * @return 与给定断言匹配的最后一个元素
     */
    default java.util.Optional<T> findLast(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        final MutableObject<T> last = new MutableObject<>(null);
        spliterator().forEachRemaining(e -> {
            if (predicate.test(e)) {
                last.set(e);
            }
        });
        return java.util.Optional.ofNullable(last.get());
    }

    /**
     * 获取与给定断言匹配的最后一个元素的下标，并行流下标永远为-1
     *
     * @param predicate 断言
     * @return 与给定断言匹配的最后一个元素的下标，如果不存在则返回-1
     */
    default int findLastIdx(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        final MutableInt idxRef = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        forEachIdx((e, i) -> {
            if (predicate.test(e)) {
                idxRef.set(i);
            }
        });
        return idxRef.get();
    }

    /**
     * 获取流中指定下标的元素，如果是负数，则从最后一个开始数起
     *
     * @param idx 下标
     * @return 指定下标的元素
     */
    default java.util.Optional<T> at(final Integer idx) {
        return Optional.ofNullable(idx).map(i -> (T) ArrayKit.get(toArray(), i)).toOptional();
    }

    /**
     * 流是否为空
     *
     * @return 流是否为空
     */
    default boolean isEmpty() {
        return !findAny().isPresent();
    }

    /**
     * 流是否不为空
     *
     * @return 流是否不为空
     */
    default boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * 返回拼接后的字符串
     *
     * @return 拼接后的字符串
     * @see #join(CharSequence, CharSequence, CharSequence)
     */
    default String join() {
        return this.join("");
    }

    /**
     * 返回拼接后的字符串
     *
     * @param delimiter 分隔符
     * @return 拼接后的字符串
     * @see #join(CharSequence, CharSequence, CharSequence)
     */
    default String join(final CharSequence delimiter) {
        return this.join(delimiter, "", "");
    }

    /**
     * 返回拼接后的字符串
     *
     * @param delimiter 分隔符
     * @param prefix    前缀
     * @param suffix    后缀
     * @return 拼接后的字符串
     */
    default String join(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return unwrap().map(String::valueOf)
                .collect(CollectorKit.joining(delimiter, prefix, suffix, Function.identity()));
    }

    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据，得到的键为{@code null}时不会抛出异常
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @return map
     * @see #group(Function, Supplier, Collector)
     */
    default <K> Map<K, List<T>> group(final Function<? super T, ? extends K> classifier) {
        return this.group(classifier, Collectors.toList());
    }

    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据，得到的键为{@code null}时不会抛出异常
     * @param downstream 下游操作
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @param <D>        下游操作对应返回类型，也是Map中value的类型
     * @param <A>        下游操作在进行中间操作时对应类型
     * @return map
     * @see #group(Function, Supplier, Collector)
     */
    default <K, A, D> Map<K, D> group(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return this.group(classifier, HashMap::new, downstream);
    }

    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据，得到的键为{@code null}时不会抛出异常
     * @param mapFactory 提供的map
     * @param downstream 下游操作
     * @param <K>        实体中的分组依据对应类型，也是Map中key的类型
     * @param <D>        下游操作对应返回类型，也是Map中value的类型
     * @param <A>        下游操作在进行中间操作时对应类型
     * @param <M>        最后返回结果Map类型
     * @return map
     * @see CollectorKit#groupingBy(Function, Supplier, Collector)
     */
    default <K, D, A, M extends Map<K, D>> M group(final Function<? super T, ? extends K> classifier,
            final Supplier<M> mapFactory, final Collector<? super T, A, D> downstream) {
        Objects.requireNonNull(classifier);
        Objects.requireNonNull(mapFactory);
        Objects.requireNonNull(downstream);
        return unwrap().collect(CollectorKit.groupingBy(classifier, mapFactory, downstream));
    }

    /**
     * 根据给定判断条件分组
     *
     * @param predicate 判断条件
     * @return map
     * @see #partition(Predicate, Collector)
     */
    default Map<Boolean, List<T>> partition(final Predicate<T> predicate) {
        return this.partition(predicate, ArrayList::new);
    }

    /**
     * 根据给定判断条件分组
     *
     * @param <C>         值类型
     * @param predicate   判断条件
     * @param collFactory 提供的集合
     * @return map
     * @see #partition(Predicate, Collector)
     */
    default <C extends Collection<T>> Map<Boolean, C> partition(final Predicate<T> predicate,
            final Supplier<C> collFactory) {
        return this.partition(predicate, Collectors.toCollection(collFactory));
    }

    /**
     * 根据给定判断条件分组
     *
     * @param predicate  判断条件
     * @param downstream 下游操作
     * @param <R>        返回值类型
     * @return map
     */
    default <R> Map<Boolean, R> partition(final Predicate<T> predicate, final Collector<T, ?, R> downstream) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(downstream);
        return unwrap().collect(Collectors.partitioningBy(predicate, downstream));
    }

    /**
     * 对流里面的每一个元素执行一个操作，操作带下标，并行流时下标永远为-1 这是一个终端操作
     *
     * @param action 操作
     */
    default void forEachIdx(final BiConsumer<? super T, Integer> action) {
        Objects.requireNonNull(action);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        unwrap().forEach(e -> action.accept(e, index.incrementAndGet()));
    }

    /**
     * 对流里面的每一个元素按照顺序执行一个操作，操作带下标，并行流时下标永远为-1 这是一个终端操作
     *
     * @param action 操作
     */
    default void forEachOrderedIdx(final BiConsumer<? super T, Integer> action) {
        Objects.requireNonNull(action);
        final MutableInt index = new MutableInt(NOT_FOUND_ELEMENT_INDEX);
        unwrap().forEachOrdered(e -> action.accept(e, index.incrementAndGet()));
    }

}
