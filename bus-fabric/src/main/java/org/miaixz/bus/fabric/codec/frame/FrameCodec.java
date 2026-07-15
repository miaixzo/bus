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
     * @return line codec
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
     * @param length frame length
     * @return fixed-length codec
     */
    static FrameCodec length(final int length) {
        return FixedCodec.of(length);
    }

    /**
     * Creates the default length-field codec.
     *
     * @return length-field codec
     */
    static FrameCodec lengthField() {
        return LengthCodec.create();
    }

    /**
     * Creates a raw byte codec.
     *
     * @return raw codec
     */
    static FrameCodec raw() {
        return RawCodec.create();
    }

    /**
     * Decodes frames from input.
     *
     * @param input input bytes
     * @return decoded frames
     */
    List<Frame> decode(Buffer input);

    /**
     * Encodes a frame.
     *
     * @param frame  frame
     * @param output output buffer
     */
    void encode(Frame frame, Buffer output);

    /**
     * Resets codec state.
     */
    void reset();

}
