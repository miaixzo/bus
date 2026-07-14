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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
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
        this.codec = Assert.notNull(codec, () -> new ValidateException("Frame codec must not be null"));
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
    public List<SocketFrame> decode(final Buffer input) {
        final Buffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Socket codec input must not be null"));
        final List<Frame> decoded = codec.decode(checkedInput);
        final ArrayList<SocketFrame> frames = new ArrayList<>(decoded.size());
        for (final Frame frame : decoded) {
            frames.add(SocketFrame.of(frame.payload()));
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a socket frame.
     *
     * @param frame  frame
     * @param output output buffer
     */
    public void encode(final SocketFrame frame, final Buffer output) {
        final SocketFrame checkedFrame = Assert
                .notNull(frame, () -> new ValidateException("Socket frame must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Socket codec output must not be null"));
        codec.encode(Frame.of(checkedFrame.payload()), checkedOutput);
    }

    /**
     * Decodes socket frames from a JDK byte buffer compatibility boundary.
     *
     * @param input input
     * @return frames
     * @deprecated use {@link #decode(Buffer)}
     */
    @Deprecated(since = "8.8.3")
    public List<SocketFrame> decode(final ByteBuffer input) {
        final ByteBuffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Socket codec input must not be null"));
        final List<Frame> decoded = codec.decode(checkedInput);
        final ArrayList<SocketFrame> frames = new ArrayList<>(decoded.size());
        for (final Frame frame : decoded) {
            frames.add(SocketFrame.of(frame.payload()));
        }
        return List.copyOf(frames);
    }

    /**
     * Encodes a socket frame to a JDK byte buffer compatibility boundary.
     *
     * @param frame frame
     * @return encoded bytes
     * @deprecated use {@link #encode(SocketFrame, Buffer)}
     */
    @Deprecated(since = "8.8.3")
    public ByteBuffer encode(final SocketFrame frame) {
        final SocketFrame checkedFrame = Assert
                .notNull(frame, () -> new ValidateException("Socket frame must not be null"));
        return codec.encode(Frame.of(checkedFrame.payload()));
    }

    /**
     * Resets decoder state.
     */
    public void reset() {
        codec.reset();
    }

}
