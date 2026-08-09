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
package org.miaixz.bus.fabric.protocol.websocket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.fabric.protocol.websocket.frame.WebSocketFrame;

/**
 * Stateful assembler for WebSocket data and continuation frames.
 * <p>
 * Complete single-frame messages are returned without copying. A buffer is allocated only after the first fragmented
 * data frame and is released immediately when the message completes or the assembler is reset.
 *
 * @author Kimi Liu
 */
final class WebSocketAssembler {

    /**
     * Text data opcode.
     */
    static final int TEXT = 0x1;

    /**
     * Binary data opcode.
     */
    static final int BINARY = 0x2;

    /**
     * Continuation opcode.
     */
    static final int CONTINUATION = 0x0;

    /**
     * Maximum complete message bytes.
     */
    private final long maximumBytes;

    /**
     * Fragment buffer, allocated only while a fragmented message is open.
     */
    private Buffer fragments;

    /**
     * Initial data opcode for the open fragmented message.
     */
    private int opcode;

    /**
     * Accumulated fragment bytes.
     */
    private long bytes;

    /**
     * Creates an assembler.
     *
     * @param maximumBytes maximum complete message bytes
     */
    WebSocketAssembler(final long maximumBytes) {
        if (maximumBytes < Normal._0) {
            throw new IllegalArgumentException("WebSocket maximum message bytes must be non-negative");
        }
        this.maximumBytes = maximumBytes;
        this.opcode = Normal.__1;
    }

    /**
     * Accepts one data or continuation frame.
     *
     * @param frame validated frame
     * @return complete message, or {@code null} while awaiting more fragments
     */
    Message accept(final WebSocketFrame frame) {
        final int currentOpcode = frame.opcode();
        if (currentOpcode == TEXT || currentOpcode == BINARY) {
            if (fragments != null) {
                throw new ProtocolException("WebSocket fragmented message is already open");
            }
            check(frame.payload().size());
            if (frame.fin()) {
                return new Message(currentOpcode, frame.payload());
            }
            fragments = new Buffer();
            opcode = currentOpcode;
            append(frame.payload());
            return null;
        }
        if (currentOpcode != CONTINUATION || fragments == null) {
            throw new ProtocolException("WebSocket continuation has no initial frame");
        }
        append(frame.payload());
        if (!frame.fin()) {
            return null;
        }
        final Message complete = new Message(opcode, fragments.readByteString());
        reset();
        return complete;
    }

    /**
     * Releases an incomplete fragmented message.
     */
    void reset() {
        if (fragments != null) {
            fragments.clear();
        }
        fragments = null;
        opcode = Normal.__1;
        bytes = Normal.LONG_ZERO;
    }

    /**
     * Appends one fragment with overflow and aggregate-size protection.
     *
     * @param fragment fragment payload
     */
    private void append(final ByteString fragment) {
        final long next = bytes + fragment.size();
        if (next < bytes) {
            throw new ProtocolException("WebSocket aggregated message size overflow");
        }
        check(next);
        fragments.write(fragment);
        bytes = next;
    }

    /**
     * Enforces the configured aggregate limit.
     *
     * @param size proposed complete size
     */
    private void check(final long size) {
        if (size > maximumBytes) {
            throw new ProtocolException("WebSocket aggregated message is too large");
        }
    }

    /**
     * Complete assembled data message.
     *
     * @param opcode  initial text or binary opcode
     * @param payload complete payload
     */
    record Message(int opcode, ByteString payload) {
    }

}
