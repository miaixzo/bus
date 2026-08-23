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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the aggregate Source scheme shared by all explicitly assembled third-party platforms.
 * <p>
 * This class owns only the stable aggregate Source identifier, aggregate protocol classification, and common
 * authentication entry capabilities. Platform metadata and immutable variant facts belong to {@link VendorManifest};
 * project-supplied deployment values belong to {@link VendorOptions}. This scheme does not select a platform, define
 * platform fields or endpoints, bind an adapter, or compile a worker.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorScheme implements Scheme<VendorOptions<?>> {

    /**
     * Stable aggregate Vendor Source type.
     */
    public static final String ID = "vendor";

    /**
     * Capabilities common to every Vendor Source before a concrete variant is selected.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of());

    /**
     * Provider-neutral metadata for the aggregate Vendor routing scheme.
     */
    private static final Metadata METADATA = new Metadata("Vendor",
            "Selects an explicitly registered platform variant.", "vendor");

    /**
     * Empty aggregate form because concrete option fields depend on the selected platform options type.
     */
    private static final Form FORM = new Form(List.of());

    /**
     * Immutable protocols represented by all assembled Vendor variants.
     */
    private final Set<Protocol> protocols;

    /**
     * Creates an aggregate scheme from the exact assembled Vendor locator.
     *
     * @param locator assembled Vendor locator
     */
    public VendorScheme(final VendorLocator locator) {
        final Set<Protocol> selected = new LinkedHashSet<>();
        for (VendorManifest<?> manifest : locator.manifests()) {
            for (VendorManifest.Variant variant : manifest.variants()) {
                selected.add(variant.protocol());
            }
        }
        if (selected.isEmpty()) {
            throw new ValidateException("Vendor scheme must represent at least one registered variant protocol");
        }
        this.protocols = Set.copyOf(selected);
    }

    /**
     * Returns the stable aggregate Vendor Source type identifier.
     *
     * @return stable Vendor scheme identifier
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * Returns implementation-neutral metadata for the aggregate Vendor driver.
     *
     * @return immutable aggregate Vendor metadata
     */
    @Override
    public Metadata metadata() {
        return METADATA;
    }

    /**
     * Returns every real protocol represented by the assembled Vendor variants.
     *
     * @return immutable represented protocol set
     */
    @Override
    public Set<Protocol> protocols() {
        return protocols;
    }

    /**
     * Returns the capability intersection shared by every registered Vendor variant.
     *
     * @return immutable common capability manifest, currently empty
     */
    @Override
    public Capability.Manifest manifest() {
        return CAPABILITIES;
    }

    /**
     * Returns no aggregate conformance claim because conformance belongs to each selected Variant.
     *
     * @return empty conformance container
     */
    @Override
    public Optional<Conformance> conformance() {
        return Optional.empty();
    }

    /**
     * Returns the empty aggregate form used before a concrete platform is selected.
     *
     * @return immutable empty aggregate form
     */
    @Override
    public Form form() {
        return FORM;
    }

    /**
     * Returns no default Vendor options because a platform and Variant must be selected explicitly.
     *
     * @return empty default-options container
     */
    @Override
    public Optional<VendorOptions<?>> defaults() {
        // A concrete platform and variant must be selected before deployment options can exist.
        return Optional.empty();
    }

}
