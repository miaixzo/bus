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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.observe.tags.Tags;
import org.miaixz.bus.fabric.protocol.http.agent.UserAgent;

/**
 * HTTP-specific header helpers built on the shared immutable {@link Headers} value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpHeaders {

    /**
     * Prevents utility construction.
     */
    private HttpHeaders() {
        // No initialization required.
    }

    /**
     * Returns headers with a Host value when absent.
     *
     * @param url     request URL
     * @param headers source headers
     * @return updated headers
     */
    public static Headers host(final UnoUrl url, final Headers headers) {
        require(url, "URL");
        final Headers source = require(headers, "Headers");
        final Headers.Builder builder = copy(source);
        if (source.contains(HTTP.HOST)) {
            return builder.build();
        }
        final String scheme = url.address().scheme();
        final String host = url.address().host();
        final int port = url.address().port();
        if (host.isBlank()) {
            throw new ValidateException("Host must be non-blank");
        }
        final int defaultPort = defaultPort(scheme);
        final String value = port == defaultPort ? formatHost(host) : formatHost(host) + Symbol.COLON + port;
        return builder.set(HTTP.HOST, value).build();
    }

    /**
     * Returns headers with a Content-Length policy.
     *
     * @param headers source headers
     * @param length  content length, or -1 when unknown
     * @return updated headers
     */
    public static Headers contentLength(final Headers headers, final long length) {
        Assert.isTrue(length >= Normal.__1, () -> new ValidateException("Content length must be -1 or greater"));
        final Headers.Builder builder = copy(require(headers, "Headers"));
        return length == Normal.__1 ? builder.remove(HTTP.CONTENT_LENGTH).build()
                : builder.set(HTTP.CONTENT_LENGTH, Long.toString(length)).build();
    }

    /**
     * Returns whether cached and request headers satisfy response Vary fields.
     *
     * @param cached   cached request headers
     * @param request  current request headers
     * @param response cached response headers
     * @return true when Vary fields match
     */
    public static boolean varyMatches(final Headers cached, final Headers request, final Headers response) {
        require(cached, "Cached headers");
        require(request, "Request headers");
        require(response, "Response headers");
        final Map<String, String> vary = new LinkedHashMap<>();
        for (final String name : values(response, HTTP.VARY)) {
            if (Symbol.STAR.equals(name)) {
                return false;
            }
            vary.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
        }
        for (final String name : vary.values()) {
            if (!values(cached, name).equals(values(request, name))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns comma-split values for a header name.
     *
     * @param headers headers
     * @param name    header name
     * @return immutable values
     */
    public static List<String> values(final Headers headers, final String name) {
        final Headers source = require(headers, "Headers");
        final String checkedName = validateName(name);
        final List<String> values = new ArrayList<>();
        source.asMap().forEach((headerName, headerValues) -> {
            if (headerName.equalsIgnoreCase(checkedName)) {
                headerValues.forEach(value -> split(value, values));
            }
        });
        return List.copyOf(values);
    }

    /**
     * Parses the last User-Agent header value.
     *
     * @param headers headers
     * @return parsed User-Agent, or null when absent or blank
     */
    public static UserAgent userAgent(final Headers headers) {
        return UserAgent.parse(require(headers, "Headers").get(HTTP.USER_AGENT));
    }

    /**
     * Returns a redacted header snapshot for logging and observation.
     *
     * @param headers headers
     * @return redacted headers
     */
    public static Headers redacted(final Headers headers) {
        final Headers source = require(headers, "Headers");
        final Headers.Builder builder = Headers.builder();
        source.asMap()
                .forEach((name, values) -> values.forEach(value -> builder.add(name, Tags.sanitize(name, value))));
        return builder.build();
    }

    /**
     * Copies headers into a new builder.
     *
     * @param headers headers
     * @return builder
     */
    private static Headers.Builder copy(final Headers headers) {
        final Headers.Builder builder = Headers.builder();
        headers.asMap().forEach((name, values) -> values.forEach(value -> builder.add(name, value)));
        return builder;
    }

    /**
     * Splits a comma-separated header value.
     *
     * @param value  header value
     * @param target target values
     */
    private static void split(final String value, final List<String> target) {
        if (value == null || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ProtocolException("Invalid HTTP header value");
        }
        int start = 0;
        while (start <= value.length()) {
            final int comma = value.indexOf(Symbol.C_COMMA, start);
            final int end = comma < 0 ? value.length() : comma;
            final String trimmed = value.substring(start, end).trim();
            if (!trimmed.isEmpty()) {
                target.add(trimmed);
            }
            if (comma < 0) {
                break;
            }
            start = comma + 1;
        }
    }

    /**
     * Formats a host for header usage.
     *
     * @param host host
     * @return host text
     */
    private static String formatHost(final String host) {
        return host.indexOf(Symbol.COLON) >= 0 && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
    }

    /**
     * Returns the default port for an HTTP scheme.
     *
     * @param scheme scheme
     * @return default port
     */
    private static int defaultPort(final String scheme) {
        if (Protocol.HTTP.name.equals(scheme)) {
            return PORT._80.getPort();
        }
        if (Protocol.HTTPS.name.equals(scheme)) {
            return PORT._443.getPort();
        }
        throw new ProtocolException("Unsupported HTTP scheme: " + scheme);
    }

    /**
     * Validates a header name.
     *
     * @param name header name
     * @return header name
     */
    private static String validateName(final String name) {
        Assert.isFalse(
                StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Header name must be non-blank and single-line"));
        return name;
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
