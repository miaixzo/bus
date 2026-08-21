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
package org.miaixz.bus.auth.vendor.toutiao;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
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
 * Declares the Toutiao OAuth 2.0 browser Vendor manifest.
 * <p>
 * Only authorization is published as a standard OAuth operation. Toutiao's {@code client_key} mapping, authorization
 * extensions, token response without {@code token_type}, and enveloped profile remain private platform deviations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ToutiaoManifest implements VariantManifest<ToutiaoOptions> {

    /**
     * Stable Toutiao platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("toutiao");

    /**
     * Stable identifier of the sole Toutiao variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Toutiao client-secret authentication carried in token query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operation exposed by Toutiao.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact historical Toutiao private authentication deviations.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "client_key",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "auth_only/display",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "client_key/client_secret/code/grant_type",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "access_token/expires_in/open_id without token_type",
                    "OAuth access token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "client_key/access_token",
                    "Authorization header",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "error_code/data",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable Toutiao endpoint, client, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of(),
            new VendorTargets(Optional
                    .of(fixed("https://open.snssdk.com/auth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://open.snssdk.com/auth/token", Http.Method.POST, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://open.snssdk.com/data/user_profile",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Toutiao manifest used by Vendor directory assembly.
     */
    public ToutiaoManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed Toutiao HTTPS endpoint.
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
     * Creates one exact Toutiao deviation declaration.
     *
     * @param operation    affected public or Source operation
     * @param location     exact wire location
     * @param vendorName   exact Toutiao representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    response media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Toutiao wraps the response
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
     * Returns the stable Toutiao routing identifier.
     *
     * @return Toutiao platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Toutiao presentation metadata.
     *
     * @return immutable Toutiao management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Toutiao", "Toutiao account authorization", "toutiao");
    }

    /**
     * Returns the sole supported Toutiao variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default Toutiao manifest.
     *
     * @param variant requested Toutiao variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Toutiao Vendor variant is not supported");
        }
        return VARIANT;
    }

}
