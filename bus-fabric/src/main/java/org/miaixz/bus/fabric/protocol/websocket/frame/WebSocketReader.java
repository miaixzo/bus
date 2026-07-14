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
import java.io.InputStream;
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
import org.miaixz.bus.core.xyz.IoKit;
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
     * Default address used by compatibility constructors.
     */
    private static final String DEFAULT_ADDRESS = Protocol.WS_PREFIX + "localhost";

    /**
     * RSV bit mask.
     */
    private static final int RSV_MASK = 0x70;

    /**
     * FIN bit mask.
     */
    private static final int FIN_MASK = 0x80;

    /**
     * Opcode bit mask.
     */
    private static final int OPCODE_MASK = 0x0F;

    /**
     * Payload mask flag.
     */
    private static final int MASK_FLAG = 0x80;

    /**
     * Payload length bit mask.
     */
    private static final int LENGTH_MASK = 0x7F;

    /**
     * Marker for unsigned 16-bit payload length.
     */
    private static final int LENGTH_16_MARKER = 126;

    /**
     * Marker for unsigned 64-bit payload length.
     */
    private static final int LENGTH_64_MARKER = 127;

    /**
     * WebSocket mask key byte length.
     */
    private static final int MASK_KEY_BYTES = 4;

    /**
     * Last mask key index for modulo arithmetic.
     */
    private static final long MASK_INDEX = 3L;

    /**
     * Default normal close code used when no close payload is present.
     */
    private static final int DEFAULT_CLOSE_CODE = 1000;

    /**
     * Initial fragmented opcode marker.
     */
    private static final int NO_FRAGMENT_OPCODE = -1;

    /**
     * Cursor value returned when no segment is available.
     */
    private static final int NO_CURSOR_SEGMENT = -1;

    /**
     * Maximum message payload bytes accepted by this implementation.
     */
    private static final long MAX_PAYLOAD_BYTES = Normal._16 * Normal.MEBI;

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
        this(source, expectMasked, Address.parse(DEFAULT_ADDRESS));
    }

    /**
     * Creates a compatibility reader.
     *
     * @param source       source stream
     * @param expectMasked expected mask flag
     * @deprecated use {@link #WebSocketReader(Source, boolean)}
     */
    @Deprecated(since = "8.8.3")
    public WebSocketReader(final InputStream source, final boolean expectMasked) {
        this(IoKit.source(require(source, "WebSocket source")), expectMasked, Address.parse(DEFAULT_ADDRESS));
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
     * Creates a compatibility reader.
     *
     * @param source       source stream
     * @param expectMasked expected mask flag
     * @param address      message address
     * @deprecated use {@link #WebSocketReader(Source, boolean, Address)}
     */
    @Deprecated(since = "8.8.3")
    public WebSocketReader(final InputStream source, final boolean expectMasked, final Address address) {
        this(IoKit.source(require(source, "WebSocket source")), expectMasked, address);
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
        if ((first & RSV_MASK) != Normal._0) {
            throw new ProtocolException("WebSocket RSV bits must be zero");
        }
        final boolean fin = (first & FIN_MASK) != Normal._0;
        final int opcode = first & OPCODE_MASK;
        final boolean masked = (second & MASK_FLAG) != Normal._0;
        long length = second & LENGTH_MASK;
        if (masked != expectMasked) {
            throw new ProtocolException("Unexpected WebSocket mask flag");
        }
        if (length == LENGTH_16_MARKER) {
            length = readUnsignedShort();
        } else if (length == LENGTH_64_MARKER) {
            length = readLong();
            if (length < Normal.LONG_ZERO) {
                throw new ProtocolException("Invalid WebSocket payload length");
            }
        }
        if (length > MAX_PAYLOAD_BYTES) {
            throw new ProtocolException("WebSocket payload is too large");
        }
        final byte[] mask = masked ? readBytes(MASK_KEY_BYTES) : null;
        final Buffer payload = readBuffer((int) length);
        if (mask != null) {
            unmask(payload, mask, length);
        }
        final WebSocketFrame frame = new WebSocketFrame(opcode, fin, payload.readByteString(), opcode >= CLOSE);
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
        int fragmentedOpcode = NO_FRAGMENT_OPCODE;
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
                        deliver(session, handler, opcode, frame.payload());
                    } else {
                        fragmented = new Buffer();
                        fragmentedOpcode = opcode;
                        fragmented.write(frame.payload());
                    }
                } else if (opcode == CONTINUATION) {
                    if (fragmented == null) {
                        throw new ProtocolException("WebSocket continuation has no initial frame");
                    }
                    fragmented.write(frame.payload());
                    if (frame.fin()) {
                        deliver(session, handler, fragmentedOpcode, fragmented.readByteString());
                        fragmented = null;
                        fragmentedOpcode = NO_FRAGMENT_OPCODE;
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
        if (opcode == TEXT) {
            final String text = data.string(StandardCharsets.UTF_8);
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
        if (frame.opcode() != CLOSE || frame.payload().size() == Normal._0) {
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
            return WebSocketClose.of(DEFAULT_CLOSE_CODE, null);
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
            return input.readByte() & 0xff;
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
            while (available != NO_CURSOR_SEGMENT && processed < length) {
                for (int i = cursor.start; i < cursor.end && processed < length; i++) {
                    cursor.data[i] = (byte) (cursor.data[i] ^ mask[(int) (processed & MASK_INDEX)]);
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
