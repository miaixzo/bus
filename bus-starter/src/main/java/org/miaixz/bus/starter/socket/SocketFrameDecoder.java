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
package org.miaixz.bus.starter.socket;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;
import org.miaixz.bus.fabric.protocol.socket.frame.SocketCodec;

/**
 * Starter-local socket frame codec supplier.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SocketFrameDecoder {

    /**
     * Codec factory, invoked once per accepted connection.
     */
    private final Supplier<FrameCodec> codecFactory;

    /**
     * Creates a decoder.
     *
     * @param codecFactory codec factory
     */
    private SocketFrameDecoder(final Supplier<FrameCodec> codecFactory) {
        if (codecFactory == null) {
            throw new ValidateException("Socket codec factory must not be null");
        }
        this.codecFactory = codecFactory;
    }

    /**
     * Creates a decoder from a current frame codec factory.
     *
     * @param codecFactory codec factory
     * @return decoder
     */
    static SocketFrameDecoder of(final Supplier<FrameCodec> codecFactory) {
        return new SocketFrameDecoder(codecFactory);
    }

    /**
     * Creates a line-delimited decoder.
     *
     * @return decoder
     */
    static SocketFrameDecoder line() {
        return new SocketFrameDecoder(FrameCodec::line);
    }

    /**
     * Creates a fixed-length decoder.
     *
     * @param length frame length
     * @return decoder
     */
    static SocketFrameDecoder length(final int length) {
        return new SocketFrameDecoder(() -> FrameCodec.length(length));
    }

    /**
     * Creates a current socket codec for one accepted connection.
     *
     * @return socket codec
     */
    SocketCodec codec() {
        return SocketCodec.of(codecFactory.get());
    }

}
