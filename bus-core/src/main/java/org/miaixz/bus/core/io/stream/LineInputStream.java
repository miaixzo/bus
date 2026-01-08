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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.io.buffer.FastByteBuffer;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Line reader, similar to {@link java.io.BufferedInputStream}, supporting multi-line escaping. The rules are as
 * follows:
 * <ul>
 * <li>Supports '\n' and '\r\n' as line endings, but not '\r'.</li>
 * <li>If an escape character is to be read, it must be defined as '\\'.</li>
 * <li>Newline characters and spaces after an escape character in a multi-line escaped sequence will be ignored.</li>
 * </ul>
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineInputStream extends FilterInputStream implements Iterable<byte[]> {

    /**
     * Constructs a new {@code LineInputStream} with the specified underlying input stream.
     *
     * @param in The input stream to be wrapped.
     */
    public LineInputStream(final InputStream in) {
        super(in);
    }

    /**
     * Reads a line from the input stream and converts it to a {@link String} using the specified charset.
     *
     * @param charset The character set to use for decoding the bytes.
     * @return The line as a {@link String}, or {@code null} if the end of the stream has been reached.
     */
    public String readLine(final Charset charset) {
        return StringKit.toString(readLine(), charset);
    }

    /**
     * Reads a line from the input stream.
     *
     * @return The line as a byte array, or {@code null} if the end of the stream has been reached.
     * @throws RuntimeException If an {@link IOException} occurs during reading.
     */
    public byte[] readLine() {
        try {
            return _readLine();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an iterator over the lines in this input stream. Each element returned by the iterator is a byte array
     * representing a line.
     *
     * @return An {@link Iterator} of byte arrays, where each array is a line from the stream.
     */
    @Override
    public Iterator<byte[]> iterator() {
        return new ComputeIterator<>() {

            /**
             * Computenext method.
             *
             * @return the byte[] value
             */
            @Override
            protected byte[] computeNext() {
                return readLine();
            }
        };
    }

    /**
     * Reads a line from the input stream, handling escape characters and line endings. This method supports '\n' and
     * '\r\n' as line terminators. If a '\' character is encountered, it acts as an escape character. If the preceding
     * character was a '\' and the current character is a whitespace, the whitespace is ignored.
     *
     * @return The line as a byte array, or {@code null} if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs during reading.
     */
    private byte[] _readLine() throws IOException {
        FastByteBuffer out = null;
        // Flag to indicate if the preceding character was an escape character (backslash).
        boolean precedingBackslash = false;
        int c;
        while ((c = read()) > 0) {
            if (null == out) {
                out = new FastByteBuffer();
            }
            if (Symbol.C_BACKSLASH == c) {
                // Handle escape character.
                // If a backslash is needed at the end of a line, it must be escaped as `\\`.
                if (!precedingBackslash) {
                    // First backslash, set flag but don't add to buffer yet.
                    precedingBackslash = true;
                    continue;
                } else {
                    // Second backslash, meaning `\\`, so add one backslash to buffer.
                    precedingBackslash = false;
                }
            } else {
                if (precedingBackslash) {
                    // In escape mode, skip all whitespace characters after the escape character.
                    if (CharKit.isBlankChar(c)) {
                        continue;
                    }
                    // Encountered a non-whitespace character, exit escape mode.
                    precedingBackslash = false;
                } else if (Symbol.C_LF == c) {
                    // Not in escape mode, and encountered a newline character, indicating end of line.
                    // If the newline is `\r\n`, remove the trailing `\r`.
                    final int lastIndex = out.length() - 1;
                    if (lastIndex >= 0 && Symbol.C_CR == out.get(lastIndex)) {
                        return out.toArray(0, lastIndex);
                    }
                    break;
                }
            }

            out.append((byte) c);
        }

        return ObjectKit.apply(out, FastByteBuffer::toArray);
    }

}
