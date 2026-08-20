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
 * Executes one atomic RFC 4511 Modify DN operation without changing its rename or move fields.
 *
 * @author Kimi Liu
 */
public final class ModifyDNService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen Provider settings retained for uniform construction.
     */
    private final LdapProviderSettings settings;

    /**
     * External directory implementation responsible for atomic rename and move semantics.
     */
    private final DirectoryStore store;

    /**
     * Creates a Modify DN service for one compiled LDAP Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param settings   validated LDAP Provider settings
     * @param store      externally implemented directory store
     */
    public ModifyDNService(final String providerId, final LdapProviderSettings settings, final DirectoryStore store) {
        this.providerId = Assert.notBlank(providerId, "LDAP Modify DN Provider id must not be blank");
        this.settings = Assert.notNull(settings, "LDAP Modify DN Provider settings must not be null");
        this.store = Assert.notNull(store, "LDAP Modify DN directory store must not be null");
    }

    /**
     * Correlates one external Modify DN response and preserves failures.
     *
     * @param messageId request identifier
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return complete Modify DN outcome
     */
    private static Outcome<LdapMessage> map(
            final int messageId,
            final Outcome<ModifyDNResponse> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Modify DN directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<ModifyDNResponse> succeeded -> succeeded.value() == null
                    ? Outcome.failed(failure("LDAP Modify DN directory store returned no response"))
                    : Outcome.succeeded(new LdapMessage(messageId, succeeded.value(), List.of()));
            case Outcome.Rejected<ModifyDNResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<ModifyDNResponse> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Creates one local formal Modify DN failure response.
     *
     * @param messageId request identifier
     * @param code      standard LDAP result code
     * @return successful response outcome
     */
    private static Outcome<LdapMessage> local(final int messageId, final LdapResultCode code) {
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY),
                "The LDAP Modify DN request was not accepted.", org.miaixz.bus.core.lang.Optional.empty());
        return Outcome.succeeded(new LdapMessage(Math.max(0, messageId), new ModifyDNResponse(result), List.of()));
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
     * Delegates the complete request and correlates its formal ModifyDNResponse.
     *
     * @param message complete Modify DN request message
     * @param context immutable invocation context with trusted connection state
     * @param timeout shared end-to-end time budget
     * @return stage containing a correlated ModifyDNResponse or closed failure
     */
    public CompletionStage<Outcome<LdapMessage>> modifyDN(
            final LdapMessage message,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(message, "LDAP Modify DN message must not be null");
        Assert.notNull(context, "LDAP Modify DN context must not be null");
        Assert.notNull(timeout, "LDAP Modify DN time budget must not be null");
        if (!(message.protocolOp() instanceof ModifyDNRequest request) || message.messageId() <= 0) {
            return completed(local(message.messageId(), LdapResultCode.PROTOCOL_ERROR));
        }
        if (message.controls().stream().anyMatch(control -> control.criticality())) {
            return completed(local(message.messageId(), LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION));
        }
        Assert.isTrue(
                context.network().connection().isPresent(),
                "LDAP Modify DN requires a trusted connection snapshot");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Modify DN time budget expired")));
        }
        final CompletionStage<Outcome<ModifyDNResponse>> stage;
        try {
            stage = store
                    .modifyDN(providerId, context.network().connection().getOrThrow().id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Modify DN directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Modify DN directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), outcome, thrown));
    }

}
