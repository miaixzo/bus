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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.ldap.AbandonRequest;
import org.miaixz.bus.auth.source.protocol.ldap.LdapMessage;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Delegates RFC 4511 Abandon correlation while preserving its absolute no-response semantics.
 *
 * @author Kimi Liu
 */
public class AbandonService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String sourceId;

    /**
     * Frozen LDAP server options retained for uniform construction.
     */
    private final LdapServerOptions options;

    /**
     * External connection-operation state owner.
     */
    private final DirectoryStore store;

    /**
     * Creates an Abandon service for one compiled LDAP server.
     *
     * @param sourceId compiled server-role Source identifier
     * @param options  validated LDAP server options
     * @param store    externally implemented directory store
     */
    public AbandonService(final String sourceId, final LdapServerOptions options, final DirectoryStore store) {
        this.sourceId = Assert.notBlank(sourceId, "LDAP Abandon Source id must not be blank");
        this.options = Assert.notNull(options, "LDAP Abandon LDAP server options must not be null");
        this.store = Assert.notNull(store, "LDAP Abandon directory store must not be null");
    }

    /**
     * Creates a safe operational failure that never becomes an Abandon response.
     *
     * @param description fixed non-sensitive description
     * @return closed failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._503, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Requests cancellation of the target operation and never returns an LDAP response message.
     *
     * @param message complete Abandon request message
     * @param context immutable invocation context with trusted connection state
     * @param timeout shared end-to-end timeout
     * @return stage containing only internal empty success or a closed operational failure
     */
    public CompletionStage<Outcome<Void>> abandon(
            final LdapMessage message,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(message, "LDAP Abandon message must not be null");
        Assert.notNull(context, "LDAP Abandon context must not be null");
        Assert.notNull(timeout, "LDAP Abandon timeout must not be null");
        if (!(message.protocolOp() instanceof AbandonRequest request) || message.messageId() <= 0
                || message.controls().stream().anyMatch(control -> control.criticality())) {
            return CompletableFuture.completedFuture(Outcome.succeeded(null));
        }
        Assert.isTrue(
                context.network().connection().isPresent(),
                "LDAP Abandon requires a trusted connection snapshot");
        if (timeout.expired()) {
            return CompletableFuture.completedFuture(Outcome.failed(failure("LDAP Abandon timeout expired")));
        }
        final CompletionStage<Outcome<Void>> stage;
        try {
            stage = store
                    .abandon(sourceId, context.network().connection().getOrThrow().id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(Outcome.failed(failure("LDAP Abandon directory store failed")));
        }
        if (stage == null) {
            return CompletableFuture
                    .completedFuture(Outcome.failed(failure("LDAP Abandon directory store returned no stage")));
        }
        return stage.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.failed(failure("LDAP Abandon directory store failed")));
    }

}
