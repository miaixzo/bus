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
package org.miaixz.bus.auth.worker.identity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.resolver.ClaimParser;
import org.miaixz.bus.auth.resolver.SubjectParser;
import org.miaixz.bus.auth.shared.claim.ClaimSet;
import org.miaixz.bus.auth.worker.loader.ClaimLoader;
import org.miaixz.bus.auth.worker.loader.IdentityLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Completes one verified Source authentication as a protocol-neutral framework identity.
 * <p>
 * This worker is the concrete framework consumer of {@link IdentityLoader} and {@link ClaimLoader}. It owns only
 * validation, ordered composition, timeout enforcement, result construction, and typed failure propagation. The
 * external project remains responsible for both loader implementations and for every business-session action after this
 * worker succeeds.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthenticationWorker {

    /**
     * Project-supplied external-account and Subject loading port.
     */
    private final IdentityLoader identityLoader;

    /**
     * Project-supplied claim lookup and disclosure port.
     */
    private final ClaimLoader claimLoader;

    /**
     * Pure parser for project-loaded Subject records.
     */
    private final SubjectParser subjectParser;

    /**
     * Pure parser for project-loaded claim records.
     */
    private final ClaimParser claimParser;

    /**
     * Framework-owned external identity structural verifier.
     */
    private final ExternalIdentityVerifier identityVerifier;

    /**
     * Creates one identity-completion worker with its two project extension ports.
     *
     * @param identityLoader project external-account and Subject loader
     * @param claimLoader    project claim loader
     * @throws IllegalArgumentException if either loader is {@code null}
     */
    public AuthenticationWorker(final IdentityLoader identityLoader, final ClaimLoader claimLoader) {
        this.identityLoader = Assert.notNull(identityLoader, "Identity loader must not be null");
        this.claimLoader = Assert.notNull(claimLoader, "Claim loader must not be null");
        this.subjectParser = new SubjectParser();
        this.claimParser = new ClaimParser();
        this.identityVerifier = new ExternalIdentityVerifier();
    }

    /**
     * Invokes one project port without allowing null stages or dependency exceptions to escape as normal flow.
     *
     * @param <T>      successful project-port value type
     * @param supplier deferred project-port invocation
     * @param timeout  shared end-to-end operation timeout
     * @param failure  safe description used for dependency failures
     * @return normalized asynchronous outcome
     */
    private static <T> CompletionStage<Outcome<T>> invoke(
            final StageSupplier<T> supplier,
            final Timeout timeout,
            final String failure) {
        if (timeout.expired()) {
            return completed(expired());
        }
        try {
            final CompletionStage<Outcome<T>> stage = supplier.get();
            if (stage == null) {
                return completed(failed(failure));
            }
            return stage.handle((outcome, cause) -> cause == null && outcome != null ? outcome : failed(failure));
        } catch (RuntimeException cause) {
            return completed(failed(failure));
        }
    }

    /**
     * Composes a successful value while preserving rejection and operational failure outcomes.
     *
     * @param <A>   current successful value type
     * @param <B>   following successful value type
     * @param stage current asynchronous outcome
     * @param next  function invoked only for a successful current value
     * @return stage preserving rejection or failure and containing the following successful value
     */
    private static <A, B> CompletionStage<Outcome<B>> flatMap(
            final CompletionStage<Outcome<A>> stage,
            final Function<? super A, ? extends CompletionStage<Outcome<B>>> next) {
        return stage.thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<A> success -> {
                try {
                    final CompletionStage<Outcome<B>> following = next.apply(success.value());
                    yield following == null ? completed(failed("Authentication worker step returned no stage"))
                            : following;
                } catch (RuntimeException cause) {
                    yield completed(failed("Authentication worker step failed"));
                }
            }
            case Outcome.Rejected<A> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<A> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Creates one already completed outcome stage.
     *
     * @param <T>     successful outcome value type
     * @param outcome outcome to expose through a completed stage
     * @return already completed asynchronous outcome
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one safe operation-timeout failure.
     *
     * @param <T> expected operation value type
     * @return typed timeout failure outcome
     */
    private static <T> Outcome<T> expired() {
        return Outcome.failed(
                new Outcome.Failure(ErrorCode._408, "Authentication worker timeout has expired",
                        new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates one safe dependency or framework failure.
     *
     * @param <T>         expected operation value type
     * @param description safe failure description
     * @return typed operational failure outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Completes one Source result through identity loading, claim loading, parsing, and immutable result construction.
     *
     * @param sourceId selected Source identifier
     * @param identity completed verified external identity
     * @param context  immutable non-secret invocation context
     * @param timeout  shared end-to-end operation timeout
     * @return stage containing the completed authentication result, expected rejection, or operational failure
     */
    public CompletionStage<Outcome<AuthenticationResult>> complete(
            final String sourceId,
            final Identity identity,
            final Context context,
            final Timeout timeout) {
        Assert.notBlank(sourceId, "Authentication worker Source id must not be blank");
        Assert.notNull(identity, "Authentication worker external identity must not be null");
        Assert.notNull(context, "Authentication worker context must not be null");
        Assert.notNull(timeout, "Authentication worker timeout must not be null");
        if (timeout.expired()) {
            return completed(expired());
        }

        final Identity verified;
        try {
            verified = identityVerifier.verify(sourceId, identity);
        } catch (RuntimeException cause) {
            return completed(failed("Completed Source identity is invalid"));
        }

        return flatMap(
                invoke(() -> identityLoader.load(verified, context, timeout), timeout, "Identity loading failed"),
                record -> {
                    final Subject subject = subjectParser.parse(record);
                    return flatMap(
                            invoke(
                                    () -> claimLoader
                                            .load(new ClaimLoader.Request(subject, verified), context, timeout),
                                    timeout,
                                    "Claim loading failed"),
                            claims -> completeResult(subject, verified, claimParser.parse(claims), timeout));
                });
    }

    /**
     * Completes framework-owned authentication result construction.
     *
     * @param subject  parsed stable subject
     * @param identity verified external identity
     * @param claims   parsed disclosed claim set
     * @param timeout  shared end-to-end operation timeout
     * @return stage containing the completed authentication result or a safe failure
     */
    private CompletionStage<Outcome<AuthenticationResult>> completeResult(
            final Subject subject,
            final Identity identity,
            final ClaimSet claims,
            final Timeout timeout) {
        if (timeout.expired()) {
            return completed(expired());
        }
        try {
            final Subject checked = Assert.notNull(subject, "Identity parser returned no Subject");
            final ClaimSet verified = Assert.notNull(claims, "Claim parser returned no ClaimSet");
            return completed(Outcome.succeeded(new AuthenticationResult(checked, identity, verified)));
        } catch (RuntimeException cause) {
            return completed(failed("Authentication result construction failed"));
        }
    }

    /**
     * Supplies one asynchronous project-port invocation.
     *
     * @param <T> successful project-port value type
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface StageSupplier<T> {

        /**
         * Starts the deferred project-port invocation.
         *
         * @return asynchronous project-port outcome
         */
        CompletionStage<Outcome<T>> get();

    }

}
