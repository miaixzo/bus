package org.aoju.bus.http.internal.cache;

import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;

import java.io.IOException;

/**
 * internal cache interface. Applications shouldn't implement this: instead use
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface InternalCache {
    Response get(Request request) throws IOException;

    CacheRequest put(Response response) throws IOException;

    /**
     * Remove any cache entries for the supplied {@code request}. This is invoked when the client
     * invalidates the cache, such as when making POST requests.
     */
    void remove(Request request) throws IOException;

    /**
     * Handles a conditional request hit by updating the stored cache response with the headers from
     * {@code network}. The cached response body is not updated. If the stored response has changed
     * since {@code cached} was returned, this does nothing.
     */
    void update(Response cached, Response network);

    /**
     * Track an conditional GET that was satisfied by this cache.
     */
    void trackConditionalCacheHit();

    /**
     * Track an HTTP response being satisfied with {@code cacheStrategy}.
     */
    void trackResponse(CacheStrategy cacheStrategy);
}
