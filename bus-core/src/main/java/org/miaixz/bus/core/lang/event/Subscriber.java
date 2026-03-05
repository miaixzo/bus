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
