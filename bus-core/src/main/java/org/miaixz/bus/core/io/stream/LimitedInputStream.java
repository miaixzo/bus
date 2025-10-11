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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * An implementation of {@link FilterInputStream} that limits the maximum number of bytes that can be read from the
 * underlying stream.
 * <p>
 * Source:
 * https://github.com/skylot/jadx/blob/master/jadx-plugins/jadx-plugins-api/src/main/java/jadx/api/plugins/utils/LimitedInputStream.java
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LimitedInputStream extends FilterInputStream {

    /**
     * Flag indicating whether an {@link IOException} should be thrown when the read limit is reached. If {@code false},
     * {@code -1} will be returned after the limit is reached.
     */
    private final boolean throwWhenReachLimit;
    /**
     * The remaining number of bytes that can be read from the stream.
     */
    protected long limit;

    /**
     * Constructs a new {@code LimitedInputStream} with the specified underlying input stream and a read limit. By
     * default, an {@link IOException} will be thrown when the limit is reached.
     *
     * @param in    The underlying {@link InputStream}.
     * @param limit The maximum number of bytes that can be read, in bytes.
     */
    public LimitedInputStream(final InputStream in, final long limit) {
        this(in, limit, true);
    }

    /**
     * Constructs a new {@code LimitedInputStream} with the specified underlying input stream, read limit, and error
     * handling behavior.
     *
     * @param in                  The underlying {@link InputStream}.
     * @param limit               The maximum number of bytes that can be read, in bytes.
     * @param throwWhenReachLimit {@code true} to throw an {@link IOException} when the limit is reached, {@code false}
     *                            to return -1 instead.
     */
    public LimitedInputStream(final InputStream in, final long limit, final boolean throwWhenReachLimit) {
        super(Assert.notNull(in, "InputStream must not be null!"));
        this.limit = Math.max(0L, limit);
        this.throwWhenReachLimit = throwWhenReachLimit;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is returned as an {@code int} in the range
     * {@code 0} to {@code 255}. If no byte is available because the end of the stream has been reached, the value
     * {@code -1} is returned. If the read limit is reached and {@code throwWhenReachLimit} is {@code true}, an
     * {@link IOException} is thrown.
     *
     * @return The next byte of data, or {@code -1} if the end of the stream is reached or the limit is reached and
     *         {@code throwWhenReachLimit} is {@code false}.
     * @throws IOException If an I/O error occurs, or if the read limit is reached and {@code throwWhenReachLimit} is
     *                     {@code true}.
     */
    @Override
    public int read() throws IOException {
        final int data = (limit == 0) ? Normal.__1 : super.read();
        checkLimit(data);
        limit = (data < 0) ? 0 : limit - 1;
        return data;
    }

    /**
     * Reads up to {@code len} bytes of data from this input stream into an array of bytes. An attempt is made to read
     * as many as {@code len} bytes, but a smaller number may be read. If the read limit is reached and
     * {@code throwWhenReachLimit} is {@code true}, an {@link IOException} is thrown.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset in the destination array {@code b} at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached or the limit is reached and {@code throwWhenReachLimit} is {@code false}.
     * @throws IOException If an I/O error occurs, or if the read limit is reached and {@code throwWhenReachLimit} is
     *                     {@code true}.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int length = (limit == 0) ? Normal.__1 : super.read(b, off, len > limit ? (int) limit : len);
        checkLimit(length);
        limit = (length < 0) ? 0 : limit - length;
        return length;
    }

    /**
     * Skips over and discards {@code len} bytes of data from the input stream. The {@code skip} method may skip over
     * some smaller number of bytes, possibly zero. If the read limit is reached and {@code throwWhenReachLimit} is
     * {@code true}, an {@link IOException} is thrown.
     *
     * @param len The number of bytes to be skipped.
     * @return The actual number of bytes skipped.
     * @throws IOException If an I/O error occurs, or if the read limit is reached and {@code throwWhenReachLimit} is
     *                     {@code true}.
     */
    @Override
    public long skip(final long len) throws IOException {
        final long length = super.skip(Math.min(len, limit));
        checkLimit(length);
        limit -= length;
        return length;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without
     * blocking by the next invocation of a method for this input stream. This method respects the remaining read limit.
     *
     * @return An estimate of the number of bytes that can be read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        final int length = super.available();
        return length > limit ? (int) limit : length;
    }

    /**
     * Checks if the read operation has exceeded the defined limit. If the end of the stream is reached (data < 0) and
     * there was still a limit remaining (limit > 0), and {@code throwWhenReachLimit} is {@code true}, an
     * {@link IOException} is thrown.
     *
     * @param data The result of the last read operation (number of bytes read, or -1 for end of stream).
     * @throws IOException If the read limit is exceeded and configured to throw an exception.
     */
    private void checkLimit(final long data) throws IOException {
        if (data < 0 && limit > 0 && throwWhenReachLimit) {
            throw new IOException("Read limit exceeded");
        }
    }

}
