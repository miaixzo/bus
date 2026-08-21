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
import java.util.List;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Declares the framework-owned immutable authentication manifest for exactly one third-party platform and all variants
 * supported for that platform.
 * <p>
 * A manifest contains only platform identity, presentation metadata, and immutable {@link Variant} facts. Project
 * deployment input remains in {@link VendorOptions}; manifest-to-factory assembly remains in {@link VendorDriver};
 * execution remains in {@link VendorAdapter}. A manifest performs no persistence decoding, credential resolution,
 * global registration, Source compilation, network operation, or authentication execution.
 * </p>
 *
 * @param <O> exact immutable options type accepted by this platform manifest
 * @author Kimi Liu
 */
public interface VariantManifest<O extends VendorOptions<?>> {

    /**
     * Returns the stable identifier of the platform described by this manifest.
     *
     * @return platform identifier
     */
    Vendor.Id vendor();

    /**
     * Returns immutable presentation metadata for management and discovery views.
     *
     * @return management presentation metadata
     */
    Vendor.Metadata metadata();

    /**
     * Returns all supported variants in deterministic declaration order.
     *
     * @return immutable non-empty variants
     */
    List<Variant> variants();

    /**
     * Returns the unique platform variant under the requested identifier.
     *
     * @param variant requested variant identifier
     * @return exact immutable variant
     * @throws ValidateException if the variant is unsupported
     */
    Variant variant(Vendor.Variant variant);

    /**
     * Returns the management form for this platform's immutable options type.
     * <p>
     * A platform may override the common Vendor fields when it has additional template inputs.
     * </p>
     *
     * @return immutable platform options form
     */
    default Scheme.Form form() {
        return Forms.common(variants().stream().anyMatch(variant -> variant.pkce() == Pkce.OPTIONAL));
    }

    /**
     * Holds the immutable common Vendor options form.
     */
    final class Forms {

        private Forms() {
        }

        private static Scheme.Form common(final boolean optionalPkce) {
            final List<Scheme.Form.Field> fields = new ArrayList<>();
            fields.add(field("clientId", "Client identifier", Scheme.Form.Type.TEXT, true));
            fields.add(field("credential", "Credential reference", Scheme.Form.Type.TEXT, true));
            fields.add(field("redirectUri", "Redirect URI", Scheme.Form.Type.URL, false));
            fields.add(field("scopes", "Scopes", Scheme.Form.Type.MULTI_SELECT, false));
            if (optionalPkce) {
                fields.add(field("pkce", "Enable optional PKCE", Scheme.Form.Type.BOOLEAN, false));
            }
            return new Scheme.Form(List.of(new Scheme.Form.Section("client", "Vendor client", fields)));
        }

        private static Scheme.Form.Field field(
                final String key,
                final String label,
                final Scheme.Form.Type type,
                final boolean required) {
            return new Scheme.Form.Field(key, label, type, required, Optional.empty(), List.of());
        }
    }

    /**
     * Carries the framework-owned immutable authentication facts for one exact platform variant.
     *
     * @param platform           stable identifier of the owning platform manifest
     * @param variant            stable platform variant identifier
     * @param protocol           actual industry-standard or proprietary wire protocol
     * @param pkce               immutable platform PKCE policy
     * @param defaultScopes      ordered framework defaults for authorization requests
     * @param targets            official fixed or constrained-template platform targets
     * @param capabilityManifest fully implemented capability manifest
     * @param deviations         documented platform deviations from the selected protocol
     * @author Kimi Liu
     */
    record Variant(Vendor.Id platform, Vendor.Variant variant, Protocol protocol, Pkce pkce,
            List<String> defaultScopes, VendorTargets targets, Capability.Manifest capabilityManifest,
            List<VendorDeviation> deviations) {

        /**
         * Creates a variant that does not support PKCE.
         * <p>
         * This compatibility constructor deliberately defaults to {@link Pkce#DISABLED}; manifests must opt in to
         * optional or required PKCE explicitly so deployment options cannot invent a platform capability.
         * </p>
         *
         * @param platform           stable owning platform
         * @param variant            stable platform variant
         * @param protocol           actual wire protocol
         * @param defaultScopes      ordered framework scope defaults
         * @param targets            fixed or constrained platform targets
         * @param capabilityManifest implemented capability manifest
         * @param deviations         documented protocol deviations
         */
        public Variant(final Vendor.Id platform, final Vendor.Variant variant, final Protocol protocol,
                final List<String> defaultScopes, final VendorTargets targets,
                final Capability.Manifest capabilityManifest, final List<VendorDeviation> deviations) {
            this(platform, variant, protocol, Pkce.DISABLED, defaultScopes, targets, capabilityManifest, deviations);
        }

        /**
         * Validates and freezes one platform variant.
         *
         * @throws IllegalArgumentException if a component or collection item is {@code null}
         */
        public Variant {
            platform = Assert.notNull(platform, "Variant manifest platform must not be null");
            variant = Assert.notNull(variant, "Variant manifest identifier must not be null");
            protocol = Assert.notNull(protocol, "Variant manifest protocol must not be null");
            pkce = Assert.notNull(pkce, "Variant manifest PKCE policy must not be null");
            Assert.notNull(defaultScopes, "Variant manifest default scopes must not be null");
            final List<String> scopes = new ArrayList<>(defaultScopes.size());
            for (String scope : defaultScopes) {
                scopes.add(Assert.notBlank(scope, "Variant manifest default scope must not be blank"));
            }
            defaultScopes = List.copyOf(scopes);
            targets = Assert.notNull(targets, "Variant manifest targets must not be null");
            capabilityManifest = Assert.notNull(capabilityManifest, "Variant capability manifest must not be null");
            Assert.notNull(deviations, "Variant manifest deviations must not be null");
            final List<VendorDeviation> copy = new ArrayList<>(deviations.size());
            for (VendorDeviation deviation : deviations) {
                copy.add(Assert.notNull(deviation, "Variant manifest deviation must not be null"));
            }
            deviations = List.copyOf(copy);
        }

    }

    /**
     * Defines whether one platform variant forbids, permits, or requires S256 PKCE.
     * <p>
     * This is a platform protocol fact. Project options participate only when the manifest explicitly declares
     * {@link #OPTIONAL}; they can never disable {@link #REQUIRED} or enable {@link #DISABLED}.
     * </p>
     */
    enum Pkce {

        DISABLED,
        OPTIONAL,
        REQUIRED;

        /**
         * Resolves the final PKCE decision from this immutable policy and deployment options.
         *
         * @param options selected immutable deployment options
         * @return whether S256 PKCE must be generated for this Source
         * @throws ValidateException if options try to enable PKCE for a disabled variant
         */
        public boolean resolve(final VendorOptions<?> options) {
            final VendorOptions<?> checked = Assert.notNull(options, "Vendor PKCE options must not be null");
            return switch (this) {
                case DISABLED -> {
                    if (checked.pkce()) {
                        throw new ValidateException("Selected Vendor variant does not support PKCE");
                    }
                    yield false;
                }
                case OPTIONAL -> checked.pkce();
                case REQUIRED -> true;
            };
        }
    }

}
