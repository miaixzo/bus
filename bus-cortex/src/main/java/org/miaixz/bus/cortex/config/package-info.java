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
 * Dynamic configuration management with versioned history, watch-based change notification and gray-release routing.
 * <p>
 * {@code ConfigAssets} persists a single configuration entry, extending the base asset definition with a group
 * identifier, a string key, the configuration content and an integer version counter that is separate from the asset's
 * string version field. {@code ConfigPublisher} writes a new value to CacheX and immediately records a
 * {@code ConfigVersion} snapshot (version number, content, timestamp) to maintain a full change history; for cases
 * where history is not needed, {@code SimpleConfigPublisher} in the {@code builtin.event} package writes the value
 * directly without versioning. {@code ConfigChange} and {@code ConfigChangeEvent} model the change payload and the
 * event dispatched to subscribers. {@code ConfigWatcher} manages per-namespace subscriptions backed by the shared watch
 * infrastructure and notifies all registered callbacks when a key changes.
 * <p>
 * {@code DefaultConfig} is the CacheX-backed implementation of the {@link org.miaixz.bus.cortex.Config} contract; it
 * integrates the publisher, watcher and gray router into a single entry point. {@code GrayRouter} evaluates a
 * {@code GrayRule} against a {@code RequestContext} to decide whether to serve a gray-release configuration value.
 * {@code GrayRule} supports four match strategies: HEADER (header name equals a value), IP (exact client IP), IP_RANGE
 * (client IP falls within a CIDR subnet) and PERCENTAGE (random fraction of requests). {@code RequestContext} carries
 * the client IP address and request headers consumed by the router during evaluation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.config;
