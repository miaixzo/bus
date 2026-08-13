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
package org.miaixz.bus.auth.protocol.ssf;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.Failed;
import org.miaixz.bus.auth.Outcome.Failure;
import org.miaixz.bus.auth.Outcome.Kind;
import org.miaixz.bus.auth.Outcome.Success;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.http.HttpValues;
import org.miaixz.bus.auth.codec.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.protocol.ssf.SSF.Event;
import org.miaixz.bus.auth.protocol.ssf.SSF.TransmitterPort;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Creates SETs and performs bounded HTTP push delivery for only network, 408, 429, and 5xx retry conditions.
 *
 * @author Kimi Liu
 */
public final class Transmitter implements TransmitterPort {

    /**
     * Immutable stream configuration.
     */
    private final StreamConfiguration configuration;

    /**
     * Trusted JWT issuance policy.
     */
    private final VerificationPolicy policy;

    /**
     * Trusted SET signer.
     */
    private final JWTSigner signer;

    /**
     * Fabric context used to create bounded HTTP clients.
     */
    private final org.miaixz.bus.fabric.Context fabric;

    /**
     * Trusted security clock.
     */
    private final Clock clock;

    /**
     * Secure JWT entropy source.
     */
    private final SecureRandom random;

    /**
     * JSON provider.
     */
    private final JsonProvider json;

    /**
     * Atomic delivery state store.
     */
    private final StateStore states;

    /**
     * Immutable protocol limits.
     */
    private final Limits limits;

