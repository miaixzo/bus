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
package org.miaixz.bus.fabric.protocol.stomp.frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.stream.SegmentedBuffer;

/**
 * Stateful STOMP frame codec supporting content-length and NUL terminators.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompCodec {

    /**
     * Frame terminator.
     */
    private static final byte NUL = 0;

    /**
     * Accumulated inbound bytes.
     */
    private final SegmentedBuffer buffer;

    /**
     * Creates an empty codec.
     */
    public StompCodec() {
        this.buffer = SegmentedBuffer.create();
    }

    /**
     * Encodes a frame.
     *
     * @param frame frame
     * @return encoded buffer
     */
    public ByteBuffer encode(final StompFrame frame) {
        if (frame == null) {
            throw new ValidateException("STOMP frame must not be null");
        }
        final Buffer output = new Buffer();
        writeAscii(output, frame.command());
        output.writeByte(Symbol.C_LF);
        for (final Map.Entry<String, List<String>> entry : frame.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                writeAscii(output, escape(entry.getKey()));
                output.writeByte(Symbol.C_COLON);
                writeAscii(output, escape(value));
                output.writeByte(Symbol.C_LF);
            }
        }
        output.writeByte(Symbol.C_LF);
        final byte[] body = frame.body().bytes();
        final int declared = contentLength(frame.headers());
        if (declared >= 0 && declared != body.length) {
            throw new ProtocolException("STOMP content-length does not match body length");
        }
        output.write(body);
        output.writeByte(NUL);
        return ByteBuffer.wrap(output.readByteArray()).asReadOnlyBuffer();
    }

    /**
     * Decodes zero or more complete frames.
     *
     * @param input input bytes
     * @return decoded frames
     */
    public List<StompFrame> decode(final ByteBuffer input) {
        if (input == null) {
            throw new ValidateException("STOMP input must not be null");
        }
        append(input);
        final ArrayList<StompFrame> frames = new ArrayList<>();
        int offset = 0;
        while (offset < buffer.size()) {
            while (offset < buffer.size() && buffer.get(offset) == Symbol.C_LF) {
                offset++;
            }
            final ParseResult result = parse(offset);
            if (result == null) {
                break;
            }
            frames.add(result.frame);
            offset = result.nextOffset;
        }
        if (offset > 0) {
            buffer.discard(offset);
        }
        return List.copyOf(frames);
    }

    /**
     * Resets the inbound parser state.
     */
    public void reset() {
        buffer.clear();
    }

    /**
     * Appends inbound bytes.
     *
     * @param input input
     */
    private void append(final ByteBuffer input) {
        buffer.append(input);
    }

    /**
     * Parses one frame or returns null for a partial frame.
     *
     * @param offset start offset
     * @return parse result or null
     */
    private ParseResult parse(final int offset) {
        final int commandEnd = lineEnd(offset);
        if (commandEnd < 0) {
            return null;
        }
        final String command = ascii(offset, commandEnd);
        if (command.isBlank()) {
            throw new ProtocolException("STOMP command must not be blank");
        }
        int cursor = commandEnd + 1;
        final Headers.Builder headers = Headers.builder();
        while (true) {
            final int lineEnd = lineEnd(cursor);
            if (lineEnd < 0) {
                return null;
            }
            if (lineEnd == cursor) {
                cursor++;
                break;
            }
            final String line = ascii(cursor, lineEnd);
            final int colon = line.indexOf(Symbol.C_COLON);
            if (colon <= 0) {
                throw new ProtocolException("Invalid STOMP header line");
            }
            headers.add(unescape(line.substring(0, colon)), unescape(line.substring(colon + 1)));
            cursor = lineEnd + 1;
        }
        final Headers snapshot = headers.build();
        final int length = contentLength(snapshot);
        final BodyResult body = length >= 0 ? fixedBody(cursor, length) : nulBody(cursor);
        if (body == null) {
            return null;
        }
        return new ParseResult(StompFrame.of(command, snapshot, Payload.of(body.bytes)), body.nextOffset);
    }

    /**
     * Finds a line ending.
     *
     * @param offset start offset
     * @return line ending or -1
     */
    private int lineEnd(final int offset) {
        for (int i = offset; i < buffer.size(); i++) {
            final byte value = buffer.get(i);
            if (value == Symbol.C_LF) {
                return i;
            }
            if (value == Symbol.C_CR) {
                throw new ProtocolException("STOMP lines must use LF");
            }
        }
        return -1;
    }

    /**
     * Reads a fixed-size body.
     *
     * @param cursor start
     * @param length length
     * @return body or null
     */
    private BodyResult fixedBody(final int cursor, final int length) {
        if (buffer.size() < cursor + length + 1) {
            return null;
        }
        if (buffer.get(cursor + length) != NUL) {
            throw new ProtocolException("STOMP frame missing NUL terminator");
        }
        return new BodyResult(buffer.copy(cursor, length), cursor + length + 1);
    }

    /**
     * Reads a NUL-terminated body.
     *
     * @param cursor start
     * @return body or null
     */
    private BodyResult nulBody(final int cursor) {
        for (int i = cursor; i < buffer.size(); i++) {
            if (buffer.get(i) == NUL) {
                return new BodyResult(buffer.copy(cursor, i - cursor), i + 1);
            }
        }
        return null;
    }

    /**
     * Parses content length.
     *
     * @param headers headers
     * @return content length or -1
     */
    private static int contentLength(final Headers headers) {
        final String value = headers.get("content-length");
        if (value == null) {
            return -1;
        }
        try {
            final int length = Integer.parseInt(value);
            if (length < 0) {
                throw new ProtocolException("STOMP content-length must be non-negative");
            }
            return length;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid STOMP content-length", e);
        }
    }

    /**
     * Escapes a header component.
     *
     * @param value value
     * @return escaped value
     */
    private static String escape(final String value) {
        final StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            switch (current) {
                case Symbol.C_BACKSLASH -> escaped.append("\\\\");
                case Symbol.C_CR -> escaped.append("\\r");
                case Symbol.C_LF -> escaped.append("\\n");
                case Symbol.C_COLON -> escaped.append("\\c");
                default -> escaped.append(current);
            }
        }
        return escaped.toString();
    }

    /**
     * Unescapes a header component.
     *
     * @param value value
     * @return unescaped value
     */
    private static String unescape(final String value) {
        final StringBuilder unescaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            final char current = value.charAt(i);
            if (current != Symbol.C_BACKSLASH) {
                unescaped.append(current);
                continue;
            }
            if (++i >= value.length()) {
                throw new ProtocolException("Invalid STOMP header escape");
            }
            switch (value.charAt(i)) {
                case Symbol.C_BACKSLASH -> unescaped.append(Symbol.C_BACKSLASH);
                case 'r' -> unescaped.append(Symbol.C_CR);
                case 'n' -> unescaped.append(Symbol.C_LF);
                case 'c' -> unescaped.append(Symbol.C_COLON);
                default -> throw new ProtocolException("Invalid STOMP header escape");
            }
        }
        return unescaped.toString();
    }

    /**
     * Writes ASCII text.
     *
     * @param output output
     * @param value  value
     */
    private static void writeAscii(final Buffer output, final String value) {
        output.write(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads ASCII text.
     *
     * @param start start
     * @param end   end
     * @return text
     */
    private String ascii(final int start, final int end) {
        return new String(buffer.copy(start, end - start), StandardCharsets.UTF_8);
    }

    /**
     * Parsed frame result.
     *
     * @param frame      frame
     * @param nextOffset next offset
     */
    private record ParseResult(StompFrame frame, int nextOffset) {

    }

    /**
     * Parsed body result.
     *
     * @param bytes      body bytes
     * @param nextOffset next offset
     */
    private record BodyResult(byte[] bytes, int nextOffset) {

    }

}
