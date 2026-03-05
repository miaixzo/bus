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
