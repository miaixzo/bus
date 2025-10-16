/**
 * Provides the final request handlers and global exception handling for the gateway.
 * <p>
 * This package contains the components that act at the end of the request processing lifecycle:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.handler.VortexHandler}: The main handler for Spring WebFlux functional endpoints. It
 * receives the fully populated {@link org.miaixz.bus.vortex.Context} after the strategy chain has completed and routes
 * the request to the appropriate downstream service via a {@link org.miaixz.bus.vortex.Router}.</li>
 * <li>{@link org.miaixz.bus.vortex.handler.ErrorsHandler}: A global
 * {@link org.springframework.web.server.WebExceptionHandler} that catches all exceptions thrown during the request
 * lifecycle and produces a standardized error response.</li>
 * <li>{@link org.miaixz.bus.vortex.handler.AccessHandler}: An interceptor-style handler for cross-cutting concerns like
 * logging.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.handler;
