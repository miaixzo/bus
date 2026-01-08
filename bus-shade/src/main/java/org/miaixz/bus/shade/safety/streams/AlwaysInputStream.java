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
package org.miaixz.bus.shade.safety.streams;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that wraps another input stream but prevents it from being closed. The {@link #close()} method
 * is overridden to do nothing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AlwaysInputStream extends InputStream {

    /**
     * The underlying input stream.
     */
    private final InputStream in;

    /**
     * Constructs a new {@code AlwaysInputStream}.
     *
     * @param in The input stream to wrap.
     */
    public AlwaysInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return The next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return in.read();
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array {@code b}.
     *
     * @param b The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into an array of bytes.
     *
     * @param b   The buffer into which the data is read.
     * @param off The start offset in array {@code b} at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if there is no more data.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream.
     *
     * @param n The number of bytes to be skipped.
     * @return The actual number of bytes skipped.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream.
     *
     * @return An estimate of the number of bytes that can be read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Marks the current position in this input stream.
     *
     * @param readLimit The maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public void mark(int readLimit) {
        in.mark(readLimit);
    }

    /**
     * Repositions this stream to the position at the time the {@code mark} method was last called.
     *
     * @throws IOException If this stream has not been marked or if the mark has been invalidated.
     */
    @Override
    public void reset() throws IOException {
        in.reset();
    }

    /**
     * Tests if this input stream supports the {@code mark} and {@code reset} methods.
     *
     * @return {@code true} if this stream instance supports the mark and reset methods; {@code false} otherwise.
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Does nothing. This method is overridden to prevent the underlying stream from being closed.
     */
    @Override
    public void close() {
        // Do nothing, intentionally left blank to prevent closing the underlying stream.
    }

}
