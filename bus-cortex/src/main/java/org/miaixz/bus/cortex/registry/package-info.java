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
 * Registry infrastructure for cache projection, optional durable-store coordination, watch delivery, and health-driven
 * instance refresh.
 * <p>
 * {@code AbstractRegistry} provides the low-level cache-backed implementation of the
 * {@link org.miaixz.bus.cortex.Registry} contract. {@code StoreBackedRegistry} builds on that foundation to coordinate
 * optional {@code RegistryStore} persistence, cache warming and rebuild, and ordered post-commit {@code RegistryChange}
 * events. Shared watch lifecycles, async event fan-out, namespace limits, and per-watch backlog handling live in
 * {@code org.miaixz.bus.cortex.magic.watch}. {@code HealthProbeScheduler} runs fixed-rate probe submission with a
 * virtual-thread worker pool, republishes refreshed API instance health through {@code ApiRegistry}, and emits watch
 * updates when instance state changes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.registry;
