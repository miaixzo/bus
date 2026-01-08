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
package org.miaixz.bus.core.center.set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link Set} implementation backed by a {@link Map}. This class provides a way to create a Set view of the keys of
 * an underlying Map.
 *
 * @param <E> The type of elements in the set.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetFromMap<E> extends AbstractSet<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852280238812L;

    /**
     * The backing map. The values in this map are always {@link Boolean#TRUE}.
     */
    private final Map<E, Boolean> map;
    /**
     * The key set of the backing map, which serves as the actual set implementation.
     */
    private transient Set<E> set;

    /**
     * Constructs a new {@code SetFromMap} with the specified backing map.
     *
     * @param map The map to back this set. Its keys will be the elements of this set.
     */
    public SetFromMap(final Map<E, Boolean> map) {
        this.map = map;
        this.set = map.keySet();
    }

    /**
     * Removes all of the elements from this set. The set will be empty after this call returns.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return The number of elements in this set.
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o The element whose presence in this set is to be tested.
     * @return {@code true} if this set contains the specified element.
     */
    @Override
    public boolean contains(final Object o) {
        return map.containsKey(o);
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o The element to be removed from this set, if present.
     * @return {@code true} if the set contained the specified element.
     */
    @Override
    public boolean remove(final Object o) {
        return map.remove(o) != null;
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param e The element to be added to this set.
     * @return {@code true} if this set did not already contain the specified element.
     */
    @Override
    public boolean add(final E e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    /**
     * Returns an iterator over the elements in this set.
     *
     * @return An {@link Iterator} over the elements in this set.
     */
    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    /**
     * Returns an array containing all of the elements in this set.
     *
     * @return An array containing all of the elements in this set.
     */
    @Override
    public Object[] toArray() {
        return set.toArray();
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
        return super.toArray(a);
    }

    /**
     * Returns a string representation of this set.
     *
     * @return A string representation of this set.
     */
    @Override
    public String toString() {
        return set.toString();
    }

    /**
     * Returns the hash code value for this set.
     *
     * @return The hash code value for this set.
     */
    @Override
    public int hashCode() {
        return set.hashCode();
    }

    /**
     * Compares the specified object with this set for equality.
     *
     * @param o The object to be compared for equality with this set.
     * @return {@code true} if the specified object is equal to this set.
     */
    @Override
    public boolean equals(final Object o) {
        return o == this || set.equals(o);
    }

    /**
     * Returns {@code true} if this set contains all of the elements of the specified collection.
     *
     * @param c Collection to be checked for containment in this set.
     * @return {@code true} if this set contains all of the elements of the specified collection.
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return set.containsAll(c);
    }

    /**
     * Removes from this set all of its elements that are contained in the specified collection.
     *
     * @param c Collection containing elements to be removed from this set.
     * @return {@code true} if this set changed as a result of the call.
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return set.removeAll(c);
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
        return set.retainAll(c);
    }

    /**
     * Performs the given action for each element of the {@code Iterable} until all elements have been processed or the
     * action throws an exception.
     *
     * @param action The action to be performed for each element.
     */
    @Override
    public void forEach(final Consumer<? super E> action) {
        set.forEach(action);
    }

    /**
     * Removes all of the elements of this collection that satisfy the given predicate.
     *
     * @param filter A predicate which returns {@code true} for elements to be removed.
     * @return {@code true} if any elements were removed.
     */
    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        return set.removeIf(filter);
    }

    /**
     * Creates a {@link Spliterator} over the elements in this set.
     *
     * @return A {@link Spliterator} over the elements in this set.
     */
    @Override
    public Spliterator<E> spliterator() {
        return set.spliterator();
    }

    /**
     * Returns a sequential {@link Stream} with this collection as its source.
     *
     * @return A sequential {@link Stream} over the elements in this set.
     */
    @Override
    public Stream<E> stream() {
        return set.stream();
    }

    /**
     * Returns a parallel {@link Stream} with this collection as its source.
     *
     * @return A parallel {@link Stream} over the elements in this set.
     */
    @Override
    public Stream<E> parallelStream() {
        return set.parallelStream();
    }

    /**
     * Reconstitutes this {@code SetFromMap} instance from a stream (that is, deserializes it). This method is part of
     * the Java Serialization mechanism.
     *
     * @param stream The {@link ObjectInputStream} from which the state is read.
     * @throws IOException            if an I/O error occurs during deserialization.
     * @throws ClassNotFoundException if the class of a serialized object could not be found.
     */
    private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        set = map.keySet();
    }

}
