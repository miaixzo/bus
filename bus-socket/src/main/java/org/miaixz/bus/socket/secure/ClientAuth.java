/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.socket.secure;

/**
 * Configures the {@link javax.net.ssl.SSLEngine} to request client authentication. This option is only relevant for
 * engines operating in server mode.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ClientAuth {
    /**
     * No client authentication is required.
     */
    NONE,
    /**
     * Client authentication is requested. If this option is set and the client chooses not to provide its own
     * authentication information, the handshake will continue.
     */
    OPTIONAL,
    /**
     * Client authentication is required. If this option is set and the client chooses not to provide its own
     * authentication information, the handshake will terminate and the engine will begin its shutdown process.
     */
    REQUIRE

}
