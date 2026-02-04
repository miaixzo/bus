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
package org.miaixz.bus.core.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Implementation for Morse code encoding and decoding. Reference:
 * <a href="https://github.com/TakWolf/Java-MorseCoder">https://github.com/TakWolf/Java-MorseCoder</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Morse {

    /**
     * Maps character code points to their Morse code representation (e.g., 'A' -> "01"). The '0' represents a dit
     * (short mark or dot) and '1' represents a dah (longer mark or dash).
     */
    private static final Map<Integer, String> ALPHABETS = new HashMap<>(); // code point -> morse
    /**
     * Maps Morse code representations to their character code points (e.g., "01" -> 'A'). The '0' represents a dit
     * (short mark or dot) and '1' represents a dah (longer mark or dash).
     */
    private static final Map<String, Integer> DICTIONARIES = new HashMap<>(); // morse -> code point

    static {
        // Letters
        registerMorse('A', "01");
        registerMorse('B', "1000");
        registerMorse('C', "1010");
        registerMorse('D', "100");
        registerMorse('E', "0");
        registerMorse('F', "0010");
        registerMorse('G', "110");
        registerMorse('H', "0000");
        registerMorse('I', "00");
        registerMorse('J', "0111");
        registerMorse('K', "101");
        registerMorse('L', "0100");
        registerMorse('M', "11");
        registerMorse('N', "10");
        registerMorse('O', "111");
        registerMorse('P', "0110");
        registerMorse('Q', "1101");
        registerMorse('R', "010");
        registerMorse('S', "000");
        registerMorse('T', "1");
        registerMorse('U', "001");
        registerMorse('V', "0001");
        registerMorse('W', "011");
        registerMorse('X', "1001");
        registerMorse('Y', "1011");
        registerMorse('Z', "1100");
        // Numbers
        registerMorse('0', "11111");
        registerMorse('1', "01111");
        registerMorse('2', "00111");
        registerMorse('3', "00011");
        registerMorse('4', "00001");
        registerMorse('5', "00000");
        registerMorse('6', "10000");
        registerMorse('7', "11000");
        registerMorse('8', "11100");
        registerMorse('9', "11110");
        // Punctuation
        registerMorse('.', "010101");
        registerMorse(Symbol.C_COMMA, "110011");
        registerMorse('?', "001100");
        registerMorse('\'', "011110");
        registerMorse(Symbol.C_NOT, "101011");
        registerMorse('/', "10010");
        registerMorse(Symbol.C_PARENTHESE_LEFT, "10110");
        registerMorse(')', "101101");
        registerMorse(Symbol.C_AND, "01000");
        registerMorse(Symbol.C_COLON, "111000");
        registerMorse(Symbol.C_SEMICOLON, "101010");
        registerMorse(Symbol.C_EQUAL, "10001");
        registerMorse(Symbol.C_PLUS, "01010");
        registerMorse(Symbol.C_MINUS, "100001");
        registerMorse(Symbol.C_UNDERLINE, "001101");
        registerMorse('"', "010010");
        registerMorse(Symbol.C_DOLLAR, "0001001");
        registerMorse(Symbol.C_AT, "011010");
    }

    /**
     * The character representing a short mark or dot (dit) in Morse code.
     */
    private final char dit;
    /**
     * The character representing a longer mark or dash (dah) in Morse code.
     */
    private final char dah;
    /**
     * The character used as a separator between Morse code characters.
     */
    private final char split;

    /**
     * Constructs a {@code Morse} encoder/decoder with default characters for dit, dah, and split. Default dit: '.',
     * Default dah: '-', Default split: '/'.
     */
    public Morse() {
        this(Symbol.C_DOT, Symbol.C_MINUS, Symbol.C_SLASH);
    }

    /**
     * Constructs a {@code Morse} encoder/decoder with custom characters for dit, dah, and split.
     *
     * @param dit   The character to represent a short mark or dot.
     * @param dah   The character to represent a longer mark or dash.
     * @param split The character to use as a separator between Morse code characters.
     */
    public Morse(final char dit, final char dah, final char split) {
        this.dit = dit;
        this.dah = dah;
        this.split = split;
    }

    /**
     * Registers a character and its corresponding binary Morse code representation into the internal dictionaries. The
     * binary representation uses '0' for dit and '1' for dah.
     *
     * @param abc  The character (alphabet or symbol) to register.
     * @param dict The binary Morse code string for the character.
     */
    private static void registerMorse(final Character abc, final String dict) {
        ALPHABETS.put((int) abc, dict);
        DICTIONARIES.put(dict, (int) abc);
    }

    /**
     * Encodes the given text into Morse code. The input text is converted to uppercase before encoding. Characters not
     * found in the predefined Morse code map will be represented by their binary Unicode code point.
     *
     * @param text The plain text to encode. Must not be null.
     * @return The encoded Morse code string, with characters separated by the {@code split} character.
     * @throws NullPointerException if the input text is null.
     */
    public String encode(String text) {
        Assert.notNull(text, "Text should not be null.");

        text = text.toUpperCase();
        final StringBuilder morseBuilder = new StringBuilder();
        final int len = text.codePointCount(0, text.length());
        for (int i = 0; i < len; i++) {
            final int codePoint = text.codePointAt(i);
            String word = ALPHABETS.get(codePoint);
            if (word == null) {
                word = Integer.toBinaryString(codePoint);
            }
            morseBuilder.append(word.replace('0', dit).replace('1', dah)).append(split);
        }
        return morseBuilder.toString();
    }

    /**
     * Decodes the given Morse code string back into plain text. The Morse code string should use the configured dit,
     * dah, and split characters.
     *
     * @param morse The Morse code string to decode. Must not be null.
     * @return The decoded plain text string.
     * @throws NullPointerException     if the input morse string is null.
     * @throws IllegalArgumentException if the morse string contains characters other than the configured dit, dah, or
     *                                  split.
     */
    public String decode(final String morse) {
        Assert.notNull(morse, "Morse should not be null.");

        final char dit = this.dit;
        final char dah = this.dah;
        final char split = this.split;
        if (!StringKit.containsOnly(morse, dit, dah, split)) {
            throw new IllegalArgumentException("Incorrect morse.");
        }
        final List<String> words = CharsBacker.split(morse, String.valueOf(split));
        final StringBuilder textBuilder = new StringBuilder();
        Integer codePoint;
        for (String word : words) {
            if (StringKit.isEmpty(word)) {
                continue;
            }
            word = word.replace(dit, '0').replace(dah, '1');
            codePoint = DICTIONARIES.get(word);
            if (codePoint == null) {
                codePoint = Integer.valueOf(word, 2);
            }
            textBuilder.appendCodePoint(codePoint);
        }
        return textBuilder.toString();
    }

}
