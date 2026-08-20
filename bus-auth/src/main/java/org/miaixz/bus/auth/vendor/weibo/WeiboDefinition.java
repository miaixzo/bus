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
package org.miaixz.bus.auth.vendor.weibo;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
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
 * Declares the immutable Weibo OAuth 2.0 browser-login definition.
 * <p>
 * Authorization remains a public standard operation. The query-bearing token response stays private to Source
 * authentication because it omits {@code token_type}; profile retrieval is a Weibo resource operation rather than
 * OpenID Connect UserInfo. Revocation accepts the standard request model but maps it to Weibo's registered GET query
 * and {@code result=true} response.
 * </p>
 *
 * @author Kimi Liu
 */
public final class WeiboDefinition implements VendorDefinition<WeiboSourceSettings> {

    /**
     * Stable Weibo platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("weibo");

    /**
     * Sole Weibo browser variant identifier.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Weibo client-secret authentication carried in token endpoint query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Weibo profile authentication using query token plus its historical OAuth2 authorization scheme.
     */
    private static final Endpoint.Authentication PROFILE_QUERY_AND_HEADER = new Endpoint.Authentication(
            "access_token_query_oauth2_header");

    /**
     * Weibo revocation authentication using an access-token query parameter.
     */
    private static final Endpoint.Authentication ACCESS_TOKEN_QUERY = new Endpoint.Authentication("access_token_query");

    /**
     * Exact Source authentication, authorization, and revocation capabilities.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.REVOCATION));

    /**
     * Exact Weibo wire deviations confined to the selected adapter.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "code/client_id/client_secret/grant_type/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.FORM,
                    "empty form body",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "access_token/uid/expires_in without token_type",
                    "token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/uid",
                    "Bearer profile request",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.HEADER,
                    "Authorization: OAuth2 uid=...&access_token=...",
                    "Authorization: Bearer",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "revoke",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    "token form field",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "revoke",
                    VendorDeviation.Location.RESPONSE,
                    "result=true",
                    "empty response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable Weibo definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("all"),
            new VendorTargets(Optional
                    .of(fixed("https://api.weibo.com/oauth2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed("https://api.weibo.com/oauth2/access_token", Http.Method.POST, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://api.weibo.com/2/users/show.json",
                                    Http.Method.GET,
                                    PROFILE_QUERY_AND_HEADER)),
                    Optional.empty(), Optional.empty(),
                    Optional.of(fixed("https://api.weibo.com/oauth2/revokeOIDC", Http.Method.GET, ACCESS_TOKEN_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Weibo definition used by Vendor directory assembly.
     */
    public WeiboDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed credential-free Weibo HTTPS endpoint.
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
     * Creates one immutable registered Weibo wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact Weibo field or representation name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the result uses a platform envelope
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName), mediaType,
                method, enveloped);
    }

    /**
     * Returns the stable Weibo routing identifier.
     *
     * @return Weibo platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Weibo presentation metadata.
     *
     * @return immutable Weibo management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Weibo", "Weibo OAuth login and profile access", "weibo");
    }

    /**
     * Returns the exact Weibo settings record.
     *
     * @return Weibo Source settings class
     */
    @Override
    public Class<WeiboSourceSettings> settingsType() {
        return WeiboSourceSettings.class;
    }

    /**
     * Returns the sole Weibo definition.
     *
     * @return immutable singleton variant list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the sole supported Weibo variant.
     *
     * @param variant requested variant
     * @return immutable Weibo definition
     * @throws ValidateException if the requested variant is not {@code default}
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return DEFINITION;
        }
        throw new ValidateException("Weibo Vendor variant is not supported");
    }

}
