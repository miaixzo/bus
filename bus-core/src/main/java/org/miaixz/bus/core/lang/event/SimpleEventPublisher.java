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
package org.miaixz.bus.core.lang.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.miaixz.bus.core.Loader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.loader.LazyFunLoader;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A simple implementation of {@link EventPublisher} based on {@link Subscriber} and {@link Event}. This publisher
 * manages a list of subscribers and dispatches events to them, supporting both synchronous and asynchronous execution
 * based on the subscriber's preference.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleEventPublisher implements EventPublisher {

    /**
     * The list of registered subscribers.
     */
    private final List<Subscriber> subscribers;
    /**
     * A loader for the {@link ExecutorService} used for asynchronous event processing. Defaults to a new executor
     * created by {@link ThreadKit#newExecutor()}.
     */
    private Loader<ExecutorService> executorServiceLoader;

    /**
     * Constructs a new {@code SimpleEventPublisher}.
     *
     * @param subscribers           The initial list of subscribers. Can be {@code null} to start with an empty list.
     * @param executorServiceLoader A loader for the {@link ExecutorService} to be used for asynchronous event
     *                              execution. If {@code null}, a default executor will be used via
     *                              {@link ThreadKit#newExecutor()}.
     */
    public SimpleEventPublisher(final List<Subscriber> subscribers,
            final Loader<ExecutorService> executorServiceLoader) {
        this.subscribers = ObjectKit.defaultIfNull(subscribers, ArrayList::new);
        this.executorServiceLoader = ObjectKit
                .defaultIfNull(executorServiceLoader, LazyFunLoader.of(ThreadKit::newExecutor));
    }

    /**
     * Creates a default {@code SimpleEventPublisher} instance. It starts with no subscribers and uses a default
     * {@link ExecutorService} created by {@link ThreadKit#newExecutor()} for asynchronous tasks.
     *
     * @return A new {@code SimpleEventPublisher} instance.
     */
    public static SimpleEventPublisher of() {
        return of(null);
    }

    /**
     * Creates a {@code SimpleEventPublisher} instance with an optional initial list of subscribers. It uses a default
     * {@link ExecutorService} created by {@link ThreadKit#newExecutor()} for asynchronous tasks.
     *
     * @param subscribers An initial list of subscribers. Can be {@code null} or an empty list. Additional subscribers
     *                    can be added later using {@link #register(Subscriber)}.
     * @return A new {@code SimpleEventPublisher} instance.
     */
    public static SimpleEventPublisher of(final List<Subscriber> subscribers) {
        return new SimpleEventPublisher(subscribers, null);
    }

    /**
     * Sets a custom {@link ExecutorService} for asynchronous event processing. If not set, a default executor from
     * {@link ThreadKit#newExecutor()} is used.
     *
     * @param executorService The {@link ExecutorService} to use. Must not be {@code null}.
     * @return This {@code SimpleEventPublisher} instance, allowing for method chaining.
     * @throws IllegalArgumentException if the provided {@code executorService} is {@code null}.
     */
    public SimpleEventPublisher setExecutorService(final ExecutorService executorService) {
        this.executorServiceLoader = () -> Assert.notNull(executorService);
        return this;
    }

    /**
     * Registers a {@link Subscriber} with this publisher. Registered subscribers will receive events published by this
     * instance. The list of subscribers is sorted after each registration based on their {@link Subscriber#order()}.
     *
     * @param subscriber The subscriber to register.
     * @return This {@code EventPublisher} instance, allowing for method chaining.
     */
    @Override
    public EventPublisher register(final Subscriber subscriber) {
        subscribers.add(subscriber);
        Collections.sort(subscribers);
        return this;
    }

    /**
     * Publishes an {@link Event} to all registered subscribers. If a subscriber is configured for asynchronous
     * execution ({@link Subscriber#async()} returns {@code true}), the event will be processed in the
     * {@link ExecutorService}. Otherwise, it will be processed synchronously.
     *
     * @param event The event object to publish.
     */
    @Override
    public void publish(final Event event) {
        for (final Subscriber subscriber : subscribers) {
            if (subscriber.async()) {
                executorServiceLoader.get().submit(() -> subscriber.update(event));
            } else {
                subscriber.update(event);
            }
        }
    }

}
