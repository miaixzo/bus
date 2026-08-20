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
package org.miaixz.bus.auth.protocol.ldap.codec;

import java.io.EOFException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.codec.frame.Frame;
import org.miaixz.bus.fabric.codec.frame.FrameCodec;

/**
 * Frames LDAPv3 messages on a Fabric byte stream and supplies restricted BER primitives to LDAP message codecs.
 * <p>
 * LDAP uses the definite-length Basic Encoding Rules rather than a separate transport length prefix. This codec
 * therefore retains incoming bytes until one complete top-level LDAPMessage SEQUENCE is available and emits the
 * complete TLV as one Fabric frame. The package-private reader and writer deliberately implement only the BER forms
 * required by RFC 4511; protocol-operation mapping remains in {@link LdapMessageDecoder} and
 * {@link LdapMessageEncoder}.
 * </p>
 *
 * @author Kimi Liu
 */
public final class BerCodec implements FrameCodec {

    /**
     * Universal constructed SEQUENCE tag required at the LDAPMessage boundary.
     */
    static final int SEQUENCE_TAG = 0x30;

    /**
     * BER indefinite-length marker forbidden by the LDAP transfer profile.
     */
    private static final int INDEFINITE_LENGTH = 0x80;

    /**
     * Maximum length-octet count representable by a signed Java long.
     */
    private static final int MAXIMUM_LENGTH_OCTETS = Long.BYTES;

    /**
     * Configured maximum complete LDAPMessage size in octets.
     */
    private final long maximumMessageBytes;

    /**
     * Configured maximum nested BER element depth.
     */
    private final int maximumDepth;

    /**
     * Per-session accumulator containing incomplete stream bytes.
     */
    private final Buffer accumulator;

    /**
     * Creates an independent LDAP BER frame codec.
     *
     * @param maximumMessageBytes positive maximum complete LDAPMessage size
     * @param maximumDepth        positive maximum nested BER element depth
     * @throws ValidateException if a limit is non-positive or the message limit exceeds Fabric frame range
     */
    public BerCodec(final long maximumMessageBytes, final int maximumDepth) {
        if (maximumMessageBytes <= 0 || maximumMessageBytes > Integer.MAX_VALUE) {
            throw new ValidateException("LDAP BER message limit must be between 1 and Integer.MAX_VALUE");
        }
        if (maximumDepth <= 0) {
            throw new ValidateException("LDAP BER depth limit must be positive");
        }
        this.maximumMessageBytes = maximumMessageBytes;
        this.maximumDepth = maximumDepth;
        this.accumulator = new Buffer();
    }

    /**
     * Reads an unsigned big-endian BER length value without consuming the buffer.
     *
     * @param input  buffer containing the complete long-form length
     * @param offset first length-value octet
     * @param count  number of length-value octets
     * @return non-negative signed-long length
     * @throws ProtocolException if the encoded value exceeds signed-long range
     */
    private static long readLength(final Buffer input, final int offset, final int count) {
        long value = 0;
        for (int index = 0; index < count; index++) {
            final int current = Byte.toUnsignedInt(input.getByte(offset + index));
            if (index == 0 && count == Long.BYTES && (current & 0x80) != 0) {
                throw new ProtocolException("LDAP BER length exceeds signed-long range");
            }
            value = (value << Byte.SIZE) | current;
        }
        return value;
    }

    /**
     * Consumes newly received bytes and emits every complete LDAPMessage TLV in wire order.
     *
     * @param input non-empty stream bytes consumed by this session codec
     * @return immutable complete LDAPMessage frames, possibly empty while a message remains incomplete
     * @throws ValidateException if input is {@code null} or empty
     * @throws ProtocolException if a top-level tag or definite length is invalid or exceeds the configured limit
     */
    @Override
    public List<Frame> decode(final Buffer input) {
        final Buffer current = Assert.notNull(input, () -> new ValidateException("LDAP BER input must not be null"));
        if (current.size() == 0) {
            throw new ValidateException("LDAP BER input must not be empty");
        }
        accumulator.write(current, current.size());
        final List<Frame> frames = new ArrayList<>();
        long frameLength;
        while ((frameLength = completeFrameLength(accumulator)) >= 0) {
            try {
                frames.add(Frame.of(accumulator.readByteString(frameLength)));
            } catch (EOFException exception) {
                throw new InternalException("Unable to consume a complete LDAP BER frame", exception);
            }
        }
        return List.copyOf(frames);
    }

