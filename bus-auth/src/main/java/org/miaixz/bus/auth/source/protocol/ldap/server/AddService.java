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
package org.miaixz.bus.auth.source.protocol.ldap.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.ldap.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Executes one complete RFC 4511 Add request through the external directory store.
 *
 * @author Kimi Liu
 */
public class AddService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String sourceId;

    /**
     * Frozen LDAP server options retained for uniform construction.
     */
    private final LdapServerOptions options;

    /**
     * External directory implementation responsible for schema and atomicity.
     */
    private final DirectoryStore store;

    /**
     * Creates an Add service for one compiled LDAP server.
     *
     * @param sourceId compiled server-role Source identifier
     * @param options  validated LDAP server options
     * @param store    externally implemented directory store
     */
    public AddService(final String sourceId, final LdapServerOptions options, final DirectoryStore store) {
        this.sourceId = Assert.notBlank(sourceId, "LDAP Add Source id must not be blank");
        this.options = Assert.notNull(options, "LDAP Add LDAP server options must not be null");
        this.store = Assert.notNull(store, "LDAP Add directory store must not be null");
    }

    /**
     * Correlates one external Add response and preserves failure classification.
     *
     * @param messageId request identifier
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return complete Add outcome
     */
    private static Outcome<LdapMessage> map(
            final int messageId,
            final Outcome<AddResponse> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Add directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<AddResponse> succeeded -> succeeded.value() == null
                    ? Outcome.failed(failure("LDAP Add directory store returned no response"))
                    : Outcome.succeeded(new LdapMessage(messageId, succeeded.value(), List.of()));
            case Outcome.Rejected<AddResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<AddResponse> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Creates one local formal Add failure response.
     *
     * @param messageId request identifier
     * @param code      standard LDAP result code
     * @return successful response outcome
     */
    private static Outcome<LdapMessage> local(final int messageId, final LdapResultCode code) {
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY),
                "The LDAP Add request was not accepted.", Optional.empty());
        return Outcome.succeeded(new LdapMessage(Math.max(0, messageId), new AddResponse(result), List.of()));
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
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one Add request and correlates the formal AddResponse.
     *
     * @param message complete Add request message
     * @param context immutable invocation context with trusted connection state
     * @param timeout shared end-to-end timeout
     * @return stage containing a correlated AddResponse or closed failure
     */
    public CompletionStage<Outcome<LdapMessage>> add(
            final LdapMessage message,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(message, "LDAP Add message must not be null");
        Assert.notNull(context, "LDAP Add context must not be null");
        Assert.notNull(timeout, "LDAP Add timeout must not be null");
        if (!(message.protocolOp() instanceof AddRequest request) || message.messageId() <= 0) {
            return completed(local(message.messageId(), LdapResultCode.PROTOCOL_ERROR));
        }
        if (message.controls().stream().anyMatch(control -> control.criticality())) {
            return completed(local(message.messageId(), LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION));
        }
        Assert.isTrue(context.network().connection().isPresent(), "LDAP Add requires a trusted connection snapshot");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Add timeout expired")));
        }
        final CompletionStage<Outcome<AddResponse>> stage;
        try {
            stage = store.add(sourceId, context.network().connection().getOrThrow().id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Add directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Add directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), outcome, thrown));
    }

}
