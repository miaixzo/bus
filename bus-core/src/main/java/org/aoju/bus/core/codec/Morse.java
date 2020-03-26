/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.core.codec;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 莫尔斯电码的编码和解码
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
public class Morse {

    private static final Map<Integer, String> alphabets = new HashMap<>();
    private static final Map<String, Integer> dictionaries = new HashMap<>();

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
        registerMorse(Symbol.C_ZERO, "11111");
        registerMorse(Symbol.C_ONE, "01111");
        registerMorse(Symbol.C_TWO, "00111");
        registerMorse(Symbol.C_THREE, "00011");
        registerMorse(Symbol.C_FOUR, "00001");
        registerMorse(Symbol.C_FIVE, "00000");
        registerMorse(Symbol.C_SIX, "10000");
        registerMorse(Symbol.C_SEVEN, "11000");
        registerMorse(Symbol.C_EIGHT, "11100");
        registerMorse(Symbol.C_NINE, "11110");
        // Punctuation
        registerMorse(Symbol.C_DOT, "010101");
        registerMorse(Symbol.C_COMMA, "110011");
        registerMorse(Symbol.C_QUESTION_MARK, "001100");
        registerMorse(Symbol.C_SINGLE_QUOTE, "011110");
        registerMorse(Symbol.C_NOT, "101011");
        registerMorse(Symbol.C_SLASH, "10010");
        registerMorse(Symbol.C_PARENTHESE_LEFT, "10110");
        registerMorse(Symbol.C_PARENTHESE_RIGHT, "101101");
        registerMorse(Symbol.C_AND, "01000");
        registerMorse(Symbol.C_COLON, "111000");
        registerMorse(Symbol.C_SEMICOLON, "101010");
        registerMorse(Symbol.C_EQUAL, "10001");
        registerMorse(Symbol.C_PLUS, "01010");
        registerMorse(Symbol.C_HYPHEN, "100001");
        registerMorse(Symbol.C_UNDERLINE, "001101");
        registerMorse(Symbol.C_DOUBLE_QUOTES, "010010");
        registerMorse(Symbol.C_DOLLAR, "0001001");
        registerMorse(Symbol.C_AT, "011010");
    }

    private final char dit;
    private final char dah;
    private final char split;

    /**
     * 构造
     */
    public Morse() {
        this(Symbol.C_DOT, Symbol.C_HYPHEN, Symbol.C_SLASH);
    }

    /**
     * 构造
     *
     * @param dit   点表示的字符
     * @param dah   横线表示的字符
     * @param split 分隔符
     */
    public Morse(char dit, char dah, char split) {
        this.dit = dit;
        this.dah = dah;
        this.split = split;
    }

    /**
     * 注册莫尔斯电码表
     *
     * @param abc  字母和字符
     * @param dict 二进制
     */
    private static void registerMorse(Character abc, String dict) {
        alphabets.put(Integer.valueOf(abc), dict);
        dictionaries.put(dict, Integer.valueOf(abc));
    }

    /**
     * 编码
     *
     * @param text 文本
     * @return 密文
     */
    public String encode(String text) {
        Assert.notNull(text, "Text should not be null.");

        text = text.toUpperCase();
        final StringBuilder morseBuilder = new StringBuilder();
        final int len = text.codePointCount(0, text.length());
        for (int i = 0; i < len; i++) {
            int codePoint = text.codePointAt(i);
            String word = alphabets.get(codePoint);
            if (word == null) {
                word = Integer.toBinaryString(codePoint);
            }
            morseBuilder.append(word.replace(Symbol.C_ZERO, dit).replace(Symbol.C_ONE, dah)).append(split);
        }
        return morseBuilder.toString();
    }

    /**
     * 解码
     *
     * @param morse 莫尔斯电码
     * @return 明文
     */
    public String decode(String morse) {
        Assert.notNull(morse, "Morse should not be null.");

        final char dit = this.dit;
        final char dah = this.dah;
        final char split = this.split;
        if (false == StringUtils.containsOnly(morse, dit, dah, split)) {
            throw new IllegalArgumentException("Incorrect morse.");
        }
        final List<String> words = StringUtils.split(morse, split);
        final StringBuilder textBuilder = new StringBuilder();
        Integer codePoint;
        for (String word : words) {
            if (StringUtils.isEmpty(word)) {
                continue;
            }
            word = word.replace(dit, Symbol.C_ZERO).replace(dah, Symbol.C_ONE);
            codePoint = dictionaries.get(word);
            if (codePoint == null) {
                codePoint = Integer.valueOf(word, 2);
            }
            textBuilder.appendCodePoint(codePoint);
        }
        return textBuilder.toString();
    }

}
