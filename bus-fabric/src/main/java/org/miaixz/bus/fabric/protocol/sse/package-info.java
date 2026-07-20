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
 * Implements server-sent event exchanges, reconnect handling and listener delivery.
 *
 * <p>
 * Synchronous and asynchronous client opens follow {@code SseX -> SseCall -> Mediator -> SseRunner}. The runner obtains
 * its HTTP response stream only through the allowed {@code Mediator.convert(SSE, HTTP_STREAM) -> HttpRunner} carrier
 * transition. {@code EventSourceFactory} delegates to {@code SseX}; it does not add a second routing layer.
 * {@code SseSession} owns lifecycle control and listener notification, and each reconnect keeps its independently
 * created cancellation scope while using the same conversion.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.fabric.protocol.sse;
