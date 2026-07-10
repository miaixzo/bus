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

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable time policy shared by protocol builders and runtime calls.
 * <p>
 * Each value is optional. {@link Duration#ZERO} means that the protocol stage does not install an explicit timeout for
 * that field.
 * <ul>
 * <li>{@code connect}: connection establishment. Used by HTTP, SSE, WebSocket upgrade, STOMP over WebSocket, and TCP
 * socket opens.</li>
 * <li>{@code read}: blocking protocol reads. Used by HTTP response reads, HTTP proxy negotiation, SSE stream reads
 * through HTTP, and WebSocket upgrade reads. Raw socket sessions do not currently enforce this after open.</li>
 * <li>{@code write}: blocking protocol writes. Used by HTTP request writes, HTTP proxy negotiation, WebSocket upgrade
 * writes, and the initial STOMP CONNECT frame. Raw socket sessions do not currently enforce this after open.</li>
 * <li>{@code call}: logical operation wait. Currently used by STOMP while waiting for CONNECTED, falling back to
 * {@code connect} when zero. HTTP, SSE, WebSocket, and raw socket sessions do not currently use it as a global
 * watchdog, because some of them are long-lived streams.</li>
 * <li>{@code ping}: automatic keep-alive interval. Currently used by WebSocket sessions only. It is not HTTP, SSE
 * retry, raw socket keep-alive, or STOMP heartbeat configuration.</li>
 * </ul>
 *
 * @param connect connection establishment timeout; used by HTTP-family connects and TCP socket opens
 * @param read    read timeout for HTTP-family blocking reads; zero means no explicit read timeout
 * @param write   write timeout for HTTP-family blocking writes and STOMP CONNECT writes; zero means no explicit write
 *                timeout
 * @param call    logical operation timeout; currently used by STOMP CONNECTED wait, not as a global HTTP/SSE/socket
 *                timeout
 * @param ping    WebSocket automatic ping interval; zero disables automatic pings
 * @author Kimi Liu
 * @since Java 21+
 */
public record Timeout(Duration connect, Duration read, Duration write, Duration call, Duration ping) {

    /**
     * Creates a validated immutable time policy.
     *
     * @param connect connection establishment timeout
     * @param read    protocol read timeout
     * @param write   protocol write timeout
     * @param call    logical operation timeout
     * @param ping    WebSocket ping interval
     */
    public Timeout {
        connect = validate(connect, "Connect timeout");
        read = validate(read, "Read timeout");
        write = validate(write, "Write timeout");
        call = validate(call, "Call timeout");
        ping = validate(ping, "Ping interval");
    }

    /**
     * Returns the default time policy.
     *
     * @return default time policy
     */
    public static Timeout defaults() {
        return Instances.get(
                Timeout.class.getName() + ".defaults",
                () -> new Timeout(Duration.ofSeconds(10), Duration.ofSeconds(10), Duration.ofSeconds(10), Duration.ZERO,
                        Duration.ZERO));
    }

    /**
     * Creates a policy that applies the same timeout to connect, read, write, and call.
     * <p>
     * The WebSocket ping interval stays disabled because a timeout and a periodic keep-alive interval are different
     * concepts.
     *
     * @param timeout unified timeout
     * @return timeout policy
     */
    public static Timeout of(final Duration timeout) {
        final Duration validated = validate(timeout, "Timeout");
        return new Timeout(validated, validated, validated, validated, Duration.ZERO);
    }

    /**
     * Creates a time policy builder with zero values.
     * <p>
     * Zero values mean no explicit timeout for each corresponding field until the user sets it.
     *
     * @return time policy builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the connection establishment timeout.
     * <p>
     * Used by HTTP-family connection acquisition, SSE and WebSocket HTTP handshakes, STOMP over WebSocket opening, and
     * TCP socket opens.
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
     * Used by HTTP response reads, HTTP proxy negotiation, SSE reads through HTTP, and WebSocket HTTP upgrade reads.
     * Raw socket sessions do not currently enforce this after open.
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
     * Used by HTTP request writes, HTTP proxy negotiation, WebSocket HTTP upgrade writes, and the initial STOMP CONNECT
     * frame write. Raw socket sessions do not currently enforce this after open.
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
     * Currently used by STOMP while waiting for CONNECTED. HTTP, SSE, WebSocket, and raw socket sessions do not use
     * this as a global watchdog.
     *
     * @return logical operation timeout
     */
    @Override
    public Duration call() {
        return call;
    }

    /**
     * Returns the WebSocket automatic ping interval.
     * <p>
     * Currently used by WebSocket sessions only. This does not configure HTTP, SSE reconnects, raw socket keep-alives,
     * or STOMP heartbeats.
     *
     * @return WebSocket ping interval
     */
    @Override
    public Duration ping() {
        return ping;
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
     * Builder for immutable time policies.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Candidate connection establishment timeout.
         */
        private Duration connect = Duration.ZERO;

        /**
         * Candidate protocol read timeout.
         */
        private Duration read = Duration.ZERO;

        /**
         * Candidate protocol write timeout.
         */
        private Duration write = Duration.ZERO;

        /**
         * Candidate logical operation timeout.
         */
        private Duration call = Duration.ZERO;

        /**
         * Candidate WebSocket automatic ping interval.
         */
        private Duration ping = Duration.ZERO;

        /**
         * Creates a builder with zero timeout semantics.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the connection establishment timeout.
         * <p>
         * Applies to HTTP-family connects and TCP socket opens.
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
         * Applies to HTTP response/proxy reads, SSE reads through HTTP, and WebSocket upgrade reads.
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
         * Applies to HTTP request/proxy writes, WebSocket upgrade writes, and the initial STOMP CONNECT frame write.
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
         * Currently used by STOMP while waiting for CONNECTED. It is not a global timeout for HTTP, SSE, WebSocket, or
         * raw socket sessions.
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
         * Sets the WebSocket automatic ping interval.
         * <p>
         * This does not configure HTTP, SSE reconnects, raw socket keep-alives, or STOMP heartbeats.
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
         * Builds an immutable time policy.
         *
         * @return time policy
         */
        public Timeout build() {
            return new Timeout(connect, read, write, call, ping);
        }

    }

}
