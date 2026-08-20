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
package org.miaixz.bus.auth.vendor.taobao;

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
 * Declares the Taobao OAuth 2.0 browser Vendor manifest.
 * <p>
 * Public operations retain RFC 6749 authorization and token request/result models. Taobao's {@code view} parameter,
 * query-bearing empty-form token POST, identity-bearing token extensions, and encoded nickname remain exact Vendor
 * deviations handled by the platform adapter.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TaobaoManifest implements VariantManifest<TaobaoOptions> {

    /**
     * Stable Taobao platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("taobao");

    /**
     * Stable identifier of the sole Taobao variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Taobao client-secret authentication carried in token query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by Taobao.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN));

    /**
     * Exact historical Taobao wire deviations isolated from the OAuth protocol package.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "view",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    "client_id/client_secret/code/grant_type/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "taobao_user_id/taobao_open_uid/taobao_user_nick/id_token",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false));

    /**
     * Complete immutable Taobao endpoint, client, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of(),
            new VendorTargets(
                    Optional.of(
                            fixed("https://oauth.taobao.com/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://oauth.taobao.com/token", Http.Method.POST, CLIENT_SECRET_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Taobao manifest used by Vendor directory assembly.
     */
    public TaobaoManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed Taobao HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         operation HTTP method
     * @param authentication endpoint authentication declaration
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
     * Creates one exact Taobao deviation declaration.
     *
     * @param operation    affected public operation
     * @param location     exact wire location
     * @param vendorName   exact Taobao representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    response media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Taobao wraps the response
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
     * Returns the stable Taobao routing identifier.
     *
     * @return Taobao platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Taobao presentation metadata.
     *
     * @return immutable Taobao management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Taobao", "Taobao account authorization", "taobao");
    }

    /**
     * Returns the sole supported Taobao variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default Taobao manifest.
     *
     * @param variant requested Taobao variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Taobao Vendor variant is not supported");
        }
        return VARIANT;
    }

}
