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
package org.miaixz.bus.auth.guard;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.ReplayCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Atomically registers irreversible authentication artifact digests for replay detection.
 * <p>
 * Each digest input is isolated by namespace, industry protocol, registered authority, and artifact purpose. Length
 * prefixes make the input tuple unambiguous. Only the SHA-256 digest and a non-sensitive purpose label cross the cache
 * boundary; raw nonce, assertion, token, code, and authenticator material never does.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ReplayGuard {

    /**
     * Atomic replay cache supplied by the runtime assembly.
     */
    private final ReplayCache cache;

    /**
     * Creates a replay guard backed by the required atomic cache wrapper.
     *
     * @param cache atomic replay cache
     * @throws IllegalArgumentException if {@code cache} is {@code null}
     */
    public ReplayGuard(final ReplayCache cache) {
        this.cache = Assert.notNull(cache, "Replay cache must not be null");
    }

    /**
     * Encodes a tuple as concatenated UTF-16 lengths and values without delimiter ambiguity.
     *
     * @param values tuple values
     * @return unambiguous digest input
     */
    private static String tuple(final String... values) {
        final StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(value.length()).append(Symbol.C_COLON).append(value);
        }
        return builder.toString();
    }

    /**
     * Calculates a positive backend TTL while saturating durations beyond the millisecond range.
     *
     * @param now       shared-clock instant used for the registration decision
     * @param expiresAt effective expiration instant
     * @return positive backend TTL in milliseconds
     */
    private static long ttlMillis(final Instant now, final Instant expiresAt) {
        try {
            return Math.max(1L, Duration.between(now, expiresAt).toMillis());
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed internal outcome
     * @param <T>     outcome success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates safe internal failure detail without exposing replay material.
     *
     * @param error       shared Bus error definition
     * @param description non-sensitive diagnostic description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Atomically registers one authentication artifact until its effective expiration.
     *
     * @param namespace external registration namespace
     * @param protocol  industry protocol that owns the artifact
     * @param authority stable Provider or Source authority identifier
     * @param purpose   non-sensitive artifact purpose such as JWT jti or SAML assertion identifier
     * @param artifact  raw artifact lexical value used only as digest input
     * @param expiresAt absolute artifact expiration
     * @param timeout   existing end-to-end operation budget
     * @return stage containing success, replay rejection, or operational cache failure
     * @throws IllegalArgumentException if any argument is {@code null} or a required string is blank
     */
    public CompletionStage<Outcome<Void>> register(
            final String namespace,
            final Protocol protocol,
            final String authority,
            final String purpose,
            final String artifact,
            final Instant expiresAt,
            final Timeout.Budget timeout) {
        Assert.notBlank(namespace, "Replay namespace must not be blank");
        Assert.notNull(protocol, "Replay protocol must not be null");
        Assert.notBlank(authority, "Replay authority must not be blank");
        Assert.notBlank(purpose, "Replay purpose must not be blank");
        Assert.notBlank(artifact, "Replay artifact must not be blank");
        Assert.notNull(expiresAt, "Replay artifact expiration must not be null");
        Assert.notNull(timeout, "Replay operation budget must not be null");

        final Instant now = timeout.clock().now();
        final Instant effectiveExpiry = expiresAt.isBefore(timeout.deadline()) ? expiresAt : timeout.deadline();
        if (!effectiveExpiry.isAfter(now)) {
            return completed(Outcome.failed(failure(ErrorCode._408, "Replay registration has no remaining lifetime")));
        }
        final String key = Builder.sha256(tuple(namespace, protocol.name(), authority, purpose, artifact));
        final ExpiringValue<String> value = new ExpiringValue<>(protocol.name() + Symbol.C_COLON + purpose,
                effectiveExpiry);
        final long ttlMillis = ttlMillis(now, effectiveExpiry);
        try {
            final CompletionStage<Boolean> creation = cache.mark(key, value);
            if (creation == null) {
                return completed(Outcome.failed(failure(ErrorCode._500, "Replay cache returned no creation stage")));
            }
            return creation.handle((created, cause) -> {
                if (cause != null || created == null) {
                    return Outcome.<Void>failed(failure(ErrorCode._500, "Replay cache creation failed"));
                }
                if (!created) {
                    return Outcome.<Void>rejected(failure(ErrorCode._409, "Authentication artifact was already used"));
                }
                return Outcome.<Void>succeeded(null);
            });
        } catch (RuntimeException ignored) {
            return completed(Outcome.failed(failure(ErrorCode._500, "Replay cache creation failed")));
        }
    }

}
