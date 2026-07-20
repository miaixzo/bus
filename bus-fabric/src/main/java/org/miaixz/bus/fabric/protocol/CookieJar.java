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
package org.miaixz.bus.fabric.protocol;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpCookie;

/**
 * Thread-safe in-memory protocol cookie jar.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CookieJar {

    /**
     * Stored cookie snapshot.
     */
    private final List<Cookie> cookies;

    /**
     * Whether this jar accepts cookies.
     */
    private final boolean accepts;

    /**
     * Runtime clock used for all expiry decisions.
     */
    private final Clock clock;

    /**
     * Creates an empty jar.
     */
    private CookieJar() {
        this(true, Clock.system());
    }

    /**
     * Creates a jar.
     *
     * @param accepts whether cookies are accepted
     * @param clock   runtime clock
     */
    private CookieJar(final boolean accepts, final Clock clock) {
        this.cookies = new ArrayList<>();
        this.accepts = accepts;
        this.clock = require(clock, "Clock");
    }

    /**
     * Creates an in-memory cookie jar.
     *
     * @return cookie jar
     */
    public static CookieJar memory() {
        return memory(Clock.system());
    }

    /**
     * Creates an in-memory cookie jar using an explicit runtime clock.
     *
     * @param clock runtime clock
     * @return cookie jar
     */
    public static CookieJar memory(final Clock clock) {
        return new CookieJar(true, clock);
    }

    /**
     * Creates a cookie jar that never stores or returns cookies.
     *
     * @return cookie jar
     */
    public static CookieJar noCookies() {
        return new CookieJar(false, Clock.system());
    }

    /**
     * Saves valid cookies from response headers.
     *
     * @param url     response URL
     * @param headers response headers
     */
    public synchronized void save(final UnoUrl url, final Headers headers) {
        if (!accepts) {
            return;
        }
        final UnoUrl source = require(url, "URL");
        final Headers sourceHeaders = require(headers, "Headers");
        for (final String header : sourceHeaders.values(HTTP.SET_COOKIE)) {
            try {
                save(List.of(Cookie.parse(header, source)));
            } catch (final RuntimeException ignored) {
                // Invalid Set-Cookie values are ignored by the automatic store.
            }
        }
    }

    /**
     * Saves cookies, replacing existing entries with the same identity.
     *
     * @param values cookies
     */
    public synchronized void save(final List<Cookie> values) {
        if (!accepts) {
            return;
        }
        final List<Cookie> source = require(values, "Cookies");
        pruneExpired();
        for (final Cookie cookie : source) {
            final Cookie current = require(cookie, "Cookie");
            remove(current);
            if (!expired(current)) {
                cookies.add(current);
            }
        }
    }

    /**
     * Loads cookies matching a request URL.
     *
     * @param url request URL
     * @return immutable matching cookies
     */
    public synchronized List<Cookie> load(final UnoUrl url) {
        if (!accepts) {
            require(url, "URL");
            return List.of();
        }
        pruneExpired();
        return HttpCookie.match(require(url, "URL"), cookies);
    }

    /**
     * Saves cookies received from a response URL.
     *
     * @param url    response URL
     * @param values cookies
     */
    public void saveFromResponse(final UnoUrl url, final List<Cookie> values) {
        require(url, "URL");
        save(values);
    }

    /**
     * Loads cookies that should be sent for a request URL.
     *
     * @param url request URL
     * @return cookies
     */
    public List<Cookie> loadForRequest(final UnoUrl url) {
        return load(url);
    }

    /**
     * Returns all non-expired cookies.
     *
     * @return immutable cookies
     */
    public synchronized List<Cookie> all() {
        pruneExpired();
        return List.copyOf(cookies);
    }

    /**
     * Returns redacted non-expired cookie header values for logs and metrics.
     *
     * @return redacted cookie headers
     */
    public synchronized List<String> redactedHeaders() {
        pruneExpired();
        return cookies.stream().map(Cookie::redactedHeader).toList();
    }

    /**
     * Returns the non-expired cookie count.
     *
     * @return cookie count
     */
    public synchronized int size() {
        pruneExpired();
        return cookies.size();
    }

    /**
     * Clears all cookies.
     */
    public synchronized void clear() {
        cookies.clear();
    }

    /**
     * Removes a cookie with the same identity.
     *
     * @param cookie cookie
     */
    private void remove(final Cookie cookie) {
        cookies.removeIf(existing -> sameIdentity(existing, cookie));
    }

    /**
     * Removes expired cookies.
     */
    private void pruneExpired() {
        cookies.removeIf(this::expired);
    }

    /**
     * Returns whether a cookie has expired.
     *
     * @param cookie cookie
     * @return true when expired
     */
    private boolean expired(final Cookie cookie) {
        final Instant expires = cookie.expires();
        return expires != null && !clock.now().isBefore(expires);
    }

    /**
     * Returns whether two cookies share the same replacement identity.
     *
     * @param first  first cookie
     * @param second second cookie
     * @return true when identity matches
     */
    private static boolean sameIdentity(final Cookie first, final Cookie second) {
        if (!first.name().equals(second.name()) || !first.path().equals(second.path())) {
            return false;
        }
        if (first.domain() != null || second.domain() != null) {
            return equal(first.domain(), second.domain());
        }
        return equal(first.host(), second.host());
    }

    /**
     * Returns whether two nullable strings are equal.
     *
     * @param first  first value
     * @param second second value
     * @return true when equal
     */
    private static boolean equal(final String first, final String second) {
        return first == null ? second == null : first.equals(second);
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
