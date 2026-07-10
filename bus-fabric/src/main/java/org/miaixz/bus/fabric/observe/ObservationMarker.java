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
    CALL_START("call.start", false, false),

    /**
     * Call success.
     */
    CALL_SUCCESS("call.success", false, true),

    /**
     * Call failure.
     */
    CALL_FAILED("call.failed", true, true),

    /**
     * Call cancellation.
     */
    CALL_CANCELLED("call.cancelled", true, true),

    /**
     * Lifecycle listener failure.
     */
    LISTENER_FAILED("listener.failed", true, false),

    /**
     * DNS start.
     */
    DNS_START("dns.start", false, false),

    /**
     * DNS success.
     */
    DNS_SUCCESS("dns.success", false, true),

    /**
     * DNS failure.
     */
    DNS_FAILED("dns.failed", true, true),

    /**
     * Connection start.
     */
    CONNECT_START("connect.start", false, false),

    /**
     * Connection success.
     */
    CONNECT_SUCCESS("connect.success", false, true),

    /**
     * Connection failure.
     */
    CONNECT_FAILED("connect.failed", true, true),

    /**
     * Pool acquire.
     */
    POOL_ACQUIRE("pool.acquire", false, false),

    /**
     * Pool release.
     */
    POOL_RELEASE("pool.release", false, true),

    /**
     * Route backoff.
     */
    ROUTE_BACKOFF("route.backoff", true, false),

    /**
     * Route ready.
     */
    ROUTE_READY("route.ready", false, false),

    /**
     * HTTP request.
     */
    HTTP_REQUEST("http.request", false, false),

    /**
     * HTTP request headers.
     */
    HTTP_REQUEST_HEADERS("http.request.headers", false, false),

    /**
     * HTTP request body.
     */
    HTTP_REQUEST_BODY("http.request.body", false, false),

    /**
     * HTTP response.
     */
    HTTP_RESPONSE("http.response", false, true),

    /**
     * HTTP response headers.
     */
    HTTP_RESPONSE_HEADERS("http.response.headers", false, false),

    /**
     * HTTP response body.
     */
    HTTP_RESPONSE_BODY("http.response.body", false, true),

    /**
     * HTTP failure.
     */
    HTTP_FAILED("http.failed", true, true),

    /**
     * HTTP cache hit.
     */
    CACHE_HIT("cache.hit", false, true),

    /**
     * HTTP cache miss.
     */
    CACHE_MISS("cache.miss", false, false),

    /**
     * HTTP cache conditional hit or update.
     */
    CACHE_CONDITIONAL_HIT("cache.conditional", false, true),

    /**
     * WebSocket open.
     */
    WEBSOCKET_OPEN("websocket.open", false, false),

    /**
     * WebSocket message.
     */
    WEBSOCKET_MESSAGE("websocket.message", false, false),

    /**
     * WebSocket closed.
     */
    WEBSOCKET_CLOSED("websocket.closed", false, true),

    /**
     * WebSocket failure.
     */
    WEBSOCKET_FAILED("websocket.failed", true, true),

    /**
     * SSE open.
     */
    SSE_OPEN("sse.open", false, false),

    /**
     * SSE event.
     */
    SSE_EVENT("sse.event", false, false),

    /**
     * SSE closed.
     */
    SSE_CLOSED("sse.closed", false, true),

    /**
     * SSE failure.
     */
    SSE_FAILED("sse.failed", true, true),

    /**
     * STOMP open.
     */
    STOMP_OPEN("stomp.open", false, false),

    /**
     * STOMP frame or message.
     */
    STOMP_MESSAGE("stomp.message", false, false),

    /**
     * STOMP closed.
     */
    STOMP_CLOSED("stomp.closed", false, true),

    /**
     * STOMP failure.
     */
    STOMP_FAILED("stomp.failed", true, true),

    /**
     * Socket open.
     */
    SOCKET_OPEN("socket.open", false, false),

    /**
     * Socket read.
     */
    SOCKET_READ("socket.read", false, false),

    /**
     * Socket write.
     */
    SOCKET_WRITE("socket.write", false, false),

    /**
     * Socket closed.
     */
    SOCKET_CLOSED("socket.closed", false, true),

    /**
     * Socket failure.
     */
    SOCKET_FAILED("socket.failed", true, true),

    /**
     * PROXY protocol metadata parsed.
     */
    PROXY_PARSED("proxy.parsed", false, false),

    /**
     * TLS handshake start.
     */
    TLS_START("tls.start", false, false),

    /**
     * TLS handshake completed.
     */
    TLS_HANDSHAKE("tls.handshake", false, false),

    /**
     * TLS failure.
     */
    TLS_FAILED("tls.failed", true, true);

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
     * Creates a marker.
     *
     * @param code     stable code
     * @param failure  failure flag
     * @param terminal terminal flag
     */
    ObservationMarker(final String code, final boolean failure, final boolean terminal) {
        this.code = code;
        this.failure = failure;
        this.terminal = terminal;
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

}
