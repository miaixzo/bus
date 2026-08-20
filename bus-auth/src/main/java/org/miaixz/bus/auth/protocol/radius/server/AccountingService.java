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
package org.miaixz.bus.auth.protocol.radius.server;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.radius.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Orchestrates RFC 2866 Accounting validation, durable external recording, and response authentication.
 * <p>
 * An Accounting-Response is emitted only after the external handler succeeds. Any validation, client, secret,
 * authenticator, or persistence failure remains an internal failure for the transport adapter to silently discard.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AccountingService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen accepted versions and packet limits.
     */
    private final RadiusProviderSettings settings;

    /**
     * Exact external client and Accounting data port.
     */
    private final RadiusRequestHandler handler;

    /**
     * External short-lived shared-secret resolver.
     */
    private final SecretResolver secretResolver;

    /**
     * Historic security and RADIUS/1.1 response correlator.
     */
    private final RadiusAuthenticator authenticator;

    /**
     * Creates an Accounting service for one compiled RADIUS Provider.
     *
     * @param providerId     compiled server-role Source identifier
     * @param settings       validated RADIUS settings
     * @param handler        exact external request handler binding
     * @param secretResolver shared-secret lease resolver
     * @param authenticator  packet security implementation
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public AccountingService(final String providerId, final RadiusProviderSettings settings,
            final RadiusRequestHandler handler, final SecretResolver secretResolver,
            final RadiusAuthenticator authenticator) {
        this.providerId = Assert.notBlank(providerId, "RADIUS Accounting Provider id must not be blank");
        this.settings = Assert.notNull(settings, "RADIUS Accounting settings must not be null");
        this.handler = Assert.notNull(handler, "RADIUS Accounting handler must not be null");
        this.secretResolver = Assert.notNull(secretResolver, "RADIUS Accounting secret resolver must not be null");
        this.authenticator = Assert.notNull(authenticator, "RADIUS Accounting authenticator must not be null");
    }

    /**
     * Enforces required NAS and Acct-Status-Type fields and RFC 2866/3579 forbidden Attributes.
     *
     * @param request decoded Accounting request
     * @return whether the Attribute sequence satisfies the base Accounting profile
     */
    private static boolean validAttributes(final AccountingRequest request) {
        final boolean nas = has(request, Radius.Attributes.NAS_IP_ADDRESS)
                || has(request, Radius.Attributes.NAS_IDENTIFIER);
        final long statuses = request.attributes().stream()
                .filter(
                        attribute -> attribute.type().value() == Radius.Attributes.ACCT_STATUS_TYPE
                                && attribute.value().length == 4)
                .count();
        final boolean messageAuthenticatorForbidden = request.header() instanceof RadiusPacket.LegacyHeader
                && has(request, Radius.Attributes.MESSAGE_AUTHENTICATOR);
        final boolean forbidden = has(request, Radius.Attributes.USER_PASSWORD)
                || has(request, Radius.Attributes.CHAP_PASSWORD) || has(request, Radius.Attributes.REPLY_MESSAGE)
                || has(request, Radius.Attributes.STATE) || has(request, Radius.Attributes.EAP_MESSAGE)
                || messageAuthenticatorForbidden;
        return nas && statuses == 1 && !forbidden;
    }

    /**
     * Removes RFC 9765 invalid Message-Authenticator Attributes before client resolution and accounting processing.
     *
     * @param request RADIUS/1.1 Accounting request
     * @return sanitized request with the original Token
     */
    private static AccountingRequest sanitize(final AccountingRequest request) {
        return new AccountingRequest(request.header(), request.attributes().stream()
                .filter(attribute -> attribute.type().value() != Radius.Attributes.MESSAGE_AUTHENTICATOR).toList());
    }

    /**
     * Replaces response Proxy-State with all request Proxy-State Attributes in request order.
     *
     * @param response handler response
     * @param request  matching request
     * @return standard correlated Accounting-Response
     */
    private static AccountingResponse proxyState(final AccountingResponse response, final AccountingRequest request) {
        final ArrayList<RadiusAttribute> attributes = new ArrayList<>();
        response.attributes().stream().filter(attribute -> attribute.type().value() != Radius.Attributes.PROXY_STATE)
                .forEach(attributes::add);
        request.attributes().stream().filter(attribute -> attribute.type().value() == Radius.Attributes.PROXY_STATE)
                .forEach(attributes::add);
        return new AccountingResponse(response.header(), attributes);
    }

    /**
     * Tests whether a packet contains one Attribute Type.
     *
     * @param packet packet to inspect
     * @param type   unsigned Attribute Type
     * @return whether at least one occurrence exists
     */
    private static boolean has(final RadiusPacket packet, final int type) {
        return packet.attributes().stream().anyMatch(attribute -> attribute.type().value() == type);
    }

    /**
     * Converts a missing asynchronous Outcome into an operational failure.
     *
     * @param outcome     candidate outcome
     * @param description safe missing-value description
     * @param <T>         success value type
     * @return non-null outcome
     */
    private static <T> Outcome<T> safe(final Outcome<T> outcome, final String description) {
        return outcome == null ? Outcome.failed(failure(ErrorCode._503, description)) : outcome;
    }

    /**
     * Creates a protocol-invalid silent-discard rejection.
     *
     * @param description safe fixed description
     * @return completed rejection stage
     */
    private static CompletionStage<Outcome<AccountingResponse>> discard(final String description) {
        return completed(Outcome.rejected(failure(ErrorCode._400, description)));
    }

    /**
     * Creates an operational silent-discard failure.
     *
     * @param description safe fixed description
     * @return completed failure stage
     */
    private static CompletionStage<Outcome<AccountingResponse>> unavailable(final String description) {
        return completed(Outcome.failed(failure(ErrorCode._503, description)));
    }

    /**
     * Creates a safe Bus failure without packet data or secrets.
     *
     * @param error       existing Bus error
     * @param description fixed non-sensitive description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
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
     * Records one complete Accounting-Request and returns its transport-ready acknowledgment.
     *
     * @param request complete standard Accounting-Request
     * @param context immutable invocation context with trusted network metadata
     * @param timeout shared end-to-end operation budget
     * @return stage containing Accounting-Response or a silent-discard failure
     */
    public CompletionStage<Outcome<AccountingResponse>> accounting(
            final AccountingRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "RADIUS Accounting-Request must not be null");
        Assert.notNull(context, "RADIUS Accounting context must not be null");
        Assert.notNull(timeout, "RADIUS Accounting time budget must not be null");
        if (timeout.expired()) {
            return unavailable("RADIUS Accounting time budget expired");
        }
        if (!validTransport(request, context) || !validAttributes(request)) {
            return discard("RADIUS Accounting-Request failed protocol validation");
        }
        final AccountingRequest effective = request.header() instanceof RadiusPacket.Radius11Header ? sanitize(request)
                : request;
        final CompletionStage<Outcome<RadiusRequestHandler.Client>> stage;
        try {
            stage = handler.resolve(providerId, context.network().remoteAddress(), effective, context, timeout);
        } catch (RuntimeException exception) {
            return unavailable("RADIUS Accounting client resolution failed");
        }
        if (stage == null) {
            return unavailable("RADIUS Accounting client resolver returned no stage");
        }
        return stage.thenCompose(
                outcome -> switch (safe(outcome, "RADIUS Accounting client resolver returned no outcome")) {
                    case Outcome.Succeeded<RadiusRequestHandler.Client> succeeded -> client(
                            effective,
                            context,
                            timeout,
                            succeeded.value());
                    case Outcome.Rejected<RadiusRequestHandler.Client> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<RadiusRequestHandler.Client> failed -> completed(
                            Outcome.failed(failed.failure()));
                }).exceptionally(
                        exception -> Outcome.failed(
                                failure(ErrorCode._503, "RADIUS Accounting client resolution failed asynchronously")));
    }

    /**
     * Enforces client Code policy and version-specific request authentication.
     *
     * @param request validated Accounting request
     * @param context invocation context
     * @param timeout operation budget
     * @param client  resolved client
     * @return durable Accounting stage
     */
    private CompletionStage<Outcome<AccountingResponse>> client(
            final AccountingRequest request,
            final Context context,
            final Timeout.Budget timeout,
            final RadiusRequestHandler.Client client) {
        if (client == null || !client.allowedCodes().contains(new RadiusCode(Radius.Codes.ACCOUNTING_REQUEST))) {
            return discard("RADIUS client is not permitted to send Accounting-Request");
        }
        if (request.header() instanceof RadiusPacket.Radius11Header) {
            if (client.sharedSecret().isPresent()) {
                return discard("RADIUS/1.1 client must not have a shared secret");
            }
            return invokeHandler(request, client, context.withClientId(client.id()), timeout);
        }
        final Credential.Reference reference = client.sharedSecret().getOrNull();
        if (reference == null) {
            return discard("Historic RADIUS Accounting client has no shared secret");
        }
        final CompletionStage<Outcome<SecretLease>> resolved = resolveSecret(reference, context, timeout);
        if (resolved == null) {
            return unavailable("RADIUS Accounting shared-secret resolver returned no stage");
        }
        return resolved.thenCompose(
                outcome -> switch (safe(outcome, "RADIUS Accounting secret resolver returned no outcome")) {
                    case Outcome.Succeeded<SecretLease> succeeded -> {
                        final SecretLease lease = succeeded.value();
                        if (lease == null) {
                            yield unavailable("RADIUS Accounting secret resolver returned no lease");
                        }
                        final boolean valid;
                        try (lease) {
                            valid = authenticator.verifyAccountingRequest(request, lease);
                        }
                        yield valid ? invokeHandler(request, client, context.withClientId(client.id()), timeout)
                                : discard("RADIUS Accounting Request Authenticator is invalid");
                    }
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Invokes durable external accounting after all request security checks.
     *
     * @param request validated Accounting request
     * @param client  resolved client
     * @param context context carrying verified client id
     * @param timeout operation budget
     * @return handler response stage
     */
    private CompletionStage<Outcome<AccountingResponse>> invokeHandler(
            final AccountingRequest request,
            final RadiusRequestHandler.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        final CompletionStage<Outcome<AccountingResponse>> stage;
        try {
            stage = handler.accounting(providerId, client, request, context, timeout);
        } catch (RuntimeException exception) {
            return unavailable("RADIUS Accounting handler failed");
        }
        if (stage == null) {
            return unavailable("RADIUS Accounting handler returned no stage");
        }
        return stage.thenCompose(outcome -> switch (safe(outcome, "RADIUS Accounting handler returned no outcome")) {
            case Outcome.Succeeded<AccountingResponse> succeeded -> finish(
                    succeeded.value(),
                    request,
                    client,
                    context,
                    timeout);
            case Outcome.Rejected<AccountingResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<AccountingResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Correlates Proxy-State and generates the version-specific response security fields.
     *
     * @param response handler-produced response
     * @param request  matching request
     * @param client   resolved client
     * @param context  verified-client context
     * @param timeout  operation budget
     * @return transport-ready Accounting-Response stage
     */
    private CompletionStage<Outcome<AccountingResponse>> finish(
            final AccountingResponse response,
            final AccountingRequest request,
            final RadiusRequestHandler.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        if (response == null || !response.header().equals(request.header())) {
            return unavailable("RADIUS Accounting handler returned an invalid response correlation");
        }
        final AccountingResponse correlated = proxyState(response, request);
        if (request.header() instanceof RadiusPacket.Radius11Header) {
            return completed(
                    Outcome.succeeded((AccountingResponse) authenticator.radius11Response(correlated, request)));
        }
        final CompletionStage<Outcome<SecretLease>> resolved = resolveSecret(
                client.sharedSecret().getOrNull(),
                context,
                timeout);
        if (resolved == null) {
            return unavailable("RADIUS Accounting response secret resolver returned no stage");
        }
        return resolved.thenApply(
                outcome -> switch (safe(outcome, "RADIUS Accounting response secret resolver returned no outcome")) {
                    case Outcome.Succeeded<SecretLease> succeeded -> sign(correlated, request, succeeded.value());
                    case Outcome.Rejected<SecretLease> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<SecretLease> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Uses and closes one independent Accounting response-signing lease.
     *
     * @param response correlated response
     * @param request  matching request
     * @param lease    new response-signing lease
     * @return signed Accounting outcome
     */
    private Outcome<AccountingResponse> sign(
            final AccountingResponse response,
            final AccountingRequest request,
            final SecretLease lease) {
        if (lease == null) {
            return Outcome.failed(failure(ErrorCode._503, "RADIUS Accounting response secret lease is missing"));
        }
        try (lease) {
            return Outcome.succeeded((AccountingResponse) authenticator.authenticateResponse(response, request, lease));
        }
    }

    /**
     * Validates version policy and the trusted TLS boundary required for RADIUS/1.1.
     *
     * @param request Accounting request
     * @param context invocation context
     * @return whether transport and header may be processed
     */
    private boolean validTransport(final AccountingRequest request, final Context context) {
        if (!settings.versions().contains(request.header().version())) {
            return false;
        }
        if (request.header().version() == RadiusPacket.Version.RADIUS_1_0) {
            return true;
        }
        return context.network().connection().isPresent()
                && context.network().connection().getOrThrow().transport() == Endpoint.Transport.RADIUS_TLS;
    }

    /**
     * Resolves one shared secret while closing synchronous resolver failure.
     *
     * @param reference exact shared-secret reference
     * @param context   invocation context
     * @param timeout   operation budget
     * @return resolver stage or {@code null} when no stage was returned
     */
    private CompletionStage<Outcome<SecretLease>> resolveSecret(
            final Credential.Reference reference,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            return secretResolver.resolve(reference, context, timeout);
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(failure(ErrorCode._503, "RADIUS Accounting shared-secret resolution failed")));
        }
    }

}
