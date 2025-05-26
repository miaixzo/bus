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
package org.miaixz.bus.core.codec.binary.provider;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Base16（Hex）编码解码器 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制，一般用数字0到9和字母A到F表示（其中:A~F即10~15）。 例如十进制数57，在二进制写作111001，在16进制写作39。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base16Provider implements Encoder<byte[], char[]>, Decoder<CharSequence, byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852239163560L;

    /**
     * 编码解码器：小写
     */
    public static final Base16Provider CODEC_LOWER = new Base16Provider(true);
    /**
     * 编码解码器：大写
     */
    public static final Base16Provider CODEC_UPPER = new Base16Provider(false);

    private final char[] alphabets;

    /**
     * 构造
     *
     * @param lowerCase 是否小写
     */
    public Base16Provider(final boolean lowerCase) {
        this.alphabets = (lowerCase ? "0123456789abcdef" : "0123456789ABCDEF").toCharArray();
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws InternalException 当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit < 0) {
            throw new InternalException("Illegal hexadecimal character {} at index {}", ch, index);
        }
        return digit;
    }

    @Override
    public char[] encode(final byte[] data) {
        final int len = data.length;
        final char[] out = new char[len << 1];// len*2
        // 十六进制值中的两个字符
        for (int i = 0, j = 0; i < len; i++) {
            // 高位
            out[j++] = hexDigit(data[i] >> 4);
            // 低位
            out[j++] = hexDigit(data[i]);
        }
        return out;
    }

    @Override
    public byte[] decode(CharSequence encoded) {
        if (StringKit.isEmpty(encoded)) {
            return null;
        }

        encoded = StringKit.cleanBlank(encoded);
        int len = encoded.length();

        if ((len & 0x01) != 0) {
            // 如果提供的数据是奇数长度，则前面补0凑偶数
            encoded = "0" + encoded;
            len = encoded.length();
        }

        final byte[] out = new byte[len >> 1];

        // 十六进制值中的两个字符
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(encoded.charAt(j), j) << 4;
            j++;
            f = f | toDigit(encoded.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将指定char值转换为Unicode字符串形式，常用于特殊字符（例如汉字）转Unicode形式 转换的字符串如果u后不足4位，则前面用0填充，例如：
     *
     * <pre>
     * 你 = &#92;u4f60
     * </pre>
     *
     * @param ch char值
     * @return Unicode表现形式
     */
    public String toUnicodeHex(final char ch) {
        return "\\u" + hexDigit(ch >> 12) + hexDigit(ch >> 8) + hexDigit(ch >> 4) + hexDigit(ch);
    }

    /**
     * 将byte值转为16进制并添加到{@link StringBuilder}中
     *
     * @param builder {@link StringBuilder}
     * @param b       byte
     */
    public void appendHex(final StringBuilder builder, final byte b) {
        // 高位
        builder.append(hexDigit(b >> 4));
        // 低位
        builder.append(hexDigit(b));
    }

    /**
     * 将byte值转为16进制
     *
     * @param b byte
     * @return the hex char
     */
    public char hexDigit(final int b) {
        return alphabets[b & 0x0f];
    }

}
