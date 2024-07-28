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
package org.miaixz.bus.image.builtin;

/**
 * Implementation
 *
 * @see <a href="http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf">Martin Wilz</a>
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class Phonem implements FuzzyString {

    public static void main(String[] args) {
        Phonem inst = new Phonem();
        for (String arg : args)
            System.out.println(inst.toFuzzy(arg));
    }

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
