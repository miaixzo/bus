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
package org.miaixz.bus.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Line reader, similar to BufferedInputStream, supports multi-line escaping with the following rules:
 * <ul>
 * <li>Supports both '\n' and '\r\n' as line endings, but not '\r'.</li>
 * <li>If an escape character is desired, it must be defined as '\\'.</li>
 * <li>Line endings and spaces after multi-line escapes will be ignored.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LineReader extends ReaderWrapper implements Iterable<String> {

    /**
     * Constructs a new {@code LineReader} from an {@link InputStream} and a {@link Charset}.
     *
     * @param in      The input stream.
     * @param charset The character set for decoding the stream.
     */
    public LineReader(final InputStream in, final Charset charset) {
        this(IoKit.toReader(in, charset));
    }

    /**
     * Constructs a new {@code LineReader} from a {@link Reader}.
     *
     * @param reader The reader.
     */
    public LineReader(final Reader reader) {
        super(IoKit.toBuffered(reader));
    }

    /**
     * Reads a line of text.
     *
     * @return The content of the line, or null if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs.
     */
    public String readLine() throws IOException {
        StringBuilder text = null;
        // Whether the character before the newline is an escape character
        boolean precedingBackslash = false;
        int c;
        while ((c = read()) > 0) {
            if (null == text) {
                // Initialize the line only if there are characters, otherwise it's the end of the line.
                text = StringKit.builder(1024);
            }
            if (Symbol.C_BACKSLASH == c) {
                // Escape character. If a '\' is needed at the end of the line, use '\\'.
                if (!precedingBackslash) {
                    // Escape character, add a flag but do not add the character.
                    precedingBackslash = true;
                    continue;
                } else {
                    precedingBackslash = false;
                }
            } else {
                if (precedingBackslash) {
                    // In escape mode, skip all whitespace characters after the escape character.
                    if (CharKit.isBlankChar(c)) {
                        continue;
                    }
                    // Encountered a normal character, turn off escaping.
                    precedingBackslash = false;
                } else if (Symbol.C_LF == c) {
                    // In non-escape state, indicates the end of the line.
                    // If the newline character is `\r\n`, remove the trailing `\r`.
                    final int lastIndex = text.length() - 1;
                    if (lastIndex >= 0 && Symbol.C_CR == text.charAt(lastIndex)) {
                        text.deleteCharAt(lastIndex);
                    }
                    break;
                }
            }

            text.append((char) c);
        }

        return StringKit.toStringOrNull(text);
    }

    /**
     * Returns an iterator over the lines in this reader.
     *
     * @return An {@link Iterator} of strings, where each string is a line from the reader.
     * @throws InternalException If an {@link IOException} occurs during reading.
     */
    @Override
    public Iterator<String> iterator() {
        return new ComputeIterator<>() {

            /**
             * Computenext method.
             *
             * @return the String value
             */
            @Override
            protected String computeNext() {
                try {
                    return readLine();
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        };
    }

}
