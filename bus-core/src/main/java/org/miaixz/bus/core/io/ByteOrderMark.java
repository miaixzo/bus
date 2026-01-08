/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Description of a Byte Order Mark (BOM) header. BOM definitions:
 * <a href="http://www.unicode.org/unicode/faq/utf_bom.html">http://www.unicode.org/unicode/faq/utf_bom.html</a>
 * <ul>
 * <li>EF BB BF = UTF-8</li>
 * <li>FE FF = UTF-16BE, big-endian</li>
 * <li>FF FE = UTF-16LE, little-endian</li>
 * <li>00 00 FE FF = UTF-32BE, big-endian</li>
 * <li>FF FE 00 00 = UTF-32LE, little-endian</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteOrderMark implements Predicate<byte[]>, Comparable<ByteOrderMark>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852276768599L;

    /**
     * UTF-8 BOM.
     */
    public static final ByteOrderMark UTF_8 = new ByteOrderMark(Charset.DEFAULT_UTF_8, (byte) 0xEF, (byte) 0xBB,
            (byte) 0xBF);
    /**
     * UTF-16BE BOM (Big-Endian).
     */
    public static final ByteOrderMark UTF_16BE = new ByteOrderMark(Charset.DEFAULT_UTF_16_BE, (byte) 0xFE, (byte) 0xFF);
    /**
     * UTF-16LE BOM (Little-Endian).
     */
    public static final ByteOrderMark UTF_16LE = new ByteOrderMark(Charset.DEFAULT_UTF_16_LE, (byte) 0xFF, (byte) 0xFE);
    /**
     * UTF-32BE BOM (Big-Endian).
     */
    public static final ByteOrderMark UTF_32BE = new ByteOrderMark(Charset.DEFAULT_UTF_32_BE, (byte) 0x00, (byte) 0x00,
            (byte) 0xFE, (byte) 0xFF);
    /**
     * UTF-32LE BOM (Little-Endian).
     */
    public static final ByteOrderMark UTF_32LE = new ByteOrderMark(Charset.DEFAULT_UTF_32_LE, (byte) 0xFF, (byte) 0xFE,
            (byte) 0x00, (byte) 0x00);
    /**
     * All predefined BOM information.
     */
    public static final ByteOrderMark[] ALL = new ByteOrderMark[] { UTF_32BE, UTF_32LE, UTF_8, UTF_16BE, UTF_16LE };

    /**
     * The character set name defined by this BOM.
     */
    private final String charsetName;
    /**
     * The byte sequence that makes up this BOM.
     */
    private final byte[] bytes;

    /**
     * Constructs a new {@code ByteOrderMark} instance.
     *
     * @param charsetName The character set name defined by the BOM.
     * @param bytes       The BOM bytes.
     * @throws IllegalArgumentException if the character set name is empty or the bytes array is empty.
     */
    public ByteOrderMark(final String charsetName, final byte... bytes) {
        if (ArrayKit.isEmpty(bytes)) {
            throw new IllegalArgumentException("No bytes specified");
        }
        this.charsetName = Assert.notEmpty(charsetName, "No charsetName specified");
        this.bytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    /**
     * Gets the character set name defined by the BOM header.
     *
     * @return The character set name.
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Gets the number of bytes in the BOM header.
     *
     * @return The number of bytes in the BOM header.
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Gets the byte value at the specified position.
     *
     * @param pos The position.
     * @return The specified byte.
     */
    public int get(final int pos) {
        return bytes[pos];
    }

    /**
     * Gets a copy of the BOM's bytes.
     *
     * @return A copy of the BOM's bytes.
     */
    public byte[] getBytes() {
        return Arrays.copyOfRange(bytes, 0, bytes.length);
    }

    /**
     * Checks if the given head bytes match the BOM information. Returns {@code false} if the provided length is less
     * than the length required for BOM check.
     *
     * @param headBytes The head bytes to check.
     * @return {@code true} if the head bytes match the BOM, {@code false} otherwise.
     */
    @Override
    public boolean test(final byte[] headBytes) {
        if (headBytes.length < bytes.length) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != headBytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares this {@code ByteOrderMark} to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is a {@code ByteOrderMark} object that represents the same sequence of bytes as
     * this object.
     *
     * @param object The object to compare this {@code ByteOrderMark} against.
     * @return {@code true} if the given object represents a {@code ByteOrderMark} equivalent to this byte order mark,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof ByteOrderMark)) {
            return false;
        }
        final ByteOrderMark bom = (ByteOrderMark) object;
        return Arrays.equals(this.bytes, bom.bytes);
    }

    /**
     * Returns a hash code for this byte order mark.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hashCode = getClass().hashCode();
        for (final int b : bytes) {
            hashCode += b;
        }
        return hashCode;
    }

    /**
     * Returns a string representation of this {@code ByteOrderMark}.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('[');
        builder.append(charsetName);
        builder.append(": ");
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                builder.append(Symbol.COMMA);
            }
            builder.append("0x");
            builder.append(Integer.toHexString(0xFF & bytes[i]).toUpperCase());
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Compares this byte order mark to another byte order mark. The comparison is based on the length of the BOM, in
     * descending order.
     *
     * @param o The other byte order mark to compare to.
     * @return A negative integer, zero, or a positive integer as this byte order mark is less than, equal to, or
     *         greater than the specified byte order mark.
     */
    @Override
    public int compareTo(final ByteOrderMark o) {
        // Sort by length in descending order
        return Integer.compare(o.length(), this.length());
    }

}
