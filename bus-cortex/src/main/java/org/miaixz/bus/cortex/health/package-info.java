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
 * carrying a boolean healthy flag and the measured round-trip latency in milliseconds. {@code HttpProber} sends an HTTP
 * GET to a configurable URL and considers the instance healthy when the response status code is 2xx. {@code TcpProber}
 * opens a TCP socket to the instance host and port and reports healthy when the connection succeeds within the timeout
 * defined by {@code Builder.DEFAULT_HEALTH_TIMEOUT_MS}. {@code ProcessProber} checks whether a local OS process with a
 * given PID is still alive. {@code McpPingProber} sends an MCP ping request over HTTP to verify that a remote MCP
 * server is responsive. {@code CompositeProber} fans out to an ordered list of delegate probers and reports healthy
 * only when every delegate succeeds; the first failure short-circuits the remaining delegates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.health;
