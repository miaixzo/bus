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

import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.observe.event.FabricEvent;

/**
 * Bridges current fabric observer events to named listener callbacks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class EventListenerBridge implements EventObserver {

    /**
     * Listener receiving named callbacks with current event payloads.
     */
    private final Listener listener;

    /**
     * Creates a bridge.
     *
     * @param listener listener
     */
    private EventListenerBridge(final Listener listener) {
        if (listener == null) {
            throw new ValidateException("Event listener must not be null");
        }
        this.listener = listener;
    }

    /**
     * Creates a bridge.
     *
     * @param listener listener
     * @return event observer bridge
     */
    public static EventListenerBridge of(final Listener listener) {
        return new EventListenerBridge(listener);
    }

    /**
     * Returns current markers mapped to a listener callback name.
     *
     * @param callbackName callback name
     * @return current markers
     */
    public static List<ObservationMarker> markers(final String callbackName) {
        if (callbackName == null || callbackName.isBlank()) {
            throw new ValidateException("Event callback must be non-blank");
        }
        return switch (callbackName.trim().toLowerCase(Locale.ROOT)) {
            case "callstart" -> List.of(ObservationMarker.CALL_START);
            case "callend" -> List.of(ObservationMarker.CALL_SUCCESS);
            case "callfailed" -> List
                    .of(ObservationMarker.CALL_FAILED, ObservationMarker.CALL_CANCELLED, ObservationMarker.HTTP_FAILED);
            case "dnsstart" -> List.of(ObservationMarker.DNS_START);
            case "dnsend" -> List.of(ObservationMarker.DNS_SUCCESS);
            case "dnsfailed" -> List.of(ObservationMarker.DNS_FAILED);
            case "connectstart" -> List.of(ObservationMarker.CONNECT_START);
            case "connectend" -> List.of(ObservationMarker.CONNECT_SUCCESS);
            case "connectfailed" -> List.of(ObservationMarker.CONNECT_FAILED);
            case "secureconnectstart" -> List.of(ObservationMarker.TLS_START);
            case "secureconnectend" -> List.of(ObservationMarker.TLS_HANDSHAKE);
            case "secureconnectfailed" -> List.of(ObservationMarker.TLS_FAILED);
            case "requestheadersstart" -> List.of(ObservationMarker.HTTP_REQUEST);
            case "requestheadersend" -> List.of(ObservationMarker.HTTP_REQUEST_HEADERS);
            case "requestbodystart", "requestbodyend" -> List.of(ObservationMarker.HTTP_REQUEST_BODY);
            case "responseheadersstart" -> List.of(ObservationMarker.HTTP_RESPONSE);
            case "responseheadersend" -> List.of(ObservationMarker.HTTP_RESPONSE_HEADERS);
            case "responsebodystart", "responsebodyend" -> List.of(ObservationMarker.HTTP_RESPONSE_BODY);
            case "cachehit" -> List.of(ObservationMarker.CACHE_HIT);
            case "cachemiss" -> List.of(ObservationMarker.CACHE_MISS);
            case "cacheconditionalhit" -> List.of(ObservationMarker.CACHE_CONDITIONAL_HIT);
            case "connectionacquired" -> List.of(ObservationMarker.POOL_ACQUIRE);
            case "connectionreleased" -> List.of(ObservationMarker.POOL_RELEASE);
            case "close" -> List.of(
                    ObservationMarker.WEBSOCKET_CLOSED,
                    ObservationMarker.SSE_CLOSED,
                    ObservationMarker.STOMP_CLOSED,
                    ObservationMarker.SOCKET_CLOSED);
            default -> throw new ValidateException("Unknown event callback: " + callbackName);
        };
    }

    @Override
    public void emit(final FabricEvent event) {
        if (event == null) {
            return;
        }
        invoke(() -> listener.event(event));
        switch (event.marker()) {
            case CALL_START -> invoke(() -> listener.callStart(event));
            case CALL_SUCCESS -> invoke(() -> listener.callEnd(event));
            case CALL_FAILED, CALL_CANCELLED -> invoke(() -> listener.callFailed(event, event.cause()));
            case DNS_START -> invoke(() -> listener.dnsStart(event));
            case DNS_SUCCESS -> invoke(() -> listener.dnsEnd(event));
            case DNS_FAILED -> invoke(() -> listener.dnsFailed(event, event.cause()));
            case CONNECT_START -> invoke(() -> listener.connectStart(event));
            case CONNECT_SUCCESS -> invoke(() -> listener.connectEnd(event));
            case CONNECT_FAILED -> invoke(() -> listener.connectFailed(event, event.cause()));
            case TLS_START -> invoke(() -> listener.secureConnectStart(event));
            case TLS_HANDSHAKE -> invoke(() -> listener.secureConnectEnd(event));
            case TLS_FAILED -> invoke(() -> listener.secureConnectFailed(event, event.cause()));
            case HTTP_REQUEST -> invoke(() -> listener.requestHeadersStart(event));
            case HTTP_REQUEST_HEADERS -> invoke(() -> listener.requestHeadersEnd(event));
            case HTTP_REQUEST_BODY -> {
                invoke(() -> listener.requestBodyStart(event));
                invoke(() -> listener.requestBodyEnd(event));
            }
            case HTTP_RESPONSE -> invoke(() -> listener.responseHeadersStart(event));
            case HTTP_RESPONSE_HEADERS -> invoke(() -> listener.responseHeadersEnd(event));
            case HTTP_RESPONSE_BODY -> {
                invoke(() -> listener.responseBodyStart(event));
                invoke(() -> listener.responseBodyEnd(event));
            }
            case HTTP_FAILED -> invoke(() -> listener.callFailed(event, event.cause()));
            case CACHE_HIT -> invoke(() -> listener.cacheHit(event));
            case CACHE_MISS -> invoke(() -> listener.cacheMiss(event));
            case CACHE_CONDITIONAL_HIT -> invoke(() -> listener.cacheConditionalHit(event));
            case POOL_ACQUIRE -> invoke(() -> listener.connectionAcquired(event));
            case POOL_RELEASE -> invoke(() -> listener.connectionReleased(event));
            case WEBSOCKET_CLOSED, SSE_CLOSED, STOMP_CLOSED, SOCKET_CLOSED -> invoke(() -> listener.close(event));
            case WEBSOCKET_FAILED, SSE_FAILED, STOMP_FAILED, SOCKET_FAILED -> invoke(
                    () -> listener.failure(event, event.cause()));
            default -> {
                if (event.marker().failure()) {
                    invoke(() -> listener.failure(event, event.cause()));
                }
            }
        }
    }

    /**
     * Invokes a listener callback without allowing observer failures to escape.
     *
     * @param task callback task
     */
    private static void invoke(final Runnable task) {
        try {
            task.run();
        } catch (final RuntimeException ignored) {
            // Observer callbacks must never change protocol execution.
        }
    }

    /**
     * Event listener callbacks receiving current event payloads.
     */
    public interface Listener {

        /**
         * Receives every event before marker-specific callbacks are invoked.
         *
         * @param event observed event
         */
        default void event(final FabricEvent event) {
        }

        /**
         * Receives the beginning of a call execution.
         *
         * @param event call start event
         */
        default void callStart(final FabricEvent event) {
        }

        /**
         * Receives successful completion of a call.
         *
         * @param event call completion event
         */
        default void callEnd(final FabricEvent event) {
        }

        /**
         * Receives failed completion of a call and the associated cause.
         *
         * @param event call failure event
         * @param cause failure cause
         */
        default void callFailed(final FabricEvent event, final Throwable cause) {
        }

        /**
         * Receives the start of host resolution.
         *
         * @param event DNS start event
         */
        default void dnsStart(final FabricEvent event) {
        }

        /**
         * Receives successful host resolution.
         *
         * @param event DNS completion event
         */
        default void dnsEnd(final FabricEvent event) {
        }

        /**
         * Receives failed host resolution and the associated cause.
         *
         * @param event DNS failure event
         * @param cause failure cause
         */
        default void dnsFailed(final FabricEvent event, final Throwable cause) {
        }

        /**
         * Receives the start of a transport connection attempt.
         *
         * @param event connection start event
         */
        default void connectStart(final FabricEvent event) {
        }

        /**
         * Receives a successfully established transport connection.
         *
         * @param event connection completion event
         */
        default void connectEnd(final FabricEvent event) {
        }

        /**
         * Receives a failed transport connection attempt and the associated cause.
         *
         * @param event connection failure event
         * @param cause failure cause
         */
        default void connectFailed(final FabricEvent event, final Throwable cause) {
        }

        /**
         * Receives the start of a TLS handshake.
         *
         * @param event secure connection start event
         */
        default void secureConnectStart(final FabricEvent event) {
        }

        /**
         * Receives successful TLS handshake completion.
         *
         * @param event secure connection completion event
         */
        default void secureConnectEnd(final FabricEvent event) {
        }

        /**
         * Receives a failed TLS handshake and the associated cause.
         *
         * @param event secure connection failure event
         * @param cause failure cause
         */
        default void secureConnectFailed(final FabricEvent event, final Throwable cause) {
        }

        /**
         * Receives the point immediately before request headers are written.
         *
         * @param event request-header start event
         */
        default void requestHeadersStart(final FabricEvent event) {
        }

        /**
         * Receives completion of request header writing.
         *
         * @param event request-header completion event
         */
        default void requestHeadersEnd(final FabricEvent event) {
        }

        /**
         * Receives the point immediately before request body bytes are written.
         *
         * @param event request-body start event
         */
        default void requestBodyStart(final FabricEvent event) {
        }

        /**
         * Receives completion of request body writing.
         *
         * @param event request-body completion event
         */
        default void requestBodyEnd(final FabricEvent event) {
        }

        /**
         * Receives the point immediately before response headers are read.
         *
         * @param event response-header start event
         */
        default void responseHeadersStart(final FabricEvent event) {
        }

        /**
         * Receives completion of response header reading.
         *
         * @param event response-header completion event
         */
        default void responseHeadersEnd(final FabricEvent event) {
        }

        /**
         * Receives the point immediately before response body bytes are consumed.
         *
         * @param event response-body start event
         */
        default void responseBodyStart(final FabricEvent event) {
        }

        /**
         * Receives completion of response body consumption.
         *
         * @param event response-body completion event
         */
        default void responseBodyEnd(final FabricEvent event) {
        }

        /**
         * Receives a cache lookup that satisfied the request.
         *
         * @param event cache hit event
         */
        default void cacheHit(final FabricEvent event) {
        }

        /**
         * Receives a cache lookup that required the network.
         *
         * @param event cache miss event
         */
        default void cacheMiss(final FabricEvent event) {
        }

        /**
         * Receives a conditional cache validation that reused cached body data.
         *
         * @param event conditional cache hit event
         */
        default void cacheConditionalHit(final FabricEvent event) {
        }

        /**
         * Receives a connection lease handed to a protocol exchange.
         *
         * @param event connection acquired event
         */
        default void connectionAcquired(final FabricEvent event) {
        }

        /**
         * Receives a connection lease returned to the registry or closed.
         *
         * @param event connection released event
         */
        default void connectionReleased(final FabricEvent event) {
        }

        /**
         * Receives session or stream close events.
         *
         * @param event close event
         */
        default void close(final FabricEvent event) {
        }

        /**
         * Receives generic failures that do not map to a narrower callback.
         *
         * @param event failure event
         * @param cause failure cause
         */
        default void failure(final FabricEvent event, final Throwable cause) {
        }

    }

}
