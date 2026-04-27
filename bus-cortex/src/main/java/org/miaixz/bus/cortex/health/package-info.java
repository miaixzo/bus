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
 * Active liveness and health probe implementations for registered service instances.
 * <p>
 * Every class in this package implements the {@link org.miaixz.bus.cortex.Prober} contract and returns a {@code Status}
 * carrying the health flag, measured latency, and diagnostic details. {@code HttpProber} performs an HTTP GET against
 * the instance health endpoint. {@code TcpProber} attempts a TCP socket connection within the configured timeout.
 * {@code ProcessProber} checks whether a local OS process with a given PID is still alive. {@code McpPingProber} posts
 * a JSON-RPC ping to the MCP endpoint. {@code CompositeProber} runs an ordered list of delegate probers, skips
 * delegates that do not support the target instance, and returns the first failure or a combined success result.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.health;
