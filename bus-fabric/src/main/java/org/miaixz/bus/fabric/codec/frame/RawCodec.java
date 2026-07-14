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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Raw byte codec that maps each inbound buffer to one frame.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RawCodec implements FrameCodec {

    /**
     * Creates a raw codec.
     */
    private RawCodec() {
        // No initialization required.
    }

    /**
     * Creates a raw codec.
     *
     * @return raw codec
     */
    public static RawCodec create() {
        return new RawCodec();
    }

    /**
     * Decodes all input bytes as one frame.
     *
     * @param input input bytes
     * @return decoded frame list
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        final Buffer checkedInput = Assert
                .notNull(input, () -> new ValidateException("Raw frame input must not be null"));
        if (checkedInput.size() == 0) {
            return List.of();
        }
        try {
            return List.of(Frame.of(checkedInput.readByteString(checkedInput.size())));
        } catch (final java.io.EOFException e) {
            throw new InternalException("Unable to read raw frame", e);
        }
    }

    /**
     * Encodes frame bytes without framing.
     *
     * @param frame  frame
     * @param output encoded byte destination
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame checkedFrame = Assert.notNull(frame, () -> new ValidateException("Raw frame must not be null"));
        final Buffer checkedOutput = Assert
                .notNull(output, () -> new ValidateException("Raw frame output must not be null"));
        checkedOutput.write(checkedFrame.payload());
    }

    /**
     * Resets codec state.
     */
    @Override
    public void reset() {
        // Stateless.
    }

}
