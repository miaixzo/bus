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
 * @since Java 21+
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
