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
package org.miaixz.bus.fabric.protocol.websocket;

/**
 * WebSocket endpoint role that selects RFC 6455 mask behavior.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum WebSocketRole {

    /**
     * Client endpoint.
     */
    CLIENT,

    /**
     * Server endpoint.
     */
    SERVER;

    /**
     * Returns whether outbound frames must be masked.
     *
     * @return true when writer must mask frames
     */
    public boolean writerMask() {
        return this == CLIENT;
    }

    /**
     * Returns whether inbound frames are expected to be masked.
     *
     * @return true when reader expects masked frames
     */
    public boolean readerExpectMasked() {
        return this == SERVER;
    }

}
