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
package org.miaixz.bus.auth.source.vendor.huawei;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
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
 * Declares the frozen Huawei Account Kit OpenID Connect Vendor manifest.
 * <p>
 * The manifest separates standard OpenID Connect request and response models from Huawei's documented form-post,
 * numeric-error, proprietary profile, and revocation wire differences. Runtime endpoint values remain immutable and
 * cannot be supplied by external registration data.
 * </p>
 *
 * @author Kimi Liu
 */
public class HuaweiManifest implements VendorManifest<HuaweiOptions> {

    /**
     * Stable Huawei platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("huawei");

    /**
     * Internal identifier of the only supported Huawei web variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact public operations exposed by a compiled Huawei Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OAuth2ClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET));

    /**
     * Complete immutable manifest for Huawei's default web Authorization Code Flow.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VendorManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("openid", "profile", "email"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/oauth2/v3/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://account.cloud.huawei.com/rest.php?nsp_svc=GOpen.User.getInfo",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/oauth2/v3/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/oauth2/v3/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth-login.cloud.huawei.com/oauth2/v3/certs",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            "authentication",
                            VendorDeviation.Location.QUERY,
                            "response_mode",
                            Optional.of("response_mode"),
                            Optional.empty(),
                            Http.Method.GET,
                            false),
                    deviation(
                            "authentication",
                            VendorDeviation.Location.RESPONSE,
                            JwtClaims.ISSUER,
                            Optional.of(JwtClaims.ISSUER),
                            Optional.empty(),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "supportAlg",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.JSON,
                            OAuth2.Parameters.ERROR,
                            Optional.of(OAuth2.Parameters.ERROR),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.JSON,
                            "sub_error",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "jwks",
                            VendorDeviation.Location.RESPONSE,
                            MediaType.APPLICATION_JSON,
                            Optional.of(MediaType.APPLICATION_JWK_SET_JSON),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.GET,
                            false),
                    deviation(
                            "profile",
                            VendorDeviation.Location.FORM,
                            OAuth2.Parameters.ACCESS_TOKEN,
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "profile",
                            VendorDeviation.Location.HEADER,
                            "NSP_STATUS",
                            Optional.empty(),
                            Optional.empty(),
                            Http.Method.POST,
                            false),
                    deviation(
                            "revoke",
                            VendorDeviation.Location.FORM,
                            OAuth2.Parameters.TOKEN,
                            Optional.of(OAuth2.Parameters.TOKEN),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "revoke",
                            VendorDeviation.Location.JSON,
                            OAuth2.Parameters.ERROR,
                            Optional.of(OAuth2.Parameters.ERROR),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false),
                    deviation(
                            "revoke",
                            VendorDeviation.Location.RESPONSE,
                            "{}",
                            Optional.empty(),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE),
                            Http.Method.POST,
                            false)));

    /**
     * Creates the stateless Huawei manifest used by Vendor directory assembly.
     */
    public HuaweiManifest() {
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
     * Creates one immutable exact Huawei wire deviation.
     *
     * @param operation    affected operation
     * @param location     affected wire location
     * @param vendorName   exact Huawei field or representation
     * @param standardName corresponding standard field when present
     * @param mediaType    exact media type when relevant
     * @param method       exact HTTP method
     * @param enveloped    whether Huawei wraps the response
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
     * Returns the stable Huawei routing identifier.
     *
     * @return Huawei platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Huawei catalog presentation metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Huawei", "Huawei Account Kit OpenID Connect sign-in", "huawei");
    }

    /**
     * Returns the immutable single Huawei variant manifest.
     *
     * @return single-element variant list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the sole supported Huawei variant.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Huawei Vendor variant is not supported");
        }
        return VARIANT;
    }

}
