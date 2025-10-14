/**
 * Provides registries for storing and retrieving runtime configuration and operational components.
 * <p>
 * This package contains classes that act as in-memory databases or registries for various gateway assets. These
 * registries are typically populated at startup and provide fast, on-demand access to configuration data needed during
 * request processing.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.registry.AssetsRegistry}: Manages all API metadata
 * ({@link org.miaixz.bus.vortex.Assets}).</li>
 * <li>{@link org.miaixz.bus.vortex.registry.LimiterRegistry}: Manages all rate limiter
 * ({@link org.miaixz.bus.vortex.magic.Limiter}) instances.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.registry;
