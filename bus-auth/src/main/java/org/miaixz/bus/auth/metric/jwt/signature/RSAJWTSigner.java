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
import java.security.PrivateKey;
import java.security.PublicKey;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.Sign;

/**
 * RSA 非对称加密 JWT 签名器。
 * <p>
 * 实现 {@link JWTSigner} 接口，使用 RSA 算法（如 RS256、RS384、RS512）对 JWT 进行签名和验证。 支持公钥验证签名和私钥生成签名，默认编码为 UTF-8。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see JWTSigner
 */
public class RSAJWTSigner implements JWTSigner {

    /**
     * RSA 签名和验证的核心实现。
     */
    private final Sign sign;

    /**
     * 编码方式，默认 UTF-8。
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * 构造函数，初始化 RSA 签名器。
     * <p>
     * 根据提供的算法和密钥（公钥或私钥）初始化签名器，公钥用于验证签名，私钥用于生成签名。
     * </p>
     *
     * @param algorithm 算法标识（如 RS256、SHA256withRSA）
     * @param key       密钥（公钥 {@link PublicKey} 或私钥 {@link PrivateKey}）
     * @throws IllegalArgumentException 如果算法或密钥无效
     */
    public RSAJWTSigner(final String algorithm, final Key key) {
        // 提取公钥或私钥
        final PublicKey publicKey = key instanceof PublicKey ? (PublicKey) key : null;
        final PrivateKey privateKey = key instanceof PrivateKey ? (PrivateKey) key : null;
        // 初始化签名器，构造密钥对
        this.sign = new Sign(algorithm, new KeyPair(publicKey, privateKey));
    }

    /**
     * 构造函数，初始化 RSA 签名器。
     * <p>
     * 使用密钥对（包含公钥和私钥）初始化签名器。
     * </p>
     *
     * @param algorithm 算法标识（如 RS256、SHA256withRSA）
     * @param keyPair   密钥对（包含公钥和私钥）
     * @throws IllegalArgumentException 如果算法或密钥对无效
     */
    public RSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        // 初始化签名器，使用密钥对
        this.sign = new Sign(algorithm, keyPair);
    }

    /**
     * 设置编码方式。
     *
     * @param charset 编码方式（如 UTF-8）
     * @return 当前对象，支持链式调用
     * @throws IllegalArgumentException 如果编码无效
     */
    public RSAJWTSigner setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 对 JWT 的 header 和 payload 进行 RSA 签名。
     * <p>
     * 将 Base64 编码的 header 和 payload 拼接为 "header.payload" 格式， 使用 RSA 算法生成签名并返回 Base64 编码结果。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @return Base64 编码的签名
     * @throws IllegalStateException 如果私钥不可用
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        // 拼接 header 和 payload，格式为 "header.payload"
        final String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        // 将字符串转换为字节数组并签名
        byte[] signedData = sign(ByteKit.toBytes(data, charset));
        // 返回 Base64 URL 安全的签名
        return Base64.encodeUrlSafe(signedData);
    }

    /**
     * 对字节数组数据进行 RSA 签名。
     * <p>
     * 使用配置的私钥和算法生成签名。
     * </p>
     *
     * @param data 要签名的数据
     * @return 签名字节数组
     * @throws IllegalStateException 如果私钥不可用
     */
    protected byte[] sign(final byte[] data) {
        return sign.sign(data);
    }

    /**
     * 验证 JWT 签名。
     * <p>
     * 使用公钥验证 Base64 编码的签名是否与 header 和 payload 匹配。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @param signBase64    Base64 编码的签名
     * @return 是否验证通过
     * @throws IllegalStateException 如果公钥不可用
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        // 拼接 header 和 payload，格式为 "header.payload"
        byte[] data = ByteKit.toBytes(StringKit.format("{}.{}", headerBase64, payloadBase64), charset);
        // 解码 Base64 签名
        byte[] signed = Base64.decode(signBase64);
        // 验证签名
        return verify(data, signed);
    }

    /**
     * 验证数据的 RSA 签名。
     * <p>
     * 使用配置的公钥验证数据和签名的匹配性。
     * </p>
     *
     * @param data   要验证的数据
     * @param signed 签名字节数组
     * @return 是否验证通过
     * @throws IllegalStateException 如果公钥不可用
     */
    protected boolean verify(final byte[] data, final byte[] signed) {
        return sign.verify(data, signed);
    }

    /**
     * 获取签名算法名称。
     *
     * @return 算法名称（如 SHA256withRSA）
     */
    @Override
    public String getAlgorithm() {
        return this.sign.getSignature().getAlgorithm();
    }

}