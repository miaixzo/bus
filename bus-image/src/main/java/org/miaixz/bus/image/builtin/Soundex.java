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
 * @author Kimi Liu
 * @since Java 17+
 */
public class Soundex implements FuzzyString {

    protected static final String MAP_6 =
            // A BCD E FG H I JKLMN O PQRST U V W X Y Z
            "\000123\00012\001\00022455\00012623\0001\0012\0002";
    protected static final String MAP_9 =
            // A BCD E FG H I JKLMN O PQRST U V W X Y Z
            "\000136\00024\001\00043788\00015936\0002\0015\0005";

    private final boolean encodeFirst;
    private final int codeLength;
    private final int padLength;
    private final char[] map;

    public Soundex() {
        this(false, 4, 4, MAP_6);
    }

    public Soundex(boolean encodeFirst, int codeLength, int padLength, String map) {
        this.encodeFirst = encodeFirst;
        this.codeLength = codeLength;
        this.padLength = padLength;
        this.map = map.toCharArray();
    }

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

    private char map(char c) {
        try {
            return map[c >= 'a' ? c - 'a' : c - 'A'];
        } catch (IndexOutOfBoundsException e) {
            return (c == 'ß' || c == 'Ç' || c == 'ç') ? map['c' - 'a'] : '\u0000';
        }
    }

}
