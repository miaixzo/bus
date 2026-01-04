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

    @Override
    public boolean isFull() {
        return size() == capacity;
    }

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

    @Override
    public Iterator<E> iterator() {
        return toList().iterator();
    }

}
