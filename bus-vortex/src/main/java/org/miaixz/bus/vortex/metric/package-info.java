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
 * Provides monitoring and metrics collection utilities for the Vortex reactive gateway.
 * <p>
 * This package contains classes for tracking and analyzing gateway performance metrics in real-time.
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.metric.DefaultMonitor}: A default in-memory implementation of the
 * {@link org.miaixz.bus.vortex.Monitor} interface for tracking request metrics, cache statistics, and database
 * operations.</li>
 * <li>{@link org.miaixz.bus.vortex.metric.CacheStats}: Data transfer object for capturing cache performance metrics
 * including hit/miss counts, hit rate, and cache size.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex.metric;
