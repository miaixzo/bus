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
package org.miaixz.bus.auth.metric.ldap.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

import org.miaixz.bus.auth.metric.AuthMetric.StreamRead;
import org.miaixz.bus.auth.metric.AuthMetric.StreamSession;
import org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy;
import org.miaixz.bus.auth.metric.LDAP.ResultCode;
import org.miaixz.bus.auth.metric.ldap.codec.BerFrameReader;
import org.miaixz.bus.auth.metric.ldap.codec.LdapMessageCodec;
import org.miaixz.bus.auth.metric.ldap.message.LdapMessage;
import org.miaixz.bus.auth.metric.ldap.message.LdapProtocolOp;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Exclusive asynchronous LDAP client stream state machine. Writes and reads remain serialized at the Bus stream port,
 * while response messages are routed concurrently by message identifier. Bind credentials cannot cross required
 * plaintext transport, abandoned responses are drained, and StartTLS permits no pipelined plaintext before upgrade.
 *
 * @author Kimi Liu
 */
public final class LdapClientSession {

    /**
     * Raw BER frame reader.
     */
    private final BerFrameReader frames;
    /**
     * Abandoned exchanges retained only to drain racing terminal responses.
     */
    private final Map<Integer, Pending> abandoned = new LinkedHashMap<>();
    /**
     * Complete LDAP message codec.
     */
    private final LdapMessageCodec messages;
    /**
     * Outstanding exchanges by message identifier.
     */
    private final Map<Integer, Pending> pending = new LinkedHashMap<>();
    /**
     * Maximum bytes requested from one stream read.
     */
    private final int readBytes;
    /**
     * Exclusive Bus stream session.
     */
    private final StreamSession stream;
    /**
     * Policy used for StartTLS handshakes.
     */
    private final TransportPolicy transportPolicy;
    /**
     * Shared idempotent close completion.
     */
    private CompletionStage<Void> closeStage;
    /**
     * Whether one asynchronous stream read is active.
     */
    private boolean reading;
    /**
     * Current transport state guarded by this instance monitor.
     */
    private State state;
    /**
     * Serialized write tail guarded by this instance monitor.
     */
    private CompletionStage<Void> writeTail = CompletableFuture.completedFuture(null);

    /**
     * Creates one session over an already opened Bus stream.
     *
     * @param stream              exclusive stream
     * @param transportPolicy     closed LDAP transport policy
     * @param maximumMessageBytes positive LDAP message ceiling
     * @param tlsActive           whether the opened stream already completed LDAPS
     */
    public LdapClientSession(final StreamSession stream, final TransportPolicy transportPolicy,
            final int maximumMessageBytes, final boolean tlsActive) {
        this.stream = Assert.notNull(stream, () -> new ValidateException("LDAP stream session must not be null"));
        this.transportPolicy = Assert
                .notNull(transportPolicy, () -> new ValidateException("LDAP transport policy must not be null"));
        Assert.isTrue(
                maximumMessageBytes > Normal._0,
                () -> new ValidateException("LDAP maximum message size must be positive"));
        this.readBytes = maximumMessageBytes;
        this.messages = new LdapMessageCodec(maximumMessageBytes);
        this.frames = new BerFrameReader(maximumMessageBytes);
        this.state = tlsActive ? State.TLS : State.PLAINTEXT;
    }

    /**
     * Requires an exact response count.
     *
     * @param responses responses
     * @param expected  expected count
     */
    private static void requireCount(final List<LdapMessage> responses, final int expected) {
        if (responses.size() != expected) {
            reject();
        }
    }

    /**
     * Throws the common LDAP protocol failure as an asynchronous completion failure.
     */
    private static void reject() {
        throw new CompletionException(failure());
    }

    /**
     * Creates the common LDAP protocol failure.
     *
     * @return protocol failure
     */
    private static ProtocolException failure() {
        return new ProtocolException(ErrorCode._100300);
    }

