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
 * Pairs one externally contributed Vendor definition with the exact factories for all of its declared variants.
 * <p>
 * A driver is startup code, not persisted registration data. External projects continue to load configured Source
 * registrations through the common RegistrationLoader after the complete Vendor module has been frozen.
 * </p>
 *
 * @param <S> exact immutable settings type accepted by the contributed definition
 * @author Kimi Liu
 */
public final class VendorDriver<S extends VendorSettings> {

    /**
     * Immutable contributed Vendor definition.
     */
    private final VendorDefinition<S> vendorDefinition;

    /**
     * Immutable factory map covering every declared platform variant.
     */
    private final Map<Vendor.Variant, VendorAdapter.Factory<S>> factories;

    /**
     * Creates and validates one complete immutable driver.
     *
     * @param vendorDefinition contributed Vendor definition
     * @param factories        exact factory map covering every declared variant
     * @throws IllegalArgumentException if an argument or map member is null
     * @throws ValidateException        if a variant is missing, duplicated, or not declared by the definition
     */
    private VendorDriver(final VendorDefinition<S> vendorDefinition,
            final Map<Vendor.Variant, VendorAdapter.Factory<S>> factories) {
        this.vendorDefinition = Assert.notNull(vendorDefinition, "Vendor driver definition must not be null");
        Assert.notNull(factories, "Vendor driver factories must not be null");
        final Map<Vendor.Variant, VendorAdapter.Factory<S>> copy = new LinkedHashMap<>(factories.size());
        for (Map.Entry<Vendor.Variant, VendorAdapter.Factory<S>> entry : factories.entrySet()) {
            final Vendor.Variant variant = Assert.notNull(entry.getKey(), "Vendor driver variant must not be null");
            final VendorAdapter.Factory<S> factory = Assert
                    .notNull(entry.getValue(), "Vendor driver factory must not be null");
            vendorDefinition.variant(variant);
            if (copy.putIfAbsent(variant, factory) != null) {
                throw new ValidateException("Duplicate Vendor driver variant: " + variant.value());
            }
        }
        for (VendorDefinition.Definition variantDefinition : vendorDefinition.variants()) {
            if (!copy.containsKey(variantDefinition.variant())) {
                throw new ValidateException(
                        "Vendor driver factory is missing for variant: " + variantDefinition.variant().value());
            }
        }
        if (copy.size() != vendorDefinition.variants().size()) {
            throw new ValidateException("Vendor driver factories must exactly cover the defined variants");
        }
        this.factories = Map.copyOf(copy);
    }

    /**
     * Creates a driver with explicit per-variant platform execution factories.
     *
     * @param vendorDefinition contributed Vendor definition
     * @param factories        exact factory map covering every declared variant
     * @param <S>              exact immutable settings type
     * @return complete custom driver
     */
    public static <S extends VendorSettings> VendorDriver<S> of(
            final VendorDefinition<S> vendorDefinition,
            final Map<Vendor.Variant, VendorAdapter.Factory<S>> factories) {
        return new VendorDriver<>(vendorDefinition, factories);
    }

    /**
     * Returns the contributed immutable Vendor definition.
     *
     * @return Vendor definition
     */
    public VendorDefinition<S> definition() {
        return vendorDefinition;
    }

    /**
     * Returns the immutable exact per-variant factory map.
     *
     * @return variant factories
     */
    public Map<Vendor.Variant, VendorAdapter.Factory<S>> factories() {
        return factories;
    }

}
