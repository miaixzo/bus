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
package org.miaixz.bus.auth.worker;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.SessionCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Coordinates one protocol session state transition between AuthCache and the project Session worker.
 * <p>
 * It owns key derivation, atomic cache transitions, bounded compare-and-replace retries, and project notification. It
 * does not create project users, business sessions, cookies, authorization, or persistence records.
 * </p>
 *
 * @author Kimi Liu
 */
public class SessionCoordinator {

    /**
     * Maximum compare-and-replace attempts for one Session transition.
     */
    private static final int MAXIMUM_REPLACE_ATTEMPTS = Normal._3;

    /**
     * Exact compiled Source identifier.
     */
    private final String sourceId;
    /**
     * Framework protocol Session cache.
     */
    private final SessionCache sessionCache;
    /**
     * Project Session integration port.
     */
    private final SessionWorker sessionWorker;

    /**
     * Creates a coordinator isolated to one compiled Source and only its required session capabilities.
     *
     * @param sourceId      exact compiled Source identifier
     * @param sessionCache  framework protocol-session cache
     * @param sessionWorker project session integration port
     */
    public SessionCoordinator(final String sourceId, final SessionCache sessionCache,
            final SessionWorker sessionWorker) {
        this.sourceId = Assert.notBlank(sourceId, "Session coordinator Source id must not be blank");
        this.sessionCache = Assert.notNull(sessionCache, "Session coordinator cache must not be null");
        this.sessionWorker = Assert.notNull(sessionWorker, "Session coordinator worker must not be null");
    }

    /**
     * Creates a safe Session failure.
     *
     * @param description safe description
     * @return failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Wraps an outcome in a completed stage.
     *
     * @param <T>     value type
     * @param outcome outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Derives a Source-isolated Session key.
     *
     * @param sessionKey public Session key
     * @return digest
     */
    private String key(final Session.Key sessionKey) {
        return Builder.sha256Hex(sourceId + Symbol.C_NUL + sessionKey.value());
    }

    /**
     * Atomically establishes framework protocol state, then confirms the project integration state.
     *
     * @param session active Session to establish
     * @param context invocation context
     * @param timeout operation timeout
     * @return establishment outcome
     */
    public CompletionStage<Outcome<Void>> establish(
            final Session session,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(session, "Authentication Session must not be null");
        Assert.notNull(context, "Authentication Session context must not be null");
        Assert.notNull(timeout, "Authentication Session timeout must not be null");
        final Instant now = timeout.clock().now();
        if (timeout.expired() || session.state() != Session.State.ACTIVE || !session.expiresAt().isAfter(now)) {
            return completed(
                    Outcome.failed(failure("Authentication Session is not active within the operation timeout")));
        }
        final String cacheKey = key(session.key());
        final ExpiringValue<Session> value = new ExpiringValue<>(session, session.expiresAt());
        final CompletionStage<Boolean> creating;
        try {
            creating = sessionCache.establish(cacheKey, value);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache establishment failed")));
        }
        if (creating == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no establishment stage")));
        }
        return creating.handle((created, cause) -> cause == null ? created : null).thenCompose(
                created -> created == null
                        ? completed(Outcome.failed(failure("Authentication Session cache establishment failed")))
                        : created ? notifyEstablished(cacheKey, session, context, timeout, true)
                                : confirmExisting(cacheKey, session, context, timeout));
    }

