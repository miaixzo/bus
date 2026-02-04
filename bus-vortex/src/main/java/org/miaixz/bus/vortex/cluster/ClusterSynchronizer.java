/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.cluster;

import java.util.Collection;

/**
 * Cluster synchronizer interface for gateway cluster data synchronization.
 * <p>
 * Framework-layer abstract interface defining the contract for cluster data synchronization. Concrete implementations
 * are provided by the application layer (database-based, Redis-based, etc.).
 * </p>
 *
 * <p>
 * <b>Design Principles:</b>
 * </p>
 * <ul>
 * <li>Defines synchronization data source interface</li>
 * <li>Defines synchronization trigger mechanism</li>
 * <li>Decoupled from Registry</li>
 * <li>Supports multiple synchronization strategies</li>
 * </ul>
 *
 * <p>
 * <b>Implementation Strategies:</b>
 * </p>
 * <ul>
 * <li>Database Polling: Incremental data fetching based on {@code modified} field</li>
 * <li>Redis Pub/Sub: Real-time notification mechanism</li>
 * <li>Message Queue: Asynchronous decoupling</li>
 * </ul>
 *
 * <p>
 * <b>Use Cases:</b>
 * </p>
 * <ul>
 * <li>Gateway cluster data synchronization</li>
 * <li>Real-time configuration updates</li>
 * <li>Multi-instance load balancing</li>
 * </ul>
 *
 * <p>
 * <b>Implementation Naming Examples:</b>
 * </p>
 * 
 * <pre>{@code
 * // Implementation class naming
 * public class DatabaseClusterSynchronizer implements ClusterSynchronizer<Assets> {
 *     // Database polling-based implementation
 * }
 *
 * public class RedisClusterSynchronizer implements ClusterSynchronizer<Assets> {
 *     // Redis Pub/Sub-based implementation
 * }
 * }</pre>
 *
 * @param <T> the type of data to synchronize
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ClusterSynchronizer<T> {

    /**
     * Retrieves all data (full sync).
     *
     * @return collection of all data items
     */
    Collection<T> getAll();

    /**
     * Retrieves changed data since last synchronization (incremental sync).
     *
     * @param lastSyncTime timestamp of last synchronization (milliseconds since epoch)
     * @return collection of changed data items
     */
    Collection<T> getChanged(long lastSyncTime);

    /**
     * Triggers synchronization.
     * <p>
     * Called when local data changes to notify other gateway instances to synchronize.
     * </p>
     */
    void trigger();

    /**
     * Retrieves current synchronization status.
     *
     * @return synchronization status
     */
    Object getStatus();

}
