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
package org.miaixz.bus.auth.source.protocol.oidc.server;

import static org.miaixz.bus.auth.Builder.ABSENT_VALUE;
import static org.miaixz.bus.auth.Builder.CONFIGURED_VALUE;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Scheme.Options;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.source.protocol.oauth2.server.OAuth2ServerOptions;
import org.miaixz.bus.auth.source.protocol.oidc.SubjectType;
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
 * @param oauth2Options                        validated OAuth authorization-server options
 * @param discoveryEndpoint                    required OpenID Provider Metadata endpoint
 * @param userInfoEndpoint                     optional UserInfo endpoint
 * @param jwkSetEndpoint                       required public JWK Set endpoint
 * @param endSessionEndpoint                   optional RP-Initiated Logout endpoint
 * @param subjectTypesSupported                subject identifier types implemented by this OpenID Provider
 * @param scopeClaims                          exact scope-to-claim mapping used by ID Token, UserInfo, and discovery
 *                                             generation
 * @param pairwisePolicy                       optional pairwise subject derivation policy
 * @param idTokenEncryptionAlgorithmsSupported allowed ID Token JWE key-management algorithms
 * @param idTokenEncryptionMethodsSupported    allowed ID Token JWE content-encryption methods
 * @param idTokenSigningAlgorithm              exact ID Token JWS algorithm
 * @param idTokenSigningKeyId                  exact public JWK {@code kid} used to resolve the signing key
 * @param idTokenLifetime                      positive ID Token lifetime not exceeding one hour
 * @author Kimi Liu
 */
