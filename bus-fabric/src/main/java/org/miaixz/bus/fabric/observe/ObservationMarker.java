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
package org.miaixz.bus.fabric.observe;

/**
 * Stable observation marker codes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ObservationMarker {

    /**
     * Call start.
     */
    CALL_START("call.start", false, false, Timing.START, "call"),

    /**
     * Call success.
     */
    CALL_SUCCESS("call.success", false, true, Timing.STOP, "call"),

    /**
     * Call failure.
     */
    CALL_FAILED("call.failed", true, true, Timing.STOP, "call"),

    /**
     * Call cancellation.
     */
    CALL_CANCELLED("call.cancelled", true, true, Timing.STOP, "call"),

    /**
     * Lifecycle listener failure.
     */
    LISTENER_FAILED("listener.failed", true, false, Timing.NONE, null),

    /**
     * DNS start.
     */
    DNS_START("dns.start", false, false, Timing.START, "dns"),

    /**
     * DNS success.
     */
    DNS_SUCCESS("dns.success", false, true, Timing.STOP, "dns"),

    /**
     * DNS failure.
     */
    DNS_FAILED("dns.failed", true, true, Timing.STOP, "dns"),

    /**
     * Connection start.
     */
    CONNECT_START("connect.start", false, false, Timing.START, "connect"),

    /**
     * Connection success.
     */
    CONNECT_SUCCESS("connect.success", false, true, Timing.STOP, "connect"),

    /**
     * Connection failure.
     */
    CONNECT_FAILED("connect.failed", true, true, Timing.STOP, "connect"),

    /**
     * Pool acquire.
     */
    POOL_ACQUIRE("pool.acquire", false, false, Timing.NONE, null),

    /**
     * Pool release.
     */
    POOL_RELEASE("pool.release", false, true, Timing.NONE, null),

    /**
     * Route backoff.
     */
    ROUTE_BACKOFF("route.backoff", true, false, Timing.NONE, null),

    /**
     * Route ready.
     */
    ROUTE_READY("route.ready", false, false, Timing.NONE, null),

    /**
     * HTTP request.
     */
    HTTP_REQUEST("http.request", false, false, Timing.START, "http"),

    /**
     * HTTP request headers.
     */
    HTTP_REQUEST_HEADERS("http.request.headers", false, false, Timing.NONE, "http"),

    /**
     * HTTP request body.
     */
    HTTP_REQUEST_BODY("http.request.body", false, false, Timing.NONE, "http"),

    /**
     * HTTP response.
     */
    HTTP_RESPONSE("http.response", false, true, Timing.STOP, "http"),

    /**
     * HTTP response headers.
     */
    HTTP_RESPONSE_HEADERS("http.response.headers", false, false, Timing.NONE, "http"),

    /**
     * HTTP response body.
     */
    HTTP_RESPONSE_BODY("http.response.body", false, true, Timing.NONE, "http"),

    /**
     * HTTP failure.
     */
    HTTP_FAILED("http.failed", true, true, Timing.STOP, "http"),

    /**
     * HTTP cache hit.
     */
    CACHE_HIT("cache.hit", false, true, Timing.NONE, null),

    /**
     * HTTP cache miss.
     */
    CACHE_MISS("cache.miss", false, false, Timing.NONE, null),

    /**
     * HTTP cache conditional hit or update.
     */
    CACHE_CONDITIONAL_HIT("cache.conditional", false, true, Timing.NONE, null),

    /**
     * WebSocket open.
     */
    WEBSOCKET_OPEN("websocket.open", false, false, Timing.START, "websocket"),

    /**
     * WebSocket message.
     */
    WEBSOCKET_MESSAGE("websocket.message", false, false, Timing.NONE, "websocket"),

    /**
     * WebSocket closed.
     */
    WEBSOCKET_CLOSED("websocket.closed", false, true, Timing.STOP, "websocket"),

    /**
     * WebSocket failure.
     */
    WEBSOCKET_FAILED("websocket.failed", true, true, Timing.STOP, "websocket"),

    /**
     * WebSocket cancellation.
     */
    WEBSOCKET_CANCELLED("websocket.cancelled", true, true, Timing.STOP, "websocket"),

    /**
     * SSE open.
     */
    SSE_OPEN("sse.open", false, false, Timing.START, "sse"),

    /**
     * SSE event.
     */
    SSE_EVENT("sse.event", false, false, Timing.NONE, "sse"),

    /**
     * SSE closed.
     */
    SSE_CLOSED("sse.closed", false, true, Timing.STOP, "sse"),

    /**
     * SSE failure.
     */
    SSE_FAILED("sse.failed", true, true, Timing.STOP, "sse"),

    /**
     * SSE cancellation.
     */
    SSE_CANCELLED("sse.cancelled", true, true, Timing.STOP, "sse"),

    /**
     * STOMP open.
     */
    STOMP_OPEN("stomp.open", false, false, Timing.START, "stomp"),

    /**
     * STOMP frame or message.
     */
    STOMP_MESSAGE("stomp.message", false, false, Timing.NONE, "stomp"),

    /**
     * STOMP closed.
     */
    STOMP_CLOSED("stomp.closed", false, true, Timing.STOP, "stomp"),

    /**
     * STOMP failure.
     */
    STOMP_FAILED("stomp.failed", true, true, Timing.STOP, "stomp"),

    /**
     * STOMP cancellation.
     */
    STOMP_CANCELLED("stomp.cancelled", true, true, Timing.STOP, "stomp"),

    /**
     * Socket open.
     */
    SOCKET_OPEN("socket.open", false, false, Timing.START, "socket"),

    /**
     * Socket read.
     */
    SOCKET_READ("socket.read", false, false, Timing.NONE, "socket"),

    /**
     * Socket write.
     */
    SOCKET_WRITE("socket.write", false, false, Timing.NONE, "socket"),

    /**
     * Socket closed.
     */
    SOCKET_CLOSED("socket.closed", false, true, Timing.STOP, "socket"),

    /**
     * Socket failure.
     */
    SOCKET_FAILED("socket.failed", true, true, Timing.STOP, "socket"),

    /**
     * Socket cancellation.
     */
    SOCKET_CANCELLED("socket.cancelled", true, true, Timing.STOP, "socket"),

    /**
     * PROXY protocol metadata parsed.
     */
    PROXY_PARSED("proxy.parsed", false, false, Timing.NONE, null),

    /**
     * TLS handshake start.
     */
    TLS_START("tls.start", false, false, Timing.START, "tls"),

    /**
     * TLS handshake completed.
     */
    TLS_HANDSHAKE("tls.handshake", false, true, Timing.STOP, "tls"),

    /**
     * TLS failure.
     */
    TLS_FAILED("tls.failed", true, true, Timing.STOP, "tls"),

    /**
     * TLS handshake cancellation.
     */
    TLS_CANCELLED("tls.cancelled", true, true, Timing.STOP, "tls");

    /**
     * Stable code.
     */
    private final String code;

    /**
     * Failure flag.
     */
    private final boolean failure;

    /**
     * Terminal flag.
     */
    private final boolean terminal;

    /**
     * Timer role.
     */
    private final Timing timing;

    /**
     * Timer family or null for non-timed infrastructure events.
     */
    private final String family;

    /**
     * Creates a marker.
     *
     * @param code     stable code
     * @param failure  failure flag
     * @param terminal terminal flag
     * @param timing   timer role
     * @param family   timer family or null
     */
    ObservationMarker(final String code, final boolean failure, final boolean terminal, final Timing timing,
            final String family) {
        this.code = code;
        this.failure = failure;
        this.terminal = terminal;
        this.timing = timing;
        this.family = family;
    }

    /**
     * Returns the stable code.
     *
     * @return code
     */
    public String code() {
        return code;
    }

    /**
     * Returns whether the marker represents failure.
     *
     * @return true when failed
     */
    public boolean failure() {
        return failure;
    }

    /**
     * Returns whether the marker is terminal.
     *
     * @return true when terminal
     */
    public boolean terminal() {
        return terminal;
    }

    /**
     * Returns the timer role.
     *
     * @return timer role
     */
    public Timing timing() {
        return timing;
    }

    /**
     * Returns the timer family.
     *
     * @return timer family or null for non-timed infrastructure events
     */
    public String family() {
        return family;
    }

    /**
     * Observation timer role.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Timing {

        /**
         * Does not affect a timer.
         */
        NONE,

        /**
         * Starts a timer.
         */
        START,

        /**
         * Stops a timer.
         */
        STOP

    }

}
