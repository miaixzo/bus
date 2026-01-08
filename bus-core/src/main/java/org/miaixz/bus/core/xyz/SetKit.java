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

import java.util.*;

import org.miaixz.bus.core.center.set.SetFromMap;

/**
 * Encapsulates methods related to {@link java.util.Set} in collections.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetKit {

    /**
     * Creates a new {@link HashSet} or {@link LinkedHashSet}. If the provided initial array is empty, a default initial
     * capacity is used.
     *
     * @param <T>      The type of elements in the set.
     * @param isLinked If {@code true}, a {@link LinkedHashSet} is created (maintains insertion order); otherwise, a
     *                 {@link HashSet} is created.
     * @return A new {@link HashSet} or {@link LinkedHashSet} object.
     */
    public static <T> HashSet<T> of(final boolean isLinked) {
        return _of(isLinked, null);
    }

    /**
     * Creates a new {@link HashSet} from an array of elements.
     *
     * @param <T> The type of elements in the set.
     * @param ts  The array of elements.
     * @return A new {@link HashSet} object.
     */
    @SafeVarargs
    public static <T> HashSet<T> of(final T... ts) {
        return _of(false, ts);
    }

    /**
     * Creates a new {@link LinkedHashSet} from an array of elements.
     *
     * @param <T> The type of elements in the set.
     * @param ts  The array of elements.
     * @return A new {@link LinkedHashSet} object.
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> ofLinked(final T... ts) {
        return (LinkedHashSet<T>) _of(true, ts);
    }

    /**
     * Creates a new {@link HashSet} from an {@link Iterable} collection.
     *
     * @param <T>      The type of elements in the set.
     * @param iterable The iterable collection.
     * @return A new {@link HashSet} object.
     */
    public static <T> HashSet<T> of(final Iterable<T> iterable) {
        return of(false, iterable);
    }

    /**
     * Creates a new {@link HashSet} or {@link LinkedHashSet} from an {@link Iterable} collection. If the provided
     * iterable is {@code null}, an empty set of the specified type is returned.
     *
     * @param <T>      The type of elements in the set.
     * @param isLinked If {@code true}, a {@link LinkedHashSet} is created; otherwise, a {@link HashSet}.
     * @param iterable The {@link Iterable} collection.
     * @return A new {@link HashSet} or {@link LinkedHashSet} object.
     */
    public static <T> HashSet<T> of(final boolean isLinked, final Iterable<T> iterable) {
        if (null == iterable) {
            return of(isLinked);
        }
        if (iterable instanceof Collection<T> collection) {
            return isLinked ? new LinkedHashSet<>(collection) : new HashSet<>(collection);
        }
        return of(isLinked, iterable.iterator());
    }

    /**
     * Creates a new {@link HashSet} or {@link LinkedHashSet} from an {@link Iterator}.
     *
     * @param <T>      The type of elements in the set.
     * @param isSorted If {@code true}, a {@link LinkedHashSet} is created (maintains insertion order); otherwise, a
     *                 {@link HashSet} is created.
     * @param iter     The {@link Iterator}.
     * @return A new {@link HashSet} or {@link LinkedHashSet} object.
     */
    public static <T> HashSet<T> of(final boolean isSorted, final Iterator<T> iter) {
        if (null == iter) {
            return _of(isSorted, null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<>() : new HashSet<>();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        return set;
    }

    /**
     * Creates a new {@link HashSet} or {@link LinkedHashSet} from an {@link Enumeration}.
     *
     * @param <T>         The type of elements in the set.
     * @param isLinked    If {@code true}, a {@link LinkedHashSet} is created (maintains insertion order); otherwise, a
     *                    {@link HashSet} is created.
     * @param enumeration The {@link Enumeration}.
     * @return A new {@link HashSet} or {@link LinkedHashSet} object.
     */
    public static <T> HashSet<T> of(final boolean isLinked, final Enumeration<T> enumeration) {
        if (null == enumeration) {
            return _of(isLinked, null);
        }
        final HashSet<T> set = isLinked ? new LinkedHashSet<>() : new HashSet<>();
        while (enumeration.hasMoreElements()) {
            set.add(enumeration.nextElement());
        }
        return set;
    }

    /**
     * Creates a new {@link SetFromMap} from a given {@link Map}.
     *
     * @param <T> The type of elements in the set (which are the keys of the map).
     * @param map The map to back the set.
     * @return A new {@link SetFromMap} object.
     */
    public static <T> SetFromMap<T> of(final Map<T, Boolean> map) {
        return new SetFromMap<>(map);
    }

    /**
     * Converts an array of elements into an unmodifiable {@link Set}. Similar to Java 9's {@code Set.of} method.
     *
     * @param ts  The array of elements.
     * @param <T> The type of elements.
     * @return An unmodifiable {@link Set}.
     */
    @SafeVarargs
    public static <T> Set<T> view(final T... ts) {
        return view(of(ts));
    }

    /**
     * Converts a {@link Set} into an unmodifiable {@link Set}.
     *
     * @param ts  The set to make unmodifiable.
     * @param <T> The type of elements.
     * @return An unmodifiable {@link Set}. If the provided set is {@code null} or empty, {@link Collections#emptySet()}
     *         is returned.
     */
    public static <T> Set<T> view(final Set<T> ts) {
        if (ArrayKit.isEmpty(ts)) {
            return empty();
        }
        return Collections.unmodifiableSet(ts);
    }

    /**
     * Returns an empty, unmodifiable {@link Set}.
     *
     * @param <T> The type of elements.
     * @return An empty, unmodifiable {@link Set}.
     * @see Collections#emptySet()
     */
    public static <T> Set<T> empty() {
        return Collections.emptySet();
    }

    /**
     * Returns a new, mutable {@link HashSet} with an initial capacity of 0.
     *
     * @param <T> The type of elements.
     * @return A new, mutable {@link HashSet} with an initial capacity of 0.
     */
    public static <T> Set<T> zero() {
        return new HashSet<>(0, 1);
    }

    /**
     * Returns an unmodifiable {@link Set} containing only the specified element.
     *
     * @param <T>     The type of the element.
     * @param element The single element to be contained in the set.
     * @return An unmodifiable {@link Set} containing only the specified element.
     */
    public static <T> Set<T> singleton(final T element) {
        return Collections.singleton(element);
    }

    /**
     * Returns a new, mutable {@link LinkedHashSet} with an initial capacity of 0.
     *
     * @param <T> The type of elements.
     * @return A new, mutable {@link LinkedHashSet} with an initial capacity of 0.
     */
    public static <T> Set<T> zeroLinked() {
        return new LinkedHashSet<>(0, 1);
    }

    /**
     * Returns an unmodifiable view of the specified set.
     *
     * @param <T> The type of elements.
     * @param c   The set for which an unmodifiable view is to be returned.
     * @return An unmodifiable view of the specified set. If the input set is {@code null}, {@code null} is returned.
     * @see Collections#unmodifiableSet(Set)
     */
    public static <T> Set<T> unmodifiable(final Set<? extends T> c) {
        if (null == c) {
            return null;
        }
        return Collections.unmodifiableSet(c);
    }

    /**
     * Internal helper method to create a new {@link HashSet} or {@link LinkedHashSet}.
     *
     * @param <T>      The type of elements in the set.
     * @param isLinked If {@code true}, a {@link LinkedHashSet} is created; otherwise, a {@link HashSet}.
     * @param ts       The array of elements to add to the set. If {@code null} or empty, an empty set is created.
     * @return A new {@link HashSet} or {@link LinkedHashSet} object.
     */
    private static <T> HashSet<T> _of(final boolean isLinked, final T[] ts) {
        if (ArrayKit.isEmpty(ts)) {
            return isLinked ? new LinkedHashSet<>() : new HashSet<>();
        }
        final int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        final HashSet<T> set = isLinked ? new LinkedHashSet<>(initialCapacity) : new HashSet<>(initialCapacity);
        Collections.addAll(set, ts);
        return set;
    }

}
