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
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.array.ArrayValidator;
import org.miaixz.bus.core.xyz.IteratorKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Provides utility methods for validating collections and other iterable data structures. This class offers a
 * consistent way to check for empty or non-empty states across various types, including {@link Collection},
 * {@link Iterable}, {@link Iterator}, {@link Enumeration}, and {@link Map}.
 * <p>
 * <b>Definitions:</b>
 *
 * <ul>
 * <li><b>Empty:</b> A data structure is considered empty if it is {@code null} or contains no elements.</li>
 * <li><b>Blank:</b> While not explicitly implemented here for collections, this term typically refers to strings that
 * are {@code null}, empty, or contain only whitespace. This class focuses on element presence.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionValidator {

    /**
     * Constructs a new CollectionValidator. Utility class constructor for static access.
     */
    public CollectionValidator() {
    }

    /**
     * Checks if the given collection is empty.
     *
     * @param collection The collection to check.
     * @return {@code true} if the collection is {@code null} or has no elements; {@code false} otherwise.
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the given {@link Iterable} is empty.
     *
     * @param iterable The {@link Iterable} to check.
     * @return {@code true} if the iterable is {@code null} or has no elements; {@code false} otherwise.
     * @see IteratorKit#isEmpty(Iterable)
     */
    public static boolean isEmpty(final Iterable<?> iterable) {
        return IteratorKit.isEmpty(iterable);
    }

    /**
     * Checks if the given {@link Iterator} is empty.
     *
     * @param iterator The {@link Iterator} to check.
     * @return {@code true} if the iterator is {@code null} or has no more elements; {@code false} otherwise.
     * @see IteratorKit#isEmpty(Iterator)
     */
    public static boolean isEmpty(final Iterator<?> iterator) {
        return IteratorKit.isEmpty(iterator);
    }

    /**
     * Checks if the given {@link Enumeration} is empty.
     *
     * @param enumeration The {@link Enumeration} to check.
     * @return {@code true} if the enumeration is {@code null} or has no more elements; {@code false} otherwise.
     */
    public static boolean isEmpty(final Enumeration<?> enumeration) {
        return null == enumeration || !enumeration.hasMoreElements();
    }

    /**
     * Checks if the given {@link Map} is empty.
     *
     * @param map The {@link Map} to check.
     * @return {@code true} if the map is {@code null} or has no key-value mappings; {@code false} otherwise.
     * @see MapKit#isEmpty(Map)
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return MapKit.isEmpty(map);
    }

    /**
     * Returns the given collection if it is not empty, otherwise returns a default collection.
     *
     * @param <T>               The type of the collection.
     * @param <E>               The type of elements in the collection.
     * @param collection        The collection to check.
     * @param defaultCollection The default collection to return if the input collection is empty.
     * @return The original collection if not empty, otherwise the default collection.
     */
    public static <T extends Collection<E>, E> T defaultIfEmpty(final T collection, final T defaultCollection) {
        return isEmpty(collection) ? defaultCollection : collection;
    }

    /**
     * Returns the result of a handler function if the collection is not empty, otherwise returns a default value from a
     * supplier.
     *
     * @param <T>             The type of the collection.
     * @param <E>             The type of elements in the collection.
     * @param collection      The collection to check.
     * @param handler         The function to apply to the non-empty collection.
     * @param defaultSupplier A supplier that provides a default collection if the input is empty.
     * @return The result of the handler function or the value from the default supplier.
     */
    public static <T extends Collection<E>, E> T defaultIfEmpty(
            final T collection,
            final Function<T, T> handler,
            final Supplier<? extends T> defaultSupplier) {
        return isEmpty(collection) ? defaultSupplier.get() : handler.apply(collection);
    }

    /**
     * Returns an unmodifiable empty set if the provided set is {@code null}, otherwise returns the original set.
     *
     * @param <T> The type of elements in the set.
     * @param set The set to check, may be {@code null}.
     * @return The original set, or an unmodifiable empty set if the input is {@code null}.
     */
    public static <T> Set<T> emptyIfNull(final Set<T> set) {
        return ObjectKit.defaultIfNull(set, Collections.emptySet());
    }

    /**
     * Returns an unmodifiable empty list if the provided list is {@code null}, otherwise returns the original list.
     *
     * @param <T>  The type of elements in the list.
     * @param list The list to check, may be {@code null}.
     * @return The original list, or an unmodifiable empty list if the input is {@code null}.
     */
    public static <T> List<T> emptyIfNull(final List<T> list) {
        return ObjectKit.defaultIfNull(list, Collections.emptyList());
    }

    /**
     * Checks if the given collection is not empty.
     *
     * @param collection The collection to check.
     * @return {@code true} if the collection is not {@code null} and contains elements; {@code false} otherwise.
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Checks if the given {@link Iterable} is not empty.
     *
     * @param iterable The {@link Iterable} to check.
     * @return {@code true} if the iterable is not {@code null} and contains elements; {@code false} otherwise.
     * @see IteratorKit#isNotEmpty(Iterable)
     */
    public static boolean isNotEmpty(final Iterable<?> iterable) {
        return IteratorKit.isNotEmpty(iterable);
    }

    /**
     * Checks if the given {@link Iterator} is not empty.
     *
     * @param iterator The {@link Iterator} to check.
     * @return {@code true} if the iterator is not {@code null} and has more elements; {@code false} otherwise.
     * @see IteratorKit#isNotEmpty(Iterator)
     */
    public static boolean isNotEmpty(final Iterator<?> iterator) {
        return IteratorKit.isNotEmpty(iterator);
    }

    /**
     * Checks if the given {@link Enumeration} is not empty.
     *
     * @param enumeration The {@link Enumeration} to check.
     * @return {@code true} if the enumeration is not {@code null} and has more elements; {@code false} otherwise.
     */
    public static boolean isNotEmpty(final Enumeration<?> enumeration) {
        return null != enumeration && enumeration.hasMoreElements();
    }

    /**
     * Checks if the given {@link Map} is not empty.
     *
     * @param map The {@link Map} to check.
     * @return {@code true} if the map is not {@code null} and has key-value mappings; {@code false} otherwise.
     * @see MapKit#isNotEmpty(Map)
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return MapKit.isNotEmpty(map);
    }

    /**
     * Checks if the given iterable contains any {@code null} elements.
     *
     * @param iterable The {@link Iterable} to check. If {@code null}, this method returns {@code true}.
     * @return {@code true} if the iterable is {@code null} or contains at least one {@code null} element; {@code false}
     *         otherwise.
     * @see IteratorKit#hasNull(Iterator)
     */
    public static boolean hasNull(final Iterable<?> iterable) {
        return IteratorKit.hasNull(IteratorKit.getIter(iterable));
    }

    /**
     * Determines if one collection is a sub-collection of another by comparing element cardinalities. This method
     * ignores the order of elements.
     *
     * @param subCollection The potential sub-collection.
     * @param collection    The main collection.
     * @return {@code true} if every element in {@code subCollection} has a cardinality less than or equal to its
     *         cardinality in {@code collection}.
     */
    public static boolean isSub(final Collection<?> subCollection, final Collection<?> collection) {
        if (size(subCollection) > size(collection)) {
            return false;
        }
        return IteratorKit.isSub(subCollection, collection);
    }

    /**
     * Checks if two collections are equal in terms of both elements and their order. This is equivalent to calling
     * {@code list1.equals(list2)} if both are lists.
     *
     * @param list1 The first collection.
     * @param list2 The second collection.
     * @return {@code true} if the collections have the same size and elements in the same order; {@code false}
     *         otherwise.
     */
    public static boolean isEqualList(final Collection<?> list1, final Collection<?> list2) {
        return equals(list1, list2, false);
    }

    /**
     * Compares two collections for equality, with an option to ignore the order of elements. If order is ignored, the
     * comparison is based on element cardinalities (multiset equality).
     *
     * @param coll1       The first collection.
     * @param coll2       The second collection.
     * @param ignoreOrder If {@code true}, element order is ignored; otherwise, order is significant.
     * @return {@code true} if the collections are equal based on the specified criteria; {@code false} otherwise.
     */
    public static boolean equals(final Collection<?> coll1, final Collection<?> coll2, final boolean ignoreOrder) {
        if (size(coll1) != size(coll2)) {
            return false;
        }

        return IteratorKit.equals(coll1, coll2, ignoreOrder);
    }

    /**
     * Returns the size of a given object if it is a known data structure type. Supported types are {@link Collection},
     * {@link Map}, array, {@link Iterator}, {@link Iterable}, and {@link Enumeration}.
     *
     * @param object The object whose size is to be determined.
     * @return The size of the object, or 0 if the object is {@code null}.
     * @throws IllegalArgumentException if the object type is not supported.
     */
    public static int size(final Object object) {
        if (object == null) {
            return 0;
        }

        if (object instanceof Collection<?>) {
            return ((Collection<?>) object).size();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>) object).size();
        } else if (object instanceof Iterable<?>) {
            return IteratorKit.size((Iterable<?>) object);
        } else if (object instanceof Iterator<?>) {
            return IteratorKit.size((Iterator<?>) object);
        } else if (object instanceof final Enumeration<?> it) {
            int total = 0;
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
