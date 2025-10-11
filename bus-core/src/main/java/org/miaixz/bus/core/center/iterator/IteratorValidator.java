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
package org.miaixz.bus.core.center.iterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.CollectionValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utility class for validating {@link Iterator} and {@link Iterable} objects. Provides methods for checking if
 * iterators/iterables are empty, blank, or contain null elements.
 * <ul>
 * <li>Empty definition: {@code null} or an empty string {@code ""}</li>
 * <li>Blank definition: {@code null} or an empty string {@code ""} or invisible characters like spaces, full-width
 * spaces, tabs, newlines, etc.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IteratorValidator {

    /**
     * Checks if the given {@link Iterable} is empty.
     *
     * @param iterable the {@link Iterable} object to check
     * @return {@code true} if the iterable is {@code null} or contains no elements, {@code false} otherwise
     */
    public static boolean isEmpty(final Iterable<?> iterable) {
        return null == iterable || isEmpty(iterable.iterator());
    }

    /**
     * Checks if the given {@link Iterator} is empty.
     *
     * @param iterator the {@link Iterator} object to check
     * @return {@code true} if the iterator is {@code null} or has no more elements, {@code false} otherwise
     */
    public static boolean isEmpty(final Iterator<?> iterator) {
        return null == iterator || !iterator.hasNext();
    }

    /**
     * Checks if the given {@link Iterable} is not empty.
     *
     * @param iterable the {@link Iterable} object to check
     * @return {@code true} if the iterable is not {@code null} and contains at least one element, {@code false}
     *         otherwise
     */
    public static boolean isNotEmpty(final Iterable<?> iterable) {
        return null != iterable && isNotEmpty(iterable.iterator());
    }

    /**
     * Checks if the given {@link Iterator} is not empty.
     *
     * @param iterator the {@link Iterator} object to check
     * @return {@code true} if the iterator is not {@code null} and has at least one more element, {@code false}
     *         otherwise
     */
    public static boolean isNotEmpty(final Iterator<?> iterator) {
        return null != iterator && iterator.hasNext();
    }

    /**
     * Checks if the given {@link Iterator} contains any {@code null} elements.
     * <ul>
     * <li>If the {@link Iterator} is {@code null}, returns {@code true}.</li>
     * <li>If the {@link Iterator} is empty (contains no elements), returns {@code false}.</li>
     * <li>If the {@link Iterator} contains elements that are empty strings {@code ""}, returns {@code false} (as empty
     * strings are not {@code null}).</li>
     * </ul>
     *
     * @param iter the {@link Iterator} object to check. If {@code null}, returns {@code true}.
     * @return {@code true} if the iterator contains a {@code null} element or the iterator itself is {@code null},
     *         {@code false} otherwise
     */
    public static boolean hasNull(final Iterator<?> iter) {
        if (null == iter) {
            return true;
        }
        while (iter.hasNext()) {
            if (null == iter.next()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if all elements in the given {@link Iterable} are {@code null}.
     *
     * @param iter the {@link Iterable} object to check. If {@code null}, returns {@code true}.
     * @return {@code true} if all elements are {@code null} or the iterable itself is {@code null}, {@code false}
     *         otherwise
     */
    public static boolean isAllNull(final Iterable<?> iter) {
        return isAllNull(null == iter ? null : iter.iterator());
    }

    /**
     * Checks if all elements in the given {@link Iterator} are {@code null}.
     *
     * @param iter the {@link Iterator} object to check. If {@code null}, returns {@code true}.
     * @return {@code true} if all elements are {@code null} or the iterator itself is {@code null}, {@code false}
     *         otherwise
     */
    public static boolean isAllNull(final Iterator<?> iter) {
        return null == getFirstNoneNull(iter);
    }

    /**
     * Checks if any string in the given {@link Iterable} of {@link CharSequence} is blank.
     *
     * @param args the {@link Iterable} of {@link CharSequence} to check
     * @return {@code true} if the iterable is empty or contains any blank string, {@code false} otherwise
     */
    public static boolean hasBlank(final Iterable<? extends CharSequence> args) {
        if (CollectionValidator.isEmpty(args)) {
            return true;
        }
        for (final CharSequence text : args) {
            if (StringKit.isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all strings in the given {@link Iterable} of {@link CharSequence} are blank.
     *
     * @param args the {@link Iterable} of {@link CharSequence} to check
     * @return {@code true} if all strings are blank or the iterable is empty, {@code false} otherwise
     */
    public static boolean isAllBlank(final Iterable<? extends CharSequence> args) {
        if (CollectionValidator.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (StringKit.isNotBlank(text)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if {@code subIter} is a sub-collection of {@code iter}, ignoring order and considering element counts.
     * <ul>
     * <li>Returns {@code true} if both collections are the same instance.</li>
     * <li>Returns {@code true} if both collections contain the same elements with the same frequencies (regardless of
     * order).</li>
     * </ul>
     *
     * @param subIter the first {@link Iterable} object, considered as the potential sub-collection.
     * @param iter    the second {@link Iterable} object, which can be any collection implementing {@link Iterable}.
     * @return {@code true} if {@code subIter} is a sub-collection of {@code iter}; {@code false} otherwise.
     */
    public static boolean isSub(final Iterable<?> subIter, final Iterable<?> iter) {
        // If both Iterable objects refer to the same instance, it is definitely a sub-collection.
        if (subIter == iter) {
            return true;
        }
        // If either Iterable object is null, it's not a sub-collection relationship.
        if (subIter == null || iter == null) {
            return false;
        }

        // Use Map to record the occurrence count of each element in each Iterable.
        final Map<?, Integer> countMap1 = countMap(subIter.iterator());
        final Map<?, Integer> countMap2 = countMap(iter.iterator());

        // Iterate through each element in the first Iterable.
        for (final Object object : subIter) {
            // Compare the occurrence count of elements in the first Iterable with those in the second Iterable.
            // If the occurrence count of an element in the first Iterable is greater than in the second Iterable, it's
            // not a sub-collection.
            if (MathKit.nullToZero(countMap1.get(object)) > MathKit.nullToZero(countMap2.get(object))) {
                return false;
            }
        }
        // If all element occurrence count comparisons satisfy the sub-collection relationship, return true.
        return true;
    }

    /**
     * Determines if two {@link Iterable} objects contain the same elements in the same order. Returns {@code true}
     * under the following conditions:
     * <ul>
     * <li>Both {@link Iterable} objects are {@code null}.</li>
     * <li>Both {@link Iterable} objects refer to the same instance ({@code iterable1 == iterable2}).</li>
     * <li>All elements at corresponding positions in both {@link Iterable} objects satisfy
     * {@link Objects#equals(Object, Object)}.</li>
     * </ul>
     * This method is inspired by Apache Commons Collections4.
     *
     * @param iterable1 the first {@link Iterable} to compare
     * @param iterable2 the second {@link Iterable} to compare
     * @return {@code true} if the two iterables are equal in content and order, {@code false} otherwise
     */
    public static boolean isEqualList(final Iterable<?> iterable1, final Iterable<?> iterable2) {
        return equals(iterable1, iterable2, false);
    }

    /**
     * Determines if two {@link Iterable} objects contain the same elements, with an option to ignore order. Returns
     * {@code true} under the following conditions:
     * <ul>
     * <li>Both {@link Iterable} objects are {@code null}.</li>
     * <li>Both {@link Iterable} objects refer to the same instance ({@code iterable1 == iterable2}).</li>
     * <li>If order is ignored, it checks if the elements and their frequencies are the same in both collections.</li>
     * <li>If order is not ignored, all elements at corresponding positions in both {@link Iterable} objects must
     * satisfy {@link Objects#equals(Object, Object)}.</li>
     * </ul>
     *
     * @param iterable1   the first {@link Iterable} to compare
     * @param iterable2   the second {@link Iterable} to compare
     * @param ignoreOrder {@code true} to ignore the order of elements, {@code false} to consider order
     * @return {@code true} if the two iterables are equal based on the specified criteria, {@code false} otherwise
     */
    public static boolean equals(final Iterable<?> iterable1, final Iterable<?> iterable2, final boolean ignoreOrder) {
        // If both Iterable objects refer to the same instance, they are definitely equal.
        if (iterable1 == iterable2) {
            return true;
        }
        // If either Iterable object is null, they are not equal (unless both are null, handled by the previous check).
        if (iterable1 == null || iterable2 == null) {
            return false;
        }

        if (ignoreOrder) {
            final Map<?, Integer> countMap1 = countMap(iterable1.iterator());
            final Map<?, Integer> countMap2 = countMap(iterable2.iterator());

            if (countMap1.size() != countMap2.size()) {
                // If the number of distinct elements is different, they are not equal.
                return false;
            }

            for (final Object object : iterable1) {
                // Compare the occurrence count of elements in the first Iterable with those in the second Iterable.
                if (MathKit.nullToZero(countMap1.get(object)) != MathKit.nullToZero(countMap2.get(object))) {
                    return false;
                }
            }
            // If all element occurrence count comparisons match, return true.
            return true;
        } else {
            final Iterator<?> iter1 = iterable1.iterator();
            final Iterator<?> iter2 = iterable2.iterator();
            Object obj1;
            Object obj2;
            while (iter1.hasNext() && iter2.hasNext()) {
                obj1 = iter1.next();
                obj2 = iter2.next();
                if (!Objects.equals(obj1, obj2)) {
                    return false;
                }
            }
            // If one iterator has more elements than the other, they are not equal.
            return !(iter1.hasNext() || iter2.hasNext());
        }
    }

    /**
     * Iterates through the given {@link Iterator} and retrieves the element at the specified index.
     *
     * @param iterator the {@link Iterator} to traverse
     * @param index    the zero-based index of the element to retrieve
     * @param <E>      the type of elements in the iterator
     * @return the element at the specified index, or {@code null} if the index is out of bounds or the iterator is
     *         {@code null}
     * @throws IndexOutOfBoundsException if {@code index} is less than 0
     */
    public static <E> E get(final Iterator<E> iterator, int index) throws IndexOutOfBoundsException {
        if (null == iterator) {
            return null;
        }
        Assert.isTrue(index >= 0, "[index] must be >= 0");
        while (iterator.hasNext()) {
            index--;
            if (-1 == index) {
                return iterator.next();
            }
            iterator.next();
        }
        return null;
    }

    /**
     * Retrieves the first element from the given {@link Iterator}.
     *
     * @param <T>      the type of elements in the iterator
     * @param iterator the {@link Iterator} to get the first element from
     * @return the first element, or {@code null} if the iterator is empty or {@code null}
     */
    public static <T> T getFirst(final Iterator<T> iterator) {
        return get(iterator, 0);
    }

    /**
     * Returns the first element from the {@link Iterator} that satisfies the given {@link Predicate}.
     *
     * @param <T>       the type of elements in the iterator
     * @param iterator  the {@link Iterator} to search through
     * @param predicate the {@link Predicate} to test elements against, must not be {@code null}
     * @return the first matching element, or {@code null} if no element matches or the iterator is {@code null}
     * @throws IllegalArgumentException if the {@code predicate} is {@code null}
     */
    public static <T> T getFirst(final Iterator<T> iterator, final Predicate<T> predicate) {
        Assert.notNull(predicate, "Matcher must be not null !");
        if (null != iterator) {
            while (iterator.hasNext()) {
                final T next = iterator.next();
                if (predicate.test(next)) {
                    return next;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the first non-{@code null} element from the given {@link Iterator}.
     *
     * @param <T>      the type of elements in the iterator
     * @param iterator the {@link Iterator} to search through
     * @return the first non-{@code null} element, or {@code null} if all elements are {@code null} or the iterator is
     *         empty/{@code null}
     */
    public static <T> T getFirstNoneNull(final Iterator<T> iterator) {
        return getFirst(iterator, Objects::nonNull);
    }

    /**
     * Creates a frequency map for the elements in the given {@link Iterator}. Each element in the iterator becomes a
     * key in the map, and its corresponding value is the number of times it appears. For example, for an iterator over
     * {@code [a, b, c, c, c]}, the map would be: {@code {a: 1, b: 1, c: 3}}.
     *
     * @param <T>  the type of elements in the iterator
     * @param iter the {@link Iterator} to count elements from. If {@code null}, an empty map is returned.
     * @return a {@link Map} where keys are elements and values are their frequencies
     */
    public static <T> Map<T, Integer> countMap(final Iterator<T> iter) {
        final Map<T, Integer> countMap = new HashMap<>();
        if (null != iter) {
            while (iter.hasNext()) {
                countMap.merge(iter.next(), 1, Integer::sum);
            }
        }
        return countMap;
    }

}
