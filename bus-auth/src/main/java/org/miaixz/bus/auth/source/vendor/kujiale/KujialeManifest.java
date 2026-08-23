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
package org.miaixz.bus.auth.source.vendor.kujiale;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
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
 * Declares the immutable Kujiale OAuth 2.0 Vendor manifest and its private account-resolution wire contract.
 * <p>
 * Only authorization is exposed as a standard OAuth operation. Token acquisition, refresh, OpenID lookup, and profile
 * retrieval remain private Source-authentication stages because Kujiale carries credentials in queries, posts empty
 * forms, omits {@code token_type}, and wraps results in its {@code c/m/d/f} envelope.
 * </p>
 *
 * @author Kimi Liu
 */
public class KujialeManifest implements VendorManifest<KujialeOptions> {

    /**
     * Stable Kujiale platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("kujiale");

    /**
     * Stable identifier of the sole supported Kujiale login variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Kujiale token-endpoint authentication that carries the client secret in the query.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Kujiale resource authentication that carries the access token in the query.
     */
    private static final Endpoint.Authentication ACCESS_TOKEN_QUERY = new Endpoint.Authentication("access_token_query");

    /**
     * Exact Roster-visible operations implemented by Kujiale login.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("get_user_info"),
            new VendorTargets(Optional
                    .of(fixed("https://oauth.kujiale.com/oauth2/show", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth.kujiale.com/oauth2/auth/token",
                                    Http.Method.POST,
                                    CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://oauth.kujiale.com/oauth2/openapi/user",
                                    Http.Method.GET,
                                    ACCESS_TOKEN_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://oauth.kujiale.com/oauth2/auth/token/refresh",
                                    Http.Method.POST,
                                    CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://oauth.kujiale.com/oauth2/auth/user", Http.Method.GET, ACCESS_TOKEN_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            "authorize",
                            VendorDeviation.Location.QUERY,
                            "comma-delimited scope",
                            Optional.of(OAuth2.Parameters.SCOPE),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.CLIENT_SECRET,
                            Optional.of(OAuth2.Parameters.CLIENT_SECRET),
                            Optional.empty(),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "empty form body",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.JSON,
                            "accessToken/refreshToken/expiresIn",
                            Optional.of("access_token/refresh_token/expires_in"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "missing token_type",
                            Optional.of(OAuth2.Parameters.TOKEN_TYPE),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "string c and c/m/d/f envelope",
                            Optional.of("OAuth error response"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.CLIENT_SECRET,
                            Optional.of(OAuth2.Parameters.CLIENT_SECRET),
                            Optional.empty(),
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.FORM,
                            "empty form body",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.JSON,
                            "accessToken/refreshToken/expiresIn and c/m/d/f envelope",
                            Optional.of("access_token/refresh_token/expires_in"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            true),
                    deviation(
                            "openid_lookup",
                            VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.ACCESS_TOKEN,
                            Optional.of("Bearer authorization"),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "openid_lookup",
                            VendorDeviation.Location.RESPONSE,
                            "non-RFC 7662 OpenID c/m/d/f envelope",
                            Optional.of("token introspection"),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.GET,
                            true),
                    deviation(
                            "profile",
                            VendorDeviation.Location.QUERY,
                            OAuth2.Parameters.ACCESS_TOKEN,
                            Optional.of("Bearer authorization"),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "profile",
                            VendorDeviation.Location.QUERY,
                            "open_id",
                            Optional.empty(),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "profile",
                            VendorDeviation.Location.JSON,
                            "userName/openId/avatar and c/m/d/f envelope",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.GET,
                            true)));

    /**
     * Creates the stateless Kujiale manifest used by Vendor directory assembly.
     */
    public KujialeManifest() {
        // No initialization required.
        // All manifest state is frozen in immutable constants.
    }

    /**
     * Creates one immutable fixed HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication method
     * @return immutable endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable Kujiale wire-deviation declaration.
     *
     * @param operation    affected operation
     * @param location     affected wire location
     * @param vendorName   exact Kujiale field or representation
     * @param standardName corresponding standard field when present
     * @param mediaType    exact media type when relevant
     * @param method       exact HTTP method
     * @param enveloped    whether Kujiale wraps the response
     * @return immutable deviation
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
     * Returns the stable Kujiale routing identifier.
     *
     * @return Kujiale platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Kujiale catalog metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Kujiale", "Kujiale account authorization", "kujiale");
    }

    /**
     * Returns the immutable single Kujiale variant manifest.
     *
     * @return single-element variant list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the sole supported Kujiale variant.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Kujiale Vendor variant is not supported");
        }
        return VARIANT;
    }

}
