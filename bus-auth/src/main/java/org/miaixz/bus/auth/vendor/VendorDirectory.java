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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds the immutable creation-time directory of all explicitly contributed Vendor manifests and variants.
 * <p>
 * The directory performs no reflection, service loading, endpoint execution, Registry access, or post-construction
 * registration.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorDirectory {

    /**
     * Vendor manifests retained in deterministic construction order.
     */
    private final List<VariantManifest<?>> manifests;

    /**
     * Immutable platform lookup index.
     */
    private final Map<Vendor.Id, VariantManifest<?>> manifestsById;

    /**
     * Immutable variant lookup index grouped by platform identifier.
     */
    private final Map<Vendor.Id, Map<Vendor.Variant, VariantManifest.Variant>> variantsById;

    /**
     * Creates and freezes a directory whose platform and per-platform variant identifiers are unique.
     *
     * @param manifests complete explicitly assembled Vendor manifest list
     * @throws IllegalArgumentException if a list member or required manifest value is null
     * @throws ValidateException        if a platform is duplicated or a manifest has no variant or duplicate variants
     */
    public VendorDirectory(final List<VariantManifest<?>> manifests) {
        Assert.notNull(manifests, "Vendor manifests must not be null");
        final List<VariantManifest<?>> orderedManifests = new ArrayList<>(manifests.size());
        final Map<Vendor.Id, VariantManifest<?>> manifestsById = new LinkedHashMap<>(manifests.size());
        final Map<Vendor.Id, Map<Vendor.Variant, VariantManifest.Variant>> variantsById = new LinkedHashMap<>(
                manifests.size());
        for (VariantManifest<?> candidateManifest : manifests) {
            final VariantManifest<?> manifest = Assert.notNull(candidateManifest, "Vendor manifest must not be null");
            final Vendor.Id vendorId = Assert.notNull(manifest.vendor(), "Vendor manifest id must not be null");
            if (manifestsById.putIfAbsent(vendorId, manifest) != null) {
                throw new ValidateException("Duplicate Vendor manifest id: " + vendorId.value());
            }
            final List<VariantManifest.Variant> variants = Assert
                    .notNull(manifest.variants(), "Vendor manifest variants must not be null");
            if (variants.isEmpty()) {
                throw new ValidateException("Vendor manifest must declare at least one variant: " + vendorId.value());
            }
            final Map<Vendor.Variant, VariantManifest.Variant> variantsByKey = new LinkedHashMap<>(variants.size());
            for (VariantManifest.Variant variant : variants) {
                final VariantManifest.Variant checkedVariant = Assert
                        .notNull(variant, "Vendor variant must not be null");
                if (!vendorId.equals(checkedVariant.platform())) {
                    throw new ValidateException("Vendor variant platform does not match its owning manifest: "
                            + vendorId.value() + Symbol.SLASH + checkedVariant.variant().value());
                }
                if (variantsByKey.putIfAbsent(checkedVariant.variant(), checkedVariant) != null) {
                    throw new ValidateException("Duplicate Vendor variant " + vendorId.value() + Symbol.SLASH
                            + checkedVariant.variant().value());
                }
            }
            orderedManifests.add(manifest);
            variantsById.put(vendorId, Map.copyOf(variantsByKey));
        }
        this.manifests = List.copyOf(orderedManifests);
        this.manifestsById = Map.copyOf(manifestsById);
        this.variantsById = Map.copyOf(variantsById);
    }

    /**
     * Returns all Vendor manifests in deterministic construction order.
     *
     * @return immutable Vendor manifest list
     */
    public List<VariantManifest<?>> manifests() {
        return manifests;
    }

    /**
     * Finds a Vendor manifest by its exact stable identifier.
     *
     * @param id platform identifier
     * @return matching manifest or empty when absent
     */
    public Optional<VariantManifest<?>> find(final Vendor.Id id) {
        Assert.notNull(id, "Vendor manifest id must not be null");
        return Optional.ofNullable(manifestsById.get(id));
    }

    /**
     * Returns the unique Vendor manifest registered under an identifier.
     *
     * @param id platform identifier
     * @return matching immutable manifest
     * @throws IllegalArgumentException if the identifier is null
     * @throws ValidateException        if no manifest is registered
     */
    public VariantManifest<?> require(final Vendor.Id id) {
        Assert.notNull(id, "Vendor manifest id must not be null");
        final VariantManifest<?> manifest = manifestsById.get(id);
        if (manifest == null) {
            throw new ValidateException("Vendor manifest not found: " + id.value());
        }
        return manifest;
    }

    /**
     * Returns the unique variant registered for a platform and variant pair.
     *
     * @param id        platform identifier
     * @param variantId platform variant identifier
     * @return matching immutable variant
     * @throws IllegalArgumentException if an identifier is null
     * @throws ValidateException        if the platform or variant is not registered
     */
    public VariantManifest.Variant require(final Vendor.Id id, final Vendor.Variant variantId) {
        Assert.notNull(id, "Vendor manifest id must not be null");
        Assert.notNull(variantId, "Vendor variant must not be null");
        final Map<Vendor.Variant, VariantManifest.Variant> variants = variantsById.get(id);
        if (variants == null) {
            throw new ValidateException("Vendor manifest not found: " + id.value());
        }
        final VariantManifest.Variant variant = variants.get(variantId);
        if (variant == null) {
            throw new ValidateException("Vendor variant not found: " + id.value() + Symbol.SLASH + variantId.value());
        }
        return variant;
    }

}
