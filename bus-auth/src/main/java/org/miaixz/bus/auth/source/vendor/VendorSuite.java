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
package org.miaixz.bus.auth.source.vendor;

import java.util.*;

import org.miaixz.bus.auth.source.SourceDiscovery;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Registers Vendor connectors atomically and freezes their declared platform bindings into one module.
 * <p>
 * The suite depends only on the shared Vendor contracts and imports no concrete platform package. Each visible platform
 * is discovered through the Bus SPI loader and can therefore be added or removed without editing this class. Each
 * registration invokes the connector's connect callback against detached staging state and commits only after the
 * complete callback succeeds. The suite retains platform bindings, not connector instances, and performs no Source
 * loading, adapter construction, Roster access, platform API call, or runtime mutation.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorSuite implements VendorRegistry {

    /**
     * Complete platform bindings retained in deterministic registration order.
     */
    private final Map<Vendor.Id, VendorBinding<?>> bindings;

    /**
     * Platform key currently allowed to submit a binding during one connector callback.
     */
    private Vendor.Id active;

    /**
     * Binding emitted by the currently active connector callback.
     */
    private VendorBinding<?> emitted;

    /**
     * Whether this suite has frozen its platform registrations.
     */
    private boolean frozen;

    /**
     * Creates an empty mutable Vendor registration suite.
     */
    public VendorSuite() {
        this(new LinkedHashMap<>());
    }

    /**
     * Creates a mutable staging suite from detached binding state.
     *
     * @param bindings detached Vendor binding map
     */
    private VendorSuite(final Map<Vendor.Id, VendorBinding<?>> bindings) {
        this.bindings = bindings;
    }

    /**
     * Loads every visible Vendor connector from the unified Source SPI in stable platform order.
     *
     * @param selected selected platform keys, or {@code null} for every visible registration
     * @return immutable ordered connector list
     * @throws ValidateException if no connector is visible or a selected key is unknown
     */
    static List<VendorConnector> connectors(final Set<Vendor.Id> selected) {
        return SourceDiscovery.load().vendors(selected);
    }

    /**
     * Expands one exact typed binding into the immutable module indexes.
     *
     * @param binding         typed Vendor binding
     * @param manifests       mutable ordered manifest list
     * @param adapterBindings mutable exact adapter binding index
     * @param optionBindings  mutable exact Options binding index
     * @param <O>             exact immutable Vendor options type
     */
    private static <O extends VendorOptions<?>> void assemble(
            final VendorBinding<O> binding,
            final List<VendorManifest<?>> manifests,
            final Map<AdapterBindings.Key, AdapterBindings.Binding> adapterBindings,
            final Map<OptionsBindings.Key, OptionsBindings.Binding> optionBindings) {
        final VendorManifest<O> manifest = binding.manifest();
        manifests.add(manifest);
        for (VendorManifest.Variant variant : manifest.variants()) {
            final AdapterBindings.Key adapterKey = new AdapterBindings.Key(manifest.vendor(), variant.variant());
            if (adapterBindings.putIfAbsent(adapterKey, binding.binding(variant.variant())) != null) {
                throw new ValidateException("Duplicate Vendor adapter factory: " + manifest.vendor().value()
                        + Symbol.C_SLASH + variant.variant().value());
            }
            final OptionsBindings.Key optionKey = new OptionsBindings.Key(manifest.vendor(), variant.variant());
            if (optionBindings.putIfAbsent(optionKey, binding.optionsBinding(variant.variant())) != null) {
                throw new ValidateException("Duplicate Vendor Options factory: " + manifest.vendor().value()
                        + Symbol.C_SLASH + variant.variant().value());
            }
        }
    }

    /**
     * Binds one complete typed Vendor binding to the active registration.
     *
     * @param binding complete typed Vendor binding
     * @return this suite
     */
    @Override
    public synchronized VendorRegistry bind(final VendorBinding<?> binding) {
        mutable();
        if (active == null) {
            throw new ValidateException("Vendor bindings may be bound only during a Vendor connector callback");
        }
        if (emitted != null) {
            throw new ValidateException("Vendor connector must submit exactly one complete binding");
        }
        final VendorBinding<?> checked = Assert.notNull(binding, "Vendor binding must not be null");
        if (!active.equals(checked.key())) {
            throw new ValidateException("Vendor binding id does not match its connector key");
        }
        emitted = checked;
        return this;
    }

    /**
     * Builds and binds one complete typed Vendor binding from connector-local declarations.
     *
     * @param manifest       immutable platform manifest
     * @param optionsFactory Options factory shared by all manifest variants
     * @param adapters       exact adapter declarations
     * @param <O>            exact immutable Vendor options type
     * @return this suite
     */
    @Override
    public synchronized <O extends VendorOptions<?>> VendorRegistry bind(
            final VendorManifest<O> manifest,
            final VendorOptions.Factory<O> optionsFactory,
            final Collection<? extends Adapter<O>> adapters) {
        final VendorManifest<O> checkedManifest = Assert.notNull(manifest, "Vendor manifest must not be null");
        final VendorOptions.Factory<O> checkedOptions = Assert
                .notNull(optionsFactory, "Vendor Options factory must not be null");
        Assert.notNull(adapters, "Vendor adapter declarations must not be null");
        final Map<Vendor.Variant, VendorAdapter.Factory<O>> adapterFactories = new LinkedHashMap<>();
        for (Adapter<O> candidate : adapters) {
            final Adapter<O> adapter = Assert.notNull(candidate, "Vendor adapter declaration must not be null");
            for (Vendor.Variant variant : adapter.variants()) {
                checkedManifest.variant(variant);
                if (adapterFactories.putIfAbsent(variant, adapter.factory()) != null) {
                    throw new ValidateException("Duplicate Vendor adapter factory: " + checkedManifest.vendor().value()
                            + Symbol.C_SLASH + variant.value());
                }
            }
        }
        final Map<Vendor.Variant, VendorOptions.Factory<O>> optionFactories = new LinkedHashMap<>();
        for (VendorManifest.Variant variant : checkedManifest.variants()) {
            optionFactories.put(variant.variant(), checkedOptions);
        }
        return bind(new VendorBinding<>(checkedManifest, adapterFactories, optionFactories));
    }

    /**
     * Atomically registers one Vendor registration.
     *
     * @param connector Vendor connector to register
     * @return this suite
     */
    @Override
    public synchronized VendorRegistry register(final VendorConnector connector) {
        return registerAll(List.of(Assert.notNull(connector, "Vendor connector must not be null")));
    }

    /**
     * Atomically registers all Vendor registrations in iteration order.
     *
     * @param connectors Vendor connectors to register
     * @return this suite
     */
    @Override
    public synchronized VendorRegistry registerAll(final Collection<? extends VendorConnector> connectors) {
        mutable();
        Assert.notNull(connectors, "Vendor connector collection must not be null");
        final VendorSuite staged = copy();
        for (VendorConnector candidate : connectors) {
            staged.apply(Assert.notNull(candidate, "Vendor connector must not be null"));
        }
        replace(staged);
        return this;
    }

    /**
     * Atomically removes one complete Vendor registration.
     *
     * @param key Vendor platform key
     * @return this suite
     */
    @Override
    public synchronized VendorRegistry unregister(final Vendor.Id key) {
        return unregisterAll(List.of(Assert.notNull(key, "Vendor connector key must not be null")));
    }

    /**
     * Atomically removes all complete Vendor registrations owned by the supplied keys.
     *
     * @param keys Vendor platform keys
     * @return this suite
     */
    @Override
    public synchronized VendorRegistry unregisterAll(final Collection<? extends Vendor.Id> keys) {
        mutable();
        Assert.notNull(keys, "Vendor connector key collection must not be null");
        final VendorSuite staged = copy();
        final Set<Vendor.Id> requested = new HashSet<>();
        for (Vendor.Id candidate : keys) {
            final Vendor.Id key = Assert.notNull(candidate, "Vendor connector key must not be null");
            if (!requested.add(key)) {
                throw new ValidateException("Duplicate Vendor connector removal key: " + key.value());
            }
            if (staged.bindings.remove(key) == null) {
                throw new ValidateException("Vendor connector is not registered: " + key.value());
            }
        }
        replace(staged);
        return this;
    }

    /**
     * Reports whether a Vendor registration is currently registered.
     *
     * @param key Vendor platform key
     * @return {@code true} when registered
     */
    @Override
    public synchronized boolean contains(final Vendor.Id key) {
        return bindings.containsKey(Assert.notNull(key, "Vendor connector key must not be null"));
    }

    /**
     * Freezes all complete registrations into one immutable Vendor module.
     *
     * @return immutable Vendor module
     * @throws ValidateException if no platform has been registered or this suite is already frozen
     */
    public synchronized VendorModule freeze() {
        mutable();
        if (bindings.isEmpty()) {
            throw new ValidateException("Vendor module must contain at least one platform registration");
        }
        final List<VendorManifest<?>> manifests = new ArrayList<>(bindings.size());
        final Map<AdapterBindings.Key, AdapterBindings.Binding> adapterBindings = new LinkedHashMap<>();
        final Map<OptionsBindings.Key, OptionsBindings.Binding> optionBindings = new LinkedHashMap<>();
        for (VendorBinding<?> binding : bindings.values()) {
            assemble(binding, manifests, adapterBindings, optionBindings);
        }
        final VendorModule module = new VendorModule(new VendorLocator(manifests), new AdapterBindings(adapterBindings),
                new OptionsBindings(optionBindings));
        frozen = true;
        return module;
    }

    /**
     * Applies one connector to this staging suite.
     *
     * @param connector checked Vendor connector
     */
    private void apply(final VendorConnector connector) {
        final Vendor.Id key = Assert.notNull(connector.key(), "Vendor connector key must not be null");
        if (bindings.containsKey(key)) {
            throw new ValidateException("Duplicate Vendor connector: " + key.value());
        }
        active = key;
        emitted = null;
        try {
            connector.connect(this);
            if (emitted == null) {
                throw new ValidateException("Vendor connector must submit one complete binding: " + key.value());
            }
            bindings.put(key, emitted);
        } finally {
            active = null;
            emitted = null;
        }
    }

    /**
     * Creates detached mutable staging state for an atomic operation.
     *
     * @return detached suite copy
     */
    private VendorSuite copy() {
        return new VendorSuite(new LinkedHashMap<>(bindings));
    }

    /**
     * Commits detached staging state after a complete successful operation.
     *
     * @param staged successfully mutated staging suite
     */
    private void replace(final VendorSuite staged) {
        bindings.clear();
        bindings.putAll(staged.bindings);
    }

    /**
     * Rejects mutation after this suite has frozen its platform registrations.
     */
    private void mutable() {
        if (frozen) {
            throw new ValidateException("Vendor registry is already frozen");
        }
    }

}
