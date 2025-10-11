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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import javax.security.auth.x500.X500Principal;

/**
 * A parser for X.500 distinguished names as specified in RFC 2253. This parser can extract specific attribute values
 * from a DN string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class DistinguishedNameParser {

    /**
     * The distinguished name string.
     */
    private final String dn;
    /**
     * The length of the DN string.
     */
    private final int length;
    /**
     * The current parsing position.
     */
    private int pos;
    /**
     * The beginning position of the current token.
     */
    private int beg;
    /**
     * The end position of the current token.
     */
    private int end;
    /**
     * The current position within the character array being built.
     */
    private int cur;
    /**
     * The character array of the distinguished name.
     */
    private char[] chars;

    /**
     * Constructs a new parser for the given principal.
     *
     * @param principal The X.500 principal to parse.
     */
    DistinguishedNameParser(X500Principal principal) {
        // We get the RFC 2253 format, which is a predictable, ordered format.
        this.dn = principal.getName(X500Principal.RFC2253);
        this.length = this.dn.length();
    }

    /**
     * Finds the next attribute type (e.g., "cn", "o").
     * 
     * @return The attribute type string.
     */
    private String nextAT() {
        // Skip leading spaces.
        while (pos < length && chars[pos] == Symbol.C_SPACE) {
            pos++;
        }
        if (pos == length) {
            return null; // No more attributes.
        }

        // Mark the start of the attribute type.
        beg = pos;

        // Scan until we hit a space or '='.
        pos++;
        while (pos < length && chars[pos] != Symbol.C_EQUAL && chars[pos] != Symbol.C_SPACE) {
            pos++;
        }
        if (pos >= length) {
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        // Mark the end of the attribute type.
        end = pos;

        // Skip spaces between the type and the '='.
        if (chars[pos] == Symbol.C_SPACE) {
            while (pos < length && chars[pos] != Symbol.C_EQUAL && chars[pos] == Symbol.C_SPACE) {
                pos++;
            }

            if (chars[pos] != Symbol.C_EQUAL || pos == length) {
                throw new IllegalStateException("Unexpected end of DN: " + dn);
            }
        }

        pos++; // Skip the '='.

        // Skip spaces after the '='.
        while (pos < length && chars[pos] == Symbol.C_SPACE) {
            pos++;
        }

        // Handle OID notation (e.g., OID.1.2.3)
        if ((end - beg > 4) && (chars[beg + 3] == Symbol.C_DOT) && (chars[beg] == 'O' || chars[beg] == 'o')
                && (chars[beg + 1] == 'I' || chars[beg + 1] == 'i')
                && (chars[beg + 2] == 'D' || chars[beg + 2] == 'd')) {
            beg += 4;
        }

        return new String(chars, beg, end - beg);
    }

    /**
     * Parses a quoted attribute value.
     * 
     * @return The unescaped value.
     */
    private String quotedAV() {
        pos++; // Skip leading quote.
        beg = pos;
        end = beg;
        while (true) {
            if (pos == length) {
                throw new IllegalStateException("Unexpected end of DN: " + dn);
            }

            if (chars[pos] == Symbol.C_DOUBLE_QUOTES) {
                pos++; // Skip trailing quote.
                break;
            } else if (chars[pos] == Symbol.C_BACKSLASH) {
                chars[end] = getEscaped();
            } else {
                chars[end] = chars[pos];
            }
            pos++;
            end++;
        }

        // Skip trailing spaces.
        while (pos < length && chars[pos] == Symbol.C_SPACE) {
            pos++;
        }

        return new String(chars, beg, end - beg);
    }

    /**
     * Parses a hex-encoded attribute value.
     * 
     * @return The hex string value.
     */
    private String hexAV() {
        if (pos + 4 >= length) {
            // We need at least "#" and two hex digits.
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        beg = pos; // Mark the start of the hex string.
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
                while (pos < length && chars[pos] == Symbol.C_SPACE) {
                    pos++;
                }
                break;
            } else if (chars[pos] >= 'A' && chars[pos] <= 'F') {
                // Normalize to lowercase for consistency.
                chars[pos] += Normal._32;
            }
            pos++;
        }

        int hexLen = end - beg;
        if (hexLen < 5 || (hexLen & 1) == 0) {
            // Must have at least one byte (2 hex chars) and an even number of hex chars.
            throw new IllegalStateException("Unexpected end of DN: " + dn);
        }

        byte[] encoded = new byte[hexLen / 2];
        for (int i = 0, p = beg + 1; i < encoded.length; p += 2, i++) {
            encoded[i] = (byte) getByte(p);
        }

        return new String(chars, beg, hexLen);
    }

    /**
     * Parses an escaped attribute value.
     * 
     * @return The unescaped value.
     */
    private String escapedAV() {
        beg = pos;
        end = pos;
        while (true) {
            if (pos >= length) {
                return new String(chars, beg, end - beg);
            }

            switch (chars[pos]) {
                case Symbol.C_PLUS:
                case Symbol.C_COMMA:
                case Symbol.C_SEMICOLON:
                    // End of value.
                    return new String(chars, beg, end - beg);

                case Symbol.C_BACKSLASH:
                    // Handle escaped character.
                    chars[end++] = getEscaped();
                    pos++;
                    break;

                case Symbol.C_SPACE:
                    // Handle trailing spaces.
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

    /**
     * Gets an escaped character.
     * 
     * @return The unescaped character.
     */
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
            case Symbol.C_HASH:
            case Symbol.C_SEMICOLON:
            case Symbol.C_SPACE:
            case Symbol.C_STAR:
            case Symbol.C_PERCENT:
            case Symbol.C_UNDERLINE:
                return chars[pos];

            default:
                // This is a hex-escaped UTF-8 character.
                return getUTF8();
        }
    }

    /**
     * Decodes a UTF-8 character that was escaped as a hex sequence.
     * 
     * @return The decoded character.
     */
    private char getUTF8() {
        int res = getByte(pos);
        pos++; // Advance past the first hex char.

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

            for (int i = 0; i < count; i++) {
                pos++;
                if (pos == length || chars[pos] != Symbol.C_BACKSLASH) {
                    return '?'; // Malformed.
                }
                pos++;

                int b = getByte(pos);
                pos++; // Advance past the first hex char.
                if ((b & 0xC0) != 0x80) {
                    return '?'; // Malformed.
                }

                res = (res << 6) + (b & 0x3F);
            }
            return (char) res;
        } else {
            return '?'; // Malformed.
        }
    }

    /**
     * Converts a two-character hex string at the given position to a byte.
     * 
     * @param position The starting position of the two hex characters.
     * @return The integer value of the byte.
     */
    private int getByte(int position) {
        if (position + 1 >= length) {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        int b1 = chars[position];
        if (b1 >= Symbol.C_ZERO && b1 <= Symbol.C_NINE) {
            b1 = b1 - Symbol.C_ZERO;
        } else if (b1 >= 'a' && b1 <= 'f') {
            b1 = b1 - 87; // 'a' - 10
        } else if (b1 >= 'A' && b1 <= 'F') {
            b1 = b1 - 55; // 'A' - 10
        } else {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        int b2 = chars[position + 1];
        if (b2 >= Symbol.C_ZERO && b2 <= Symbol.C_NINE) {
            b2 = b2 - Symbol.C_ZERO;
        } else if (b2 >= 'a' && b2 <= 'f') {
            b2 = b2 - 87; // 'a' - 10
        } else if (b2 >= 'A' && b2 <= 'F') {
            b2 = b2 - 55; // 'A' - 10
        } else {
            throw new IllegalStateException("Malformed DN: " + dn);
        }

        return (b1 << 4) + b2;
    }

    /**
     * Parses the DN and returns the most significant attribute value for an attribute type, or null if none found.
     *
     * @param attributeType attribute type to look for (e.g. "cn", "o").
     * @return The value of the first matching attribute, or null if not found.
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

                case Symbol.C_HASH:
                    attValue = hexAV();
                    break;

                case Symbol.C_PLUS:
                case Symbol.C_COMMA:
                case Symbol.C_SEMICOLON:
                    // Empty value.
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

            if (chars[pos] != Symbol.C_COMMA && chars[pos] != Symbol.C_SEMICOLON && chars[pos] != Symbol.C_PLUS) {
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
