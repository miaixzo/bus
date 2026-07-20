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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.network.Connection;

/**
 * Stateless HTTP/2 network-byte to frame parser.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Reader {

    /**
     * HTTP/2 client connection preface bytes.
     */
    private static final byte[] CONNECTION_PREFACE = { 'P', 'R', 'I', Symbol.C_SPACE, Symbol.C_STAR, Symbol.C_SPACE,
            'H', 'T', 'T', 'P', Symbol.C_SLASH, Symbol.C_TWO, Symbol.C_DOT, Symbol.C_ZERO, Symbol.C_CR, Symbol.C_LF,
            Symbol.C_CR, Symbol.C_LF, 'S', 'M', Symbol.C_CR, Symbol.C_LF, Symbol.C_CR, Symbol.C_LF };

    /**
     * Buffered network source borrowed from the owning connection.
     */
    private final BufferSource source;

    /**
     * Connection-owned HPACK decoder callback.
     */
    private final Function<Buffer, List<Http2Header>> headerDecoder;

    /**
     * True after the connection preface is parsed.
     */
    private boolean prefaceRead;

    /**
     * Creates a parser borrowing network and HPACK capabilities from one connection.
     *
     * @param connection owning connection
     */
    public Http2Reader(final Http2Connection connection) {
        final Http2Connection owner = require(connection, "HTTP/2 connection");
        this.source = IoKit.buffer(owner.network().source());
        this.headerDecoder = owner::decodeHeaders;
    }

    /**
     * Creates a parser over a network connection with an explicit connection-owned decoder.
     *
     * @param connection network connection
     * @param decoder    HPACK decoder callback
     */
    Http2Reader(final Connection connection, final Function<Buffer, List<Http2Header>> decoder) {
        final Connection checked = require(connection, "Network connection");
        this.source = IoKit.buffer(checked.source());
        this.headerDecoder = require(decoder, "HTTP/2 header decoder");
    }

    /**
     * Reads the next recognized complete frame, skipping unknown extensions by their declared length.
     *
     * @return parsed frame
     */
    public Http2Frame nextFrame() {
        while (true) {
            final FrameHeader header = readHeader();
            if (!recognized(header.type())) {
                readFully(header.length());
                continue;
            }
            validateFrame(header);
            ByteString payload = readFully(header.length()).readByteString();
            if (header.type() == Normal._0 || header.type() == Normal._1 || header.type() == Normal._5) {
                payload = removePadding(header.type(), header.flags(), payload);
            }
            Http2Priority priority = null;
            Http2AlternateService alternateService = null;
            final List<Http2Header> headers = switch (header.type()) {
                case Normal._1 -> {
                    priority = decodeHeaderPriority(header.streamId(), header.flags(), payload);
                    final ByteString block = headerBlock(
                            header.streamId(),
                            header.flags(),
                            headerFragment(header.flags(), payload));
                    yield headerDecoder.apply(new Buffer().write(block));
                }
                case Normal._5 -> {
                    final ByteString block = headerBlock(
                            header.streamId(),
                            header.flags(),
                            pushHeaderFragment(payload));
                    yield headerDecoder.apply(new Buffer().write(block));
                }
                case Normal._2 -> {
                    priority = Http2Priority.decode(payload, header.streamId());
                    yield List.of();
                }
                case Normal._10 -> {
                    alternateService = Http2AlternateService.decode(payload, header.streamId());
                    yield List.of();
                }
                default -> List.of();
            };
            final int flags = header.type() == Normal._1 || header.type() == Normal._5 ? header.flags() | Normal._4
                    : header.flags();
            return Http2Frame
                    .decoded(header.type(), header.streamId(), flags, payload, headers, priority, alternateService);
        }
    }

    /**
     * Parses the fixed client connection preface once.
     */
    public void readConnectionPreface() {
        if (prefaceRead) {
            throw new StatefulException("HTTP/2 connection preface has already been read");
        }
        final Buffer actual = readFully(CONNECTION_PREFACE.length);
        for (final byte expected : CONNECTION_PREFACE) {
            if (actual.readByte() != expected) {
                throw new ProtocolException("Invalid HTTP/2 connection preface");
            }
        }
        prefaceRead = true;
    }

    /**
     * Reads one frame header.
     *
     * @return header value
     */
    private FrameHeader readHeader() {
        final Buffer header = readFully(Normal._9);
        final int length = readMedium(header);
        final int type = header.readByte() & Builder.UNSIGNED_BYTE_MASK;
        final int flags = header.readByte() & Builder.UNSIGNED_BYTE_MASK;
        final int streamId = header.readInt() & Integer.MAX_VALUE;
        if (length > Normal._16384) {
            throw new ProtocolException("HTTP/2 frame exceeds the local maximum frame size");
        }
        return new FrameHeader(length, type, flags, streamId);
    }

    /**
     * Reads an exact byte count without taking ownership of the source.
     *
     * @param length byte count
     * @return buffer
     */
    private Buffer readFully(final int length) {
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            final long remaining = Math.min(length - buffer.size(), Normal._16384);
            final long read;
            try {
                read = source.read(buffer, remaining);
            } catch (final IOException e) {
                throw new SocketException("HTTP/2 frame read failed", e);
            }
            if (read < Normal.LONG_ZERO) {
                throw new SocketException("HTTP/2 frame is truncated");
            }
        }
        return buffer;
    }

    /**
     * Validates metadata for every recognized standard frame.
     *
     * @param header frame header
     */
    private static void validateFrame(final FrameHeader header) {
        final int type = header.type();
        final int streamId = header.streamId();
        final int flags = header.flags();
        final int length = header.length();
        switch (type) {
            case Normal._4 -> {
                if (streamId != Normal._0 || (flags & ~Normal._1) != Normal._0
                        || ((flags & Normal._1) != Normal._0 && length != Normal._0)
                        || length % Normal._6 != Normal._0) {
                    throw new ProtocolException("Invalid HTTP/2 SETTINGS frame");
                }
            }
            case Normal._6 -> {
                if (streamId != Normal._0 || (flags & ~Normal._1) != Normal._0 || length != Normal._8) {
                    throw new ProtocolException("Invalid HTTP/2 PING frame");
                }
            }
            case Normal._7 -> {
                if (streamId != Normal._0 || flags != Normal._0 || length < Normal._8) {
                    throw new ProtocolException("Invalid HTTP/2 GOAWAY frame");
                }
            }
            case Normal._8 -> {
                if (flags != Normal._0 || length != Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 WINDOW_UPDATE frame");
                }
            }
            case Normal._10 -> {
                if (flags != Normal._0 || length < Normal._2) {
                    throw new ProtocolException("Invalid HTTP/2 ALTSVC frame");
                }
            }
            default -> validateStreamFrame(type, streamId, flags, length);
        }
    }

    /**
     * Validates recognized stream-scoped frame metadata.
     *
     * @param type     frame type
     * @param streamId stream id
     * @param flags    frame flags
     * @param length   payload length
     */
    private static void validateStreamFrame(final int type, final int streamId, final int flags, final int length) {
        if (streamId <= Normal._0) {
            throw new ProtocolException("HTTP/2 stream frame id must be positive");
        }
        switch (type) {
            case Normal._0 -> validateFlags(flags, Normal._1 | Normal._8);
            case Normal._1 -> {
                validateFlags(flags, Normal._1 | Normal._4 | Normal._8 | Normal._32);
                final int minimum = ((flags & Normal._8) != Normal._0 ? Normal._1 : Normal._0)
                        + ((flags & Normal._32) != Normal._0 ? Normal._5 : Normal._0);
                if (length < minimum) {
                    throw new ProtocolException("Invalid HTTP/2 HEADERS payload length");
                }
            }
            case Normal._2 -> {
                validateFlags(flags, Normal._0);
                if (length != Normal._5) {
                    throw new ProtocolException("Invalid HTTP/2 PRIORITY frame");
                }
            }
            case Normal._3 -> {
                validateFlags(flags, Normal._0);
                if (length != Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 RST_STREAM frame");
                }
            }
            case Normal._5 -> {
                validateFlags(flags, Normal._4 | Normal._8);
                final int minimum = Normal._4 + ((flags & Normal._8) != Normal._0 ? Normal._1 : Normal._0);
                if (length < minimum) {
                    throw new ProtocolException("Invalid HTTP/2 PUSH_PROMISE payload length");
                }
            }
            case Normal._9 -> throw new ProtocolException("Unexpected HTTP/2 CONTINUATION frame");
            default -> throw new ProtocolException("Unsupported HTTP/2 frame type");
        }
    }

    /**
     * Removes a PADDED prefix and trailing padding with strict bounds validation.
     *
     * @param type    frame type
     * @param flags   frame flags
     * @param payload payload
     * @return unpadded payload
     */
    private static ByteString removePadding(final int type, final int flags, final ByteString payload) {
        if ((flags & Normal._8) == Normal._0) {
            return payload;
        }
        if (payload.size() == Normal._0) {
            throw new ProtocolException("HTTP/2 padded frame is missing pad length");
        }
        final int padding = payload.getByte(Normal._0) & Builder.UNSIGNED_BYTE_MASK;
        final int required = Normal._1 + padding
                + (type == Normal._1 && (flags & Normal._32) != Normal._0 ? Normal._5 : Normal._0)
                + (type == Normal._5 ? Normal._4 : Normal._0);
        if (required > payload.size()) {
            throw new ProtocolException("Invalid HTTP/2 frame padding");
        }
        return payload.substring(Normal._1, payload.size() - padding);
    }

    /**
     * Reads all contiguous CONTINUATION fragments for one header block.
     *
     * @param streamId stream id
     * @param flags    initial flags
     * @param first    first fragment
     * @return complete encoded block
     */
    private ByteString headerBlock(final int streamId, final int flags, final ByteString first) {
        final Buffer fragments = new Buffer();
        int total = appendHeaderFragment(fragments, first, Normal._0);
        int currentFlags = flags;
        while ((currentFlags & Normal._4) == Normal._0) {
            final FrameHeader continuation = readHeader();
            if (continuation.type() != Normal._9 || continuation.streamId() != streamId) {
                throw new ProtocolException("HTTP/2 header block requires contiguous CONTINUATION frames");
            }
            validateFlags(continuation.flags(), Normal._4);
            currentFlags = continuation.flags();
            total = appendHeaderFragment(fragments, readFully(continuation.length()).readByteString(), total);
        }
        return fragments.readByteString();
    }

    /**
     * Appends one encoded header fragment with a deterministic size limit.
     *
     * @param fragments target
     * @param fragment  fragment
     * @param total     current total
     * @return new total
     */
    private static int appendHeaderFragment(final Buffer fragments, final ByteString fragment, final int total) {
        final int next = total + fragment.size();
        if (next < total || next > Builder.BYTES_64_KIB) {
            throw new ProtocolException("HTTP/2 header block exceeds the configured limit");
        }
        fragments.write(fragment);
        return next;
    }

    /**
     * Decodes HEADERS priority metadata after an optional pad-length prefix was removed.
     *
     * @param streamId stream id
     * @param flags    flags
     * @param payload  unpadded payload
     * @return priority or null
     */
    private static Http2Priority decodeHeaderPriority(final int streamId, final int flags, final ByteString payload) {
        return (flags & Normal._32) == Normal._0 ? null : Http2Priority.decode(payload, streamId);
    }

    /**
     * Returns the HEADERS block fragment after optional priority metadata.
     *
     * @param flags   flags
     * @param payload unpadded payload
     * @return block fragment
     */
    private static ByteString headerFragment(final int flags, final ByteString payload) {
        return (flags & Normal._32) == Normal._0 ? payload : payload.substring(Normal._5);
    }

    /**
     * Returns the PUSH_PROMISE block fragment after the promised stream id.
     *
     * @param payload unpadded payload
     * @return block fragment
     */
    private static ByteString pushHeaderFragment(final ByteString payload) {
        return payload.substring(Normal._4);
    }

    /**
     * Returns whether a frame type is recognized by this implementation.
     *
     * @param type frame type
     * @return true for standard and registered supported types
     */
    private static boolean recognized(final int type) {
        return type >= Normal._0 && type <= Normal._10;
    }

    /**
     * Validates a frame flag mask.
     *
     * @param flags   flags
     * @param allowed allowed bits
     */
    private static void validateFlags(final int flags, final int allowed) {
        if ((flags & ~allowed) != Normal._0) {
            throw new ProtocolException("Unsupported HTTP/2 frame flags");
        }
    }

    /**
     * Reads an unsigned 24-bit integer.
     *
     * @param buffer source buffer
     * @return value
     */
    private static int readMedium(final Buffer buffer) {
        return ((buffer.readByte() & Builder.UNSIGNED_BYTE_MASK) << Normal._16)
                | ((buffer.readByte() & Builder.UNSIGNED_BYTE_MASK) << Normal._8)
                | (buffer.readByte() & Builder.UNSIGNED_BYTE_MASK);
    }

    /**
     * Validates a required reference.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Immutable parsed frame header.
     *
     * @param length   payload length
     * @param type     frame type
     * @param flags    frame flags
     * @param streamId stream id
     */
    private record FrameHeader(int length, int type, int flags, int streamId) {

    }

}
