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
 * Provides concrete implementations of the {@link org.miaixz.bus.cache.CacheX} interface.
 * <p>
 * This package contains various cache implementations that can be plugged into the framework. It includes support for
 * popular in-memory and distributed caching solutions:
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.cache.metric.MemoryCache}: A simple, thread-safe in-memory cache.</li>
 * <li>{@link org.miaixz.bus.cache.metric.GuavaCache}: An implementation backed by Google's Guava Cache.</li>
 * <li>{@link org.miaixz.bus.cache.metric.CaffeineCache}: An implementation backed by the high-performance Caffeine
 * cache.</li>
 * <li>{@link org.miaixz.bus.cache.metric.RedisCache}: A distributed cache using a single Redis instance.</li>
 * <li>{@link org.miaixz.bus.cache.metric.RedisClusterCache}: A distributed cache for a Redis Cluster setup.</li>
 * <li>{@link org.miaixz.bus.cache.metric.MemcachedCache}: A distributed cache using Memcached.</li>
 * <li>{@link org.miaixz.bus.cache.metric.NoOpCache}: A null implementation that disables caching.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cache.metric;
