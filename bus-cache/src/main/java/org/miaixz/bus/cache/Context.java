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

import org.miaixz.bus.core.lang.EnumValue;

import java.util.Map;

/**
 * Represents the global configuration for the cache system.
 * <p>
 * This class holds settings such as the cache implementations, metrics component, and global feature switches like
 * cache enabling and penetration prevention.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context {

    /**
     * A map of named cache instances, where the key is the cache name and the value is the {@link CacheX}
     * implementation.
     */
    private Map<String, CacheX> caches;

    /**
     * The component responsible for tracking cache metrics, such as hit and miss rates.
     */
    private Metrics metrics;

    /**
     * The global switch to enable or disable all caching operations.
     */
    private EnumValue.Switch cache;

    /**
     * The global switch to enable or disable cache penetration prevention.
     */
    private EnumValue.Switch prevent;

    /**
     * Creates a new {@link Context} instance with default settings.
     * <p>
     * By default, caching is enabled ({@code Switch.ON}), and penetration prevention is disabled ({@code Switch.OFF}).
     * The metrics component is initially null.
     * </p>
     *
     * @param caches A map of cache names to {@link CacheX} instances.
     * @return A new, configured {@link Context} instance.
     */
    public static Context newConfig(Map<String, CacheX> caches) {
        Context config = new Context();
        config.caches = caches;
        config.cache = EnumValue.Switch.ON;
        config.prevent = EnumValue.Switch.OFF;
        config.metrics = null;
        return config;
    }

    /**
     * Checks if the cache penetration prevention feature is globally enabled.
     *
     * @return {@code true} if penetration prevention is on, otherwise {@code false}.
     */
    public boolean isPreventOn() {
        return null != prevent && prevent == EnumValue.Switch.ON;
    }

    /**
     * Gets the map of cache implementations.
     *
     * @return A map where keys are cache names and values are {@link CacheX} instances.
     */
    public Map<String, CacheX> getCaches() {
        return caches;
    }

    /**
     * Sets the map of cache implementations.
     *
     * @param caches A map where keys are cache names and values are {@link CacheX} instances.
     */
    public void setCaches(Map<String, CacheX> caches) {
        this.caches = caches;
    }

    /**
     * Gets the cache metrics component.
     *
     * @return The {@link Metrics} component, or {@code null} if not configured.
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Sets the cache metrics component.
     *
     * @param metrics The {@link Metrics} component to be used for tracking statistics.
     */
    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Gets the global cache switch status.
     *
     * @return The current status of the global cache switch (ON/OFF).
     */
    public EnumValue.Switch getCache() {
        return cache;
    }

    /**
     * Sets the global cache switch status.
     *
     * @param cache The desired status for the global cache switch (ON/OFF).
     */
    public void setCache(EnumValue.Switch cache) {
        this.cache = cache;
    }

    /**
     * Gets the cache penetration prevention switch status.
     *
     * @return The current status of the penetration prevention switch (ON/OFF).
     */
    public EnumValue.Switch getPrevent() {
        return prevent;
    }

    /**
     * Sets the cache penetration prevention switch status.
     *
     * @param prevent The desired status for the penetration prevention switch (ON/OFF).
     */
    public void setPrevent(EnumValue.Switch prevent) {
        this.prevent = prevent;
    }

}
