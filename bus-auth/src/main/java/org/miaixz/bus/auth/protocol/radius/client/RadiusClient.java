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
package org.miaixz.bus.auth.protocol.radius.client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.Failed;
import org.miaixz.bus.auth.Outcome.Failure;
import org.miaixz.bus.auth.Outcome.Kind;
import org.miaixz.bus.auth.Outcome.Success;
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
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpSession;

/**
 * Exchanges authentication and accounting packets through the runtime datagram transport.
 *
 * @author Kimi Liu
 */
public final class RadiusClient implements Client {

    /**
     * Access-Request code.
     */
    private static final int ACCESS_REQUEST = Normal._1;

    /**
     * Access-Accept code.
     */
    private static final int ACCESS_ACCEPT = Normal._2;

    /**
     * Access-Reject code.
     */
    private static final int ACCESS_REJECT = Normal._3;

    /**
     * Accounting-Request code.
     */
    private static final int ACCOUNTING_REQUEST = Normal._4;

    /**
     * Accounting-Response code.
     */
    private static final int ACCOUNTING_RESPONSE = 5;

    /**
     * Access-Challenge code.
     */
    private static final int ACCESS_CHALLENGE = 11;

    /**
     * Client configuration.
     */
    private final ClientConfig configuration;

    /**
     * Explicit Fabric UDP network used for every datagram exchange.
     */
    private final UdpNetwork network;

    /**
     * Runtime-bound DNS resolver.
     */
    private final DnsResolver resolver;

    /**
     * Shared-secret resolver.
     */
    private final SecretResolver secrets;

    /**
     * Secure packet entropy source.
     */
    private final SecureRandom random;

