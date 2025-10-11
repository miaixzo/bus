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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.lang.Normal;

/**
 * Implements the Soundex algorithm for phonetic encoding of words. This algorithm converts a word into a phonetic code,
 * allowing for approximate string matching based on pronunciation rather than spelling. It supports different mapping
 * tables and configurations for encoding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Soundex implements FuzzyString {

    /**
     * Soundex mapping table for 6-digit codes. Maps characters to their Soundex digits or special control characters.
     */
    protected static final String MAP_6 =
            // A BCD E FG H I JKLMN O PQRST U V W X Y Z
            "\000123\00012\001\00022455\00012623\0001\0012\0002";
    /**
     * Soundex mapping table for 9-digit codes. Maps characters to their Soundex digits or special control characters.
     */
    protected static final String MAP_9 =
            // A BCD E FG H I JKLMN O PQRST U V W X Y Z
            "\000136\00024\001\00043788\00015936\0002\0015\0005";

    /**
     * Flag indicating whether the first letter of the input string should be encoded.
     */
    private final boolean encodeFirst;
    /**
     * The desired length of the Soundex code.
     */
    private final int codeLength;
    /**
     * The minimum length to which the Soundex code should be padded with '0's.
     */
    private final int padLength;
    /**
     * The character mapping array used for Soundex encoding.
     */
    private final char[] map;

    /**
     * Constructs a default {@code Soundex} instance. Initializes with {@code encodeFirst = false},
     * {@code codeLength = 4}, {@code padLength = 4}, and {@code MAP_6}.
     */
    public Soundex() {
        this(false, 4, 4, MAP_6);
    }

    /**
     * Constructs a {@code Soundex} instance with custom encoding parameters.
     *
     * @param encodeFirst {@code true} to encode the first letter, {@code false} otherwise.
     * @param codeLength  The maximum length of the Soundex code.
     * @param padLength   The minimum length to which the Soundex code should be padded.
     * @param map         The string representing the character mapping table.
     */
    public Soundex(boolean encodeFirst, int codeLength, int padLength, String map) {
        this.encodeFirst = encodeFirst;
        this.codeLength = codeLength;
        this.padLength = padLength;
        this.map = map.toCharArray();
    }

    /**
     * Converts the input string into its Soundex phonetic representation. The algorithm processes the string character
     * by character, applying a set of rules to reduce it to a phonetic key. This key can be used for approximate string
     * matching.
     *
     * @param s The input string to convert.
     * @return The Soundex encoded string. Returns an empty string if the input is null or empty.
     */
    @Override
    public String toFuzzy(String s) {
        if (s == null || s.length() == 0)
            return Normal.EMPTY;

        char[] in = s.toCharArray();
        char[] out = in.length < padLength ? new char[padLength] : in;
        int i = 0;
        int j = 0;
        char prevout = 0;
        if (!encodeFirst) {
            while (!Character.isLetter(in[i]))
                if (++i >= in.length)
                    return Normal.EMPTY;
            prevout = map(out[j++] = Character.toUpperCase(in[i++]));
        }

        char curout = 0;
        for (; i < in.length && j < codeLength; i++) {
            curout = map(in[i]);
            switch (curout) {
                case '\0':
                    prevout = curout;
                case '\1':
                    break;

                default:
                    if (curout != prevout)
                        out[j++] = prevout = curout;
            }
        }
        while (j < padLength)
            out[j++] = '0';
        return new String(out, 0, j);
    }

    /**
     * Maps a character to its Soundex code using the configured mapping table. Handles both uppercase and lowercase
     * input characters.
     *
     * @param c The character to map.
     * @return The Soundex code character, or a special control character if not found or a vowel.
     */
    private char map(char c) {
        try {
            return map[c >= 'a' ? c - 'a' : c - 'A'];
        } catch (IndexOutOfBoundsException e) {
            return (c == 'ß' || c == 'Ç' || c == 'ç') ? map['c' - 'a'] : '\u0000';
        }
    }

}
