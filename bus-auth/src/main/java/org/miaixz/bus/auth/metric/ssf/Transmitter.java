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
package org.miaixz.bus.auth.metric.ssf;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.SSF.Event;
import org.miaixz.bus.auth.metric.SSF.TransmitterPort;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Creates SETs and performs bounded HTTP push delivery for only network, 408, 429, and 5xx retry conditions.
 */
public final class Transmitter implements TransmitterPort {

    /**
     * Stream configuration.
     */
    private final StreamConfiguration configuration;

    /**
     * Trusted JWT policy.
     */
    private final VerificationPolicy policy;

    /**
     * Trusted signer.
     */
    private final JWTSigner signer;

    /**
     * Runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one transmitter.
     *
     * @param configuration stream configuration
     * @param policy        JWT policy
     * @param signer        signer
     * @param runtime       runtime
     */
    public Transmitter(final StreamConfiguration configuration, final VerificationPolicy policy, final JWTSigner signer,
            final Runtime runtime) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("SSF stream configuration must not be null"));
        this.policy = Assert.notNull(policy, () -> new ValidateException("SSF JWT policy must not be null"));
        this.signer = Assert.notNull(signer, () -> new ValidateException("SSF signer must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("SSF runtime must not be null"));
    }

    /**
     * Returns whether a status permits retry.
     *
     * @param status HTTP status
     * @return retry eligibility
     */
    static boolean retryable(final int status) {
        return status == Http.Status.REQUEST_TIMEOUT || status == Http.Status.TOO_MANY_REQUESTS || status >= 500;
    }

    /**
     * Parses a delta-seconds Retry-After header, defaulting to zero.
     *
     * @param response response
     * @return non-negative seconds
     */
    static long retryAfter(final Response response) {
        final List<String> values = response.headers().get(Http.Header.RETRY_AFTER.toLowerCase(java.util.Locale.ROOT));
        if (values == null) {
            return Normal._0;
        }
        Assert.isTrue(values.size() == Normal._1, () -> new ValidateException("SSF Retry-After header is duplicated"));
        try {
            final long seconds = Long.parseLong(values.getFirst());
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
        final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        return new Failed<>(new Failure(FailureKind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Pushes one event.
     *
     * @param invocation operation context
     * @param event      event
     * @return delivery outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> push(final Invocation invocation, final Event event) {
        Assert.isTrue(
                configuration.mode() == StreamConfiguration.Mode.PUSH,
                () -> new ValidateException("SSF stream is not configured for push delivery"));
        final String token = SecurityEventToken.create(event, policy, runtime, signer);
        return attempt(invocation, token, Normal._1).<Outcome<Void>>thenApply(ignored -> new Success<>(null))
                .exceptionally(Transmitter::failure);
    }

    /**
     * Creates one token for product polling storage.
     *
     * @param event event
     * @return compact SET
     */
    @Override
    public String poll(final Event event) {
        Assert.isTrue(
                configuration.mode() == StreamConfiguration.Mode.POLL,
                () -> new ValidateException("SSF stream is not configured for polling"));
        return SecurityEventToken.create(event, policy, runtime, signer);
    }

    /**
     * Executes one bounded delivery attempt.
     *
     * @param invocation context
     * @param token      compact SET
     * @param attempt    one-based attempt
     * @return completion stage
     */
    CompletionStage<Void> attempt(final Invocation invocation, final String token, final int attempt) {
        final Request request = new Request(Http.Method.POST, configuration.endpoint(),
                Map.of(Http.Header.CONTENT_TYPE, List.of(MediaType.APPLICATION_SECEVENT_JWT)), Map.of(), "",
                token.getBytes(StandardCharsets.UTF_8));
        return runtime.transports().protocol().exchange(invocation, request, configuration.policy())
                .handle((response, network) -> {
                    if (network != null) {
                        return retry(invocation, token, attempt, network);
                    }
                    if (response.status() >= 200 && response.status() < 300) {
                        return CompletableFuture.<Void>completedFuture(null);
                    }
                    if (retryable(response.status())
                            && retryAfter(response) <= configuration.maximumRetryAfter().toSeconds()) {
                        return retry(
                                invocation,
                                token,
                                attempt,
                                new IllegalStateException("SSF endpoint returned a retryable status"));
                    }
                    return CompletableFuture
                            .<Void>failedFuture(new ValidateException("SSF endpoint returned a terminal status"));
                }).thenCompose(stage -> stage);
    }

    /**
     * Continues a permitted retry or terminates at the attempt ceiling.
     *
     * @param invocation context
     * @param token      token
     * @param attempt    completed attempt
     * @param failure    previous failure
     * @return next stage
     */
    CompletionStage<Void> retry(
            final Invocation invocation,
            final String token,
            final int attempt,
            final Throwable failure) {
        return attempt < configuration.maximumAttempts() ? attempt(invocation, token, attempt + Normal._1)
                : CompletableFuture.failedFuture(failure);
    }

}