    /**
     * Creates one client.
     *
     * @param configuration configuration
     * @param network       Fabric UDP network
     * @param resolver      runtime-bound DNS resolver
     * @param secrets       shared-secret resolver
     * @param random        secure packet entropy source
     * @throws ValidateException if any configuration or collaborator is {@code null}
     */
    public RadiusClient(final ClientConfig configuration, final UdpNetwork network, final DnsResolver resolver,
            final SecretResolver secrets, final SecureRandom random) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS client configuration must not be null"));
        this.network = Assert.notNull(network, () -> new ValidateException("RADIUS UDP network must not be null"));
        this.resolver = Assert.notNull(resolver, () -> new ValidateException("RADIUS DNS resolver must not be null"));
        this.secrets = Assert.notNull(secrets, () -> new ValidateException("RADIUS secret resolver must not be null"));
        this.random = Assert.notNull(random, () -> new ValidateException("RADIUS random source must not be null"));
    }

    /**
     * Maps one verified access code.
     *
     * @param code response code
     * @return decision
     * @throws ValidateException if the code is not an Access-Accept, Access-Reject, or Access-Challenge
     */
    static DecisionType decision(final int code) {
        return switch (code) {
            case ACCESS_ACCEPT -> DecisionType.ACCEPT;
            case ACCESS_REJECT -> DecisionType.REJECT;
            case ACCESS_CHALLENGE -> DecisionType.CHALLENGE;
            default -> throw new ValidateException("RADIUS access response code is invalid");
        };
    }

    /**
     * Maps one failure without exposing secrets or packet bytes.
     *
     * @param <T>     outcome value type
     * @param failure failure
     * @return failed outcome
     */
    static <T> Outcome<T> failed(final Throwable failure) {
        final Throwable cause = ExceptionKit.unwrap(failure);
        return new Failed<>(new Failure(Kind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Exchanges one authentication request.
     *
     * @param invocation operation context
     * @param request    access request
     * @return access outcome
     * @throws ValidateException if the request is {@code null}
     */
    @Override
    public CompletionStage<Outcome<AccessResponse>> authenticate(
            final Context invocation,
            final AccessRequest request) {
        Assert.notNull(request, () -> new ValidateException("RADIUS access request must not be null"));
        return secret(invocation).thenCompose(secret -> {
            final int identifier = identifier();
            final RadiusPacket unsigned = new RadiusPacket(ACCESS_REQUEST, identifier,
                    RadiusAuthenticator.access(random), request.attributes());
            final boolean eap = request.attributes().stream().anyMatch(
                    attribute -> attribute
                            .type() == org.miaixz.bus.auth.protocol.radius.eap.EapPacket.RADIUS_ATTRIBUTE);
            final RadiusPacket packet = eap ? MessageAuthenticator.apply(unsigned, secret) : unsigned;
            return exchange(
                    invocation,
                    configuration.authenticationTarget(),
                    configuration.authenticationPolicy(),
                    packet,
                    secret,
                    Normal._0)
                            .thenApply(
                                    response -> new Success<>(
                                            new AccessResponse(decision(response.code()), response.attributes())))
                            .<Outcome<AccessResponse>>thenApply(outcome -> outcome).exceptionally(RadiusClient::failed)
                            .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Exchanges one accounting request.
     *
     * @param invocation operation context
     * @param request    accounting request
     * @return accounting outcome
     * @throws ValidateException if the request is {@code null}
     */
    @Override
    public CompletionStage<Outcome<AccountingResponse>> account(
            final Context invocation,
            final AccountingRequest request) {
        Assert.notNull(request, () -> new ValidateException("RADIUS accounting request must not be null"));
        return secret(invocation).thenCompose(secret -> {
            final RadiusPacket unsigned = new RadiusPacket(ACCOUNTING_REQUEST, identifier(),
                    new byte[RadiusPacket.AUTHENTICATOR_BYTES], request.attributes());
            final RadiusPacket packet = new RadiusPacket(unsigned.code(), unsigned.identifier(),
                    RadiusAuthenticator.accounting(unsigned, secret), unsigned.attributes());
            return exchange(
                    invocation,
                    configuration.accountingTarget(),
                    configuration.accountingPolicy(),
                    packet,
                    secret,
                    Normal._0).thenApply(response -> {
                        Assert.isTrue(
                                response.code() == ACCOUNTING_RESPONSE,
                                () -> new ValidateException("RADIUS accounting response code is invalid"));
                        return new Success<>(new AccountingResponse(response.attributes()));
                    }).<Outcome<AccountingResponse>>thenApply(outcome -> outcome).exceptionally(RadiusClient::failed)
                            .whenComplete((ignored, failure) -> Arrays.fill(secret, (byte) Normal._0));
        });
    }

    /**
     * Exchanges the identical logical packet across bounded retries.
     *
     * @param invocation context
     * @param endpoint   target endpoint
     * @param policy     transport policy
     * @param request    request packet
     * @param secret     secret bytes
     * @param attempt    zero-based attempt
     * @return verified response
     * @throws IllegalArgumentException if a required Fabric operation returns {@code null}
     */
    CompletionStage<RadiusPacket> exchange(
            final Context invocation,
            final Address endpoint,
            final org.miaixz.bus.auth.bridge.TransportPolicy policy,
            final RadiusPacket request,
            final byte[] secret,
            final int attempt) {
        return network.connect(endpoint, policy.addressPolicy(), resolver).thenCompose(
                session -> CompletableFuture.supplyAsync(() -> exchange(session, request))
                        .handle((response, failure) -> {
                            session.close();
                            if (failure != null) {
                                if (attempt < configuration.retryCount()) {
                                    return exchange(invocation, endpoint, policy, request, secret, attempt + Normal._1);
                                }
                                return CompletableFuture.<RadiusPacket>failedFuture(failure);
                            }
                            try {
                                Assert.isTrue(
                                        endpoint.equals(response.address()),
                                        () -> new ValidateException("RADIUS response source endpoint is invalid"));
                                final RadiusPacket packet = RadiusPacketCodec
                                        .decode(response.payload().bytes(RadiusPacketCodec.MAXIMUM_PACKET_BYTES));
                                Assert.isTrue(
                                        packet.identifier() == request.identifier(),
                                        () -> new ValidateException("RADIUS response identifier is invalid"));
                                final byte[] expected = RadiusAuthenticator
                                        .response(packet, request.authenticator(), secret);
                                Assert.isTrue(
                                        RadiusAuthenticator.verify(expected, packet.authenticator()),
                                        () -> new ValidateException("RADIUS response authenticator is invalid"));
                                return CompletableFuture.completedFuture(packet);
                            } catch (final RuntimeException invalid) {
                                return CompletableFuture.<RadiusPacket>failedFuture(invalid);
                            }
                        }).thenCompose(stage -> stage));
    }

    /**
     * Sends and receives one complete RADIUS datagram through an owned Fabric UDP session.
     *
     * @param session connected session closed by the caller
     * @param request immutable request packet
     * @return received Fabric message
     */
    private Message exchange(final UdpSession session, final RadiusPacket request) {
        final byte[] bytes = RadiusPacketCodec.encode(request);
        final int written = session.sendDatagram(Payload.of(bytes)).await(configuration.requestTimeout());
        Assert.isTrue(written == bytes.length, () -> new ValidateException("RADIUS request send was incomplete"));
        return session.receive().await(configuration.requestTimeout());
    }

    /**
     * Resolves and converts one shared secret.
     *
     * @param invocation context
     * @return copied secret bytes
     * @throws IllegalArgumentException if the context, resolver stage, or secret value is {@code null}
     */
    CompletionStage<byte[]> secret(final Context invocation) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
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

    /**
     * Creates one identifier from runtime entropy.
     *
     * @return unsigned identifier
     */
    int identifier() {
        final byte[] value = new byte[Normal._1];
        random.nextBytes(value);
        return Byte.toUnsignedInt(value[Normal._0]);
    }

}
