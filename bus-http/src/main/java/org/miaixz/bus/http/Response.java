/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.cache.CacheControl;
import org.miaixz.bus.http.secure.Challenge;
import org.miaixz.bus.http.socket.Handshake;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * HTTP响应。该类的实例不是不可变的:
 * 响应体是一次性的值，可能只使用一次，然后关闭。所有其他属性都是不可变的.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Response implements Closeable {

    final Request request;
    final Protocol protocol;
    final int code;
    final String message;
    final Handshake handshake;
    final Headers headers;
    final ResponseBody body;
    final Response networkResponse;
    final Response cacheResponse;
    final Response priorResponse;
    final long sentRequestAtMillis;
    final long receivedResponseAtMillis;
    final Exchange exchange;

    private volatile CacheControl cacheControl;

    Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
        this.exchange = builder.exchange;
    }

    /**
     * The wire-level request that initiated this HTTP response. This is not necessarily the same
     * request issued by the application:
     *
     * <ul>
     *     <li>It may be transformed by the HTTP client. For example, the client may copy headers like
     *         {@code Content-Length} from the request body.
     *     <li>It may be the request generated in response to an HTTP redirect or authentication
     *         challenge. In this case the request URL may be different than the initial request URL.
     * </ul>
     */
    public Request request() {
        return request;
    }

    /**
     * Returns the HTTP protocol, such as {@link Protocol#HTTP_1_1} or {@link Protocol#HTTP_1_0}.
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the HTTP status code.
     */
    public int code() {
        return code;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was successfully received,
     * understood, and accepted.
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * Returns the HTTP status message.
     */
    public String message() {
        return message;
    }

    /**
     * Returns the TLS handshake of the connection that carried this response, or null if the response
     * was received without TLS.
     */
    public Handshake handshake() {
        return handshake;
    }

    public List<String> headers(String name) {
        return headers.values(name);
    }

    public String header(String name) {
        return header(name, null);
    }

    public String header(String name, String defaultValue) {
        String result = headers.get(name);
        return null != result ? result : defaultValue;
    }

    public Headers headers() {
        return headers;
    }

    /**
     * Returns the trailers after the HTTP response, which may be empty. It is an error to call this
     * before the entire HTTP response body has been consumed.
     */
    public Headers trailers() throws IOException {
        if (exchange == null) throw new IllegalStateException("trailers not available");
        return exchange.trailers();
    }

    /**
     * Peeks up to {@code byteCount} bytes from the response body and returns them as a new response
     * body. If fewer than {@code byteCount} bytes are in the response body, the full response body is
     * returned. If more than {@code byteCount} bytes are in the response body, the returned value
     * will be truncated to {@code byteCount} bytes.
     * <p>
     * It is an error to call this method after the body has been consumed.
     *
     * <strong>Warning:</strong> this method loads the requested bytes into memory. Most
     * applications should set a modest limit on {@code byteCount}, such as 1 MiB.
     */
    public ResponseBody peekBody(long byteCount) throws IOException {
        BufferSource peeked = body.source().peek();
        Buffer buffer = new Buffer();
        peeked.request(byteCount);
        buffer.write(peeked, Math.min(byteCount, peeked.getBuffer().size()));
        return ResponseBody.create(body.mediaType(), buffer.size(), buffer);
    }

    /**
     * Returns a non-null value if this response was passed to {@link Callback#onResponse} or returned
     * from {@link NewCall#execute()}. Response bodies must be {@linkplain ResponseBody closed} and may
     * be consumed only once.
     * <p>
     * This always returns null on responses returned from {@link #cacheResponse}, {@link
     * #networkResponse}, and {@link #priorResponse()}.
     */
    public ResponseBody body() {
        return body;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Returns true if this response redirects to another resource.
     */
    public boolean isRedirect() {
        switch (code) {
            case Http.HTTP_PERM_REDIRECT:
            case Http.HTTP_TEMP_REDIRECT:
            case Http.HTTP_MULT_CHOICE:
            case Http.HTTP_MOVED_PERM:
            case Http.HTTP_MOVED_TEMP:
            case Http.HTTP_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the raw response received from the network. Will be null if this response didn't use
     * the network, such as when the response is fully cached. The body of the returned response
     * should not be read.
     */
    public Response networkResponse() {
        return networkResponse;
    }

    /**
     * Returns the raw response received from the cache. Will be null if this response didn't use the
     * cache. For conditional get requests the cache response and network response may both be
     * non-null. The body of the returned response should not be read.
     */
    public Response cacheResponse() {
        return cacheResponse;
    }

    /**
     * Returns the response for the HTTP redirect or authorization challenge that triggered this
     * response, or null if this response wasn't triggered by an automatic retry. The body of the
     * returned response should not be read because it has already been consumed by the redirecting
     * client.
     */
    public Response priorResponse() {
        return priorResponse;
    }

    /**
     * Returns the RFC 7235 authorization challenges appropriate for this response's code. If the
     * response code is 401 unauthorized, this returns the "WWW-Authenticate" challenges. If the
     * response code is 407 proxy unauthorized, this returns the "Proxy-Authenticate" challenges.
     * Otherwise this returns an empty list of challenges.
     * <p>
     * If a challenge uses the {@code token68} variant instead of auth params, there is exactly one
     * auth param in the challenge at key {@code null}. Invalid headers and challenges are ignored.
     * No semantic validation is done, for example that {@code Basic} auth must have a {@code realm}
     * auth param, this is up to the caller that interprets these challenges.
     */
    public List<Challenge> challenges() {
        String responseField;
        if (code == Http.HTTP_UNAUTHORIZED) {
            responseField = Header.WWW_AUTHENTICATE;
        } else if (code == Http.HTTP_PROXY_AUTH) {
            responseField = Header.PROXY_AUTHENTICATE;
        } else {
            return Collections.emptyList();
        }
        return Headers.parseChallenges(headers(), responseField);
    }

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers));
    }

    /**
     * Returns a {@linkplain System#currentTimeMillis() timestamp} taken immediately before Http
     * transmitted the initiating request over the network. If this response is being served from the
     * cache then this is the timestamp of the original request.
     */
    public long sentRequestAtMillis() {
        return sentRequestAtMillis;
    }

    /**
     * Returns a {@linkplain System#currentTimeMillis() timestamp} taken immediately after Http
     * received this response's headers from the network. If this response is being served from the
     * cache then this is the timestamp of the original response.
     */
    public long receivedResponseAtMillis() {
        return receivedResponseAtMillis;
    }

    /**
     * Closes the response body. Equivalent to {@code body().close()}.
     * <p>
     * It is an error to close a response that is not eligible for a body. This includes the
     * responses returned from {@link #cacheResponse}, {@link #networkResponse}, and {@link
     * #priorResponse()}.
     */
    @Override
    public void close() {
        if (null == body) {
            throw new IllegalStateException("response is not eligible for a body and must not be closed");
        }
        body.close();
    }

    @Override
    public String toString() {
        return "Response{protocol="
                + protocol
                + ", code="
                + code
                + ", message="
                + message
                + ", url="
                + request.url()
                + Symbol.C_BRACE_RIGHT;
    }

    public static class Builder {
        Request request;
        Protocol protocol;
        int code = -1;
        String message;
        Handshake handshake;
        Headers.Builder headers;
        ResponseBody body;
        Response networkResponse;
        Response cacheResponse;
        Response priorResponse;
        long sentRequestAtMillis;
        long receivedResponseAtMillis;
        Exchange exchange;

        public Builder() {
            headers = new Headers.Builder();
        }

        Builder(Response response) {
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
            this.networkResponse = response.networkResponse;
            this.cacheResponse = response.cacheResponse;
            this.priorResponse = response.priorResponse;
            this.sentRequestAtMillis = response.sentRequestAtMillis;
            this.receivedResponseAtMillis = response.receivedResponseAtMillis;
            this.exchange = response.exchange;
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder handshake(Handshake handshake) {
            this.handshake = handshake;
            return this;
        }

        /**
         * Sets the header named {@code name} to {@code value}. If this request already has any headers
         * with that name, they are all replaced.
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
         * headers like "Set-Cookie".
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Removes all headers named {@code name} on this builder.
         */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * Removes all headers on this builder and adds {@code headers}.
         */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        public Builder body(ResponseBody body) {
            this.body = body;
            return this;
        }

        public Builder networkResponse(Response networkResponse) {
            if (networkResponse != null) checkSupportResponse("networkResponse", networkResponse);
            this.networkResponse = networkResponse;
            return this;
        }

        public Builder cacheResponse(Response cacheResponse) {
            if (cacheResponse != null) checkSupportResponse("cacheResponse", cacheResponse);
            this.cacheResponse = cacheResponse;
            return this;
        }

        private void checkSupportResponse(String name, Response response) {
            if (null != response.body) {
                throw new IllegalArgumentException(name + ".body != null");
            } else if (null != response.networkResponse) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            } else if (null != response.cacheResponse) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            } else if (null != response.priorResponse) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }

        public Builder priorResponse(Response priorResponse) {
            if (null != priorResponse) {
                checkPriorResponse(priorResponse);
            }
            this.priorResponse = priorResponse;
            return this;
        }

        private void checkPriorResponse(Response response) {
            if (null != response.body) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        public Builder sentRequestAtMillis(long sentRequestAtMillis) {
            this.sentRequestAtMillis = sentRequestAtMillis;
            return this;
        }

        public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
            this.receivedResponseAtMillis = receivedResponseAtMillis;
            return this;
        }

        void initExchange(Exchange deferredTrailers) {
            this.exchange = deferredTrailers;
        }

        public Response build() {
            if (null == request) {
                throw new IllegalStateException("request == null");
            }
            if (null == protocol) {
                throw new IllegalStateException("protocol == null");
            }
            if (code < 0) {
                throw new IllegalStateException("code < 0: " + code);
            }
            if (null == message) {
                throw new IllegalStateException("message == null");
            }
            return new Response(this);
        }
    }

}
