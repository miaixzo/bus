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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Builder;
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
     * Runtime clock used for cache observations, or null for legacy event-disabled construction.
     */
    private final Clock clock;

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
     * Cache miss count.
     */
    private final AtomicLong missCount;

    /**
     * Corrupt candidate count.
     */
    private final AtomicLong corruptionCount;

    /**
     * Cache write success count.
     */
    private final AtomicLong writeSuccessCount;

    /**
     * Cache write abort count.
     */
    private final AtomicLong writeAbortCount;

    /**
     * Cache write failure count.
     */
    private final AtomicLong writeFailureCount;

    /**
     * Creates a cache.
     *
     * @param store    persistent or in-memory cache store
     * @param policy   policy deciding cacheability and freshness
     * @param observer observer receiving cache events
     * @param clock    event timestamp source, or {@code null} to disable event emission
     */
    private HttpCache(final CacheStore store, final CachePolicy policy, final EventObserver observer,
            final Clock clock) {
        this.store = require(store, "Cache store");
        this.policy = require(policy, "Cache policy");
        this.observer = EventObserver.safe(require(observer, "Event observer"));
        this.clock = clock;
        this.state = new AtomicReference<>(Status.OPENED);
        this.requestCount = new AtomicLong();
        this.networkCount = new AtomicLong();
        this.hitCount = new AtomicLong();
        this.missCount = new AtomicLong();
        this.corruptionCount = new AtomicLong();
        this.writeSuccessCount = new AtomicLong();
        this.writeAbortCount = new AtomicLong();
        this.writeFailureCount = new AtomicLong();
    }

    /**
     * Creates a cache.
     *
     * @param store    persistent or in-memory cache store
     * @param policy   policy deciding cacheability and freshness
     * @param observer observer receiving cache events
     * @return cache without timestamped event emission
     */
    public static HttpCache create(final CacheStore store, final CachePolicy policy, final EventObserver observer) {
        return created(new HttpCache(store, policy, observer, null), store, policy);
    }

    /**
     * Creates a cache with an explicit runtime clock for observations.
     *
     * @param store    persistent or in-memory cache store
     * @param policy   policy deciding cacheability and freshness
     * @param observer observer receiving cache events
     * @param clock    runtime clock used for event timestamps
     * @return cache using the supplied runtime clock
     */
    public static HttpCache create(
            final CacheStore store,
            final CachePolicy policy,
            final EventObserver observer,
            final Clock clock) {
        return created(new HttpCache(store, policy, observer, require(clock, "Clock")), store, policy);
    }

    /**
     * Logs one completed cache construction.
     *
     * @param cache  fully constructed cache to return
     * @param store  configured store reported in the log
     * @param policy configured policy reported in the log
     * @return the supplied cache
     */
    private static HttpCache created(final HttpCache cache, final CacheStore store, final CachePolicy policy) {
        Logger.info(
                false,
                "Fabric",
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
        Logger.info(true, "Fabric", "HTTP cache initialization started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.initialize();
        }
        Logger.info(false, "Fabric", "HTTP cache initialization completed: store={}", store.getClass().getName());
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
                missCount.get(),
                corruptionCount.get(),
                writeSuccessCount.get(),
                writeAbortCount.get(),
                writeFailureCount.get());
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
     * Returns cache miss count.
     *
     * @return miss count
     */
    public long missCount() {
        return missCount.get();
    }

    /**
     * Returns corrupt candidate count.
     *
     * @return corruption count
     */
    public long corruptionCount() {
        return corruptionCount.get();
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
     * Returns cache write failure count.
     *
     * @return write failure count
     */
    public long writeFailureCount() {
        return writeFailureCount.get();
    }

    /**
     * Gets a cached response.
     *
     * @param request request used to calculate the base key and match Vary fields
     * @return cache-backed response, or {@code null} on a miss
     */
    public HttpResponse get(final HttpRequest request) {
        require(request, "HTTP request");
        ensureOpen();
        final String prefix = HttpCacheKey.baseKey(request);
        final var keys = store.keys();
        boolean missRecorded = false;
        while (keys.hasNext()) {
            final String key = keys.next();
            if (!key.startsWith(prefix)) {
                continue;
            }
            HttpResponse cached = null;
            try {
                final CacheEntry entry = store.get(key);
                if (entry == null) {
                    continue;
                }
                cached = readCandidate(entry);
                if (!HttpCacheKey.varyMatches(cached, request)) {
                    cached.close();
                    continue;
                }
                final HttpResponse result = HttpCacheCodec.copyResponse(request, cached);
                emit("hit", key);
                Logger.info(
                        false,
                        "Fabric",
                        "HTTP cache hit: key={}, method={}, scheme={}, host={}, port={}, path={}",
                        keyHash(key),
                        request.method().value(),
                        request.url().scheme(),
                        request.url().host(),
                        request.url().port(),
                        request.url().path());
                return result;
            } catch (final RuntimeException e) {
                closeQuietly(cached);
                recordCorruption();
                if (!missRecorded) {
                    recordMiss();
                    missRecorded = true;
                }
                removeCorrupt(key);
                emit("corruption", key);
                Logger.warn(
                        false,
                        "Fabric",
                        e,
                        "HTTP cache candidate discarded: key={}, exception={}",
                        keyHash(key),
                        e.getClass().getSimpleName());
            }
        }
        if (!missRecorded) {
            recordMiss();
        }
        emit("miss", prefix);
        Logger.info(
                false,
                "Fabric",
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
     * @param request  originating request used to calculate the cache key
     * @param response complete response to store when cacheable
     */
    public void put(final HttpRequest request, final HttpResponse response) {
        require(request, "HTTP request");
        require(response, "HTTP response");
        if (!policy.cacheable(request, response)) {
            HttpCachePurge.remove(store, request, removed -> emit("remove", removed));
            Logger.info(
                    false,
                    "Fabric",
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
        final String key = HttpCacheKey.key(request, response.headers().get(Http.Header.VARY));
        try {
            store.put(key, HttpCacheCodec.toEntry(request, response));
            recordWriteSuccess();
            emit("write", key);
            Logger.info(false, "Fabric", "HTTP cache stored: key={}, code={}", keyHash(key), response.code());
        } catch (final RuntimeException e) {
            recordWriteFailure();
            emit("write-failure", key);
            Logger.warn(false, "Fabric", e, "HTTP cache store failed: key={}", keyHash(key));
        }
    }

    /**
     * Returns a response that writes cacheable one-shot bodies to the cache as callers consume them.
     *
     * @param request  originating request used to calculate the cache key
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
                    "Fabric",
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
        final String key = HttpCacheKey.key(request, response.headers().get(Http.Header.VARY));
        if (response.body().payload().repeatable()) {
            try {
                store.put(key, HttpCacheCodec.toEntry(request, response));
                recordWriteSuccess();
                emit("write", key);
                Logger.info(
                        false,
                        "Fabric",
                        "HTTP cache stored: key={}, code={}, repeatable={}",
                        keyHash(key),
                        response.code(),
                        true);
            } catch (final RuntimeException e) {
                recordWriteFailure();
                emit("write-failure", key);
                Logger.warn(false, "Fabric", e, "HTTP cache repeatable write failed: key={}", keyHash(key));
            }
            return response;
        }
        final CacheWriter writer;
        try {
            writer = store.writer(key, HttpCacheCodec.toEntry(request, response, Payload.empty()));
        } catch (final RuntimeException e) {
            recordWriteFailure();
            emit("write-failure", key);
            Logger.warn(false, "Fabric", e, "HTTP cache writer creation failed: key={}", keyHash(key));
            return response;
        }
        if (writer == null) {
            recordWriteFailure();
            emit("write-failure", key);
            Logger.warn(
                    false,
                    "Fabric",
                    "HTTP cache writer unavailable: key={}, code={}",
                    keyHash(key),
                    response.code());
            return response;
        }
        final IsolatedCacheWriter isolated = new IsolatedCacheWriter(writer, () -> {
            recordWriteFailure();
            emit("write-failure", key);
        });
        try {
            final PayloadBody body = PayloadBody.of(new HttpCacheWriter(response.body().payload(), isolated, () -> {
                if (!isolated.failed()) {
                    recordWriteSuccess();
                    emit("write", key);
                    Logger.info(
                            false,
                            "Fabric",
                            "HTTP cache streamed body stored: key={}, code={}",
                            keyHash(key),
                            response.code());
                }
            }, () -> {
                if (!isolated.failed()) {
                    recordWriteAbort();
                    emit("abort", key);
                    Logger.warn(
                            false,
                            "Fabric",
                            "HTTP cache streamed body aborted: key={}, code={}",
                            keyHash(key),
                            response.code());
                }
            }), response.body().media());
            return HttpCacheCodec.copyResponse(request, response, body);
        } catch (final RuntimeException e) {
            isolated.fail(e);
            return response;
        }
    }

    /**
     * Returns whether a response may be cached by this cache policy.
     *
     * @param request  request evaluated by the cache policy
     * @param response response evaluated by the cache policy
     * @return true when cacheable
     */
    public boolean cacheable(final HttpRequest request, final HttpResponse response) {
        return policy.cacheable(require(request, "HTTP request"), require(response, "HTTP response"));
    }

    /**
     * Returns whether a cached response is still fresh.
     *
     * @param response cached response whose age is evaluated
     * @param clock    time source used for freshness calculation
     * @return true when fresh
     */
    public boolean fresh(final HttpResponse response, final Clock clock) {
        final boolean fresh = policy.fresh(require(response, "HTTP response"), require(clock, "Runtime clock"));
        if (!fresh) {
            recordMiss();
        }
        return fresh;
    }

    /**
     * Returns whether a cached response is fresh enough for a request.
     *
     * @param request  request whose cache directives affect freshness
     * @param response cached response whose age is evaluated
     * @param clock    time source used for freshness calculation
     * @return true when fresh
     */
    public boolean fresh(final HttpRequest request, final HttpResponse response, final Clock clock) {
        final boolean fresh = policy.fresh(
                require(request, "HTTP request"),
                require(response, "HTTP response"),
                require(clock, "Runtime clock"));
        if (!fresh) {
            recordMiss();
        }
        return fresh;
    }

    /**
     * Creates a conditional request for stale cached metadata.
     *
     * @param request stale request to convert into a validator request
     * @param cached  cached response
     * @return conditional request
     */
    public HttpRequest conditional(final HttpRequest request, final HttpResponse cached) {
        return policy.conditional(require(request, "HTTP request"), require(cached, "Cached response"));
    }

    /**
     * Removes a cached response.
     *
     * @param request request identifying entries to remove
     */
    public void remove(final HttpRequest request) {
        require(request, "HTTP request");
        HttpCachePurge.remove(store, request, key -> emit("remove", key));
        Logger.info(
                false,
                "Fabric",
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
        Logger.info(true, "Fabric", "HTTP cache eviction started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.evictAll();
            Logger.info(false, "Fabric", "HTTP cache eviction completed: store={}", store.getClass().getName());
            return;
        }
        final List<String> keys = new ArrayList<>();
        store.keys().forEachRemaining(keys::add);
        keys.forEach(store::remove);
        Logger.info(
                false,
                "Fabric",
                "HTTP cache eviction completed: store={}, entries={}",
                store.getClass().getName(),
                keys.size());
    }

    /**
     * Flushes store state when supported.
     */
    public void flush() {
        ensureOpen();
        Logger.info(true, "Fabric", "HTTP cache flush started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.flush();
        }
        Logger.info(false, "Fabric", "HTTP cache flush completed: store={}", store.getClass().getName());
    }

    /**
     * Deletes all cache files and closes this cache when supported.
     */
    public void delete() {
        ensureOpen();
        Logger.info(true, "Fabric", "HTTP cache delete started: store={}", store.getClass().getName());
        if (store instanceof DiskStore disk) {
            disk.delete();
            state.set(Status.CLOSED);
            Logger.info(false, "Fabric", "HTTP cache delete completed: store={}", store.getClass().getName());
            return;
        }
        evictAll();
        Logger.info(false, "Fabric", "HTTP cache delete completed: store={}", store.getClass().getName());
    }

    /**
     * Updates cached response with a network response.
     *
     * @param cached  cached response
     * @param network network response
     * @return merged cached response for HTTP 304, otherwise the network response
     */
    public HttpResponse update(final HttpResponse cached, final HttpResponse network) {
        require(cached, "Cached response");
        require(network, "Network response");
        if (network.code() != Http.Status.NOT_MODIFIED) {
            cached.close();
            emit("update", Integer.toString(network.code()));
            Logger.info(false, "Fabric", "HTTP cache update bypassed: code={}", network.code());
            return network;
        }
        final Headers headers = HttpCacheCodec.mergeHeaders(cached.headers(), network.headers());
        final HttpResponse merged = cached.toBuilder().request(network.request()).headers(headers)
                .body(HttpCacheCodec.copyBody(cached)).protocol(network.protocol()).handshake(network.handshake())
                .networkResponse(network).cacheResponse(cached).sentRequestAtMillis(network.sentRequestAtMillis())
                .receivedResponseAtMillis(network.receivedResponseAtMillis()).build();
        emit("update", Integer.toString(Http.Status.NOT_MODIFIED));
        Logger.info(false, "Fabric", "HTTP cache conditional hit: code={}", network.code());
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
                    "Fabric",
                    failure,
                    "HTTP cache close failed: store={}, exception={}",
                    store.getClass().getName(),
                    failure.getClass().getSimpleName());
            throw failure instanceof InternalException internal ? internal
                    : new InternalException("Unable to close cache", failure);
        }
        Logger.info(false, "Fabric", "HTTP cache closed: store={}", store.getClass().getName());
    }

    /**
     * Emits cache event.
     *
     * @param action stable cache action name
     * @param key    cache key used for non-sensitive event correlation
     */
    private void emit(final String action, final String key) {
        if (clock == null) {
            return;
        }
        observer.emit(
                FabricEvent.builder(cacheMarker(action), clock).tag(Builder.TAG_OPERATION_ID, keyHash(key))
                        .tag(Builder.TAG_CACHE, action).tag(Builder.TAG_KEY, keyHash(key)).build());
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
     * @return observation marker corresponding to the cache action
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
     * Records a cache miss.
     */
    public void recordMiss() {
        missCount.incrementAndGet();
    }

    /**
     * Records one corrupt cache candidate.
     */
    public void recordCorruption() {
        corruptionCount.incrementAndGet();
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
     * Records a cache write failure.
     */
    public void recordWriteFailure() {
        writeFailureCount.incrementAndGet();
    }

    /**
     * Opens and validates a cache candidate while preserving snapshot ownership in the returned body.
     *
     * @param entry cache entry whose metadata and body are opened
     * @return decoded response retaining ownership of the opened cache body
     */
    private static HttpResponse readCandidate(final CacheEntry entry) {
        final CacheEntry current = require(entry, "Cache entry");
        OpenedPayload opened = null;
        try {
            final Payload payload = require(current.payload(), "Cache payload");
            final long actualLength = payload.length();
            if (actualLength < -1L) {
                throw new StatefulException("Cached payload length is invalid");
            }
            final Source source = require(payload.source(), "Cache body source");
            opened = new OpenedPayload(source, actualLength, payload instanceof AutoCloseable owner ? owner : source);
            final HttpResponse response = HttpCacheCodec.fromEntry(CacheEntry.of(current.metadata(), opened));
            final long declaredLength = response.headers().contentLength();
            if (declaredLength >= 0L && actualLength >= 0L && declaredLength != actualLength) {
                response.close();
                throw new StatefulException("Cached body length does not match Content-Length");
            }
            return response;
        } catch (final RuntimeException e) {
            if (opened != null) {
                closeQuietly(opened);
            } else if (current.payload() instanceof AutoCloseable owner) {
                closeQuietly(owner);
            }
            throw e;
        }
    }

    /**
     * Removes one corrupt key without letting cleanup hide the original candidate failure.
     *
     * @param key corrupt cache key to remove
     */
    private void removeCorrupt(final String key) {
        try {
            store.remove(key);
        } catch (final RuntimeException e) {
            Logger.warn(false, "Fabric", e, "HTTP corrupt cache removal failed: key={}", keyHash(key));
        }
    }

    /**
     * Closes a response without allowing cleanup failure to escape candidate isolation.
     *
     * @param response response to close, or {@code null}
     */
    private static void closeQuietly(final HttpResponse response) {
        closeQuietly((AutoCloseable) response);
    }

    /**
     * Closes a resource without allowing cleanup failure to escape candidate isolation.
     *
     * @param closeable resource to close, or {@code null}
     */
    private static void closeQuietly(final AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (final Exception ignored) {
            // Candidate isolation keeps cleanup failure local to the corrupt entry.
        }
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
     * @param value reference to validate
     * @param name  diagnostic parameter name
     * @param <T>   type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Open cache payload that closes the source and its owning snapshot together.
     */
    private static final class OpenedPayload implements Payload, AutoCloseable {

        /**
         * Open body source.
         */
        private final Source source;

        /**
         * Declared stored body length.
         */
        private final long length;

        /**
         * Source or snapshot owner.
         */
        private final AutoCloseable owner;

        /**
         * Source-open guard.
         */
        private final AtomicBoolean opened;

        /**
         * Close guard.
         */
        private final AtomicBoolean closed;

        /**
         * Creates an opened cache payload.
         *
         * @param source already-open cache body source
         * @param length stored body length, or {@code -1} when unknown
         * @param owner  resource owning the source and any backing snapshot
         */
        private OpenedPayload(final Source source, final long length, final AutoCloseable owner) {
            this.source = require(source, "Cache body source");
            this.length = length;
            this.owner = require(owner, "Cache body owner");
            this.opened = new AtomicBoolean();
            this.closed = new AtomicBoolean();
        }

        /**
         * Returns stored body length.
         *
         * @return stored body length, or {@code -1} when unknown
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Transfers the already-open source once.
         *
         * @return owned cache body source on its first request
         */
        @Override
        public Source source() {
            if (!opened.compareAndSet(false, true)) {
                throw new StatefulException("Cached body source can only be opened once");
            }
            return source;
        }

        /**
         * Returns whether this payload can be reopened.
         *
         * @return {@code false} because the opened source is transferable only once
         */
        @Override
        public boolean repeatable() {
            return false;
        }

        /**
         * Closes the source owner exactly once.
         */
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                owner.close();
            } catch (final IOException e) {
                throw new InternalException("Unable to close cached body", e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new InternalException("Unable to close cached body", e);
            }
        }

    }

    /**
     * Cache writer facade that prevents cache I/O failures from breaking the network response body.
     */
    private static final class IsolatedCacheWriter implements CacheWriter {

        /**
         * Store writer.
         */
        private final CacheWriter delegate;

        /**
         * Failure callback.
         */
        private final Runnable failureCallback;

        /**
         * Failure guard.
         */
        private final AtomicBoolean failed;

        /**
         * Creates an isolated writer.
         *
         * @param delegate        store writer isolated from network-body failures
         * @param failureCallback action invoked for the first cache write failure
         */
        private IsolatedCacheWriter(final CacheWriter delegate, final Runnable failureCallback) {
            this.delegate = require(delegate, "Cache writer");
            this.failureCallback = require(failureCallback, "Cache writer failure callback");
            this.failed = new AtomicBoolean();
        }

        /**
         * Returns the delegate body or an in-memory discard sink after failure.
         *
         * @return delegate sink before failure, otherwise an in-memory discard sink
         */
        @Override
        public Sink body() {
            if (failed.get()) {
                return new org.miaixz.bus.core.io.buffer.Buffer();
            }
            try {
                return delegate.body();
            } catch (final RuntimeException e) {
                fail(e);
                return new org.miaixz.bus.core.io.buffer.Buffer();
            }
        }

        /**
         * Writes cache bytes while isolating store failure.
         *
         * @param source    network bytes to copy or discard
         * @param byteCount exact bytes consumed from the source buffer
         */
        @Override
        public void write(final org.miaixz.bus.core.io.buffer.Buffer source, final long byteCount) {
            if (failed.get()) {
                try {
                    source.skip(byteCount);
                } catch (final IOException e) {
                    throw new InternalException("Unable to discard failed cache bytes", e);
                }
                return;
            }
            try {
                delegate.write(source, byteCount);
            } catch (final IOException | RuntimeException e) {
                fail(e);
                try {
                    source.clear();
                } catch (final RuntimeException ignored) {
                    // The network body remains authoritative after a cache failure.
                }
            }
        }

        /**
         * Commits the delegate unless a prior write failed.
         */
        @Override
        public void commit() {
            if (failed.get()) {
                return;
            }
            try {
                delegate.commit();
            } catch (final RuntimeException e) {
                fail(e);
            }
        }

        /**
         * Aborts the delegate best-effort.
         */
        @Override
        public void abort() {
            try {
                delegate.abort();
            } catch (final RuntimeException e) {
                fail(e);
            }
        }

        /**
         * Aborts this writer.
         */
        @Override
        public void close() {
            abort();
        }

        /**
         * Returns whether the cache writer failed.
         *
         * @return {@code true} after the first isolated writer failure
         */
        private boolean failed() {
            return failed.get();
        }

        /**
         * Records the first cache writer failure and aborts the delegate.
         *
         * @param cause first cache writer failure
         */
        private void fail(final Throwable cause) {
            if (!failed.compareAndSet(false, true)) {
                return;
            }
            try {
                delegate.abort();
            } catch (final RuntimeException abortFailure) {
                if (cause != abortFailure) {
                    cause.addSuppressed(abortFailure);
                }
            }
            failureCallback.run();
        }

    }

}
