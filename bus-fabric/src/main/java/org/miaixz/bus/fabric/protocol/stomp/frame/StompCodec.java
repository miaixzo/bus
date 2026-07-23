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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Stateful STOMP frame codec supporting content-length and NUL terminators.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompCodec {

    /**
     * Accumulated inbound bytes.
     */
    private final Buffer buffer;

    /**
     * Creates an empty codec.
     */
    public StompCodec() {
        this.buffer = new Buffer();
    }

    /**
     * Encodes a frame.
     *
     * @param frame  STOMP frame or shared heartbeat frame to encode
     * @param output destination buffer receiving the complete wire representation
     */
    public void encode(final StompFrame frame, final Buffer output) {
        final StompFrame currentFrame = require(frame, "STOMP frame");
        final Buffer currentOutput = require(output, "STOMP output");
        if (currentFrame == StompFrame.heartbeat()) {
            currentOutput.writeByte(Symbol.C_LF);
            return;
        }
        writeAscii(currentOutput, currentFrame.command());
        currentOutput.writeByte(Symbol.C_LF);
        for (final Map.Entry<String, List<String>> entry : currentFrame.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                writeAscii(currentOutput, escape(entry.getKey()));
                currentOutput.writeByte(Symbol.C_COLON);
                writeAscii(currentOutput, escape(value));
                currentOutput.writeByte(Symbol.C_LF);
            }
        }
        currentOutput.writeByte(Symbol.C_LF);
        final long declared = contentLength(currentFrame.headers());
        final long bodyLength = currentFrame.body().length();
        if (declared >= Normal._0 && bodyLength >= Normal.LONG_ZERO && declared != bodyLength) {
            throw new ProtocolException("STOMP content-length does not match body length");
        }
        writeBody(currentOutput, currentFrame.body(), declared >= Normal._0 ? declared : bodyLength);
        currentOutput.writeByte(Normal._0);
    }

    /**
     * Decodes zero or more complete frames.
     *
     * @param input newly received bytes consumed into the incremental parser buffer
     * @return decoded frames
     */
    public List<StompFrame> decode(final Buffer input) {
        append(require(input, "STOMP input"));
        final ArrayList<StompFrame> frames = new ArrayList<>();
        int offset = Normal._0;
        while (offset < bufferSize()) {
            if (byteAt(offset) == Symbol.C_LF) {
                frames.add(StompFrame.heartbeat());
                offset++;
                continue;
            }
            final ParseResult result = parse(offset);
            if (result == null) {
                break;
            }
            frames.add(result.frame);
            offset = result.nextOffset;
        }
        if (offset > 0) {
            discard(offset);
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
     * @param input source buffer whose remaining bytes are transferred
     */
    private void append(final Buffer input) {
        buffer.write(input, input.size());
    }

    /**
     * Parses one frame or returns null for a partial frame.
     *
     * @param offset start offset
     * @return parse result or null
     */
    private ParseResult parse(final int offset) {
        final int commandEnd = lineEnd(offset);
        if (commandEnd < Normal._0) {
            return null;
        }
        final String command = ascii(offset, commandEnd);
        validateCommand(command);
        int cursor = commandEnd + Normal._1;
        final Headers.Builder headers = Headers.builder();
        while (true) {
            final int lineEnd = lineEnd(cursor);
            if (lineEnd < Normal._0) {
                return null;
            }
            if (lineEnd == cursor) {
                cursor++;
                break;
            }
            final String line = ascii(cursor, lineEnd);
            final int colon = line.indexOf(Symbol.C_COLON);
            if (colon <= Normal._0) {
                throw new ProtocolException("Invalid STOMP header line");
            }
            headers.add(unescape(line.substring(Normal._0, colon)), unescape(line.substring(colon + Normal._1)));
            cursor = lineEnd + Normal._1;
        }
        final Headers snapshot = headers.build();
        final long declared = contentLength(snapshot);
        final int length = materializableLength(declared);
        final BodyResult body = length >= Normal._0 ? fixedBody(cursor, length) : nulBody(cursor);
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
        final long size = buffer.size();
        final long end = buffer.indexOf((byte) Symbol.C_LF, offset, size);
        final long cr = buffer.indexOf((byte) Symbol.C_CR, offset, end >= Normal.LONG_ZERO ? end : size);
        if (cr >= Normal.LONG_ZERO) {
            throw new ProtocolException("STOMP lines must use LF");
        }
        return toIndex(end);
    }

    /**
     * Reads a fixed-size body.
     *
     * @param cursor start
     * @param length declared content length in bytes
     * @return parsed body and next frame offset, or {@code null} when incomplete
     */
    private BodyResult fixedBody(final int cursor, final int length) {
        final long required = (long) cursor + length + Normal._1;
        if (buffer.size() < required) {
            return null;
        }
        if (byteAt(cursor + length) != Normal._0) {
            throw new ProtocolException("STOMP frame missing NUL terminator");
        }
        return new BodyResult(copy(cursor, length), cursor + length + Normal._1);
    }

    /**
     * Reads a NUL-terminated body.
     *
     * @param cursor start
     * @return parsed body and next frame offset, or {@code null} when no NUL is buffered
     */
    private BodyResult nulBody(final int cursor) {
        final int end = toIndex(buffer.indexOf((byte) Normal._0, cursor));
        if (end >= Normal._0) {
            return new BodyResult(copy(cursor, end - cursor), end + Normal._1);
        }
        return null;
    }

    /**
     * Parses content length.
     *
     * @param headers frame headers containing an optional Content-Length field
     * @return content length or -1
     */
    private static long contentLength(final Headers headers) {
        final List<String> values = headers.values(Http.Header.CONTENT_LENGTH);
        if (values.isEmpty()) {
            return Normal.__1;
        }
        if (values.size() != Normal._1) {
            throw new ProtocolException("STOMP content-length must be unique");
        }
        final String value = values.getFirst();
        if (value.isEmpty()) {
            throw new ProtocolException("Invalid STOMP content-length");
        }
        for (int i = Normal._0; i < value.length(); i++) {
            if (value.charAt(i) < '0' || value.charAt(i) > '9') {
                throw new ProtocolException("Invalid STOMP content-length");
            }
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid STOMP content-length", e);
        }
    }

    /**
     * Converts a declared body length after enforcing the materialization limit.
     *
     * @param length declared length, or -1
     * @return bounded length, or -1
     */
    private static int materializableLength(final long length) {
        if (length < Normal.LONG_ZERO) {
            return Normal.__1;
        }
        if (length > Builder.DEFAULT_MATERIALIZE_MAX_BYTES || length > Integer.MAX_VALUE) {
            throw new ProtocolException("STOMP content-length exceeds the materialization limit");
        }
        return (int) length;
    }

    /**
     * Validates an inbound command without normalizing malformed wire data.
     *
     * @param command inbound command token exactly as received
     */
    private static void validateCommand(final String command) {
        if (StringKit.isBlank(command)) {
            throw new ProtocolException("STOMP command must not be blank");
        }
        for (int i = Normal._0; i < command.length(); i++) {
            final char current = command.charAt(i);
            if (current < 'A' || current > 'Z') {
                throw new ProtocolException("Invalid STOMP command");
            }
        }
    }

    /**
     * Escapes a header component.
     *
     * @param value raw STOMP header name or value
     * @return escaped value
     */
    private static String escape(final String value) {
        final StringBuilder escaped = new StringBuilder(value.length());
        for (int i = Normal._0; i < value.length(); i++) {
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
     * @param value escaped STOMP header name or value
     * @return unescaped value
     */
    private static String unescape(final String value) {
        final StringBuilder unescaped = new StringBuilder(value.length());
        for (int i = Normal._0; i < value.length(); i++) {
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
     * Writes command or header text as UTF-8.
     *
     * @param output destination frame buffer
     * @param value  command or escaped header text
     */
    private static void writeAscii(final Buffer output, final String value) {
        output.writeUtf8(value);
    }

    /**
     * Decodes a buffered command or header range as UTF-8.
     *
     * @param start inclusive buffered byte index
     * @param end   exclusive buffered byte index
     * @return text
     */
    private String ascii(final int start, final int end) {
        return copy(start, end - start).string(StandardCharsets.UTF_8);
    }

    /**
     * Returns one buffered byte.
     *
     * @param index zero-based buffered byte index
     * @return byte
     */
    private byte byteAt(final int index) {
        return buffer.getByte(index);
    }

    /**
     * Copies a buffered byte range.
     *
     * @param offset zero-based buffered byte offset
     * @param length number of bytes to copy
     * @return copied bytes
     */
    private ByteString copy(final int offset, final int length) {
        final Buffer target = new Buffer();
        buffer.copyTo(target, offset, length);
        return target.readByteString();
    }

    /**
     * Writes a payload body.
     *
     * @param output destination frame buffer
     * @param body   body payload
     * @param length body length, or -1 when unknown
     */
    private static void writeBody(final Buffer output, final Payload body, final long length) {
        if (length < Normal.LONG_ZERO) {
            output.write(body.bytes());
            return;
        }
        final Buffer chunk = new Buffer();
        long written = Normal.LONG_ZERO;
        try (Source source = body.source()) {
            while (written < length) {
                final long read = source.read(chunk, Math.min(Normal._8192, length - written));
                if (read < Normal.LONG_ZERO) {
                    throw new ProtocolException("STOMP body ended before content-length");
                }
                if (read == Normal.LONG_ZERO) {
                    continue;
                }
                output.write(chunk, read);
                written += read;
            }
            if (source.read(chunk, Normal._1) >= Normal.LONG_ZERO) {
                throw new ProtocolException("STOMP body exceeds content-length");
            }
        } catch (final IOException e) {
            throw new ProtocolException("Unable to write STOMP body", e);
        }
    }

    /**
     * Discards bytes from the buffered head.
     *
     * @param count byte count
     */
    private void discard(final int count) {
        try {
            buffer.skip(count);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to discard STOMP input", e);
        }
    }

    /**
     * Returns buffered size as an int.
     *
     * @return buffered size
     */
    private int bufferSize() {
        final long size = buffer.size();
        if (size > Integer.MAX_VALUE) {
            throw new ProtocolException("STOMP buffer is too large");
        }
        return (int) size;
    }

    /**
     * Converts a long buffer index to int.
     *
     * @param index long index
     * @return int index
     */
    private int toIndex(final long index) {
        if (index > Integer.MAX_VALUE) {
            throw new ProtocolException("STOMP buffer index is too large");
        }
        return (int) index;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Parsed frame result.
     *
     * @param frame      decoded STOMP frame
     * @param nextOffset first buffered byte after the frame terminator
     */
    private record ParseResult(StompFrame frame, int nextOffset) {

    }

    /**
     * Parsed body result.
     *
     * @param bytes      body bytes
     * @param nextOffset first buffered byte after the body terminator
     */
    private record BodyResult(ByteString bytes, int nextOffset) {

    }

}
