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

import static org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.runtime.Reactor;

/**
 * Immutable fabric context shared by protocol builders and runtime objects.
 *
 * @author Kimi Liu
 */
public class Context implements AutoCloseable {

    /**
     * Reactor borrowed from the shared runtime lease.
     */
    private final Reactor reactor;

    /**
     * Shared reactor lease and reference count for Context views.
     */
    private final RuntimeLease runtime;

    /**
     * Immutable option snapshot inherited by protocol builders.
     */
    private final Options options;

    /**
     * Shared lifecycle listener for network and protocol resources.
     */
    private final Listener<Object> listener;

    /**
     * Shared protocol-neutral message filter.
     */
    private final Filter filter;

    /**
     * Per-view flag ensuring this context releases its runtime lease once.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a validated context after all builder resources have been resolved.
     *
     * @param runtime  shared reactor lease owned by this context
     * @param options  immutable option snapshot
     * @param listener lifecycle listener, or {@code null} when disabled
     * @param filter   protocol-neutral message filter, or {@code null} when disabled
     */
    public Context(final RuntimeLease runtime, final Options options, final Listener<Object> listener,
            final Filter filter) {
        this.runtime = require(runtime, "Runtime lease");
        this.reactor = runtime.reactor;
        this.options = require(options, "Options");
        this.listener = listener;
        this.filter = filter;
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a default context with shared runtime collaborators.
     *
     * @return context with default options, a newly owned reactor, and the system DNS resolver
     */
    public static Context create() {
        return builder().build();
    }

    /**
     * Creates an inert context builder without allocating runtime resources.
     *
     * @return builder containing defaults that do not yet own runtime resources
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the shared reactor.
     *
     * @return reactor shared by this context and its derived views
     */
    public Reactor reactor() {
        return reactor;
    }

    /**
     * Returns the shared options.
     *
     * @return immutable option snapshot used by this context
     */
    public Options options() {
        return options;
    }

    /**
     * Returns the reactor clock shared by all context operations.
     *
     * @return clock supplied by the shared reactor
     */
    public Clock clock() {
        return reactor.clock();
    }

    /**
     * Returns the shared lifecycle listener.
     *
     * @return shared lifecycle listener, or {@code null} when disabled
     */
    public Listener<Object> listener() {
        return listener;
    }

    /**
     * Returns the shared protocol-neutral filter.
     *
     * @return shared filter, or {@code null} when disabled
     */
    public Filter filter() {
        return filter;
    }

    /**
     * Returns a context view sharing runtime services with a replacement filter.
     *
     * @param filter replacement filter, or {@code null} to disable filtering in the new view
     * @return new context view sharing the runtime, options, resolver, and listener
     * @throws ValidateException if this context's runtime has already been fully released
     */
    public Context withFilter(final Filter filter) {
        return new Context(runtime.retain(), options, listener, filter);
    }

    /**
     * Releases this context view's runtime lease once, closing the reactor only when the final view is released.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            runtime.release();
        }
    }

    /**
     * Validates non-null values.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
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
     */
    public static class Builder {

        /**
         * Reactor transferred to the context, or {@code null} to create one during {@link #build()}.
         */
        private Reactor reactor;

        /**
         * Options candidate.
         */
        private Options options = Options.empty();

        /**
         * Optional lifecycle listener inherited by the context.
         */
        private Listener<Object> listener;

        /**
         * Optional protocol-neutral filter inherited by the context.
         */
        private Filter filter;

        /**
         * Creates an inert builder without allocating a reactor or resolver.
         */
        public Builder() {
            // No initialization required.
        }

        /**
         * Sets the reactor whose ownership is transferred to the built context.
         *
         * @param reactor reactor to share across the context and its derived views
         * @return this builder
         * @throws ValidateException if {@code reactor} is {@code null}
         */
        public Builder reactor(final Reactor reactor) {
            this.reactor = require(reactor, "Reactor");
            return this;
        }

        /**
         * Adds a policy through the protocol-neutral policy contract.
         *
         * @param policy policy to add to the immutable option snapshot
         * @return this builder
         * @throws ValidateException if {@code policy} is {@code null}
         */
        public Builder policy(final Policy policy) {
            this.options = require(policy, "Policy").from(options);
            return this;
        }

        /**
         * Replaces the immutable option snapshot inherited by the built context.
         *
         * @param options immutable option snapshot
         * @return this builder
         * @throws ValidateException if {@code options} is {@code null}
         */
        public Builder options(final Options options) {
            this.options = require(options, "Options");
            return this;
        }

        /**
         * Sets the shared timeout value.
         *
         * @param timeout immutable timeout value
         * @return this builder
         * @throws ValidateException if {@code timeout} is {@code null}
         */
        public Builder timeout(final Timeout timeout) {
            this.options = options.with(OPTION_TIMEOUT, require(timeout, "Timeout"));
            return this;
        }

        /**
         * Sets the shared lifecycle listener.
         *
         * @param listener lifecycle listener, or {@code null} to disable lifecycle notifications
         * @return this builder
         */
        public Builder listener(final Listener<Object> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets the shared protocol-neutral filter.
         *
         * @param filter protocol-neutral filter, or {@code null} to disable filtering
         * @return this builder
         */
        public Builder filter(final Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds an immutable context and transfers ownership of its final reactor only after success.
         *
         * @return immutable context owning either the supplied reactor or a newly created reactor
         */
        public Context build() {
            Reactor resolvedReactor = reactor;
            final boolean createdReactor = resolvedReactor == null;
            if (createdReactor) {
                resolvedReactor = Reactor.builder().options(options).build();
            }
            try {
                return new Context(new RuntimeLease(resolvedReactor), options, listener, filter);
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

    /**
     * Reference-counted ownership shared by context views that borrow the same reactor.
     * <p>
     * The final view to release the lease closes the reactor. Retaining a lease after its reference count reaches zero
     * is rejected so a closed runtime cannot be resurrected.
     * </p>
     */
    private static final class RuntimeLease {

        /**
         * Reactor closed when the final context view releases this lease.
         */
        private final Reactor reactor;

        /**
         * Number of context views that currently own the reactor.
         */
        private final AtomicInteger references = new AtomicInteger(1);

        /**
         * Creates the initial lease for a reactor.
         *
         * @param reactor reactor owned collectively by all views retaining this lease
         */
        private RuntimeLease(final Reactor reactor) {
            this.reactor = require(reactor, "Reactor");
        }

        /**
         * Adds one context view to the shared ownership count.
         *
         * @return this lease
         * @throws ValidateException if the reactor has already been released
         */
        private RuntimeLease retain() {
            while (true) {
                final int current = references.get();
                if (current == 0) {
                    throw new ValidateException("Context runtime is closed");
                }
                if (references.compareAndSet(current, current + 1)) {
                    return this;
                }
            }
        }

        /**
         * Releases one context view and closes the reactor when the count reaches zero.
         *
         * @throws IllegalStateException if releases outnumber successful retains
         */
        private void release() {
            final int remaining = references.decrementAndGet();
            if (remaining < 0) {
                throw new IllegalStateException("Context runtime reference underflow");
            }
            if (remaining == 0) {
                reactor.close();
            }
        }
    }

}
