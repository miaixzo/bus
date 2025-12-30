/**
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.Router} and
 * {@link org.miaixz.bus.vortex.Executor} interfaces for different downstream protocols.
 * <p>
 * This package contains the specific logic for routing and executing requests to various backend systems based on the
 * protocol determined by the API's configuration. Each implementation encapsulates the details of communicating with a
 * specific protocol.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.support.RestRouter}: Routes requests to standard HTTP/HTTPS endpoints.</li>
 * <li>{@link org.miaixz.bus.vortex.support.rest.RestExecutor}: Executes requests to standard HTTP/HTTPS endpoints.</li>
 * <li>{@link org.miaixz.bus.vortex.support.McpRouter}: Routes requests to MCP services.</li>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.McpExecutor}: Manages and executes requests to services implementing the
 * Miaixz Communication Protocol.</li>
 * <li>{@link org.miaixz.bus.vortex.support.MqRouter}: Routes requests to message queues.</li>
 * <li>{@link org.miaixz.bus.vortex.support.mq.MqExecutor}: Executes requests as messages to a message queue.</li>
 * <li>{@link org.miaixz.bus.vortex.support.WsRouter}: Routes requests to WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.support.ws.WsExecutor}: Manages and executes WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.support.GrpcRouter}: Routes requests to gRPC services.</li>
 * <li>{@link org.miaixz.bus.vortex.support.grpc.GrpcExecutor}: Executes gRPC methods via HTTP gateway.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support;
