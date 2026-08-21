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
 * Binds one externally contributed {@link VariantManifest} to the exact adapter factory for every declared variant.
 * <p>
 * A driver is immutable assembly input. It validates complete manifest-to-factory coverage but does not register
 * itself, load or persist Source data, construct adapters, or execute authentication. External projects own
 * contribution and Source loading; {@link VendorModule} owns freezing the resulting inventory.
 * </p>
 *
 * @param <O> exact immutable options type accepted by the contributed manifest
 * @author Kimi Liu
 */
public final class VendorDriver<O extends VendorOptions<?>> {

    /**
     * Immutable contributed Vendor manifest.
     */
    private final VariantManifest<O> manifest;

    /**
     * Immutable factory map covering every declared platform variant.
     */
    private final Map<Vendor.Variant, VendorAdapter.Factory<O>> factories;

    /**
     * Creates and validates one complete immutable driver.
     *
     * @param manifest  contributed platform manifest
     * @param factories exact factory map covering every declared variant
     * @throws IllegalArgumentException if an argument or map member is null
     * @throws ValidateException        if a variant is missing, duplicated, or not declared by the manifest
     */
    private VendorDriver(final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorAdapter.Factory<O>> factories) {
        this.manifest = Assert.notNull(manifest, "Vendor driver manifest must not be null");
        Assert.notNull(factories, "Vendor driver factories must not be null");
        final Map<Vendor.Variant, VendorAdapter.Factory<O>> copy = new LinkedHashMap<>(factories.size());
        for (Map.Entry<Vendor.Variant, VendorAdapter.Factory<O>> entry : factories.entrySet()) {
            final Vendor.Variant variant = Assert.notNull(entry.getKey(), "Vendor driver variant must not be null");
            final VendorAdapter.Factory<O> factory = Assert
                    .notNull(entry.getValue(), "Vendor driver factory must not be null");
            manifest.variant(variant);
            if (copy.putIfAbsent(variant, factory) != null) {
                throw new ValidateException("Duplicate Vendor driver variant: " + variant.value());
            }
        }
        for (VariantManifest.Variant variant : manifest.variants()) {
            if (!copy.containsKey(variant.variant())) {
                throw new ValidateException(
                        "Vendor driver factory is missing for variant: " + variant.variant().value());
            }
        }
        if (copy.size() != manifest.variants().size()) {
            throw new ValidateException("Vendor driver factories must exactly cover the defined variants");
        }
        this.factories = Map.copyOf(copy);
    }

    /**
     * Creates a driver with explicit per-variant platform execution factories.
     *
     * @param manifest  contributed platform manifest
     * @param factories exact factory map covering every declared variant
     * @param <O>       exact immutable options type
     * @return complete custom driver
     */
    public static <O extends VendorOptions<?>> VendorDriver<O> of(
            final VariantManifest<O> manifest,
            final Map<Vendor.Variant, VendorAdapter.Factory<O>> factories) {
        return new VendorDriver<>(manifest, factories);
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
     */
    AdapterBindings.Binding binding(final Vendor.Variant variant) {
        final VendorAdapter.Factory<O> factory = factories
                .get(Assert.notNull(variant, "Vendor driver variant must not be null"));
        return AdapterBindings.binding(manifest, Assert.notNull(factory, "Vendor driver factory must not be null"));
    }

    /**
     * Returns the immutable exact per-variant factory map.
     *
     * @return variant factories
     */
    public Map<Vendor.Variant, VendorAdapter.Factory<O>> factories() {
        return factories;
    }

}
