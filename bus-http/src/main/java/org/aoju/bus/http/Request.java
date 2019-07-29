package org.aoju.bus.http;

import org.aoju.bus.http.internal.Internal;
import org.aoju.bus.http.internal.http.HttpMethod;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTP request. Instances of this class are immutable if their {@link #body} is null or itself
 * immutable.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Request {

    final HttpUrl url;
    final String method;
    final Headers headers;
    final RequestBody body;
    final Map<Class<?>, Object> tags;

    private volatile CacheControl cacheControl; // Lazily initialized.

    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tags = Internal.immutableMap(builder.tags);
    }

    public HttpUrl url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Headers headers() {
        return headers;
    }

    public String header(String name) {
        return headers.get(name);
    }

    public List<String> headers(String name) {
        return headers.values(name);
    }

    public RequestBody body() {
        return body;
    }

    /**
     * Returns the tag attached with {@code Object.class} as a key, or null if no tag is attached with
     * that key.
     *
     * <p>Prior to HttpClient 3.11, this method never returned null if no tag was attached. Instead it
     * returned either this request, or the request upon which this request was derived with {@link
     * #newBuilder()}.
     */
    public Object tag() {
        return tag(Object.class);
    }

    /**
     * Returns the tag attached with {@code type} as a key, or null if no tag is attached with that
     * key.
     */
    public <T> T tag(Class<? extends T> type) {
        return type.cast(tags.get(type));
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Returns the cache control directives for this response. This is never null, even if this
     * response contains no {@code Cache-Control} header.
     */
    public CacheControl cacheControl() {
        CacheControl result = cacheControl;
        return result != null ? result : (cacheControl = CacheControl.parse(headers));
    }

    public boolean isHttps() {
        return url.isHttps();
    }

    @Override
    public String toString() {
        return "Request{method="
                + method
                + ", url="
                + url
                + ", tags="
                + tags
                + '}';
    }

    public static class Builder {
        HttpUrl url;
        String method;
        Headers.Builder headers;
        RequestBody body;

        /**
         * A mutable map of tags, or an immutable empty map if we don't have any.
         */
        Map<Class<?>, Object> tags = Collections.emptyMap();

        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tags = request.tags.isEmpty()
                    ? Collections.<Class<?>, Object>emptyMap()
                    : new LinkedHashMap<>(request.tags);
            this.headers = request.headers.newBuilder();
        }

        public Builder url(HttpUrl url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * Sets the URL target of this request.
         *
         * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
         *                                  exception by calling {@link HttpUrl#parse}; it returns null for invalid URLs.
         */
        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");

            // Silently replace web socket URLs with HTTP URLs.
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            return url(HttpUrl.get(url));
        }

        /**
         * Sets the URL target of this request.
         *
         * @throws IllegalArgumentException if the scheme of {@code url} is not {@code http} or {@code
         *                                  https}.
         */
        public Builder url(URL url) {
            if (url == null) throw new NullPointerException("url == null");
            return url(HttpUrl.get(url.toString()));
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
         * headers like "Cookie".
         *
         * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
         * HttpClient may replace {@code value} with a header derived from the request body.
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

        /**
         * Sets this request's {@code Cache-Control} header, replacing any cache control headers already
         * present. If {@code cacheControl} doesn't define any directives, this clears this request's
         * cache-control headers.
         */
        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) return removeHeader("Cache-Control");
            return header("Cache-Control", value);
        }

        public Builder get() {
            return method("GET", null);
        }

        public Builder head() {
            return method("HEAD", null);
        }

        public Builder post(RequestBody body) {
            return method("POST", body);
        }

        public Builder delete(RequestBody body) {
            return method("DELETE", body);
        }

        public Builder delete() {
            return delete(Internal.EMPTY_REQUEST);
        }

        public Builder put(RequestBody body) {
            return method("PUT", body);
        }

        public Builder patch(RequestBody body) {
            return method("PATCH", body);
        }

        public Builder method(String method, RequestBody body) {
            if (method == null) throw new NullPointerException("method == null");
            if (method.length() == 0) throw new IllegalArgumentException("method.length() == 0");
            if (body != null && !HttpMethod.permitsRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            }
            if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            }
            this.method = method;
            this.body = body;
            return this;
        }

        /**
         * Attaches {@code tag} to the request using {@code Object.class} as a key.
         */
        public Builder tag(Object tag) {
            return tag(Object.class, tag);
        }

        /**
         * Attaches {@code tag} to the request using {@code type} as a key. Tags can be read from a
         * request using {@link Request#tag}. Use null to remove any existing tag assigned for {@code
         * type}.
         *
         * <p>Use this API to attach timing, debugging, or other application data to a request so that
         * you may read it in interceptors, event listeners, or callbacks.
         */
        public <T> Builder tag(Class<? super T> type, T tag) {
            if (type == null) throw new NullPointerException("type == null");

            if (tag == null) {
                tags.remove(type);
            } else {
                if (tags.isEmpty()) tags = new LinkedHashMap<>();
                tags.put(type, type.cast(tag));
            }

            return this;
        }

        public Request build() {
            if (url == null) throw new IllegalStateException("url == null");
            return new Request(this);
        }
    }
}