    /**
     * Writes one already encoded complete LDAPMessage TLV without adding a transport prefix.
     *
     * @param frame  complete LDAPMessage frame
     * @param output destination stream buffer
     * @throws ValidateException if an argument is {@code null}
     * @throws ProtocolException if the payload is not exactly one valid bounded LDAPMessage frame
     */
    @Override
    public void encode(final Frame frame, final Buffer output) {
        final Frame current = Assert.notNull(frame, () -> new ValidateException("LDAP BER frame must not be null"));
        encodeOwned(current.payload(), output);
    }

    /**
     * Writes one immutable complete LDAPMessage payload without an intermediate Frame allocation.
     *
     * @param payload complete LDAPMessage TLV
     * @param output  destination stream buffer
     * @throws ValidateException if an argument is {@code null}
     * @throws ProtocolException if the payload is empty, malformed, trailing, or exceeds the configured limit
     */
    @Override
    public void encodeOwned(final ByteString payload, final Buffer output) {
        final ByteString current = Assert
                .notNull(payload, () -> new ValidateException("LDAP BER payload must not be null"));
        final Buffer target = Assert.notNull(output, () -> new ValidateException("LDAP BER output must not be null"));
        if (current.size() == 0 || current.size() > maximumMessageBytes) {
            throw new ProtocolException("LDAP BER payload size is outside the configured range");
        }
        final Buffer inspection = new Buffer().write(current);
        final long length = completeFrameLength(inspection);
        if (length != current.size()) {
            throw new ProtocolException("LDAP BER payload must contain exactly one complete LDAPMessage");
        }
        target.write(current);
    }

    /**
     * Creates a fresh per-session codec with identical immutable limits.
     *
     * @return independent empty LDAP BER codec
     */
    @Override
    public BerCodec fork() {
        return new BerCodec(maximumMessageBytes, maximumDepth);
    }

    /**
     * Discards incomplete bytes retained for the current stream session.
     */
    @Override
    public void reset() {
        accumulator.clear();
    }

    /**
     * Returns the configured complete-message size limit to package message codecs.
     *
     * @return positive maximum LDAPMessage size
     */
    long maximumMessageBytes() {
        return maximumMessageBytes;
    }

    /**
     * Returns the configured nested-element depth limit to package message codecs.
     *
     * @return positive maximum BER depth
     */
    int maximumDepth() {
        return maximumDepth;
    }

    /**
     * Determines whether the accumulator starts with a complete top-level LDAPMessage TLV.
     *
     * @param input accumulated stream bytes
     * @return complete frame length, or {@code -1} when more bytes are required
     * @throws ProtocolException if the available header is invalid or declares an excessive frame
     */
    private long completeFrameLength(final Buffer input) {
        if (input.size() < 2) {
            return -1;
        }
        if (Byte.toUnsignedInt(input.getByte(0)) != SEQUENCE_TAG) {
            throw new ProtocolException("LDAP BER frame must begin with a constructed SEQUENCE");
        }
        final int firstLength = Byte.toUnsignedInt(input.getByte(1));
        final int lengthOctets;
        final long contentLength;
        if ((firstLength & INDEFINITE_LENGTH) == 0) {
            lengthOctets = 0;
            contentLength = firstLength;
        } else {
            lengthOctets = firstLength & 0x7f;
            if (lengthOctets == 0) {
                throw new ProtocolException("LDAP BER indefinite length is forbidden");
            }
            if (lengthOctets > MAXIMUM_LENGTH_OCTETS) {
                throw new ProtocolException("LDAP BER length field exceeds the supported range");
            }
            if (input.size() < 2L + lengthOctets) {
                return -1;
            }
            contentLength = readLength(input, 2, lengthOctets);
        }
        final long headerLength = 2L + lengthOctets;
        if (contentLength > Long.MAX_VALUE - headerLength) {
            throw new ProtocolException("LDAP BER frame length overflows the supported range");
        }
        final long total = headerLength + contentLength;
        if (total > maximumMessageBytes) {
            throw new ProtocolException("LDAP BER message exceeds the configured byte limit");
        }
        return input.size() < total ? -1 : total;
    }

    /**
     * Reads definite-length BER elements from one bounded immutable byte region.
     *
     * @author Kimi Liu
     */
    static final class Reader {

        /**
         * Immutable bytes owned by this reader.
         */
        private final byte[] bytes;

        /**
         * Maximum permitted nested element depth.
         */
        private final int maximumDepth;

        /**
         * Depth represented by this element content reader.
         */
        private final int depth;

