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
package org.miaixz.bus.auth.vendor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.CredentialWriter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Freezes one exact set of built-in and externally contributed third-party platforms for runtime and management use.
 * <p>
 * The same immutable manifest and adapter-binding inventories produce both the management directory and the Source
 * driver. A built module cannot accept later mutation, perform data loading, or execute authentication during startup.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorModule {

    /**
     * Immutable management and compilation manifest directory.
     */
    private final VendorDirectory vendorDirectory;

    /**
     * Immutable exact platform-variant adapter factory bindings.
     */
    private final AdapterBindings adapterBindings;

    /**
     * Immutable exact platform-variant Options factory bindings.
     */
    private final OptionsBindings optionsBindings;

    /**
     * Creates an immutable Vendor module from a validated manifest directory and adapter bindings.
     *
     * @param vendorDirectory complete Vendor manifest directory
     * @param adapterBindings complete adapter factory bindings
     * @param optionsBindings complete Options factory bindings
     */
    public VendorModule(final VendorDirectory vendorDirectory, final AdapterBindings adapterBindings,
            final OptionsBindings optionsBindings) {
        this.vendorDirectory = Assert.notNull(vendorDirectory, "Vendor module directory must not be null");
        this.adapterBindings = Assert.notNull(adapterBindings, "Vendor module adapter bindings must not be null");
        this.optionsBindings = Assert.notNull(optionsBindings, "Vendor module Options bindings must not be null");
        final Set<AdapterBindings.Key> expected = new HashSet<>();
        final Set<OptionsBindings.Key> expectedOptions = new HashSet<>();
        for (VariantManifest<?> manifest : vendorDirectory.manifests()) {
            for (VariantManifest.Variant variant : manifest.variants()) {
                expected.add(new AdapterBindings.Key(manifest.vendor(), variant.variant()));
                expectedOptions.add(new OptionsBindings.Key(manifest.vendor(), variant.variant()));
            }
        }
        if (!expected.equals(adapterBindings.bindings().keySet())) {
            throw new ValidateException("Vendor manifests and adapter bindings must have exact variant coverage");
        }
        if (!expectedOptions.equals(optionsBindings.keys())) {
            throw new ValidateException("Vendor manifests and Options bindings must have exact variant coverage");
        }
    }

    /**
     * Creates an empty one-shot module builder.
     *
     * @return mutable build-scoped Vendor module builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the exact immutable directory represented by this Vendor module.
     *
     * @return immutable Vendor manifest directory
     */
    public VendorDirectory directory() {
        return vendorDirectory;
    }

    /**
     * Creates the single Vendor compiler backed by the same frozen directory and adapter bindings.
     *
     * @return immutable Vendor Source compiler
     */
    public SourceDriver<VendorOptions<?>> source() {
        return new VendorCompiler(vendorDirectory, adapterBindings);
    }

    /**
     * Creates the client-side configuration coordinator backed by this module's exact Options factories.
     *
     * @param writer project-owned recoverable credential storage port
     * @return immutable client-side Vendor configuration coordinator
     */
    public VendorConfigurer configurer(final CredentialWriter writer) {
        return new VendorConfigurer(optionsBindings, writer);
    }

    /**
     * Builds one immutable Vendor module from explicit built-in and external platform drivers.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Collected Vendor manifests in deterministic contribution order.
         */
        private final List<VariantManifest<?>> manifests = new ArrayList<>();

        /**
         * Collected exact platform-variant adapter bindings.
         */
        private final Map<AdapterBindings.Key, AdapterBindings.Binding> adapterBindings = new LinkedHashMap<>();

        /**
         * Collected exact platform-variant Options bindings.
         */
        private final Map<OptionsBindings.Key, OptionsBindings.Binding> optionsBindings = new LinkedHashMap<>();

        /**
         * Whether the built-in platform baseline has already been contributed.
         */
        private boolean builtins;

        /**
         * Whether the one-shot build process has begun.
         */
        private boolean built;

        /**
         * Creates an empty build-scoped collector.
         */
        public Builder() {
            // No initialization required.
        }

        /**
         * Adds the complete built-in platform baseline exactly once.
         *
         * @return this builder
         * @throws ValidateException if the baseline was already added or the builder is frozen
         */
        public synchronized Builder builtins() {
            return builtins((Set<Vendor.Id>) null);
        }

        /**
         * Adds only the selected built-in platforms.
         *
         * @param vendors selected platform identifiers
         * @return this builder
         */
        public synchronized Builder builtins(final Set<Vendor.Id> vendors) {
            mutable();
            if (builtins) {
                throw new ValidateException("Built-in Vendor platforms have already been added");
            }
            builtins = true;
            final VendorDirectory directory = VendorSuite.directory();
            final Set<Vendor.Id> selected = vendors == null ? null : Set.copyOf(vendors);
            final Set<Vendor.Id> unresolved = selected == null ? Set.of() : new HashSet<>(selected);
            for (VariantManifest<?> manifest : directory.manifests()) {
                if (selected == null || selected.contains(manifest.vendor())) {
                    manifests.add(manifest);
                    if (selected != null) {
                        unresolved.remove(manifest.vendor());
                    }
                }
            }
            if (!unresolved.isEmpty()) {
                throw new ValidateException("Unknown built-in Vendor platform selection");
            }
            final Map<AdapterBindings.Key, AdapterBindings.Binding> selectedBindings = new LinkedHashMap<>();
            VendorSuite.bindings().bindings().forEach((key, binding) -> {
                if (selected == null || selected.contains(key.vendor())) {
                    selectedBindings.put(key, binding);
                }
            });
            mergeBindings(selectedBindings);
            final Map<OptionsBindings.Key, OptionsBindings.Binding> selectedOptions = new LinkedHashMap<>();
            VendorSuite.options(directory).values().forEach((key, binding) -> {
                if (selected == null || selected.contains(key.vendor())) {
                    selectedOptions.put(key, binding);
                }
            });
            mergeOptions(selectedOptions);
            return this;
        }

        /**
         * Adds only the selected built-in platforms.
         *
         * @param vendors selected platform identifiers
         * @return this builder
         */
        public synchronized Builder builtins(final Vendor.Id... vendors) {
            Assert.notNull(vendors, "Built-in Vendor selection must not be null");
            return builtins(Set.of(vendors));
        }

        /**
         * Adds one complete external platform driver.
         *
         * @param driver immutable platform driver
         * @return this builder
         * @throws ValidateException if a platform or variant factory conflicts with an earlier contribution
         */
        public synchronized Builder driver(final VendorDriver<?> driver) {
            mutable();
            add(Assert.notNull(driver, "Vendor driver must not be null"));
            return this;
        }

        /**
         * Adds external platform drivers in caller-provided deterministic order.
         *
         * @param drivers immutable driver list
         * @return this builder
         */
        public synchronized Builder drivers(final List<VendorDriver<?>> drivers) {
            mutable();
            Assert.notNull(drivers, "Vendor drivers must not be null");
            for (VendorDriver<?> driver : drivers) {
                add(Assert.notNull(driver, "Vendor driver must not be null"));
            }
            return this;
        }

        /**
         * Freezes all contributions into one immutable Vendor module.
         *
         * @return frozen Vendor module
         * @throws ValidateException if the builder is reused or contains no platform
         */
        public synchronized VendorModule build() {
            mutable();
            built = true;
            if (manifests.isEmpty()) {
                throw new ValidateException("Vendor module must contain at least one platform driver");
            }
            return new VendorModule(new VendorDirectory(manifests), new AdapterBindings(adapterBindings),
                    new OptionsBindings(optionsBindings));
        }

        /**
         * Adds one type-safe driver through the only contained generic boundary.
         *
         * @param driver contributed platform driver
         * @param <S>    exact contributed options type
         */
        private <S extends VendorOptions<?>> void add(final VendorDriver<S> driver) {
            final VariantManifest<S> manifest = driver.manifest();
            for (VariantManifest<?> existing : manifests) {
                if (existing.vendor().equals(manifest.vendor())) {
                    throw new ValidateException("Duplicate Vendor manifest id: " + manifest.vendor().value());
                }
            }
            manifests.add(manifest);
            final Map<AdapterBindings.Key, AdapterBindings.Binding> contributed = new LinkedHashMap<>();
            for (Map.Entry<Vendor.Variant, VendorAdapter.Factory<S>> entry : driver.factories().entrySet()) {
                contributed.put(
                        new AdapterBindings.Key(manifest.vendor(), entry.getKey()),
                        driver.binding(entry.getKey()));
            }
            mergeBindings(contributed);
            final Map<OptionsBindings.Key, OptionsBindings.Binding> contributedOptions = new LinkedHashMap<>();
            for (Map.Entry<Vendor.Variant, VendorOptions.Factory<S>> entry : driver.optionFactories().entrySet()) {
                contributedOptions.put(
                        new OptionsBindings.Key(manifest.vendor(), entry.getKey()),
                        driver.optionsBinding(entry.getKey()));
            }
            mergeOptions(contributedOptions);
        }

        /**
         * Merges exact adapter bindings while rejecting platform-variant collisions.
         *
         * @param contributed immutable adapter bindings to merge
         */
        private void mergeBindings(final Map<AdapterBindings.Key, AdapterBindings.Binding> contributed) {
            for (Map.Entry<AdapterBindings.Key, AdapterBindings.Binding> entry : contributed.entrySet()) {
                if (adapterBindings.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
                    throw new ValidateException("Duplicate Vendor adapter factory: " + entry.getKey().vendor().value()
                            + Symbol.C_SLASH + entry.getKey().variant().value());
                }
            }
        }

        /**
         * Merges exact Options bindings while rejecting platform-variant collisions.
         *
         * @param contributed immutable Options bindings to merge
         */
        private void mergeOptions(final Map<OptionsBindings.Key, OptionsBindings.Binding> contributed) {
            for (Map.Entry<OptionsBindings.Key, OptionsBindings.Binding> entry : contributed.entrySet()) {
                if (optionsBindings.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
                    throw new ValidateException("Duplicate Vendor Options factory: " + entry.getKey().vendor().value()
                            + Symbol.C_SLASH + entry.getKey().variant().value());
                }
            }
        }

        /**
         * Rejects mutation after the one-shot build process has begun.
         */
        private void mutable() {
            if (built) {
                throw new ValidateException("Vendor module builder is already frozen");
            }
        }

    }

}
