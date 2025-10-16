/**
 * Provides the Spring WebFlux {@link org.springframework.web.server.WebFilter} implementations that integrate the
 * Vortex gateway with the web server.
 * <p>
 * This package acts as the primary entry point for all incoming HTTP requests. The
 * {@link org.miaixz.bus.vortex.filter.PrimaryFilter} is the most critical component, responsible for intercepting
 * requests, initializing the {@link org.miaixz.bus.vortex.Context}, and dispatching the request to the internal
 * strategy chain for processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.filter;
