/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
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
 * @since Java 21+
 */
package org.miaixz.bus.vortex.support.mcp;
