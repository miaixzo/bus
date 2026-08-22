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
package org.miaixz.bus.auth.vendor.apple;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen Sign in with Apple OpenID Connect Vendor manifest.
 *
 * @author Kimi Liu
 */
public class AppleManifest implements VariantManifest<AppleOptions> {

    /**
     * Stable platform routing identifier shared by registration, catalog, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("apple");

    /**
     * Internal identifier of the single Sign in with Apple variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact public operations supported by the compiled Sign in with Apple Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.TOKEN,
            OpenIdClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET));

    /**
     * Dynamic client-secret semantics applied to the otherwise standard token and revocation form fields.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            new VendorDeviation(Builder.SOURCE_AUTHENTICATION_INITIATE, VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.SCOPE, Optional.of(OAuth2.Parameters.SCOPE), Optional.empty(), Http.Method.GET,
                    false),
            new VendorDeviation(OAuth2.Parameters.TOKEN, VendorDeviation.Location.FORM, OAuth2.Parameters.CLIENT_SECRET,
                    Optional.of(OAuth2.Parameters.CLIENT_SECRET),
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE), Http.Method.POST, false),
            new VendorDeviation("revoke", VendorDeviation.Location.FORM, OAuth2.Parameters.CLIENT_SECRET,
                    Optional.of(OAuth2.Parameters.CLIENT_SECRET),
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE), Http.Method.POST, false));

    /**
     * Complete immutable endpoint, policy, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VariantManifest.Pkce.DISABLED, Credential.Type.PRIVATE_KEY, List.of("name", "email"),
            new VendorTargets(Optional.of(
                    fixed("https://appleid.apple.com/auth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://appleid.apple.com/auth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://appleid.apple.com/auth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://appleid.apple.com/auth/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://appleid.apple.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://appleid.apple.com/auth/keys",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Sign in with Apple manifest used by Vendor directory assembly.
     */
    public AppleManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the operation
     * @param authentication endpoint authentication method
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the one-time Apple client form including Developer Team and signing-key identifiers.
     *
     * @return immutable Sign in with Apple client configuration form
     */
    @Override
    public Scheme.Form form() {
        return VariantManifest.Forms.extended(
                false,
                List.of(
                        VariantManifest.Forms
                                .field("teamId", "Apple Developer Team identifier", Scheme.Form.Type.TEXT, true),
                        VariantManifest.Forms
                                .field("keyId", "Apple signing key identifier", Scheme.Form.Type.TEXT, true)));
    }

    /**
     * Returns the stable platform identifier used to select this manifest.
     *
     * @return stable Apple routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Sign in with Apple presentation metadata.
     *
     * @return immutable Apple presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Apple", "Sign in with Apple OpenID Connect", "apple");
    }

    /**
     * Returns the sole supported Sign in with Apple variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact default manifest.
     *
     * @param variant requested variant
     * @return exact Sign in with Apple manifest
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Sign in with Apple Vendor variant is not supported");
        }
        return VARIANT;
    }

}
