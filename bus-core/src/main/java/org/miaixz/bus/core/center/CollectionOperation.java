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
package org.miaixz.bus.core.center;

import java.util.*;

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * Provides a suite of set-based operations for collections, treating them as multisets (bags). This class supports
 * operations that respect the cardinality (number of occurrences) of each element. Operations include:
 * <ul>
 * <li>{@link #union()}: Multiset union.</li>
 * <li>{@link #unionDistinct()}: Standard set union (unique elements).</li>
 * <li>{@link #unionAll()}: Concatenation of all collections.</li>
 * <li>{@link #intersection()}: Multiset intersection.</li>
 * <li>{@link #intersectionDistinct()}: Standard set intersection (unique elements).</li>
 * <li>{@link #disjunction()}: Multiset symmetric difference.</li>
 * <li>{@link #subtract()}: Multiset difference (relative complement).</li>
 * </ul>
 *
 * @param <E> The type of elements in the collections.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionOperation<E> {

    /**
     * The array of collections on which the operations will be performed.
     */
    private final Collection<E>[] colls;

    /**
     * Constructs a {@code CollectionOperation} with the given array of collections.
     *
     * @param colls An array of collections to be used in the operations.
     */
    public CollectionOperation(final Collection<? extends E>[] colls) {
        this.colls = (Collection<E>[]) colls;
    }

    /**
     * Creates a {@code CollectionOperation} instance for performing set operations on the given collections. This is a
     * factory method for convenient instantiation.
     *
     * @param <E>   The type of elements in the collections.
     * @param colls A variable-length argument list of collections.
     * @return A new {@code CollectionOperation} instance.
     */
    @SafeVarargs
    public static <E> CollectionOperation<E> of(final Collection<? extends E>... colls) {
        return new CollectionOperation<>(colls);
    }

    /**
     * Computes the multiset union of two collections. The cardinality of an element in the result is the maximum of its
     * cardinalities in the two input collections.
     * <p>
     * Example: {@code _union([a, b, c, c], [a, c, c, c])} returns {@code [a, b, c, c, c]}.
     *
     * @param <T>   The type of elements in the collections.
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return The multiset union of the two collections as a new {@link ArrayList}.
     */
    private static <T> Collection<T> _union(final Collection<T> coll1, final Collection<T> coll2) {
        if (CollKit.isEmpty(coll1)) {
            return ListKit.of(coll2);
        } else if (CollKit.isEmpty(coll2)) {
            return ListKit.of(coll1);
        }

        final Map<T, Integer> map1 = CollKit.countMap(coll1);
        final Map<T, Integer> map2 = CollKit.countMap(coll2);
        final Set<T> elements = CollectionOperation.of(map1.keySet(), map2.keySet()).unionDistinct();
        final List<T> list = new ArrayList<>(coll1.size() + coll2.size());
        for (final T t : elements) {
            final int amount = Math.max(map1.getOrDefault(t, 0), map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Computes the multiset intersection of two collections. The cardinality of an element in the result is the minimum
     * of its cardinalities in the two input collections.
     * <p>
     * Example: {@code _intersection([a, b, c, c], [a, c, c, c])} returns {@code [a, c, c]}.
     *
     * @param <E>   The type of elements in the collections.
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return The multiset intersection of the two collections as a new {@link ArrayList}.
     */
    private static <E> Collection<E> _intersection(final Collection<E> coll1, final Collection<E> coll2) {
        if (CollKit.isEmpty(coll1) || CollKit.isEmpty(coll2)) {
            return ListKit.zero();
        }
        final Map<E, Integer> map1 = CollKit.countMap(coll1);
        final Map<E, Integer> map2 = CollKit.countMap(coll2);

        final boolean isFirstSmaller = map1.size() <= map2.size();
        final Set<E> elements = SetKit.of(isFirstSmaller ? map1.keySet() : map2.keySet());
        final List<E> list = new ArrayList<>(isFirstSmaller ? coll1.size() : coll2.size());
        for (final E t : elements) {
            final int amount = Math.min(map1.getOrDefault(t, 0), map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Computes the multiset symmetric difference (disjunction) of two collections. The cardinality of an element in the
     * result is the absolute difference of its cardinalities in the two input collections.
     * <p>
     * Example: {@code _disjunction([a, b, c, c], [a, c, c, c])} returns {@code [b, c]}.
     *
     * @param <T>   The type of elements in the collections.
     * @param coll1 The first collection.
     * @param coll2 The second collection.
     * @return The multiset symmetric difference as a new {@link ArrayList}.
     */
    private static <T> Collection<T> _disjunction(final Collection<T> coll1, final Collection<T> coll2) {
        if (CollKit.isEmpty(coll1)) {
            return CollKit.isEmpty(coll2) ? ListKit.zero() : coll2;
        }
        if (CollKit.isEmpty(coll2)) {
            return coll1;
        }

        final List<T> result = new ArrayList<>(coll1.size() + coll2.size());
        final Map<T, Integer> map1 = CollKit.countMap(coll1);
        final Map<T, Integer> map2 = CollKit.countMap(coll2);
        final Set<T> elements = SetKit.of(map1.keySet());
        elements.addAll(map2.keySet());
        for (final T t : elements) {
            final int amount = Math.abs(map1.getOrDefault(t, 0) - map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Computes the multiset union of all collections provided at construction. The cardinality of an element in the
     * result is the maximum of its cardinalities across all collections.
     *
     * @return The multiset union of the collections as a new {@link ArrayList}.
     */
    public Collection<E> union() {
        if (ArrayKit.isEmpty(this.colls)) {
            return ListKit.zero();
        }

        Collection<E> result = this.colls[0];
        for (int i = 1; i < this.colls.length; i++) {
            result = _union(result, this.colls[i]);
        }

        return result;
    }

    /**
     * Computes the standard set union (distinct union) of all collections. The result contains each element that
     * appears in any of the collections, with duplicates removed.
     *
     * @return The unique union of the collections as a new {@link HashSet}.
     */
    public Set<E> unionDistinct() {
        if (ArrayKit.isEmpty(this.colls)) {
            return SetKit.zero();
        }
        int totalLength = 0;
        for (final Collection<E> set : this.colls) {
            if (CollKit.isNotEmpty(set)) {
                totalLength += set.size();
            }
        }
        final Set<E> result = new HashSet<>(totalLength, 1);
        for (final Collection<E> set : this.colls) {
            if (CollKit.isNotEmpty(set)) {
                result.addAll(set);
            }
        }
        return result;
    }

    /**
     * Computes the complete union (concatenation) of all collections, similar to SQL's {@code UNION ALL}. All instances
     * of every element from all collections are retained in the order of their appearance.
     *
     * @return The complete union of the collections as a new {@link ArrayList}.
     */
    public List<E> unionAll() {
        if (ArrayKit.isEmpty(this.colls)) {
            return ListKit.zero();
        }
        int totalSize = 0;
        for (final Collection<E> coll : this.colls) {
            totalSize += CollKit.size(coll);
        }
        if (totalSize == 0) {
            return ListKit.zero();
        }

        final List<E> result = new ArrayList<>(totalSize);
        for (final Collection<E> coll : this.colls) {
            if (CollKit.isNotEmpty(coll)) {
                result.addAll(coll);
            }
        }

        return result;
    }

    /**
     * Computes the multiset intersection of all collections provided at construction. The cardinality of an element in
     * the result is the minimum of its cardinalities across all collections.
     *
     * @return The multiset intersection of the collections as a new {@link ArrayList}.
     */
    public Collection<E> intersection() {
        if (ArrayKit.isEmpty(this.colls)) {
            return ListKit.zero();
        }

        Collection<E> result = this.colls[0];
        for (int i = 1; i < this.colls.length; i++) {
            result = _intersection(result, this.colls[i]);
        }

        return result;
    }

    /**
     * Computes the standard set intersection (distinct intersection) of all collections. The result contains only the
     * elements that are present in all of the collections, with duplicates removed.
     *
     * @return The unique intersection of the collections as a new {@link LinkedHashSet}.
     */
    public Set<E> intersectionDistinct() {
        if (ArrayKit.isEmpty(this.colls)) {
            return SetKit.zeroLinked();
        }

        for (final Collection<E> coll : this.colls) {
            if (CollKit.isEmpty(coll)) {
                return SetKit.zeroLinked();
            }
        }

        final Set<E> result = SetKit.of(true, this.colls[0]);
        for (int i = 1; i < this.colls.length; i++) {
            result.retainAll(this.colls[i]);
        }

        return result;
    }

    /**
     * Computes the multiset symmetric difference (disjunction) of all collections provided at construction. This
     * operation is performed iteratively.
     *
     * @return The multiset symmetric difference of the collections as a new {@link ArrayList}.
     */
    public Collection<E> disjunction() {
        if (ArrayKit.isEmpty(this.colls)) {
            return ListKit.zero();
        }

        Collection<E> result = this.colls[0];
        for (int i = 1; i < this.colls.length; i++) {
            result = _disjunction(result, this.colls[i]);
        }

        return result;
    }

    /**
     * Computes the multiset difference (relative complement). It returns elements that are in the first collection but
     * not in any of the subsequent collections. Cardinality is respected.
     * <p>
     * Example: {@code subtract([1, 2, 3, 4], [2, 3, 5])} returns {@code [1, 4]}.
     *
     * @return A {@link List} containing elements from the first collection that are not in the others.
     */
    public List<E> subtract() {
        if (ArrayKit.isEmpty(this.colls)) {
            return ListKit.zero();
        }
        final List<E> result = ListKit.of(this.colls[0]);
        for (int i = 1; i < this.colls.length; i++) {
            result.removeAll(this.colls[i]);
        }
        return result;
    }

}
