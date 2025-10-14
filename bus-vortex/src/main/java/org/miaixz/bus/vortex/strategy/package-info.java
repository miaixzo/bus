/**
 * Provides the core implementation of the Chain of Responsibility pattern for the gateway.
 * <p>
 * This package contains the {@link org.miaixz.bus.vortex.Strategy} interface and all its concrete implementations. Each
 * strategy represents a single, distinct step in the request processing pipeline, such as request parsing,
 * authorization, rate limiting, or data transformation.
 * <p>
 * The {@link org.miaixz.bus.vortex.strategy.StrategyFactory} is a key component in this package, responsible for
 * assembling the appropriate chain of strategies for a given request.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.strategy;
