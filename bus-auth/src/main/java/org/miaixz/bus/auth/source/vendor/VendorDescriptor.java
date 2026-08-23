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

import java.util.List;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.source.SourceDescriptor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;

/**
 * Describes one exact registered third-party platform variant as a selectable Source.
 * <p>
 * Strongly typed Vendor facts are retained for management configuration, while adapter and options factories remain
 * private to Vendor assembly. Matching reads only non-sensitive route values already present in Source options.
 * </p>
 *
 * @author Kimi Liu
 */
public class VendorDescriptor implements SourceDescriptor {

    /**
     * Stable descriptor identifier prefix.
     */
    private static final String PREFIX = "vendor" + Symbol.C_SLASH;

    /**
     * Platform manifest that owns the selected variant.
     */
    private final VendorManifest<?> vendorManifest;

    /**
     * Exact selected immutable variant facts.
     */
    private final VendorManifest.Variant selected;

    /**
     * Exact presentation metadata resolved by the owning manifest.
     */
    private final Scheme.Metadata metadata;

    /**
     * Exact configuration form resolved by the owning manifest.
     */
    private final Scheme.Form form;

    /**
     * Verified formal conformance resolved by the owning manifest.
     */
    private final Optional<Conformance> conformance;

    /**
     * Creates one immutable descriptor for a manifest-owned exact variant.
     *
     * @param manifest platform manifest
     * @param variant  exact manifest variant
     */
    public VendorDescriptor(final VendorManifest<?> manifest, final VendorManifest.Variant variant) {
        this.vendorManifest = Assert.notNull(manifest, "Vendor descriptor manifest must not be null");
        this.selected = Assert.notNull(variant, "Vendor descriptor variant must not be null");
        if (!manifest.vendor().equals(variant.platform()) || !manifest.variant(variant.variant()).equals(variant)) {
            throw new IllegalArgumentException("Vendor descriptor variant is not owned by its manifest");
        }
        this.metadata = Assert
                .notNull(manifest.metadata(variant.variant()), "Vendor descriptor metadata must not be null");
        this.form = Assert.notNull(manifest.form(variant.variant()), "Vendor descriptor form must not be null");
        this.conformance = Assert
                .notNull(manifest.conformance(variant.variant()), "Vendor descriptor conformance must not be null");
    }

    /**
     * Returns the stable globally unique Vendor selection identifier.
     *
     * @return identifier in {@code vendor/platform/variant} form
     */
    @Override
    public String id() {
        return PREFIX + vendor().value() + Symbol.C_SLASH + variant().value();
    }

    /**
     * Returns the aggregate Vendor driver type written to Source persistence.
     *
     * @return Vendor scheme identifier
     */
    @Override
    public String type() {
        return VendorScheme.ID;
    }

    /**
     * Returns the Vendor management grouping.
     *
     * @return Vendor descriptor kind
     */
    @Override
    public Kind kind() {
        return Kind.VENDOR;
    }

    /**
     * Returns exact platform-variant presentation metadata.
     *
     * @return immutable metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return metadata;
    }

    /**
     * Returns the actual protocol used by this platform variant.
     *
     * @return exact variant protocol
     */
    @Override
    public Protocol protocol() {
        return selected.protocol();
    }

    /**
     * Returns the exact capabilities implemented by this variant adapter.
     *
     * @return immutable variant capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return selected.capabilityManifest();
    }

    /**
     * Returns the variant's verified formal standards basis.
     *
     * @return conformance declaration or empty
     */
    @Override
    public Optional<Conformance> conformance() {
        return conformance;
    }

    /**
     * Returns the exact form accepted for this platform variant.
     *
     * @return immutable variant form
     */
    @Override
    public Scheme.Form form() {
        return form;
    }

    /**
     * Matches Vendor Source type, protocol, platform and variant without resolving credentials.
     *
     * @param source persisted Source candidate
     * @return whether the candidate has this exact Vendor route
     */
    @Override
    public boolean matches(final Source source) {
        if (source == null || !type().equals(source.getType()) || source.getProtocol() == null
                || !protocol().name().equalsIgnoreCase(source.getProtocol())) {
            return false;
        }
        return source.getOptions() instanceof VendorOptions<?> options && vendor().equals(options.vendor())
                && variant().equals(options.variant());
    }

    /**
     * Returns the exact platform identifier.
     *
     * @return Vendor platform identifier
     */
    public Vendor.Id vendor() {
        return selected.platform();
    }

    /**
     * Returns the exact platform variant identifier.
     *
     * @return Vendor variant identifier
     */
    public Vendor.Variant variant() {
        return selected.variant();
    }

    /**
     * Returns the credential material category required by this variant.
     *
     * @return credential type
     */
    public Credential.Type credentialType() {
        return selected.credentialType();
    }

    /**
     * Returns ordered default scopes supplied by the platform manifest.
     *
     * @return immutable default scope list
     */
    public List<String> defaultScopes() {
        return selected.defaultScopes();
    }

    /**
     * Returns the immutable PKCE requirement of this exact variant.
     *
     * @return PKCE policy
     */
    public VendorManifest.Pkce pkce() {
        return selected.pkce();
    }

    /**
     * Returns official fixed or constrained-template endpoint targets.
     *
     * @return immutable target declarations
     */
    public VendorTargets targets() {
        return selected.targets();
    }

    /**
     * Returns documented platform deviations from the selected protocol.
     *
     * @return immutable deviation list
     */
    public List<VendorDeviation> deviations() {
        return selected.deviations();
    }

    /**
     * Returns the owning platform manifest for strongly typed management inspection.
     *
     * @return immutable platform manifest
     */
    public VendorManifest<?> vendorManifest() {
        return vendorManifest;
    }

}
