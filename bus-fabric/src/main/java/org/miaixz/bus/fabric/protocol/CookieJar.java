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
import org.miaixz.bus.core.net.Http;
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
     * Mutable collection of immutable cookies guarded by this jar's monitor.
     */
    private final List<Cookie> cookies;

    /**
     * Whether save and lookup operations use the in-memory collection.
     */
    private final boolean accepts;

    /**
     * Runtime clock used for all expiry decisions.
     */
    private final Clock clock;

    /** Lock-free empty-state hint that avoids monitor acquisition for the overwhelmingly common no-cookie path. */
    private volatile boolean empty = true;

    /**
     * Creates an accepting empty jar using the system clock.
     */
    private CookieJar() {
        this(true, Clock.system());
    }

    /**
     * Creates an empty jar with explicit acceptance and time behavior.
     *
     * @param accepts whether cookies are accepted
     * @param clock   clock used for expiration checks
     */
    private CookieJar(final boolean accepts, final Clock clock) {
        this.cookies = new ArrayList<>();
        this.accepts = accepts;
        this.clock = require(clock, "Clock");
    }

    /**
     * Creates an in-memory cookie jar.
     *
     * @return accepting empty jar using the system clock
     */
    public static CookieJar memory() {
        return memory(Clock.system());
    }

    /**
     * Creates an in-memory cookie jar using an explicit runtime clock.
     *
     * @param clock clock used for expiration checks
     * @return accepting empty jar using the supplied clock
     * @throws ValidateException if {@code clock} is {@code null}
     */
    public static CookieJar memory(final Clock clock) {
        return new CookieJar(true, clock);
    }

    /**
     * Creates a cookie jar that never stores or returns cookies.
     *
     * @return non-accepting jar using the system clock
     */
    public static CookieJar noCookies() {
        return new CookieJar(false, Clock.system());
    }

    /**
     * Parses each {@code Set-Cookie} response header and saves every valid result.
     * <p>
     * Individual malformed header values are ignored. A non-accepting jar ignores the complete call, including its
     * arguments.
     * </p>
     *
     * @param url     response URL used to derive cookie origin and defaults
     * @param headers response headers containing zero or more {@code Set-Cookie} values
     * @throws ValidateException if this jar accepts cookies and either argument is {@code null}
     */
    public void save(final UnoUrl url, final Headers headers) {
        if (!accepts) {
            return;
        }
        final UnoUrl source = require(url, "URL");
        final Headers sourceHeaders = require(headers, "Headers");
        if (!sourceHeaders.contains(Http.Header.SET_COOKIE)) {
            return;
        }
        synchronized (this) {
            for (final String header : sourceHeaders.values(Http.Header.SET_COOKIE)) {
                try {
                    save(List.of(Cookie.parse(header, source)));
                } catch (final RuntimeException ignored) {
                    // Invalid Set-Cookie values are ignored by the automatic store.
                }
            }
        }
    }

    /**
     * Prunes expired entries and saves cookies, replacing existing entries with the same name, path, and domain or host
     * identity.
     * <p>
     * Expired input cookies remove their previous identity without being retained. A non-accepting jar ignores the
     * complete call, including its argument.
     * </p>
     *
     * @param values cookies to validate and save in iteration order
     * @throws ValidateException if this jar accepts cookies and the list or one of its elements is {@code null}
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
        empty = cookies.isEmpty();
    }

    /**
     * Loads cookies matching a request URL.
     *
     * @param url request URL used for cookie domain, path, and security matching
     * @return immutable list of non-expired cookies applicable to the request, or an empty list for a non-accepting jar
     * @throws ValidateException if {@code url} is {@code null}
     */
    public List<Cookie> load(final UnoUrl url) {
        if (!accepts) {
            require(url, "URL");
            return List.of();
        }
        final UnoUrl source = require(url, "URL");
        if (empty) {
            return List.of();
        }
        synchronized (this) {
            pruneExpired();
            return HttpCookie.match(source, cookies);
        }
    }

    /**
     * Validates the response URL and delegates cookie replacement to {@link #save(List)}.
     *
     * @param url    response URL required by the protocol-facing contract
     * @param values cookies to save; ignored by a non-accepting jar
     * @throws ValidateException if {@code url} is {@code null}, or if an accepting jar receives a null list or element
     */
    public void saveFromResponse(final UnoUrl url, final List<Cookie> values) {
        require(url, "URL");
        save(values);
    }

    /**
     * Loads cookies that should be sent for a request URL.
     *
     * @param url request URL used for cookie matching
     * @return immutable list of non-expired cookies applicable to the request
     * @throws ValidateException if {@code url} is {@code null}
     */
    public List<Cookie> loadForRequest(final UnoUrl url) {
        return load(url);
    }

    /**
     * Returns all non-expired cookies.
     *
     * @return immutable snapshot of all cookies remaining after expiration pruning
     */
    public synchronized List<Cookie> all() {
        pruneExpired();
        return List.copyOf(cookies);
    }

    /**
     * Returns redacted non-expired cookie header values for logs and metrics.
     *
     * @return immutable list containing one redacted header representation per retained cookie
     */
    public synchronized List<String> redactedHeaders() {
        pruneExpired();
        return cookies.stream().map(Cookie::redactedHeader).toList();
    }

    /**
     * Returns the non-expired cookie count.
     *
     * @return number of cookies remaining after expiration pruning
     */
    public synchronized int size() {
        pruneExpired();
        return cookies.size();
    }

    /**
     * Returns the lock-free empty-state hint used by request fast paths.
     *
     * @return true when the jar currently contains no retained cookies
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Clears all cookies.
     */
    public synchronized void clear() {
        cookies.clear();
        empty = true;
    }

    /**
     * Removes a cookie with the same identity.
     *
     * @param cookie cookie whose replacement identity is removed
     */
    private void remove(final Cookie cookie) {
        cookies.removeIf(existing -> sameIdentity(existing, cookie));
    }

    /**
     * Removes expired cookies.
     */
    private void pruneExpired() {
        cookies.removeIf(this::expired);
        empty = cookies.isEmpty();
    }

    /**
     * Returns whether a cookie has expired.
     *
     * @param cookie cookie whose optional expiration instant is checked
     * @return {@code true} when the clock is at or after the cookie expiration instant
     */
    private boolean expired(final Cookie cookie) {
        final Instant expires = cookie.expires();
        return expires != null && !clock.now().isBefore(expires);
    }

    /**
     * Returns whether two cookies share the same replacement identity.
     *
     * @param first  retained cookie identity
     * @param second incoming cookie identity
     * @return {@code true} when name and path match and either domains match or both host-only origins match
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
     * @param first  first nullable string
     * @param second second nullable string
     * @return {@code true} when both values are null or equal by {@link String#equals(Object)}
     */
    private static boolean equal(final String first, final String second) {
        return first == null ? second == null : first.equals(second);
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
