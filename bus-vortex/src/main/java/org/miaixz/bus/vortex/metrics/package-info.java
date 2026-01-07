/**
 * Provides monitoring and metrics collection utilities for the Vortex reactive gateway.
 * <p>
 * This package contains classes for tracking and analyzing gateway performance metrics in real-time.
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.metrics.DefaultMonitor}: A default in-memory implementation of the
 * {@link org.miaixz.bus.vortex.Monitor} interface for tracking request metrics, cache statistics, and database
 * operations.</li>
 * <li>{@link org.miaixz.bus.vortex.metrics.CacheStats}: Data transfer object for capturing cache performance metrics
 * including hit/miss counts, hit rate, and cache size.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.metrics;
