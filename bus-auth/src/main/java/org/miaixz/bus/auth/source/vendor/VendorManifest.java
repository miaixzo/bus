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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Scheme.Conformance;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Declares the framework-owned immutable authentication manifest for exactly one third-party platform and all variants
 * supported for that platform.
 * <p>
 * A manifest contains only platform identity, presentation metadata, and immutable {@link Variant} facts. Project
 * deployment input remains in {@link VendorOptions}; manifest-to-factory assembly remains in {@link VendorBinding};
 * execution remains in {@link VendorAdapter}. A manifest performs no persistence decoding, credential resolution,
 * global registration, Source compilation, network operation, or authentication execution.
 * </p>
 *
 * @param <O> exact immutable options type accepted by this platform manifest
 * @author Kimi Liu
 */
public interface VendorManifest<O extends VendorOptions<?>> {

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
    Scheme.Metadata metadata();

    /**
     * Returns presentation metadata for one exact variant selection.
     * <p>
     * Single-variant platforms retain their platform metadata. Multi-variant platforms receive a stable variant suffix
     * unless the manifest overrides this method with a more specific product name.
     * </p>
     *
     * @param variant exact variant identifier
     * @return exact immutable variant metadata
     */
    default Scheme.Metadata metadata(final Vendor.Variant variant) {
        final Variant selected = variant(Assert.notNull(variant, "Vendor metadata variant must not be null"));
        final Scheme.Metadata platform = Assert.notNull(metadata(), "Vendor platform metadata must not be null");
        if (variants().size() == 1) {
            return platform;
        }
        final String suffix = selected.variant().value();
        return new Scheme.Metadata(platform.name() + " - " + suffix,
                platform.description() + " Variant: " + suffix + Symbol.DOT, platform.icon());
    }

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
     * @param variant exact platform variant whose form is requested
     * @return immutable platform options form
     */
    default Scheme.Form form(final Vendor.Variant variant) {
        final Variant selected = variant(Assert.notNull(variant, "Vendor form variant must not be null"));
        return Forms.common(selected);
    }

    /**
     * Returns the verified formal standards basis for one exact variant.
     * <p>
     * Vendor transports and authorization exchanges do not imply conformance automatically. Implementations override
     * this method only after the selected variant has been checked against a formal standard profile.
     * </p>
     *
     * @param variant exact variant identifier
     * @return verified conformance declaration or empty
     */
    default Optional<Conformance> conformance(final Vendor.Variant variant) {
        variant(Assert.notNull(variant, "Vendor conformance variant must not be null"));
        return Optional.empty();
    }

    /**
     * Defines whether one platform variant forbids, permits, or requires S256 PKCE.
     * <p>
     * This is a platform protocol fact. Project options participate only when the manifest explicitly declares
     * {@link #OPTIONAL}; they can never disable {@link #REQUIRED} or enable {@link #DISABLED}.
     * </p>
     *
     * @author Kimi Liu
     */
    enum Pkce {

        /**
         * Platform forbids PKCE.
         */
        DISABLED,
        /**
         * Deployment may enable PKCE.
         */
        OPTIONAL,
        /**
         * Platform always requires PKCE.
         */
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

    /**
     * Holds the immutable common Vendor options form.
     *
     * @author Kimi Liu
     */
    class Forms {

        /**
         * Creates a Vendor form encoder constant holder.
         */
        public Forms() {
            // No initialization required.
        }

        /**
         * Builds the common Vendor client configuration form.
         *
         * @param variant exact selected Vendor variant
         * @return immutable form
         */
        public static Scheme.Form common(final Variant variant) {
            final Variant selected = Assert.notNull(variant, "Vendor form variant must not be null");
            return extended(selected, browser(selected) || !selected.defaultScopes().isEmpty(), List.of());
        }

        /**
         * Builds the common Vendor client fields followed by declared platform parameter fields.
         *
         * @param variant    exact selected Vendor variant
         * @param parameters immutable platform-specific non-secret parameter fields
         * @return immutable one-time client configuration form
         */
        public static Scheme.Form extended(final Variant variant, final List<Scheme.Form.Field> parameters) {
            final Variant selected = Assert.notNull(variant, "Vendor form variant must not be null");
            return extended(selected, browser(selected) || !selected.defaultScopes().isEmpty(), parameters);
        }

