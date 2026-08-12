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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.StreamRead;
import org.miaixz.bus.auth.metric.AuthMetric.StreamSession;
import org.miaixz.bus.auth.metric.AuthMetric.TransportPolicy;
import org.miaixz.bus.auth.metric.LDAP.ResultCode;
import org.miaixz.bus.auth.metric.ldap.codec.BerFrameReader;
import org.miaixz.bus.auth.metric.ldap.codec.LdapMessageCodec;
import org.miaixz.bus.auth.metric.ldap.message.LdapMessage;
import org.miaixz.bus.auth.metric.ldap.message.LdapProtocolOp;
import org.miaixz.bus.auth.metric.ldap.message.LdapResult;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * One accepted LDAP server session. It serializes all decoded messages, isolates Bind state per connection, enforces
 * StartTLS plaintext pipeline exclusion, writes the successful extended response before asking Fabric to upgrade, and
 * closes its exclusive stream on EOF, Unbind, protocol failure, or handshake failure.
 *
 * @author Kimi Liu
 */
public final class LdapServerSession {

    /**
     * Raw stream frame reader.
     */
    private final BerFrameReader frames;
    /**
     * Accepted session invocation.
     */
    private final Invocation invocation;
    /**
     * Complete message codec.
     */
    private final LdapMessageCodec messages;
    /**
     * Maximum bytes requested per raw read.
     */
    private final int readBytes;
    /**
     * Product directory request mapper.
     */
    private final LdapRequestHandler requests;
    /**
     * Exclusive Bus stream.
     */
    private final StreamSession stream;
    /**
     * Closed LDAP server transport policy.
     */
    private final TransportPolicy transportPolicy;
    /**
     * Whether this connection has an accepted Bind identity.
     */
    private boolean bound;
    /**
     * Shared close stage.
     */
    private CompletionStage<Void> closeStage;
    /**
     * Current session state.
     */
    private State state;

    /**
     * Creates one accepted server session.
     *
     * @param invocation          accepted session invocation
     * @param stream              exclusive accepted stream
     * @param transportPolicy     server transport policy
     * @param requests            product directory request mapper
     * @param maximumMessageBytes positive LDAP message ceiling
     * @param tlsActive           whether LDAPS is already active
     */
    public LdapServerSession(final Invocation invocation, final StreamSession stream,
            final TransportPolicy transportPolicy, final LdapRequestHandler requests, final int maximumMessageBytes,
            final boolean tlsActive) {
        this.invocation = Assert
                .notNull(invocation, () -> new ValidateException("LDAP session invocation must not be null"));
        this.stream = Assert.notNull(stream, () -> new ValidateException("LDAP server stream must not be null"));
        this.transportPolicy = Assert
                .notNull(transportPolicy, () -> new ValidateException("LDAP server transport policy must not be null"));
        this.requests = Assert.notNull(requests, () -> new ValidateException("LDAP request handler must not be null"));
        Assert.isTrue(
                maximumMessageBytes > Normal._0,
                () -> new ValidateException("LDAP maximum message size must be positive"));
        this.readBytes = maximumMessageBytes;
        this.messages = new LdapMessageCodec(maximumMessageBytes);
        this.frames = new BerFrameReader(maximumMessageBytes);
        this.state = tlsActive ? State.TLS : State.PLAINTEXT;
    }

    /**
     * Creates the common protocol sequencing failure.
     *
     * @return protocol failure
     */
    private static ProtocolException failure() {
        return new ProtocolException(ErrorCode._100300);
    }

    /**
     * Runs the session read loop until it closes.
     *
     * @return session completion
     */
    public CompletionStage<Void> run() {
        return read().handle((ignored, failure) -> failure).thenCompose(failure -> close().thenCompose(ignored -> {
            if (failure == null) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.failedFuture(failure);
        }));
    }

    /**
     * Closes the exclusive stream idempotently.
     *
     * @return shared close completion
     */
    public synchronized CompletionStage<Void> close() {
        if (closeStage == null) {
            state = State.CLOSED;
            try {
                closeStage = stream.close();
            } catch (final Throwable failure) {
                closeStage = CompletableFuture.failedFuture(failure);
            }
        }
        return closeStage;
    }

    /**
     * Reads and processes the next raw stream chunk.
     *
     * @return loop completion
     */
    private CompletionStage<Void> read() {
        return stream.readChunk(readBytes).thenCompose(read -> {
            final List<byte[]> encoded = read.bytes().length == Normal._0 ? List.of() : frames.append(read.bytes());
            CompletionStage<Void> sequence = CompletableFuture.completedFuture(null);
            for (int index = Normal._0; index < encoded.size(); index++) {
                final LdapMessage message = messages.decode(encoded.get(index));
                final boolean hasPlaintextAfter = index + Normal._1 < encoded.size()
                        || frames.pendingBytes() > Normal._0;
                sequence = sequence.thenCompose(ignored -> handle(message, hasPlaintextAfter));
            }
            return sequence.thenCompose(ignored -> continueReading(read));
        });
    }

    /**
     * Continues reading or validates clean EOF.
     *
     * @param read completed stream read
     * @return loop or EOF completion
     */
    private CompletionStage<Void> continueReading(final StreamRead read) {
        if (state == State.CLOSED) {
            return CompletableFuture.completedFuture(null);
        }
        if (read.endOfStream()) {
            frames.finish();
            return CompletableFuture.completedFuture(null);
        }
        return read();
    }

