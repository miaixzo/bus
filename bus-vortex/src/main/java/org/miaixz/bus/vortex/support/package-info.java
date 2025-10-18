/**
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.Router} interface for different downstream
 * protocols.
 * <p>
 * This package contains the specific logic for routing requests to various backend services based on the protocol
 * determined by the API's configuration. Each router encapsulates the details of communicating with a specific
 * protocol.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.support.RestRouter}: Forwards requests to standard HTTP/HTTPS endpoints.</li>
 * <li>{@link org.miaixz.bus.vortex.support.McpRouter}: Forwards requests to services implementing the Miaixz
 * Communication Protocol.</li>
 * <li>{@link org.miaixz.bus.vortex.support.MqRouter}: Sends requests as messages to a message queue.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support;
