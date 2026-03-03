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
package org.miaixz.bus.core.center.queue;

import java.io.Serial;
import java.util.*;

import org.miaixz.bus.core.center.BoundedCollection;

/**
 * A bounded priority queue. Elements are sorted according to the given sorting rules. When the queue is full, the last
 * element is eliminated according to the given sorting rules (removing the last element).
 *
 * @param <E> the element type
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> implements BoundedCollection<E> {

    @Serial
    private static final long serialVersionUID = 2852278996807L;

    /**
     * The capacity.
     */
    private final int capacity;
    /**
     * The comparator.
     */
    private final Comparator<? super E> comparator;

    /**
     * Constructs a new BoundedPriorityQueue.
     *
     * @param capacity the capacity
     */
    public BoundedPriorityQueue(final int capacity) {
        this(capacity, null);
    }

    /**
     * Constructs a new BoundedPriorityQueue.
     *
     * @param capacity   the capacity
     * @param comparator the comparator
     */
    public BoundedPriorityQueue(final int capacity, final Comparator<? super E> comparator) {
        super(capacity, (o1, o2) -> {
            final int cResult;
            if (comparator != null) {
                cResult = comparator.compare(o1, o2);
            } else {
                final Comparable<E> o1c = (Comparable<E>) o1;
                cResult = o1c.compareTo(o2);
            }

            return -cResult;
        });
        this.capacity = capacity;
        this.comparator = comparator;
    }

    /**
     * Returns true if this cache is full.
     *
     * @return true if this cache is full
     */
    @Override
    public boolean isFull() {
        return size() == capacity;
    }

    /**
     * Maxsize method.
     *
     * @return the int value
     */
    @Override
    public int maxSize() {
        return capacity;
    }

    /**
     * Adds an element. When the queue is full, the last element is eliminated.
     *
     * @param e the element
     * @return whether the addition was successful
     */
    @Override
    public boolean offer(final E e) {
        if (isFull()) {
            final E head = peek();
            if (this.comparator().compare(e, head) <= 0) {
                return true;
            }
            // When the queue is full, eliminate the top element
            poll();
        }
        return super.offer(e);
    }

    /**
     * Adds multiple elements. For collection parameters, please use {@link PriorityQueue#addAll}.
     *
     * @param c the element array
     * @return whether any changes were made
     */
    public boolean addAll(final E[] c) {
        return this.addAll(Arrays.asList(c));
    }

    /**
     * Returns a sorted list of all elements in this queue.
     *
     * @return the sorted list
     */
    public ArrayList<E> toList() {
        final ArrayList<E> list = new ArrayList<>(this);
        list.sort(comparator);
        return list;
    }

    /**
     * Returns an iterator over elements of type T.
     *
     * @return an Iterator
     */
    @Override
    public Iterator<E> iterator() {
        return toList().iterator();
    }

}
