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
package org.miaixz.bus.core.io.stream;

import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation that discards all data written to it. This stream acts like writing to
 * {@code /dev/null} on Unix-like systems, effectively ignoring all output. This class is useful for scenarios where
 * output is required but should be suppressed.
 * <p>
 * Inspired by Apache Commons IO.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmptyOutputStream extends OutputStream {

    /**
     * The singleton instance of {@code EmptyOutputStream}.
     */
    public static final EmptyOutputStream INSTANCE = new EmptyOutputStream();

    /**
     * Private constructor to enforce the singleton pattern.
     */
    private EmptyOutputStream() {
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream. This
     * method does nothing, effectively discarding the data.
     *
     * @param b   The data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) {
        // to /dev/null
    }

    /**
     * Writes the specified byte to this output stream. This method does nothing, effectively discarding the byte.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(final int b) {
        // to /dev/null
    }

    /**
     * Writes {@code b.length} bytes from the specified byte array to this output stream. This method does nothing,
     * effectively discarding the data.
     *
     * @param b The data to write.
     */
    @Override
    public void write(final byte[] b) {
        // to /dev/null
    }

}
