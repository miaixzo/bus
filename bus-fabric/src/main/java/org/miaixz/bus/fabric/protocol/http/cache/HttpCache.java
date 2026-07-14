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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.cache.CacheEntry;
import org.miaixz.bus.fabric.cache.CacheStore;
import org.miaixz.bus.fabric.cache.CacheWriter;
import org.miaixz.bus.fabric.cache.DiskStore;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.observe.tags.Tags;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP cache orchestration over a store and policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCache implements AutoCloseable {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Cache store.
     */
    private final CacheStore store;

    /**
     * Cache policy.
     */
    private final CachePolicy policy;

    /**
     * Event observer.
     */
    private final EventObserver observer;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Cache strategy request count.
     */
    private final AtomicLong requestCount;

    /**
     * Network response count.
     */
    private final AtomicLong networkCount;

    /**
     * Cache hit count.
     */
    private final AtomicLong hitCount;

    /**
     * Cache write success count.
     */
    private final AtomicLong writeSuccessCount;

    /**
     * Cache write abort count.
     */
    private final AtomicLong writeAbortCount;

    /**
     * Creates a cache.
     *
     * @param store    store
     * @param policy   policy
     * @param observer observer
     */
    private HttpCache(final CacheStore store, final CachePolicy policy, final EventObserver observer) {
        this.store = require(store, "Cache store");
        this.policy = require(policy, "Cache policy");
        this.observer = EventObserver.safe(require(observer, "Event observer"));
        this.state = new AtomicReference<>(Status.OPENED);
        this.requestCount = new AtomicLong();
        this.networkCount = new AtomicLong();
        this.hitCount = new AtomicLong();
        this.writeSuccessCount = new AtomicLong();
        this.writeAbortCount = new AtomicLong();
    }

    /**
     * Creates a cache.
     *
     * @param store    store
     * @param policy   policy
     * @param observer observer
     * @return cache
     */
    public static HttpCache create(final CacheStore store, final CachePolicy policy, final EventObserver observer) {
        final HttpCache cache = new HttpCache(store, policy, observer);
        Logger.info(
                false,
                LOG_TAG,
                "HTTP cache created: store={}, policy={}",
                store == null ? "null" : store.getClass().getName(),
                policy == null ? "null" : policy.getClass().getName());
        return cache;
    }

    /**
     * Initializes the underlying cache store when supported.
     */
    public void initialize() {
        ensureOpen();
        Logger.info(true, LOG_TAG, "HTTP cache initialization started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.initialize();
        }
        Logger.info(false, LOG_TAG, "HTTP cache initialization completed: store={}", store.getClass().getName());
    }

    /**
     * Returns cached URL snapshots.
     *
     * @return URLs
     */
    public Iterator<String> urls() {
        ensureOpen();
        final ArrayList<String> urls = new ArrayList<>();
        final Iterator<String> keys = store.keys();
        while (keys.hasNext()) {
            final CacheEntry entry = store.get(keys.next());
            if (entry == null) {
                continue;
            }
            try (HttpResponse response = HttpCacheCodec.fromEntry(entry)) {
                urls.add(response.request().url().encoded());
            }
        }
        return urls.iterator();
    }

    /**
     * Returns current store size in bytes.
     *
     * @return size, or -1 when unsupported
     */
    public long size() {
        ensureOpen();
        return store instanceof DiskStore disk ? disk.size() : -1L;
    }

    /**
     * Returns maximum store size in bytes.
     *
     * @return max size, or -1 when unsupported
     */
    public long maxSize() {
        ensureOpen();
        return store instanceof DiskStore disk ? disk.maxSize() : -1L;
    }

    /**
     * Returns cache directory.
     *
     * @return directory, or null when unsupported
     */
    public Path directory() {
        ensureOpen();
        return store instanceof DiskStore disk ? disk.directory() : null;
    }

    /**
     * Returns a statistics snapshot.
     *
     * @return stats
     */
    public HttpCacheStats stats() {
        return HttpCacheStats.of(
                requestCount.get(),
                networkCount.get(),
                hitCount.get(),
                writeSuccessCount.get(),
                writeAbortCount.get());
    }

    /**
     * Returns request count.
     *
     * @return request count
     */
    public long requestCount() {
        return requestCount.get();
    }

    /**
     * Returns network count.
     *
     * @return network count
     */
    public long networkCount() {
        return networkCount.get();
    }

    /**
     * Returns hit count.
     *
     * @return hit count
     */
    public long hitCount() {
        return hitCount.get();
    }

    /**
     * Returns write success count.
     *
     * @return write success count
     */
    public long writeSuccessCount() {
        return writeSuccessCount.get();
    }

    /**
     * Returns write abort count.
     *
     * @return write abort count
     */
    public long writeAbortCount() {
        return writeAbortCount.get();
    }

    /**
     * Gets a cached response.
     *
     * @param request request
     * @return response or null
     */
    public HttpResponse get(final HttpRequest request) {
        require(request, "HTTP request");
        final String prefix = HttpCacheKey.baseKey(request);
        final var keys = store.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (!key.startsWith(prefix)) {
                continue;
            }
            final CacheEntry entry = store.get(key);
            final HttpResponse cached = entry == null ? null : HttpCacheCodec.fromEntry(entry);
            if (cached != null && HttpCacheKey.varyMatches(cached, request)) {
                emit("hit", key);
                Logger.info(
                        false,
                        LOG_TAG,
                        "HTTP cache hit: key={}, method={}, scheme={}, host={}, port={}, path={}",
                        keyHash(key),
                        request.method().value(),
                        request.url().scheme(),
                        request.url().host(),
                        request.url().port(),
                        request.url().path());
                return HttpCacheCodec.copyResponse(request, cached);
            }
            if (cached != null) {
                cached.close();
            }
        }
        emit("miss", prefix);
        Logger.info(
                false,
                LOG_TAG,
                "HTTP cache miss: key={}, method={}, scheme={}, host={}, port={}, path={}",
                keyHash(prefix),
                request.method().value(),
                request.url().scheme(),
                request.url().host(),
                request.url().port(),
                request.url().path());
        return null;
    }

    /**
     * Puts a response.
     *
     * @param request  request
     * @param response response
     */
    public void put(final HttpRequest request, final HttpResponse response) {
        require(request, "HTTP request");
        require(response, "HTTP response");
        if (!policy.cacheable(request, response)) {
            HttpCachePurge.remove(store, request, removed -> emit("remove", removed));
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP cache skipped: reason=not-cacheable, method={}, scheme={}, host={}, port={}, path={}, "
                            + "code={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path(),
                    response.code());
            return;
        }
        final String key = HttpCacheKey.key(request, response.headers().get(HTTP.VARY));
        store.put(key, HttpCacheCodec.toEntry(request, response));
        recordWriteSuccess();
        emit("write", key);
        Logger.info(false, LOG_TAG, "HTTP cache stored: key={}, code={}", keyHash(key), response.code());
    }

    /**
     * Returns a response that writes cacheable one-shot bodies to the cache as callers consume them.
     *
     * @param request  request
     * @param response network response
     * @return original or cache-writing response
     */
    public HttpResponse write(final HttpRequest request, final HttpResponse response) {
        require(request, "HTTP request");
        require(response, "HTTP response");
        if (!policy.cacheable(request, response)) {
            HttpCachePurge.remove(store, request, removed -> emit("remove", removed));
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP cache write skipped: reason=not-cacheable, method={}, scheme={}, host={}, port={}, path={}, "
                            + "code={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path(),
                    response.code());
            return response;
        }
        final String key = HttpCacheKey.key(request, response.headers().get(HTTP.VARY));
        if (response.body().payload().repeatable()) {
            store.put(key, HttpCacheCodec.toEntry(request, response));
            recordWriteSuccess();
            emit("write", key);
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP cache stored: key={}, code={}, repeatable={}",
                    keyHash(key),
                    response.code(),
                    true);
            return response;
        }
        final CacheWriter writer = store.writer(key, HttpCacheCodec.toEntry(request, response, Payload.empty()));
        if (writer == null) {
            Logger.warn(
                    false,
                    LOG_TAG,
                    "HTTP cache writer unavailable: key={}, code={}",
                    keyHash(key),
                    response.code());
            return response;
        }
        final PayloadBody body = PayloadBody.of(new HttpCacheWriter(response.body().payload(), writer, () -> {
            recordWriteSuccess();
            emit("write", key);
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP cache streamed body stored: key={}, code={}",
                    keyHash(key),
                    response.code());
        }, () -> {
            recordWriteAbort();
            emit("abort", key);
            Logger.warn(
                    false,
                    LOG_TAG,
                    "HTTP cache streamed body aborted: key={}, code={}",
                    keyHash(key),
                    response.code());
        }), response.body().media());
        return HttpCacheCodec.copyResponse(request, response, body);
    }

    /**
     * Returns whether a response may be cached by this cache policy.
     *
     * @param request  request
     * @param response response
     * @return true when cacheable
     */
    public boolean cacheable(final HttpRequest request, final HttpResponse response) {
        return policy.cacheable(require(request, "HTTP request"), require(response, "HTTP response"));
    }

    /**
     * Returns whether a cached response is still fresh.
     *
     * @param response response
     * @param clock    clock
     * @return true when fresh
     */
    public boolean fresh(final HttpResponse response, final Clock clock) {
        return policy.fresh(require(response, "HTTP response"), require(clock, "Runtime clock"));
    }

    /**
     * Returns whether a cached response is fresh enough for a request.
     *
     * @param request  request
     * @param response response
     * @param clock    clock
     * @return true when fresh
     */
    public boolean fresh(final HttpRequest request, final HttpResponse response, final Clock clock) {
        return policy.fresh(
                require(request, "HTTP request"),
                require(response, "HTTP response"),
                require(clock, "Runtime clock"));
    }

    /**
     * Creates a conditional request for stale cached metadata.
     *
     * @param request request
     * @param cached  cached response
     * @return conditional request
     */
    public HttpRequest conditional(final HttpRequest request, final HttpResponse cached) {
        return policy.conditional(require(request, "HTTP request"), require(cached, "Cached response"));
    }

    /**
     * Removes a cached response.
     *
     * @param request request
     */
    public void remove(final HttpRequest request) {
        require(request, "HTTP request");
        HttpCachePurge.remove(store, request, key -> emit("remove", key));
        Logger.info(
                false,
                LOG_TAG,
                "HTTP cache removal requested: method={}, scheme={}, host={}, port={}, path={}",
                request.method().value(),
                request.url().scheme(),
                request.url().host(),
                request.url().port(),
                request.url().path());
    }

    /**
     * Evicts all cache entries while keeping the cache open.
     */
    public void evictAll() {
        ensureOpen();
        Logger.info(true, LOG_TAG, "HTTP cache eviction started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.evictAll();
            Logger.info(false, LOG_TAG, "HTTP cache eviction completed: store={}", store.getClass().getName());
            return;
        }
        final List<String> keys = new ArrayList<>();
        store.keys().forEachRemaining(keys::add);
        keys.forEach(store::remove);
        Logger.info(
                false,
                LOG_TAG,
                "HTTP cache eviction completed: store={}, entries={}",
                store.getClass().getName(),
                keys.size());
    }

    /**
     * Flushes store state when supported.
     */
    public void flush() {
        ensureOpen();
        Logger.info(true, LOG_TAG, "HTTP cache flush started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.flush();
        }
        Logger.info(false, LOG_TAG, "HTTP cache flush completed: store={}", store.getClass().getName());
    }

    /**
     * Deletes all cache files and closes this cache when supported.
     */
    public void delete() {
        ensureOpen();
        Logger.info(true, LOG_TAG, "HTTP cache delete started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.delete();
            state.set(Status.CLOSED);
            Logger.info(false, LOG_TAG, "HTTP cache delete completed: store={}", store.getClass().getName());
            return;
        }
        evictAll();
        Logger.info(false, LOG_TAG, "HTTP cache delete completed: store={}", store.getClass().getName());
    }

    /**
     * Updates cached response with a network response.
     *
     * @param cached  cached response
     * @param network network response
     * @return response
     */
    public HttpResponse update(final HttpResponse cached, final HttpResponse network) {
        require(cached, "Cached response");
        require(network, "Network response");
        if (network.code() != HTTP.HTTP_NOT_MODIFIED) {
            cached.close();
            emit("update", Integer.toString(network.code()));
            Logger.info(false, LOG_TAG, "HTTP cache update bypassed: code={}", network.code());
            return network;
        }
        final Headers headers = HttpCacheCodec.mergeHeaders(cached.headers(), network.headers());
        final HttpResponse merged = cached.toBuilder().request(network.request()).headers(headers)
                .body(HttpCacheCodec.copyBody(cached)).protocol(network.protocol()).handshake(network.handshake())
                .networkResponse(network).cacheResponse(cached).sentRequestAtMillis(network.sentRequestAtMillis())
                .receivedResponseAtMillis(network.receivedResponseAtMillis()).build();
        emit("update", Integer.toString(HTTP.HTTP_NOT_MODIFIED));
        Logger.info(false, LOG_TAG, "HTTP cache conditional hit: code={}", network.code());
        return merged;
    }

    /**
     * Closes this cache.
     */
    @Override
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("HTTP cache cannot close from state " + current);
        }
        state.set(Status.CLOSING);
        RuntimeException failure = null;
        try {
            store.close();
        } catch (final RuntimeException e) {
            failure = e;
        }
        state.set(Status.CLOSED);
        if (failure != null) {
            Logger.error(
                    false,
                    LOG_TAG,
                    failure,
                    "HTTP cache close failed: store={}, exception={}",
                    store.getClass().getName(),
                    failure.getClass().getSimpleName());
            throw failure instanceof InternalException internal ? internal
                    : new InternalException("Unable to close cache", failure);
        }
        Logger.info(false, LOG_TAG, "HTTP cache closed: store={}", store.getClass().getName());
    }

    /**
     * Emits cache event.
     *
     * @param action action
     * @param key    key
     */
    private void emit(final String action, final String key) {
        observer.emit(
                FabricEvent.builder(cacheMarker(action)).tag(Tags.CACHE, action).tag(Tags.KEY, keyHash(key)).build());
    }

    /**
     * Returns a stable non-sensitive cache key hash for logs.
     *
     * @param key cache key
     * @return hexadecimal key hash
     */
    private static String keyHash(final String key) {
        return Integer.toHexString(key.hashCode());
    }

    /**
     * Maps a cache action to a stable observation marker.
     *
     * @param action cache action
     * @return marker
     */
    private static ObservationMarker cacheMarker(final String action) {
        return switch (action) {
            case "hit" -> ObservationMarker.CACHE_HIT;
            case "miss" -> ObservationMarker.CACHE_MISS;
            case "update" -> ObservationMarker.CACHE_CONDITIONAL_HIT;
            default -> ObservationMarker.HTTP_RESPONSE;
        };
    }

    /**
     * Records a cache strategy request.
     */
    public void recordRequest() {
        requestCount.incrementAndGet();
    }

    /**
     * Records a network response.
     */
    public void recordNetwork() {
        networkCount.incrementAndGet();
    }

    /**
     * Records a cache hit.
     */
    public void recordHit() {
        hitCount.incrementAndGet();
    }

    /**
     * Records a successful cache write.
     */
    public void recordWriteSuccess() {
        writeSuccessCount.incrementAndGet();
    }

    /**
     * Records an aborted cache write.
     */
    public void recordWriteAbort() {
        writeAbortCount.incrementAndGet();
    }

    /**
     * Ensures this cache is open.
     */
    private void ensureOpen() {
        if (state.get() == Status.CLOSED) {
            throw new StatefulException("HTTP cache is closed");
        }
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
