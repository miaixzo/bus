/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.CollectionOperation;
import org.miaixz.bus.core.center.CollectionStream;
import org.miaixz.bus.core.center.TransCollection;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.Consumer3X;
import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.center.iterator.IteratorEnumeration;
import org.miaixz.bus.core.center.set.UniqueKeySet;
import org.miaixz.bus.core.codec.hash.Hash32;
import org.miaixz.bus.core.compare.PinyinCompare;
import org.miaixz.bus.core.compare.PropertyCompare;
import org.miaixz.bus.core.convert.CompositeConverter;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * A utility class for {@link Collection} and {@link Iterable} operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollKit extends CollectionStream {

    /**
     * Constructs a new CollKit. Utility class constructor for static access.
     */
    private CollKit() {
    }

    /**
     * Returns a `Predicate` that maintains state for filtering distinct elements based on a key extractor.
     *
     * @param <T> The type of the elements.
     * @param key The key extractor function.
     * @return A {@link Predicate} for distinct filtering.
     */
    public static <T> Predicate<T> distinct(final Function<? super T, ?> key) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(key.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Removes duplicate elements from a collection, returning a new {@link List} with unique elements.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @return A new {@link List} with distinct elements.
     */
    public static <T> List<T> distinct(final Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        } else if (collection instanceof Set) {
            return new ArrayList<>(collection);
        } else {
            return new ArrayList<>(new LinkedHashSet<>(collection));
        }
    }

    /**
     * Removes duplicate elements from a collection based on a key extractor. Allows specifying whether to keep the
     * first or last encountered element for a given key.
     *
     * @param <T>        The type of the elements.
     * @param <K>        The type of the unique key.
     * @param collection The collection.
     * @param key        The function to extract the unique key.
     * @param override   If {@code true}, new values will override old values with the same key; otherwise, new values
     *                   are ignored.
     * @return A new {@link List} with distinct elements.
     */
    public static <T, K> List<T> distinct(
            final Collection<T> collection,
            final Function<T, K> key,
            final boolean override) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }

        final UniqueKeySet<K, T> set = new UniqueKeySet<>(true, key);
        if (override) {
            set.addAll(collection);
        } else {
            set.addAllIfAbsent(collection);
        }
        return new ArrayList<>(set);
    }

    /**
     * Returns the union of multiple collections. If an element appears multiple times, the count is the maximum of its
     * counts in any of the collections.
     *
     * @param <T>   The type of the elements.
     * @param colls The collections.
     * @return The union of the collections as an {@link ArrayList}.
     */
    @SafeVarargs
    public static <T> Collection<T> union(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).union();
    }

    /**
     * Returns the distinct union of multiple collections (like SQL's "UNION DISTINCT"). Only one instance of each
     * element is kept.
     *
     * @param <T>   The type of the elements.
     * @param colls The collections.
     * @return The distinct union as a {@link LinkedHashSet}.
     */
    @SafeVarargs
    public static <T> Set<T> unionDistinct(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).unionDistinct();
    }

    /**
     * Returns the full union of multiple collections (like SQL's "UNION ALL"). All elements from all collections are
     * included.
     *
     * @param <T>   The type of the elements.
     * @param colls The collections.
     * @return The full union as an {@link ArrayList}.
     */
    @SafeVarargs
    public static <T> List<T> unionAll(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).unionAll();
    }

    /**
     * Returns the intersection of multiple collections. The count of each element in the result is the minimum of its
     * counts in any of the collections.
     *
     * @param <T>   The type of the elements.
     * @param colls The collections.
     * @return The intersection as an {@link ArrayList}.
     */
    @SafeVarargs
    public static <T> Collection<T> intersection(final Collection<T>... colls) {
        return CollectionOperation.of(colls).intersection();
    }

    /**
     * Returns the distinct intersection of multiple collections. Only one instance of each common element is kept.
     *
     * @param <T>   The type of the elements.
     * @param colls The collections.
     * @return The distinct intersection as a {@link LinkedHashSet}.
     */
    @SafeVarargs
    public static <T> Set<T> intersectionDistinct(final Collection<T>... colls) {
        return CollectionOperation.of(colls).intersectionDistinct();
    }

    /**
     * Calculates the symmetric difference (disjunction) of two collections: (A-B) ∪ (B-A).
     *
     * @param <T>   The type of the elements.
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return The disjunction as an {@link ArrayList}.
     */
    public static <T> Collection<T> disjunction(final Collection<T> coll1, final Collection<T> coll2) {
        return CollectionOperation.of(coll1, coll2).disjunction();
    }

    /**
     * Calculates the subtract of `coll2` from `coll1` (coll1 - coll2). Example: `subtract([1,2,3,4], [2,3,4,5])` ->
     * `[1]`
     *
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @param <T>   The type of the elements.
     * @return A new collection containing the result of the subtraction.
     */
    public static <T> Collection<T> subtract(final Collection<T> coll1, final Collection<T> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return coll1;
        }
        Collection<T> result = ObjectKit.clone(coll1);
        try {
            if (null == result) {
                result = of(coll1.getClass());
                result.addAll(coll1);
            }
            result.removeAll(coll2);
        } catch (final UnsupportedOperationException e) {
            // Handle read-only collections
            result = of(AbstractCollection.class);
            result.addAll(coll1);
            result.removeAll(coll2);
        }
        return result;
    }

    /**
     * Calculates the subtract of `coll2` from `coll1` and returns it as a `List`.
     *
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @param <T>   The type of the elements.
     * @return The result of the subtraction as a `List`.
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2) {
        return subtractToList(coll1, coll2, true);
    }

    /**
     * Calculates the subtract of `coll2` from `coll1` and returns it as a `List`.
     *
     * @param coll1    The collection to subtract from.
     * @param coll2    The collection of elements to remove.
     * @param isLinked If true, returns a {@link LinkedList}; otherwise, returns an {@link ArrayList}.
     * @param <T>      The type of the elements.
     * @return The result of the subtraction.
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2, boolean isLinked) {
        if (isEmpty(coll1)) {
            return ListKit.empty();
        }

        if (isEmpty(coll2)) {
            return isLinked ? new LinkedList<>(coll1) : new ArrayList<>(coll1);
        }

        final List<T> result = isLinked ? new LinkedList<>() : new ArrayList<>(coll1.size());
        Set<T> set = new HashSet<>(coll2);
        for (T t : coll1) {
            if (!set.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Checks if a collection contains a specific value.
     *
     * @param collection The collection.
     * @param value      The value to find.
     * @return {@code true} if the value is found.
     * @see Collection#contains(Object)
     */
    public static boolean contains(final Collection<?> collection, final Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

    /**
     * Safely checks if a collection contains a value, catching `ClassCastException` and `NullPointerException`.
     *
     * @param collection The collection.
     * @param value      The value to find.
     * @return {@code true} if the value is found.
     */
    public static boolean safeContains(final Collection<?> collection, final Object value) {
        try {
            return contains(collection, value);
        } catch (final ClassCastException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Checks if a collection contains any element matching a given predicate.
     *
     * @param collection  The collection.
     * @param containFunc The predicate.
     * @param <T>         The type of the elements.
     * @return {@code true} if any element matches.
     */
    public static <T> boolean contains(final Collection<T> collection, final Predicate<? super T> containFunc) {
        if (isEmpty(collection)) {
            return false;
        }
        for (final T t : collection) {
            if (containFunc.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two collections have at least one element in common.
     *
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return {@code true} if there is at least one common element.
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        final boolean isFirstSmaller = coll1.size() <= coll2.size();
        final Collection<?> smallerColl = isFirstSmaller ? coll1 : coll2;
        final Set<?> biggerSet = isFirstSmaller ? new HashSet<>(coll2) : new HashSet<>(coll1);
        for (final Object object : smallerColl) {
            if (biggerSet.contains(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if `coll1` contains all elements of `coll2`.
     *
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return {@code true} if `coll1` contains all elements of `coll2`.
     */
    public static boolean containsAll(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1)) {
            return isEmpty(coll2);
        }
        if (isEmpty(coll2)) {
            return true;
        }
        if (coll1 instanceof Set) {
            return coll1.containsAll(coll2);
        }

        final Iterator<?> it = coll1.iterator();
        final Set<Object> elementsAlreadySeen = new HashSet<>(coll1.size(), 1);
        for (final Object nextElement : coll2) {
            if (elementsAlreadySeen.contains(nextElement)) {
                continue;
            }

            boolean foundCurrentElement = false;
            while (it.hasNext()) {
                final Object p = it.next();
                elementsAlreadySeen.add(p);
                if (Objects.equals(nextElement, p)) {
                    foundCurrentElement = true;
                    break;
                }
            }
            if (!foundCurrentElement) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a `Map` where keys are the elements in the collection and values are their frequency counts.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @return A frequency map.
     * @see IteratorKit#countMap(Iterator)
     */
    public static <T> Map<T, Integer> countMap(final Iterable<T> collection) {
        return IteratorKit.countMap(IteratorKit.getIter(collection));
    }

    /**
     * Joins the elements of an `Iterable` into a string with a separator, using a custom function to convert elements
     * to strings.
     *
     * @param <T>         The type of the elements.
     * @param iterable    The `Iterable`.
     * @param conjunction The separator.
     * @param func        The function to convert elements to strings.
     * @return The joined string.
     * @see IteratorKit#join(Iterator, CharSequence, Function)
     */
    public static <T> String join(
            final Iterable<T> iterable,
            final CharSequence conjunction,
            final Function<T, ? extends CharSequence> func) {
        return IteratorKit.join(IteratorKit.getIter(iterable), conjunction, func);
    }

    /**
     * Joins the elements of an `Iterable` into a string with a separator.
     *
     * @param <T>         The type of the elements.
     * @param iterable    The `Iterable`.
     * @param conjunction The separator.
     * @return The joined string.
     * @see IteratorKit#join(Iterator, CharSequence)
     */
    public static <T> String join(final Iterable<T> iterable, final CharSequence conjunction) {
        if (null == iterable) {
            return null;
        }
        return IteratorKit.join(iterable.iterator(), conjunction);
    }

    /**
     * Joins the elements of an `Iterable` into a string with a separator, prefix, and suffix.
     *
     * @param <T>         The type of the elements.
     * @param iterable    The `Iterable`.
     * @param conjunction The separator.
     * @param prefix      The prefix for each element.
     * @param suffix      The suffix for each element.
     * @return The joined string.
     */
    public static <T> String join(
            final Iterable<T> iterable,
            final CharSequence conjunction,
            final String prefix,
            final String suffix) {
        if (null == iterable) {
            return null;
        }
        return IteratorKit.join(iterable.iterator(), conjunction, prefix, suffix);
    }

    /**
     * Pops a specified number of elements from the top of a `Stack` and returns them as a `List`.
     *
     * @param <T>      The type of the elements.
     * @param stack    The stack.
     * @param partSize The number of elements to pop.
     * @return A `List` of the popped elements.
     */
    public static <T> List<T> popPart(final Stack<T> stack, final int partSize) {
        if (isEmpty(stack)) {
            return ListKit.empty();
        }
        final int popSize = Math.min(partSize, stack.size());
        final List<T> resultList = new ArrayList<>(popSize);
        for (int i = 0; i < popSize; i++) {
            resultList.add(stack.pop());
        }
        return resultList;
    }

    /**
     * Pops a specified number of elements from a `Deque` and returns them as a `List`.
     *
     * @param <T>      The type of the elements.
     * @param deque    The deque.
     * @param partSize The number of elements to pop.
     * @return A `List` of the popped elements.
     */
    public static <T> List<T> popPart(final Deque<T> deque, final int partSize) {
        if (isEmpty(deque)) {
            return ListKit.empty();
        }
        final int popSize = Math.min(partSize, deque.size());
        final List<T> resultList = new ArrayList<>(popSize);
        for (int i = 0; i < popSize; i++) {
            resultList.add(deque.pop());
        }
        return resultList;
    }

    /**
     * Creates a new {@link BlockingQueue}.
     *
     * @param <T>      The type of the elements.
     * @param capacity The capacity of the queue.
     * @param isLinked If true, creates a `LinkedBlockingDeque`; otherwise, creates an `ArrayBlockingQueue`.
     * @return A new {@link BlockingQueue}.
     */
    public static <T> BlockingQueue<T> newBlockingQueue(final int capacity, final boolean isLinked) {
        final BlockingQueue<T> queue;
        if (isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }

    /**
     * Returns an appropriate empty, unmodifiable collection for a given collection class type. Supports `NavigableSet`,
     * `SortedSet`, `Set`, and `List`.
     *
     * @param <E>             The type of the elements.
     * @param <T>             The type of the collection.
     * @param collectionClass The collection class.
     * @return An empty collection.
     */
    public static <E, T extends Collection<E>> T empty(final Class<?> collectionClass) {
        if (null == collectionClass) {
            return (T) Collections.emptyList();
        }

        if (Set.class.isAssignableFrom(collectionClass)) {
            if (NavigableSet.class == collectionClass) {
                return (T) Collections.emptyNavigableSet();
            } else if (SortedSet.class == collectionClass) {
                return (T) Collections.emptySortedSet();
            } else {
                return (T) Collections.emptySet();
            }
        } else if (List.class.isAssignableFrom(collectionClass)) {
            return (T) Collections.emptyList();
        }

        throw new IllegalArgumentException(StringKit.format("[{}] is not support to get empty!", collectionClass));
    }

    /**
     * Creates a new, mutable collection instance of a specific type.
     *
     * @param <T>            The type of the elements.
     * @param collectionType The collection type (e.g., `ArrayList.class`).
     * @return A new instance of the collection.
     */
    public static <T> Collection<T> of(final Class<?> collectionType) {
        final Collection<T> list;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>();
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>();
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>((o1, o2) -> {
                if (o1 instanceof Comparable) {
                    return ((Comparable<T>) o1).compareTo(o2);
                }
                return CompareKit.compare(o1.toString(), o2.toString());
            });
        } else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        } else {
            try {
                list = (Collection<T>) ReflectKit.newInstance(collectionType);
            } catch (final Exception e) {
                final Class<?> superclass = collectionType.getSuperclass();
                if (null != superclass && collectionType != superclass) {
                    return of(superclass);
                }
                throw ExceptionKit.wrapRuntime(e);
            }
        }
        return list;
    }

    /**
     * Creates a new, mutable collection instance of a specific type.
     *
     * @param <T>            The type of the elements.
     * @param collectionType The collection type.
     * @param elementType    The element type, required for creating an `EnumSet`.
     * @return A new instance of the collection.
     */
    public static <T> Collection<T> of(final Class<?> collectionType, final Class<T> elementType) {
        if (EnumSet.class.isAssignableFrom(collectionType)) {
            return (Collection<T>) EnumSet.noneOf((Class<Enum>) Assert.notNull(elementType));
        }
        return of(collectionType);
    }

    /**
     * Returns a view of the portion of this list between the specified `fromIndex` (inclusive) and `toIndex`
     * (exclusive).
     *
     * @param <T>       The type of the elements.
     * @param list      The list to slice.
     * @param fromIndex The start index (inclusive).
     * @param toIndex   The end index (exclusive).
     * @return The sublist.
     * @see ListKit#sub(List, int, int)
     */
    public static <T> List<T> sub(final List<T> list, final int fromIndex, final int toIndex) {
        return ListKit.sub(list, fromIndex, toIndex);
    }

    /**
     * Returns a view of the portion of this list with a given step.
     *
     * @param <T>       The type of the elements.
     * @param list      The list to slice.
     * @param fromIndex The start index (inclusive).
     * @param toIndex   The end index (exclusive).
     * @param step      The step size.
     * @return The sublist.
     * @see ListKit#sub(List, int, int, int)
     */
    public static <T> List<T> sub(final List<T> list, final int fromIndex, final int toIndex, final int step) {
        return ListKit.sub(list, fromIndex, toIndex, step);
    }

    /**
     * Slices a portion of a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection to slice.
     * @param fromIndex  The start index (inclusive).
     * @param toIndex    The end index (exclusive).
     * @return The sliced list.
     */
    public static <T> List<T> sub(final Collection<T> collection, final int fromIndex, final int toIndex) {
        return sub(collection, fromIndex, toIndex, 1);
    }

    /**
     * Slices a portion of a collection with a given step.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection to slice.
     * @param fromIndex  The start index (inclusive).
     * @param toIndex    The end index (exclusive).
     * @param step       The step size.
     * @return The sliced list.
     */
    public static <T> List<T> sub(
            final Collection<T> collection,
            final int fromIndex,
            final int toIndex,
            final int step) {
        if (isEmpty(collection)) {
            return ListKit.empty();
        }
        final List<T> list = collection instanceof List ? (List<T>) collection : ListKit.of(collection);
        return sub(list, fromIndex, toIndex, step);
    }

    /**
     * Partitions a collection into a `List` of sub-lists, each of a specified size.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param size       The size of each partition.
     * @return A list of sub-lists.
     */
    public static <T> List<List<T>> partition(final Collection<T> collection, final int size) {
        final List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        final int initSize = Math.min(collection.size(), size);
        ArrayList<T> subList = new ArrayList<>(initSize);
        for (final T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(initSize);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    /**
     * Creates a new collection by applying an editor function to each element. The function can modify elements or
     * filter them out by returning null.
     *
     * @param <T>        The type of the collection.
     * @param <E>        The type of the elements.
     * @param collection The collection.
     * @param editor     The editor function.
     * @return A new, edited collection.
     */
    public static <T extends Collection<E>, E> T edit(final T collection, final UnaryOperator<E> editor) {
        if (null == collection || null == editor) {
            return collection;
        }
        final T collection2 = (T) of(collection.getClass());
        if (isEmpty(collection)) {
            return collection2;
        }
        E modified;
        for (final E t : collection) {
            modified = editor.apply(t);
            if (null != modified) {
                collection2.add(modified);
            }
        }
        return collection2;
    }

    /**
     * Filters a collection based on a predicate, returning a new collection.
     *
     * @param <T>        The type of the collection.
     * @param <E>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The filter predicate.
     * @return A new, filtered collection.
     */
    public static <T extends Collection<E>, E> T filter(final T collection, final Predicate<E> predicate) {
        if (null == collection || null == predicate) {
            return collection;
        }
        return edit(collection, t -> predicate.test(t) ? t : null);
    }

    /**
     * Removes multiple specified elements from a collection (modifies the original collection).
     *
     * @param <T>         The type of the collection.
     * @param <E>         The type of the elements.
     * @param collection  The collection.
     * @param elesRemoved The elements to remove.
     * @return The modified collection.
     */
    public static <T extends Collection<E>, E> T removeAny(final T collection, final E... elesRemoved) {
        collection.removeAll(SetKit.of(elesRemoved));
        return collection;
    }

    /**
     * Removes all elements from an iterable that satisfy a given predicate (modifies the original).
     *
     * @param <T>       The type of the iterable.
     * @param <E>       The type of the elements.
     * @param iter      The iterable.
     * @param predicate The predicate.
     * @return The modified iterable.
     */
    public static <T extends Iterable<E>, E> T remove(final T iter, final Predicate<E> predicate) {
        if (null == iter) {
            return null;
        }
        IteratorKit.remove(iter.iterator(), predicate);
        return iter;
    }

    /**
     * Removes all `null` elements from a collection (modifies the original).
     *
     * @param <T>        The type of the collection.
     * @param <E>        The type of the elements.
     * @param collection The collection.
     * @return The modified collection.
     */
    public static <T extends Collection<E>, E> T removeNull(final T collection) {
        return remove(collection, Objects::isNull);
    }

    /**
     * Removes all empty (`""`) or `null` elements from a collection (modifies the original).
     *
     * @param <T>        The type of the collection.
     * @param <E>        The type of the CharSequence elements.
     * @param collection The collection.
     * @return The modified collection.
     */
    public static <T extends Collection<E>, E extends CharSequence> T removeEmpty(final T collection) {
        return remove(collection, StringKit::isEmpty);
    }

    /**
     * Removes all blank (`""`, `null`, or whitespace-only) elements from a collection (modifies the original).
     *
     * @param <T>        The type of the collection.
     * @param <E>        The type of the CharSequence elements.
     * @param collection The collection.
     * @return The modified collection.
     */
    public static <T extends Collection<E>, E extends CharSequence> T removeBlank(final T collection) {
        return remove(collection, StringKit::isBlank);
    }

    /**
     * Removes elements from `targetCollection` if they match the predicate, and adds them to `resultCollection`.
     *
     * @param <T>              The type of the collection.
     * @param <E>              The type of the elements.
     * @param resultCollection The collection to store removed elements.
     * @param targetCollection The collection to remove elements from.
     * @param predicate        The predicate to determine removal.
     * @return The `resultCollection`.
     */
    public static <T extends Collection<E>, E> T removeWithAddIf(
            final T targetCollection,
            final T resultCollection,
            final Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate);
        final Iterator<E> each = targetCollection.iterator();
        while (each.hasNext()) {
            final E next = each.next();
            if (predicate.test(next)) {
                resultCollection.add(next);
                each.remove();
            }
        }
        return resultCollection;
    }

    /**
     * Removes elements from `targetCollection` if they match the predicate and returns them in a new `List`.
     *
     * @param <T>              The type of the collection.
     * @param <E>              The type of the elements.
     * @param targetCollection The collection to remove elements from.
     * @param predicate        The predicate to determine removal.
     * @return A new `List` of the removed elements.
     */
    public static <T extends Collection<E>, E> List<E> removeWithAddIf(
            final T targetCollection,
            final Predicate<? super E> predicate) {
        final List<E> removed = new ArrayList<>();
        removeWithAddIf(targetCollection, removed, predicate);
        return removed;
    }

    /**
     * Applies a function to each element of a collection, returning a new `List` of the results. `null` values after
     * mapping are ignored by default.
     *
     * @param <T>        The type of the source elements.
     * @param <R>        The type of the result elements.
     * @param collection The source collection.
     * @param func       The mapping function.
     * @return A new `List` of mapped elements.
     */
    public static <T, R> List<R> map(final Iterable<T> collection, final Function<? super T, ? extends R> func) {
        return map(collection, func, true);
    }

    /**
     * Applies a function to each element of a collection, returning a new `List` of the results.
     *
     * @param <T>        The type of the source elements.
     * @param <R>        The type of the result elements.
     * @param collection The source collection.
     * @param mapper     The mapping function.
     * @param ignoreNull If true, ignores `null` values both before and after mapping.
     * @return A new `List` of mapped elements.
     */
    public static <T, R> List<R> map(
            final Iterable<T> collection,
            final Function<? super T, ? extends R> mapper,
            final boolean ignoreNull) {
        if (ignoreNull) {
            return StreamKit.of(collection).filter(Objects::nonNull).map(mapper).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return StreamKit.of(collection).map(mapper).collect(Collectors.toList());
    }

    /**
     * Extracts the values of a specific property/field from a collection of beans or maps.
     *
     * @param collection The collection of beans or maps.
     * @param fieldName  The name of the field or map key.
     * @return A list of the property values.
     */
    public static Collection<Object> getFieldValues(final Iterable<?> collection, final String fieldName) {
        return getFieldValues(collection, fieldName, false);
    }

    /**
     * Extracts the values of a specific property/field from a collection of beans or maps.
     *
     * @param collection The collection of beans or maps.
     * @param fieldName  The name of the field or map key.
     * @param ignoreNull If true, ignores `null` property values.
     * @return A list of the property values.
     */
    public static List<Object> getFieldValues(
            final Iterable<?> collection,
            final String fieldName,
            final boolean ignoreNull) {
        return map(collection, bean -> {
            if (bean instanceof Map) {
                return ((Map<?, ?>) bean).get(fieldName);
            } else {
                return FieldKit.getFieldValue(bean, fieldName);
            }
        }, ignoreNull);
    }

    /**
     * Extracts the values of a specific property/field from a collection of beans or maps, converting them to a
     * specific type.
     *
     * @param <T>         The target element type.
     * @param collection  The collection of beans or maps.
     * @param fieldName   The name of the field or map key.
     * @param elementType The target class of the elements.
     * @return A list of the property values.
     */
    public static <T> List<T> getFieldValues(
            final Iterable<?> collection,
            final String fieldName,
            final Class<T> elementType) {
        final Collection<Object> fieldValues = getFieldValues(collection, fieldName);
        return Convert.toList(elementType, fieldValues);
    }

    /**
     * Creates a `Map` from a collection, where the key is a specified property of the element and the value is the
     * element itself.
     *
     * @param <K>       The type of the key (property value).
     * @param <V>       The type of the element.
     * @param iterable  The collection of objects.
     * @param fieldName The name of the property to use as the key.
     * @return A `Map` from property value to object.
     */
    public static <K, V> Map<K, V> fieldValueMap(final Iterable<V> iterable, final String fieldName) {
        return IteratorKit.fieldValueMap(IteratorKit.getIter(iterable), fieldName);
    }

    /**
     * Creates a `Map` from a collection by extracting two properties from each element, one for the key and one for the
     * value.
     *
     * @param <K>               The type of the key.
     * @param <V>               The type of the value.
     * @param iterable          The collection of objects.
     * @param fieldNameForKey   The property name for the map key.
     * @param fieldNameForValue The property name for the map value.
     * @return A `Map` created from the properties.
     */
    public static <K, V> Map<K, V> fieldValueAsMap(
            final Iterable<?> iterable,
            final String fieldNameForKey,
            final String fieldNameForValue) {
        return IteratorKit.fieldValueAsMap(IteratorKit.getIter(iterable), fieldNameForKey, fieldNameForValue);
    }

    /**
     * Gets the first element of an `Iterable`. Returns `null` if the iterable is empty.
     *
     * @param <T>      The type of the elements.
     * @param iterable The `Iterable`.
     * @return The first element, or `null`.
     */
    public static <T> T getFirst(final Iterable<T> iterable) {
        if (iterable instanceof final List<T> list) {
            return isEmpty(list) ? null : list.get(0);
        }
        return IteratorKit.getFirst(IteratorKit.getIter(iterable));
    }

    /**
     * Gets the first non-null element of an `Iterable`.
     *
     * @param <T>      The type of the elements.
     * @param iterable The `Iterable`.
     * @return The first non-null element.
     */
    public static <T> T getFirstNoneNull(final Iterable<T> iterable) {
        return IteratorKit.getFirstNoneNull(IteratorKit.getIter(iterable));
    }

    /**
     * Finds the first element in a collection that matches a given predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate to match.
     * @return The first matching element.
     */
    public static <T> T getFirst(final Iterable<T> collection, final Predicate<T> predicate) {
        return IteratorKit.getFirst(IteratorKit.getIter(collection), predicate);
    }

    /**
     * Finds the first element in a collection (of beans or maps) where a specific property matches a given value.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param fieldName  The name of the field or map key.
     * @param fieldValue The value to match.
     * @return The first matching element.
     */
    public static <T> T getFirstByField(final Iterable<T> collection, final String fieldName, final Object fieldValue) {
        return getFirst(collection, t -> {
            if (t instanceof final Map<?, ?> map) {
                final Object value = map.get(fieldName);
                return ObjectKit.equals(value, fieldValue);
            }
            final Object value = FieldKit.getFieldValue(t, fieldName);
            return ObjectKit.equals(value, fieldValue);
        });
    }

    /**
     * Counts the number of elements in an iterable that match a given predicate.
     *
     * @param <T>       The type of the elements.
     * @param iterable  The `Iterable`.
     * @param predicate The predicate to match.
     * @return The count of matching elements.
     */
    public static <T> int count(final Iterable<T> iterable, final Predicate<T> predicate) {
        int count = 0;
        if (null != iterable) {
            for (final T t : iterable) {
                if (null == predicate || predicate.test(t)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets the index of the first element that matches a predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate to match.
     * @return The index of the first match, or -1 if not found.
     */
    public static <T> int indexOf(final Collection<T> collection, final Predicate<T> predicate) {
        if (isNotEmpty(collection)) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * Gets the index of the last element that matches a predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate to match.
     * @return The index of the last match, or -1 if not found.
     */
    public static <T> int lastIndexOf(final Collection<T> collection, final Predicate<? super T> predicate) {
        if (collection instanceof List) {
            return ListKit.lastIndexOf((List<T>) collection, predicate);
        }
        int matchIndex = -1;
        if (isNotEmpty(collection)) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    matchIndex = index;
                }
                index++;
            }
        }
        return matchIndex;
    }

    /**
     * Gets the indices of all elements that match a predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate to match.
     * @return An array of matching indices.
     */
    public static <T> int[] indexOfAll(final Collection<T> collection, final Predicate<T> predicate) {
        return Convert.convert(int[].class, indexListOfAll(collection, predicate));
    }

    /**
     * Gets a list of indices of all elements that match a predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate to match.
     * @return A list of matching indices.
     */
    public static <T> List<Integer> indexListOfAll(final Collection<T> collection, final Predicate<T> predicate) {
        final List<Integer> indexList = new ArrayList<>();
        if (null != collection) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    indexList.add(index);
                }
                index++;
            }
        }
        return indexList;
    }

    /**
     * Creates a map from two delimited strings (like Python's `zip()` function).
     *
     * @param keys      A delimited string of keys.
     * @param values    A delimited string of values.
     * @param delimiter The delimiter.
     * @param isOrder   If true, returns a `LinkedHashMap`.
     * @return The resulting map.
     */
    public static Map<String, String> zip(
            final String keys,
            final String values,
            final String delimiter,
            final boolean isOrder) {
        return ArrayKit
                .zip(CharsBacker.splitToArray(keys, delimiter), CharsBacker.splitToArray(values, delimiter), isOrder);
    }

    /**
     * Creates a map from two delimited strings (like Python's `zip()` function).
     *
     * @param keys      A delimited string of keys.
     * @param values    A delimited string of values.
     * @param delimiter The delimiter.
     * @return The resulting `HashMap`.
     */
    public static Map<String, String> zip(final String keys, final String values, final String delimiter) {
        return zip(keys, values, delimiter, false);
    }

    /**
     * Creates a map from a collection of keys and a collection of values (like Python's `zip()` function).
     *
     * @param <K>    The type of the keys.
     * @param <V>    The type of the values.
     * @param keys   The collection of keys.
     * @param values The collection of values.
     * @return The resulting map.
     */
    public static <K, V> Map<K, V> zip(final Collection<K> keys, final Collection<V> values) {
        if (isEmpty(keys) || isEmpty(values)) {
            return MapKit.empty();
        }

        int entryCount = Math.min(keys.size(), values.size());
        final Map<K, V> map = MapKit.newHashMap(entryCount);

        final Iterator<K> keyIterator = keys.iterator();
        final Iterator<V> valueIterator = values.iterator();
        while (entryCount-- > 0) {
            map.put(keyIterator.next(), valueIterator.next());
            entryCount--;
        }

        return map;
    }

    /**
     * Pairs elements from two lists by index, merges them through a specified function, and returns a new result list.
     * The length of the new list will be based on the shorter of the two input lists.
     *
     * @param <A>         Element type of the first list
     * @param <B>         Element type of the second list
     * @param <R>         Element type of the result list
     * @param collectionA The first list
     * @param collectionB The second list
     * @param zipper      Merge function that receives two elements from listA and listB, and returns a result element
     * @return The merged new list
     */
    public static <A, B, R> List<R> zip(
            Collection<A> collectionA,
            Collection<B> collectionB,
            BiFunction<A, B, R> zipper) {
        if (isEmpty(collectionA) || isEmpty(collectionB)) {
            return new ArrayList<>();
        }
        Assert.notNull(zipper, "Zipper function must not be null");

        int size = Math.min(collectionA.size(), collectionB.size());
        final List<R> result = new ArrayList<>(size);
        final Iterator<A> aIterator = collectionA.iterator();
        final Iterator<B> bIterator = collectionB.iterator();

        while (size-- > 0) {
            result.add(zipper.apply(aIterator.next(), bIterator.next()));
        }
        return result;
    }

    /**
     * Converts a collection to a sorted `TreeSet`.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param comparator The comparator.
     * @return A sorted `TreeSet`.
     */
    public static <T> TreeSet<T> toTreeSet(final Collection<T> collection, final Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        return treeSet;
    }

    /**
     * Adapts the specified {@link Iterator} to the {@link Enumeration} interface.
     *
     * @param <E>  The type of the elements.
     * @param iter The `Iterator`.
     * @return An `Enumeration`.
     */
    public static <E> Enumeration<E> asEnumeration(final Iterator<E> iter) {
        return new IteratorEnumeration<>(Objects.requireNonNull(iter));
    }

    /**
     * Converts an `Iterable` to a `Collection`.
     *
     * @param <E>      The type of the elements.
     * @param iterable The `Iterable`.
     * @return A `Collection`.
     */
    public static <E> Collection<E> toCollection(final Iterable<E> iterable) {
        return (iterable instanceof Collection) ? (Collection<E>) iterable : ListKit.of(IteratorKit.getIter(iterable));
    }

    /**
     * Pivots a list of maps (rows to columns).
     *
     * @param <K>     The type of the keys.
     * @param <V>     The type of the values.
     * @param mapList The list of maps.
     * @return A map where keys are the original keys and values are lists of original values.
     * @see MapKit#toListMap(Iterable)
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<? extends Map<K, V>> mapList) {
        return MapKit.toListMap(mapList);
    }

    /**
     * Pivots a map of lists (columns to rows).
     *
     * @param <K>     The type of the keys.
     * @param <V>     The type of the values.
     * @param listMap The map of lists.
     * @return A list of maps.
     * @see MapKit#toMapList(Map)
     */
    public static <K, V> List<Map<K, V>> toMapList(final Map<K, ? extends Iterable<V>> listMap) {
        return MapKit.toMapList(listMap);
    }

    /**
     * Adds all elements from a source object to a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The target collection.
     * @param value      The source object (can be Iterator, Iterable, Enumeration, Array).
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Object value) {
        return addAll(collection, value, TypeKit.getTypeArgument(collection.getClass()));
    }

    /**
     * Adds all elements from a source object to a collection, with type conversion.
     *
     * @param <T>         The type of the elements.
     * @param collection  The target collection.
     * @param value       The source object.
     * @param elementType The target element type.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Object value, Type elementType) {
        return addAll(collection, value, elementType, null);
    }

    /**
     * Adds all elements from a source object to a collection, with type conversion.
     *
     * @param <T>         The type of the elements.
     * @param collection  The target collection.
     * @param value       The source object.
     * @param elementType The target element type.
     * @param converter   A custom converter.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(
            final Collection<T> collection,
            final Object value,
            Type elementType,
            final Converter converter) {
        if (null == collection || null == value) {
            return collection;
        }
        if (TypeKit.isUnknown(elementType)) {
            elementType = Object.class;
        }

        final Iterator iter;
        if (value instanceof CharSequence) {
            final String arrayStr = StringKit.unWrap((CharSequence) value, '[', ']');
            iter = CharsBacker.splitTrim(arrayStr, Symbol.COMMA).iterator();
        } else if (value instanceof Map && BeanKit.isWritableBean(TypeKit.getClass(elementType))) {
            iter = new ArrayIterator(new Object[] { value });
        } else {
            iter = IteratorKit.getIter(value);
        }

        final Converter convert = ObjectKit.defaultIfNull(converter, CompositeConverter::getInstance);
        while (iter.hasNext()) {
            collection.add((T) convert.convert(elementType, iter.next()));
        }
        return collection;
    }

    /**
     * Adds all elements from an `Iterator` to a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The target collection.
     * @param iterator   The `Iterator`.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Iterator<T> iterator) {
        if (null != collection && null != iterator) {
            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        return collection;
    }

    /**
     * Adds all elements from an `Iterable` to a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The target collection.
     * @param iterable   The `Iterable`.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Iterable<T> iterable) {
        if (iterable == null) {
            return collection;
        }
        return addAll(collection, iterable.iterator());
    }

    /**
     * Adds all elements from an `Enumeration` to a collection.
     *
     * @param <T>         The type of the elements.
     * @param collection  The target collection.
     * @param enumeration The `Enumeration`.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Enumeration<T> enumeration) {
        if (null != collection && null != enumeration) {
            while (enumeration.hasMoreElements()) {
                collection.add(enumeration.nextElement());
            }
        }
        return collection;
    }

    /**
     * Adds all elements from an array to a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The target collection.
     * @param values     The array of values.
     * @return The modified collection.
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final T[] values) {
        if (null != collection && null != values) {
            Collections.addAll(collection, values);
        }
        return collection;
    }

    /**
     * Gets the last element of a collection.
     *
     * @param <T>        The type of the elements.
     * @param collection The `Collection`.
     * @return The last element.
     */
    public static <T> T getLast(final Collection<T> collection) {
        return get(collection, -1);
    }

    /**
     * Gets the element at a specific index, supporting negative indices (e.g., -1 for the last element).
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param index      The index (can be negative).
     * @return The element, or `null` if the index is out of bounds.
     */
    public static <T> T get(final Collection<T> collection, int index) {
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
        if (index >= size || index < 0) {
            return null;
        }
        if (collection instanceof final List<T> list) {
            return list.get(index);
        } else {
            return IteratorKit.get(collection.iterator(), index);
        }
    }

    /**
     * Gets elements at multiple specified indices, supporting negative indices.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param indexes    The indices.
     * @return A `List` of the elements.
     */
    public static <T> List<T> getAny(final Collection<T> collection, final int... indexes) {
        if (isEmpty(collection) || ArrayKit.isEmpty(indexes)) {
            return ListKit.zero();
        }
        final int size = collection.size();
        final List<T> result = new ArrayList<>(indexes.length);
        if (collection instanceof final List<T> list) {
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add(list.get(index));
            }
        } else {
            final Object[] array = collection.toArray();
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
     * Sorts a collection, returning a new sorted `List`.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param comparator The comparator.
     * @return A new sorted `List`.
     */
    public static <T> List<T> sort(final Collection<T> collection, final Comparator<? super T> comparator) {
        final List<T> list = new ArrayList<>(collection);
        list.sort(comparator);
        return list;
    }

    /**
     * Sorts a `List` in place.
     *
     * @param <T>  The type of the elements.
     * @param list The list to be sorted.
     * @param c    The `Comparator`.
     * @return The original list, now sorted.
     */
    public static <T> List<T> sort(final List<T> list, final Comparator<? super T> c) {
        return ListKit.sort(list, c);
    }

    /**
     * Sorts a collection of beans by a specified property.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param property   The property name.
     * @return A new sorted `List`.
     */
    public static <T> List<T> sortByProperty(final Collection<T> collection, final String property) {
        return sort(collection, new PropertyCompare<>(property));
    }

    /**
     * Sorts a list of beans by a specified property.
     *
     * @param <T>      The type of the elements.
     * @param list     The list.
     * @param property The property name.
     * @return The sorted list.
     */
    public static <T> List<T> sortByProperty(final List<T> list, final String property) {
        return ListKit.sortByProperty(list, property);
    }

    /**
     * Sorts a collection of strings based on their Pinyin (Chinese phonetic) order.
     *
     * @param collection The collection.
     * @return A new sorted `List`.
     */
    public static List<String> sortByPinyin(final Collection<String> collection) {
        return sort(collection, new PinyinCompare());
    }

    /**
     * Sorts a list of strings based on their Pinyin (Chinese phonetic) order.
     *
     * @param list The list.
     * @return The sorted list.
     */
    public static List<String> sortByPinyin(final List<String> list) {
        return ListKit.sortByPinyin(list);
    }

    /**
     * Sorts a `Map` by its keys.
     *
     * @param <K>        The type of the keys.
     * @param <V>        The type of the values.
     * @param map        The map.
     * @param comparator The key comparator.
     * @return A sorted `TreeMap`.
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map, final Comparator<? super K> comparator) {
        final TreeMap<K, V> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * Sorts a collection of map entries and returns a `LinkedHashMap`.
     *
     * @param <K>             The type of the keys.
     * @param <V>             The type of the values.
     * @param entryCollection The collection of entries.
     * @param comparator      The entry comparator.
     * @return A sorted `LinkedHashMap`.
     */
    public static <K, V> LinkedHashMap<K, V> sortToMap(
            final Collection<Map.Entry<K, V>> entryCollection,
            final Comparator<Map.Entry<K, V>> comparator) {
        final List<Map.Entry<K, V>> list = new LinkedList<>(entryCollection);
        list.sort(comparator);
        final LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Sorts a map by its entries.
     *
     * @param <K>        The type of the keys.
     * @param <V>        The type of the values.
     * @param map        The map to sort.
     * @param comparator The entry comparator.
     * @return A sorted `LinkedHashMap`.
     */
    public static <K, V> LinkedHashMap<K, V> sortByEntry(
            final Map<K, V> map,
            final Comparator<Map.Entry<K, V>> comparator) {
        return sortToMap(map.entrySet(), comparator);
    }

    /**
     * Sorts a collection of map entries by their values.
     *
     * @param <K>        The type of the keys.
     * @param <V>        The type of the values.
     * @param collection The collection of entries.
     * @return A sorted list of entries.
     */
    public static <K, V> List<Entry<K, V>> sortEntryToList(final Collection<Entry<K, V>> collection) {
        final List<Entry<K, V>> list = new LinkedList<>(collection);
        list.sort((o1, o2) -> {
            final V v1 = o1.getValue();
            final V v2 = o2.getValue();
            if (v1 instanceof Comparable) {
                return ((Comparable) v1).compareTo(v2);
            } else {
                return v1.toString().compareTo(v2.toString());
            }
        });
        return list;
    }

    /**
     * Iterates over an `Iterable`, applying a consumer to each element along with its index.
     *
     * @param <T>      The type of the elements.
     * @param iterable The `Iterable`.
     * @param consumer The consumer.
     */
    public static <T> void forEach(final Iterable<T> iterable, final BiConsumerX<Integer, T> consumer) {
        if (iterable == null) {
            return;
        }
        forEach(iterable.iterator(), consumer);
    }

    /**
     * Iterates over an `Iterator`, applying a consumer to each element along with its index.
     *
     * @param <T>      The type of the elements.
     * @param iterator The `Iterator`.
     * @param consumer The consumer.
     */
    public static <T> void forEach(final Iterator<T> iterator, final BiConsumerX<Integer, T> consumer) {
        IteratorKit.forEach(iterator, consumer);
    }

    /**
     * Iterates over an `Enumeration`, applying a consumer to each element along with its index.
     *
     * @param <T>         The type of the elements.
     * @param enumeration The `Enumeration`.
     * @param consumer    The consumer.
     */
    public static <T> void forEach(final Enumeration<T> enumeration, final BiConsumerX<Integer, T> consumer) {
        if (enumeration == null) {
            return;
        }
        int index = 0;
        while (enumeration.hasMoreElements()) {
            consumer.accept(index, enumeration.nextElement());
            index++;
        }
    }

    /**
     * Iterates over a `Map`, applying a consumer to each entry along with its index.
     *
     * @param <K>        The type of the keys.
     * @param <V>        The type of the values.
     * @param map        The `Map`.
     * @param kvConsumer The consumer.
     */
    public static <K, V> void forEach(final Map<K, V> map, final Consumer3X<Integer, K, V> kvConsumer) {
        MapKit.forEach(map, kvConsumer);
    }

    /**
     * Groups a collection into sub-lists based on a hash function.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param hash       The hash function.
     * @return A list of lists representing the groups.
     */
    public static <T> List<List<T>> group(final Collection<T> collection, Hash32<T> hash) {
        final List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        if (null == hash) {
            hash = t -> (null == t) ? 0 : t.hashCode();
        }

        int index;
        List<T> subList;
        for (final T t : collection) {
            index = hash.hash32(t);
            if (result.size() - 1 < index) {
                while (result.size() - 1 < index) {
                    result.add(null);
                }
                result.set(index, ListKit.of(t));
            } else {
                subList = result.get(index);
                if (null == subList) {
                    result.set(index, ListKit.of(t));
                } else {
                    subList.add(t);
                }
            }
        }
        return result;
    }

    /**
     * Groups a collection of beans by a specified property value.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param fieldName  The property name.
     * @return A list of lists representing the groups.
     */
    public static <T> List<List<T>> groupByField(final Collection<T> collection, final String fieldName) {
        return groupByFunc(collection, t -> BeanKit.getProperty(t, fieldName));
    }

    /**
     * Groups a collection based on the result of a getter function.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param getter     The getter function.
     * @return A list of lists representing the groups.
     */
    public static <T> List<List<T>> groupByFunc(final Collection<T> collection, final Function<T, ?> getter) {
        return group(collection, new Hash32<>() {

            private final List<Object> hashValList = new ArrayList<>();

            /**
             * Hash32 method.
             *
             * @return the int value
             */
            @Override
            public int hash32(final T t) {
                if (null == t || !BeanKit.isWritableBean(t.getClass())) {
                    return 0;
                }
                final Object value = getter.apply(t);
                int hash = hashValList.indexOf(value);
                if (hash < 0) {
                    hashValList.add(value);
                    hash = hashValList.size() - 1;
                }
                return hash;
            }
        });
    }

    /**
     * Gets all unique keys from a collection of maps.
     *
     * @param <K>           The type of the keys.
     * @param mapCollection The collection of maps.
     * @return A set of all keys.
     */
    public static <K> Set<K> keySet(final Collection<Map<K, ?>> mapCollection) {
        if (isEmpty(mapCollection)) {
            return new HashSet<>();
        }
        final HashSet<K> set = new HashSet<>(mapCollection.size() * 16);
        for (final Map<K, ?> map : mapCollection) {
            set.addAll(map.keySet());
        }
        return set;
    }

    /**
     * Gets all values from a collection of maps.
     *
     * @param <V>           The type of the values.
     * @param mapCollection The collection of maps.
     * @return A list of all values.
     */
    public static <V> List<V> values(final Collection<Map<?, V>> mapCollection) {
        if (isEmpty(mapCollection)) {
            return ListKit.zero();
        }
        int size = 0;
        for (final Map<?, V> map : mapCollection) {
            size += map.size();
        }
        if (size == 0) {
            return ListKit.zero();
        }
        final List<V> values = new ArrayList<>(size);
        for (final Map<?, V> map : mapCollection) {
            values.addAll(map.values());
        }
        return values;
    }

    /**
     * Finds the maximum element in a collection, handling nulls gracefully.
     *
     * @param <T>  The type of the elements.
     * @param coll The collection.
     * @return The maximum element, or `null` if the collection is empty or contains only nulls.
     * @see Collections#max(Collection)
     */
    public static <T extends Comparable<? super T>> T max(final Collection<T> coll) {
        if (isEmpty(coll)) {
            return null;
        }
        final Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();
        while (i.hasNext()) {
            candidate = CompareKit.max(candidate, i.next());
        }
        return candidate;
    }

    /**
     * Finds the minimum element in a collection, handling nulls gracefully.
     *
     * @param <T>  The type of the elements.
     * @param coll The collection.
     * @return The minimum element, or `null` if the collection is empty or contains only nulls.
     * @see Collections#min(Collection)
     */
    public static <T extends Comparable<? super T>> T min(final Collection<T> coll) {
        if (isEmpty(coll)) {
            return null;
        }
        final Iterator<? extends T> i = coll.iterator();
        T candidate = i.next();
        T next;
        while (i.hasNext()) {
            next = i.next();
            candidate = CompareKit.compare(candidate, next, true) < 0 ? candidate : next;
        }
        return candidate;
    }

    /**
     * Returns an unmodifiable view of the specified collection.
     *
     * @param <T> The type of the elements.
     * @param c   The collection.
     * @return An unmodifiable view.
     */
    public static <T> Collection<T> view(final Collection<? extends T> c) {
        if (null == c) {
            return null;
        }
        return Collections.unmodifiableCollection(c);
    }

    /**
     * Clears all elements from one or more collections.
     *
     * @param collections The collections to clear.
     */
    public static void clear(final Collection<?>... collections) {
        for (final Collection<?> collection : collections) {
            if (isNotEmpty(collection)) {
                collection.clear();
            }
        }
    }

    /**
     * Pads a `List` on the left with a given object to reach a minimum length.
     *
     * @param <T>    The type of the elements.
     * @param list   The list.
     * @param minLen The minimum length.
     * @param padObj The object to pad with.
     */
    public static <T> void padLeft(final List<T> list, final int minLen, final T padObj) {
        Objects.requireNonNull(list);
        if (list.size() >= minLen) {
            return;
        }
        if (list instanceof ArrayList) {
            list.addAll(0, Collections.nCopies(minLen - list.size(), padObj));
        } else {
            for (int i = list.size(); i < minLen; i++) {
                list.add(0, padObj);
            }
        }
    }

    /**
     * Pads a `Collection` on the right with a given object to reach a minimum length.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param minLen     The minimum length.
     * @param padObj     The object to pad with.
     */
    public static <T> void padRight(final Collection<T> collection, final int minLen, final T padObj) {
        Objects.requireNonNull(collection);
        for (int i = collection.size(); i < minLen; i++) {
            collection.add(padObj);
        }
    }

    /**
     * Returns a view of a collection that transforms its elements on-the-fly using a mapping function.
     *
     * @param <F>        The source element type.
     * @param <T>        The target element type.
     * @param collection The source collection.
     * @param function   The transformation function.
     * @return A new collection with transformed elements.
     */
    public static <F, T> Collection<T> trans(
            final Collection<F> collection,
            final Function<? super F, ? extends T> function) {
        return new TransCollection<>(collection, function);
    }

    /**
     * Updates elements in a collection by looking up values in a map.
     *
     * @param <E>         The type of the elements.
     * @param <K>         The type of the map key.
     * @param <V>         The type of the map value.
     * @param iterable    The collection.
     * @param map         The map of values.
     * @param keyGenerate The function to generate the lookup key from an element.
     * @param biConsumer  The consumer to update the element with the looked-up value.
     */
    public static <E, K, V> void setValueByMap(
            final Iterable<E> iterable,
            final Map<K, V> map,
            final Function<E, K> keyGenerate,
            final BiConsumer<E, V> biConsumer) {
        iterable.forEach(
                x -> Optional.ofNullable(map.get(keyGenerate.apply(x))).ifPresent(y -> biConsumer.accept(x, y)));
    }

    /**
     * Adds an object to a collection only if it is not null and not already present.
     *
     * @param <T>        The type of the collection elements.
     * @param <S>        The type of the object to add.
     * @param collection The collection.
     * @param object     The object to add.
     * @return {@code true} if the object was added.
     */
    public static <T, S extends T> boolean addIfAbsent(final Collection<T> collection, final S object) {
        if (object == null || collection == null || collection.contains(object)) {
            return false;
        }
        return collection.add(object);
    }

    /**
     * Checks if at least one element in the collection matches the given predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate.
     * @return {@code true} if any element matches.
     */
    public static <T> boolean anyMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Boolean.FALSE;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * Checks if all elements in the collection match the given predicate.
     *
     * @param <T>        The type of the elements.
     * @param collection The collection.
     * @param predicate  The predicate.
     * @return {@code true} if all elements match.
     */
    public static <T> boolean allMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Boolean.FALSE;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * Flattens a multi-level collection into a single-level list. e.g., {@code List<List<List<String>>>} becomes
     * {@code List<String>}.
     *
     * @param <T>        The type of the final elements.
     * @param collection The collection to flatten.
     * @return The flattened list.
     */
    public static <T> List<T> flat(final Collection<?> collection) {
        return flat(collection, true);
    }

    /**
     * Flattens a multi-level collection into a single-level list.
     *
     * @param <T>        The type of the final elements.
     * @param collection The collection to flatten.
     * @param skipNull   If true, null elements are excluded from the result.
     * @return The flattened list.
     */
    public static <T> List<T> flat(final Collection<?> collection, final boolean skipNull) {
        final LinkedList<Object> queue = new LinkedList<>(collection);
        final List<Object> result = new ArrayList<>();
        while (isNotEmpty(queue)) {
            final Object t = queue.removeFirst();
            if (skipNull && t == null) {
                continue;
            }
            if (t instanceof Collection) {
                queue.addAll((Collection<?>) t);
            } else {
                result.add(t);
            }
        }
        return (List<T>) result;
    }

    /**
     * Provides a circular index for a collection size using an `AtomicInteger` for thread-safe round-robin access.
     *
     * @param object        The object to get the size from (Collection, Map, Array, etc.).
     * @param atomicInteger The atomic integer.
     * @return The next circular index.
     */
    public static int ringNextIntByObject(final Object object, final AtomicInteger atomicInteger) {
        Assert.notNull(object);
        final int modulo = size(object);
        return ringNextInt(modulo, atomicInteger);
    }

    /**
     * Provides a circular index within a given range using an `AtomicInteger`.
     *
     * @param modulo        The cycle length.
     * @param atomicInteger The atomic integer.
     * @return The next circular index.
     */
    public static int ringNextInt(final int modulo, final AtomicInteger atomicInteger) {
        Assert.notNull(atomicInteger);
        Assert.isTrue(modulo > 0);
        if (modulo <= 1) {
            return 0;
        }
        for (;;) {
            final int current = atomicInteger.get();
            final int next = (current + 1) % modulo;
            if (atomicInteger.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * Provides a circular index within a given range using an `AtomicLong`.
     *
     * @param modulo     The cycle length.
     * @param atomicLong The atomic long.
     * @return The next circular index.
     */
    public static long ringNextLong(final long modulo, final AtomicLong atomicLong) {
        Assert.notNull(atomicLong);
        Assert.isTrue(modulo > 0);
        if (modulo <= 1) {
            return 0;
        }
        for (;;) {
            final long current = atomicLong.get();
            final long next = (current + 1) % modulo;
            if (atomicLong.compareAndSet(current, next)) {
                return next;
            }
        }
    }

}
