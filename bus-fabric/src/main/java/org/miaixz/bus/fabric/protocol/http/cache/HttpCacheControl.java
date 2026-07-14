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
package org.miaixz.bus.fabric.protocol.http.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;

/**
 * Immutable Cache-Control header snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCacheControl {

    /**
     * Empty cache control snapshot.
     */
    private static final HttpCacheControl EMPTY = new HttpCacheControl(Map.of());

    /**
     * Forces a network validation.
     */
    public static final HttpCacheControl FORCE_NETWORK = new HttpCacheControl(
            Map.of(HTTP.CACHE_DIRECTIVE_NO_CACHE, Normal.EMPTY));

    /**
     * Forces cache usage and accepts stale entries.
     */
    public static final HttpCacheControl FORCE_CACHE = new HttpCacheControl(Map.of(
            HTTP.CACHE_DIRECTIVE_ONLY_IF_CACHED,
            Normal.EMPTY,
            HTTP.CACHE_DIRECTIVE_MAX_STALE,
            Integer.toString(Integer.MAX_VALUE)));

    /**
     * Parsed directive values keyed by lower-case directive name.
     */
    private final Map<String, String> directives;

    /**
     * Creates a cache control snapshot.
     *
     * @param directives parsed directives
     */
    private HttpCacheControl(final Map<String, String> directives) {
        this.directives = directives.isEmpty() ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(directives));
    }

    /**
     * Parses Cache-Control and Pragma headers.
     *
     * @param headers headers
     * @return cache control snapshot
     */
    public static HttpCacheControl parse(final Headers headers) {
        final Headers source = require(headers, "Headers");
        final LinkedHashMap<String, String> parsed = new LinkedHashMap<>();
        for (final String token : HttpHeaders.values(source, HTTP.CACHE_CONTROL)) {
            parseDirective(token, parsed);
        }
        for (final String token : HttpHeaders.values(source, HTTP.PRAGMA)) {
            if (HTTP.CACHE_DIRECTIVE_NO_CACHE.equalsIgnoreCase(token.trim())) {
                parsed.putIfAbsent(HTTP.CACHE_DIRECTIVE_NO_CACHE, Normal.EMPTY);
            }
        }
        return parsed.isEmpty() ? EMPTY : new HttpCacheControl(parsed);
    }

    /**
     * Returns whether the no-cache directive exists.
     *
     * @return true when present
     */
    public boolean noCache() {
        return contains(HTTP.CACHE_DIRECTIVE_NO_CACHE);
    }

    /**
     * Returns whether the no-store directive exists.
     *
     * @return true when present
     */
    public boolean noStore() {
        return contains(HTTP.CACHE_DIRECTIVE_NO_STORE);
    }

    /**
     * Returns whether the only-if-cached directive exists.
     *
     * @return true when present
     */
    public boolean onlyIfCached() {
        return contains(HTTP.CACHE_DIRECTIVE_ONLY_IF_CACHED);
    }

    /**
     * Returns whether the no-transform directive exists.
     *
     * @return true when present
     */
    public boolean noTransform() {
        return contains(HTTP.CACHE_DIRECTIVE_NO_TRANSFORM);
    }

    /**
     * Returns whether the immutable directive exists.
     *
     * @return true when present
     */
    public boolean immutable() {
        return contains(HTTP.CACHE_DIRECTIVE_IMMUTABLE);
    }

    /**
     * Returns whether the public directive exists.
     *
     * @return true when present
     */
    public boolean isPublic() {
        return contains(HTTP.CACHE_DIRECTIVE_PUBLIC);
    }

    /**
     * Returns whether the private directive exists.
     *
     * @return true when present
     */
    public boolean isPrivate() {
        return contains(HTTP.CACHE_DIRECTIVE_PRIVATE);
    }

    /**
     * Returns whether the must-revalidate directive exists.
     *
     * @return true when present
     */
    public boolean mustRevalidate() {
        return contains(HTTP.CACHE_DIRECTIVE_MUST_REVALIDATE);
    }

    /**
     * Returns max-age seconds.
     *
     * @return seconds, or -1 when absent
     */
    public int maxAgeSeconds() {
        return seconds(HTTP.CACHE_DIRECTIVE_MAX_AGE, -1);
    }

    /**
     * Returns shared max-age seconds.
     *
     * @return seconds, or -1 when absent
     */
    public int sMaxAgeSeconds() {
        return seconds(HTTP.CACHE_DIRECTIVE_S_MAXAGE, -1);
    }

    /**
     * Returns max-stale seconds.
     *
     * @return seconds, Integer.MAX_VALUE when valueless, or -1 when absent
     */
    public int maxStaleSeconds() {
        return seconds(HTTP.CACHE_DIRECTIVE_MAX_STALE, Integer.MAX_VALUE);
    }

    /**
     * Returns min-fresh seconds.
     *
     * @return seconds, or -1 when absent
     */
    public int minFreshSeconds() {
        return seconds(HTTP.CACHE_DIRECTIVE_MIN_FRESH, -1);
    }

    /**
     * Returns an immutable directive snapshot.
     *
     * @return directives
     */
    public Map<String, String> directives() {
        return directives;
    }

    /**
     * Returns whether a directive exists.
     *
     * @param name directive name
     * @return true when present
     */
    public boolean contains(final String name) {
        return directives.containsKey(normalizeName(name));
    }

    /**
     * Returns a directive value.
     *
     * @param name directive name
     * @return value, or null when absent or valueless
     */
    public String value(final String name) {
        final String value = directives.get(normalizeName(name));
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Parses one directive token.
     *
     * @param token      directive token
     * @param directives target directives
     */
    private static void parseDirective(final String token, final Map<String, String> directives) {
        final String trimmed = token == null ? Normal.EMPTY : token.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        final int equals = trimmed.indexOf(Symbol.C_EQUAL);
        final String name = equals < 0 ? trimmed : trimmed.substring(0, equals).trim();
        final String value = equals < 0 ? Normal.EMPTY : unquote(trimmed.substring(equals + 1).trim());
        directives.putIfAbsent(normalizeName(name), value);
    }

    /**
     * Returns a directive seconds value.
     *
     * @param directive       directive name
     * @param valuelessResult value returned when directive is valueless
     * @return seconds or -1 when absent
     */
    private int seconds(final String directive, final int valuelessResult) {
        final String value = directives.get(directive);
        if (value == null) {
            return -1;
        }
        if (value.isBlank()) {
            return valuelessResult;
        }
        try {
            final long seconds = Long.parseLong(value);
            Assert.isFalse(seconds < 0, () -> new ProtocolException("Cache-Control seconds must be non-negative"));
            return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid Cache-Control seconds", e);
        }
    }

    /**
     * Removes optional surrounding quotes.
     *
     * @param value value
     * @return unquoted value
     */
    private static String unquote(final String value) {
        if (value.length() >= 2 && value.charAt(0) == Symbol.C_DOUBLE_QUOTES
                && value.charAt(value.length() - 1) == Symbol.C_DOUBLE_QUOTES) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Normalizes a directive name.
     *
     * @param name directive name
     * @return normalized name
     */
    private static String normalizeName(final String name) {
        if (StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Cache-Control directive must be non-blank and single-line");
        }
        return name.trim().toLowerCase(Locale.ROOT);
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
