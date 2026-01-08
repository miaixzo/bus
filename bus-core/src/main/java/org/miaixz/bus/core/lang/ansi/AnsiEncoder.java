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
package org.miaixz.bus.core.lang.ansi;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Utility class for encoding ANSI escape sequences for text styling. This class provides methods to construct ANSI
 * strings by converting {@link AnsiElement} instances into their corresponding escape codes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnsiEncoder {

    /**
     * Constructs a new AnsiEncoder. Utility class constructor for static access.
     */
    private AnsiEncoder() {
    }

    /**
     * The starting sequence for an ANSI escape code.
     */
    private static final String ENCODE_START = "\033[";
    /**
     * The ending sequence for an ANSI escape code.
     */
    private static final String ENCODE_END = "m";
    /**
     * The ANSI reset code, which resets all formatting to default.
     */
    private static final String RESET = "0;" + Ansi4BitColor.DEFAULT;

    /**
     * Encodes a sequence of objects into an ANSI string. {@link AnsiElement} instances within the arguments will be
     * converted into their ANSI escape code representations. Non-{@link AnsiElement} objects will be appended as their
     * string representation. The resulting string will automatically include a reset code at the end if any
     * {@link AnsiElement} was used.
     *
     * @param args An array of objects, which may include {@link AnsiElement}s and regular strings.
     * @return The ANSI encoded string.
     */
    public static String encode(final Object... args) {
        final StringBuilder sb = new StringBuilder();
        buildEnabled(sb, args);
        return sb.toString();
    }

    /**
     * Appends the ANSI escape sequences and other elements to the given {@link StringBuilder}. This method iterates
     * through the provided arguments, appending ANSI escape codes for {@link AnsiElement}s and string representations
     * for other objects. It manages the opening and closing of ANSI escape sequences.
     *
     * @param sb   The {@link StringBuilder} to which the ANSI string will be appended.
     * @param args An array of objects to be processed.
     */
    private static void buildEnabled(final StringBuilder sb, final Object[] args) {
        boolean writingAnsi = false;
        boolean containsEncoding = false;
        for (final Object element : args) {
            if (null == element) {
                continue;
            }
            if (element instanceof AnsiElement) {
                containsEncoding = true;
                if (writingAnsi) {
                    sb.append(Symbol.SEMICOLON);
                } else {
                    sb.append(ENCODE_START);
                    writingAnsi = true;
                }
            } else {
                if (writingAnsi) {
                    sb.append(ENCODE_END);
                    writingAnsi = false;
                }
            }
            sb.append(element);
        }

        // Reset to default if any ANSI encoding was applied
        if (containsEncoding) {
            sb.append(writingAnsi ? Symbol.SEMICOLON : ENCODE_START);
            sb.append(RESET);
            sb.append(ENCODE_END);
        }
    }

}
