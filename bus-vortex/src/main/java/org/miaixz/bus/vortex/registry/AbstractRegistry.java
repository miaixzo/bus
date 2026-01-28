/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.registry;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Monitor;
import org.miaixz.bus.vortex.Registry;
import org.miaixz.bus.vortex.cache.CacheManager;
import org.springframework.beans.factory.InitializingBean;

import reactor.core.publisher.Mono;

/**
 * An abstract, generic, thread-safe base class for creating in-memory registries.
 * <p>
 * This class provides the core functionality for a key-value store, using a {@link ConcurrentHashMap} for thread-safe
 * operations. It is designed to be extended by concrete registry implementations (e.g., {@link AssetsRegistry}), which
 * must provide a key generation strategy.
 * <p>
 * <b>Performance Optimization (Two-Level Cache Architecture):</b>
 * </p>
 * <ul>
 * <li>Uses {@link CacheManager} to manage two-level caching</li>
 * <li>L1 Cache: ConcurrentHashMap (hot data, ultra-fast access)</li>
 * <li>L2 Cache: Caffeine (full data set, LRU eviction)</li>
 * <li>Query order: L1 → L2 → null</li>
 * <li>Update strategy: Write-through to both cache levels</li>
 * </ul>
 * <p>
 * <b>Responsibility Separation:</b>
 * </p>
 * <ul>
 * <li>AbstractRegistry: Registry business logic</li>
 * <li>CacheManager: Generic cache management (reusable by other components)</li>
 * </ul>
 *
 * @param <T> The type of objects to be stored in the registry.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    /**
     * The underlying thread-safe map that stores the registered items.
     * <p>
     * L1 Cache: ConcurrentHashMap, provides the fastest access.
     * </p>
     */
    private final Map<String, T> registry = new ConcurrentHashMap<>();

    /**
     * Cache Manager: Encapsulates L2 cache logic.
     */
    protected final CacheManager<String, T> cacheManager;

    /**
     * The function used to generate a unique key for each item stored in the registry.
     */
    protected Function<T, String> keyGenerator;

    /**
     * Constructor: Initializes the cache manager.
     */
    public AbstractRegistry() {
        this.cacheManager = new CacheManager<>();
    }

    /**
     * Sets the key generation strategy for this registry. This method must be called by subclasses in their constructor
     * to define how items are indexed.
     *
     * @param keyGenerator A {@link Function} that takes an item of type {@code T} and returns its unique string key.
     */
    protected void setKeyGenerator(Function<T, String> keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    /**
     * Sets the performance monitor (optional).
     * <p>
     * Delegates to CacheManager.
     * </p>
     *
     * @param monitor The performance monitor.
     */
    public void setMonitor(Monitor monitor) {
        this.cacheManager.setPerformanceMonitor(monitor);
        Logger.debug("Performance monitor set: {}", monitor.getClass().getSimpleName());
    }

    /**
     * Registers an item in the registry using the configured key generator.
     *
     * @param item The item to register.
     * @throws IllegalStateException If the key generator has not been set.
     */
    @Override
    public void register(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set. Call setKeyGenerator in the constructor.");
        }
        register(keyGenerator.apply(item), item);
    }

    /**
     * Registers an item in the registry with a specific key.
     * <p>
     * Updates both the registry (L1 cache) and the cache manager (L2 cache).
     * </p>
     *
     * @param key  The unique key for the item.
     * @param item The item to register.
     */
    @Override
    public void register(String key, T item) {
        // Update both registry and cacheManager
        this.registry.put(key, item);
        this.cacheManager.put(key, item);
    }

    /**
     * Removes an item from the registry by its key.
     * <p>
     * Removes from both the registry (L1 cache) and the cache manager (L2 cache).
     * </p>
     *
     * @param key The key of the item to remove.
     */
    @Override
    public void destroy(String key) {
        // Remove from both registry and cacheManager
        this.registry.remove(key);
        this.cacheManager.remove(key);
    }

    /**
     * Removes an item from the registry using the configured key generator.
     *
     * @param item The item to remove.
     * @throws IllegalStateException If the key generator has not been set.
     */
    @Override
    public void destroy(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        destroy(keyGenerator.apply(item));
    }

    /**
     * Updates an item in the registry using the configured key generator.
     *
     * @param item The item to update.
     * @throws IllegalStateException If the key generator has not been set.
     */
    @Override
    public void update(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        update(keyGenerator.apply(item), item);
    }

    /**
     * Updates an item in the registry with a specific key.
     * <p>
     * Update is implemented as re-registration.
     * </p>
     *
     * @param key  The unique key for the item.
     * @param item The item to update.
     */
    @Override
    public void update(String key, T item) {
        // Update is re-registration
        register(key, item);
    }

    /**
     * Asynchronously clears the registry and then calls the asynchronous {@link #init()} method.
     * <p>
     * Clears both the registry (L1 cache) and the cache manager (L2 cache).
     * </p>
     *
     * @return A {@code Mono<Void>} that completes when the registry is cleared and re-initialized.
     */
    @Override
    public Mono<Void> refresh() {
        return Mono.fromRunnable(() -> {
            // Clear both registry and cacheManager
            this.registry.clear();
            this.cacheManager.clear();
        }).then(init()); // Chain to the async init() method
    }

    /**
     * Retrieves an item from the registry by its key.
     * <p>
     * Query order:
     * </p>
     * <ol>
     * <li>Check L1 cache (registry)</li>
     * <li>If not found, check L2 cache (cacheManager)</li>
     * <li>If found in L2, promote to L1</li>
     * <li>If not found in either, return null</li>
     * </ol>
     *
     * @param key The key of the item to retrieve.
     * @return The item associated with the key, or {@code null} if not found.
     */
    @Override
    public T get(String key) {
        // 1. First query registry (L1 cache)
        T item = this.registry.get(key);
        if (item != null) {
            return item;
        }

        // 2. Registry miss, query cacheManager (L2 cache)
        item = this.cacheManager.get(key);
        if (item != null) {
            // L2 cache hit, promote to registry
            this.registry.put(key, item);
            return item;
        }

        // 3. Complete miss
        return null;
    }

    /**
     * Retrieves all items currently stored in the registry.
     *
     * @return A collection of all registered items.
     */
    @Override
    public Collection<T> getAll() {
        return this.registry.values();
    }

    /**
     * Integrates with the Spring lifecycle. After all bean properties are set, this method is called, which in turn
     * triggers the initial {@link #refresh()} of the registry.
     * <p>
     * This method will **block** the startup thread until the asynchronous {@code refresh()} completes. This is
     * acceptable behavior during application startup to ensure the registry is populated before use.
     */
    @Override
    public void afterPropertiesSet() {
        // Bridge the synchronous Spring lifecycle with our async refresh.
        // This is acceptable on startup but should be avoided at runtime.
        refresh().block();
    }

    /**
     * Retrieves cache statistics.
     * <p>
     * Delegates to CacheManager.
     * </p>
     *
     * @return Statistics information.
     */
    public Object getStats() {
        return this.cacheManager.getStats();
    }

}
