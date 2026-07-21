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

import org.miaixz.bus.core.lang.Assert;
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
     * Ordered stages.
     */
    private final HttpStage[] stages;

    /**
     * Current stage index.
     */
    private final int index;

    /**
     * Current leased connection.
     */
    private final ConnectionLease lease;

    /**
     * Current network connection.
     */
    private final Connection connection;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * One-shot cursor state; cursors are exchange-local and synchronously consumed.
     */
    private boolean proceeded;

    /**
     * Creates a chain.
     *
     * @param stages     stages
     * @param index      current index
     * @param lease      leased connection
     * @param connection network connection
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
     * @param stages stages
     * @return chain
     */
    public static HttpChain create(final List<HttpStage> stages) {
        return create(stages, Cancellation.create());
    }

    /**
     * Creates a chain at index zero with a shared cancellation scope.
     *
     * @param stages       stages
     * @param cancellation cancellation scope
     * @return chain
     */
    public static HttpChain create(final List<HttpStage> stages, final Cancellation cancellation) {
        return new HttpChain(snapshot(stages), Normal._0, null, null, cancellation);
    }

    /**
     * Proceeds to the next stage.
     *
     * @param request request
     * @return response
     */
    public HttpResponse proceed(final HttpRequest request) {
        final HttpRequest current = Assert
                .notNull(request, () -> new ValidateException("HTTP request must not be null"));
        if (proceeded) {
            throw new StatefulException("HTTP chain cursor has already proceeded");
        }
        proceeded = true;
        if (index >= stages.length) {
            throw new StatefulException("HTTP chain is exhausted");
        }
        cancellation.throwIfCancelled();
        final HttpStage stage = stages[index];
        return stage.execute(current, new HttpChain(stages, index + 1, lease, connection, cancellation));
    }

    /**
     * Returns a new chain with an appended stage.
     *
     * @param stage stage
     * @return new chain
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
     * @return index
     */
    public int index() {
        return index;
    }

    /**
     * Returns stage snapshot.
     *
     * @return stages
     */
    public List<HttpStage> stages() {
        return List.of(stages);
    }

    /**
     * Creates a fresh one-shot cursor at the same downstream position for an explicit retry or follow-up attempt.
     *
     * @return fresh replay cursor
     */
    HttpChain replayFromCurrent() {
        return new HttpChain(stages, index, lease, connection, cancellation);
    }

    /**
     * Returns a chain that carries a leased connection for downstream stages.
     *
     * @param lease      lease
     * @param connection connection
     * @return contextual chain
     */
    HttpChain withConnection(final ConnectionLease lease, final Connection connection) {
        final ConnectionLease currentLease = Assert
                .notNull(lease, () -> new ValidateException("Connection lease must not be null"));
        final Connection currentConnection = Assert
                .notNull(connection, () -> new ValidateException("Network connection must not be null"));
        return new HttpChain(stages, index, currentLease, currentConnection, cancellation);
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
     * @param stages stages
     * @return stages
     */
    private static HttpStage[] snapshot(final List<HttpStage> stages) {
        final List<HttpStage> source = Assert
                .notNull(stages, () -> new ValidateException("HTTP stages must not be null"));
        for (final HttpStage stage : source) {
            validateStage(stage);
        }
        return source.toArray(HttpStage[]::new);
    }

    /**
     * Validates a stage.
     *
     * @param stage stage
     */
    private static void validateStage(final HttpStage stage) {
        Assert.notNull(stage, () -> new ValidateException("HTTP stage must not be null"));
    }

    /**
     * Validates a stage index.
     *
     * @param index index
     * @param size  stage size
     * @return index
     */
    private static int validateIndex(final int index, final int size) {
        Assert.isTrue(
                index >= Normal._0 && index <= size,
                () -> new ValidateException("HTTP chain index is out of range"));
        return index;
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
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
