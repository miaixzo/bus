/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.center.BoundedCollection;

/**
 * A {@link List} implementation with a specified maximum boundary size. This class restricts the maximum capacity of
 * the list.
 *
 * @param <E> the type of elements in this list
 * @author Kimi Liu
 * @since Java 21+
 */
public class BoundedList<E> extends ListWrapper<E> implements BoundedCollection<E> {

    /**
     * The maximum allowed size of this list.
     */
    private final int maxSize;

    /**
     * Constructs a new {@code BoundedList} with the specified maximum capacity. An internal {@link ArrayList} is used
     * as the backing list.
     *
     * @param maxSize the maximum number of elements this list can hold
     */
    public BoundedList(final int maxSize) {
        this(new ArrayList<>(maxSize), maxSize);
    }

    /**
     * Constructs a new {@code BoundedList} with the specified backing {@link List} and maximum capacity.
     *
     * @param raw     the underlying {@link List} to wrap
     * @param maxSize the maximum number of elements this list can hold
     */
    public BoundedList(final List<E> raw, final int maxSize) {
        super(raw);
        this.maxSize = maxSize;
    }

    /**
     * Checks if this list is full.
     *
     * @return {@code true} if the current size of the list equals its maximum capacity, {@code false} otherwise
     */
    @Override
    public boolean isFull() {
        return size() == this.maxSize;
    }

    /**
     * Returns the maximum allowed size of this list.
     *
     * @return the maximum size of the list
     */
    @Override
    public int maxSize() {
        return this.maxSize;
    }

    /**
     * Appends the specified element to the end of this list. This operation is only allowed if the list is not full.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IndexOutOfBoundsException if adding the element would exceed the maximum capacity of the list
     */
    @Override
    public boolean add(final E e) {
        checkFull(1);
        return super.add(e);
    }

    /**
     * Inserts the specified element at the specified position in this list. This operation is only allowed if the list
     * is not full.
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException if adding the element would exceed the maximum capacity of the list or if the
     *                                   index is out of range ({@code index < 0 || index > size()})
     */
    @Override
    public void add(final int index, final E element) {
        checkFull(1);
        super.add(index, element);
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list, in the order that they are
     * returned by the specified collection's iterator. This operation is only allowed if adding all elements would not
     * exceed the maximum capacity of the list.
     *
     * @param c collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if adding the elements would exceed the maximum capacity of the list
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        checkFull(c.size());
        return super.addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list at the specified position. This operation
     * is only allowed if adding all elements would not exceed the maximum capacity of the list.
     *
     * @param index index at which to insert the first element from the specified collection
     * @param c     collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException if adding the elements would exceed the maximum capacity of the list or if the
     *                                   index is out of range ({@code index < 0 || index > size()})
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        checkFull(c.size());
        return super.addAll(index, c);
    }

    /**
     * Checks if the list has enough space to add a given number of elements.
     *
     * @param addSize the number of elements to be added
     * @throws IndexOutOfBoundsException if adding {@code addSize} elements would exceed the maximum capacity of the
     *                                   list
     */
    private void checkFull(final int addSize) {
        if (size() + addSize > this.maxSize) {
            throw new IndexOutOfBoundsException("List is no space to add " + addSize + " elements!");
        }
    }

}
