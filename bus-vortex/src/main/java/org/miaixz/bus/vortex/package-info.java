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
 * Provides the core interfaces and data structures for the Vortex reactive gateway.
 * <p>
 * This package defines the fundamental contracts of the gateway's architecture, including:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.Strategy}: The primary interface for implementing individual processing steps in the
 * request chain (Chain of Responsibility pattern).</li>
 * <li>{@link org.miaixz.bus.vortex.Executor}: The interface for executing requests to different downstream protocols
 * (e.g., HTTP, gRPC, WebSocket, MQ, MCP).</li>
 * <li>{@link org.miaixz.bus.vortex.Router}: The interface for routing requests to different downstream protocols based
 * on configuration.</li>
 * <li>{@link org.miaixz.bus.vortex.Handler}: The interface for handling the final request processing and for
 * implementing interceptor-style logic.</li>
 * <li>{@link org.miaixz.bus.vortex.Context}: The central data carrier object that holds the state for a single request
 * as it flows through the gateway.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex;
