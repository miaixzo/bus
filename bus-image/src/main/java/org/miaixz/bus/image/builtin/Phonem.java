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
package org.miaixz.bus.image.builtin;

/**
 * Implements the Phonem algorithm for fuzzy string matching, based on the work of Martin Wilz. This algorithm
 * transforms a string into a phonetic representation, useful for comparing words that sound alike but may have
 * different spellings.
 *
 * @see <a href="http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf">Martin Wilz</a>
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
public class Phonem implements FuzzyString {

    /**
     * Main method for testing the Phonem algorithm. It takes command-line arguments and prints their phonetic
     * representation.
     *
     * @param args Command-line arguments (strings to be phonetically encoded).
     */
    public static void main(String[] args) {
        Phonem inst = new Phonem();
        for (String arg : args)
            System.out.println(inst.toFuzzy(arg));
    }

    /**
     * Converts the input string into its Phonem phonetic representation. The algorithm processes the string by applying
     * a series of rules to transform character combinations into a simplified phonetic code. It handles various
     * German-specific phonetic rules.
     *
     * @param s The input string to convert to its phonetic representation.
     * @return The Phonem code for the input string. Returns an empty string if the input is null or empty.
     */
    @Override
    public String toFuzzy(String s) {
        if (s == null || s.length() == 0)
            return "";

        char[] in = s.toUpperCase().toCharArray();
        char next = in[0];
        int j = 0;
        for (int i = 1; i < in.length; i++) {
            char prev = next;
            switch ((prev << 8) + (next = in[i])) {
                case 0x5343: // SC
                case 0x535a: // SZ
                case 0x435a: // CZ
                case 0x5453: // TS
                    next = 'C';
                    break;

                case 0x4b53: // KS
                    next = 'X';
                    break;

                case 0x5046: // PF
                case 0x5048: // PH
                    next = 'V';
                    break;

                case 0x5545: // UE
                    next = 'Y';
                    break;

                case 0x4145: // AE
                    prev = 'E';
                    break;

                case 0x4f45: // OE
                    next = 'Ö';
                    break;

                case 0x4f55: // OU
                    next = '§';
                    break;

                case 0x5155: // QU
                    in[j++] = 'K';
                    next = 'W';
                    break;

                case 0x4549: // EI
                case 0x4559: // EY
                    in[j++] = 'A';
                    next = 'Y';
                    break;

                case 0x4555: // EU
                    in[j++] = 'O';
                    next = 'Y';
                    break;

                case 0x4155: // AU
                    in[j++] = 'A';
                    next = '§';
                    break;

                default:
                    in[j++] = prev;
                    break;
            }
        }
        in[j++] = next;
        int k = 0;
        char prev = 0;
        for (int i = 0; i < j; i++) {
            char ch = in[i];
            switch (ch) {
                case 'Z':
                case 'K':
                case 'G':
                case 'Q':
                case 'Ç':
                    ch = 'C';
                    break;

                case 'À':
                case 'Á':
                case 'Â':
                case 'Ã':
                case 'Å':
                    ch = 'A';
                    break;

                case 'Ä':
                case 'Æ':
                case 'È':
                case 'É':
                case 'Ê':
                case 'Ë':
                    ch = 'E';
                    break;

                case 'I':
                case 'J':
                case 'Ì':
                case 'Í':
                case 'Î':
                case 'Ï':
                case 'Ü':
                case 'Ý':
                    ch = 'Y';
                    break;

                case 'Ñ':
                    ch = 'N';
                    break;

                case 'Ò':
                case 'Ó':
                case 'Ô':
                case 'Õ':
                    ch = 'O';
                    break;

                case 'Ø':
                    ch = 'Ö';
                    break;

                case 'ß':
                    ch = 'S';
                    break;

                case 'F':
                case 'W':
                    ch = 'V';
                    break;

                case 'P':
                    ch = 'B';
                    break;

                case 'T':
                    ch = 'D';
                    break;

                case '§':
                case 'Ù':
                case 'Ú':
                case 'Û':
                    ch = 'U';
                    break;

                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'R':
                case 'S':
                case 'U':
                case 'V':
                case 'X':
                case 'Y':
                case 'Ö':
                    break;

                default:
                    continue;
            }
            if (ch != prev)
                in[k++] = prev = ch;
        }
        return new String(in, 0, k);
    }

}
