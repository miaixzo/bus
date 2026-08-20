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
package org.miaixz.bus.auth.vendor.slack;

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
 * Declares the Slack OAuth 2.0 browser Vendor manifest.
 * <p>
 * Public operations retain standard authorization, token, and revocation request/result types. The comma-delimited
 * authorization scope, query-authenticated {@code oauth.v2.access} call, Slack response envelopes, {@code users.info},
 * and {@code auth.revoke} transport remain exact registered Vendor deviations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SlackManifest implements VariantManifest<SlackOptions> {

    /**
     * Stable Slack platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("slack");

    /**
     * Stable identifier of the sole Slack variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Slack client-secret authentication carried in token query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by Slack.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN,
            OAuth2ClientScheme.REVOCATION));

    /**
     * Exact historical Slack wire deviations isolated from the OAuth protocol package.
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
                    "client_id/client_secret/code/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/authed_user",
                    "access token response extensions",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.QUERY,
                    "user",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/user",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    "revocation",
                    VendorDeviation.Location.HEADER,
                    "Bearer access token",
                    "token form member",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "revocation",
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/revoked",
                    "empty success response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable Slack endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            List.of("users.profile:read", "users:read", "users:read.email"),
            new VendorTargets(Optional
                    .of(fixed("https://slack.com/oauth/v2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://slack.com/api/oauth.v2.access", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://slack.com/api/users.info", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://slack.com/api/auth.revoke",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Creates the stateless Slack manifest used by Vendor directory assembly.
     */
    public SlackManifest() {
        // No initialization required.
        // Immutable manifest state is retained by class constants.
    }

    /**
     * Creates one fixed credential-free Slack HTTPS endpoint.
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
     * Creates one immutable registered Slack wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact Slack field or representation name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the result uses a Slack response envelope
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
     * Returns the stable Slack routing identifier.
     *
     * @return Slack platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Slack presentation metadata.
     *
     * @return immutable Slack management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Slack", "Slack workspace account authorization", "slack");
    }

    /**
     * Returns the sole supported Slack variant.
     *
     * @return immutable single-element manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT);
    }

    /**
     * Resolves the exact default Slack manifest.
     *
     * @param variant requested Slack variant
     * @return immutable default manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("Slack Vendor variant is not supported");
        }
        return VARIANT;
    }

}
