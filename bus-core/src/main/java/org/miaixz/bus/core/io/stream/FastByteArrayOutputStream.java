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
package org.miaixz.bus.core.io.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * An {@link OutputStream} implementation backed by {@link Buffer}. Data can be retrieved using {@link #toByteArray()}
 * and {@link #toString()}. The {@link #close()} method has no effect, and no {@link IOException} will be thrown when
 * the stream is closed.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FastByteArrayOutputStream extends OutputStream {

    /**
     * Buffer used to store the written bytes.
     */
    private final Buffer buffer;

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
        buffer = new Buffer();
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
        buffer.write(b, off, len);
    }

    /**
     * Writes the specified byte to this output stream. The byte is appended to the internal buffer.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(final int b) {
        buffer.writeByte(b);
    }

    /**
     * Returns the current size of the buffer, which is the number of valid bytes written to the stream.
     *
     * @return The current size of the buffer.
     */
    public int size() {
        return Math.toIntExact(buffer.size());
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
        buffer.clear();
    }

    /**
     * Writes the complete contents of this {@code FastByteArrayOutputStream} to the specified output stream.
     *
     * @param out The output stream to write to.
     * @throws InternalException If an {@link IOException} occurs during writing.
     */
    public void writeTo(final OutputStream out) throws InternalException {
        try {
            buffer.copyTo(out);
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
        return buffer.clone().readByteArray();
    }

    /**
     * Creates a newly allocated byte array containing a portion of the buffer.
     *
     * @param start The starting index (inclusive).
     * @param len   The length of the portion to extract.
     * @return A byte array containing the specified portion of the buffer.
     */
    public byte[] toByteArray(final int start, final int len) {
        if (start < 0) {
            throw new IllegalArgumentException("Start must be greater than zero!");
        }
        if (len < 0) {
            throw new IllegalArgumentException("Length must be greater than zero!");
        }
        if (start >= buffer.size() || len == 0) {
            return new byte[0];
        }
        Buffer copy = buffer.clone();
        int byteCount = (int) Math.min(len, buffer.size() - start);
        try {
            copy.skip(start);
            return copy.readByteArray(byteCount);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the current contents as a byte array. This method keeps the legacy name but returns an isolated copy
     * because the stream is backed by segmented buffers.
     *
     * @return A byte array containing the current contents.
     */
    public byte[] toByteArrayZeroCopyIfPossible() {
        return toByteArray();
    }

    /**
     * Retrieves the byte at the specified index in the buffer.
     *
     * @param index The index of the byte to retrieve.
     * @return The byte at the specified index.
     */
    public byte get(final int index) {
        return buffer.getByte(index);
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
