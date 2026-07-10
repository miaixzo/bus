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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;

/**
 * Thread-safe registry for active network connections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ConnectionRegistry {

    /**
     * Active connections grouped by destination.
     */
    private final ConcurrentHashMap<Destination, CopyOnWriteArrayList<Connection>> active;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates an empty connection registry.
     */
    public ConnectionRegistry() {
        this.active = new ConcurrentHashMap<>();
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
        if (closed.get()) {
            throw new StatefulException("Connection registry is closed");
        }
        active.computeIfAbsent(destination, ignored -> new CopyOnWriteArrayList<>()).addIfAbsent(connection);
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
        final CopyOnWriteArrayList<Connection> bucket = active.get(destination);
        if (bucket == null || !bucket.remove(connection)) {
            return;
        }
        if (bucket.isEmpty()) {
            active.remove(destination, bucket);
        }
        try {
            connection.close();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close connection", e);
        }
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
            for (final Connection connection : List.copyOf(entry.getValue())) {
                if (tag.equals(entry.getKey()) || tag.equals(connection)) {
                    close(entry.getKey(), connection);
                    matched = true;
                }
            }
        }
        return matched;
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
