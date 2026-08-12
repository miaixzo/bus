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
package org.miaixz.bus.auth.metric;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.metric.oidc.*;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Defines the sole public OpenID Connect provider and relying-party protocol boundary. OpenID Connect authorization
 * remains owned by {@link OAuth2}; this type adds discovery, JSON Web Key Set resolution, ID Token validation, UserInfo
 * subject binding, and RP-Initiated Logout without exposing internal protocol packages.
 *
 * @author Kimi Liu
 */
public interface OIDC {

    /**
     * Maximum number of published or resolved JSON Web Keys.
     */
    int MAXIMUM_KEYS = 128;

    /**
     * Creates one immutable OpenID Provider facade.
     *
     * @param configuration trusted provider configuration
     * @param oauth         OAuth authorization engine
     * @param runtime       authentication runtime
     * @return provider facade
     */
    static Provider provider(
            final ProviderConfiguration configuration,
            final OAuth2.Engine oauth,
            final Runtime runtime) {
        return new DefaultProvider(configuration, oauth, runtime);
    }

    /**
     * Creates one immutable relying-party facade.
     *
     * @param configuration trusted relying-party configuration
     * @param runtime       authentication runtime
     * @return relying-party facade
     */
    static RelyingParty relyingParty(final RelyingPartyConfiguration configuration, final Runtime runtime) {
        return new DefaultRelyingParty(configuration, runtime);
    }

    /**
     * OpenID Connect errors not already defined by OAuth 2.0.
     */
    enum ProtocolError implements Errors {

        /**
         * End-user authentication is required.
         */
        LOGIN_REQUIRED("login_required", "End-user authentication is required"),

        /**
         * End-user account selection is required.
         */
        ACCOUNT_SELECTION_REQUIRED("account_selection_required", "End-user account selection is required"),

        /**
         * End-user consent is required.
         */
        CONSENT_REQUIRED("consent_required", "End-user consent is required"),

        /**
         * End-user interaction is required.
         */
        INTERACTION_REQUIRED("interaction_required", "End-user interaction is required");

        /**
         * Stable wire key.
         */
        private final String key;

        /**
         * Fixed safe wire description.
         */
        private final String value;