    /**
     * Executes one simple Bind exchange.
     *
     * @param messageId positive message identifier
     * @param request   bind request operation
     * @return terminal Bind response
     */
    public CompletionStage<LdapProtocolOp.BindResponse> bind(
            final int messageId,
            final LdapProtocolOp.BindRequest request) {
        synchronized (this) {
            requireApplicationTransport();
        }
        return exchange(
                LdapMessage.of(messageId, request),
                operation -> operation instanceof LdapProtocolOp.BindResponse).thenApply(responses -> {
                    requireCount(responses, Normal._1);
                    return (LdapProtocolOp.BindResponse) responses.getFirst().operation();
                });
    }

    /**
     * Executes one Search exchange through its terminal SearchResultDone response.
     *
     * @param message request message including optional page control
     * @return ordered Search entry and completion messages
     */
    public CompletionStage<List<LdapMessage>> search(final LdapMessage message) {
        synchronized (this) {
            requireApplicationTransport();
        }
        return exchange(message, operation -> operation instanceof LdapProtocolOp.SearchDone).thenApply(responses -> {
            for (int index = Normal._0; index < responses.size() - Normal._1; index++) {
                if (!(responses.get(index).operation() instanceof LdapProtocolOp.SearchEntry)) {
                    reject();
                }
            }
            return responses;
        });
    }

    /**
     * Executes one Compare exchange.
     *
     * @param messageId positive message identifier
     * @param request   compare request operation
     * @return terminal Compare response
     */
    public CompletionStage<LdapProtocolOp.CompareResponse> compare(
            final int messageId,
            final LdapProtocolOp.CompareRequest request) {
        synchronized (this) {
            requireApplicationTransport();
        }
        return exchange(
                LdapMessage.of(messageId, request),
                operation -> operation instanceof LdapProtocolOp.CompareResponse).thenApply(responses -> {
                    requireCount(responses, Normal._1);
                    return (LdapProtocolOp.CompareResponse) responses.getFirst().operation();
                });
    }

    /**
     * Sends one Abandon request and drains any subsequent target responses without closing the session.
     *
     * @param messageId          new request message identifier
     * @param abandonedMessageId outstanding operation identifier
     * @return write completion
     */
    public CompletionStage<Void> abandon(final int messageId, final int abandonedMessageId) {
        synchronized (this) {
            requireApplicationTransport();
            final Pending target = pending.get(abandonedMessageId);
            if (target != null && !target.abandoned) {
                target.abandoned = true;
                pending.remove(abandonedMessageId);
                abandoned.put(abandonedMessageId, target);
                target.result.completeExceptionally(new CancellationException("LDAP operation was abandoned"));
            }
        }
        return write(messages.encode(LdapMessage.of(messageId, new LdapProtocolOp.AbandonRequest(abandonedMessageId))));
    }

    /**
     * Performs the complete StartTLS request, response, pipeline check, and Fabric upgrade transition.
     *
     * @param messageId positive message identifier
     * @return TLS upgrade completion
     */
    public CompletionStage<Void> startTls(final int messageId) {
        synchronized (this) {
            require(State.PLAINTEXT);
            if (!pending.isEmpty() || !abandoned.isEmpty()) {
                return CompletableFuture
                        .failedFuture(new IllegalStateException("LDAP StartTLS requires no outstanding operations"));
            }
            state = State.UPGRADING;
        }
        final CompletionStage<Void> result = exchange(
                LdapMessage.of(messageId, new LdapProtocolOp.StartTlsRequest()),
                operation -> operation instanceof LdapProtocolOp.StartTlsResponse).thenCompose(responses -> {
                    requireCount(responses, Normal._1);
                    final LdapProtocolOp.StartTlsResponse response = (LdapProtocolOp.StartTlsResponse) responses
                            .getFirst().operation();
                    synchronized (this) {
                        if (response.result().code() != ResultCode.SUCCESS || frames.pendingBytes() != Normal._0
                                || !pending.isEmpty()) {
                            reject();
                        }
                    }
                    return stream.upgradeTls(transportPolicy);
                }).thenRun(() -> {
                    synchronized (this) {
                        require(State.UPGRADING);
                        state = State.TLS;
                    }
                });
        result.whenComplete((ignored, failure) -> {
            if (failure != null) {
                close();
            }
        });
        return result;
    }

