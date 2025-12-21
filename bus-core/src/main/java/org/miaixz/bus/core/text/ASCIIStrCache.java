/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.text;

/**
 * Cache for ASCII character strings. This class provides a cached {@link String} representation for ASCII characters to
 * improve performance when converting characters to strings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ASCIIStrCache {

    /**
     * Constructs a new ASCIIStrCache. Utility class constructor for static access.
     */
    private ASCIIStrCache() {
    }

    /**
     * The maximum length of the ASCII character set (0-127).
     */
    private static final int ASCII_LENGTH = 128;
    /**
     * The cache array for ASCII character strings. Each index corresponds to an ASCII character's integer value, and
     * the value is its {@link String} representation.
     */
    private static final String[] CACHE = new String[ASCII_LENGTH];

    static {
        // Populate the cache with String representations of ASCII characters.
        for (char c = 0; c < ASCII_LENGTH; c++) {
            CACHE[c] = String.valueOf(c);
        }
    }

    /**
     * Converts a character to its {@link String} representation. If the character is an ASCII character, its cached
     * string value is returned. Otherwise, a new {@link String} is created.
     *
     * @param c The character to convert.
     * @return The {@link String} representation of the character.
     */
    public static String toString(final char c) {
        return c < ASCII_LENGTH ? CACHE[c] : String.valueOf(c);
    }

}
