/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
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
 * An HTTP response, encapsulating all information from the server, including the request, protocol, status code,
 * headers, and response body.
 * <p>
 * Note: All properties of this class are immutable except for the response body. The response body is a one-shot
 * resource that must be closed after reading and can only be consumed once.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Response implements Closeable {

    /**
     * The original request that initiated this response.
     */
    final Request request;
    /**
     * The protocol used for the response (e.g., HTTP/1.1, HTTP/2).
     */
    final Protocol protocol;
    /**
     * The HTTP status code.
     */
    final int code;
    /**
     * The HTTP status message.
     */
    final String message;
    /**
     * The TLS handshake information, or null for non-TLS connections.
     */
    final Handshake handshake;
    /**
     * The response headers.
     */
    final Headers headers;
    /**
     * The response body, which may be null (e.g., for HEAD requests).
     */
    final ResponseBody body;
    /**
     * The network response, obtained directly from the network (not from cache).
     */
    final Response networkResponse;
    /**
     * The cache response, obtained from the cache.
     */
    final Response cacheResponse;
    /**
     * The prior response, if this response was triggered by a redirect or authentication.
     */
    final Response priorResponse;
    /**
     * The timestamp when the request was sent, in milliseconds.
     */
    final long sentRequestAtMillis;
    /**
     * The timestamp when the response headers were received, in milliseconds.
     */
    final long receivedResponseAtMillis;
    /**
     * The exchange object that manages the request and response transmission.
     */
    final Exchange exchange;
    /**
     * The cache control directives, lazily initialized.
     */
    private volatile CacheControl cacheControl;

    /**
     * Constructs a new {@code Response} instance from a builder.
     *
     * @param builder The builder instance containing all response properties.
     */
    Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build(); // Build immutable headers.
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
        this.exchange = builder.exchange;
    }

    /**
     * Returns the original request that initiated this response.
     * <p>
     * Note: This request may differ from the one issued by the application if it was modified by the HTTP client (e.g.,
     * by adding a Content-Length header) or if a new request was generated due to a redirect or authentication (the URL
     * may be different).
     * </p>
     *
     * @return The {@link Request} object that initiated the response.
     */
    public Request request() {
        return request;
    }

    /**
     * Returns the HTTP protocol that was used.
     *
     * @return The protocol, such as HTTP/1.1 or HTTP/2.
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return The status code (e.g., 200, 404).
     */
    public int code() {
        return code;
    }

    /**
     * Returns whether the request was successful (status code in the range [200..300)).
     *
     * @return {@code true} if the request was successfully received, understood, and accepted.
     */
    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * Returns the HTTP status message.
     *
     * @return The status message (e.g., "OK", "Not Found").
     */
    public String message() {
        return message;
    }

    /**
     * Returns the TLS handshake information.
     *
     * @return The {@link Handshake} object, or null for non-TLS connections.
     */
    public Handshake handshake() {
        return handshake;
    }

    /**
     * Returns a list of header values for the given name.
     *
     * @param name The header name.
     * @return A list of header values, which may be empty.
     */
    public List<String> headers(String name) {
        return headers.values(name);
    }

    /**
     * Returns the first header value for the given name.
     *
     * @param name The header name.
     * @return The header value, or null if not found.
     */
    public String header(String name) {
        return header(name, null);
    }

    /**
     * Returns the first header value for the given name, with a default value.
     *
     * @param name         The header name.
     * @param defaultValue The default value to return if the header is not found.
     * @return The header value or the default value.
     */
    public String header(String name, String defaultValue) {
        String result = headers.get(name);
        return null != result ? result : defaultValue;
    }

    /**
     * Returns all response headers.
     *
     * @return The {@link Headers} object.
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the trailer headers of the response.
     * <p>
     * Note: This must be called after the response body has been fully consumed, otherwise an
     * {@link IllegalStateException} will be thrown.
     * </p>
     *
     * @return The trailer {@link Headers} object, which may be empty.
     * @throws IOException           if the trailers could not be retrieved.
     * @throws IllegalStateException if the response body has not been fully consumed.
     */
    public Headers trailers() throws IOException {
        if (exchange == null)
            throw new IllegalStateException("trailers not available");
        return exchange.trailers(); // Get trailers from the exchange object.
    }

    /**
     * Peeks at the response body content, up to a specified number of bytes.
     * <p>
     * Returns a new {@link ResponseBody} containing up to {@code byteCount} bytes of the response body. If the body is
     * smaller than {@code byteCount}, the entire content is returned; otherwise, it is truncated.
     * </p>
     * <p>
     * Note: This method loads the requested bytes into memory, so it is recommended to set a reasonable
     * {@code byteCount} (e.g., 1MB). Calling this after the response body has been consumed will result in an error.
     * </p>
     *
     * @param byteCount The maximum number of bytes to peek.
     * @return A new {@link ResponseBody} object.
     * @throws IOException if reading the response body fails.
     */
    public ResponseBody peekBody(long byteCount) throws IOException {
        BufferSource peeked = body.source().peek();
        Buffer buffer = new Buffer();
        peeked.request(byteCount);
        buffer.write(peeked, Math.min(byteCount, peeked.getBuffer().size())); // Copy up to byteCount bytes.
        return ResponseBody.of(body.contentType(), buffer.size(), buffer);
    }

    /**
     * Returns the response body.
     * <p>
     * Note: The response body is a one-shot resource that must be closed and can only be consumed once. For responses
     * returned by {@link #cacheResponse}, {@link #networkResponse}, and {@link #priorResponse}, this method returns
     * null.
     * </p>
     *
     * @return The {@link ResponseBody} object, which may be null.
     */
    public ResponseBody body() {
        return body;
    }

    /**
     * Creates a new builder instance initialized with this response's properties.
     *
     * @return A new {@link Builder} instance.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Returns whether this response is a redirect.
     *
     * @return {@code true} if the status code indicates a redirect (300, 301, 302, 303, 307, 308).
     */
    public boolean isRedirect() {
        switch (code) {
            case HTTP.HTTP_PERM_REDIRECT: // 308
            case HTTP.HTTP_TEMP_REDIRECT: // 307
            case HTTP.HTTP_MULT_CHOICE: // 300
            case HTTP.HTTP_MOVED_PERM: // 301
            case HTTP.HTTP_MOVED_TEMP: // 302
            case HTTP.HTTP_SEE_OTHER: // 303
                return true;

            default:
                return false;
        }
    }

    /**
     * Returns the network response.
     * <p>
     * If the response was served directly from the network (not from cache), this returns the original response;
     * otherwise, it returns null. The body of the returned response should not be read.
     * </p>
     *
     * @return The network {@link Response} object, which may be null.
     */
    public Response networkResponse() {
        return networkResponse;
    }

    /**
     * Returns the cache response.
     * <p>
     * If the response was served from the cache, this returns the cached response; otherwise, it returns null. For
     * conditional GET requests, both a cache and network response may be present. The body of the returned response
     * should not be read.
     * </p>
     *
     * @return The cached {@link Response} object, which may be null.
     */
    public Response cacheResponse() {
        return cacheResponse;
    }

    /**
     * Returns the prior response.
     * <p>
     * If this response was triggered by a redirect or authentication challenge, this returns the prior response;
     * otherwise, it returns null. The body of the returned response should not be read (it has already been consumed).
     * </p>
     *
     * @return The prior {@link Response} object, which may be null.
     */
    public Response priorResponse() {
        return priorResponse;
    }

    /**
     * Returns a list of RFC 7235 authentication challenges.
     * <p>
     * For a 401 (Unauthorized) status code, this returns "WWW-Authenticate" challenges. For a 407 (Proxy Authentication
     * Required) status code, it returns "Proxy-Authenticate" challenges. Other status codes return an empty list.
     * </p>
     *
     * @return A list of authentication challenges, which may be empty.
     */
    public List<Challenge> challenges() {
        String responseField;
        if (code == HTTP.HTTP_UNAUTHORIZED) {
            responseField = HTTP.WWW_AUTHENTICATE; // 401 uses WWW-Authenticate.
        } else if (code == HTTP.HTTP_PROXY_AUTH) {
            responseField = HTTP.PROXY_AUTHENTICATE; // 407 uses Proxy-Authenticate.
        } else {
            return Collections.emptyList(); // No challenges for other status codes.
        }
        return Headers.parseChallenges(headers(), responseField); // Parse challenge headers.
    }

    /**
     * Returns the cache control directives.
     * <p>
     * This returns a non-null {@link CacheControl} object even if the response does not have a "Cache-Control" header.
     * Lazily initialized for performance.
     * </p>
     *
     * @return The {@link CacheControl} object.
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers)); // Lazily parse cache control.
    }

    /**
     * Returns the timestamp when the request was sent.
     *
     * @return The timestamp in milliseconds (from System.currentTimeMillis()).
     */
    public long sentRequestAtMillis() {
        return sentRequestAtMillis;
    }

    /**
     * Returns the timestamp when the response headers were received.
     *
     * @return The timestamp in milliseconds (from System.currentTimeMillis()).
     */
    public long receivedResponseAtMillis() {
        return receivedResponseAtMillis;
    }

    /**
     * Closes the response body.
     * <p>
     * This is equivalent to calling {@code body().close()}. For responses without a body (like those from
     * {@link #cacheResponse}, {@link #networkResponse}, or {@link #priorResponse}), this will throw an exception.
     * </p>
     *
     * @throws IllegalStateException if the response does not have a body.
     */
    @Override
    public void close() {
        if (null == body) {
            throw new IllegalStateException("response is not eligible for a body and must not be closed");
        }
        body.close(); // Close the response body.
    }

    /**
     * Returns a string representation of this response.
     *
     * @return A string containing the protocol, status code, message, and URL.
     */
    @Override
    public String toString() {
        return "Response{protocol=" + protocol + ", code=" + code + ", message=" + message + ", url=" + request.url()
                + Symbol.C_BRACE_RIGHT;
    }

    /**
     * A builder for creating and modifying {@link Response} instances.
     */
    public static class Builder {

        /**
         * The request that initiated this response.
         */
        Request request;
        /**
         * The protocol used for the response.
         */
        Protocol protocol;
        /**
         * The HTTP status code.
         */
        int code = -1;
        /**
         * The HTTP status message.
         */
        String message;
        /**
         * The TLS handshake information.
         */
        Handshake handshake;
        /**
         * The response headers builder.
         */
        Headers.Builder headers;
        /**
         * The response body.
         */
        ResponseBody body;
        /**
         * The network response.
         */
        Response networkResponse;
        /**
         * The cache response.
         */
        Response cacheResponse;
        /**
         * The prior response.
         */
        Response priorResponse;
        /**
         * The timestamp when the request was sent.
         */
        long sentRequestAtMillis;
        /**
         * The timestamp when the response was received.
         */
        long receivedResponseAtMillis;
        /**
         * The exchange object.
         */
        Exchange exchange;

        /**
         * Default constructor that initializes an empty builder.
         */
        public Builder() {
            headers = new Headers.Builder();
        }

        /**
         * Constructor that initializes the builder with an existing response.
         *
         * @param response The {@link Response} instance.
         */
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

        /**
         * Sets the request that initiated this response.
         *
         * @param request The {@link Request} object.
         * @return this builder instance.
         */
        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        /**
         * Sets the protocol used.
         *
         * @param protocol The protocol (e.g., HTTP/1.1, HTTP/2).
         * @return this builder instance.
         */
        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets the HTTP status code.
         *
         * @param code The status code.
         * @return this builder instance.
         */
        public Builder code(int code) {
            this.code = code;
            return this;
        }

        /**
         * Sets the HTTP status message.
         *
         * @param message The status message.
         * @return this builder instance.
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the TLS handshake information.
         *
         * @param handshake The {@link Handshake} object, which may be null.
         * @return this builder instance.
         */
        public Builder handshake(Handshake handshake) {
            this.handshake = handshake;
            return this;
        }

        /**
         * Sets a header, replacing any existing headers with the same name.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return this builder instance.
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header, preserving any existing headers with the same name.
         *
         * @param name  The header name.
         * @param value The header value.
         * @return this builder instance.
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        /**
         * Removes all headers with the given name.
         *
         * @param name The header name.
         * @return this builder instance.
         */
        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * Sets all headers, replacing any existing headers.
         *
         * @param headers The {@link Headers} object.
         * @return this builder instance.
         */
        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
            return this;
        }

        /**
         * Sets the response body.
         *
         * @param body The {@link ResponseBody} object, which may be null.
         * @return this builder instance.
         */
        public Builder body(ResponseBody body) {
            this.body = body;
            return this;
        }

        /**
         * Sets the network response.
         *
         * @param networkResponse The network {@link Response} object, which may be null.
         * @return this builder instance.
         * @throws IllegalArgumentException if {@code networkResponse} has invalid properties.
         */
        public Builder networkResponse(Response networkResponse) {
            if (networkResponse != null)
                checkSupportResponse("networkResponse", networkResponse);
            this.networkResponse = networkResponse;
            return this;
        }

        /**
         * Sets the cache response.
         *
         * @param cacheResponse The cached {@link Response} object, which may be null.
         * @return this builder instance.
         * @throws IllegalArgumentException if {@code cacheResponse} has invalid properties.
         */
        public Builder cacheResponse(Response cacheResponse) {
            if (cacheResponse != null)
                checkSupportResponse("cacheResponse", cacheResponse);
            this.cacheResponse = cacheResponse;
            return this;
        }

        /**
         * Validates a support response (networkResponse or cacheResponse).
         *
         * @param name     The name of the response (for error messages).
         * @param response The {@link Response} object.
         * @throws IllegalArgumentException if the response contains a body, networkResponse, cacheResponse, or
         *                                  priorResponse.
         */
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

        /**
         * Sets the prior response.
         *
         * @param priorResponse The prior {@link Response} object, which may be null.
         * @return this builder instance.
         * @throws IllegalArgumentException if {@code priorResponse} contains a body.
         */
        public Builder priorResponse(Response priorResponse) {
            if (null != priorResponse) {
                checkPriorResponse(priorResponse);
            }
            this.priorResponse = priorResponse;
            return this;
        }

        /**
         * Validates the prior response.
         *
         * @param response The {@link Response} object.
         * @throws IllegalArgumentException if the response contains a body.
         */
        private void checkPriorResponse(Response response) {
            if (null != response.body) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        /**
         * Sets the timestamp when the request was sent.
         *
         * @param sentRequestAtMillis The timestamp in milliseconds.
         * @return this builder instance.
         */
        public Builder sentRequestAtMillis(long sentRequestAtMillis) {
            this.sentRequestAtMillis = sentRequestAtMillis;
            return this;
        }

        /**
         * Sets the timestamp when the response headers were received.
         *
         * @param receivedResponseAtMillis The timestamp in milliseconds.
         * @return this builder instance.
         */
        public Builder receivedResponseAtMillis(long receivedResponseAtMillis) {
            this.receivedResponseAtMillis = receivedResponseAtMillis;
            return this;
        }

        /**
         * Initializes the exchange object.
         *
         * @param deferredTrailers The {@link Exchange} object.
         */
        void initExchange(Exchange deferredTrailers) {
            this.exchange = deferredTrailers;
        }

        /**
         * Builds a new {@link Response} instance.
         *
         * @return A new {@link Response} object.
         * @throws IllegalStateException if request, protocol, or message is null, or if code is less than 0.
         */
        public Response build() {
            if (null == request)
                throw new IllegalStateException("request == null");
            if (null == protocol)
                throw new IllegalStateException("protocol == null");
            if (code < 0)
                throw new IllegalStateException("code < 0: " + code);
            if (null == message)
                throw new IllegalStateException("message == null");
            return new Response(this);
        }
    }

}