        /**
         * Next unread byte offset.
         */
        private int offset;

        /**
         * Creates a root reader over one complete encoded value.
         *
         * @param bytes        encoded BER bytes
         * @param maximumDepth positive maximum nested depth
         */
        Reader(final byte[] bytes, final int maximumDepth) {
            this(bytes, maximumDepth, 0);
        }

        /**
         * Creates a reader for one bounded element content.
         *
         * @param bytes        owned element content bytes
         * @param maximumDepth positive maximum nested depth
         * @param depth        current content depth
         */
        private Reader(final byte[] bytes, final int maximumDepth, final int depth) {
            this.bytes = Assert.notNull(bytes, "LDAP BER reader bytes must not be null").clone();
            if (maximumDepth <= 0 || depth < 0 || depth > maximumDepth) {
                throw new ValidateException("LDAP BER reader depth is outside the configured range");
            }
            this.maximumDepth = maximumDepth;
            this.depth = depth;
        }

        /**
         * Returns whether another complete element is expected in this bounded region.
         *
         * @return {@code true} when unread bytes remain
         */
        boolean hasRemaining() {
            return offset < bytes.length;
        }

        /**
         * Returns the next one-octet BER tag without consuming it.
         *
         * @return unsigned next tag octet
         * @throws ProtocolException if this bounded region is exhausted
         */
        int peekTag() {
            requireAvailable(1, "LDAP BER element tag is missing");
            return Byte.toUnsignedInt(bytes[offset]);
        }

        /**
         * Reads the next BER element without interpreting its tag-specific content.
         *
         * @return bounded immutable element
         * @throws ProtocolException if tag or length encoding is malformed or truncated
         */
        Element read() {
            requireAvailable(2, "LDAP BER element header is truncated");
            final int tag = Byte.toUnsignedInt(bytes[offset++]);
            if ((tag & 0x1f) == 0x1f) {
                throw new ProtocolException("LDAP BER high-tag-number form is unsupported");
            }
            final int firstLength = Byte.toUnsignedInt(bytes[offset++]);
            final long length;
            if ((firstLength & INDEFINITE_LENGTH) == 0) {
                length = firstLength;
            } else {
                final int count = firstLength & 0x7f;
                if (count == 0) {
                    throw new ProtocolException("LDAP BER indefinite length is forbidden");
                }
                if (count > MAXIMUM_LENGTH_OCTETS) {
                    throw new ProtocolException("LDAP BER element length exceeds the supported range");
                }
                requireAvailable(count, "LDAP BER long-form length is truncated");
                long current = 0;
                for (int index = 0; index < count; index++) {
                    final int value = Byte.toUnsignedInt(bytes[offset++]);
                    if (index == 0 && count == Long.BYTES && (value & 0x80) != 0) {
                        throw new ProtocolException("LDAP BER element length exceeds signed-long range");
                    }
                    current = (current << Byte.SIZE) | value;
                }
                length = current;
            }
            if (length > Integer.MAX_VALUE || length > bytes.length - offset) {
                throw new ProtocolException("LDAP BER element content is truncated or excessive");
            }
            final byte[] content = java.util.Arrays.copyOfRange(bytes, offset, offset + (int) length);
            offset += (int) length;
            return new Element(tag, content, maximumDepth, depth + 1);
        }

        /**
         * Reads the next element and requires an exact one-octet tag.
         *
         * @param expectedTag required BER tag octet
         * @return matching bounded element
         * @throws ProtocolException if the next element uses another tag
         */
        Element read(final int expectedTag) {
            final Element element = read();
            if (element.tag() != expectedTag) {
                throw new ProtocolException("LDAP BER element has an unexpected tag");
            }
            return element;
        }

        /**
         * Requires complete consumption of this bounded element content.
         *
         * @throws ProtocolException if trailing bytes remain
         */
        void requireFinished() {
            if (hasRemaining()) {
                throw new ProtocolException("LDAP BER element contains trailing bytes");
            }
        }

        /**
         * Verifies that a requested byte count remains in this bounded region.
         *
         * @param count   required byte count
         * @param message fixed safe failure message
         * @throws ProtocolException if the region is truncated
         */
        private void requireAvailable(final int count, final String message) {
            if (count < 0 || count > bytes.length - offset) {
                throw new ProtocolException(message);
            }
        }

    }

    /**
     * Represents one fully bounded BER TLV after its header has been validated.
     *
     * @author Kimi Liu
     */
    static final class Element {

