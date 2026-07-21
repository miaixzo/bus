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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.network.Connection;
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
public final class HttpTransport implements HttpStage {

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Codec factory.
     */
    private final Function<HttpChain, HttpCodec> codecs;

    /**
     * Creates a transport stage.
     */
    public HttpTransport() {
        this(HttpTransport::networkCodec);
    }

    /**
     * Creates a transport stage with shared dispatcher.
     *
     * @param dispatcher runtime dispatcher
     */
    public HttpTransport(final Dispatcher dispatcher) {
        this(chain -> networkCodec(chain, require(dispatcher, "Dispatcher")));
    }

    /**
     * Creates a transport stage with a codec factory.
     *
     * @param codecs codec factory
     */
    HttpTransport(final Function<HttpChain, HttpCodec> codecs) {
        this.name = normalizeName("http-transport");
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
        try {
            writeRequest(current, codec);
        } catch (final HttpChain.ExchangeFailure e) {
            throw e;
        } catch (final RuntimeException e) {
            throw failure(cancellation, HttpChain.FailureScope.REQUEST, e);
        }
        final long sentRequestAtMillis = System.currentTimeMillis();
        cancellation.throwIfCancelled();
        final HttpResponse response;
        try {
            response = readResponse(current, codec);
        } catch (final HttpChain.ExchangeFailure e) {
            throw e;
        } catch (final RuntimeException e) {
            throw failure(cancellation, connectionScope(codec), e);
        }
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
        final Connection connection = Assert.notNull(
                chain.connection(),
                () -> new StatefulException("HTTP chain does not contain a network connection"));
        if (http2(connection)) {
            return new Http2Codec(http2Session(connection, null));
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
        final Connection connection = Assert.notNull(
                chain.connection(),
                () -> new StatefulException("HTTP chain does not contain a network connection"));
        if (http2(connection)) {
            return new Http2Codec(http2Session(connection, dispatcher));
        }
        return new Http1Codec(connection);
    }

    /**
     * Returns whether the selected connection is HTTP/2.
     *
     * @param destination connection destination
     * @return true when HTTP/2 should be used
     */
    private static boolean http2(final Connection connection) {
        final Protocol protocol = require(connection, "Network connection").protocol();
        return protocol == Protocol.HTTP_2 || protocol == Protocol.H2_PRIOR_KNOWLEDGE;
    }

    /**
     * Atomically initializes and reuses one HTTP/2 connection session per physical connection.
     */
    private static Http2Connection http2Session(final Connection connection, final Dispatcher dispatcher) {
        final Connection.MultiplexAttachment attachment = Assert.notNull(
                connection.multiplexAttachment(),
                () -> new StatefulException("HTTP/2 connection does not expose a multiplex attachment"));
        synchronized (attachment) {
            final Object existing = attachment.session();
            if (existing != null) {
                if (existing instanceof Http2Connection session) {
                    return session;
                }
                throw new StatefulException("HTTP/2 multiplex attachment contains an incompatible session");
            }
            final Http2Connection created = dispatcher == null ? Http2Connection.create(connection)
                    : Http2Connection.create(connection, dispatcher);
            if (!attachment.compareAndSetSession(null, created)) {
                throw new StatefulException("HTTP/2 session initialization was not linearizable");
            }
            return created;
        }
    }

    /**
     * Wraps an unstructured transport failure with conservative authoritative facts.
     */
    private static HttpChain.ExchangeFailure failure(
            final Cancellation cancellation,
            final HttpChain.FailureScope scope,
            final RuntimeException cause) {
        final HttpChain.FailureReason reason = cancellation.cancelled() ? HttpChain.FailureReason.CANCELLED
                : cause instanceof org.miaixz.bus.core.lang.exception.ProtocolException
                        ? HttpChain.FailureReason.PROTOCOL
                        : HttpChain.FailureReason.IO;
        return new HttpChain.ExchangeFailure(HttpChain.DeliveryState.MAYBE_PROCESSED, scope, reason, cause);
    }

    /**
     * HTTP/2 failures are stream-scoped unless the session owner reports a connection failure later.
     */
    private static HttpChain.FailureScope connectionScope(final HttpCodec codec) {
        return codec instanceof Http2Codec ? HttpChain.FailureScope.STREAM : HttpChain.FailureScope.CONNECTION;
    }

    /**
     * Normalizes a stage name.
     *
     * @param value value
     * @return normalized name
     */
    private static String normalizeName(final String value) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP transport name must be non-blank and single-line"));
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
