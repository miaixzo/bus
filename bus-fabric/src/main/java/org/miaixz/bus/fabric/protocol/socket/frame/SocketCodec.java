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
package org.miaixz.bus.fabric.protocol.socket.frame;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;

/**
 * Socket frame codec adapter over the shared fabric frame codec.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketCodec {

    /**
     * Shared frame codec.
     */
    private final FrameCodec codec;

    /**
     * Creates a codec.
     *
     * @param codec frame codec
     */
    private SocketCodec(final FrameCodec codec) {
        if (codec == null) {
            throw new ValidateException("Frame codec must not be null");
        }
        this.codec = codec;
    }

    /**
     * Wraps a shared frame codec.
     *
     * @param codec frame codec
     * @return socket codec
     */
    public static SocketCodec of(final FrameCodec codec) {
        return new SocketCodec(codec);
    }

    /**
     * Decodes socket frames.
     *
     * @param input input
     * @return frames
     */
    public List<SocketFrame> decode(final ByteBuffer input) {
        if (input == null) {
            throw new ValidateException("Socket codec input must not be null");
        }
        final ArrayList<SocketFrame> frames = new ArrayList<>();
        for (final Frame frame : codec.decode(input.duplicate())) {
            frames.add(SocketFrame.of(frame.payload()));
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a socket frame.
     *
     * @param frame frame
     * @return encoded bytes
     */
    public ByteBuffer encode(final SocketFrame frame) {
        if (frame == null) {
            throw new ValidateException("Socket frame must not be null");
        }
        return codec.encode(Frame.of(frame.payload())).asReadOnlyBuffer();
    }

    /**
     * Resets decoder state.
     */
    public void reset() {
        codec.reset();
    }

}
