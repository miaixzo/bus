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

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.decoder.Base62Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base62Encoder;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Base62编码解码实现，常用于短URL From https://github.com/seruco/base62
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Provider implements Encoder<byte[], byte[]>, Decoder<byte[], byte[]>, Serializable {

    @Serial
    public static final long serialVersionUID = 2852259077806L;

    /**
     * 单例
     */
    public static Base62Provider INSTANCE = new Base62Provider();

    /**
     * 按照字典转换bytes
     *
     * @param indices    内容
     * @param dictionary 字典
     * @return 转换值
     */
    public static byte[] translate(final byte[] indices, final byte[] dictionary) {
        final byte[] translation = new byte[indices.length];

        for (int i = 0; i < indices.length; i++) {
            translation[i] = dictionary[indices[i]];
        }

        return translation;
    }

    /**
     * 使用定义的字母表从源基准到目标基准
     *
     * @param message    消息bytes
     * @param sourceBase 源基准长度
     * @param targetBase 目标基准长度
     * @return 计算结果
     */
    public static byte[] convert(final byte[] message, final int sourceBase, final int targetBase) {
        // 计算结果长度，算法来自：http://codegolf.stackexchange.com/a/21672
        final int estimatedLength = estimateOutputLength(message.length, sourceBase, targetBase);

        final ByteArrayOutputStream out = new ByteArrayOutputStream(estimatedLength);

        byte[] source = message;

        while (source.length > 0) {
            final ByteArrayOutputStream quotient = new ByteArrayOutputStream(source.length);

            int remainder = 0;

            for (final byte b : source) {
                final int accumulator = (b & 0xFF) + remainder * sourceBase;
                final int digit = (accumulator - (accumulator % targetBase)) / targetBase;

                remainder = accumulator % targetBase;

                if (quotient.size() > 0 || digit > 0) {
                    quotient.write(digit);
                }
            }

            out.write(remainder);

            source = quotient.toByteArray();
        }

        // 使用与消息中与之数量相对应的‘0’来填充输出
        for (int i = 0; i < message.length - 1 && message[i] == 0; i++) {
            out.write(0);
        }

        return ArrayKit.reverse(out.toByteArray());
    }

    /**
     * 估算结果长度
     *
     * @param inputLength 输入长度
     * @param sourceBase  源基准长度
     * @param targetBase  目标基准长度
     * @return 估算长度
     */
    private static int estimateOutputLength(final int inputLength, final int sourceBase, final int targetBase) {
        return (int) Math.ceil((Math.log(sourceBase) / Math.log(targetBase)) * inputLength);
    }

    /**
     * 编码指定消息bytes为Base62格式的bytes
     *
     * @param data 被编码的消息
     * @return Base62内容
     */
    @Override
    public byte[] encode(final byte[] data) {
        return encode(data, false);
    }

    /**
     * 编码指定消息bytes为Base62格式的bytes
     *
     * @param data        被编码的消息
     * @param useInverted 是否使用反转风格，即将GMP风格中的大小写做转换
     * @return Base62内容
     */
    public byte[] encode(final byte[] data, final boolean useInverted) {
        final Base62Encoder encoder = useInverted ? Base62Encoder.INVERTED_ENCODER : Base62Encoder.GMP_ENCODER;
        return encoder.encode(data);
    }

    /**
     * 解码Base62消息
     *
     * @param encoded Base62内容
     * @return 消息
     */
    @Override
    public byte[] decode(final byte[] encoded) {
        return decode(encoded, false);
    }

    /**
     * 解码Base62消息
     *
     * @param encoded     Base62内容
     * @param useInverted 是否使用反转风格，即将GMP风格中的大小写做转换
     * @return 消息
     */
    public byte[] decode(final byte[] encoded, final boolean useInverted) {
        final Base62Decoder decoder = useInverted ? Base62Decoder.INVERTED_DECODER : Base62Decoder.GMP_DECODER;
        return decoder.decode(encoded);
    }

}
