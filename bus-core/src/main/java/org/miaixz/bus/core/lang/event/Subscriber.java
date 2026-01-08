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
package org.miaixz.bus.core.lang.event;

import java.util.EventListener;

import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Interface for event subscribers. Implementations of this interface can register to receive and process events.
 * Subscribers are comparable based on their {@link #order()} for determining execution sequence.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Subscriber extends EventListener, Comparable<Subscriber> {

    /**
     * Action to be performed when an event occurs.
     *
     * @param event The event object. Implementations can choose whether to process the event based on its type or
     *              content.
     */
    void update(Event event);

    /**
     * Retrieves the execution order of this subscriber. Subscribers with smaller order values will be executed before
     * those with larger values. The default order is 1000.
     *
     * @return The execution order.
     */
    default int order() {
        return 1000;
    }

    /**
     * Compares this subscriber with the specified subscriber for order. The comparison is based on the {@link #order()}
     * method, allowing subscribers to be sorted.
     *
     * @param o The subscriber to be compared.
     * @return A negative integer, zero, or a positive integer as this subscriber's order is less than, equal to, or
     *         greater than the specified subscriber's order.
     */
    @Override
    default int compareTo(final Subscriber o) {
        return CompareKit.compare(this.order(), o.order());
    }

    /**
     * Determines whether this subscriber should execute asynchronously. By default, subscribers execute synchronously.
     *
     * @return {@code true} if the subscriber should execute asynchronously, {@code false} otherwise.
     */
    default boolean async() {
        return false;
    }

}
