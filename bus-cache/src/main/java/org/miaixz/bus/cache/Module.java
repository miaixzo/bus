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
package org.miaixz.bus.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.miaixz.bus.cache.reader.AbstractReader;
import org.miaixz.bus.cache.reader.MultiCacheReader;
import org.miaixz.bus.cache.reader.SingleCacheReader;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.logger.Logger;

/**
 * A singleton factory for initializing and providing access to cache components.
 * <p>
 * This class orchestrates the setup of the caching infrastructure, including the {@link Complex} facade, {@link Manage}
 * for cache instances, and various readers. It follows the initialization-on-demand holder idiom to ensure thread-safe,
 * lazy instantiation of the singleton.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Module {

    /**
     * The primary facade for all cache operations.
     */
    private Complex complex;

    /**
     * The configuration context for the cache module.
     */
    private Context context;

    /**
     * An unmodifiable map of named cache instances.
     */
    private Map<String, CacheX> caches;

    /**
     * The component for tracking cache performance metrics.
     */
    private Metrics metrics;

    /**
     * The reader responsible for handling single-key cache lookups.
     */
    private AbstractReader singleCacheReader;

    /**
     * The reader responsible for handling multi-key batch cache lookups.
     */
    private AbstractReader multiCacheReader;

    /**
     * The manager for all registered cache instances.
     */
    private Manage manage;

    /**
     * A flag indicating whether the module has been initialized.
     */
    private boolean initialized;

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private Module() {
        this.initialized = false;
    }

    /**
     * Retrieves the singleton instance of the {@code Module}.
     *
     * @return The singleton {@code Module} instance.
     */
    private static Module getInstance() {
        return ModuleHolder.INSTANCE;
    }

    /**
     * Initializes the cache module with the given configuration and returns the operational facade.
     * <p>
     * This method is synchronized to ensure that the initialization process is performed only once. On subsequent
     * calls, it returns the already-initialized {@link Complex} instance without re-initializing.
     * </p>
     *
     * @param config The cache configuration object ({@link Context}).
     * @return The fully configured {@link Complex} instance, ready for use.
     * @throws IllegalArgumentException if the configuration is null or contains no cache instances.
     */
    public static synchronized Complex instance(Context config) {
        Module module = getInstance();
        if (!module.initialized) {
            module.initialize(config);
            module.initialized = true;
            Logger.info("Cache factory initialized successfully");
        }
        return module.complex;
    }

    /**
     * Performs the one-time initialization of all cache components.
     * <p>
     * This method sets up the cache map, metrics, readers, manager, and the main {@code Complex} object. It is called
     * internally by the {@link #instance(Context)} method.
     * </p>
     *
     * @param config The cache configuration object.
     * @throws IllegalArgumentException if the configuration is null or the cache map is empty.
     */
    private void initialize(Context config) {
        Assert.isTrue(null != config, "context param can not be null.");
        Assert.isTrue(CollKit.isNotEmpty(config.getCaches()), "caches param can not be empty.");

        this.context = config;
        // Initialize cache map
        caches = Collections.unmodifiableMap(new HashMap<>(config.getCaches()));
        Logger.debug("Initialized caches with size: {}", caches.size());

        // Initialize Metrics
        metrics = config.getMetrics();
        Logger.debug("Initialized metrics: {}", metrics != null ? metrics.getClass().getSimpleName() : "null");

        // Initialize Reader instances
        singleCacheReader = new SingleCacheReader();
        multiCacheReader = new MultiCacheReader();
        Logger.debug("Initialized singleCacheReader and multiCacheReader");

        // Initialize Manage instance
        manage = new Manage(caches, metrics);
        Logger.debug("Initialized manage");

        // Create Complex instance
        complex = new Complex();
        // Set dependencies for the Complex instance
        complex.setContext(context);
        complex.setManage(manage);
        complex.setSingleCacheReader(singleCacheReader);
        complex.setMultiCacheReader(multiCacheReader);
        Logger.debug("Complex instance created and dependencies set");
    }

    /**
     * Retrieves a named cache instance.
     *
     * @param name The name of the cache to retrieve.
     * @return The {@link CacheX} instance associated with the given name.
     * @throws IllegalStateException if the cache module has not been initialized.
     */
    public CacheX getCache(String name) {
        if (caches == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        CacheX cache = caches.get(name);
        Logger.debug(
                "Retrieved cache: {} for name: {}",
                cache != null ? cache.getClass().getSimpleName() : "null",
                name);
        return cache;
    }

    /**
     * Retrieves the cache metrics component.
     *
     * @return An {@link Optional} containing the {@link Metrics} instance, or an empty Optional if it is not
     *         configured.
     */
    public Optional<Metrics> getHitting() {
        Logger.debug("Retrieved metrics: {}", metrics != null ? metrics.getClass().getSimpleName() : "null");
        return Optional.ofNullable(metrics);
    }

    /**
     * Retrieves the reader for single-key cache operations.
     *
     * @return The configured {@link AbstractReader} for single lookups.
     * @throws IllegalStateException if the cache module has not been initialized.
     */
    public AbstractReader getSingleCacheReader() {
        if (singleCacheReader == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        Logger.debug("Retrieved singleCacheReader");
        return singleCacheReader;
    }

    /**
     * Retrieves the reader for multi-key cache operations.
     *
     * @return The configured {@link AbstractReader} for batch lookups.
     * @throws IllegalStateException if the cache module has not been initialized.
     */
    public AbstractReader getMultiCacheReader() {
        if (multiCacheReader == null) {
            Logger.error("CacheFactory not initialized");
            throw new IllegalStateException("CacheFactory not initialized");
        }
        Logger.debug("Retrieved multiCacheReader");
        return multiCacheReader;
    }

    /**
     * A static inner class that holds the singleton instance of {@code Module}.
     * <p>
     * This pattern ensures lazy and thread-safe initialization without using explicit synchronization.
     * </p>
     */
    private static class ModuleHolder {

        /**
         * The singleton instance of Module.
         */
        private static final Module INSTANCE = new Module();
    }

}
