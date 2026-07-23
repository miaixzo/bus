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
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.registry.Directory;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;
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
     * Single runtime metric owner borrowed by Directory, Registry and Pool.
     */
    private final FabricMeter meter;

    /**
     * Resource scope.
     */
    private final ResourceScope scope;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a reactor from fully resolved owned collaborators.
     *
     * @param clock      runtime wall and monotonic clock
     * @param observer   runtime event observer
     * @param meter      runtime metrics registry
     * @param dispatcher runtime activity dispatcher
     * @param scope      root resource scope
     * @param directory  runtime registry and connection directory
     */
    private Reactor(final Clock clock, final EventObserver observer, final FabricMeter meter,
            final Dispatcher dispatcher, final ResourceScope scope, final Directory directory) {
        this.clock = require(clock, "Clock");
        final EventObserver currentObserver = require(observer, "Observer");
        this.observer = currentObserver;
        this.meter = require(meter, "Fabric meter");
        this.dispatcher = require(dispatcher, "Dispatcher");
        this.scope = require(scope, "Scope");
        this.directory = require(directory, "Directory");
        this.closed = new AtomicBoolean();
        this.directory.connectionPool().startIdleEviction(this.dispatcher);
    }

    /**
     * Creates a default reactor.
     *
     * @return reactor built with default runtime collaborators
     */
    public static Reactor create() {
        return builder().build();
    }

    /**
     * Creates a reactor builder.
     *
     * @return new reactor builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the dispatcher.
     *
     * @return runtime activity dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Returns the clock.
     *
     * @return runtime wall and monotonic clock
     */
    public Clock clock() {
        return clock;
    }

    /**
     * Returns the directory.
     *
     * @return runtime registry and connection directory
     */
    public Directory directory() {
        return directory;
    }

    /**
     * Returns the observer.
     *
     * @return runtime event observer
     */
    public EventObserver observer() {
        return observer;
    }

    /**
     * Returns the unique runtime meter.
     *
     * @return meter shared by reactor-owned components
     */
    public FabricMeter meter() {
        return meter;
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
     * @param tag activity ownership tag passed to the dispatcher
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
        Throwable failure = null;
        try {
            scope.close();
        } catch (final Throwable e) {
            failure = e;
        }
        try {
            directory.close();
        } catch (final Throwable e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        try {
            dispatcher.close();
        } catch (final Throwable e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw new InternalException("Unable to close reactor", failure);
        }
    }

    /**
     * Validates non-null values.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
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
         * Clock candidate.
         */
        private Clock clock;

        /**
         * Observer candidate.
         */
        private EventObserver observer;

        /**
         * Dispatcher candidate.
         */
        private Dispatcher dispatcher;

        /**
         * Scope candidate.
         */
        private ResourceScope scope;

        /**
         * Optional compatibility directory.
         */
        private Directory directory;

        /**
         * Connection pool policy.
         */
        private PoolPolicy poolPolicy;

        /**
         * Generic policy options supplied by a context composition root.
         */
        private Options options = Options.empty();

        /**
         * Creates a runtime builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the dispatcher.
         *
         * @param dispatcher runtime dispatcher borrowed by the reactor
         * @return this builder
         */
        public Builder dispatcher(final Dispatcher dispatcher) {
            this.dispatcher = require(dispatcher, "Dispatcher");
            return this;
        }

        /**
         * Sets the clock.
         *
         * @param clock runtime clock borrowed by the reactor
         * @return this builder
         */
        public Builder clock(final Clock clock) {
            this.clock = require(clock, "Clock");
            return this;
        }

        /**
         * Sets the observer.
         *
         * @param observer runtime event observer
         * @return this builder
         */
        public Builder observer(final EventObserver observer) {
            this.observer = require(observer, "Observer");
            return this;
        }

        /**
         * Sets the resource scope.
         *
         * @param scope resource scope
         * @return this builder
         */
        public Builder scope(final ResourceScope scope) {
            this.scope = require(scope, "Scope");
            return this;
        }

        /**
         * Sets an existing directory for compatibility injection.
         *
         * @param directory directory borrowed by the reactor
         * @return this builder
         */
        public Builder directory(final Directory directory) {
            this.directory = require(directory, "Directory");
            return this;
        }

        /**
         * Sets the connection-pool policy used by the runtime-owned directory.
         *
         * @param poolPolicy connection-pool policy
         * @return this builder
         */
        public Builder poolPolicy(final PoolPolicy poolPolicy) {
            this.poolPolicy = require(poolPolicy, "Pool policy");
            return this;
        }

        /**
         * Sets generic policy options used when no explicit component policy is supplied.
         *
         * @param options immutable option snapshot
         * @return this builder
         */
        public Builder options(final Options options) {
            this.options = require(options, "Options");
            return this;
        }

        /**
         * Builds a reactor, creating defaults in dependency order and transferring ownership only after success.
         *
         * @return fully initialized reactor owning any collaborators created by this build
         */
        @Override
        public Reactor build() {
            final Clock resolvedClock = clock == null ? Clock.system() : clock;
            final EventObserver resolvedObserver = observer == null ? EventObserver.noop() : observer;
            final FabricMeter resolvedMeter = FabricMeter.create(resolvedClock);
            Dispatcher resolvedDispatcher = dispatcher;
            ResourceScope resolvedScope = scope;
            Directory resolvedDirectory = directory;
            final boolean createdDispatcher = resolvedDispatcher == null;
            final boolean createdScope = resolvedScope == null;
            try {
                if (createdDispatcher) {
                    resolvedDispatcher = Dispatcher.create(resolvedObserver);
                }
                if (createdScope) {
                    resolvedScope = ResourceScope.create();
                }
                if (resolvedDirectory == null) {
                    resolvedDirectory = Directory.create(
                            resolvedClock,
                            poolPolicy == null ? PoolPolicy.resolve(options) : poolPolicy,
                            resolvedMeter,
                            resolvedDispatcher);
                }
                return new Reactor(resolvedClock, resolvedObserver, resolvedMeter, resolvedDispatcher, resolvedScope,
                        resolvedDirectory);
            } catch (final RuntimeException | Error failure) {
                if (directory == null) {
                    closeCreated(resolvedDirectory, failure);
                }
                if (createdScope) {
                    closeCreated(resolvedScope, failure);
                }
                if (createdDispatcher) {
                    closeCreated(resolvedDispatcher, failure);
                }
                throw failure;
            }
        }

        /**
         * Closes one default resource created by this build and suppresses cleanup failure on the primary failure.
         *
         * @param resource resource created by this builder, or null
         * @param failure  primary build failure
         */
        private static void closeCreated(final AutoCloseable resource, final Throwable failure) {
            if (resource == null) {
                return;
            }
            try {
                resource.close();
            } catch (final Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
        }

    }

}