    /**
     * Confirms idempotent establishment when the Session cache key already exists.
     *
     * @param cacheKey derived cache key
     * @param session  requested Session
     * @param context  invocation context
     * @param timeout  operation timeout
     * @return establishment outcome
     */
    private CompletionStage<Outcome<Void>> confirmExisting(
            final String cacheKey,
            final Session session,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = sessionCache.find(cacheKey);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache lookup failed")));
        }
        if (lookup == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no lookup stage")));
        }
        return lookup.handle((stored, cause) -> cause == null ? stored : null).thenCompose(stored -> {
            if (stored == null || !session.equals(stored.value()) || !session.expiresAt().equals(stored.expiresAt())) {
                return completed(Outcome.failed(failure("Authentication Session key already identifies other state")));
            }
            return notifyEstablished(cacheKey, session, context, timeout, false);
        });
    }

    /**
     * Notifies the project of established framework Session state.
     *
     * @param cacheKey derived cache key
     * @param session  established Session
     * @param context  invocation context
     * @param timeout  operation timeout
     * @param rollback whether framework state was newly created
     * @return project notification outcome
     */
    private CompletionStage<Outcome<Void>> notifyEstablished(
            final String cacheKey,
            final Session session,
            final Context context,
            final Timeout timeout,
            final boolean rollback) {
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = sessionWorker.establish(new SessionWorker.Binding(sourceId, session), context, timeout);
        } catch (RuntimeException cause) {
            return rollback(cacheKey, rollback, Outcome.failed(failure("Project Session establishment failed")));
        }
        if (stage == null) {
            return rollback(
                    cacheKey,
                    rollback,
                    Outcome.failed(failure("Project Session worker returned no establishment stage")));
        }
        return stage.handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : Outcome.<Void>failed(failure("Project Session establishment failed")))
                .thenCompose(
                        outcome -> outcome instanceof Outcome.Succeeded<Void> ? completed(outcome)
                                : rollback(cacheKey, rollback, outcome));
    }

    /**
     * Best-effort removes newly created framework state after project failure.
     *
     * @param cacheKey derived cache key
     * @param rollback whether deletion is required
     * @param outcome  project outcome to preserve when rollback succeeds
     * @return normalized outcome
     */
    private CompletionStage<Outcome<Void>> rollback(
            final String cacheKey,
            final boolean rollback,
            final Outcome<Void> outcome) {
        if (!rollback) {
            return completed(outcome);
        }
        try {
            final CompletionStage<Boolean> deleting = sessionCache.invalidate(cacheKey);
            if (deleting == null) {
                return completed(
                        Outcome.failed(
                                failure(
                                        "Project Session establishment failed and framework rollback returned no stage")));
            }
            return deleting.handle(
                    (deleted, cause) -> cause == null && Boolean.TRUE.equals(deleted) ? outcome
                            : Outcome.<Void>failed(
                                    failure(
                                            "Project Session establishment failed and framework rollback was not confirmed")));
        } catch (RuntimeException ignored) {
            return completed(
                    Outcome.failed(failure("Project Session establishment failed and framework rollback failed")));
        }
    }

    /**
     * Ends an active framework session and then propagates the transition to the project integration.
     *
     * @param sessionKey Session key to end
     * @param context    invocation context
     * @param timeout    operation timeout
     * @return ending outcome
     */
    public CompletionStage<Outcome<End>> end(
            final Session.Key sessionKey,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(sessionKey, "Session key must not be null");
        Assert.notNull(context, "Session ending context must not be null");
        Assert.notNull(timeout, "Session ending timeout must not be null");
        return end(sessionKey, context, timeout, 1);
    }

    /**
     * Performs one bounded Session-ending transition attempt.
     *
     * @param sessionKey Session key
     * @param context    invocation context
     * @param timeout    operation timeout
     * @param attempt    one-based attempt
     * @return ending outcome
     */
    private CompletionStage<Outcome<End>> end(
            final Session.Key sessionKey,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("Authentication Session ending exhausted its timeout")));
        }
        final String cacheKey = key(sessionKey);
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = sessionCache.find(cacheKey);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache lookup failed")));
        }
        if (lookup == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no lookup stage")));
        }
        return lookup.handle((stored, cause) -> cause == null ? stored : null)
                .thenCompose(stored -> transition(cacheKey, sessionKey, stored, context, timeout, attempt));
    }

    /**
     * Performs one atomic transition from active to ending Session state.
     *
     * @param cacheKey     derived cache key
     * @param requestedKey requested Session key
     * @param stored       current cached state
     * @param context      invocation context
     * @param timeout      operation timeout
     * @param attempt      one-based attempt
     * @return ending outcome
     */
    private CompletionStage<Outcome<End>> transition(
            final String cacheKey,
            final Session.Key requestedKey,
            final ExpiringValue<Session> stored,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final Instant now = timeout.clock().now();
        if (stored == null || stored.value() == null || !stored.expiresAt().isAfter(now)) {
            return completed(Outcome.succeeded(End.MISSING));
        }
        final Session session = stored.value();
        if (!requestedKey.equals(session.key())) {
            return completed(Outcome.failed(failure("Stored Authentication Session has an invalid key binding")));
        }
        if (session.state() == Session.State.ENDED || session.state() == Session.State.EXPIRED) {
            return completed(Outcome.succeeded(End.ALREADY_ENDED));
        }
        if (session.state() == Session.State.ENDING) {
            return notifyEnded(cacheKey, stored, context, timeout, attempt);
        }
        if (session.state() != Session.State.ACTIVE) {
            return completed(Outcome.failed(failure("Authentication Session has an unsupported lifecycle state")));
        }
        final Session ending = new Session(session.key(), Session.State.ENDING, session.issuedAt(),
                session.expiresAt());
        final ExpiringValue<Session> endingValue = new ExpiringValue<>(ending, stored.expiresAt());
        final CompletionStage<Boolean> replacement;
        try {
            replacement = sessionCache.refresh(cacheKey, stored, endingValue);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache replacement failed")));
        }
        if (replacement == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no replacement stage")));
        }
        return replacement.handle((replaced, cause) -> cause == null ? replaced : null).thenCompose(replaced -> {
            if (replaced == null) {
                return completed(Outcome.failed(failure("Authentication Session cache replacement failed")));
            }
            if (!replaced) {
                return attempt >= MAXIMUM_REPLACE_ATTEMPTS
                        ? completed(Outcome.failed(failure("Authentication Session changed concurrently")))
                        : end(requestedKey, context, timeout, attempt + 1);
            }
            return notifyEnded(cacheKey, endingValue, context, timeout, attempt);
        });
    }

    /**
     * Notifies the project that a Session is ending.
     *
     * @param cacheKey derived cache key
     * @param ending   current ending state
     * @param context  invocation context
     * @param timeout  operation timeout
     * @param attempt  one-based attempt
     * @return ending outcome
     */
    private CompletionStage<Outcome<End>> notifyEnded(
            final String cacheKey,
            final ExpiringValue<Session> ending,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = sessionWorker.end(new SessionWorker.Binding(sourceId, ending.value()), context, timeout);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Project Session ending failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("Project Session worker returned no ending stage")));
        }
        return stage.handle((outcome, cause) -> cause == null ? outcome : null).thenCompose(outcome -> {
            if (outcome == null || outcome instanceof Outcome.Failed<Void>) {
                return completed(Outcome.failed(failure("Project Session ending failed")));
            }
            if (outcome instanceof Outcome.Rejected<Void> rejected) {
                return completed(Outcome.rejected(rejected.failure()));
            }
            return commitEnded(cacheKey, ending, context, timeout, attempt);
        });
    }

    /**
     * Commits the framework terminal state only after the project has confirmed its idempotent ending operation.
     *
     * @param cacheKey derived Session cache key
     * @param ending   current ending state
     * @param context  invocation context
     * @param timeout  operation timeout
     * @param attempt  one-based attempt
     * @return finalization outcome
     */
    private CompletionStage<Outcome<End>> commitEnded(
            final String cacheKey,
            final ExpiringValue<Session> ending,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("Authentication Session ending exhausted its timeout")));
        }
        final Session current = ending.value();
        final Session ended = new Session(current.key(), Session.State.ENDED, current.issuedAt(), current.expiresAt());
        final CompletionStage<Boolean> replacement;
        try {
            replacement = sessionCache.refresh(cacheKey, ending, new ExpiringValue<>(ended, ending.expiresAt()));
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session finalization failed")));
        }
        if (replacement == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no finalization stage")));
        }
        return replacement.handle((replaced, cause) -> cause == null ? replaced : null).thenCompose(replaced -> {
            if (Boolean.TRUE.equals(replaced)) {
                return completed(Outcome.succeeded(End.ENDED));
            }
            if (replaced == null) {
                return completed(Outcome.failed(failure("Authentication Session finalization failed")));
            }
            return confirmFinalState(cacheKey, current.key(), context, timeout, attempt);
        });
    }

    /**
     * Re-reads a concurrently changed session and accepts only the exact terminal state.
     *
     * @param cacheKey   derived Session cache key
     * @param sessionKey public Session key
     * @param context    invocation context
     * @param timeout    operation timeout
     * @param attempt    one-based attempt
     * @return confirmed terminal outcome
     */
    private CompletionStage<Outcome<End>> confirmFinalState(
            final String cacheKey,
            final Session.Key sessionKey,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = sessionCache.find(cacheKey);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session finalization lookup failed")));
        }
        if (lookup == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no finalization lookup")));
        }
        return lookup.handle((stored, cause) -> cause == null ? stored : null).thenCompose(stored -> {
            if (stored != null && stored.value() != null && sessionKey.equals(stored.value().key())
                    && stored.value().state() == Session.State.ENDED) {
                return completed(Outcome.succeeded(End.ENDED));
            }
            if (attempt >= MAXIMUM_REPLACE_ATTEMPTS) {
                return completed(Outcome.failed(failure("Authentication Session finalization changed concurrently")));
            }
            return end(sessionKey, context, timeout, attempt + 1);
        });
    }

    /**
     * Reports the framework transition observed by a logout protocol.
     *
     * @author Kimi Liu
     */
    public enum End {
        /**
         * Session transitioned to the terminal ended state.
         */
        ENDED,
        /**
         * Session was already in a terminal state.
         */
        ALREADY_ENDED,
        /**
         * No active Session existed for the requested key.
         */
        MISSING

    }

}
