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
package org.miaixz.bus.auth.vendor.twitter;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1SourceProfile;
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
 * Declares the Twitter OAuth 1.0a browser Vendor definition.
 * <p>
 * Temporary credentials, resource-owner authorization, token credentials, and protected-resource access remain RFC 5849
 * operations. Only the Twitter profile request fields and JSON identity projection are platform-specific.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TwitterDefinition implements VendorDefinition<TwitterSourceSettings> {

    /**
     * Stable Twitter platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("twitter");

    /**
     * Stable identifier of the sole Twitter OAuth 1.0a variant.
     */
    public static final Vendor.Variant OAUTH1 = new Vendor.Variant("oauth1");

    /**
     * OAuth 1.0 Authorization-header authentication declaration.
     */
    private static final Endpoint.Authentication OAUTH1_HEADER = new Endpoint.Authentication("oauth1");

    /**
     * Exact Source authentication and RFC 5849 operations exposed by Twitter.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth1SourceProfile.TEMPORARY_CREDENTIALS,
            OAuth1SourceProfile.RESOURCE_OWNER_AUTHORIZATION,
            OAuth1SourceProfile.TOKEN_CREDENTIALS,
            OAuth1SourceProfile.PROTECTED_RESOURCE));

    /**
     * Exact Twitter profile request and response adaptations used only by Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "include_entities/include_email",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "Twitter user JSON",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable Twitter endpoint, client, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, OAUTH1,
            Protocol.OAUTH1, List.of(),
            new VendorTargets(Optional.of(
                    fixed("https://api.twitter.com/oauth/authenticate", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://api.twitter.com/oauth/request_token", Http.Method.POST, OAUTH1_HEADER)),
                    Optional.of(fixed("https://api.twitter.com/oauth/access_token", Http.Method.POST, OAUTH1_HEADER)),
                    Optional.of(
                            fixed(
                                    "https://api.twitter.com/1.1/account/verify_credentials.json",
                                    Http.Method.GET,
                                    OAUTH1_HEADER)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Twitter definition used by Vendor directory assembly.
     */
    public TwitterDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed Twitter HTTPS endpoint.
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
     * Creates one exact Twitter deviation declaration.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact Twitter representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    response media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Twitter wraps the response
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
     * Returns the stable Twitter routing identifier.
     *
     * @return Twitter platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Twitter presentation metadata.
     *
     * @return immutable Twitter management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Twitter", "Twitter OAuth 1.0 account authorization", "twitter");
    }

    /**
     * Returns the exact externally decoded settings record.
     *
     * @return Twitter Source settings class
     */
    @Override
    public Class<TwitterSourceSettings> settingsType() {
        return TwitterSourceSettings.class;
    }

    /**
     * Returns the sole supported Twitter variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the exact Twitter OAuth 1.0a definition.
     *
     * @param variant requested Twitter variant
     * @return immutable OAuth 1.0a definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!OAUTH1.equals(variant)) {
            throw new ValidateException("Twitter Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
