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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Internal immutable HTTP stage chain with an index cursor.
 *
 * <p>
 * Protocol users extend HTTP behavior through builder {@code filter(...)}, {@code guard(...)}, and {@code observe(...)}
 * hooks. This chain coordinates built-in runtime stages only.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpChain {

    /**
     * Most recently compiled immutable stage list identity.
     */
    private static volatile CompiledStages compiledStages;

    /**
     * Ordered stages.
     */
    private final HttpStage[] stages;

    /**
     * Current stage index.
     */
    private int index;

    /**
     * Current leased connection.
     */
    private ConnectionLease lease;

    /**
     * Current network connection.
     */
    private Connection connection;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Creates a chain.
     *
     * @param stages       immutable ordered stage array shared by chain cursors
     * @param index        current index
     * @param lease        leased connection
     * @param connection   network connection
     * @param cancellation cancellation scope shared by all stages
     */
    private HttpChain(final HttpStage[] stages, final int index, final ConnectionLease lease,
            final Connection connection, final Cancellation cancellation) {
        this.stages = stages;
        this.index = validateIndex(index, this.stages.length);
        this.lease = lease;
        this.connection = connection;
        this.cancellation = require(cancellation, "Cancellation");
    }

    /**
     * Creates a chain at index zero.
     *
     * @param stages ordered built-in stages to snapshot
     * @return new one-shot chain cursor positioned before the first stage
     */
    public static HttpChain create(final List<HttpStage> stages) {
        return create(stages, Cancellation.create());
    }

    /**
     * Creates a chain at index zero with a shared cancellation scope.
     *
     * @param stages       ordered built-in stages to snapshot
     * @param cancellation cancellation scope
     * @return new one-shot chain cursor sharing the cancellation scope
     */
    public static HttpChain create(final List<HttpStage> stages, final Cancellation cancellation) {
        return new HttpChain(snapshot(stages), Normal._0, null, null, cancellation);
    }

    /**
     * Proceeds to the next stage.
     *
     * @param request immutable request passed to the current stage
     * @return response
     */
    public HttpResponse proceed(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        if (index >= stages.length) {
            throw new StatefulException("HTTP chain is exhausted");
        }
        cancellation.throwIfCancelled();
        final HttpStage stage = stages[index++];
        return stage.execute(current, this);
    }

    /**
     * Returns a new chain with an appended stage.
     *
     * @param stage built-in stage appended after the current snapshot
     * @return new chain sharing cursor context with the extended stage array
     */
    public HttpChain add(final HttpStage stage) {
        validateStage(stage);
        final HttpStage[] copy = Arrays.copyOf(stages, stages.length + Normal._1);
        copy[stages.length] = stage;
        return new HttpChain(copy, index, lease, connection, cancellation);
    }

    /**
     * Returns the current stage index.
     *
     * @return zero-based index of the next stage to execute
     */
    public int index() {
        return index;
    }

    /**
     * Returns stage snapshot.
     *
     * @return immutable ordered stage snapshot
     */
    public List<HttpStage> stages() {
        return List.of(stages);
    }

    /**
     * Creates a fresh one-shot cursor at the same downstream position for an explicit retry or follow-up attempt.
     *
     * @param replayIndex zero-based downstream stage index for the new cursor
     * @return fresh replay cursor
     */
    public HttpChain replayFrom(final int replayIndex) {
        return new HttpChain(stages, replayIndex, lease, connection, cancellation);
    }

    /**
     * Returns a chain that carries a leased connection for downstream stages.
     *
     * @param lease      connection lease carried to downstream stages
     * @param connection leased physical connection carried to downstream stages
     * @return contextual chain
     */
    HttpChain withConnection(final ConnectionLease lease, final Connection connection) {
        final ConnectionLease currentLease = require(lease, "Connection lease");
        final Connection currentConnection = require(connection, "Network connection");
        this.lease = currentLease;
        this.connection = currentConnection;
        return this;
    }

    /**
     * Returns the current connection lease.
     *
     * @return lease or null
     */
    ConnectionLease lease() {
        return lease;
    }

    /**
     * Returns the current network connection.
     *
     * @return connection or null
     */
    Connection connection() {
        return connection;
    }

    /**
     * Returns the shared cancellation scope.
     *
     * @return cancellation scope
     */
    Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Validates stage list.
     *
     * @param stages source stage list whose identity may reuse a compiled array
     * @return validated immutable stage array snapshot
     */
    private static HttpStage[] snapshot(final List<HttpStage> stages) {
        final List<HttpStage> source = require(stages, "HTTP stages");
        final CompiledStages cached = compiledStages;
        if (cached != null && source == cached.source) {
            return cached.stages;
        }
        for (final HttpStage stage : source) {
            validateStage(stage);
        }
        final HttpStage[] compiled = source.toArray(HttpStage[]::new);
        compiledStages = new CompiledStages(source, compiled);
        return compiled;
    }

    /**
     * Validates a stage.
     *
     * @param stage stage reference to validate
     */
    private static void validateStage(final HttpStage stage) {
        require(stage, "HTTP stage");
    }

    /**
     * Validates a stage index.
     *
     * @param index candidate next-stage index
     * @param size  stage size
     * @return validated index between zero and stage count inclusive
     */
    private static int validateIndex(final int index, final int size) {
        if (index < Normal._0 || index > size) {
            throw new ValidateException("HTTP chain index is out of range");
        }
        return index;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name
     * @param <T>   value type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Atomically published compiled stage snapshot.
     *
     * @param source source list identity
     * @param stages compiled stage array
     */
    private record CompiledStages(List<HttpStage> source, HttpStage[] stages) {
    }

    /**
     * Delivery state supplied by the network owner, never inferred from an exception class.
     */
    public enum DeliveryState {
        /**
         * No request bytes reached the peer.
         */
        NOT_SENT,
        /**
         * The peer may have received or processed part of the request.
         */
        MAYBE_PROCESSED,
        /**
         * The peer explicitly confirmed that the request was not processed.
         */
        PEER_CONFIRMED_UNPROCESSED
    }

    /**
     * Failure ownership scope.
     */
    public enum FailureScope {
        /**
         * Failure is isolated to the logical request.
         */
        REQUEST,
        /**
         * Failure terminates one multiplexed stream.
         */
        STREAM,
        /**
         * Failure invalidates the underlying physical connection.
         */
        CONNECTION
    }

    /**
     * Structured retry-relevant failure reason.
     */
    public enum FailureReason {
        /**
         * DNS resolution failed.
         */
        DNS,
        /**
         * Transport connection establishment failed.
         */
        CONNECT,
        /**
         * TLS negotiation or validation failed.
         */
        TLS,
        /**
         * A configured deadline expired.
         */
        TIMEOUT,
        /**
         * The caller or runtime cancelled the exchange.
         */
        CANCELLED,
        /**
         * The peer refused an HTTP/2 stream before processing it.
         */
        REFUSED_STREAM,
        /**
         * Peer GOAWAY confirmed that the stream was not processed.
         */
        GOAWAY_UNPROCESSED,
        /**
         * Network input or output failed.
         */
        IO,
        /**
         * The peer violated the active protocol.
         */
        PROTOCOL,
        /**
         * No more specific authoritative reason was available.
         */
        UNKNOWN
    }

    /**
     * Structured exchange failure carrying authoritative delivery and ownership facts.
     */
    public static final class ExchangeFailure extends RuntimeException {

        /**
         * Authoritative request delivery state at the failure boundary.
         */
        private final DeliveryState deliveryState;

        /**
         * Resource ownership scope invalidated by the failure.
         */
        private final FailureScope scope;

        /**
         * Stable retry-relevant failure classification.
         */
        private final FailureReason reason;

        /**
         * Creates a structured exchange failure.
         *
         * @param deliveryState authoritative delivery state
         * @param scope         invalidated ownership scope
         * @param reason        stable failure reason
         * @param cause         underlying failure, or {@code null}
         */
        public ExchangeFailure(final DeliveryState deliveryState, final FailureScope scope, final FailureReason reason,
                final Throwable cause) {
            super(cause == null ? "HTTP exchange failed: " + reason : cause.getMessage(), cause);
            this.deliveryState = require(deliveryState, "Delivery state");
            this.scope = require(scope, "Failure scope");
            this.reason = require(reason, "Failure reason");
        }

        /**
         * Returns the authoritative delivery state.
         *
         * @return delivery state
         */
        public DeliveryState deliveryState() {
            return deliveryState;
        }

        /**
         * Returns the invalidated ownership scope.
         *
         * @return failure scope
         */
        public FailureScope scope() {
            return scope;
        }

        /**
         * Returns the stable failure classification.
         *
         * @return failure reason
         */
        public FailureReason reason() {
            return reason;
        }
    }

}
