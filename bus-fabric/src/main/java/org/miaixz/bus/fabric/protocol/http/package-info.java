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
 * Implements the current HTTP request/response exchange model.
 *
 * <p>
 * Synchronous and asynchronous client requests follow {@code HttpX -> HttpCall -> Mediator -> HttpRunner}.
 * {@code SoapX} delegates to {@code HttpX}, and the default Context-backed {@code HttpDownload} exchange also enters
 * {@code HttpRunner} through {@code Mediator}; a custom download Exchange remains caller supplied. {@code HTTP_STREAM}
 * and {@code HTTP_UPGRADE} are internal carrier targets reached only by the allowed SSE and WebSocket conversions.
 * Request, response and cookie types carry HTTP semantics, while connection acquisition, TLS and proxy routing remain
 * in the network and registry packages. HTTP/1 and HTTP/2 codecs use core.io {@code Source}, {@code Sink}, and
 * {@code Buffer} for connection IO.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.fabric.protocol.http;
