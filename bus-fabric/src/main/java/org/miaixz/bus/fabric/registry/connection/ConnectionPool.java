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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter.Counter;
import org.miaixz.bus.fabric.registry.policy.PoolPolicy;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Thread-safe reusable connection pool.
 *
 * @author Kimi Liu
 */
public class ConnectionPool implements AutoCloseable {

    /**
     * Shared unregister action for synchronous callers that cannot be cancelled.
     */
    private static final Runnable NOOP_UNREGISTER = () -> {
    };

    /**
     * Oldest proven-HTTP/1 waiters admitted together to avoid a descheduled queue-head convoy.
     */
    private static final int HTTP1_ADMISSION_WINDOW = Normal._1;

    /**
     * Normal per-destination H1 width before sustained queueing proves that the service is latency-bound.
     */
    private static final int HTTP1_BASE_CONNECTIONS = Normal._16;

    /**
     * Queue duration required before a proven H1 destination may expand beyond its normal width.
     */
    private static final long HTTP1_EXPANSION_WAIT_NANOS = 50_000_000L;

    /**
     * Minimum simultaneous queue depth required to latch full slow-service expansion.
     */
    private static final int HTTP1_EXPANSION_LATCH_WAITERS = Normal._48;

    /**
     * Pool policy.
     */
    private final PoolPolicy policy;

    /**
     * Pool-owned time source.
     */
    private final Clock clock;

    /**
     * Idle connections by destination.
     */
    private final IdleConnectionIndex idleIndex;

    /**
     * Exclusive idle connections detached while active validation runs outside the pool lock.
     */
    private final Set<Connection> validatingIdle;

    /**
     * Destination values whose stable option identity has already been verified.
     */
    private final Set<Destination> validatedDestinations;

    /**
     * Most recently validated immutable destination identity used by the steady single-origin fast path.
     */
    private volatile Destination lastValidatedDestination;

    /**
     * Leased connections.
     */
    private final Set<ConnectionLease> leased;

    /**
     * Active logical lease counts by physical connection identity.
     */
    private final Map<Connection, Integer> active;

    /**
     * Physical connection count by destination.
     */
    private final Map<Destination, Integer> physicalByDestination;

    /**
     * Multiplex-capable active connections by destination.
     */
    private final MultiplexCapacity multiplexCapacity;

    /**
     * Proven H1 destinations whose sustained queueing permits expansion through their configured upper bound.
     */
    private final Set<Destination> expandedHttp1Destinations;

    /**
     * Multiplex capacity listener registrations.
     */
    /**
     * O(1) total physical connection count.
     */
    private volatile int physicalCount;

    /**
     * O(1) total idle connection count.
     */
    /**
     * Coordination lock.
     */
    private final Object lock;

    /**
     * Reserved connection creations currently running outside the pool lock.
     */
    private int creating;

    /**
     * Reserved connection creations by destination.
     */
    private final Map<Destination, Integer> creatingByDestination;

    /**
     * Fair first-in-first-out acquisition waiters.
     */
    private final PoolWaiters waiters;

    /**
     * Closed flag.
     */
    private final AtomicBoolean closed;

    /**
     * Idle eviction scheduler start guard.
     */
    private final AtomicBoolean evictionStarted;

    /**
     * Borrowed metric owner.
     */
    private final FabricMeter meter;

    /**
     * Runtime dispatcher, borrowed when supplied and lazily owned otherwise.
     */
    private volatile Dispatcher runtimeDispatcher;

    /**
     * True only when this pool created the runtime dispatcher.
     */
    private volatile boolean ownsRuntimeDispatcher;

    /**
     * Current scheduled eviction handle.
     */
    private volatile DispatchHandle evictionHandle;

    /**
     * Dispatcher owning the eviction handle.
     */
    private volatile Dispatcher evictionDispatcher;

    /**
     * Creates a pool.
     *
     * @param policy     pool policy
     * @param clock      pool time source
     * @param meter      meter recording physical and logical lease activity
     * @param dispatcher dispatcher used for scheduled eviction, or {@code null}
     */
    public ConnectionPool(final PoolPolicy policy, final Clock clock, final FabricMeter meter,
            final Dispatcher dispatcher) {
        this.policy = policy;
        this.clock = clock;
        this.meter = meter;
        this.runtimeDispatcher = dispatcher;
        this.idleIndex = new IdleConnectionIndex();
        this.validatingIdle = Collections.newSetFromMap(new IdentityHashMap<>());
        this.validatedDestinations = ConcurrentHashMap.newKeySet();
        this.leased = Collections.newSetFromMap(new IdentityHashMap<>());
        this.active = new IdentityHashMap<>();
        this.physicalByDestination = new LinkedHashMap<>();
        this.multiplexCapacity = new MultiplexCapacity();
        this.expandedHttp1Destinations = ConcurrentHashMap.newKeySet();
        this.lock = new Object();
        this.creatingByDestination = new LinkedHashMap<>();
        this.waiters = new PoolWaiters();
        this.closed = new AtomicBoolean();
        this.evictionStarted = new AtomicBoolean();
        if (Logger.isInfoEnabled()) {
            Logger.info(
                    true,
                    "Fabric",
                    "Connection pool initialized: maxConnections={}, maxIdle={}, perDestination={}",
                    policy.maxConnections(),
                    policy.maxIdle(),
                    policy.maxConnectionsPerDestination());
        }
    }

    /**
     * Creates a connection pool.
     *
     * @param policy pool policy or null for defaults
     * @return connection pool
     */
    public static ConnectionPool create(final PoolPolicy policy) {
        return create(policy, Clock.system());
    }

