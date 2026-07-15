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
 * {@code WebSocketX} performs client HTTP upgrades, {@code WebSocketServer} accepts server upgrades, and
 * {@code WebSocketSession} is the shared client/server session that owns frame read/write, ping/pong, close handshake,
 * outbound queue limits and handler notification. {@code WebSocketRole} centralizes mask expectations so the same frame
 * reader and writer implementations work for both endpoint roles. Client and server sessions attach to network IO
 * through {@code Connection.source()} and {@code Connection.sink()}, so WebSocket frame codecs consume bus-core source
 * and sink views instead of protocol-owned channel adapters.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.fabric.protocol.websocket;
