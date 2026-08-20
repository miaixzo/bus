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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ServerOptions;
import org.miaixz.bus.auth.protocol.oidc.SubjectType;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Holds immutable deployment and security options for one OpenID Provider.
 * <p>
 * OAuth authorization, token, scope, grant, and credential policy remains in the composed OAuth options. Signing key
 * material and JWK Set data are resolved externally at runtime; this record stores only the standard key identifier.
 * </p>
 *
 * @param oauth2Options           validated OAuth authorization-server options
 * @param discoveryEndpoint       required OpenID Provider Metadata endpoint
 * @param userInfoEndpoint        optional UserInfo endpoint
 * @param jwkSetEndpoint          required public JWK Set endpoint
 * @param endSessionEndpoint      optional RP-Initiated Logout endpoint
 * @param subjectTypesSupported   subject identifier types implemented by this Provider
 * @param claimsSupported         claim names this Provider can supply
 * @param idTokenSigningAlgorithm exact ID Token JWS algorithm
 * @param idTokenSigningKeyId     exact public JWK {@code kid} used to resolve the signing key
 * @param idTokenLifetime         positive ID Token lifetime not exceeding one hour
 * @author Kimi Liu
 */
public record OpenIdServerOptions(OAuth2ServerOptions oauth2Options, Optional<Endpoint> discoveryEndpoint,
        Optional<Endpoint> userInfoEndpoint, Optional<Endpoint> jwkSetEndpoint, Optional<Endpoint> endSessionEndpoint,
        Set<SubjectType> subjectTypesSupported, Set<String> claimsSupported, JwaAlgorithm idTokenSigningAlgorithm,
        String idTokenSigningKeyId, Duration idTokenLifetime) implements Options<OpenIdServerOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<OpenIdServerOptions> type() {
        return OpenIdServerOptions.class;
    }

    /**
     * Minimum whole-second lifetime representable as a JWT NumericDate interval.
     */
    private static final Duration MINIMUM_ID_TOKEN_LIFETIME = Duration.ofSeconds(1);

    /**
     * Maximum ID Token lifetime permitted by the Provider security policy.
     */
    private static final Duration MAXIMUM_ID_TOKEN_LIFETIME = Duration.ofHours(1);

    /**
     * Normalizes endpoints and enforces the OIDC code-flow, claim, algorithm, and lifetime profile.
     *
     * @throws IllegalArgumentException if a required component, container, collection, or item is {@code null}
     * @throws ValidateException        if OAuth policy, endpoints, subject type, claims, algorithm, or lifetime is
     *                                  invalid
     */
    public OpenIdServerOptions {
        Assert.notNull(oauth2Options, "OpenID Connect OAuth 2.x Provider options must not be null");
        if (oauth2Options.authorizationEndpoint().isEmpty() || oauth2Options.tokenEndpoint().isEmpty()) {
            throw new ValidateException(
                    "OpenID Connect Authorization Code Flow requires OAuth authorization and token endpoints");
        }
        if (!oauth2Options.scopesSupported().contains("openid")) {
            throw new ValidateException("OpenID Connect Provider scopes must contain openid");
        }
        if (!oauth2Options.grantTypesSupported().contains(GrantType.AUTHORIZATION_CODE)) {
            throw new ValidateException("OpenID Connect Provider must enable the authorization_code grant");
        }
        if (!oauth2Options.pkceRequired() || !oauth2Options.refreshTokenRotationRequired()) {
            throw new ValidateException("OpenID Connect Provider policy requires PKCE and refresh-token rotation");
        }

        discoveryEndpoint = normalize(discoveryEndpoint, "discovery endpoint");
        userInfoEndpoint = normalize(userInfoEndpoint, "UserInfo endpoint");
        jwkSetEndpoint = normalize(jwkSetEndpoint, "JWK Set endpoint");
        endSessionEndpoint = normalize(endSessionEndpoint, "end-session endpoint");
        if (discoveryEndpoint.isEmpty() || jwkSetEndpoint.isEmpty()) {
            throw new ValidateException("OpenID Connect Providers require discovery and JWK Set endpoints");
        }
        requireEndpoint(discoveryEndpoint.getOrNull(), Endpoint.Authentication.NONE, "Discovery endpoint");
        requireEndpoint(userInfoEndpoint.getOrNull(), Endpoint.Authentication.BEARER, "UserInfo endpoint");
        requireEndpoint(jwkSetEndpoint.getOrNull(), Endpoint.Authentication.NONE, "JWK Set endpoint");
        requireEndpoint(endSessionEndpoint.getOrNull(), Endpoint.Authentication.NONE, "End-session endpoint");

        Assert.notNull(subjectTypesSupported, "OpenID Connect subject type set must not be null");
        if (subjectTypesSupported.isEmpty()) {
            throw new ValidateException("OpenID Connect subject type set must not be empty");
        }
        for (SubjectType type : subjectTypesSupported) {
            if (!SubjectType.PUBLIC.equals(Assert.notNull(type, "OpenID Connect subject type must not be null"))) {
                throw new ValidateException("OpenID Connect Provider supports only public subject identifiers");
            }
        }
        subjectTypesSupported = Set.copyOf(subjectTypesSupported);

        Assert.notNull(claimsSupported, "OpenID Connect supported claim set must not be null");
        final Set<String> claimNames = new LinkedHashSet<>();
        for (String claim : claimsSupported) {
            claimNames.add(Assert.notBlank(claim, "OpenID Connect supported claim name must not be blank"));
        }
        if (!claimNames.contains(JwtClaims.SUBJECT)) {
            throw new ValidateException("OpenID Connect supported claims must contain sub");
        }
        claimsSupported = Set.copyOf(claimNames);

        Assert.notNull(idTokenSigningAlgorithm, "OpenID Connect ID Token signing algorithm must not be null")
                .require(JwaAlgorithm.Kind.SIGNATURE);
        idTokenSigningKeyId = Assert
                .notBlank(idTokenSigningKeyId, "OpenID Connect ID Token signing key identifier must not be blank");
        idTokenLifetime = Assert.notNull(idTokenLifetime, "OpenID Connect ID Token lifetime must not be null");
        if (idTokenLifetime.compareTo(MINIMUM_ID_TOKEN_LIFETIME) < 0
                || idTokenLifetime.compareTo(MAXIMUM_ID_TOKEN_LIFETIME) > 0) {
            throw new ValidateException("OpenID Connect ID Token lifetime must be between one second and one hour");
        }
    }

    /**
     * Normalizes one Bus optional endpoint container.
     *
     * @param value optional endpoint container
     * @param label safe endpoint label
     * @return normalized container
     */
    private static Optional<Endpoint> normalize(final Optional<Endpoint> value, final String label) {
        Assert.notNull(value, "OpenID Connect " + label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Requires one present OIDC endpoint to use fragment-free HTTPS GET and its exact authentication scheme.
     *
     * @param endpoint       optional endpoint value
     * @param authentication required endpoint authentication declaration
     * @param label          safe endpoint label
     * @throws ValidateException if transport, method, fragment, or authentication is invalid
     */
    private static void requireEndpoint(
            final Endpoint endpoint,
            final Endpoint.Authentication authentication,
            final String label) {
        if (endpoint == null) {
            return;
        }
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != Http.Method.GET
                || endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException(label + " must be a fragment-free HTTPS GET endpoint");
        }
        if (!endpoint.authentication().contains(authentication)) {
            throw new ValidateException(label + " must declare " + authentication.value() + " authentication");
        }
    }

    /**
     * Returns only whether an endpoint is configured.
     *
     * @param endpoint optional sensitive endpoint
     * @return stable presence label
     */
    private static String present(final Optional<Endpoint> endpoint) {
        return endpoint.isPresent() ? "[CONFIGURED]" : "[ABSENT]";
    }

    /**
     * Returns the issuer from the composed OAuth Provider options.
     *
     * @return exact OpenID Provider issuer
     */
    public String issuer() {
        return oauth2Options.issuer();
    }

    /**
     * Returns a diagnostic summary without key identifier, OAuth internals, or endpoint query values.
     *
     * @return redacted OpenID Provider options summary
     */
    @Override
    public String toString() {
        return "OpenIdServerOptions[oauth2Options=[REDACTED], discoveryEndpoint=" + present(discoveryEndpoint)
                + ", userInfoEndpoint=" + present(userInfoEndpoint) + ", jwkSetEndpoint=" + present(jwkSetEndpoint)
                + ", endSessionEndpoint=" + present(endSessionEndpoint) + ", subjectTypesSupported="
                + subjectTypesSupported + ", claimsSupported=" + claimsSupported + ", idTokenSigningAlgorithm="
                + idTokenSigningAlgorithm + ", idTokenSigningKeyId=[REDACTED], idTokenLifetime=" + idTokenLifetime
                + Symbol.BRACKET_RIGHT;
    }

}
