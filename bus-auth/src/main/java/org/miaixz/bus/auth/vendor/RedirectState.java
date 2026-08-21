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
package org.miaixz.bus.auth.vendor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.CodeVerifier;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.CredentialStore;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Persists and atomically consumes one-time Vendor redirect correlation material.
 * <p>
 * This class owns cache and credential-store coordination, key derivation, validation, and rollback only. It does not
 * generate redirects, invoke platform operations, parse callbacks, construct identities, or decide authentication
 * outcomes beyond the integrity of stored correlation material.
 * </p>
 *
 * @author Kimi Liu
 */
final class RedirectState {

    private static final String STATE_PURPOSE = "vendor-state";
    private static final String PKCE_PURPOSE = "vendor-pkce";

    private final String namespaceId;
    private final String sourceId;
    private final DriverServices services;
    private final boolean pkceEnabled;

    RedirectState(final String namespaceId, final String sourceId, final DriverServices services,
            final boolean pkceEnabled) {
        this.namespaceId = Assert.notBlank(namespaceId, "Vendor redirect namespace id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "Vendor redirect Source id must not be blank");
        this.services = Assert.notNull(services, "Vendor redirect execution services must not be null");
        this.pkceEnabled = pkceEnabled;
    }

    /**
     * Persists the optional secret verifier before publishing the single authoritative callback correlation.
     * <p>
     * The correlation already carries its OIDC nonce, so a second NonceCache copy would create a redundant
     * cross-cache transaction without adding an integrity boundary.
     * </p>
     */
    CompletionStage<Outcome<Void>> store(
            final Callback.Correlation correlation,
            final Optional<CodeVerifier> verifier,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(correlation, "Vendor callback correlation must not be null");
        Assert.notNull(verifier, "Vendor PKCE verifier container must not be null");
        Assert.notNull(context, "Vendor redirect context must not be null");
        Assert.notNull(timeout, "Vendor redirect budget must not be null");
        Assert.equals(sourceId, correlation.sourceId(), "Vendor callback correlation Source must match");
        return storeVerifier(correlation.state(), verifier, correlation.expiresAt(), context, timeout)
                .thenCompose(stored -> switch (stored) {
                    case Outcome.Succeeded<Void> ignored -> createState(correlation).thenCompose(state -> switch (state) {
                        case Outcome.Succeeded<Void> storedState -> completed(Outcome.succeeded(null));
                        case Outcome.Rejected<Void> rejected -> rollback(
                                correlation.state(), verifier.isPresent(), context, timeout)
                                .thenApply(ignoredRollback -> Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<Void> failed -> rollback(
                                correlation.state(), verifier.isPresent(), context, timeout)
                                .thenApply(ignoredRollback -> Outcome.failed(failed.failure()));
                    });
                    case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Atomically consumes the authoritative state and then takes its optional secret PKCE verifier.
     * <p>
     * State is deliberately consumed first so a callback can never be replayed after a credential-store fault. A
     * verifier failure therefore fails closed and triggers best-effort verifier deletion; it is not represented as a
     * retryable distributed transaction across two independently owned stores.
     * </p>
     */
    CompletionStage<Outcome<Consumed>> consume(
            final String correlationValue,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notBlank(correlationValue, "Vendor callback correlation value must not be blank");
        Assert.notNull(context, "Vendor redirect context must not be null");
        Assert.notNull(timeout, "Vendor redirect budget must not be null");
        return takeCorrelation(correlationValue, timeout).thenCompose(correlation -> switch (correlation) {
            case Outcome.Succeeded<Callback.Correlation> success -> consumeVerifier(correlationValue, context, timeout)
                    .thenCompose(verifier -> switch (verifier) {
                        case Outcome.Succeeded<Optional<SecretLease>> lease -> completed(
                                Outcome.succeeded(new Consumed(success.value(), lease.value())));
                        case Outcome.Rejected<Optional<SecretLease>> rejected -> deleteVerifier(
                                correlationValue, context, timeout)
                                .thenApply(ignored -> Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<Optional<SecretLease>> failed -> deleteVerifier(
                                correlationValue, context, timeout)
                                .thenApply(ignored -> Outcome.failed(failed.failure()));
                    });
            case Outcome.Rejected<Callback.Correlation> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Callback.Correlation> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    private CompletionStage<Outcome<Void>> storeVerifier(
            final String correlationValue,
            final Optional<CodeVerifier> verifier,
            final Instant expiresAt,
            final Context context,
            final Timeout.Budget timeout) {
        if (verifier.isEmpty()) {
            return completed(Outcome.succeeded(null));
        }
        final SecretLease lease = new SecretLease(verifier.getOrNull().value().toCharArray());
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = services.credentialStore()
                    .store(credentialKey(correlationValue), lease, Optional.of(expiresAt), context, timeout);
        } catch (RuntimeException cause) {
            lease.close();
            return completed(failed("Vendor PKCE verifier storage failed"));
        }
        if (stage == null) {
            lease.close();
            return completed(failed("Vendor PKCE verifier storage returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            lease.close();
            return cause == null && outcome != null ? outcome : failed("Vendor PKCE verifier storage failed");
        });
    }

    private CompletionStage<Outcome<Void>> createState(final Callback.Correlation correlation) {
        final CompletionStage<Boolean> stage;
        try {
            stage = services.stateCache().issue(
                    digest(STATE_PURPOSE, correlation.state()),
                    new ExpiringValue<>(correlation, correlation.expiresAt()));
        } catch (RuntimeException cause) {
            return completed(failed("Vendor callback state storage failed"));
        }
        return booleanCreation(stage, "Vendor callback state storage failed", "Vendor callback state collided");
    }

    private CompletionStage<Outcome<Callback.Correlation>> takeCorrelation(
            final String correlationValue,
            final Timeout.Budget timeout) {
        final CompletionStage<ExpiringValue<Callback.Correlation>> stage;
        try {
            stage = services.stateCache().consume(digest(STATE_PURPOSE, correlationValue));
        } catch (RuntimeException cause) {
            return completed(failed("Vendor callback state cache failed"));
        }
        if (stage == null) {
            return completed(failed("Vendor callback state cache returned no stage"));
        }
        return stage.handle((stored, cause) -> {
            if (cause != null) {
                return failed("Vendor callback state cache failed");
            }
            if (stored == null || !sourceId.equals(stored.value().sourceId())
                    || !correlationValue.equals(stored.value().state())
                    || !stored.expiresAt().equals(stored.value().expiresAt())
                    || !timeout.clock().now().isBefore(stored.expiresAt())) {
                return rejected("Vendor callback state is missing, consumed, invalid, or expired");
            }
            return Outcome.succeeded(stored.value());
        });
    }

    private CompletionStage<Outcome<Optional<SecretLease>>> consumeVerifier(
            final String correlationValue,
            final Context context,
            final Timeout.Budget timeout) {
        if (!pkceEnabled) {
            return completed(Outcome.succeeded(Optional.empty()));
        }
        final CompletionStage<Outcome<SecretLease>> stage;
        try {
            stage = services.credentialStore().take(credentialKey(correlationValue), context, timeout);
        } catch (RuntimeException cause) {
            return completed(failed("Vendor PKCE verifier store failed"));
        }
        if (stage == null) {
            return completed(failed("Vendor PKCE verifier store returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return RedirectState.<Optional<SecretLease>>failed("Vendor PKCE verifier store failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<SecretLease> success -> success.value() == null
                        ? rejected("Vendor PKCE verifier is missing or consumed")
                        : Outcome.succeeded(Optional.of(success.value()));
                case Outcome.Rejected<SecretLease> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<SecretLease> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    private CompletionStage<Void> rollback(
            final String correlationValue,
            final boolean verifierStored,
            final Context context,
            final Timeout.Budget timeout) {
        CompletionStage<Void> result = CompletableFuture.completedFuture(null);
        if (verifierStored) {
            result = result.thenCompose(ignored -> deleteVerifier(correlationValue, context, timeout));
        }
        return result;
    }

    private CompletionStage<Void> deleteVerifier(
            final String correlationValue,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            final CompletionStage<Outcome<Void>> stage = services.credentialStore()
                    .delete(credentialKey(correlationValue), context, timeout);
            return stage == null ? CompletableFuture.completedFuture(null) : stage.handle((ignored, cause) -> null);
        } catch (RuntimeException cause) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private String digest(final String purpose, final String binding) {
        return Builder.sha256Hex(namespaceId + '\0' + sourceId + '\0' + purpose + '\0' + binding);
    }

    private CredentialStore.Key credentialKey(final String correlationValue) {
        return new CredentialStore.Key(namespaceId, sourceId, PKCE_PURPOSE, digest(PKCE_PURPOSE, correlationValue),
                Credential.Type.SHARED_SECRET);
    }

    private static CompletionStage<Outcome<Void>> booleanCreation(
            final CompletionStage<Boolean> stage,
            final String failureDescription,
            final String collisionDescription) {
        if (stage == null) {
            return completed(failed(failureDescription));
        }
        return stage.handle((created, cause) -> {
            if (cause != null || created == null) {
                return failed(failureDescription);
            }
            return created ? Outcome.succeeded(null) : failed(collisionDescription);
        });
    }

    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Transfers consumed correlation and the caller-owned optional verifier lease to redirect orchestration.
     */
    record Consumed(Callback.Correlation correlation, Optional<SecretLease> verifier) {

        Consumed {
            Assert.notNull(correlation, "Consumed Vendor callback correlation must not be null");
            Assert.notNull(verifier, "Consumed Vendor verifier container must not be null");
        }
    }

}