        /**
         * Exact one-octet BER tag.
         */
        private final int tag;

        /**
         * Immutable element content octets.
         */
        private final byte[] content;

        /**
         * Maximum permitted nested depth.
         */
        private final int maximumDepth;

        /**
         * Depth occupied by this element.
         */
        private final int depth;

        /**
         * Creates one bounded element.
         *
         * @param tag          exact tag octet
         * @param content      owned content bytes
         * @param maximumDepth maximum nested depth
         * @param depth        current element depth
         */
        private Element(final int tag, final byte[] content, final int maximumDepth, final int depth) {
            if (depth > maximumDepth) {
                throw new ProtocolException("LDAP BER nesting exceeds the configured depth");
            }
            this.tag = tag;
            this.content = content;
            this.maximumDepth = maximumDepth;
            this.depth = depth;
        }

        /**
         * Returns the unsigned LDAP BER tag without advancing or exposing the element content reader.
         *
         * @return exact unsigned tag octet
         */
        int tag() {
            return tag;
        }

        /**
         * Opens a bounded reader over constructed element content.
         *
         * @return nested content reader
         * @throws ProtocolException if this element is primitive
         */
        Reader reader() {
            if ((tag & 0x20) == 0) {
                throw new ProtocolException("LDAP BER primitive element cannot contain child elements");
            }
            return new Reader(content, maximumDepth, depth);
        }

        /**
         * Returns a defensive copy of primitive content octets.
         *
         * @return copied content bytes
         * @throws ProtocolException if this element is constructed
         */
        byte[] octets() {
            requirePrimitive();
            return content.clone();
        }

        /**
         * Decodes one BER BOOLEAN value.
         *
         * @return {@code false} for zero and {@code true} for any nonzero value
         * @throws ProtocolException if the value is constructed or not exactly one octet
         */
        boolean booleanValue() {
            requirePrimitive();
            if (content.length != 1) {
                throw new ProtocolException("LDAP BER BOOLEAN must contain exactly one octet");
            }
            return content[0] != 0;
        }

        /**
         * Decodes a BER INTEGER or ENUMERATED value in the signed Java integer range.
         *
         * @return exact signed integer value
         * @throws ProtocolException if content is empty or outside the Java integer range
         */
        int integerValue() {
            requirePrimitive();
            if (content.length == 0) {
                throw new ProtocolException("LDAP BER INTEGER must not be empty");
            }
            try {
                return new BigInteger(content).intValueExact();
            } catch (ArithmeticException exception) {
                throw new ProtocolException("LDAP BER INTEGER exceeds the supported range", exception);
            }
        }

        /**
         * Decodes primitive content as a strict UTF-8 LDAPString.
         *
         * @return decoded Unicode string
         * @throws ProtocolException if content is constructed or malformed UTF-8
         */
        String utf8() {
            requirePrimitive();
            try {
                final CharBuffer decoded = Charset.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(content));
                return decoded.toString();
            } catch (CharacterCodingException exception) {
                throw new ProtocolException("LDAP BER LDAPString is not valid UTF-8", exception);
            }
        }

