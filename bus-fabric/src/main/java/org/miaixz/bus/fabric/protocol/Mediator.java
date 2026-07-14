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
package org.miaixz.bus.fabric.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.chain.HttpBridge;
import org.miaixz.bus.fabric.protocol.http.chain.HttpConnect;
import org.miaixz.bus.fabric.protocol.http.codec.Http1Codec;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketX;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;

/**
 * Central protocol mediator used by leaf protocols to reuse sibling protocol capabilities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Mediator {

    /**
     * Utility class.
     */
    private Mediator() {
        // No initialization required.
    }

    /**
     * Opens an HTTP GET stream through the HTTP exchange chain.
     *
     * @param context shared context
     * @param uri     target URI
     * @param headers request headers
     * @param timeout request timeout
     * @return protocol-neutral stream response
     */
    public static HttpStream openHttpStream(
            final Context context,
            final URI uri,
            final Headers headers,
            final org.miaixz.bus.fabric.Timeout timeout) {
        final HttpResponse response = org.miaixz.bus.fabric.protocol.http.HttpX.builder(require(context, "Context"))
                .to(require(uri, "HTTP URI").toString()).get().headers(require(headers, "Headers"))
                .timeout(require(timeout, "Timeout")).build().execute();
        return new HttpStream(response.code(), response.headers(), response.body().payload(), response);
    }

    /**
     * Performs an HTTP/1.1 upgrade request and returns the still-open leased connection.
     *
     * @param context shared context
     * @param uri     HTTP upgrade URI
     * @param headers upgrade request headers
     * @param timeout upgrade timeout
     * @return upgrade result
     */
    public static HttpUpgrade upgradeHttp1(
            final Context context,
            final URI uri,
            final Headers headers,
            final org.miaixz.bus.fabric.Timeout timeout) {
        final Context current = require(context, "Context");
        final HttpRequest request = new HttpBridge(cookieJar(current), userAgent(current)).prepare(
                HttpRequest.builder().method(HTTP.Method.GET)
                        .url(UnoUrl.parse(require(uri, "HTTP upgrade URI").toString()))
                        .headers(require(headers, "Headers")).timeout(require(timeout, "Timeout")).build());
        final HttpConnect connect = new HttpConnect(current.directory().connectionPool(), tlsContext(current),
                tlsSettings(current), current.listener(), current.resolver(), current.reactor().dispatcher());
        final ConnectionLease lease = connect.acquire(request);
        try {
            final Connection connection = lease.connection();
            final Http1Codec codec = new Http1Codec(connection);
            codec.writeRequest(request);
            final HttpResponse response = codec.readResponse(request);
            final CookieJar cookies = cookieJar(current);
            if (cookies != null) {
                cookies.save(request.url(), response.headers());
            }
            return new HttpUpgrade(response.code(), response.headers(), connection, lease);
        } catch (final RuntimeException e) {
            lease.close();
            throw e;
        }
    }

    /**
     * Opens a WebSocket session through the WebSocket exchange builder.
     *
     * @param context shared context
     * @param uri     target URI
     * @param headers request headers
     * @param timeout open timeout policy
     * @param handler message handler
     * @return opened session
     */
    public static Session openWebSocket(
            final Context context,
            final URI uri,
            final Headers headers,
            final org.miaixz.bus.fabric.Timeout timeout,
            final Handler handler) {
        return WebSocketX.builder(require(context, "Context")).to(require(uri, "WebSocket URI").toString())
                .headers(require(headers, "Headers")).timeout(require(timeout, "Timeout"))
                .onMessage(require(handler, "Handler")).open();
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
     * Returns configured cookie jar, if present.
     *
     * @param context context
     * @return cookie jar or null
     */
    private static CookieJar cookieJar(final Context context) {
        if (context.options().contains("http.cookieJar")) {
            return context.options().get("http.cookieJar", CookieJar.class);
        }
        if (context.options().contains("cookieJar")) {
            return context.options().get("cookieJar", CookieJar.class);
        }
        if (context.options().contains("http.cookieStore")) {
            return context.options().get("http.cookieStore", CookieJar.class);
        }
        if (context.options().contains("cookieStore")) {
            return context.options().get("cookieStore", CookieJar.class);
        }
        return null;
    }

    /**
     * Returns configured HTTP User-Agent.
     *
     * @param context context
     * @return User-Agent
     */
    private static String userAgent(final Context context) {
        if (context.options().contains("http.userAgent")) {
            final String value = context.options().get("http.userAgent", String.class);
            return value == null ? HttpBridge.defaultUserAgent() : value;
        }
        if (context.options().contains("userAgent")) {
            final String value = context.options().get("userAgent", String.class);
            return value == null ? HttpBridge.defaultUserAgent() : value;
        }
        return HttpBridge.defaultUserAgent();
    }

    /**
     * Returns configured TLS context.
     *
     * @param context context
     * @return TLS context
     */
    private static TlsContext tlsContext(final Context context) {
        if (context.options().contains("http.tlsContext")) {
            return context.options().get("http.tlsContext", TlsContext.class);
        }
        if (context.options().contains("tlsContext")) {
            return context.options().get("tlsContext", TlsContext.class);
        }
        return TlsContext.defaults();
    }

    /**
     * Returns configured TLS settings.
     *
     * @param context context
     * @return TLS settings
     */
    private static TlsSettings tlsSettings(final Context context) {
        if (context.options().contains("http.tlsSettings")) {
            return context.options().get("http.tlsSettings", TlsSettings.class);
        }
        if (context.options().contains("tlsSettings")) {
            return context.options().get("tlsSettings", TlsSettings.class);
        }
        return TlsSettings.defaults();
    }

    /**
     * HTTP stream response without exposing HTTP implementation classes to leaf protocols.
     *
     * @param status  status code
     * @param headers response headers
     * @param body    body payload
     * @param owner   close owner
     */
    public record HttpStream(int status, Headers headers, Payload body, AutoCloseable owner) implements AutoCloseable {

        /**
         * Creates a stream snapshot.
         *
         * @param status  status code
         * @param headers response headers
         * @param body    body payload
         * @param owner   close owner
         */
        public HttpStream {
            headers = require(headers, "Headers");
            body = require(body, "Payload");
            owner = require(owner, "Stream owner");
        }

        /**
         * Opens the response body source and closes the owner with the source.
         *
         * @return body source
         */
        public Source source() {
            return new OwnedSource(body.source(), this);
        }

        /**
         * Opens the response body stream and closes the owner with the stream.
         *
         * @return body stream
         * @deprecated use {@link #source()}
         */
        @Deprecated(since = "8.8.3")
        public InputStream stream() {
            return IoKit.buffer(source()).inputStream();
        }

        @Override
        public void close() {
            try {
                owner.close();
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new InternalException("Unable to close HTTP stream", e);
            }
        }

    }

    /**
     * HTTP upgrade response without exposing HTTP implementation classes to leaf protocols.
     *
     * @param status     status code
     * @param headers    response headers
     * @param connection upgraded connection
     * @param lease      connection lease
     */
    public record HttpUpgrade(int status, Headers headers, Connection connection, ConnectionLease lease)
            implements AutoCloseable {

        /**
         * Creates an upgrade snapshot.
         *
         * @param status     status code
         * @param headers    response headers
         * @param connection upgraded connection
         * @param lease      connection lease
         */
        public HttpUpgrade {
            headers = require(headers, "Headers");
            connection = require(connection, "Network connection");
            lease = require(lease, "Connection lease");
        }

        @Override
        public void close() {
            lease.close();
        }

    }

    /**
     * Source that closes its protocol owner after the body source.
     */
    private static final class OwnedSource extends AssignSource {

        /**
         * Close owner.
         */
        private final AutoCloseable owner;

        /**
         * Creates an owned source.
         *
         * @param source body source
         * @param owner  close owner
         */
        private OwnedSource(final Source source, final AutoCloseable owner) {
            super(require(source, "Body source"));
            this.owner = require(owner, "Stream owner");
        }

        @Override
        public void close() throws IOException {
            IOException failure = null;
            try {
                super.close();
            } catch (final IOException e) {
                failure = e;
            }
            try {
                owner.close();
            } catch (final RuntimeException e) {
                if (failure != null) {
                    e.addSuppressed(failure);
                }
                throw e;
            } catch (final Exception e) {
                final InternalException wrapped = new InternalException("Unable to close owned stream", e);
                if (failure != null) {
                    wrapped.addSuppressed(failure);
                }
                throw wrapped;
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

}
