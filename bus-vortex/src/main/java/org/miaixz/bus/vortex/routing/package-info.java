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
 * {@link org.miaixz.bus.vortex.Executor} interfaces for outbound forwarding.
 * <p>
 * This package contains the routing layer for requests whose asset has already been resolved by the strategy chain.
 * Each router delegates route-specific execution to its executor.
 * <p>
 * The HTTP outbound routes share {@link org.miaixz.bus.vortex.Egress.Http} and keep their own strategies and executors.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.routing.RestRouter}: Standard HTTP forwarding.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.LlmRouter}: Forwards model requests through an HTTP provider.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.SlugRouter}: Resolves public slug paths and forwards their requests.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.SdcRouter}: Relays authenticated service discovery and configuration
 * requests.</li>
 * </ul>
 * <p>
 * Protocol and message broker adapters:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.routing.McpRouter}: Routes standard {@code /router/mcp/**} requests.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mcp.McpExecutor}: Proxies registered MCP Streamable HTTP targets without
 * creating MCP servers or clients.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.MqRouter}: Routes requests to message queues.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.mq.MqExecutor}: Executes requests as messages to a message queue.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.WsRouter}: Routes requests to WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.ws.WsExecutor}: Manages and executes WebSocket connections.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.GrpcRouter}: Routes requests to gRPC services.</li>
 * <li>{@link org.miaixz.bus.vortex.routing.grpc.GrpcExecutor}: Executes gRPC methods via HTTP gateway.</li>
 * </ul>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.vortex.routing;