    /**
     * Creates one transmitter.
     *
     * @param configuration stream configuration
     * @param policy        JWT policy
     * @param signer        signer
     * @param fabric        Fabric context used for push delivery
     * @param clock         trusted security clock
     * @param random        secure JWT entropy source
     * @param json          JSON provider
     * @param states        atomic delivery state store
     * @param limits        immutable protocol limits
     * @throws ValidateException if a dependency is null
     */
    public Transmitter(final StreamConfiguration configuration, final VerificationPolicy policy, final JWTSigner signer,
            final org.miaixz.bus.fabric.Context fabric, final Clock clock, final SecureRandom random,
            final JsonProvider json, final StateStore states, final Limits limits) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("SSF stream configuration must not be null"));
        this.policy = Assert.notNull(policy, () -> new ValidateException("SSF JWT policy must not be null"));
        this.signer = Assert.notNull(signer, () -> new ValidateException("SSF signer must not be null"));
        this.fabric = Assert.notNull(fabric, () -> new ValidateException("SSF Fabric context must not be null"));
        this.clock = Assert.notNull(clock, () -> new ValidateException("SSF clock must not be null"));
        this.random = Assert.notNull(random, () -> new ValidateException("SSF random source must not be null"));
        this.json = Assert.notNull(json, () -> new ValidateException("SSF JSON provider must not be null"));
        this.states = Assert.notNull(states, () -> new ValidateException("SSF state store must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("SSF limits must not be null"));
    }

    /**
     * Returns whether a status permits retry.
     *
     * @param status HTTP status
     * @return retry eligibility
     */
    static boolean retryable(final int status) {
        return status == Http.Status.REQUEST_TIMEOUT || status == Http.Status.TOO_MANY_REQUESTS
                || status >= 500 && status <= 599;
    }

    /**
     * Parses a delta-seconds Retry-After header, defaulting to zero.
     *
     * @param response response
     * @return non-negative seconds
     * @throws ValidateException if the header is duplicated, negative, or not delta-seconds
     */
    static long retryAfter(final HttpResponse response) {
        final String value = HttpValues.header(
                response.headers().asMap(),
                Http.Header.RETRY_AFTER,
                () -> new ValidateException("SSF Retry-After header is duplicated"));
        if (value == null) {
            return Normal._0;
        }
        try {
            final long seconds = Long.parseLong(value);
            Assert.isTrue(seconds >= Normal._0, () -> new ValidateException("SSF Retry-After is negative"));
            return seconds;
        } catch (final NumberFormatException failure) {
            throw new ValidateException("SSF Retry-After is invalid");
        }
    }

    /**
     * Maps delivery failure to a safe outcome.
     *
     * @param failure delivery failure
     * @return safe delivery outcome
     */
    static Outcome<Void> failure(final Throwable failure) {
        final Throwable cause = ExceptionKit.unwrap(failure);
        return new Failed<>(new Failure(Kind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Encodes one delivery state inside the shared authenticated state envelope.
     *
     * @param state immutable delivery state
     * @return independent authenticated envelope bytes
     */
    static byte[] encode(final DeliveryState state) {
        final DeliveryState source = Assert
                .notNull(state, () -> new ValidateException("SSF delivery state must not be null"));
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES * 2 + Long.BYTES + 1);
        payload.putInt(source.attempts()).putInt(source.maximumAttempts()).putLong(source.deadline().toEpochMilli())
                .put((byte) (source.acknowledged() ? Normal._1 : Normal._0));
        return StateEnvelopeCodec.INSTANCE.encode(payload.array());
    }

    /**
     * Pushes one event.
     *
     * @param invocation operation context
     * @param event      event
     * @return delivery outcome
     * @throws ValidateException if the stream mode, invocation, event, or issuance input is invalid
     */
    @Override
    public CompletionStage<Outcome<Void>> push(final Context invocation, final Event event) {
        final Context context = Assert
                .notNull(invocation, () -> new ValidateException("SSF invocation must not be null"));
        Assert.isTrue(
                configuration.mode() == StreamConfiguration.Mode.PUSH,
                () -> new ValidateException("SSF stream is not configured for push delivery"));
        final String token = SecurityEventToken.create(event, policy, clock, random, json, limits, signer);
        final Instant created = Assert.notNull(clock.now(), () -> new ValidateException("SSF clock returned null"));
        final DeliveryState initial = new DeliveryState(Normal._0, configuration.maximumAttempts(),
                created.plus(policy.maximumLifetime()), false);
        final byte[] encoded = encode(initial);
        final String key = ReplayKey.derive(context.tenantId(), "ssf", "delivery", token);
        return states.putIfAbsent(context, key, encoded, ttl(initial.deadline())).thenCompose(inserted -> {
            if (!Boolean.TRUE.equals(inserted)) {
                return CompletableFuture.failedFuture(new ValidateException("SSF delivery state already exists"));
            }
            return attempt(context, key, token, initial, encoded);
        }).<Outcome<Void>>thenApply(ignored -> new Success<>(null)).exceptionally(Transmitter::failure);
    }

    /**
     * Creates one token for product polling storage.
     *
     * @param event event
     * @return compact SET
     * @throws ValidateException if the stream mode, event, or issuance input is invalid
     */
    @Override
    public String poll(final Event event) {
        Assert.isTrue(
                configuration.mode() == StreamConfiguration.Mode.POLL,
                () -> new ValidateException("SSF stream is not configured for polling"));
        return SecurityEventToken.create(event, policy, clock, random, json, limits, signer);
    }

    /**
     * Executes one bounded delivery attempt.
     *
     * @param invocation operation context
     * @param key        tenant-isolated delivery-state key
     * @param token      compact SET
     * @param state      current immutable delivery state
     * @param expected   exact persisted state envelope
     * @return completion stage
     * @throws ValidateException if the state cannot begin another attempt
     */
    CompletionStage<Void> attempt(
            final Context invocation,
            final String key,
            final String token,
            final DeliveryState state,
            final byte[] expected) {
        final DeliveryState advanced = state.attempt(clock.now());
        final byte[] update = encode(advanced);
        return states.compareAndSet(invocation, key, expected, update, ttl(advanced.deadline()))
                .thenCompose(changed -> {
                    if (!Boolean.TRUE.equals(changed)) {
                        return CompletableFuture
                                .failedFuture(new ValidateException("SSF delivery state changed concurrently"));
                    }
                    return deliver(invocation, key, token, advanced, update);
                });
    }

    /**
     * Executes one Fabric HTTP delivery after the attempt state has been persisted.
     *
     * @param invocation operation context
     * @param key        tenant-isolated delivery-state key
     * @param token      compact SET
     * @param state      persisted attempt state
     * @param expected   exact persisted state envelope
     * @return completion stage
     */
    CompletionStage<Void> deliver(
            final Context invocation,
            final String key,
            final String token,
            final DeliveryState state,
            final byte[] expected) {
        return CompletableFuture
                .supplyAsync(
                        () -> HttpX.builder(fabric).post(configuration.endpoint().toASCIIString())
                                .body(token.getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_SECEVENT_JWT_TYPE)
                                .addressPolicy(configuration.policy().addressPolicy())
                                .timeout(configuration.policy().timeout()).build().execute())
                .handle((response, network) -> {
                    if (network != null) {
                        return retry(invocation, key, token, state, expected, network);
                    }
                    try (HttpResponse source = response) {
                        if (source.code() >= 200 && source.code() < 300) {
                            return acknowledge(invocation, key, state, expected);
                        }
                        if (retryable(source.code())
                                && retryAfter(source) <= configuration.maximumRetryAfter().toSeconds()) {
                            return retry(
                                    invocation,
                                    key,
                                    token,
                                    state,
                                    expected,
                                    new IllegalStateException("SSF endpoint returned a retryable status"));
                        }
                        return CompletableFuture
                                .<Void>failedFuture(new ValidateException("SSF endpoint returned a terminal status"));
                    }
                }).thenCompose(stage -> stage);
    }

    /**
     * Continues a permitted retry or terminates at the attempt ceiling.
     *
     * @param invocation operation context
     * @param key        tenant-isolated delivery-state key
     * @param token      compact SET
     * @param state      completed attempt state
     * @param expected   exact persisted state envelope
     * @param failure    previous delivery failure
     * @return next stage
     */
    CompletionStage<Void> retry(
            final Context invocation,
            final String key,
            final String token,
            final DeliveryState state,
            final byte[] expected,
            final Throwable failure) {
        return state.retry(clock.now()) ? attempt(invocation, key, token, state, expected)
                : CompletableFuture.failedFuture(failure);
    }

    /**
     * Persists terminal acknowledgment and removes the completed delivery state.
     *
     * @param invocation operation context
     * @param key        tenant-isolated delivery-state key
     * @param state      successful attempt state
     * @param expected   exact persisted state envelope
     * @return completion stage
     */
    CompletionStage<Void> acknowledge(
            final Context invocation,
            final String key,
            final DeliveryState state,
            final byte[] expected) {
        final byte[] acknowledged = encode(state.acknowledge());
        return states.compareAndSet(invocation, key, expected, acknowledged, ttl(state.deadline()))
                .thenCompose(changed -> {
                    if (!Boolean.TRUE.equals(changed)) {
                        return CompletableFuture
                                .failedFuture(new ValidateException("SSF delivery acknowledgment conflicted"));
                    }
                    return states.remove(invocation, key).thenCompose(
                            removed -> Boolean.TRUE.equals(removed) ? CompletableFuture.completedFuture(null)
                                    : CompletableFuture.failedFuture(
                                            new ValidateException("SSF acknowledged delivery state was not removed")));
                });
    }

    /**
     * Calculates the positive remaining StateStore lifetime from the trusted Fabric clock.
     *
     * @param deadline absolute delivery deadline
     * @return positive remaining lifetime
     * @throws ValidateException if the deadline has been reached
     */
    Duration ttl(final Instant deadline) {
        final Duration remaining = Duration
                .between(Assert.notNull(clock.now(), () -> new ValidateException("SSF clock returned null")), deadline);
        Assert.isTrue(
                !remaining.isNegative() && !remaining.isZero(),
                () -> new ValidateException("SSF delivery deadline has been reached"));
        return remaining;
    }

}
