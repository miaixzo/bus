/**
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.support.mcp.client.McpClient} interface, each
 * tailored for a specific underlying communication protocol.
 * <p>
 * This package encapsulates the protocol-specific details of interacting with different types of MCP services.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.client.StdioClient}: Communicates with a local process via standard
 * I/O.</li>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.client.HttpClient}: Interacts with a standard RESTful HTTP
 * endpoint.</li>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.client.SseClient}: Connects to a remote service that exposes an SSE
 * stream.</li>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.client.OpenApiClient}: Dynamically discovers tools from an OpenAPI
 * specification.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.mcp.client;
