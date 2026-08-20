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

import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds the immutable exact bindings between platform variants and their internal adapter factories.
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
     * Performs the single contained generic cast after exact runtime settings-class validation.
     *
     * @param namespaceId       namespace identifier
     * @param sourceId          Source identifier
     * @param vendorDefinition  selected Vendor definition
     * @param variantDefinition selected variant definition
     * @param settings          exact settings instance
     * @param services          runtime dependencies
     * @param factory           paired factory
     * @param <S>               exact settings type
     * @return adapter created by the paired factory
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <S extends VendorSettings> VendorAdapter createTyped(
            final String namespaceId,
            final String sourceId,
            final VendorDefinition<?> vendorDefinition,
            final VendorDefinition.Definition variantDefinition,
            final VendorSettings settings,
            final ExecutionServices services,
            final VendorAdapter.Factory<?> factory) {
        return ((VendorAdapter.Factory) factory).create(
                namespaceId,
                sourceId,
                (VendorDefinition) vendorDefinition,
                variantDefinition,
                settings,
                services);
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
     * @param namespaceId       namespace identifier from the current registration
     * @param sourceId          Source identifier from the current registration
     * @param vendorDefinition  exact selected Vendor definition
     * @param variantDefinition exact selected variant definition
     * @param settings          decoded exact platform settings
     * @param services          complete external runtime dependencies
     * @return non-null adapter whose manifest exactly equals the selected definition
     * @throws IllegalArgumentException if an input is null or an isolation identifier is blank
     * @throws ValidateException        if pairing, settings type, route values, factory execution, or manifest is
     *                                  invalid
     */
    VendorAdapter create(
            final String namespaceId,
            final String sourceId,
            final VendorDefinition<?> vendorDefinition,
            final VendorDefinition.Definition variantDefinition,
            final VendorSettings settings,
            final ExecutionServices services) {
        Assert.notBlank(namespaceId, "Vendor adapter namespace id must not be blank");
        Assert.notBlank(sourceId, "Vendor adapter Source id must not be blank");
        final VendorDefinition<?> checkedVendorDefinition = Assert
                .notNull(vendorDefinition, "Vendor adapter definition must not be null");
        final VendorDefinition.Definition checkedVariantDefinition = Assert
                .notNull(variantDefinition, "Vendor adapter variant definition must not be null");
        final VendorSettings checkedSettings = Assert.notNull(settings, "Vendor adapter settings must not be null");
        final ExecutionServices checkedComponents = Assert
                .notNull(services, "Vendor adapter execution services must not be null");
        if (checkedSettings.getClass() != checkedVendorDefinition.settingsType()) {
            throw new ValidateException(
                    "Vendor settings class must exactly equal the selected definition settings type");
        }
        if (!checkedSettings.vendor().equals(checkedVendorDefinition.type())
                || !checkedVariantDefinition.platform().equals(checkedVendorDefinition.type())
                || !checkedSettings.variant().equals(checkedVariantDefinition.variant())) {
            throw new ValidateException(
                    "Vendor settings routing keys do not match the selected definition and variant");
        }
        final VendorAdapter.Factory<?> factory = bindings
                .get(new Key(checkedVendorDefinition.type(), checkedVariantDefinition.variant()));
        if (factory == null) {
            throw new ValidateException("Vendor adapter factory is not registered for the selected platform variant");
        }
        final VendorAdapter vendorAdapter;
        try {
            vendorAdapter = createTyped(
                    namespaceId,
                    sourceId,
                    checkedVendorDefinition,
                    checkedVariantDefinition,
                    checkedSettings,
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
        if (!vendorAdapter.manifest().equals(checkedVariantDefinition.manifest())) {
            throw new ValidateException("Vendor adapter manifest must equal the selected variant definition manifest");
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
