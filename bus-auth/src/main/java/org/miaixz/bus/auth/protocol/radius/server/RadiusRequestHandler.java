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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Outcome.Failed;
import org.miaixz.bus.auth.Outcome.Failure;
import org.miaixz.bus.auth.Outcome.Kind;
import org.miaixz.bus.auth.Outcome.Success;
import org.miaixz.bus.auth.protocol.Handler;
import org.miaixz.bus.auth.protocol.radius.RADIUS.*;
import org.miaixz.bus.auth.protocol.radius.packet.RadiusPacket;
import org.miaixz.bus.auth.protocol.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.auth.protocol.radius.security.MessageAuthenticator;
import org.miaixz.bus.auth.protocol.radius.security.RadiusAuthenticator;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;

/**
 * Verifies one complete packet before product dispatch and emits a signed response or an RFC silent drop.
 *
 * @author Kimi Liu
 */
public final class RadiusRequestHandler implements Handler<Message, Optional<Payload>> {

    /**
     * Protocol capability selecting accounting packet dispatch.
     */
    private static final Capability ACCOUNT = Capability.of("account");

    /**
     * Immutable protocol handler description used by the central mediator.
     */
    private static final Descriptor DESCRIPTOR = new Descriptor("radius", "RADIUS", Protocol.UDP,
            java.util.Set.of(Builder.CAPABILITY_AUTHENTICATE, ACCOUNT), java.util.Map.of(), Options.empty());

    /**
     * Access-Request code.
     */
    private static final int ACCESS_REQUEST = Normal._1;

    /**
     * Accounting-Request code.
     */
    private static final int ACCOUNTING_REQUEST = Normal._4;

    /**
     * Accounting-Response code.
     */
    private static final int ACCOUNTING_RESPONSE = 5;

    /**
     * Configuration.
     */
    private final ServerConfig configuration;

    /**
     * Tenant-aware shared-secret resolver.
     */
    private final SecretResolver secrets;

    /**
     * Access product handler.
     */
    private final AccessHandler access;

    /**
     * Accounting product handler.
     */
    private final AccountingHandler accounting;

