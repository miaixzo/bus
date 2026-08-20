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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.radius.*;
import org.miaixz.bus.auth.protocol.radius.codec.EapMessageCodec;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.runtime.LoadResult;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Orchestrates the RFC 2865 Access operation across wire validation, client resolution, and external policy data.
 * <p>
 * Invalid packets produce closed internal failures for the transport adapter to silently discard. A normal credential
 * or authorization denial is represented only by a handler-produced {@link AccessReject} success packet.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AccessService {

    /**
     * Compiled server-role Source identifier.
     */
    private final String providerId;

    /**
     * Frozen accepted versions and Access security policy.
     */
    private final RadiusServerOptions options;

    /**
     * Exact external client and Access data port.
     */
    private final RadiusRequestHandler handler;

    /**
     * External short-lived shared-secret loader and framework-owned parser.
     */
    private final ExecutionServices services;

    /**
     * Historic security and RADIUS/1.1 response correlator.
     */
    private final RadiusAuthenticator authenticator;

    /**
     * RFC 3579 single-packet fragment codec.
     */
    private final EapMessageCodec eapCodec;

    /**
     * Creates an Access service for one compiled RADIUS Provider.
     *
     * @param providerId    compiled server-role Source identifier
     * @param options       validated RADIUS options
     * @param handler       exact external request handler binding
     * @param services      external loaders and pure parsers
     * @param authenticator packet security implementation
     * @param eapCodec      EAP-Message fragment codec
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public AccessService(final String providerId, final RadiusServerOptions options, final RadiusRequestHandler handler,
            final ExecutionServices services, final RadiusAuthenticator authenticator, final EapMessageCodec eapCodec) {
        this.providerId = Assert.notBlank(providerId, "RADIUS Access Provider id must not be blank");
        this.options = Assert.notNull(options, "RADIUS Access options must not be null");
        this.handler = Assert.notNull(handler, "RADIUS Access handler must not be null");
        this.services = Assert.notNull(services, "RADIUS Access execution services must not be null");
        this.authenticator = Assert.notNull(authenticator, "RADIUS Access authenticator must not be null");
        this.eapCodec = Assert.notNull(eapCodec, "RADIUS Access EAP codec must not be null");
    }

    /**
     * Removes RFC 9765 invalid Message-Authenticator Attributes before external processing.
     *
     * @param request RADIUS/1.1 request
     * @return sanitized request with the original Token
     */
    private static AccessRequest sanitize(final AccessRequest request) {
        return new AccessRequest(request.header(), request.attributes().stream()
                .filter(attribute -> attribute.type().value() != Radius.Attributes.MESSAGE_AUTHENTICATOR).toList());
    }

    /**
     * Replaces response Proxy-State with all and only request Proxy-State Attributes in request order.
     *
     * @param response validated response
     * @param request  matching request
     * @return response with standard opaque Proxy-State copying
     */
    private static RadiusPacket proxyState(final RadiusPacket response, final AccessRequest request) {
        final ArrayList<RadiusAttribute> attributes = new ArrayList<>();
        response.attributes().stream().filter(attribute -> attribute.type().value() != Radius.Attributes.PROXY_STATE)
                .forEach(attributes::add);
        request.attributes().stream().filter(attribute -> attribute.type().value() == Radius.Attributes.PROXY_STATE)
                .forEach(attributes::add);
        if (response instanceof AccessAccept)
            return new AccessAccept(response.header(), attributes);
        if (response instanceof AccessReject)
            return new AccessReject(response.header(), attributes);
        return new AccessChallenge(response.header(), attributes);
    }

    /**
     * Counts one Attribute Type in a packet.
     *
     * @param packet packet to inspect
     * @param type   unsigned Attribute Type
     * @return occurrence count
     */
    private static int count(final RadiusPacket packet, final int type) {
        return (int) packet.attributes().stream().filter(attribute -> attribute.type().value() == type).count();
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
     * Converts a missing asynchronous Outcome into an operational failure value.
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
     * Creates a protocol-invalid failure whose external wire action is silent discard.
     *
     * @param description safe fixed description
     * @return already completed rejection stage
     */
    private static CompletionStage<Outcome<RadiusPacket>> discard(final String description) {
        return completed(Outcome.rejected(failure(ErrorCode._400, description)));
    }

    /**
     * Creates an operational failure whose external wire action is silent discard.
     *
     * @param description safe fixed description
     * @return already completed failure stage
     */
    private static CompletionStage<Outcome<RadiusPacket>> unavailable(final String description) {
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
     * Processes one complete Access-Request and returns a transport-ready standard response packet.
     *
     * @param request complete standard Access-Request
     * @param context immutable invocation context with trusted remote address and optional connection metadata
     * @param timeout shared end-to-end operation budget
     * @return stage containing Access-Accept, Access-Reject, Access-Challenge, or a silent-discard failure
     */
    public CompletionStage<Outcome<RadiusPacket>> access(
            final AccessRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "RADIUS Access-Request must not be null");
        Assert.notNull(context, "RADIUS Access context must not be null");
        Assert.notNull(timeout, "RADIUS Access time budget must not be null");
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._503, "RADIUS Access time budget expired")));
        }
        if (!validTransport(request, context) || !validRequestAttributes(request)) {
            return discard("RADIUS Access-Request failed protocol validation");
        }
        final AccessRequest effective = request.header() instanceof RadiusPacket.Radius11Header ? sanitize(request)
                : request;
        final CompletionStage<Outcome<RadiusRequestHandler.Client>> resolved;
        try {
            resolved = handler.resolve(providerId, context.network().remoteAddress(), effective, context, timeout);
        } catch (RuntimeException exception) {
            return unavailable("RADIUS client resolution failed");
        }
        if (resolved == null) {
            return unavailable("RADIUS client loader returned no stage");
        }
        return resolved.thenCompose(outcome -> switch (safe(outcome, "RADIUS client loader returned no outcome")) {
            case Outcome.Succeeded<RadiusRequestHandler.Client> succeeded -> client(
                    effective,
                    context,
                    timeout,
                    succeeded.value());
            case Outcome.Rejected<RadiusRequestHandler.Client> rejected -> completed(
                    Outcome.rejected(rejected.failure()));
            case Outcome.Failed<RadiusRequestHandler.Client> failed -> completed(Outcome.failed(failed.failure()));
        }).exceptionally(
                exception -> Outcome.failed(failure(ErrorCode._503, "RADIUS client resolution failed asynchronously")));
    }

    /**
     * Applies client Code policy and version-specific request authentication.
     *
     * @param request validated Access request
     * @param context invocation context
     * @param timeout operation budget
     * @param client  resolved client
     * @return next Access stage
     */
    private CompletionStage<Outcome<RadiusPacket>> client(
            final AccessRequest request,
            final Context context,
            final Timeout.Budget timeout,
            final RadiusRequestHandler.Client client) {
        if (client == null || !client.allowedCodes().contains(new RadiusCode(Radius.Codes.ACCESS_REQUEST))) {
            return discard("RADIUS client is not permitted to send Access-Request");
        }
        if (request.header() instanceof RadiusPacket.Radius11Header) {
            if (client.sharedSecret().isPresent()) {
                return discard("RADIUS/1.1 client must not have a shared secret");
            }
            return invokeHandler(request, client, context.withClientId(client.id()), timeout);
        }
        final Credential.Reference reference = client.sharedSecret().getOrNull();
        if (reference == null) {
            return discard("Historic RADIUS client has no shared secret");
        }
        final CompletionStage<Outcome<SecretLease>> resolved = resolveSecret(reference, context, timeout);
        if (resolved == null) {
            return unavailable("RADIUS shared-secret loader returned no stage");
        }
        return resolved
                .thenCompose(outcome -> switch (safe(outcome, "RADIUS shared-secret loader returned no outcome")) {
                    case Outcome.Succeeded<SecretLease> succeeded -> {
                        final SecretLease lease = succeeded.value();
                        if (lease == null) {
                            yield unavailable("RADIUS shared-secret loader returned no lease");
                        }
                        final boolean valid;
                        try (lease) {
                            final boolean eap = has(request, Radius.Attributes.EAP_MESSAGE);
                            valid = authenticator
                                    .verifyAccessRequest(request, lease, options.requireMessageAuthenticator() || eap);
                        }
                        yield valid ? invokeHandler(request, client, context.withClientId(client.id()), timeout)
                                : discard("RADIUS Access-Request authenticator is invalid");
                    }
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Calls external Access data processing or produces the required local EAP rejection.
     *
     * @param request wire-validated and version-sanitized request
     * @param client  resolved client
     * @param context context carrying the verified client id
     * @param timeout operation budget
     * @return handler or local response stage
     */
    private CompletionStage<Outcome<RadiusPacket>> invokeHandler(
            final AccessRequest request,
            final RadiusRequestHandler.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        if (!options.eapSupported() && has(request, Radius.Attributes.EAP_MESSAGE)) {
            return finish(new AccessReject(request.header(), List.of()), request, client, context, timeout);
        }
        final CompletionStage<Outcome<RadiusPacket>> stage;
        try {
            stage = handler.access(providerId, client, request, context, timeout);
        } catch (RuntimeException exception) {
            return unavailable("RADIUS Access handler failed");
        }
        if (stage == null) {
            return unavailable("RADIUS Access handler returned no stage");
        }
        return stage.thenCompose(outcome -> switch (safe(outcome, "RADIUS Access handler returned no outcome")) {
            case Outcome.Succeeded<RadiusPacket> succeeded -> finish(
                    succeeded.value(),
                    request,
                    client,
                    context,
                    timeout);
            case Outcome.Rejected<RadiusPacket> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<RadiusPacket> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Validates, correlates, restores Proxy-State, and authenticates one response.
     *
     * @param response handler-produced standard response
     * @param request  matching request supplied to the handler
     * @param client   resolved client
     * @param context  verified-client context
     * @param timeout  operation budget
     * @return transport-ready response stage
     */
    private CompletionStage<Outcome<RadiusPacket>> finish(
            final RadiusPacket response,
            final AccessRequest request,
            final RadiusRequestHandler.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        if (!(response instanceof AccessAccept || response instanceof AccessReject
                || response instanceof AccessChallenge) || !response.header().equals(request.header())) {
            return unavailable("RADIUS Access handler returned an invalid response correlation");
        }
        try {
            eapCodec.decode(response.attributes());
        } catch (RuntimeException exception) {
            return unavailable("RADIUS Access handler returned invalid EAP-Message attributes");
        }
        final RadiusPacket correlated = proxyState(response, request);
        if (request.header() instanceof RadiusPacket.Radius11Header) {
            return completed(Outcome.succeeded(authenticator.radius11Response(correlated, request)));
        }
        final Credential.Reference reference = client.sharedSecret().getOrNull();
        final CompletionStage<Outcome<SecretLease>> resolved = resolveSecret(reference, context, timeout);
        if (resolved == null) {
            return unavailable("RADIUS response shared-secret loader returned no stage");
        }
        return resolved
                .thenApply(outcome -> switch (safe(outcome, "RADIUS response secret loader returned no outcome")) {
                    case Outcome.Succeeded<SecretLease> succeeded -> sign(correlated, request, succeeded.value());
                    case Outcome.Rejected<SecretLease> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<SecretLease> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Uses and closes the independent response-signing lease in one synchronous continuation.
     *
     * @param response correlated response
     * @param request  matching request
     * @param lease    new response-signing lease
     * @return signed outcome
     */
    private Outcome<RadiusPacket> sign(
            final RadiusPacket response,
            final AccessRequest request,
            final SecretLease lease) {
        if (lease == null) {
            return Outcome.failed(failure(ErrorCode._503, "RADIUS response secret loader returned no lease"));
        }
        try (lease) {
            return Outcome.succeeded(authenticator.authenticateResponse(response, request, lease));
        }
    }

    /**
     * Validates version selection, RADIUS/1.1 trusted transport, and Provider version policy.
     *
     * @param request Access request
     * @param context invocation context
     * @return whether transport and header may be processed
     */
    private boolean validTransport(final AccessRequest request, final Context context) {
        if (!options.versions().contains(request.header().version())) {
            return false;
        }
        if (request.header().version() == RadiusPacket.Version.RADIUS_1_0) {
            return true;
        }
        return context.network().connection().isPresent()
                && context.network().connection().getOrThrow().transport() == Endpoint.Transport.RADIUS_TLS;
    }

    /**
     * Enforces RFC 2865 NAS and authentication-method occurrence rules plus RFC 3579 EAP framing.
     *
     * @param request decoded Access request
     * @return whether required Attributes and mutually exclusive methods are valid
     */
    private boolean validRequestAttributes(final AccessRequest request) {
        if (!has(request, Radius.Attributes.NAS_IP_ADDRESS) && !has(request, Radius.Attributes.NAS_IDENTIFIER)) {
            return false;
        }
        final int userPassword = count(request, Radius.Attributes.USER_PASSWORD);
        final int chapPassword = count(request, Radius.Attributes.CHAP_PASSWORD);
        final int state = count(request, Radius.Attributes.STATE);
        final boolean eap = has(request, Radius.Attributes.EAP_MESSAGE);
        if (userPassword > 1 || chapPassword > 1 || state > 1
                || (userPassword > 0 ? 1 : 0) + (chapPassword > 0 ? 1 : 0) + (state > 0 ? 1 : 0) + (eap ? 1 : 0) > 1) {
            return false;
        }
        try {
            eapCodec.decode(request.attributes());
            return true;
        } catch (ProtocolException exception) {
            return false;
        }
    }

    /**
     * Loads and parses one shared secret while converting synchronous loader failure into a closed stage.
     *
     * @param reference exact shared-secret reference
     * @param context   invocation context
     * @param timeout   operation budget
     * @return loader/parser stage or {@code null} when the loader returned no stage
     */
    private CompletionStage<Outcome<SecretLease>> resolveSecret(
            final Credential.Reference reference,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            return LoadResult.parse(
                    services.secretLoader().load(reference, context, timeout),
                    loaded -> services.secretParser().parse(reference, loaded));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._503, "RADIUS shared-secret resolution failed")));
        }
    }

}
