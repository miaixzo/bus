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
package org.miaixz.bus.auth.vendor.pinterest;

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
 * Declares the preserved Pinterest OAuth 2.0 browser Vendor definition.
 * <p>
 * Standard OAuth request and response models remain public. Pinterest's comma-delimited scope, query token parameters,
 * empty form body, status envelope, and query profile transport are registered as exact Vendor deviations and remain
 * private to its adapter.
 * </p>
 *
 * @author Kimi Liu
 */
public final class PinterestDefinition implements VendorDefinition<PinterestSourceSettings> {

    /**
     * Stable Pinterest platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("pinterest");

    /**
     * Stable identifier of the preserved Pinterest variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Pinterest token authentication carried in the request query.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by Pinterest.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN));

    /**
     * Exact historical Pinterest wire deviations isolated from the OAuth protocol package.
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
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    "authorization-code fields",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.CLIENT_SECRET,
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.FORM,
                    "empty form body",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "status/message envelope",
                    "OAuth response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Http.Header.AUTHORIZATION,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "fields",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "status/message/data envelope",
                    "profile response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable Pinterest endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("read_public"),
            new VendorTargets(
                    Optional.of(
                            fixed("https://api.pinterest.com/oauth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed("https://api.pinterest.com/v1/oauth/token", Http.Method.POST, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://api.pinterest.com/v1/me", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Pinterest definition used by Vendor directory assembly.
     */
    public PinterestDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed Pinterest HTTPS endpoint.
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
     * Creates one immutable Pinterest wire deviation.
     *
     * @param operation    exact affected operation
     * @param location     exact wire location
     * @param vendorName   Pinterest field or representation name
     * @param standardName corresponding standard field or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the response uses a wrapper object
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
     * Returns the stable Pinterest routing identifier.
     *
     * @return Pinterest platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Pinterest presentation metadata.
     *
     * @return immutable Pinterest management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Pinterest", "Pinterest account authorization", "pinterest");
    }

    /**
     * Returns the exact externally decoded settings record.
     *
     * @return Pinterest Source settings class
     */
    @Override
    public Class<PinterestSourceSettings> settingsType() {
        return PinterestSourceSettings.class;
    }

    /**
     * Returns the sole preserved Pinterest variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Resolves the exact default Pinterest definition.
     *
     * @param variant requested Pinterest variant
     * @return immutable default definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Pinterest Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
