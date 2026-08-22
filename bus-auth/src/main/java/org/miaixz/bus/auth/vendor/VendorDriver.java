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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Binds one externally contributed {@link VariantManifest} to exact adapter and Options factories for every declared
 * variant.
 * <p>
 * A driver is immutable assembly input. It validates complete manifest-to-factory coverage but does not register
 * itself, load or persist Source data, construct adapters, or execute authentication. External projects own
 * contribution and Source loading; {@link VendorModule} owns freezing the resulting inventory.
 * </p>
 *
 * @param <O> exact immutable options type accepted by the contributed manifest
 * @author Kimi Liu
 */
public class VendorDriver<O extends VendorOptions<?>> {

    /**
     * Immutable contributed Vendor manifest.
     */
    private final VariantManifest<O> manifest;

    /**
     * Immutable factory map covering every declared platform variant.
     */
    private final Map<Vendor.Variant, VendorAdapter.Factory<O>> adapterFactories;

    /**
     * Immutable Options factory map covering every declared platform variant.
     */
    private final Map<Vendor.Variant, VendorOptions.Factory<O>> optionFactories;

    /**
     * Creates and validates one complete immutable driver.
     *
     * @param manifest         contributed platform manifest
     * @param adapterFactories exact adapter factory map covering every declared variant
     * @param optionFactories  exact Options factory map covering every declared variant
     * @throws IllegalArgumentException if an argument or map member is null
     * @throws ValidateException        if a variant is missing, duplicated, or not declared by the manifest
     */
    public VendorDriver(final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorAdapter.Factory<O>> adapterFactories,
            final Map<Vendor.Variant, VendorOptions.Factory<O>> optionFactories) {
        this.manifest = Assert.notNull(manifest, "Vendor driver manifest must not be null");
        this.adapterFactories = adapters(manifest, adapterFactories);
        this.optionFactories = options(manifest, optionFactories);
        if (!this.adapterFactories.keySet().equals(this.optionFactories.keySet())) {
            throw new ValidateException("Vendor adapter and Options factories must have exact variant coverage");
        }
    }

    /**
     * Validates and freezes exact adapter factory coverage.
     *
     * @param <O>       concrete immutable Vendor options type
     * @param manifest  contributed platform manifest
     * @param factories candidate adapter factories keyed by exact variant
     * @return immutable exact adapter factory map
     */
    private static <O extends VendorOptions<?>> Map<Vendor.Variant, VendorAdapter.Factory<O>> adapters(
            final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorAdapter.Factory<O>> factories) {
        Assert.notNull(factories, "Vendor driver adapter factories must not be null");
        final Map<Vendor.Variant, VendorAdapter.Factory<O>> copy = new LinkedHashMap<>(factories.size());
        for (Map.Entry<Vendor.Variant, VendorAdapter.Factory<O>> entry : factories.entrySet()) {
            final Vendor.Variant variant = Assert.notNull(entry.getKey(), "Vendor driver variant must not be null");
            final VendorAdapter.Factory<O> factory = Assert
                    .notNull(entry.getValue(), "Vendor driver adapter factory must not be null");
            manifest.variant(variant);
            if (copy.putIfAbsent(variant, factory) != null) {
                throw new ValidateException("Duplicate Vendor adapter factory variant: " + variant.value());
            }
        }
        for (VariantManifest.Variant variant : manifest.variants()) {
            if (!copy.containsKey(variant.variant())) {
                throw new ValidateException(
                        "Vendor adapter factory is missing for variant: " + variant.variant().value());
            }
        }
        if (copy.size() != manifest.variants().size()) {
            throw new ValidateException("Vendor adapter factories must exactly cover the defined variants");
        }
        return Map.copyOf(copy);
    }

    /**
     * Validates and freezes exact Options factory coverage.
     *
     * @param <O>       concrete immutable Vendor options type
     * @param manifest  contributed platform manifest
     * @param factories candidate Options factories keyed by exact variant
     * @return immutable exact Options factory map
     */
    private static <O extends VendorOptions<?>> Map<Vendor.Variant, VendorOptions.Factory<O>> options(
            final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorOptions.Factory<O>> factories) {
        Assert.notNull(factories, "Vendor driver Options factories must not be null");
        final Map<Vendor.Variant, VendorOptions.Factory<O>> copy = new LinkedHashMap<>(factories.size());
        for (Map.Entry<Vendor.Variant, VendorOptions.Factory<O>> entry : factories.entrySet()) {
            final Vendor.Variant variant = Assert.notNull(entry.getKey(), "Vendor driver variant must not be null");
            final VendorOptions.Factory<O> factory = Assert
                    .notNull(entry.getValue(), "Vendor driver Options factory must not be null");
            manifest.variant(variant);
            if (copy.putIfAbsent(variant, factory) != null) {
                throw new ValidateException("Duplicate Vendor Options factory variant: " + variant.value());
            }
        }
        for (VariantManifest.Variant variant : manifest.variants()) {
            if (!copy.containsKey(variant.variant())) {
                throw new ValidateException(
                        "Vendor Options factory is missing for variant: " + variant.variant().value());
            }
        }
        if (copy.size() != manifest.variants().size()) {
            throw new ValidateException("Vendor Options factories must exactly cover the defined variants");
        }
        return Map.copyOf(copy);
    }

    /**
     * Creates a driver with explicit per-variant platform execution factories.
     *
     * @param manifest         contributed platform manifest
     * @param adapterFactories exact adapter factory map covering every declared variant
     * @param optionFactories  exact Options factory map covering every declared variant
     * @param <O>              exact immutable options type
     * @return complete custom driver
     */
    public static <O extends VendorOptions<?>> VendorDriver<O> of(
            final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorAdapter.Factory<O>> adapterFactories,
            final Map<Vendor.Variant, VendorOptions.Factory<O>> optionFactories) {
        return new VendorDriver<>(manifest, adapterFactories, optionFactories);
    }

    /**
     * Returns the contributed immutable Vendor manifest.
     *
     * @return Vendor manifest
     */
    public VariantManifest<O> manifest() {
        return manifest;
    }

    /**
     * Creates the checked adapter binding for one declared variant.
     *
     * @param variant declared Vendor variant
     * @return checked adapter binding
     */
    AdapterBindings.Binding binding(final Vendor.Variant variant) {
        final VendorAdapter.Factory<O> factory = adapterFactories
                .get(Assert.notNull(variant, "Vendor driver variant must not be null"));
        return AdapterBindings.binding(manifest, Assert.notNull(factory, "Vendor driver factory must not be null"));
    }

    /**
     * Returns the immutable exact per-variant factory map.
     *
     * @return variant factories
     */
    public Map<Vendor.Variant, VendorAdapter.Factory<O>> factories() {
        return adapterFactories;
    }

    /**
     * Creates the checked Options binding for one declared variant.
     *
     * @param variant declared Vendor variant
     * @return checked Options binding
     */
    OptionsBindings.Binding optionsBinding(final Vendor.Variant variant) {
        final VariantManifest.Variant selected = manifest
                .variant(Assert.notNull(variant, "Vendor driver variant must not be null"));
        final VendorOptions.Factory<O> factory = optionFactories.get(variant);
        return new OptionsBindings.Binding(manifest, selected,
                Assert.notNull(factory, "Vendor driver Options factory must not be null"));
    }

    /**
     * Returns the immutable exact per-variant Options factory map.
     *
     * @return variant Options factories
     */
    public Map<Vendor.Variant, VendorOptions.Factory<O>> optionFactories() {
        return optionFactories;
    }

}
