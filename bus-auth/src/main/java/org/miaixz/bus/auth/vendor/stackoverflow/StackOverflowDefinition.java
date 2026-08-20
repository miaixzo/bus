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
package org.miaixz.bus.auth.vendor.stackoverflow;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
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
 * Declares the Stack Overflow OAuth 2.0 browser Vendor definition.
 * <p>
 * Only authorization is published as a standard RFC 6749 operation. Stack Overflow's comma-delimited scope,
 * query-bearing token request, token response without {@code token_type}, and Stack Exchange API envelope remain
 * private Source-authentication deviations and do not alter the protocol package.
 * </p>
 *
 * @author Kimi Liu
 */
public final class StackOverflowDefinition implements VendorDefinition<StackOverflowSourceSettings> {

    /**
     * Stable Stack Overflow platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("stackoverflow");

    /**
     * Stable identifier of the sole Stack Overflow variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Stack Overflow client-secret authentication carried in token request parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by Stack Overflow.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION));

    /**
     * Exact Stack Overflow wire deviations isolated from the OAuth protocol package.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "client_id/client_secret/code/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "access_token/expires without token_type",
                    "OAuth access token response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/key/site",
                    "Authorization header",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "items/error/error_description",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable Stack Overflow endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("read_inbox"),
            new VendorTargets(
                    Optional.of(
                            fixed("https://stackoverflow.com/oauth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://stackoverflow.com/oauth/access_token/json",
                                    Http.Method.POST,
                                    CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://api.stackexchange.com/2.2/me",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Stack Overflow definition used by Vendor directory assembly.
     */
    public StackOverflowDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed Stack Overflow HTTPS endpoint.
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
     * Creates one exact Stack Overflow deviation declaration.
     *
     * @param operation    affected public or Source operation
     * @param location     exact wire location
     * @param vendorName   exact Stack Overflow representation
     * @param standardName corresponding standard representation, if any
     * @param mediaType    response media type, if applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Stack Overflow wraps the response
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
     * Returns the stable Stack Overflow routing identifier.
     *
     * @return Stack Overflow platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Stack Overflow presentation metadata.
     *
     * @return immutable Stack Overflow management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Stack Overflow", "Stack Overflow account authorization", "stackoverflow");
    }

    /**
     * Returns the exact externally decoded settings record.
     *
     * @return Stack Overflow Source settings class
     */
    @Override
    public Class<StackOverflowSourceSettings> settingsType() {
        return StackOverflowSourceSettings.class;
    }

    /**
     * Returns the sole supported Stack Overflow variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the exact default Stack Overflow definition.
     *
     * @param variant requested Stack Overflow variant
     * @return immutable default definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Stack Overflow Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
