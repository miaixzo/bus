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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;

/**
 * Wrapper for {@link Reader}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderWrapper extends Reader implements Wrapper<Reader> {

    /**
     * The wrapped {@link Reader}.
     */
    protected final Reader raw;

    /**
     * Constructs a new {@code ReaderWrapper}.
     *
     * @param reader The {@link Reader} to wrap.
     */
    public ReaderWrapper(final Reader reader) {
        this.raw = Assert.notNull(reader);
    }

    /**
     * Returns the wrapped {@link Reader}.
     *
     * @return The wrapped {@link Reader}.
     */
    @Override
    public Reader getRaw() {
        return this.raw;
    }

    /**
     * Reads a single character.
     *
     * @return The character read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        return raw.read();
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param target The buffer to transfer characters into.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(final CharBuffer target) throws IOException {
        return raw.read(target);
    }

    /**
     * Reads characters into an array.
     *
     * @param cbuf The array to transfer characters into.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(final char[] cbuf) throws IOException {
        return raw.read(cbuf);
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param buffer The array to transfer characters into.
     * @param off    The offset at which to start writing characters.
     * @param len    The maximum number of characters to read.
     * @return The number of characters read, or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read(final char[] buffer, final int off, final int len) throws IOException {
        return raw.read(buffer, off, len);
    }

    /**
     * Tells whether this stream supports the mark operation.
     *
     * @return True if this stream supports the mark operation, false otherwise.
     */
    @Override
    public boolean markSupported() {
        return this.raw.markSupported();
    }

    /**
     * Marks the present position in the stream.
     *
     * @param readAheadLimit The maximum limit of characters that can be read before the mark position becomes invalid.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        this.raw.mark(readAheadLimit);
    }

    /**
     * Skips characters.
     *
     * @param n The number of characters to skip.
     * @return The number of characters actually skipped.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        return this.raw.skip(n);
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        return this.raw.ready();
    }

    /**
     * Resets the stream to the most recent mark.
     *
     * @throws IOException If the stream has not been marked, or if the mark has been invalidated.
     */
    @Override
    public void reset() throws IOException {
        this.raw.reset();
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        raw.close();
    }

}
