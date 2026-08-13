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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.Failed;
import org.miaixz.bus.auth.Outcome.Failure;
import org.miaixz.bus.auth.Outcome.Kind;
import org.miaixz.bus.auth.Outcome.Success;
import org.miaixz.bus.auth.protocol.Mediator;
import org.miaixz.bus.auth.protocol.radius.RADIUS.AccessHandler;
import org.miaixz.bus.auth.protocol.radius.RADIUS.AccountingHandler;
import org.miaixz.bus.auth.protocol.radius.RADIUS.Server;
import org.miaixz.bus.auth.protocol.radius.RADIUS.ServerConfig;
import org.miaixz.bus.auth.protocol.radius.packet.RadiusPacketCodec;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.dns.DnsResolver;
import org.miaixz.bus.fabric.network.udp.UdpNetwork;
import org.miaixz.bus.fabric.network.udp.UdpNetwork.ServerBinding;

/**
 * Owns authentication and accounting runtime bindings with deterministic start rollback and close ordering.
 *
 * @author Kimi Liu
 */
public final class RadiusServer implements Server {

    /**
     * Configuration.
     */
    private final ServerConfig configuration;

    /**
     * Explicit Fabric UDP server network.
     */
    private final UdpNetwork network;

    /**
     * Runtime-bound DNS resolver.
     */
    private final DnsResolver resolver;

    /**
     * Verified request handler.
     */
    private final RadiusRequestHandler handler;

    /**
     * Authentication binding.
     */
    private ServerBinding authentication;

    /**
     * Accounting binding.
     */
    private ServerBinding accounting;

    /**
     * Shared close stage.
     */
    private CompletionStage<Void> closing;

    /**
     * Start-in-progress flag.
     */
    private boolean starting;

    /**
     * Externally observable bus-core lifecycle state.
     */
    private State lifecycle = State.NEW;

    /**
     * Creates one server.
     *
     * @param configuration configuration
     * @param network       Fabric UDP network
     * @param resolver      runtime-bound DNS resolver
     * @param secrets       shared-secret resolver
     * @param access        access handler
     * @param accounting    accounting handler
     * @throws ValidateException if any configuration or collaborator is {@code null}
     */
    public RadiusServer(final ServerConfig configuration, final UdpNetwork network, final DnsResolver resolver,
            final SecretResolver secrets, final AccessHandler access, final AccountingHandler accounting) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS server configuration must not be null"));
        this.network = Assert.notNull(network, () -> new ValidateException("RADIUS UDP network must not be null"));
        this.resolver = Assert.notNull(resolver, () -> new ValidateException("RADIUS DNS resolver must not be null"));
        this.handler = new RadiusRequestHandler(configuration, secrets, access, accounting);
    }

    /**
     * Maps one server failure.
     *
     * @param <T>   outcome type
     * @param cause cause
     * @return failed outcome
     */
    static <T> Outcome<T> failed(final Throwable cause) {
        return new Failed<>(new Failure(Kind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Dispatches one UDP message through the central protocol mediator and converts internal failure to RFC silence.
     *
     * @param invocation non-null server operation context
     * @param datagram   non-null received Fabric UDP message
     * @return stage containing a standard RADIUS response payload or an empty optional for silent drop
     */
    private CompletionStage<Optional<Payload>> dispatch(final Context invocation, final Message datagram) {
        return Mediator.execute(handler, invocation, datagram).thenApply(outcome -> {
            if (outcome instanceof Success<Optional<Payload>> success) {
                return success.value();
            }
            return Optional.empty();
        });
    }

    /**
     * Starts authentication and accounting bindings in deterministic order.
     *
     * @param invocation operation context
     * @return startup outcome
     * @throws ValidateException if the context is {@code null}
     */
    @Override
    public synchronized CompletionStage<Outcome<Void>> start(final Context invocation) {
        final Context context = Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
        if (starting || authentication != null || closing != null) {
            return CompletableFuture.completedFuture(
                    new Failed<>(new Failure(Kind.CONFLICT, ErrorCode._100807, false),
                            new IllegalStateException("RADIUS server is already started")));
        }
        starting = true;
        lifecycle = State.STARTING;
        return network.bind(
                configuration.authenticationBind(),
                configuration.authenticationPolicy().addressPolicy(),
                resolver,
                RadiusPacketCodec.MAXIMUM_PACKET_BYTES,
                configuration.maxInFlight(),
                datagram -> dispatch(context, datagram)).thenCompose(binding -> {
                    synchronized (this) {
                        authentication = binding;
                    }
                    return network.bind(
                            configuration.accountingBind(),
                            configuration.accountingPolicy().addressPolicy(),
                            resolver,
                            RadiusPacketCodec.MAXIMUM_PACKET_BYTES,
                            configuration.maxInFlight(),
                            datagram -> dispatch(context, datagram)).handle((second, failure) -> {
                                if (failure == null) {
                                    synchronized (this) {
                                        accounting = second;
                                        starting = false;
                                        lifecycle = State.RUNNING;
                                    }
                                    return CompletableFuture.<Outcome<Void>>completedFuture(new Success<>(null));
                                }
                                return binding.shutdown().handle((ignored, closeFailure) -> {
                                    synchronized (this) {
                                        authentication = null;
                                        starting = false;
                                        lifecycle = State.FAILED;
                                    }
                                    return RadiusServer.<Void>failed(failure);
                                });
                            }).thenCompose(stage -> stage);
                }).exceptionally(failure -> {
                    synchronized (this) {
                        starting = false;
                        lifecycle = State.FAILED;
                    }
                    return failed(failure);
                });
    }

    /**
     * Returns the authentication endpoint.
     *
     * @return authentication endpoint
     */
    @Override
    public synchronized Address authenticationEndpoint() {
        return authentication == null ? configuration.authenticationBind() : authentication.local();
    }

    /**
     * Returns the accounting endpoint.
     *
     * @return accounting endpoint
     */
    @Override
    public synchronized Address accountingEndpoint() {
        return accounting == null ? configuration.accountingBind() : accounting.local();
    }

    /**
     * Returns whether both server bindings are running.
     *
     * @return {@code true} when both bindings are running
     */
    @Override
    public synchronized boolean running() {
        return lifecycle == State.RUNNING && authentication != null && accounting != null && closing == null;
    }

    /**
     * Returns the exact externally observable server lifecycle state.
     *
     * @return current bus-core lifecycle state
     */
    @Override
    public synchronized State state() {
        return lifecycle;
    }

    /**
     * Closes accounting and authentication bindings in deterministic reverse order.
     *
     * @return asynchronous close completion
     */
    @Override
    public synchronized CompletionStage<Void> close() {
        if (closing != null) {
            return closing;
        }
        lifecycle = State.CLOSING;
        final ServerBinding second = accounting;
        final ServerBinding first = authentication;
        CompletionStage<Void> stage = second == null ? CompletableFuture.completedFuture(null) : second.shutdown();
        stage = stage.handle(
                (ignored, failure) -> first == null ? CompletableFuture.<Void>completedFuture(null) : first.shutdown())
                .thenCompose(value -> value);
        closing = stage.whenComplete((ignored, failure) -> {
            synchronized (this) {
                accounting = null;
                authentication = null;
                lifecycle = failure == null ? State.CLOSED : State.FAILED;
            }
        });
        return closing;
    }

}
