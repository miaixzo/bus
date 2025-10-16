/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
