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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.CodeChallenge;
import org.miaixz.bus.auth.shared.pkce.CodeVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceGenerator;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Orchestrates one Source-bound Vendor redirect authentication flow.
 * <p>
 * Platform adapters provide authorization preparation, callback correlation extraction, and verified identity
 * completion. This manager owns flow ordering and generated public security material only. Atomic persistence,
 * consumption, key derivation, and rollback are delegated to {@link RedirectState}; protocol parsing and Registry
 * routing remain outside both classes.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RedirectManager {

    /** Maximum lifetime of generated redirect correlation material. */
    private static final Duration LIFETIME = Duration.ofMinutes(10);

    /** Exact configured Source identifier owning this redirect flow. */
    private final String sourceId;
    /** Validated exact Vendor options for the configured Source. */
    private final VendorOptions<?> vendorOptions;
    /** Whether the selected OpenID Connect variant requires a nonce. */
    private final boolean nonceEnabled;
    /** Whether the selected variant and options require PKCE. */
    private final boolean pkceEnabled;
    /** Policy-bound PKCE generator, or {@code null} when PKCE is disabled. */
    private final PkceGenerator pkceGenerator;
    /** Number of random bytes used for generated state and nonce values. */
    private final int randomBytes;
    /** Source-isolated one-time correlation persistence coordinator. */
    private final RedirectState state;

    /**
     * Creates one validated Source-bound redirect flow coordinator.
     *
     * @param namespaceId   registration namespace identifier
     * @param sourceId      registration Source identifier
     * @param variant       selected Vendor variant
     * @param vendorOptions validated deployment options
     * @param services      Source-scoped runtime services
     */
    private RedirectManager(final String namespaceId, final String sourceId, final VariantManifest.Variant variant,
            final VendorOptions<?> vendorOptions, final DriverServices services) {
        this.sourceId = Assert.notBlank(sourceId, "Vendor redirect Source id must not be blank");
        final VariantManifest.Variant checkedVariant = Assert
                .notNull(variant, "Vendor redirect manifest must not be null");
        this.vendorOptions = Assert.notNull(vendorOptions, "Vendor redirect options must not be null");
        final DriverServices checkedServices = Assert
                .notNull(services, "Vendor redirect execution services must not be null");
        final var policy = checkedServices.securityBaseline().require(checkedVariant.protocol());
        this.nonceEnabled = checkedVariant.protocol() == Protocol.OIDC;
        this.pkceEnabled = checkedVariant.pkce().resolve(vendorOptions);
        this.pkceGenerator = pkceEnabled ? new PkceGenerator(policy) : null;
        this.randomBytes = (Math.max(256, policy.minimumEntropyBits()) + Byte.SIZE - 1) / Byte.SIZE;
        this.state = new RedirectState(namespaceId, sourceId, checkedServices, pkceEnabled);
    }

    /**
     * Creates Source-isolated redirect orchestration backed by external worker and cache ports.
     *
     * @param namespaceId   registration namespace identifier
     * @param sourceId      registration Source identifier
     * @param variant       exact selected platform variant
     * @param vendorOptions decoded exact deployment options
     * @param services      externally supplied protocol execution services
     * @return Source-bound redirect manager
     */
    public static RedirectManager create(
            final String namespaceId,
            final String sourceId,
            final VariantManifest.Variant variant,
            final VendorOptions<?> vendorOptions,
            final DriverServices services) {
        return new RedirectManager(namespaceId, sourceId, variant, vendorOptions, services);
    }

    /**
     * Starts a Vendor redirect interaction and publishes a redirect only after all one-time material is durable.
     *
     * @param request   Source-bound browser initiation request
     * @param operation platform authorization preparation operation
     * @param context   immutable invocation context
     * @param timeout   shared end-to-end time budget
     * @return asynchronous redirect initiation outcome
     */
    public CompletionStage<Outcome<SourceWorkflow.Stage>> initiate(
            final SourceWorkflow.Request.BrowserStart request,
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
        final Optional<String> nonce = nonceEnabled ? Optional.of(random()) : Optional.empty();
        final Optional<PkceGenerator.Pair> pair = pkceEnabled ? Optional.of(pkceGenerator.generate())
                : Optional.empty();
        final Initiation initiation = new Initiation(random(), nonce,
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
     * Completes a Vendor redirect interaction after its correlation material is atomically consumed.
     *
     * @param request   Source-bound raw browser callback
     * @param extractor platform correlation extractor
     * @param operation platform callback verification operation
     * @param context   immutable invocation context
     * @param timeout   shared end-to-end time budget
     * @return asynchronous verified Source authentication outcome
     */
    public CompletionStage<Outcome<ExternalIdentity>> complete(
            final SourceWorkflow.Request.BrowserCallback request,
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
        return state.consume(correlationValue, context, timeout).thenCompose(consumed -> switch (consumed) {
            case Outcome.Succeeded<RedirectState.Consumed> success -> finish(
                    request.callback(),
                    success.value().correlation(),
                    success.value().verifier(),
                    operation,
                    context,
                    timeout);
            case Outcome.Rejected<RedirectState.Consumed> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<RedirectState.Consumed> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Persists generated correlation and optional verifier before publishing a redirect.
     *
     * @param initiation framework-generated public security material
     * @param prepared   platform-prepared redirect
     * @param verifier   optional caller-owned PKCE verifier
     * @param context    invocation context
     * @param timeout    operation budget
     * @return redirect-stage outcome
     */
    private CompletionStage<Outcome<SourceWorkflow.Stage>> persist(
            final Initiation initiation,
            final Prepared prepared,
            final Optional<CodeVerifier> verifier,
            final Context context,
            final Timeout.Budget timeout) {
        if (prepared == null) {
            return completed(failed("Vendor authorization preparation returned invalid redirect data"));
        }
        if (!initiation.state().equals(prepared.correlationValue())) {
            return completed(rejected("Vendor authorization correlation does not equal generated state"));
        }
        if (timeout.expired()) {
            return completed(failed("Vendor redirect initiation exhausted its time budget before state storage"));
        }
        final Callback.Correlation correlation = new Callback.Correlation(sourceId, prepared.correlationValue(),
                initiation.nonce(), timeout.clock().now().plus(LIFETIME));
        return state.store(correlation, verifier, context, timeout).thenApply(stored -> switch (stored) {
            case Outcome.Succeeded<Void> ignored -> Outcome
                    .succeeded(new SourceWorkflow.Stage.Redirect(prepared.location(), correlation));
            case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Converts a consumed verifier lease and delegates final callback verification.
     *
     * @param callback    raw inbound callback
     * @param correlation consumed correlation
     * @param lease       optional caller-owned verifier lease
     * @param operation   platform completion operation
     * @param context     invocation context
     * @param timeout     operation budget
     * @return verified external identity outcome
     */
    private CompletionStage<Outcome<ExternalIdentity>> finish(
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
                    Arrays.fill(material, Symbol.C_NUL);
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
                return RedirectManager.<ExternalIdentity>failed("Vendor callback completion failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<ExternalIdentity> success -> success.value() == null
                        ? failed("Vendor callback completion returned no identity")
                        : Outcome.succeeded(success.value());
                case Outcome.Rejected<ExternalIdentity> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<ExternalIdentity> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Generates a policy-sized opaque value.
     *
     * @return cryptographically random hexadecimal value
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
     * Validates common redirect invocation collaborators.
     *
     * @param context invocation context
     * @param timeout operation budget
     */
    private static void invocation(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Vendor redirect invocation context must not be null");
        Assert.notNull(timeout, "Vendor redirect invocation budget must not be null");
    }

    /**
     * Closes an optional caller-owned verifier lease.
     *
     * @param lease optional verifier lease
     */
    private static void close(final Optional<SecretLease> lease) {
        if (lease.isPresent()) {
            lease.getOrNull().close();
        }
    }

    /**
     * Wraps an outcome in an already completed stage.
     *
     * @param <T>     outcome value type
     * @param outcome outcome to expose
     * @return completed outcome stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected redirect rejection.
     *
     * @param <T>         expected value type
     * @param description safe rejection description
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational redirect failure.
     *
     * @param <T>         expected value type
     * @param description safe failure description
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Builds one exact platform authorization redirect from generated security material.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface PrepareOperation {

        /**
         * Prepares one platform authorization redirect.
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
         * Extracts and validates the callback correlation field.
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
         * Verifies platform callback artifacts and stable identity.
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
     * @param codeChallenge optional RFC 7636 S256 challenge
     * @author Kimi Liu
     */
    public record Initiation(String state, Optional<String> nonce, Optional<CodeChallenge> codeChallenge) {

        /** Validates generated redirect authorization material. */
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
     * @param correlationValue callback value bound to generated state
     * @author Kimi Liu
     */
    public record Prepared(String location, String correlationValue) {

        /** Validates one platform-prepared authorization redirect. */
        public Prepared {
            Assert.notBlank(location, "Vendor authorization redirect location must not be blank");
            Assert.notBlank(correlationValue, "Vendor authorization correlation value must not be blank");
        }

    }

    /**
     * Carries a raw callback with consumed correlation and an optional one-time PKCE verifier.
     *
     * @param callback     raw platform callback transport
     * @param correlation  consumed callback correlation
     * @param codeVerifier optional RFC 7636 verifier
     * @author Kimi Liu
     */
    public record Completion(Callback.Inbound callback, Callback.Correlation correlation,
            Optional<CodeVerifier> codeVerifier) {

        /** Validates consumed callback security material passed to the platform adapter. */
        public Completion {
            Assert.notNull(callback, "Vendor inbound callback must not be null");
            Assert.notNull(correlation, "Vendor callback correlation must not be null");
            Assert.notNull(codeVerifier, "Vendor PKCE verifier container must not be null");
        }

    }

}
