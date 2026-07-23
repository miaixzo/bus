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

import java.util.List;

import org.miaixz.bus.core.io.source.GzipSource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.Cookie;
import org.miaixz.bus.fabric.protocol.CookieJar;
import org.miaixz.bus.fabric.protocol.http.HttpCookie;
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
     * Stable identifier exposed to the HTTP stage chain.
     */
    private final String name;

    /**
     * Cookie store used to load request cookies and save response cookies, or {@code null} when disabled.
     */
    private final CookieJar cookies;

    /**
     * Validated fallback value added when a request omits {@code User-Agent}.
     */
    private final String userAgent;

    /** Most recent cookie-free prepared request shape and its immutable prepared result. */
    private volatile PreparedRequest preparedRequest;

    /**
     * Creates a bridge stage.
     */
    public HttpBridge() {
        this(null, defaultUserAgent());
    }

    /**
     * Creates a bridge stage with an automatic cookie jar.
     *
     * @param cookies cookie jar, or {@code null} to disable automatic cookie handling
     */
    public HttpBridge(final CookieJar cookies) {
        this(cookies, defaultUserAgent());
    }

    /**
     * Creates a bridge stage with automatic cookies and a default User-Agent.
     *
     * @param cookies   cookie jar, or {@code null} to disable automatic cookie handling
     * @param userAgent non-blank, single-line fallback {@code User-Agent}
     * @throws ValidateException if {@code userAgent} is blank or contains a line break
     */
    public HttpBridge(final CookieJar cookies, final String userAgent) {
        this.name = "http-bridge";
        this.cookies = cookies;
        this.userAgent = validateUserAgent(userAgent);
    }

    /**
     * Returns the default User-Agent.
     *
     * @return product name followed by the package implementation version when available
     */
    public static String defaultUserAgent() {
        final Package source = HttpBridge.class.getPackage();
        final String version = source == null ? null : source.getImplementationVersion();
        return version == null || version.isBlank() ? Keys.BUS : Keys.BUS + "/" + version;
    }

    /**
     * Prepares request headers, proceeds, and receives the response.
     *
     * @param request request whose protocol headers are prepared
     * @param chain   remaining exchange chain to execute
     * @return response after optional gzip decoding and cookie persistence
     * @throws ValidateException if the request or chain is {@code null}
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
     * @param request request to normalize for transport
     * @return original request when no changes are required, otherwise a copy with missing protocol headers added
     * @throws ProtocolException if a positive body length conflicts with {@code Content-Length} or the URL scheme is
     *                           unsupported
     * @throws ValidateException if the request or URL host is invalid
     */
    public HttpRequest prepare(final HttpRequest request) {
        final HttpRequest source = require(request, "HTTP request");
        final PreparedRequest cached = preparedRequest;
        if (cached != null && (cookies == null || cookies.isEmpty()) && cached.matches(source)) {
            return cached.prepared;
        }
        final Headers headers = source.headers();
        final long bodyLength = source.body().length();
        final long declaredLength = bodyLength > Normal._0 ? declaredLength(headers) : Normal.__1;
        if (declaredLength >= Normal._0 && declaredLength != bodyLength) {
            throw new ProtocolException("Content-Length does not match body length");
        }
        final boolean addHost = !headers.contains(Http.Header.HOST);
        final boolean addConnection = !headers.contains(Http.Header.CONNECTION);
        final boolean addEncoding = !headers.contains(Http.Header.ACCEPT_ENCODING);
        final boolean addAgent = !headers.contains(Http.Header.USER_AGENT);
        final boolean addLength = bodyLength > Normal._0 && declaredLength < Normal._0;
        final boolean addChunked = bodyLength < Normal._0 && !headers.contains(Http.Header.TRANSFER_ENCODING);
        final String cookie = cookies == null || headers.contains(Http.Header.COOKIE) ? null
                : cookieValue(source.url(), cookies.load(source.url()));
        if (!addHost && !addConnection && !addEncoding && !addAgent && !addLength && !addChunked && cookie == null) {
            return source;
        }
        final Headers.Builder builder = headers.newBuilder();
        if (addHost) {
            builder.add(Http.Header.HOST, hostValue(source.url()));
        }
        if (addConnection) {
            builder.add(Http.Header.CONNECTION, Http.Header.CONNECTION_KEEP_ALIVE);
        }
        if (addEncoding) {
            builder.add(Http.Header.ACCEPT_ENCODING, Http.Header.CONTENT_CODING_GZIP);
        }
        if (addAgent) {
            builder.add(Http.Header.USER_AGENT, userAgent);
        }
        if (addLength) {
            builder.add(Http.Header.CONTENT_LENGTH, Long.toString(bodyLength));
        } else if (addChunked) {
            builder.remove(Http.Header.CONTENT_LENGTH)
                    .add(Http.Header.TRANSFER_ENCODING, Http.Header.TRANSFER_CODING_CHUNKED);
        }
        if (cookie != null) {
            builder.add(Http.Header.COOKIE, cookie);
        }
        final HttpRequest prepared = source.toBuilder().headers(builder.build()).build();
        if (cookie == null && (cookies == null || cookies.isEmpty()) && source.tag() == null) {
            preparedRequest = new PreparedRequest(source, prepared);
        }
        return prepared;
    }

    /**
     * One immutable request-shape cache entry for repeated cookie-free calls.
     *
     * @param source   source request shape
     * @param prepared fully prepared immutable request
     */
    private record PreparedRequest(HttpRequest source, HttpRequest prepared) {

        /** Returns whether a request is value-identical for all fields changed or observed by this bridge. */
        private boolean matches(final HttpRequest candidate) {
            return source.method() == candidate.method() && source.url().toString().equals(candidate.url().toString())
                    && source.bodyLength() == Normal._0 && candidate.bodyLength() == Normal._0
                    && source.proxy().equals(candidate.proxy()) && sameTimeout(source.timeout(), candidate.timeout())
                    && candidate.tag() == null && sameHeaders(source.headers(), candidate.headers());
        }

        /** Compares ordered header pairs without materializing map views. */
        private static boolean sameHeaders(final Headers left, final Headers right) {
            final int size = left.size();
            if (size != right.size()) {
                return false;
            }
            for (int index = Normal._0; index < size; index++) {
                if (!left.name(index).equals(right.name(index)) || !left.value(index).equals(right.value(index))) {
                    return false;
                }
            }
            return true;
        }

        /** Compares timeout values because Timeout intentionally has identity equality. */
        private static boolean sameTimeout(final Timeout left, final Timeout right) {
            return left.connect().equals(right.connect()) && left.read().equals(right.read())
                    && left.write().equals(right.write()) && left.call().equals(right.call())
                    && left.ping().equals(right.ping()) && left.close().equals(right.close());
        }
    }

    /**
     * Receives and decodes a response.
     *
     * @param response transport response to inspect
     * @return original response when not gzip encoded, otherwise a copy with a streaming gzip-decoded body
     * @throws ValidateException if {@code response} is {@code null}
     */
    public HttpResponse receive(final HttpResponse response) {
        final HttpResponse source = require(response, "HTTP response");
        if (!gzip(source.headers())) {
            return source;
        }
        final Headers headers = source.headers().without(Http.Header.CONTENT_ENCODING)
                .without(Http.Header.CONTENT_LENGTH);
        final PayloadBody body = PayloadBody
                .of(Payload.source(new GzipSource(source.body().source()), Normal.__1), source.body().media());
        return source.toBuilder().headers(headers).body(body).build();
    }

    /**
     * Returns stage name.
     *
     * @return stable bridge-stage identifier
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Parses declared Content-Length.
     *
     * @param headers request headers to inspect
     * @return non-negative declared byte length, or {@code -1} when absent
     * @throws ProtocolException if the header is not a non-negative decimal integer
     */
    private static long declaredLength(final Headers headers) {
        final String value = headers.get(Http.Header.CONTENT_LENGTH);
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
     * @param headers response headers to inspect
     * @return {@code true} when any {@code Content-Encoding} field contains the {@code gzip} token
     */
    private static boolean gzip(final Headers headers) {
        for (int index = 0; index < headers.size(); index++) {
            if (Http.Header.CONTENT_ENCODING.equalsIgnoreCase(headers.name(index))
                    && containsToken(headers.value(index), Http.Header.CONTENT_CODING_GZIP)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a comma-separated header contains a token.
     *
     * @param value comma-separated header field value
     * @param token case-insensitive token to locate
     * @return {@code true} when a complete token matches
     */
    private static boolean containsToken(final String value, final String token) {
        int start = 0;
        while (start < value.length()) {
            int end = value.indexOf(Symbol.C_COMMA, start);
            if (end < 0) {
                end = value.length();
            }
            while (start < end && value.charAt(start) == Symbol.C_SPACE) {
                start++;
            }
            while (end > start && value.charAt(end - 1) == Symbol.C_SPACE) {
                end--;
            }
            if (end - start == token.length() && value.regionMatches(true, start, token, 0, token.length())) {
                return true;
            }
            start = end + 1;
        }
        return false;
    }

    /**
     * Formats the request Host header.
     *
     * @param url request URL supplying host, port, and scheme
     * @return RFC-compatible Host header value
     * @throws ProtocolException if the URL does not use HTTP or HTTPS
     * @throws ValidateException if the URL host is blank
     */
    private static String hostValue(final UnoUrl url) {
        final String scheme = url.address().scheme();
        final String host = url.address().host();
        if (host.isBlank()) {
            throw new ValidateException("Host must be non-blank");
        }
        final int defaultPort = Protocol.HTTP.name.equals(scheme) ? Port._80.getPort()
                : Protocol.HTTPS.name.equals(scheme) ? Port._443.getPort() : throwUnsupportedScheme(scheme);
        final String formatted = host.indexOf(Symbol.COLON) >= 0 && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
        return url.address().port() == defaultPort ? formatted : formatted + Symbol.COLON + url.address().port();
    }

    /**
     * Throws for an unsupported HTTP scheme while retaining expression form.
     *
     * @param scheme unsupported URL scheme
     * @return never returns
     */
    private static int throwUnsupportedScheme(final String scheme) {
        throw new ProtocolException("Unsupported HTTP scheme: " + scheme);
    }

    /**
     * Formats matching cookies without creating an intermediate Headers value.
     *
     * @param url       request URL used for cookie matching
     * @param available cookies available in the jar
     * @return Cookie header value, or {@code null} when none match
     */
    private static String cookieValue(final UnoUrl url, final List<Cookie> available) {
        final List<Cookie> matched = HttpCookie.match(url, available);
        if (matched.isEmpty()) {
            return null;
        }
        final StringBuilder value = new StringBuilder();
        for (int index = 0; index < matched.size(); index++) {
            if (index > 0) {
                value.append(Symbol.SEMICOLON).append(Symbol.SPACE);
            }
            final Cookie cookie = matched.get(index);
            value.append(cookie.name()).append(Symbol.C_EQUAL).append(cookie.value());
        }
        return value.toString();
    }

    /**
     * Saves response cookies when an automatic store is configured.
     *
     * @param response decoded response whose request URL and headers are persisted
     */
    private void save(final HttpResponse response) {
        if (cookies != null) {
            cookies.save(response.request().url(), response.headers());
        }
    }

    /**
     * Validates a default User-Agent value.
     *
     * @param value fallback {@code User-Agent} text to validate
     * @return trimmed, non-blank, single-line user-agent text
     * @throws ValidateException if the text is blank or contains a line break
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
