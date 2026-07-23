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
import org.miaixz.bus.core.net.Http;
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
     * Shared empty snapshot returned when header parsing finds no directives.
     */
    private static final HttpCacheControl EMPTY = new HttpCacheControl(Map.of());

    /**
     * Insertion-ordered immutable directive snapshot; parsed keys are lower-case, while {@link #of(Map)} preserves
     * supplied keys unchanged.
     */
    private final Map<String, String> directives;

    /**
     * Copies directive entries into an insertion-ordered immutable snapshot.
     *
     * @param directives directive entries to copy without normalization
     */
    private HttpCacheControl(final Map<String, String> directives) {
        this.directives = directives.isEmpty() ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(directives));
    }

    /**
     * Creates a cache-control snapshot by copying directive entries exactly as supplied.
     *
     * @param directives directive names and values to snapshot
     * @return immutable insertion-ordered cache-control snapshot
     * @throws ValidateException if {@code directives} is {@code null}
     */
    public static HttpCacheControl of(final Map<String, String> directives) {
        return new HttpCacheControl(Assert
                .notNull(directives, () -> new ValidateException("HTTP cache control directives must not be null")));
    }

    /**
     * Parses comma-tokenized {@code Cache-Control} directives and the legacy {@code Pragma: no-cache} signal.
     * <p>
     * Directive names are normalized to lower case, surrounding value quotes are removed, and the first occurrence of
     * each directive wins.
     * </p>
     *
     * @param headers HTTP headers to inspect
     * @return parsed immutable snapshot, or the shared empty snapshot when no directives are present
     * @throws ValidateException if {@code headers} is {@code null} or a directive name is blank or multi-line
     */
    public static HttpCacheControl parse(final Headers headers) {
        final Headers source = require(headers, "Headers");
        final LinkedHashMap<String, String> parsed = new LinkedHashMap<>();
        for (final String token : HttpHeaders.values(source, Http.Header.CACHE_CONTROL)) {
            parseDirective(token, parsed);
        }
        for (final String token : HttpHeaders.values(source, Http.Header.PRAGMA)) {
            if (Http.Cache.NO_CACHE.equalsIgnoreCase(token.trim())) {
                parsed.putIfAbsent(Http.Cache.NO_CACHE, Normal.EMPTY);
            }
        }
        return parsed.isEmpty() ? EMPTY : new HttpCacheControl(parsed);
    }

    /**
     * Returns whether the no-cache directive exists.
     *
     * @return {@code true} when {@code no-cache} is present
     */
    public boolean noCache() {
        return contains(Http.Cache.NO_CACHE);
    }

    /**
     * Returns whether the no-store directive exists.
     *
     * @return {@code true} when {@code no-store} is present
     */
    public boolean noStore() {
        return contains(Http.Cache.NO_STORE);
    }

    /**
     * Returns whether the only-if-cached directive exists.
     *
     * @return {@code true} when {@code only-if-cached} is present
     */
    public boolean onlyIfCached() {
        return contains(Http.Cache.ONLY_IF_CACHED);
    }

    /**
     * Returns whether the no-transform directive exists.
     *
     * @return {@code true} when {@code no-transform} is present
     */
    public boolean noTransform() {
        return contains(Http.Cache.NO_TRANSFORM);
    }

    /**
     * Returns whether the immutable directive exists.
     *
     * @return {@code true} when {@code immutable} is present
     */
    public boolean immutable() {
        return contains(Http.Cache.IMMUTABLE);
    }

    /**
     * Returns whether the public directive exists.
     *
     * @return {@code true} when {@code public} is present
     */
    public boolean isPublic() {
        return contains(Http.Cache.PUBLIC);
    }

    /**
     * Returns whether the private directive exists.
     *
     * @return {@code true} when {@code private} is present
     */
    public boolean isPrivate() {
        return contains(Http.Cache.PRIVATE);
    }

    /**
     * Returns whether the must-revalidate directive exists.
     *
     * @return {@code true} when {@code must-revalidate} is present
     */
    public boolean mustRevalidate() {
        return contains(Http.Cache.MUST_REVALIDATE);
    }

    /**
     * Returns max-age seconds.
     *
     * @return non-negative seconds clamped to {@link Integer#MAX_VALUE}, or {@code -1} when absent or valueless
     * @throws ProtocolException if the present value is negative or not an integer
     */
    public int maxAgeSeconds() {
        return seconds(Http.Cache.MAX_AGE, -1);
    }

    /**
     * Returns shared max-age seconds.
     *
     * @return non-negative seconds clamped to {@link Integer#MAX_VALUE}, or {@code -1} when absent or valueless
     * @throws ProtocolException if the present value is negative or not an integer
     */
    public int sMaxAgeSeconds() {
        return seconds(Http.Cache.S_MAXAGE, -1);
    }

    /**
     * Returns max-stale seconds.
     *
     * @return non-negative seconds clamped to {@link Integer#MAX_VALUE}, {@link Integer#MAX_VALUE} when valueless, or
     *         {@code -1} when absent
     * @throws ProtocolException if the present value is negative or not an integer
     */
    public int maxStaleSeconds() {
        return seconds(Http.Cache.MAX_STALE, Integer.MAX_VALUE);
    }

    /**
     * Returns min-fresh seconds.
     *
     * @return non-negative seconds clamped to {@link Integer#MAX_VALUE}, or {@code -1} when absent or valueless
     * @throws ProtocolException if the present value is negative or not an integer
     */
    public int minFreshSeconds() {
        return seconds(Http.Cache.MIN_FRESH, -1);
    }

    /**
     * Returns an immutable directive snapshot.
     *
     * @return immutable insertion-ordered directive map retained by this snapshot
     */
    public Map<String, String> directives() {
        return directives;
    }

    /**
     * Returns whether a directive exists.
     *
     * @param name directive name normalized before lookup
     * @return {@code true} when the normalized name is a key in this snapshot
     * @throws ValidateException if {@code name} is blank or multi-line
     */
    public boolean contains(final String name) {
        return directives.containsKey(normalizeName(name));
    }

    /**
     * Returns a directive value.
     *
     * @param name directive name normalized before lookup
     * @return non-empty directive value, or {@code null} when absent, null, or valueless
     * @throws ValidateException if {@code name} is blank or multi-line
     */
    public String value(final String name) {
        final String value = directives.get(normalizeName(name));
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Parses one directive token.
     *
     * @param token      individual directive token, or {@code null} to ignore
     * @param directives insertion-ordered target map in which the first normalized name wins
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
     * @param directive       normalized directive key to read
     * @param valuelessResult value returned when the directive exists without a value
     * @return parsed non-negative seconds clamped to integer range, the valueless result, or {@code -1} when absent
     * @throws ProtocolException if the present value is negative or not an integer
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
     * @param value directive value to inspect
     * @return value without one matching pair of surrounding double quotes, or the unchanged value
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
     * @param name directive name to validate
     * @return trimmed lower-case directive name
     * @throws ValidateException if {@code name} is blank or contains a carriage return or line feed
     */
    private static String normalizeName(final String name) {
        if (StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Cache-Control directive must be non-blank and single-line");
        }
        return name.trim().toLowerCase(Locale.ROOT);
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