        /**
         * Requires this element to use primitive encoding.
         *
         * @throws ProtocolException if the constructed bit is set
         */
        private void requirePrimitive() {
            if ((tag & 0x20) != 0) {
                throw new ProtocolException("LDAP BER primitive value uses constructed encoding");
            }
        }

    }

    /**
     * Produces definite-length BER elements using minimal generated length and integer forms.
     *
     * @author Kimi Liu
     */
    static final class Writer {

        /**
         * Maximum permitted nested depth.
         */
        private final int maximumDepth;

        /**
         * Depth represented by this writer content.
         */
        private final int depth;

        /**
         * Encoded child elements in declaration order.
         */
        private final Buffer content;

        /**
         * Creates a root BER content writer.
         *
         * @param maximumDepth positive maximum nested depth
         */
        Writer(final int maximumDepth) {
            this(maximumDepth, 1);
        }

        /**
         * Creates a nested BER content writer.
         *
         * @param maximumDepth positive maximum nested depth
         * @param depth        current enclosing element depth
         */
        private Writer(final int maximumDepth, final int depth) {
            if (maximumDepth <= 0 || depth <= 0 || depth > maximumDepth) {
                throw new ValidateException("LDAP BER writer depth is outside the configured range");
            }
            this.maximumDepth = maximumDepth;
            this.depth = depth;
            this.content = new Buffer();
        }

        /**
         * Validates a supported one-octet BER tag and its constructed bit.
         *
         * @param tag         candidate unsigned tag octet
         * @param constructed required constructed state
         * @throws ValidateException if tag range, high-tag form, or constructed bit is invalid
         */
        private static void validateTag(final int tag, final boolean constructed) {
            if (tag < 0 || tag > 0xff || (tag & 0x1f) == 0x1f || ((tag & 0x20) != 0) != constructed) {
                throw new ValidateException("LDAP BER tag is unsupported or has an invalid constructed bit");
            }
        }

        /**
         * Writes one complete TLV using the shortest generated definite-length form.
         *
         * @param output destination buffer
         * @param tag    validated tag octet
         * @param value  content bytes
         */
        private static void writeTlv(final Buffer output, final int tag, final byte[] value) {
            output.writeByte(tag);
            writeLength(output, value.length);
            output.write(value);
        }

        /**
         * Writes a shortest-form non-negative BER definite length.
         *
         * @param output destination buffer
         * @param length non-negative content length
         */
        private static void writeLength(final Buffer output, final int length) {
            if (length < 0x80) {
                output.writeByte(length);
                return;
            }
            int count = 0;
            int current = length;
            while (current != 0) {
                count++;
                current >>>= Byte.SIZE;
            }
            output.writeByte(0x80 | count);
            for (int shift = (count - 1) * Byte.SIZE; shift >= 0; shift -= Byte.SIZE) {
                output.writeByte((length >>> shift) & 0xff);
            }
        }

        /**
         * Appends a primitive element with an exact tag and opaque content.
         *
         * @param tag   primitive one-octet BER tag
         * @param value content octets
         * @return this writer
         */
        Writer primitive(final int tag, final byte[] value) {
            validateTag(tag, false);
            writeTlv(content, tag, Assert.notNull(value, "LDAP BER primitive bytes must not be null"));
            return this;
        }

        /**
         * Appends a BER BOOLEAN using the canonical generated true and false octets.
         *
         * @param tag   primitive BOOLEAN-compatible tag
         * @param value boolean value
         * @return this writer
         */
        Writer bool(final int tag, final boolean value) {
            return primitive(tag, new byte[] { value ? (byte) 0xff : 0 });
        }

        /**
         * Appends a minimally encoded signed BER INTEGER or ENUMERATED value.
         *
         * @param tag   primitive INTEGER-compatible tag
         * @param value signed integer value
         * @return this writer
         */
        Writer integer(final int tag, final int value) {
            return primitive(tag, BigInteger.valueOf(value).toByteArray());
        }

        /**
         * Appends an opaque OCTET STRING-compatible value.
         *
         * @param tag   primitive OCTET STRING-compatible tag
         * @param value exact value octets
         * @return this writer
         */
        Writer octets(final int tag, final byte[] value) {
            return primitive(tag, value);
        }

        /**
         * Appends a strict UTF-8 LDAPString-compatible value.
         *
         * @param tag   primitive OCTET STRING-compatible tag
         * @param value Unicode string
         * @return this writer
         */
        Writer utf8(final int tag, final String value) {
            return primitive(tag, Assert.notNull(value, "LDAP BER string must not be null").getBytes(Charset.UTF_8));
        }

        /**
         * Appends a constructed element populated by a nested writer.
         *
         * @param tag      constructed one-octet BER tag
         * @param children ordered child-element writer
         * @return this writer
         * @throws ProtocolException if nesting exceeds the configured depth
         */
        Writer constructed(final int tag, final Consumer<Writer> children) {
            validateTag(tag, true);
            Assert.notNull(children, "LDAP BER child writer must not be null");
            if (depth >= maximumDepth) {
                throw new ProtocolException("LDAP BER nesting exceeds the configured depth");
            }
            final Writer nested = new Writer(maximumDepth, depth + 1);
            children.accept(nested);
            writeTlv(content, tag, nested.bytes());
            return this;
        }

        /**
         * Wraps all accumulated child elements in one outer constructed tag.
         *
         * @param tag constructed outer tag
         * @return complete encoded TLV
         */
        byte[] encoded(final int tag) {
            validateTag(tag, true);
            final Buffer output = new Buffer();
            writeTlv(output, tag, bytes());
            return output.readByteArray();
        }

        /**
         * Returns a detached copy of accumulated child-element bytes.
         *
         * @return ordered encoded child elements
         */
        byte[] bytes() {
            return content.clone().readByteArray();
        }

    }

}
