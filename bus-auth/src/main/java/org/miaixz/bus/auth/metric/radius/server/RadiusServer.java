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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.RADIUS.AccessHandler;
import org.miaixz.bus.auth.metric.RADIUS.AccountingHandler;
import org.miaixz.bus.auth.metric.RADIUS.Server;
import org.miaixz.bus.auth.metric.RADIUS.ServerConfig;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Owns authentication and accounting runtime bindings with deterministic start rollback and close ordering.
 */
public final class RadiusServer implements Server {

    /**
     * Configuration.
     */
    private final ServerConfig configuration;

    /**
     * Runtime.
     */
    private final Runtime runtime;

    /**
     * Verified request handler.
     */
    private final RadiusRequestHandler handler;

    /**
     * Authentication binding.
     */
    private DatagramServerBinding authentication;

    /**
     * Accounting binding.
     */
    private DatagramServerBinding accounting;

    /**
     * Shared close stage.
     */
    private CompletionStage<Void> closing;

    /**
     * Start-in-progress flag.
     */
    private boolean starting;

    /**
     * Creates one server.
     *
     * @param configuration configuration
     * @param runtime       runtime
     * @param access        access handler
     * @param accounting    accounting handler
     */
    public RadiusServer(final ServerConfig configuration, final Runtime runtime, final AccessHandler access,
            final AccountingHandler accounting) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("RADIUS server configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("RADIUS runtime must not be null"));
        this.handler = new RadiusRequestHandler(configuration, runtime, access, accounting);
    }

    /**
     * Maps one server failure.
     *
     * @param <T>   outcome type
     * @param cause cause
     * @return failed outcome
     */
    static <T> Outcome<T> failed(final Throwable cause) {
        return new Failed<>(new Failure(FailureKind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Starts authentication and accounting bindings in deterministic order.
     *
     * @param invocation operation context
     * @return startup outcome
     */
    @Override
    public synchronized CompletionStage<Outcome<Void>> start(final Invocation invocation) {
        if (starting || authentication != null || closing != null) {
            return CompletableFuture.completedFuture(
                    new Failed<>(new Failure(FailureKind.CONFLICT, ErrorCode._100807, false),
                            new IllegalStateException("RADIUS server is already started")));
        }
        starting = true;
        return runtime.transports().datagramServer()
                .bind(
                        invocation,
                        configuration.authenticationBind(),
                        configuration.authenticationPolicy(),
                        handler::authentication)
                .thenCompose(binding -> {
                    synchronized (this) {
                        authentication = binding;
                    }
                    return runtime.transports().datagramServer()
                            .bind(
                                    invocation,
                                    configuration.accountingBind(),
                                    configuration.accountingPolicy(),
                                    handler::accounting)
                            .handle((second, failure) -> {
                                if (failure == null) {
                                    synchronized (this) {
                                        accounting = second;
                                        starting = false;
                                    }
                                    return CompletableFuture.<Outcome<Void>>completedFuture(new Success<>(null));
                                }
                                return binding.close().handle((ignored, closeFailure) -> {
                                    synchronized (this) {
                                        authentication = null;
                                        starting = false;
                                    }
                                    return RadiusServer.<Void>failed(failure);
                                });
                            }).thenCompose(stage -> stage);
                }).exceptionally(failure -> {
                    synchronized (this) {
                        starting = false;
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
    public synchronized Endpoint authenticationEndpoint() {
        return authentication == null ? configuration.authenticationBind() : authentication.localEndpoint();
    }

    /**
     * Returns the accounting endpoint.
     *
     * @return accounting endpoint
     */
    @Override
    public synchronized Endpoint accountingEndpoint() {
        return accounting == null ? configuration.accountingBind() : accounting.localEndpoint();
    }

    /**
     * Returns whether both server bindings are running.
     *
     * @return {@code true} when both bindings are running
     */
    @Override
    public synchronized boolean running() {
        return authentication != null && accounting != null && closing == null;
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
        final DatagramServerBinding second = accounting;
        final DatagramServerBinding first = authentication;
        CompletionStage<Void> stage = second == null ? CompletableFuture.completedFuture(null) : second.close();
        stage = stage.handle(
                (ignored, failure) -> first == null ? CompletableFuture.<Void>completedFuture(null) : first.close())
                .thenCompose(value -> value);
        closing = stage.whenComplete((ignored, failure) -> {
            synchronized (this) {
                accounting = null;
                authentication = null;
            }
        });
        return closing;
    }

}
