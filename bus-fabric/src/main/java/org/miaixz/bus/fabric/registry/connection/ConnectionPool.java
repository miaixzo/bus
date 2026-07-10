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
package org.miaixz.bus.fabric.registry.connection;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Thread-safe reusable connection pool.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ConnectionPool implements AutoCloseable {

    /**
     * Pool policy.
     */
    private final PoolPolicy policy;

    /**
     * Idle connections by destination.
     */
    private final ConcurrentHashMap<Destination, ArrayDeque<PooledConnection>> idle;

    /**
     * Leased connections.
     */
    private final Set<ConnectionLease> leased;

    /**
     * Active logical lease counts by physical connection identity.
     */
    private final Map<Connection, Integer> active;

    /**
     * Coordination lock.
     */
    private final Object lock;

    /**
     * Reserved connection creations currently running outside the pool lock.
     */
    private int creating;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Idle eviction scheduler start guard.
     */
    private final AtomicBoolean evictionStarted;

    /**
     * Current scheduled eviction handle.
     */
    private volatile DispatchHandle evictionHandle;

    /**
     * Dispatcher owning the eviction handle.
     */
    private volatile Dispatcher evictionDispatcher;

    /**
     * Clock used by idle eviction.
     */
    private volatile Clock evictionClock;

    /**
     * Creates a pool.
     *
     * @param policy pool policy
     */
    private ConnectionPool(final PoolPolicy policy) {
        this.policy = policy;
        this.idle = new ConcurrentHashMap<>();
        this.leased = Collections.newSetFromMap(new IdentityHashMap<>());
        this.active = new IdentityHashMap<>();
        this.lock = new Object();
        this.closed = new AtomicBoolean();
        this.evictionStarted = new AtomicBoolean();
    }

    /**
     * Creates a connection pool.
     *
     * @param policy pool policy or null for defaults
     * @return connection pool
     */
    public static ConnectionPool create(final PoolPolicy policy) {
        return new ConnectionPool(policy == null ? PoolPolicy.defaults() : policy);
    }

    /**
     * Acquires a connection lease.
     *
     * @param destination connection destination
     * @param factory     connection factory
     * @return connection lease
     */
    public ConnectionLease acquire(final Destination destination, final Supplier<Connection> factory) {
        return acquire(destination, factory, Cancellation.create());
    }

    /**
     * Acquires a connection lease with a cancellation scope.
     *
     * @param destination  connection destination
     * @param factory      connection factory
     * @param cancellation cancellation scope
     * @return connection lease
     */
    public ConnectionLease acquire(
            final Destination destination,
            final Supplier<Connection> factory,
            final Cancellation cancellation) {
        require(destination, "Connection destination");
        require(factory, "Connection factory");
        final Cancellation scope = require(cancellation, "Cancellation");
        final Runnable unregister = scope.onCancel(() -> {
            synchronized (lock) {
                lock.notifyAll();
            }
        });
        try {
            while (true) {
                scope.throwIfCancelled();
                final ConnectionLease shared = acquireShared(destination);
                if (shared != null) {
                    return shared;
                }
                scope.throwIfCancelled();
                final ConnectionLease reused = acquireIdle(destination);
                if (reused != null) {
                    return reused;
                }
                scope.throwIfCancelled();
                if (reserveCreate()) {
                    return createLease(destination, factory, scope);
                }
                waitForAvailability(destination, scope);
            }
        } finally {
            unregister.run();
        }
    }

    /**
     * Releases a connection lease.
     *
     * @param lease lease
     */
    public void release(final ConnectionLease lease) {
        if (!releaseLease(require(lease, "Connection lease"))) {
            return;
        }
    }

    /**
     * Returns idle connection count.
     *
     * @return idle count
     */
    public int idle() {
        synchronized (lock) {
            return idleCount();
        }
    }

    /**
     * Returns leased connection count.
     *
     * @return leased count
     */
    public int leased() {
        synchronized (lock) {
            return leased.size();
        }
    }

    /**
     * Returns active physical connection count.
     *
     * @return active physical connection count
     */
    public int active() {
        synchronized (lock) {
            return active.size();
        }
    }

    /**
     * Evicts idle connections according to policy.
     *
     * @param clock clock
     * @return evicted count
     */
    public int evictIdle(final Clock clock) {
        require(clock, "Runtime clock");
        final List<Connection> evicted = new ArrayList<>();
        synchronized (lock) {
            final Instant now = clock.now();
            int kept = 0;
            for (final ArrayDeque<PooledConnection> bucket : idle.values()) {
                final ArrayDeque<PooledConnection> retained = new ArrayDeque<>();
                while (!bucket.isEmpty()) {
                    final PooledConnection pooled = bucket.removeFirst();
                    final boolean expired = Duration.between(pooled.lastUsed(), now).compareTo(policy.keepAlive()) > 0;
                    if (expired || kept >= policy.maxIdle()) {
                        evicted.add(pooled.connection());
                    } else {
                        retained.addLast(pooled);
                        kept++;
                    }
                }
                bucket.addAll(retained);
            }
            idle.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        closeAll(evicted);
        if (idle() == 0) {
            cancelScheduledEviction();
        }
        return evicted.size();
    }

    /**
     * Closes leases that have stayed leased longer than the supplied age.
     *
     * @param maxAge maximum leased age
     * @param clock  clock
     * @return leaked lease count
     */
    public int pruneLeaked(final Duration maxAge, final Clock clock) {
        if (maxAge == null || maxAge.isNegative()) {
            throw new ValidateException("Leak max age must be non-null and non-negative");
        }
        require(clock, "Runtime clock");
        final List<ConnectionLease> leaked = new ArrayList<>();
        synchronized (lock) {
            final Instant now = clock.now();
            for (final ConnectionLease lease : leased) {
                if (Duration.between(lease.acquiredAt(), now).compareTo(maxAge) > 0) {
                    leaked.add(lease);
                }
            }
        }
        for (final ConnectionLease lease : leaked) {
            lease.leak();
        }
        return leaked.size();
    }

    /**
     * Starts automatic idle connection eviction.
     *
     * @param dispatcher dispatcher
     * @param clock      clock
     */
    public void startIdleEviction(final Dispatcher dispatcher, final Clock clock) {
        require(dispatcher, "Dispatcher");
        require(clock, "Runtime clock");
        if (!evictionStarted.compareAndSet(false, true)) {
            return;
        }
        evictionDispatcher = dispatcher;
        evictionClock = clock;
        scheduleEvictionIfNeeded();
    }

    /**
     * Closes all pooled connections.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        final List<Connection> connections = new ArrayList<>();
        final Set<Connection> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        synchronized (lock) {
            cancelEviction();
            for (final ArrayDeque<PooledConnection> bucket : idle.values()) {
                while (!bucket.isEmpty()) {
                    final Connection connection = bucket.removeLast().connection();
                    if (seen.add(connection)) {
                        connections.add(connection);
                    }
                }
            }
            idle.clear();
            for (final ConnectionLease lease : List.copyOf(leased)) {
                if (seen.add(lease.connection())) {
                    connections.add(lease.connection());
                }
            }
            leased.clear();
            active.clear();
            lock.notifyAll();
        }
        closeAll(connections);
    }

    /**
     * Returns pool counts by destination.
     *
     * @return immutable pool snapshot
     */
    public Map<Destination, Integer> snapshot() {
        synchronized (lock) {
            final LinkedHashMap<Destination, Integer> snapshot = new LinkedHashMap<>();
            for (final Map.Entry<Destination, ArrayDeque<PooledConnection>> entry : idle.entrySet()) {
                snapshot.put(entry.getKey(), entry.getValue().size());
            }
            for (final ConnectionLease lease : leased) {
                snapshot.merge(lease.destination(), 1, Integer::sum);
            }
            return Collections.unmodifiableMap(snapshot);
        }
    }

    /**
     * Releases a lease from either the lease or pool API.
     *
     * @param lease lease
     * @return true when this call changed state
     */
    boolean releaseLease(final ConnectionLease lease) {
        require(lease, "Connection lease");
        if (lease.pool() != this) {
            throw new StatefulException("Connection lease belongs to another pool");
        }
        Connection closeable = null;
        boolean scheduleEviction = false;
        synchronized (lock) {
            if (!leased.contains(lease)) {
                return false;
            }
            if (!lease.markReleased()) {
                return false;
            }
            leased.remove(lease);
            final int remaining = decrementActive(lease.connection());
            if (remaining > 0) {
                lock.notifyAll();
                return true;
            }
            if (!closed.get() && !lease.leaked() && lease.connection().healthy()) {
                idle.computeIfAbsent(lease.destination(), ignored -> new ArrayDeque<>())
                        .addLast(new PooledConnection(lease.connection(), Instant.now()));
                scheduleEviction = true;
            } else {
                closeable = lease.connection();
            }
            lock.notifyAll();
        }
        if (closeable != null) {
            closeOne(closeable);
        }
        if (scheduleEviction) {
            scheduleEvictionIfNeeded();
        }
        return true;
    }

    /**
     * Closes a lease without returning its physical connection to idle.
     *
     * @param lease lease
     * @return true when this call changed state
     */
    boolean closeLease(final ConnectionLease lease) {
        require(lease, "Connection lease");
        if (lease.pool() != this) {
            throw new StatefulException("Connection lease belongs to another pool");
        }
        Connection closeable = null;
        synchronized (lock) {
            if (!leased.contains(lease)) {
                return false;
            }
            if (!lease.markClosed()) {
                return false;
            }
            leased.remove(lease);
            final int remaining = decrementActive(lease.connection());
            if (remaining == 0) {
                closeable = lease.connection();
            }
            lock.notifyAll();
        }
        if (closeable != null) {
            closeOne(closeable);
        }
        return true;
    }

    /**
     * Detaches a lease without returning it to idle.
     *
     * @param lease lease
     */
    void detach(final ConnectionLease lease) {
        synchronized (lock) {
            if (leased.remove(lease)) {
                decrementActive(lease.connection());
            }
            lock.notifyAll();
        }
    }

    /**
     * Attempts to acquire an already leased multiplex-capable connection.
     *
     * @param destination connection destination
     * @return shared lease or null
     */
    private ConnectionLease acquireShared(final Destination destination) {
        if (!destination.multiplex()) {
            return null;
        }
        synchronized (lock) {
            ensureOpen();
            Connection candidate = null;
            for (final ConnectionLease lease : leased) {
                final Connection connection = lease.connection();
                if (destination.equals(lease.destination()) && connection.healthy()
                        && active.getOrDefault(connection, 0) < destination.maxMultiplexStreams()) {
                    candidate = connection;
                    break;
                }
            }
            if (candidate == null) {
                return null;
            }
            final ConnectionLease shared = new ConnectionLease(this, destination, candidate, Instant.now());
            leased.add(shared);
            active.merge(candidate, 1, Integer::sum);
            return shared;
        }
    }

    /**
     * Attempts to acquire an idle connection.
     *
     * @param destination connection destination
     * @return lease or null
     */
    private ConnectionLease acquireIdle(final Destination destination) {
        final List<Connection> discarded = new ArrayList<>();
        ConnectionLease lease = null;
        boolean cancelEviction = false;
        synchronized (lock) {
            ensureOpen();
            final ArrayDeque<PooledConnection> bucket = idle.get(destination);
            while (bucket != null && !bucket.isEmpty()) {
                final Connection connection = bucket.removeFirst().connection();
                if (connection.healthy()) {
                    lease = new ConnectionLease(this, destination, connection, Instant.now());
                    leased.add(lease);
                    active.put(connection, 1);
                    if (bucket.isEmpty()) {
                        idle.remove(destination, bucket);
                    }
                    cancelEviction = idleCount() == 0;
                    break;
                }
                discarded.add(connection);
            }
        }
        if (cancelEviction) {
            cancelScheduledEviction();
        }
        RuntimeException failure = null;
        try {
            closeAll(discarded);
        } catch (final RuntimeException e) {
            failure = e;
        }
        if (failure != null) {
            if (lease != null) {
                releaseLease(lease);
            }
            throw failure;
        }
        return lease;
    }

    /**
     * Returns whether another connection can be created.
     *
     * @return true when under limit
     */
    private boolean reserveCreate() {
        synchronized (lock) {
            ensureOpen();
            if (connectionCount() + creating >= policy.maxConnections()) {
                return false;
            }
            creating++;
            return true;
        }
    }

    /**
     * Creates a new lease outside the pool lock with cancellation support.
     *
     * @param destination  destination
     * @param factory      factory
     * @param cancellation cancellation scope
     * @return lease
     */
    private ConnectionLease createLease(
            final Destination destination,
            final Supplier<Connection> factory,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final Connection connection;
        try {
            connection = require(factory.get(), "Created connection");
        } catch (final RuntimeException e) {
            releaseCreateReservation();
            throw e instanceof InternalException || e instanceof ProtocolException || e instanceof SocketException
                    || e instanceof TimeoutException || e instanceof StatefulException || e instanceof ValidateException
                            ? e
                            : new InternalException("Unable to create connection", e);
        }
        boolean closeable = false;
        boolean cancelled = false;
        synchronized (lock) {
            creating--;
            if (closed.get() || scope.cancelled()) {
                closeable = true;
                cancelled = scope.cancelled();
            } else {
                final ConnectionLease lease = new ConnectionLease(this, destination, connection, Instant.now());
                leased.add(lease);
                active.put(connection, 1);
                lock.notifyAll();
                return lease;
            }
            lock.notifyAll();
        }
        if (closeable) {
            closeOne(connection);
        }
        if (cancelled) {
            scope.throwIfCancelled();
        }
        throw new StatefulException("Connection pool is closed");
    }

    /**
     * Releases a reserved creation slot after factory failure.
     */
    private void releaseCreateReservation() {
        synchronized (lock) {
            creating--;
            lock.notifyAll();
        }
    }

    /**
     * Waits for either pool capacity or an existing reusable connection.
     */
    private void waitForAvailability(final Destination destination, final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        final long timeoutNanos = policy.acquireTimeout().toNanos();
        final long deadline = System.nanoTime() + timeoutNanos;
        synchronized (lock) {
            while (!closed.get() && connectionCount() + creating >= policy.maxConnections()
                    && !existingCandidateAvailable(destination)) {
                scope.throwIfCancelled();
                final long remaining = deadline - System.nanoTime();
                if (remaining <= 0L) {
                    throw new TimeoutException("Connection acquire timed out");
                }
                try {
                    lock.wait(Math.max(1L, remaining / 1_000_000L));
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InternalException("Interrupted while waiting for connection", e);
                }
            }
            scope.throwIfCancelled();
            ensureOpen();
        }
    }

    /**
     * Returns whether an existing connection can be retried by the acquire loop.
     *
     * @param destination requested destination
     * @return true when an idle or multiplex-capable active connection may satisfy the request
     */
    private boolean existingCandidateAvailable(final Destination destination) {
        final ArrayDeque<PooledConnection> bucket = idle.get(destination);
        if (bucket != null && !bucket.isEmpty()) {
            return true;
        }
        if (!destination.multiplex()) {
            return false;
        }
        for (final ConnectionLease lease : leased) {
            final Connection connection = lease.connection();
            if (destination.equals(lease.destination()) && connection.healthy()
                    && active.getOrDefault(connection, 0) < destination.maxMultiplexStreams()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts idle connections.
     *
     * @return idle count
     */
    private int idleCount() {
        int count = 0;
        for (final ArrayDeque<PooledConnection> bucket : idle.values()) {
            count += bucket.size();
        }
        return count;
    }

    /**
     * Counts physical connections tracked by the pool.
     *
     * @return physical connection count
     */
    private int connectionCount() {
        return idleCount() + active.size();
    }

    /**
     * Decrements an active physical connection reference count.
     *
     * @param connection connection
     * @return remaining active references
     */
    private int decrementActive(final Connection connection) {
        final int count = active.getOrDefault(connection, 0);
        if (count <= 1) {
            active.remove(connection);
            return 0;
        }
        active.put(connection, count - 1);
        return count - 1;
    }

    /**
     * Schedules the next idle eviction run.
     *
     * @param dispatcher dispatcher
     * @param clock      clock
     */
    private void scheduleEvictionIfNeeded() {
        final Dispatcher dispatcher = evictionDispatcher;
        final Clock clock = evictionClock;
        if (dispatcher == null || clock == null || closed.get() || !evictionStarted.get()) {
            return;
        }
        synchronized (lock) {
            if (evictionHandle != null) {
                return;
            }
            final Duration delay = evictionDelay(clock);
            if (delay == null) {
                return;
            }
            evictionHandle = dispatcher.schedule(
                    "connection-pool:idle-evict",
                    delay,
                    Activity.of("connection-pool:idle-evict", this::runEviction));
        }
    }

    /**
     * Runs one idle eviction pass and schedules the next one if needed.
     */
    private void runEviction() {
        synchronized (lock) {
            evictionHandle = null;
        }
        if (closed.get() || !evictionStarted.get()) {
            return;
        }
        final Clock clock = evictionClock;
        if (clock == null) {
            return;
        }
        evictIdle(clock);
        scheduleEvictionIfNeeded();
    }

    /**
     * Returns next eviction delay.
     *
     * @param clock clock
     * @return delay
     */
    private Duration evictionDelay(final Clock clock) {
        require(clock, "Runtime clock");
        final Duration keepAlive = policy.keepAlive();
        if (keepAlive.isZero()) {
            return Duration.ZERO;
        }
        final Instant now = clock.now();
        Duration delay = null;
        for (final ArrayDeque<PooledConnection> bucket : idle.values()) {
            for (final PooledConnection pooled : bucket) {
                final Duration age = Duration.between(pooled.lastUsed(), now);
                final Duration remaining = keepAlive.minus(age);
                if (remaining.isZero() || remaining.isNegative()) {
                    return Duration.ZERO;
                }
                if (delay == null || remaining.compareTo(delay) < 0) {
                    delay = remaining;
                }
            }
        }
        return delay;
    }

    /**
     * Cancels current eviction handle.
     */
    private void cancelEviction() {
        final Dispatcher dispatcher = evictionDispatcher;
        evictionStarted.set(false);
        evictionClock = null;
        evictionDispatcher = null;
        cancelScheduledEviction(dispatcher);
    }

    /**
     * Cancels the currently scheduled idle eviction handle.
     */
    private void cancelScheduledEviction() {
        cancelScheduledEviction(evictionDispatcher);
    }

    /**
     * Cancels the currently scheduled idle eviction handle.
     *
     * @param dispatcher dispatcher that owns the delayed handle
     */
    private void cancelScheduledEviction(final Dispatcher dispatcher) {
        final DispatchHandle handle = evictionHandle;
        if (handle != null) {
            evictionHandle = null;
            if (dispatcher != null) {
                dispatcher.cancel(handle);
            } else {
                handle.cancel();
            }
        }
    }

    /**
     * Ensures the pool is open.
     */
    private void ensureOpen() {
        if (closed.get()) {
            throw new StatefulException("Connection pool is closed");
        }
    }

    /**
     * Closes a list of connections.
     *
     * @param connections connections
     */
    private static void closeAll(final List<Connection> connections) {
        RuntimeException failure = null;
        for (final Connection connection : connections) {
            try {
                connection.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        if (failure != null) {
            throw new InternalException("Unable to close pooled connections", failure);
        }
    }

    /**
     * Closes one connection.
     *
     * @param connection connection
     */
    private static void closeOne(final Connection connection) {
        try {
            connection.close();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close pooled connection", e);
        }
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Idle pooled connection.
     *
     * @param connection connection
     * @param lastUsed   last used time
     */
    private record PooledConnection(Connection connection, Instant lastUsed) {

    }

}
