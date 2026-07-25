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
     * Atomic state guarded during registration to prevent races with global shutdown.
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
     * @param meter non-null borrowed meter updated for creation and active-count changes
     * @throws ValidateException if {@code meter} is {@code null}
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
     * @param destination destination bucket under which the physical connection is registered
     * @param connection  non-terminal physical connection to register
     * @throws StatefulException if the registry is closed or the connection is terminal
     * @throws ValidateException if either argument is {@code null}
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
     * @param destination destination expected to own the registration
     * @param connection  registered physical connection to remove and close
     * @throws InternalException if the removed connection cannot be closed
     * @throws ValidateException if either argument is {@code null}
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
     * @param destination destination expected to own the registration
     * @param connection  physical connection expected in that destination bucket
     * @return {@code true} only when this call atomically removes the matching registration
     * @throws ValidateException if either argument is {@code null}
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
     * @param connection physical connection to look up
     * @return positive identifier, or zero when not registered
     * @throws ValidateException if {@code connection} is {@code null}
     */
    public long id(final Connection connection) {
        final Registration registration = registrations.get(require(connection, "Connection"));
        return registration == null ? 0L : registration.id;
    }

    /**
     * Returns active connections for a destination.
     *
     * @param destination destination bucket to snapshot
     * @return immutable snapshot of that destination's currently observed connections
     * @throws ValidateException if {@code destination} is {@code null}
     */
    public List<Connection> active(final Destination destination) {
        require(destination, "Connection destination");
        final List<Connection> bucket = active.get(destination);
        return bucket == null ? List.of() : List.copyOf(bucket);
    }

    /**
     * Returns all active connections.
     *
     * @return immutable map and list copies of connections observed during concurrent traversal
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
     * @param tag destination or connection identity to match by equality
     * @return {@code true} when at least one matching registration was removed and closed
     * @throws InternalException if a matching connection cannot be closed
     * @throws ValidateException if {@code tag} is {@code null}
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
     *
     * @throws InternalException if one or more physical connections cannot be closed; later failures are suppressed
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
     * @param value reference to validate
     * @param name  logical reference name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Stable physical connection registration.
     *
     * @param id          positive registration identifier
     * @param destination destination bucket selected when the connection was opened
     */
    private record Registration(long id, Destination destination) {
    }

}
