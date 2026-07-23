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

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.network.Connection;

/**
 * Immutable pool metadata for one retained physical connection.
 *
 * <p>
 * The entry never owns or copies the physical connection lifecycle; it only records the idle timestamp and a
 * protocol-capacity snapshot used by a destination bucket.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class PooledConnection {

    /**
     * Retained physical connection reference.
     */
    private final Connection connection;

    /**
     * Pool-clock instant recorded when the connection became idle.
     */
    private long lastUsedMillis;

    /**
     * Established protocol snapshot captured when this entry was created.
     */
    private final Protocol protocol;

    /**
     * Non-negative logical lease-capacity snapshot captured when this entry was created.
     */
    private final int capacity;

    /**
     * Creates an entry using the connection's current protocol and non-negative capacity snapshots.
     *
     * @param connection non-null physical connection to retain
     * @param lastUsed   non-null pool-clock instant when the connection became idle
     */
    PooledConnection(final Connection connection, final Instant lastUsed) {
        this(connection, lastUsed, connection == null ? null : connection.protocol(),
                connection == null ? 0 : Math.max(0, connection.capacity()));
    }

    /**
     * Creates a fully specified immutable entry.
     *
     * @param connection non-null physical connection to retain
     * @param lastUsed   non-null pool-clock instant when the connection became idle
     * @param protocol   non-null established protocol snapshot
     * @param capacity   non-negative logical lease-capacity snapshot
     */
    PooledConnection(final Connection connection, final Instant lastUsed, final Protocol protocol, final int capacity) {
        if (connection == null || lastUsed == null || protocol == null || capacity < 0) {
            throw new ValidateException("Invalid pooled connection metadata");
        }
        this.connection = connection;
        this.lastUsedMillis = lastUsed.toEpochMilli();
        this.protocol = protocol;
        this.capacity = capacity;
    }

    /**
     * Creates a reusable entry from an allocation-free epoch-millisecond timestamp.
     *
     * @param connection     retained physical connection
     * @param lastUsedMillis last-use time in epoch milliseconds
     */
    PooledConnection(final Connection connection, final long lastUsedMillis) {
        if (connection == null || lastUsedMillis < 0) {
            throw new ValidateException("Invalid pooled connection metadata");
        }
        this.connection = connection;
        this.lastUsedMillis = lastUsedMillis;
        this.protocol = connection.protocol();
        this.capacity = Math.max(0, connection.capacity());
    }

    /**
     * Updates the idle timestamp before the same physical entry is queued again.
     *
     * @param value last-use time in epoch milliseconds
     */
    void lastUsedMillis(final long value) {
        if (value < 0) {
            throw new ValidateException("Pooled connection timestamp must not be negative");
        }
        lastUsedMillis = value;
    }

    /**
     * Returns the retained physical connection.
     *
     * @return original connection reference
     */
    Connection connection() {
        return connection;
    }

    /**
     * Returns the recorded idle timestamp.
     *
     * @return pool-clock instant when the connection became idle
     */
    Instant lastUsed() {
        return Instant.ofEpochMilli(lastUsedMillis);
    }

    /**
     * Returns the established protocol snapshot.
     *
     * @return protocol captured at entry creation
     */
    Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the logical lease-capacity snapshot.
     *
     * @return non-negative capacity captured at entry creation
     */
    int capacity() {
        return capacity;
    }

}
