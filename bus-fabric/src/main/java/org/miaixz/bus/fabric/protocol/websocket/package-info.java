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
 * Implements WebSocket exchanges, sessions and listener lifecycle.
 *
 * <p>
 * Synchronous and asynchronous client opens follow {@code WebSocketX -> WebSocketCall -> Mediator -> WebSocketRunner}.
 * The runner performs its HTTP upgrade only through the allowed
 * {@code Mediator.convert(WEBSOCKET, HTTP_UPGRADE) -> HttpRunner} carrier transition. {@code WebSocketSession} owns
 * established-session frame read/write, ping/pong, close handshake, outbound queue limits and handler notification.
 * {@code WebSocketServer} accepts server upgrades outside the client Mediator route, while {@code WebSocketRole}
 * centralizes client and server mask expectations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.fabric.protocol.websocket;
