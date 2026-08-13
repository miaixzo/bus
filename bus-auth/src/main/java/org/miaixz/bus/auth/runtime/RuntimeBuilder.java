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

import org.miaixz.bus.auth.Policy;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.protocol.Handler;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.registry.Binding;

/**
 * Single-use builder registering typed providers, handlers, and policies for one authentication runtime.
 *
 * @author Kimi Liu
 */
public final class RuntimeBuilder implements org.miaixz.bus.core.Builder<AuthRuntime> {

    /**
     * Built-in vendor definitions used only by vendor client creation.
     */
    private final Registry<VendorDefinition> vendors = Registry.create();

    /**
     * Registered protocol-neutral providers.
     */
    private final Registry<Provider> providers = Registry.create();

    /**
     * Registered server protocol handlers.
     */
    private final Registry<Handler<?, ?>> handlers = Registry.create();

    /**
     * Registered authentication policies.
     */
    private final Registry<Policy> policies = Registry.create();

    /**
     * Whether ownership has already transferred to a runtime.
     */
    private boolean built;

    /**
     * Creates one builder and optionally installs built-in vendor definitions.
     */
    private RuntimeBuilder(final boolean defaults) {
        if (defaults) {
            for (final BuiltinVendors vendor : BuiltinVendors.values()) {
                vendors.put(Binding.of(vendor.descriptor().id(), vendor));
            }
        }
    }

    /**
     * @return builder preloaded with built-in vendor definitions
     */
    public static RuntimeBuilder create() {
        return new RuntimeBuilder(true);
    }

    /**
     * @return empty builder
     */
    public static RuntimeBuilder empty() {
        return new RuntimeBuilder(false);
    }

    /**
     * Registers or replaces a provider by descriptor identifier.
     */
    public RuntimeBuilder provider(final Provider provider) {
        ensureMutable();
        final Provider checked = Assert
                .notNull(provider, () -> new ValidateException("Authentication provider must not be null"));
        providers.put(Binding.of(checked.descriptor().id(), checked));
        return this;
    }

    /**
     * Registers or replaces a protocol handler by descriptor identifier.
     */
    public RuntimeBuilder handler(final Handler<?, ?> handler) {
        ensureMutable();
        final Handler<?, ?> checked = Assert
                .notNull(handler, () -> new ValidateException("Protocol handler must not be null"));
        handlers.put(Binding.of(checked.descriptor().id(), checked));
        return this;
    }

    /**
     * Registers or replaces a policy under an explicit stable name.
     */
    public RuntimeBuilder policy(final String name, final Policy policy) {
        ensureMutable();
        policies.put(
                Binding.of(
                        name,
                        Assert.notNull(policy, () -> new ValidateException("Authentication policy must not be null"))));
        return this;
    }

    /**
     * Transfers the four registries to one active runtime.
     */
    @Override
    public AuthRuntime build() {
        ensureMutable();
        built = true;
        return new AuthRuntime(vendors, providers, handlers, policies);
    }

    /**
     * Rejects mutation after ownership transfer.
     */
    private void ensureMutable() {
        if (built) {
            throw new StatefulException("Authentication runtime builder has already built its runtime");
        }
    }

}
