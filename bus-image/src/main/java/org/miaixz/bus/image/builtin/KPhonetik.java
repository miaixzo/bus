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
public class KPhonetik implements FuzzyString {

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