    /**
     * Creates a connection pool with an owned time source.
     *
     * @param policy pool policy or null for defaults
     * @param clock  pool time source
     * @return connection pool
     */
    public static ConnectionPool create(final PoolPolicy policy, final Clock clock) {
        return new ConnectionPool(policy == null ? PoolPolicy.defaults() : policy, require(clock, "Runtime clock"),
                FabricMeter.create(clock), null);
    }

    /**
     * Creates a compatibility pool that borrows a meter and lazily owns its runtime dispatcher.
     *
     * @param policy pool policy, or null to use the defaults
     * @param clock  pool time source
     * @param meter  meter borrowed by the pool
     * @return connection pool with a lazily created dispatcher
     */
    public static ConnectionPool create(final PoolPolicy policy, final Clock clock, final FabricMeter meter) {
        return new ConnectionPool(policy == null ? PoolPolicy.defaults() : policy, require(clock, "Runtime clock"),
                require(meter, "Fabric meter"), null);
    }

    /**
     * Creates a pool that borrows both the runtime meter and dispatcher.
     *
     * @param policy     pool policy, or null to use the defaults
     * @param clock      pool time source
     * @param meter      meter borrowed by the pool
     * @param dispatcher dispatcher borrowed by the pool
     * @return connection pool using the supplied runtime services
     */
    public static ConnectionPool create(
            final PoolPolicy policy,
            final Clock clock,
            final FabricMeter meter,
            final Dispatcher dispatcher) {
        return new ConnectionPool(policy == null ? PoolPolicy.defaults() : policy, require(clock, "Runtime clock"),
                require(meter, "Fabric meter"), require(dispatcher, "Runtime dispatcher"));
    }

    /**
     * Returns the pool runtime dispatcher, lazily creating exactly one owned instance for compatibility pools.
     *
     * @return runtime dispatcher
     */
    public Dispatcher runtimeDispatcher() {
        Dispatcher current = runtimeDispatcher;
        if (current != null) {
            return current;
        }
        synchronized (lock) {
            ensureOpen();
            current = runtimeDispatcher;
            if (current == null) {
                current = Dispatcher.create();
                runtimeDispatcher = current;
                ownsRuntimeDispatcher = true;
            }
            return current;
        }
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
        final Destination target = require(destination, "Connection destination");
        if (target != lastValidatedDestination) {
            if (validatedDestinations.add(target)) {
                try {
                    validateDestination(target);
                } catch (final RuntimeException e) {
                    validatedDestinations.remove(target);
                    throw e;
                }
            }
            lastValidatedDestination = target;
        }
        require(factory, "Connection factory");
        final Cancellation scope = require(cancellation, "Cancellation");
        final long deadline = deadline(policy.acquireTimeout());
        scope.throwIfCancelled();
        final ConnectionLease immediateIdle = acquireIdle(target, null, scope);
        if (immediateIdle != null) {
            return immediateIdle;
        }
        scope.throwIfCancelled();
        final PoolWaiters.Waiter waiter = new PoolWaiters.Waiter(target);
        final Runnable unregister = scope.cancellable() ? scope.onCancel(() -> {
            synchronized (lock) {
                waiter.cancelled = true;
                LockSupport.unpark(waiter.thread);
            }
        }) : NOOP_UNREGISTER;
        try {
            while (true) {
                final ConnectionLease handedOff = takeHandoff(waiter);
                if (handedOff != null) {
                    if (scope.cancelled()) {
                        handedOff.release();
                        scope.throwIfCancelled();
                    }
                    return handedOff;
                }
                scope.throwIfCancelled();
                final ConnectionLease shared = acquireShared(target, waiter);
                if (shared != null) {
                    return shared;
                }
                scope.throwIfCancelled();
                final ConnectionLease reused = acquireIdle(target, waiter, scope);
                if (reused != null) {
                    return reused;
                }
                scope.throwIfCancelled();
                if (reserveCreate(target, waiter)) {
                    return createLease(target, factory, scope);
                }
                waitForAvailability(waiter, scope, deadline);
            }
        } finally {
            unregister.run();
            final ConnectionLease abandoned = removeWaiter(waiter);
            if (abandoned != null) {
                abandoned.release();
            }
        }
    }

    /**
     * Opens an explicitly non-reusable connection without running idle/shared lookup and waiter arbitration. The
     * returned lease is still registered so counters, cancellation, and terminal close remain exact.
     *
     * @param destination  stable destination of the physical connection
     * @param factory      supplier that creates the connected physical connection
     * @param cancellation cancellation scope checked before lifecycle registration
     * @return registered lease whose release closes the physical connection
     */
    public ConnectionLease acquireTransient(
            final Destination destination,
            final Supplier<Connection> factory,
            final Cancellation cancellation) {
        final Destination target = require(destination, "Connection destination");
        final Supplier<Connection> source = require(factory, "Connection factory");
        final Cancellation scope = require(cancellation, "Cancellation");
        scope.throwIfCancelled();
        final Connection connection = require(source.get(), "Created connection");
        boolean closeable = false;
        synchronized (lock) {
            if (closed.get() || scope.cancelled()) {
                closeable = true;
            } else {
                final ConnectionLease lease = ConnectionLease.transientLease(this, target, connection, clock.millis());
                leased.add(lease);
                physicalCount++;
                physicalCreated();
                logicalAcquired();
                return lease;
            }
        }
        if (closeable)
            abortOne(connection);
        scope.throwIfCancelled();
        throw new StatefulException("Connection pool is closed");
    }

