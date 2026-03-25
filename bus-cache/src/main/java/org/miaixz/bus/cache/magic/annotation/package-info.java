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
 * Provides the annotations used to drive the caching behavior of the framework.
 * <p>
 * This package contains the core annotations that developers can use to declaratively manage caching on their methods.
 * The primary annotations include:
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.cache.magic.annotation.Cached}: For read-through caching (read, and write on miss).</li>
 * <li>{@link org.miaixz.bus.cache.magic.annotation.CachedGet}: For read-only caching (read, but no write on miss).</li>
 * <li>{@link org.miaixz.bus.cache.magic.annotation.Invalid}: For cache invalidation.</li>
 * <li>{@link org.miaixz.bus.cache.magic.annotation.CacheKey}: To designate a parameter's role in forming the cache
 * key.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cache.magic.annotation;
