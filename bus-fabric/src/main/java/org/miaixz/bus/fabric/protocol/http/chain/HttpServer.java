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

import java.util.Locale;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.codec.Http1Codec;
import org.miaixz.bus.fabric.protocol.http.codec.Http2Codec;
import org.miaixz.bus.fabric.protocol.http.codec.HttpCodec;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Connection;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * HTTP chain stage that transfers a request and response over the selected connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpServer implements HttpStage {

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Codec factory.
     */
    private final Function<HttpChain, HttpCodec> codecs;

    /**
     * Creates a server stage.
     */
    public HttpServer() {
        this(HttpServer::networkCodec);
    }

    /**
     * Creates a server stage with shared dispatcher.
     *
     * @param dispatcher runtime dispatcher
     */
    public HttpServer(final Dispatcher dispatcher) {
        this(chain -> networkCodec(chain, require(dispatcher, "Dispatcher")));
    }

    /**
     * Creates a server stage with a codec factory.
     *
     * @param codecs codec factory
     */
    HttpServer(final Function<HttpChain, HttpCodec> codecs) {
        this.name = normalizeName("http-server");
        this.codecs = require(codecs, "HTTP codec factory");
    }

    /**
     * Writes a request and reads the response through the chain connection.
     *
     * @param request request
     * @param chain   chain
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain context = require(chain, "HTTP chain");
        final Cancellation cancellation = context.cancellation();
        cancellation.throwIfCancelled();
        final HttpCodec codec = require(codecs.apply(context), "HTTP codec");
        cancellation.onCancel(codec::cancel);
        writeRequest(current, codec);
        final long sentRequestAtMillis = System.currentTimeMillis();
        cancellation.throwIfCancelled();
        final HttpResponse response = readResponse(current, codec);
        final long receivedResponseAtMillis = System.currentTimeMillis();
        return response.toBuilder().timing(sentRequestAtMillis, receivedResponseAtMillis).build();
    }

    /**
     * Writes a request through a codec.
     *
     * @param request request
     * @param codec   codec
     */
    public void writeRequest(final HttpRequest request, final HttpCodec codec) {
        require(codec, "HTTP codec").writeRequest(require(request, "HTTP request"));
    }

    /**
     * Reads a response through a codec.
     *
     * @param request request
     * @param codec   codec
     * @return response
     */
    public HttpResponse readResponse(final HttpRequest request, final HttpCodec codec) {
        return require(codec, "HTTP codec").readResponse(require(request, "HTTP request"));
    }

    /**
     * Returns stage name.
     *
     * @return stage name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Creates a network codec from the current chain.
     *
     * @param chain chain
     * @return codec
     */
    private static HttpCodec networkCodec(final HttpChain chain) {
        final Connection connection = chain.connection();
        if (connection == null) {
            throw new StatefulException("HTTP chain does not contain a network connection");
        }
        if (http2(connection.destination())) {
            return new Http2Codec(Http2Connection.create(connection));
        }
        return new Http1Codec(connection);
    }

    /**
     * Creates a network codec from the current chain.
     *
     * @param chain      chain
     * @param dispatcher runtime dispatcher
     * @return codec
     */
    private static HttpCodec networkCodec(final HttpChain chain, final Dispatcher dispatcher) {
        final Connection connection = chain.connection();
        if (connection == null) {
            throw new StatefulException("HTTP chain does not contain a network connection");
        }
        if (http2(connection.destination())) {
            return new Http2Codec(Http2Connection.create(connection, dispatcher));
        }
        return new Http1Codec(connection);
    }

    /**
     * Returns whether the selected connection is HTTP/2.
     *
     * @param destination connection destination
     * @return true when HTTP/2 should be used
     */
    private static boolean http2(final Destination destination) {
        if (destination == null) {
            return false;
        }
        if (destination.protocol() == Protocol.HTTP_2 || destination.protocol() == Protocol.H2_PRIOR_KNOWLEDGE) {
            return true;
        }
        final Object protocol = destination.options().get("protocol");
        return protocol != null
                && ("h2".equalsIgnoreCase(protocol.toString()) || "http/2".equalsIgnoreCase(protocol.toString())
                        || "h2_prior_knowledge".equalsIgnoreCase(protocol.toString()));
    }

    /**
     * Normalizes a stage name.
     *
     * @param value value
     * @return normalized name
     */
    private static String normalizeName(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("HTTP server name must be non-blank and single-line");
        }
        return StringKit.trim(value).toLowerCase(Locale.ROOT);
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
