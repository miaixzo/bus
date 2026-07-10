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
package org.miaixz.bus.fabric.protocol.websocket.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.protocol.websocket.WebSocketClose;

/**
 * Streaming WebSocket frame reader.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketReader implements AutoCloseable {

    /**
     * Continuation opcode.
     */
    private static final int CONTINUATION = 0x0;

    /**
     * Text opcode.
     */
    private static final int TEXT = 0x1;

    /**
     * Binary opcode.
     */
    private static final int BINARY = 0x2;

    /**
     * Close opcode.
     */
    private static final int CLOSE = 0x8;

    /**
     * Ping opcode.
     */
    private static final int PING = 0x9;

    /**
     * Pong opcode.
     */
    private static final int PONG = 0xA;

    /**
     * Maximum payload length.
     */
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

    /**
     * Source stream.
     */
    private final InputStream source;

    /**
     * Message address.
     */
    private final Address address;

    /**
     * Expected mask flag.
     */
    private final boolean expectMasked;

    /**
     * Closed flag.
     */
    private boolean closed;

    /**
     * Creates a reader.
     *
     * @param source       source stream
     * @param expectMasked expected mask flag
     */
    public WebSocketReader(final InputStream source, final boolean expectMasked) {
        this(source, expectMasked, Address.parse("ws://localhost"));
    }

    /**
     * Creates a reader.
     *
     * @param source       source stream
     * @param expectMasked expected mask flag
     * @param address      message address
     */
    public WebSocketReader(final InputStream source, final boolean expectMasked, final Address address) {
        if (source == null) {
            throw new ValidateException("WebSocket source must not be null");
        }
        if (address == null) {
            throw new ValidateException("WebSocket address must not be null");
        }
        this.source = source;
        this.expectMasked = expectMasked;
        this.address = address;
    }

    /**
     * Reads the next frame.
     *
     * @return frame
     */
    public WebSocketFrame next() {
        ensureOpen();
        final int first = readByte();
        final int second = readByte();
        if ((first & 0x70) != 0) {
            throw new ProtocolException("WebSocket RSV bits must be zero");
        }
        final boolean fin = (first & 0x80) != 0;
        final int opcode = first & 0x0F;
        final boolean masked = (second & 0x80) != 0;
        long length = second & 0x7F;
        if (masked != expectMasked) {
            throw new ProtocolException("Unexpected WebSocket mask flag");
        }
        if (length == 126) {
            length = readUnsignedShort();
        } else if (length == 127) {
            length = readLong();
            if (length < 0) {
                throw new ProtocolException("Invalid WebSocket payload length");
            }
        }
        if (length > MAX_PAYLOAD) {
            throw new ProtocolException("WebSocket payload is too large");
        }
        final byte[] mask = masked ? readBytes(4) : null;
        final byte[] payload = readBytes((int) length);
        if (mask != null) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ mask[i % 4]);
            }
        }
        final WebSocketFrame frame = new WebSocketFrame(opcode, fin, ByteBuffer.wrap(payload), opcode >= CLOSE);
        validateClose(frame);
        return frame;
    }

    /**
     * Reads frames until close or EOF.
     *
     * @param handler message handler
     */
    public void readLoop(final Handler handler) {
        readLoop(null, handler);
    }

    /**
     * Reads frames until close or EOF.
     *
     * @param session owner session
     * @param handler message handler
     */
    public void readLoop(final Session session, final Handler handler) {
        readLoop(session, handler, NoopControl.INSTANCE);
    }

    /**
     * Reads frames until close or EOF.
     *
     * @param session owner session
     * @param handler message handler
     * @param control control-frame handler
     * @return peer close description, or null when reader was already closed
     */
    public WebSocketClose readLoop(final Session session, final Handler handler, final Control control) {
        if (handler == null) {
            throw new ValidateException("WebSocket handler must not be null");
        }
        final Control events = control == null ? NoopControl.INSTANCE : control;
        ByteArrayOutputStream fragmented = null;
        int fragmentedOpcode = -1;
        try {
            while (!closed) {
                final WebSocketFrame frame = next();
                final int opcode = frame.opcode();
                if (opcode == CLOSE) {
                    final WebSocketClose close = close(frame);
                    events.close(session, close);
                    return close;
                }
                if (opcode == PING) {
                    events.ping(session, frame.payload());
                    continue;
                }
                if (opcode == PONG) {
                    events.pong(session, frame.payload());
                    continue;
                }
                if (opcode == TEXT || opcode == BINARY) {
                    if (fragmented != null) {
                        throw new ProtocolException("WebSocket fragmented message is already open");
                    }
                    if (frame.fin()) {
                        deliver(session, handler, opcode, bytes(frame.payload()));
                    } else {
                        fragmented = new ByteArrayOutputStream();
                        fragmentedOpcode = opcode;
                        fragmented.writeBytes(bytes(frame.payload()));
                    }
                } else if (opcode == CONTINUATION) {
                    if (fragmented == null) {
                        throw new ProtocolException("WebSocket continuation has no initial frame");
                    }
                    fragmented.writeBytes(bytes(frame.payload()));
                    if (frame.fin()) {
                        deliver(session, handler, fragmentedOpcode, fragmented.toByteArray());
                        fragmented = null;
                        fragmentedOpcode = -1;
                    }
                }
            }
            return null;
        } catch (final RuntimeException e) {
            close();
            if (e instanceof ProtocolException || e instanceof SocketException) {
                throw e;
            }
            throw new InternalException("WebSocket read loop failed", e);
        }
    }

    /**
     * Delivers one complete message.
     *
     * @param session owner session
     * @param handler message handler
     * @param opcode  initial opcode
     * @param data    message bytes
     */
    private void deliver(final Session session, final Handler handler, final int opcode, final byte[] data) {
        if (opcode == TEXT) {
            final String text = new String(data, StandardCharsets.UTF_8);
            handler.message(session, message(Payload.of(text, StandardCharsets.UTF_8)));
        } else if (opcode == BINARY) {
            handler.message(session, message(Payload.of(data)));
        }
    }

    /**
     * Closes the reader.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            source.close();
        } catch (final IOException e) {
            throw new SocketException("Unable to close WebSocket reader", e);
        }
    }

    /**
     * Validates a close frame payload.
     *
     * @param frame frame
     */
    private static void validateClose(final WebSocketFrame frame) {
        if (frame.opcode() != CLOSE || frame.payload().remaining() == 0) {
            return;
        }
        final ByteBuffer payload = frame.payload();
        if (payload.remaining() == 1) {
            throw new ProtocolException("Invalid WebSocket close payload");
        }
        final int code = Short.toUnsignedInt(payload.getShort());
        if (!validCloseCode(code)) {
            throw new ProtocolException("Invalid WebSocket close code");
        }
    }

    /**
     * Parses a close frame payload.
     *
     * @param frame close frame
     * @return close description
     */
    private static WebSocketClose close(final WebSocketFrame frame) {
        final ByteBuffer payload = frame.payload();
        if (payload.remaining() == 0) {
            return WebSocketClose.of(1000, null);
        }
        final int code = Short.toUnsignedInt(payload.getShort());
        final byte[] reason = new byte[payload.remaining()];
        payload.get(reason);
        return WebSocketClose.of(code, new String(reason, StandardCharsets.UTF_8));
    }

    /**
     * Returns whether a close code is valid on the wire.
     *
     * @param code code
     * @return true when valid
     */
    private static boolean validCloseCode(final int code) {
        return code == 1000 || code >= 1001 && code <= 1014 && code != 1005 && code != 1006
                || code >= 3000 && code <= 4999;
    }

    /**
     * Builds a fabric message.
     *
     * @param payload payload
     * @return message
     */
    private Message message(final Payload payload) {
        return Message.of(Protocol.WS, address, Headers.empty(), payload, null);
    }

    /**
     * Reads one byte.
     *
     * @return byte value
     */
    private int readByte() {
        try {
            final int value = source.read();
            if (value < 0) {
                throw new SocketException("Unexpected WebSocket EOF");
            }
            return value;
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket frame", e);
        }
    }

    /**
     * Reads an unsigned short.
     *
     * @return value
     */
    private int readUnsignedShort() {
        return (readByte() << 8) | readByte();
    }

    /**
     * Reads a long.
     *
     * @return value
     */
    private long readLong() {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | readByte();
        }
        return value;
    }

    /**
     * Reads bytes.
     *
     * @param length length
     * @return bytes
     */
    private byte[] readBytes(final int length) {
        final byte[] bytes = new byte[length];
        int offset = 0;
        try {
            while (offset < length) {
                final int read = source.read(bytes, offset, length - offset);
                if (read < 0) {
                    throw new SocketException("Unexpected WebSocket EOF");
                }
                offset += read;
            }
            return bytes;
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket payload", e);
        }
    }

    /**
     * Copies buffer bytes.
     *
     * @param buffer buffer
     * @return bytes
     */
    private static byte[] bytes(final ByteBuffer buffer) {
        final ByteBuffer duplicate = buffer.duplicate();
        final byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return bytes;
    }

    /**
     * Ensures the reader is open.
     */
    private void ensureOpen() {
        if (closed) {
            throw new SocketException("WebSocket reader is closed");
        }
    }

    /**
     * Control-frame callback contract.
     */
    public interface Control {

        /**
         * Handles a ping frame.
         *
         * @param session owner session
         * @param payload ping payload
         */
        default void ping(final Session session, final ByteBuffer payload) {
            // Default control handler intentionally ignores ping frames.
        }

        /**
         * Handles a pong frame.
         *
         * @param session owner session
         * @param payload pong payload
         */
        default void pong(final Session session, final ByteBuffer payload) {
            // Default control handler intentionally ignores pong frames.
        }

        /**
         * Handles a close frame.
         *
         * @param session owner session
         * @param close   peer close description
         */
        default void close(final Session session, final WebSocketClose close) {
            // Default control handler intentionally ignores close frames.
        }

    }

    /**
     * No-op control handler.
     */
    private enum NoopControl implements Control {

        /**
         * Singleton instance.
         */
        INSTANCE

    }

}
