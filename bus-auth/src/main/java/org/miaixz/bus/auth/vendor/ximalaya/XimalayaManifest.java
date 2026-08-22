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
package org.miaixz.bus.auth.vendor.ximalaya;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Scheme;
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

/**
 * Declares the immutable Ximalaya OAuth 2.0 browser-login manifest.
 * <p>
 * Authorization remains a public RFC 6749 operation. Ximalaya's token response omits {@code token_type}, while its
 * profile resource requires device, package, access-token, and proprietary signature query parameters. Those two
 * operations therefore remain private steps of Source authentication and never become protocol Token or UserInfo
 * responses.
 * </p>
 *
 * @author Kimi Liu
 */
public class XimalayaManifest implements VariantManifest<XimalayaOptions> {

    /**
     * Stable Ximalaya platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("ximalaya");
    /**
     * Sole Ximalaya browser variant identifier.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);
    /**
     * Standard client-secret form authentication used by the token endpoint.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_POST = new Endpoint.Authentication("client_secret_post");
    /**
     * Ximalaya profile authentication carried by a signed query.
     */
    private static final Endpoint.Authentication SIGNED_PROFILE_QUERY = new Endpoint.Authentication(
            "ximalaya_signed_query");
    /**
     * Exact Source authentication and OAuth authorization capabilities.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));
    /**
     * Exact Ximalaya wire deviations confined to its private adapter.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "client_os_type",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "device_id",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "device_id",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.FORM,
                    "device_id",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "access_token/refresh_token/expires_in/uid/device_id/scope without token_type",
                    "OAuth token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "access_token/app_key/client_os_type/device_id/pack_id/sig",
                    "Bearer resource request",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "errcode/error_no/error_desc or error_no/error_code/error_desc/service",
                    "OAuth error response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "id/nickname/avatar_url",
                    "External identity",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));
    /**
     * Complete immutable Ximalaya endpoint, client, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VariantManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of(),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://api.ximalaya.com/oauth2/js/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://api.ximalaya.com/oauth2/v2/access_token",
                                    Http.Method.POST,
                                    CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed("https://api.ximalaya.com/profile/user_info", Http.Method.GET, SIGNED_PROFILE_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Ximalaya manifest used by Vendor directory assembly.
     */
    public XimalayaManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed Ximalaya HTTPS endpoint.
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
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one exact Ximalaya deviation declaration.
     *
     * @param operation    affected public or Source operation
     * @param location     exact wire location
     * @param vendorName   exact Ximalaya representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    exact representation media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Ximalaya wraps the response
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
     * Returns the one-time Ximalaya client form including device and application selectors.
     *
     * @return immutable Ximalaya client configuration form
     */
    @Override
    public Scheme.Form form() {
        return VariantManifest.Forms.extended(
                false,
                List.of(
                        VariantManifest.Forms.field("deviceId", "Device identifier", Scheme.Form.Type.TEXT, true),
                        VariantManifest.Forms
                                .field("clientOsType", "Client operating-system type", Scheme.Form.Type.TEXT, true),
                        VariantManifest.Forms.field("packageId", "Package identifier", Scheme.Form.Type.TEXT, true)));
    }

    /**
     * Returns the stable Ximalaya routing identifier.
     *
     * @return Ximalaya platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Ximalaya presentation metadata.
     *
     * @return immutable Ximalaya management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Ximalaya", "Ximalaya account authorization", "ximalaya");
    }

    /**
     * Returns the sole supported Ximalaya variant.
     *
     * @return immutable singleton manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default Ximalaya manifest.
     *
     * @param variant requested Ximalaya variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        throw new ValidateException("Ximalaya Vendor variant is not supported");
    }

    /**
     * Defines Ximalaya-specific wire parameter and response member names that are not owned by OAuth 2.0.
     *
     * @author Kimi Liu
     */
    public static class Parameters {

        /**
         * Ximalaya client operating-system type parameter name.
         */
        public static final String CLIENT_OS_TYPE = "client_os_type";

        /**
         * Ximalaya registered device identifier parameter name.
         */
        public static final String DEVICE_ID = "device_id";

        /**
         * Ximalaya package identifier parameter name.
         */
        public static final String PACKAGE_ID = "package_id";

        /**
         * Creates a Ximalaya request-parameter namespace instance.
         */
        public Parameters() {
            // No initialization required.
        }

    }

}
