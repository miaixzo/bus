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
package org.miaixz.bus.fabric.network.udp;

import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Payload;

/**
 * Transforms logical UDP datagrams to and from a physical relay endpoint.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface UdpDatagramCodec {

    /**
     * Physical relay address used by the connected UDP channel.
     *
     * @return non-null physical relay destination
     */
    Address relay();

    /**
     * Encodes one payload for transmission to the relay.
     *
     * @param target  logical datagram destination represented in the relay frame
     * @param payload application datagram payload
     * @return encoded physical-relay payload
     */
    Payload encode(Address target, Payload payload);

    /**
     * Decodes one payload received from the relay.
     *
     * @param target  logical peer expected by the connected session
     * @param payload framed payload received from the physical relay
     * @return decoded application datagram payload
     */
    Payload decode(Address target, Payload payload);

}
