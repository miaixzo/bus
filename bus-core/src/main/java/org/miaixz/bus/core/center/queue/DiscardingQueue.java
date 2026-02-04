/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
