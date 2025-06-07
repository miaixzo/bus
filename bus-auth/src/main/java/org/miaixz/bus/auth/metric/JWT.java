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
package org.miaixz.bus.auth.metric;

import java.lang.reflect.Type;
import java.security.Key;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.metric.jwt.JWTHeader;
import org.miaixz.bus.auth.metric.jwt.JWTPayload;
import org.miaixz.bus.auth.metric.jwt.JWTRegister;
import org.miaixz.bus.auth.metric.jwt.JWTVerifier;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.auth.metric.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.JWTException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JSON Web Token (JWT) 实现类，基于 RFC 7519 标准，用于在网络应用间传递声明。
 * <ul>
 * <li>header：声明签名算法等信息</li>
 * <li>payload：承载声明和明文数据</li>
 * <li>signature：签名部分（JWS），确保数据完整性</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWT implements JWTRegister<JWT> {

    /**
     * JWT 头部信息
     */
    private final JWTHeader header;
    /**
     * JWT 载荷信息
     */
    private final JWTPayload payload;
    /**
     * 编码格式，默认 UTF-8
     */
    private java.nio.charset.Charset charset;
    /**
     * 签名器，用于生成和验证签名
     */
    private JWTSigner signer;
    /**
     * 解析后的 JWT 令牌分段（header, payload, signature）
     */
    private List<String> tokens;

    /**
     * 创建空的 JWT 对象。
     *
     * @return 新的 JWT 实例
     */
    public static JWT of() {
        return new JWT();
    }

    /**
     * 创建并解析 JWT 对象。
     *
     * @param token JWT 令牌字符串，格式为 xxxx.yyyy.zzzz
     * @return 解析后的 JWT 实例
     * @throws IllegalArgumentException 如果令牌为空
     */
    public static JWT of(final String token) {
        return new JWT(token);
    }

    /**
     * 构造函数，初始化空的 JWT 对象，设置默认编码为 UTF-8。
     */
    public JWT() {
        this.header = new JWTHeader();
        this.payload = new JWTPayload();
        this.charset = Charset.UTF_8;
    }

    /**
     * 构造函数，初始化并解析 JWT 令牌。
     *
     * @param token JWT 令牌字符串
     */
    public JWT(final String token) {
        this();
        parse(token);
    }

    /**
     * 解析 JWT 令牌字符串，分解为 header、payload 和 signature 三部分。
     *
     * @param token JWT 令牌字符串
     * @return 当前 JWT 实例
     * @throws IllegalArgumentException 如果令牌为空或格式不正确
     */
    public JWT parse(final String token) throws IllegalArgumentException {
        Assert.notBlank(token, "Token String must be not blank!");
        final List<String> tokens = splitToken(token);
        this.tokens = tokens;
        this.header.parse(tokens.get(0), this.charset);
        this.payload.parse(tokens.get(1), this.charset);
        return this;
    }

    /**
     * 设置 JWT 使用的字符编码。
     *
     * @param charset 字符编码
     * @return 当前 JWT 实例
     */
    public JWT setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置签名密钥，默认使用 HS256 (HmacSHA256) 算法。
     *
     * @param key 签名密钥
     * @return 当前 JWT 实例
     */
    public JWT setKey(final byte[] key) {
        final String algorithmId = (String) this.header.getClaim(JWTHeader.ALGORITHM);
        if (StringKit.isNotBlank(algorithmId)) {
            return setSigner(algorithmId, key);
        }
        return setSigner(JWTSignerBuilder.hs256(key));
    }

    /**
     * 设置签名算法和密钥。
     *
     * @param algorithmId 签名算法 ID（如 HS256）
     * @param key         签名密钥
     * @return 当前 JWT 实例
     */
    public JWT setSigner(final String algorithmId, final byte[] key) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, key));
    }

    /**
     * 设置签名算法和密钥。
     *
     * @param algorithmId 签名算法 ID（如 HS256）
     * @param key         签名密钥
     * @return 当前 JWT 实例
     */
    public JWT setSigner(final String algorithmId, final Key key) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, key));
    }

    /**
     * 设置非对称签名算法和密钥对。
     *
     * @param algorithmId 签名算法 ID（如 RS256）
     * @param keyPair     密钥对
     * @return 当前 JWT 实例
     */
    public JWT setSigner(final String algorithmId, final KeyPair keyPair) {
        return setSigner(JWTSignerBuilder.createSigner(algorithmId, keyPair));
    }

    /**
     * 设置签名器。
     *
     * @param signer 签名器
     * @return 当前 JWT 实例
     */
    public JWT setSigner(final JWTSigner signer) {
        this.signer = signer;
        return this;
    }

    /**
     * 获取签名器。
     *
     * @return 当前使用的签名器
     */
    public JWTSigner getSigner() {
        return this.signer;
    }

    /**
     * 获取所有头部信息。
     *
     * @return 头部信息的 Map
     */
    public Map<String, Object> getHeaders() {
        return this.header.getClaimsJson();
    }

    /**
     * 获取头部对象。
     *
     * @return JWTHeader 实例
     */
    public JWTHeader getHeader() {
        return this.header;
    }

    /**
     * 获取指定头部信息。
     *
     * @param name 头部字段名称
     * @return 头部字段值
     */
    public Object getHeader(final String name) {
        return this.header.getClaim(name);
    }

    /**
     * 获取算法 ID（alg）头部信息。
     *
     * @return 算法 ID
     * @see JWTHeader#ALGORITHM
     */
    public String getAlgorithm() {
        return (String) this.header.getClaim(JWTHeader.ALGORITHM);
    }

    /**
     * 设置头部信息。
     *
     * @param name  头部字段名称
     * @param value 头部字段值
     * @return 当前 JWT 实例
     */
    public JWT setHeader(final String name, final Object value) {
        this.header.setClaim(name, value);
        return this;
    }

    /**
     * 添加多个头部信息。
     *
     * @param headers 头部信息 Map
     * @return 当前 JWT 实例
     */
    public JWT addHeaders(final Map<String, ?> headers) {
        this.header.putAll(headers);
        return this;
    }

    /**
     * 获取所有载荷信息。
     *
     * @return 载荷信息的 Map
     */
    public Map<String, Object> getPayloads() {
        return this.payload.getClaimsJson();
    }

    /**
     * 获取载荷对象。
     *
     * @return JWTPayload 实例
     */
    public JWTPayload getPayload() {
        return this.payload;
    }

    /**
     * 获取指定载荷信息。
     *
     * @param name 载荷字段名称
     * @return 载荷字段值
     */
    public Object getPayload(final String name) {
        return getPayload().getClaim(name);
    }

    /**
     * 获取指定载荷信息并转换为指定类型。
     *
     * @param <T>          目标类型
     * @param propertyName 载荷字段名称
     * @param propertyType 目标类型
     * @return 转换后的载荷字段值，或 null 如果无法转换
     */
    public <T> T getPayload(final String propertyName, final Type propertyType) {
        Object value = getPayload().getClaim(propertyName);
        if (value != null && propertyType instanceof Class) {
            return ((Class<T>) propertyType).cast(value);
        }
        return null;
    }

    /**
     * 设置载荷信息。
     *
     * @param name  载荷字段名称
     * @param value 载荷字段值
     * @return 当前 JWT 实例
     */
    @Override
    public JWT setPayload(final String name, final Object value) {
        this.payload.setClaim(name, value);
        return this;
    }

    /**
     * 添加多个载荷信息。
     *
     * @param payloads 载荷信息 Map
     * @return 当前 JWT 实例
     */
    public JWT addPayloads(final Map<String, ?> payloads) {
        this.payload.putAll(payloads);
        return this;
    }

    /**
     * 使用默认签名器生成 JWT 字符串。
     *
     * @return JWT 字符串（header.payload.signature）
     */
    public String sign() {
        return sign(this.signer);
    }

    /**
     * 使用指定签名器生成 JWT 字符串
     * <p>
     * 自动补充头部信息：
     * <ul>
     * <li>若未定义 "alg"，根据签名器设置算法 ID</li>
     * </ul>
     *
     * @param signer 签名器
     * @return JWT 字符串
     * @throws JWTException 如果签名器为 null
     */
    public String sign(final JWTSigner signer) {
        Assert.notNull(signer, () -> new JWTException("No Signer provided!"));
        final String algorithm = (String) this.header.getClaim(JWTHeader.ALGORITHM);
        if (StringKit.isBlank(algorithm)) {
            this.header.setClaim(JWTHeader.ALGORITHM, JWTSignerBuilder.getId(signer.getAlgorithm()));
        }
        final String headerBase64 = Base64.encodeUrlSafe(this.header.toString(), charset);
        final String payloadBase64 = Base64.encodeUrlSafe(this.payload.toString(), charset);
        final String sign = signer.sign(headerBase64, payloadBase64);
        return StringKit.format("{}.{}.{}", headerBase64, payloadBase64, sign);
    }

    /**
     * 验证 JWT 令牌是否有效。
     *
     * @return true 如果签名有效，否则 false
     */
    public boolean verify() {
        return verify(this.signer);
    }

    /**
     * 验证 JWT 令牌是否有效，包括签名和时间字段检查。
     * <p>
     * 检查内容：
     * <ul>
     * <li>签名是否有效</li>
     * <li>notBefore：生效时间不能晚于当前时间</li>
     * <li>expiresAt：失效时间不能早于当前时间</li>
     * <li>issuedAt：签发时间不能晚于当前时间</li>
     * </ul>
     *
     * @param leeway 容忍时间（秒），用于时间检查的宽松度
     * @return true 如果令牌有效，否则 false
     */
    public boolean validate(final long leeway) {
        if (!verify()) {
            return false;
        }
        try {
            JWTVerifier.of(this).validateDate(DateKit.now(), leeway);
        } catch (final ValidateException e) {
            return false;
        }
        return true;
    }

    /**
     * 使用指定签名器验证 JWT 令牌。
     * <p>
     * 如果签名器为 null 或 NoneJWTSigner，则认为 JWT 无签名，签名部分必须为空。
     *
     * @param signer 签名器，null 时默认为无签名
     * @return true 如果签名有效，否则 false
     * @throws JWTException 如果没有可验证的令牌
     */
    public boolean verify(JWTSigner signer) {
        if (null == signer) {
            signer = NoneJWTSigner.NONE;
        }
        final List<String> tokens = this.tokens;
        if (CollKit.isEmpty(tokens)) {
            throw new JWTException("No token to verify!");
        }
        return signer.verify(tokens.get(0), tokens.get(1), tokens.get(2));
    }

    /**
     * 将 JWT 令牌字符串拆分为三部分（header, payload, signature）。
     *
     * @param token JWT 令牌字符串
     * @return 包含三部分的 List
     * @throws JWTException 如果令牌格式不正确（不是三部分）
     */
    public static List<String> splitToken(final String token) {
        final List<String> tokens = StringKit.split(token, Symbol.DOT);
        if (3 != tokens.size()) {
            throw new JWTException("The token was expected 3 parts, but got {}.", tokens.size());
        }
        return tokens;
    }

}