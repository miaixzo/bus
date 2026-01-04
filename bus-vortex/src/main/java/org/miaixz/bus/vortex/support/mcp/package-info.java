/**
 * Provides the core executors and concrete implementations for supporting the Miaixz Communication Protocol (MCP).
 * <p>
 * This package is the root for all MCP-related functionality. It contains:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.support.mcp.McpExecutor}: The central executor that manages the lifecycle of all MCP
 * clients.</li>
 * <li>The {@code client} subpackage: Contains different {@link org.miaixz.bus.vortex.support.mcp.client.McpClient}
 * implementations for various transport protocols (e.g., stdio, http).</li>
 * <li>The {@code process} subpackage: Contains the default implementation for managing local MCP service
 * processes.</li>
 * </ul>
 * <p>
 * The corresponding {@link org.miaixz.bus.vortex.support.McpRouter} delegates routing logic to this executor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.mcp;
