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

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;

/**
 * Single ownership boundary for idle connection buckets, identity entries, and their O(1) count.
 * <p>
 * Mutation remains under the existing pool lock; this component intentionally owns no scheduler or lock.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class IdleConnectionIndex {

    /**
     * Idle connections grouped by destination.
     */
    final ConcurrentHashMap<Destination, ArrayDeque<PooledConnection>> buckets = new ConcurrentHashMap<>();

    /**
     * Reusable idle entry by physical connection identity.
     */
    final Map<Connection, PooledConnection> entries = new IdentityHashMap<>();

    /**
     * Total number of entries across all buckets.
     */
    volatile int count;

}
