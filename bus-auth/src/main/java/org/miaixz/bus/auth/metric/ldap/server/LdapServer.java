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
package org.miaixz.bus.auth.metric.ldap.server;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.LDAP;
import org.miaixz.bus.auth.metric.LDAP.Directory;
import org.miaixz.bus.auth.metric.LDAP.ServerConfig;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Sole {@link LDAP.Server} implementation. It owns one Bus stream-server binding, creates one isolated protocol state
 * machine per accepted stream, rejects duplicate starts, and closes accepted sessions before completing the binding
 * drain. No socket, TLS engine, executor, or thread is created by this class.
 *
 * @author Kimi Liu
 */
public final class LdapServer implements LDAP.Server {

    /**
     * Active accepted session set.
     */
    private final Set<LdapServerSession> sessions = ConcurrentHashMap.newKeySet();

    /**
     * Immutable server configuration.
     */
    private final ServerConfig configuration;

    /**
     * Stateless directory request mapper.
     */
    private final LdapRequestHandler requests;

    /**
     * Product-supplied runtime.
     */
    private final Runtime runtime;

    /**
     * Active binding after successful start.
     */
    private StreamServerBinding binding;

    /**
     * Shared binding-opening stage after the sole start attempt.
     */
    private CompletionStage<StreamServerBinding> bindingStage;

    /**
     * Shared close stage.
     */
    private CompletionStage<Void> closeStage;

    /**
     * Whether a start attempt has been made.
     */
    private boolean started;

    /**
     * Whether new accepted sessions may run.
     */
    private volatile boolean running;

    /**
     * Creates one managed LDAP server.
     *
     * @param configuration immutable server configuration
     * @param runtime       authentication runtime
     * @param directory     product directory port
     */
    public LdapServer(final ServerConfig configuration, final Runtime runtime, final Directory directory) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("LDAP server configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("LDAP runtime must not be null"));
        this.requests = new LdapRequestHandler(directory, configuration.maximumPageSize());
    }

    /**
     * Creates a stable remote server failure.
     *
     * @param cause original failure
     * @return failed outcome
     */
    private static Outcome<Void> failed(final Throwable cause) {
        return new Failed<>(new Failure(FailureKind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Unwraps an asynchronous completion wrapper.
     *
     * @param failure source failure
     * @return original cause
     */
    private static Throwable unwrap(final Throwable failure) {
        return failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
    }

    /**
     * Binds the sole managed stream server.
     *
     * @param invocation operation context
     * @return start outcome
     */
    @Override
    public synchronized CompletionStage<Outcome<Void>> start(final Invocation invocation) {
        final Invocation context = Assert
                .notNull(invocation, () -> new ValidateException("LDAP invocation must not be null"));
        if (started || closeStage != null) {
            return CompletableFuture.completedFuture(
                    new Failed<>(new Failure(FailureKind.CONFLICT, ErrorCode._100807, false),
                            new IllegalStateException("LDAP server cannot be started more than once")));
        }
        started = true;
        running = true;
        try {
            bindingStage = runtime.transports().streamServer()
                    .bind(context, configuration.endpoint(), configuration.transportPolicy(), this::accept);
        } catch (final Throwable failure) {
            running = false;
            return CompletableFuture.completedFuture(failed(failure));
        }
        return bindingStage.<Outcome<Void>>handle((opened, failure) -> {
            synchronized (this) {
                if (failure == null) {
                    binding = opened;
                    return org.miaixz.bus.auth.metric.AuthMetric.completed();
                }
                running = false;
                return failed(unwrap(failure));
            }
        });
    }

    /**
     * Returns the effective bound endpoint after start, or configured endpoint before binding completes.
     *
     * @return local endpoint
     */
    @Override
    public synchronized Endpoint localEndpoint() {
        return binding == null ? configuration.endpoint() : binding.localEndpoint();
    }

    /**
     * Reports whether new sessions are accepted.
     *
     * @return running state
     */
    @Override
    public boolean running() {
        return running;
    }

    /**
     * Closes active sessions and then completes the Bus binding drain idempotently.
     *
     * @return shared close completion
     */
    @Override
    public synchronized CompletionStage<Void> close() {
        if (closeStage == null) {
            running = false;
            final CompletableFuture<?>[] active = sessions.stream().map(LdapServerSession::close)
                    .map(CompletionStage::toCompletableFuture).toArray(CompletableFuture[]::new);
            final CompletionStage<Void> sessionClose = CompletableFuture.allOf(active);
            final CompletionStage<Void> bindingClose = bindingStage == null ? CompletableFuture.completedFuture(null)
                    : bindingStage.handle((opened, failure) -> opened).thenCompose(
                            opened -> opened == null ? CompletableFuture.completedFuture(null) : opened.close());
            closeStage = CompletableFuture
                    .allOf(sessionClose.toCompletableFuture(), bindingClose.toCompletableFuture());
        }
        return closeStage;
    }

    /**
     * Creates and runs one isolated accepted protocol session.
     *
     * @param invocation accepted session context
     * @param stream     accepted exclusive stream
     * @return session completion
     */
    private CompletionStage<Void> accept(final Invocation invocation, final StreamSession stream) {
        if (!running) {
            return stream.close();
        }
        final boolean tlsActive = configuration.endpoint().protocol() == Protocol.LDAPS;
        final LdapServerSession session = new LdapServerSession(invocation, stream, configuration.transportPolicy(),
                requests, runtime.limits().maxLdapMessageBytes(), tlsActive);
        sessions.add(session);
        return session.run().whenComplete((ignored, failure) -> sessions.remove(session));
    }

}
