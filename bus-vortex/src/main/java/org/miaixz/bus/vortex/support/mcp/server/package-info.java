/**
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.provider.ProcessProvider} interface.
 * <p>
 * This package contains different strategies for managing the lifecycle of external processes. The default
 * implementation, {@link org.miaixz.bus.vortex.support.mcp.server.ManageProvider}, manages processes running on the
 * local operating system. Future implementations could include providers for managing Docker containers or remote
 * processes via SSH.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.support.mcp.server;