    /**
     * Handles one decoded request in strict session order.
     *
     * @param message           decoded request
     * @param hasPlaintextAfter whether the raw chunk contains bytes after this message
     * @return operation completion
     */
    private CompletionStage<Void> handle(final LdapMessage message, final boolean hasPlaintextAfter) {
        if (state == State.CLOSED || state == State.UPGRADING) {
            return CompletableFuture.failedFuture(failure());
        }
        return switch (message.operation()) {
            case LdapProtocolOp.BindRequest ignored -> bind(message);
            case LdapProtocolOp.SearchRequest ignored -> authorized(message, requests::search);
            case LdapProtocolOp.CompareRequest ignored -> authorizedSingle(message, requests::compare);
            case LdapProtocolOp.AbandonRequest ignored -> CompletableFuture.completedFuture(null);
            case LdapProtocolOp.UnbindRequest ignored -> close();
            case LdapProtocolOp.StartTlsRequest ignored -> startTls(message, hasPlaintextAfter);
            default -> CompletableFuture.failedFuture(failure());
        };
    }

    /**
     * Handles Bind with required transport protection and per-session identity state.
     *
     * @param message Bind request
     * @return response write completion
     */
    private CompletionStage<Void> bind(final LdapMessage message) {
        if (transportPolicy.requireStartTls() && state != State.TLS) {
            bound = false;
            return write(
                    LdapMessage.of(
                            message.messageId(),
                            new LdapProtocolOp.BindResponse(
                                    LdapResult.of(ResultCode.CONFIDENTIALITY_REQUIRED, "", "TLS is required"))));
        }
        return requests.bind(invocation, message).thenCompose(response -> {
            bound = ((LdapProtocolOp.BindResponse) response.operation()).result().code() == ResultCode.SUCCESS;
            return write(response);
        });
    }

    /**
     * Executes an authenticated operation returning multiple messages.
     *
     * @param message   request message
     * @param operation directory operation
     * @return response write completion
     */
    private CompletionStage<Void> authorized(
            final LdapMessage message,
            final java.util.function.BiFunction<Invocation, LdapMessage, CompletionStage<List<LdapMessage>>> operation) {
        if (!bound) {
            return write(
                    LdapMessage.of(
                            message.messageId(),
                            new LdapProtocolOp.SearchDone(
                                    LdapResult.of(ResultCode.INSUFFICIENT_ACCESS_RIGHTS, "", "Bind is required"))));
        }
        return operation.apply(invocation, message).thenCompose(this::write);
    }

    /**
     * Executes an authenticated operation returning one message.
     *
     * @param message   request message
     * @param operation directory operation
     * @return response write completion
     */
    private CompletionStage<Void> authorizedSingle(
            final LdapMessage message,
            final java.util.function.BiFunction<Invocation, LdapMessage, CompletionStage<LdapMessage>> operation) {
        if (!bound) {
            return write(
                    LdapMessage.of(
                            message.messageId(),
                            new LdapProtocolOp.CompareResponse(
                                    LdapResult.of(ResultCode.INSUFFICIENT_ACCESS_RIGHTS, "", "Bind is required"))));
        }
        return operation.apply(invocation, message).thenCompose(this::write);
    }

    /**
     * Performs the server-side StartTLS response-before-upgrade transition.
     *
     * @param message           StartTLS request
     * @param hasPlaintextAfter whether pipelined plaintext follows
     * @return handshake completion
     */
    private CompletionStage<Void> startTls(final LdapMessage message, final boolean hasPlaintextAfter) {
        if (state != State.PLAINTEXT || hasPlaintextAfter) {
            return CompletableFuture.failedFuture(failure());
        }
        state = State.UPGRADING;
        bound = false;
        final LdapMessage response = LdapMessage.of(
                message.messageId(),
                new LdapProtocolOp.StartTlsResponse(LdapResult.of(ResultCode.SUCCESS, "", "")));
        return write(response).thenCompose(ignored -> stream.upgradeTls(transportPolicy))
                .thenRun(() -> state = State.TLS);
    }

    /**
     * Writes one encoded response.
     *
     * @param message response message
     * @return write completion
     */
    private CompletionStage<Void> write(final LdapMessage message) {
        return stream.write(messages.encode(message));
    }

    /**
     * Writes response messages in protocol order.
     *
     * @param responses ordered responses
     * @return final write completion
     */
    private CompletionStage<Void> write(final List<LdapMessage> responses) {
        CompletionStage<Void> result = CompletableFuture.completedFuture(null);
        for (final LdapMessage response : responses) {
            result = result.thenCompose(ignored -> write(response));
        }
        return result;
    }

    /**
     * Session lifecycle and transport state.
     */
    private enum State {

        /**
         * Plaintext session accepts StartTLS and permitted LDAP operations.
         */
        PLAINTEXT,

        /**
         * Successful response was written and TLS handshake is active.
         */
        UPGRADING,

        /**
         * TLS-protected session.
         */
        TLS,

        /**
         * Session is permanently closed.
         */
        CLOSED
    }

}