        /**
         * Builds an exact Vendor form with explicit scope input policy for variants whose browser interaction does not
         * accept scopes.
         *
         * @param variant    exact selected Vendor variant
         * @param scopes     whether the selected options contract accepts scope input
         * @param parameters immutable platform-specific non-secret parameter fields
         * @return immutable exact variant configuration form
         */
        public static Scheme.Form extended(
                final Variant variant,
                final boolean scopes,
                final List<Scheme.Form.Field> parameters) {
            final Variant selected = Assert.notNull(variant, "Vendor form variant must not be null");
            final List<Scheme.Form.Field> fields = new ArrayList<>();
            fields.add(field("clientId", "Client identifier", Scheme.Form.Type.TEXT, true));
            fields.add(field("credential", "Client secret/private key", Scheme.Form.Type.SECRET, true));
            if (browser(selected)) {
                fields.add(field("redirectUri", "Redirect URI", Scheme.Form.Type.URL, true));
            }
            if (scopes) {
                fields.add(field("scopes", "Scopes", Scheme.Form.Type.MULTI_SELECT, false));
            }
            if (selected.pkce() == Pkce.OPTIONAL) {
                fields.add(field("pkce", "Enable optional PKCE", Scheme.Form.Type.BOOLEAN, false));
            }
            Assert.notNull(parameters, "Vendor form parameter fields must not be null");
            for (Scheme.Form.Field parameter : parameters) {
                fields.add(Assert.notNull(parameter, "Vendor form parameter field must not be null"));
            }
            return new Scheme.Form(List.of(new Scheme.Form.Section("client", "Vendor client", fields)));
        }

        /**
         * Reports whether a selected variant performs a user-agent redirect interaction.
         *
         * @param variant exact selected Vendor variant
         * @return whether the exact configuration requires a redirect URI
         */
        private static boolean browser(final Variant variant) {
            if (variant.targets().authorization().isPresent()) {
                return true;
            }
            for (Capability<?, ?> capability : variant.capabilityManifest().capabilities()) {
                if (capability.interactions().contains(Capability.Interaction.REDIRECT)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Creates one common Vendor form field without a default or constraints.
         *
         * @param key      formal option key
         * @param label    display label
         * @param type     presentation type
         * @param required whether required
         * @return immutable field
         */
        public static Scheme.Form.Field field(
                final String key,
                final String label,
                final Scheme.Form.Type type,
                final boolean required) {
            return new Scheme.Form.Field(key, label, type, required, Optional.empty(), List.of());
        }

        /**
         * Creates one Vendor form field with a non-sensitive default and no external constraints.
         *
         * @param key          formal option key
         * @param label        display label
         * @param type         presentation type
         * @param required     whether required
         * @param defaultValue non-sensitive default value
         * @return immutable field
         */
        public static Scheme.Form.Field field(
                final String key,
                final String label,
                final Scheme.Form.Type type,
                final boolean required,
                final JsonValue defaultValue) {
            return new Scheme.Form.Field(key, label, type, required, Optional.of(defaultValue), List.of());
        }

    }

    /**
     * Carries the framework-owned immutable authentication facts for one exact platform variant.
     *
     * @param platform           stable identifier of the owning platform manifest
     * @param variant            stable platform variant identifier
     * @param protocol           actual industry-standard or proprietary wire protocol
     * @param pkce               immutable platform PKCE policy
     * @param credentialType     exact credential material required by this variant
     * @param defaultScopes      ordered framework defaults for authorization requests
     * @param targets            official fixed or constrained-template platform targets
     * @param capabilityManifest fully implemented capability manifest
     * @param deviations         documented platform deviations from the selected protocol
     * @author Kimi Liu
     */
    record Variant(Vendor.Id platform, Vendor.Variant variant, Protocol protocol, Pkce pkce,
            Credential.Type credentialType, List<String> defaultScopes, VendorTargets targets,
            Capability.Manifest capabilityManifest, List<VendorDeviation> deviations) {

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
            credentialType = Assert.notNull(credentialType, "Variant manifest credential type must not be null");
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

}
