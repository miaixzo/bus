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
package org.miaixz.bus.auth.source.vendor.linkedin;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorDeviation;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen current LinkedIn OpenID Connect Vendor manifest.
 * <p>
 * The manifest represents LinkedIn's replacement OIDC product and deliberately excludes the retired
 * {@code r_liteprofile}, {@code r_emailaddress}, {@code /v2/me}, email projection, and pseudo-refresh behavior. Token
 * exchange remains private to Source authentication until LinkedIn's complete standard success and error wire is
 * frozen; public operations are limited to Authentication, JWK Set, and UserInfo.
 * </p>
 *
 * @author Kimi Liu
 */
public class LinkedInManifest implements VendorManifest<LinkedInOptions> {

    /**
     * Stable LinkedIn platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("linkedin");

    /**
     * Stable identifier of the sole supported current LinkedIn OIDC variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact public operations supported by the compiled LinkedIn Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OpenIdClientScheme.JWK_SET,
            OpenIdClientScheme.USERINFO));

    /**
     * Complete immutable current LinkedIn endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("openid", "profile", "email"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://www.linkedin.com/oauth/v2/authorization",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://www.linkedin.com/oauth/v2/accessToken",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://api.linkedin.com/v2/userinfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://www.linkedin.com/oauth/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://www.linkedin.com/oauth/openid/jwks",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            Builder.SOURCE_AUTHENTICATION_COMPLETE,
                            VendorDeviation.Location.RESPONSE,
                            "retired OAuth Sign In replaced by OpenID Connect",
                            Optional.of("OpenID Connect current product"),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "client_secret_post replaces historical query secret",
                            Optional.of(OAuth2.Parameters.CLIENT_SECRET),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            OpenIdConnect.Parameters.ID_TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "https://www.linkedin.com",
                            Optional.of("iss=https://www.linkedin.com/oauth"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "discovery",
                            VendorDeviation.Location.RESPONSE,
                            "missing response_modes_supported/grant_types_supported/token_endpoint_auth_methods_supported",
                            Optional.of("OpenID Provider Metadata"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.GET,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "private incomplete token evidence",
                            Optional.of("TokenResponse"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.JSON,
                            "invalid_redirect_uri",
                            Optional.of("invalid_request"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.RESPONSE,
                            "retired pseudo refresh endpoint removed",
                            Optional.empty(),
                            Optional.empty(),
                            Http.Method.POST,
                            false)));

    /**
     * Creates the stateless current LinkedIn manifest used by Vendor directory assembly.
     */
    public LinkedInManifest() {
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
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable exact LinkedIn migration or wire-deviation declaration.
     *
     * @param operation    affected operation
     * @param location     affected wire location
     * @param vendorName   exact LinkedIn representation
     * @param standardName corresponding standard representation when present
     * @param mediaType    exact media type when relevant
     * @param method       exact HTTP method
     * @param enveloped    whether LinkedIn wraps the response
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
     * Returns the stable LinkedIn routing identifier.
     *
     * @return LinkedIn platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive LinkedIn catalog metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("LinkedIn", "LinkedIn OpenID Connect sign-in", "linkedin");
    }

    /**
     * Returns the immutable single LinkedIn variant manifest.
     *
     * @return single-element variant list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the sole supported current LinkedIn variant.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("LinkedIn Vendor variant is not supported");
        }
        return VARIANT;
    }

}
