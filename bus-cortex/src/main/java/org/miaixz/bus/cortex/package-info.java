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
 * bus-cortex — a lightweight, CacheX-backed unified registry and dynamic configuration center for API gateways, MCP
 * tool servers and LLM prompt templates. It provides a single set of domain objects, contracts and constants shared by
 * every sub-package.
 * <p>
 * The domain model centers on three complementary objects. {@code Nature} is the minimal common base carrying
 * {@code id}, {@code namespace} and {@code species}. {@code Assets} extends it with gateway-facing routing fields:
 * name, host/port/path, HTTP method, auth policy, timeout, throttle limit, load-balance strategy, key/value labels and
 * generic metadata. {@code Instance} is the runtime snapshot of a single live node, holding host, port, weight, healthy
 * flag, SHA-256 fingerprint, OS process ID and scheme; the health scheduler rewrites it on every probe cycle.
 * {@code Vector} carries query and filter criteria — namespace, method, semantic version, label selectors, instance
 * state and pagination limit/offset — passed directly to registry lookup calls. {@code Species} classifies entries as
 * API, MCP, PROMPT, CONFIG or VERSION, and {@code Status} wraps the result of any health probe: a boolean healthy flag,
 * measured latency and a diagnostic message.
 * <p>
 * Four contracts define the integration surface. {@link org.miaixz.bus.cortex.Registry} is the generic CRUD and watch
 * interface implemented by every concrete registry; all callers depend only on this type.
 * {@link org.miaixz.bus.cortex.Prober} is the single-method health-check interface whose implementations live in
 * {@code cortex.health}. {@link org.miaixz.bus.cortex.Listener} is the typed callback invoked by {@code WatchManager}
 * when registry entries are added, removed or updated. {@link org.miaixz.bus.cortex.Config} is the configuration-center
 * read/write contract implemented by {@code DefaultConfig}.
 * <p>
 * {@code Builder} holds all shared CacheX key prefixes ({@code reg:}, {@code cfg:}, {@code sec:}, {@code seq:},
 * {@code audit:}) and every HTTP endpoint path constant used by the Cortex server controllers. {@code Cortex} is the
 * static bootstrap facade; call {@code Cortex.start(cacheX)} to initialise the runtime and obtain the service locator
 * used across the application.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex;
