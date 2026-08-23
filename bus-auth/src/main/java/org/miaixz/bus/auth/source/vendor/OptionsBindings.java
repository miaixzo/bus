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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds exact platform-variant bindings to immutable Vendor Options factories.
 * <p>
 * This package-private binding index performs only assembly-time validation and exact-key lookup. It never constructs
 * Options, accesses credential material, writes project data, or compiles a Source.
 * </p>
 *
 * @author Kimi Liu
 */
final class OptionsBindings {

    /**
     * Immutable exact-key Options factory index.
     */
    private final Map<Key, Binding> values;

    /**
     * Validates and freezes one exact Options factory index.
     *
     * @param values complete platform-variant bindings
     */
    OptionsBindings(final Map<Key, Binding> values) {
        Assert.notNull(values, "Vendor Options bindings must not be null");
        final Map<Key, Binding> copy = new LinkedHashMap<>(values.size());
        values.forEach((key, binding) -> {
            final Key checkedKey = Assert.notNull(key, "Vendor Options binding key must not be null");
            final Binding checkedBinding = Assert.notNull(binding, "Vendor Options binding must not be null");
            if (!checkedKey.vendor().equals(checkedBinding.manifest().vendor())
                    || !checkedKey.variant().equals(checkedBinding.variant().variant())) {
                throw new ValidateException("Vendor Options binding key does not match its manifest variant");
            }
            if (copy.putIfAbsent(checkedKey, checkedBinding) != null) {
                throw new ValidateException("Duplicate Vendor Options factory binding");
            }
        });
        this.values = Map.copyOf(copy);
    }

    /**
     * Resolves one exact platform-variant Options factory binding.
     *
     * @param vendor  exact platform identifier
     * @param variant exact platform variant identifier
     * @return matching immutable binding
     * @throws ValidateException if no binding exists
     */
    Binding resolve(final Vendor.Id vendor, final Vendor.Variant variant) {
        final Binding binding = values.get(new Key(vendor, variant));
        if (binding == null) {
            throw new ValidateException("Vendor Options factory is not registered for the selected platform variant");
        }
        return binding;
    }

    /**
     * Returns all exact platform-variant keys.
     *
     * @return immutable exact binding keys
     */
    Set<Key> keys() {
        return values.keySet();
    }

    /**
     * Returns the immutable exact-key binding map for module assembly.
     *
     * @return immutable binding map
     */
    Map<Key, Binding> values() {
        return values;
    }

    /**
     * Identifies one exact Vendor platform variant.
     *
     * @param vendor  exact platform identifier
     * @param variant exact platform variant identifier
     * @author Kimi Liu
     */
    record Key(Vendor.Id vendor, Vendor.Variant variant) {

        /**
         * Validates one exact platform-variant key.
         */
        Key {
            vendor = Assert.notNull(vendor, "Vendor Options key platform must not be null");
            variant = Assert.notNull(variant, "Vendor Options key variant must not be null");
        }

    }

    /**
     * Joins one immutable manifest variant with its exact Options factory.
     *
     * @param manifest immutable owning platform manifest
     * @param variant  exact immutable platform variant
     * @param factory  exact immutable Options factory
     * @author Kimi Liu
     */
    record Binding(VendorManifest<?> manifest, VendorManifest.Variant variant, VendorOptions.Factory<?> factory) {

        /**
         * Validates one immutable Options factory binding.
         */
        Binding {
            manifest = Assert.notNull(manifest, "Vendor Options binding manifest must not be null");
            variant = Assert.notNull(variant, "Vendor Options binding variant must not be null");
            factory = Assert.notNull(factory, "Vendor Options binding factory must not be null");
            if (!manifest.vendor().equals(variant.platform()) || !manifest.variant(variant.variant()).equals(variant)) {
                throw new ValidateException("Vendor Options binding variant does not belong to its manifest");
            }
        }

    }

}
