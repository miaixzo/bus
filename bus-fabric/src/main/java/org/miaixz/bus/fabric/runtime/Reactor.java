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
package org.miaixz.bus.fabric.runtime;

import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.registry.Directory;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.ResourceScope;

/**
 * Shared reactor for dispatch, directory, observation, and resources.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Reactor implements AutoCloseable {

    /**
     * Dispatcher component.
     */
    private final Dispatcher dispatcher;

    /**
     * Clock component.
     */
    private final Clock clock;

    /**
     * Directory component.
     */
    private final Directory directory;

    /**
     * Observer component.
     */
    private final EventObserver observer;

    /**
     * Resource scope.
     */
    private final ResourceScope scope;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a reactor.
     *
     * @param dispatcher dispatcher
     * @param clock      clock
     * @param directory  directory
     * @param observer   observer
     * @param scope      scope
     */
    private Reactor(final Dispatcher dispatcher, final Clock clock, final Directory directory,
            final EventObserver observer, final ResourceScope scope) {
        this.dispatcher = require(dispatcher, "Dispatcher");
        this.clock = require(clock, "Clock");
        this.directory = require(directory, "Directory");
        this.observer = EventObserver.safe(require(observer, "Observer"));
        this.scope = require(scope, "Scope");
        this.closed = new AtomicBoolean();
        this.directory.connectionPool().startIdleEviction(this.dispatcher, this.clock);
    }

    /**
     * Creates a default reactor.
     *
     * @return reactor
     */
    public static Reactor create() {
        return builder().build();
    }

    /**
     * Creates a reactor builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the dispatcher.
     *
     * @return dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Returns the clock.
     *
     * @return clock
     */
    public Clock clock() {
        return clock;
    }

    /**
     * Returns the directory.
     *
     * @return directory
     */
    public Directory directory() {
        return directory;
    }

    /**
     * Returns the observer.
     *
     * @return observer
     */
    public EventObserver observer() {
        return observer;
    }

    /**
     * Returns the resource scope.
     *
     * @return resource scope
     */
    public ResourceScope scope() {
        return scope;
    }

    /**
     * Cancels work associated with a tag.
     *
     * @param tag tag
     * @return true when cancellation matched work
     */
    public boolean cancel(final Object tag) {
        return dispatcher.cancel(Assert.notNull(tag, () -> new ValidateException("Tag must not be null")));
    }

    /**
     * Closes runtime resources.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        RuntimeException failure = null;
        try {
            directory.close();
        } catch (final RuntimeException e) {
            failure = e;
        }
        try {
            dispatcher.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        try {
            scope.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        if (failure != null) {
            throw new InternalException("Unable to close reactor", failure);
        }
    }

    /**
     * Validates non-null values.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Builder for reactors.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder implements org.miaixz.bus.core.Builder<Reactor> {

        /**
         * Dispatcher candidate.
         */
        private Dispatcher dispatcher = Dispatcher.create();

        /**
         * Clock candidate.
         */
        private Clock clock = Clock.system();

        /**
         * Directory candidate.
         */
        private Directory directory = Directory.create();

        /**
         * Observer candidate.
         */
        private EventObserver observer = EventObserver.noop();

        /**
         * Scope candidate.
         */
        private ResourceScope scope = ResourceScope.create();

        /**
         * Creates a runtime builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the dispatcher.
         *
         * @param dispatcher dispatcher
         * @return this builder
         */
        public Builder dispatcher(final Dispatcher dispatcher) {
            this.dispatcher = require(dispatcher, "Dispatcher");
            return this;
        }

        /**
         * Sets the clock.
         *
         * @param clock clock
         * @return this builder
         */
        public Builder clock(final Clock clock) {
            this.clock = require(clock, "Clock");
            return this;
        }

        /**
         * Sets the directory.
         *
         * @param directory directory
         * @return this builder
         */
        public Builder directory(final Directory directory) {
            this.directory = require(directory, "Directory");
            return this;
        }

        /**
         * Sets the observer.
         *
         * @param observer observer
         * @return this builder
         */
        public Builder observer(final EventObserver observer) {
            this.observer = require(observer, "Observer");
            return this;
        }

        /**
         * Builds a reactor.
         *
         * @return reactor
         */
        @Override
        public Reactor build() {
            return new Reactor(dispatcher, clock, directory, observer, scope);
        }

    }

}
