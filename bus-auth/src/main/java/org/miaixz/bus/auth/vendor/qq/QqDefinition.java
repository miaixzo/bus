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
package org.miaixz.bus.auth.vendor.qq;

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
 * Declares QQ Open Platform browser login and QQ Mini Program direct authentication.
 * <p>
 * The open variant exposes standard OAuth authorization only. Its token response omits the mandatory
 * {@code token_type}, so token, OpenID, and profile documents remain private to Source authentication. The mini-program
 * variant is proprietary code-to-session authentication and never represents a session key as an OAuth token.
 * </p>
 *
 * @author Kimi Liu
 */
public final class QqDefinition implements VendorDefinition<QqSourceSettings> {

    /**
     * Stable QQ platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("qq");

    /**
     * Stable QQ Open Platform browser variant identifier.
     */
    public static final Vendor.Variant OPEN = new Vendor.Variant("open");

    /**
     * Stable QQ Mini Program direct variant identifier.
     */
    public static final Vendor.Variant MINI_PROGRAM = new Vendor.Variant("mini-program");

    /**
     * QQ client-secret authentication carried in endpoint query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact QQ Open Platform Source and public authorization capabilities.
     */
    private static final Capability.Manifest OPEN_MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION));

    /**
     * Exact QQ Mini Program direct Source capability.
     */
    private static final Capability.Manifest MINI_MANIFEST = new Capability.Manifest(
            List.of(SourceAuthentication.initiate(Set.of(Capability.Interaction.DIRECT))));

    /**
     * Exact QQ Open Platform deviations retained inside the selected adapter.
     */
    private static final List<VendorDeviation> OPEN_DEVIATIONS = List.of(
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
                    "code/client_id/client_secret/grant_type/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.FORM,
                    "empty form body",
                    null,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "text parameters without token_type",
                    "access token response",
                    Optional.of(MediaType.TEXT_PLAIN_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "unionid",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "callback JSONP",
                    null,
                    Optional.of(MediaType.TEXT_PLAIN_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "access_token/oauth_consumer_key/openid",
                    "Bearer profile request",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "ret/msg profile",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Exact QQ Mini Program code-to-session deviations.
     */
    private static final List<VendorDeviation> MINI_DEVIATIONS = List.of(
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.QUERY,
                    "appid/secret/js_code/grant_type",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.initiate",
                    VendorDeviation.Location.RESPONSE,
                    "session_key/openid/unionid/errcode/errmsg",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable QQ Open Platform definition.
     */
    private static final VendorDefinition.Definition OPEN_DEFINITION = new VendorDefinition.Definition(ID, OPEN,
            Protocol.OAUTH2, List.of("get_user_info"),
            new VendorTargets(Optional.of(
                    fixed("https://graph.qq.com/oauth2.0/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(fixed("https://graph.qq.com/oauth2.0/token", Http.Method.POST, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed(
                                    "https://graph.qq.com/user/get_user_info",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://graph.qq.com/oauth2.0/token", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://graph.qq.com/oauth2.0/me", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            OPEN_MANIFEST, OPEN_DEVIATIONS);

    /**
     * Complete immutable QQ Mini Program definition.
     */
    private static final VendorDefinition.Definition MINI_DEFINITION = new VendorDefinition.Definition(ID, MINI_PROGRAM,
            Protocol.VENDOR_AUTH, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(),
                    Optional.of(fixed("https://api.q.qq.com/sns/jscode2session", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty()),
            MINI_MANIFEST, MINI_DEVIATIONS);

    /**
     * Creates the stateless QQ definition used by Vendor directory assembly.
     */
    public QqDefinition() {
        // No initialization required.
        // Immutable definition state is retained by class constants.
    }

    /**
     * Creates one fixed credential-free QQ HTTPS endpoint.
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
     * Creates one immutable registered QQ wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact QQ field or representation name
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
     * Returns the stable QQ routing identifier.
     *
     * @return QQ platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive QQ presentation metadata.
     *
     * @return immutable QQ management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("QQ", "QQ Open Platform and Mini Program authentication", "qq");
    }

    /**
     * Returns the exact settings record shared by both QQ variants.
     *
     * @return QQ Source settings class
     */
    @Override
    public Class<QqSourceSettings> settingsType() {
        return QqSourceSettings.class;
    }

    /**
     * Returns both QQ variants in stable management order.
     *
     * @return open then mini-program definitions
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(OPEN_DEFINITION, MINI_DEFINITION);
    }

    /**
     * Resolves the unique selected QQ variant.
     *
     * @param variant requested QQ variant
     * @return exact immutable definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (OPEN.equals(variant)) {
            return OPEN_DEFINITION;
        }
        if (MINI_PROGRAM.equals(variant)) {
            return MINI_DEFINITION;
        }
        throw new ValidateException("QQ Vendor variant is not supported");
    }

}
