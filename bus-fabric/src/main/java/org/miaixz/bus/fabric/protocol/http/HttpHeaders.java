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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Port;
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
     * Keeps HTTP header normalization on the static API.
     */
    private HttpHeaders() {
        // No initialization required.
    }

    /**
     * Returns headers with a Host value when absent.
     *
     * @param url     request URL supplying scheme, host, and effective port
     * @param headers source headers to preserve
     * @return original headers when Host is present, otherwise a copy with a formatted Host field
     * @throws ProtocolException if a missing Host field must be generated for a non-HTTP scheme
     * @throws ValidateException if the URL, headers, or generated host is invalid
     */
    public static Headers host(final UnoUrl url, final Headers headers) {
        require(url, "URL");
        final Headers source = require(headers, "Headers");
        if (source.contains(Http.Header.HOST)) {
            return source;
        }
        final String scheme = url.address().scheme();
        final String host = url.address().host();
        final int port = url.address().port();
        if (host.isBlank()) {
            throw new ValidateException("Host must be non-blank");
        }
        final int defaultPort = defaultPort(scheme);
        final String value = port == defaultPort ? formatHost(host) : formatHost(host) + Symbol.COLON + port;
        return source.with(Http.Header.HOST, value);
    }

    /**
     * Returns headers with a Content-Length policy.
     *
     * @param headers source headers
     * @param length  content length, or -1 when unknown
     * @return original headers when already equal, otherwise a copy with the field set or removed
     * @throws ProtocolException if an existing Content-Length field is malformed
     * @throws ValidateException if headers is {@code null} or length is less than {@code -1}
     */
    public static Headers contentLength(final Headers headers, final long length) {
        Assert.isTrue(length >= Normal.__1, () -> new ValidateException("Content length must be -1 or greater"));
        final Headers source = require(headers, "Headers");
        if (length == Normal.__1) {
            return source.without(Http.Header.CONTENT_LENGTH);
        }
        final long current = source.contentLength();
        return current == length ? source : source.with(Http.Header.CONTENT_LENGTH, Long.toString(length));
    }

    /**
     * Returns the validated Content-Length value.
     *
     * @param headers source headers
     * @return content length, or -1 when absent
     * @throws ProtocolException if a present Content-Length field is malformed
     * @throws ValidateException if {@code headers} is {@code null}
     */
    public static long contentLength(final Headers headers) {
        return require(headers, "Headers").contentLength();
    }

    /**
     * Returns whether cached and request headers satisfy response Vary fields.
     *
     * @param cached   request headers stored with the cached response
     * @param request  current request headers to compare
     * @param response cached response headers declaring Vary field names
     * @return {@code true} when no wildcard is present and every declared field has the same ordered values
     * @throws ValidateException if any header collection is {@code null}
     */
    public static boolean varyMatches(final Headers cached, final Headers request, final Headers response) {
        require(cached, "Cached headers");
        require(request, "Request headers");
        require(response, "Response headers");
        final Map<String, String> vary = new LinkedHashMap<>();
        for (final String name : values(response, Http.Header.VARY)) {
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
     * @param headers headers whose matching fields are scanned in order
     * @param name    case-insensitive header name to select
     * @return immutable non-empty, trimmed comma-separated field values in encounter order
     * @throws ProtocolException if a stored value contains a line break
     * @throws ValidateException if headers or the field name is invalid
     */
    public static List<String> values(final Headers headers, final String name) {
        final Headers source = require(headers, "Headers");
        final String checkedName = validateName(name);
        ArrayList<String> values = null;
        String single = null;
        for (int index = 0; index < source.size(); index++) {
            if (!source.name(index).equalsIgnoreCase(checkedName)) {
                continue;
            }
            final String value = source.value(index);
            if (StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
                throw new ProtocolException("Invalid HTTP header value");
            }
            int start = 0;
            while (start <= value.length()) {
                final int comma = value.indexOf(Symbol.C_COMMA, start);
                final int end = comma < 0 ? value.length() : comma;
                int left = start;
                int right = end;
                while (left < right && Character.isWhitespace(value.charAt(left))) {
                    left++;
                }
                while (right > left && Character.isWhitespace(value.charAt(right - 1))) {
                    right--;
                }
                if (right > left) {
                    final String parsed = left == 0 && right == value.length() ? value : value.substring(left, right);
                    if (single == null) {
                        single = parsed;
                    } else {
                        if (values == null) {
                            values = new ArrayList<>(4);
                            values.add(single);
                        }
                        values.add(parsed);
                    }
                }
                if (comma < 0) {
                    break;
                }
                start = comma + 1;
            }
        }
        if (single == null) {
            return List.of();
        }
        return values == null ? List.of(single) : Collections.unmodifiableList(values);
    }

    /**
     * Returns a builder initialized from an immutable header snapshot for batch updates.
     *
     * @param headers source headers
     * @return initialized builder
     */
    public static Headers.Builder newBuilder(final Headers headers) {
        return require(headers, "Headers").newBuilder();
    }

    /**
     * Parses the last User-Agent header value.
     *
     * @param headers source headers whose final User-Agent field is parsed
     * @return parsed User-Agent, or null when absent or blank
     */
    public static UserAgent userAgent(final Headers headers) {
        return UserAgent.parse(require(headers, "Headers").get(Http.Header.USER_AGENT));
    }

    /**
     * Returns a redacted header snapshot for logging and observation.
     *
     * @param headers source headers whose values are sanitized independently
     * @return immutable header snapshot preserving names and multiplicity with sensitive content fingerprinted
     * @throws ValidateException if {@code headers} is {@code null}
     */
    public static Headers redacted(final Headers headers) {
        final Headers source = require(headers, "Headers");
        final Headers.Builder builder = Headers.builder();
        source.asMap()
                .forEach((name, values) -> values.forEach(value -> builder.add(name, Tags.sanitize(name, value))));
        return builder.build();
    }

    /**
     * Formats a host for header usage.
     *
     * @param host host name or address literal
     * @return IPv6-like colon-containing host enclosed in brackets, or unchanged host text
     */
    private static String formatHost(final String host) {
        return host.indexOf(Symbol.COLON) >= 0 && !host.startsWith(Symbol.BRACKET_LEFT)
                ? Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT
                : host;
    }

    /**
     * Returns the default port for an HTTP scheme.
     *
     * @param scheme exact lowercase HTTP or HTTPS scheme
     * @return port 80 for HTTP or 443 for HTTPS
     * @throws ProtocolException if the scheme is unsupported
     */
    private static int defaultPort(final String scheme) {
        if (Protocol.HTTP.name.equals(scheme)) {
            return Port._80.getPort();
        }
        if (Protocol.HTTPS.name.equals(scheme)) {
            return Port._443.getPort();
        }
        throw new ProtocolException("Unsupported HTTP scheme: " + scheme);
    }

    /**
     * Validates a header name.
     *
     * @param name header field name to validate
     * @return unchanged non-blank, single-line field name
     * @throws ValidateException if the name is blank or contains a line break
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
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
