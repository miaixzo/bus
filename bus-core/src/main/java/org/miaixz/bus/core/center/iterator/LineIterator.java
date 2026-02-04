/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
