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
 * Provides the MCP Streamable HTTP reverse-proxy executor.
 * <p>
 * The package contains only the runtime executor for standard {@code /router/mcp/**} requests. It does not create MCP
 * servers, start stdio processes, aggregate tools, or maintain MCP clients. Every request is resolved through
 * registered assets and then proxied to one registered Streamable HTTP target endpoint.
 * <p>
 * The corresponding {@link org.miaixz.bus.vortex.routing.McpRouter} delegates to
 * {@link org.miaixz.bus.vortex.routing.mcp.McpExecutor#execute(org.miaixz.bus.vortex.Context, org.springframework.web.reactive.function.server.ServerRequest)}.
 * Without an explicit {@code format} query parameter, responses follow standard MCP semantics and are returned as JSON,
 * Server-Sent Events, or empty 202/204 responses. When {@code format} is present, the strategy chain applies the
 * gateway's normal response formatting after proxy execution.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex.routing.mcp;
