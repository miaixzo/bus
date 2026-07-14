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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.protocol.http.SoapX;
import org.miaixz.bus.fabric.protocol.socket.SocketX;
import org.miaixz.bus.fabric.protocol.sse.SseX;
import org.miaixz.bus.fabric.protocol.stomp.StompX;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketX;

/**
 * Protocol-neutral fabric entry point backed by one shared context.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Fabric {

    /**
     * Shared context used by all protocol builders.
     */
    private final Context context;

    /**
     * Creates an entry point around an already validated context.
     *
     * @param context shared context
     */
    private Fabric(final Context context) {
        this.context = context;
    }

    /**
     * Creates an entry point using a default context.
     *
     * @return fabric entry point
     */
    public static Fabric create() {
        return create(defaultContext());
    }

    /**
     * Creates an entry point using the supplied context.
     *
     * @param context shared context
     * @return fabric entry point
     */
    public static Fabric create(final Context context) {
        if (context == null) {
            throw new ValidateException("Context must not be null");
        }
        return new Fabric(context);
    }

    /**
     * Returns the shared immutable context.
     *
     * @return shared context
     */
    public Context context() {
        return context;
    }

    /**
     * Creates an HTTP exchange builder using a default context.
     *
     * @return HTTP builder
     */
    public static HttpX.Builder http() {
        return http(defaultContext());
    }

    /**
     * Creates an HTTP exchange builder using the supplied context.
     *
     * @param context shared context
     * @return HTTP builder
     */
    public static HttpX.Builder http(final Context context) {
        return HttpX.builder(require(context, "Context"));
    }

    /**
     * Creates a SOAP exchange using a default context.
     *
     * @param url target URL
     * @return SOAP exchange
     */
    public static SoapX soap(final String url) {
        return soap(defaultContext(), url);
    }

    /**
     * Creates a SOAP exchange using the supplied context.
     *
     * @param context shared context
     * @param url     target URL
     * @return SOAP exchange
     */
    public static SoapX soap(final Context context, final String url) {
        return SoapX.of(require(context, "Context"), url);
    }

    /**
     * Creates a SOAP exchange using a default context.
     *
     * @param url target URL
     * @return SOAP exchange
     */
    public static SoapX soap(final UnoUrl url) {
        return soap(defaultContext(), url);
    }

    /**
     * Creates a SOAP exchange using the supplied context.
     *
     * @param context shared context
     * @param url     target URL
     * @return SOAP exchange
     */
    public static SoapX soap(final Context context, final UnoUrl url) {
        return SoapX.of(require(context, "Context"), require(url, "URL"));
    }

    /**
     * Creates a socket exchange builder using a default context.
     *
     * @return socket builder
     */
    public static SocketX.Builder socket() {
        return socket(defaultContext());
    }

    /**
     * Creates a socket exchange builder using the supplied context.
     *
     * @param context shared context
     * @return socket builder
     */
    public static SocketX.Builder socket(final Context context) {
        return SocketX.builder(require(context, "Context"));
    }

    /**
     * Creates a WebSocket exchange builder using a default context.
     *
     * @return WebSocket builder
     */
    public static WebSocketX.Builder websocket() {
        return websocket(defaultContext());
    }

    /**
     * Creates a WebSocket exchange builder using the supplied context.
     *
     * @param context shared context
     * @return WebSocket builder
     */
    public static WebSocketX.Builder websocket(final Context context) {
        return WebSocketX.builder(require(context, "Context"));
    }

    /**
     * Creates an SSE exchange builder using a default context.
     *
     * @return SSE builder
     */
    public static SseX.Builder sse() {
        return sse(defaultContext());
    }

    /**
     * Creates an SSE exchange builder using the supplied context.
     *
     * @param context shared context
     * @return SSE builder
     */
    public static SseX.Builder sse(final Context context) {
        return SseX.builder(require(context, "Context"));
    }

    /**
     * Creates a STOMP exchange builder using a default context.
     *
     * @return STOMP builder
     */
    public static StompX.Builder stomp() {
        return stomp(defaultContext());
    }

    /**
     * Creates a STOMP exchange builder using the supplied context.
     *
     * @param context shared context
     * @return STOMP builder
     */
    public static StompX.Builder stomp(final Context context) {
        return StompX.builder(require(context, "Context"));
    }

    /**
     * Creates a default context and wraps initialization failures.
     *
     * @return default context
     */
    private static Context defaultContext() {
        try {
            return Context.create();
        } catch (final RuntimeException e) {
            if (e instanceof InternalException) {
                throw e;
            }
            throw new InternalException("Unable to create fabric context", e);
        }
    }

    /**
     * Validates a non-null reference.
     *
     * @param value value
     * @param name  value name
     * @param <T>   value type
     * @return validated value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
