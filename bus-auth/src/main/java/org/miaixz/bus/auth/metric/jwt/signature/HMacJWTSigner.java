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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.center.HMac;

/**
 * HMAC 算法 JWT 签名器。
 * <p>
 * 实现 {@link JWTSigner} 接口，使用 HMAC 算法（如 HS256、HS384、HS512）对 JWT 进行签名和验证。 支持自定义编码，默认使用 UTF-8。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HMacJWTSigner implements JWTSigner {

    /**
     * HMAC 算法实例，用于执行签名和验证。
     */
    private final HMac hMac;
    /**
     * 编码方式，默认 UTF-8。
     */
    private java.nio.charset.Charset charset = Charset.UTF_8;

    /**
     * 构造函数，初始化 HMAC 签名器。
     *
     * @param algorithm HMAC 算法（如 HS256、HS384、HS512）
     * @param key       密钥（字节数组）
     * @throws IllegalArgumentException 如果算法或密钥无效
     */
    public HMacJWTSigner(final String algorithm, final byte[] key) {
        // 初始化 HMAC 算法实例
        this.hMac = new HMac(algorithm, key);
    }

    /**
     * 构造函数，初始化 HMAC 签名器。
     *
     * @param algorithm HMAC 算法（如 HS256、HS384、HS512）
     * @param key       密钥（Java 安全密钥对象）
     * @throws IllegalArgumentException 如果算法或密钥无效
     */
    public HMacJWTSigner(final String algorithm, final Key key) {
        // 初始化 HMAC 算法实例
        this.hMac = new HMac(algorithm, key);
    }

    /**
     * 设置编码方式。
     *
     * @param charset 编码方式（如 UTF-8）
     * @return 当前对象，支持链式调用
     * @throws IllegalArgumentException 如果编码无效
     */
    public HMacJWTSigner setCharset(final java.nio.charset.Charset charset) {
        // 更新编码方式
        this.charset = charset;
        return this;
    }

    /**
     * 对 JWT 的 header 和 payload 进行 HMAC 签名。
     * <p>
     * 将 Base64 编码的 header 和 payload 拼接为 "header.payload" 格式， 使用 HMAC 算法生成 Base64 签名。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @return Base64 编码的签名
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        // 拼接 header 和 payload，格式为 "header.payload"
        String data = StringKit.format("{}.{}", headerBase64, payloadBase64);
        // 使用 HMAC 算法生成 Base64 签名
        return hMac.digestBase64(data, charset, true);
    }

    /**
     * 验证 JWT 签名。
     * <p>
     * 使用 HMAC 算法对 header 和 payload 重新生成签名，与提供的签名进行比较。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @param signBase64    Base64 编码的签名
     * @return 是否验证通过
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        // 生成预期签名
        final String sign = sign(headerBase64, payloadBase64);
        // 比较预期签名与提供的签名
        return hMac.verify(ByteKit.toBytes(sign, charset), ByteKit.toBytes(signBase64, charset));
    }

    /**
     * 获取签名算法名称。
     *
     * @return 算法名称（如 HS256、HS384、HS512）
     */
    @Override
    public String getAlgorithm() {
        // 返回 HMAC 算法名称
        return this.hMac.getAlgorithm();
    }

}