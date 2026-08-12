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
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Outcome;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.metric.oauth2.OAuth2Engine;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;

/**
 * Defines the sole public OAuth 2.0 protocol facade and its immutable operation contracts.
 * <p>
 * The facade exposes authorization code with PKCE, client credentials, rotating refresh tokens, device authorization,
 * pushed authorization requests, JWT-secured requests and responses, token introspection, and token revocation.
 * Implicit and resource-owner-password grants have no representation in this contract and therefore cannot enter an
 * OAuth engine.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2 {

    /**
     * Prevents construction of the protocol contract namespace.
     */
    private OAuth2() {
        // No initialization required.
    }

    /**
     * Creates the sole production OAuth engine over trusted policy and product runtime ports.
     *
     * @param policy  trusted OAuth security policy
     * @param runtime validated authentication runtime
     * @return fully assembled OAuth engine
     */
    public static Engine engine(final Policy policy, final Runtime runtime) {
        return new OAuth2Engine(policy, runtime);
    }

    /**
     * Returns an insertion-ordered immutable scope snapshot.
     *
     * @param values requested or granted scopes
     * @return immutable scope snapshot
     */
    private static Set<String> snapshotScopes(final Set<String> values) {
        return values == null || values.isEmpty() ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    /**
     * Returns an independent secret-character snapshot.
     *
     * @param value optional secret characters
     * @return independent secret copy
     */
    private static char[] characters(final char[] value) {
        return value == null ? new char[0] : value.clone();
    }

    /**
     * Requires one positive trusted duration.
     *
     * @param value configured duration
     * @param label programming-contract label
     * @return unchanged positive duration
     */
    private static Duration positive(final Duration value, final String label) {
        final Duration current = Assert.notNull(value, label + " must be not null!");
        Assert.isTrue(!current.isZero() && !current.isNegative(), label + " must be positive!");
        return current;
    }

    /**
     * OAuth grants enabled by the protocol engine.
     */
    public enum GrantType {

        /**
         * Authorization code grant with mandatory PKCE.
         */
        AUTHORIZATION_CODE("authorization_code"),

        /**
         * Confidential-client credentials grant.
         */
        CLIENT_CREDENTIALS("client_credentials"),

        /**
         * Rotating refresh-token grant.
         */
        REFRESH_TOKEN("refresh_token"),

        /**
         * Device authorization grant.
         */
        DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code");

        /**
         * Exact wire value.
         */
        private final String value;

        /**
         * Creates one enabled grant registration.
         *
         * @param value exact wire value
         */
        GrantType(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact case-sensitive wire value.
         *
         * @return exact wire value
         */
        public String value() {
            return value;
        }
    }

    /**
     * Authorization response modes supported by the protocol engine.
     */
    public enum ResponseMode {

        /**
         * Query response carrying a code and state.
         */
        QUERY("query"),

        /**
         * Form-post response carrying a code and state.
         */
        FORM_POST("form_post"),

        /**
         * Query response carrying one JWT-secured authorization response.
         */
        QUERY_JWT("query.jwt"),

        /**
         * Form-post response carrying one JWT-secured authorization response.
         */
        FORM_POST_JWT("form_post.jwt");

        /**
         * Exact wire value.
         */
        private final String value;

        /**
         * Creates one response-mode registration.
         *
         * @param value exact wire value
         */
        ResponseMode(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact case-sensitive wire value.
         *
         * @return exact wire value
         */
        public String value() {
            return value;
        }
    }

    /**
     * PKCE transformation methods admitted by authorization-code processing.
     */
    public enum CodeChallengeMethod {

        /**
         * SHA-256 PKCE transformation.
         */
        S256("S256");

        /**
         * Exact wire value.
         */
        private final String value;

        /**
         * Creates the fixed PKCE method registration.
         *
         * @param value exact wire value
         */
        CodeChallengeMethod(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact case-sensitive wire value.
         *
         * @return exact wire value
         */
        public String value() {
            return value;
        }
    }

    /**
     * Access-token types issued and disclosed by the protocol engine.
     */
    public enum TokenType {

        /**
         * RFC 6750 bearer token.
         */
        BEARER("Bearer");

        /**
         * Exact response value.
         */
        private final String value;

        /**
         * Creates the fixed token-type registration.
         *
         * @param value exact response value
         */
        TokenType(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact response value.
         *
         * @return exact response value
         */
        public String value() {
            return value;
        }
    }

    /**
     * Token type hints accepted by introspection and revocation.
     */
    public enum TokenTypeHint {

        /**
         * Access-token hint.
         */
        ACCESS_TOKEN("access_token"),

        /**
         * Refresh-token hint.
         */
        REFRESH_TOKEN("refresh_token");

        /**
         * Exact wire value.
         */
        private final String value;

        /**
         * Creates one token-type hint registration.
         *
         * @param value exact wire value
         */
        TokenTypeHint(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact case-sensitive wire value.
         *
         * @return exact wire value
         */
        public String value() {
            return value;
        }
    }

    /**
     * Stable OAuth wire errors that are not represented by generic Bus errors.
     */
    public enum ProtocolError implements Errors {

        /**
         * The request is malformed, duplicated, incomplete, or otherwise invalid.
         */
        INVALID_REQUEST("invalid_request", "The OAuth request is invalid"),

        /**
         * Client authentication failed.
         */
        INVALID_CLIENT("invalid_client", "Client authentication failed"),

        /**
         * The authorization grant or refresh token is invalid.
         */
        INVALID_GRANT("invalid_grant", "The authorization grant is invalid"),

        /**
         * The authenticated client cannot use the requested grant.
         */
        UNAUTHORIZED_CLIENT("unauthorized_client", "The client is not authorized for this request"),

        /**
         * The authorization server does not support the requested grant.
         */
        UNSUPPORTED_GRANT_TYPE("unsupported_grant_type", "The requested grant type is not supported"),

        /**
         * The requested scope is invalid or exceeds authorization.
         */
        INVALID_SCOPE("invalid_scope", "The requested scope is invalid"),

        /**
         * The resource owner or policy denied authorization.
         */
        ACCESS_DENIED("access_denied", "The authorization request was denied"),

        /**
         * The requested authorization response type is unsupported.
         */
        UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", "The requested response type is not supported"),

        /**
         * Device authorization has not completed.
         */
        AUTHORIZATION_PENDING("authorization_pending", "Device authorization is pending"),

        /**
         * Device polling is faster than the allowed interval.
         */
        SLOW_DOWN("slow_down", "Device authorization polling must slow down"),

        /**
         * A device or one-time authorization transaction expired.
         */
        EXPIRED_TOKEN("expired_token", "The authorization transaction expired"),

        /**
         * The service cannot currently process the request.
         */
        TEMPORARILY_UNAVAILABLE("temporarily_unavailable", "The authorization service is temporarily unavailable"),

        /**
         * The pushed request URI is invalid, expired, or already consumed.
         */
        INVALID_REQUEST_URI("invalid_request_uri", "The pushed authorization request URI is invalid"),

        /**
         * The JWT-secured authorization request is invalid.
         */
        INVALID_REQUEST_OBJECT("invalid_request_object", "The authorization request object is invalid"),

        /**
         * JWT-secured authorization requests are unavailable for the client.
         */
        REQUEST_NOT_SUPPORTED("request_not_supported", "Authorization request objects are not supported"),

        /**
         * Pushed request URIs are unavailable for the client.
         */
        REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported", "Authorization request URIs are not supported");

        /**
         * Stable wire key.
         */
        private final String key;

        /**
         * Fixed safe wire description.
         */
        private final String value;

        /**
         * Creates one unregistered protocol error.
         *
         * @param key   standard OAuth error key
         * @param value fixed safe description
         */
        ProtocolError(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the standard OAuth error key.
         *
         * @return standard OAuth error key
         */
        @Override
        public String getKey() {
            return key;
        }

        /**
         * Returns the fixed safe OAuth error description.
         *
         * @return fixed safe description
         */
        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Fixed OAuth operation surface implemented by the internal protocol engine.
     */
    public interface Engine {

        /**
         * Processes an authorization endpoint request.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded authorization request
         * @param decision   product authorization-page decision
         * @return stage containing an authorization response or stable failure
         */
        CompletionStage<Outcome<AuthorizationResponse>> authorize(
                Invocation invocation,
                AuthorizationRequest request,
                AuthorizationDecision decision);

        /**
         * Processes a token endpoint request for an allowed grant.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded token request
         * @return stage containing a token response or stable failure
         */
        CompletionStage<Outcome<TokenResponse>> token(Invocation invocation, TokenRequest request);

        /**
         * Creates a device authorization transaction.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded device authorization request
         * @return stage containing device authorization details or stable failure
         */
        CompletionStage<Outcome<DeviceAuthorizationResponse>> device(
                Invocation invocation,
                DeviceAuthorizationRequest request);

        /**
         * Completes or denies a pending device authorization transaction.
         *
         * @param invocation tenant-scoped operation context
         * @param request    product verification-page decision
         * @return stage containing completion or stable failure
         */
        CompletionStage<Outcome<Void>> completeDevice(Invocation invocation, DeviceVerificationRequest request);

        /**
         * Stores a pushed authorization request.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded pushed authorization request
         * @return stage containing a one-time request URI or stable failure
         */
        CompletionStage<Outcome<PushedAuthorizationResponse>> push(
                Invocation invocation,
                PushedAuthorizationRequest request);

        /**
         * Returns the authorized active state of one token.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded introspection request
         * @return stage containing minimal token state or stable failure
         */
        CompletionStage<Outcome<IntrospectionResponse>> introspect(Invocation invocation, IntrospectionRequest request);

        /**
         * Idempotently revokes one access or refresh token.
         *
         * @param invocation tenant-scoped operation context
         * @param request    bounded revocation request
         * @return stage containing completion or stable failure
         */
        CompletionStage<Outcome<Void>> revoke(Invocation invocation, RevocationRequest request);
    }

    /**
     * Immutable product-selected OAuth security policy shared by every flow.
     *
     * @param issuer                exact token issuer
     * @param audiences             non-empty access-token audiences
     * @param scopes                complete server scope allowlist
     * @param grants                complete server grant allowlist
     * @param tokenAlgorithm        trusted JWT signing algorithm
     * @param signingKeyId          trusted signing key identifier
     * @param accessTokenLifetime   positive access-token lifetime
     * @param refreshTokenLifetime  positive refresh-token family lifetime
     * @param deviceVerificationUri exact HTTPS device verification URI
     */
    public record Policy(String issuer, Set<String> audiences, Set<String> scopes, Set<GrantType> grants,
            TrustedAlgorithm tokenAlgorithm, String signingKeyId, Duration accessTokenLifetime,
            Duration refreshTokenLifetime, URI deviceVerificationUri) {

        /**
         * Validates trusted policy and snapshots all allowlists.
         *
         * @param issuer                exact token issuer
         * @param audiences             access-token audiences
         * @param scopes                server scopes
         * @param grants                server grants
         * @param tokenAlgorithm        trusted token algorithm
         * @param signingKeyId          signing key identifier
         * @param accessTokenLifetime   access-token lifetime
         * @param refreshTokenLifetime  refresh-token family lifetime
         * @param deviceVerificationUri device verification URI
         */
        public Policy {
            issuer = Assert.notBlank(issuer, "OAuth issuer must be not blank!");
            audiences = snapshotScopes(audiences);
            Assert.isTrue(!audiences.isEmpty(), "OAuth audiences must be not empty!");
            scopes = snapshotScopes(scopes);
            grants = grants == null || grants.isEmpty() ? Set.of()
                    : Collections.unmodifiableSet(new LinkedHashSet<>(grants));
            Assert.isTrue(!grants.isEmpty(), "OAuth grants must be not empty!");
            tokenAlgorithm = Assert.notNull(tokenAlgorithm, "OAuth token algorithm must be not null!");
            signingKeyId = Assert.notBlank(signingKeyId, "OAuth signing key identifier must be not blank!");
            accessTokenLifetime = positive(accessTokenLifetime, "OAuth access-token lifetime");
            refreshTokenLifetime = positive(refreshTokenLifetime, "OAuth refresh-token lifetime");
            deviceVerificationUri = Assert
                    .notNull(deviceVerificationUri, "OAuth device verification URI must be not null!");
            Assert.isTrue(
                    deviceVerificationUri.isAbsolute() && !deviceVerificationUri.isOpaque()
                            && "https".equals(deviceVerificationUri.getScheme())
                            && deviceVerificationUri.getHost() != null && deviceVerificationUri.getUserInfo() == null
                            && deviceVerificationUri.getFragment() == null,
                    "OAuth device verification URI must be an absolute HTTPS URI!");
        }
    }

    /**
     * Immutable authorization-page decision supplied by the product layer.
     *
     * @param subjectId      authenticated subject identifier
     * @param approvedScopes scopes explicitly approved by the subject and policy
     * @param approved       whether authorization was approved
     */
    public record AuthorizationDecision(String subjectId, Set<String> approvedScopes, boolean approved) {

        /**
         * Snapshots approved scopes without accepting credentials or page state.
         *
         * @param subjectId      authenticated subject identifier
         * @param approvedScopes approved scopes
         * @param approved       approval decision
         */
        public AuthorizationDecision {
            approvedScopes = snapshotScopes(approvedScopes);
        }
    }

    /**
     * Immutable authorization endpoint input.
     *
     * @param clientId            client identifier
     * @param redirectUri         requested redirect URI
     * @param scopes              requested scopes
     * @param state               optional client state
     * @param codeChallenge       mandatory authorization-code PKCE challenge
     * @param codeChallengeMethod fixed PKCE method
     * @param requestObject       optional JWT-secured authorization request
     * @param requestUri          optional one-time pushed request URI
     * @param responseMode        requested response mode
     * @param nonce               optional OpenID Connect nonce
     */
    public record AuthorizationRequest(String clientId, URI redirectUri, Set<String> scopes, String state,
            String codeChallenge, CodeChallengeMethod codeChallengeMethod, String requestObject, String requestUri,
            ResponseMode responseMode, String nonce) {

        /**
         * Snapshots authorization request state without performing network-input validation.
         *
         * @param clientId            client identifier
         * @param redirectUri         requested redirect URI
         * @param scopes              requested scopes
         * @param state               optional client state
         * @param codeChallenge       PKCE challenge
         * @param codeChallengeMethod fixed PKCE method
         * @param requestObject       optional request object
         * @param requestUri          optional pushed request URI
         * @param responseMode        response mode
         * @param nonce               optional nonce
         */
        public AuthorizationRequest {
            scopes = snapshotScopes(scopes);
        }

        /**
         * Redacts state, PKCE, request-object, and pushed-request artifacts from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "AuthorizationRequest[REDACTED]";
        }
    }

    /**
     * Immutable successful authorization endpoint output.
     *
     * @param redirectUri exact validated redirect URI
     * @param code        optional one-time authorization code
     * @param state       optional client state copied without transformation
     * @param response    optional JWT-secured authorization response
     */
    public record AuthorizationResponse(URI redirectUri, String code, String state, String response) {

        /**
         * Redacts authorization artifacts from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "AuthorizationResponse[REDACTED]";
        }
    }

    /**
     * Immutable token endpoint input covering only enabled grants.
     *
     * @param grantType    selected enabled grant
     * @param clientId     client identifier
     * @param clientSecret optional copied client secret
     * @param code         optional authorization code
     * @param redirectUri  optional exact redirect URI
     * @param codeVerifier optional PKCE verifier
     * @param refreshToken optional rotating refresh token
     * @param deviceCode   optional device code
     * @param scopes       requested scopes
     */
    public record TokenRequest(GrantType grantType, String clientId, char[] clientSecret, String code, URI redirectUri,
            String codeVerifier, String refreshToken, String deviceCode, Set<String> scopes) {

        /**
         * Copies token request secrets and scopes.
         *
         * @param grantType    selected enabled grant
         * @param clientId     client identifier
         * @param clientSecret optional client secret
         * @param code         optional authorization code
         * @param redirectUri  optional redirect URI
         * @param codeVerifier optional PKCE verifier
         * @param refreshToken optional refresh token
         * @param deviceCode   optional device code
         * @param scopes       requested scopes
         */
        public TokenRequest {
            clientSecret = characters(clientSecret);
            scopes = snapshotScopes(scopes);
        }

        /**
         * Returns an independent client-secret copy.
         *
         * @return copied client secret
         */
        @Override
        public char[] clientSecret() {
            return clientSecret.clone();
        }

        /**
         * Redacts every credential and token from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "TokenRequest[REDACTED]";
        }
    }

    /**
     * Immutable token endpoint output.
     *
     * @param accessToken  issued access token
     * @param tokenType    fixed token type
     * @param expiresIn    positive lifetime in seconds
     * @param scopes       granted scopes
     * @param refreshToken optional rotated refresh token
     * @param idToken      optional OpenID Connect ID token
     */
    public record TokenResponse(String accessToken, TokenType tokenType, long expiresIn, Set<String> scopes,
            String refreshToken, String idToken) {

        /**
         * Snapshots granted scopes.
         *
         * @param accessToken  issued access token
         * @param tokenType    fixed token type
         * @param expiresIn    lifetime in seconds
         * @param scopes       granted scopes
         * @param refreshToken optional refresh token
         * @param idToken      optional ID token
         */
        public TokenResponse {
            scopes = snapshotScopes(scopes);
        }

        /**
         * Redacts issued tokens from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "TokenResponse[REDACTED]";
        }
    }

    /**
     * Immutable device authorization endpoint input.
     *
     * @param clientId client identifier
     * @param scopes   requested scopes
     */
    public record DeviceAuthorizationRequest(String clientId, Set<String> scopes) {

        /**
         * Snapshots requested scopes.
         *
         * @param clientId client identifier
         * @param scopes   requested scopes
         */
        public DeviceAuthorizationRequest {
            scopes = snapshotScopes(scopes);
        }
    }

    /**
     * Immutable device authorization endpoint output.
     *
     * @param deviceCode              secret device code
     * @param userCode                user-facing verification code
     * @param verificationUri         verification page URI
     * @param verificationUriComplete optional pre-populated verification URI
     * @param expiresIn               lifetime in seconds
     * @param interval                minimum polling interval in seconds
     */
    public record DeviceAuthorizationResponse(String deviceCode, String userCode, URI verificationUri,
            URI verificationUriComplete, long expiresIn, long interval) {

        /**
         * Redacts device credentials from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "DeviceAuthorizationResponse[REDACTED]";
        }
    }

    /**
     * Immutable verification-page decision for a pending device transaction.
     *
     * @param userCode       user-entered verification code
     * @param subjectId      authenticated subject identifier
     * @param approvedScopes scopes approved by the subject and policy
     * @param approved       whether device authorization was approved
     */
    public record DeviceVerificationRequest(String userCode, String subjectId, Set<String> approvedScopes,
            boolean approved) {

        /**
         * Snapshots approved scopes.
         *
         * @param userCode       user verification code
         * @param subjectId      subject identifier
         * @param approvedScopes approved scopes
         * @param approved       approval decision
         */
        public DeviceVerificationRequest {
            approvedScopes = snapshotScopes(approvedScopes);
        }

        /**
         * Redacts the user code from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "DeviceVerificationRequest[REDACTED]";
        }
    }

    /**
     * Immutable pushed authorization endpoint input.
     *
     * @param authorization authorization request to validate and store
     * @param clientSecret  optional copied client authentication secret
     */
    public record PushedAuthorizationRequest(AuthorizationRequest authorization, char[] clientSecret) {

        /**
         * Validates programming contracts and copies the client secret.
         *
         * @param authorization authorization request
         * @param clientSecret  optional client secret
         */
        public PushedAuthorizationRequest {
            authorization = Assert.notNull(authorization, "Authorization request must be not null!");
            clientSecret = characters(clientSecret);
        }

        /**
         * Returns an independent client-secret copy.
         *
         * @return copied client secret
         */
        @Override
        public char[] clientSecret() {
            return clientSecret.clone();
        }

        /**
         * Redacts the pushed request and credential from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "PushedAuthorizationRequest[REDACTED]";
        }
    }

    /**
     * Immutable pushed authorization endpoint output.
     *
     * @param requestUri one-time pushed request URI
     * @param expiresIn  lifetime in seconds
     */
    public record PushedAuthorizationResponse(String requestUri, long expiresIn) {

        /**
         * Redacts the one-time request URI from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "PushedAuthorizationResponse[REDACTED]";
        }
    }

    /**
     * Immutable token introspection endpoint input.
     *
     * @param token        token presented for introspection
     * @param hint         optional token type hint
     * @param clientId     authenticated client identifier
     * @param clientSecret copied client authentication secret
     */
    public record IntrospectionRequest(String token, TokenTypeHint hint, String clientId, char[] clientSecret) {

        /**
         * Copies the client authentication secret.
         *
         * @param token        presented token
         * @param hint         optional token hint
         * @param clientId     client identifier
         * @param clientSecret client secret
         */
        public IntrospectionRequest {
            clientSecret = characters(clientSecret);
        }

        /**
         * Returns an independent client-secret copy.
         *
         * @return copied client secret
         */
        @Override
        public char[] clientSecret() {
            return clientSecret.clone();
        }

        /**
         * Redacts token and client credentials from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "IntrospectionRequest[REDACTED]";
        }
    }

    /**
     * Immutable minimal token introspection output.
     *
     * @param active    whether the token is active and authorized for disclosure
     * @param clientId  authorized client identifier
     * @param subjectId authorized subject identifier
     * @param scopes    granted scopes
     * @param issuedAt  optional issue time
     * @param expiresAt optional expiration time
     * @param tokenType optional fixed token type
     */
    public record IntrospectionResponse(boolean active, String clientId, String subjectId, Set<String> scopes,
            Instant issuedAt, Instant expiresAt, TokenType tokenType) {

        /**
         * Snapshots disclosed scopes and suppresses fields for inactive tokens.
         *
         * @param active    active state
         * @param clientId  client identifier
         * @param subjectId subject identifier
         * @param scopes    granted scopes
         * @param issuedAt  issue time
         * @param expiresAt expiration time
         * @param tokenType fixed token type
         */
        public IntrospectionResponse {
            scopes = snapshotScopes(scopes);
            if (!active) {
                clientId = null;
                subjectId = null;
                scopes = Set.of();
                issuedAt = null;
                expiresAt = null;
                tokenType = null;
            }
        }
    }

    /**
     * Immutable token revocation endpoint input.
     *
     * @param token        token presented for revocation
     * @param hint         optional token type hint
     * @param clientId     authenticated client identifier
     * @param clientSecret copied client authentication secret
     */
    public record RevocationRequest(String token, TokenTypeHint hint, String clientId, char[] clientSecret) {

        /**
         * Copies the client authentication secret.
         *
         * @param token        presented token
         * @param hint         optional token hint
         * @param clientId     client identifier
         * @param clientSecret client secret
         */
        public RevocationRequest {
            clientSecret = characters(clientSecret);
        }

        /**
         * Returns an independent client-secret copy.
         *
         * @return copied client secret
         */
        @Override
        public char[] clientSecret() {
            return clientSecret.clone();
        }

        /**
         * Redacts token and client credentials from diagnostic output.
         *
         * @return fixed redacted representation
         */
        @Override
        public String toString() {
            return "RevocationRequest[REDACTED]";
        }
    }

}
