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
 * Provides the core components for an industrial-grade, annotation-driven caching framework.
 *
 * <p>
 * This package contains the central interfaces and classes that form the foundation of the caching mechanism. Key
 * components include:
 * <ul>
 * <li>{@link org.miaixz.bus.cache.CacheX}: The fundamental interface defining cache operations.</li>
 * <li>{@link org.miaixz.bus.cache.Manage}: A manager for handling multiple, named cache instances.</li>
 * <li>{@link org.miaixz.bus.cache.Complex}: The primary engine that processes caching annotations and orchestrates
 * cache reads, writes, and invalidations.</li>
 * <li>{@link org.miaixz.bus.cache.Context}: A configuration holder for global cache settings.</li>
 * <li>{@link org.miaixz.bus.cache.Collector}: An interface for implementing cache performance tracking.</li>
 * </ul>
 *
 * <p>
 * Sub-packages contain supporting elements like annotations, readers, and other utilities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cache;