public record OpenIdServerOptions(OAuth2ServerOptions oauth2Options, Optional<Endpoint> discoveryEndpoint,
        Optional<Endpoint> userInfoEndpoint, Optional<Endpoint> jwkSetEndpoint, Optional<Endpoint> endSessionEndpoint,
        Set<SubjectType> subjectTypesSupported, Map<String, Set<String>> scopeClaims,
        Optional<PairwisePolicy> pairwisePolicy, Set<JwaAlgorithm> idTokenEncryptionAlgorithmsSupported,
        Set<JwaAlgorithm> idTokenEncryptionMethodsSupported, JwaAlgorithm idTokenSigningAlgorithm,
        String idTokenSigningKeyId, Duration idTokenLifetime) implements Options<OpenIdServerOptions> {

    /**
     * Minimum whole-second lifetime representable as a JWT NumericDate interval.
     */
    private static final Duration MINIMUM_ID_TOKEN_LIFETIME = Duration.ofSeconds(1);
    /**
     * Maximum ID Token lifetime permitted by the OpenID Provider security policy.
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
        Assert.notNull(oauth2Options, "OpenID Provider OAuth 2.x authorization-server options must not be null");
        if (oauth2Options.authorizationEndpoint().isEmpty() || oauth2Options.tokenEndpoint().isEmpty()) {
            throw new ValidateException(
                    "OpenID Connect Authorization Code Flow requires OAuth authorization and token endpoints");
        }
        if (!oauth2Options.scopesSupported().contains("openid")) {
            throw new ValidateException("OpenID Provider scopes must contain openid");
        }
        if (!oauth2Options.grantTypesSupported().contains(GrantType.AUTHORIZATION_CODE)) {
            throw new ValidateException("OpenID Provider must enable the authorization_code grant");
        }
        if (!oauth2Options.pkceRequired() || !oauth2Options.refreshTokenRotationRequired()) {
            throw new ValidateException("OpenID Provider policy requires PKCE and refresh-token rotation");
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
            Assert.notNull(type, "OpenID Connect subject type must not be null");
            if (!SubjectType.PUBLIC.equals(type) && !SubjectType.PAIRWISE.equals(type)) {
                throw new ValidateException("OpenID Connect subject identifier type is unsupported");
            }
        }
        subjectTypesSupported = Set.copyOf(subjectTypesSupported);
        Assert.notNull(pairwisePolicy, "OpenID Connect pairwise policy container must not be null");
        pairwisePolicy = Optional.ofNullable(pairwisePolicy.getOrNull());
        if (subjectTypesSupported.contains(SubjectType.PAIRWISE) != pairwisePolicy.isPresent()) {
            throw new ValidateException("Pairwise subject support requires exactly one pairwise policy");
        }
        scopeClaims = claims(oauth2Options, scopeClaims);
        idTokenEncryptionAlgorithmsSupported = algorithms(
                idTokenEncryptionAlgorithmsSupported,
                JwaAlgorithm.Kind.KEY_MANAGEMENT,
                "ID Token key management");
        idTokenEncryptionMethodsSupported = algorithms(
                idTokenEncryptionMethodsSupported,
                JwaAlgorithm.Kind.CONTENT_ENCRYPTION,
                "ID Token content encryption");
        if (idTokenEncryptionAlgorithmsSupported.isEmpty() != idTokenEncryptionMethodsSupported.isEmpty()) {
            throw new ValidateException("ID Token encryption algorithm and method sets must be configured together");
        }

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
     * Creates a secure-default OpenID Provider builder over one frozen OAuth server.
     *
     * @param oauth2 immutable OAuth authorization-server options
     * @return new secure-default OpenID Provider builder
     */
    public static Builder builder(final OAuth2ServerOptions oauth2) {
        return new Builder(oauth2);
    }

    /**
     * Validates and freezes the scope-to-claim publication mapping.
     *
     * @param oauth2 immutable OAuth authorization-server options
     * @param values requested scope-to-claim mapping
     * @return immutable validated scope-to-claim mapping
     */
    private static Map<String, Set<String>> claims(
            final OAuth2ServerOptions oauth2,
            final Map<String, Set<String>> values) {
        Assert.notNull(values, "OpenID Connect scope claim map must not be null");
        final Map<String, Set<String>> copy = new LinkedHashMap<>();
        values.forEach((scope, claims) -> {
            Assert.notBlank(scope, "OpenID Connect claim scope must not be blank");
            if (!oauth2.scopesSupported().contains(scope)) {
                throw new ValidateException("OpenID Connect claim scope is not supported by OAuth options");
            }
            final Set<String> names = new LinkedHashSet<>();
            for (String claim : Assert.notNull(claims, "OpenID Connect scope claim set must not be null")) {
                names.add(Assert.notBlank(claim, "OpenID Connect claim name must not be blank"));
            }
            copy.put(scope, Set.copyOf(names));
        });
        return Map.copyOf(copy);
    }

    /**
     * Validates and freezes algorithms of one required JOSE kind.
     *
     * @param values requested algorithm set
     * @param kind   required JOSE algorithm kind
     * @param label  safe validation label
     * @return immutable validated algorithm set
     */
    private static Set<JwaAlgorithm> algorithms(
            final Set<JwaAlgorithm> values,
            final JwaAlgorithm.Kind kind,
            final String label) {
        Assert.notNull(values, label + " algorithm set must not be null");
        for (JwaAlgorithm value : values) {
            Assert.notNull(value, label + " algorithm must not be null").require(kind);
        }
        return Set.copyOf(values);
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
        return endpoint.isPresent() ? CONFIGURED_VALUE : ABSENT_VALUE;
    }

    /**
     * Derives the published claim set from the single scope-to-claim mapping.
     *
     * @return immutable published claim-name set
     */
    public Set<String> claimsSupported() {
        final Set<String> claims = new LinkedHashSet<>();
        claims.add(JwtClaims.SUBJECT);
        scopeClaims.values().forEach(claims::addAll);
        return Set.copyOf(claims);
    }

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
     * Returns this immutable configuration snapshot.
     *
     * @return this immutable configuration
     */
    @Override
    public OpenIdServerOptions snapshot() {
        return this;
    }

    /**
     * Returns the issuer from the composed OAuth authorization-server options.
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
                + subjectTypesSupported + ", claimsSupported=" + claimsSupported() + ", idTokenSigningAlgorithm="
                + idTokenSigningAlgorithm + ", idTokenSigningKeyId=[REDACTED], idTokenLifetime=" + idTokenLifetime
                + Symbol.BRACKET_RIGHT;
    }

    /**
     * Stable key identifier for the pairwise subject derivation secret.
     *
     * @param keyId external key identifier used for pairwise subject derivation
     * @author Kimi Liu
     */
    public record PairwisePolicy(String keyId) {

        /**
         * Validates the external pairwise-subject key identifier.
         *
         * @param keyId external key identifier used for pairwise subject derivation
         */
        public PairwisePolicy {
            Assert.notBlank(keyId, "Pairwise subject key identifier must not be blank");
        }

    }

    /**
     * Collects OpenID Provider configuration while retaining canonical-constructor validation.
     *
     * @author Kimi Liu
     */
    public static class Builder {

        /**
         * Immutable OAuth authorization-server options extended by this profile.
         */
        private final OAuth2ServerOptions oauth2;
        /**
         * OpenID Provider discovery endpoint.
         */
        private Endpoint discoveryEndpoint;
        /**
         * UserInfo endpoint.
         */
        private Endpoint userInfoEndpoint;
        /**
         * Relying-party initiated end-session endpoint.
         */
        private Endpoint endSessionEndpoint;
        /**
         * OpenID Provider JWK Set endpoint.
         */
        private Endpoint jwkSetEndpoint;
        /**
         * External ID Token signing-key identifier.
         */
        private String signingKeyId;
        /**
         * ID Token signing algorithm.
         */
        private JwaAlgorithm signingAlgorithm;
        /**
         * ID Token lifetime.
         */
        private Duration idTokenLifetime = Duration.ofMinutes(5);
        /**
         * Supported subject-identifier types.
         */
        private Set<SubjectType> subjectTypes = Set.of(SubjectType.PUBLIC);
        /**
         * Optional external pairwise-subject key identifier.
         */
        private String pairwiseKeyId;
        /**
         * Scope-to-claim publication mapping.
         */
        private Map<String, Set<String>> scopeClaims = Map.of("openid", Set.of(JwtClaims.SUBJECT));
        /**
         * Supported ID Token key-management algorithms.
         */
        private Set<JwaAlgorithm> encryptionAlgorithms = Set.of();
        /**
         * Supported ID Token content-encryption methods.
         */
        private Set<JwaAlgorithm> encryptionMethods = Set.of();

        /**
         * Creates a secure-default collector over immutable OAuth options.
         *
         * @param oauth2 immutable OAuth authorization-server options
         */
        public Builder(final OAuth2ServerOptions oauth2) {
            this.oauth2 = Assert.notNull(oauth2, "OpenID Connect OAuth options must not be null");
        }

        /**
         * Selects the OpenID Provider discovery endpoint.
         *
         * @param value immutable discovery endpoint
         * @return this builder
         */
        public Builder discoveryEndpoint(final Endpoint value) {
            this.discoveryEndpoint = Assert.notNull(value, "Discovery endpoint must not be null");
            return this;
        }

        /**
         * Selects the UserInfo endpoint.
         *
         * @param value immutable UserInfo endpoint
         * @return this builder
         */
        public Builder userInfoEndpoint(final Endpoint value) {
            this.userInfoEndpoint = Assert.notNull(value, "UserInfo endpoint must not be null");
            return this;
        }

        /**
         * Selects the relying-party initiated end-session endpoint.
         *
         * @param value immutable end-session endpoint
         * @return this builder
         */
        public Builder endSessionEndpoint(final Endpoint value) {
            this.endSessionEndpoint = Assert.notNull(value, "End-session endpoint must not be null");
            return this;
        }

        /**
         * Selects the provider JWK Set endpoint.
         *
         * @param value immutable JWK Set endpoint
         * @return this builder
         */
        public Builder jwkSetEndpoint(final Endpoint value) {
            this.jwkSetEndpoint = Assert.notNull(value, "JWK Set endpoint must not be null");
            return this;
        }

        /**
         * Selects the ID Token signing key and algorithm.
         *
         * @param keyId     external signing-key identifier
         * @param algorithm ID Token signature algorithm
         * @return this builder
         */
        public Builder signing(final String keyId, final JwaAlgorithm algorithm) {
            this.signingKeyId = Assert.notBlank(keyId, "ID Token signing key id must not be blank");
            this.signingAlgorithm = Assert.notNull(algorithm, "ID Token signing algorithm must not be null");
            return this;
        }

        /**
         * Replaces the ID Token lifetime.
         *
         * @param value ID Token lifetime
         * @return this builder
         */
        public Builder idTokenLifetime(final Duration value) {
            this.idTokenLifetime = Assert.notNull(value, "ID Token lifetime must not be null");
            return this;
        }

        /**
         * Replaces the supported subject-identifier types.
         *
         * @param values supported subject types
         * @return this builder
         */
        public Builder subjectTypes(final Set<SubjectType> values) {
            this.subjectTypes = Set.copyOf(Assert.notNull(values, "Subject types must not be null"));
            return this;
        }

        /**
         * Enables pairwise subject identifiers using one external key.
         *
         * @param keyId external pairwise-subject key identifier
         * @return this builder
         */
        public Builder pairwise(final String keyId) {
            this.pairwiseKeyId = Assert.notBlank(keyId, "Pairwise key id must not be blank");
            return this;
        }

        /**
         * Replaces the scope-to-claim publication mapping.
         *
         * @param values scope-to-claim mapping
         * @return this builder
         */
        public Builder scopeClaims(final Map<String, Set<String>> values) {
            this.scopeClaims = Map.copyOf(Assert.notNull(values, "Scope claims must not be null"));
            return this;
        }

        /**
         * Replaces the supported ID Token encryption algorithms and methods.
         *
         * @param algorithms key-management algorithms
         * @param methods    content-encryption methods
         * @return this builder
         */
        public Builder idTokenEncryption(final Set<JwaAlgorithm> algorithms, final Set<JwaAlgorithm> methods) {
            this.encryptionAlgorithms = Set.copyOf(Assert.notNull(algorithms, "JWE algorithms must not be null"));
            this.encryptionMethods = Set.copyOf(Assert.notNull(methods, "JWE methods must not be null"));
            return this;
        }

        /**
         * Builds and validates immutable OpenID Provider options.
         *
         * @return validated immutable OpenID Provider options
         */
        public OpenIdServerOptions build() {
            return new OpenIdServerOptions(oauth2, Optional.ofNullable(discoveryEndpoint),
                    Optional.ofNullable(userInfoEndpoint), Optional.ofNullable(jwkSetEndpoint),
                    Optional.ofNullable(endSessionEndpoint), subjectTypes, scopeClaims,
                    Optional.ofNullable(pairwiseKeyId == null ? null : new PairwisePolicy(pairwiseKeyId)),
                    encryptionAlgorithms, encryptionMethods, signingAlgorithm, signingKeyId, idTokenLifetime);
        }

    }

}
