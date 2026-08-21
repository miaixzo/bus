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
package org.miaixz.bus.auth.source;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.worker.SessionWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Coordinates one protocol session state transition between AuthCache and the project Session worker.
 * <p>
 * It owns key derivation, atomic cache transitions, bounded compare-and-replace retries, and project notification. It
 * does not create project users, business sessions, cookies, authorization, or persistence records.
 * </p>
 */
public final class SessionCoordinator {

    private static final int MAXIMUM_REPLACE_ATTEMPTS = 3;

    private final String sourceId;
    private final DriverServices services;

    /**
     * Creates a coordinator isolated to one compiled Source.
     */
    public SessionCoordinator(final String sourceId, final DriverServices services) {
        this.sourceId = Assert.notBlank(sourceId, "Session coordinator Source id must not be blank");
        this.services = Assert.notNull(services, "Session coordinator services must not be null");
    }

    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of()));
    }

    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    private String key(final Session.Key sessionKey) {
        return Builder.sha256Hex(sourceId + '\0' + sessionKey.value());
    }

    /**
     * Atomically establishes framework protocol state, then confirms the project integration state.
     */
    public CompletionStage<Outcome<Void>> establish(
            final Session session,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(session, "Authentication Session must not be null");
        Assert.notNull(context, "Authentication Session context must not be null");
        Assert.notNull(timeout, "Authentication Session budget must not be null");
        final Instant now = timeout.clock().now();
        if (timeout.expired() || session.state() != Session.State.ACTIVE || !session.expiresAt().isAfter(now)) {
            return completed(Outcome.failed(failure("Authentication Session is not active within the operation budget")));
        }
        final String cacheKey = key(session.key());
        final ExpiringValue<Session> value = new ExpiringValue<>(session, session.expiresAt());
        final CompletionStage<Boolean> creating;
        try {
            creating = services.sessionCache().establish(cacheKey, value);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache establishment failed")));
        }
        if (creating == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no establishment stage")));
        }
        return creating.handle((created, cause) -> cause == null ? created : null)
                .thenCompose(created -> created == null
                        ? completed(Outcome.failed(failure("Authentication Session cache establishment failed")))
                        : created ? notifyEstablished(cacheKey, session, context, timeout, true)
                                : confirmExisting(cacheKey, session, context, timeout));
    }

    private CompletionStage<Outcome<Void>> confirmExisting(
            final String cacheKey,
            final Session session,
            final Context context,
            final Timeout.Budget timeout) {
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = services.sessionCache().find(cacheKey);
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

    private CompletionStage<Outcome<Void>> notifyEstablished(
            final String cacheKey,
            final Session session,
            final Context context,
            final Timeout.Budget timeout,
            final boolean rollback) {
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = services.sessionWorker().establish(new SessionWorker.Binding(sourceId, session), context, timeout);
        } catch (RuntimeException cause) {
            return rollback(cacheKey, rollback, Outcome.failed(failure("Project Session establishment failed")));
        }
        if (stage == null) {
            return rollback(cacheKey, rollback,
                    Outcome.failed(failure("Project Session worker returned no establishment stage")));
        }
        return stage.handle((outcome, cause) -> cause == null && outcome != null ? outcome
                : Outcome.<Void>failed(failure("Project Session establishment failed")))
                .thenCompose(outcome -> outcome instanceof Outcome.Succeeded<Void> ? completed(outcome)
                        : rollback(cacheKey, rollback, outcome));
    }

    private CompletionStage<Outcome<Void>> rollback(
            final String cacheKey,
            final boolean rollback,
            final Outcome<Void> outcome) {
        if (!rollback) {
            return completed(outcome);
        }
        try {
            final CompletionStage<Boolean> deleting = services.sessionCache().invalidate(cacheKey);
            if (deleting == null) {
                return completed(Outcome.failed(failure(
                        "Project Session establishment failed and framework rollback returned no stage")));
            }
            return deleting.handle((deleted, cause) -> cause == null && Boolean.TRUE.equals(deleted) ? outcome
                    : Outcome.<Void>failed(failure(
                            "Project Session establishment failed and framework rollback was not confirmed")));
        } catch (RuntimeException ignored) {
            return completed(Outcome.failed(failure(
                    "Project Session establishment failed and framework rollback failed")));
        }
    }

    /**
     * Ends an active framework session and then propagates the transition to the project integration.
     */
    public CompletionStage<Outcome<End>> end(
            final Session.Key sessionKey,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(sessionKey, "Session key must not be null");
        Assert.notNull(context, "Session ending context must not be null");
        Assert.notNull(timeout, "Session ending budget must not be null");
        return end(sessionKey, context, timeout, 1);
    }

    private CompletionStage<Outcome<End>> end(
            final Session.Key sessionKey,
            final Context context,
            final Timeout.Budget timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("Authentication Session ending exhausted its time budget")));
        }
        final String cacheKey = key(sessionKey);
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = services.sessionCache().find(cacheKey);
        } catch (RuntimeException cause) {
            return completed(Outcome.failed(failure("Authentication Session cache lookup failed")));
        }
        if (lookup == null) {
            return completed(Outcome.failed(failure("Authentication Session cache returned no lookup stage")));
        }
        return lookup.handle((stored, cause) -> cause == null ? stored : null)
                .thenCompose(stored -> transition(cacheKey, sessionKey, stored, context, timeout, attempt));
    }

    private CompletionStage<Outcome<End>> transition(
            final String cacheKey,
            final Session.Key requestedKey,
            final ExpiringValue<Session> stored,
            final Context context,
            final Timeout.Budget timeout,
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
        final Session ending = new Session(session.key(), Session.State.ENDING, session.issuedAt(), session.expiresAt());
        final ExpiringValue<Session> endingValue = new ExpiringValue<>(ending, stored.expiresAt());
        final CompletionStage<Boolean> replacement;
        try {
            replacement = services.sessionCache().refresh(cacheKey, stored, endingValue);
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

    private CompletionStage<Outcome<End>> notifyEnded(
            final String cacheKey,
            final ExpiringValue<Session> ending,
            final Context context,
            final Timeout.Budget timeout,
            final int attempt) {
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = services.sessionWorker()
                    .end(new SessionWorker.Binding(sourceId, ending.value()), context, timeout);
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
     */
    private CompletionStage<Outcome<End>> commitEnded(
            final String cacheKey,
            final ExpiringValue<Session> ending,
            final Context context,
            final Timeout.Budget timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("Authentication Session ending exhausted its time budget")));
        }
        final Session current = ending.value();
        final Session ended = new Session(current.key(), Session.State.ENDED, current.issuedAt(), current.expiresAt());
        final CompletionStage<Boolean> replacement;
        try {
            replacement = services.sessionCache().refresh(cacheKey, ending,
                    new ExpiringValue<>(ended, ending.expiresAt()));
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
     */
    private CompletionStage<Outcome<End>> confirmFinalState(
            final String cacheKey,
            final Session.Key sessionKey,
            final Context context,
            final Timeout.Budget timeout,
            final int attempt) {
        final CompletionStage<ExpiringValue<Session>> lookup;
        try {
            lookup = services.sessionCache().find(cacheKey);
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
     */
    public enum End {
        ENDED,
        ALREADY_ENDED,
        MISSING
    }

}
