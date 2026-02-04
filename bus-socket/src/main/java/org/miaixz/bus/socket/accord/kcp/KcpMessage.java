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
package org.miaixz.bus.socket.accord.kcp;

import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Session;

import java.nio.ByteBuffer;

/**
 * Represents a KCP message for protocol decoding. This class is a basic implementation of the {@link Message} interface
 * for KCP packets.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KcpMessage implements Message<KcpPacket> {

    /**
     * Decodes a {@link KcpPacket} from the given {@link ByteBuffer}.
     * <p>
     * This implementation creates a new {@link KcpPacket} if the buffer has remaining data. It does not perform any
     * actual data decoding from the buffer into the packet.
     * </p>
     *
     * @param readBuffer the buffer containing the incoming data
     * @param session    the current session
     * @return a new {@link KcpPacket} if the buffer is not empty, otherwise {@code null}
     */
    @Override
    public KcpPacket decode(ByteBuffer readBuffer, Session session) {
        if (!readBuffer.hasRemaining()) {
            return null;
        }
        KcpPacket packet = new KcpPacket();
        return packet;
    }

}
