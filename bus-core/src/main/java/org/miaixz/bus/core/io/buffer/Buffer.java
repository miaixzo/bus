/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.*;
import java.nio.channels.ByteChannel;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.PeekSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A mutable, resizable collection of bytes in memory.
 *
 * <p>
 * This class provides a flexible and efficient way to work with binary data. It uses a linked list of segments to store
 * data, allowing for efficient insertion, deletion, and resizing operations without requiring large contiguous memory
 * allocations.
 *
 * <p>
 * Key features:
 * <ul>
 * <li>Efficient read/write operations for primitive types and strings</li>
 * <li>Support for various character encodings with UTF-8 as the default</li>
 * <li>Cryptographic hash and HMAC computation</li>
 * <li>Integration with Java I/O streams and NIO buffers</li>
 * <li>Memory-efficient segment pooling</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Buffer buffer = new Buffer();
 * buffer.writeUtf8("Hello, World!");
 * buffer.writeInt(42);
 *
 * String message = buffer.readUtf8();
 * int number = buffer.readInt();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Buffer implements BufferSource, BufferSink, Cloneable, ByteChannel {

    /**
     * Unicode replacement character (U+FFFD) used when invalid UTF-8 sequences are encountered.
     */
    public static final int REPLACEMENT_CHARACTER = '\ufffd';

    /**
     * The head segment of the linked list that stores the buffer's data. This is the first segment in the segment
     * chain.
     */
    public SectionBuffer head;

    /**
     * The total number of bytes currently stored in this buffer.
     */
    public long size;

    /**
     * Creates a new empty buffer with no initial capacity.
     */
    public Buffer() {

    }

    /**
     * Returns the number of bytes currently stored in this buffer.
     *
     * @return the size of the buffer in bytes
     */
    public final long size() {
        return size;
    }

    /**
     * Returns this buffer instance.
     *
     * @return this buffer
     */
    @Override
    public Buffer buffer() {
        return this;
    }

    /**
     * Returns this buffer instance.
     *
     * @return this buffer
     */
    @Override
    public Buffer getBuffer() {
        return this;
    }

    /**
     * Returns an output stream that writes to this buffer. The returned stream will append all written bytes to this
     * buffer.
     *
     * @return an output stream that writes to this buffer
     */
    @Override
    public OutputStream outputStream() {
        return new OutputStream() {

            /**
             * Write method.
             *
             * @return the void value
             */
            @Override
            public void write(int b) {
                writeByte((byte) b);
            }

            /**
             * Write method.
             *
             * @return the void value
             */
            @Override
            public void write(byte[] data, int offset, int byteCount) {
                Buffer.this.write(data, offset, byteCount);
            }

            /**
             * Flush method.
             *
             * @return the void value
             */
            @Override
            public void flush() {
            }

            /**
             * Close method.
             *
             * @return the void value
             */
            @Override
            public void close() {
            }

            /**
             * Returns the string representation of this object.
             *
             * @return the string representation
             */
            @Override
            public String toString() {
                return Buffer.this + ".outputStream()";
            }
        };
    }

    /**
     * Returns this buffer without any modifications. This method is part of the BufferSink interface and is included
     * for compatibility.
     *
     * @return this buffer
     */
    @Override
    public Buffer emitCompleteSegments() {
        return this;
    }

    /**
     * Returns this buffer sink.
     *
     * @return this buffer sink
     */
    @Override
    public BufferSink emit() {
        return this;
    }

    /**
     * Returns true if this buffer contains no bytes.
     *
     * @return true if the buffer is empty, false otherwise
     */
    @Override
    public boolean exhausted() {
        return size == 0;
    }

    /**
     * Throws an EOFException if this buffer contains fewer than {@code byteCount} bytes. This method is useful for
     * validating that sufficient data is available before reading.
     *
     * @param byteCount the minimum number of bytes that must be available
     * @throws EOFException if the buffer has fewer than {@code byteCount} bytes
     */
    @Override
    public void require(long byteCount) throws EOFException {
        if (size < byteCount)
            throw new EOFException();
    }

    /**
     * Returns true if this buffer contains at least {@code byteCount} bytes.
     *
     * @param byteCount the number of bytes to check for
     * @return true if the buffer has at least {@code byteCount} bytes, false otherwise
     */
    @Override
    public boolean request(long byteCount) {
        return size >= byteCount;
    }

    /**
     * Returns a buffer source that can peek at this buffer without consuming its data. The returned source provides a
     * read-only view of this buffer's current contents.
     *
     * @return a peekable buffer source
     */
    @Override
    public BufferSource peek() {
        return IoKit.buffer(new PeekSource(this));
    }

    /**
     * Returns an input stream that reads from this buffer. The returned stream will consume bytes from this buffer as
     * they are read.
     *
     * @return an input stream that reads from this buffer
     */
    @Override
    public InputStream inputStream() {
        return new InputStream() {

            /**
             * Read method.
             *
             * @return the int value
             */
            @Override
            public int read() {
                if (size > 0)
                    return readByte() & 0xff;
                return -1;
            }

            /**
             * Read method.
             *
             * @return the int value
             */
            @Override
            public int read(byte[] sink, int offset, int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }

            /**
             * Available method.
             *
             * @return the int value
             */
            @Override
            public int available() {
                return (int) Math.min(size, Integer.MAX_VALUE);
            }

            /**
             * Close method.
             *
             * @return the void value
             */
            @Override
            public void close() {
            }

            /**
             * Returns the string representation of this object.
             *
             * @return the string representation
             */
            @Override
            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    /**
     * Copies all bytes from this buffer to the specified output stream. This method does not modify the source buffer.
     *
     * @param out the output stream to write to
     * @return this buffer
     * @throws IOException              if an I/O error occurs while writing
     * @throws IllegalArgumentException if {@code out} is null
     */
    public final Buffer copyTo(OutputStream out) throws IOException {
        return copyTo(out, 0, size);
    }

    /**
     * Copies {@code byteCount} bytes from this buffer, starting at {@code offset}, to the specified output stream. This
     * method does not modify the source buffer.
     *
     * @param out       the output stream to write to
     * @param offset    the starting position in this buffer (0-based)
     * @param byteCount the number of bytes to copy
     * @return this buffer
     * @throws IOException               if an I/O error occurs while writing
     * @throws IllegalArgumentException  if {@code out} is null
     * @throws IndexOutOfBoundsException if {@code offset} or {@code byteCount} are invalid
     */
    public final Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, offset, byteCount);
        if (byteCount == 0)
            return this;

        SectionBuffer s = head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }

        for (; byteCount > 0; s = s.next) {
            int pos = (int) (s.pos + offset);
            int toCopy = (int) Math.min(s.limit - pos, byteCount);
            out.write(s.data, pos, toCopy);
            byteCount -= toCopy;
            offset = 0;
        }

        return this;
    }

    /**
     * Copies {@code byteCount} bytes from this buffer, starting at {@code offset}, to the specified destination buffer.
     * This method does not modify the source buffer.
     *
     * @param out       the destination buffer
     * @param offset    the starting position in this buffer (0-based)
     * @param byteCount the number of bytes to copy
     * @return this buffer
     * @throws IllegalArgumentException  if {@code out} is null
     * @throws IndexOutOfBoundsException if {@code offset} or {@code byteCount} are invalid
     */
    public final Buffer copyTo(Buffer out, long offset, long byteCount) {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, offset, byteCount);
        if (byteCount == 0)
            return this;

        out.size += byteCount;

        SectionBuffer s = head;
        for (; offset >= (s.limit - s.pos); s = s.next) {
            offset -= (s.limit - s.pos);
        }
        for (; byteCount > 0; s = s.next) {
            SectionBuffer copy = s.sharedCopy();
            copy.pos += offset;
            copy.limit = Math.min(copy.pos + (int) byteCount, copy.limit);
            if (null == out.head) {
                out.head = copy.next = copy.prev = copy;
            } else {
                out.head.prev.push(copy);
            }
            byteCount -= copy.limit - copy.pos;
            offset = 0;
        }

        return this;
    }

    /**
     * Writes all bytes from this buffer to the specified output stream. This method consumes the bytes from this
     * buffer.
     *
     * @param out the output stream to write to
     * @return this buffer
     * @throws IOException              if an I/O error occurs while writing
     * @throws IllegalArgumentException if {@code out} is null
     */
    public final Buffer writeTo(OutputStream out) throws IOException {
        return writeTo(out, size);
    }

    /**
     * Writes {@code byteCount} bytes from this buffer to the specified output stream. This method consumes the bytes
     * from this buffer.
     *
     * @param out       the output stream to write to
     * @param byteCount the number of bytes to write
     * @return this buffer
     * @throws IOException               if an I/O error occurs while writing
     * @throws IllegalArgumentException  if {@code out} is null
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    public final Buffer writeTo(OutputStream out, long byteCount) throws IOException {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        IoKit.checkOffsetAndCount(size, 0, byteCount);

        SectionBuffer s = head;
        while (byteCount > 0) {
            int toCopy = (int) Math.min(byteCount, s.limit - s.pos);
            out.write(s.data, s.pos, toCopy);

            s.pos += toCopy;
            size -= toCopy;
            byteCount -= toCopy;

            if (s.pos == s.limit) {
                SectionBuffer toRecycle = s;
                head = s = toRecycle.pop();
                LifeCycle.recycle(toRecycle);
            }
        }

        return this;
    }

    /**
     * Reads all bytes from the specified input stream into this buffer. This method reads until the end of the stream
     * is reached.
     *
     * @param in the input stream to read from
     * @return this buffer
     * @throws IOException              if an I/O error occurs while reading
     * @throws IllegalArgumentException if {@code in} is null
     */
    public final Buffer readFrom(InputStream in) throws IOException {
        readFrom(in, Long.MAX_VALUE, true);
        return this;
    }

    /**
     * Reads {@code byteCount} bytes from the specified input stream into this buffer. This method reads exactly
     * {@code byteCount} bytes or throws an exception if fewer bytes are available.
     *
     * @param in        the input stream to read from
     * @param byteCount the number of bytes to read
     * @return this buffer
     * @throws IOException              if an I/O error occurs while reading
     * @throws IllegalArgumentException if {@code in} is null or {@code byteCount} is negative
     * @throws EOFException             if the end of the stream is reached before reading {@code byteCount} bytes
     */
    public final Buffer readFrom(InputStream in, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        readFrom(in, byteCount, false);
        return this;
    }

    /**
     * Internal method to read bytes from an input stream into this buffer.
     *
     * @param in        the input stream to read from
     * @param byteCount the number of bytes to read
     * @param forever   if true, read until the end of the stream
     * @throws IOException              if an I/O error occurs while reading
     * @throws IllegalArgumentException if {@code in} is null
     * @throws EOFException             if the end of the stream is reached before reading the required bytes
     */
    private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
        if (null == in) {
            throw new IllegalArgumentException("in == null");
        }
        while (byteCount > 0 || forever) {
            SectionBuffer tail = writableSegment(1);
            int maxToCopy = (int) Math.min(byteCount, SectionBuffer.SIZE - tail.limit);
            int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
            if (bytesRead == -1) {
                if (tail.pos == tail.limit) {
                    // We allocated a tail segment, but didn't end up needing it. Recycle!
                    head = tail.pop();
                    LifeCycle.recycle(tail);
                }
                if (forever)
                    return;
                throw new EOFException();
            }
            tail.limit += bytesRead;
            size += bytesRead;
            byteCount -= bytesRead;
        }
    }

    /**
     * Returns the number of bytes in segments that are not writable. This is the number of bytes that can be flushed
     * immediately to an underlying sink without harming throughput.
     *
     * @return the number of bytes in complete segments
     */
    public final long completeSegmentByteCount() {
        long result = size;
        if (result == 0)
            return 0;

        SectionBuffer tail = head.prev;
        if (tail.limit < SectionBuffer.SIZE && tail.owner) {
            result -= tail.limit - tail.pos;
        }

        return result;
    }

    /**
     * Reads and returns a single byte from this buffer.
     *
     * @return the byte read
     * @throws IllegalStateException if the buffer is empty
     */
    @Override
    public byte readByte() {
        if (size == 0)
            throw new IllegalStateException("size == 0");

        SectionBuffer segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        byte[] data = segment.data;
        byte b = data[pos++];
        size -= 1;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return b;
    }

    /**
     * Returns the byte at the specified position in this buffer without consuming it.
     *
     * @param pos the position of the byte to return (0-based)
     * @return the byte at the specified position
     * @throws IndexOutOfBoundsException if {@code pos} is out of bounds
     */
    public final byte getByte(long pos) {
        IoKit.checkOffsetAndCount(size, pos, 1);
        if (size - pos > pos) {
            for (SectionBuffer s = head; true; s = s.next) {
                int segmentByteCount = s.limit - s.pos;
                if (pos < segmentByteCount)
                    return s.data[s.pos + (int) pos];
                pos -= segmentByteCount;
            }
        } else {
            pos -= size;
            for (SectionBuffer s = head.prev; true; s = s.prev) {
                pos += s.limit - s.pos;
                if (pos >= 0)
                    return s.data[s.pos + (int) pos];
            }
        }
    }

    /**
     * Reads and returns a 16-bit big-endian short value from this buffer.
     *
     * @return the short value read
     * @throws IllegalStateException if the buffer contains fewer than 2 bytes
     */
    @Override
    public short readShort() {
        if (size < 2)
            throw new IllegalStateException("size < 2: " + size);

        SectionBuffer segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 2) {
            int s = (readByte() & 0xff) << 8 | (readByte() & 0xff);
            return (short) s;
        }

        byte[] data = segment.data;
        int s = (data[pos++] & 0xff) << 8 | (data[pos++] & 0xff);
        size -= 2;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return (short) s;
    }

    /**
     * Reads and returns a 32-bit big-endian integer value from this buffer.
     *
     * @return the integer value read
     * @throws IllegalStateException if the buffer contains fewer than 4 bytes
     */
    @Override
    public int readInt() {
        if (size < 4)
            throw new IllegalStateException("size < 4: " + size);

        SectionBuffer segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 4) {
            return (readByte() & 0xff) << 24 | (readByte() & 0xff) << 16 | (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
        }

        byte[] data = segment.data;
        int i = (data[pos++] & 0xff) << 24 | (data[pos++] & 0xff) << 16 | (data[pos++] & 0xff) << 8
                | (data[pos++] & 0xff);
        size -= 4;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return i;
    }

    /**
     * Reads and returns a 64-bit big-endian long value from this buffer.
     *
     * @return the long value read
     * @throws IllegalStateException if the buffer contains fewer than 8 bytes
     */
    @Override
    public long readLong() {
        if (size < 8)
            throw new IllegalStateException("size < 8: " + size);

        SectionBuffer segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        if (limit - pos < 8) {
            return (readInt() & 0xffffffffL) << Normal._32 | (readInt() & 0xffffffffL);
        }

        byte[] data = segment.data;
        long v = (data[pos++] & 0xffL) << 56 | (data[pos++] & 0xffL) << 48 | (data[pos++] & 0xffL) << 40
                | (data[pos++] & 0xffL) << Normal._32 | (data[pos++] & 0xffL) << 24 | (data[pos++] & 0xffL) << 16
                | (data[pos++] & 0xffL) << 8 | (data[pos++] & 0xffL);
        size -= 8;

        if (pos == limit) {
            head = segment.pop();
            LifeCycle.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return v;
    }

    /**
     * Reads and returns a 16-bit little-endian short value from this buffer.
     *
     * @return the short value read
     */
    @Override
    public short readShortLe() {
        return IoKit.reverseBytesShort(readShort());
    }

    /**
     * Reads and returns a 32-bit little-endian integer value from this buffer.
     *
     * @return the integer value read
     */
    @Override
    public int readIntLe() {
        return IoKit.reverseBytesInt(readInt());
    }

    /**
     * Reads and returns a 64-bit little-endian long value from this buffer.
     *
     * @return the long value read
     */
    @Override
    public long readLongLe() {
        return IoKit.reverseBytesLong(readLong());
    }

    /**
     * Reads and returns a decimal long value from this buffer. The number may be preceded by an optional '-' sign for
     * negative values.
     *
     * @return the long value read
     * @throws NumberFormatException if the number is too large or invalid
     * @throws IllegalStateException if the buffer is empty
     */
    @Override
    public long readDecimalLong() {
        if (size == 0)
            throw new IllegalStateException("size == 0");

        long value = 0;
        int seen = 0;
        boolean negative = false;
        boolean done = false;

        long overflowZone = Long.MIN_VALUE / 10;
        long overflowDigit = (Long.MIN_VALUE % 10) + 1;

        do {
            SectionBuffer segment = head;

            byte[] data = segment.data;
            int pos = segment.pos;
            int limit = segment.limit;

            for (; pos < limit; pos++, seen++) {
                byte b = data[pos];
                if (b >= Symbol.C_ZERO && b <= Symbol.C_NINE) {
                    int digit = Symbol.C_ZERO - b;

                    // Detect when the digit would cause an overflow.
                    if (value < overflowZone || value == overflowZone && digit < overflowDigit) {
                        Buffer buffer = new Buffer().writeDecimalLong(value).writeByte(b);
                        if (!negative)
                            buffer.readByte(); // Skip negative sign.
                        throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                    }
                    value *= 10;
                    value += digit;
                } else if (b == Symbol.C_MINUS && seen == 0) {
                    negative = true;
                    overflowDigit -= 1;
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9] or '-' character but was 0x" + Integer.toHexString(b));
                    }
                    // Set a flag to stop iteration. We still need to run through segment updating below.
                    done = true;
                    break;
                }
            }

            if (pos == limit) {
                head = segment.pop();
                LifeCycle.recycle(segment);
            } else {
                segment.pos = pos;
            }
        } while (!done && head != null);

        size -= seen;
        return negative ? value : -value;
    }

    /**
     * Reads and returns a hexadecimal unsigned long value from this buffer. The number may contain uppercase or
     * lowercase hexadecimal digits (0-9, a-f, A-F).
     *
     * @return the unsigned long value read
     * @throws NumberFormatException if the number is too large or invalid
     * @throws IllegalStateException if the buffer is empty
     */
    @Override
    public long readHexadecimalUnsignedLong() {
        if (size == 0)
            throw new IllegalStateException("size == 0");

        long value = 0;
        int seen = 0;
        boolean done = false;

        do {
            SectionBuffer segment = head;

            byte[] data = segment.data;
            int pos = segment.pos;
            int limit = segment.limit;

            for (; pos < limit; pos++, seen++) {
                int digit;

                byte b = data[pos];
                if (b >= Symbol.C_ZERO && b <= Symbol.C_NINE) {
                    digit = b - Symbol.C_ZERO;
                } else if (b >= 'a' && b <= 'f') {
                    digit = b - 'a' + 10;
                } else if (b >= 'A' && b <= 'F') {
                    digit = b - 'A' + 10;
                } else {
                    if (seen == 0) {
                        throw new NumberFormatException(
                                "Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
                    }
                    // Set a flag to stop iteration. We still need to run through segment updating below.
                    done = true;
                    break;
                }

                // Detect when the shift would overflow.
                if ((value & 0xf000000000000000L) != 0) {
                    Buffer buffer = new Buffer().writeHexadecimalUnsignedLong(value).writeByte(b);
                    throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                }

                value <<= 4;
                value |= digit;
            }

            if (pos == limit) {
                head = segment.pop();
                LifeCycle.recycle(segment);
            } else {
                segment.pos = pos;
            }
        } while (!done && null != head);

        size -= seen;
        return value;
    }

    /**
     * Reads and returns a byte string containing all remaining bytes in this buffer. This method consumes all bytes
     * from the buffer.
     *
     * @return a byte string containing all remaining bytes
     */
    @Override
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    /**
     * Reads and returns a byte string containing {@code byteCount} bytes from this buffer. This method consumes the
     * specified number of bytes from the buffer.
     *
     * @param byteCount the number of bytes to read
     * @return a byte string containing the read bytes
     * @throws EOFException              if the buffer contains fewer than {@code byteCount} bytes
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public ByteString readByteString(long byteCount) throws EOFException {
        return new ByteString(readByteArray(byteCount));
    }

    /**
     * Finds and returns the index of the first matching option in the specified segment buffer. If a match is found,
     * the matching bytes are consumed from this buffer.
     *
     * @param segmentBuffer the segment buffer containing options to match against
     * @return the index of the matching option, or -1 if no match is found
     * @throws IllegalArgumentException if {@code segmentBuffer} is null
     */
    @Override
    public int select(SegmentBuffer segmentBuffer) {
        int index = selectPrefix(segmentBuffer, false);
        if (index == -1)
            return -1;

        // If the prefix match actually matched a full byte string, consume it and return it.
        int selectedSize = segmentBuffer.byteStrings[index].size();
        try {
            skip(selectedSize);
        } catch (EOFException e) {
            throw new AssertionError();
        }
        return index;
    }

    /**
     * Returns the index of the first matching option in the specified segment buffer. This method performs two
     * synchronized iterations: one through the trie and one through this buffer.
     *
     * @param segmentBuffer   the segment buffer containing options to match against
     * @param selectTruncated if true, returns -2 when a possible result is present but truncated
     * @return the index of the matching option, -1 if no match is found, or -2 if a match is truncated
     * @throws IllegalArgumentException if {@code segmentBuffer} is null
     */
    public int selectPrefix(SegmentBuffer segmentBuffer, boolean selectTruncated) {
        SectionBuffer head = this.head;
        if (head == null) {
            if (selectTruncated)
                return -2; // A result is present but truncated.
            return segmentBuffer.indexOf(ByteString.EMPTY);
        }

        SectionBuffer s = head;
        byte[] data = head.data;
        int pos = head.pos;
        int limit = head.limit;

        int[] trie = segmentBuffer.trie;
        int triePos = 0;

        int prefixIndex = -1;

        navigateTrie: while (true) {
            int scanOrSelect = trie[triePos++];

            int possiblePrefixIndex = trie[triePos++];
            if (possiblePrefixIndex != -1) {
                prefixIndex = possiblePrefixIndex;
            }

            int nextStep;

            if (s == null) {
                break;
            } else if (scanOrSelect < 0) {
                // Scan: take multiple bytes from the buffer and the trie, looking for any mismatch.
                int scanByteCount = -1 * scanOrSelect;
                int trieLimit = triePos + scanByteCount;
                while (true) {
                    int b = data[pos++] & 0xff;
                    if (b != trie[triePos++])
                        return prefixIndex; // Fail 'cause we found a mismatch.
                    boolean scanComplete = (triePos == trieLimit);

                    // Advance to the next buffer segment if this one is exhausted.
                    if (pos == limit) {
                        s = s.next;
                        pos = s.pos;
                        data = s.data;
                        limit = s.limit;
                        if (s == head) {
                            if (!scanComplete)
                                break navigateTrie; // We were exhausted before the scan completed.
                            s = null; // We were exhausted at the end of the scan.
                        }
                    }

                    if (scanComplete) {
                        nextStep = trie[triePos];
                        break;
                    }
                }
            } else {
                // Select: take one byte from the buffer and find a match in the trie.
                int selectChoiceCount = scanOrSelect;
                int b = data[pos++] & 0xff;
                int selectLimit = triePos + selectChoiceCount;
                while (true) {
                    if (triePos == selectLimit)
                        return prefixIndex; // Fail 'cause we didn't find a match.

                    if (b == trie[triePos]) {
                        nextStep = trie[triePos + selectChoiceCount];
                        break;
                    }

                    triePos++;
                }

                // Advance to the next buffer segment if this one is exhausted.
                if (pos == limit) {
                    s = s.next;
                    pos = s.pos;
                    data = s.data;
                    limit = s.limit;
                    if (s == head) {
                        s = null; // No more segments! The next trie node will be our last.
                    }
                }
            }

            if (nextStep >= 0)
                return nextStep; // Found a matching option.
            triePos = -nextStep; // Found another node to continue the search.
        }

        // We break out of the loop above when we've exhausted the buffer without exhausting the trie.
        if (selectTruncated)
            return -2; // The buffer is a prefix of at least one option.
        return prefixIndex; // Return any matches we encountered while searching for a deeper match.
    }

    /**
     * Reads {@code byteCount} bytes from this buffer and writes them to the specified sink. This method consumes the
     * bytes from this buffer.
     *
     * @param sink      the sink to write to
     * @param byteCount the number of bytes to read
     * @throws EOFException              if the buffer contains fewer than {@code byteCount} bytes
     * @throws IllegalArgumentException  if {@code sink} is null
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public void readFully(Buffer sink, long byteCount) throws EOFException {
        if (size < byteCount) {
            sink.write(this, size); // Exhaust ourselves.
            throw new EOFException();
        }
        sink.write(this, byteCount);
    }

    /**
     * Reads all remaining bytes from this buffer and writes them to the specified sink. This method consumes all bytes
     * from this buffer.
     *
     * @param sink the sink to write to
     * @return the number of bytes read and written
     * @throws IOException              if an I/O error occurs while writing
     * @throws IllegalArgumentException if {@code sink} is null
     */
    @Override
    public long readAll(Sink sink) throws IOException {
        long byteCount = size;
        if (byteCount > 0) {
            sink.write(this, byteCount);
        }
        return byteCount;
    }

    /**
     * Reads and returns a UTF-8 string containing all remaining bytes in this buffer. This method consumes all bytes
     * from the buffer.
     *
     * @return a UTF-8 string containing all remaining bytes
     */
    @Override
    public String readUtf8() {
        try {
            return readString(size, Charset.UTF_8);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Reads and returns a UTF-8 string containing {@code byteCount} bytes from this buffer. This method consumes the
     * specified number of bytes from the buffer.
     *
     * @param byteCount the number of bytes to read
     * @return a UTF-8 string containing the read bytes
     * @throws EOFException              if the buffer contains fewer than {@code byteCount} bytes
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public String readUtf8(long byteCount) throws EOFException {
        return readString(byteCount, Charset.UTF_8);
    }

    /**
     * Reads and returns a string containing all remaining bytes in this buffer, decoded using the specified charset.
     * This method consumes all bytes from the buffer.
     *
     * @param charset the charset to use for decoding
     * @return a string containing all remaining bytes
     * @throws IllegalArgumentException if {@code charset} is null
     */
    @Override
    public String readString(java.nio.charset.Charset charset) {
        try {
            return readString(size, charset);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Reads and returns a string containing {@code byteCount} bytes from this buffer, decoded using the specified
     * charset. This method consumes the specified number of bytes.
     *
     * @param byteCount the number of bytes to read
     * @param charset   the charset to use for decoding
     * @return a string containing the read bytes
     * @throws EOFException              if the buffer contains fewer than {@code byteCount} bytes
     * @throws IllegalArgumentException  if {@code charset} is null
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public String readString(long byteCount, java.nio.charset.Charset charset) throws EOFException {
        IoKit.checkOffsetAndCount(size, 0, byteCount);
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }
        if (byteCount == 0)
            return Normal.EMPTY;

        SectionBuffer s = head;
        if (s.pos + byteCount > s.limit) {
            // If the string spans multiple segments, delegate to readBytes().
            return new String(readByteArray(byteCount), charset);
        }

        String result = new String(s.data, s.pos, (int) byteCount, charset);
        s.pos += byteCount;
        size -= byteCount;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return result;
    }

    /**
     * Reads and returns a UTF-8 string from this buffer up to the next line break. Returns null if the buffer is empty.
     * The line break characters are consumed but not included in the returned string. Handles both LF (\n) and CRLF
     * (\r\n) line endings.
     *
     * @return a UTF-8 string up to the next line break, or null if the buffer is empty
     * @throws EOFException if an I/O error occurs
     */
    @Override
    public String readUtf8Line() throws EOFException {
        long newline = indexOf((byte) Symbol.C_LF);

        if (newline == -1) {
            return size != 0 ? readUtf8(size) : null;
        }

        return readUtf8Line(newline);
    }

    /**
     * Reads and returns a UTF-8 string from this buffer up to the next line break. Throws an EOFException if the buffer
     * does not contain a line break. The line break characters are consumed but not included in the returned string.
     *
     * @return a UTF-8 string up to the next line break
     * @throws EOFException if the buffer does not contain a line break
     */
    @Override
    public String readUtf8LineStrict() throws EOFException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    /**
     * Reads and returns a UTF-8 string from this buffer up to the next line break, limiting the search to {@code limit}
     * bytes. Throws an EOFException if the buffer does not contain a line break within the limit. The line break
     * characters are consumed but not included in the returned string.
     *
     * @param limit the maximum number of bytes to search for a line break
     * @return a UTF-8 string up to the next line break
     * @throws EOFException             if the buffer does not contain a line break within the limit
     * @throws IllegalArgumentException if {@code limit} is negative
     */
    @Override
    public String readUtf8LineStrict(long limit) throws EOFException {
        if (limit < 0)
            throw new IllegalArgumentException("limit < 0: " + limit);
        long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
        long newline = indexOf((byte) Symbol.C_LF, 0, scanLength);
        if (newline != -1)
            return readUtf8Line(newline);
        if (scanLength < size() && getByte(scanLength - 1) == Symbol.C_CR && getByte(scanLength) == Symbol.C_LF) {
            return readUtf8Line(scanLength);
        }
        Buffer data = new Buffer();
        copyTo(data, 0, Math.min(Normal._32, size()));
        throw new EOFException(
                "\\n not found: limit=" + Math.min(size(), limit) + " content=" + data.readByteString().hex() + '…');
    }

    /**
     * Internal method to read a UTF-8 line ending at the specified position.
     *
     * @param newline the position of the line break
     * @return a UTF-8 string up to the line break
     * @throws EOFException if an I/O error occurs
     */
    public String readUtf8Line(long newline) throws EOFException {
        if (newline > 0 && getByte(newline - 1) == Symbol.C_CR) {
            String result = readUtf8((newline - 1));
            skip(2);
            return result;

        } else {
            String result = readUtf8(newline);
            skip(1);
            return result;
        }
    }

    /**
     * Reads and returns a UTF-8 code point from this buffer. Handles 1 to 4 byte UTF-8 sequences. Invalid sequences are
     * replaced with the Unicode replacement character (U+FFFD).
     *
     * @return the UTF-8 code point read
     * @throws EOFException if the buffer is empty or contains an incomplete UTF-8 sequence
     */
    @Override
    public int readUtf8CodePoint() throws EOFException {
        if (size == 0)
            throw new EOFException();

        byte b0 = getByte(0);
        int codePoint;
        int byteCount;
        int min;

        if ((b0 & 0x80) == 0) {
            // 0xxxxxxx.
            codePoint = b0 & 0x7f;
            byteCount = 1; // 7 bits (ASCII).
            min = 0x0;

        } else if ((b0 & 0xe0) == 0xc0) {
            // 0x110xxxxx
            codePoint = b0 & 0x1f;
            byteCount = 2; // 11 bits (5 + 6).
            min = 0x80;

        } else if ((b0 & 0xf0) == 0xe0) {
            // 0x1110xxxx
            codePoint = b0 & 0x0f;
            byteCount = 3; // 16 bits (4 + 6 + 6).
            min = 0x800;

        } else if ((b0 & 0xf8) == 0xf0) {
            // 0x11110xxx
            codePoint = b0 & 0x07;
            byteCount = 4; // 21 bits (3 + 6 + 6 + 6).
            min = 0x10000;

        } else {
            skip(1);
            return REPLACEMENT_CHARACTER;
        }

        if (size < byteCount) {
            throw new EOFException("size < " + byteCount + ": " + size + " (to read code point prefixed 0x"
                    + Integer.toHexString(b0) + Symbol.PARENTHESE_RIGHT);
        }

        for (int i = 1; i < byteCount; i++) {
            byte b = getByte(i);
            if ((b & 0xc0) == 0x80) {
                // 0x10xxxxxx
                codePoint <<= 6;
                codePoint |= b & 0x3f;
            } else {
                skip(i);
                return REPLACEMENT_CHARACTER;
            }
        }

        skip(byteCount);

        if (codePoint > 0x10ffff) {
            return REPLACEMENT_CHARACTER;
        }

        if (codePoint >= 0xd800 && codePoint <= 0xdfff) {
            return REPLACEMENT_CHARACTER;
        }

        if (codePoint < min) {
            return REPLACEMENT_CHARACTER;
        }

        return codePoint;
    }

    /**
     * Reads and returns a byte array containing all remaining bytes in this buffer. This method consumes all bytes from
     * the buffer.
     *
     * @return a byte array containing all remaining bytes
     */
    @Override
    public byte[] readByteArray() {
        try {
            return readByteArray(size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Reads and returns a byte array containing {@code byteCount} bytes from this buffer. This method consumes the
     * specified number of bytes from the buffer.
     *
     * @param byteCount the number of bytes to read
     * @return a byte array containing the read bytes
     * @throws EOFException              if the buffer contains fewer than {@code byteCount} bytes
     * @throws IllegalArgumentException  if {@code byteCount} is greater than Integer.MAX_VALUE
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public byte[] readByteArray(long byteCount) throws EOFException {
        IoKit.checkOffsetAndCount(size, 0, byteCount);
        if (byteCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        }

        byte[] result = new byte[(int) byteCount];
        readFully(result);
        return result;
    }

    /**
     * Reads bytes from this buffer into the specified byte array. Reads up to {@code sink.length} bytes or until the
     * buffer is empty.
     *
     * @param sink the byte array to read into
     * @return the number of bytes read, or -1 if the buffer is empty
     * @throws NullPointerException if {@code sink} is null
     */
    @Override
    public int read(byte[] sink) {
        return read(sink, 0, sink.length);
    }

    /**
     * Reads bytes from this buffer into the specified byte array until it is full.
     *
     * @param sink the byte array to read into
     * @throws EOFException         if the buffer becomes empty before the array is full
     * @throws NullPointerException if {@code sink} is null
     */
    @Override
    public void readFully(byte[] sink) throws EOFException {
        int offset = 0;
        while (offset < sink.length) {
            int read = read(sink, offset, sink.length - offset);
            if (read == -1)
                throw new EOFException();
            offset += read;
        }
    }

    /**
     * Reads bytes from this buffer into the specified byte array.
     *
     * @param sink      the byte array to read into
     * @param offset    the starting offset in the byte array
     * @param byteCount the maximum number of bytes to read
     * @return the number of bytes read, or -1 if the buffer is empty
     * @throws NullPointerException      if {@code sink} is null
     * @throws IndexOutOfBoundsException if {@code offset} or {@code byteCount} are invalid
     */
    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        IoKit.checkOffsetAndCount(sink.length, offset, byteCount);
        SectionBuffer s = head;
        if (null == s) {
            return -1;
        }
        int toCopy = Math.min(byteCount, s.limit - s.pos);
        System.arraycopy(s.data, s.pos, sink, offset, toCopy);

        s.pos += toCopy;
        size -= toCopy;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return toCopy;
    }

    /**
     * Reads bytes from this buffer into the specified byte buffer. Reads up to {@code sink.remaining()} bytes or until
     * the buffer is empty.
     *
     * @param sink the byte buffer to read into
     * @return the number of bytes read, or -1 if the buffer is empty
     * @throws IOException          if an I/O error occurs
     * @throws NullPointerException if {@code sink} is null
     */
    @Override
    public int read(java.nio.ByteBuffer sink) throws IOException {
        SectionBuffer s = head;
        if (null == s) {
            return -1;
        }

        int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
        sink.put(s.data, s.pos, toCopy);

        s.pos += toCopy;
        size -= toCopy;

        if (s.pos == s.limit) {
            head = s.pop();
            LifeCycle.recycle(s);
        }

        return toCopy;
    }

    /**
     * Discards all bytes in this buffer. After calling this method, the buffer will be empty.
     */
    public final void clear() {
        try {
            skip(size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Discards {@code byteCount} bytes from the beginning of this buffer.
     *
     * @param byteCount the number of bytes to discard
     * @throws EOFException             if the buffer contains fewer than {@code byteCount} bytes
     * @throws IllegalArgumentException if {@code byteCount} is negative
     */
    @Override
    public void skip(long byteCount) throws EOFException {
        while (byteCount > 0) {
            if (null == head) {
                throw new EOFException();
            }

            int toSkip = (int) Math.min(byteCount, head.limit - head.pos);
            size -= toSkip;
            byteCount -= toSkip;
            head.pos += toSkip;

            if (head.pos == head.limit) {
                SectionBuffer toRecycle = head;
                head = toRecycle.pop();
                LifeCycle.recycle(toRecycle);
            }
        }
    }

    /**
     * Writes the specified byte string to this buffer.
     *
     * @param byteString the byte string to write
     * @return this buffer
     * @throws IllegalArgumentException if {@code byteString} is null
     */
    @Override
    public Buffer write(ByteString byteString) {
        if (null == byteString) {
            throw new IllegalArgumentException("byteString == null");
        }
        byteString.write(this);
        return this;
    }

    /**
     * Writes the specified string to this buffer using UTF-8 encoding.
     *
     * @param string the string to write
     * @return this buffer
     * @throws IllegalArgumentException if {@code string} is null
     */
    @Override
    public Buffer writeUtf8(String string) {
        return writeUtf8(string, 0, string.length());
    }

    /**
     * Writes a substring of the specified string to this buffer using UTF-8 encoding.
     *
     * @param string     the string to write
     * @param beginIndex the beginning index, inclusive
     * @param endIndex   the ending index, exclusive
     * @return this buffer
     * @throws IllegalArgumentException  if {@code string} is null or indices are invalid
     * @throws IndexOutOfBoundsException if indices are out of bounds
     */
    @Override
    public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
        if (null == string) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }

        for (int i = beginIndex; i < endIndex;) {
            int c = string.charAt(i);

            if (c < 0x80) {
                SectionBuffer tail = writableSegment(1);
                byte[] data = tail.data;
                int segmentOffset = tail.limit - i;
                int runLimit = Math.min(endIndex, SectionBuffer.SIZE - segmentOffset);

                // Emit a 7-bit character with 1 byte.
                data[segmentOffset + i++] = (byte) c; // 0xxxxxxx

                // Fast-path contiguous runs of ASCII characters. This is ugly, but yields a ~4x performance
                // improvement over independent calls to writeByte().
                while (i < runLimit) {
                    c = string.charAt(i);
                    if (c >= 0x80)
                        break;
                    data[segmentOffset + i++] = (byte) c; // 0xxxxxxx
                }

                int runSize = i + segmentOffset - tail.limit; // Equivalent to i - (previous i).
                tail.limit += runSize;
                size += runSize;

            } else if (c < 0x800) {
                // Emit a 11-bit character with 2 bytes.
                writeByte(c >> 6 | 0xc0); // 110xxxxx
                writeByte(c & 0x3f | 0x80); // 10xxxxxx
                i++;

            } else if (c < 0xd800 || c > 0xdfff) {
                // Emit a 16-bit character with 3 bytes.
                writeByte(c >> 12 | 0xe0); // 1110xxxx
                writeByte(c >> 6 & 0x3f | 0x80); // 10xxxxxx
                writeByte(c & 0x3f | 0x80); // 10xxxxxx
                i++;

            } else {
                // c is a surrogate. Make sure it is a high surrogate & that its successor is a low
                // surrogate. If not, the UTF-16 is invalid, in which case we emit a replacement character.
                int low = i + 1 < endIndex ? string.charAt(i + 1) : 0;
                if (c > 0xdbff || low < 0xdc00 || low > 0xdfff) {
                    writeByte(Symbol.C_QUESTION_MARK);
                    i++;
                    continue;
                }

                // UTF-16 high surrogate: 110110xxxxxxxxxx (10 bits)
                // UTF-16 low surrogate: 110111yyyyyyyyyy (10 bits)
                // Unicode code point: 00010000000000000000 + xxxxxxxxxxyyyyyyyyyy (21 bits)
                int codePoint = 0x010000 + ((c & ~0xd800) << 10 | low & ~0xdc00);

                // Emit a 21-bit character with 4 bytes.
                writeByte(codePoint >> 18 | 0xf0); // 11110xxx
                writeByte(codePoint >> 12 & 0x3f | 0x80); // 10xxxxxx
                writeByte(codePoint >> 6 & 0x3f | 0x80); // 10xxyyyy
                writeByte(codePoint & 0x3f | 0x80); // 10yyyyyy
                i += 2;
            }
        }

        return this;
    }

    /**
     * Writes the specified Unicode code point to this buffer using UTF-8 encoding.
     *
     * @param codePoint the Unicode code point to write
     * @return this buffer
     * @throws IllegalArgumentException if the code point is invalid
     */
    @Override
    public Buffer writeUtf8CodePoint(int codePoint) {
        if (codePoint < 0x80) {
            // Emit a 7-bit code point with 1 byte.
            writeByte(codePoint);

        } else if (codePoint < 0x800) {
            // Emit a 11-bit code point with 2 bytes.
            writeByte(codePoint >> 6 | 0xc0); // 110xxxxx
            writeByte(codePoint & 0x3f | 0x80); // 10xxxxxx

        } else if (codePoint < 0x10000) {
            if (codePoint >= 0xd800 && codePoint <= 0xdfff) {
                // Emit a replacement character for a partial surrogate.
                writeByte(Symbol.C_QUESTION_MARK);
            } else {
                // Emit a 16-bit code point with 3 bytes.
                writeByte(codePoint >> 12 | 0xe0); // 1110xxxx
                writeByte(codePoint >> 6 & 0x3f | 0x80); // 10xxxxxx
                writeByte(codePoint & 0x3f | 0x80); // 10xxxxxx
            }

        } else if (codePoint <= 0x10ffff) {
            // Emit a 21-bit code point with 4 bytes.
            writeByte(codePoint >> 18 | 0xf0); // 11110xxx
            writeByte(codePoint >> 12 & 0x3f | 0x80); // 10xxxxxx
            writeByte(codePoint >> 6 & 0x3f | 0x80); // 10xxxxxx
            writeByte(codePoint & 0x3f | 0x80); // 10xxxxxx

        } else {
            throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(codePoint));
        }

        return this;
    }

    /**
     * Writes the specified string to this buffer using the specified charset.
     *
     * @param string  the string to write
     * @param charset the charset to use for encoding
     * @return this buffer
     * @throws IllegalArgumentException if {@code string} or {@code charset} is null
     */
    @Override
    public Buffer writeString(String string, java.nio.charset.Charset charset) {
        return writeString(string, 0, string.length(), charset);
    }

    /**
     * Writes a substring of the specified string to this buffer using the specified charset.
     *
     * @param string     the string to write
     * @param beginIndex the beginning index, inclusive
     * @param endIndex   the ending index, exclusive
     * @param charset    the charset to use for encoding
     * @return this buffer
     * @throws IllegalArgumentException  if {@code string} or {@code charset} is null or indices are invalid
     * @throws IndexOutOfBoundsException if indices are out of bounds
     */
    @Override
    public Buffer writeString(String string, int beginIndex, int endIndex, java.nio.charset.Charset charset) {
        if (null == string) {
            throw new IllegalArgumentException("string == null");
        }
        if (beginIndex < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
        }
        if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        }
        if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        if (charset.equals(Symbol.C_SLASH)) {
            return writeUtf8(string, beginIndex, endIndex);
        }
        byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
        return write(data, 0, data.length);
    }

    /**
     * Writes the specified byte array to this buffer.
     *
     * @param source the byte array to write
     * @return this buffer
     * @throws IllegalArgumentException if {@code source} is null
     */
    @Override
    public Buffer write(byte[] source) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        return write(source, 0, source.length);
    }

    /**
     * Writes a portion of the specified byte array to this buffer.
     *
     * @param source    the byte array to write
     * @param offset    the starting offset in the byte array
     * @param byteCount the number of bytes to write
     * @return this buffer
     * @throws IllegalArgumentException  if {@code source} is null
     * @throws IndexOutOfBoundsException if {@code offset} or {@code byteCount} are invalid
     */
    @Override
    public Buffer write(byte[] source, int offset, int byteCount) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        IoKit.checkOffsetAndCount(source.length, offset, byteCount);
        int limit = offset + byteCount;
        while (offset < limit) {
            SectionBuffer tail = writableSegment(1);

            int toCopy = Math.min(limit - offset, SectionBuffer.SIZE - tail.limit);
            System.arraycopy(source, offset, tail.data, tail.limit, toCopy);

            offset += toCopy;
            tail.limit += toCopy;
        }

        size += byteCount;
        return this;
    }

    /**
     * Writes bytes from the specified byte buffer to this buffer. Reads all remaining bytes from the source buffer.
     *
     * @param source the byte buffer to read from
     * @return the number of bytes written
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if {@code source} is null
     */
    @Override
    public int write(java.nio.ByteBuffer source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }

        int byteCount = source.remaining();
        int remaining = byteCount;
        while (remaining > 0) {
            SectionBuffer tail = writableSegment(1);

            int toCopy = Math.min(remaining, SectionBuffer.SIZE - tail.limit);
            source.get(tail.data, tail.limit, toCopy);

            remaining -= toCopy;
            tail.limit += toCopy;
        }

        size += byteCount;
        return byteCount;
    }

    /**
     * Reads all bytes from the specified source and writes them to this buffer. Continues reading until the source is
     * exhausted.
     *
     * @param source the source to read from
     * @return the total number of bytes read
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if {@code source} is null
     */
    @Override
    public long writeAll(Source source) throws IOException {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(this, SectionBuffer.SIZE)) != -1;) {
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }

    /**
     * Reads {@code byteCount} bytes from the specified source and writes them to this buffer.
     *
     * @param source    the source to read from
     * @param byteCount the number of bytes to read
     * @return this buffer
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if {@code source} is null
     * @throws EOFException             if the end of the source is reached before reading {@code byteCount} bytes
     */
    @Override
    public BufferSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(this, byteCount);
            if (read == -1)
                throw new EOFException();
            byteCount -= read;
        }
        return this;
    }

    /**
     * Writes a single byte to this buffer.
     *
     * @param b the byte to write
     * @return this buffer
     */
    @Override
    public Buffer writeByte(int b) {
        SectionBuffer tail = writableSegment(1);
        tail.data[tail.limit++] = (byte) b;
        size += 1;
        return this;
    }

    /**
     * Writes a 16-bit big-endian short value to this buffer.
     *
     * @param s the short value to write
     * @return this buffer
     */
    @Override
    public Buffer writeShort(int s) {
        SectionBuffer tail = writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((s >>> 8) & 0xff);
        data[limit++] = (byte) (s & 0xff);
        tail.limit = limit;
        size += 2;
        return this;
    }

    /**
     * Writes a 16-bit little-endian short value to this buffer.
     *
     * @param s the short value to write
     * @return this buffer
     */
    @Override
    public Buffer writeShortLe(int s) {
        return writeShort(IoKit.reverseBytesShort((short) s));
    }

    /**
     * Writes a 32-bit big-endian integer value to this buffer.
     *
     * @param i the integer value to write
     * @return this buffer
     */
    @Override
    public Buffer writeInt(int i) {
        SectionBuffer tail = writableSegment(4);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((i >>> 24) & 0xff);
        data[limit++] = (byte) ((i >>> 16) & 0xff);
        data[limit++] = (byte) ((i >>> 8) & 0xff);
        data[limit++] = (byte) (i & 0xff);
        tail.limit = limit;
        size += 4;
        return this;
    }

    /**
     * Writes a 32-bit little-endian integer value to this buffer.
     *
     * @param i the integer value to write
     * @return this buffer
     */
    @Override
    public Buffer writeIntLe(int i) {
        return writeInt(IoKit.reverseBytesInt(i));
    }

    /**
     * Writes a 64-bit big-endian long value to this buffer.
     *
     * @param v the long value to write
     * @return this buffer
     */
    @Override
    public Buffer writeLong(long v) {
        SectionBuffer tail = writableSegment(8);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((v >>> 56L) & 0xff);
        data[limit++] = (byte) ((v >>> 48L) & 0xff);
        data[limit++] = (byte) ((v >>> 40L) & 0xff);
        data[limit++] = (byte) ((v >>> 32L) & 0xff);
        data[limit++] = (byte) ((v >>> 24L) & 0xff);
        data[limit++] = (byte) ((v >>> 16L) & 0xff);
        data[limit++] = (byte) ((v >>> 8L) & 0xff);
        data[limit++] = (byte) (v & 0xff);
        tail.limit = limit;
        size += 8;
        return this;
    }

    /**
     * Writes a 64-bit little-endian long value to this buffer.
     *
     * @param v the long value to write
     * @return this buffer
     */
    @Override
    public Buffer writeLongLe(long v) {
        return writeLong(IoKit.reverseBytesLong(v));
    }

    /**
     * Writes a decimal long value to this buffer.
     *
     * @param v the long value to write
     * @return this buffer
     */
    @Override
    public Buffer writeDecimalLong(long v) {
        if (v == 0) {
            return writeByte(Symbol.C_ZERO);
        }

        boolean negative = false;
        if (v < 0) {
            v = -v;
            if (v < 0) {
                return writeUtf8("-9223372036854775808");
            }
            negative = true;
        }

        int width = v < 100000000L
                ? v < 10000L ? v < 100L ? v < 10L ? 1 : 2 : v < 1000L ? 3 : 4
                        : v < 1000000L ? v < 100000L ? 5 : 6 : v < 10000000L ? 7 : 8
                : v < 1000000000000L ? v < 10000000000L ? v < 1000000000L ? 9 : 10 : v < 100000000000L ? 11 : 12
                        : v < 1000000000000000L ? v < 10000000000000L ? 13 : v < 100000000000000L ? 14 : 15
                                : v < 100000000000000000L ? v < 10000000000000000L ? 16 : 17
                                        : v < 1000000000000000000L ? 18 : 19;
        if (negative) {
            ++width;
        }

        SectionBuffer tail = writableSegment(width);
        byte[] data = tail.data;
        int pos = tail.limit + width;
        while (v != 0) {
            int digit = (int) (v % 10);
            data[--pos] = ByteKit.toBytes(Normal.DIGITS_16_LOWER)[digit];
            v /= 10;
        }
        if (negative) {
            data[--pos] = Symbol.C_MINUS;
        }

        tail.limit += width;
        this.size += width;
        return this;
    }

    /**
     * Writes an unsigned long value to this buffer in hexadecimal format.
     *
     * @param v the unsigned long value to write
     * @return this buffer
     */
    @Override
    public Buffer writeHexadecimalUnsignedLong(long v) {
        if (v == 0) {
            return writeByte(Symbol.C_ZERO);
        }

        int width = Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4 + 1;

        SectionBuffer tail = writableSegment(width);
        byte[] data = tail.data;
        for (int pos = tail.limit + width - 1, start = tail.limit; pos >= start; pos--) {
            data[pos] = ByteKit.toBytes(Normal.DIGITS_16_LOWER)[(int) (v & 0xF)];
            v >>>= 4;
        }
        tail.limit += width;
        size += width;
        return this;
    }

    /**
     * Returns a tail segment that we can write at least {@code minimumCapacity} bytes to, creating it if necessary.
     *
     * @param minimumCapacity the minimum number of bytes the segment must be able to hold
     * @return a writable segment
     * @throws IllegalArgumentException if {@code minimumCapacity} is invalid
     */
    public SectionBuffer writableSegment(int minimumCapacity) {
        if (minimumCapacity < 1 || minimumCapacity > SectionBuffer.SIZE)
            throw new IllegalArgumentException();

        if (null == head) {
            head = LifeCycle.take(); // Acquire a first segment.
            return head.next = head.prev = head;
        }

        SectionBuffer tail = head.prev;
        if (tail.limit + minimumCapacity > SectionBuffer.SIZE || !tail.owner) {
            tail = tail.push(LifeCycle.take()); // Append a new empty segment to fill up.
        }
        return tail;
    }

    /**
     * Writes {@code byteCount} bytes from the specified source buffer to this buffer. This method consumes the bytes
     * from the source buffer.
     *
     * @param source    the source buffer to read from
     * @param byteCount the number of bytes to write
     * @throws IllegalArgumentException  if {@code source} is null or equals this buffer
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public void write(Buffer source, long byteCount) {
        if (null == source) {
            throw new IllegalArgumentException("source == null");
        }
        if (source == this) {
            throw new IllegalArgumentException("source == this");
        }
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);

        while (byteCount > 0) {
            // Is a prefix of the source's head segment all that we need to move?
            if (byteCount < (source.head.limit - source.head.pos)) {
                SectionBuffer tail = head != null ? head.prev : null;
                if (tail != null && tail.owner
                        && (byteCount + tail.limit - (tail.shared ? 0 : tail.pos) <= SectionBuffer.SIZE)) {
                    // Our existing segments are sufficient. Move bytes from source's head to our tail.
                    source.head.writeTo(tail, (int) byteCount);
                    source.size -= byteCount;
                    size += byteCount;
                    return;
                } else {
                    // We're going to need another segment. Split the source's head
                    // segment in two, then move the first of those two to this buffer.
                    source.head = source.head.split((int) byteCount);
                }
            }

            // Remove the source's head segment and append it to our tail.
            SectionBuffer segmentToMove = source.head;
            long movedByteCount = segmentToMove.limit - segmentToMove.pos;
            source.head = segmentToMove.pop();
            if (head == null) {
                head = segmentToMove;
                head.next = head.prev = head;
            } else {
                SectionBuffer tail = head.prev;
                tail = tail.push(segmentToMove);
                tail.compact();
            }
            source.size -= movedByteCount;
            size += movedByteCount;
            byteCount -= movedByteCount;
        }
    }

    /**
     * Reads {@code byteCount} bytes from this buffer and writes them to the specified sink buffer. This method consumes
     * the bytes from this buffer.
     *
     * @param sink      the sink buffer to write to
     * @param byteCount the number of bytes to read
     * @return the number of bytes read, or -1 if this buffer is empty
     * @throws IllegalArgumentException  if {@code sink} is null
     * @throws IndexOutOfBoundsException if {@code byteCount} is invalid
     */
    @Override
    public long read(Buffer sink, long byteCount) {
        if (null == sink) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (size == 0) {
            return -1L;
        }
        if (byteCount > size) {
            byteCount = size;
        }
        sink.write(this, byteCount);
        return byteCount;
    }

    /**
     * Returns the index of the first occurrence of the specified byte in this buffer.
     *
     * @param b the byte to search for
     * @return the index of the first occurrence of the byte, or -1 if not found
     */
    @Override
    public long indexOf(byte b) {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    /**
     * Returns the index of the first occurrence of the specified byte in this buffer, starting at the specified
     * position.
     *
     * @param b         the byte to search for
     * @param fromIndex the starting position for the search (0-based)
     * @return the index of the first occurrence of the byte, or -1 if not found
     * @throws IndexOutOfBoundsException if {@code fromIndex} is out of bounds
     */
    @Override
    public long indexOf(byte b, long fromIndex) {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    /**
     * Returns the index of the first occurrence of the specified byte in this buffer, within the specified range.
     *
     * @param b         the byte to search for
     * @param fromIndex the starting position for the search (0-based)
     * @param toIndex   the ending position for the search (exclusive)
     * @return the index of the first occurrence of the byte, or -1 if not found
     * @throws IllegalArgumentException  if the range is invalid
     * @throws IndexOutOfBoundsException if indices are out of bounds
     */
    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) {
        if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(
                    String.format("size=%s fromIndex=%s toIndex=%s", size, fromIndex, toIndex));
        }

        if (toIndex > size)
            toIndex = size;
        if (fromIndex == toIndex)
            return -1L;

        SectionBuffer s;
        long offset;

        findSegmentAndOffset: {
            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex;) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        while (offset < toIndex) {
            byte[] data = s.data;
            int limit = (int) Math.min(s.limit, s.pos + toIndex - offset);
            int pos = (int) (s.pos + fromIndex - offset);
            for (; pos < limit; pos++) {
                if (data[pos] == b) {
                    return pos - s.pos + offset;
                }
            }

            offset += (s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
        }

        return -1L;
    }

    /**
     * Returns the index of the first occurrence of the specified byte string in this buffer.
     *
     * @param bytes the byte string to search for
     * @return the index of the first occurrence of the byte string, or -1 if not found
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if {@code bytes} is null or empty
     */
    @Override
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    /**
     * Returns the index of the first occurrence of the specified byte string in this buffer, starting at the specified
     * position.
     *
     * @param bytes     the byte string to search for
     * @param fromIndex the starting position for the search (0-based)
     * @return the index of the first occurrence of the byte string, or -1 if not found
     * @throws IOException               if an I/O error occurs
     * @throws IllegalArgumentException  if {@code bytes} is null or empty
     * @throws IndexOutOfBoundsException if {@code fromIndex} is out of bounds
     */
    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (bytes.size() == 0)
            throw new IllegalArgumentException("bytes is empty");
        if (fromIndex < 0)
            throw new IllegalArgumentException("fromIndex < 0");

        SectionBuffer s;
        long offset;

        findSegmentAndOffset: {
            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex;) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        byte b0 = bytes.getByte(0);
        int bytesSize = bytes.size();
        long resultLimit = size - bytesSize + 1;
        while (offset < resultLimit) {
            byte[] data = s.data;
            int segmentLimit = (int) Math.min(s.limit, s.pos + resultLimit - offset);
            for (int pos = (int) (s.pos + fromIndex - offset); pos < segmentLimit; pos++) {
                if (data[pos] == b0 && rangeEquals(s, pos + 1, bytes, 1, bytesSize)) {
                    return pos - s.pos + offset;
                }
            }

            // Not in this segment. Try the next one.
            offset += (s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
        }

        return -1L;
    }

    /**
     * Returns the index of the first occurrence of any byte from the specified target bytes in this buffer.
     *
     * @param targetBytes the bytes to search for
     * @return the index of the first occurrence of any target byte, or -1 if not found
     * @throws IllegalArgumentException if {@code targetBytes} is null
     */
    @Override
    public long indexOfElement(ByteString targetBytes) {
        return indexOfElement(targetBytes, 0);
    }

    /**
     * Returns the index of the first occurrence of any byte from the specified target bytes in this buffer, starting at
     * the specified position.
     *
     * @param targetBytes the bytes to search for
     * @param fromIndex   the starting position for the search (0-based)
     * @return the index of the first occurrence of any target byte, or -1 if not found
     * @throws IllegalArgumentException  if {@code targetBytes} is null
     * @throws IndexOutOfBoundsException if {@code fromIndex} is out of bounds
     */
    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) {
        if (fromIndex < 0)
            throw new IllegalArgumentException("fromIndex < 0");

        SectionBuffer s;
        long offset;

        findSegmentAndOffset: {

            s = head;
            if (null == s) {
                return -1L;
            } else if (size - fromIndex < fromIndex) {
                offset = size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (s.limit - s.pos);
                }
            } else {
                offset = 0L;
                for (long nextOffset; (nextOffset = offset + (s.limit - s.pos)) < fromIndex;) {
                    s = s.next;
                    offset = nextOffset;
                }
            }
        }

        // Special case searching for one of two bytes. This is a common case for tools like Moshi,
        // which search for pairs of chars like `\r` and `\n` or {@code `"` and `\`. The impact of this
        // optimization is a ~5x speedup for this case without a substantial cost to other cases.
        if (targetBytes.size() == 2) {
            // Scan through the segments, searching for either of the two bytes.
            byte b0 = targetBytes.getByte(0);
            byte b1 = targetBytes.getByte(1);
            while (offset < size) {
                byte[] data = s.data;
                for (int pos = (int) (s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
                    int b = data[pos];
                    if (b == b0 || b == b1) {
                        return pos - s.pos + offset;
                    }
                }

                // Not in this segment. Try the next one.
                offset += (s.limit - s.pos);
                fromIndex = offset;
                s = s.next;
            }
        } else {
            // Scan through the segments, searching for a byte that's also in the array.
            byte[] targetByteArray = targetBytes.internalArray();
            while (offset < size) {
                byte[] data = s.data;
                for (int pos = (int) (s.pos + fromIndex - offset), limit = s.limit; pos < limit; pos++) {
                    int b = data[pos];
                    for (byte t : targetByteArray) {
                        if (b == t)
                            return pos - s.pos + offset;
                    }
                }

                // Not in this segment. Try the next one.
                offset += (s.limit - s.pos);
                fromIndex = offset;
                s = s.next;
            }
        }

        return -1L;
    }

    /**
     * Returns true if the bytes in this buffer starting at the specified offset match the specified byte string.
     *
     * @param offset the starting offset in this buffer (0-based)
     * @param bytes  the byte string to compare with
     * @return true if the bytes match, false otherwise
     * @throws IllegalArgumentException  if {@code bytes} is null
     * @throws IndexOutOfBoundsException if {@code offset} is out of bounds
     */
    @Override
    public boolean rangeEquals(long offset, ByteString bytes) {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    /**
     * Returns true if the bytes in this buffer starting at the specified offset match the specified portion of the byte
     * string.
     *
     * @param offset      the starting offset in this buffer (0-based)
     * @param bytes       the byte string to compare with
     * @param bytesOffset the starting offset in the byte string
     * @param byteCount   the number of bytes to compare
     * @return true if the bytes match, false otherwise
     * @throws IllegalArgumentException  if {@code bytes} is null
     * @throws IndexOutOfBoundsException if any offset or count is invalid
     */
    @Override
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) {
        if (offset < 0 || bytesOffset < 0 || byteCount < 0 || size - offset < byteCount
                || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; i++) {
            if (getByte(offset + i) != bytes.getByte(bytesOffset + i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the range within this buffer starting at {@code segmentPos} in {@code segment} is equal to
     * {@code bytes[bytesOffset..bytesLimit)}.
     *
     * @param segment     the segment to compare
     * @param segmentPos  the starting position in the segment
     * @param bytes       the byte string to compare with
     * @param bytesOffset the starting offset in the byte string
     * @param bytesLimit  the ending offset in the byte string
     * @return true if the bytes match, false otherwise
     */
    private boolean rangeEquals(
            SectionBuffer segment,
            int segmentPos,
            ByteString bytes,
            int bytesOffset,
            int bytesLimit) {
        int segmentLimit = segment.limit;
        byte[] data = segment.data;

        for (int i = bytesOffset; i < bytesLimit;) {
            if (segmentPos == segmentLimit) {
                segment = segment.next;
                data = segment.data;
                segmentPos = segment.pos;
                segmentLimit = segment.limit;
            }

            if (data[segmentPos] != bytes.getByte(i)) {
                return false;
            }

            segmentPos++;
            i++;
        }

        return true;
    }

    /**
     * Flushes this buffer. This implementation does nothing as the buffer is in-memory.
     */
    @Override
    public void flush() {
    }

    /**
     * Returns true if this buffer is open. Buffers are always open unless explicitly closed.
     *
     * @return true if this buffer is open, false otherwise
     */
    @Override
    public boolean isOpen() {
        return true;
    }

    /**
     * Closes this buffer. This implementation does nothing as the buffer is in-memory.
     */
    @Override
    public void close() {
    }

    /**
     * Returns the timeout for this buffer. This implementation returns a timeout with no time limit.
     *
     * @return the timeout for this buffer
     */
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }

    /**
     * For testing purposes only. Returns the sizes of the segments in this buffer.
     *
     * @return a list of segment sizes
     */
    List<Integer> segmentSizes() {
        if (null == head) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        result.add(head.limit - head.pos);
        for (SectionBuffer s = head.next; s != head; s = s.next) {
            result.add(s.limit - s.pos);
        }
        return result;
    }

    /**
     * Returns the 128-bit MD5 hash of this buffer.
     *
     * @return the MD5 hash of this buffer as a byte string
     */
    public ByteString md5() {
        return digest(Algorithm.MD5.getValue());
    }

    /**
     * Returns the 160-bit SHA-1 hash of this buffer.
     *
     * @return the SHA-1 hash of this buffer as a byte string
     */
    public ByteString sha1() {
        return digest(Algorithm.SHA1.getValue());
    }

    /**
     * Returns the 256-bit SHA-256 hash of this buffer.
     *
     * @return the SHA-256 hash of this buffer as a byte string
     */
    public ByteString sha256() {
        return digest(Algorithm.SHA256.getValue());
    }

    /**
     * Returns the 512-bit SHA-512 hash of this buffer.
     *
     * @return the SHA-512 hash of this buffer as a byte string
     */
    public ByteString sha512() {
        return digest(Algorithm.SHA512.getValue());
    }

    /**
     * Computes the digest of this buffer using the specified algorithm.
     *
     * @param algorithm the digest algorithm to use
     * @return the digest of this buffer as a byte string
     * @throws AssertionError if the algorithm is not available
     */
    private ByteString digest(String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (null != head) {
                messageDigest.update(head.data, head.pos, head.limit - head.pos);
                for (SectionBuffer s = head.next; s != head; s = s.next) {
                    messageDigest.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    /**
     * Returns the 160-bit SHA-1 HMAC of this buffer using the specified key.
     *
     * @param key the key to use for the HMAC
     * @return the SHA-1 HMAC of this buffer as a byte string
     * @throws IllegalArgumentException if {@code key} is null
     */
    public ByteString hmacSha1(ByteString key) {
        return hmac(Algorithm.HMACSHA1.getValue(), key);
    }

    /**
     * Returns the 256-bit SHA-256 HMAC of this buffer using the specified key.
     *
     * @param key the key to use for the HMAC
     * @return the SHA-256 HMAC of this buffer as a byte string
     * @throws IllegalArgumentException if {@code key} is null
     */
    public ByteString hmacSha256(ByteString key) {
        return hmac(Algorithm.HMACSHA256.getValue(), key);
    }

    /**
     * Returns the 512-bit SHA-512 HMAC of this buffer using the specified key.
     *
     * @param key the key to use for the HMAC
     * @return the SHA-512 HMAC of this buffer as a byte string
     * @throws IllegalArgumentException if {@code key} is null
     */
    public final ByteString hmacSha512(ByteString key) {
        return hmac(Algorithm.HMACSHA512.getValue(), key);
    }

    /**
     * Computes the HMAC of this buffer using the specified algorithm and key.
     *
     * @param algorithm the HMAC algorithm to use
     * @param key       the key to use for the HMAC
     * @return the HMAC of this buffer as a byte string
     * @throws IllegalArgumentException if {@code key} is null or invalid
     * @throws AssertionError           if the algorithm is not available
     */
    private ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            if (null != head) {
                mac.update(head.data, head.pos, head.limit - head.pos);
                for (SectionBuffer s = head.next; s != head; s = s.next) {
                    mac.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(mac.doFinal());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Compares this buffer to the specified object for equality. Two buffers are considered equal if they contain the
     * same bytes in the same order.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Buffer))
            return false;
        Buffer that = (Buffer) o;
        if (size != that.size)
            return false;
        if (size == 0)
            return true;

        SectionBuffer sa = this.head;
        SectionBuffer sb = that.head;
        int posA = sa.pos;
        int posB = sb.pos;

        for (long pos = 0, count; pos < size; pos += count) {
            count = Math.min(sa.limit - posA, sb.limit - posB);

            for (int i = 0; i < count; i++) {
                if (sa.data[posA++] != sb.data[posB++])
                    return false;
            }

            if (posA == sa.limit) {
                sa = sa.next;
                posA = sa.pos;
            }

            if (posB == sb.limit) {
                sb = sb.next;
                posB = sb.pos;
            }
        }

        return true;
    }

    /**
     * Returns a hash code value for this buffer. The hash code is based on the bytes contained in the buffer.
     *
     * @return a hash code value for this buffer
     */
    @Override
    public int hashCode() {
        SectionBuffer s = head;
        if (null == s) {
            return 0;
        }
        int result = 1;
        do {
            for (int pos = s.pos, limit = s.limit; pos < limit; pos++) {
                result = 31 * result + s.data[pos];
            }
            s = s.next;
        } while (s != head);
        return result;
    }

    /**
     * Returns a human-readable string that describes the contents of this buffer. The format of the returned string
     * depends on the buffer contents.
     *
     * @return a string representation of this buffer
     */
    @Override
    public String toString() {
        return snapshot().toString();
    }

    /**
     * Returns a deep copy of this buffer. The new buffer contains the same bytes but can be modified independently.
     *
     * @return a deep copy of this buffer
     */
    @Override
    public Buffer clone() {
        Buffer result = new Buffer();
        if (size == 0)
            return result;

        result.head = head.sharedCopy();
        result.head.next = result.head.prev = result.head;
        for (SectionBuffer s = head.next; s != head; s = s.next) {
            result.head.prev.push(s.sharedCopy());
        }
        result.size = size;
        return result;
    }

    /**
     * Returns an immutable copy of this buffer as a byte string.
     *
     * @return an immutable copy of this buffer as a byte string
     * @throws IllegalArgumentException if the buffer size exceeds Integer.MAX_VALUE
     */
    public ByteString snapshot() {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + size);
        }
        return snapshot((int) size);
    }

    /**
     * Returns an immutable copy of the first {@code byteCount} bytes of this buffer as a byte string.
     *
     * @param byteCount the number of bytes to include in the snapshot
     * @return an immutable copy of the specified bytes as a byte string
     * @throws IllegalArgumentException if {@code byteCount} is invalid
     */
    public ByteString snapshot(int byteCount) {
        if (byteCount == 0)
            return ByteString.EMPTY;
        return new ByteBuffer(this, byteCount);
    }

    /**
     * Returns a new unsafe cursor for reading this buffer. The cursor provides direct access to the underlying segments
     * for efficient operations.
     *
     * @return a new unsafe cursor for reading this buffer
     */
    public UnsafeCursor readUnsafe() {
        return readUnsafe(new UnsafeCursor());
    }

    /**
     * Attaches the specified unsafe cursor to this buffer for reading. The cursor provides direct access to the
     * underlying segments for efficient operations.
     *
     * @param unsafeCursor the cursor to attach
     * @return the attached cursor
     * @throws IllegalStateException if the cursor is already attached to a buffer
     */
    public UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
        if (null != unsafeCursor.buffer) {
            throw new IllegalStateException("already attached to a buffer");
        }

        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = false;
        return unsafeCursor;
    }

    /**
     * Returns a new unsafe cursor for reading and writing this buffer. The cursor provides direct access to the
     * underlying segments for efficient operations.
     *
     * @return a new unsafe cursor for reading and writing this buffer
     */
    public UnsafeCursor readAndWriteUnsafe() {
        return readAndWriteUnsafe(new UnsafeCursor());
    }

    /**
     * Attaches the specified unsafe cursor to this buffer for reading and writing. The cursor provides direct access to
     * the underlying segments for efficient operations.
     *
     * @param unsafeCursor the cursor to attach
     * @return the attached cursor
     * @throws IllegalStateException if the cursor is already attached to a buffer
     */
    public UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
        if (null != unsafeCursor.buffer) {
            throw new IllegalStateException("already attached to a buffer");
        }

        unsafeCursor.buffer = this;
        unsafeCursor.readWrite = true;
        return unsafeCursor;
    }

    /**
     * An unsafe cursor that provides direct access to the underlying segments of a buffer. This class allows for
     * efficient reading and writing of buffer data without the overhead of the normal buffer API, but it requires
     * careful handling to avoid data corruption.
     *
     * <p>
     * Unsafe cursors are intended for advanced use cases where performance is critical. They provide direct access to
     * the segment data arrays, bypassing the normal bounds checking and segment management of the Buffer class.
     *
     * <p>
     * Example usage:
     * 
     * <pre>{@code
     * try (UnsafeCursor cursor = buffer.readUnsafe()) {
     *     while (cursor.next() != -1) {
     *         // Process cursor.data[cursor.start ... cursor.end)
     *     }
     * }
     * }</pre>
     */
    public static final class UnsafeCursor implements Closeable {

        /**
         * Constructs a new UnsafeCursor.
         */
        public UnsafeCursor() {
        }

        /**
         * The buffer this cursor is attached to.
         */
        public Buffer buffer;

        /**
         * True if this cursor can write to the buffer, false if it can only read.
         */
        public boolean readWrite;

        /**
         * The current offset in the buffer.
         */
        public long offset = -1L;

        /**
         * The data array of the current segment.
         */
        public byte[] data;

        /**
         * The starting position of the readable data in the current segment.
         */
        public int start = -1;

        /**
         * The ending position of the readable data in the current segment.
         */
        public int end = -1;

        /**
         * The current segment.
         */
        private SectionBuffer segment;

        /**
         * Seeks to the next range of bytes, advancing the offset by {@code end - start}. Returns the size of the
         * readable range (at least 1), or -1 if we have reached the end of the buffer and there are no more bytes to
         * read.
         *
         * @return the size of the readable range, or -1 if at the end of the buffer
         * @throws IllegalStateException if the cursor is not attached to a buffer
         */
        public int next() {
            if (offset == buffer.size)
                throw new IllegalStateException();
            if (offset == -1L)
                return seek(0L);
            return seek(offset + (end - start));
        }

        /**
         * Repositions the cursor so that the data at {@code offset} is readable at {@code data[start]}. Returns the
         * number of bytes readable in {@code data} (at least 1), or -1 if there are no data to read.
         *
         * @param offset the new offset in the buffer (0-based)
         * @return the number of bytes readable in the current segment, or -1 if at the end of the buffer
         * @throws IllegalStateException     if the cursor is not attached to a buffer
         * @throws IndexOutOfBoundsException if {@code offset} is out of bounds
         */
        public int seek(long offset) {
            if (offset < -1 || offset > buffer.size) {
                throw new ArrayIndexOutOfBoundsException(String.format("offset=%s > size=%s", offset, buffer.size));
            }

            if (offset == -1 || offset == buffer.size) {
                this.segment = null;
                this.offset = offset;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return -1;
            }

            // Navigate to the segment that contains `offset`. Start from our current segment if possible.
            long min = 0L;
            long max = buffer.size;
            SectionBuffer head = buffer.head;
            SectionBuffer tail = buffer.head;
            if (null != this.segment) {
                long segmentOffset = this.offset - (this.start - this.segment.pos);
                if (segmentOffset > offset) {
                    // Set the cursor segment to be the 'end'
                    max = segmentOffset;
                    tail = this.segment;
                } else {
                    // Set the cursor segment to be the 'beginning'
                    min = segmentOffset;
                    head = this.segment;
                }
            }

            SectionBuffer next;
            long nextOffset;
            if (max - offset > offset - min) {
                // Start at the 'beginning' and search forwards
                next = head;
                nextOffset = min;
                while (offset >= nextOffset + (next.limit - next.pos)) {
                    nextOffset += (next.limit - next.pos);
                    next = next.next;
                }
            } else {
                // Start at the 'end' and search backwards
                next = tail;
                nextOffset = max;
                while (nextOffset > offset) {
                    next = next.prev;
                    nextOffset -= (next.limit - next.pos);
                }
            }

            // If we're going to write and our segment is shared, swap it for a read-write one.
            if (readWrite && next.shared) {
                SectionBuffer unsharedNext = next.unsharedCopy();
                if (buffer.head == next) {
                    buffer.head = unsharedNext;
                }
                next = next.push(unsharedNext);
                next.prev.pop();
            }

            // Update this cursor to the requested offset within the found segment.
            this.segment = next;
            this.offset = offset;
            this.data = next.data;
            this.start = next.pos + (int) (offset - nextOffset);
            this.end = next.limit;
            return end - start;
        }

        /**
         * Changes the size of the buffer so that it equals {@code newSize} by either adding new capacity at the end or
         * truncating the buffer at the end. Newly added capacity may span multiple segments.
         *
         * <p>
         * As a side-effect this cursor will {@link #seek seek}. If the buffer is being enlarged it will move
         * {@link #offset} to the first byte of newly-added capacity. This is the size of the buffer prior to the
         * {@code resizeBuffer()} call. If the buffer is being shrunk it will move {@link #offset} to the end of the
         * buffer.
         *
         * <p>
         * <strong>Warning:</strong> it is the caller's responsibility to write new data to every byte of the
         * newly-allocated capacity. Failure to do so may cause serious security problems as the data in the returned
         * buffers is not zero filled. Buffers may contain dirty pooled segments that hold very sensitive data from
         * other parts of the current process.
         *
         * @param newSize the new size of the buffer
         * @return the previous size of the buffer
         * @throws IllegalStateException    if the cursor is not attached to a buffer or is read-only
         * @throws IllegalArgumentException if {@code newSize} is negative
         */
        public long resizeBuffer(long newSize) {
            if (buffer == null) {
                throw new IllegalStateException("not attached to a buffer");
            }
            if (!readWrite) {
                throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
            }

            long oldSize = buffer.size;
            if (newSize <= oldSize) {
                if (newSize < 0) {
                    throw new IllegalArgumentException("newSize < 0: " + newSize);
                }
                // Shrink the buffer by either shrinking segments or removing them.
                for (long bytesToSubtract = oldSize - newSize; bytesToSubtract > 0;) {
                    SectionBuffer tail = buffer.head.prev;
                    int tailSize = tail.limit - tail.pos;
                    if (tailSize <= bytesToSubtract) {
                        buffer.head = tail.pop();
                        LifeCycle.recycle(tail);
                        bytesToSubtract -= tailSize;
                    } else {
                        tail.limit -= bytesToSubtract;
                        break;
                    }
                }
                // Seek to the end.
                this.segment = null;
                this.offset = newSize;
                this.data = null;
                this.start = -1;
                this.end = -1;
            } else if (newSize > oldSize) {
                // Enlarge the buffer by either enlarging segments or adding them.
                boolean needsToSeek = true;
                for (long bytesToAdd = newSize - oldSize; bytesToAdd > 0;) {
                    SectionBuffer tail = buffer.writableSegment(1);
                    int segmentBytesToAdd = (int) Math.min(bytesToAdd, SectionBuffer.SIZE - tail.limit);
                    tail.limit += segmentBytesToAdd;
                    bytesToAdd -= segmentBytesToAdd;

                    // If this is the first segment we're adding, seek to it.
                    if (needsToSeek) {
                        this.segment = tail;
                        this.offset = oldSize;
                        this.data = tail.data;
                        this.start = tail.limit - segmentBytesToAdd;
                        this.end = tail.limit;
                        needsToSeek = false;
                    }
                }
            }

            buffer.size = newSize;

            return oldSize;
        }

        /**
         * Grows the buffer by adding a <strong>contiguous range</strong> of capacity in a single segment. This adds at
         * least {@code minByteCount} bytes but may add up to a full segment of additional capacity.
         *
         * <p>
         * As a side-effect this cursor will {@link #seek seek}. It will move {@link #offset} to the first byte of
         * newly-added capacity. This is the size of the buffer prior to the {@code expandBuffer()} call.
         *
         * <p>
         * If {@code minByteCount} bytes are available in the buffer's current tail segment that will be used; otherwise
         * another segment will be allocated and appended. In either case this returns the number of bytes of capacity
         * added to this buffer.
         *
         * <p>
         * <strong>Warning:</strong> it is the caller's responsibility to either write new data to every byte of the
         * newly-allocated capacity, or to {@link #resizeBuffer shrink} the buffer to the data written. Failure to do so
         * may cause serious security problems as the data in the returned buffers is not zero filled. Buffers may
         * contain dirty pooled segments that hold very sensitive data from other parts of the current process.
         *
         * @param minByteCount the size of the contiguous capacity. Must be positive and not greater than the capacity
         *                     size of a single segment (8 KiB).
         * @return the number of bytes expanded by. Not less than {@code minByteCount}.
         * @throws IllegalStateException    if the cursor is not attached to a buffer or is read-only
         * @throws IllegalArgumentException if {@code minByteCount} is invalid
         */
        public final long expandBuffer(int minByteCount) {
            if (minByteCount <= 0) {
                throw new IllegalArgumentException("minByteCount <= 0: " + minByteCount);
            }
            if (minByteCount > SectionBuffer.SIZE) {
                throw new IllegalArgumentException("minByteCount > SectionBuffer.SIZE: " + minByteCount);
            }
            if (null == buffer) {
                throw new IllegalStateException("not attached to a buffer");
            }
            if (!readWrite) {
                throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
            }

            long oldSize = buffer.size;
            SectionBuffer tail = buffer.writableSegment(minByteCount);
            int result = SectionBuffer.SIZE - tail.limit;
            tail.limit = SectionBuffer.SIZE;
            buffer.size = oldSize + result;

            // Seek to the old size.
            this.segment = tail;
            this.offset = oldSize;
            this.data = tail.data;
            this.start = SectionBuffer.SIZE - result;
            this.end = SectionBuffer.SIZE;

            return result;
        }

        /**
         * Detaches this cursor from its buffer. After calling this method, the cursor can no longer be used to access
         * the buffer.
         *
         * @throws IllegalStateException if the cursor is not attached to a buffer
         */
        @Override
        public void close() {
            if (null == buffer) {
                throw new IllegalStateException("not attached to a buffer");
            }

            buffer = null;
            segment = null;
            offset = -1L;
            data = null;
            start = -1;
            end = -1;
        }
    }

}
