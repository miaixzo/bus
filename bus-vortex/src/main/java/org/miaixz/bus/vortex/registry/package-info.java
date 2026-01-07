/**
 * Provides registries for storing and retrieving runtime configuration and operational components.
 * <p>
 * This package contains classes that act as in-memory databases or registries for various gateway assets. These
 * registries are typically populated at startup and provide fast, on-demand access to configuration data needed during
 * request processing.
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.registry.AssetsRegistry}: Manages all API metadata
 * ({@link org.miaixz.bus.vortex.Assets}).</li>
 * <li>{@link org.miaixz.bus.vortex.registry.LimiterRegistry}: Manages all rate limiter
 * ({@link org.miaixz.bus.vortex.magic.Limiter}) instances.</li>
 * <li>{@link org.miaixz.bus.vortex.registry.ServerRegistry}: Manages server connection information and health
 * status.</li>
 * <li>{@link org.miaixz.bus.vortex.registry.AbstractRegistry}: Base class for all registry implementations, providing
 * common functionality including two-level caching support.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.registry;
