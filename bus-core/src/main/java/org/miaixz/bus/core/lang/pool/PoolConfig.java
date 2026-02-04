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
package org.miaixz.bus.core.lang.pool;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration class for object pools, providing essential parameters for pool management. This includes:
 * <ul>
 * <li>Minimum pool size (initial size)</li>
 * <li>Maximum pool size</li>
 * <li>Maximum wait time for borrowing an object</li>
 * <li>Maximum idle time for objects in the pool</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PoolConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852272106223L;
    /**
     * The minimum (initial) size of the object pool. This defines the number of objects the pool will start with or try
     * to maintain as a minimum.
     */
    private int minSize = 5;
    /**
     * The maximum size of the object pool. The pool will not create more objects than this limit.
     */
    private int maxSize = 20;
    /**
     * The maximum time in milliseconds to wait for an object to become available when borrowing from the pool. The
     * default wait time is 5 seconds.
     */
    private long maxWait = 5000;
    /**
     * The maximum idle time in milliseconds for an object in the pool. If an object remains idle for longer than this
     * duration, it may be evicted from the pool.
     */
    private long maxIdle;

    /**
     * Creates a new {@code PoolConfig} instance with default settings.
     *
     * @return a new {@code PoolConfig} instance
     */
    public static PoolConfig of() {
        return new PoolConfig();
    }

    /**
     * Retrieves the minimum (initial) size of the object pool.
     *
     * @return the minimum (initial) pool size
     */
    public int getMinSize() {
        return minSize;
    }

    /**
     * Sets the minimum (initial) size of the object pool.
     *
     * @param minSize the new minimum (initial) pool size
     * @return this {@code PoolConfig} instance for method chaining
     */
    public PoolConfig setMinSize(final int minSize) {
        this.minSize = minSize;
        return this;
    }

    /**
     * Retrieves the maximum size of the object pool.
     *
     * @return the maximum pool size
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum size of the object pool.
     *
     * @param maxSize the new maximum pool size
     * @return this {@code PoolConfig} instance for method chaining
     */
    public PoolConfig setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Retrieves the maximum wait time in milliseconds for borrowing an object from the pool.
     *
     * @return the maximum wait time in milliseconds
     */
    public long getMaxWait() {
        return maxWait;
    }

    /**
     * Sets the maximum wait time in milliseconds for borrowing an object from the pool.
     *
     * @param maxWait the new maximum wait time in milliseconds
     * @return this {@code PoolConfig} instance for method chaining
     */
    public PoolConfig setMaxWait(final long maxWait) {
        this.maxWait = maxWait;
        return this;
    }

    /**
     * Retrieves the maximum idle time in milliseconds for an object in the pool.
     *
     * @return the maximum idle time in milliseconds. A value less than or equal to 0 indicates no idle time limit.
     */
    public long getMaxIdle() {
        return maxIdle;
    }

    /**
     * Sets the maximum idle time in milliseconds for an object in the pool.
     *
     * @param maxIdle the new maximum idle time in milliseconds. A value less than or equal to 0 indicates no idle time
     *                limit.
     * @return this {@code PoolConfig} instance for method chaining
     */
    public PoolConfig setMaxIdle(final long maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

}
