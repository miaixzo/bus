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
package org.miaixz.bus.auth.vendor.line;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the frozen LINE Login OpenID Connect Vendor manifest.
 * <p>
 * Standard authentication, token, discovery, and JWK Set operations remain explicit. LINE's legacy {@code /v2/profile}
 * identity resource, web-channel HS256 ID Tokens, and {@code access_token} revocation form are isolated as registered
 * deviations without changing the standard OIDC or OAuth models.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LineManifest implements VariantManifest<LineOptions> {

    /**
     * Stable LINE platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("line");

    /**
     * Stable identifier of the sole supported LINE Login web variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact public operations supported by the compiled LINE Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OpenIdClientScheme.TOKEN,
            OpenIdClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET));

    /**
     * Complete immutable LINE web endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            List.of("profile", "openid"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://access.line.me/oauth2/v2.1/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://api.line.me/oauth2/v2.1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed("https://api.line.me/v2/profile", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://api.line.me/oauth2/v2.1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://api.line.me/oauth2/v2.1/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://access.line.me/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://api.line.me/oauth2/v2.1/certs",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            "profile",
                            VendorDeviation.Location.RESPONSE,
                            "https://api.line.me/v2/profile",
                            Optional.of("userinfo_endpoint"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.GET,
                            false),
                    deviation(
                            OpenIdConnect.Parameters.ID_TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "HS256 for web channels",
                            Optional.of("id_token_signing_alg_values_supported=ES256"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "authentication",
                            VendorDeviation.Location.QUERY,
                            "independent state and nonce",
                            Optional.of("state/nonce"),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "revoke",
                            VendorDeviation.Location.FORM,
                            OAuth2.Parameters.ACCESS_TOKEN,
                            Optional.of(OAuth2.Parameters.TOKEN),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "revoke",
                            VendorDeviation.Location.RESPONSE,
                            "empty body",
                            Optional.of("successful revocation response"),
                            Optional.empty(),
                            Http.Method.POST,
                            false)));

    /**
     * Creates the stateless LINE manifest used by Vendor directory assembly.
     */
    public LineManifest() {
        // No initialization required.
        // All manifest state is frozen in immutable constants.
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication method
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
     * Creates one immutable exact LINE wire-deviation declaration.
     *
     * @param operation    affected operation
     * @param location     affected wire location
     * @param vendorName   exact LINE field or representation
     * @param standardName corresponding standard field when present
     * @param mediaType    exact media type when relevant
     * @param method       exact HTTP method
     * @param enveloped    whether LINE wraps the response
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final Optional<String> standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, standardName, mediaType, method, enveloped);
    }

    /**
     * Returns the stable LINE routing identifier.
     *
     * @return LINE platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive LINE catalog metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("LINE", "LINE Login OpenID Connect sign-in", "line");
    }

    /**
     * Returns the immutable single LINE variant manifest.
     *
     * @return single-element variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the sole supported LINE variant.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("LINE Vendor variant is not supported");
        }
        return VARIANT;
    }

}
