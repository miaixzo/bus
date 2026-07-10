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

import java.io.IOException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.HttpCookie;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP bridge stage that prepares protocol headers and decodes bridge responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpBridge implements HttpStage {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Product token used in the default User-Agent.
     */
    private static final String PRODUCT = "bus-fabric";

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
        return version == null || version.isBlank() ? PRODUCT : PRODUCT + "/" + version;
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
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP bridge stage started: method={}, host={}, port={}, path={}",
                request.method().value(),
                request.url().host(),
                request.url().port(),
                request.url().path());
        final HttpRequest prepared = prepare(request);
        final HttpResponse response = receive(require(chain, "HTTP chain").proceed(prepared));
        save(response);
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP bridge stage completed: method={}, host={}, port={}, path={}, code={}",
                prepared.method().value(),
                prepared.url().host(),
                prepared.url().port(),
                prepared.url().path(),
                response.code());
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
        Headers headers = HttpHeaders.host(source.url(), source.headers());
        headers = setIfMissing(headers, "Connection", "keep-alive");
        headers = setIfMissing(headers, "Accept-Encoding", "gzip");
        headers = setIfMissing(headers, "User-Agent", userAgent);
        headers = bodyHeaders(headers, source.body());
        if (cookies != null && !headers.contains("Cookie")) {
            headers = HttpCookie.attach(source.url(), headers, cookies.load(source.url()));
        }
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP bridge headers prepared: host={}, port={}, bodyLength={}, repeatable={}, cookiesEnabled={}, headerNames={}",
                source.url().host(),
                source.url().port(),
                source.body().length(),
                source.body().repeatable(),
                cookies != null,
                headers.asMap().keySet());
        return HttpRequest.builder().method(source.method()).url(source.url()).headers(headers).body(source.body())
                .tag(source.tag()).proxy(source.proxy()).timeout(source.timeout()).build();
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
            Logger.debug(false, LOG_TAG, "HTTP bridge response decode skipped: code={}, gzip={}", source.code(), false);
            return source;
        }
        try {
            Logger.debug(true, LOG_TAG, "HTTP bridge gzip decode started: code={}", source.code());
            final GZIPInputStream input = new GZIPInputStream(source.body().stream());
            final Headers headers = source.headers().without("Content-Encoding").without("Content-Length");
            final HttpBody body = HttpBody.of(Payload.stream(input, -1), source.body().media());
            final HttpResponse decoded = HttpResponse.builder().request(source.request()).code(source.code())
                    .message(source.message()).headers(headers).body(body).build();
            Logger.debug(false, LOG_TAG, "HTTP bridge gzip decode completed: code={}", source.code());
            return decoded;
        } catch (final IOException e) {
            throw new InternalException("Unable to decode gzip HTTP response", e);
        }
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
    private static Headers bodyHeaders(final Headers headers, final HttpBody body) {
        final long length = body.length();
        if (length == 0) {
            return headers;
        }
        if (length >= 0) {
            final long declared = declaredLength(headers);
            if (declared >= 0 && declared != length) {
                throw new ProtocolException("Content-Length does not match body length");
            }
            return declared >= 0 ? headers : HttpHeaders.contentLength(headers, length);
        }
        return headers.contains("Transfer-Encoding") ? headers
                : headers.with("Transfer-Encoding", "chunked").without("Content-Length");
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
        final String value = headers.get("Content-Length");
        if (value == null) {
            return -1;
        }
        try {
            final long length = Long.parseLong(value);
            if (length < 0) {
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
        for (final String value : HttpHeaders.values(headers, "Content-Encoding")) {
            if ("gzip".equals(value.toLowerCase(Locale.ROOT))) {
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
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP bridge cookies saved: host={}, port={}, code={}",
                    response.request().url().host(),
                    response.request().url().port(),
                    response.code());
        }
    }

    /**
     * Validates a default User-Agent value.
     *
     * @param value User-Agent value
     * @return validated value
     */
    private static String validateUserAgent(final String value) {
        if (value == null || value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new ValidateException("User-Agent must be non-blank and single-line");
        }
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
