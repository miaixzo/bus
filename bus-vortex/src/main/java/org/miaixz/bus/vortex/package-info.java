/**
 * Provides the core interfaces and data structures for the Vortex reactive gateway.
 * <p>
 * This package defines the fundamental contracts of the gateway's architecture, including:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.Strategy}: The primary interface for implementing individual processing steps in the
 * request chain (Chain of Responsibility pattern).</li>
 * <li>{@link org.miaixz.bus.vortex.Executor}: The interface for executing requests to different downstream protocols
 * (e.g., HTTP, gRPC, WebSocket, MQ, MCP).</li>
 * <li>{@link org.miaixz.bus.vortex.Router}: The interface for routing requests to different downstream protocols based
 * on configuration.</li>
 * <li>{@link org.miaixz.bus.vortex.Handler}: The interface for handling the final request processing and for
 * implementing interceptor-style logic.</li>
 * <li>{@link org.miaixz.bus.vortex.Context}: The central data carrier object that holds the state for a single request
 * as it flows through the gateway.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex;
