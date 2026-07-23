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
package org.miaixz.bus.fabric.network.dns;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * DNS resolver with observer events and system fallback.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsResolver {

    /**
     * Backend that performs uncached host lookups.
     */
    private final Resolver backend;

    /**
     * Failure-safe observer receiving DNS lifecycle events.
     */
    private final EventObserver observer;

    /**
     * Concurrent cache and request-coalescing state shared by resolver views.
     */
    private final State state;

    /**
     * Borrowed runtime clock, or {@code null} for synchronous compatibility.
     */
    private final Clock runtimeClock;

    /**
     * Borrowed runtime dispatcher, or {@code null} for synchronous compatibility.
     */
    private final Dispatcher dispatcher;

    /**
     * Creates a DNS resolver.
     *
     * @param backend  backend performing uncached lookups
     * @param observer observer receiving DNS lifecycle events
     */
    private DnsResolver(final Resolver backend, final EventObserver observer) {
        this(backend, observer, new State(), null, null);
    }

    /**
     * Creates a resolver view over shared state.
     *
     * @param backend      backend performing uncached DNS lookups
     * @param observer     observer receiving DNS lifecycle events
     * @param state        cache and in-flight lookup state shared by resolver views
     * @param runtimeClock runtime monotonic clock, or {@code null} for synchronous compatibility
     * @param dispatcher   borrowed runtime dispatcher, or {@code null} for synchronous compatibility
     */
    private DnsResolver(final Resolver backend, final EventObserver observer, final State state,
            final Clock runtimeClock, final Dispatcher dispatcher) {
        this.backend = Assert.notNull(backend, () -> new ValidateException("Resolver must not be null"));
        final EventObserver checked = Assert
                .notNull(observer, () -> new ValidateException("Observer must not be null"));
        this.observer = checked == EventObserver.noop() ? checked : EventObserver.safe(checked);
        this.state = state;
        this.runtimeClock = runtimeClock;
        this.dispatcher = dispatcher;
    }

    /**
     * Returns the system DNS resolver.
     *
     * @return process-wide resolver backed by the default system lookup contract
     */
    public static DnsResolver system() {
        return Instances.get(
                DnsResolver.class.getName() + ".system",
                () -> new DnsResolver(systemResolver(), EventObserver.noop()));
    }

    /**
     * Wraps a resolver.
     *
     * @param resolver non-null backend performing uncached lookups
     * @return resolver with shared caching, request coalescing, and no event observer
     * @throws ValidateException if {@code resolver} is {@code null}
     */
    public static DnsResolver of(final Resolver resolver) {
        return new DnsResolver(resolver, EventObserver.noop());
    }

    /**
     * Resolves a host.
     *
     * @param host host name or address literal to normalize and resolve
     * @return cached or newly resolved DNS result
     * @throws ValidateException if the host is invalid
     * @throws SocketException   if the backend lookup fails
     */
    public DnsResult resolve(final String host) {
        return resolve(host, Clock.system());
    }

    /**
     * Resolves a host using the owning Context or Reactor clock.
     *
     * @param host  host name or address literal to normalize and resolve
     * @param clock monotonic clock used for cache expiry and observation timing
     * @return cached, coalesced, or newly resolved DNS result
     * @throws ValidateException if the host or clock is invalid
     * @throws SocketException   if the backend lookup fails
     */
    public DnsResult resolve(final String host, final Clock clock) {
        final String normalized = NetKit.normalizeHost(host, "DNS host");
        final Clock operationClock = Assert.notNull(clock, () -> new ValidateException("DNS clock must not be null"));
        final long now = operationClock.nanos();
        final CacheEntry cached = state.cache.get(normalized);
        if (cached != null && cached.expiresAt > now) {
            return cached.result;
        }
        if (cached != null) {
            state.cache.remove(normalized, cached);
        }
        final CompletableFuture<DnsResult> created = new CompletableFuture<>();
        final CompletableFuture<DnsResult> existing = state.inFlight.putIfAbsent(normalized, created);
        if (existing != null) {
            return await(existing);
        }
        try {
            final DnsResult result = resolveBackend(normalized, operationClock);
            cache(normalized, result, operationClock.nanos());
            created.complete(result);
            return result;
        } catch (final RuntimeException failure) {
            created.completeExceptionally(failure);
            throw failure;
        } finally {
            state.inFlight.remove(normalized, created);
        }
    }

    /**
     * Resolves through a borrowed runtime without blocking the caller.
     *
     * @param host host name or address literal to normalize and resolve
     * @return independently cancellable waiter view
     * @throws IllegalStateException if runtime services have not been attached with
     *                               {@link #withRuntime(Clock, Dispatcher)}
     * @throws ValidateException     if the host is invalid
     */
    public CompletableFuture<DnsResult> resolveAsync(final String host) {
        if (runtimeClock == null || dispatcher == null) {
            throw new IllegalStateException("Asynchronous DNS resolution requires withRuntime(clock, dispatcher)");
        }
        final String normalized = NetKit.normalizeHost(host, "DNS host");
        final long now = runtimeClock.nanos();
        final CacheEntry cached = state.cache.get(normalized);
        if (cached != null && cached.expiresAt > now) {
            return CompletableFuture.completedFuture(cached.result);
        }
        if (cached != null) {
            state.cache.remove(normalized, cached);
        }
        final CompletableFuture<DnsResult> created = new CompletableFuture<>();
        final CompletableFuture<DnsResult> shared = state.inFlight.putIfAbsent(normalized, created);
        if (shared == null) {
            try {
                dispatcher.background(
                        "dns:" + normalized,
                        created,
                        Activity.of("dns-resolve", () -> completeAsync(normalized, created)));
            } catch (final RuntimeException failure) {
                state.inFlight.remove(normalized, created);
                created.completeExceptionally(failure);
            }
        }
        return (shared == null ? created : shared).thenApply(result -> result);
    }

    /**
     * Returns a resolver view that borrows the supplied runtime services while sharing resolution state.
     *
     * @param clock      time source used for cache expiry
     * @param dispatcher dispatcher used for asynchronous resolution
     * @return resolver view backed by the existing backend, observer, cache, and in-flight requests
     * @throws ValidateException if either runtime service is {@code null}
     */
    public DnsResolver withRuntime(final Clock clock, final Dispatcher dispatcher) {
        return new DnsResolver(backend, observer, state,
                Assert.notNull(clock, () -> new ValidateException("DNS clock must not be null")),
                Assert.notNull(dispatcher, () -> new ValidateException("DNS dispatcher must not be null")));
    }

    /**
     * Returns an observer-isolated wrapper over the same backend.
     *
     * @param observer non-null observer for the returned view
     * @return resolver view sharing backend, cache, runtime services, and in-flight requests
     * @throws ValidateException if {@code observer} is {@code null}
     */
    public DnsResolver withObserver(final EventObserver observer) {
        return new DnsResolver(backend,
                Assert.notNull(observer, () -> new ValidateException("DNS observer must not be null")), state,
                runtimeClock, dispatcher);
    }

    /**
     * Completes one dispatcher-owned backend operation.
     *
     * @param host   normalized host being resolved
     * @param future shared in-flight future to complete
     */
    private void completeAsync(final String host, final CompletableFuture<DnsResult> future) {
        try {
            final DnsResult result = resolveBackend(host, runtimeClock);
            cache(host, result, runtimeClock.nanos());
            future.complete(result);
        } catch (final RuntimeException failure) {
            future.completeExceptionally(failure);
        } finally {
            state.inFlight.remove(host, future);
        }
    }

    /**
     * Performs the sole observable backend call.
     *
     * @param host  normalized host to resolve
     * @param clock clock used for timestamps and observation duration
     * @return backend resolution result
     */
    private DnsResult resolveBackend(final String host, final Clock clock) {
        final boolean observed = observer != EventObserver.noop();
        final String operationId = observed ? ID.objectId() : null;
        if (observed) {
            emit(ObservationMarker.DNS_START, host, null, clock, operationId);
        }
        try {
            final DnsResult result = backend.resolveResult(host, clock);
            if (observed) {
                emit(
                        result.empty() ? ObservationMarker.DNS_FAILED : ObservationMarker.DNS_SUCCESS,
                        host,
                        result.empty() ? new SocketException("DNS returned no address for " + host) : null,
                        clock,
                        operationId);
            }
            return result;
        } catch (final RuntimeException failure) {
            final RuntimeException mapped = failure instanceof SocketException ? failure
                    : new SocketException("Unable to resolve host " + host, failure);
            if (observed) {
                emit(ObservationMarker.DNS_FAILED, host, mapped, clock, operationId);
            }
            throw mapped;
        }
    }

    /**
     * Publishes a capacity-limited cache entry, evicting one concurrent-map entry when necessary.
     *
     * @param host   normalized cache key
     * @param result resolution result to cache
     * @param now    current monotonic time in nanoseconds
     */
    private void cache(final String host, final DnsResult result, final long now) {
        long ttl = result.empty() ? Builder.DNS_RESOLVER_NEGATIVE_TTL_NANOS
                : result.hasTtl() ? result.ttl().toNanos() : Builder.DNS_RESOLVER_DEFAULT_POSITIVE_TTL_NANOS;
        if (ttl <= 0L) {
            return;
        }
        if (state.cache.size() >= Builder.DNS_RESOLVER_MAX_CACHE_ENTRIES && !state.cache.containsKey(host)) {
            final var iterator = state.cache.entrySet().iterator();
            if (iterator.hasNext()) {
                final Map.Entry<String, CacheEntry> victim = iterator.next();
                state.cache.remove(victim.getKey(), victim.getValue());
            }
        }
        final long expiresAt = now > Long.MAX_VALUE - ttl ? Long.MAX_VALUE : now + ttl;
        state.cache.put(host, new CacheEntry(result, expiresAt));
    }

    /**
     * Waits for a shared synchronous resolution and restores its cause.
     *
     * @param future shared resolution future
     * @return completed DNS result
     */
    private static DnsResult await(final CompletableFuture<DnsResult> future) {
        try {
            return future.join();
        } catch (final CompletionException failure) {
            final Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw failure;
        }
    }

    /**
     * Emits an event.
     *
     * @param marker      DNS lifecycle marker to publish
     * @param host        normalized host associated with the lookup
     * @param cause       failure attached to the event, or {@code null}
     * @param clock       operation clock
     * @param operationId identifier correlating start and terminal events
     */
    private void emit(
            final ObservationMarker marker,
            final String host,
            final Throwable cause,
            final Clock clock,
            final String operationId) {
        observer.emit(
                FabricEvent.builder(marker, clock).tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.HOST, host)
                        .cause(cause).build());
    }

    /**
     * Returns the shared system resolver contract.
     *
     * @return system resolver contract
     */
    private static Resolver systemResolver() {
        return Instances.get(DnsResolver.class.getName() + ".systemResolver", () -> new Resolver() {
        });
    }

    /**
     * Mutable state shared by resolver views that borrow the same backend.
     * <p>
     * Both maps are concurrent because synchronous callers and dispatcher activities may resolve different hosts at the
     * same time.
     * </p>
     */
    private static final class State {

        /**
         * Cached terminal results keyed by normalized host name.
         */
        private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

        /**
         * Backend resolutions currently shared by callers of the same normalized host.
         */
        private final ConcurrentHashMap<String, CompletableFuture<DnsResult>> inFlight = new ConcurrentHashMap<>();
    }

    /**
     * DNS cache record with a monotonic expiration deadline.
     *
     * @param result    resolved addresses and optional backend TTL
     * @param expiresAt expiration deadline in {@link Clock#nanos()} units
     */
    private record CacheEntry(DnsResult result, long expiresAt) {
    }

}
