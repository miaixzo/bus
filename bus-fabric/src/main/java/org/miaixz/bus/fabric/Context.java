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
package org.miaixz.bus.fabric;

import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.registry.Directory;
import org.miaixz.bus.fabric.runtime.Reactor;

/**
 * Immutable fabric context shared by protocol builders and runtime objects.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Context implements AutoCloseable {

    /**
     * Shared reactor.
     */
    private final Reactor reactor;

    /**
     * Shared option snapshot.
     */
    private final Options options;

    /**
     * Shared DNS resolver.
     */
    private final DnsResolver resolver;

    /**
     * Shared lifecycle listener for network and protocol resources.
     */
    private final Listener<Object> listener;

    /**
     * Shared protocol-neutral message filter.
     */
    private final Filter filter;

    /**
     * Closed state guarding reactor ownership.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a validated context after all builder resources have been resolved.
     *
     * @param reactor  owned reactor
     * @param options  option snapshot
     * @param resolver context-local DNS resolver
     * @param listener lifecycle listener
     * @param filter   shared message filter
     */
    private Context(final Reactor reactor, final Options options, final DnsResolver resolver,
            final Listener<Object> listener, final Filter filter) {
        this.reactor = require(reactor, "Reactor");
        this.options = require(options, "Options");
        this.resolver = require(resolver, "DNS resolver");
        this.listener = listener;
        this.filter = filter;
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a default context with shared runtime collaborators.
     *
     * @return default context
     */
    public static Context create() {
        return builder().build();
    }

    /**
     * Creates an inert context builder without allocating runtime resources.
     *
     * @return context builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the shared reactor.
     *
     * @return reactor
     */
    public Reactor reactor() {
        return reactor;
    }

    /**
     * Returns the shared options.
     *
     * @return options
     */
    public Options options() {
        return options;
    }

    /**
     * Returns the reactor clock shared by all context operations.
     *
     * @return runtime clock
     */
    public Clock clock() {
        return reactor.clock();
    }

    /**
     * Returns the shared directory.
     *
     * @return directory
     */
    public Directory directory() {
        return reactor.directory();
    }

    /**
     * Returns the shared DNS resolver.
     *
     * @return DNS resolver
     */
    public DnsResolver resolver() {
        return resolver;
    }

    /**
     * Returns the shared lifecycle listener.
     *
     * @return lifecycle listener
     */
    public Listener<Object> listener() {
        return listener;
    }

    /**
     * Returns the shared protocol-neutral filter.
     *
     * @return shared filter, or null when disabled
     */
    public Filter filter() {
        return filter;
    }

    /**
     * Returns a context view sharing runtime services with a replacement filter.
     *
     * @param filter replacement filter, or null
     * @return context view
     */
    public Context withFilter(final Filter filter) {
        return new Context(reactor, options, resolver, listener, filter);
    }

    /**
     * Closes the owned reactor exactly once.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            reactor.close();
        }
    }

    /**
     * Validates non-null values.
     *
     * @param value validated value
     * @param name  value name
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Builder for immutable contexts.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Reactor candidate.
         */
        private Reactor reactor;

        /**
         * Options candidate.
         */
        private Options options = Options.empty();

        /**
         * DNS resolver candidate.
         */
        private DnsResolver resolver;

        /**
         * Lifecycle listener candidate.
         */
        private Listener<Object> listener;

        /**
         * Shared filter candidate.
         */
        private Filter filter;

        /**
         * Creates an inert builder without allocating a reactor or resolver.
         */
        private Builder() {
            // Defaults without resource allocation are declared on builder fields.
        }

        /**
         * Sets the reactor.
         *
         * @param reactor reactor
         * @return this builder
         */
        public Builder reactor(final Reactor reactor) {
            this.reactor = require(reactor, "Reactor");
            return this;
        }

        /**
         * Sets the options.
         *
         * @param options options
         * @return this builder
         */
        public Builder options(final Options options) {
            this.options = require(options, "Options");
            return this;
        }

        /**
         * Sets the DNS resolver.
         *
         * @param resolver DNS resolver
         * @return this builder
         */
        public Builder resolver(final DnsResolver resolver) {
            this.resolver = require(resolver, "DNS resolver");
            return this;
        }

        /**
         * Sets the shared lifecycle listener.
         *
         * @param listener listener
         * @return this builder
         */
        public Builder listener(final Listener<Object> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets the shared protocol-neutral filter.
         *
         * @param filter filter
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds an immutable context and transfers ownership of its final reactor only after success.
         *
         * @return context
         */
        public Context build() {
            Reactor resolvedReactor = reactor;
            final boolean createdReactor = resolvedReactor == null;
            if (createdReactor) {
                resolvedReactor = Reactor.create();
            }
            try {
                final DnsResolver resolvedResolver = resolver == null ? DnsResolver.system() : resolver;
                final DnsResolver localResolver = resolvedResolver.withObserver(resolvedReactor.observer());
                return new Context(resolvedReactor, options, localResolver, listener, filter);
            } catch (final RuntimeException | Error failure) {
                if (createdReactor) {
                    try {
                        resolvedReactor.close();
                    } catch (final RuntimeException closeFailure) {
                        failure.addSuppressed(closeFailure);
                    }
                }
                throw failure;
            }
        }

    }

}