    /**
     * Creates one request handler.
     *
     * @param configuration configuration
     * @param secrets       shared-secret resolver
     * @param access        access handler
     * @param accounting    accounting handler
     * @throws ValidateException if any configuration or collaborator is {@code null}
     */
    public RadiusRequestHandler(final ServerConfig configuration, final SecretResolver secrets,
            final AccessHandler access, final AccountingHandler accounting) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS server configuration must not be null"));
        this.secrets = Assert.notNull(secrets, () -> new ValidateException("RADIUS secret resolver must not be null"));
        this.access = Assert.notNull(access, () -> new ValidateException("RADIUS access handler must not be null"));
        this.accounting = Assert
                .notNull(accounting, () -> new ValidateException("RADIUS accounting handler must not be null"));
    }

    /**
     * Builds a signed response datagram.
     *
     * @param datagram   source datagram
     * @param request    request packet
     * @param code       response code
     * @param attributes response attributes
     * @param secret     shared secret
     * @return optional response
     * @throws ValidateException if packet, response code, attributes, or shared secret is invalid
     */
    static Optional<Payload> response(
            final RadiusPacket request,
            final int code,
            final java.util.List<org.miaixz.bus.auth.protocol.radius.packet.RadiusAttribute> attributes,
            final byte[] secret) {
        final RadiusPacket unsigned = new RadiusPacket(code, request.identifier(),
                new byte[RadiusPacket.AUTHENTICATOR_BYTES], attributes);
        final RadiusPacket signed = new RadiusPacket(code, request.identifier(),
                RadiusAuthenticator.response(unsigned, request.authenticator(), secret), attributes);
        return Optional.of(Payload.of(RadiusPacketCodec.encode(signed)));
    }

    /**
     * Maps one product access decision.
     *
     * @param decision decision
     * @return RADIUS code
     * @throws NullPointerException if the decision is {@code null}
     */
    static int accessCode(final AccessDecision decision) {
        return switch (decision.type()) {
            case ACCEPT -> Normal._2;
            case REJECT -> Normal._3;
            case CHALLENGE -> 11;
        };
    }

    /**
     * Returns the immutable RADIUS handler descriptor.
     *
     * @return UDP descriptor declaring authentication and accounting capabilities
     */
    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * Dispatches one complete RADIUS message by its standard packet code through the protocol handler contract.
     *
     * @param invocation non-null operation context
     * @param input      non-null Fabric UDP message
     * @return non-null stage containing a standard response payload or RFC silent drop
     * @throws ValidateException if an input is {@code null}
     */
    @Override
    public CompletionStage<Outcome<Optional<Payload>>> handle(final Context invocation, final Message input) {
        final Context context = Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
        final Message datagram = Assert.notNull(input, () -> new ValidateException("RADIUS message must not be null"));
        final int code;
        try {
            code = RadiusPacketCodec.decode(datagram.payload().bytes(RadiusPacketCodec.MAXIMUM_PACKET_BYTES)).code();
        } catch (final RuntimeException invalid) {
            return CompletableFuture.completedFuture(new Success<>(Optional.empty()));
        }
        final CompletionStage<Optional<Payload>> stage = code == ACCESS_REQUEST ? authentication(context, datagram)
                : code == ACCOUNTING_REQUEST ? accounting(context, datagram)
                        : CompletableFuture.completedFuture(Optional.empty());
        return Assert.notNull(stage, "RADIUS handler stage must be not null!").handle((value, failure) -> {
            if (failure == null) {
                return new Success<>(Assert.notNull(value, "RADIUS handler result must be not null!"));
            }
            return new Failed<>(new Failure(Kind.REMOTE, ErrorCode._100805, true), ExceptionKit.unwrap(failure));
        });
    }

    /**
     * Handles one authentication datagram.
     *
     * @param invocation context
     * @param datagram   datagram
     * @return optional response
     * @throws ValidateException if the context or message is {@code null}
     */
    public CompletionStage<Optional<Payload>> authentication(final Context invocation, final Message datagram) {
        final Context context = Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
        final Message message = Assert
                .notNull(datagram, () -> new ValidateException("RADIUS message must not be null"));
        final RadiusPacket request;
        try {
            request = RadiusPacketCodec.decode(message.payload().bytes(RadiusPacketCodec.MAXIMUM_PACKET_BYTES));
            if (request.code() != ACCESS_REQUEST) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        } catch (final RuntimeException invalid) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return secret(context).thenCompose(secret -> {
            final boolean protectedPacket = request.attributes().stream()
                    .anyMatch(attribute -> attribute.type() == 79 || attribute.type() == MessageAuthenticator.TYPE);
            if (protectedPacket && !MessageAuthenticator.verify(request, secret)) {
                Arrays.fill(secret, (byte) Normal._0);
                return CompletableFuture.completedFuture(Optional.empty());
            }
            final CompletionStage<AccessDecision> handled = Assert.notNull(
                    access.handle(context, new AccessRequest(request.attributes())),
                    "RADIUS access handler stage must be not null!");
            return handled.thenApply(decision -> response(request, accessCode(decision), decision.attributes(), secret))
                    .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Handles one accounting datagram.
     *
     * @param invocation context
     * @param datagram   datagram
     * @return optional response
     * @throws ValidateException if the context or message is {@code null}
     */
    public CompletionStage<Optional<Payload>> accounting(final Context invocation, final Message datagram) {
        final Context context = Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
        final Message message = Assert
                .notNull(datagram, () -> new ValidateException("RADIUS message must not be null"));
        final RadiusPacket request;
        try {
            request = RadiusPacketCodec.decode(message.payload().bytes(RadiusPacketCodec.MAXIMUM_PACKET_BYTES));
            if (request.code() != ACCOUNTING_REQUEST) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        } catch (final RuntimeException invalid) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return secret(context).thenCompose(secret -> {
            final byte[] expected = RadiusAuthenticator.accounting(request, secret);
            if (!RadiusAuthenticator.verify(expected, request.authenticator())) {
                Arrays.fill(secret, (byte) Normal._0);
                return CompletableFuture.completedFuture(Optional.empty());
            }
            final CompletionStage<Void> handled = Assert.notNull(
                    accounting.handle(context, new AccountingRequest(request.attributes())),
                    "RADIUS accounting handler stage must be not null!");
            return handled.thenApply(ignored -> response(request, ACCOUNTING_RESPONSE, java.util.List.of(), secret))
                    .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Resolves and converts a shared secret.
     *
     * @param invocation context
     * @return secret bytes
     * @throws ValidateException if context, resolver stage, or secret value is {@code null}
     */
    CompletionStage<byte[]> secret(final Context invocation) {
        final Context context = Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
        final CompletionStage<char[]> resolved = Assert.notNull(
                secrets.resolve(context, "radius", configuration.secretKey()),
                "RADIUS secret resolver stage must be not null!");
        return resolved.thenApply(value -> {
            final char[] characters = Assert.notNull(value, "RADIUS shared secret must be not null!");
            final char[] copy = Arrays.copyOf(characters, characters.length);
            Arrays.fill(characters, (char) Normal._0);
            try {
                final ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(copy));
                final byte[] result = new byte[encoded.remaining()];
                encoded.get(result);
                return result;
            } finally {
                Arrays.fill(copy, (char) Normal._0);
            }
        });
    }

}
