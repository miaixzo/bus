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
package org.miaixz.bus.fabric.codec.frame;

import java.util.List;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Stateful frame codec contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface FrameCodec extends Decoder<Buffer, List<Frame>> {

    /**
     * Creates the default line codec.
     *
     * @return new stateful line-delimited frame codec with default delimiter and limits
     */
    static FrameCodec line() {
        try {
            return LineCodec.create();
        } catch (final RuntimeException e) {
            if (e instanceof ValidateException || e instanceof InternalException) {
                throw e;
            }
            throw new InternalException("Unable to create line frame codec", e);
        }
    }

    /**
     * Creates a fixed-length codec.
     *
     * @param length required payload size for every frame
     * @return new stateful fixed-length codec
     */
    static FrameCodec length(final int length) {
        return FixedCodec.of(length);
    }

    /**
     * Creates the default length-field codec.
     *
     * @return new stateful codec using the default length-field configuration
     */
    static FrameCodec lengthField() {
        return LengthCodec.create();
    }

    /**
     * Creates a raw byte codec.
     *
     * @return new raw codec that treats each non-empty input buffer as one frame
     */
    static FrameCodec raw() {
        return RawCodec.create();
    }

    /**
     * Decodes frames from input.
     *
     * @param input buffer containing newly available encoded bytes
     * @return frames completed by this input, in wire order
     */
    List<Frame> decode(Buffer input);

    /**
     * Encodes a frame.
     *
     * @param frame  frame to encode according to this codec's format
     * @param output destination buffer receiving encoded bytes
     */
    void encode(Frame frame, Buffer output);

    /**
     * Discards decoder state retained from previous input.
     */
    void reset();

}
