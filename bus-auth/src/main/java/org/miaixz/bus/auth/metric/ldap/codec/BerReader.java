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
package org.miaixz.bus.auth.metric.ldap.codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Bounded LDAP-specific BER definite-length reader. The reader rejects high-tag-number form, indefinite length,
 * non-minimal length and integer encodings, truncation, integer overflow, depth overflow, and trailing bytes. Child
 * readers are immutable slices over an internal defensive input copy and never allocate beyond the configured bound.
 *
 * @author Kimi Liu
 */
public final class BerReader {

    /**
     * Universal Boolean tag.
     */
    public static final int BOOLEAN = 0x01;

    /**
     * Universal Integer tag.
     */
    public static final int INTEGER = 0x02;

    /**
     * Universal Octet String tag.
     */
    public static final int OCTET_STRING = 0x04;

    /**
     * Universal Null tag.
     */
    public static final int NULL = 0x05;

    /**
     * Universal Enumerated tag.
     */
    public static final int ENUMERATED = 0x0a;

    /**
     * Universal Sequence tag.
     */
    public static final int SEQUENCE = 0x30;

    /**
     * Maximum supported long-form length octets.
     */
    private static final int MAXIMUM_LENGTH_OCTETS = Normal._4;

    /**
     * Defensive input bytes.
     */
    private final byte[] source;

    /**
     * Exclusive slice end.
     */
    private final int end;

    /**
     * Maximum permitted nesting depth.
     */
    private final int maximumDepth;

    /**
     * Current nesting depth.
     */
    private final int depth;

    /**
     * Current read offset.
     */
    private int offset;

    /**
     * Creates one root reader.
     *
     * @param source       encoded BER bytes
     * @param maximumBytes positive message-size ceiling
     * @param maximumDepth positive nesting-depth ceiling
     */
    public BerReader(final byte[] source, final int maximumBytes, final int maximumDepth) {
        final byte[] input = Assert.notNull(source, () -> new ValidateException("BER input must not be null")).clone();
        Assert.isTrue(
                maximumBytes > Normal._0 && input.length <= maximumBytes,
                () -> new ValidateException("BER input exceeds the configured maximum"));
        Assert.isTrue(maximumDepth > Normal._0, () -> new ValidateException("BER maximum depth must be positive"));
        this.source = input;
        this.offset = Normal._0;
        this.end = input.length;
        this.depth = Normal._0;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Creates one child slice reader.
     *
     * @param source       shared immutable source bytes
     * @param offset       inclusive child offset
     * @param end          exclusive child end
     * @param depth        child nesting depth
     * @param maximumDepth maximum nesting depth
     */
    private BerReader(final byte[] source, final int offset, final int end, final int depth, final int maximumDepth) {
        this.source = source;
        this.offset = offset;
        this.end = end;
        this.depth = depth;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Throws the stable malformed-BER failure.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100300);
    }

    /**
     * Returns the number of unread bytes.
     *
     * @return unread byte count
     */
    public int remaining() {
        return end - offset;
    }

    /**
     * Reports whether the current slice is exhausted.
     *
     * @return whether no unread bytes remain
     */
    public boolean exhausted() {
        return offset == end;
    }

    /**
     * Returns the next one-octet tag without consuming it.
     *
     * @return next tag as an unsigned integer
     */
    public int peekTag() {
        require(1);
        return Byte.toUnsignedInt(source[offset]);
    }

    /**
     * Reads one exact tag and returns a bounded reader over its content.
     *
     * @param expectedTag exact expected one-octet tag
     * @return child content reader
     */
    public BerReader readElement(final int expectedTag) {
        final int tag = readUnsigned();
        if ((tag & 0x1f) == 0x1f || tag != expectedTag) {
            reject();
        }
        final int length = readLength();
        require(length);
        final int nextDepth = depth + Normal._1;
        if (nextDepth > maximumDepth) {
            reject();
        }
        final BerReader child = new BerReader(source, offset, offset + length, nextDepth, maximumDepth);
        offset += length;
        return child;
    }

