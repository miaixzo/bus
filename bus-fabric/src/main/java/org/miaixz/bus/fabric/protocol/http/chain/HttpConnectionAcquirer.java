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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.util.function.Supplier;

import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.fabric.registry.connection.ConnectionPool;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Connection-pool acquisition boundary for HTTP routes.
 *
 * @author Kimi Liu
 */
final class HttpConnectionAcquirer {

    /**
     * Shared connection pool.
     */
    private final ConnectionPool pool;

    /**
     * Creates an acquirer.
     *
     * @param pool connection pool
     */
    HttpConnectionAcquirer(final ConnectionPool pool) {
        this.pool = pool;
    }

    /**
     * Acquires, waits for, or creates a connection and closes a lease if cancellation wins publication.
     *
     * @param destination         pool destination
     * @param factory             physical connection factory
     * @param cancellation        cancellation scope
     * @param transientConnection whether reuse is forbidden
     * @return acquired lease
     */
    ConnectionLease acquire(
            final Destination destination,
            final Supplier<Connection> factory,
            final Cancellation cancellation,
            final boolean transientConnection) {
        cancellation.throwIfCancelled();
        final ConnectionLease lease = transientConnection ? pool.acquireTransient(destination, factory, cancellation)
                : pool.acquire(destination, factory, cancellation);
        try {
            cancellation.throwIfCancelled();
            return lease;
        } catch (final RuntimeException failure) {
            try {
                lease.close();
            } catch (final RuntimeException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

}
