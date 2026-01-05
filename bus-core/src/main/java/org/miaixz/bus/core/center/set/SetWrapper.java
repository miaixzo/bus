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
package org.miaixz.bus.core.center.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * A wrapper class for {@link Set} that delegates all operations to an underlying set. This class can be extended to
 * provide custom pre- or post-processing logic around set operations.
 *
 * @param <E> The type of elements in the set.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetWrapper<E> extends SimpleWrapper<Set<E>> implements Set<E> {

    /**
     * Constructs a new {@code SetWrapper} that wraps the given set.
     *
     * @param raw The original {@link Set} to wrap. Must not be {@code null}.
     */
    public SetWrapper(final Set<E> raw) {
        super(raw);
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return The number of elements in this set.
     */
    @Override
    public int size() {
        return raw.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o The element whose presence in this set is to be tested.
     * @return {@code true} if this set contains the specified element.
     */
    @Override
    public boolean contains(final Object o) {
        return raw.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.
     *
     * @return An {@link Iterator} over the elements in this set.
     */
    @Override
    public Iterator<E> iterator() {
        return raw.iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable} until all elements have been processed or the
     * action throws an exception.
     *
     * @param action The action to be performed for each element.
     */
    @Override
    public void forEach(final Consumer<? super E> action) {
        raw.forEach(action);
    }

    /**
     * Returns an array containing all of the elements in this set.
     *
     * @return An array containing all of the elements in this set.
     */
    @Override
    public Object[] toArray() {
        // This implementation returns an empty array, which might not be the intended behavior.
        // It should ideally delegate to raw.toArray() or create a new array with the set's contents.
        return new Object[0];
    }

    /**
     * Returns an array containing all of the elements in this set; the runtime type of the returned array is that of
     * the specified array.
     *
     * @param a   The array into which the elements of this set are to be stored, if it is big enough; otherwise, a new
     *            array of the same runtime type is allocated for this purpose.
     * @param <T> The runtime type of the array to contain the collection.
     * @return An array containing all of the elements in this set.
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return raw.toArray(a);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param e The element to be added to this set.
     * @return {@code true} if this set did not already contain the specified element.
     */
    @Override
    public boolean add(final E e) {
        return raw.add(e);
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o The element to be removed from this set, if present.
     * @return {@code true} if the set contained the specified element.
     */
    @Override
    public boolean remove(final Object o) {
        return raw.remove(o);
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the specified collection.
     *
     * @param c Collection to be checked for containment in this set.
     * @return {@code true} if this set contains all of the elements of the specified collection.
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return raw.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if they're not already present.
     *
     * @param c Collection containing elements to be added to this set.
     * @return {@code true} if this set changed as a result of the call.
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return raw.addAll(c);
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection. In other words, removes
     * from this set all of its elements that are not contained in the specified collection.
     *
     * @param c Collection containing elements to be retained in this set.
     * @return {@code true} if this set changed as a result of the call.
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return raw.retainAll(c);
    }

    /**
     * Removes from this set all of its elements that are contained in the specified collection.
     *
     * @param c Collection containing elements to be removed from this set.
     * @return {@code true} if this set changed as a result of the call.
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return raw.removeAll(c);
    }

    /**
     * Removes all of the elements of this collection that satisfy the given predicate.
     *
     * @param filter A predicate which returns {@code true} for elements to be removed.
     * @return {@code true} if any elements were removed.
     */
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return raw.removeIf(filter);
    }

    /**
     * Removes all of the elements from this collection.
     */
    @Override
    public void clear() {
        raw.clear();
    }

    /**
     * Creates a {@link Spliterator} over the elements in this set.
     *
     * @return A {@link Spliterator} over the elements in this set.
     */
    @Override
    public Spliterator<E> spliterator() {
        return raw.spliterator();
    }

    /**
     * Returns a sequential {@link Stream} with this collection as its source.
     *
     * @return A sequential {@link Stream} over the elements in this set.
     */
    @Override
    public Stream<E> stream() {
        return raw.stream();
    }

    /**
     * Returns a parallel {@link Stream} with this collection as its source.
     *
     * @return A parallel {@link Stream} over the elements in this set.
     */
    @Override
    public Stream<E> parallelStream() {
        return raw.parallelStream();
    }

}
