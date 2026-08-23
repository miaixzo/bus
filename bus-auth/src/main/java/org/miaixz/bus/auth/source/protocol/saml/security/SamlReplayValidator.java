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
package org.miaixz.bus.auth.source.protocol.saml.security;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.source.protocol.saml.Assertion;
import org.miaixz.bus.auth.source.protocol.saml.Conditions;
import org.miaixz.bus.auth.source.protocol.saml.Response;
import org.miaixz.bus.auth.source.protocol.saml.client.SamlClientOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Protocol;

/**
 * Atomically registers validated SAML Response and Assertion identifiers against replay.
 * <p>
 * The Source space is injected from the owning Source configuration at compilation time. Raw XML never crosses the
 * replay-cache boundary; the root guard hashes the isolated tuple before caching. Registration remains ordered so a
 * repeated Response prevents any assertion processing and the first repeated assertion terminates the operation
 * immediately.
 * </p>
 *
 * @author Kimi Liu
 */
public class SamlReplayValidator {

    /**
     * Shared digesting atomic replay guard.
     */
    private final ReplayGuard replayGuard;

    /**
     * External Source space isolating this Source's replay entries.
     */
    private final String space;

    /**
     * Creates a space-isolated SAML replay validator.
     *
     * @param replayGuard shared atomic replay guard
     * @param space       non-blank space from the owning Source configuration
     * @throws IllegalArgumentException if the guard is null or space is blank
     */
    public SamlReplayValidator(final ReplayGuard replayGuard, final String space) {
        this.replayGuard = Assert.notNull(replayGuard, "SAML ReplayGuard must not be null");
        this.space = Assert.notBlank(space, "SAML replay space must not be blank");
    }

    /**
     * Derives the Response replay expiry from the earliest assertion validity boundary.
     *
     * @param response validated response
     * @param options  Source maximum assertion age
     * @param deadline operation deadline
     * @return earliest effective expiration
     */
    private static Instant responseExpiry(
            final Response response,
            final SamlClientOptions options,
            final Instant deadline) {
        Instant result = deadline;
        for (Response.AssertionContent content : response.assertions()) {
            if (content instanceof Response.PlainAssertion plain) {
                final Instant candidate = assertionExpiry(plain.assertion(), options, deadline);
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
     * @param options   Source maximum assertion age
     * @param deadline  operation deadline ceiling
     * @return effective replay expiration
     */
    private static Instant assertionExpiry(
            final Assertion assertion,
            final SamlClientOptions options,
            final Instant deadline) {
        Instant expiration = assertion.issueInstant().plus(options.maximumAssertionAge());
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
     * @param options  trusted Source options supplying the IdP authority and maximum age
     * @param context  immutable invocation context retained for pipeline consistency
     * @param timeout  shared end-to-end timeout limiting every replay expiration
     * @return stage containing the unchanged response, replay rejection, or store failure
     */
    public CompletionStage<Outcome<Response>> validate(
            final Response response,
            final SamlClientOptions options,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(response, "SAML Response must not be null");
        Assert.notNull(options, "SAML Source options must not be null");
        Assert.notNull(context, "SAML replay context must not be null");
        Assert.notNull(timeout, "SAML replay timeout must not be null");
        final Instant responseExpiry = responseExpiry(response, options, timeout.deadline());
        CompletionStage<Outcome<Void>> stage = replayGuard.register(
                space,
                Protocol.SAML,
                options.identityProviderEntityId(),
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
                        space,
                        Protocol.SAML,
                        options.identityProviderEntityId(),
                        "assertion",
                        assertion.id(),
                        assertionExpiry(assertion, options, timeout.deadline()),
                        timeout);
                case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
            });
        }
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(response);
            case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

}
