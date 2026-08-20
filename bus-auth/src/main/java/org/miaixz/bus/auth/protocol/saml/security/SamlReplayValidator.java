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
package org.miaixz.bus.auth.protocol.saml.security;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.protocol.saml.Assertion;
import org.miaixz.bus.auth.protocol.saml.Conditions;
import org.miaixz.bus.auth.protocol.saml.Response;
import org.miaixz.bus.auth.protocol.saml.client.SamlSourceSettings;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Protocol;

/**
 * Atomically registers validated SAML Response and Assertion identifiers against replay.
 * <p>
 * The registration namespace is injected from the owning Source registration at compilation time. Raw XML never crosses
 * the replay-store boundary; the root guard hashes the isolated tuple before storage. Registration remains ordered so a
 * repeated Response prevents any assertion processing and the first repeated assertion terminates the operation
 * immediately.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SamlReplayValidator {

    /**
     * Shared digesting atomic replay guard.
     */
    private final ReplayGuard replayGuard;

    /**
     * External registration namespace isolating this Source's replay entries.
     */
    private final String namespace;

    /**
     * Creates a namespace-isolated SAML replay validator.
     *
     * @param replayGuard shared atomic replay guard
     * @param namespace   non-blank namespace from the owning Source registration
     * @throws IllegalArgumentException if the guard is null or namespace is blank
     */
    public SamlReplayValidator(final ReplayGuard replayGuard, final String namespace) {
        this.replayGuard = Assert.notNull(replayGuard, "SAML ReplayGuard must not be null");
        this.namespace = Assert.notBlank(namespace, "SAML replay namespace must not be blank");
    }

    /**
     * Derives the Response replay expiry from the earliest assertion validity boundary.
     *
     * @param response validated response
     * @param settings Source maximum assertion age
     * @param deadline operation deadline
     * @return earliest effective expiration
     */
    private static Instant responseExpiry(
            final Response response,
            final SamlSourceSettings settings,
            final Instant deadline) {
        Instant result = deadline;
        for (Response.AssertionContent content : response.assertions()) {
            if (content instanceof Response.PlainAssertion plain) {
                final Instant candidate = assertionExpiry(plain.assertion(), settings, deadline);
                if (candidate.isBefore(result))
                    result = candidate;
            }
        }
        return result;
    }

    /**
     * Derives one Assertion replay expiry from Conditions or the configured maximum assertion age.
     *
     * @param assertion validated assertion
     * @param settings  Source maximum assertion age
     * @param deadline  operation deadline ceiling
     * @return effective replay expiration
     */
    private static Instant assertionExpiry(
            final Assertion assertion,
            final SamlSourceSettings settings,
            final Instant deadline) {
        Instant expiration = assertion.issueInstant().plus(settings.maximumAssertionAge());
        if (assertion.conditions().isPresent()) {
            final Conditions conditions = assertion.conditions().getOrNull();
            if (conditions.notOnOrAfter().isPresent() && conditions.notOnOrAfter().getOrNull().isBefore(expiration)) {
                expiration = conditions.notOnOrAfter().getOrNull();
            }
        }
        return expiration.isBefore(deadline) ? expiration : deadline;
    }

    /**
     * Creates a type-inferred completed stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Registers the Response ID followed by every plaintext Assertion ID.
     *
     * @param response fully validated plaintext SAML Response
     * @param settings trusted Source settings supplying the IdP authority and maximum age
     * @param context  immutable invocation context retained for pipeline consistency
     * @param timeout  shared end-to-end budget limiting every replay expiration
     * @return stage containing the unchanged response, replay rejection, or store failure
     */
    public CompletionStage<Outcome<Response>> validate(
            final Response response,
            final SamlSourceSettings settings,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(response, "SAML Response must not be null");
        Assert.notNull(settings, "SAML Source settings must not be null");
        Assert.notNull(context, "SAML replay context must not be null");
        Assert.notNull(timeout, "SAML replay budget must not be null");
        final Instant responseExpiry = responseExpiry(response, settings, timeout.deadline());
        CompletionStage<Outcome<Void>> stage = replayGuard.register(
                namespace,
                Protocol.SAML,
                settings.identityProviderEntityId(),
                "response",
                response.id(),
                responseExpiry,
                timeout);
        for (Response.AssertionContent content : response.assertions()) {
            if (!(content instanceof Response.PlainAssertion plain)) {
                throw new IllegalArgumentException("SAML replay validation requires plaintext assertions");
            }
            final Assertion assertion = plain.assertion();
            stage = stage.thenCompose(outcome -> switch (outcome) {
                case Outcome.Succeeded<Void> ignored -> replayGuard.register(
                        namespace,
                        Protocol.SAML,
                        settings.identityProviderEntityId(),
                        "assertion",
                        assertion.id(),
                        assertionExpiry(assertion, settings, timeout.deadline()),
                        timeout);
                case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
            });
        }
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(response);
            case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
        });
    }

}
