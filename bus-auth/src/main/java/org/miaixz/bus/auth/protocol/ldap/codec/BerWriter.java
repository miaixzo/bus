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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Deterministic bounded LDAP-specific BER writer. It emits one-octet tags, minimal definite lengths, minimal big-endian
 * signed integers, canonical Boolean values, and bounded constructed elements without indefinite-length encoding.
 *
 * @author Kimi Liu
 */
public final class BerWriter {

    /**
     * Maximum output bytes.
     */
    private final int maximumBytes;

    /**
     * Maximum constructed nesting depth.
     */
    private final int maximumDepth;

    /**
     * Current constructed nesting depth.
     */
    private final int depth;

    /**
     * Accumulated encoded bytes.
     */
    private final ByteArrayOutputStream output;

    /**
     * Creates one root writer.
     *
     * @param maximumBytes positive output-size ceiling
     * @param maximumDepth positive constructed-depth ceiling
     */
    public BerWriter(final int maximumBytes, final int maximumDepth) {
        Assert.isTrue(
                maximumBytes > Normal._0,
                () -> new ValidateException("BER maximum output size must be positive"));
        Assert.isTrue(maximumDepth > Normal._0, () -> new ValidateException("BER maximum depth must be positive"));
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
        this.depth = Normal._0;
        this.output = new ByteArrayOutputStream(Math.min(maximumBytes, Normal._1024));
    }

    /**
     * Creates one bounded child writer.
     *
     * @param maximumBytes output ceiling
     * @param maximumDepth depth ceiling
     * @param depth        child depth
     */
    private BerWriter(final int maximumBytes, final int maximumDepth, final int depth) {
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
        this.depth = depth;
        this.output = new ByteArrayOutputStream(Math.min(maximumBytes, Normal._1024));
    }

    /**
     * Encodes one minimal definite length.
     *
     * @param value non-negative content length
     * @return encoded length octets
     */
    private static byte[] length(final int value) {
        if (value < Normal._128) {
            return new byte[] { (byte) value };
        }
        int current = value;
        int count = Normal._0;
        while (current != Normal._0) {
            count++;
            current >>>= Byte.SIZE;
        }
        final byte[] result = new byte[count + Normal._1];
        result[Normal._0] = (byte) (0x80 | count);
        current = value;
        for (int index = count; index > Normal._0; index--) {
            result[index] = (byte) current;
            current >>>= Byte.SIZE;
        }
        return result;
    }

    /**
     * Validates one supported one-octet tag.
     *
     * @param tag source tag
     */
    private static void validateTag(final int tag) {
        Assert.isTrue(
                tag >= Normal._0 && tag <= 0xff && (tag & 0x1f) != 0x1f,
                () -> new ValidateException("BER tag is invalid"));
    }

    /**
     * Writes one BER integer.
     *
     * @param value signed integer value
     * @return this writer
     */
    public BerWriter writeInteger(final int value) {
        return writeNumber(BerReader.INTEGER, value);
    }

    /**
     * Writes one BER enumerated value.
     *
     * @param value signed enumerated value
     * @return this writer
     */
    public BerWriter writeEnumerated(final int value) {
        return writeNumber(BerReader.ENUMERATED, value);
    }

    /**
     * Writes one canonical BER Boolean.
     *
     * @param value Boolean value
     * @return this writer
     */
    public BerWriter writeBoolean(final boolean value) {
        return writeElement(BerReader.BOOLEAN, new byte[] { value ? (byte) 0xff : (byte) Normal._0 });
    }

    /**
     * Writes one BER octet string.
     *
     * @param value source bytes
     * @return this writer
     */
    public BerWriter writeOctets(final byte[] value) {
        return writeElement(BerReader.OCTET_STRING, value);
    }

    /**
     * Writes one exact tagged octet string.
     *
     * @param tag   exact one-octet tag
     * @param value source bytes
     * @return this writer
     */
    public BerWriter writeOctets(final int tag, final byte[] value) {
        return writeElement(tag, value);
    }

    /**
     * Writes one UTF-8 LDAP string as an octet string.
     *
     * @param value source string
     * @return this writer
     */
    public BerWriter writeString(final String value) {
        return writeOctets(
                Assert.notNull(value, () -> new ValidateException("BER string must not be null"))
                        .getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes one exact tagged UTF-8 LDAP string.
     *
     * @param tag   exact one-octet tag
     * @param value source string
     * @return this writer
     */
    public BerWriter writeString(final int tag, final String value) {
        return writeOctets(
                tag,
                Assert.notNull(value, () -> new ValidateException("BER string must not be null"))
                        .getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes one canonical BER Null.
     *
     * @return this writer
     */
    public BerWriter writeNull() {
        return writeElement(BerReader.NULL, new byte[0]);
    }

    /**
     * Writes one primitive or pre-encoded constructed element.
     *
     * @param tag   exact one-octet tag
     * @param value copied element content
     * @return this writer
     */
    public BerWriter writeElement(final int tag, final byte[] value) {
        validateTag(tag);
        final byte[] content = Assert
                .notNull(value, () -> new ValidateException("BER element content must not be null"));
        final byte[] length = length(content.length);
        require(1L + length.length + content.length);
        output.write(tag);
        output.writeBytes(length);
        output.writeBytes(content);
        return this;
    }

    /**
     * Writes one bounded constructed element using a child writer.
     *
     * @param tag       exact constructed tag
     * @param operation child encoding operation
     * @return this writer
     */
    public BerWriter writeConstructed(final int tag, final Consumer<BerWriter> operation) {
        validateTag(tag);
        Assert.isTrue(
                (tag & 0x20) != Normal._0,
                () -> new ValidateException("BER constructed element requires a constructed tag"));
        final int childDepth = depth + Normal._1;
        Assert.isTrue(
                childDepth <= maximumDepth,
                () -> new ValidateException("BER constructed depth exceeds the maximum"));
        final BerWriter child = new BerWriter(maximumBytes, maximumDepth, childDepth);
        Assert.notNull(operation, () -> new ValidateException("BER constructed operation must not be null"))
                .accept(child);
        return writeElement(tag, child.toByteArray());
    }

    /**
     * Returns an independent encoded byte array.
     *
     * @return encoded BER bytes
     */
    public byte[] toByteArray() {
        return output.toByteArray();
    }

    /**
     * Writes one minimal signed integer form.
     *
     * @param tag   integer-like tag
     * @param value signed value
     * @return this writer
     */
    private BerWriter writeNumber(final int tag, final int value) {
        final byte[] full = new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8),
                (byte) value };
        int start = Normal._0;
        while (start < full.length - Normal._1
                && (full[start] == Normal._0 && (full[start + Normal._1] & 0x80) == Normal._0
                        || full[start] == (byte) 0xff && (full[start + Normal._1] & 0x80) != Normal._0)) {
            start++;
        }
        return writeElement(tag, java.util.Arrays.copyOfRange(full, start, full.length));
    }

    /**
     * Requires an append to remain within the output ceiling.
     *
     * @param additional additional encoded bytes
     */
    private void require(final long additional) {
        Assert.isTrue(
                additional >= Normal._0 && output.size() + additional <= maximumBytes,
                () -> new ValidateException("BER output exceeds the configured maximum"));
    }

}