    /**
     * Reads one BER integer.
     *
     * @return signed 32-bit integer
     */
    public int readInteger() {
        return integer(INTEGER);
    }

    /**
     * Reads one BER enumerated integer.
     *
     * @return signed 32-bit enumerated value
     */
    public int readEnumerated() {
        return integer(ENUMERATED);
    }

    /**
     * Reads one BER Boolean using canonical LDAP encoding.
     *
     * @return decoded Boolean
     */
    public boolean readBoolean() {
        final BerReader value = readElement(BOOLEAN);
        if (value.remaining() != Normal._1) {
            reject();
        }
        final int encoded = value.readUnsigned();
        if (encoded != Normal._0 && encoded != 0xff) {
            reject();
        }
        value.requireEnd();
        return encoded != Normal._0;
    }

    /**
     * Reads one BER octet string.
     *
     * @return copied octets
     */
    public byte[] readOctets() {
        return readOctets(OCTET_STRING);
    }

    /**
     * Reads one exact tagged octet string.
     *
     * @param tag exact expected tag
     * @return copied octets
     */
    public byte[] readOctets(final int tag) {
        final BerReader value = readElement(tag);
        final byte[] result = Arrays.copyOfRange(value.source, value.offset, value.end);
        value.offset = value.end;
        return result;
    }

    /**
     * Reads one UTF-8 LDAP string from an octet string.
     *
     * @return decoded string
     */
    public String readString() {
        return new String(readOctets(), StandardCharsets.UTF_8);
    }

    /**
     * Reads one exact tagged UTF-8 LDAP string.
     *
     * @param tag exact expected tag
     * @return decoded string
     */
    public String readString(final int tag) {
        return new String(readOctets(tag), StandardCharsets.UTF_8);
    }

    /**
     * Reads a canonical BER Null value.
     */
    public void readNull() {
        final BerReader value = readElement(NULL);
        value.requireEnd();
    }

    /**
     * Requires this slice to have no unread trailing bytes.
     */
    public void requireEnd() {
        if (!exhausted()) {
            reject();
        }
    }

    /**
     * Reads one exact integer form.
     *
     * @param tag integer or enumerated tag
     * @return decoded integer
     */
    private int integer(final int tag) {
        final BerReader value = readElement(tag);
        final int length = value.remaining();
        if (length < Normal._1 || length > Integer.BYTES) {
            reject();
        }
        final int first = Byte.toUnsignedInt(value.source[value.offset]);
        if (length > Normal._1) {
            final int second = Byte.toUnsignedInt(value.source[value.offset + Normal._1]);
            if (first == Normal._0 && (second & 0x80) == Normal._0 || first == 0xff && (second & 0x80) != Normal._0) {
                reject();
            }
        }
        int result = (first & 0x80) == 0 ? Normal._0 : -1;
        while (!value.exhausted()) {
            result = result << Byte.SIZE | value.readUnsigned();
        }
        return result;
    }

    /**
     * Reads one minimal definite BER length.
     *
     * @return decoded non-negative length
     */
    private int readLength() {
        final int first = readUnsigned();
        if ((first & 0x80) == Normal._0) {
            return first;
        }
        final int count = first & 0x7f;
        if (count < Normal._1 || count > MAXIMUM_LENGTH_OCTETS) {
            reject();
        }
        require(count);
        if (source[offset] == Normal._0) {
            reject();
        }
        long length = Normal._0;
        for (int index = Normal._0; index < count; index++) {
            length = length << Byte.SIZE | readUnsigned();
        }
        if (length < Normal._128 || length > Integer.MAX_VALUE || length > remaining()) {
            reject();
        }
        return (int) length;
    }

    /**
     * Reads one unsigned source octet.
     *
     * @return unsigned octet
     */
    private int readUnsigned() {
        require(1);
        return Byte.toUnsignedInt(source[offset++]);
    }

    /**
     * Requires a bounded number of unread bytes.
     *
     * @param length required byte count
     */
    private void require(final int length) {
        if (length < Normal._0 || length > end - offset) {
            reject();
        }
    }

}
