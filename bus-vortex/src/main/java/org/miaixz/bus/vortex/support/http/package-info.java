/**
 * Provides the concrete implementation for routing requests to downstream HTTP/HTTPS services.
 * <p>
 * This package contains the {@link org.miaixz.bus.vortex.support.http.HttpService}, which uses Spring's
 * {@code WebClient} to perform the actual HTTP request forwarding. It is responsible for building the downstream
 * request, executing it, and transforming the downstream response into a format suitable for the gateway client.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.http;
