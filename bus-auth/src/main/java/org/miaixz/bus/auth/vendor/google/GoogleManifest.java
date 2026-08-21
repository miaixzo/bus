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
package org.miaixz.bus.auth.vendor.google;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
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
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the frozen Google OpenID Connect Vendor manifest.
 *
 * @author Kimi Liu
 */
public final class GoogleManifest implements VariantManifest<GoogleOptions> {

    /**
     * Stable platform routing identifier shared by registration, catalog, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("google");

    /**
     * Internal identifier of the single Google OpenID Connect variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact public operations supported by the compiled Google Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OpenIdClientScheme.TOKEN,
            OpenIdClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET,
            OpenIdClientScheme.USERINFO));

    /**
     * Complete immutable endpoint, client-policy, scope, capability, and form manifest for the default variant.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VariantManifest.Pkce.REQUIRED, List.of("openid", "profile", "email"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://accounts.google.com/o/oauth2/v2/auth",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://openidconnect.googleapis.com/v1/userinfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://accounts.google.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://www.googleapis.com/oauth2/v3/certs",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    new VendorDeviation(OpenIdConnect.Parameters.ID_TOKEN, VendorDeviation.Location.RESPONSE,
                            "accounts.google.com", Optional.of(JwtClaims.ISSUER),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.POST, false),
                    new VendorDeviation(OAuth2.Parameters.TOKEN, VendorDeviation.Location.RESPONSE, "error_subtype",
                            Optional.empty(), Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.POST, false),
                    new VendorDeviation("revoke", VendorDeviation.Location.FORM, OAuth2.Parameters.TOKEN,
                            Optional.of(OAuth2.Parameters.TOKEN),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE), Http.Method.POST, false)));

    /**
     * Creates the stateless Google manifest used by Vendor directory assembly.
     */
    public GoogleManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the standard operation
     * @param authentication endpoint authentication
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable platform identifier used to select this manifest.
     *
     * @return stable Google routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Google presentation metadata for external management catalogs.
     *
     * @return immutable Google presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Google", "Google OpenID Connect sign-in", "google");
    }

    /**
     * Returns the sole supported Google OpenID Connect variant.
     *
     * @return immutable single-element variant manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact default manifest.
     *
     * @param variant requested variant
     * @return exact default Google manifest
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Google Vendor variant is not supported");
        }
        return VARIANT;
    }

}
