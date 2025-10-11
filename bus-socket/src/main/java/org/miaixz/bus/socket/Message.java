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
package org.miaixz.bus.socket;

import org.miaixz.bus.socket.accord.AioClient;
import org.miaixz.bus.socket.accord.AioServer;

import java.nio.ByteBuffer;

/**
 * Defines the message protocol for data transfer.
 * <p>
 * Implementations of this interface define how messages are encoded and decoded according to a specific protocol. The
 * implemented class should be registered with the server startup classes, such as {@link AioClient} or
 * {@link AioServer}.
 * </p>
 * Note: All Socket links within the framework reuse the same Message instance. Therefore, do not store link-specific
 * data in the member variables of the implementation class.
 *
 * @param <T> the type of the message object entity
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Message<T> {

    /**
     * Decodes data from the socket stream according to the current protocol implementation.
     *
     * @param readBuffer the {@link ByteBuffer} containing the data to be decoded
     * @param session    the {@link Session} for which the message is being decoded
     * @return the business message object encapsulated after successful decoding, or {@code null} if decoding is not
     *         yet complete
     */
    T decode(final ByteBuffer readBuffer, Session session);

}
