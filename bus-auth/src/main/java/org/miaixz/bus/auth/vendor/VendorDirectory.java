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
 * Holds the immutable creation-time directory of all explicitly contributed Vendor definitions and variants.
 * <p>
 * The directory performs no reflection, service loading, endpoint execution, Registry access, or post-construction
 * registration.
 * </p>
 *
 * @author Kimi Liu
 */
public final class VendorDirectory {

    /**
     * Vendor definitions retained in deterministic construction order.
     */
    private final List<VendorDefinition<?>> definitions;

    /**
     * Immutable platform lookup index.
     */
    private final Map<Vendor.Id, VendorDefinition<?>> definitionsById;

    /**
     * Immutable variant lookup index grouped by platform identifier.
     */
    private final Map<Vendor.Id, Map<Vendor.Variant, VendorDefinition.Definition>> variantDefinitionsById;

    /**
     * Creates and freezes a directory whose platform and per-platform variant identifiers are unique.
     *
     * @param definitions complete explicitly assembled Vendor definition list
     * @throws IllegalArgumentException if a list member or required definition value is null
     * @throws ValidateException        if a platform is duplicated or a definition has no variant or duplicate variants
     */
    public VendorDirectory(final List<VendorDefinition<?>> definitions) {
        Assert.notNull(definitions, "Vendor definitions must not be null");
        final List<VendorDefinition<?>> orderedDefinitions = new ArrayList<>(definitions.size());
        final Map<Vendor.Id, VendorDefinition<?>> definitionsById = new LinkedHashMap<>(definitions.size());
        final Map<Vendor.Id, Map<Vendor.Variant, VendorDefinition.Definition>> variantDefinitionsById = new LinkedHashMap<>(
                definitions.size());
        for (VendorDefinition<?> candidateDefinition : definitions) {
            final VendorDefinition<?> vendorDefinition = Assert
                    .notNull(candidateDefinition, "Vendor definition must not be null");
            final Vendor.Id vendorId = Assert.notNull(vendorDefinition.type(), "Vendor definition id must not be null");
            if (definitionsById.putIfAbsent(vendorId, vendorDefinition) != null) {
                throw new ValidateException("Duplicate Vendor definition id: " + vendorId.value());
            }
            final List<VendorDefinition.Definition> variantDefinitions = Assert
                    .notNull(vendorDefinition.variants(), "Vendor definition variants must not be null");
            if (variantDefinitions.isEmpty()) {
                throw new ValidateException("Vendor definition must declare at least one variant: " + vendorId.value());
            }
            final Map<Vendor.Variant, VendorDefinition.Definition> definitionsByVariant = new LinkedHashMap<>(
                    variantDefinitions.size());
            for (VendorDefinition.Definition variantDefinition : variantDefinitions) {
                final VendorDefinition.Definition checkedVariantDefinition = Assert
                        .notNull(variantDefinition, "Vendor variant definition must not be null");
                if (!vendorId.equals(checkedVariantDefinition.platform())) {
                    throw new ValidateException("Vendor variant platform does not match its owning definition: "
                            + vendorId.value() + Symbol.SLASH + checkedVariantDefinition.variant().value());
                }
                if (definitionsByVariant
                        .putIfAbsent(checkedVariantDefinition.variant(), checkedVariantDefinition) != null) {
                    throw new ValidateException("Duplicate Vendor variant " + vendorId.value() + Symbol.SLASH
                            + checkedVariantDefinition.variant().value());
                }
            }
            orderedDefinitions.add(vendorDefinition);
            variantDefinitionsById.put(vendorId, Map.copyOf(definitionsByVariant));
        }
        this.definitions = List.copyOf(orderedDefinitions);
        this.definitionsById = Map.copyOf(definitionsById);
        this.variantDefinitionsById = Map.copyOf(variantDefinitionsById);
    }

    /**
     * Returns all Vendor definitions in deterministic construction order.
     *
     * @return immutable Vendor definition list
     */
    public List<VendorDefinition<?>> definitions() {
        return definitions;
    }

    /**
     * Finds a Vendor definition by its exact stable identifier.
     *
     * @param id platform identifier
     * @return matching definition or empty when absent
     */
    public Optional<VendorDefinition<?>> find(final Vendor.Id id) {
        Assert.notNull(id, "Vendor definition id must not be null");
        return Optional.ofNullable(definitionsById.get(id));
    }

    /**
     * Returns the unique Vendor definition registered under an identifier.
     *
     * @param id platform identifier
     * @return matching immutable definition
     * @throws IllegalArgumentException if the identifier is null
     * @throws ValidateException        if no definition is registered
     */
    public VendorDefinition<?> require(final Vendor.Id id) {
        Assert.notNull(id, "Vendor definition id must not be null");
        final VendorDefinition<?> vendorDefinition = definitionsById.get(id);
        if (vendorDefinition == null) {
            throw new ValidateException("Vendor definition not found: " + id.value());
        }
        return vendorDefinition;
    }

    /**
     * Returns the unique definition registered for a platform and variant pair.
     *
     * @param id      platform identifier
     * @param variant platform variant identifier
     * @return matching immutable variant definition
     * @throws IllegalArgumentException if an identifier is null
     * @throws ValidateException        if the platform or variant is not registered
     */
    public VendorDefinition.Definition require(final Vendor.Id id, final Vendor.Variant variant) {
        Assert.notNull(id, "Vendor definition id must not be null");
        Assert.notNull(variant, "Vendor variant must not be null");
        final Map<Vendor.Variant, VendorDefinition.Definition> variantDefinitions = variantDefinitionsById.get(id);
        if (variantDefinitions == null) {
            throw new ValidateException("Vendor definition not found: " + id.value());
        }
        final VendorDefinition.Definition variantDefinition = variantDefinitions.get(variant);
        if (variantDefinition == null) {
            throw new ValidateException("Vendor variant not found: " + id.value() + Symbol.SLASH + variant.value());
        }
        return variantDefinition;
    }

}
