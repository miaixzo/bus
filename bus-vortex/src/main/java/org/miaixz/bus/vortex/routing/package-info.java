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
 * Provides concrete implementations of the {@link org.miaixz.bus.vortex.Router} and
 * {@link org.miaixz.bus.vortex.Executor} interfaces for different downstream protocols.
 * <p>
 * This package contains the specific logic for routing and executing requests to various backend systems based on the
 * protocol determined by the API's configuration. Each implementation encapsulates the details of communicating with a
 * specific protocol.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.routing.RestRouter}: Routes requests to standard HTTP/HTTPS endpoints.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.rest.RestExecutor}: Executes requests to standard HTTP/HTTPS endpoints.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.McpRouter}: Routes requests to MCP services.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.McpExecutor}: Manages and executes requests to services implementing the
 * Miaixz Communication Protocol.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.MqRouter}: Routes requests to message queues.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mq.MqExecutor}: Executes requests as messages to a message queue.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.WsRouter}: Routes requests to WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.ws.WsExecutor}: Manages and executes WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.GrpcRouter}: Routes requests to gRPC services.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.grpc.GrpcExecutor}: Executes gRPC methods via HTTP gateway.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex.routing;
