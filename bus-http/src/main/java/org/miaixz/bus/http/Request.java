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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.cache.CacheControl;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTP request, encapsulating all information for a single request, including the URL, method, headers, body, and
 * tags.
 * <p>
 * Note: Instances of this class are immutable if the {@link #body} is null or also immutable. The request body can,
 * however, affect the state of the instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Request {

    /**
     * The URL for this request.
     */
    final UnoUrl url;
    /**
     * The HTTP method (e.g., GET, POST).
     */
    final String method;
    /**
     * The request headers.
     */
    final Headers headers;
    /**
     * The request body, which may be null.
     */
    final RequestBody body;
    /**
     * A map of tags for attaching metadata.
     */
    final Map<Class<?>, Object> tags;
    /**
     * The cache control directives, lazily initialized.
     */
    private volatile CacheControl cacheControl;

    /**
     * Constructs a new {@code Request} instance from a builder.
     *
     * @param builder The builder instance containing all request properties.
     */
    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tags = org.miaixz.bus.http.Builder.immutableMap(builder.tags);
    }

    /**
     * Returns the URL for this request.
     *
     * @return The {@link UnoUrl} object.
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * Returns the HTTP method for this request.
     *
     * @return The method name (e.g., GET, POST).
     */
    public String method() {
        return method;
    }

    /**
     * Returns all headers for this request.
     *
     * @return The {@link Headers} object.
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the first header value for the given name.
     *
     * @param name The header name.
     * @return The header value, or null if not found.
     */
    public String header(String name) {
        return headers.get(name);
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
     * Returns the request body.
     *
     * @return The {@link RequestBody} object, which may be null.
     */
    public RequestBody body() {
        return body;
    }

    /**
     * Returns the tag attached to this request with {@code Object.class} as the key.
     * <p>
     * If no tag is attached, this returns null. To get a tag from a derived request, a new instance must be created
     * with {@link #newBuilder()}.
     * </p>
     *
     * @return The tag object, which may be null.
     */
    public Object tag() {
        return tag(Object.class);
    }

    /**
     * Returns the tag of the specified type attached to this request.
     * <p>
     * This uses the specified {@code type} as a key to look up a value from the tags map. The returned value will be an
     * instance of the specified type, or null if no tag is found.
     * </p>
     *
     * @param type The type of the tag.
     * @param <T>  The type of the tag value.
     * @return The tag value, which may be null.
     */
    public <T> T tag(Class<? extends T> type) {
        return type.cast(tags.get(type));
    }

    /**
     * Creates a new builder instance initialized with this request's properties.
     *
     * @return A new {@link Builder} instance.
     */
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Returns the cache control directives for this request.
     * <p>
     * This returns a non-null {@link CacheControl} object even if this request does not have a "Cache-Control" header.
     * Lazily initialized for performance.
     * </p>
     *
     * @return The {@link CacheControl} object.
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return null != result ? result : (cacheControl = CacheControl.parse(headers));
    }

    /**
     * Returns true if this request uses the HTTPS protocol.
     *
     * @return {@code true} if the URL uses HTTPS.
     */
    public boolean isHttps() {
        return url.isHttps();
    }

    /**
     * Returns a string representation of this request.
     *
     * @return A string containing the method, URL, and tags.
     */
    @Override
    public String toString() {
        return "Request{method=" + method + ", url=" + url + ", tags=" + tags + Symbol.C_BRACE_RIGHT;
    }

    /**
     * A builder for creating and modifying {@link Request} instances.
     */
    public static class Builder {

        /**
         * The URL for the request.
         */
        UnoUrl url;
        /**
         * The HTTP method.
         */
        String method;
        /**
         * The request headers builder.
         */
        Headers.Builder headers;
        /**
         * The request body.
         */
        RequestBody body;
        /**
         * A map of tags (mutable or empty).
         */
        Map<Class<?>, Object> tags = Collections.emptyMap();

        /**
         * Default constructor that initializes a GET request.
         */
        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        /**
         * Constructor that initializes the builder with an existing request.
         *
         * @param request The {@link Request} instance.
         */
        Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tags = request.tags.isEmpty() ? Collections.emptyMap() : new LinkedHashMap<>(request.tags);
            this.headers = request.headers.newBuilder();
        }

        /**
         * Sets the URL for this request.
         *
         * @param url The {@link UnoUrl} object.
         * @return this builder instance.
         * @throws NullPointerException if url is null.
         */
        public Builder url(UnoUrl url) {
            if (url == null)
                throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * Sets the URL for this request from a string.
         * <p>
         * This converts WebSocket URLs (ws: or wss:) to HTTP URLs (http: or https:).
         * </p>
         *
         * @param url The URL string.
         * @return this builder instance.
         * @throws NullPointerException     if url is null.
         * @throws IllegalArgumentException if the URL is invalid.
         */
        public Builder url(String url) {
            if (url == null)
                throw new NullPointerException("url == null");

            // Handle WebSocket URLs.
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            return url(UnoUrl.get(url));
        }

        /**
         * Sets the URL for this request from a {@link URL} object.
         *
         * @param url The {@link URL} object.
         * @return this builder instance.
         * @throws NullPointerException     if url is null.
         * @throws IllegalArgumentException if the URL scheme is not http or https.
         */
        public Builder url(URL url) {
            if (url == null)
                throw new NullPointerException("url == null");
            return url(UnoUrl.get(url.toString()));
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
         * <p>
         * For certain headers (like Content-Length, Content-Encoding), the HTTP client may replace the value based on
         * the request body.
         * </p>
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
         * Sets the cache control header.
         * <p>
         * This replaces any existing Cache-Control header. If {@code cacheControl} has no directives, the Cache-Control
         * header will be removed.
         * </p>
         *
         * @param cacheControl The {@link CacheControl} object.
         * @return this builder instance.
         */
        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty())
                return removeHeader(HTTP.CACHE_CONTROL);
            return header(HTTP.CACHE_CONTROL, value);
        }

        /**
         * Sets this request to be a GET request.
         *
         * @return this builder instance.
         */
        public Builder get() {
            return method(HTTP.GET, null);
        }

        /**
         * Sets this request to be a HEAD request.
         *
         * @return this builder instance.
         */
        public Builder head() {
            return method(HTTP.HEAD, null);
        }

        /**
         * Sets this request to be a POST request.
         *
         * @param body The request body.
         * @return this builder instance.
         */
        public Builder post(RequestBody body) {
            return method(HTTP.POST, body);
        }

        /**
         * Sets this request to be a DELETE request with a request body.
         *
         * @param body The request body.
         * @return this builder instance.
         */
        public Builder delete(RequestBody body) {
            return method(HTTP.DELETE, body);
        }

        /**
         * Sets this request to be a DELETE request with no request body.
         *
         * @return this builder instance.
         */
        public Builder delete() {
            return delete(RequestBody.of(null, Normal.EMPTY_BYTE_ARRAY));
        }

        /**
         * Sets this request to be a PUT request.
         *
         * @param body The request body.
         * @return this builder instance.
         */
        public Builder put(RequestBody body) {
            return method(HTTP.PUT, body);
        }

        /**
         * Sets this request to be a PATCH request.
         *
         * @param body The request body.
         * @return this builder instance.
         */
        public Builder patch(RequestBody body) {
            return method(HTTP.PATCH, body);
        }

        /**
         * Sets the HTTP method and request body.
         *
         * @param method The HTTP method.
         * @param body   The request body, which may be null.
         * @return this builder instance.
         * @throws NullPointerException     if method is null.
         * @throws IllegalArgumentException if method is empty or if the body is not compatible with the method.
         */
        public Builder method(String method, RequestBody body) {
            if (null == method)
                throw new NullPointerException("method == null");
            if (method.length() == 0)
                throw new IllegalArgumentException("method.length() == 0");
            if (body != null && !HTTP.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HTTP.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        /**
         * Attaches a tag to this request using {@code Object.class} as the key.
         *
         * @param tag The tag object.
         * @return this builder instance.
         */
        public Builder tag(Object tag) {
            return tag(Object.class, tag);
        }

        /**
         * Attaches a tag to this request using the specified type as the key.
         * <p>
         * Tags can be used to attach metadata for debugging, timing, or other purposes, and can be read in
         * interceptors, event listeners, or callbacks. Use null to remove an existing tag of the specified type.
         * </p>
         *
         * @param type The type of the tag.
         * @param tag  The tag value, which may be null.
         * @param <T>  The type of the tag value.
         * @return this builder instance.
         * @throws NullPointerException if type is null.
         */
        public <T> Builder tag(Class<? super T> type, T tag) {
            if (null == type)
                throw new NullPointerException("type == null");

            if (null == tag) {
                tags.remove(type);
            } else {
                if (tags.isEmpty())
                    tags = new LinkedHashMap<>();
                tags.put(type, type.cast(tag));
            }

            return this;
        }

        /**
         * Builds a new {@link Request} instance.
         *
         * @return A new {@link Request} object.
         * @throws IllegalStateException if the URL is not set.
         */
        public Request build() {
            if (null == url)
                throw new IllegalStateException("url == null");
            return new Request(this);
        }
    }

}
