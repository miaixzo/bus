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

import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds the immutable exact bindings between platform variants and their adapter factories.
 * <p>
 * Selection uses only {@link Key}; the bindings perform no platform switch, reflection, service loading, package
 * scanning, or fallback adapter construction.
 * </p>
 *
 * @author Kimi Liu
 */
final class AdapterBindings {

    /**
     * Immutable factory bindings keyed by exact platform and variant.
     */
    private final Map<Key, VendorAdapter.Factory<?>> bindings;

    /**
     * Creates and freezes the complete platform adapter factory bindings.
     *
     * @param bindings complete exact platform-variant adapter bindings
     * @throws IllegalArgumentException if the map, a key, or a factory is null
     */
    AdapterBindings(final Map<Key, VendorAdapter.Factory<?>> bindings) {
        Assert.notNull(bindings, "Vendor adapter bindings must not be null");
        final Map<Key, VendorAdapter.Factory<?>> copy = new LinkedHashMap<>(bindings.size());
        for (Map.Entry<Key, VendorAdapter.Factory<?>> entry : bindings.entrySet()) {
            final Key key = Assert.notNull(entry.getKey(), "Vendor adapter factory key must not be null");
            final VendorAdapter.Factory<?> factory = Assert
                    .notNull(entry.getValue(), "Vendor adapter factory must not be null");
            copy.put(key, factory);
        }
        this.bindings = Map.copyOf(copy);
    }

    /**
     * Performs the single contained generic cast after exact runtime options-class validation.
     *
     * @param namespaceId namespace identifier
     * @param sourceId    Source identifier
     * @param manifest    selected platform manifest
     * @param variant     selected variant
     * @param options     exact options instance
     * @param services    runtime dependencies
     * @param factory     paired factory
     * @param <S>         exact options type
     * @return adapter created by the paired factory
     */
    private static <S extends VendorOptions<?>> VendorAdapter createTyped(
            final String namespaceId,
            final String sourceId,
            final VariantManifest<?> manifest,
            final VariantManifest.Variant variant,
            final VendorOptions<?> options,
            final ExecutionServices services,
            final VendorAdapter.Factory<?> factory) {
        return ((VendorAdapter.Factory) factory)
                .create(namespaceId, sourceId, (VariantManifest) manifest, variant, options, services);
    }

    /**
     * Returns the immutable adapter bindings used when composing a larger Vendor module.
     *
     * @return immutable exact platform-variant adapter bindings
     */
    Map<Key, VendorAdapter.Factory<?>> bindings() {
        return bindings;
    }

    /**
     * Creates and verifies the adapter registered for one exact platform Source.
     *
     * @param namespaceId namespace identifier from the current registration
     * @param sourceId    Source identifier from the current registration
     * @param manifest    exact selected platform manifest
     * @param variant     exact selected variant
     * @param options     decoded exact platform options
     * @param services    complete external runtime dependencies
     * @return non-null adapter whose capability manifest equals the selected variant's capability manifest
     * @throws IllegalArgumentException if an input is null or an isolation identifier is blank
     * @throws ValidateException        if pairing, options type, route values, factory execution, or manifest is
     *                                  invalid
     */
    VendorAdapter create(
            final String namespaceId,
            final String sourceId,
            final VariantManifest<?> manifest,
            final VariantManifest.Variant variant,
            final VendorOptions<?> options,
            final ExecutionServices services) {
        Assert.notBlank(namespaceId, "Vendor adapter namespace id must not be blank");
        Assert.notBlank(sourceId, "Vendor adapter Source id must not be blank");
        final VariantManifest<?> checkedManifest = Assert.notNull(manifest, "Vendor adapter manifest must not be null");
        final VariantManifest.Variant checkedVariant = Assert
                .notNull(variant, "Vendor adapter variant manifest must not be null");
        final VendorOptions<?> checkedOptions = Assert.notNull(options, "Vendor adapter options must not be null");
        final ExecutionServices checkedComponents = Assert
                .notNull(services, "Vendor adapter execution services must not be null");
        if (!checkedOptions.vendor().equals(checkedManifest.vendor())
                || !checkedVariant.platform().equals(checkedManifest.vendor())
                || !checkedOptions.variant().equals(checkedVariant.variant())) {
            throw new ValidateException("Vendor options routing keys do not match the selected manifest and variant");
        }
        final VendorAdapter.Factory<?> factory = bindings
                .get(new Key(checkedManifest.vendor(), checkedVariant.variant()));
        if (factory == null) {
            throw new ValidateException("Vendor adapter factory is not registered for the selected platform variant");
        }
        final VendorAdapter vendorAdapter;
        try {
            vendorAdapter = createTyped(
                    namespaceId,
                    sourceId,
                    checkedManifest,
                    checkedVariant,
                    checkedOptions,
                    checkedComponents,
                    factory);
        } catch (RuntimeException cause) {
            if (cause instanceof ValidateException validation) {
                throw validation;
            }
            throw new ValidateException("Vendor adapter factory failed", cause);
        }
        if (vendorAdapter == null) {
            throw new ValidateException("Vendor adapter factory returned null");
        }
        if (!vendorAdapter.manifest().equals(checkedVariant.capabilityManifest())) {
            throw new ValidateException("Vendor adapter capabilities must equal the selected variant capabilities");
        }
        return vendorAdapter;
    }

    /**
     * Identifies one exact platform adapter factory pairing.
     *
     * @param vendor  platform identifier
     * @param variant platform variant identifier
     * @author Kimi Liu
     */
    record Key(Vendor.Id vendor, Vendor.Variant variant) {

        /**
         * Validates an exact platform and variant key.
         *
         * @throws IllegalArgumentException if either identifier is null
         */
        public Key {
            Assert.notNull(vendor, "Vendor adapter key platform must not be null");
            Assert.notNull(variant, "Vendor adapter key variant must not be null");
        }

    }

}
