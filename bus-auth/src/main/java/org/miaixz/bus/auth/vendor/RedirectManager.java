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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.NonceCache;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.CodeChallenge;
import org.miaixz.bus.auth.shared.pkce.CodeVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceGenerator;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthenticationInitiation;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.source.SourceAuthenticationResult;
import org.miaixz.bus.auth.worker.CredentialStore;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Manages the single state, nonce, PKCE, and callback lifecycle used by Vendor redirect authentication adapters.
 * <p>
 * Platform adapters supply only their exact authorization preparation, callback correlation extraction, and verified
 * identity operation. This class does not parse protocol fields, call Registry, or create platform-neutral token or
 * user-profile models.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RedirectManager {

    /**
     * Fixed lifetime of a Vendor redirect correlation.
     */
    private static final Duration LIFETIME = Duration.ofMinutes(10);

    /**
     * State-cache isolation purpose.
     */
    private static final String STATE_PURPOSE = "vendor-state";

    /**
     * Nonce-cache isolation purpose.
     */
    private static final String NONCE_PURPOSE = "vendor-nonce";

    /**
     * Dynamic credential-store isolation purpose.
     */
    private static final String PKCE_PURPOSE = "vendor-pkce";

    /**
     * Registered namespace isolation identifier.
     */
    private final String namespaceId;

    /**
     * Registered Source identifier.
     */
    private final String sourceId;

    /**
     * Exact selected platform variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Exact decoded platform Source options.
     */
    private final VendorOptions<?> vendorOptions;

    /**
     * Externally owned stores, clock, and security baseline.
     */
    private final ExecutionServices runtimeComponents;

    /**
     * Whether this variant generates and consumes an OpenID Connect nonce.
     */
    private final boolean nonceEnabled;

    /**
     * Whether this configured variant generates and consumes an S256 PKCE pair.
     */
    private final boolean pkceEnabled;

    /**
     * PKCE generator bound to the actual protocol security policy.
     */
    private final PkceGenerator pkceGenerator;

    /**
     * Number of secure random bytes used for state and nonce.
     */
    private final int randomBytes;

    /**
     * Creates a redirect manager for one compiled platform Source.
     *
     * @param namespaceId       exact namespace identifier from the registration
     * @param sourceId          exact Source identifier from the registration
     * @param variant           selected platform variant manifest
     * @param vendorOptions     decoded exact platform options
     * @param runtimeComponents complete external runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     */
    private RedirectManager(final String namespaceId, final String sourceId, final VariantManifest.Variant variant,
            final VendorOptions<?> vendorOptions, final ExecutionServices runtimeComponents) {
        this.namespaceId = Assert.notBlank(namespaceId, "Vendor redirect namespace id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "Vendor redirect Source id must not be blank");
        this.variant = Assert.notNull(variant, "Vendor redirect manifest must not be null");
        this.vendorOptions = Assert.notNull(vendorOptions, "Vendor redirect options must not be null");
        this.runtimeComponents = Assert
                .notNull(runtimeComponents, "Vendor redirect execution services must not be null");
        final var policy = runtimeComponents.securityBaseline().require(variant.protocol());
        this.nonceEnabled = variant.protocol() == Protocol.OIDC;
        this.pkceEnabled = vendorOptions.pkce();
        this.pkceGenerator = pkceEnabled ? new PkceGenerator(policy) : null;
        this.randomBytes = (Math.max(256, policy.minimumEntropyBits()) + Byte.SIZE - 1) / Byte.SIZE;
    }

    /**
     * Creates a Source-isolated redirect manager backed by the configured runtime stores and security policy.
     *
     * @param namespaceId       registration namespace identifier
     * @param sourceId          registration Source identifier
     * @param variant           selected platform variant manifest
     * @param vendorOptions     decoded immutable platform options
     * @param runtimeComponents complete externally supplied runtime dependencies
     * @return framework-owned redirect manager
     */
    public static RedirectManager create(
            final String namespaceId,
            final String sourceId,
            final VariantManifest.Variant variant,
            final VendorOptions<?> vendorOptions,
            final ExecutionServices runtimeComponents) {
        return new RedirectManager(namespaceId, sourceId, variant, vendorOptions, runtimeComponents);
    }

    /**
     * Converts an atomic create stage into a closed framework outcome.
     *
     * @param stage                atomic cache creation stage
     * @param failureDescription   safe operational failure description
     * @param collisionDescription safe collision description
     * @return normalized creation outcome
     */
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

    /**
     * Validates common invocation dependencies without replacing the caller's budget.
     *
     * @param context immutable invocation context
     * @param timeout shared time budget
     */
    private static void invocation(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Vendor redirect invocation context must not be null");
        Assert.notNull(timeout, "Vendor redirect invocation budget must not be null");
    }

    /**
     * Closes an optional sensitive lease.
     *
     * @param lease optional lease owned by this manager
     */
    private static void close(final Optional<SecretLease> lease) {
        if (lease.isPresent()) {
            lease.getOrNull().close();
        }
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected redirect-lifecycle rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational redirect-lifecycle failure.
     *
     * @param description non-sensitive failure description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Starts a Vendor redirect interaction and persists all one-time material before returning a redirect.
     *
     * @param request   Source-bound browser initiation request
     * @param operation platform authorization preparation operation
     * @param context   immutable invocation context
     * @param timeout   shared end-to-end time budget
     * @return redirect initiation only after nonce, PKCE, and state storage succeeds
     */
    public CompletionStage<Outcome<SourceAuthenticationInitiation>> initiate(
            final SourceAuthenticationRequest.BrowserStart request,
            final PrepareOperation operation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Vendor redirect start request must not be null");
        Assert.notNull(operation, "Vendor redirect prepare operation must not be null");
        invocation(context, timeout);
        if (!sourceId.equals(request.sourceId()) || vendorOptions.redirectUri().isEmpty()
                || !vendorOptions.redirectUri().getOrNull().equals(request.callbackTarget().redirectUri())) {
            return completed(rejected("Vendor redirect callback target does not match the registered Source"));
        }
        if (timeout.expired()) {
            return completed(failed("Vendor redirect initiation has no remaining time budget"));
        }
        final String state = random();
        final Optional<String> nonce = nonceEnabled ? Optional.of(random()) : Optional.empty();
        final Optional<PkceGenerator.Pair> pair = pkceEnabled ? Optional.of(pkceGenerator.generate())
                : Optional.empty();
        final Initiation initiation = new Initiation(state, nonce,
                pair.isPresent() ? Optional.of(pair.getOrNull().challenge()) : Optional.empty());
        final CompletionStage<Outcome<Prepared>> prepared;
        try {
            prepared = operation.prepare(initiation, context, timeout);
        } catch (RuntimeException cause) {
            return completed(failed("Vendor authorization preparation failed"));
        }
        if (prepared == null) {
            return completed(failed("Vendor authorization preparation returned no stage"));
        }
        return prepared
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : RedirectManager.<Prepared>failed("Vendor authorization preparation failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Prepared> success -> persist(
                            initiation,
                            success.value(),
                            pair.isPresent() ? Optional.of(pair.getOrNull().verifier()) : Optional.empty(),
                            context,
                            timeout);
                    case Outcome.Rejected<Prepared> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Prepared> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Completes a Vendor redirect interaction after atomically consuming its state, nonce, and PKCE verifier.
     *
     * @param request   Source-bound raw browser callback
     * @param extractor platform callback correlation extractor
     * @param operation platform callback and identity verification operation
     * @param context   immutable invocation context
     * @param timeout   shared end-to-end time budget
     * @return verified Source authentication result
     */
    public CompletionStage<Outcome<SourceAuthenticationResult>> complete(
            final SourceAuthenticationRequest.BrowserCallback request,
            final CorrelationExtractor extractor,
            final CompleteOperation operation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Vendor redirect callback request must not be null");
        Assert.notNull(extractor, "Vendor callback correlation extractor must not be null");
        Assert.notNull(operation, "Vendor callback completion operation must not be null");
        invocation(context, timeout);
        if (!sourceId.equals(request.sourceId()) || !sourceId.equals(request.callback().sourceId())
                || timeout.expired()) {
            return completed(rejected("Vendor redirect callback does not match an active Source interaction"));
        }
        final String correlationValue;
        try {
            correlationValue = Assert.notBlank(
                    extractor.extract(request.callback()),
                    "Vendor callback correlation value must not be blank");
        } catch (RuntimeException cause) {
            return completed(rejected("Vendor redirect callback correlation is invalid"));
        }
        return takeCorrelation(correlationValue, timeout).thenCompose(correlation -> switch (correlation) {
            case Outcome.Succeeded<Callback.Correlation> success -> consumeNonce(
                    correlationValue,
                    success.value(),
                    timeout).thenCompose(nonce -> switch (nonce) {
                        case Outcome.Succeeded<Void> ignored -> consumeVerifier(correlationValue, context, timeout)
                                .thenCompose(verifier -> switch (verifier) {
                                    case Outcome.Succeeded<Optional<SecretLease>> lease -> finish(
                                            request.callback(),
                                            success.value(),
                                            lease.value(),
                                            operation,
                                            context,
                                            timeout);
                                    case Outcome.Rejected<Optional<SecretLease>> rejected -> completed(
                                            Outcome.rejected(rejected.failure()));
                                    case Outcome.Failed<Optional<SecretLease>> failed -> completed(
                                            Outcome.failed(failed.failure()));
                                });
                        case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                    });
            case Outcome.Rejected<Callback.Correlation> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Callback.Correlation> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Persists nonce, PKCE verifier, and state in dependency order with reverse-order rollback.
     *
     * @param initiation generated public authorization material
     * @param prepared   platform-generated redirect and correlation binding
     * @param verifier   optional sensitive PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared time budget
     * @return redirect outcome after durable state creation
     */
    private CompletionStage<Outcome<SourceAuthenticationInitiation>> persist(
            final Initiation initiation,
            final Prepared prepared,
            final Optional<CodeVerifier> verifier,
            final Context context,
            final Timeout.Budget timeout) {
        if (prepared == null || prepared.location() == null || prepared.location().isBlank()
                || prepared.correlationValue() == null || prepared.correlationValue().isBlank()) {
            return completed(failed("Vendor authorization preparation returned invalid redirect data"));
        }
        if (!initiation.state().equals(prepared.correlationValue())) {
            return completed(rejected("Vendor authorization correlation does not equal generated state"));
        }
        final Instant expiresAt = timeout.clock().now().plus(LIFETIME);
        final Callback.Correlation correlation = new Callback.Correlation(sourceId, prepared.correlationValue(),
                initiation.nonce(), expiresAt);
        return createNonce(prepared.correlationValue(), initiation.nonce(), expiresAt)
                .thenCompose(nonce -> switch (nonce) {
                    case Outcome.Succeeded<Void> ignored -> storeVerifier(
                            prepared.correlationValue(),
                            verifier,
                            expiresAt,
                            context,
                            timeout).thenCompose(stored -> switch (stored) {
                                case Outcome.Succeeded<Void> storedVerifier -> createState(correlation)
                                        .thenCompose(state -> switch (state) {
                                            case Outcome.Succeeded<Void> storedState -> completed(
                                                    Outcome.succeeded(
                                                            new SourceAuthenticationInitiation.Redirect(
                                                                    prepared.location(), correlation)));
                                            case Outcome.Rejected<Void> rejected -> rollback(
                                                    prepared.correlationValue(),
                                                    initiation.nonce().isPresent(),
                                                    verifier.isPresent(),
                                                    context,
                                                    timeout).thenApply(
                                                            ignoredRollback -> Outcome.rejected(rejected.failure()));
                                            case Outcome.Failed<Void> failed -> rollback(
                                                    prepared.correlationValue(),
                                                    initiation.nonce().isPresent(),
                                                    verifier.isPresent(),
                                                    context,
                                                    timeout).thenApply(
                                                            ignoredRollback -> Outcome.failed(failed.failure()));
                                        });
                                case Outcome.Rejected<Void> rejected -> rollback(
                                        prepared.correlationValue(),
                                        initiation.nonce().isPresent(),
                                        false,
                                        context,
                                        timeout).thenApply(ignoredRollback -> Outcome.rejected(rejected.failure()));
                                case Outcome.Failed<Void> failed -> rollback(
                                        prepared.correlationValue(),
                                        initiation.nonce().isPresent(),
                                        false,
                                        context,
                                        timeout).thenApply(ignoredRollback -> Outcome.failed(failed.failure()));
                            });
                    case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Atomically creates the optional nonce binding.
     *
     * @param correlationValue platform callback correlation value
     * @param nonce            optional generated nonce
     * @param expiresAt        absolute interaction expiration
     * @return creation outcome
     */
    private CompletionStage<Outcome<Void>> createNonce(
            final String correlationValue,
            final Optional<String> nonce,
            final Instant expiresAt) {
        if (nonce.isEmpty()) {
            return completed(Outcome.succeeded(null));
        }
        final CompletionStage<Boolean> stage;
        try {
            stage = runtimeComponents.nonceCache().create(
                    digest(NONCE_PURPOSE, correlationValue),
                    new ExpiringValue<>(new NonceCache.Nonce(sourceId, nonce.getOrNull()), expiresAt),
                    LIFETIME.toMillis());
        } catch (RuntimeException cause) {
            return completed(failed("Vendor nonce storage failed"));
        }
        return booleanCreation(stage, "Vendor nonce storage failed", "Vendor nonce identifier collided");
    }

    /**
     * Securely stores the optional PKCE verifier.
     *
     * @param correlationValue platform callback correlation value
     * @param verifier         optional generated verifier
     * @param expiresAt        absolute interaction expiration
     * @param context          immutable invocation context
     * @param timeout          shared time budget
     * @return storage outcome
     */
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
            stage = runtimeComponents.credentialStore()
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

    /**
     * Atomically creates the callback correlation after all secret material is durable.
     *
     * @param correlation complete callback correlation
     * @return creation outcome
     */
    private CompletionStage<Outcome<Void>> createState(final Callback.Correlation correlation) {
        final CompletionStage<Boolean> stage;
        try {
            stage = runtimeComponents.stateCache().create(
                    digest(STATE_PURPOSE, correlation.state()),
                    new ExpiringValue<>(correlation, correlation.expiresAt()),
                    LIFETIME.toMillis());
        } catch (RuntimeException cause) {
            return completed(failed("Vendor callback state storage failed"));
        }
        return booleanCreation(stage, "Vendor callback state storage failed", "Vendor callback state collided");
    }

    /**
     * Atomically consumes and validates one callback correlation.
     *
     * @param correlationValue extracted platform correlation value
     * @param timeout          shared time budget and clock
     * @return validated correlation outcome
     */
    private CompletionStage<Outcome<Callback.Correlation>> takeCorrelation(
            final String correlationValue,
            final Timeout.Budget timeout) {
        final CompletionStage<ExpiringValue<Callback.Correlation>> stage;
        try {
            stage = runtimeComponents.stateCache().take(digest(STATE_PURPOSE, correlationValue));
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

    /**
     * Atomically consumes and validates the nonce bound to a correlation.
     *
     * @param correlationValue platform callback correlation value
     * @param correlation      validated callback correlation
     * @param timeout          shared clock and budget
     * @return nonce-consumption outcome
     */
    private CompletionStage<Outcome<Void>> consumeNonce(
            final String correlationValue,
            final Callback.Correlation correlation,
            final Timeout.Budget timeout) {
        if (correlation.nonce().isEmpty()) {
            return completed(Outcome.succeeded(null));
        }
        final CompletionStage<ExpiringValue<NonceCache.Nonce>> stage;
        try {
            stage = runtimeComponents.nonceCache().take(digest(NONCE_PURPOSE, correlationValue));
        } catch (RuntimeException cause) {
            return completed(failed("Vendor nonce cache failed"));
        }
        if (stage == null) {
            return completed(failed("Vendor nonce cache returned no stage"));
        }
        return stage.handle((stored, cause) -> {
            if (cause != null) {
                return failed("Vendor nonce cache failed");
            }
            if (stored == null || !sourceId.equals(stored.value().sourceId())
                    || !correlation.nonce().getOrNull().equals(stored.value().nonce())
                    || !timeout.clock().now().isBefore(stored.expiresAt())) {
                return rejected("Vendor nonce is missing, consumed, invalid, or expired");
            }
            return Outcome.succeeded(null);
        });
    }

    /**
     * Atomically consumes the optional sensitive PKCE verifier lease.
     *
     * @param correlationValue platform callback correlation value
     * @param context          immutable invocation context
     * @param timeout          shared time budget
     * @return optional caller-owned lease outcome
     */
    private CompletionStage<Outcome<Optional<SecretLease>>> consumeVerifier(
            final String correlationValue,
            final Context context,
            final Timeout.Budget timeout) {
        if (!pkceEnabled) {
            return completed(Outcome.succeeded(Optional.empty()));
        }
        final CompletionStage<Outcome<SecretLease>> stage;
        try {
            stage = runtimeComponents.credentialStore().take(credentialKey(correlationValue), context, timeout);
        } catch (RuntimeException cause) {
            return completed(failed("Vendor PKCE verifier store failed"));
        }
        if (stage == null) {
            return completed(failed("Vendor PKCE verifier store returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return RedirectManager.<Optional<SecretLease>>failed("Vendor PKCE verifier store failed");
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

    /**
     * Executes platform completion while keeping an optional verifier lease alive for exactly that stage.
     *
     * @param callback    raw callback transport
     * @param correlation validated one-time correlation
     * @param lease       optional owned verifier lease
     * @param operation   platform callback completion operation
     * @param context     immutable invocation context
     * @param timeout     shared time budget
     * @return verified Source authentication outcome
     */
    private CompletionStage<Outcome<SourceAuthenticationResult>> finish(
            final Callback.Inbound callback,
            final Callback.Correlation correlation,
            final Optional<SecretLease> lease,
            final CompleteOperation operation,
            final Context context,
            final Timeout.Budget timeout) {
        final Optional<CodeVerifier> verifier;
        try {
            if (lease.isPresent()) {
                final char[] material = lease.getOrNull().material();
                try {
                    verifier = Optional.of(new CodeVerifier(new String(material)));
                } finally {
                    Arrays.fill(material, '\0');
                }
            } else {
                verifier = Optional.empty();
            }
        } catch (RuntimeException cause) {
            close(lease);
            return completed(failed("Vendor PKCE verifier is invalid"));
        }
        final CompletionStage<Outcome<ExternalIdentity>> stage;
        try {
            stage = operation.complete(new Completion(callback, correlation, verifier), context, timeout);
        } catch (RuntimeException cause) {
            close(lease);
            return completed(failed("Vendor callback completion failed"));
        }
        if (stage == null) {
            close(lease);
            return completed(failed("Vendor callback completion returned no stage"));
        }
        return stage.handle((outcome, cause) -> {
            close(lease);
            if (cause != null || outcome == null) {
                return RedirectManager.<SourceAuthenticationResult>failed("Vendor callback completion failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<ExternalIdentity> success -> success.value() == null
                        ? failed("Vendor callback completion returned no identity")
                        : Outcome.succeeded(new SourceAuthenticationResult(success.value()));
                case Outcome.Rejected<ExternalIdentity> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<ExternalIdentity> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Deletes previously created values in reverse dependency order after an initiation failure.
     *
     * @param correlationValue platform callback correlation value
     * @param nonceCached      whether a nonce may have been created
     * @param verifierStored   whether a verifier may have been stored
     * @param context          immutable invocation context
     * @param timeout          shared time budget
     * @return stage completed after best-effort rollback attempts
     */
    private CompletionStage<Void> rollback(
            final String correlationValue,
            final boolean nonceCached,
            final boolean verifierStored,
            final Context context,
            final Timeout.Budget timeout) {
        CompletionStage<Void> result = CompletableFuture.completedFuture(null);
        if (verifierStored) {
            result = result.thenCompose(ignored -> deleteVerifier(correlationValue, context, timeout));
        }
        if (nonceCached) {
            result = result.thenCompose(ignored -> deleteNonce(correlationValue));
        }
        return result;
    }

    /**
     * Deletes one dynamic verifier during best-effort rollback.
     *
     * @param correlationValue platform callback correlation value
     * @param context          immutable invocation context
     * @param timeout          shared time budget
     * @return always normally completed rollback stage
     */
    private CompletionStage<Void> deleteVerifier(
            final String correlationValue,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            final CompletionStage<Outcome<Void>> stage = runtimeComponents.credentialStore()
                    .delete(credentialKey(correlationValue), context, timeout);
            return stage == null ? CompletableFuture.completedFuture(null) : stage.handle((ignored, cause) -> null);
        } catch (RuntimeException cause) {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Deletes one nonce during best-effort rollback.
     *
     * @param correlationValue platform callback correlation value
     * @return always normally completed rollback stage
     */
    private CompletionStage<Void> deleteNonce(final String correlationValue) {
        try {
            final CompletionStage<Boolean> stage = runtimeComponents.nonceCache()
                    .delete(digest(NONCE_PURPOSE, correlationValue));
            return stage == null ? CompletableFuture.completedFuture(null) : stage.handle((ignored, cause) -> null);
        } catch (RuntimeException cause) {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Creates a cryptographically strong lowercase hexadecimal state or nonce value.
     *
     * @return random state or nonce with two hexadecimal characters per entropy byte
     */
    private String random() {
        final byte[] random = RandomKit.randomBytes(randomBytes, RandomKit.getSecureRandom());
        try {
            return Convert.toHex(random);
        } finally {
            Arrays.fill(random, (byte) 0);
        }
    }

    /**
     * Creates one irreversible namespace- and Source-isolated cache key.
     *
     * @param purpose stable cache purpose
     * @param binding opaque protocol binding
     * @return lowercase SHA-256 hexadecimal key
     */
    private String digest(final String purpose, final String binding) {
        return Builder.sha256Hex(namespaceId + '\0' + sourceId + '\0' + purpose + '\0' + binding);
    }

    /**
     * Creates the isolated one-time dynamic credential key for a PKCE verifier.
     *
     * @param correlationValue platform callback correlation value
     * @return external credential-store key
     */
    private CredentialStore.Key credentialKey(final String correlationValue) {
        return new CredentialStore.Key(namespaceId, sourceId, PKCE_PURPOSE, digest(PKCE_PURPOSE, correlationValue),
                Credential.Type.SHARED_SECRET);
    }

    /**
     * Builds one exact platform authorization redirect from generated security material.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface PrepareOperation {

        /**
         * Prepares one platform authorization request.
         *
         * @param initiation generated state, nonce, and PKCE challenge
         * @param context    immutable invocation context
         * @param timeout    shared time budget
         * @return platform redirect and exact callback correlation binding
         */
        CompletionStage<Outcome<Prepared>> prepare(Initiation initiation, Context context, Timeout.Budget timeout);

    }

    /**
     * Extracts the single platform-defined correlation value from a raw callback.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface CorrelationExtractor {

        /**
         * Extracts and validates the platform correlation field cardinality and syntax.
         *
         * @param callback raw inbound callback
         * @return exact opaque correlation value
         */
        String extract(Callback.Inbound callback);

    }

    /**
     * Completes one platform callback and returns only a fully verified external identity.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface CompleteOperation {

        /**
         * Verifies platform callback artifacts, tokens, signatures, and stable identity.
         *
         * @param completion consumed callback security material
         * @param context    immutable invocation context
         * @param timeout    shared time budget
         * @return verified external identity outcome
         */
        CompletionStage<Outcome<ExternalIdentity>> complete(
                Completion completion,
                Context context,
                Timeout.Budget timeout);

    }

    /**
     * Carries generated public redirect authorization material.
     *
     * @param state         generated opaque state
     * @param nonce         optional generated OpenID Connect nonce
     * @param codeChallenge optional generated RFC 7636 S256 challenge
     * @author Kimi Liu
     */
    public record Initiation(String state, Optional<String> nonce, Optional<CodeChallenge> codeChallenge) {

        /**
         * Validates generated authorization material.
         */
        public Initiation {
            Assert.notBlank(state, "Vendor redirect state must not be blank");
            Assert.notNull(nonce, "Vendor redirect nonce container must not be null");
            Assert.notNull(codeChallenge, "Vendor redirect code challenge container must not be null");
        }

    }

    /**
     * Carries a platform-prepared redirect and its callback correlation binding.
     *
     * @param location         platform authorization redirect location
     * @param correlationValue callback value used to locate one-time state
     * @author Kimi Liu
     */
    public record Prepared(String location, String correlationValue) {

        /**
         * Validates platform-prepared redirect data.
         */
        public Prepared {
            Assert.notBlank(location, "Vendor authorization redirect location must not be blank");
            Assert.notBlank(correlationValue, "Vendor authorization correlation value must not be blank");
        }

    }

    /**
     * Carries a raw callback with its consumed correlation and optional one-time PKCE verifier.
     *
     * @param callback     raw platform callback transport
     * @param correlation  consumed callback correlation
     * @param codeVerifier optional RFC 7636 verifier
     * @author Kimi Liu
     */
    public record Completion(Callback.Inbound callback, Callback.Correlation correlation,
            Optional<CodeVerifier> codeVerifier) {

        /**
         * Validates callback completion material.
         */
        public Completion {
            Assert.notNull(callback, "Vendor inbound callback must not be null");
            Assert.notNull(correlation, "Vendor callback correlation must not be null");
            Assert.notNull(codeVerifier, "Vendor PKCE verifier container must not be null");
        }

    }

}
