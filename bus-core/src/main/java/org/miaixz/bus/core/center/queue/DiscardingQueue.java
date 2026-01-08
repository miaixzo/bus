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
package org.miaixz.bus.core.center.queue;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;

/**
 * A queue that is always empty, all added nodes are discarded.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DiscardingQueue extends AbstractQueue<Object> {

    /**
     * Constructs a new DiscardingQueue. Utility class constructor for static access.
     */
    private DiscardingQueue() {
    }

    private static final DiscardingQueue INSTANCE = new DiscardingQueue();

    /**
     * Gets the singleton empty queue instance.
     *
     * @param <E> the element type
     * @return the DiscardingQueue instance
     */
    public static <E> Queue<E> getInstance() {
        return (Queue<E>) INSTANCE;
    }

    /**
     * Add method.
     *
     * @return the boolean value
     */
    @Override
    public boolean add(final Object e) {
        return true;
    }

    /**
     * Offer method.
     *
     * @return the boolean value
     */
    @Override
    public boolean offer(final Object e) {
        return true;
    }

    /**
     * Poll method.
     *
     * @return the Object value
     */
    @Override
    public Object poll() {
        return null;
    }

    /**
     * Peek method.
     *
     * @return the Object value
     */
    @Override
    public Object peek() {
        return null;
    }

    /**
     * Returns the number of elements in this collection.
     *
     * @return the number of elements
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Returns an iterator over elements of type T.
     *
     * @return an Iterator
     */
    @Override
    public Iterator<Object> iterator() {
        return Collections.emptyIterator();
    }

}
