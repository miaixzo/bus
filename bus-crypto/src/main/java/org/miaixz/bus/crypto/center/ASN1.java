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
package org.miaixz.bus.crypto.center;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * ASN.1 – Abstract Syntax Notation dot one，抽象记法1 工具类。 ASN.1描述了一种对数据进行表示、编码、传输和解码的数据格式。它的编码格式包括DER、BER、DL等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ASN1 {

    /**
     * 编码为DER格式
     *
     * @param elements ASN.1元素
     * @return 编码后的bytes
     */
    public static byte[] encodeDer(final ASN1Encodable... elements) {
        return encode(ASN1Encoding.DER, elements);
    }

    /**
     * 编码为指定ASN1格式
     *
     * @param asn1Encoding 编码格式，见{@link ASN1Encoding}，可选DER、BER或DL
     * @param elements     ASN.1元素
     * @return 编码后的bytes
     */
    public static byte[] encode(final String asn1Encoding, final ASN1Encodable... elements) {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        encodeTo(asn1Encoding, out, elements);
        return out.toByteArray();
    }

    /**
     * 编码为指定ASN1格式
     *
     * @param asn1Encoding 编码格式，见{@link ASN1Encoding}，可选DER、BER或DL
     * @param out          输出流
     * @param elements     ASN.1元素
     */
    public static void encodeTo(final String asn1Encoding, final OutputStream out, final ASN1Encodable... elements) {
        final ASN1Sequence sequence;
        switch (asn1Encoding) {
        case ASN1Encoding.DER:
            sequence = new DERSequence(elements);
            break;
        case ASN1Encoding.BER:
            sequence = new BERSequence(elements);
            break;
        case ASN1Encoding.DL:
            sequence = new DLSequence(elements);
            break;
        default:
            throw new CryptoException("Unsupported ASN1 encoding: {}", asn1Encoding);
        }
        try {
            sequence.encodeTo(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取ASN.1数据流为{@link ASN1Object}
     *
     * @param in ASN.1数据
     * @return {@link ASN1Object}
     */
    public static ASN1Object decode(final InputStream in) {
        final ASN1InputStream asn1In = new ASN1InputStream(in);
        try {
            return asn1In.readObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取ASN1格式的导出格式，一般用于调试
     *
     * @param in ASN.1数据
     * @return {@link ASN1Object}的字符串表示形式
     * @see ASN1Dump#dumpAsString(Object)
     */
    public static String getDumpString(final InputStream in) {
        return ASN1Dump.dumpAsString(decode(in));
    }

}
