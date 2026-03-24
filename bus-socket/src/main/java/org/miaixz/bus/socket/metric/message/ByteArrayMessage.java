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

import org.miaixz.bus.socket.Session;

/**
 * A message implementation that treats the entire received data as a byte array. This class extends
 * {@link FixedLengthBytesMessage} and simply returns the raw byte array as the decoded message.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ByteArrayMessage extends FixedLengthBytesMessage<byte[]> {

    /**
     * Decodes the given byte array into a message. In this implementation, the byte array itself is the message.
     *
     * @param bytes   the byte array to decode
     * @param session the session associated with the message
     * @return the input byte array as the decoded message
     */
    @Override
    protected byte[] decode(byte[] bytes, Session session) {
        return bytes;
    }

}
