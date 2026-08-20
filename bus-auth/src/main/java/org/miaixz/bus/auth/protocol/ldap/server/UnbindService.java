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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.LdapMessage;
import org.miaixz.bus.auth.protocol.ldap.UnbindRequest;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Executes the response-free RFC 4511 Unbind operation for one compiled server-role Source connection.
 *
 * @author Kimi Liu
 */
public final class UnbindService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen Provider options retained for a uniform service construction contract.
     */
    private final LdapServerOptions options;

    /**
     * External connection-state and directory implementation.
     */
    private final DirectoryStore store;

    /**
     * Creates an Unbind service for one compiled LDAP Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param options    validated LDAP Provider options
     * @param store      externally implemented directory store
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public UnbindService(final String providerId, final LdapServerOptions options, final DirectoryStore store) {
        this.providerId = Assert.notBlank(providerId, "LDAP Unbind Provider id must not be blank");
        this.options = Assert.notNull(options, "LDAP Unbind Provider options must not be null");
        this.store = Assert.notNull(store, "LDAP Unbind directory store must not be null");
    }

    /**
     * Determines whether an unsupported critical control suppresses store execution.
     *
     * @param message complete request message
     * @return whether the request must be ignored without a response
     */
    private static boolean hasCriticalControl(final LdapMessage message) {
        return message.controls().stream().anyMatch(control -> control.criticality());
    }

    /**
     * Creates a safe operational failure without protocol payload data.
     *
     * @param description fixed non-sensitive description
     * @return closed operational failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._503, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Releases external connection state and never produces an LDAP response message.
     *
     * @param message complete Unbind request message
     * @param context immutable invocation context with a trusted connection snapshot
     * @param timeout shared end-to-end time budget
     * @return stage containing only internal empty success or a closed failure
     */
    public CompletionStage<Outcome<Void>> unbind(
            final LdapMessage message,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(message, "LDAP Unbind message must not be null");
        Assert.notNull(context, "LDAP Unbind context must not be null");
        Assert.notNull(timeout, "LDAP Unbind time budget must not be null");
        if (!(message.protocolOp() instanceof UnbindRequest request) || message.messageId() <= 0
                || hasCriticalControl(message)) {
            return CompletableFuture.completedFuture(Outcome.succeeded(null));
        }
        Assert.isTrue(context.network().connection().isPresent(), "LDAP Unbind requires a trusted connection snapshot");
        final String connectionId = context.network().connection().getOrThrow().id();
        if (timeout.expired()) {
            return CompletableFuture.completedFuture(Outcome.failed(failure("LDAP Unbind time budget expired")));
        }
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = store.unbind(providerId, connectionId, request, context, timeout);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(Outcome.failed(failure("LDAP Unbind directory store failed")));
        }
        if (stage == null) {
            return CompletableFuture
                    .completedFuture(Outcome.failed(failure("LDAP Unbind directory store returned no stage")));
        }
        return stage.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.failed(failure("LDAP Unbind directory store failed")));
    }

}
