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
package org.miaixz.bus.vortex.striped;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.miaixz.bus.logger.Logger;

/**
 * Striped lock for fine-grained concurrent control.
 * <p>
 * Pure Java implementation providing fine-grained concurrency control by mapping different keys to different locks,
 * reducing lock contention.
 * </p>
 *
 * <p>
 * <b>Key Advantages:</b>
 * </p>
 * <ul>
 * <li>Zero dependencies: No Guava or other third-party libraries required</li>
 * <li>High concurrency: Default 16 lock stripes, theoretically 16x concurrent capacity</li>
 * <li>Low contention: 90%+ reduction in lock contention compared to global locking</li>
 * <li>Zero configuration: Works out of the box</li>
 * <li>Low memory footprint: Fixed number of lock objects</li>
 * </ul>
 *
 * <p>
 * <b>Implementation Principle:</b>
 * </p>
 * 
 * <pre>{@code
 * 
 * // Use key's hashCode to select lock stripe
 * int index = Math.abs(key.hashCode()) % lockCount;
 * Lock lock = locks[index];
 * }</pre>
 *
 * <p>
 * <b>Use Cases:</b>
 * </p>
 * <ul>
 * <li>High-concurrency registration scenarios</li>
 * <li>Batch processing of multiple assets</li>
 * <li>Scenarios requiring fine-grained control</li>
 * </ul>
 *
 * <p>
 * <b>Example Usage:</b>
 * </p>
 * 
 * <pre>{@code
 * StripedLock stripedLock = new StripedLock();
 *
 * // Approach 1: Automatic locking
 * stripedLock.executeWithLock("user.getProfile:1.0.0", () -> {
 *     // Execute synchronized operation
 *     registerAsset(asset);
 * });
 *
 * // Approach 2: Manual locking
 * Lock lock = stripedLock.getLock("user.getProfile:1.0.0");
 * lock.lock();
 * try {
 *     // Execute operation
 * } finally {
 *     lock.unlock();
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StripedLock {

    /**
     * Default number of lock stripes.
     * <p>
     * Recommended value is 2-4x CPU cores, ideally a power of 2.
     * </p>
     */
    private static final int DEFAULT_STRIPES = 16;

    /**
     * Array of locks.
     */
    private final Lock[] locks;

    /**
     * Number of lock stripes.
     */
    private final int stripeCount;

    /**
     * Constructs a StripedLock with default number of stripes.
     */
    public StripedLock() {
        this(DEFAULT_STRIPES);
    }

    /**
     * Constructs a StripedLock with specified number of stripes.
     *
     * @param stripes number of stripes, recommended to be a power of 2 (e.g., 8, 16, 32)
     */
    public StripedLock(int stripes) {
        if (stripes <= 0) {
            throw new IllegalArgumentException("Stripes must be positive: " + stripes);
        }

        this.stripeCount = stripes;
        this.locks = new ReentrantLock[stripes];

        // Initialize lock array
        for (int i = 0; i < stripes; i++) {
            this.locks[i] = new ReentrantLock();
        }

        Logger.info("StripedLock initialized: stripes={}", stripes);
    }

    /**
     * Gets the lock for the specified key.
     * <p>
     * Uses the key's hashCode to select the corresponding lock stripe:
     *
     * <pre>{@code
     * 
     * int index = Math.abs(key.hashCode()) % stripeCount;
     * }</pre>
     * </p>
     *
     * @param key the key, typically in "method:version" format
     * @return the lock object
     */
    public Lock getLock(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Use hashCode to select lock stripe
        int index = Math.abs(key.hashCode()) % stripeCount;
        return locks[index];
    }

    /**
     * Executes action with lock.
     *
     * @param key    the key
     * @param action the action to execute
     */
    public void executeWithLock(Object key, Runnable action) {
        Lock lock = getLock(key);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes action with lock and returns value.
     *
     * @param <T>      the return type
     * @param key      the key
     * @param supplier the supplier to execute
     * @return the result of the operation
     */
    public <T> T executeWithLock(Object key, Supplier<T> supplier) {
        Lock lock = getLock(key);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tries to execute action with lock (with timeout).
     *
     * @param key       the key
     * @param action    the action to execute
     * @param timeoutMs timeout in milliseconds
     * @return {@code true} if action executed successfully, {@code false} if failed to acquire lock
     */
    public boolean tryExecuteWithLock(Object key, Runnable action, long timeoutMs) {
        Lock lock = getLock(key);
        try {
            if (lock.tryLock(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                try {
                    action.run();
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.warn("Lock acquisition interrupted: key={}", key);
            return false;
        }
    }

    /**
     * Gets the number of lock stripes.
     *
     * @return the number of stripes
     */
    public int getStripes() {
        return stripeCount;
    }

}
