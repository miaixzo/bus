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
 * Provides registries for storing and retrieving runtime configuration and operational components.
 * <p>
 * This package contains classes that act as in-memory databases or registries for various gateway assets. These
 * registries are typically populated at startup and provide fast, on-demand access to configuration data needed during
 * request processing.
 * </p>
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.registry.AssetsRegistry}: Manages all API metadata
 * ({@link org.miaixz.bus.cortex.Assets}).</li>
 * <li>{@link org.miaixz.bus.vortex.registry.LimiterRegistry}: Manages all rate limiter
 * ({@link org.miaixz.bus.vortex.magic.Limiter}) instances.</li>
 * <li>{@link org.miaixz.bus.vortex.registry.ServerRegistry}: Manages server connection information and health
 * status.</li>
 * <li>{@link org.miaixz.bus.vortex.registry.AbstractRegistry}: Base class for all registry implementations, providing
 * common functionality including two-level caching support.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.registry;
