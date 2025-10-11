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

import java.io.InputStream;

/**
 * An {@link InputStream} implementation that contains no data. All read operations immediately return end-of-stream
 * (-1), and other operations either do nothing or return values consistent with an empty stream. This class is useful
 * as a placeholder or for testing scenarios where an empty input stream is required.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class EmptyInputStream extends InputStream {

    /**
     * The singleton instance of {@code EmptyInputStream}.
     */
    public static final EmptyInputStream INSTANCE = new EmptyInputStream();

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private EmptyInputStream() {
    }

    /**
     * Returns 0, indicating that no bytes are available to be read from this empty stream.
     *
     * @return 0
     */
    @Override
    public int available() {
        return 0;
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream. This method does nothing
     * as there are no resources to close for an empty stream.
     */
    @Override
    public void close() {
        // Do nothing
    }

    /**
     * Marks the current position in this input stream. This method does nothing as there is no position to mark in an
     * empty stream.
     *
     * @param readLimit The maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public void mark(final int readLimit) {
        // Do nothing
    }

    /**
     * Tests if this input stream supports the {@code mark} and {@code reset} methods. Always returns {@code true} for
     * {@code EmptyInputStream}.
     *
     * @return {@code true}
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads the next byte of data from the input stream. Since this is an empty stream, this method always returns -1,
     * indicating that the end of the stream has been reached.
     *
     * @return -1, indicating the end of the stream.
     */
    @Override
    public int read() {
        return -1;
    }

    /**
     * Reads up to {@code buf.length} bytes of data from the input stream into an array of bytes. Since this is an empty
     * stream, this method always returns -1, indicating that the end of the stream has been reached.
     *
     * @param buf The buffer into which the data is read.
     * @return -1, indicating the end of the stream.
     */
    @Override
    public int read(final byte[] buf) {
        return -1;
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into an array of bytes. An attempt is made to read as
     * many as {@code len} bytes, but a smaller number may be read. Since this is an empty stream, this method always
     * returns -1, indicating that the end of the stream has been reached.
     *
     * @param buf The buffer into which the data is read.
     * @param off The start offset in the destination array {@code buf} at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return -1, indicating the end of the stream.
     */
    @Override
    public int read(final byte[] buf, final int off, final int len) {
        return -1;
    }

    /**
     * Repositions this stream to the position at the time the {@code mark} method was last called on this input stream.
     * This method does nothing as there is no position to reset for an empty stream.
     */
    @Override
    public void reset() {
        // Do nothing
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream. Since this is an empty stream, no bytes
     * are skipped, and this method always returns 0.
     *
     * @param n The number of bytes to be skipped.
     * @return 0L, as no bytes are skipped.
     */
    @Override
    public long skip(final long n) {
        return 0L;
    }

}
