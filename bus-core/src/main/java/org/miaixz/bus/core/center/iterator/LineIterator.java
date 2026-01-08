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
package org.miaixz.bus.core.center.iterator;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * An {@link Iterator} that reads lines from a {@code Reader}. This object should be closed after use. The recommended
 * way to use it is with a try-with-resources block or a {@code finally} block.
 *
 * <pre>{@code
 * try (LineIterator it = new LineIterator(reader)) {
 *     while (it.hasNext()) {
 *         String line = it.next();
 * // do something with line
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineIterator extends ComputeIterator<String> implements IterableIterator<String>, Closeable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267076328L;

    /**
     * The underlying BufferedReader.
     */
    private final BufferedReader bufferedReader;

    /**
     * Constructs a {@code LineIterator} from an {@link InputStream} and a character set.
     *
     * @param in      The {@link InputStream} to read from.
     * @param charset The character set.
     * @throws IllegalArgumentException if the InputStream is null.
     */
    public LineIterator(final InputStream in, final Charset charset) throws IllegalArgumentException {
        this(IoKit.toReader(in, charset));
    }

    /**
     * Constructs a {@code LineIterator} from a {@link Reader}.
     *
     * @param reader The {@link Reader} object, which cannot be null.
     * @throws IllegalArgumentException if the reader is null.
     */
    public LineIterator(final Reader reader) throws IllegalArgumentException {
        Assert.notNull(reader, "Reader must not be null");
        this.bufferedReader = IoKit.toBuffered(reader);
    }

    /**
     * Computes the next line to be returned by the iterator.
     *
     * @return The next valid line, or null if the end of the stream is reached.
     */
    @Override
    protected String computeNext() {
        try {
            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    return null; // End of stream
                }
                if (isValidLine(line)) {
                    return line;
                }
                // Skip invalid lines and continue to the next one.
            }
        } catch (final IOException ioe) {
            close();
            throw new InternalException(ioe);
        }
    }

    /**
     * Closes the underlying {@link BufferedReader}.
     */
    @Override
    public void close() {
        super.finish();
        IoKit.closeQuietly(bufferedReader);
    }

    /**
     * Override this method to determine whether a line should be included in the iteration. By default, all non-null
     * lines are included.
     *
     * @param line The line to validate.
     * @return {@code true} if the line should be returned by the iterator, {@code false} otherwise.
     */
    protected boolean isValidLine(final String line) {
        return true;
    }

}
