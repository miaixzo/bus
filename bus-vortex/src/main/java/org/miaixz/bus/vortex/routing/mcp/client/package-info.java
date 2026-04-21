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
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.routing.mcp.client.McpClient} interface, each
 * tailored for a specific underlying communication protocol.
 * <p>
 * This package encapsulates the protocol-specific details of interacting with different types of MCP services.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.client.StdioClient}: Communicates with a local process via standard
 * I/O.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.client.HttpClient}: Interacts with a standard RESTful HTTP
 * endpoint.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.client.SseClient}: Connects to a remote service that exposes an SSE
 * stream.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.client.OpenApiClient}: Dynamically discovers tools from an OpenAPI
 * specification.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex.routing.mcp.client;
