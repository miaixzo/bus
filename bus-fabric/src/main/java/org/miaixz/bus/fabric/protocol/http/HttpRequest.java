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
package org.miaixz.bus.fabric.protocol.http;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.codec.body.RequestBody;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.protocol.http.agent.UserAgent;
import org.miaixz.bus.fabric.protocol.http.body.FileBody;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.body.TextBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCacheControl;

/**
 * Immutable HTTP request snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpRequest {

    /**
     * HTTP method.
     */
    private final HTTP.Method method;

    /**
     * Request URL.
     */
    private final UnoUrl url;

    /**
     * Header snapshot.
     */
    private final Headers headers;

    /**
     * Payload body.
     */
    private final PayloadBody body;

    /**
     * Request tags keyed by type.
     */
    private final Map<Class<?>, Object> tags;

    /**
     * Proxy plan.
     */
    private final ProxyPlan proxy;

    /**
     * Timeout policy.
     */
    private final Timeout timeout;

    /**
     * Lazily parsed cache control snapshot.
     */
    private volatile HttpCacheControl cacheControl;

    /**
     * Lazily encoded request authority.
     */
    private volatile String authority;

    /**
     * Lazily encoded origin-form request target.
     */
    private volatile String requestTarget;

    /**
     * Stable body length used by codecs and retry policy.
     */
    private final long bodyLength;

    /**
     * Stable replay eligibility used by retry policy.
     */
    private final boolean replayable;

    /**
     * Creates an HTTP request.
     *
     * @param method  method
     * @param url     URL
     * @param headers headers
     * @param body    body
     * @param tag     tag
     * @param proxy   proxy plan
     * @param timeout timeout
     */
    private HttpRequest(final HTTP.Method method, final UnoUrl url, final Headers headers, final PayloadBody body,
            final Map<Class<?>, Object> tags, final ProxyPlan proxy, final Timeout timeout) {
        this(method, url, headers, body, tags, false, proxy, timeout);
    }

    /**
     * Creates a request, optionally accepting an immutable tag snapshot from another request.
     */
    private HttpRequest(final HTTP.Method method, final UnoUrl url, final Headers headers, final PayloadBody body,
            final Map<Class<?>, Object> tags, final boolean trustedTags, final ProxyPlan proxy, final Timeout timeout) {
        this.method = require(method, "HTTP method");
        this.url = require(url, "URL");
        this.headers = require(headers, "Headers");
        this.body = require(body, "Body");
        this.tags = trustedTags ? tags : immutableTags(tags);
        this.proxy = require(proxy, "Proxy plan");
        this.timeout = require(timeout, "Timeout");
        this.bodyLength = this.body.length();
        this.replayable = this.body.repeatable();
        validateBodyPolicy(this.method, this.body);
    }

    /**
     * Creates an HTTP request builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with this request snapshot.
     *
     * @return builder
     */
    public Builder toBuilder() {
        return newBuilder();
    }

    /**
     * Returns a builder initialized with this request snapshot.
     *
     * @return builder
     */
    public Builder newBuilder() {
        final Builder builder = builder().method(method).url(url).headers(headers).body(body).proxy(proxy)
                .timeout(timeout);
        builder.tags = tags;
        builder.tagsSnapshot = true;
        return builder;
    }

    /**
     * Returns the HTTP method.
     *
     * @return HTTP method
     */
    public HTTP.Method method() {
        return method;
    }

    /**
     * Returns the request URL.
     *
     * @return URL
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * Returns headers.
     *
     * @return headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the parsed User-Agent header.
     *
     * @return parsed User-Agent, or null when absent or blank
     */
    public UserAgent userAgent() {
        return HttpHeaders.userAgent(headers);
    }

    /**
     * Returns body.
     *
     * @return body
     */
    public PayloadBody body() {
        return body;
    }

    /**
     * Returns the stable method text consumed by HTTP codecs.
     *
     * @return canonical request-method text
     */
    public String methodText() {
        return method.value();
    }

    /**
     * Returns the stable authority including the effective port.
     *
     * @return host and effective port in authority form
     */
    public String authority() {
        String current = authority;
        if (current == null) {
            current = url.host() + Symbol.C_COLON + url.port();
            authority = current;
        }
        return current;
    }

    /**
     * Returns the encoded origin-form path and query consumed by HTTP/1.1 and HTTP/2.
     *
     * @return encoded path followed by the query when present
     */
    public String requestTarget() {
        String current = requestTarget;
        if (current == null) {
            final URI uri = url.toUri();
            final String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? Symbol.SLASH
                    : uri.getRawPath();
            current = uri.getRawQuery() == null ? path : path + Symbol.C_QUESTION_MARK + uri.getRawQuery();
            requestTarget = current;
        }
        return current;
    }

    /**
     * Returns the stable body length metadata.
     *
     * @return body length, or a negative value when unknown
     */
    public long bodyLength() {
        return bodyLength;
    }

    /**
     * Returns whether the body may be replayed by a retry or follow-up.
     *
     * @return true when another transmission can consume the body safely
     */
    public boolean replayable() {
        return replayable;
    }

    /**
     * Returns the tag.
     *
     * @return tag
     */
    public Object tag() {
        return tag(Object.class);
    }

    /**
     * Returns the tag associated with a type.
     *
     * @param type tag type
     * @param <T>  tag type
     * @return tag, or null when absent
     */
    public <T> T tag(final Class<T> type) {
        final Object value = tags.get(require(type, "Tag type"));
        return value == null ? null : type.cast(value);
    }

    /**
     * Returns the parsed Cache-Control request snapshot.
     *
     * @return cache control snapshot
     */
    public HttpCacheControl cacheControl() {
        HttpCacheControl parsed = cacheControl;
        if (parsed == null) {
            parsed = HttpCacheControl.parse(headers);
            cacheControl = parsed;
        }
        return parsed;
    }

    /**
     * Returns whether the request uses HTTPS.
     *
     * @return true when URL scheme is https
     */
    public boolean isHttps() {
        return Protocol.HTTPS.name.equals(url.scheme());
    }

    /**
     * Returns the proxy plan.
     *
     * @return proxy plan
     */
    public ProxyPlan proxy() {
        return proxy;
    }

    /**
     * Returns request timeout.
     *
     * @return timeout
     */
    public Timeout timeout() {
        return timeout;
    }

    /**
     * Validates method and body constraints.
     *
     * @param method method
     * @param body   body
     */
    private static void validateBodyPolicy(final HTTP.Method method, final PayloadBody body) {
        Assert.isFalse(
                !method.supportsBody() && body.length() > 0,
                () -> new ValidateException("HTTP method does not support a body"));
    }

    /**
     * Copies request tags into an immutable map.
     *
     * @param tags source tags
     * @return immutable tags
     */
    private static Map<Class<?>, Object> immutableTags(final Map<Class<?>, Object> tags) {
        if (tags == null || tags.isEmpty()) {
            return Map.of();
        }
        final LinkedHashMap<Class<?>, Object> copy = new LinkedHashMap<>(tags.size());
        tags.forEach((type, value) -> {
            require(type, "Tag type");
            if (value != null) {
                copy.put(type, value);
            }
        });
        return copy.isEmpty() ? Map.of() : Collections.unmodifiableMap(copy);
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
     * Builder for HTTP requests.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Candidate method.
         */
        private HTTP.Method method;

        /**
         * Candidate URL.
         */
        private UnoUrl url;

        /**
         * Candidate headers.
         */
        private Headers headers = Headers.empty();

        /**
         * Candidate body.
         */
        private PayloadBody body = PayloadBody.empty();

        /**
         * Candidate tags keyed by type.
         */
        private Map<Class<?>, Object> tags = Map.of();

        /**
         * True when tags already belong to an immutable request snapshot.
         */
        private boolean tagsSnapshot = true;

        /**
         * Candidate proxy plan.
         */
        private ProxyPlan proxy = ProxyPlan.direct();

        /**
         * Candidate timeout.
         */
        private Timeout timeout = Timeout.defaults();

        /**
         * Creates a builder with default snapshots.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the method.
         *
         * @param method method
         * @return this builder
         */
        public Builder method(final HTTP.Method method) {
            this.method = require(method, "HTTP method");
            return this;
        }

        /**
         * Sets the URL.
         *
         * @param url URL
         * @return this builder
         */
        public Builder url(final UnoUrl url) {
            this.url = require(url, "URL");
            return this;
        }

        /**
         * Sets headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            this.headers = require(headers, "Headers");
            return this;
        }

        /**
         * Sets the User-Agent header.
         *
         * @param value User-Agent value
         * @return this builder
         */
        public Builder userAgent(final String value) {
            headers = headers.with(HTTP.USER_AGENT, validateUserAgent(value));
            return this;
        }

        /**
         * Sets body.
         *
         * @param body body
         * @return this builder
         */
        public Builder body(final PayloadBody body) {
            this.body = require(body, "Body");
            return this;
        }

        /**
         * Sets a request body.
         *
         * @param body request body
         * @return this builder
         */
        public Builder body(final RequestBody body) {
            final RequestBody current = require(body, "Request body");
            return body(PayloadBody.of(current.payload(), current.media()));
        }

        /**
         * Sets a UTF-8 text body.
         *
         * @param value body text
         * @return this builder
         */
        public Builder text(final String value) {
            return body(TextBody.of(value));
        }

        /**
         * Sets a text body with explicit media.
         *
         * @param value body text
         * @param media media
         * @return this builder
         */
        public Builder text(final String value, final MediaType media) {
            return body(TextBody.of(value, media));
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param path file path
         * @return this builder
         */
        public Builder body(final Path path) {
            return body(path, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param path  file path
         * @param media media
         * @return this builder
         */
        public Builder body(final Path path, final MediaType media) {
            return body(FileBody.of(path, media));
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file file
         * @return this builder
         */
        public Builder body(final File file) {
            return body(file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        }

        /**
         * Sets a file as the complete request body.
         *
         * @param file  file
         * @param media media
         * @return this builder
         */
        public Builder body(final File file, final MediaType media) {
            return body(require(file, "Body file").toPath(), media);
        }

        /**
         * Sets tag.
         *
         * @param tag tag
         * @return this builder
         */
        public Builder tag(final Object tag) {
            return tag(Object.class, tag);
        }

        /**
         * Sets or removes a tag associated with a type.
         *
         * @param type tag type
         * @param tag  tag value, or null to remove
         * @param <T>  tag type
         * @return this builder
         */
        public <T> Builder tag(final Class<? super T> type, final T tag) {
            final Class<?> checkedType = require(type, "Tag type");
            if (tag == null) {
                if (!tags.isEmpty()) {
                    mutableTags().remove(checkedType);
                }
            } else {
                mutableTags().put(checkedType, tag);
            }
            return this;
        }

        /**
         * Sets proxy plan.
         *
         * @param proxy proxy plan
         * @return this builder
         */
        public Builder proxy(final ProxyPlan proxy) {
            this.proxy = require(proxy, "Proxy plan");
            return this;
        }

        /**
         * Sets timeout.
         *
         * @param timeout timeout
         * @return this builder
         */
        public Builder timeout(final Timeout timeout) {
            this.timeout = require(timeout, "Timeout");
            return this;
        }

        /**
         * Builds an immutable request.
         *
         * @return HTTP request
         */
        public HttpRequest build() {
            return new HttpRequest(method, url, headers, body, tags, tagsSnapshot, proxy, timeout);
        }

        /**
         * Returns mutable tags.
         *
         * @return mutable tags
         */
        private Map<Class<?>, Object> mutableTags() {
            if (!(tags instanceof LinkedHashMap)) {
                tags = new LinkedHashMap<>(tags);
                tagsSnapshot = false;
            }
            return tags;
        }

        /**
         * Validates a User-Agent value.
         *
         * @param value User-Agent value
         * @return validated value
         */
        private static String validateUserAgent(final String value) {
            Assert.isFalse(
                    StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                    () -> new ValidateException("User-Agent must be non-blank and single-line"));
            return StringKit.trim(value);
        }

    }

}