    /**
     * Sends Unbind after outstanding exchanges complete and closes the stream exactly once.
     *
     * @param messageId positive message identifier
     * @return close completion after write
     */
    public CompletionStage<Void> unbind(final int messageId) {
        synchronized (this) {
            requireOperational();
            if (!pending.isEmpty()) {
                return CompletableFuture
                        .failedFuture(new IllegalStateException("LDAP Unbind requires no outstanding operations"));
            }
        }
        return write(messages.encode(LdapMessage.of(messageId, new LdapProtocolOp.UnbindRequest())))
                .thenCompose(ignored -> close());
    }

    /**
     * Reports whether TLS protects this session.
     *
     * @return whether the transport state is TLS
     */
    public synchronized boolean tlsActive() {
        return state == State.TLS;
    }

    /**
     * Closes the stream, fails outstanding exchanges, and returns one shared completion.
     *
     * @return shared close completion
     */
    public synchronized CompletionStage<Void> close() {
        if (closeStage == null) {
            state = State.CLOSED;
            final ProtocolException failure = failure();
            pending.values().forEach(value -> value.result.completeExceptionally(failure));
            pending.clear();
            abandoned.clear();
            try {
                closeStage = stream.close();
            } catch (final Throwable cause) {
                closeStage = CompletableFuture.failedFuture(cause);
            }
        }
        return closeStage;
    }

    /**
     * Registers, writes, and asynchronously routes one response exchange.
     *
     * @param request  request message
     * @param terminal terminal operation predicate
     * @return ordered response messages
     */
    private CompletionStage<List<LdapMessage>> exchange(
            final LdapMessage request,
            final Predicate<LdapProtocolOp> terminal) {
        final Pending current = new Pending(terminal);
        synchronized (this) {
            requireOperationalOrUpgrading();
            if (pending.putIfAbsent(request.messageId(), current) != null) {
                return CompletableFuture
                        .failedFuture(new IllegalStateException("LDAP message identifier is already outstanding"));
            }
            pump();
        }
        write(messages.encode(request)).whenComplete((ignored, failure) -> {
            if (failure != null) {
                fail(request.messageId(), failure);
            }
        });
        return current.result;
    }

    /**
     * Serializes one outbound stream write.
     *
     * @param encoded copied message bytes
     * @return write completion
     */
    private synchronized CompletionStage<Void> write(final byte[] encoded) {
        if (state == State.CLOSED) {
            return CompletableFuture.failedFuture(new IllegalStateException("LDAP client session is closed"));
        }
        final CompletionStage<Void> result = writeTail.thenCompose(ignored -> stream.write(encoded));
        writeTail = result.handle((ignored, failure) -> null);
        result.whenComplete((ignored, failure) -> {
            if (failure != null) {
                close();
            }
        });
        return result;
    }

    /**
     * Starts the sole asynchronous read when outstanding exchanges require it.
     */
    private synchronized void pump() {
        if (reading || pending.isEmpty() && abandoned.isEmpty() || state == State.CLOSED) {
            return;
        }
        reading = true;
        final CompletionStage<StreamRead> read;
        try {
            read = stream.readChunk(readBytes);
        } catch (final Throwable failure) {
            received(null, failure);
            return;
        }
        read.whenComplete(this::received);
    }

