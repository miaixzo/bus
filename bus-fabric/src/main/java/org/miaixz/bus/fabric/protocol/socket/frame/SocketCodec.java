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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;

/**
 * Socket frame codec adapter over the shared fabric frame codec.
 *
 * @author Kimi Liu
 */
public class SocketCodec {

    /**
     * Stateful fabric codec that performs the underlying framing operations.
     */
    private final FrameCodec codec;

    /**
     * Creates a codec.
     *
     * @param codec non-null stateful frame codec to adapt
     */
    public SocketCodec(final FrameCodec codec) {
        this.codec = Assert.notNull(codec, () -> new ValidateException("Frame codec must not be null"));
    }

    /**
     * Wraps a shared frame codec.
     *
     * @param codec non-null stateful frame codec to adapt
     * @return socket-specific adapter backed by the supplied codec
     */
    public static SocketCodec of(final FrameCodec codec) {
        return new SocketCodec(codec);
    }

    /**
     * Creates a socket codec with frame state owned by one session.
     *
     * @param prototype frame codec configuration prototype
     * @return socket codec backed by an independent session codec
     */
    public static SocketCodec forSession(final FrameCodec prototype) {
        final FrameCodec current = Assert
                .notNull(prototype, () -> new ValidateException("Frame codec prototype must not be null"));
        return new SocketCodec(
                Assert.notNull(current.fork(), () -> new ValidateException("Frame codec fork must not be null")));
    }

    /**
     * Decodes socket frames.
     *
     * @param input non-null buffer containing newly available socket bytes
     * @return immutable list of socket frames carrying the decoded fabric-frame payloads
     */
    public List<SocketFrame> decode(final Buffer input) {
        final List<ByteString> decoded = decodeOwned(input);
        final ArrayList<SocketFrame> frames = new ArrayList<>(decoded.size());
        for (final ByteString payload : decoded) {
            frames.add(SocketFrame.of(payload));
        }
        return List.copyOf(frames);
    }

    /**
     * Decodes frame payload owners for the Socket runtime without an intermediate SocketFrame snapshot.
     *
     * @param input non-null buffer containing newly available socket bytes
     * @return immutable list of decoded immutable payload owners
     */
    public List<ByteString> decodeOwned(final Buffer input) {
        final Buffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Socket codec input must not be null"));
        return codec.decodeOwned(checkedInput);
    }

    /**
     * Decodes payload owners into caller-owned storage.
     *
     * @param input  non-null buffer containing newly available socket bytes
     * @param output destination collection
     * @return number of decoded payload owners
     */
    public int decodeOwned(final Buffer input, final Collection<? super ByteString> output) {
        final Buffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Socket codec input must not be null"));
        return codec.decodeOwned(checkedInput, output);
    }

    /**
     * Decodes shared frames for the Socket runtime without another list or payload snapshot.
     *
     * @param input non-null buffer containing newly available socket bytes
     * @return immutable decoded frame list produced by the configured FrameCodec
     */
    public List<Frame> decodeFrames(final Buffer input) {
        final Buffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Socket codec input must not be null"));
        return codec.decode(checkedInput);
    }

    /**
     * Encodes a socket frame.
     *
     * @param frame  non-null socket frame whose payload is encoded
     * @param output non-null destination buffer receiving encoded bytes
     */
    public void encode(final SocketFrame frame, final Buffer output) {
        final SocketFrame checkedFrame = Assert
                .notNull(frame, () -> new ValidateException("Socket frame must not be null"));
        encodeOwned(checkedFrame.payload(), output);
    }

    /**
     * Encodes an immutable payload owner for the Socket runtime without an intermediate SocketFrame snapshot.
     *
     * @param payload non-null immutable payload owner
     * @param output  non-null destination buffer receiving encoded bytes
     */
    public void encodeOwned(final ByteString payload, final Buffer output) {
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("Socket frame payload must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Socket codec output must not be null"));
        codec.encodeOwned(checkedPayload, checkedOutput);
    }

    /**
     * Resets the state retained by the underlying frame codec.
     */
    public void reset() {
        codec.reset();
    }

}
