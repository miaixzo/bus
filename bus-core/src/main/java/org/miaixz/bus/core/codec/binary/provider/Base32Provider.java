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
package org.miaixz.bus.core.codec.binary.provider;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base32Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base32Encoder;

import java.io.Serializable;

/**
 * Base32 - encodes and decodes RFC4648 Base32 (see <a href=
 * "https://datatracker.ietf.org/doc/html/rfc4648#section-6">https://datatracker.ietf.org/doc/html/rfc4648#section-6</a>
 * ) base32就是用32（2的5次方）个特定ASCII码来表示256个ASCII码。 所以，5个ASCII字符经过base32编码后会变为8个字符（公约数为40），长度增加3/5.不足8n用“=”补足。 根据RFC4648
 * Base32规范，支持两种模式：
 * <ul>
 * <li>Base 32 Alphabet (ABCDEFGHIJKLMNOPQRSTUVWXYZ234567)</li>
 * <li>"Extended Hex" Base 32 Alphabet (0123456789ABCDEFGHIJKLMNOPQRSTUV)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base32Provider implements Encoder<byte[], String>, Decoder<CharSequence, byte[]>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 单例对象
     */
    public static Base32Provider INSTANCE = new Base32Provider();

    @Override
    public String encode(final byte[] data) {
        return encode(data, false);
    }

    /**
     * 编码数据
     *
     * @param data   数据
     * @param useHex 是否使用Hex Alphabet
     * @return 编码后的Base32字符串
     */
    public String encode(final byte[] data, final boolean useHex) {
        final Base32Encoder encoder = useHex ? Base32Encoder.HEX_ENCODER : Base32Encoder.ENCODER;
        return encoder.encode(data);
    }

    @Override
    public byte[] decode(final CharSequence encoded) {
        return decode(encoded, false);
    }

    /**
     * 解码数据
     *
     * @param encoded base32字符串
     * @param useHex  是否使用Hex Alphabet
     * @return 解码后的内容
     */
    public byte[] decode(final CharSequence encoded, final boolean useHex) {
        final Base32Decoder decoder = useHex ? Base32Decoder.HEX_DECODER : Base32Decoder.DECODER;
        return decoder.decode(encoded);
    }

}
