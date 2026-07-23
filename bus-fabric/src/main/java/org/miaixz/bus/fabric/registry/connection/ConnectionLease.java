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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;

/**
 * Lease for a borrowed network connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ConnectionLease {

    /**
     * Lease-state CAS without a per-lease atomic wrapper.
     */
    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(ConnectionLease.class, "state", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Lease is active.
     */
    private static final int LEASED = 0;

    /**
     * Lease was returned for reuse.
     */
    private static final int RELEASED = 1;

    /**
     * Lease was terminally closed.
     */
    private static final int CLOSED = 2;

    /**
     * Process-wide sequence allocated only when a diagnostic lease identifier is requested.
     */
    private static final AtomicLong IDS = new AtomicLong();

    /**
     * Pool responsible for atomic release and close transitions.
     */
    private final ConnectionPool pool;

    /**
     * Destination under which the physical connection was acquired.
     */
    private final Destination destination;

    /**
     * Borrowed connection.
     */
    private final Connection connection;

    /**
     * Lazily materialized diagnostic identifier.
     */
    private volatile String id;

    /**
     * Wall-clock time recorded when the lease was acquired.
     */
    private final long acquiredAtMillis;

    /** Whether this lease owns an explicitly non-reusable physical connection. */
    private final boolean transientConnection;

    /**
     * VarHandle-managed state initialized to {@link #LEASED}.
     */
    private volatile int state;

    /**
     * Whether leak handling performed the terminal close transition.
     */
    private volatile boolean leaked;

    /**
     * Creates a connection lease.
     *
     * @param pool        owning connection pool
     * @param destination destination associated with the acquisition
     * @param connection  borrowed physical connection
     * @param acquiredAt  wall-clock acquisition time
     * @throws ValidateException if any component is {@code null}
     */
    ConnectionLease(final ConnectionPool pool, final Destination destination, final Connection connection,
            final Instant acquiredAt) {
        this(pool, destination, connection, require(acquiredAt, "Acquired time").toEpochMilli());
    }

    /**
     * Creates a lease from an allocation-free epoch-millisecond timestamp.
     *
     * @param pool             owning connection pool
     * @param destination      destination key used by the pool
     * @param connection       leased physical connection
     * @param acquiredAtMillis acquisition time in epoch milliseconds
     */
    ConnectionLease(final ConnectionPool pool, final Destination destination, final Connection connection,
            final long acquiredAtMillis) {
        this(pool, destination, connection, acquiredAtMillis, false);
    }

    /**
     * Creates a lease and records whether release must remain terminal.
     *
     * @param pool                owning connection pool
     * @param destination         destination key used by the pool
     * @param connection          leased physical connection
     * @param acquiredAtMillis    acquisition time in epoch milliseconds
     * @param transientConnection whether release must close rather than pool the connection
     */
    ConnectionLease(final ConnectionPool pool, final Destination destination, final Connection connection,
            final long acquiredAtMillis, final boolean transientConnection) {
        this.pool = require(pool, "Connection pool");
        this.destination = require(destination, "Connection destination");
        this.connection = require(connection, "Connection");
        if (acquiredAtMillis < 0) {
            throw new ValidateException("Acquired time must not be negative");
        }
        this.acquiredAtMillis = acquiredAtMillis;
        this.transientConnection = transientConnection;
    }

    /**
     * Returns the connection destination.
     *
     * @return destination associated with the acquired connection
     */
    public Destination destination() {
        return destination;
    }

    /**
     * Returns the connection.
     *
     * @return borrowed physical connection
     */
    public Connection connection() {
        return connection;
    }

    /**
     * Returns acquisition time.
     *
     * @return wall-clock acquisition timestamp
     */
    public Instant acquiredAt() {
        return Instant.ofEpochMilli(acquiredAtMillis);
    }

    /**
     * Returns the lightweight lease trace id.
     *
     * @return lazily allocated process-unique identifier prefixed with {@code lease-}
     */
    public String id() {
        String current = id;
        if (current == null) {
            synchronized (this) {
                current = id;
                if (current == null) {
                    current = "lease-" + IDS.incrementAndGet();
                    id = current;
                }
            }
        }
        return current;
    }

    /**
     * Releases this lease back to the pool.
     *
     * @return {@code true} when the pool accepts this call's leased-to-released transition
     */
    public boolean release() {
        return pool.releaseLease(this);
    }

    /**
     * Closes this lease and its connection.
     *
     * @return {@code true} when the pool accepts this call's leased-to-closed transition
     */
    public boolean close() {
        return pool.closeLease(this);
    }

    /**
     * Returns whether this lease has been released.
     *
     * @return {@code true} only when the current state is released, not closed
     */
    public boolean released() {
        return (int) STATE.getVolatile(this) == RELEASED;
    }

    /**
     * Requests terminal close through the pool and records a leak only when this call performs that transition.
     */
    public void leak() {
        if (pool.closeLease(this)) {
            leaked = true;
        }
    }

    /**
     * Marks this lease released from the owning pool.
     *
     * @return {@code true} when state changes atomically from leased to released
     */
    boolean markReleased() {
        return STATE.compareAndSet(this, LEASED, RELEASED);
    }

    /**
     * Marks this lease closed from the owning pool.
     *
     * @return {@code true} when state changes atomically from leased to closed
     */
    boolean markClosed() {
        return STATE.compareAndSet(this, LEASED, CLOSED);
    }

    /**
     * Returns whether this lease is leaked.
     *
     * @return {@code true} when leak handling performed the terminal close transition
     */
    boolean leaked() {
        return leaked;
    }

    /**
     * Returns whether this connection can never be returned to the idle pool.
     *
     * @return {@code true} when release must close the physical connection
     */
    boolean transientConnection() {
        return transientConnection;
    }

    /**
     * Returns the owning pool.
     *
     * @return pool that owns this lease's state transitions
     */
    ConnectionPool pool() {
        return pool;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical reference name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
