/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
