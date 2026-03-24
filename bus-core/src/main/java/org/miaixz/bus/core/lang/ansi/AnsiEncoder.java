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
package org.miaixz.bus.core.lang.ansi;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Utility class for encoding ANSI escape sequences for text styling. This class provides methods to construct ANSI
 * strings by converting {@link AnsiElement} instances into their corresponding escape codes.
 *
 * @author Kimi Liu
 * @since Java 21+
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
