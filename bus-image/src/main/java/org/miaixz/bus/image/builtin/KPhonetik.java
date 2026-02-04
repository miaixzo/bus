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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.lang.Normal;

/**
 * Implements the Kölner Phonetik (Cologne Phonetics) algorithm for fuzzy string matching. This algorithm converts a
 * given string into a phonetic code, allowing for comparison of words that sound similar but are spelled differently.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KPhonetik implements FuzzyString {

    /**
     * Converts the input string into its Kölner Phonetik (Cologne Phonetics) representation. The algorithm processes
     * the input character by character, mapping them to phonetic codes. Vowels and certain consonants are mapped to
     * '0', while other consonants are mapped to digits based on their phonetic similarity. Consecutive identical
     * phonetic codes are reduced to a single code.
     *
     * @param s The input string to convert to its phonetic representation.
     * @return The Kölner Phonetik code for the input string. Returns an empty string if the input is null or empty.
     */
    @Override
    public String toFuzzy(String s) {
        if (null == s || s.length() == 0)
            return Normal.EMPTY;

        char[] in = s.toUpperCase().toCharArray();
        int countX = 0;
        for (char c : in)
            if (c == 'X')
                countX++;
        char[] out = countX > 0 ? new char[in.length + countX] : in;
        int i = 0;
        int j = 0;
        char prevout = 0;
        char curout = 0;
        char prev = 0;
        char cur = 0;
        char next = in[0];

        for (; i < in.length; i++) {
            prev = cur;
            cur = next;
            next = i + 1 < in.length ? in[i + 1] : 0;
            switch (cur) {
                case 'A':
                case 'E':
                case 'I':
                case 'J':
                case 'O':
                case 'U':
                case 'Y':
                case 'Ä':
                case 'Ö':
                case 'Ü':
                    if (j > 0) {
                        prevout = '0';
                        continue;
                    }
                    curout = '0';
                    break;

                case 'B':
                    curout = '1';
                    break;

                case 'P':
                    curout = next == 'H' ? '3' : '1';
                    break;

                case 'D':
                case 'T':
                    curout = (next == 'C' || next == 'S' || next == 'Z') ? '8' : '2';
                    break;

                case 'F':
                case 'V':
                case 'W':
                    curout = '3';
                    break;

                case 'G':
                case 'K':
                case 'Q':
                    curout = '4';
                    break;

                case 'C':
                    switch (next) {
                        case 'A':
                        case 'H':
                        case 'K':
                        case 'O':
                        case 'Q':
                        case 'U':
                        case 'X':
                            curout = i == 0 || (prev != 'S' && prev != 'Z') ? '4' : '8';
                            break;

                        case 'L':
                        case 'R':
                            curout = i == 0 ? '4' : '8';
                            break;
                    }
                    break;

                case 'X':
                    if (prev != 'C' && prev != 'K' && prev != 'Q' && prevout != '4')
                        out[j++] = prevout = '4';
                    curout = '8';
                    break;

                case 'L':
                    curout = '5';
                    break;

                case 'M':
                case 'N':
                    curout = '6';
                    break;

                case 'R':
                    curout = '7';
                    break;

                case 'S':
                case 'Z':
                case 'ß':
                    curout = '8';
                    break;

                default:
                    prevout = 0;
                    continue;
            }
            if (prevout != curout)
                out[j++] = prevout = curout;
        }
        return new String(out, 0, j);
    }

}
