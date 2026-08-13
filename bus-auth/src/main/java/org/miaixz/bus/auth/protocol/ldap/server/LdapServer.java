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

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.Failed;
import org.miaixz.bus.auth.Outcome.Failure;
import org.miaixz.bus.auth.Outcome.Kind;
import org.miaixz.bus.auth.protocol.ldap.LDAP;
import org.miaixz.bus.auth.protocol.ldap.LDAP.Directory;
import org.miaixz.bus.auth.protocol.ldap.LDAP.ServerConfig;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.protocol.socket.SocketServer;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;

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
    private final SocketServer.Builder serverBuilder;

    /**
     * Positive LDAP message-size ceiling in bytes.
     */
    private final int maximumMessageBytes;

    /**
     * Fabric TLS policy used for LDAP StartTLS sessions.
     */
    private final TlsPolicy tlsPolicy;

    /**
     * Active binding after successful start.
     */
    private SocketServer binding;

    /**
     * Shared binding-opening stage after the sole start attempt.
     */
    private CompletionStage<SocketServer> bindingStage;

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
     * @param configuration       immutable server configuration
     * @param serverBuilder       exclusive Fabric socket-server builder
     * @param tlsPolicy           Fabric TLS policy required for StartTLS, or {@code null} when StartTLS is disabled
     * @param directory           product directory port
     * @param maximumMessageBytes positive LDAP message-size ceiling in bytes
     */
    public LdapServer(final ServerConfig configuration, final SocketServer.Builder serverBuilder,
            final TlsPolicy tlsPolicy, final Directory directory, final int maximumMessageBytes) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("LDAP server configuration must not be null"));
        this.serverBuilder = Assert.notNull(
                serverBuilder,
                () -> new ValidateException("LDAP Fabric Socket server builder must not be null"));
        this.tlsPolicy = tlsPolicy;
        if (configuration.transportPolicy().requireStartTls() && tlsPolicy == null) {
            throw new ValidateException("LDAP StartTLS server requires a Fabric TLS policy");
        }
        Assert.isTrue(
                maximumMessageBytes > 0,
                () -> new ValidateException("LDAP maximum message size must be positive"));
        this.maximumMessageBytes = maximumMessageBytes;
        this.requests = new LdapRequestHandler(directory, configuration.maximumPageSize());
    }

    /**
     * Creates a stable remote server failure.
     *
     * @param cause original failure
     * @return failed outcome
     */
    private static Outcome<Void> failed(final Throwable cause) {
        return new Failed<>(new Failure(Kind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Binds the sole managed stream server.
     *
     * @param invocation operation context
     * @return start outcome
     */
    @Override
    public synchronized CompletionStage<Outcome<Void>> start(final Context invocation) {
        final Context context = Assert
                .notNull(invocation, () -> new ValidateException("LDAP invocation must not be null"));
        if (started || closeStage != null) {
            return CompletableFuture.completedFuture(
                    new Failed<>(new Failure(Kind.CONFLICT, ErrorCode._100807, false),
                            new IllegalStateException("LDAP server cannot be started more than once")));
        }
        started = true;
        running = true;
        try {
            binding = serverBuilder.bind(configuration.endpoint()).rawFrame().onOpen(stream -> accept(context, stream))
                    .start();
            bindingStage = CompletableFuture.completedFuture(binding);
        } catch (final Throwable failure) {
            running = false;
            return CompletableFuture.completedFuture(failed(failure));
        }
        return bindingStage.<Outcome<Void>>handle((opened, failure) -> {
            synchronized (this) {
                if (failure == null) {
                    binding = opened;
                    return org.miaixz.bus.auth.Outcome.completed();
                }
                running = false;
                return failed(ExceptionKit.unwrap(failure));
            }
        });
    }

    /**
     * Returns the effective bound endpoint after start, or configured endpoint before binding completes.
     *
     * @return local endpoint
     */
    @Override
    public synchronized Address localEndpoint() {
        return binding == null ? configuration.endpoint() : binding.address();
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
                    : bindingStage.handle((opened, failure) -> opened).thenApply(opened -> {
                        if (opened != null) {
                            opened.close();
                        }
                        return null;
                    });
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
    private void accept(final Context invocation, final SocketSession stream) {
        if (!running) {
            stream.close();
            return;
        }
        final boolean tlsActive = configuration.endpoint().protocol() == Protocol.LDAPS;
        final LdapServerSession session = new LdapServerSession(invocation, stream, configuration.transportPolicy(),
                tlsPolicy, requests, maximumMessageBytes, tlsActive);
        sessions.add(session);
        session.run().whenComplete((ignored, failure) -> sessions.remove(session));
    }

}
