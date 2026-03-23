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
 * Registry infrastructure providing the generic CacheX-backed base, watch subscription management and periodic health
 * scheduling for all Cortex registries.
 * <p>
 * {@code AbstractRegistry} is a generic base that implements the {@link org.miaixz.bus.cortex.Registry} contract for
 * any {@code Assets} subtype. It provides keyed CRUD operations — save, remove, get and list — using a configurable
 * subtype prefix to partition keys in CacheX, and delegates watch notifications to a {@code WatchManager} on every
 * write. {@code WatchManager} maintains active watch subscriptions keyed by generated IDs, enforces a per-namespace
 * subscription limit (default 1 000) and an idle-expiry timeout (default 24 h), and fans out change events to all
 * matching {@link org.miaixz.bus.cortex.Listener} callbacks. {@code WatchSubscription} is the value object representing
 * a single active subscription: the listener reference, the namespace filter and the last-access timestamp used for
 * expiry. {@code HealthProbeScheduler} runs a single-threaded scheduled loop that periodically probes every registered
 * runtime instance via a configured {@link org.miaixz.bus.cortex.Prober}, rewrites the instance healthy flag in CacheX,
 * and publishes a watch change event whenever the health state transitions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.registry;
