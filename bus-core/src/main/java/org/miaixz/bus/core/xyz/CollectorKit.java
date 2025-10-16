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

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.center.stream.EntryStream;
import org.miaixz.bus.core.center.stream.SimpleCollector;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;

/**
 * Utility class for mutable reduction operations using {@link Collector}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectorKit {

    /**
     * Collector characteristics indicating IDENTITY_FINISH.
     */
    public static final Set<Collector.Characteristics> CH_ID = Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    /**
     * Collector characteristics indicating no IDENTITY_FINISH.
     */
    public static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    /**
     * Provides a {@link Collector} that joins any object by calling its {@code toString()} method.
     *
     * @param delimiter The delimiter to be used between each element.
     * @param <T>       The type of the objects.
     * @return A {@link Collector}.
     */
    public static <T> Collector<T, ?, String> joining(final CharSequence delimiter) {
        return joining(delimiter, Object::toString);
    }

    /**
     * Provides a {@link Collector} that joins any object using a custom toString function.
     *
     * @param delimiter    The delimiter to be used between each element.
     * @param toStringFunc A function to convert each object to a string.
     * @param <T>          The type of the objects.
     * @return A {@link Collector}.
     */
    public static <T> Collector<T, ?, String> joining(
            final CharSequence delimiter,
            final Function<T, ? extends CharSequence> toStringFunc) {
        return joining(delimiter, Normal.EMPTY, Normal.EMPTY, toStringFunc);
    }

    /**
     * Provides a {@link Collector} that joins any object using a custom toString function, with a prefix and suffix.
     *
     * @param delimiter    The delimiter to be used between each element.
     * @param prefix       The prefix to be used at the beginning.
     * @param suffix       The suffix to be used at the end.
     * @param toStringFunc A function to convert each object to a string.
     * @param <T>          The type of the objects.
     * @return A {@link Collector}.
     */
    public static <T> Collector<T, ?, String> joining(
            final CharSequence delimiter,
            final CharSequence prefix,
            final CharSequence suffix,
            final Function<T, ? extends CharSequence> toStringFunc) {
        return new SimpleCollector<>(() -> new StringJoiner(delimiter, prefix, suffix),
                (joiner, ele) -> joiner.add(toStringFunc.apply(ele)), StringJoiner::merge, StringJoiner::toString,
                Collections.emptySet());
    }

    /**
     * Provides a null-friendly {@link Collector} that performs a `groupingBy` operation, with a specified map type.
     *
     * @param classifier The classifier function mapping input elements to keys.
     * @param mapFactory A function which returns a new, empty {@code Map} into which the results will be inserted.
     * @param downstream A {@code Collector} implementing the downstream reduction.
     * @param <T>        The type of the input elements.
     * @param <K>        The type of the keys.
     * @param <D>        The result type of the downstream reduction.
     * @param <A>        The intermediate accumulation type of the downstream collector.
     * @param <M>        The type of the resulting {@code Map}.
     * @return A {@link Collector}.
     */
    public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier,
            final Supplier<M> mapFactory,
            final Collector<? super T, A, D> downstream) {
        final Supplier<A> downstreamSupplier = downstream.supplier();
        final BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        final BiConsumer<Map<K, A>, T> accumulator = (m, t) -> {
            final K key = java.util.Optional.ofNullable(t).map(classifier).orElse(null);
            final A container = m.computeIfAbsent(key, k -> downstreamSupplier.get());
            if (ArrayKit.isArray(container) || Objects.nonNull(t)) {
                downstreamAccumulator.accept(container, t);
            }
        };
        final BinaryOperator<Map<K, A>> merger = mapMerger(downstream.combiner());
        final Supplier<Map<K, A>> mangledFactory = (Supplier<Map<K, A>>) mapFactory;

        if (downstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
            return new SimpleCollector<>(mangledFactory, accumulator, merger, CH_ID);
        } else {
            final Function<A, A> downstreamFinisher = (Function<A, A>) downstream.finisher();
            final Function<Map<K, A>, M> finisher = intermediate -> {
                intermediate.replaceAll((k, v) -> downstreamFinisher.apply(v));
                final M castResult = (M) intermediate;
                return castResult;
            };
            return new SimpleCollector<>(mangledFactory, accumulator, merger, finisher, CH_NOID);
        }
    }

    /**
     * Provides a null-friendly {@link Collector} that performs a `groupingBy` operation.
     *
     * @param classifier The classifier function.
     * @param downstream The downstream collector.
     * @param <T>        The type of the input elements.
     * @param <K>        The type of the keys.
     * @param <D>        The result type of the downstream reduction.
     * @param <A>        The intermediate accumulation type of the downstream collector.
     * @return A {@link Collector}.
     */
    public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(
            final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return groupingBy(classifier, HashMap::new, downstream);
    }

    /**
     * Provides a null-friendly {@link Collector} that performs a `groupingBy` operation into a `List`.
     *
     * @param classifier The classifier function.
     * @param <T>        The type of the input elements.
     * @param <K>        The type of the keys.
     * @return A {@link Collector}.
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(
            final Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, Collectors.toList());
    }

    /**
     * Provides a null-friendly `groupingBy` collector that also maps the grouped values.
     *
     * @param classifier       The classifier function.
     * @param valueMapper      The function to map the values.
     * @param valueCollFactory The factory for the collection of values.
     * @param mapFactory       The factory for the resulting map.
     * @param <T>              The type of the input elements.
     * @param <K>              The type of the keys.
     * @param <R>              The type of the mapped values.
     * @param <C>              The type of the value collection.
     * @param <M>              The type of the resulting map.
     * @return A {@link Collector}.
     */
    public static <T, K, R, C extends Collection<R>, M extends Map<K, C>> Collector<T, ?, M> groupingBy(
            final Function<? super T, ? extends K> classifier,
            final Function<? super T, ? extends R> valueMapper,
            final Supplier<C> valueCollFactory,
            final Supplier<M> mapFactory) {
        return groupingBy(
                classifier,
                mapFactory,
                Collectors.mapping(valueMapper, Collectors.toCollection(valueCollFactory)));
    }

    /**
     * Provides a null-friendly `groupingBy` collector that also maps the grouped values.
     *
     * @param classifier       The classifier function.
     * @param valueMapper      The function to map the values.
     * @param valueCollFactory The factory for the collection of values.
     * @param <T>              The type of the input elements.
     * @param <K>              The type of the keys.
     * @param <R>              The type of the mapped values.
     * @param <C>              The type of the value collection.
     * @return A {@link Collector}.
     */
    public static <T, K, R, C extends Collection<R>> Collector<T, ?, Map<K, C>> groupingBy(
            final Function<? super T, ? extends K> classifier,
            final Function<? super T, ? extends R> valueMapper,
            final Supplier<C> valueCollFactory) {
        return groupingBy(classifier, valueMapper, valueCollFactory, HashMap::new);
    }

    /**
     * Provides a null-friendly `groupingBy` collector that also maps the grouped values into a `List`.
     *
     * @param classifier  The classifier function.
     * @param valueMapper The function to map the values.
     * @param <T>         The type of the input elements.
     * @param <K>         The type of the keys.
     * @param <R>         The type of the mapped values.
     * @return A {@link Collector}.
     */
    public static <T, K, R> Collector<T, ?, Map<K, List<R>>> groupingBy(
            final Function<? super T, ? extends K> classifier,
            final Function<? super T, ? extends R> valueMapper) {
        return groupingBy(classifier, valueMapper, ArrayList::new, HashMap::new);
    }

    /**
     * Provides a null-friendly {@link Collector} that collects elements into a `Map`, using `HashMap` by default.
     *
     * @param keyMapper   A mapping function to produce keys.
     * @param valueMapper A mapping function to produce values.
     * @param <T>         The type of the input elements.
     * @param <K>         The output type of the key mapping function.
     * @param <U>         The output type of the value mapping function.
     * @return A null-friendly {@link Collector}.
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, (l, r) -> r);
    }

    /**
     * Provides a null-friendly {@link Collector} that collects elements into a `Map`, using `HashMap` by default.
     *
     * @param keyMapper A mapping function to produce keys.
     * @param <T>       The type of the input elements.
     * @param <K>       The output type of the key mapping function.
     * @return A null-friendly {@link Collector}.
     */
    public static <T, K> Collector<T, ?, Map<K, T>> toMap(final Function<? super T, ? extends K> keyMapper) {
        return toMap(keyMapper, Function.identity());
    }

    /**
     * Provides a null-friendly {@link Collector} that collects elements into a `Map`, using `HashMap` by default.
     *
     * @param keyMapper     A mapping function to produce keys.
     * @param valueMapper   A mapping function to produce values.
     * @param mergeFunction A merge function, used to resolve collisions between values associated with the same key.
     * @param <T>           The type of the input elements.
     * @param <K>           The output type of the key mapping function.
     * @param <U>           The output type of the value mapping function.
     * @return A null-friendly {@link Collector}.
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper,
            final BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * Provides a null-friendly {@link Collector} that collects elements into a `Map`.
     *
     * @param keyMapper     A mapping function to produce keys.
     * @param valueMapper   A mapping function to produce values.
     * @param mergeFunction A merge function, used to resolve collisions.
     * @param mapSupplier   A function which returns a new, empty {@code Map}.
     * @param <T>           The type of the input elements.
     * @param <K>           The output type of the key mapping function.
     * @param <U>           The output type of the value mapping function.
     * @param <M>           The type of the resulting {@code Map}.
     * @return A null-friendly {@link Collector}.
     */
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper,
            final BinaryOperator<U> mergeFunction,
            final Supplier<M> mapSupplier) {
        final BiConsumer<M, T> accumulator = (map, element) -> map.put(
                Optional.ofNullable(element).map(keyMapper).orElse(null),
                Optional.ofNullable(element).map(valueMapper).orElse(null));
        return new SimpleCollector<>(mapSupplier, accumulator, mapMerger(mergeFunction), CH_ID);
    }

    /**
     * Returns a `BinaryOperator` for merging two maps, using a provided function to resolve value collisions.
     *
     * @param mergeFunction The function to resolve collisions.
     * @param <K>           The type of the keys.
     * @param <V>           The type of the values.
     * @param <M>           The type of the maps.
     * @return A `BinaryOperator` for merging maps.
     */
    public static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(final BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (final Map.Entry<K, V> e : m2.entrySet()) {
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            }
            return m1;
        };
    }

    /**
     * A {@link Collector} that aggregates a stream of maps ({@code Map<K, V>}) into a single {@code Map<K, List<V>>}.
     *
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     * @return The aggregated map.
     */
    public static <K, V> Collector<Map<K, V>, ?, Map<K, List<V>>> reduceListMap() {
        return reduceListMap(HashMap::new);
    }

    /**
     * A {@link Collector} that aggregates a stream of maps ({@code Map<K, V>}) into a single {@code Map<K, List<V>>}.
     *
     * @param mapSupplier A function which returns a new, empty {@code Map}.
     * @param <K>         The type of the keys.
     * @param <V>         The type of the values.
     * @param <R>         The type of the resulting map.
     * @return The aggregated map.
     */
    public static <K, V, R extends Map<K, List<V>>> Collector<Map<K, V>, ?, R> reduceListMap(
            final Supplier<R> mapSupplier) {
        return Collectors.reducing(mapSupplier.get(), value -> {
            final R result = mapSupplier.get();
            value.forEach((k, v) -> result.computeIfAbsent(k, i -> new ArrayList<>()).add(v));
            return result;
        }, (l, r) -> {
            final R resultMap = mapSupplier.get();
            resultMap.putAll(l);
            r.forEach((k, v) -> resultMap.computeIfAbsent(k, i -> new ArrayList<>()).addAll(v));
            return resultMap;
        });
    }

    /**
     * A {@link Collector} that transforms a stream into an {@link EntryStream}.
     *
     * @param keyMapper The mapping function for the key.
     * @param <T>       The type of the input elements.
     * @param <K>       The type of the entry key.
     * @return A collector.
     */
    public static <T, K> Collector<T, List<T>, EntryStream<K, T>> toEntryStream(
            final Function<? super T, ? extends K> keyMapper) {
        return toEntryStream(keyMapper, Function.identity());
    }

    /**
     * A {@link Collector} that transforms a stream into an {@link EntryStream}.
     *
     * @param keyMapper   The mapping function for the key.
     * @param valueMapper The mapping function for the value.
     * @param <T>         The type of the input elements.
     * @param <K>         The type of the entry key.
     * @param <V>         The type of the entry value.
     * @return A collector.
     */
    public static <T, K, V> Collector<T, List<T>, EntryStream<K, V>> toEntryStream(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);
        return transform(ArrayList::new, list -> EntryStream.of(list, keyMapper, valueMapper));
    }

    /**
     * A {@link Collector} that transforms a stream into an {@link EasyStream}.
     *
     * @param <T> The type of the input elements.
     * @return A collector.
     */
    public static <T> Collector<T, ?, EasyStream<T>> toEasyStream() {
        return transform(ArrayList::new, EasyStream::of);
    }

    /**
     * A {@link Collector} that first collects elements into a specified {@link Collection} and then applies a final
     * transformation to that collection. The effect is equivalent to:
     * 
     * <pre>{@code
     * 
     * Collection<T> coll = Stream.of(a, b, c, d).collect(Collectors.toCollection(collFactory));
     * R result = mapper.apply(coll);
     * }</pre>
     *
     * @param collFactory The factory for the intermediate collection.
     * @param mapper      The final mapping function to apply to the collection.
     * @param <R>         The type of the final result.
     * @param <T>         The type of the input elements.
     * @param <C>         The type of the intermediate collection.
     * @return A collector.
     */
    public static <T, R, C extends Collection<T>> Collector<T, C, R> transform(
            final Supplier<C> collFactory,
            final Function<C, R> mapper) {
        Objects.requireNonNull(collFactory);
        Objects.requireNonNull(mapper);
        return new SimpleCollector<>(collFactory, C::add, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }, mapper, CH_NOID);
    }

    /**
     * A {@link Collector} that first collects elements into an {@link ArrayList} and then applies a final
     * transformation to that list. The effect is equivalent to:
     * 
     * <pre>{@code
     * 
     * List<T> list = Stream.of(a, b, c, d).collect(Collectors.toList());
     * R result = mapper.apply(list);
     * }</pre>
     *
     * @param mapper The final mapping function to apply to the list.
     * @param <R>    The type of the final result.
     * @param <T>    The type of the input elements.
     * @return A collector.
     */
    public static <T, R> Collector<T, List<T>, R> transform(final Function<List<T>, R> mapper) {
        return transform(ArrayList::new, mapper);
    }

    /**
     * A {@link Collector} for converting a {@code Stream<Map.Entry<K, V>>} into a {@code Map<K, V>}.
     *
     * @param <K> The type of the keys.
     * @param <V> The type of the values.
     * @return A map.
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryToMap() {
        return toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * A {@link Collector} that filters elements based on a predicate before passing them to a downstream collector.
     *
     * @param predicate  The predicate to apply to each element.
     * @param downstream The downstream collector.
     * @param <T>        The type of the input elements.
     * @param <A>        The intermediate accumulation type of the downstream collector.
     * @param <R>        The result type of the downstream collector.
     * @return A collector which filters elements.
     */
    public static <T, A, R> Collector<T, ?, R> filtering(
            final Predicate<? super T> predicate,
            final Collector<? super T, A, R> downstream) {
        final BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        return new SimpleCollector<>(downstream.supplier(),
                (r, t) -> Optional.of(t).filter(predicate).ifPresent(e -> downstreamAccumulator.accept(r, e)),
                downstream.combiner(), downstream.finisher(), downstream.characteristics());
    }

    /**
     * A {@link Collector} that partitions a stream into two separate `List`s based on two mapping functions, returning
     * the result as a {@link Pair}.
     *
     * @param lMapper The mapping function for the left list.
     * @param rMapper The mapping function for the right list.
     * @param <T>     The type of the input elements.
     * @param <L>     The type of elements in the left list.
     * @param <R>     The type of elements in the right list.
     * @return A {@code Pair} containing the two lists.
     */
    public static <T, L, R> Collector<T, ?, Pair<List<L>, List<R>>> toPairList(
            final Function<? super T, ? extends L> lMapper,
            final Function<? super T, ? extends R> rMapper) {
        return toPair(lMapper, rMapper, Collectors.toList(), Collectors.toList());
    }

    /**
     * A {@link Collector} that partitions a stream into two separate collections based on two mapping functions and
     * downstream collectors, returning the result as a {@link Pair}.
     *
     * @param lMapper     The mapping function for the left element.
     * @param rMapper     The mapping function for the right element.
     * @param lDownstream The downstream collector for the left side.
     * @param rDownstream The downstream collector for the right side.
     * @param <T>         The type of the input elements.
     * @param <LU>        The type of the left element before downstream collection.
     * @param <LA>        The intermediate accumulation type of the left collector.
     * @param <LR>        The final result type of the left collector.
     * @param <RU>        The type of the right element before downstream collection.
     * @param <RA>        The intermediate accumulation type of the right collector.
     * @param <RR>        The final result type of the right collector.
     * @return A {@code Pair} containing the two collected results.
     */
    public static <T, LU, LA, LR, RU, RA, RR> Collector<T, ?, Pair<LR, RR>> toPair(
            final Function<? super T, ? extends LU> lMapper,
            final Function<? super T, ? extends RU> rMapper,
            final Collector<? super LU, LA, LR> lDownstream,
            final Collector<? super RU, RA, RR> rDownstream) {
        return new SimpleCollector<>(() -> Pair.of(lDownstream.supplier().get(), rDownstream.supplier().get()),

                (listPair, element) -> {
                    lDownstream.accumulator().accept(listPair.getLeft(), lMapper.apply(element));
                    rDownstream.accumulator().accept(listPair.getRight(), rMapper.apply(element));
                },

                (listPair1, listPair2) -> Pair.of(
                        lDownstream.combiner().apply(listPair1.getLeft(), listPair2.getLeft()),
                        rDownstream.combiner().apply(listPair1.getRight(), listPair2.getRight())),

                finisherPair -> {
                    final LR finisherLeftValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherLeftValue = (LR) finisherPair.getLeft();
                    } else {
                        finisherLeftValue = lDownstream.finisher().apply(finisherPair.getLeft());
                    }

                    final RR finisherRightValue;
                    if (rDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherRightValue = (RR) finisherPair.getRight();
                    } else {
                        finisherRightValue = rDownstream.finisher().apply(finisherPair.getRight());
                    }

                    return Pair.of(finisherLeftValue, finisherRightValue);
                }, CH_NOID);
    }

    /**
     * A {@link Collector} that partitions a stream into three separate `List`s based on three mapping functions,
     * returning the result as a {@link Triplet}.
     *
     * @param lMapper The mapping function for the left list.
     * @param mMapper The mapping function for the middle list.
     * @param rMapper The mapping function for the right list.
     * @param <T>     The type of the input elements.
     * @param <L>     The type of elements in the left list.
     * @param <M>     The type of elements in the middle list.
     * @param <R>     The type of elements in the right list.
     * @return A {@code Triplet} containing the three lists.
     */
    public static <T, L, M, R> Collector<T, ?, Triplet<List<L>, List<M>, List<R>>> toTripletList(
            final Function<? super T, ? extends L> lMapper,
            final Function<? super T, ? extends M> mMapper,
            final Function<? super T, ? extends R> rMapper) {
        return toTriplet(lMapper, mMapper, rMapper, Collectors.toList(), Collectors.toList(), Collectors.toList());
    }

    /**
     * A {@link Collector} that partitions a stream into three separate collections based on three mapping functions and
     * downstream collectors, returning the result as a {@link Triplet}.
     *
     * @param lMapper     The mapping function for the left element.
     * @param mMapper     The mapping function for the middle element.
     * @param rMapper     The mapping function for the right element.
     * @param lDownstream The downstream collector for the left side.
     * @param mDownstream The downstream collector for the middle side.
     * @param rDownstream The downstream collector for the right side.
     * @param <T>         The type of the input elements.
     * @param <LU>        The type of the left element.
     * @param <LA>        ... (Types for left downstream collector)
     * @param <LR>
     * @param <MU>        The type of the middle element.
     * @param <MA>        ... (Types for middle downstream collector)
     * @param <MR>
     * @param <RU>        The type of the right element.
     * @param <RA>        ... (Types for right downstream collector)
     * @param <RR>
     * @return A {@code Triplet} containing the three collected results.
     */
    public static <T, LU, LA, LR, MU, MA, MR, RU, RA, RR> Collector<T, ?, Triplet<LR, MR, RR>> toTriplet(
            final Function<? super T, ? extends LU> lMapper,
            final Function<? super T, ? extends MU> mMapper,
            final Function<? super T, ? extends RU> rMapper,
            final Collector<? super LU, LA, LR> lDownstream,
            final Collector<? super MU, MA, MR> mDownstream,
            final Collector<? super RU, RA, RR> rDownstream) {
        return new SimpleCollector<>(
                () -> Triplet
                        .of(lDownstream.supplier().get(), mDownstream.supplier().get(), rDownstream.supplier().get()),

                (listTriple, element) -> {
                    lDownstream.accumulator().accept(listTriple.getLeft(), lMapper.apply(element));
                    mDownstream.accumulator().accept(listTriple.getMiddle(), mMapper.apply(element));
                    rDownstream.accumulator().accept(listTriple.getRight(), rMapper.apply(element));
                },

                (listTriple1, listTriple2) -> Triplet.of(
                        lDownstream.combiner().apply(listTriple1.getLeft(), listTriple2.getLeft()),
                        mDownstream.combiner().apply(listTriple1.getMiddle(), listTriple2.getMiddle()),
                        rDownstream.combiner().apply(listTriple1.getRight(), listTriple2.getRight())),

                finisherTriple -> {
                    final LR finisherLeftValue;
                    if (lDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherLeftValue = (LR) finisherTriple.getLeft();
                    } else {
                        finisherLeftValue = lDownstream.finisher().apply(finisherTriple.getLeft());
                    }

                    final MR finisherMiddleValue;
                    if (mDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherMiddleValue = (MR) finisherTriple.getMiddle();
                    } else {
                        finisherMiddleValue = mDownstream.finisher().apply(finisherTriple.getMiddle());
                    }

                    final RR finisherRightValue;
                    if (rDownstream.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH)) {
                        finisherRightValue = (RR) finisherTriple.getRight();
                    } else {
                        finisherRightValue = rDownstream.finisher().apply(finisherTriple.getRight());
                    }

                    return Triplet.of(finisherLeftValue, finisherMiddleValue, finisherRightValue);
                }, CH_NOID);
    }

}
