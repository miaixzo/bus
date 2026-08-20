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
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates and executes the RFC 4511 LDAPv3 Bind operation for one compiled server-role Source.
 *
 * @author Kimi Liu
 */
public final class BindService {

    /**
     * Compiled server-role Source identifier forwarded to the external directory store.
     */
    private final String providerId;

    /**
     * Frozen Provider Bind and transport settings.
     */
    private final LdapProviderSettings settings;

    /**
     * External connection-state and directory implementation.
     */
    private final DirectoryStore store;

    /**
     * Creates a Bind service for one compiled LDAP Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param settings   validated LDAP Provider settings
     * @param store      externally implemented directory store
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public BindService(final String providerId, final LdapProviderSettings settings, final DirectoryStore store) {
        this.providerId = Assert.notBlank(providerId, "LDAP Bind Provider id must not be blank");
        this.settings = Assert.notNull(settings, "LDAP Bind Provider settings must not be null");
        this.store = Assert.notNull(store, "LDAP Bind directory store must not be null");
    }

    /**
     * Converts one external store outcome to the complete correlated Bind response.
     *
     * @param messageId request message identifier
     * @param outcome   external store outcome
     * @param thrown    asynchronous store failure
     * @return correlated Bind outcome
     */
    private static Outcome<LdapMessage> map(
            final int messageId,
            final Outcome<BindResponse> outcome,
            final Throwable thrown) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(failure("LDAP Bind directory store failed"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<BindResponse> succeeded -> succeeded.value() == null
                    ? Outcome.failed(failure("LDAP Bind directory store returned no response"))
                    : Outcome.succeeded(new LdapMessage(messageId, succeeded.value(), List.of()));
            case Outcome.Rejected<BindResponse> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<BindResponse> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Returns the required trusted connection snapshot.
     *
     * @param context immutable invocation context
     * @return trusted connection snapshot
     * @throws IllegalArgumentException if no connection snapshot is present
     */
    private static Context.Network.Connection connection(final Context context) {
        Assert.isTrue(context.network().connection().isPresent(), "LDAP Bind requires a trusted connection snapshot");
        return context.network().connection().getOrThrow();
    }

    /**
     * Determines whether the message carries an unsupported critical control.
     *
     * @param message complete request message
     * @return whether execution must be rejected before reaching the store
     */
    private static boolean hasCriticalControl(final LdapMessage message) {
        return message.controls().stream().anyMatch(control -> control.criticality());
    }

    /**
     * Creates one complete local Bind response with no SASL credentials or response controls.
     *
     * @param messageId  request identifier, normalized to zero only for an invalid envelope
     * @param code       standard LDAP result code
     * @param diagnostic fixed non-sensitive diagnostic
     * @return successful framework outcome carrying the formal response
     */
    private static Outcome<LdapMessage> response(
            final int messageId,
            final LdapResultCode code,
            final String diagnostic) {
        final LdapResult result = new LdapResult(code, new DistinguishedName(Normal.EMPTY), diagnostic,
                org.miaixz.bus.core.lang.Optional.empty());
        final BindResponse response = new BindResponse(result, org.miaixz.bus.core.lang.Optional.empty());
        return Outcome.succeeded(new LdapMessage(Math.max(0, messageId), response, List.of()));
    }

    /**
     * Creates a safe shared Bus store or timeout failure.
     *
     * @param description fixed non-sensitive failure description
     * @return closed operational failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._503, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already completed stage.
     *
     * @param outcome completed value
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one complete LDAPv3 Bind message after standard version, mechanism, control, and TLS checks.
     *
     * @param message complete Bind request message
     * @param context immutable invocation context with a trusted connection snapshot
     * @param timeout shared end-to-end time budget
     * @return stage containing a formal Bind response or a closed store failure
     */
    public CompletionStage<Outcome<LdapMessage>> bind(
            final LdapMessage message,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(message, "LDAP Bind message must not be null");
        Assert.notNull(context, "LDAP Bind context must not be null");
        Assert.notNull(timeout, "LDAP Bind time budget must not be null");
        if (!(message.protocolOp() instanceof BindRequest request) || message.messageId() <= 0) {
            return completed(
                    response(
                            message.messageId(),
                            LdapResultCode.PROTOCOL_ERROR,
                            "The LDAP Bind request is malformed."));
        }
        if (hasCriticalControl(message)) {
            return completed(
                    response(
                            message.messageId(),
                            LdapResultCode.UNAVAILABLE_CRITICAL_EXTENSION,
                            "The LDAP Bind control is not supported."));
        }
        if (request.version() != BindRequest.VERSION_3) {
            return completed(
                    response(message.messageId(), LdapResultCode.PROTOCOL_ERROR, "Only LDAP version 3 is supported."));
        }
        final Context.Network.Connection connection = connection(context);
        final LdapResultCode rejection = validateAuthentication(request, connection.transport());
        if (rejection != null) {
            return completed(
                    response(message.messageId(), rejection, "The LDAP Bind authentication choice is not permitted."));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(failure("LDAP Bind has no remaining time budget")));
        }

        final CompletionStage<Outcome<BindResponse>> stage;
        try {
            stage = store.bind(providerId, connection.id(), request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure("LDAP Bind directory store failed")));
        }
        if (stage == null) {
            return completed(Outcome.failed(failure("LDAP Bind directory store returned no stage")));
        }
        return stage.handle((outcome, thrown) -> map(message.messageId(), outcome, thrown));
    }

    /**
     * Validates the selected standard authentication choice against Provider settings and transport confidentiality.
     *
     * @param request   validated LDAPv3 Bind request
     * @param transport effective connection transport
     * @return rejection result code, or {@code null} when the request may reach the store
     */
    private LdapResultCode validateAuthentication(final BindRequest request, final Endpoint.Transport transport) {
        if (request.authentication() instanceof AuthenticationChoice.Simple simple) {
            final boolean anonymous = request.name().value().isEmpty() && simple.password().length == 0;
            if (anonymous) {
                return settings.anonymousBindSupported() ? null : LdapResultCode.INAPPROPRIATE_AUTHENTICATION;
            }
            if (!settings.simpleBindSupported()) {
                return LdapResultCode.AUTH_METHOD_NOT_SUPPORTED;
            }
            return transport == Endpoint.Transport.TLS ? null : LdapResultCode.CONFIDENTIALITY_REQUIRED;
        }
        final AuthenticationChoice.Sasl sasl = (AuthenticationChoice.Sasl) request.authentication();
        return settings.saslMechanisms().contains(sasl.credentials().mechanism()) ? null
                : LdapResultCode.AUTH_METHOD_NOT_SUPPORTED;
    }

}
