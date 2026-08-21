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
package org.miaixz.bus.auth.vendor.douyin;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.SourceWorkflow;
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
 * Declares Douyin's open-platform browser flow and ordinary mini-program code-to-session flow.
 * <p>
 * The open variant uses OAuth 2.0 authorization semantics but keeps every platform-specific request and envelope inside
 * Source authentication. The mini-program variant is proprietary direct authentication and never represents its session
 * key as an OAuth token.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DouyinManifest implements VariantManifest<DouyinOptions> {

    /**
     * Stable platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("douyin");

    /**
     * Stable open-platform browser variant identifier.
     */
    public static final Vendor.Variant OPEN = new Vendor.Variant("open");

    /**
     * Stable ordinary mini-program direct variant identifier.
     */
    public static final Vendor.Variant MINI_PROGRAM = new Vendor.Variant("mini-program");

    /**
     * Douyin's registered form client-key/client-secret authentication method.
     */
    private static final Endpoint.Authentication CLIENT_KEY_SECRET_FORM = new Endpoint.Authentication(
            "client_key_secret_form");

    /**
     * Exact browser Source-authentication manifest of the open variant.
     */
    private static final Capability.Manifest OPEN_CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT))));

    /**
     * Exact direct one-time-code Source-authentication manifest of the mini-program variant.
     */
    private static final Capability.Manifest MINI_CAPABILITIES = new Capability.Manifest(
            List.of(SourceWorkflow.initiate(Set.of(Capability.Interaction.DIRECT))));

    /**
     * Exact open-platform wire deviations retained only inside Source authentication.
     */
    private static final List<VendorDeviation> OPEN_DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    "client_key",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.SCOPE,
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.FORM,
                    "client_key",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "data",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.FORM,
                    "open_id",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Exact mini-program wire deviations retained inside direct Source authentication.
     */
    private static final List<VendorDeviation> MINI_DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.JSON,
                    "appid",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.JSON,
                    "secret",
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.RESPONSE,
                    "data",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Complete immutable open-platform manifest.
     */
    private static final VariantManifest.Variant OPEN_VARIANT = new VariantManifest.Variant(ID, OPEN, Protocol.OAUTH2,
            List.of("user_info"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://open.douyin.com/platform/oauth/connect/",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://open.douyin.com/oauth/access_token/",
                                    Http.Method.POST,
                                    CLIENT_KEY_SECRET_FORM)),
                    Optional.of(
                            fixed(
                                    "https://open.douyin.com/oauth/userinfo/",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://open.douyin.com/oauth/refresh_token/",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            OPEN_CAPABILITIES, OPEN_DEVIATIONS);

    /**
     * Complete immutable ordinary mini-program manifest.
     */
    private static final VariantManifest.Variant MINI_VARIANT = new VariantManifest.Variant(ID, MINI_PROGRAM,
            Protocol.VENDOR_AUTH, List.of(),
            new VendorTargets(Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://developer.toutiao.com/api/apps/v2/jscode2session",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty()),
            MINI_CAPABILITIES, MINI_DEVIATIONS);

    /**
     * Creates the stateless Douyin manifest used by Vendor directory assembly.
     */
    public DouyinManifest() {
        // No initialization required.
        // All manifest state is held by immutable class constants.
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
     * Creates one immutable registered Douyin wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact Douyin field or envelope name
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
     * Returns the stable Douyin routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Douyin presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Douyin", "Douyin open-platform and mini-program login", "douyin");
    }

    /**
     * Returns both variants in stable management order.
     *
     * @return open then mini-program manifests
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(OPEN_VARIANT, MINI_VARIANT);
    }

    /**
     * Returns the unique selected variant manifest.
     *
     * @param variant requested variant
     * @return exact immutable manifest
     * @throws ValidateException if the variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (OPEN.equals(variant)) {
            return OPEN_VARIANT;
        }
        if (MINI_PROGRAM.equals(variant)) {
            return MINI_VARIANT;
        }
        throw new ValidateException("Douyin Vendor variant is not supported");
    }

}
