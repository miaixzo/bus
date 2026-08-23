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
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.ldap.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Executes RFC 4511 Extended operations and handles the standard StartTLS response boundary locally.
 *
 * @author Kimi Liu
 */
public class ExtendedOperationService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String sourceId;

    /**
     * Frozen LDAP server extended-operation and StartTLS options.
     */
    private final LdapServerOptions options;

    /**
     * External implementation for operations other than framework-handled StartTLS.
     */
    private final DirectoryStore store;

    /**
     * Creates an Extended Operation service for one compiled LDAP server.
     *
     * @param sourceId compiled server-role Source identifier
     * @param options  validated LDAP server options
     * @param store    externally implemented directory store
     */
    public ExtendedOperationService(final String sourceId, final LdapServerOptions options,
            final DirectoryStore store) {
        this.sourceId = Assert.notBlank(sourceId, "LDAP Extended Source id must not be blank");
        this.options = Assert.notNull(options, "LDAP Extended LDAP server options must not be null");
        this.store = Assert.notNull(store, "LDAP Extended directory store must not be null");
    }

    /**
     * Correlates one externally produced ExtendedResponse and preserves failure classification.
     *
     * @param messageId request identifier
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return complete Extended outcome
     */
    private static Outcome<LdapMessage> map(
            final int messageId,
            final Outcome<ExtendedResponse> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Extended directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<ExtendedResponse> succeeded -> succeeded.value() == null
                    ? Outcome.failed(failure("LDAP Extended directory store returned no response"))
                    : Outcome.succeeded(new LdapMessage(messageId, succeeded.value(), List.of()));
            case Outcome.Rejected<ExtendedResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<ExtendedResponse> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Creates one local ExtendedResponse with an absent response value.
     *
     * @param messageId    request identifier
     * @param code         standard LDAP result code
     * @param responseName known response OID, or {@code null} when none can be selected
     * @return successful outcome carrying the formal response
     */
    private static Outcome<LdapMessage> local(
            final int messageId,
            final LdapResultCode code,
            final String responseName) {
        final String diagnostic = code.equals(LdapResultCode.SUCCESS) ? Normal.EMPTY
                : "The LDAP Extended operation was not accepted.";
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY), diagnostic,
                Optional.empty());
        final ExtendedResponse response = new ExtendedResponse(result, Optional.ofNullable(responseName),
                Optional.empty());
        return Outcome.succeeded(new LdapMessage(Math.max(0, messageId), response, List.of()));
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
     * Returns the StartTLS protocol response or delegates another Extended request to the external store.
     * <p>
     * A successful StartTLS response only authorizes the external connection adapter to begin TLS negotiation after
     * writing that response; this service does not own or upgrade the socket.
     * </p>
     *
     * @param message complete Extended request message
     * @param context immutable invocation context with trusted connection state
     * @param timeout shared end-to-end timeout
     * @return stage containing a correlated ExtendedResponse or closed failure
     */
    public CompletionStage<Outcome<LdapMessage>> extended(
            final LdapMessage message,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(message, "LDAP Extended message must not be null");
        Assert.notNull(context, "LDAP Extended context must not be null");
        Assert.notNull(timeout, "LDAP Extended timeout must not be null");
        if (!(message.protocolOp() instanceof ExtendedRequest request) || message.messageId() <= 0) {
            return completed(local(message.messageId(), LdapResultCode.PROTOCOL_ERROR, null));
        }
        if (message.controls().stream().anyMatch(control -> control.criticality())) {
            return completed(
                    local(message.messageId(), LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION, request.requestName()));
        }
        Assert.isTrue(
                context.network().connection().isPresent(),
                "LDAP Extended operation requires a trusted connection snapshot");
        final Context.Network.Connection connection = context.network().connection().getOrThrow();
        if (Ldap.START_TLS_OID.equals(request.requestName())) {
            return completed(startTls(message.messageId(), request, connection.transport()));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Extended operation timeout expired")));
        }
        final CompletionStage<Outcome<ExtendedResponse>> stage;
        try {
            stage = store.extended(sourceId, connection.id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Extended directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Extended directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), outcome, thrown));
    }

    /**
     * Applies the RFC 4511 StartTLS preconditions and creates the response written before external TLS negotiation.
     *
     * @param messageId request identifier
     * @param request   standard StartTLS request
     * @param transport effective current transport
     * @return formal StartTLS response outcome
     */
    private Outcome<LdapMessage> startTls(
            final int messageId,
            final ExtendedRequest request,
            final Endpoint.Transport transport) {
        if (!request.requestValue().isEmpty()) {
            return local(messageId, LdapResultCode.PROTOCOL_ERROR, Ldap.START_TLS_OID);
        }
        if (!options.startTlsSupported()) {
            return local(messageId, LdapResultCode.PROTOCOL_ERROR, Ldap.START_TLS_OID);
        }
        if (transport != Endpoint.Transport.TCP) {
            return local(messageId, LdapResultCode.OPERATIONS_ERROR, Ldap.START_TLS_OID);
        }
        return local(messageId, LdapResultCode.SUCCESS, Ldap.START_TLS_OID);
    }

}
