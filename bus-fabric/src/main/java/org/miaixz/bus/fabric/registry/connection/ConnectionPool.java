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
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Context;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter.Counter;
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
     * Pool-owned time source.
     */
    private final Clock clock;

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
     * Physical connection count by destination.
     */
    private final Map<Destination, Integer> physicalByDestination;

    /**
     * Multiplex-capable active connections by destination.
     */
    private final Map<Destination, ArrayDeque<Connection>> multiplex;

    /**
     * Multiplex capacity listener registrations.
     */
    private final Map<Connection, Connection.Registration> capacityRegistrations;

    /**
     * O(1) total physical connection count.
     */
    private int physicalCount;

    /**
     * O(1) total idle connection count.
     */
    private int idleCount;

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
    private final ArrayDeque<PoolWaiter> waiters;

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
     * @param policy pool policy
     * @param clock  pool time source
     */
    private ConnectionPool(final PoolPolicy policy, final Clock clock, final FabricMeter meter,
            final Dispatcher dispatcher) {
        this.policy = policy;
        this.clock = clock;
        this.meter = meter;
        this.runtimeDispatcher = dispatcher;
        this.idle = new ConcurrentHashMap<>();
        this.leased = Collections.newSetFromMap(new IdentityHashMap<>());
        this.active = new IdentityHashMap<>();
        this.physicalByDestination = new LinkedHashMap<>();
        this.multiplex = new LinkedHashMap<>();
        this.capacityRegistrations = new IdentityHashMap<>();
        this.lock = new Object();
        this.creatingByDestination = new LinkedHashMap<>();
        this.waiters = new ArrayDeque<>();
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
        validateDestination(target);
        require(factory, "Connection factory");
        final Cancellation scope = require(cancellation, "Cancellation");
        final long deadline = deadline(policy.acquireTimeout());
        PoolWaiter waiter = null;
        final Runnable unregister = scope.onCancel(() -> {
            synchronized (lock) {
                signalAllWaiters();
            }
        });
        try {
            while (true) {
                scope.throwIfCancelled();
                final ConnectionLease shared = acquireShared(target, waiter);
                if (shared != null) {
                    return shared;
                }
                scope.throwIfCancelled();
                final ConnectionLease reused = acquireIdle(target, waiter);
                if (reused != null) {
                    return reused;
                }
                scope.throwIfCancelled();
                if (reserveCreate(target, waiter)) {
                    return createLease(target, factory, scope);
                }
                if (waiter == null) {
                    waiter = new PoolWaiter(target);
                }
                waitForAvailability(waiter, scope, deadline);
            }
        } finally {
            removeWaiter(waiter);
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
            return idleCount;
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
            return physicalCount;
        }
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
            for (final ArrayDeque<PooledConnection> bucket : idle.values()) {
                final ArrayDeque<PooledConnection> retained = new ArrayDeque<>();
                while (!bucket.isEmpty()) {
                    final PooledConnection pooled = bucket.removeFirst();
                    idleCount--;
                    final boolean expired = Duration.between(pooled.lastUsed(), now).compareTo(policy.keepAlive()) > 0;
                    if (expired || kept >= policy.maxIdle()) {
                        evicted.add(pooled.connection());
                    } else {
                        retained.addLast(pooled);
                        idleCount++;
                        kept++;
                    }
                }
                bucket.addAll(retained);
            }
            idle.entrySet().removeIf(entry -> entry.getValue().isEmpty());
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
     * @param dispatcher dispatcher
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
                if (lease.markClosed()) {
                    logicalReleased();
                }
            }
            leased.clear();
            active.clear();
            physicalByDestination.clear();
            multiplex.clear();
            for (final Connection.Registration registration : capacityRegistrations.values()) {
                registration.close();
            }
            capacityRegistrations.clear();
            physicalCount = 0;
            idleCount = 0;
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
            logicalReleased();
            final int remaining = decrementActive(lease.connection());
            if (remaining > 0) {
                signalHead();
                return true;
            }
            if (!closed.get() && !lease.leaked() && lease.connection().healthy()) {
                idle.computeIfAbsent(lease.destination(), ignored -> new ArrayDeque<>())
                        .addLast(new PooledConnection(lease.connection(), clock.now()));
                idleCount++;
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
            logicalReleased();
            final int remaining = decrementActive(lease.connection());
            if (remaining == 0) {
                closeable = lease.connection();
                removePhysical(lease.connection());
            }
            signalHead();
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
                if (decrementActive(lease.connection()) == 0) {
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
    private ConnectionLease acquireShared(final Destination destination, final PoolWaiter waiter) {
        synchronized (lock) {
            ensureOpen();
            if (!hasTurn(waiter)) {
                return null;
            }
            Connection candidate = null;
            final ArrayDeque<Connection> candidates = multiplex.get(destination);
            if (candidates != null) {
                for (final Connection connection : candidates) {
                    if (usableCapacity(connection) > active.getOrDefault(connection, 0)) {
                        candidate = connection;
                        break;
                    }
                }
            }
            if (candidate == null) {
                return null;
            }
            final ConnectionLease shared = new ConnectionLease(this, destination, candidate, clock.now());
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
     * @param destination connection destination
     * @param waiter      fair waiter or null for an immediate caller
     * @return lease or null
     */
    private ConnectionLease acquireIdle(final Destination destination, final PoolWaiter waiter) {
        final List<Connection> discarded = new ArrayList<>();
        ConnectionLease lease = null;
        boolean cancelEviction = false;
        synchronized (lock) {
            ensureOpen();
            if (!hasTurn(waiter)) {
                return null;
            }
            final ArrayDeque<PooledConnection> bucket = idle.get(destination);
            while (bucket != null && !bucket.isEmpty()) {
                final Connection connection = bucket.removeFirst().connection();
                idleCount--;
                if (connection.healthy()) {
                    lease = new ConnectionLease(this, destination, connection, clock.now());
                    leased.add(lease);
                    active.put(connection, 1);
                    addMultiplex(destination, connection);
                    logicalAcquired();
                    if (bucket.isEmpty()) {
                        idle.remove(destination, bucket);
                    }
                    cancelEviction = idleCount == 0;
                    completeWaiter(waiter);
                    break;
                }
                discarded.add(connection);
                removePhysical(connection);
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
     * Reserves capacity to create another connection.
     *
     * @param destination connection destination
     * @param waiter      fair waiter or null for an immediate caller
     * @return true when under limit
     */
    private boolean reserveCreate(final Destination destination, final PoolWaiter waiter) {
        synchronized (lock) {
            ensureOpen();
            if (!hasTurn(waiter) || !creationAvailable(destination)) {
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
        } catch (final Throwable e) {
            releaseCreateReservation(destination);
            if (e instanceof Error error) {
                throw error;
            }
            final RuntimeException failure = (RuntimeException) e;
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
                final ConnectionLease lease = new ConnectionLease(this, destination, connection, clock.now());
                leased.add(lease);
                active.put(connection, 1);
                physicalCount++;
                physicalByDestination.merge(destination, 1, Integer::sum);
                if (multiplexCapable) {
                    addMultiplex(destination, connection);
                }
                if (registration != null) {
                    capacityRegistrations.put(connection, registration);
                }
                logicalAcquired();
                signalHead();
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
    private void waitForAvailability(final PoolWaiter waiter, final Cancellation cancellation, final long deadline) {
        final Cancellation scope = require(cancellation, "Cancellation");
        while (true) {
            final long remaining;
            synchronized (lock) {
                if (!waiter.queued) {
                    waiter.queued = true;
                    waiters.addLast(waiter);
                    meter.incrementCounter(Counter.WAITERS_ENQUEUED);
                    meter.incrementCounter(Counter.ACTIVE_WAITERS);
                }
                scope.throwIfCancelled();
                ensureOpen();
                if (hasTurn(waiter) && acquisitionAvailable(waiter.destination)) {
                    return;
                }
                remaining = remaining(deadline);
                if (remaining <= 0L) {
                    removeWaiterLocked(waiter);
                    throw new TimeoutException("Connection acquire timed out");
                }
            }
            waitOnPool(waiter, remaining);
        }
    }

    /**
     * Waits on the coordination monitor for no longer than the remaining deadline.
     *
     * @param remaining remaining monotonic nanoseconds
     */
    private void waitOnPool(final PoolWaiter waiter, final long remaining) {
        LockSupport.parkNanos(this, Math.max(1L, Math.min(remaining, 100_000_000L)));
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
    private boolean hasTurn(final PoolWaiter waiter) {
        return waiter == null ? waiters.isEmpty() : waiters.peekFirst() == waiter;
    }

    /**
     * Completes a successful waiter turn and wakes the next waiter.
     *
     * @param waiter completed waiter or null
     */
    private void completeWaiter(final PoolWaiter waiter) {
        if (waiter != null && waiters.peekFirst() == waiter) {
            waiters.removeFirst();
            waiter.queued = false;
            meter.addCounter(Counter.ACTIVE_WAITERS, -1L);
            signalHead();
        }
    }

    /**
     * Removes an abandoned waiter.
     *
     * @param waiter abandoned waiter or null
     */
    private void removeWaiter(final PoolWaiter waiter) {
        if (waiter == null) {
            return;
        }
        synchronized (lock) {
            removeWaiterLocked(waiter);
        }
    }

    /**
     * Removes an abandoned waiter while holding the coordination lock.
     *
     * @param waiter abandoned waiter
     */
    private void removeWaiterLocked(final PoolWaiter waiter) {
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
        if (destination.multiplex() && creatingByDestination.getOrDefault(destination, 0) > 0) {
            return false;
        }
        return physicalCount + creating < policy.maxConnections() && physicalByDestination.getOrDefault(destination, 0)
                + creatingByDestination.getOrDefault(destination, 0) < policy.maxConnectionsPerDestination();
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
        final ArrayDeque<PooledConnection> bucket = idle.get(destination);
        if (bucket != null && !bucket.isEmpty()) {
            return true;
        }
        final ArrayDeque<Connection> candidates = multiplex.get(destination);
        if (candidates == null) {
            return false;
        }
        for (final Connection connection : candidates) {
            if (usableCapacity(connection) > active.getOrDefault(connection, 0)) {
                return true;
            }
        }
        return false;
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
     * Adds a multiplex connection once to its destination candidate queue.
     */
    private void addMultiplex(final Destination destination, final Connection connection) {
        if (!connection.multiplex() || connection.draining()) {
            return;
        }
        final ArrayDeque<Connection> candidates = multiplex.computeIfAbsent(destination, ignored -> new ArrayDeque<>());
        if (!candidates.contains(connection)) {
            candidates.addLast(connection);
        }
    }

    /**
     * Removes a multiplex candidate.
     */
    private void removeMultiplex(final Destination destination, final Connection connection) {
        final ArrayDeque<Connection> candidates = multiplex.get(destination);
        if (candidates != null) {
            candidates.remove(connection);
            if (candidates.isEmpty()) {
                multiplex.remove(destination);
            }
        }
    }

    /**
     * Returns current protocol-owned logical capacity, or zero when unavailable.
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
     */
    private void capacityChanged(final Connection connection) {
        synchronized (lock) {
            if (!active.containsKey(connection)) {
                return;
            }
            final Destination destination = connection.destination();
            if (usableCapacity(connection) > active.getOrDefault(connection, 0)) {
                addMultiplex(destination, connection);
            } else if (connection.draining()) {
                removeMultiplex(destination, connection);
            }
            signalHead();
        }
    }

    /**
     * Removes final physical ownership and listener state.
     */
    private void removePhysical(final Connection connection) {
        if (physicalCount <= 0) {
            return;
        }
        final Destination destination = connection.destination();
        physicalCount--;
        final int remaining = physicalByDestination.getOrDefault(destination, 0) - 1;
        if (remaining <= 0) {
            physicalByDestination.remove(destination);
        } else {
            physicalByDestination.put(destination, remaining);
        }
        removeMultiplex(destination, connection);
        final Connection.Registration registration = capacityRegistrations.remove(connection);
        if (registration != null) {
            registration.close();
        }
    }

    /**
     * Unparks only the oldest eligible waiter.
     */
    private void signalHead() {
        final PoolWaiter waiter = waiters.peekFirst();
        if (waiter != null) {
            LockSupport.unpark(waiter.thread);
        }
    }

    /**
     * Unparks every waiter for terminal close or cancellation checks.
     */
    private void signalAllWaiters() {
        for (final PoolWaiter waiter : waiters) {
            LockSupport.unpark(waiter.thread);
        }
    }

    /**
     * Schedules the next idle eviction run.
     */
    private void scheduleEvictionIfNeeded() {
        final Dispatcher dispatcher = evictionDispatcher;
        if (dispatcher == null || closed.get() || !evictionStarted.get()) {
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
     * @return delay
     */
    private Duration evictionDelay() {
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
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
        return value instanceof TlsContext || value instanceof TlsSettings || value instanceof String
                || value instanceof Boolean || value instanceof Character || value instanceof Byte
                || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double || value instanceof java.math.BigInteger
                || value instanceof java.math.BigDecimal || value instanceof Duration || value instanceof Timeout
                || value instanceof Enum<?>;
    }

    /**
     * Fair acquisition waiter.
     *
     * @param destination requested destination
     */
    private static final class PoolWaiter {

        /**
         * Requested destination.
         */
        private final Destination destination;

        /**
         * Exact thread to unpark.
         */
        private final Thread thread;

        /**
         * Queue membership guard.
         */
        private boolean queued;

        /**
         * Captures the requesting thread and destination before the waiter enters the fair queue.
         *
         * @param destination requested destination
         */
        private PoolWaiter(final Destination destination) {
            this.destination = destination;
            this.thread = Thread.currentThread();
        }

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
