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
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.Inflater;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Wrapper implementation for {@link java.util.zip.InflaterInputStream}, providing decompression using the "deflate"
 * algorithm. Reference: org.apache.hc.client5.http.entity.DeflateInputStream
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class InflaterStream extends InputStream {

    /**
     * The underlying {@link java.util.zip.InflaterInputStream}.
     */
    private final java.util.zip.InflaterInputStream in;

    /**
     * Constructs a new InflaterStream with a default buffer size of 512 bytes.
     *
     * @param wrapped The input stream to be wrapped and decompressed.
     */
    public InflaterStream(final InputStream wrapped) {
        this(wrapped, 512);
    }

    /**
     * Constructs a new InflaterStream with a specified buffer size.
     *
     * @param wrapped The input stream to be wrapped and decompressed.
     * @param size    The buffer size for the internal {@link java.util.zip.InflaterInputStream}.
     * @throws InternalException if an unexpected end of stream occurs or an I/O error happens during stream
     *                           initialization.
     */
    public InflaterStream(final InputStream wrapped, final int size) {
        final PushbackInputStream pushback = new PushbackInputStream(wrapped, 2);
        final int i1, i2;
        try {
            i1 = pushback.read();
            i2 = pushback.read();
            if (i1 == -1 || i2 == -1) {
                throw new InternalException("Unexpected end of stream");
            }

            pushback.unread(i2);
            pushback.unread(i1);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        boolean nowrap = true;
        final int b1 = i1 & 0xFF;
        final int compressionMethod = b1 & 0xF;
        final int compressionInfo = b1 >> 4 & 0xF;
        final int b2 = i2 & 0xFF;
        if (compressionMethod == 8 && compressionInfo <= 7 && ((b1 << 8) | b2) % 31 == 0) {
            nowrap = false;
        }
        in = new java.util.zip.InflaterInputStream(pushback, new Inflater(nowrap), size);
    }

    /**
     * Reads the next byte of data from the underlying stream.
     *
     * @return The next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return this.in.read();
    }

    /**
     * Reads up to {@code b.length} bytes of data from the underlying stream into an array of bytes.
     *
     * @param b The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or {@code -1} if there is no more data.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return in.read(b);
    }

    /**
     * Reads up to {@code len} bytes of data from the underlying stream into an array of bytes.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset in the buffer at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or {@code -1} if there is no more data.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * Skips over and discards {@code n} bytes of data from the underlying stream.
     *
     * @param n The number of bytes to skip.
     * @return The actual number of bytes skipped.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        return in.skip(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read from the underlying stream without blocking.
     *
     * @return An estimate of the number of bytes that can be read without blocking.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Marks the current position in the underlying stream.
     *
     * @param readLimit The maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public void mark(final int readLimit) {
        in.mark(readLimit);
    }

    /**
     * Repositions the underlying stream to the position at the time the mark method was last called.
     *
     * @throws IOException If the stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public void reset() throws IOException {
        in.reset();
    }

    /**
     * Tests if the underlying stream supports the mark and reset methods.
     *
     * @return {@code true} if the stream supports mark/reset, {@code false} otherwise.
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        in.close();
    }

}
