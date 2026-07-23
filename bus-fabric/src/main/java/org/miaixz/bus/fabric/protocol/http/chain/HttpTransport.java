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
     * Shared unregister action for synchronous scopes without an external cancellation handle.
     */
    private static final Runnable NOOP_UNREGISTER = () -> {
    };

    /**
     * Shared stateless default codec factory.
     */
    private static final Function<HttpChain, HttpCodec> DEFAULT_CODECS = HttpTransport::networkCodec;

    /**
     * Normalized identifier exposed to the HTTP stage chain.
     */
    private final String name;

    /**
     * Factory that selects or creates the protocol codec for the current chain connection.
     */
    private final Function<HttpChain, HttpCodec> codecs;

    /**
     * Creates a transport stage that owns any dispatcher needed by an HTTP/2 session.
     */
    public HttpTransport() {
        this(DEFAULT_CODECS);
    }

    /**
     * Creates a transport stage with shared dispatcher.
     *
     * @param dispatcher shared dispatcher used when an HTTP/2 session must be created
     * @throws ValidateException if {@code dispatcher} is {@code null}
     */
    public HttpTransport(final Dispatcher dispatcher) {
        this(chain -> networkCodec(chain, require(dispatcher, "Dispatcher")));
    }

    /**
     * Creates a transport stage with a codec factory.
     *
     * @param codecs chain-aware codec factory used for each exchange
     * @throws ValidateException if {@code codecs} is {@code null}
     */
    HttpTransport(final Function<HttpChain, HttpCodec> codecs) {
        this.name = normalizeName("http-transport");
        this.codecs = require(codecs, "HTTP codec factory");
    }

    /**
     * Writes a request and reads the response through the chain connection.
     *
     * @param request request to serialize on the selected connection
     * @param chain   exchange chain containing cancellation state and a network connection
     * @return response with request-sent and response-received timestamps
     * @throws HttpChain.ExchangeFailure if request writing or response reading fails
     * @throws ValidateException         if the request, chain, or produced codec is {@code null}
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain context = require(chain, "HTTP chain");
        final Cancellation cancellation = context.cancellation();
        cancellation.throwIfCancelled();
        final HttpCodec codec = require(codecs.apply(context), "HTTP codec");
        final Runnable unregister = cancellation.cancellable() ? cancellation.onCancel(codec::cancel) : NOOP_UNREGISTER;
        try {
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
            return response.withTiming(sentRequestAtMillis, receivedResponseAtMillis);
        } finally {
            unregister.run();
        }
    }

    /**
     * Writes a request through a codec.
     *
     * @param request request to serialize
     * @param codec   active protocol codec that writes the request
     * @throws ValidateException if the request or codec is {@code null}
     */
    public void writeRequest(final HttpRequest request, final HttpCodec codec) {
        require(codec, "HTTP codec").writeRequest(require(request, "HTTP request"));
    }

    /**
     * Reads a response through a codec.
     *
     * @param request request whose response is expected
     * @param codec   active protocol codec that reads the response
     * @return response decoded by the codec
     * @throws ValidateException if the request or codec is {@code null}
     */
    public HttpResponse readResponse(final HttpRequest request, final HttpCodec codec) {
        return require(codec, "HTTP codec").readResponse(require(request, "HTTP request"));
    }

    /**
     * Returns stage name.
     *
     * @return normalized transport-stage identifier
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Creates a network codec from the current chain.
     *
     * @param chain chain whose selected physical connection is adapted
     * @return HTTP/1 codec or HTTP/2 stream codec matching the connection protocol
     * @throws StatefulException if the chain has no selected network connection
     */
    private static HttpCodec networkCodec(final HttpChain chain) {
        final Connection connection = Assert.notNull(
                chain.connection(),
                () -> new StatefulException("HTTP chain does not contain a network connection"));
        if (http2(connection)) {
            return new Http2Codec(http2Session(connection, null));
        }
        return http1Codec(connection);
    }

    /**
     * Creates a network codec from the current chain.
     *
     * @param chain      chain whose selected physical connection is adapted
     * @param dispatcher shared dispatcher used for a newly initialized HTTP/2 session
     * @return HTTP/1 codec or HTTP/2 stream codec matching the connection protocol
     * @throws StatefulException if the chain has no selected network connection
     */
    private static HttpCodec networkCodec(final HttpChain chain, final Dispatcher dispatcher) {
        final Connection connection = Assert.notNull(
                chain.connection(),
                () -> new StatefulException("HTTP chain does not contain a network connection"));
        if (http2(connection)) {
            return new Http2Codec(http2Session(connection, dispatcher));
        }
        return http1Codec(connection);
    }

    /**
     * Returns the reusable HTTP/1.1 codec owned by an exclusively leased physical connection.
     *
     * @param connection exclusively leased HTTP/1.1 connection
     * @return connection-local codec, or a standalone codec when the connection does not support attachments
     */
    private static Http1Codec http1Codec(final Connection connection) {
        final Object current = connection.protocolAttachment();
        if (current instanceof Http1Codec codec) {
            return codec;
        }
        if (current != null) {
            throw new StatefulException("HTTP/1.1 connection contains an incompatible protocol attachment");
        }
        final Http1Codec created = new Http1Codec(connection);
        if (connection.compareAndSetProtocolAttachment(null, created)) {
            return created;
        }
        final Object installed = connection.protocolAttachment();
        if (installed instanceof Http1Codec codec) {
            return codec;
        }
        return created;
    }

    /**
     * Returns whether the selected connection is HTTP/2.
     *
     * @param connection selected physical connection
     * @return {@code true} for negotiated HTTP/2 or prior-knowledge HTTP/2
     */
    private static boolean http2(final Connection connection) {
        final Protocol protocol = require(connection, "Network connection").protocol();
        return protocol == Protocol.HTTP_2 || protocol == Protocol.H2_PRIOR_KNOWLEDGE;
    }

    /**
     * Atomically initializes and reuses one HTTP/2 connection session per physical connection.
     *
     * @param connection physical HTTP/2 connection
     * @param dispatcher shared runtime dispatcher, or {@code null} for an owned dispatcher
     * @return connection-local HTTP/2 session
     * @throws StatefulException if multiplex state is missing, incompatible, or changes non-atomically
     */
    private static Http2Connection http2Session(final Connection connection, final Dispatcher dispatcher) {
        final Connection.MultiplexAttachment attachment = Assert.notNull(
                connection.multiplexAttachment(),
                () -> new StatefulException("HTTP/2 connection does not expose a multiplex attachment"));
        final Object current = attachment.session();
        if (current != null) {
            if (current instanceof Http2Connection session) {
                return session;
            }
            throw new StatefulException("HTTP/2 multiplex attachment contains an incompatible session");
        }
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
     *
     * @param cancellation cancellation scope used to classify cancellation
     * @param scope        transport resource scope affected by the failure
     * @param cause        original runtime failure
     * @return structured exchange failure
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
     *
     * @param codec active HTTP codec
     * @return conservative failure scope for the codec
     */
    private static HttpChain.FailureScope connectionScope(final HttpCodec codec) {
        return codec instanceof Http2Codec ? HttpChain.FailureScope.STREAM : HttpChain.FailureScope.CONNECTION;
    }

    /**
     * Normalizes a stage name.
     *
     * @param value stage identifier to validate and normalize
     * @return trimmed lowercase stage identifier
     * @throws ValidateException if the identifier is blank or contains a line break
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
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
