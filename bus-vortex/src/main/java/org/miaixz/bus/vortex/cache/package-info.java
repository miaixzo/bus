/**
 * Provides high-performance caching utilities for the Vortex reactive gateway.
 * <p>
 * This package contains a two-level cache implementation designed for optimal performance in high-throughput scenarios.
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.cache.CacheManager}: A generic two-level cache manager combining L1
 * (ConcurrentHashMap) and L2 (Caffeine) caches with automatic LRU eviction and performance monitoring support.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.cache;
