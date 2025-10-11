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
 * @since Java 17+
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
