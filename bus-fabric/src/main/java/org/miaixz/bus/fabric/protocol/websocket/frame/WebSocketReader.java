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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
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
     * Source bytes.
     */
    private final Source source;

    /**
     * Reusable input buffer.
     */
    private final Buffer input;

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
     * @param source       source
     * @param expectMasked expected mask flag
     */
    public WebSocketReader(final Source source, final boolean expectMasked) {
        this(source, expectMasked, Address.parse(Builder.WEB_SOCKET_READER_DEFAULT_ADDRESS));
    }

    /**
     * Creates a reader.
     *
     * @param source       source
     * @param expectMasked expected mask flag
     * @param address      message address
     */
    public WebSocketReader(final Source source, final boolean expectMasked, final Address address) {
        this.source = require(source, "WebSocket source");
        this.input = new Buffer();
        this.expectMasked = expectMasked;
        this.address = require(address, "WebSocket address");
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
        if ((first & Builder.WEBSOCKET_RSV_MASK) != Normal._0) {
            throw new ProtocolException("WebSocket RSV bits must be zero");
        }
        final boolean fin = (first & Normal._128) != Normal._0;
        final int opcode = first & Builder.WEBSOCKET_OPCODE_MASK;
        final boolean masked = (second & Normal._128) != Normal._0;
        long length = second & Builder._127;
        if (masked != expectMasked) {
            throw new ProtocolException("Unexpected WebSocket mask flag");
        }
        if (length == Builder.WEBSOCKET_LENGTH_16_MARKER) {
            length = readUnsignedShort();
        } else if (length == Builder._127) {
            length = readLong();
            if (length < Normal.LONG_ZERO) {
                throw new ProtocolException("Invalid WebSocket payload length");
            }
        }
        if (length > Builder.BYTES_16_MIB) {
            throw new ProtocolException("WebSocket payload is too large");
        }
        final byte[] mask = masked ? readBytes(Normal._4) : null;
        final Buffer payload = readBuffer((int) length);
        if (mask != null) {
            unmask(payload, mask, length);
        }
        final WebSocketFrame frame = new WebSocketFrame(opcode, fin, payload.readByteString(), opcode >= Normal._8);
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
        require(handler, "WebSocket handler");
        final Control events = control == null ? NoopControl.INSTANCE : control;
        Buffer fragmented = null;
        int fragmentedOpcode = Normal.__1;
        try {
            while (!closed) {
                final WebSocketFrame frame = next();
                final int opcode = frame.opcode();
                if (opcode == Normal._8) {
                    final WebSocketClose close = close(frame);
                    events.close(session, close);
                    return close;
                }
                if (opcode == Builder.WEBSOCKET_OPCODE_PING) {
                    events.ping(session, frame.payload());
                    continue;
                }
                if (opcode == Builder.WEBSOCKET_OPCODE_PONG) {
                    events.pong(session, frame.payload());
                    continue;
                }
                if (opcode == Normal._1 || opcode == Builder.WEBSOCKET_OPCODE_BINARY) {
                    if (fragmented != null) {
                        throw new ProtocolException("WebSocket fragmented message is already open");
                    }
                    if (frame.fin()) {
                        deliver(session, handler, opcode, frame.payload());
                    } else {
                        fragmented = new Buffer();
                        fragmentedOpcode = opcode;
                        fragmented.write(frame.payload());
                    }
                } else if (opcode == Normal._0) {
                    if (fragmented == null) {
                        throw new ProtocolException("WebSocket continuation has no initial frame");
                    }
                    fragmented.write(frame.payload());
                    if (frame.fin()) {
                        deliver(session, handler, fragmentedOpcode, fragmented.readByteString());
                        fragmented = null;
                        fragmentedOpcode = Normal.__1;
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
    private void deliver(final Session session, final Handler handler, final int opcode, final ByteString data) {
        if (opcode == Normal._1) {
            final String text = data.string(StandardCharsets.UTF_8);
            handler.message(session, message(Payload.of(text, StandardCharsets.UTF_8)));
        } else if (opcode == Builder.WEBSOCKET_OPCODE_BINARY) {
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
        if (frame.opcode() != Normal._8 || frame.payload().size() == Normal._0) {
            return;
        }
        final ByteString payload = frame.payload();
        if (payload.size() == Normal._1) {
            throw new ProtocolException("Invalid WebSocket close payload");
        }
        final int code = Short.toUnsignedInt(new Buffer().write(payload).readShort());
        try {
            WebSocketClose.of(code, Normal.EMPTY);
        } catch (final ValidateException e) {
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
        final ByteString payload = frame.payload();
        if (payload.size() == Normal._0) {
            return WebSocketClose.of(Builder._1000, null);
        }
        final Buffer buffer = new Buffer().write(payload);
        final int code = Short.toUnsignedInt(buffer.readShort());
        return WebSocketClose.of(code, buffer.readByteString().string(StandardCharsets.UTF_8));
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
            requireBytes(Byte.BYTES);
            return input.readByte() & Builder.UNSIGNED_BYTE_MASK;
        } catch (final RuntimeException e) {
            if (e instanceof SocketException) {
                throw e;
            }
            throw new SocketException("Unable to read WebSocket frame", e);
        }
    }

    /**
     * Reads an unsigned short.
     *
     * @return value
     */
    private int readUnsignedShort() {
        try {
            requireBytes(Short.BYTES);
            return Short.toUnsignedInt(input.readShort());
        } catch (final RuntimeException e) {
            if (e instanceof SocketException) {
                throw e;
            }
            throw new SocketException("Unable to read WebSocket frame", e);
        }
    }

    /**
     * Reads a long.
     *
     * @return value
     */
    private long readLong() {
        try {
            requireBytes(Long.BYTES);
            return input.readLong();
        } catch (final RuntimeException e) {
            if (e instanceof SocketException) {
                throw e;
            }
            throw new SocketException("Unable to read WebSocket frame", e);
        }
    }

    /**
     * Reads bytes.
     *
     * @param length length
     * @return bytes
     */
    private byte[] readBytes(final int length) {
        try {
            requireBytes(length);
            return input.readByteArray(length);
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket payload", e);
        }
    }

    /**
     * Reads bytes into a core buffer.
     *
     * @param length byte count
     * @return buffer
     */
    private Buffer readBuffer(final int length) {
        final Buffer buffer = new Buffer();
        try {
            requireBytes(length);
            buffer.write(input, length);
            return buffer;
        } catch (final RuntimeException e) {
            if (e instanceof SocketException) {
                throw e;
            }
            throw new SocketException("Unable to read WebSocket payload", e);
        }
    }

    /**
     * Ensures the reusable input buffer contains the requested byte count.
     *
     * @param byteCount required byte count
     */
    private void requireBytes(final long byteCount) {
        try {
            while (input.size() < byteCount) {
                final long read = source.read(input, byteCount - input.size());
                if (read < Normal.LONG_ZERO) {
                    throw new SocketException("Unexpected WebSocket EOF");
                }
            }
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket payload", e);
        }
    }

    /**
     * Applies the WebSocket mask to a payload buffer.
     *
     * @param payload payload
     * @param mask    mask key
     * @param length  payload length
     */
    private static void unmask(final Buffer payload, final byte[] mask, final long length) {
        if (length == Normal.LONG_ZERO) {
            return;
        }
        final Buffer.UnsafeCursor cursor = new Buffer.UnsafeCursor();
        payload.readAndWriteUnsafe(cursor);
        try {
            long processed = Normal.LONG_ZERO;
            int available = cursor.seek(Normal.LONG_ZERO);
            while (available != Normal.__1 && processed < length) {
                for (int i = cursor.start; i < cursor.end && processed < length; i++) {
                    cursor.data[i] = (byte) (cursor.data[i] ^ mask[(int) (processed & Normal._3)]);
                    processed++;
                }
                if (processed < length) {
                    available = cursor.next();
                }
            }
        } finally {
            cursor.close();
        }
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
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
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
        default void ping(final Session session, final ByteString payload) {
            // Default control handler intentionally ignores ping frames.
        }

        /**
         * Handles a pong frame.
         *
         * @param session owner session
         * @param payload pong payload
         */
        default void pong(final Session session, final ByteString payload) {
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
