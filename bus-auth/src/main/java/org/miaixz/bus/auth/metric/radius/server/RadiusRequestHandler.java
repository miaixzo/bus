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
package org.miaixz.bus.auth.metric.radius.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Datagram;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.RADIUS.*;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacket;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.auth.metric.radius.security.MessageAuthenticator;
import org.miaixz.bus.auth.metric.radius.security.RadiusAuthenticator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Verifies one complete packet before product dispatch and emits a signed response or an RFC silent drop.
 */
public final class RadiusRequestHandler {

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
     * Runtime.
     */
    private final Runtime runtime;

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
     * @param runtime       runtime
     * @param access        access handler
     * @param accounting    accounting handler
     */
    public RadiusRequestHandler(final ServerConfig configuration, final Runtime runtime, final AccessHandler access,
            final AccountingHandler accounting) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS server configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("RADIUS runtime must not be null"));
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
     */
    static Optional<Datagram> response(
            final Datagram datagram,
            final RadiusPacket request,
            final int code,
            final java.util.List<org.miaixz.bus.auth.metric.radius.packet.RadiusAttribute> attributes,
            final byte[] secret) {
        final RadiusPacket unsigned = new RadiusPacket(code, request.identifier(),
                new byte[RadiusPacket.AUTHENTICATOR_BYTES], attributes);
        final RadiusPacket signed = new RadiusPacket(code, request.identifier(),
                RadiusAuthenticator.response(unsigned, request.authenticator(), secret), attributes);
        return Optional.of(new Datagram(datagram.peer(), RadiusPacketCodec.encode(signed)));
    }

    /**
     * Maps one product access decision.
     *
     * @param decision decision
     * @return RADIUS code
     */
    static int accessCode(final AccessDecision decision) {
        return switch (decision.type()) {
            case ACCEPT -> Normal._2;
            case REJECT -> Normal._3;
            case CHALLENGE -> 11;
        };
    }

    /**
     * Handles one authentication datagram.
     *
     * @param invocation context
     * @param datagram   datagram
     * @return optional response
     */
    public CompletionStage<Optional<Datagram>> authentication(final Invocation invocation, final Datagram datagram) {
        final RadiusPacket request;
        try {
            request = RadiusPacketCodec.decode(datagram.bytes());
            if (request.code() != ACCESS_REQUEST) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        } catch (final RuntimeException invalid) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return secret(invocation).thenCompose(secret -> {
            final boolean protectedPacket = request.attributes().stream()
                    .anyMatch(attribute -> attribute.type() == 79 || attribute.type() == MessageAuthenticator.TYPE);
            if (protectedPacket && !MessageAuthenticator.verify(request, secret)) {
                Arrays.fill(secret, (byte) Normal._0);
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return access.handle(invocation, new AccessRequest(request.attributes())).thenApply(
                    decision -> response(datagram, request, accessCode(decision), decision.attributes(), secret))
                    .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Handles one accounting datagram.
     *
     * @param invocation context
     * @param datagram   datagram
     * @return optional response
     */
    public CompletionStage<Optional<Datagram>> accounting(final Invocation invocation, final Datagram datagram) {
        final RadiusPacket request;
        try {
            request = RadiusPacketCodec.decode(datagram.bytes());
            if (request.code() != ACCOUNTING_REQUEST) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        } catch (final RuntimeException invalid) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return secret(invocation).thenCompose(secret -> {
            final byte[] expected = RadiusAuthenticator.accounting(request, secret);
            if (!RadiusAuthenticator.verify(expected, request.authenticator())) {
                Arrays.fill(secret, (byte) Normal._0);
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return accounting.handle(invocation, new AccountingRequest(request.attributes()))
                    .thenApply(ignored -> response(datagram, request, ACCOUNTING_RESPONSE, java.util.List.of(), secret))
                    .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Resolves and converts a shared secret.
     *
     * @param invocation context
     * @return secret bytes
     */
    CompletionStage<byte[]> secret(final Invocation invocation) {
        return runtime.secrets().resolve(invocation, "radius", configuration.secretKey()).thenApply(characters -> {
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
