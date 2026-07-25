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
package org.miaixz.bus.fabric.protocol.http.retry;

import java.net.URI;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.chain.HttpChain;
import org.miaixz.bus.fabric.protocol.http.chain.HttpStage;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP retry and redirect stage.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpRetry implements HttpStage {

    /**
     * Cached debug flag that avoids logger capability lookups on every HTTP exchange.
     */
    private static final boolean DEBUG_ENABLED = Logger.isDebugEnabled();

    /**
     * Stable identifier exposed to the HTTP stage chain.
     */
    private final String name;

    /**
     * Policy controlling transport retry and redirect limits.
     */
    private final HttpRetryPolicy policy;

    /**
     * Authenticator that optionally creates 401 and 407 follow-up requests.
     */
    private final HttpAuthenticator authenticator;

    /**
     * Creates a retry stage with default policy.
     */
    public HttpRetry() {
        this(HttpRetryPolicy.defaults(), HttpAuthenticator.none());
    }

    /**
     * Creates a retry stage with an authenticator.
     *
     * @param authenticator non-null authenticator producing authorization follow-ups
     * @throws ValidateException if {@code authenticator} is {@code null}
     */
    public HttpRetry(final HttpAuthenticator authenticator) {
        this(HttpRetryPolicy.defaults(), authenticator);
    }

    /**
     * Creates a retry stage.
     *
     * @param policy        retry and redirect policy
     * @param authenticator authenticator producing authorization follow-ups
     * @throws ValidateException if either collaborator is {@code null}
     */
    public HttpRetry(final HttpRetryPolicy policy, final HttpAuthenticator authenticator) {
        this.name = "http-retry";
        this.policy = require(policy, "Retry policy");
        this.authenticator = require(authenticator, "HTTP authenticator");
    }

    /**
     * Executes retries and follow-ups.
     *
     * @param request initial request to execute and potentially replay
     * @param chain   remaining exchange chain used for first and replayed attempts
     * @return terminal response with prior follow-up responses linked using empty bodies
     * @throws ProtocolException if the configured follow-up limit is exceeded
     * @throws ValidateException if the request or chain is {@code null}
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        final boolean debug = DEBUG_ENABLED;
        int attempt = Normal._0;
        int followUps = Normal._0;
        HttpResponse prior = null;
        boolean first = true;
        final int replayIndex = next.index();
        while (true) {
            try {
                final HttpChain attemptChain = first ? next : next.replayFrom(replayIndex);
                first = false;
                final HttpResponse response = attemptChain.proceed(current);
                final HttpRequest followUp = followUp(response);
                if (followUp == null) {
                    if (debug && (attempt > Normal._0 || followUps > Normal._0)) {
                        Logger.debug(
                                false,
                                "Fabric",
                                "HTTP retry stage completed: method={}, host={}, port={}, code={}, retries={}, "
                                        + "followUps={}",
                                current.method().value(),
                                current.url().host(),
                                current.url().port(),
                                response.code(),
                                attempt,
                                followUps);
                    }
                    return prior == null ? response : response.toBuilder().priorResponse(prior).build();
                }
                if (!followUpAllowed(response.code(), followUps)) {
                    if (debug) {
                        Logger.debug(
                                false,
                                "Fabric",
                                "HTTP follow-up rejected: method={}, host={}, port={}, code={}, acceptedFollowUps={}",
                                current.method().value(),
                                current.url().host(),
                                current.url().port(),
                                response.code(),
                                followUps);
                    }
                    response.close();
                    throw new ProtocolException("Too many HTTP follow-ups");
                }
                if (debug) {
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP follow-up accepted: fromMethod={}, toMethod={}, fromHost={}, toHost={}, code={}, "
                                    + "followUp={}",
                            current.method().value(),
                            followUp.method().value(),
                            current.url().host(),
                            followUp.url().host(),
                            response.code(),
                            followUps + Normal._1);
                }
                prior = response.toBuilder().body(PayloadBody.empty()).priorResponse(prior).build();
                response.close();
                current = followUp;
                followUps++;
                attempt = Normal._0;
            } catch (final RuntimeException e) {
                final boolean recoverable = recover(current, e, attempt);
                if (debug) {
                    final HttpChain.ExchangeFailure failure = e instanceof HttpChain.ExchangeFailure currentFailure
                            ? currentFailure
                            : null;
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP retry decision: retry={}, attempt={}, maxAttempts={}, method={}, host={}, port={}, "
                                    + "delivery={}, reason={}, exception={}",
                            recoverable,
                            attempt,
                            policy.maxAttempts(),
                            current.method().value(),
                            current.url().host(),
                            current.url().port(),
                            failure == null ? "unstructured" : failure.deliveryState(),
                            failure == null ? "unstructured" : failure.reason(),
                            e.getClass().getSimpleName());
                }
                if (!recoverable) {
                    throw e;
                }
                attempt++;
            }
        }
    }

    /**
     * Creates a follow-up request for a response.
     *
     * @param response response whose status and headers determine a follow-up
     * @return redirect or authentication request, or {@code null} when no follow-up is available
     * @throws ValidateException if {@code response} is {@code null}
     */
    public HttpRequest followUp(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        return switch (current.code()) {
            case Http.Status.MULTIPLE_CHOICES, Http.Status.MOVED_PERMANENTLY, Http.Status.FOUND, Http.Status.SEE_OTHER, Http.Status.TEMPORARY_REDIRECT, Http.Status.PERMANENT_REDIRECT -> redirect(
                    current);
            case Http.Status.UNAUTHORIZED, Http.Status.PROXY_AUTHENTICATION_REQUIRED -> authenticator
                    .authenticate(current.request(), current);
            default -> null;
        };
    }

    /**
     * Returns whether a failure can be retried.
     *
     * @param cause   structured exchange failure to classify
     * @param attempt zero-based retry attempt supplied to the policy
     * @return {@code true} when delivery state, failure reason, and retry policy permit another attempt
     * @throws ValidateException if {@code cause} is {@code null}
     */
    public boolean recover(final Throwable cause, final int attempt) {
        final Throwable current = require(cause, "Failure cause");
        if (!(current instanceof HttpChain.ExchangeFailure failure)) {
            return false;
        }
        return retryableDelivery(failure) && retryableReason(failure)
                && policy.retry(failure.getCause() == null ? failure : failure.getCause(), attempt);
    }

    /**
     * Applies request replay and idempotency constraints in addition to the structured failure policy.
     *
     * @param request request considered for replay
     * @param cause   structured exchange failure
     * @param attempt zero-based retry attempt
     * @return {@code true} when policy and request semantics permit replay
     */
    private boolean recover(final HttpRequest request, final Throwable cause, final int attempt) {
        return idempotent(request.method()) && request.body().repeatable() && recover(cause, attempt);
    }

    /**
     * Only network-owner-confirmed safe delivery states can be replayed.
     *
     * @param failure structured exchange failure
     * @return {@code true} when delivery is known safe to repeat
     */
    private static boolean retryableDelivery(final HttpChain.ExchangeFailure failure) {
        return failure.deliveryState() == HttpChain.DeliveryState.NOT_SENT
                || failure.deliveryState() == HttpChain.DeliveryState.PEER_CONFIRMED_UNPROCESSED;
    }

    /**
     * Certificate/protocol/cancellation failures are never automatically recovered.
     *
     * @param failure structured exchange failure
     * @return {@code true} when the failure category allows retry
     */
    private static boolean retryableReason(final HttpChain.ExchangeFailure failure) {
        return failure.reason() != HttpChain.FailureReason.TLS && failure.reason() != HttpChain.FailureReason.PROTOCOL
                && failure.reason() != HttpChain.FailureReason.CANCELLED;
    }

    /**
     * RFC idempotent method set used by automatic transport retries.
     *
     * @param method HTTP method to classify
     * @return {@code true} when the method is idempotent
     */
    private static boolean idempotent(final Http.Method method) {
        return switch (method) {
            case GET, HEAD, PUT, DELETE, OPTIONS, TRACE -> true;
            default -> false;
        };
    }

    /**
     * Returns stage name.
     *
     * @return stable retry-stage identifier
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Creates a redirect request.
     *
     * @param response redirect response containing the original request and optional {@code Location}
     * @return redirect request, or {@code null} when location is absent or a required body cannot be replayed
     */
    private HttpRequest redirect(final HttpResponse response) {
        final String location = response.headers().get(Http.Header.LOCATION);
        if (location == null) {
            return null;
        }
        Assert.isFalse(
                StringKit.isBlank(location) || StringKit.containsAny(location, Symbol.C_CR, Symbol.C_LF),
                () -> new ProtocolException("Invalid redirect location"));
        final URI uri = response.request().url().toUri().resolve(location);
        final UnoUrl url = UnoUrl.parse(uri.toString());
        final HttpRequest request = response.request();
        final Http.Method method = redirectMethod(response.code(), request.method());
        final boolean preserveBody = preserveBody(response.code(), request.method());
        if (preserveBody && request.body().length() > Normal._0 && !request.body().repeatable()) {
            return null;
        }
        final HttpRequest.Builder builder = request.toBuilder().method(method).url(url)
                .headers(redirectHeaders(request.headers(), request.url(), url));
        if (preserveBody && method.permitsBody() && request.body().length() > Normal._0) {
            builder.body(request.body());
        } else {
            builder.body(PayloadBody.empty());
        }
        return builder.build();
    }

    /**
     * Returns whether another follow-up is allowed.
     *
     * @param code      response status that produced the follow-up
     * @param followUps number of follow-ups already accepted
     * @return {@code true} when the redirect policy or authentication hard limit allows another request
     */
    private boolean followUpAllowed(final int code, final int followUps) {
        return switch (code) {
            case Http.Status.MULTIPLE_CHOICES, Http.Status.MOVED_PERMANENTLY, Http.Status.FOUND, Http.Status.SEE_OTHER, Http.Status.TEMPORARY_REDIRECT, Http.Status.PERMANENT_REDIRECT -> policy
                    .redirect(code, followUps);
            case Http.Status.UNAUTHORIZED, Http.Status.PROXY_AUTHENTICATION_REQUIRED -> followUps < Normal._20;
            default -> false;
        };
    }

    /**
     * Selects redirect method.
     *
     * @param code   redirect response status
     * @param method original request method
     * @return original method for 307, 308, GET, or HEAD; otherwise GET
     */
    private static Http.Method redirectMethod(final int code, final Http.Method method) {
        if (code == Http.Status.TEMPORARY_REDIRECT || code == Http.Status.PERMANENT_REDIRECT
                || method == Http.Method.GET || method == Http.Method.HEAD) {
            return method;
        }
        return Http.Method.GET;
    }

    /**
     * Returns whether a redirect must preserve the original request body.
     *
     * @param code   redirect response status
     * @param method original request method
     * @return {@code true} for body-capable methods redirected by status 307 or 308
     */
    private static boolean preserveBody(final int code, final Http.Method method) {
        return (code == Http.Status.TEMPORARY_REDIRECT || code == Http.Status.PERMANENT_REDIRECT)
                && method.permitsBody();
    }

    /**
     * Removes request headers that must be recalculated for redirects.
     *
     * @param headers original request headers
     * @param from    original request URL
     * @param to      resolved redirect URL
     * @return headers without body framing fields and, for cross-origin redirects, credential fields
     */
    private static Headers redirectHeaders(final Headers headers, final UnoUrl from, final UnoUrl to) {
        Headers current = headers.without(Http.Header.CONTENT_LENGTH).without(Http.Header.CONTENT_TYPE);
        if (!sameOrigin(from, to)) {
            current = current.without(Http.Header.AUTHORIZATION).without(Http.Header.PROXY_AUTHORIZATION)
                    .without(Http.Header.COOKIE);
        }
        return current;
    }

    /**
     * Returns whether two URLs share scheme, host, and port.
     *
     * @param first  first URL
     * @param second second URL
     * @return {@code true} when scheme and port match and host names are equal ignoring case
     */
    private static boolean sameOrigin(final UnoUrl first, final UnoUrl second) {
        return first.address().scheme().equals(second.address().scheme())
                && first.address().host().equalsIgnoreCase(second.address().host())
                && first.address().port() == second.address().port();
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
