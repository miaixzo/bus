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
 * Provides Service Provider Interfaces (SPIs) for delegating business logic to external or pluggable implementations.
 * <p>
 * This package follows the Dependency Inversion Principle by defining contracts (interfaces) for services that the core
 * gateway logic depends on. This allows the gateway's core to remain agnostic of the specific implementation details,
 * which can be provided by the hosting application and injected via dependency injection.
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.provider.AuthorizeProvider}: Defines the contract for authenticating and authorizing
 * requests.</li>
 * <li>{@link org.miaixz.bus.vortex.provider.ProcessProvider}: Defines the contract for managing the lifecycle of
 * external processes.</li>
 * <li>{@link org.miaixz.bus.vortex.provider.MetricsProvider}: Defines the contract for fetching performance metrics of
 * processes.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.vortex.provider;
