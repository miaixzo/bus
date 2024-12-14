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
package org.miaixz.bus.http.secure;

import javax.security.auth.x500.X500Principal;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * 专有名称(DN)解析器。该解析器只支持从DN中提取字符串值。 它不支持十六进制字符串样式的值.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DistinguishedNameParser {

    private final String dn;
    private final int length;
    private int pos;
    private int beg;
    private int end;

    /**
     * 临时变量，用于存储当前已解析项的位置
     */
    private int cur;

    /**
     * 专有名称的字符
     */
    private char[] chars;

    DistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName(X500Principal.RFC2253);
        this.length = this.dn.length();
    }

    private String nextAT() {
        for (; pos < length && chars[pos] == Symbol.C_SPACE; pos++) {
        }
        if (pos == length) {
            return null;
        }

        beg = pos;

        pos++;
        for (; pos < length && chars[pos] != Symbol.C_EQUAL && chars[pos] != Symbol.C_SPACE; pos++) {
        }
        if (pos >= length) {
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        end = pos;

        if (chars[pos] == Symbol.C_SPACE) {
            for (; pos < length && chars[pos] != Symbol.C_EQUAL && chars[pos] == Symbol.C_SPACE; pos++) {
            }

            if (chars[pos] != Symbol.C_EQUAL || pos == length) {
                throw new IllegalStateException("Unexpected end of DN: " + dn);
            }
        }

        pos++;

        for (; pos < length && chars[pos] == Symbol.C_SPACE; pos++) {

        }

        if ((end - beg > 4) && (chars[beg + 3] == Symbol.C_DOT) && (chars[beg] == 'O' || chars[beg] == 'o')
                && (chars[beg + 1] == 'I' || chars[beg + 1] == 'i')
                && (chars[beg + 2] == 'D' || chars[beg + 2] == 'd')) {
            beg += 4;
        }

        return new String(chars, beg, end - beg);
    }

    private String quotedAV() {
        pos++;
        beg = pos;
        end = beg;
        while (true) {

            if (pos == length) {
                throw new IllegalStateException("Unexpected end of DN: " + dn);
            }

            if (chars[pos] == Symbol.C_DOUBLE_QUOTES) {
                pos++;
                break;
            } else if (chars[pos] == Symbol.C_BACKSLASH) {
                chars[end] = getEscaped();
            } else {
                chars[end] = chars[pos];
            }
            pos++;
            end++;
        }

        for (; pos < length && chars[pos] == Symbol.C_SPACE; pos++) {
        }

        return new String(chars, beg, end - beg);
    }

    private String hexAV() {
        if (pos + 4 >= length) {
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        beg = pos;
        pos++;
        while (true) {
            if (pos == length || chars[pos] == Symbol.C_PLUS || chars[pos] == Symbol.C_COMMA
                    || chars[pos] == Symbol.C_SEMICOLON) {
                end = pos;
                break;
            }

            if (chars[pos] == Symbol.C_SPACE) {
                end = pos;
                pos++;
                for (; pos < length && chars[pos] == Symbol.C_SPACE; pos++) {
                }
                break;
            } else if (chars[pos] >= 'A' && chars[pos] <= 'F') {
                chars[pos] += Normal._32;
            }

            pos++;
        }

        int hexLen = end - beg;
        if (hexLen < 5 || (hexLen & 1) == 0) {
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        // get byte encoding from string representation
        byte[] encoded = new byte[hexLen / 2];
        for (int i = 0, p = beg + 1; i < encoded.length; p += 2, i++) {
            encoded[i] = (byte) getByte(p);
        }

        return new String(chars, beg, hexLen);
    }

    // gets string attribute value: *( stringchar / pair )
    private String escapedAV() {
        beg = pos;
        end = pos;
        while (true) {
            if (pos >= length) {
                // the end of DN has been found
                return new String(chars, beg, end - beg);
            }

            switch (chars[pos]) {
            case Symbol.C_PLUS:
            case Symbol.C_COMMA:
            case Symbol.C_SEMICOLON:
                return new String(chars, beg, end - beg);
            case Symbol.C_BACKSLASH:
                chars[end++] = getEscaped();
                pos++;
                break;
            case Symbol.C_SPACE:
                cur = end;

                pos++;
                chars[end++] = Symbol.C_SPACE;

                for (; pos < length && chars[pos] == Symbol.C_SPACE; pos++) {
                    chars[end++] = Symbol.C_SPACE;
                }
                if (pos == length || chars[pos] == Symbol.C_COMMA || chars[pos] == Symbol.C_PLUS
                        || chars[pos] == Symbol.C_SEMICOLON) {
                    return new String(chars, beg, cur - beg);
                }
                break;
            default:
                chars[end++] = chars[pos];
                pos++;
            }
        }
    }

    // returns escaped char
    private char getEscaped() {
        pos++;
        if (pos == length) {
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        switch (chars[pos]) {
        case Symbol.C_DOUBLE_QUOTES:
        case Symbol.C_BACKSLASH:
        case Symbol.C_COMMA:
        case Symbol.C_EQUAL:
        case Symbol.C_PLUS:
        case Symbol.C_LT:
        case Symbol.C_GT:
        case Symbol.C_SHAPE:
        case Symbol.C_SEMICOLON:
        case Symbol.C_SPACE:
        case Symbol.C_STAR:
        case Symbol.C_PERCENT:
        case Symbol.C_UNDERLINE:
            return chars[pos];
        default:
            return getUTF8();
        }
    }

    private char getUTF8() {
        int res = getByte(pos);
        pos++; // FIXME tmp

        if (res < Normal._128) {
            return (char) res;
        } else if (res >= 192 && res <= 247) {

            int count;
            if (res <= 223) {
                count = 1;
                res = res & 0x1F;
            } else if (res <= 239) {
                count = 2;
                res = res & 0x0F;
            } else {
                count = 3;
                res = res & 0x07;
            }

            int b;
            for (int i = 0; i < count; i++) {
                pos++;
                if (pos == length || chars[pos] != Symbol.C_BACKSLASH) {
                    return 0x3F;
                }
                pos++;

                b = getByte(pos);
                pos++; // FIXME tmp
                if ((b & 0xC0) != 0x80) {
                    return 0x3F;
                }

                res = (res << 6) + (b & 0x3F);
            }
            return (char) res;
        } else {
            return 0x3F;
        }
    }

    // Returns byte representation of a char pair
    // The char pair is composed of DN char in
    // specified 'position' and the next char
    // According to BNF syntax:
    // hexchar = DIGIT / "A" / "B" / "C" / "D" / "E" / "F"
    // / "a" / "b" / "c" / "d" / "e" / "f"
    private int getByte(int position) {
        if (position + 1 >= length) {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        int b1, b2;

        b1 = chars[position];
        if (b1 >= Symbol.C_ZERO && b1 <= Symbol.C_NINE) {
            b1 = b1 - Symbol.C_ZERO;
        } else if (b1 >= 'a' && b1 <= 'f') {
            b1 = b1 - 87; // 87 = 'a' - 10
        } else if (b1 >= 'A' && b1 <= 'F') {
            b1 = b1 - 55; // 55 = 'A' - 10
        } else {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        b2 = chars[position + 1];
        if (b2 >= Symbol.C_ZERO && b2 <= Symbol.C_NINE) {
            b2 = b2 - Symbol.C_ZERO;
        } else if (b2 >= 'a' && b2 <= 'f') {
            b2 = b2 - 87; // 87 = 'a' - 10
        } else if (b2 >= 'A' && b2 <= 'F') {
            b2 = b2 - 55; // 55 = 'A' - 10
        } else {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        return (b1 << 4) + b2;
    }

    /**
     * Parses the DN and returns the most significant attribute value for an attribute type, or null if none found.
     *
     * @param attributeType attribute type to look for (e.g. "ca")
     */
    public String findMostSpecific(String attributeType) {
        // Initialize internal state.
        pos = 0;
        beg = 0;
        end = 0;
        cur = 0;
        chars = dn.toCharArray();

        String attType = nextAT();
        if (null == attType) {
            return null;
        }
        while (true) {
            String attValue = Normal.EMPTY;

            if (pos == length) {
                return null;
            }

            switch (chars[pos]) {
            case Symbol.C_DOUBLE_QUOTES:
                attValue = quotedAV();
                break;
            case Symbol.C_SHAPE:
                attValue = hexAV();
                break;
            case Symbol.C_PLUS:
            case Symbol.C_COMMA:
            case Symbol.C_SEMICOLON:
                break;
            default:
                attValue = escapedAV();
            }

            // Values are ordered from most specific to least specific
            // due to the RFC2253 formatting. So take the first match
            // we see.
            if (attributeType.equalsIgnoreCase(attType)) {
                return attValue;
            }

            if (pos >= length) {
                return null;
            }

            if (chars[pos] == Symbol.C_COMMA || chars[pos] == Symbol.C_SEMICOLON) {
            } else if (chars[pos] != Symbol.C_PLUS) {
                throw new IllegalStateException("Malformed DN: " + dn);
            }

            pos++;
            attType = nextAT();
            if (null == attType) {
                throw new IllegalStateException("Malformed DN: " + dn);
            }
        }
    }

}
