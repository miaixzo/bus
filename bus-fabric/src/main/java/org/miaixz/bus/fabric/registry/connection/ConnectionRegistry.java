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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter;
import org.miaixz.bus.fabric.observe.metrics.FabricMeter.Counter;

/**
 * Thread-safe registry for active network connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ConnectionRegistry implements AutoCloseable {

    /**
     * Active connections grouped by destination.
     */
    private final ConcurrentHashMap<Destination, CopyOnWriteArrayList<Connection>> active;

    /**
     * Stable registration facts keyed by physical connection.
     */
    private final ConcurrentHashMap<Connection, Registration> registrations;

    /**
     * Monotonic physical connection identifier.
     */
    private final AtomicLong sequence;

    /**
     * Borrowed runtime meter.
     */
    private final FabricMeter meter;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates an empty connection registry.
     */
    public ConnectionRegistry() {
        this(FabricMeter.create());
    }

    /**
     * Creates an empty registry borrowing an existing meter.
     *
     * @param meter borrowed meter
     */
    public ConnectionRegistry(final FabricMeter meter) {
        this.active = new ConcurrentHashMap<>();
        this.registrations = new ConcurrentHashMap<>();
        this.sequence = new AtomicLong();
        this.meter = require(meter, "Fabric meter");
        this.closed = new AtomicBoolean();
    }

    /**
     * Registers an active connection.
     *
     * @param destination connection destination
     * @param connection  connection
     */
    public void open(final Destination destination, final Connection connection) {
        require(destination, "Connection destination");
        require(connection, "Connection");
        synchronized (closed) {
            if (closed.get()) {
                throw new StatefulException("Connection registry is closed");
            }
            if (connection.state().terminal()) {
                throw new StatefulException("Closed connection cannot be registered");
            }
            final Registration registration = new Registration(sequence.incrementAndGet(), destination);
            if (registrations.putIfAbsent(connection, registration) != null) {
                return;
            }
            active.computeIfAbsent(destination, ignored -> new CopyOnWriteArrayList<>()).add(connection);
            meter.incrementCounter(Counter.PHYSICAL_CONNECTIONS_CREATED);
            meter.incrementCounter(Counter.ACTIVE_PHYSICAL_CONNECTIONS);
        }
    }

    /**
     * Removes and closes a matching connection.
     *
     * @param destination connection destination
     * @param connection  connection
     */
    public void close(final Destination destination, final Connection connection) {
        require(destination, "Connection destination");
        require(connection, "Connection");
        if (!remove(destination, connection)) {
            return;
        }
        try {
            connection.close();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close connection", e);
        }
    }

    /**
     * Removes a physical connection without performing network I/O.
     *
     * @param destination connection destination
     * @param connection  connection
     * @return true only for the final removal
     */
    public boolean remove(final Destination destination, final Connection connection) {
        require(destination, "Connection destination");
        require(connection, "Connection");
        final Registration registration = registrations.get(connection);
        if (registration == null || !registration.destination.equals(destination)
                || !registrations.remove(connection, registration)) {
            return false;
        }
        final CopyOnWriteArrayList<Connection> bucket = active.get(destination);
        if (bucket != null) {
            bucket.remove(connection);
            if (bucket.isEmpty()) {
                active.remove(destination, bucket);
            }
        }
        meter.addCounter(Counter.ACTIVE_PHYSICAL_CONNECTIONS, -1L);
        return true;
    }

    /**
     * Returns the stable primitive identifier of a registered connection.
     *
     * @param connection connection
     * @return positive identifier, or zero when not registered
     */
    public long id(final Connection connection) {
        final Registration registration = registrations.get(require(connection, "Connection"));
        return registration == null ? 0L : registration.id;
    }

    /**
     * Returns active connections for a destination.
     *
     * @param destination connection destination
     * @return active connection snapshot
     */
    public List<Connection> active(final Destination destination) {
        require(destination, "Connection destination");
        final List<Connection> bucket = active.get(destination);
        return bucket == null ? List.of() : List.copyOf(bucket);
    }

    /**
     * Returns all active connections.
     *
     * @return active snapshot
     */
    public Map<Destination, List<Connection>> snapshot() {
        final LinkedHashMap<Destination, List<Connection>> snapshot = new LinkedHashMap<>();
        for (final Map.Entry<Destination, CopyOnWriteArrayList<Connection>> entry : active.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Cancels active connections by tag.
     *
     * @param tag tag
     * @return true when at least one connection matched
     */
    public boolean cancel(final Object tag) {
        require(tag, "Tag");
        boolean matched = false;
        for (final Map.Entry<Destination, CopyOnWriteArrayList<Connection>> entry : active.entrySet()) {
            for (final Connection connection : entry.getValue()) {
                if (tag.equals(entry.getKey()) || tag.equals(connection)) {
                    close(entry.getKey(), connection);
                    matched = true;
                }
            }
        }
        return matched;
    }

    /**
     * Atomically prevents new registrations and closes every registered physical connection outside map operations.
     */
    @Override
    public void close() {
        synchronized (closed) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
        }
        InternalException failure = null;
        for (final Map.Entry<Connection, Registration> entry : registrations.entrySet()) {
            try {
                close(entry.getValue().destination, entry.getKey());
            } catch (final InternalException current) {
                if (failure == null) {
                    failure = current;
                } else {
                    failure.addSuppressed(current);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Validates required values.
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
     * Stable physical connection registration.
     */
    private record Registration(long id, Destination destination) {
    }

}