    /**
     * Releases a connection lease.
     *
     * @param lease lease to return through the pool release path
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
        return idleIndex.count;
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
        return physicalCount;
    }

    /**
     * Evicts idle connections according to policy.
     *
     * @return evicted count
     */
    public int evictIdle() {
        final List<Connection> evicted = new ArrayList<>();
        synchronized (lock) {
            final Instant now = clock.now();
            int kept = 0;
            for (final ArrayDeque<PooledConnection> bucket : idleIndex.buckets.values()) {
                final ArrayDeque<PooledConnection> retained = new ArrayDeque<>();
                while (!bucket.isEmpty()) {
                    final PooledConnection pooled = bucket.removeFirst();
                    idleIndex.count--;
                    final boolean expired = Duration.between(pooled.lastUsed(), now).compareTo(policy.keepAlive()) > 0;
                    if (expired || kept >= policy.maxIdle()) {
                        evicted.add(pooled.connection());
                    } else {
                        retained.addLast(pooled);
                        idleIndex.count++;
                        kept++;
                    }
                }
                bucket.addAll(retained);
            }
            idleIndex.buckets.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            for (final Connection connection : evicted) {
                removePhysical(connection);
            }
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
     * @return leaked lease count
     */
    public int pruneLeaked(final Duration maxAge) {
        final Duration currentMaxAge = Assert
                .notNull(maxAge, () -> new ValidateException("Leak max age must be non-null and non-negative"));
        Assert.isFalse(
                currentMaxAge.isNegative(),
                () -> new ValidateException("Leak max age must be non-null and non-negative"));
        final List<ConnectionLease> leaked = new ArrayList<>();
        synchronized (lock) {
            final Instant now = clock.now();
            for (final ConnectionLease lease : leased) {
                if (Duration.between(lease.acquiredAt(), now).compareTo(currentMaxAge) > 0) {
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
     * @param dispatcher runtime dispatcher that schedules eviction activities
     */
    public void startIdleEviction(final Dispatcher dispatcher) {
        final Dispatcher current = require(dispatcher, "Dispatcher");
        if (!evictionStarted.compareAndSet(false, true)) {
            return;
        }
        evictionDispatcher = current;
        if (runtimeDispatcher == null) {
            runtimeDispatcher = current;
        }
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
        final Dispatcher ownedDispatcher;
        synchronized (lock) {
            cancelEviction();
            for (final ArrayDeque<PooledConnection> bucket : idleIndex.buckets.values()) {
                while (!bucket.isEmpty()) {
                    final Connection connection = bucket.removeLast().connection();
                    if (seen.add(connection)) {
                        connections.add(connection);
                    }
                }
            }
            idleIndex.buckets.clear();
            idleIndex.entries.clear();
            for (final Connection connection : validatingIdle) {
                if (seen.add(connection)) {
                    connections.add(connection);
                }
            }
            validatingIdle.clear();
            validatedDestinations.clear();
            for (final ConnectionLease lease : List.copyOf(leased)) {
                if (seen.add(lease.connection())) {
                    connections.add(lease.connection());
                }
                if (lease.markClosed()) {
                    logicalReleased();
                }
            }
            leased.clear();
            active.clear();
            physicalByDestination.clear();
            multiplexCapacity.candidates.clear();
            multiplexCapacity.http1Destinations.clear();
            multiplexCapacity.multiplexDestinations.clear();
            expandedHttp1Destinations.clear();
            for (final Connection.Registration registration : multiplexCapacity.registrations.values()) {
                registration.close();
            }
            multiplexCapacity.registrations.clear();
            meter.addCounter(Counter.ACTIVE_PHYSICAL_CONNECTIONS, -physicalCount);
            physicalCount = 0;
            idleIndex.count = 0;
            creatingByDestination.clear();
            signalAllWaiters();
            if (!waiters.isEmpty()) {
                meter.addCounter(Counter.ACTIVE_WAITERS, -waiters.size());
            }
            waiters.clear();
            meter.addCounter(Counter.ACTIVE_LOGICAL_LEASES, -meter.counterValue(Counter.ACTIVE_LOGICAL_LEASES));
            ownedDispatcher = ownsRuntimeDispatcher ? runtimeDispatcher : null;
            runtimeDispatcher = null;
        }
        try {
            closeAll(connections);
        } finally {
            if (ownedDispatcher != null) {
                ownedDispatcher.close();
            }
        }
        if (Logger.isInfoEnabled()) {
            Logger.info(false, "Fabric", "Connection pool closed: physicalConnections={}", connections.size());
        }
    }

    /**
     * Returns pool counts by destination.
     *
     * @return immutable pool snapshot
     */
    public Map<Destination, Integer> snapshot() {
        synchronized (lock) {
            final LinkedHashMap<Destination, Integer> snapshot = new LinkedHashMap<>();
            for (final Map.Entry<Destination, ArrayDeque<PooledConnection>> entry : idleIndex.buckets.entrySet()) {
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
     * @param lease lease whose logical ownership is released or returned idle
     * @return true when this call changed state
     */
    boolean releaseLease(final ConnectionLease lease) {
        require(lease, "Connection lease");
        if (lease.pool() != this) {
            throw new StatefulException("Connection lease belongs to another pool");
        }
        if (lease.transientConnection()) {
            return closeLease(lease);
        }
        Connection closeable = null;
        boolean scheduleEviction = false;
        synchronized (lock) {
            if (!lease.markReleased()) {
                return false;
            }
            if (!leased.remove(lease)) {
                throw new StatefulException("Active connection lease is missing from its owning pool");
            }
            logicalReleased();
            final int remaining = decrementActive(lease.connection());
            if (remaining > 0) {
                signalHead();
                return true;
            }
            if (!closed.get() && !lease.leaked() && lease.connection().healthy()) {
                if (!lease.connection().multiplex() && handoff(lease.destination(), lease.connection())) {
                    return true;
                }
                final long releasedAtMillis = clock.millis();
                PooledConnection pooled = idleIndex.entries.get(lease.connection());
                if (pooled == null) {
                    pooled = new PooledConnection(lease.connection(), releasedAtMillis);
                    idleIndex.entries.put(lease.connection(), pooled);
                } else {
                    pooled.lastUsedMillis(releasedAtMillis);
                }
                idleIndex.buckets.computeIfAbsent(lease.destination(), ignored -> new ArrayDeque<>()).addLast(pooled);
                idleIndex.count++;
                removeMultiplex(lease.destination(), lease.connection());
                scheduleEviction = true;
            } else {
                closeable = lease.connection();
                removePhysical(lease.connection());
            }
            signalHead();
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
     * Closes a lease without returning its physical connection to idleIndex.buckets.
     *
     * @param lease lease removed without making its connection idle
     * @return true when this call changed state
     */
    boolean closeLease(final ConnectionLease lease) {
        require(lease, "Connection lease");
        if (lease.pool() != this) {
            throw new StatefulException("Connection lease belongs to another pool");
        }
        Connection closeable = null;
        synchronized (lock) {
            if (!lease.markClosed()) {
                return false;
            }
            if (!leased.remove(lease)) {
                throw new StatefulException("Active connection lease is missing from its owning pool");
            }
            logicalReleased();
            final int remaining = lease.transientConnection() ? 0 : decrementActive(lease.connection());
            if (remaining == 0) {
                closeable = lease.connection();
                if (lease.transientConnection()) {
                    removeTransientPhysical();
                } else {
                    removePhysical(lease.connection());
                }
            }
            signalHead();
        }
        if (closeable != null) {
            abortOne(closeable);
        }
        return true;
    }

    /**
     * Detaches a lease without returning it to idleIndex.buckets.
     *
     * @param lease leaked lease removed from active pool ownership
     */
    void detach(final ConnectionLease lease) {
        synchronized (lock) {
            if (leased.remove(lease)) {
                if (lease.transientConnection()) {
                    removeTransientPhysical();
                } else if (decrementActive(lease.connection()) == 0) {
                    removePhysical(lease.connection());
                }
                logicalReleased();
            }
            signalHead();
        }
    }

    /**
     * Attempts to acquire an already leased multiplex-capable connection.
     *
     * @param destination connection destination
     * @param waiter      fair waiter or null for an immediate caller
     * @return shared lease or null
     */
    private ConnectionLease acquireShared(final Destination destination, final PoolWaiters.Waiter waiter) {
        synchronized (lock) {
            ensureOpen();
            if (!hasTurn(waiter)) {
                return null;
            }
            Connection candidate = null;
            final ArrayDeque<Connection> candidates = multiplexCapacity.candidates.get(destination);
            if (candidates != null) {
                for (final Connection connection : candidates) {
                    if (usableCapacity(connection) > 0) {
                        candidate = connection;
                        break;
                    }
                }
            }
            if (candidate == null) {
                return null;
            }
            final ConnectionLease shared = ConnectionLease.reused(this, destination, candidate, clock.millis());
            leased.add(shared);
            active.merge(candidate, 1, Integer::sum);
            logicalAcquired();
            completeWaiter(waiter);
            return shared;
        }
    }

    /**
     * Attempts to acquire an idle connection.
     *
     * @param destination  connection destination
     * @param waiter       fair waiter or null for an immediate caller
     * @param cancellation cancellation scope that may become terminal during active validation
     * @return lease or null
     */
    private ConnectionLease acquireIdle(
            final Destination destination,
            final PoolWaiters.Waiter waiter,
            final Cancellation cancellation) {
        final Cancellation scope = require(cancellation, "Cancellation");
        while (true) {
            PooledConnection validating = null;
            Connection discarded = null;
            synchronized (lock) {
                ensureOpen();
                if (!hasTurn(waiter)) {
                    return null;
                }
                final ArrayDeque<PooledConnection> bucket = idleIndex.buckets.get(destination);
                if (bucket == null || bucket.isEmpty()) {
                    return null;
                }
                // Reuse the hottest connection first. Under closed-loop concurrency this preserves TLS/codec
                // cache locality and avoids rotating every connection across worker cores.
                final PooledConnection pooled = bucket.removeLast();
                final Connection connection = pooled.connection();
                idleIndex.count--;
                if (bucket.isEmpty()) {
                    idleIndex.buckets.remove(destination, bucket);
                }
                if (!connection.reusable()) {
                    removePhysical(connection);
                    discarded = connection;
                } else if (activeIdleValidationRequired(pooled, clock.millis())) {
                    validatingIdle.add(connection);
                    validating = pooled;
                } else {
                    return leaseIdleLocked(destination, connection, waiter);
                }
            }
            if (discarded != null) {
                closeOne(discarded);
                continue;
            }

            final PooledConnection candidate = require(validating, "Idle validation candidate");
            final Connection connection = candidate.connection();
            boolean valid;
            try {
                valid = connection.validateIdle();
            } catch (final RuntimeException ignored) {
                valid = false;
            }
            boolean closeable = false;
            synchronized (lock) {
                validatingIdle.remove(connection);
                if (closed.get()) {
                    closeable = true;
                } else if (!valid || !connection.reusable()) {
                    removePhysical(connection);
                    closeable = true;
                } else if (scope.cancelled() || waiter != null && waiter.cancelled || !hasTurn(waiter)) {
                    idleIndex.buckets.computeIfAbsent(destination, ignored -> new ArrayDeque<>()).addLast(candidate);
                    idleIndex.count++;
                    signalHead();
                    return null;
                } else {
                    return leaseIdleLocked(destination, connection, waiter);
                }
            }
            if (closeable) {
                closeOne(connection);
            }
        }
    }

    /**
     * Returns whether an exclusive idle HTTP/1 connection requires active peer-close validation.
     *
     * @param pooled retained idle connection and its protocol snapshot
     * @param now    current pool-clock time in epoch milliseconds
     * @return true when the connection is eligible and has reached the configured idle threshold
     */
    private boolean activeIdleValidationRequired(final PooledConnection pooled, final long now) {
        final Connection connection = pooled.connection();
        final Protocol protocol = pooled.protocol();
        if (connection.multiplex() || (protocol != Protocol.HTTP_1_0 && protocol != Protocol.HTTP_1_1)) {
            return false;
        }
        final long idleMillis = Math.max(0L, now - pooled.lastUsedMillis());
        return idleMillis >= policy.staleCheckAfter().toMillis();
    }

    /**
     * Registers an idle connection as a logical lease while holding the pool lock.
     *
     * @param destination connection destination
     * @param connection  reusable physical connection
     * @param waiter      fair waiter receiving the lease, or {@code null} for an immediate caller
     * @return logical lease marked as using a previously established physical connection
     */
    private ConnectionLease leaseIdleLocked(
            final Destination destination,
            final Connection connection,
            final PoolWaiters.Waiter waiter) {
        final ConnectionLease lease = ConnectionLease.reused(this, destination, connection, clock.millis());
        leased.add(lease);
        active.put(connection, 1);
        addMultiplex(destination, connection);
        logicalAcquired();
        completeWaiter(waiter);
        return lease;
    }

    /**
     * Reserves capacity to create another connection.
     *
     * @param destination connection destination
     * @param waiter      fair waiter or null for an immediate caller
     * @return true when under limit
     */
    private boolean reserveCreate(final Destination destination, final PoolWaiters.Waiter waiter) {
        synchronized (lock) {
            ensureOpen();
            if (!hasTurn(waiter) || !creationAvailable(destination)) {
                return false;
            }
            // Keep a multiplex-capable destination on one physical connection unless negotiation has explicitly
            // proved HTTP/1. The first H2 connection can be registered just before its stream capacity becomes
            // visible; treating physical registration alone as permission to create peers races a second TLS/H2
            // connection into that publication window. Proven H1 destinations retain normal parallel expansion.
            final int physical = physicalByDestination.getOrDefault(destination, 0);
            final int pending = creatingByDestination.getOrDefault(destination, 0);
            if (physical == 0 && pending != 0 || !multiplexCapacity.http1Destinations.contains(destination)
                    && multiplexCapacity.multiplexDestinations.contains(destination) && physical + pending != 0) {
                return false;
            }
            creating++;
            creatingByDestination.merge(destination, 1, Integer::sum);
            completeWaiter(waiter);
            return true;
        }
    }

    /**
     * Creates a new lease outside the pool lock with cancellation support.
     *
     * @param destination  destination reserved for the new physical connection
     * @param factory      supplier that opens the physical connection outside the pool lock
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
        } catch (final Throwable e) {
            releaseCreateReservation(destination);
            if (e instanceof Error error) {
                if (Logger.isErrorEnabled()) {
                    Logger.error(false, "Fabric", error, "Connection factory failed with an unrecoverable error");
                }
                throw error;
            }
            final RuntimeException failure = (RuntimeException) e;
            if (Logger.isErrorEnabled()) {
                Logger.error(
                        false,
                        "Fabric",
                        failure,
                        "Connection creation failed: exception={}",
                        failure.getClass().getSimpleName());
            }
            throw failure instanceof InternalException || failure instanceof ProtocolException
                    || failure instanceof SocketException || failure instanceof TimeoutException
                    || failure instanceof StatefulException || failure instanceof ValidateException ? failure
                            : new InternalException("Unable to create connection", failure);
        }
        final boolean multiplexCapable = connection.multiplex();
        final Connection.MultiplexAttachment attachment = multiplexCapable ? connection.multiplexAttachment() : null;
        final Connection.Registration registration = attachment == null ? null
                : attachment.listen((capacity, draining) -> capacityChanged(connection));
        boolean closeable = false;
        boolean cancelled = false;
        synchronized (lock) {
            releaseCreateReservationLocked(destination);
            if (closed.get() || scope.cancelled()) {
                closeable = true;
                cancelled = scope.cancelled();
            } else {
                final ConnectionLease lease = ConnectionLease.created(this, destination, connection, clock.millis());
                leased.add(lease);
                active.put(connection, 1);
                physicalCount++;
                physicalCreated();
                physicalByDestination.merge(destination, 1, Integer::sum);
                if (multiplexCapable) {
                    multiplexCapacity.multiplexDestinations.add(destination);
                    addMultiplex(destination, connection);
                } else {
                    multiplexCapacity.http1Destinations.add(destination);
                }
                if (registration != null) {
                    multiplexCapacity.registrations.put(connection, registration);
                }
                logicalAcquired();
                signalHead();
                Logger.debug(
                        false,
                        "Fabric",
                        "Physical connection created: multiplex={}, physicalConnections={}",
                        multiplexCapable,
                        physicalCount);
                return lease;
            }
            signalHead();
        }
        if (closeable) {
            if (registration != null) {
                registration.close();
            }
            closeOne(connection);
        }
        if (cancelled) {
            scope.throwIfCancelled();
        }
        throw new StatefulException("Connection pool is closed");
    }

    /**
     * Releases a reserved creation slot after factory failure.
     *
     * @param destination reserved destination
     */
    private void releaseCreateReservation(final Destination destination) {
        synchronized (lock) {
            releaseCreateReservationLocked(destination);
            signalHead();
        }
    }

    /**
     * Releases a reserved creation slot while holding the coordination lock.
     *
     * @param destination reserved destination
     */
    private void releaseCreateReservationLocked(final Destination destination) {
        creating--;
        final int remaining = creatingByDestination.getOrDefault(destination, 0) - 1;
        if (remaining <= 0) {
            creatingByDestination.remove(destination);
        } else {
            creatingByDestination.put(destination, remaining);
        }
    }

    /**
     * Waits for the caller's fair turn and an acquisition opportunity.
     *
     * @param waiter       caller waiter
     * @param cancellation cancellation scope
     * @param deadline     monotonic acquisition deadline
     */
    private void waitForAvailability(
            final PoolWaiters.Waiter waiter,
            final Cancellation cancellation,
            final long deadline) {
        final Cancellation scope = require(cancellation, "Cancellation");
        while (true) {
            final long remaining;
            synchronized (lock) {
                if (waiter.handoff != null) {
                    return;
                }
                if (!waiter.queued) {
                    waiter.queued = true;
                    waiter.queuedAtNanos = clock.nanos();
                    waiters.addLast(waiter);
                    meter.incrementCounter(Counter.WAITERS_ENQUEUED);
                    meter.incrementCounter(Counter.ACTIVE_WAITERS);
                    Logger.debug(
                            true,
                            "Fabric",
                            "Connection acquisition queued: waiters={}, physicalConnections={}",
                            waiters.size(),
                            physicalCount);
                }
                scope.throwIfCancelled();
                ensureOpen();
                if (hasTurn(waiter) && acquisitionAvailable(waiter.destination)) {
                    return;
                }
                remaining = remaining(deadline);
                if (remaining <= 0L) {
                    removeWaiterLocked(waiter);
                    if (Logger.isWarnEnabled()) {
                        Logger.warn(
                                false,
                                "Fabric",
                                "Connection acquisition timed out: physicalConnections={}, activeLeases={}",
                                physicalCount,
                                leased.size());
                    }
                    throw new TimeoutException("Connection acquire timed out");
                }
            }
            waitOnPool(waiter, remaining, cancellation.cancellable());
        }
    }

    /**
     * Waits on the coordination monitor for no longer than the remaining deadline.
     *
     * @param waiter      queued acquisition waiter being parked
     * @param remaining   remaining monotonic nanoseconds
     * @param cancellable whether periodic cancellation polling is required
     */
    private void waitOnPool(final PoolWaiters.Waiter waiter, final long remaining, final boolean cancellable) {
        final long interval = cancellable ? Math.min(remaining, 100_000_000L) : remaining;
        LockSupport.parkNanos(this, Math.max(1L, interval));
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for connection");
        }
    }

    /**
     * Returns whether the caller owns the current acquisition turn.
     *
     * @param waiter caller waiter or null for a new caller
     * @return true when the caller may attempt an acquisition
     */
    private boolean hasTurn(final PoolWaiters.Waiter waiter) {
        if (waiter == null) {
            return waiters.isEmpty();
        }
        if (!waiter.queued) {
            return waiters.isEmpty();
        }
        final int window = admissionWindow();
        int admitted = 0;
        for (final PoolWaiters.Waiter candidate : waiters) {
            if (candidate == waiter) {
                return true;
            }
            if (++admitted >= window) {
                return false;
            }
        }
        return false;
    }

    /**
     * Keeps negotiation and multiplex destinations strict; widens only after a destination is proven HTTP/1.
     */
    private int admissionWindow() {
        final PoolWaiters.Waiter head = waiters.peekFirst();
        if (head == null) {
            return 1;
        }
        final Destination destination = head.destination;
        return multiplexCapacity.http1Destinations.contains(destination) ? HTTP1_ADMISSION_WINDOW : 1;
    }

    /**
     * Completes a successful waiter turn and wakes the next waiter.
     *
     * @param waiter completed waiter or null
     */
    private void completeWaiter(final PoolWaiters.Waiter waiter) {
        if (waiter != null && waiters.remove(waiter)) {
            waiter.queued = false;
            meter.addCounter(Counter.ACTIVE_WAITERS, -1L);
            signalHead();
        }
    }

    /**
     * Removes an abandoned waiter.
     *
     * @param waiter abandoned waiter or null
     * @return transferred lease that was not consumed, or {@code null}
     */
    private ConnectionLease removeWaiter(final PoolWaiters.Waiter waiter) {
        if (waiter == null) {
            return null;
        }
        synchronized (lock) {
            removeWaiterLocked(waiter);
            final ConnectionLease abandoned = waiter.handoff;
            waiter.handoff = null;
            return abandoned;
        }
    }

    /**
     * Consumes a lease transferred directly by a releasing HTTP/1.1 owner.
     *
     * @param waiter acquisition waiter
     * @return transferred lease, or {@code null}
     */
    private ConnectionLease takeHandoff(final PoolWaiters.Waiter waiter) {
        synchronized (lock) {
            final ConnectionLease handedOff = waiter.handoff;
            waiter.handoff = null;
            return handedOff;
        }
    }

    /**
     * Transfers an available HTTP/1.1 connection directly to the compatible queue head.
     *
     * @param destination released connection destination
     * @param connection  healthy physical connection
     * @return true when ownership was transferred without entering the idle index
     */
    private boolean handoff(final Destination destination, final Connection connection) {
        final PoolWaiters.Waiter waiter = waiters.peekFirst();
        if (waiter == null || waiter.cancelled || !waiter.destination.equals(destination)) {
            return false;
        }
        final ConnectionLease handedOff = ConnectionLease.reused(this, destination, connection, clock.millis());
        leased.add(handedOff);
        active.put(connection, 1);
        logicalAcquired();
        waiter.handoff = handedOff;
        completeWaiter(waiter);
        LockSupport.unpark(waiter.thread);
        return true;
    }

    /**
     * Removes an abandoned waiter while holding the coordination lock.
     *
     * @param waiter abandoned waiter
     */
    private void removeWaiterLocked(final PoolWaiters.Waiter waiter) {
        if (waiters.remove(waiter)) {
            waiter.queued = false;
            meter.addCounter(Counter.ACTIVE_WAITERS, -1L);
            signalHead();
        }
    }

    /**
     * Returns whether the destination can reuse or create a connection.
     *
     * @param destination requested destination
     * @return true when an acquisition attempt may succeed
     */
    private boolean acquisitionAvailable(final Destination destination) {
        return existingCandidateAvailable(destination) || creationAvailable(destination);
    }

    /**
     * Returns whether physical connection capacity is available.
     *
     * @param destination requested destination
     * @return true when global and destination limits both allow creation
     */
    private boolean creationAvailable(final Destination destination) {
        final int physical = physicalByDestination.getOrDefault(destination, 0);
        final int pending = creatingByDestination.getOrDefault(destination, 0);
        final int normalLimit = Math.min(HTTP1_BASE_CONNECTIONS, policy.maxConnectionsPerDestination());
        final boolean pressure = physical + pending >= normalLimit && sustainedWait(destination);
        return PoolAdmission.allows(
                physicalCount,
                creating,
                physical,
                pending,
                policy.maxConnections(),
                policy.maxConnectionsPerDestination(),
                normalLimit,
                multiplexCapacity.http1Destinations.contains(destination),
                multiplexCapacity.multiplexDestinations.contains(destination),
                pressure);
    }

    /**
     * Returns true when queue depth or sustained wait proves that a negotiated H1 service needs expansion.
     */
    private boolean sustainedWait(final Destination destination) {
        if (!multiplexCapacity.http1Destinations.contains(destination)) {
            return false;
        }
        if (expandedHttp1Destinations.contains(destination)) {
            return true;
        }
        final long now = clock.nanos();
        int destinationWaiters = 0;
        boolean aged = false;
        for (final PoolWaiters.Waiter waiter : waiters) {
            if (waiter.destination.equals(destination)) {
                destinationWaiters++;
                aged |= waiter.queuedAtNanos != 0L && now - waiter.queuedAtNanos >= HTTP1_EXPANSION_WAIT_NANOS;
            }
        }
        if (destinationWaiters >= HTTP1_EXPANSION_LATCH_WAITERS) {
            expandedHttp1Destinations.add(destination);
            return true;
        }
        return aged;
    }

    /**
     * Computes a monotonic deadline with overflow protection.
     *
     * @param timeout acquisition timeout
     * @return monotonic deadline
     */
    private long deadline(final Duration timeout) {
        final long started = clock.nanos();
        final long nanos;
        try {
            nanos = timeout.toNanos();
        } catch (final ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
        if (nanos > 0L && started > Long.MAX_VALUE - nanos) {
            return Long.MAX_VALUE;
        }
        return started + nanos;
    }

    /**
     * Computes remaining monotonic time with overflow protection.
     *
     * @param deadline monotonic deadline
     * @return remaining nanoseconds
     */
    private long remaining(final long deadline) {
        if (deadline == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return deadline - clock.nanos();
    }

    /**
     * Returns whether an existing connection can be retried by the acquire loop.
     *
     * @param destination requested destination
     * @return true when an idle or multiplex-capable active connection may satisfy the request
     */
    private boolean existingCandidateAvailable(final Destination destination) {
        final ArrayDeque<PooledConnection> bucket = idleIndex.buckets.get(destination);
        if (bucket != null && !bucket.isEmpty()) {
            return true;
        }
        final ArrayDeque<Connection> candidates = multiplexCapacity.candidates.get(destination);
        if (candidates == null) {
            return false;
        }
        for (final Connection connection : candidates) {
            if (usableCapacity(connection) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decrements an active physical connection reference count.
     *
     * @param connection physical connection whose logical reference count is decremented
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
     * Records one logical lease acquisition on the fixed meter path.
     */
    private void logicalAcquired() {
        meter.incrementCounter(Counter.LOGICAL_LEASES_ACQUIRED);
        meter.incrementCounter(Counter.ACTIVE_LOGICAL_LEASES);
    }

    /**
     * Records one logical lease terminal transition on the fixed meter path.
     */
    private void logicalReleased() {
        meter.incrementCounter(Counter.LOGICAL_LEASES_RELEASED);
        meter.addCounter(Counter.ACTIVE_LOGICAL_LEASES, -1L);
    }

    /**
     * Records one physical connection creation and its active ownership.
     */
    private void physicalCreated() {
        meter.incrementCounter(Counter.PHYSICAL_CONNECTIONS_CREATED);
        meter.incrementCounter(Counter.ACTIVE_PHYSICAL_CONNECTIONS);
    }

    /**
     * Records one physical connection leaving active pool ownership.
     */
    private void physicalRemoved() {
        meter.addCounter(Counter.ACTIVE_PHYSICAL_CONNECTIONS, -1L);
    }

    /**
     * Adds a multiplex connection once to its destination candidate queue.
     *
     * @param destination destination candidate queue
     * @param connection  multiplex connection to register
     */
    private void addMultiplex(final Destination destination, final Connection connection) {
        if (!connection.multiplex() || connection.draining()) {
            return;
        }
        final ArrayDeque<Connection> candidates = multiplexCapacity.candidates
                .computeIfAbsent(destination, ignored -> new ArrayDeque<>());
        if (!candidates.contains(connection)) {
            candidates.addLast(connection);
        }
    }

    /**
     * Removes a multiplex candidate.
     *
     * @param destination destination candidate queue
     * @param connection  multiplex connection to remove
     */
    private void removeMultiplex(final Destination destination, final Connection connection) {
        final ArrayDeque<Connection> candidates = multiplexCapacity.candidates.get(destination);
        if (candidates != null) {
            candidates.remove(connection);
            if (candidates.isEmpty()) {
                multiplexCapacity.candidates.remove(destination);
            }
        }
    }

    /**
     * Returns current protocol-owned logical capacity, or zero when unavailable.
     *
     * @param connection physical connection whose capacity is queried
     * @return available logical stream capacity
     */
    private int usableCapacity(final Connection connection) {
        if (!connection.healthy() || connection.draining()) {
            return 0;
        }
        final Connection.MultiplexAttachment attachment = connection.multiplexAttachment();
        if (attachment != null) {
            return attachment.draining() ? 0 : Math.max(0, attachment.capacity());
        }
        return connection.multiplex() ? Math.max(0, connection.capacity()) : 0;
    }

    /**
     * Reacts to a protocol capacity publication with one precise waiter signal.
     *
     * @param connection physical connection publishing new capacity
     */
    private void capacityChanged(final Connection connection) {
        synchronized (lock) {
            if (!active.containsKey(connection)) {
                return;
            }
            final Destination destination = connection.destination();
            if (usableCapacity(connection) > 0) {
                addMultiplex(destination, connection);
            } else if (connection.draining()) {
                removeMultiplex(destination, connection);
            }
            signalHead();
        }
    }

    /**
     * Removes final physical ownership and listener state.
     *
     * @param connection physical connection leaving the pool
     */
    private void removePhysical(final Connection connection) {
        if (physicalCount <= 0) {
            return;
        }
        final Destination destination = connection.destination();
        physicalCount--;
        physicalRemoved();
        final int remaining = physicalByDestination.getOrDefault(destination, 0) - 1;
        if (remaining <= 0) {
            physicalByDestination.remove(destination);
            multiplexCapacity.http1Destinations.remove(destination);
            expandedHttp1Destinations.remove(destination);
        } else {
            physicalByDestination.put(destination, remaining);
        }
        removeMultiplex(destination, connection);
        idleIndex.entries.remove(connection);
        final Connection.Registration registration = multiplexCapacity.registrations.remove(connection);
        if (registration != null) {
            registration.close();
        }
    }

    /**
     * Removes a non-pooled physical connection that was never entered in destination capacity maps.
     */
    private void removeTransientPhysical() {
        if (physicalCount > 0) {
            physicalCount--;
            physicalRemoved();
        }
    }

    /**
     * Unparks the oldest eligible negotiated-protocol admission window.
     */
    private void signalHead() {
        final int window = admissionWindow();
        int admitted = 0;
        for (final PoolWaiters.Waiter waiter : waiters) {
            LockSupport.unpark(waiter.thread);
            if (++admitted >= window) {
                break;
            }
        }
    }

    /**
     * Unparks every waiter for terminal close or cancellation checks.
     */
    private void signalAllWaiters() {
        for (final PoolWaiters.Waiter waiter : waiters) {
            LockSupport.unpark(waiter.thread);
        }
    }

    /**
     * Schedules the next idle eviction run.
     */
    private void scheduleEvictionIfNeeded() {
        final Dispatcher dispatcher = evictionDispatcher;
        if (dispatcher == null || evictionHandle != null || closed.get() || !evictionStarted.get()) {
            return;
        }
        synchronized (lock) {
            if (evictionHandle != null) {
                return;
            }
            final Duration delay = evictionDelay();
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
        evictIdle();
        scheduleEvictionIfNeeded();
    }

    /**
     * Returns next eviction delay.
     *
     * @return shortest delay until an idle entry expires, or {@code null} when none are idle
     */
    private Duration evictionDelay() {
        final Duration keepAlive = policy.keepAlive();
        if (keepAlive.isZero()) {
            return Duration.ZERO;
        }
        final Instant now = clock.now();
        Duration delay = null;
        for (final ArrayDeque<PooledConnection> bucket : idleIndex.buckets.values()) {
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
     * @param connections physical connections to close, aggregating failures
     */
    private static void closeAll(final List<Connection> connections) {
        RuntimeException failure = null;
        for (final Connection connection : connections) {
            try {
                connection.close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
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
     * @param connection physical pooled connection to close
     */
    private static void closeOne(final Connection connection) {
        try {
            connection.close();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close pooled connection", e);
        }
    }

    /**
     * Aborts one connection after its lease has become terminally non-reusable.
     */
    private static void abortOne(final Connection connection) {
        try {
            connection.abort();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to abort pooled connection", e);
        }
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Validates that destination options have stable value semantics.
     *
     * @param destination destination to validate
     */
    private static void validateDestination(final Destination destination) {
        for (final Map.Entry<String, Object> entry : destination.options().asMap().entrySet()) {
            if (!stableOptionValue(entry.getValue())) {
                throw new ValidateException("Connection destination option must be a stable value: " + entry.getKey());
            }
        }
    }

    /**
     * Returns whether an option value is immutable and value-comparable.
     *
     * @param value option value
     * @return true when the value is safe in a destination identity
     */
    private static boolean stableOptionValue(final Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Context || value instanceof Supplier<?> || value instanceof Dispatcher
                || value instanceof Collection<?> || value instanceof Map<?, ?> || value.getClass().isArray()) {
            return false;
        }
        return value instanceof TlsPolicy || value instanceof TlsContext || value instanceof TlsSettings
                || value instanceof String || value instanceof Boolean || value instanceof Character
                || value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double || value instanceof BigInteger
                || value instanceof BigDecimal || value instanceof Duration || value instanceof Timeout
                || value instanceof Enum<?>;
    }

}
