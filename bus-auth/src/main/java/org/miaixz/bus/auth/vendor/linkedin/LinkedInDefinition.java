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
package org.miaixz.bus.auth.vendor.linkedin;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdSourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
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
 * Declares the frozen current LinkedIn OpenID Connect Vendor definition.
 * <p>
 * The definition represents LinkedIn's replacement OIDC product and deliberately excludes the retired
 * {@code r_liteprofile}, {@code r_emailaddress}, {@code /v2/me}, email projection, and pseudo-refresh behavior. Token
 * exchange remains private to Source authentication until LinkedIn's complete standard success and error wire is
 * frozen; public operations are limited to Authentication, JWK Set, and UserInfo.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LinkedInDefinition implements VendorDefinition<LinkedInSourceSettings> {

    /**
     * Stable LinkedIn platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("linkedin");

    /**
     * Stable identifier of the sole supported current LinkedIn OIDC variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Exact public operations supported by the compiled LinkedIn Source.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdSourceProfile.AUTHENTICATION,
            OpenIdSourceProfile.JWK_SET,
            OpenIdSourceProfile.USERINFO));

    /**
     * Complete immutable current LinkedIn endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OIDC, List.of("openid", "profile", "email"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://www.linkedin.com/oauth/v2/authorization",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(),
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
            MANIFEST,
            List.of(
                    deviation(
                            "source_authentication.complete",
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
     * Creates the stateless current LinkedIn definition used by Vendor directory assembly.
     */
    public LinkedInDefinition() {
        // No initialization required.
        // All definition state is frozen in immutable constants.
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
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive LinkedIn catalog metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("LinkedIn", "LinkedIn OpenID Connect sign-in", "linkedin");
    }

    /**
     * Returns the exact LinkedIn external settings type.
     *
     * @return LinkedIn settings class
     */
    @Override
    public Class<LinkedInSourceSettings> settingsType() {
        return LinkedInSourceSettings.class;
    }

    /**
     * Returns the immutable single LinkedIn variant definition.
     *
     * @return single-element variant list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the sole supported current LinkedIn variant.
     *
     * @param variant requested variant
     * @return immutable default definition
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("LinkedIn Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
