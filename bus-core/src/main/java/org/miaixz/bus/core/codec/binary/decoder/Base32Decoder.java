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
package org.miaixz.bus.core.codec.binary.decoder;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base32Encoder;
import org.miaixz.bus.core.lang.Symbol;

import java.util.Arrays;

/**
 * Base32解码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base32Decoder implements Decoder<CharSequence, byte[]> {

    /**
     * 解码器
     */
    public static final Base32Decoder DECODER = new Base32Decoder(Base32Encoder.DEFAULT_ALPHABET);
    /**
     * 16进制解码器
     */
    public static final Base32Decoder HEX_DECODER = new Base32Decoder(Base32Encoder.HEX_ALPHABET);

    private final byte[] lookupTable;

    /**
     * 构造
     *
     * @param alphabet 编码字母表
     */
    public Base32Decoder(final String alphabet) {
        lookupTable = new byte[128];
        Arrays.fill(lookupTable, (byte) -1);

        final int length = alphabet.length();

        char c;
        for (int i = 0; i < length; i++) {
            c = alphabet.charAt(i);
            lookupTable[c - Symbol.C_ZERO] = (byte) i;
            // 支持小写字母解码
            if (c >= 'A' && c <= 'Z') {
                lookupTable[Character.toLowerCase(c) - Symbol.C_ZERO] = (byte) i;
            }
        }
    }

    @Override
    public byte[] decode(final CharSequence encoded) {
        int i, index, lookup, offset, digit;
        final String base32 = encoded.toString();
        final int len = base32.endsWith(Symbol.EQUAL) ? base32.indexOf(Symbol.EQUAL) * 5 / 8 : base32.length() * 5 / 8;
        final byte[] bytes = new byte[len];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - Symbol.C_ZERO;

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= lookupTable.length) {
                continue;
            }

            digit = lookupTable[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit < 0) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length) {
                        break;
                    }
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }

}
