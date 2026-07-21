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

import org.miaixz.bus.core.io.source.GzipSource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.HttpCookie;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * HTTP bridge stage that prepares protocol headers and decodes bridge responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpBridge implements HttpStage {

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Optional automatic cookie jar.
     */
    private final CookieJar cookies;

    /**
     * Default User-Agent value.
     */
    private final String userAgent;

    /**
     * Creates a bridge stage.
     */
    public HttpBridge() {
        this(null, defaultUserAgent());
    }

    /**
     * Creates a bridge stage with an automatic cookie jar.
     *
     * @param cookies cookie jar, or null to disable automatic cookies
     */
    public HttpBridge(final CookieJar cookies) {
        this(cookies, defaultUserAgent());
    }

    /**
     * Creates a bridge stage with automatic cookies and a default User-Agent.
     *
     * @param cookies   cookie jar, or null to disable automatic cookies
     * @param userAgent default User-Agent
     */
    public HttpBridge(final CookieJar cookies, final String userAgent) {
        this.name = "http-bridge";
        this.cookies = cookies;
        this.userAgent = validateUserAgent(userAgent);
    }

    /**
     * Returns the default User-Agent.
     *
     * @return default User-Agent
     */
    public static String defaultUserAgent() {
        final Package source = HttpBridge.class.getPackage();
        final String version = source == null ? null : source.getImplementationVersion();
        return version == null || version.isBlank() ? Keys.BUS : Keys.BUS + "/" + version;
    }

    /**
     * Prepares request headers, proceeds, and receives the response.
     *
     * @param request request
     * @param chain   chain
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest prepared = prepare(request);
        final HttpResponse response = receive(require(chain, "HTTP chain").proceed(prepared));
        save(response);
        return response;
    }

    /**
     * Prepares protocol headers.
     *
     * @param request request
     * @return prepared request
     */
    public HttpRequest prepare(final HttpRequest request) {
        final HttpRequest source = require(request, "HTTP request");
        Headers headers = source.headers();
        if (!headers.contains(HTTP.HOST)) {
            headers = HttpHeaders.host(source.url(), headers);
        }
        headers = setIfMissing(headers, HTTP.CONNECTION, HTTP.CONNECTION_KEEP_ALIVE);
        headers = setIfMissing(headers, HTTP.ACCEPT_ENCODING, HTTP.CONTENT_CODING_GZIP);
        headers = setIfMissing(headers, HTTP.USER_AGENT, userAgent);
        headers = bodyHeaders(headers, source.body());
        if (cookies != null && !headers.contains(HTTP.COOKIE)) {
            headers = HttpCookie.attach(source.url(), headers, cookies.load(source.url()));
        }
        return headers == source.headers() ? source : source.toBuilder().headers(headers).build();
    }

    /**
     * Receives and decodes a response.
     *
     * @param response response
     * @return decoded response
     */
    public HttpResponse receive(final HttpResponse response) {
        final HttpResponse source = require(response, "HTTP response");
        if (!gzip(source.headers())) {
            return source;
        }
        final Headers headers = source.headers().without(HTTP.CONTENT_ENCODING).without(HTTP.CONTENT_LENGTH);
        final PayloadBody body = PayloadBody
                .of(Payload.source(new GzipSource(source.body().source()), Normal.__1), source.body().media());
        return source.toBuilder().headers(headers).body(body).build();
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
     * Applies body framing headers.
     *
     * @param headers headers
     * @param body    body
     * @return headers
     */
    private static Headers bodyHeaders(final Headers headers, final PayloadBody body) {
        final long length = body.length();
        if (length == Normal._0) {
            return headers;
        }
        if (length >= Normal._0) {
            final long declared = declaredLength(headers);
            if (declared >= Normal._0 && declared != length) {
                throw new ProtocolException("Content-Length does not match body length");
            }
            return declared >= Normal._0 ? headers : HttpHeaders.contentLength(headers, length);
        }
        return headers.contains(HTTP.TRANSFER_ENCODING) ? headers
                : headers.with(HTTP.TRANSFER_ENCODING, HTTP.TRANSFER_CODING_CHUNKED).without(HTTP.CONTENT_LENGTH);
    }

    /**
     * Sets a header when absent.
     *
     * @param headers headers
     * @param name    header name
     * @param value   header value
     * @return headers
     */
    private static Headers setIfMissing(final Headers headers, final String name, final String value) {
        return headers.contains(name) ? headers : headers.with(name, value);
    }

    /**
     * Parses declared Content-Length.
     *
     * @param headers headers
     * @return declared length or -1
     */
    private static long declaredLength(final Headers headers) {
        final String value = headers.get(HTTP.CONTENT_LENGTH);
        if (value == null) {
            return Normal.__1;
        }
        try {
            final long length = Long.parseLong(value);
            if (length < Normal._0) {
                throw new ProtocolException("Content-Length must be non-negative");
            }
            return length;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid Content-Length", e);
        }
    }

    /**
     * Returns whether response headers declare gzip.
     *
     * @param headers headers
     * @return true when gzip
     */
    private static boolean gzip(final Headers headers) {
        for (final String value : HttpHeaders.values(headers, HTTP.CONTENT_ENCODING)) {
            if (HTTP.CONTENT_CODING_GZIP.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves response cookies when an automatic store is configured.
     *
     * @param response response
     */
    private void save(final HttpResponse response) {
        if (cookies != null) {
            cookies.save(response.request().url(), response.headers());
        }
    }

    /**
     * Validates a default User-Agent value.
     *
     * @param value User-Agent value
     * @return validated value
     */
    private static String validateUserAgent(final String value) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("User-Agent must be non-blank and single-line"));
        return value.trim();
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
