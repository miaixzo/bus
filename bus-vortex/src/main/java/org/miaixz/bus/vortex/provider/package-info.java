/**
 * Provides Service Provider Interfaces (SPIs) for delegating business logic to external or pluggable implementations.
 * <p>
 * This package follows the Dependency Inversion Principle by defining contracts (interfaces) for services that the core
 * gateway logic depends on. This allows the gateway's core to remain agnostic of the specific implementation details,
 * which can be provided by the hosting application and injected via dependency injection.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.provider.AuthorizeProvider}: Defines the contract for authenticating and authorizing
 * requests.</li>
 * <li>{@link org.miaixz.bus.vortex.provider.ProcessProvider}: Defines the contract for managing the lifecycle of
 * external processes.</li>
 * <li>{@link org.miaixz.bus.vortex.provider.MetricsProvider}: Defines the contract for fetching performance metrics of
 * processes.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.provider;
