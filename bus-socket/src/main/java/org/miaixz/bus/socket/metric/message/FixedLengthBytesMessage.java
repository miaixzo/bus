/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
