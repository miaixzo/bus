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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.miaixz.bus.auth.Callback.Outbound;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.*;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.resolver.SubjectResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

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
     * @param subjects      subject resolver used by UserInfo
     * @param json          JSON provider used for strict protocol decoding
     * @param limits        immutable protocol limits
     * @return provider facade
     * @throws ValidateException if a required configuration or collaborator is {@code null}
     */
    static Provider provider(
            final ProviderConfiguration configuration,
            final OAuth2.Engine oauth,
            final SubjectResolver subjects,
            final JsonProvider json,
            final Limits limits) {
        return new DefaultProvider(configuration, oauth, subjects, json, limits);
    }

    /**
     * Creates one immutable relying-party facade.
     *
     * @param configuration trusted relying-party configuration
     * @param fabric        Fabric context used for outbound HTTP exchanges
     * @param states        replay and key-set state store
     * @param json          JSON provider used for strict protocol decoding
     * @param limits        immutable protocol limits
     * @param clock         trusted clock
     * @param keys          trusted JWT key resolver
     * @return relying-party facade
     * @throws ValidateException if a required configuration or collaborator is {@code null}
     */
    static RelyingParty relyingParty(
            final RelyingPartyConfiguration configuration,
            final org.miaixz.bus.fabric.Context fabric,
            final StateStore states,
            final JsonProvider json,
            final Limits limits,
            final Clock clock,
            final KeyResolver keys) {
        return new DefaultRelyingParty(configuration, fabric, states, json, limits, clock, keys);
    }

    /**
     * OpenID Connect errors not already defined by OAuth 2.0.
     *
     * @author Kimi Liu
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
     *
     * @author Kimi Liu
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
        CompletionStage<Outcome<UserInfoResponse>> userInfo(Context invocation, String subject);

        /**
         * Validates an RP-Initiated Logout request.
         *
         * @param invocation operation context
         * @param request    logout request
         * @return logout outcome
         */
        CompletionStage<Outcome<Outbound>> logout(Context invocation, LogoutRequest request);
    }

    /**
     * OpenID Connect Relying Party protocol facade.
     *
     * @author Kimi Liu
     */
    interface RelyingParty {

        /**
         * Fetches and validates provider discovery metadata.
         *
         * @param invocation operation context
         * @return discovery outcome
         */
        CompletionStage<Outcome<ProviderMetadata>> discover(Context invocation);

        /**
         * Fetches and validates the discovered provider key set.
         *
         * @param invocation operation context
         * @param metadata   validated provider metadata
         * @return key-set outcome
         */
        CompletionStage<Outcome<JwkSet>> jwks(Context invocation, ProviderMetadata metadata);

        /**
         * Validates an ID Token with the exact authorization nonce.
         *
         * @param invocation operation context
         * @param token      compact ID Token
         * @param nonce      exact authorization nonce
         * @return verified identity outcome
         */
        CompletionStage<Outcome<Identity>> validate(Context invocation, String token, String nonce);

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
                Context invocation,
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
        CompletionStage<Outcome<Outbound>> logout(Context invocation, ProviderMetadata metadata, LogoutRequest request);
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
     * @author Kimi Liu
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
         * @throws ValidateException if a required value is absent, blank, empty, or not a safe absolute URI
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
     * @author Kimi Liu
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
         * @throws ValidateException if required metadata or a public parameter is absent or blank
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
     * @author Kimi Liu
     */
    record JwkSet(List<Jwk> keys) {

        /**
         * Validates and snapshots one key set.
         *
         * @param keys public keys
         * @throws ValidateException if the set is absent, empty, exceeds 128 entries, or contains {@code null}
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
     * @author Kimi Liu
     */
    record ProviderConfiguration(ProviderMetadata metadata, JwkSet jwks, Set<URI> postLogoutRedirectUris) {

        /**
         * Validates and snapshots provider configuration.
         *
         * @param metadata               provider metadata
         * @param jwks                   published keys
         * @param postLogoutRedirectUris registered redirects
         * @throws ValidateException if metadata, keys, or redirect entries are invalid
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
     * @author Kimi Liu
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
         * @throws ValidateException if a required value is absent or a duration is outside its documented range
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
     * @author Kimi Liu
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
         * @throws ValidateException if required identity data is absent, blank, or empty
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
     * @author Kimi Liu
     */
    record UserInfoResponse(String subject, Map<String, Object> claims) {

        /**
         * Validates and snapshots UserInfo state.
         *
         * @param subject subject identifier
         * @param claims  released claims
         * @throws ValidateException if the subject or claims are invalid
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
     * @author Kimi Liu
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
     * Immutable provider implementation hidden behind the public contract.
     *
     * @author Kimi Liu
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
         * Subject resolver used for provider-side UserInfo requests.
         */
        private final SubjectResolver subjects;

        /**
         * Creates one provider facade.
         *
         * @param configuration trusted provider configuration
         * @param oauth         OAuth engine
         * @param subjects      subject resolver used by UserInfo
         * @param json          JSON provider used for strict protocol decoding
         * @param limits        immutable protocol limits
         * @throws ValidateException if a required configuration or collaborator is {@code null}
         */
        DefaultProvider(final ProviderConfiguration configuration, final OAuth2.Engine oauth,
                final SubjectResolver subjects, final JsonProvider json, final Limits limits) {
            this.configuration = Assert
                    .notNull(configuration, () -> new ValidateException("Provider configuration must not be null"));
            this.oauth = Assert.notNull(oauth, () -> new ValidateException("OAuth engine must not be null"));
            this.subjects = Assert.notNull(subjects, () -> new ValidateException("Subject resolver must not be null"));
            this.userInfo = new UserInfoService(json, limits);
            this.logout = new LogoutValidator(json, limits);
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
        public CompletionStage<Outcome<UserInfoResponse>> userInfo(final Context invocation, final String subject) {
            return Support.outcome(() -> userInfo.resolve(invocation, subject, subjects));
        }

        /**
         * Validates an RP-Initiated Logout request.
         *
         * @param invocation operation context
         * @param request    logout request
         * @return logout outcome
         */
        @Override
        public CompletionStage<Outcome<Outbound>> logout(final Context invocation, final LogoutRequest request) {
            return Support.outcome(() -> logout.provider(invocation, request, configuration.postLogoutRedirectUris()));
        }
    }

    /**
     * Immutable relying-party implementation hidden behind the public contract.
     *
     * @author Kimi Liu
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
         * Fabric context used for outbound HTTP exchanges.
         */
        private final org.miaixz.bus.fabric.Context fabric;

        /**
         * Creates one relying-party facade.
         *
         * @param configuration trusted relying-party configuration
         * @param fabric        Fabric context used for outbound HTTP exchanges
         * @param states        replay and key-set state store
         * @param json          JSON provider used for strict protocol decoding
         * @param limits        immutable protocol limits
         * @param clock         trusted clock
         * @param keyResolver   trusted JWT key resolver
         * @throws ValidateException if a required configuration or collaborator is {@code null}
         */
        DefaultRelyingParty(final RelyingPartyConfiguration configuration, final org.miaixz.bus.fabric.Context fabric,
                final StateStore states, final JsonProvider json, final Limits limits, final Clock clock,
                final KeyResolver keyResolver) {
            this.configuration = Assert.notNull(
                    configuration,
                    () -> new ValidateException("Relying-party configuration must not be null"));
            this.fabric = Assert.notNull(fabric, () -> new ValidateException("Fabric context must not be null"));
            this.discovery = new OidcDiscovery(fabric, json, limits);
            this.keys = new JwkSetResolver(fabric, states, json, limits);
            this.tokens = new IdTokenValidator(configuration, json, clock, keyResolver, states, limits);
            this.userInfo = new UserInfoService(json, limits);
            this.logout = new LogoutValidator(json, limits);
        }

        /**
         * Fetches and validates provider discovery metadata.
         *
         * @param invocation operation context
         * @return discovery outcome
         */
        @Override
        public CompletionStage<Outcome<ProviderMetadata>> discover(final Context invocation) {
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
        public CompletionStage<Outcome<JwkSet>> jwks(final Context invocation, final ProviderMetadata metadata) {
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
                final Context invocation,
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
                final Context invocation,
                final ProviderMetadata metadata,
                final String accessToken,
                final String subject) {
            return Support.outcome(
                    () -> userInfo.fetch(
                            invocation,
                            fabric,
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
        public CompletionStage<Outcome<Outbound>> logout(
                final Context invocation,
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
     *
     * @author Kimi Liu
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
            final Throwable cause = ExceptionKit.unwrap(failure);
            if (cause instanceof ProtocolException protocol) {
                final Errors error = protocolError(protocol);
                if (error != null) {
                    return new Rejected<>(new Failure(kind(error), error, false));
                }
            }
            return new Failed<>(new Failure(Kind.REMOTE, org.miaixz.bus.core.basic.normal.ErrorCode._100805, true),
                    cause);
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
        private static Kind kind(final Errors error) {
            if (error == OAuth2.ProtocolError.INVALID_CLIENT) {
                return Kind.AUTHENTICATION;
            }
            if (error == OAuth2.ProtocolError.ACCESS_DENIED || error == ProtocolError.CONSENT_REQUIRED) {
                return Kind.AUTHORIZATION;
            }
            return Kind.VALIDATION;
        }

        /**
         * Requires one non-blank string.
         *
         * @param value source value
         * @param name  value name
         * @return validated value
         * @throws ValidateException if the value is {@code null} or blank
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
         * @throws ValidateException if the URI is absent, unsafe, or not absolute
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
         * @throws ValidateException if the set is absent, empty, or contains a blank value
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
