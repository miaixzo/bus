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
package org.miaixz.bus.auth.identity;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationInitiation;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.source.SourceAuthenticationResult;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Orchestrates application-level Source authentication into a completed principal and session.
 * <p>
 * All Source execution enters through {@link Registry}. Browser and device initiation return a pending instruction;
 * direct authentication immediately continues from the Source's completed result through identity validation, account
 * linking, subject resolution, evidence evaluation, claim construction, principal construction, and session creation.
 * Every expected rejection or operational failure short-circuits the remaining chain without becoming protocol wire.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SignInService {

    /**
     * Only gateway to compiled Source capabilities.
     */
    private final Registry registry;

    /**
     * Validates completed Source results and their evidence provenance.
     */
    private final ExternalIdentityService externalIdentityService;

    /**
     * Resolves a verified external identity to an internal Subject reference.
     */
    private final AccountLinkService accountLinkService;

    /**
     * Resolves or externally creates the stable Subject.
     */
    private final SubjectService subjectService;

    /**
     * Evaluates verified authentication evidence for the resolved Subject.
     */
    private final EvidenceService evidenceService;

    /**
     * Builds provider-neutral claims from the Subject and evidence.
     */
    private final ClaimService claimService;

    /**
     * Builds the authenticated Principal from the Subject and claims.
     */
    private final IdentityService identityService;

    /**
     * Creates the final framework Session for the Principal.
     */
    private final SessionService sessionService;

    /**
     * Creates the Source sign-in orchestrator with its complete fixed identity chain.
     *
     * @param registry                only Source capability execution gateway
     * @param externalIdentityService completed-result validation port
     * @param accountLinkService      verified external-account link port
     * @param subjectService          stable Subject service
     * @param evidenceService         verified evidence service
     * @param claimService            provider-neutral claim service
     * @param identityService         authenticated Principal service
     * @param sessionService          framework Session service
     * @throws IllegalArgumentException if any dependency is {@code null}
     */
    public SignInService(final Registry registry, final ExternalIdentityService externalIdentityService,
            final AccountLinkService accountLinkService, final SubjectService subjectService,
            final EvidenceService evidenceService, final ClaimService claimService,
            final IdentityService identityService, final SessionService sessionService) {
        this.registry = Assert.notNull(registry, "Sign-in Registry must not be null");
        this.externalIdentityService = Assert
                .notNull(externalIdentityService, "External identity service must not be null");
        this.accountLinkService = Assert.notNull(accountLinkService, "Account link service must not be null");
        this.subjectService = Assert.notNull(subjectService, "Subject service must not be null");
        this.evidenceService = Assert.notNull(evidenceService, "Evidence service must not be null");
        this.claimService = Assert.notNull(claimService, "Claim service must not be null");
        this.identityService = Assert.notNull(identityService, "Identity service must not be null");
        this.sessionService = Assert.notNull(sessionService, "Session service must not be null");
    }

    /**
     * Extracts the registered Source identifier from a closed initiation request.
     *
     * @param request closed Source initiation request
     * @return registered Source identifier
     */
    private static String sourceId(final SourceAuthenticationRequest.Initiation request) {
        return switch (request) {
            case SourceAuthenticationRequest.BrowserStart value -> value.sourceId();
            case SourceAuthenticationRequest.DeviceStart value -> value.sourceId();
            case SourceAuthenticationRequest.Direct value -> value.sourceId();
            case SourceAuthenticationRequest.OneTimeCode value -> value.sourceId();
        };
    }

    /**
     * Extracts the registered Source identifier from a closed completion request.
     *
     * @param request closed Source completion request
     * @return registered Source identifier
     */
    private static String sourceId(final SourceAuthenticationRequest.Completion request) {
        return switch (request) {
            case SourceAuthenticationRequest.BrowserCallback value -> value.sourceId();
            case SourceAuthenticationRequest.DevicePoll value -> value.sourceId();
        };
    }

    /**
     * Maps a successful asynchronous outcome while preserving rejection and failure values.
     *
     * @param stage  source outcome stage
     * @param mapper success-value mapper
     * @param <A>    source success type
     * @param <B>    mapped success type
     * @return mapped outcome stage
     */
    private static <A, B> CompletionStage<Outcome<B>> map(
            final CompletionStage<Outcome<A>> stage,
            final Function<? super A, ? extends B> mapper) {
        return flatMap(stage, value -> completed(Outcome.succeeded(mapper.apply(value))));
    }

    /**
     * Composes one asynchronous Outcome step and short-circuits non-success values.
     *
     * @param stage source outcome stage
     * @param next  next asynchronous step for a success value
     * @param <A>   source success type
     * @param <B>   next success type
     * @return composed stage that never exposes dependency exceptions as normal authentication control flow
     */
    private static <A, B> CompletionStage<Outcome<B>> flatMap(
            final CompletionStage<Outcome<A>> stage,
            final Function<? super A, ? extends CompletionStage<Outcome<B>>> next) {
        if (stage == null) {
            return completed(failed("Identity dependency returned no outcome stage"));
        }
        return stage.handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return SignInService.<B>completed(failed("Identity dependency failed"));
            }
            if (outcome instanceof Outcome.Rejected<?> rejected) {
                return SignInService.<B>completed(Outcome.rejected(rejected.failure()));
            }
            if (outcome instanceof Outcome.Failed<?> failed) {
                return SignInService.<B>completed(Outcome.failed(failed.failure()));
            }
            try {
                @SuppressWarnings("unchecked")
                final A value = ((Outcome.Succeeded<A>) outcome).value();
                final CompletionStage<Outcome<B>> following = next.apply(value);
                return following == null ? SignInService.<B>completed(failed("Identity step returned no outcome stage"))
                        : following;
            } catch (RuntimeException ignored) {
                return SignInService.<B>completed(failed("Identity step failed"));
            }
        }).thenCompose(Function.identity());
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed internal outcome
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe operational failure without dependency exceptions or sensitive detail.
     *
     * @param description safe diagnostic description
     * @param <T>         expected success type
     * @return failed internal outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Starts Source authentication and either returns a pending interaction or completes direct sign-in.
     *
     * @param request browser, device, or direct Source initiation request
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a pending instruction, completed sign-in, expected rejection, or operational failure
     */
    public CompletionStage<Outcome<Initiation>> initiate(
            final SourceAuthenticationRequest.Initiation request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Source sign-in initiation request must not be null");
        Assert.notNull(context, "Source sign-in initiation context must not be null");
        Assert.notNull(timeout, "Source sign-in initiation budget must not be null");
        final CompletionStage<Outcome<SourceAuthenticationInitiation>> invocation = registry.invoke(
                Registry.Reference.source(sourceId(request)),
                SourceAuthentication.INITIATE,
                request,
                context,
                timeout);
        return flatMap(invocation, value -> {
            if (value instanceof SourceAuthenticationInitiation.Completed completed) {
                return map(completeIdentity(completed.result(), context, timeout), Completed::new);
            }
            return completed(Outcome.succeeded(new Pending(value)));
        });
    }

    /**
     * Completes browser or device Source authentication and executes the fixed identity chain.
     *
     * @param request browser callback or device polling completion request
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a completed sign-in, expected rejection, or operational failure
     */
    public CompletionStage<Outcome<SignInResult>> complete(
            final SourceAuthenticationRequest.Completion request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "Source sign-in completion request must not be null");
        Assert.notNull(context, "Source sign-in completion context must not be null");
        Assert.notNull(timeout, "Source sign-in completion budget must not be null");
        return flatMap(
                registry.invoke(
                        Registry.Reference.source(sourceId(request)),
                        SourceAuthentication.COMPLETE,
                        request,
                        context,
                        timeout),
                result -> completeIdentity(result, context, timeout));
    }

    /**
     * Executes the common identity completion chain from one verified Source result.
     *
     * @param result  completed Source authentication result
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing the final sign-in result or the first rejection or failure
     */
    private CompletionStage<Outcome<SignInResult>> completeIdentity(
            final SourceAuthenticationResult result,
            final Context context,
            final Timeout.Budget timeout) {
        return flatMap(
                externalIdentityService.validate(result, context, timeout),
                identity -> flatMap(
                        accountLinkService.resolve(identity, context, timeout),
                        reference -> flatMap(
                                subjectService.resolve(reference, context, timeout),
                                subject -> flatMap(
                                        evidenceService.evaluate(
                                                new EvidenceService.Request(identity, subject),
                                                context,
                                                timeout),
                                        evidence -> flatMap(
                                                claimService.claims(
                                                        new ClaimService.Request(subject, evidence),
                                                        context,
                                                        timeout),
                                                claims -> flatMap(
                                                        identityService.principal(
                                                                new IdentityService.Request(subject, claims),
                                                                context,
                                                                timeout),
                                                        principal -> map(
                                                                sessionService.create(principal, context, timeout),
                                                                session -> new SignInResult(principal, session,
                                                                        evidence))))))));
    }

    /**
     * Represents the result of starting a Source sign-in interaction.
     *
     * @author Kimi Liu
     */
    public sealed interface Initiation permits Pending, Completed {

    }

    /**
     * Carries a browser redirect or device instruction that requires a later completion request.
     *
     * @param value pending Source authentication instruction
     * @author Kimi Liu
     */
    public record Pending(SourceAuthenticationInitiation value) implements Initiation {

        /**
         * Creates a pending sign-in result from a non-completed Source instruction.
         *
         * @param value browser redirect or device instruction
         * @throws IllegalArgumentException if {@code value} is {@code null}
         * @throws ValidateException        if a completed Source result is incorrectly wrapped as pending
         */
        public Pending {
            Assert.notNull(value, "Pending Source authentication initiation must not be null");
            if (value instanceof SourceAuthenticationInitiation.Completed) {
                throw new ValidateException("Completed Source authentication cannot remain pending");
            }
        }

    }

    /**
     * Carries the final sign-in result produced immediately by direct Source authentication.
     *
     * @param result authenticated Principal, Session, and evidence
     * @author Kimi Liu
     */
    public record Completed(SignInResult result) implements Initiation {

        /**
         * Creates a completed direct sign-in result.
         *
         * @param result final sign-in result
         * @throws IllegalArgumentException if {@code result} is {@code null}
         */
        public Completed {
            Assert.notNull(result, "Completed sign-in result must not be null");
        }

    }

}
