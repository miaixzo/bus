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
package org.miaixz.bus.core.io.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * An immutable byte string composed of byte array segments. This class exists to provide an efficient snapshot of a
 * {@link Buffer}. It is implemented as an array of segments, plus a directory, describing how the segments form this
 * byte string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteBuffer extends ByteString {

    /**
     * An array of byte arrays, where each inner array is a segment of the buffer. This field is transient because its
     * content is derived from the buffer.
     */
    private transient byte[][] segments;
    /**
     * An array of integers that serves as a directory for the segments. The first half stores the cumulative sizes of
     * the segments, and the second half stores the starting positions within each segment. This field is transient
     * because its content is derived from the buffer.
     */
    private transient int[] directory;

    /**
     * Constructs a {@code ByteBuffer} by taking a snapshot of the first {@code byteCount} bytes from the provided
     * {@link Buffer}.
     *
     * @param buffer    The source buffer to create the snapshot from.
     * @param byteCount The number of bytes to include in this {@code ByteBuffer}.
     * @throws IllegalArgumentException If {@code byteCount} is negative or greater than the buffer's size.
     * @throws AssertionError           If a segment in the buffer is empty (limit == pos), indicating an internal
     *                                  error.
     */
    public ByteBuffer(Buffer buffer, int byteCount) {
        super(null);
        IoKit.checkOffsetAndCount(buffer.size, 0, byteCount);

        int offset = 0;
        int segmentCount = 0;
        for (SectionBuffer s = buffer.head; offset < byteCount; s = s.next) {
            if (s.limit == s.pos) {
                throw new AssertionError("s.limit == s.pos");
            }
            offset += s.limit - s.pos;
            segmentCount++;
        }

        // Walk through the buffer again to assign segments and build the directory.
        this.segments = new byte[segmentCount][];
        this.directory = new int[segmentCount * 2];
        offset = 0;
        segmentCount = 0;
        for (SectionBuffer s = buffer.head; offset < byteCount; s = s.next) {
            segments[segmentCount] = s.data;
            offset += s.limit - s.pos;
            if (offset > byteCount) {
                offset = byteCount;
            }
            directory[segmentCount] = offset;
            directory[segmentCount + segments.length] = s.pos;
            s.shared = true;
            segmentCount++;
        }
    }

    /**
     * Decodes this byte string as a UTF-8 string.
     *
     * @return The decoded UTF-8 string.
     */
    @Override
    public String utf8() {
        return toByteString().utf8();
    }

    /**
     * Decodes this byte string using the specified charset.
     *
     * @param charset The charset to use for decoding.
     * @return The decoded string.
     */
    @Override
    public String string(Charset charset) {
        return toByteString().string(charset);
    }

    /**
     * Encodes this byte string as a Base64 string.
     *
     * @return The Base64 encoded string.
     */
    @Override
    public String base64() {
        return toByteString().base64();
    }

    /**
     * Encodes this byte string as a hexadecimal string.
     *
     * @return The hexadecimal encoded string.
     */
    @Override
    public String hex() {
        return toByteString().hex();
    }

    /**
     * Returns a new {@code ByteString} that is the ASCII lowercase equivalent of this byte string.
     *
     * @return A new {@code ByteString} with all ASCII uppercase characters converted to lowercase.
     */
    @Override
    public ByteString toAsciiLowercase() {
        return toByteString().toAsciiLowercase();
    }

    /**
     * Returns a new {@code ByteString} that is the ASCII uppercase equivalent of this byte string.
     *
     * @return A new {@code ByteString} with all ASCII lowercase characters converted to uppercase.
     */
    @Override
    public ByteString toAsciiUppercase() {
        return toByteString().toAsciiUppercase();
    }

    /**
     * Computes the 128-bit MD5 hash of this byte string.
     *
     * @return A {@link ByteString} representing the MD5 hash.
     */
    @Override
    public ByteString md5() {
        return toByteString().md5();
    }

    /**
     * Computes the 160-bit SHA-1 hash of this byte string.
     *
     * @return A {@link ByteString} representing the SHA-1 hash.
     */
    @Override
    public ByteString sha1() {
        return toByteString().sha1();
    }

    /**
     * Computes the 256-bit SHA-256 hash of this byte string.
     *
     * @return A {@link ByteString} representing the SHA-256 hash.
     */
    @Override
    public ByteString sha256() {
        return toByteString().sha256();
    }

    /**
     * Computes the 160-bit SHA-1 HMAC of this byte string using the given key.
     *
     * @param key The secret key for the HMAC.
     * @return A {@link ByteString} representing the SHA-1 HMAC.
     */
    @Override
    public ByteString hmacSha1(ByteString key) {
        return toByteString().hmacSha1(key);
    }

    /**
     * Computes the 256-bit SHA-256 HMAC of this byte string using the given key.
     *
     * @param key The secret key for the HMAC.
     * @return A {@link ByteString} representing the SHA-256 HMAC.
     */
    @Override
    public ByteString hmacSha256(ByteString key) {
        return toByteString().hmacSha256(key);
    }

    /**
     * Encodes this byte string as a Base64 URL-safe string.
     *
     * @return The Base64 URL-safe encoded string.
     */
    @Override
    public String base64Url() {
        return toByteString().base64Url();
    }

    /**
     * Returns a new {@code ByteString} that is a substring of this byte string, starting from the specified
     * {@code beginIndex} to the end of the string.
     *
     * @param beginIndex The beginning index, inclusive.
     * @return The specified substring.
     */
    @Override
    public ByteString substring(int beginIndex) {
        return toByteString().substring(beginIndex);
    }

    /**
     * Returns a new {@code ByteString} that is a substring of this byte string, starting from the specified
     * {@code beginIndex} to the specified {@code endIndex}.
     *
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex   The ending index, exclusive.
     * @return The specified substring.
     */
    @Override
    public ByteString substring(int beginIndex, int endIndex) {
        return toByteString().substring(beginIndex, endIndex);
    }

    /**
     * Returns the byte at the specified {@code pos} in this byte string.
     *
     * @param pos The index of the byte to retrieve.
     * @return The byte at the specified position.
     * @throws IndexOutOfBoundsException If {@code pos} is out of bounds.
     */
    @Override
    public byte getByte(int pos) {
        IoKit.checkOffsetAndCount(directory[segments.length - 1], pos, 1);
        int segment = segment(pos);
        int segmentOffset = segment == 0 ? 0 : directory[segment - 1];
        int segmentPos = directory[segment + segments.length];
        return segments[segment][pos - segmentOffset + segmentPos];
    }

    /**
     * Determines which segment contains the byte at the given position.
     *
     * @param pos The absolute position within the byte string.
     * @return The index of the segment containing the byte.
     */
    private int segment(int pos) {
        int i = Arrays.binarySearch(directory, 0, segments.length, pos + 1);
        return i >= 0 ? i : ~i;
    }

    /**
     * Returns the total number of bytes in this byte string.
     *
     * @return The size of the byte string in bytes.
     */
    @Override
    public int size() {
        return directory[segments.length - 1];
    }

    /**
     * Returns a new byte array containing all bytes from this byte string.
     *
     * @return A new byte array.
     */
    @Override
    public byte[] toByteArray() {
        byte[] result = new byte[directory[segments.length - 1]];
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            System.arraycopy(segments[s], segmentPos, result, segmentOffset, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
        return result;
    }

    /**
     * Returns a read-only {@link java.nio.ByteBuffer} that wraps the contents of this byte string.
     *
     * @return A read-only {@link java.nio.ByteBuffer}.
     */
    @Override
    public java.nio.ByteBuffer asByteBuffer() {
        return java.nio.ByteBuffer.wrap(toByteArray()).asReadOnlyBuffer();
    }

    /**
     * Writes the entire contents of this byte string to the specified output stream.
     *
     * @param out The output stream to write to.
     * @throws IOException              If an I/O error occurs.
     * @throws IllegalArgumentException If {@code out} is null.
     */
    @Override
    public void write(OutputStream out) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            out.write(segments[s], segmentPos, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
    }

    /**
     * Writes the entire contents of this byte string to the specified buffer.
     *
     * @param buffer The buffer to write to.
     */
    @Override
    public void write(Buffer buffer) {
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            SectionBuffer segment = new SectionBuffer(segments[s], segmentPos,
                    segmentPos + nextSegmentOffset - segmentOffset, true, false);
            if (null == buffer.head) {
                buffer.head = segment.next = segment.prev = segment;
            } else {
                buffer.head.prev.push(segment);
            }
            segmentOffset = nextSegmentOffset;
        }
        buffer.size += segmentOffset;
    }

    /**
     * Compares a range of bytes in this byte string to a range of bytes in another {@link ByteString}.
     *
     * @param offset      The starting offset in this byte string.
     * @param other       The other {@link ByteString} to compare against.
     * @param otherOffset The starting offset in the other {@link ByteString}.
     * @param byteCount   The number of bytes to compare.
     * @return {@code true} if the ranges are equal, {@code false} otherwise.
     */
    @Override
    public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
        if (offset < 0 || offset > size() - byteCount)
            return false;
        for (int s = segment(offset); byteCount > 0; s++) {
            int segmentOffset = s == 0 ? 0 : directory[s - 1];
            int segmentSize = directory[s] - segmentOffset;
            int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
            int segmentPos = directory[segments.length + s];
            int arrayOffset = offset - segmentOffset + segmentPos;
            if (!other.rangeEquals(otherOffset, segments[s], arrayOffset, stepSize))
                return false;
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
        }
        return true;
    }

    /**
     * Compares a range of bytes in this byte string to a range of bytes in a byte array.
     *
     * @param offset      The starting offset in this byte string.
     * @param other       The byte array to compare against.
     * @param otherOffset The starting offset in the byte array.
     * @param byteCount   The number of bytes to compare.
     * @return {@code true} if the ranges are equal, {@code false} otherwise.
     */
    @Override
    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        if (offset < 0 || offset > size() - byteCount || otherOffset < 0 || otherOffset > other.length - byteCount) {
            return false;
        }
        for (int s = segment(offset); byteCount > 0; s++) {
            int segmentOffset = s == 0 ? 0 : directory[s - 1];
            int segmentSize = directory[s] - segmentOffset;
            int stepSize = Math.min(byteCount, segmentOffset + segmentSize - offset);
            int segmentPos = directory[segments.length + s];
            int arrayOffset = offset - segmentOffset + segmentPos;
            if (!IoKit.arrayRangeEquals(segments[s], arrayOffset, other, otherOffset, stepSize))
                return false;
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
        }
        return true;
    }

    /**
     * Returns the index of the first occurrence of {@code other} in this byte string, starting from {@code fromIndex}.
     *
     * @param other     The byte array to search for.
     * @param fromIndex The index to start the search from (inclusive).
     * @return The index of the first occurrence, or -1 if not found.
     */
    @Override
    public int indexOf(byte[] other, int fromIndex) {
        return toByteString().indexOf(other, fromIndex);
    }

    /**
     * Returns the index of the last occurrence of {@code other} in this byte string, searching backwards from
     * {@code fromIndex}.
     *
     * @param other     The byte array to search for.
     * @param fromIndex The index to start the search from (inclusive).
     * @return The index of the last occurrence, or -1 if not found.
     */
    @Override
    public int lastIndexOf(byte[] other, int fromIndex) {
        return toByteString().lastIndexOf(other, fromIndex);
    }

    /**
     * Converts this {@code ByteBuffer} to a standard {@link ByteString} by copying its contents into a single byte
     * array.
     *
     * @return A new {@link ByteString} instance.
     */
    private ByteString toByteString() {
        return new ByteString(toByteArray());
    }

    /**
     * Returns a direct reference to the internal byte array. This method is intended for internal use and should be
     * used with caution, as modifications to the returned array will affect this {@code ByteString}.
     *
     * @return The internal byte array.
     */
    @Override
    public byte[] internalArray() {
        return toByteArray();
    }

    /**
     * Compares this {@code ByteBuffer} to the specified object. The result is {@code true} if and only if the argument
     * is a {@code ByteString} object that contains the same sequence of bytes as this byte string.
     *
     * @param o The object to compare this {@code ByteBuffer} against.
     * @return {@code true} if the given object represents a {@code ByteString} equivalent to this byte string,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        return o instanceof ByteString && ((ByteString) o).size() == size()
                && rangeEquals(0, ((ByteString) o), 0, size());
    }

    /**
     * Returns a hash code for this byte string.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result != 0)
            return result;

        result = 1;
        int segmentOffset = 0;
        for (int s = 0, segmentCount = segments.length; s < segmentCount; s++) {
            byte[] segment = segments[s];
            int segmentPos = directory[segmentCount + s];
            int nextSegmentOffset = directory[s];
            int segmentSize = nextSegmentOffset - segmentOffset;
            for (int i = segmentPos, limit = segmentPos + segmentSize; i < limit; i++) {
                result = (31 * result) + segment[i];
            }
            segmentOffset = nextSegmentOffset;
        }
        return (hashCode = result);
    }

    /**
     * Returns a human-readable string that describes the contents of this byte string.
     *
     * @return A string representation of the byte string's contents.
     */
    @Override
    public String toString() {
        return toByteString().toString();
    }

    /**
     * This method is called by the serialization mechanism to allow a class to substitute its own serialized form. It
     * ensures that when a {@code ByteBuffer} is serialized, it is converted into a standard {@link ByteString} to avoid
     * issues with transient fields.
     *
     * @return A {@link ByteString} representation of this object for serialization.
     */
    private Object writeReplace() {
        return toByteString();
    }

}
