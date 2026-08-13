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
package org.miaixz.bus.auth.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.Policy;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.protocol.Handler;
import org.miaixz.bus.auth.vendor.VendorConfiguration;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorErrors;
import org.miaixz.bus.auth.vendor.VendorProvider;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Unified authentication runtime backed by four explicit typed registries and the shared Bus lifecycle contract.
 *
 * @author Kimi Liu
 */
public final class AuthRuntime implements Engine {

    /**
     * Immutable-access vendor definition registry.
     */
    private final Registry<VendorDefinition> vendors;

    /**
     * Protocol-neutral provider registry.
     */
    private final Registry<Provider> providers;

    /**
     * Server protocol handler registry.
     */
    private final Registry<Handler<?, ?>> handlers;

    /**
     * Shared policy registry.
     */
    private final Registry<Policy> policies;

    /**
     * Current observable runtime lifecycle state.
     */
    private final AtomicReference<Lifecycle.State> state = new AtomicReference<>(Lifecycle.State.RUNNING);

    /**
     * Creates an active runtime owning the four supplied registries.
     *
     * @param vendors   vendor definitions
     * @param providers protocol-neutral providers
     * @param handlers  server protocol handlers
     * @param policies  authentication policies
     */
    AuthRuntime(final Registry<VendorDefinition> vendors, final Registry<Provider> providers,
            final Registry<Handler<?, ?>> handlers, final Registry<Policy> policies) {
        this.vendors = required(vendors, "Vendor registry");
        this.providers = required(providers, "Provider registry");
        this.handlers = required(handlers, "Handler registry");
        this.policies = required(policies, "Policy registry");
    }

    /**
     * @return builder preloaded with built-in vendor definitions
     */
    public static RuntimeBuilder builder() {
        return RuntimeBuilder.create();
    }

    /**
     * @return empty builder requiring explicit component registration
     */
    public static RuntimeBuilder emptyBuilder() {
        return RuntimeBuilder.empty();
    }

    /**
     * Validates one required registry.
     */
    private static <T> Registry<T> required(final Registry<T> registry, final String label) {
        return Assert.notNull(registry, () -> new ValidateException(label + " must not be null"));
    }

    /**
     * Closes a registry snapshot in reverse registration order.
     */
    private static void closeValues(final Registry<?> registry) {
        final List<Object> values = new ArrayList<>(
                registry.snapshot().values().stream().map(binding -> binding.value()).toList());
        Collections.reverse(values);
        for (final Object value : values) {
            if (value instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (final RuntimeException failure) {
                    throw failure;
                } catch (final Exception failure) {
                    throw new InternalException("Unable to close authentication component", failure);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<VendorDefinition> vendors() {
        requireRunning();
        return vendors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<Provider> providers() {
        requireRunning();
        return providers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<Handler<?, ?>> handlers() {
        requireRunning();
        return handlers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<Policy> policies() {
        requireRunning();
        return policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lifecycle.State state() {
        return state.get();
    }

    /**
     * Resolves one required provider and verifies its runtime type.
     *
     * @param id   stable provider identifier
     * @param type required provider type
     * @param <P>  provider type
     * @return registered provider
     */
    public <P extends Provider> P provider(final String id, final Class<P> type) {
        final Provider provider = providers().require(id);
        final Class<P> expected = Assert
                .notNull(type, () -> new ValidateException("Authentication provider type must not be null"));
        if (!expected.isInstance(provider)) {
            throw new ValidateException("Authentication provider has the wrong type: " + id);
        }
        return expected.cast(provider);
    }

    /**
     * Creates a third-party client from a registered vendor definition.
     *
     * @param name          vendor definition name
     * @param configuration explicit construction dependencies
     * @return configured vendor provider
     */
    public VendorProvider provider(final String name, final VendorConfiguration configuration) {
        Assert.notNull(configuration, () -> new ValidateException("Vendor configuration must not be null"));
        final VendorDefinition definition = vendors().get(name);
        if (definition == null || definition.factory() == null) {
            throw new AuthorizedException(VendorErrors._110000);
        }
        return definition.factory().create(configuration);
    }

    /**
     * Closes handlers, providers, policies, and vendor definitions in that fixed order, each in reverse registration
     * order. Non-closeable values require no lifecycle action.
     */
    @Override
    public void close() {
        if (!state.compareAndSet(Lifecycle.State.RUNNING, Lifecycle.State.CLOSING)) {
            return;
        }
        try {
            closeValues(handlers);
            closeValues(providers);
            closeValues(policies);
            closeValues(vendors);
            state.set(Lifecycle.State.CLOSED);
        } catch (final RuntimeException failure) {
            state.set(Lifecycle.State.FAILED);
            throw failure;
        }
    }

    /**
     * Rejects registry access after shutdown begins.
     */
    private void requireRunning() {
        if (state.get() != Lifecycle.State.RUNNING) {
            throw new StatefulException("Authentication runtime is not running");
        }
    }

}
