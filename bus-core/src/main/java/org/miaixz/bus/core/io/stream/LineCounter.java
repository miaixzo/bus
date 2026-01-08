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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A utility class for counting lines in an {@link InputStream}. This class provides functionality to count lines, with
 * options to handle line separators at the end of the stream. It implements {@link Closeable} to ensure proper resource
 * management.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineCounter implements Closeable {

    /**
     * The input stream from which lines are to be counted.
     */
    private final InputStream is;
    /**
     * The buffer size used for reading the input stream. A larger buffer size may improve performance.
     */
    private final int bufferSize;

    /**
     * Determines whether a line separator at the very end of the stream should be counted as a new line. For example,
     * in Linux, a file typically ends with a newline character, which might or might not be considered an additional
     * line depending on this setting. If {@code true}, a trailing line separator will increment the line count. If
     * {@code false}, it will not.
     */
    private boolean lastLineSeparatorAsNewLine = true;

    /**
     * The cached line count. Initialized to -1 to indicate that the count has not yet been computed. The count is
     * computed only once upon the first call to {@link #getCount()}.
     */
    private int count = -1;

    /**
     * Constructs a new {@code LineCounter} with the specified input stream and buffer size.
     *
     * @param is         The {@link InputStream} to count lines from. This stream will be read until its end.
     * @param bufferSize The size of the buffer to use for reading. If {@code bufferSize} is less than 1, a default size
     *                   of 1024 bytes will be used instead.
     */
    public LineCounter(final InputStream is, final int bufferSize) {
        this.is = is;
        this.bufferSize = bufferSize < 1 ? 1024 : bufferSize;
    }

    /**
     * Sets whether a line separator found at the end of the stream should be treated as a new line. This setting
     * affects how the total line count is determined, especially for files that may or may not have a trailing newline
     * character.
     *
     * @param lastLineSeparatorAsNewLine {@code true} to count a trailing line separator as an additional line;
     *                                   {@code false} otherwise.
     * @return This {@code LineCounter} instance, allowing for method chaining.
     */
    public LineCounter setLastLineSeparatorAsNewLine(final boolean lastLineSeparatorAsNewLine) {
        this.lastLineSeparatorAsNewLine = lastLineSeparatorAsNewLine;
        return this;
    }

    /**
     * Retrieves the total number of lines in the input stream. The line count is computed only once and then cached.
     * Subsequent calls to this method will return the cached value.
     *
     * @return The total number of lines in the input stream.
     * @throws InternalException If an {@link IOException} occurs during the line counting process.
     */
    public int getCount() {
        if (this.count < 0) {
            try {
                this.count = count();
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        return this.count;
    }

    /**
     * Closes the underlying input stream. This method is idempotent; calling it multiple times has no further effect
     * after the first call.
     *
     * @throws IOException If an I/O error occurs while closing the stream.
     */
    @Override
    public void close() throws IOException {
        if (null != this.is) {
            this.is.close();
        }
    }

    /**
     * Counts the number of lines in the input stream. This method handles various line ending conventions including LF
     * ({@code \n}), CR ({@code \r}), and CRLF ({@code \r\n}). The counting logic considers the
     * {@link #lastLineSeparatorAsNewLine} setting for the final line.
     *
     * @return The calculated number of lines in the input stream.
     * @throws IOException If an I/O error occurs during reading from the input stream.
     */
    private int count() throws IOException {
        final byte[] buf = new byte[bufferSize];
        int readChars = is.read(buf);
        if (readChars == -1) {
            // If the file is empty, return 0 lines.
            return 0;
        }

        // Initialize count to 1, assuming at least one line if the file is not empty.
        // If there's only one line without a newline character, it will be counted as 1.
        // If there are multiple lines, and the last line has no newline, it needs separate counting.
        // If there are multiple lines, and the last line has a newline, an empty line is counted as one line.
        int count = 1;
        byte pre = 0; // Stores the previous byte read to detect CRLF sequences.
        byte c = 0; // Stores the current byte read.

        // Process full buffers
        while (readChars == bufferSize) {
            for (int i = 0; i < bufferSize; i++) {
                pre = c;
                c = buf[i];
                // Increment count for LF or CR (to handle MAC-style CR line endings).
                if (c == Symbol.C_LF || pre == Symbol.C_CR) {
                    ++count;
                }
            }
            readChars = is.read(buf);
        }

        // Process the last partial buffer (if any)
        while (readChars != -1) {
            for (int i = 0; i < readChars; i++) {
                pre = c;
                c = buf[i];
                if (c == Symbol.C_LF || pre == Symbol.C_CR) {
                    ++count;
                }
            }
            readChars = is.read(buf);
        }

        // Adjust count based on the last character and the 'lastLineSeparatorAsNewLine' setting.
        if (lastLineSeparatorAsNewLine) {
            // If the last character is CR, count it as a separate line.
            if (c == Symbol.C_CR) {
                ++count;
            }
        } else {
            // If the last character is LF, and 'lastLineSeparatorAsNewLine' is false,
            // decrement count as it implies the previous line already ended with a newline.
            if (c == Symbol.C_LF) {
                --count;
            }
        }

        return count;
    }

}
