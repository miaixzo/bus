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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.source.SourceDescriptor;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceModule;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Freezes one exact set of discovered and explicitly registered third-party platforms for runtime and management use.
 * <p>
 * The same immutable manifest and adapter-binding sets produce both the management locator and the Source driver. A
 * built module cannot accept later mutation, perform data loading, or execute authentication during startup.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorModule implements SourceModule {

    /**
     * Immutable management and compilation manifest locator.
     */
    private final VendorLocator vendorLocator;

    /**
     * Immutable exact platform-variant adapter factory bindings.
     */
    private final AdapterBindings adapterBindings;

    /**
     * Immutable exact platform-variant Options factory bindings.
     */
    private final OptionsBindings optionsBindings;

    /**
     * Single cached aggregate Vendor driver used by every variant descriptor.
     */
    private final VendorDriver driver;

    /**
     * Cached singleton driver list required by the Source module contract.
     */
    private final List<SourceDriver<?>> drivers;

    /**
     * Exact immutable descriptor list containing one entry per registered Vendor variant.
     */
    private final List<SourceDescriptor> descriptors;

    /**
     * Creates an immutable Vendor module from validated exact indexes.
     *
     * @param vendorLocator   complete Vendor manifest locator
     * @param adapterBindings complete adapter factory bindings
     * @param optionsBindings complete Options factory bindings
     */
    public VendorModule(final VendorLocator vendorLocator, final AdapterBindings adapterBindings,
            final OptionsBindings optionsBindings) {
        this.vendorLocator = Assert.notNull(vendorLocator, "Vendor module locator must not be null");
        this.adapterBindings = Assert.notNull(adapterBindings, "Vendor module adapter bindings must not be null");
        this.optionsBindings = Assert.notNull(optionsBindings, "Vendor module Options bindings must not be null");
        final Set<AdapterBindings.Key> expected = new HashSet<>();
        final Set<OptionsBindings.Key> expectedOptions = new HashSet<>();
        for (VendorManifest<?> manifest : vendorLocator.manifests()) {
            for (VendorManifest.Variant variant : manifest.variants()) {
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
        this.driver = new VendorDriver(vendorLocator, adapterBindings);
        this.drivers = List.of(driver);
        final List<SourceDescriptor> selections = new ArrayList<>(expected.size());
        for (VendorManifest<?> manifest : vendorLocator.manifests()) {
            for (VendorManifest.Variant variant : manifest.variants()) {
                selections.add(new VendorDescriptor(manifest, variant));
            }
        }
        this.descriptors = List.copyOf(selections);
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
     * Returns the exact immutable locator represented by this Vendor module.
     *
     * @return immutable Vendor manifest locator
     */
    public VendorLocator locator() {
        return vendorLocator;
    }

    /**
     * Returns the exact immutable adapter bindings retained by this module for package-level assembly verification.
     *
     * @return immutable adapter bindings
     */
    AdapterBindings adapters() {
        return adapterBindings;
    }

    /**
     * Returns the exact immutable Options bindings retained by this module for package-level assembly verification.
     *
     * @return immutable Options bindings
     */
    OptionsBindings options() {
        return optionsBindings;
    }

    /**
     * Returns the single cached aggregate Vendor driver.
     *
     * @return immutable singleton driver list
     */
    @Override
    public List<SourceDriver<?>> drivers() {
        return drivers;
    }

    /**
     * Returns one exact management selection for every registered Vendor variant.
     *
     * @return immutable Vendor descriptor list
     */
    @Override
    public List<SourceDescriptor> descriptors() {
        return descriptors;
    }

    /**
     * Creates the client-side configuration coordinator backed by this module's exact Options factories.
     *
     * @param writer project-owned recoverable credential storage port
     * @return immutable client-side Vendor configuration coordinator
     */
    public VendorConfigurer configurer(final VendorCredentialWriter writer) {
        return new VendorConfigurer(optionsBindings, writer);
    }

    /**
     * Collects visible SPI connectors and explicit bindings through the same build-scoped Vendor registry.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Shared registry used by discovered and explicitly supplied registrations.
         */
        private final VendorSuite suite;

        /**
         * Whether visible SPI connectors have already been registered.
         */
        private boolean builtins;

        /**
         * Whether the one-shot build process has begun.
         */
        private boolean built;

        /**
         * Creates an empty build-scoped Vendor module collector.
         */
        public Builder() {
            this.suite = new VendorSuite();
        }

        /**
         * Discovers and atomically registers every visible Vendor SPI connector exactly once.
         *
         * @return this builder
         * @throws ValidateException if discovery was already requested or the builder is frozen
         */
        public synchronized Builder builtins() {
            return builtins((Set<Vendor.Id>) null);
        }

        /**
         * Discovers all visible Vendor SPI connectors and atomically registers only selected platforms.
         *
         * @param vendors selected platform identifiers, or {@code null} for every visible registration
         * @return this builder
         */
        public synchronized Builder builtins(final Set<Vendor.Id> vendors) {
            mutable();
            if (builtins) {
                throw new ValidateException("Vendor SPI connectors have already been added");
            }
            suite.registerAll(VendorSuite.connectors(vendors));
            builtins = true;
            return this;
        }

        /**
         * Discovers all visible Vendor SPI connectors and registers only selected platforms.
         *
         * @param vendors selected platform identifiers
         * @return this builder
         */
        public synchronized Builder builtins(final Vendor.Id... vendors) {
            Assert.notNull(vendors, "Vendor selection must not be null");
            return builtins(Set.of(vendors));
        }

        /**
         * Registers one explicit complete platform connector through the shared registry.
         *
         * @param connector complete Vendor connector
         * @return this builder
         */
        public synchronized Builder connector(final VendorConnector connector) {
            mutable();
            suite.register(Assert.notNull(connector, "Vendor connector must not be null"));
            return this;
        }

        /**
         * Atomically registers explicit complete platform connectors through the shared registry.
         *
         * @param connectors complete Vendor connectors
         * @return this builder
         */
        public synchronized Builder connectors(final List<? extends VendorConnector> connectors) {
            mutable();
            suite.registerAll(Assert.notNull(connectors, "Vendor connectors must not be null"));
            return this;
        }

        /**
         * Adds one complete external platform binding through its programmatic Connector contract.
         *
         * @param binding immutable platform binding
         * @return this builder
         */
        public synchronized Builder binding(final VendorBinding<?> binding) {
            return connector(Assert.notNull(binding, "Vendor binding must not be null"));
        }

        /**
         * Atomically adds external platform bindings through their programmatic Connector contract.
         *
         * @param bindings immutable platform bindings
         * @return this builder
         */
        public synchronized Builder bindings(final List<VendorBinding<?>> bindings) {
            Assert.notNull(bindings, "Vendor bindings must not be null");
            return connectors(bindings);
        }

        /**
         * Freezes all registrations into one immutable Vendor module.
         *
         * @return frozen Vendor module
         * @throws ValidateException if the builder is reused or contains no platform
         */
        public synchronized VendorModule build() {
            mutable();
            final VendorModule module = suite.freeze();
            built = true;
            return module;
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
