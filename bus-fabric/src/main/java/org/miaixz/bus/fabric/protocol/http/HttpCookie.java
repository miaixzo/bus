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
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.Cookie;

/**
 * HTTP cookie helpers that reuse the shared immutable {@link Cookie} value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCookie {

    /**
     * Keeps HTTP cookie parsing and formatting on the static API.
     */
    private HttpCookie() {
        // No initialization required.
    }

    /**
     * Parses response {@code Set-Cookie} headers that pass the basic name/value shape check.
     *
     * @param url     response URL used as the cookie origin
     * @param headers response headers containing zero or more {@code Set-Cookie} values
     * @return immutable list of parsed cookies in header order
     * @throws ValidateException if {@code url} or {@code headers} is {@code null}, or if a structurally accepted value
     *                           fails cookie validation
     * @throws ProtocolException if a structurally accepted value violates cookie protocol rules
     */
    public static List<Cookie> parseAll(final UnoUrl url, final Headers headers) {
        final UnoUrl sourceUrl = require(url, "URL");
        final Headers sourceHeaders = require(headers, "Headers");
        final List<String> values = sourceHeaders.values(Http.Header.SET_COOKIE);
        final List<Cookie> cookies = new ArrayList<>(values.size());
        for (final String header : values) {
            if (malformed(header)) {
                continue;
            }
            cookies.add(Cookie.parse(header, sourceUrl));
        }
        return List.copyOf(cookies);
    }

    /**
     * Replaces any existing {@code Cookie} header with cookies that match a request URL.
     *
     * @param url     request URL used for cookie matching
     * @param headers source headers copied into the result
     * @param cookies available cookies considered in iteration order
     * @return new headers containing a joined {@code Cookie} value, or no {@code Cookie} header when nothing matches
     * @throws ValidateException if a required argument or cookie element is {@code null}
     */
    public static Headers attach(final UnoUrl url, final Headers headers, final List<Cookie> cookies) {
        final List<Cookie> matched = match(url, cookies);
        final Headers.Builder builder = copy(require(headers, "Headers"));
        builder.remove(Http.Header.COOKIE);
        if (!matched.isEmpty()) {
            final StringBuilder value = new StringBuilder();
            for (int i = 0; i < matched.size(); i++) {
                if (i > 0) {
                    value.append(Symbol.SEMICOLON).append(Symbol.SPACE);
                }
                final Cookie cookie = matched.get(i);
                value.append(cookie.name()).append(Symbol.C_EQUAL).append(cookie.value());
            }
            builder.set(Http.Header.COOKIE, value.toString());
        }
        return builder.build();
    }

    /**
     * Returns cookies matching a URL.
     *
     * @param url     request URL used for expiration, security, domain, and path matching
     * @param cookies candidate cookies considered in iteration order
     * @return immutable list of cookies whose {@link Cookie#matches(UnoUrl)} checks succeed
     * @throws ValidateException if {@code url}, {@code cookies}, or a cookie element is {@code null}
     */
    public static List<Cookie> match(final UnoUrl url, final List<Cookie> cookies) {
        final UnoUrl target = require(url, "URL");
        final List<Cookie> source = require(cookies, "Cookies");
        final List<Cookie> matched = new ArrayList<>(source.size());
        for (final Cookie cookie : source) {
            require(cookie, "Cookie");
            if (cookie.matches(target)) {
                matched.add(cookie);
            }
        }
        return List.copyOf(matched);
    }

    /**
     * Returns whether a Set-Cookie value is too malformed to parse.
     *
     * @param header candidate {@code Set-Cookie} header value
     * @return {@code true} when the value is blank, has no non-empty name, or places its first equals sign after the
     *         first attribute boundary
     */
    private static boolean malformed(final String header) {
        if (header == null || header.isBlank()) {
            return true;
        }
        final int boundary = header.indexOf(Symbol.C_SEMICOLON);
        final int separator = header.indexOf(Symbol.C_EQUAL);
        return separator <= 0 || (boundary >= 0 && separator > boundary);
    }

    /**
     * Copies headers into a new builder.
     *
     * @param headers source headers whose names and values are copied
     * @return mutable builder containing every source header value
     */
    private static Headers.Builder copy(final Headers headers) {
        final Headers.Builder builder = Headers.builder();
        headers.asMap().forEach((name, values) -> values.forEach(value -> builder.add(name, value)));
        return builder;
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
