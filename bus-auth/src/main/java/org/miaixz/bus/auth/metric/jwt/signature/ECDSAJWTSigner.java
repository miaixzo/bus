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
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;
import java.security.KeyPair;

import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * 椭圆曲线（ECDSA）JWT 签名器，继承自 RSA 签名器。
 * <p>
 * 按照 <a href="https://datatracker.ietf.org/doc/html/rfc7518#section-3.4">RFC 7518</a> 规范， ECDSA 签名需将 DER 格式转换为 (R, S)
 * 对，或反向转换以进行签名和验证。 支持 ES256、ES384、ES512 算法。
 * </p>
 *
 * @see RSAJWTSigner
 * @author Kimi Liu
 * @since Java 17+
 */
public class ECDSAJWTSigner extends RSAJWTSigner {

    /**
     * 构造函数，初始化 ECDSA 签名器。
     *
     * @param algorithm 算法标识（如 ES256、SHA256withECDSA）
     * @param key       ECDSA 密钥（公钥或私钥）
     * @throws IllegalArgumentException 如果密钥或算法无效
     */
    public ECDSAJWTSigner(final String algorithm, final Key key) {
        super(algorithm, key);
    }

    /**
     * 构造函数，初始化 ECDSA 签名器。
     *
     * @param algorithm 算法标识（如 ES256、SHA256withECDSA）
     * @param keyPair   ECDSA 密钥对（包含公钥和私钥）
     * @throws IllegalArgumentException 如果密钥对或算法无效
     */
    public ECDSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

    /**
     * 获取指定 ECDSA 算法的签名字节长度。
     * <p>
     * 根据算法（如 ES256、ES384、ES512），返回对应的签名长度（分别为 64、96、132 字节）。
     * </p>
     *
     * @param alg 算法标识（如 ES256、SHA256withECDSA）
     * @return 签名字节长度
     * @throws JWTException 如果算法不受支持
     */
    private static int getSignatureByteArrayLength(final String alg) throws JWTException {
        // 检查算法并返回对应的签名长度
        switch (alg) {
        case "ES256":
        case "SHA256withECDSA":
            return 64;
        case "ES384":
        case "SHA384withECDSA":
            return 96;
        case "ES512":
        case "SHA512withECDSA":
            return 132;
        default:
            // 抛出异常表示不支持的算法
            throw new JWTException("Unsupported Algorithm: {}", alg);
        }
    }