    /**
     * Parses one raw read and dispatches every response by message identifier.
     *
     * @param read        completed raw read
     * @param readFailure optional read failure
     */
    private void received(final StreamRead read, final Throwable readFailure) {
        synchronized (this) {
            reading = false;
            if (readFailure != null) {
                failAll(readFailure);
                return;
            }
            try {
                final List<byte[]> encoded = read.bytes().length == Normal._0 ? List.of() : frames.append(read.bytes());
                for (int index = Normal._0; index < encoded.size(); index++) {
                    final LdapMessage response = messages.decode(encoded.get(index));
                    Pending current = pending.get(response.messageId());
                    final boolean draining = current == null;
                    if (draining) {
                        current = abandoned.get(response.messageId());
                    }
                    if (current == null) {
                        throw failure();
                    }
                    if (!draining) {
                        current.responses.add(response);
                    }
                    if (current.terminal.test(response.operation())) {
                        if (state == State.UPGRADING
                                && (index + Normal._1 < encoded.size() || frames.pendingBytes() > Normal._0)) {
                            throw failure();
                        }
                        pending.remove(response.messageId());
                        abandoned.remove(response.messageId());
                        if (!draining) {
                            current.result.complete(List.copyOf(current.responses));
                        }
                    }
                }
                if (read.endOfStream()) {
                    frames.finish();
                    if (!pending.isEmpty()) {
                        throw failure();
                    }
                    close();
                    return;
                }
                if (state != State.UPGRADING) {
                    pump();
                }
            } catch (final Throwable failure) {
                failAll(failure);
            }
        }
    }

    /**
     * Fails one outstanding exchange and closes the session.
     *
     * @param messageId outstanding identifier
     * @param failure   original failure
     */
    private synchronized void fail(final int messageId, final Throwable failure) {
        final Pending current = pending.remove(messageId);
        if (current != null) {
            current.result.completeExceptionally(failure);
        }
        close();
    }

    /**
     * Fails every outstanding exchange and closes the session.
     *
     * @param failure original failure
     */
    private synchronized void failAll(final Throwable failure) {
        pending.values().forEach(value -> value.result.completeExceptionally(failure));
        pending.clear();
        abandoned.clear();
        close();
    }

    /**
     * Requires application traffic to satisfy the configured StartTLS policy.
     */
    private void requireApplicationTransport() {
        if (transportPolicy.requireStartTls() && state != State.TLS) {
            throw new IllegalStateException("LDAP operation requires an active TLS transport");
        }
        requireOperational();
    }

    /**
     * Requires an operational plaintext or TLS state.
     */
    private void requireOperational() {
        if (state == State.CLOSED || state == State.UPGRADING) {
            throw new IllegalStateException("LDAP client session state does not permit this operation");
        }
    }

    /**
     * Requires an operational state or the internal StartTLS response state.
     */
    private void requireOperationalOrUpgrading() {
        if (state == State.CLOSED) {
            throw new IllegalStateException("LDAP client session is closed");
        }
    }

    /**
     * Requires one exact transport state.
     *
     * @param expected expected state
     */
    private void require(final State expected) {
        if (state != expected) {
            throw new IllegalStateException("LDAP client session state does not permit this operation");
        }
    }

    /**
     * Session transport state.
     */
    private enum State {

        /**
         * Plaintext LDAP transport.
         */
        PLAINTEXT,

        /**
         * StartTLS response accepted and handshake in progress.
         */
        UPGRADING,

        /**
         * TLS-protected LDAP transport.
         */
        TLS,

        /**
         * Session no longer accepts operations.
         */
        CLOSED
    }

    /**
     * One outstanding routed response exchange.
     */
    private static final class Pending {

        /**
         * Ordered response accumulator.
         */
        private final ArrayList<LdapMessage> responses = new ArrayList<>();

        /**
         * Exchange result.
         */
        private final CompletableFuture<List<LdapMessage>> result = new CompletableFuture<>();

        /**
         * Terminal operation predicate.
         */
        private final Predicate<LdapProtocolOp> terminal;

        /**
         * Whether the caller abandoned this exchange.
         */
        private boolean abandoned;

        /**
         * Creates one outstanding exchange.
         *
         * @param terminal terminal operation predicate
         */
        private Pending(final Predicate<LdapProtocolOp> terminal) {
            this.terminal = terminal;
        }
    }

}