        /**
         * Creates one OpenID Connect protocol error.
         *
         * @param key   standard wire key
         * @param value fixed safe description
         */
        ProtocolError(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the standard wire key.
         *
         * @return standard wire key
         */
        @Override
        public String getKey() {
            return key;
        }

        /**
         * Returns the fixed safe description.
         *
         * @return fixed safe description
         */
        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * OpenID Provider protocol facade.
     */
    interface Provider {

        /**
         * Returns the provider's OAuth authorization engine.
         *
         * @return OAuth engine
         */
        OAuth2.Engine oauth();

        /**
         * Returns immutable discovery metadata.
         *
         * @return discovery metadata
         */
        ProviderMetadata discovery();

        /**
         * Returns immutable published public keys.
         *
         * @return published key set
         */
        JwkSet jwks();

        /**
         * Resolves subject claims and enforces exact ID Token subject binding.
         *
         * @param invocation operation context
         * @param subject    exact authenticated subject
         * @return UserInfo outcome
         */
        CompletionStage<Outcome<UserInfoResponse>> userInfo(Invocation invocation, String subject);

        /**
         * Validates an RP-Initiated Logout request.
         *
         * @param invocation operation context
         * @param request    logout request
         * @return logout outcome
         */
        CompletionStage<Outcome<LogoutResponse>> logout(Invocation invocation, LogoutRequest request);
    }

    /**
     * OpenID Connect Relying Party protocol facade.
     */
    interface RelyingParty {

        /**
         * Fetches and validates provider discovery metadata.
         *
         * @param invocation operation context
         * @return discovery outcome
         */
        CompletionStage<Outcome<ProviderMetadata>> discover(Invocation invocation);

        /**
         * Fetches and validates the discovered provider key set.
         *
         * @param invocation operation context
         * @param metadata   validated provider metadata
         * @return key-set outcome
         */
        CompletionStage<Outcome<JwkSet>> jwks(Invocation invocation, ProviderMetadata metadata);

        /**
         * Validates an ID Token with the exact authorization nonce.
         *
         * @param invocation operation context
         * @param token      compact ID Token
         * @param nonce      exact authorization nonce
         * @return verified identity outcome
         */
        CompletionStage<Outcome<Identity>> validate(Invocation invocation, String token, String nonce);

        /**
         * Fetches UserInfo and enforces exact subject equality.
         *
         * @param invocation  operation context
         * @param metadata    validated provider metadata
         * @param accessToken bearer access token
         * @param subject     exact ID Token subject
         * @return UserInfo outcome
         */
        CompletionStage<Outcome<UserInfoResponse>> userInfo(
                Invocation invocation,
                ProviderMetadata metadata,
                String accessToken,
                String subject);

        /**
         * Builds and validates an RP-Initiated Logout URI.
         *
         * @param invocation operation context
         * @param metadata   validated provider metadata
         * @param request    logout request
         * @return logout outcome
         */
        CompletionStage<Outcome<LogoutResponse>> logout(
                Invocation invocation,
                ProviderMetadata metadata,
                LogoutRequest request);
    }

    /**
     * Immutable provider metadata used for discovery and relying-party endpoint selection.
     *
     * @param issuer                     exact provider issuer
     * @param authorizationEndpoint      OAuth authorization endpoint
     * @param tokenEndpoint              OAuth token endpoint
     * @param jwksUri                    JSON Web Key Set endpoint
     * @param userInfoEndpoint           UserInfo endpoint
     * @param endSessionEndpoint         RP-Initiated Logout endpoint
     * @param responseTypesSupported     exact supported response types
     * @param subjectTypesSupported      exact supported subject types
     * @param idTokenAlgorithmsSupported exact supported ID Token algorithms
     */
    record ProviderMetadata(String issuer, URI authorizationEndpoint, URI tokenEndpoint, URI jwksUri,
            URI userInfoEndpoint, URI endSessionEndpoint, Set<String> responseTypesSupported,
            Set<String> subjectTypesSupported, Set<String> idTokenAlgorithmsSupported) {

        /**
         * Validates and snapshots provider metadata.
         *
         * @param issuer                     exact issuer
         * @param authorizationEndpoint      authorization endpoint
         * @param tokenEndpoint              token endpoint
         * @param jwksUri                    key-set endpoint
         * @param userInfoEndpoint           UserInfo endpoint
         * @param endSessionEndpoint         logout endpoint
         * @param responseTypesSupported     response types
         * @param subjectTypesSupported      subject types
         * @param idTokenAlgorithmsSupported ID Token algorithms
         */
        public ProviderMetadata {
            issuer = Support.required(issuer, "Provider issuer");
            authorizationEndpoint = Support.absolute(authorizationEndpoint, "Authorization endpoint");
            tokenEndpoint = Support.absolute(tokenEndpoint, "Token endpoint");
            jwksUri = Support.absolute(jwksUri, "JSON Web Key Set endpoint");
            userInfoEndpoint = Support.absolute(userInfoEndpoint, "UserInfo endpoint");
            endSessionEndpoint = Support.absolute(endSessionEndpoint, "End-session endpoint");
            responseTypesSupported = Support.strings(responseTypesSupported, "Response types");
            subjectTypesSupported = Support.strings(subjectTypesSupported, "Subject types");
            idTokenAlgorithmsSupported = Support.strings(idTokenAlgorithmsSupported, "ID Token algorithms");
        }
    }

    /**
     * Immutable public JSON Web Key.
     *
     * @param keyType    exact JWK key type
     * @param keyId      exact key identifier
     * @param use        exact key use
     * @param algorithm  exact JOSE algorithm
     * @param parameters public key parameters excluding private material
     */
    record Jwk(String keyType, String keyId, String use, String algorithm, Map<String, String> parameters) {

        /**
         * Validates and snapshots one public key.
         *
         * @param keyType    JWK key type
         * @param keyId      key identifier
         * @param use        key use
         * @param algorithm  JOSE algorithm
         * @param parameters public parameters
         */
        public Jwk {
            keyType = Support.required(keyType, "JSON Web Key type");
            keyId = Support.required(keyId, "JSON Web Key identifier");
            use = Support.required(use, "JSON Web Key use");
            algorithm = Support.required(algorithm, "JSON Web Key algorithm");
            final Map<String, String> source = Assert
                    .notNull(parameters, () -> new ValidateException("JSON Web Key parameters must not be null"));
            final LinkedHashMap<String, String> copy = new LinkedHashMap<>();
            source.forEach(
                    (name, value) -> copy.put(
                            Support.required(name, "JSON Web Key parameter name"),
                            Support.required(value, "JSON Web Key parameter value")));
            parameters = Map.copyOf(copy);
        }
    }

    /**
     * Immutable bounded JSON Web Key Set.
     *
     * @param keys public keys
     */
    record JwkSet(List<Jwk> keys) {

        /**
         * Validates and snapshots one key set.
         *
         * @param keys public keys
         */
        public JwkSet {
            keys = List.copyOf(Assert.notNull(keys, () -> new ValidateException("JSON Web Key Set must not be null")));
            Assert.isTrue(
                    !keys.isEmpty() && keys.size() <= MAXIMUM_KEYS,
                    () -> new ValidateException("JSON Web Key Set size must be between one and 128"));
            Assert.isTrue(
                    keys.stream().allMatch(java.util.Objects::nonNull),
                    () -> new ValidateException("JSON Web Key Set must not contain null keys"));
        }
    }

    /**
     * Immutable trusted provider configuration.
     *
     * @param metadata               provider metadata
     * @param jwks                   published public key set
     * @param postLogoutRedirectUris registered post-logout redirect URIs
     */
    record ProviderConfiguration(ProviderMetadata metadata, JwkSet jwks, Set<URI> postLogoutRedirectUris) {

        /**
         * Validates and snapshots provider configuration.
         *
         * @param metadata               provider metadata
         * @param jwks                   published keys
         * @param postLogoutRedirectUris registered redirects
         */
        public ProviderConfiguration {
            metadata = Assert.notNull(metadata, () -> new ValidateException("Provider metadata must not be null"));
            jwks = Assert.notNull(jwks, () -> new ValidateException("Provider key set must not be null"));
            postLogoutRedirectUris = Support.uris(postLogoutRedirectUris, "Post-logout redirect URIs");
        }
    }

    /**
     * Immutable trusted relying-party configuration.
     *
     * @param clientId        exact client identifier and ID Token audience
     * @param issuer          exact expected provider issuer
     * @param algorithm       product-selected ID Token algorithm
     * @param skew            non-negative clock tolerance
     * @param maximumLifetime positive ID Token lifetime limit
     * @param transportPolicy strict endpoint policy
     */
    record RelyingPartyConfiguration(String clientId, String issuer, TrustedAlgorithm algorithm, Duration skew,
            Duration maximumLifetime, TransportPolicy transportPolicy) {

        /**
         * Validates relying-party configuration.
         *
         * @param clientId        client identifier
         * @param issuer          expected issuer
         * @param algorithm       trusted algorithm
         * @param skew            clock tolerance
         * @param maximumLifetime lifetime limit
         * @param transportPolicy endpoint policy
         */
        public RelyingPartyConfiguration {
            clientId = Support.required(clientId, "Relying-party client identifier");
            issuer = Support.required(issuer, "Relying-party issuer");
            algorithm = Assert.notNull(algorithm, () -> new ValidateException("ID Token algorithm must not be null"));
            skew = Assert.notNull(skew, () -> new ValidateException("ID Token skew must not be null"));
            Assert.isTrue(!skew.isNegative(), () -> new ValidateException("ID Token skew must not be negative"));
            maximumLifetime = Assert.notNull(
                    maximumLifetime,
                    () -> new ValidateException("ID Token maximum lifetime must not be null"));
            Assert.isTrue(
                    !maximumLifetime.isZero() && !maximumLifetime.isNegative(),
                    () -> new ValidateException("ID Token maximum lifetime must be positive"));
            transportPolicy = Assert
                    .notNull(transportPolicy, () -> new ValidateException("OIDC transport policy must not be null"));
        }
    }

    /**
     * Immutable validated ID Token identity.
     *
     * @param subject         exact subject identifier
     * @param issuer          exact issuer
     * @param audiences       exact token audiences
     * @param authorizedParty optional exact authorized party
     * @param nonce           exact validated nonce
     * @param claims          immutable verified claims
     */
    record Identity(String subject, String issuer, Set<String> audiences, String authorizedParty, String nonce,
            Map<String, Object> claims) {

        /**
         * Snapshots verified identity state.
         *
         * @param subject         subject identifier
         * @param issuer          issuer
         * @param audiences       audiences
         * @param authorizedParty authorized party
         * @param nonce           nonce
         * @param claims          verified claims
         */
        public Identity {
            subject = Support.required(subject, "ID Token subject");
            issuer = Support.required(issuer, "ID Token issuer");
            audiences = Support.strings(audiences, "ID Token audiences");
            authorizedParty = StringKit.isBlank(authorizedParty) ? null : authorizedParty;
            nonce = Support.required(nonce, "ID Token nonce");
            claims = Map
                    .copyOf(Assert.notNull(claims, () -> new ValidateException("ID Token claims must not be null")));
        }

        /**
         * Redacts verified claims from diagnostic output.
         *
         * @return redacted identity representation
         */
        @Override
        public String toString() {
            return "Identity[REDACTED]";
        }
    }

    /**
     * Immutable UserInfo response.
     *
     * @param subject exact subject identifier
     * @param claims  immutable released claims
     */
    record UserInfoResponse(String subject, Map<String, Object> claims) {

        /**
         * Validates and snapshots UserInfo state.
         *
         * @param subject subject identifier
         * @param claims  released claims
         */
        public UserInfoResponse {
            subject = Support.required(subject, "UserInfo subject");
            claims = Map
                    .copyOf(Assert.notNull(claims, () -> new ValidateException("UserInfo claims must not be null")));
        }

        /**
         * Redacts released claims from diagnostic output.
         *
         * @return redacted response representation
         */
        @Override
        public String toString() {
            return "UserInfoResponse[REDACTED]";
        }
    }

    /**
     * Immutable RP-Initiated Logout request.
     *
     * @param idTokenHint           ID Token hint
     * @param postLogoutRedirectUri optional registered redirect URI
     * @param state                 optional opaque relying-party state
     */
    record LogoutRequest(String idTokenHint, URI postLogoutRedirectUri, String state) {

        /**
         * Redacts the ID Token hint and state.
         *
         * @return redacted request representation
         */
        @Override
        public String toString() {
            return "LogoutRequest[REDACTED]";
        }
    }

    /**
     * Immutable RP-Initiated Logout result.
     *
     * @param redirectUri optional exact registered redirect URI
     * @param state       optional unchanged relying-party state
     */
    record LogoutResponse(URI redirectUri, String state) {

        /**
         * Redacts logout state.
         *
         * @return redacted response representation
         */
        @Override
        public String toString() {
            return "LogoutResponse[REDACTED]";
        }
    }

    /**
     * Immutable provider implementation hidden behind the public contract.
     */
    final class DefaultProvider implements Provider {

        /**
         * Trusted provider configuration.
         */
        private final ProviderConfiguration configuration;

        /**
         * OAuth engine.
         */
        private final OAuth2.Engine oauth;

        /**
         * UserInfo operation.
         */
        private final UserInfoService userInfo;

        /**
         * Logout operation.
         */
        private final LogoutValidator logout;

        /**
         * Creates one provider facade.
         *
         * @param configuration trusted provider configuration
         * @param oauth         OAuth engine
         * @param runtime       authentication runtime
         */
        DefaultProvider(final ProviderConfiguration configuration, final OAuth2.Engine oauth, final Runtime runtime) {
            this.configuration = Assert
                    .notNull(configuration, () -> new ValidateException("Provider configuration must not be null"));
            this.oauth = Assert.notNull(oauth, () -> new ValidateException("OAuth engine must not be null"));
            final Runtime ports = Assert
                    .notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
            this.userInfo = new UserInfoService(ports);
            this.logout = new LogoutValidator(ports);
        }

        /**
         * Returns the provider's OAuth authorization engine.
         *
         * @return OAuth engine
         */
        @Override
        public OAuth2.Engine oauth() {
            return oauth;
        }

        /**
         * Returns immutable discovery metadata.
         *
         * @return discovery metadata
         */
        @Override
        public ProviderMetadata discovery() {
            return configuration.metadata();
        }

        /**
         * Returns immutable published public keys.
         *
         * @return published key set
         */
        @Override
        public JwkSet jwks() {
            return configuration.jwks();
        }

        /**
         * Resolves subject claims.
         *
         * @param invocation operation context
         * @param subject    exact authenticated subject
         * @return UserInfo outcome
         */
        @Override
        public CompletionStage<Outcome<UserInfoResponse>> userInfo(final Invocation invocation, final String subject) {
            return Support.outcome(() -> userInfo.resolve(invocation, subject));
        }

        /**
         * Validates an RP-Initiated Logout request.
         *
         * @param invocation operation context
         * @param request    logout request
         * @return logout outcome
         */
        @Override
        public CompletionStage<Outcome<LogoutResponse>> logout(
                final Invocation invocation,
                final LogoutRequest request) {
            return Support.outcome(() -> logout.provider(invocation, request, configuration.postLogoutRedirectUris()));
        }
    }

    /**
     * Immutable relying-party implementation hidden behind the public contract.
     */
    final class DefaultRelyingParty implements RelyingParty {

        /**
         * Trusted relying-party configuration.
         */
        private final RelyingPartyConfiguration configuration;

        /**
         * Discovery operation.
         */
        private final OidcDiscovery discovery;

        /**
         * Key-set operation.
         */
        private final JwkSetResolver keys;

        /**
         * ID Token operation.
         */
        private final IdTokenValidator tokens;

        /**
         * UserInfo operation.
         */
        private final UserInfoService userInfo;

        /**
         * Logout operation.
         */
        private final LogoutValidator logout;

        /**
         * Creates one relying-party facade.
         *
         * @param configuration trusted relying-party configuration
         * @param runtime       authentication runtime
         */
        DefaultRelyingParty(final RelyingPartyConfiguration configuration, final Runtime runtime) {
            this.configuration = Assert.notNull(
                    configuration,
                    () -> new ValidateException("Relying-party configuration must not be null"));
            final Runtime ports = Assert
                    .notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
            this.discovery = new OidcDiscovery(ports);
            this.keys = new JwkSetResolver(ports);
            this.tokens = new IdTokenValidator(configuration, ports);
            this.userInfo = new UserInfoService(ports);
            this.logout = new LogoutValidator(ports);
        }

        /**
         * Fetches and validates provider discovery metadata.
         *
         * @param invocation operation context
         * @return discovery outcome
         */
        @Override
        public CompletionStage<Outcome<ProviderMetadata>> discover(final Invocation invocation) {
            return Support.outcome(
                    () -> discovery.resolve(invocation, configuration.issuer(), configuration.transportPolicy()));
        }

        /**
         * Fetches and validates a provider key set.
         *
         * @param invocation operation context
         * @param metadata   validated provider metadata
         * @return key-set outcome
         */
        @Override
        public CompletionStage<Outcome<JwkSet>> jwks(final Invocation invocation, final ProviderMetadata metadata) {
            return Support.outcome(() -> keys.resolve(invocation, metadata, configuration.transportPolicy()));
        }

        /**
         * Validates an ID Token and nonce.
         *
         * @param invocation operation context
         * @param token      compact ID Token
         * @param nonce      exact authorization nonce
         * @return verified identity outcome
         */
        @Override
        public CompletionStage<Outcome<Identity>> validate(
                final Invocation invocation,
                final String token,
                final String nonce) {
            return Support.outcome(() -> tokens.validate(invocation, token, nonce));
        }

        /**
         * Fetches and subject-binds UserInfo.
         *
         * @param invocation  operation context
         * @param metadata    validated provider metadata
         * @param accessToken bearer access token
         * @param subject     exact ID Token subject
         * @return UserInfo outcome
         */
        @Override
        public CompletionStage<Outcome<UserInfoResponse>> userInfo(
                final Invocation invocation,
                final ProviderMetadata metadata,
                final String accessToken,
                final String subject) {
            return Support.outcome(
                    () -> userInfo.fetch(
                            invocation,
                            metadata.userInfoEndpoint(),
                            accessToken,
                            subject,
                            configuration.transportPolicy()));
        }

        /**
         * Builds and validates an RP-Initiated Logout URI.
         *
         * @param invocation operation context
         * @param metadata   validated provider metadata
         * @param request    logout request
         * @return logout outcome
         */
        @Override
        public CompletionStage<Outcome<LogoutResponse>> logout(
                final Invocation invocation,
                final ProviderMetadata metadata,
                final LogoutRequest request) {
            return Support.outcome(
                    () -> logout.relyingParty(
                            invocation,
                            metadata.endSessionEndpoint(),
                            request,
                            configuration.transportPolicy()));
        }
    }

    /**
     * Provides private validation and outcome-mapping operations for the two immutable facades.
     */
    final class Support {

        /**
         * Prevents construction of the support utility.
         */
        private Support() {
            // No initialization required.
        }

        /**
         * Converts one internal operation into the closed authentication outcome algebra.
         *
         * @param operation deferred internal operation
         * @param <T>       success type
         * @return asynchronous safe outcome
         */
        private static <T> CompletionStage<Outcome<T>> outcome(final Supplier<CompletionStage<T>> operation) {
            final CompletionStage<T> stage;
            try {
                stage = Assert.notNull(operation.get(), "OIDC operation stage must be not null!");
            } catch (final Throwable failure) {
                return CompletableFuture.completedFuture(failure(failure));
            }
            return stage.handle((value, failure) -> failure == null ? new Success<>(value) : failure(failure));
        }

        /**
         * Converts one internal failure without exposing its message.
         *
         * @param failure operation failure
         * @param <T>     absent success type
         * @return rejected or failed outcome
         */
        private static <T> Outcome<T> failure(final Throwable failure) {
            final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                    ? failure.getCause()
                    : failure;
            if (cause instanceof ProtocolException protocol) {
                final Errors error = protocolError(protocol);
                if (error != null) {
                    return new Rejected<>(new Failure(kind(error), error, false));
                }
            }
            return new Failed<>(
                    new Failure(FailureKind.REMOTE, org.miaixz.bus.core.basic.normal.ErrorCode._100805, true), cause);
        }

        /**
         * Resolves a protocol exception against the closed OAuth and OIDC registries.
         *
         * @param failure protocol exception
         * @return registered error or {@code null}
         */
        private static Errors protocolError(final ProtocolException failure) {
            for (final OAuth2.ProtocolError error : OAuth2.ProtocolError.values()) {
                if (error.getKey().equals(failure.getErrcode())) {
                    return error;
                }
            }
            for (final ProtocolError error : ProtocolError.values()) {
                if (error.getKey().equals(failure.getErrcode())) {
                    return error;
                }
            }
            return null;
        }

        /**
         * Maps a closed protocol error to a stable failure kind.
         *
         * @param error registered protocol error
         * @return stable failure kind
         */
        private static FailureKind kind(final Errors error) {
            if (error == OAuth2.ProtocolError.INVALID_CLIENT) {
                return FailureKind.AUTHENTICATION;
            }
            if (error == OAuth2.ProtocolError.ACCESS_DENIED || error == ProtocolError.CONSENT_REQUIRED) {
                return FailureKind.AUTHORIZATION;
            }
            return FailureKind.VALIDATION;
        }

        /**
         * Requires one non-blank string.
         *
         * @param value source value
         * @param name  value name
         * @return validated value
         */
        private static String required(final String value, final String name) {
            return Assert.notBlank(value, () -> new ValidateException(name + " must not be blank"));
        }

        /**
         * Requires one absolute URI.
         *
         * @param value source URI
         * @param name  URI name
         * @return validated URI
         */
        private static URI absolute(final URI value, final String name) {
            final URI uri = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
            Assert.isTrue(
                    uri.isAbsolute() && !uri.isOpaque() && uri.getHost() != null && uri.getUserInfo() == null
                            && uri.getFragment() == null,
                    () -> new ValidateException(name + " must be a safe absolute URI"));
            return uri;
        }

        /**
         * Snapshots one non-empty string set.
         *
         * @param values source strings
         * @param name   set name
         * @return immutable string set
         */
        private static Set<String> strings(final Set<String> values, final String name) {
            final Set<String> copy = Set
                    .copyOf(Assert.notNull(values, () -> new ValidateException(name + " must not be null")));
            Assert.isTrue(
                    !copy.isEmpty() && copy.stream().noneMatch(StringKit::isBlank),
                    () -> new ValidateException(name + " must contain non-blank values"));
            return copy;
        }

        /**
         * Snapshots one URI set.
         *
         * @param values source URIs
         * @param name   set name
         * @return immutable URI set
         */
        private static Set<URI> uris(final Set<URI> values, final String name) {
            final Set<URI> copy = Set
                    .copyOf(Assert.notNull(values, () -> new ValidateException(name + " must not be null")));
            Assert.isTrue(
                    copy.stream().allMatch(uri -> uri != null && uri.isAbsolute() && uri.getFragment() == null),
                    () -> new ValidateException(name + " must contain safe absolute URIs"));
            return copy;
        }
    }

}
