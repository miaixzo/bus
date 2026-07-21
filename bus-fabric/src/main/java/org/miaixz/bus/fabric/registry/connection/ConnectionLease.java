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

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.Assert;
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
     * Lease id sequence.
     */
    private static final AtomicLong IDS = new AtomicLong();

    /**
     * Owning pool.
     */
    private final ConnectionPool pool;

    /**
     * Connection destination.
     */
    private final Destination destination;

    /**
     * Borrowed connection.
     */
    private final Connection connection;

    /**
     * Lightweight lease trace id.
     */
    private final long sequence;

    /**
     * Lazily materialized diagnostic identifier.
     */
    private volatile String id;

    /**
     * Acquisition time.
     */
    private final Instant acquiredAt;

    /**
     * Lease state.
     */
    private final AtomicInteger state;

    /**
     * Leak flag.
     */
    private volatile boolean leaked;

    /**
     * Creates a connection lease.
     *
     * @param pool        pool
     * @param destination destination
     * @param connection  connection
     * @param acquiredAt  acquired time
     */
    ConnectionLease(final ConnectionPool pool, final Destination destination, final Connection connection,
            final Instant acquiredAt) {
        this.pool = require(pool, "Connection pool");
        this.destination = require(destination, "Connection destination");
        this.connection = require(connection, "Connection");
        this.acquiredAt = require(acquiredAt, "Acquired time");
        this.sequence = IDS.incrementAndGet();
        this.state = new AtomicInteger(LEASED);
    }

    /**
     * Returns the connection destination.
     *
     * @return destination
     */
    public Destination destination() {
        return destination;
    }

    /**
     * Returns the connection.
     *
     * @return connection
     */
    public Connection connection() {
        return connection;
    }

    /**
     * Returns acquisition time.
     *
     * @return acquisition time
     */
    public Instant acquiredAt() {
        return acquiredAt;
    }

    /**
     * Returns the lightweight lease trace id.
     *
     * @return id
     */
    public String id() {
        String current = id;
        if (current == null) {
            current = "lease-" + sequence;
            id = current;
        }
        return current;
    }

    /**
     * Releases this lease back to the pool.
     *
     * @return true when released by this call
     */
    public boolean release() {
        return pool.releaseLease(this);
    }

    /**
     * Closes this lease and its connection.
     *
     * @return true when closed by this call
     */
    public boolean close() {
        return pool.closeLease(this);
    }

    /**
     * Returns whether this lease has been released.
     *
     * @return true when released
     */
    public boolean released() {
        return state.get() == RELEASED;
    }

    /**
     * Marks this lease as leaked and closes the connection when needed.
     */
    public void leak() {
        if (pool.closeLease(this)) {
            leaked = true;
        }
    }

    /**
     * Marks this lease released from the owning pool.
     *
     * @return true when state changed
     */
    boolean markReleased() {
        return state.compareAndSet(LEASED, RELEASED);
    }

    /**
     * Marks this lease closed from the owning pool.
     *
     * @return true when state changed
     */
    boolean markClosed() {
        return state.compareAndSet(LEASED, CLOSED);
    }

    /**
     * Returns whether this lease is leaked.
     *
     * @return true when leaked
     */
    boolean leaked() {
        return leaked;
    }

    /**
     * Returns the owning pool.
     *
     * @return pool
     */
    ConnectionPool pool() {
        return pool;
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

}
