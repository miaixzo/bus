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
package org.miaixz.bus.fabric;

import java.time.Duration;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable time policy shared by protocol builders and runtime calls.
 * <p>
 * Except for {@code close}, {@link Duration#ZERO} means that the protocol stage does not install a deadline or
 * keep-alive interval for that field. Close coordination always has a positive deadline so native resources cannot
 * remain indefinitely half-closed.
 * <ul>
 * <li>{@code connect}: TCP establishment, TLS handshake, and HTTP-family connection or upgrade establishment used by
 * HTTP, WebSocket, SSE, and STOMP.</li>
 * <li>{@code read}: bounded HTTP, Socket, WebSocket, SSE, STOMP, and TLS reads.</li>
 * <li>{@code write}: bounded network writes for HTTP, Socket, WebSocket, SSE, STOMP, and TLS.</li>
 * <li>{@code call}: the complete logical protocol Call deadline rather than one individual read or write.</li>
 * <li>{@code ping}: the WebSocket or Socket keep-alive interval.</li>
 * <li>{@code close}: the TLS {@code close_notify}, WebSocket Close handshake, and bounded server/session graceful-close
 * wait.</li>
 * </ul>
 *
 * @param connect TCP, TLS, and HTTP-family connection establishment deadline; zero disables it
 * @param read    HTTP, Socket, WebSocket, SSE, STOMP, and TLS read deadline; zero disables it
 * @param write   HTTP, Socket, WebSocket, SSE, STOMP, and TLS write deadline; zero disables it
 * @param call    complete protocol Call deadline; zero disables it
 * @param ping    WebSocket and Socket keep-alive interval; zero disables it
 * @param close   positive TLS close_notify, WebSocket Close, and graceful-close deadline
 * @author Kimi Liu
 * @since Java 21+
 */
public record Timeout(Duration connect, Duration read, Duration write, Duration call, Duration ping, Duration close) {

    /**
     * Typed option for the shared protocol timeout policy.
     */
    public static final Options.Key<Timeout> OPTION = Options.key("timeout", Timeout.class);

    /**
     * Shared immutable default time policy.
     */
    private static final Timeout DEFAULTS = new Timeout(org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK,
            org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK,
            org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK, Duration.ZERO, Duration.ZERO,
            org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_CLOSE);

    /**
     * Creates a validated immutable time policy.
     *
     * @param connect TCP, TLS, and HTTP-family connection establishment deadline
     * @param read    HTTP, Socket, WebSocket, SSE, STOMP, and TLS read deadline
     * @param write   HTTP, Socket, WebSocket, SSE, STOMP, and TLS write deadline
     * @param call    complete protocol Call deadline
     * @param ping    WebSocket and Socket keep-alive interval
     * @param close   positive TLS and WebSocket close deadline
     */
    public Timeout {
        connect = validate(connect, "Connect timeout");
        read = validate(read, "Read timeout");
        write = validate(write, "Write timeout");
        call = validate(call, "Call timeout");
        ping = validate(ping, "Ping interval");
        close = validateClose(close);
    }

    /**
     * Creates a compatibility policy whose close deadline is fixed at sixty seconds.
     *
     * @param connect TCP, TLS, and HTTP-family connection establishment deadline
     * @param read    HTTP, Socket, WebSocket, SSE, STOMP, and TLS read deadline
     * @param write   HTTP, Socket, WebSocket, SSE, STOMP, and TLS write deadline
     * @param call    complete protocol Call deadline
     * @param ping    WebSocket and Socket keep-alive interval
     */
    public Timeout(final Duration connect, final Duration read, final Duration write, final Duration call,
            final Duration ping) {
        this(connect, read, write, call, ping, org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_CLOSE);
    }

    /**
     * Returns the default time policy.
     *
     * @return default time policy
     */
    public static Timeout defaults() {
        return DEFAULTS;
    }

    /**
     * Adds this timeout policy to an immutable option snapshot.
     *
     * @param options option source
     * @return updated option snapshot
     */
    public Options from(final Options options) {
        if (options == null) {
            throw new ValidateException("Options must not be null");
        }
        return options.with(OPTION, this);
    }

    /**
     * Creates a policy that applies the same timeout to connect, read, write, and call.
     * <p>
     * The WebSocket or Socket ping interval stays disabled, and close coordination retains its fixed sixty-second
     * deadline, because keep-alive and graceful close are not ordinary operation deadlines.
     *
     * @param timeout unified timeout
     * @return timeout policy
     */
    public static Timeout of(final Duration timeout) {
        final Duration validated = validate(timeout, "Timeout");
        return new Timeout(validated, validated, validated, validated, Duration.ZERO,
                org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_CLOSE);
    }

    /**
     * Creates a time policy builder initialized to {@link #defaults()} values.
     *
     * @return time policy builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the connection establishment timeout.
     * <p>
     * Used by TCP establishment, TLS handshake, and HTTP-family connection or upgrade establishment for HTTP,
     * WebSocket, SSE, and STOMP. Zero means no connection deadline is installed.
     *
     * @return connection establishment timeout
     */
    @Override
    public Duration connect() {
        return connect;
    }

    /**
     * Returns the protocol read timeout.
     * <p>
     * Used by HTTP, Socket, WebSocket, SSE, STOMP, and TLS reads. Zero means no read deadline is installed.
     *
     * @return read timeout
     */
    @Override
    public Duration read() {
        return read;
    }

    /**
     * Returns the protocol write timeout.
     * <p>
     * Used by HTTP, Socket, WebSocket, SSE, STOMP, and TLS writes. Zero means no write deadline is installed.
     *
     * @return write timeout
     */
    @Override
    public Duration write() {
        return write;
    }

    /**
     * Returns the logical operation timeout.
     * <p>
     * Covers a complete logical protocol Call rather than one read or write stage. Zero means no Call deadline is
     * installed.
     *
     * @return logical operation timeout
     */
    @Override
    public Duration call() {
        return call;
    }

    /**
     * Returns the WebSocket and Socket keep-alive interval.
     * <p>
     * Zero disables protocol keep-alive scheduling. It is separate from SSE retry and STOMP heartbeat negotiation.
     *
     * @return WebSocket ping interval
     */
    @Override
    public Duration ping() {
        return ping;
    }

    /**
     * Returns the graceful-close deadline.
     * <p>
     * Used by TLS close_notify, the WebSocket Close handshake, and bounded Socket/WebSocket server or session shutdown.
     * Unlike the other components, close is always positive.
     *
     * @return positive close deadline
     */
    @Override
    public Duration close() {
        return close;
    }

    /**
     * Validates that a duration is present and non-negative.
     *
     * @param timeout duration candidate
     * @param name    field name
     * @return validated duration
     */
    private static Duration validate(final Duration timeout, final String name) {
        if (timeout == null || timeout.isNegative()) {
            throw new ValidateException(name + " must be non-null and non-negative");
        }
        return timeout;
    }

    /**
     * Validates the mandatory positive close deadline.
     *
     * @param timeout close deadline candidate
     * @return validated close deadline
     */
    private static Duration validateClose(final Duration timeout) {
        final Duration checked = validate(timeout, "Close timeout");
        if (checked.isZero()) {
            throw new ValidateException("Close timeout must be positive");
        }
        return checked;
    }

    /**
     * Builder for immutable time policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Candidate connection establishment timeout.
         */
        private Duration connect = org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK;

        /**
         * Candidate protocol read timeout.
         */
        private Duration read = org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK;

        /**
         * Candidate protocol write timeout.
         */
        private Duration write = org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_NETWORK;

        /**
         * Candidate logical operation timeout.
         */
        private Duration call = Duration.ZERO;

        /**
         * Candidate WebSocket automatic ping interval.
         */
        private Duration ping = Duration.ZERO;

        /**
         * Candidate TLS, WebSocket, server, and session graceful-close deadline.
         */
        private Duration close = org.miaixz.bus.fabric.Builder.TIMEOUT_DEFAULT_CLOSE;

        /**
         * Creates a builder initialized to the complete default policy.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the connection establishment timeout.
         * <p>
         * Applies to TCP establishment, TLS handshake, and HTTP-family establishment for HTTP, WebSocket, SSE, and
         * STOMP. Zero disables the deadline.
         *
         * @param timeout connection establishment timeout
         * @return this builder
         */
        public Builder connect(final Duration timeout) {
            final Duration validated = validate(timeout, "Connect timeout");
            connect = validated;
            return this;
        }

        /**
         * Sets the protocol read timeout.
         * <p>
         * Applies to HTTP, Socket, WebSocket, SSE, STOMP, and TLS reads. Zero disables the deadline.
         *
         * @param timeout read timeout
         * @return this builder
         */
        public Builder read(final Duration timeout) {
            final Duration validated = validate(timeout, "Read timeout");
            read = validated;
            return this;
        }

        /**
         * Sets the protocol write timeout.
         * <p>
         * Applies to HTTP, Socket, WebSocket, SSE, STOMP, and TLS writes. Zero disables the deadline.
         *
         * @param timeout write timeout
         * @return this builder
         */
        public Builder write(final Duration timeout) {
            final Duration validated = validate(timeout, "Write timeout");
            write = validated;
            return this;
        }

        /**
         * Sets the logical operation timeout.
         * <p>
         * Covers a complete protocol Call rather than an individual read or write. Zero disables the deadline.
         *
         * @param timeout logical operation timeout
         * @return this builder
         */
        public Builder call(final Duration timeout) {
            final Duration validated = validate(timeout, "Call timeout");
            call = validated;
            return this;
        }

        /**
         * Sets the WebSocket and Socket keep-alive interval.
         * <p>
         * Zero disables keep-alive scheduling. This does not configure SSE retry or STOMP heartbeat negotiation.
         *
         * @param interval WebSocket ping interval
         * @return this builder
         */
        public Builder ping(final Duration interval) {
            final Duration validated = validate(interval, "Ping interval");
            ping = validated;
            return this;
        }

        /**
         * Sets the mandatory graceful-close deadline.
         * <p>
         * Applies to TLS close_notify, the WebSocket Close handshake, and bounded Socket/WebSocket server or session
         * shutdown. Zero is rejected.
         *
         * @param timeout positive close deadline
         * @return this builder
         */
        public Builder close(final Duration timeout) {
            final Duration validated = validateClose(timeout);
            close = validated;
            return this;
        }

        /**
         * Builds an immutable time policy.
         *
         * @return time policy
         */
        public Timeout build() {
            return new Timeout(connect, read, write, call, ping, close);
        }

    }

}
