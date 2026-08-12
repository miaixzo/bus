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
package org.miaixz.bus.auth.metric.oauth2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.miaixz.bus.auth.metric.AuthMetric;
import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides the single production implementation of the public OAuth protocol engine.
 * <p>
 * This class fixes endpoint routing, PAR and JAR canonicalization, optional JARM protection, grant dispatch, and safe
 * outcome conversion. Protocol exceptions become stable rejections; unexpected port or runtime failures become redacted
 * failed outcomes and are never serialized through OAuth wire responses.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OAuth2Engine implements OAuth2.Engine {

    /**
     * Authorization-code flow.
     */
    private final AuthorizationCodeFlow authorizationCode;

    /**
     * Client-credentials flow.
     */
    private final ClientCredentialsFlow clientCredentials;

    /**
     * Refresh-token flow.
     */
    private final RefreshTokenFlow refreshToken;

    /**
     * Device authorization flow.
     */
    private final DeviceAuthorizationFlow deviceAuthorization;

    /**
     * Pushed authorization request service.
     */
    private final PushedAuthorizationRequest pushedAuthorization;

    /**
     * JWT-secured request verifier.
     */
    private final JwtAuthorizationRequest jwtRequest;

    /**
     * JWT-secured response signer.
     */
    private final JwtAuthorizationResponse jwtResponse;

    /**
     * Token introspection service.
     */
    private final TokenIntrospection tokenIntrospection;

    /**
     * Token revocation service.
     */
    private final TokenRevocation tokenRevocation;

    /**
     * Creates the fully assembled OAuth engine.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public OAuth2Engine(final Policy policy, final Runtime runtime) {
        final Policy trusted = Assert.notNull(policy, "OAuth policy must be not null!");
        final Runtime ports = Assert.notNull(runtime, "Authentication runtime must be not null!");
        authorizationCode = new AuthorizationCodeFlow(trusted, ports);
        clientCredentials = new ClientCredentialsFlow(trusted, ports);
        refreshToken = new RefreshTokenFlow(trusted, ports);
        deviceAuthorization = new DeviceAuthorizationFlow(trusted, ports);
        pushedAuthorization = new PushedAuthorizationRequest(trusted, ports);
        jwtRequest = new JwtAuthorizationRequest(trusted, ports);
        jwtResponse = new JwtAuthorizationResponse(trusted, ports);
        tokenIntrospection = new TokenIntrospection(ports);
        tokenRevocation = new TokenRevocation(ports);
    }

    /**
     * Executes one asynchronous operation and converts its terminal state to the closed outcome algebra.
     *
     * @param operation deferred operation
     * @param <T>       success type
     * @return stage containing a safe outcome
     */
    private static <T> CompletionStage<Outcome<T>> outcome(final Supplier<CompletionStage<T>> operation) {
        final CompletionStage<T> stage;
        try {
            stage = Assert.notNull(operation.get(), "OAuth operation stage must be not null!");
        } catch (final Throwable failure) {
            return CompletableFuture.completedFuture(failure(failure));
        }
        return stage.handle((value, failure) -> failure == null ? new Success<>(value) : failure(failure));
    }

    /**
     * Executes one Void operation and uses the sole framework completion factory.
     *
     * @param operation deferred Void operation
     * @return stage containing a safe completion outcome
     */
    private static CompletionStage<Outcome<Void>> completion(final Supplier<CompletionStage<Void>> operation) {
        final CompletionStage<Void> stage;
        try {
            stage = Assert.notNull(operation.get(), "OAuth operation stage must be not null!");
        } catch (final Throwable failure) {
            return CompletableFuture.completedFuture(failure(failure));
        }
        return stage.handle((value, failure) -> failure == null ? AuthMetric.completed() : failure(failure));
    }

    /**
     * Converts one failure without disclosing its message or cause through protocol rejection fields.
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
            final ProtocolError error = protocolError(protocol);
            if (error == null) {
                return new Failed<>(new Failure(FailureKind.INTERNAL, ProtocolError.TEMPORARILY_UNAVAILABLE, true),
                        cause);
            }
            final FailureKind kind = kind(error);
            final boolean retryable = error == ProtocolError.TEMPORARILY_UNAVAILABLE || error == ProtocolError.SLOW_DOWN
                    || error == ProtocolError.AUTHORIZATION_PENDING;
            return new Rejected<>(new Failure(kind, error, retryable));
        }
        return new Failed<>(new Failure(FailureKind.INTERNAL, ProtocolError.TEMPORARILY_UNAVAILABLE, true), cause);
    }

    /**
     * Resolves a protocol exception to the fixed OAuth error registry.
     *
     * @param failure protocol exception
     * @return fixed OAuth error
     */
    private static ProtocolError protocolError(final ProtocolException failure) {
        for (final ProtocolError error : ProtocolError.values()) {
            if (error.getKey().equals(failure.getErrcode())) {
                return error;
            }
        }
        return null;
    }

    /**
     * Requires a represented grant before token dispatch.
     *
     * @param request token request
     * @return selected grant
     */
    private static GrantType grant(final TokenRequest request) {
        if (request.grantType() == null) {
            throw new ProtocolException(ProtocolError.UNSUPPORTED_GRANT_TYPE);
        }
        return request.grantType();
    }

    /**
     * Classifies one fixed OAuth error.
     *
     * @param error fixed OAuth error
     * @return stable failure kind
     */
    private static FailureKind kind(final ProtocolError error) {
        return switch (error) {
            case INVALID_CLIENT -> FailureKind.AUTHENTICATION;
            case ACCESS_DENIED, UNAUTHORIZED_CLIENT -> FailureKind.AUTHORIZATION;
            case SLOW_DOWN -> FailureKind.RATE_LIMIT;
            case AUTHORIZATION_PENDING, EXPIRED_TOKEN, INVALID_GRANT, INVALID_REQUEST_URI -> FailureKind.CONFLICT;
            case TEMPORARILY_UNAVAILABLE -> FailureKind.REMOTE;
            default -> FailureKind.VALIDATION;
        };
    }

    /**
     * Processes one authorization endpoint operation.
     *
     * @param invocation tenant-scoped operation context
     * @param request    authorization request
     * @param decision   product authorization decision
     * @return stage containing a closed authorization outcome
     */
    @Override
    public CompletionStage<Outcome<AuthorizationResponse>> authorize(
            final Invocation invocation,
            final AuthorizationRequest request,
            final AuthorizationDecision decision) {
        return outcome(
                () -> canonical(invocation, request).thenCompose(
                        canonical -> authorizationCode.authorize(invocation, canonical, decision)
                                .thenCompose(response -> secured(invocation, canonical, response))));
    }

    /**
     * Routes one token request to its selected enabled grant.
     *
     * @param invocation tenant-scoped operation context
     * @param request    token request
     * @return stage containing a closed token outcome
     */
    @Override
    public CompletionStage<Outcome<TokenResponse>> token(final Invocation invocation, final TokenRequest request) {
        return outcome(() -> switch (grant(Assert.notNull(request, "Token request must be not null!"))) {
            case AUTHORIZATION_CODE -> authorizationCode.exchange(invocation, request);
            case CLIENT_CREDENTIALS -> clientCredentials.exchange(invocation, request);
            case REFRESH_TOKEN -> refreshToken.exchange(invocation, request);
            case DEVICE_CODE -> deviceAuthorization.exchange(invocation, request);
        });
    }

    /**
     * Creates one device authorization transaction.
     *
     * @param invocation tenant-scoped operation context
     * @param request    device authorization request
     * @return stage containing a closed device outcome
     */
    @Override
    public CompletionStage<Outcome<DeviceAuthorizationResponse>> device(
            final Invocation invocation,
            final DeviceAuthorizationRequest request) {
        return outcome(() -> deviceAuthorization.authorize(invocation, request));
    }

    /**
     * Completes one pending device transaction.
     *
     * @param invocation tenant-scoped operation context
     * @param request    verification-page decision
     * @return stage containing a closed completion outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> completeDevice(
            final Invocation invocation,
            final DeviceVerificationRequest request) {
        return completion(() -> deviceAuthorization.complete(invocation, request));
    }

    /**
     * Stores one pushed authorization request.
     *
     * @param invocation tenant-scoped operation context
     * @param request    pushed authorization request
     * @return stage containing a closed pushed-request outcome
     */
    @Override
    public CompletionStage<Outcome<PushedAuthorizationResponse>> push(
            final Invocation invocation,
            final OAuth2.PushedAuthorizationRequest request) {
        return outcome(() -> pushedAuthorization.push(invocation, request));
    }

    /**
     * Introspects one opaque token for an authenticated client.
     *
     * @param invocation tenant-scoped operation context
     * @param request    introspection request
     * @return stage containing a closed introspection outcome
     */
    @Override
    public CompletionStage<Outcome<IntrospectionResponse>> introspect(
            final Invocation invocation,
            final IntrospectionRequest request) {
        return outcome(() -> tokenIntrospection.introspect(invocation, request));
    }

    /**
     * Idempotently revokes one opaque token.
     *
     * @param invocation tenant-scoped operation context
     * @param request    revocation request
     * @return stage containing a closed completion outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> revoke(final Invocation invocation, final RevocationRequest request) {
        return completion(() -> tokenRevocation.revoke(invocation, request));
    }

    /**
     * Resolves a one-time PAR or verifies JAR before authorization processing.
     *
     * @param invocation tenant-scoped operation context
     * @param request    authorization request
     * @return canonical authorization request stage
     */
    private CompletionStage<AuthorizationRequest> canonical(
            final Invocation invocation,
            final AuthorizationRequest request) {
        final AuthorizationRequest input = Assert.notNull(request, "Authorization request must be not null!");
        if (StringKit.isNotBlank(input.requestUri())) {
            return pushedAuthorization.take(invocation, input.requestUri(), input.clientId());
        }
        if (StringKit.isNotBlank(input.requestObject())) {
            return jwtRequest.verify(invocation, input);
        }
        return CompletableFuture.completedFuture(input);
    }

    /**
     * Applies JARM only for the two JWT response modes.
     *
     * @param invocation tenant-scoped operation context
     * @param request    canonical authorization request
     * @param response   authorization-code response
     * @return plain or secured response stage
     */
    private CompletionStage<AuthorizationResponse> secured(
            final Invocation invocation,
            final AuthorizationRequest request,
            final AuthorizationResponse response) {
        return request.responseMode() == ResponseMode.QUERY_JWT || request.responseMode() == ResponseMode.FORM_POST_JWT
                ? jwtResponse.secure(invocation, request.clientId(), response)
                : CompletableFuture.completedFuture(response);
    }

}
