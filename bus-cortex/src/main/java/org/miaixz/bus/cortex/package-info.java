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
 * bus-cortex provides shared models, contracts, and support services for store-backed registries, dynamic setting
 * delivery, health probing, watch dispatch, identity normalization, and bridge synchronization.
 * <p>
 * Core runtime models include {@code Nature}, {@code Assets}, {@code Instance}, {@code Vector}, and {@code Status}.
 * {@code Type} classifies both registry content (API, MCP, PROMPT, VERSION) and setting resources (namespace, app,
 * profile, item, revision, binding) while keeping registry and setting categories distinguishable. Registry and curator
 * implementations can coordinate optional durable stores with CacheX projections, while {@code Watch} and
 * {@code RegistryChange} carry ordered change notifications across watchers and bridge listeners.
 * <p>
 * Shared namespace/application defaults live in {@code magic.identity}, and shared subscription dispatch lives in
 * {@code magic.watch}. Registry-specific type and route identity remain under {@code registry}; setting resources
 * consume the common magic layer without treating registry as their infrastructure owner.
 * <p>
 * The primary integration contracts are {@link org.miaixz.bus.cortex.Registry}, {@link org.miaixz.bus.cortex.Curator},
 * {@link org.miaixz.bus.cortex.Prober}, {@link org.miaixz.bus.cortex.Change}, and
 * {@link org.miaixz.bus.cortex.Listener}. {@code Builder} holds a small set of shared runtime defaults and key
 * prefixes, {@code Callout} centralizes lightweight outbound HTTP calls, and {@code Cortex} is the static facade that
 * integration code can bind to assembled registries and curator services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex;
