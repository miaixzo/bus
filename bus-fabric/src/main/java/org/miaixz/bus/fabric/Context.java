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

import org.miaixz.bus.core.lang.exception.InternalException;
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
public final class Context {

    /**
     * Shared reactor.
     */
    private final Reactor reactor;

    /**
     * Shared option snapshot.
     */
    private final Options options;

    /**
     * Shared directory.
     */
    private final Directory directory;

    /**
     * Shared DNS resolver.
     */
    private final DnsResolver resolver;

    /**
     * Shared lifecycle listener for network and protocol resources.
     */
    private final Listener<Object> listener;

    /**
     * Creates a validated context instance.
     *
     * @param reactor   reactor
     * @param options   option snapshot
     * @param directory directory
     * @param resolver  DNS resolver
     * @param listener  lifecycle listener
     */
    private Context(final Reactor reactor, final Options options, final Directory directory, final DnsResolver resolver,
            final Listener<Object> listener) {
        this.reactor = require(reactor, "Reactor");
        this.options = require(options, "Options");
        this.directory = require(directory, "Directory");
        this.resolver = require(resolver, "DNS resolver");
        this.resolver.observer(this.reactor.observer());
        this.listener = Wiring.safe(require(listener, "Lifecycle listener"), reactor.observer());
    }

    /**
     * Creates a default context with shared runtime collaborators.
     *
     * @return default context
     */
    public static Context create() {
        try {
            final Directory directory = Directory.create();
            final Reactor reactor = Reactor.builder().directory(directory).build();
            return builder().reactor(reactor).options(Options.empty()).directory(directory).build();
        } catch (final RuntimeException e) {
            if (e instanceof InternalException) {
                throw e;
            }
            throw new InternalException("Unable to create default context", e);
        }
    }

    /**
     * Creates a context builder initialized with default collaborators.
     *
     * @return context builder
     */
    public static Builder builder() {
        final Directory directory = Directory.create();
        final Reactor reactor = Reactor.builder().directory(directory).build();
        return new Builder(reactor, Options.empty(), directory, DnsResolver.system(), Wiring.noop());
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
     * Returns the shared directory.
     *
     * @return directory
     */
    public Directory directory() {
        return directory;
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
     * Creates a context with replacement options.
     *
     * @param options replacement options
     * @return copied context
     */
    public Context withOptions(final Options options) {
        return new Context(reactor, require(options, "Options"), directory, resolver, listener);
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
        private Options options;

        /**
         * Directory candidate.
         */
        private Directory directory;

        /**
         * DNS resolver candidate.
         */
        private DnsResolver resolver;

        /**
         * Lifecycle listener candidate.
         */
        private Listener<Object> listener;

        /**
         * Creates a builder with explicit defaults.
         *
         * @param reactor   default reactor
         * @param options   default options
         * @param directory default directory
         * @param resolver  default DNS resolver
         * @param listener  default listener
         */
        private Builder(final Reactor reactor, final Options options, final Directory directory,
                final DnsResolver resolver, final Listener<Object> listener) {
            this.reactor = require(reactor, "Reactor");
            this.options = require(options, "Options");
            this.directory = require(directory, "Directory");
            this.resolver = require(resolver, "DNS resolver");
            this.listener = require(listener, "Lifecycle listener");
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
            this.listener = require(listener, "Lifecycle listener");
            return this;
        }

        /**
         * Builds an immutable context.
         *
         * @return context
         */
        public Context build() {
            return new Context(reactor, options, directory, resolver, listener);
        }

    }

}
