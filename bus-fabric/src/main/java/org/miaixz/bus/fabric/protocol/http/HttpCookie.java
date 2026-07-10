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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
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
     * Prevents utility construction.
     */
    private HttpCookie() {
        // No initialization required.
    }

    /**
     * Parses all Set-Cookie headers for a URL.
     *
     * @param url     source URL
     * @param headers response headers
     * @return immutable cookies
     */
    public static List<Cookie> parseAll(final UnoUrl url, final Headers headers) {
        final UnoUrl sourceUrl = require(url, "URL");
        final Headers sourceHeaders = require(headers, "Headers");
        final List<Cookie> cookies = new ArrayList<>();
        for (final String header : sourceHeaders.values("Set-Cookie")) {
            if (malformed(header)) {
                continue;
            }
            cookies.add(Cookie.parse(header, sourceUrl));
        }
        return List.copyOf(cookies);
    }

    /**
     * Attaches matching cookies to headers.
     *
     * @param url     request URL
     * @param headers source headers
     * @param cookies available cookies
     * @return updated headers
     */
    public static Headers attach(final UnoUrl url, final Headers headers, final List<Cookie> cookies) {
        final List<Cookie> matched = match(url, cookies);
        final Headers.Builder builder = copy(require(headers, "Headers"));
        builder.remove("Cookie");
        if (!matched.isEmpty()) {
            final StringBuilder value = new StringBuilder();
            for (int i = 0; i < matched.size(); i++) {
                if (i > 0) {
                    value.append(Symbol.SEMICOLON).append(Symbol.SPACE);
                }
                final Cookie cookie = matched.get(i);
                value.append(cookie.name()).append(Symbol.C_EQUAL).append(cookie.value());
            }
            builder.set("Cookie", value.toString());
        }
        return builder.build();
    }

    /**
     * Returns cookies matching a URL.
     *
     * @param url     request URL
     * @param cookies cookies
     * @return immutable matching cookies
     */
    public static List<Cookie> match(final UnoUrl url, final List<Cookie> cookies) {
        final UnoUrl target = require(url, "URL");
        final List<Cookie> source = require(cookies, "Cookies");
        final List<Cookie> matched = new ArrayList<>();
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
     * @param header header value
     * @return true when malformed
     */
    private static boolean malformed(final String header) {
        if (header == null || header.isBlank()) {
            return true;
        }
        final int separator = header.split(Symbol.SEMICOLON, 2)[0].indexOf(Symbol.C_EQUAL);
        return separator <= 0;
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
