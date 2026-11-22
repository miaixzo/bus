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

import java.io.InterruptedIOException;
import java.net.URI;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.errors.recovery.RecoveryCoordinator;
import org.miaixz.bus.errors.recovery.RecoveryPlan;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuthenticator;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.chain.HttpChain;
import org.miaixz.bus.fabric.protocol.http.chain.HttpStage;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP retry and redirect stage.
 *
 * @author Kimi Liu
 */
public class HttpRetry implements HttpStage {

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
     */
    public HttpRetry(final HttpAuthenticator authenticator) {
        this(HttpRetryPolicy.defaults(), authenticator);
    }

    /**
     * Creates a retry stage.
     *
     * @param policy        retry and redirect policy
     * @param authenticator authenticator producing authorization follow-ups
     */
    public HttpRetry(final HttpRetryPolicy policy, final HttpAuthenticator authenticator) {
        this.name = "http-retry";
        this.policy = require(policy, "Retry policy");
        this.authenticator = require(authenticator, "HTTP authenticator");
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
     * Creates a fresh immutable request snapshot for every guarded network attempt. Replaying through the connect stage
     * then performs a new DNS lookup and cannot retain a previous attempt's address conclusion.
     *
     * @param request request about to enter the replayable chain
     * @return original unguarded request or a fresh guarded request snapshot
     */
    private static HttpRequest addressPolicyAttempt(final HttpRequest request) {
        final AddressPolicy addressPolicy = request.tag(AddressPolicy.class);
        return addressPolicy == null ? request : request.toBuilder().tag(AddressPolicy.class, addressPolicy).build();
    }

    /**
     * Carries an installed address policy across redirect and authentication follow-ups without storing any DNS result
     * or selected numeric address in the follow-up request.
     *
     * @param source   request whose explicit policy governs the exchange
     * @param followUp candidate follow-up, possibly {@code null}
     * @return policy-preserving follow-up or {@code null}
     */
    private static HttpRequest inheritAddressPolicy(final HttpRequest source, final HttpRequest followUp) {
        if (followUp == null) {
            return null;
        }
        final AddressPolicy addressPolicy = source.tag(AddressPolicy.class);
        return addressPolicy == null ? followUp : followUp.toBuilder().tag(AddressPolicy.class, addressPolicy).build();
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Executes the exchange, applying the request-specific retry policy when supplied.
     * <p>
     * Transport retries use the reported delivery state. Response-status retries require caller-approved replay and a
     * repeatable request body, and any retry delay must fit the configured limits.
     *
     * @param request initial request to execute and potentially replay
     * @param chain   remaining exchange chain used for first and replayed attempts
     * @return terminal response with prior follow-up responses linked using empty bodies
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        final HttpRetryPolicy requestPolicy = current.tag(HttpRetryPolicy.class);
        final HttpRetryPolicy activePolicy = requestPolicy == null ? this.policy : requestPolicy;
        final Duration budget = current.timeout().call();
        final RecoveryCoordinator recovery = activePolicy.retryStatuses().isEmpty() ? null
                : new RecoveryCoordinator(new RecoveryPlan(name, activePolicy.maxAttempts(),
                        budget.isZero() ? Duration.ofSeconds(Long.MAX_VALUE) : budget));
        final long recoveryStarted = recovery == null ? 0 : Clock.system().nanos();
        final boolean debug = DEBUG_ENABLED;
        int attempt = Normal._0;
        int staleAttempts = Normal._0;
        int followUps = Normal._0;
        HttpResponse prior = null;
        boolean first = true;
        final int replayIndex = next.index();
        while (true) {
            try {
                current = addressPolicyAttempt(current);
                final HttpChain attemptChain = first ? next : next.replayFrom(replayIndex);
                first = false;
                final HttpResponse response = attemptChain.proceed(current);
                if (recovery != null && activePolicy.retryStatuses().contains(response.code())
                        && activePolicy.replaySafe() && current.body().repeatable()
                        && attempt < activePolicy.maxAttempts()) {
                    final Duration fallback = activePolicy.baseDelay().isZero() ? Duration.ofSeconds(1)
                            : activePolicy.delay(attempt + 1);
                    final Duration delay = retryDelay(response.headers().get(Http.Header.RETRY_AFTER), fallback);
                    final Duration read = current.timeout().read();
                    if (delay.compareTo(activePolicy.maxDelay()) <= 0 && (read.isZero() || delay.compareTo(read) <= 0)
                            && fitsNanos(delay)
                            && (budget.isZero() || Duration.ofNanos(Clock.system().nanos() - recoveryStarted)
                                    .plus(delay).compareTo(budget) < 0)
                            && recovery.tryRetry(
                                    name,
                                    true,
                                    true,
                                    RecoveryPlan.Delivery.REPLAY_SAFE,
                                    next.cancellation().cancelled())) {
                        response.close();
                        awaitRetry(delay, next.cancellation());
                        if (!budget.isZero()
                                && Duration.ofNanos(Clock.system().nanos() - recoveryStarted).compareTo(budget) >= 0) {
                            throw new org.miaixz.bus.core.lang.exception.TimeoutException(
                                    "HTTP retry budget exhausted");
                        }
                        attempt++;
                        continue;
                    }
                }
                final HttpRequest followUp = inheritAddressPolicy(current, followUp(response));
                if (followUp == null) {
                    if (debug && (attempt > Normal._0 || staleAttempts > Normal._0 || followUps > Normal._0)) {
                        Logger.debug(
                                false,
                                "Fabric",
                                "HTTP retry stage completed: method={}, host={}, port={}, code={}, retries={}, "
                                        + "staleRetries={}, followUps={}",
                                current.method().value(),
                                current.url().host(),
                                current.url().port(),
                                response.code(),
                                attempt,
                                staleAttempts,
                                followUps);
                    }
                    return prior == null ? response : response.toBuilder().priorResponse(prior).build();
                }
                if (!followUpAllowed(response.code(), followUps, activePolicy)) {
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
                staleAttempts = Normal._0;
            } catch (final RuntimeException e) {
                final boolean staleRecovery = attempt < activePolicy.maxAttempts()
                        && recoverStaleConnection(current, e, staleAttempts, activePolicy);
                boolean recoverable = staleRecovery || recover(current, e, attempt, activePolicy);
                if (recoverable && recovery != null) {
                    recoverable = recovery.tryRetry(
                            name,
                            true,
                            true,
                            RecoveryPlan.Delivery.REPLAY_SAFE,
                            next.cancellation().cancelled());
                }
                if (debug) {
                    final HttpChain.ExchangeFailure failure = HttpChain.ExchangeFailure.from(e);
                    Logger.debug(
                            false,
                            "Fabric",
                            "HTTP retry decision: retry={}, staleRecovery={}, attempt={}, staleAttempt={}, "
                                    + "maxAttempts={}, maxStaleAttempts={}, method={}, host={}, port={}, delivery={}, "
                                    + "reason={}, exception={}",
                            recoverable,
                            staleRecovery,
                            attempt,
                            staleAttempts,
                            activePolicy.maxAttempts(),
                            activePolicy.maxStaleRetries(),
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
                if (staleRecovery) {
                    staleAttempts++;
                }
            }
        }
    }

    /**
     * Parses a positive retry delay from the server's response.
     *
     * @param header   value of the {@code Retry-After} header, or {@code null} when absent
     * @param fallback delay to use when the header gives no valid future retry time
     * @return parsed delay, the largest representable whole-second duration on numeric overflow, or the fallback
     */
    private static Duration retryDelay(final String header, final Duration fallback) {
        if (header != null) {
            final String value = header.trim();
            try {
                final long seconds = Long.parseLong(value);
                if (seconds > 0) {
                    return Duration.ofSeconds(seconds);
                }
            } catch (final NumberFormatException ignored) {
                if (!value.isEmpty() && value.chars().allMatch(c -> c >= '0' && c <= '9')) {
                    return Duration.ofSeconds(Long.MAX_VALUE);
                }
                try {
                    final Duration delay = Duration.between(
                            Clock.system().now(),
                            ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
                    if (!delay.isNegative() && !delay.isZero()) {
                        return delay;
                    }
                } catch (final DateTimeException invalid) {
                    // An explicitly configured backoff takes precedence over the default one-second fallback.
                }
            }
        }
        return fallback;
    }

    /**
     * Checks whether the timed wait can represent the given delay.
     *
     * @param delay proposed retry delay
     * @return {@code true} if the duration can be expressed as a {@code long} number of nanoseconds
     */
    private static boolean fitsNanos(final Duration delay) {
        try {
            delay.toNanos();
            return true;
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    /**
     * Waits after the response has been closed, waking immediately on call cancellation or interruption.
     *
     * @param delay        retry delay expressed as a duration that fits in nanoseconds
     * @param cancellation cancellation scope for the current call
     * @throws CancellationException if the call is cancelled
     * @throws SocketException       if the waiting thread is interrupted
     */
    private static void awaitRetry(final Duration delay, final Cancellation cancellation) {
        final CompletableFuture<Void> wake = new CompletableFuture<>();
        final Runnable unregister = cancellation.onCancel(() -> wake.cancel(false));
        try {
            cancellation.throwIfCancelled();
            try {
                wake.get(delay.toNanos(), TimeUnit.NANOSECONDS);
            } catch (final java.util.concurrent.TimeoutException elapsed) {
                cancellation.throwIfCancelled();
            } catch (final CancellationException cancelled) {
                cancellation.throwIfCancelled();
                throw cancelled;
            } catch (final ExecutionException failed) {
                throw new CancellationException("HTTP retry wait was cancelled");
            }
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            final InterruptedIOException failure = new InterruptedIOException("HTTP retry wait was interrupted");
            failure.initCause(interrupted);
            throw new SocketException("HTTP retry wait was interrupted", failure);
        } finally {
            unregister.run();
        }
    }

    /**
     * Creates a follow-up request for a response.
     *
     * @param response response whose status and headers determine a follow-up
     * @return redirect or authentication request, or {@code null} when no follow-up is available
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
     */
    public boolean recover(final Throwable cause, final int attempt) {
        return recover(cause, attempt, this.policy);
    }

    /**
     * Checks whether the delivery state and failure reason allow a retry under the supplied policy.
     *
     * @param cause   exchange failure to classify
     * @param attempt zero-based retry attempt
     * @param policy  effective policy for the request
     * @return {@code true} if the structured failure qualifies for another attempt
     */
    private static boolean recover(final Throwable cause, final int attempt, final HttpRetryPolicy policy) {
        final Throwable current = require(cause, "Failure cause");
        final HttpChain.ExchangeFailure failure = HttpChain.ExchangeFailure.from(current);
        if (failure == null) {
            return false;
        }
        return retryableDelivery(failure) && retryableReason(failure)
                && policy.retry(failure.cause() == null ? current : failure.cause(), attempt);
    }

    /**
     * Applies request replay and idempotency constraints in addition to the structured failure policy.
     *
     * @param request request considered for replay
     * @param cause   structured exchange failure
     * @param attempt zero-based retry attempt
     * @param policy  effective policy for the request
     * @return {@code true} when policy and request semantics permit replay
     */
    private boolean recover(
            final HttpRequest request,
            final Throwable cause,
            final int attempt,
            final HttpRetryPolicy policy) {
        return RecoveryCoordinator
                .canReplay(idempotent(request.method()), request.body().repeatable(), RecoveryPlan.Delivery.UNPROCESSED)
                && (policy == this.policy ? recover(cause, attempt) : recover(cause, attempt, policy));
    }

    /**
     * Determines whether a failed exchange on a reused HTTP/1 connection qualifies for stale-connection recovery.
     *
     * @param request request considered for replay
     * @param cause   structured exchange failure
     * @param attempt zero-based stale recovery attempt
     * @param policy  effective policy for stale-connection recovery
     * @return {@code true} when all stale-connection and request replay constraints permit recovery
     */
    private static boolean recoverStaleConnection(
            final HttpRequest request,
            final Throwable cause,
            final int attempt,
            final HttpRetryPolicy policy) {
        final HttpChain.ExchangeFailure failure = HttpChain.ExchangeFailure.from(cause);
        if (!idempotent(request.method()) || !request.body().repeatable() || failure == null) {
            return false;
        }
        final Throwable underlying = failure.cause() == null ? cause : failure.cause();
        return failure.deliveryState() == HttpChain.DeliveryState.MAYBE_PROCESSED
                && failure.scope() == HttpChain.FailureScope.CONNECTION
                && failure.reason() == HttpChain.FailureReason.IO && failure.reusedConnection()
                && failure.beforeResponse() && policy.retryStaleConnection(underlying, attempt);
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
     * @param policy    effective policy for redirect follow-ups
     * @return {@code true} when the redirect policy or authentication hard limit allows another request
     */
    private static boolean followUpAllowed(final int code, final int followUps, final HttpRetryPolicy policy) {
        return switch (code) {
            case Http.Status.MULTIPLE_CHOICES, Http.Status.MOVED_PERMANENTLY, Http.Status.FOUND, Http.Status.SEE_OTHER, Http.Status.TEMPORARY_REDIRECT, Http.Status.PERMANENT_REDIRECT -> policy
                    .redirect(code, followUps);
            case Http.Status.UNAUTHORIZED, Http.Status.PROXY_AUTHENTICATION_REQUIRED -> followUps < Normal._20;
            default -> false;
        };
    }

}
