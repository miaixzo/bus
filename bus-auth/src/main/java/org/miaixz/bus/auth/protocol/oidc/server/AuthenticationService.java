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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.AuthorizationCodeCache;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.protocol.oauth2.grant.AuthorizationCodeIssuer;
import org.miaixz.bus.auth.protocol.oidc.AuthenticationRequest;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.worker.SessionCoordinator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates an OpenID Connect authentication interaction and issues its OAuth authorization code.
 * <p>
 * The external request boundary owns end-user authentication. This service requires the resulting subject and active
 * authentication event in {@link Context}, binds those facts to the authorization code atomically, and never issues an
 * ID Token at the authorization endpoint.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthenticationService {

    /**
     * OpenID Connect authorization error indicating that end-user login is required.
     */
    private static final OAuth2ErrorCode LOGIN_REQUIRED = new OAuth2ErrorCode("login_required");

    /**
     * OpenID Connect authorization error indicating that required authentication properties were not met.
     */
    private static final OAuth2ErrorCode UNMET_AUTHENTICATION_REQUIREMENTS = new OAuth2ErrorCode(
            "unmet_authentication_requirements");

    /**
     * Internal OAuth authorization-code issuer that persists the typed OpenID Connect binding.
     */
    private final AuthorizationCodeIssuer issuer;

    /**
     * Source-isolated coordinator used to validate the active authentication Session.
     */
    private final SessionCoordinator sessions;

    /**
     * Creates the OpenID Connect authentication service.
     *
     * @param issuer   shared OAuth authorization-code issuer
     * @param sessions Source-isolated authentication Session coordinator
     * @throws IllegalArgumentException if {@code issuer} is {@code null}
     */
    public AuthenticationService(final AuthorizationCodeIssuer issuer, final SessionCoordinator sessions) {
        this.issuer = Assert.notNull(issuer, "OpenID Connect authorization code issuer must not be null");
        this.sessions = Assert.notNull(sessions, "OpenID Connect Session coordinator must not be null");
    }

    /**
     * Tests the standard maximum authentication age without changing the shared clock.
     *
     * @param maximumAge      optional maximum age in seconds
     * @param authenticatedAt authentication event instant
     * @param now             current shared-clock instant
     * @return whether the authentication event is too old or the calculation overflows
     */
    private static boolean maximumAgeExceeded(final Long maximumAge, final Instant authenticatedAt, final Instant now) {
        if (maximumAge == null) {
            return false;
        }
        try {
            return authenticatedAt.plusSeconds(maximumAge).isBefore(now);
        } catch (ArithmeticException exception) {
            return true;
        }
    }

    /**
     * Evaluates essential authentication claims from the standard claims request object.
     *
     * @param requestedClaims optional complete claims request
     * @param authentication  actual authentication facts
     * @return whether every essential auth_time, acr, and amr request can be satisfied
     */
    private static boolean essentialClaimsSatisfied(
            final Optional<JsonValue.ObjectValue> requestedClaims,
            final Context.Authentication authentication) {
        final JsonValue.ObjectValue root = requestedClaims.getOrNull();
        if (root == null) {
            return true;
        }
        final JsonValue target = root.values().get(OpenIdConnect.Claims.ID_TOKEN);
        if (target == null) {
            return true;
        }
        if (!(target instanceof JsonValue.ObjectValue claims)) {
            return false;
        }
        if (essential(claims.values().get(OpenIdConnect.Claims.AUTH_TIME)) && authentication.session() == null) {
            return false;
        }
        if (essential(claims.values().get(OpenIdConnect.Claims.ACR))) {
            final String actual = authentication.authenticationContextClass().getOrNull();
            if (actual == null || !requestedValue(claims.values().get(OpenIdConnect.Claims.ACR), actual)) {
                return false;
            }
        }
        return !essential(claims.values().get(OpenIdConnect.Claims.AMR))
                || !authentication.authenticationMethods().isEmpty();
    }

    /**
     * Reports whether one individual claim request marks the claim as essential.
     *
     * @param request claim request value, JSON null, or {@code null}
     * @return whether the value is an object containing boolean {@code essential=true}
     */
    private static boolean essential(final JsonValue request) {
        return request instanceof JsonValue.ObjectValue object
                && object.values().get(OpenIdConnect.Claims.ESSENTIAL) instanceof JsonValue.BooleanValue flag
                && flag.value();
    }

    /**
     * Checks an optional standard claim {@code value} or {@code values} restriction against the actual value.
     *
     * @param request individual claim request object
     * @param actual  actual authentication claim value
     * @return whether no restriction exists or one exact requested value matches
     */
    private static boolean requestedValue(final JsonValue request, final String actual) {
        if (!(request instanceof JsonValue.ObjectValue object)) {
            return true;
        }
        final JsonValue value = object.values().get(OpenIdConnect.Claims.VALUE);
        if (value != null) {
            return value instanceof JsonValue.StringValue string && actual.equals(string.value());
        }
        final JsonValue values = object.values().get(OpenIdConnect.Claims.VALUES);
        if (values == null) {
            return true;
        }
        if (!(values instanceof JsonValue.ArrayValue array)) {
            return false;
        }
        return array.values().stream()
                .anyMatch(item -> item instanceof JsonValue.StringValue string && actual.equals(string.value()));
    }

    /**
     * Creates a closed failure carrying only the registered authorization error identifier.
     *
     * @param error         shared Bus error definition
     * @param protocolError registered OAuth or OpenID Connect error
     * @param description   non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode protocolError,
            final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(
                Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(protocolError.value()))));
    }

    /**
     * Creates an already completed asynchronous outcome.
     *
     * @param outcome completed authentication outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<AuthorizationResponse>> completed(
            final Outcome<AuthorizationResponse> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Authorizes one code-flow Authentication Request for the externally authenticated subject.
     *
     * @param request standard OpenID Connect Authentication Request
     * @param context invocation context carrying the paired subject and authentication event
     * @param timeout shared end-to-end operation timeout
     * @return stage containing the standard authentication response, expected rejection, or operational failure
     */
    public CompletionStage<Outcome<AuthorizationResponse>> authorize(
            final AuthenticationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect Authentication Request must not be null");
        Assert.notNull(context, "OpenID Connect authentication context must not be null");
        Assert.notNull(timeout, "OpenID Connect authentication timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OpenID Connect authentication has no remaining timeout")));
        }
        final Subject subject = context.authenticatedSubject().getOrNull();
        final Context.Authentication authentication = context.authentication().getOrNull();
        if (subject == null || authentication == null) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    LOGIN_REQUIRED,
                                    "OpenID Connect authentication requires an authenticated subject and session")));
        }
        final Instant now = timeout.clock().now();
        final Session session = authentication.session();
        if (session.state() != Session.State.ACTIVE || !session.expiresAt().isAfter(now)
                || maximumAgeExceeded(request.maxAge().getOrNull(), session.issuedAt(), now)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    LOGIN_REQUIRED,
                                    "OpenID Connect authentication requires a current end-user login")));
        }
        if (!essentialClaimsSatisfied(request.claims(), authentication)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    UNMET_AUTHENTICATION_REQUIREMENTS,
                                    "OpenID Connect essential authentication claims cannot be satisfied")));
        }

        final AuthorizationCodeCache.OpenIdBinding binding = new AuthorizationCodeCache.OpenIdBinding(request.nonce(),
                session.issuedAt(), authentication.authenticationContextClass(), authentication.authenticationMethods(),
                session.key(), request.claims(), Optional.empty());
        return sessions.establish(session, context, timeout).thenCompose(established -> switch (established) {
            case Outcome.Succeeded<Void> ignored -> issuer.authorize(
                    AuthorizationCodeIssuer.Request.openId(request.authorizationRequest(), binding),
                    context,
                    timeout).thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<AuthorizationCodeIssuer.Result> success -> Outcome
                                .succeeded(success.value().response());
                        case Outcome.Rejected<AuthorizationCodeIssuer.Result> rejected -> Outcome
                                .rejected(rejected.failure());
                        case Outcome.Failed<AuthorizationCodeIssuer.Result> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
            case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
