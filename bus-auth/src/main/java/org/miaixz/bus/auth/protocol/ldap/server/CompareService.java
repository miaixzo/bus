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
package org.miaixz.bus.auth.protocol.ldap.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Executes the RFC 4511 Compare operation without converting its result codes to booleans.
 *
 * @author Kimi Liu
 */
public final class CompareService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen Provider options retained for the uniform LDAP service contract.
     */
    private final LdapServerOptions options;

    /**
     * External directory implementation.
     */
    private final DirectoryStore store;

    /**
     * Creates a Compare service for one compiled LDAP Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param options    validated LDAP Provider options
     * @param store      externally implemented directory store
     */
    public CompareService(final String providerId, final LdapServerOptions options, final DirectoryStore store) {
        this.providerId = Assert.notBlank(providerId, "LDAP Compare Provider id must not be blank");
        this.options = Assert.notNull(options, "LDAP Compare Provider options must not be null");
        this.store = Assert.notNull(store, "LDAP Compare directory store must not be null");
    }

    /**
     * Correlates one valid external Compare response and preserves closed failures.
     *
     * @param messageId request message identifier
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return complete Compare outcome
     */
    private static Outcome<LdapMessage> map(
            final int messageId,
            final Outcome<CompareResponse> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Compare directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<CompareResponse> succeeded -> succeeded.value() == null
                    ? Outcome.failed(failure("LDAP Compare directory store returned no response"))
                    : Outcome.succeeded(new LdapMessage(messageId, succeeded.value(), List.of()));
            case Outcome.Rejected<CompareResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<CompareResponse> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Creates a local formal Compare failure response.
     *
     * @param messageId request identifier
     * @param code      standard LDAP failure code
     * @return successful outcome carrying the response
     */
    private static Outcome<LdapMessage> local(final int messageId, final LdapResultCode code) {
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY),
                "The LDAP Compare request was not accepted.", org.miaixz.bus.core.lang.Optional.empty());
        return Outcome.succeeded(new LdapMessage(Math.max(0, messageId), new CompareResponse(result), List.of()));
    }

    /**
     * Creates a safe operational failure.
     *
     * @param description fixed non-sensitive description
     * @return closed failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._503, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already completed stage.
     *
     * @param outcome completed outcome
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one standard Compare request and preserves compareTrue or compareFalse in CompareResponse.
     *
     * @param message complete Compare request message
     * @param context immutable invocation context with a trusted connection snapshot
     * @param timeout shared end-to-end time budget
     * @return stage containing a correlated formal Compare response or closed failure
     */
    public CompletionStage<Outcome<LdapMessage>> compare(
            final LdapMessage message,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(message, "LDAP Compare message must not be null");
        Assert.notNull(context, "LDAP Compare context must not be null");
        Assert.notNull(timeout, "LDAP Compare time budget must not be null");
        if (!(message.protocolOp() instanceof CompareRequest request) || message.messageId() <= 0) {
            return completed(local(message.messageId(), LdapResultCode.PROTOCOL_ERROR));
        }
        if (message.controls().stream().anyMatch(control -> control.criticality())) {
            return completed(local(message.messageId(), LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION));
        }
        Assert.isTrue(
                context.network().connection().isPresent(),
                "LDAP Compare requires a trusted connection snapshot");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Compare time budget expired")));
        }
        final CompletionStage<Outcome<CompareResponse>> stage;
        try {
            stage = store
                    .compare(providerId, context.network().connection().getOrThrow().id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Compare directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Compare directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), outcome, thrown));
    }

}
