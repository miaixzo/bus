/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 莫尔斯电码的编码和解码实现
 * 参考：<a href="https://github.com/TakWolf/Java-MorseCoder">https://github.com/TakWolf/Java-MorseCoder</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Morse {

    private static final Map<Integer, String> ALPHABETS = new HashMap<>(); // code point -> morse
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

    private final char dit; // short mark or dot
    private final char dah; // longer mark or dash
    private final char split;

    /**
     * 构造
     */
    public Morse() {
        this(Symbol.C_DOT, Symbol.C_MINUS, Symbol.C_SLASH);
    }

    /**
     * 构造
     *
     * @param dit   点表示的字符
     * @param dah   横线表示的字符
     * @param split 分隔符
     */
    public Morse(final char dit, final char dah, final char split) {
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
    private static void registerMorse(final Character abc, final String dict) {
        ALPHABETS.put((int) abc, dict);
        DICTIONARIES.put(dict, (int) abc);
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
     * 解码
     *
     * @param morse 莫尔斯电码
     * @return 明文
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
