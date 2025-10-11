/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.cache;

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;

import java.io.IOException;

/**
 * An internal interface for Http's cache. Applications should not implement this interface directly. Instead, they
 * should use {@link Cache} to configure a cache for an {@code Httpd} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface InternalCache {

    /**
     * Returns the cached response for the given {@code request}, or null if no cached response exists or is not
     * suitable.
     *
     * @param request The request to get the cached response for.
     * @return The cached response, or null.
     * @throws IOException if an I/O error occurs.
     */
    Response get(Request request) throws IOException;

    /**
     * Stores the given {@code response} in the cache and returns a {@link CacheRequest} to write the response body, or
     * null if the response cannot be cached.
     *
     * @param response The response to store.
     * @return A {@link CacheRequest} to write the response body, or null.
     * @throws IOException if an I/O error occurs.
     */
    CacheRequest put(Response response) throws IOException;

    /**
     * Removes any cached entries for the given {@code request}. This is called when the client invalidates the cache,
     * such as when making a POST, PUT, or DELETE request.
     *
     * @param request The request to remove from the cache.
     * @throws IOException if an I/O error occurs.
     */
    void remove(Request request) throws IOException;

    /**
     * Updates the stored cached response with headers from the new {@code network} response. This is called to handle
     * conditional requests. If the stored response has changed since {@code cached} was returned, this does nothing.
     *
     * @param cached  The stale cached response.
     * @param network The new network response.
     */
    void update(Response cached, Response network);

    /**
     * Tracks a conditional GET that was satisfied by this cache. This is used for statistics.
     */
    void trackConditionalCacheHit();

    /**
     * Tracks an HTTP response that was satisfied by the given {@code cacheStrategy}. This is used for statistics.
     *
     * @param cacheStrategy The cache strategy used for the response.
     */
    void trackResponse(CacheStrategy cacheStrategy);

}
