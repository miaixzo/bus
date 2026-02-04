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
package org.miaixz.bus.core.lang.event;

/**
 * Interface for event publishers, used to publish events to registered subscribers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EventPublisher {

    /**
     * Registers a subscriber to receive events. Once registered, the subscriber will receive all events published by
     * this publisher.
     *
     * @param subscriber The subscriber to register.
     * @return This {@code EventPublisher} instance, allowing for method chaining.
     */
    EventPublisher register(Subscriber subscriber);

    /**
     * Publishes an event to registered subscribers. The event publisher can define custom publishing strategies, such
     * as:
     * <ul>
     * <li>Broadcasting to all subscribers (multi-subscription).</li>
     * <li>Subscribers receiving the message in a specific order or by weight, with subsequent subscribers not receiving
     * it after one has processed it (single-subscription).</li>
     * <li>Selecting subscribers based on custom rules, such as message content or event type (selective
     * multi-subscription).</li>
     * </ul>
     *
     * @param event The event object to publish.
     */
    void publish(Event event);

}
