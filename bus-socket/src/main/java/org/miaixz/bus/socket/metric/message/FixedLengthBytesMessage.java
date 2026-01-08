/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric.message;

import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Session;

import java.nio.ByteBuffer;

/**
 * An abstract base class for messages that are prefixed with a fixed-length byte count.
 * <p>
 * This class implements the {@link Message} interface and provides a common decoding mechanism for messages where the
 * first {@code Integer.BYTES} bytes of the message indicate the total length of the subsequent payload.
 * </p>
 *
 * @param <T> the type of the decoded message object
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class FixedLengthBytesMessage<T> implements Message<T> {

    @Override
    public final T decode(ByteBuffer readBuffer, Session session) {
        // Ensure there are enough bytes to read the length prefix
        if (readBuffer.remaining() < Integer.BYTES) {
            return null;
        }
        readBuffer.mark(); // Mark the current position for potential reset
        int length = readBuffer.getInt(); // Read the length of the message
        // Ensure there are enough bytes in the buffer for the entire message payload
        if (readBuffer.remaining() < length) {
            readBuffer.reset(); // Reset to the marked position if not enough data
            return null;
        }
        byte[] bytes = new byte[length];
        readBuffer.get(bytes); // Get the message payload
        return decode(bytes, session); // Delegate to the abstract decode method for specific message processing
    }

    /**
     * Abstract method to be implemented by subclasses for decoding the actual message payload.
     *
     * @param bytes   the byte array containing the message payload (excluding the length prefix)
     * @param session the session associated with the message
     * @return the decoded message object of type {@code T}
     */
    protected abstract T decode(byte[] bytes, Session session);

}
