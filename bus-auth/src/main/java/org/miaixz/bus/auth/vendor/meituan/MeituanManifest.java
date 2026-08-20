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
package org.miaixz.bus.auth.vendor.meituan;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
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
 * Declares the frozen Meituan Waimai OAuth 2.0 Vendor manifest.
 * <p>
 * Standard authorization remains Registry-visible. Token, refresh, and profile operations stay private because Meituan
 * renames client credentials, omits standard token members, and returns platform success and error objects through HTTP
 * 200 responses.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MeituanManifest implements VariantManifest<MeituanOptions> {

    /**
     * Stable Meituan platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("meituan");

    /**
     * Stable identifier of the sole supported Meituan Waimai variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Meituan endpoint authentication using {@code app_id} and {@code secret} form members.
     */
    private static final Endpoint.Authentication APP_ID_SECRET_FORM = new Endpoint.Authentication("app_id_secret_form");

    /**
     * Exact Registry-visible operations implemented by Meituan account login.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Complete immutable Meituan endpoint, client, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of(),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://openapi.waimai.meituan.com/oauth/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://openapi.waimai.meituan.com/oauth/access_token",
                                    Http.Method.POST,
                                    APP_ID_SECRET_FORM)),
                    Optional.of(
                            fixed(
                                    "https://openapi.waimai.meituan.com/oauth/userinfo",
                                    Http.Method.POST,
                                    APP_ID_SECRET_FORM)),
                    Optional.of(
                            fixed(
                                    "https://openapi.waimai.meituan.com/oauth/refresh_token",
                                    Http.Method.POST,
                                    APP_ID_SECRET_FORM)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    deviation(
                            "authorize",
                            VendorDeviation.Location.QUERY,
                            "scope=",
                            OAuth2.Parameters.SCOPE,
                            null,
                            Http.Method.GET,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "app_id",
                            OAuth2.Parameters.CLIENT_ID,
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "secret",
                            OAuth2.Parameters.CLIENT_SECRET,
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.FORM,
                            "app_id_secret_form",
                            "client_secret_post",
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "missing token_type and scope",
                            "token_type and scope",
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.TOKEN,
                            VendorDeviation.Location.RESPONSE,
                            "HTTP 200 error_code/error_msg envelope",
                            "OAuth error response",
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            true),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.FORM,
                            "app_id",
                            OAuth2.Parameters.CLIENT_ID,
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.FORM,
                            "secret",
                            OAuth2.Parameters.CLIENT_SECRET,
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.FORM,
                            "app_id_secret_form",
                            "client_secret_post",
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.RESPONSE,
                            "missing token_type and scope",
                            "token_type and scope",
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            "refresh",
                            VendorDeviation.Location.RESPONSE,
                            "HTTP 200 error_code/error_msg envelope",
                            "OAuth error response",
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            true),
                    deviation(
                            "profile",
                            VendorDeviation.Location.FORM,
                            "app_id/secret/access_token",
                            "Bearer authorization",
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            "profile",
                            VendorDeviation.Location.JSON,
                            "openid/nickname/avatar",
                            null,
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            false),
                    deviation(
                            OAuth2.Parameters.ERROR,
                            VendorDeviation.Location.JSON,
                            "error_msg",
                            "historical erroe_msg",
                            MediaType.APPLICATION_JSON_TYPE,
                            Http.Method.POST,
                            true)));

    /**
     * Creates the stateless Meituan manifest used by Vendor directory assembly.
     */
    public MeituanManifest() {
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
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable Meituan wire-deviation declaration.
     *
     * @param operation    affected operation
     * @param location     affected wire location
     * @param vendorName   exact Meituan representation
     * @param standardName corresponding standard representation or {@code null}
     * @param mediaType    exact media type or {@code null}
     * @param method       exact HTTP method
     * @param enveloped    whether Meituan wraps the representation
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final MediaType mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName),
                Optional.ofNullable(mediaType), method, enveloped);
    }

    /**
     * Returns the stable Meituan routing identifier.
     *
     * @return Meituan platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Meituan catalog metadata.
     *
     * @return immutable presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Meituan", "Meituan Waimai account authorization", "meituan");
    }

    /**
     * Returns the immutable single Meituan variant manifest.
     *
     * @return single-element variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the sole supported Meituan variant.
     *
     * @param variant requested variant
     * @return immutable default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Meituan Vendor variant is not supported");
        }
        return VARIANT;
    }

}
