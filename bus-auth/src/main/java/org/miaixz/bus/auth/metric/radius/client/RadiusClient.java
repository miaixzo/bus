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
package org.miaixz.bus.auth.metric.radius.client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.RADIUS.*;
import org.miaixz.bus.auth.metric.RADIUS.Client;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacket;
import org.miaixz.bus.auth.metric.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.auth.metric.radius.security.RadiusAuthenticator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Exchanges authentication and accounting packets through the runtime datagram transport.
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
     * Runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one client.
     *
     * @param configuration configuration
     * @param runtime       runtime
     */
    public RadiusClient(final ClientConfig configuration, final Runtime runtime) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS client configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("RADIUS runtime must not be null"));
    }

    /**
     * Maps one verified access code.
     *
     * @param code response code
     * @return decision
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
        final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        return new Failed<>(new Failure(FailureKind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Exchanges one authentication request.
     *
     * @param invocation operation context
     * @param request    access request
     * @return access outcome
     */
    @Override
    public CompletionStage<Outcome<AccessResponse>> authenticate(
            final Invocation invocation,
            final AccessRequest request) {
        Assert.notNull(request, () -> new ValidateException("RADIUS access request must not be null"));
        return secret(invocation).thenCompose(secret -> {
            final int identifier = identifier();
            final RadiusPacket packet = new RadiusPacket(ACCESS_REQUEST, identifier,
                    RadiusAuthenticator.access(runtime.random()), request.attributes());
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
     */
    @Override
    public CompletionStage<Outcome<AccountingResponse>> account(
            final Invocation invocation,
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
     */
    CompletionStage<RadiusPacket> exchange(
            final Invocation invocation,
            final org.miaixz.bus.auth.metric.AuthMetric.Endpoint endpoint,
            final org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy policy,
            final RadiusPacket request,
            final byte[] secret,
            final int attempt) {
        final Datagram datagram = new Datagram(endpoint, RadiusPacketCodec.encode(request));
        return runtime.transports().datagram().exchange(invocation, endpoint, datagram, policy)
                .handle((response, failure) -> {
                    if (failure != null) {
                        if (attempt < configuration.retryCount()) {
                            return exchange(invocation, endpoint, policy, request, secret, attempt + Normal._1);
                        }
                        return CompletableFuture.<RadiusPacket>failedFuture(failure);
                    }
                    try {
                        Assert.isTrue(
                                endpoint.equals(response.peer()),
                                () -> new ValidateException("RADIUS response source endpoint is invalid"));
                        final RadiusPacket packet = RadiusPacketCodec.decode(response.bytes());
                        Assert.isTrue(
                                packet.identifier() == request.identifier(),
                                () -> new ValidateException("RADIUS response identifier is invalid"));
                        final byte[] expected = RadiusAuthenticator.response(packet, request.authenticator(), secret);
                        Assert.isTrue(
                                RadiusAuthenticator.verify(expected, packet.authenticator()),
                                () -> new ValidateException("RADIUS response authenticator is invalid"));
                        return CompletableFuture.completedFuture(packet);
                    } catch (final RuntimeException invalid) {
                        return CompletableFuture.<RadiusPacket>failedFuture(invalid);
                    }
                }).thenCompose(stage -> stage);
    }

    /**
     * Resolves and converts one shared secret.
     *
     * @param invocation context
     * @return copied secret bytes
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

    /**
     * Creates one identifier from runtime entropy.
     *
     * @return unsigned identifier
     */
    int identifier() {
        final byte[] value = runtime.random().nextBytes(Normal._1);
        Assert.isTrue(
                value.length == Normal._1,
                () -> new ValidateException("RADIUS random identifier length is invalid"));
        return Byte.toUnsignedInt(value[Normal._0]);
    }

}
