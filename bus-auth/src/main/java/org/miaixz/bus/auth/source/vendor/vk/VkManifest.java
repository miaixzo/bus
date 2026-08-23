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
package org.miaixz.bus.auth.source.vendor.vk;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
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
 * Declares the VK ID OAuth 2.0 Vendor manifest with mandatory S256 PKCE.
 * <p>
 * Authorization remains a standard OAuth operation. Token, refresh, revocation, and current-user processing retain
 * standard public request and response models while the adapter owns VK's device binding, response extensions,
 * revocation marker, and private profile envelope.
 * </p>
 *
 * @author Kimi Liu
 */
public class VkManifest implements VendorManifest<VkOptions> {

    /**
     * Stable VK platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("vk");

    /**
     * Stable identifier of the sole VK ID variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Exact Source authentication and public OAuth capabilities of VK ID.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN,
            OAuth2ClientScheme.REVOCATION));

    /**
     * Exact VK wire differences handled by the platform adapter.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
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
                    "access_token/client_id",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "user",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.FORM,
                    "state/device_id/client_id",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "error/message/user_id",
                    "OAuth token response with extensions",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "revoke",
                    VendorDeviation.Location.FORM,
                    "access_token/client_id",
                    OAuth2.Parameters.TOKEN,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "revoke",
                    VendorDeviation.Location.RESPONSE,
                    "response=1",
                    "empty successful response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Complete immutable VK endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VendorManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("vkid.personal_info", "email"),
            new VendorTargets(
                    Optional.of(fixed("https://id.vk.com/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://id.vk.com/oauth2/auth", Http.Method.POST, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://id.vk.com/oauth2/user_info",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://id.vk.com/oauth2/auth", Http.Method.POST, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed("https://id.vk.com/oauth2/revoke", Http.Method.POST, Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless VK manifest used by Vendor directory assembly.
     */
    public VkManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed credential-free VK HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact operation HTTP method
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
     * Creates one exact VK wire-deviation declaration.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   VK field or representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    exact representation media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether VK wraps the response
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
     * Returns the stable VK routing identifier.
     *
     * @return VK platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive VK ID presentation metadata.
     *
     * @return immutable VK management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("VK", "VK ID OAuth 2.0 authorization", "vk");
    }

    /**
     * Returns the sole supported VK variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default VK manifest.
     *
     * @param variant requested VK variant
     * @return immutable VK manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("VK Vendor variant is not supported");
        }
        return VARIANT;
    }

}
