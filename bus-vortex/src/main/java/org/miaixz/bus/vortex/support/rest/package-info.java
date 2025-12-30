/**
 * Provides the concrete implementation for executing HTTP/HTTPS requests to downstream services.
 * <p>
 * This package contains the {@link org.miaixz.bus.vortex.support.rest.RestExecutor}, which uses Spring's
 * {@code WebClient} to perform the actual HTTP request execution. It is responsible for building the downstream
 * request, executing it, and transforming the downstream response into a format suitable for the gateway client.
 * <p>
 * The corresponding {@link org.miaixz.bus.vortex.support.RestRouter} delegates routing logic to this executor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.rest;
