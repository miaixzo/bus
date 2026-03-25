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
 * MCP (Model Context Protocol) tool server registry and stdio process lifecycle management.
 * <p>
 * {@code McpAssets} extends the base asset definition with MCP-specific fields: the public tool name exposed to LLM
 * clients, the transport protocol (e.g. stdio, SSE), the capability schema advertised by the tool, and a list of
 * discovery tags. {@code McpRegistry} provides full CRUD and watch support for {@code McpAssets} definitions backed by
 * the generic {@code AbstractRegistry}. {@code ProcessManager} tracks locally hosted stdio MCP server sub-processes in
 * a {@code ConcurrentHashMap} keyed by a caller-supplied process identifier; it starts a command via
 * {@code ProcessBuilder} with inherited I/O, stops it forcibly on deregister, and checks liveness via
 * {@code isAlive()}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.registry.mcp;
