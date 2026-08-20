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
package org.miaixz.bus.auth.vendor.baidu;

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
 * Declares the frozen Baidu OAuth 2.0 browser Vendor definition.
 * <p>
 * Only the authorization endpoint is published as a standard OAuth operation. Baidu's query-authenticated GET token
 * exchange and profile resource remain private Source-authentication steps because their wire responses cannot be
 * represented as standard token or UserInfo responses.
 * </p>
 *
 * @author Kimi Liu
 */
public final class BaiduDefinition implements VendorDefinition<BaiduSourceSettings> {

    /**
     * Stable platform routing identifier shared by catalog and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("baidu");

    /**
     * Internal identifier of the sole Baidu browser authorization variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Baidu token endpoint authentication identifier retained outside the standard OAuth allow-list.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact public operations supported by one compiled Baidu Source.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION));

    /**
     * Exact Baidu wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.CLIENT_SECRET,
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "token_response_without_token_type",
                    OAuth2.Parameters.TOKEN_TYPE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Http.Header.AUTHORIZATION,
                    Optional.empty(),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable endpoint, client policy, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("basic"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://openapi.baidu.com/oauth/2.0/authorize",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed("https://openapi.baidu.com/oauth/2.0/token", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://openapi.baidu.com/rest/2.0/passport/users/getInfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed("https://openapi.baidu.com/oauth/2.0/token", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless Baidu definition used by Vendor directory assembly.
     */
    public BaiduDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the operation
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
     * Creates one immutable Baidu wire deviation.
     *
     * @param operation    exact affected operation
     * @param location     exact wire location
     * @param vendorName   Baidu field or representation name
     * @param standardName corresponding standard field name
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the platform wraps the response
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
     * Returns the stable platform identifier used to select this definition.
     *
     * @return stable Baidu routing identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Baidu presentation metadata.
     *
     * @return immutable Baidu management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Baidu", "Baidu account OAuth authorization", "baidu");
    }

    /**
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return Baidu Source settings class
     */
    @Override
    public Class<BaiduSourceSettings> settingsType() {
        return BaiduSourceSettings.class;
    }

    /**
     * Returns the sole supported Baidu variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default Baidu definition.
     *
     * @param variant requested variant
     * @return exact Baidu definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Baidu Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
