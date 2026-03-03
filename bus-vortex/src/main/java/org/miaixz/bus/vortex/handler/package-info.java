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
 * Provides the final request handlers and global exception handling for the gateway.
 * <p>
 * This package contains the components that act at the end of the request processing lifecycle:
 * <ul>
 * <li>{@link org.miaixz.bus.vortex.handler.VortexHandler}: The main handler for Spring WebFlux functional endpoints. It
 * receives the fully populated {@link org.miaixz.bus.vortex.Context} after the strategy chain has completed and routes
 * the request to the appropriate downstream service via a {@link org.miaixz.bus.vortex.Router}.</li>
 * <li>{@link org.miaixz.bus.vortex.handler.ErrorsHandler}: A global
 * {@link org.springframework.web.server.WebExceptionHandler} that catches all exceptions thrown during the request
 * lifecycle and produces a standardized error response.</li>
 * <li>{@link org.miaixz.bus.vortex.handler.AccessHandler}: An interceptor-style handler for cross-cutting concerns like
 * logging.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.vortex.handler;
