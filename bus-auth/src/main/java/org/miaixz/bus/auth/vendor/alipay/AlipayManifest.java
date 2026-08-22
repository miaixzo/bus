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
package org.miaixz.bus.auth.vendor.alipay;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
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
 * Declares the frozen Alipay RSA2 gateway authentication Vendor manifest.
 *
 * @author Kimi Liu
 */
public class AlipayManifest implements VariantManifest<AlipayOptions> {

    /**
     * Stable routing identifier used to select the Alipay Vendor manifest.
     */
    public static final Vendor.Id ID = new Vendor.Id("alipay");
    /**
     * Stable variant identifier for Alipay public-application authorization.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);
    /**
     * Browser-only application capability manifest; the proprietary gateway is not published as OAuth 2.0.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT))));
    /**
     * Exact gateway deviations that prevent the Alipay flow from being advertised as standard OAuth 2.0.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    "app_id",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "auth_code",
                    OAuth2.Parameters.CODE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "alipay.system.oauth.token",
                    VendorDeviation.Location.FORM,
                    "sign",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "alipay.system.oauth.token",
                    VendorDeviation.Location.RESPONSE,
                    "alipay_system_oauth_token_response",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "alipay.user.info.share",
                    VendorDeviation.Location.FORM,
                    "auth_token",
                    Http.Header.AUTHORIZATION,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "alipay.user.info.share",
                    VendorDeviation.Location.RESPONSE,
                    "alipay_user_info_share_response",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));
    /**
     * Complete immutable manifest for the frozen Alipay public-application flow.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.HTTPS,
            VariantManifest.Pkce.DISABLED, Credential.Type.PRIVATE_KEY, List.of("auth_user"),
            new VendorTargets(
                    Optional.of(fixed("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm", Http.Method.GET)),
                    Optional.of(fixed("https://openapi.alipay.com/gateway.do", Http.Method.POST)),
                    Optional.of(fixed("https://openapi.alipay.com/gateway.do", Http.Method.POST)), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Alipay manifest that exposes its immutable gateway manifest.
     */
    public AlipayManifest() {
        // No initialization required.
    }

    /**
     * Creates one fixed unauthenticated HTTPS endpoint.
     *
     * @param value  endpoint URL
     * @param method HTTP method
     * @return fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(final String value, final Http.Method method) {
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(Endpoint.Authentication.NONE), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable platform deviation with an optional corresponding standard field name.
     *
     * @param operation    exact affected platform or application operation
     * @param location     wire location containing the deviation
     * @param vendorName   exact Alipay field or envelope name
     * @param standardName corresponding standard field, or {@code null} when none exists
     * @param mediaType    exact representation when relevant
     * @param method       exact HTTP method
     * @param enveloped    whether the operation uses an Alipay response envelope
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
     * Returns the one-time Alipay client form including its public verification-key identifier.
     *
     * @return immutable Alipay client configuration form
     */
    @Override
    public Scheme.Form form() {
        return VariantManifest.Forms.extended(
                false,
                List.of(
                        VariantManifest.Forms.field(
                                "verificationKeyId",
                                "Alipay verification key identifier",
                                Scheme.Form.Type.TEXT,
                                true)));
    }

    /**
     * Returns the immutable platform routing identifier.
     *
     * @return stable Alipay identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns presentation metadata for management catalogs without exposing gateway credentials.
     *
     * @return immutable Alipay management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Alipay", "Alipay public application identity authorization", "alipay");
    }

    /**
     * Returns the sole supported Alipay public-application variant.
     *
     * @return immutable single-element variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Returns the exact Alipay manifest.
     *
     * @param variant requested variant
     * @return default manifest
     * @throws ValidateException if another variant is requested
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant))
            throw new ValidateException("Alipay Vendor variant is not supported");
        return VARIANT;
    }

}