    /**
     * 将 DER 格式签名转换为 JWS 所需的 (R, S) 拼接格式。
     * <p>
     * 解析 DER 格式，提取 R 和 S 分量，拼接为固定长度的字节数组。
     * </p>
     *
     * @param derSignature DER 格式签名
     * @param outputLength 目标签名长度（由算法决定）
     * @return (R, S) 拼接格式的字节数组
     * @throws JWTException 如果 DER 格式无效
     */
    private static byte[] derToConcat(final byte[] derSignature, final int outputLength) throws JWTException {
        // 验证 DER 签名长度和起始字节（0x30 表示序列）
        if (derSignature.length < 8 || derSignature[0] != 48) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // 确定偏移量，处理长度字节（0x81 表示长长度）
        final int offset;
        if (derSignature[1] > 0) {
            offset = 2;
        } else if (derSignature[1] == (byte) 0x81) {
            offset = 3;
        } else {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // 获取 R 分量的长度
        final byte rLength = derSignature[offset + 1];
        // 跳过前导零字节，确定 R 的有效长度
        int i = rLength;
        while ((i > 0) && (derSignature[(offset + 2 + rLength) - i] == 0)) {
            i--;
        }

        // 获取 S 分量的长度
        final byte sLength = derSignature[offset + 2 + rLength + 1];
        // 跳过前导零字节，确定 S 的有效长度
        int j = sLength;
        while ((j > 0) && (derSignature[(offset + 2 + rLength + 2 + sLength) - j] == 0)) {
            j--;
        }

        // 确定输出数组的长度，取 R、S 和目标长度的最大值
        int rawLen = Math.max(i, j);
        rawLen = Math.max(rawLen, outputLength / 2);

        // 验证 DER 格式的完整性
        if ((derSignature[offset - 1] & 0xff) != derSignature.length - offset
                || (derSignature[offset - 1] & 0xff) != 2 + rLength + 2 + sLength || derSignature[offset] != 2
                || derSignature[offset + 2 + rLength] != 2) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // 创建输出数组，长度为 2 * rawLen
        final byte[] concatSignature = new byte[2 * rawLen];
        // 复制 R 分量到输出数组
        System.arraycopy(derSignature, (offset + 2 + rLength) - i, concatSignature, rawLen - i, i);
        // 复制 S 分量到输出数组
        System.arraycopy(derSignature, (offset + 2 + rLength + 2 + sLength) - j, concatSignature, 2 * rawLen - j, j);

        return concatSignature;
    }

    /**
     * 将 JWS 的 (R, S) 拼接格式转换为 DER 格式签名。
     * <p>
     * 从 (R, S) 字节数组中提取 R 和 S 分量，构建符合 ASN.1 DER 编码的签名。
     * </p>
     *
     * @param jwsSignature JWS 格式签名（(R, S) 拼接）
     * @return DER 格式签名字节数组
     * @throws JWTException 如果签名格式无效或长度过长
     */
    private static byte[] concatToDER(final byte[] jwsSignature) {
        // 计算 R 和 S 各占一半长度
        final int rawLen = jwsSignature.length / 2;

        // 跳过 R 分量的前导零字节
        int i = rawLen;
        while ((i > 0) && (jwsSignature[rawLen - i] == 0)) {
            i--;
        }

        // 确定 R 的实际长度，考虑符号位
        int j = i;
        if (jwsSignature[rawLen - i] < 0) {
            j += 1;
        }

        // 跳过 S 分量的前导零字节
        int k = rawLen;
        while ((k > 0) && (jwsSignature[2 * rawLen - k] == 0)) {
            k--;
        }

        // 确定 S 的实际长度，考虑符号位
        int l = k;
        if (jwsSignature[2 * rawLen - k] < 0) {
            l += 1;
        }

        // 计算 DER 编码的总长度
        final int len = 2 + j + 2 + l;
        // 验证长度是否有效（不超过 255）
        if (len > 255) {
            throw new JWTException("Invalid ECDSA signature format");
        }

        // 初始化 DER 输出数组
        final byte[] derSignature;
        int offset;
        // 根据长度选择编码方式（短长度或长长度）
        if (len < 128) {
            derSignature = new byte[2 + 2 + j + 2 + l];
            offset = 1;
        } else {
            derSignature = new byte[3 + 2 + j + 2 + l];
            derSignature[1] = (byte) 0x81;
            offset = 2;
        }

        // 设置 DER 序列头（0x30）
        derSignature[0] = 48;
        // 设置序列总长度
        derSignature[offset++] = (byte) len;
        // 设置 R 分量标识（0x02）
        derSignature[offset++] = 2;
        // 设置 R 分量长度
        derSignature[offset++] = (byte) j;
        // 复制 R 分量
        System.arraycopy(jwsSignature, rawLen - i, derSignature, (offset + j) - i, i);
        // 更新偏移量
        offset += j;
        // 设置 S 分量标识（0x02）
        derSignature[offset++] = 2;
        // 设置 S 分量长度
        derSignature[offset++] = (byte) l;
        // 复制 S 分量
        System.arraycopy(jwsSignature, 2 * rawLen - k, derSignature, (offset + l) - k, k);

        return derSignature;
    }

    /**
     * 对数据进行 ECDSA 签名，并将结果从 DER 格式转换为 (R, S) 对。
     * <p>
     * 调用父类的签名方法后，将 DER 格式签名转换为符合 JWS 的 (R, S) 拼接格式。
     * </p>
     *
     * @param data 要签名的数据
     * @return 签名后的 (R, S) 字节数组
     * @throws JWTException 如果签名过程失败或格式无效
     */
    @Override
    protected byte[] sign(final byte[] data) {
        // 调用父类 RSAJWTSigner 的签名方法，生成 DER 格式签名
        byte[] derSignature = super.sign(data);
        // 转换为 JWS 所需的 (R, S) 拼接格式
        return derToConcat(derSignature, getSignatureByteArrayLength(getAlgorithm()));
    }

    /**
     * 验证 ECDSA 签名，需将输入的 (R, S) 格式转换为 DER 格式。
     * <p>
     * 将 JWS 的 (R, S) 拼接格式转换为 DER 格式后，调用父类验证方法。
     * </p>
     *
     * @param data   要验证的数据
     * @param signed 签名字节数组（(R, S) 格式）
     * @return 是否验证通过
     * @throws JWTException 如果验证过程失败或格式无效
     */
    @Override
    protected boolean verify(final byte[] data, final byte[] signed) {
        // 将 (R, S) 格式转换为 DER 格式
        byte[] derSignature = concatToDER(signed);
        // 调用父类 RSAJWTSigner 的验证方法
        return super.verify(data, derSignature);
    }

}
