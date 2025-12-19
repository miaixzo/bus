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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.center.list.AvgPartition;
import org.miaixz.bus.core.center.list.Partition;
import org.miaixz.bus.core.center.list.RandomAccessAvgPartition;
import org.miaixz.bus.core.center.list.RandomAccessPartition;
import org.miaixz.bus.core.compare.PinyinCompare;
import org.miaixz.bus.core.compare.PropertyCompare;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.lang.reflect.creator.PossibleObjectCreator;

/**
 * Utility class for `List`.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListKit {

    /**
     * Creates a new {@link ArrayList}.
     *
     * @param <T>    The element type.
     * @param values The initial values.
     * @return A new `ArrayList`.
     */
    @SafeVarargs
    public static <T> ArrayList<T> of(final T... values) {
        if (ArrayKit.isEmpty(values)) {
            return new ArrayList<>();
        }
        final ArrayList<T> arrayList = new ArrayList<>(values.length);
        Collections.addAll(arrayList, values);
        return arrayList;
    }

    /**
     * Creates a new {@link LinkedList}.
     *
     * @param <T>    The element type.
     * @param values The initial values.
     * @return A new `LinkedList`.
     */
    @SafeVarargs
    public static <T> LinkedList<T> ofLinked(final T... values) {
        final LinkedList<T> list = new LinkedList<>();
        if (ArrayKit.isNotEmpty(values)) {
            Collections.addAll(list, values);
        }
        return list;
    }

    /**
     * Creates a new `List`.
     *
     * @param <T>      The element type.
     * @param isLinked If `true`, creates a `LinkedList`; otherwise, an `ArrayList`.
     * @return A new `List`.
     */
    public static <T> List<T> of(final boolean isLinked) {
        return isLinked ? ofLinked() : of();
    }

    /**
     * Creates a new `List` from an `Iterable`.
     *
     * @param <T>      The element type.
     * @param isLinked If `true`, creates a `LinkedList`.
     * @param iterable The `Iterable`.
     * @return A new `List`.
     */
    public static <T> List<T> of(final boolean isLinked, final Iterable<T> iterable) {
        if (null == iterable) {
            return of(isLinked);
        }
        if (iterable instanceof final Collection<T> collection) {
            return isLinked ? new LinkedList<>(collection) : new ArrayList<>(collection);
        }
        return of(isLinked, iterable.iterator());
    }

    /**
     * Creates a new `List` from an `Enumeration`.
     *
     * @param <T>         The element type.
     * @param isLinked    If `true`, creates a `LinkedList`.
     * @param enumeration The `Enumeration`.
     * @return A new `List`.
     */
    public static <T> List<T> of(final boolean isLinked, final Enumeration<T> enumeration) {
        return of(isLinked, (Iterator<T>) new EnumerationIterator<>(enumeration));
    }

    /**
     * Creates a new `List` from an `Iterator`.
     *
     * @param <T>      The element type.
     * @param isLinked If `true`, creates a `LinkedList`.
     * @param iter     The `Iterator`.
     * @return A new `List`.
     */
    public static <T> List<T> of(final boolean isLinked, final Iterator<T> iter) {
        final List<T> list = of(isLinked);
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * Creates a new `ArrayList` from an `Iterable`.
     *
     * @param <T>      The element type.
     * @param iterable The `Iterable`.
     * @return A new `ArrayList`.
     */
    public static <T> ArrayList<T> of(final Iterable<T> iterable) {
        return (ArrayList<T>) of(false, iterable);
    }

    /**
     * Creates a new `ArrayList` from an `Iterator`.
     *
     * @param <T>      The element type.
     * @param iterator The `Iterator`.
     * @return A new `ArrayList`.
     */
    public static <T> ArrayList<T> of(final Iterator<T> iterator) {
        return (ArrayList<T>) of(false, iterator);
    }

    /**
     * Creates a new `ArrayList` from an `Enumeration`.
     *
     * @param <T>         The element type.
     * @param enumeration The `Enumeration`.
     * @return A new `ArrayList`.
     */
    public static <T> ArrayList<T> of(final Enumeration<T> enumeration) {
        return (ArrayList<T>) of(false, enumeration);
    }

    /**
     * Returns an unmodifiable view of the specified array as a list.
     *
     * @param <T> The element type.
     * @param ts  The elements.
     * @return An unmodifiable list.
     */
    @SafeVarargs
    public static <T> List<T> view(final T... ts) {
        return view(of(ts));
    }

    /**
     * Returns an unmodifiable view of the specified list.
     *
     * @param <T> The element type.
     * @param ts  The list.
     * @return An unmodifiable list.
     */
    public static <T> List<T> view(final List<T> ts) {
        if (ArrayKit.isEmpty(ts)) {
            return empty();
        }
        return Collections.unmodifiableList(ts);
    }

    /**
     * Returns an empty, unmodifiable list.
     *
     * @param <T> The element type.
     * @return An empty list.
     * @see Collections#emptyList()
     */
    public static <T> List<T> empty() {
        return Collections.emptyList();
    }

    /**
     * Returns an empty, mutable list with an initial capacity of 0.
     *
     * @param <T> The element type.
     * @return An empty list.
     */
    public static <T> List<T> zero() {
        return new ArrayList<>(0);
    }

    /**
     * Returns an unmodifiable list containing only the specified object.
     *
     * @param <T>     The element type.
     * @param element The element.
     * @return A singleton list.
     */
    public static <T> List<T> singleton(final T element) {
        return Collections.singletonList(element);
    }

    /**
     * Creates a new `CopyOnWriteArrayList` from a collection.
     *
     * @param <T>        The element type.
     * @param collection The collection.
     * @return A new `CopyOnWriteArrayList`.
     */
    public static <T> CopyOnWriteArrayList<T> ofCopyOnWrite(final Collection<T> collection) {
        return (null == collection) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(collection));
    }

    /**
     * Creates a new `CopyOnWriteArrayList` from an array.
     *
     * @param <T> The element type.
     * @param ts  The elements.
     * @return A new `CopyOnWriteArrayList`.
     */
    @SafeVarargs
    public static <T> CopyOnWriteArrayList<T> ofCopyOnWrite(final T... ts) {
        return (null == ts) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(ts));
    }

    /**
     * Sorts a list in place using natural ordering.
     *
     * @param <T>  The element type.
     * @param list The list to sort.
     * @return The original sorted list.
     */
    public static <T> List<T> sort(final List<T> list) {
        return sort(list, null);
    }

    /**
     * Sorts a list in place.
     *
     * @param <T>  The element type.
     * @param list The list to sort.
     * @param c    The `Comparator` (null for null-safe natural ordering).
     * @return The original sorted list.
     */
    public static <T> List<T> sort(final List<T> list, Comparator<? super T> c) {
        if (CollKit.isEmpty(list)) {
            return list;
        }
        if (null == c) {
            c = Comparator.nullsFirst((Comparator<? super T>) Comparator.naturalOrder());
        }
        list.sort(c);
        return list;
    }

    /**
     * Sorts a list of beans by a specified property.
     *
     * @param <T>      The element type.
     * @param list     The list.
     * @param property The property name.
     * @return The sorted list.
     */
    public static <T> List<T> sortByProperty(final List<T> list, final String property) {
        return sort(list, new PropertyCompare<>(property));
    }

    /**
     * Sorts a list of strings based on their Pinyin (Chinese phonetic) order.
     *
     * @param list The list.
     * @return The sorted list.
     */
    public static List<String> sortByPinyin(final List<String> list) {
        return sort(list, new PinyinCompare());
    }

    /**
     * Reverses the order of elements in a list in place.
     *
     * @param <T>  The element type.
     * @param list The list to reverse.
     * @return The reversed list.
     * @see Collections#reverse(List)
     */
    public static <T> List<T> reverse(final List<T> list) {
        if (CollKit.isEmpty(list)) {
            return list;
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * Returns a new list with the elements in reverse order. The original list is not modified.
     *
     * @param <T>  The element type.
     * @param list The list to reverse.
     * @return A new reversed list.
     */
    public static <T> List<T> reverseNew(final List<T> list) {
        if (null == list) {
            return null;
        }
        List<T> list2 = ObjectKit.clone(list);
        if (null == list2) {
            list2 = new ArrayList<>(list);
        }
        try {
            return reverse(list2);
        } catch (final UnsupportedOperationException e) {
            return reverse(of(list));
        }
    }

    /**
     * Sets or appends an element. If the index is within bounds, it replaces the element; otherwise, it adds it to the
     * end.
     *
     * @param <T>     The element type.
     * @param list    The list.
     * @param index   The index.
     * @param element The new element.
     * @return The modified list.
     */
    public static <T> List<T> setOrAppend(final List<T> list, final int index, final T element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            list.add(element);
        }
        return list;
    }

    /**
     * Sets an element at a specific index, padding with `null` if the index is out of bounds.
     *
     * @param <T>     The element type.
     * @param list    The list.
     * @param index   The index.
     * @param element The new element.
     * @return The modified list.
     */
    public static <T> List<T> setOrPadding(final List<T> list, final int index, final T element) {
        return setOrPadding(list, index, element, null);
    }

    /**
     * Sets an element at a specific index, padding with a specified element if the index is out of bounds.
     *
     * @param <T>            The element type.
     * @param list           The list.
     * @param index          The index.
     * @param element        The new element.
     * @param paddingElement The element to use for padding.
     * @return The modified list.
     */
    public static <T> List<T> setOrPadding(
            final List<T> list,
            final int index,
            final T element,
            final T paddingElement) {
        return setOrPadding(list, index, element, paddingElement, (list.size() + 1) * 10);
    }

    /**
     * Sets an element at a specific index, padding with a specified element if the index is out of bounds.
     *
     * @param <T>            The element type.
     * @param list           The list.
     * @param index          The index.
     * @param element        The new element.
     * @param paddingElement The element to use for padding.
     * @param indexLimit     The maximum allowed index.
     * @return The modified list.
     */
    public static <T> List<T> setOrPadding(
            final List<T> list,
            final int index,
            final T element,
            final T paddingElement,
            final int indexLimit) {
        Assert.notNull(list, "List must be not null !");
        final int size = list.size();
        if (index < size) {
            list.set(index, element);
        } else {
            if (indexLimit > 0) {
                Validator.checkIndexLimit(index, indexLimit);
            }
            for (int i = size; i < index; i++) {
                list.add(paddingElement);
            }
            list.add(element);
        }
        return list;
    }

    /**
     * Slices a portion of a list.
     *
     * @param <T>       The element type.
     * @param list      The list to slice.
     * @param fromIndex The start index (inclusive).
     * @param toIndex   The end index (exclusive).
     * @return The sliced list as a new `ArrayList`.
     */
    public static <T> List<T> sub(final List<T> list, final int fromIndex, final int toIndex) {
        return sub(list, fromIndex, toIndex, 1);
    }

    /**
     * Slices a portion of a list with a given step. This creates a new list, unlike `List.subList`.
     *
     * @param <T>       The element type.
     * @param list      The list to slice.
     * @param fromIndex The start index (inclusive).
     * @param toIndex   The end index (exclusive).
     * @param step      The step size.
     * @return The sliced list as a new `ArrayList`.
     */
    public static <T> List<T> sub(final List<T> list, int fromIndex, int toIndex, int step) {
        if (list == null) {
            return null;
        }

        List<T> result = PossibleObjectCreator.of(list.getClass()).create();
        if (null == result) {
            result = new ArrayList<>(0);
        }

        if (list.isEmpty()) {
            return result;
        }

        final int size = list.size();
        if (fromIndex < 0) {
            fromIndex += size;
        }
        if (toIndex < 0) {
            toIndex += size;
        }
        if (fromIndex == size) {
            return result;
        }
        if (fromIndex > toIndex) {
            final int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }
        if (toIndex > size) {
            if (fromIndex >= size) {
                return result;
            }
            toIndex = size;
        }

        if (step < 1) {
            step = 1;
        }

        for (int i = fromIndex; i < toIndex; i += step) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * Gets the index of the last element that matches a predicate.
     *
     * @param <T>     The element type.
     * @param list    The list.
     * @param matcher The predicate to match.
     * @return The index of the last match, or -1 if not found.
     */
    public static <T> int lastIndexOf(final List<T> list, final Predicate<? super T> matcher) {
        if (null != list) {
            final int size = list.size();
            if (size > 0) {
                for (int i = size - 1; i >= 0; i--) {
                    if (null == matcher || matcher.test(list.get(i))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Partitions a list into sub-lists of a specified size. The returned lists are views of the original list.
     *
     * @param <T>  The element type.
     * @param list The list.
     * @param size The size of each partition.
     * @return A list of sub-lists.
     */
    public static <T> List<List<T>> partition(final List<T> list, final int size) {
        if (CollKit.isEmpty(list)) {
            return empty();
        }
        return (list instanceof RandomAccess) ? new RandomAccessPartition<>(list, size) : new Partition<>(list, size);
    }

    /**
     * Partitions a list into a specified number of sub-lists of roughly equal size.
     *
     * @param <T>   The element type.
     * @param list  The list.
     * @param limit The number of partitions.
     * @return A list of sub-lists.
     */
    public static <T> List<List<T>> avgPartition(final List<T> list, final int limit) {
        if (CollKit.isEmpty(list)) {
            return empty();
        }
        return (list instanceof RandomAccess) ? new RandomAccessAvgPartition<>(list, limit)
                : new AvgPartition<>(list, limit);
    }

    /**
     * Moves an element to a new position within the list.
     *
     * @param <T>         The element type.
     * @param list        The list.
     * @param element     The element to move.
     * @param targetIndex The target index.
     */
    public static <T> void swapTo(final List<T> list, final T element, final Integer targetIndex) {
        if (CollKit.isNotEmpty(list)) {
            final int index = list.indexOf(element);
            if (index >= 0) {
                Collections.swap(list, index, targetIndex);
            }
        }
    }

    /**
     * Swaps the positions of two elements in a list.
     *
     * @param <T>           The element type.
     * @param list          The list.
     * @param element       The first element.
     * @param targetElement The second element.
     */
    public static <T> void swapElement(final List<T> list, final T element, final T targetElement) {
        if (CollKit.isNotEmpty(list)) {
            final int targetIndex = list.indexOf(targetElement);
            if (targetIndex >= 0) {
                swapTo(list, element, targetIndex);
            }
        }
    }

    /**
     * Returns an unmodifiable view of the specified list.
     *
     * @param <T> The element type.
     * @param c   The collection.
     * @return An unmodifiable list.
     */
    public static <T> List<T> unmodifiable(final List<? extends T> c) {
        if (null == c) {
            return null;
        }
        return Collections.unmodifiableList(c);
    }

    /**
     * Adds all elements from another list if they are not already present.
     *
     * @param <T>       The element type.
     * @param list      The target list.
     * @param otherList The list of elements to add.
     * @return The modified target list.
     */
    public static <T> List<T> addAllIfNotContains(final List<T> list, final List<T> otherList) {
        for (final T t : otherList) {
            if (!list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Changes the contents of a list by removing or replacing existing elements and/or adding new elements. This is
     * similar to JavaScript's `splice` function and does not modify the original list.
     *
     * @param <T>         The element type.
     * @param list        The list.
     * @param start       The starting index (can be negative).
     * @param deleteCount The number of elements to remove.
     * @param items       The elements to add.
     * @return A new list with the changes.
     */
    @SafeVarargs
    public static <T> List<T> splice(final List<T> list, int start, int deleteCount, final T... items) {
        if (CollKit.isEmpty(list)) {
            return zero();
        }
        final int size = list.size();
        if (start < 0) {
            start += size;
        } else if (start >= size) {
            start = size;
            deleteCount = 0;
        }
        if (start + deleteCount > size) {
            deleteCount = size - start;
        }

        final int newSize = size - deleteCount + items.length;
        List<T> resList = list;
        if (newSize > size) {
            resList = new ArrayList<>(newSize);
            resList.addAll(list);
        }
        if (deleteCount > 0) {
            resList.subList(start, start + deleteCount).clear();
        }
        if (ArrayKit.isNotEmpty(items)) {
            resList.addAll(start, Arrays.asList(items));
        }
        return resList;
    }

    /**
     * Paginates a list.
     *
     * @param <T>      The element type.
     * @param pageNo   The page number (1-based).
     * @param pageSize The number of items per page.
     * @param list     The list.
     * @return A sublist representing the page.
     */
    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        if (CollKit.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        int resultSize = list.size();
        if (resultSize <= pageSize) {
            if (pageNo <= 1) {
                return unmodifiable(list);
            } else {
                return new ArrayList<>(0);
            }
        }
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 0;
        }

        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize;
        if (end > resultSize) {
            end = resultSize;
        }
        if (start > end) {
            return new ArrayList<>(0);
        }
        return sub(list, start, end);
    }

    /**
     * Moves an element to a new position within the list. If the element is not in the list, it is added at the new
     * position. If the element is already in the list, it is first removed and then added at the new position.
     *
     * @param <T>         The element type.
     * @param list        The list to modify.
     * @param element     The element to move.
     * @param newPosition The new position for the element.
     * @return The modified list.
     */
    public static <T> List<T> move(final List<T> list, final T element, final int newPosition) {
        Assert.notNull(list);
        if (!list.contains(element)) {
            list.add(newPosition, element);
        } else {
            list.remove(element);
            list.add(newPosition, element);
        }
        return list;
    }

}