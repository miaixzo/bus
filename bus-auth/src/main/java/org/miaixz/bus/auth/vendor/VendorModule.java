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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Freezes one exact set of built-in and externally contributed third-party platforms for runtime and management use.
 * <p>
 * The same immutable definition and adapter-binding inventories produce both the management directory and the Source
 * driver. A built module cannot accept later mutation, perform data loading, or execute authentication during startup.
 * </p>
 *
 * @author Kimi Liu
 */
public final class VendorModule {

    /**
     * Immutable management and compilation definition directory.
     */
    private final VendorDirectory vendorDirectory;

    /**
     * Immutable exact platform-variant adapter factory bindings.
     */
    private final AdapterBindings adapterBindings;

    /**
     * Creates an immutable Vendor module from a validated definition directory and adapter bindings.
     *
     * @param vendorDirectory complete Vendor definition directory
     * @param adapterBindings complete adapter factory bindings
     */
    private VendorModule(final VendorDirectory vendorDirectory, final AdapterBindings adapterBindings) {
        this.vendorDirectory = Assert.notNull(vendorDirectory, "Vendor module directory must not be null");
        this.adapterBindings = Assert.notNull(adapterBindings, "Vendor module adapter bindings must not be null");
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
     * @return immutable Vendor definition directory
     */
    public VendorDirectory directory() {
        return vendorDirectory;
    }

    /**
     * Creates the single Vendor compiler backed by the same frozen directory and adapter bindings.
     *
     * @return immutable Vendor Source compiler
     */
    public SourceDriver<VendorSettings> source() {
        return new VendorCompiler(vendorDirectory, adapterBindings);
    }

    /**
     * Builds one immutable Vendor module from explicit built-in and external platform drivers.
     *
     * @author Kimi Liu
     */
    public static final class Builder {

        /**
         * Collected Vendor definitions in deterministic contribution order.
         */
        private final List<VendorDefinition<?>> definitions = new ArrayList<>();

        /**
         * Collected exact platform-variant adapter bindings.
         */
        private final Map<AdapterBindings.Key, VendorAdapter.Factory<?>> adapterBindings = new LinkedHashMap<>();

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
        private Builder() {
            // No initialization required.
        }

        /**
         * Adds the complete built-in platform baseline exactly once.
         *
         * @return this builder
         * @throws ValidateException if the baseline was already added or the builder is frozen
         */
        public synchronized Builder builtins() {
            mutable();
            if (builtins) {
                throw new ValidateException("Built-in Vendor platforms have already been added");
            }
            builtins = true;
            definitions.addAll(VendorSuite.directory().definitions());
            mergeBindings(VendorSuite.bindings().bindings());
            return this;
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
            if (definitions.isEmpty()) {
                throw new ValidateException("Vendor module must contain at least one platform driver");
            }
            return new VendorModule(new VendorDirectory(definitions), new AdapterBindings(adapterBindings));
        }

        /**
         * Adds one type-safe driver through the only contained generic boundary.
         *
         * @param driver contributed platform driver
         * @param <S>    exact contributed settings type
         */
        private <S extends VendorSettings> void add(final VendorDriver<S> driver) {
            final VendorDefinition<S> vendorDefinition = driver.definition();
            for (VendorDefinition<?> existing : definitions) {
                if (existing.type().equals(vendorDefinition.type())) {
                    throw new ValidateException("Duplicate Vendor definition id: " + vendorDefinition.type().value());
                }
            }
            definitions.add(vendorDefinition);
            final Map<AdapterBindings.Key, VendorAdapter.Factory<?>> contributed = new LinkedHashMap<>();
            for (Map.Entry<Vendor.Variant, VendorAdapter.Factory<S>> entry : driver.factories().entrySet()) {
                contributed.put(new AdapterBindings.Key(vendorDefinition.type(), entry.getKey()), entry.getValue());
            }
            mergeBindings(contributed);
        }

        /**
         * Merges exact adapter bindings while rejecting platform-variant collisions.
         *
         * @param contributed immutable adapter bindings to merge
         */
        private void mergeBindings(final Map<AdapterBindings.Key, VendorAdapter.Factory<?>> contributed) {
            for (Map.Entry<AdapterBindings.Key, VendorAdapter.Factory<?>> entry : contributed.entrySet()) {
                if (adapterBindings.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
                    throw new ValidateException("Duplicate Vendor adapter factory: " + entry.getKey().vendor().value()
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
