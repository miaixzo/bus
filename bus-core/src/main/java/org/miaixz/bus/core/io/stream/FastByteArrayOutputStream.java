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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.io.buffer.FastByteBuffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An {@link OutputStream} implementation based on {@link FastByteBuffer}, which automatically expands its buffer as
 * data grows. Data can be retrieved using {@link #toByteArray()} and {@link #toString()}. The {@link #close()} method
 * has no effect, and no {@link IOException} will be thrown when the stream is closed. This design avoids reallocating
 * memory blocks by allocating new buffers as needed, and buffers are not garbage collected, nor is data copied to other
 * buffers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastByteArrayOutputStream extends OutputStream {

    /**
     * The underlying {@link FastByteBuffer} used to store the written bytes.
     */
    private final FastByteBuffer buffer;

    /**
     * Constructs a new {@code FastByteArrayOutputStream} with a default initial buffer size of 1024 bytes.
     */
    public FastByteArrayOutputStream() {
        this(Normal._1024);
    }

    /**
     * Constructs a new {@code FastByteArrayOutputStream} with the specified initial buffer size.
     *
     * @param size The estimated initial size of the buffer.
     */
    public FastByteArrayOutputStream(final int size) {
        buffer = new FastByteBuffer(size);
    }

    /**
     * Creates a {@code FastByteArrayOutputStream} object based on the total length of the input stream. If the length
     * of the input stream is uncertain, and greater than the limit, the limit will be used as the initial size.
     *
     * @param in    The input stream to determine the length from.
     * @param limit The maximum allowed size for the output stream. If the input stream's length exceeds this, this
     *              limit is used.
     * @return A new {@code FastByteArrayOutputStream} instance.
     */
    public static FastByteArrayOutputStream of(final InputStream in, final int limit) {
        int length = IoKit.length(in);
        if (length < 0 || length > limit) {
            length = limit;
        }
        if (length < 0) {
            length = Normal._1024;
        }
        return new FastByteArrayOutputStream(length);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream. The
     * bytes are appended to the internal buffer.
     *
     * @param b   The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        buffer.append(b, off, len);
    }

    /**
     * Writes the specified byte to this output stream. The byte is appended to the internal buffer.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(final int b) {
        buffer.append((byte) b);
    }

    /**
     * Returns the current size of the buffer, which is the number of valid bytes written to the stream.
     *
     * @return The current size of the buffer.
     */
    public int size() {
        return buffer.length();
    }

    /**
     * Closes this output stream. This method has no effect as the {@code FastByteArrayOutputStream} does not hold any
     * system resources that need to be closed. No {@link IOException} will be thrown.
     */
    @Override
    public void close() {
        // nop
    }

    /**
     * Resets the internal buffer, discarding all currently accumulated input in the buffer. The buffer can then be
     * reused for writing new data.
     */
    public void reset() {
        buffer.reset();
    }

    /**
     * Writes the complete contents of this {@code FastByteArrayOutputStream} to the specified output stream.
     *
     * @param out The output stream to write to.
     * @throws InternalException If an {@link IOException} occurs during writing.
     */
    public void writeTo(final OutputStream out) throws InternalException {
        final int index = buffer.index();
        if (index < 0) {
            // No data to write
            return;
        }
        byte[] buf;
        try {
            for (int i = 0; i < index; i++) {
                buf = buffer.array(i);
                out.write(buf);
            }
            out.write(buffer.array(index), 0, buffer.offset());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of this output stream and the valid contents
     * of the buffer have been copied into it.
     *
     * @return The current contents of this output stream, as a byte array.
     */
    public byte[] toByteArray() {
        return buffer.toArray();
    }

    /**
     * Creates a newly allocated byte array containing a portion of the buffer.
     *
     * @param start The starting index (inclusive).
     * @param len   The length of the portion to extract.
     * @return A byte array containing the specified portion of the buffer.
     */
    public byte[] toByteArray(final int start, final int len) {
        return buffer.toArray(start, len);
    }

    /**
     * Returns the internal byte array directly if the buffer's data length is fixed and fits within a single array.
     * <p>
     * WARNING: This method shares the internal array. Modifying the returned array will affect the internal state of
     * this stream.
     *
     * @return The internal byte array, or a copy if the data spans multiple internal arrays.
     */
    public byte[] toByteArrayZeroCopyIfPossible() {
        return buffer.toArrayZeroCopyIfPossible();
    }

    /**
     * Retrieves the byte at the specified index in the buffer.
     *
     * @param index The index of the byte to retrieve.
     * @return The byte at the specified index.
     */
    public byte get(final int index) {
        return buffer.get(index);
    }

    /**
     * Converts the buffer's contents into a {@link String} using the default character set.
     *
     * @return A {@link String} representation of the buffer's contents.
     */
    @Override
    public String toString() {
        return toString(Charset.defaultCharset());
    }

    /**
     * Converts the buffer's contents into a {@link String} using the specified character set.
     *
     * @param charset The character set to use for decoding the bytes. If {@code null}, the default character set will
     *                be used.
     * @return A {@link String} representation of the buffer's contents.
     */
    public String toString(final java.nio.charset.Charset charset) {
        return new String(toByteArray(), ObjectKit.defaultIfNull(charset, Charset::defaultCharset));
    }

}
