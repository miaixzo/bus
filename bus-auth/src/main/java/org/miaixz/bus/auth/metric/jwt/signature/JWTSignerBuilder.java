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
import java.util.HashMap;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.map.BiMap;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * JWT 签名器工厂。
 * <p>
 * 提供创建 {@link JWTSigner} 实例的功能，支持多种签名算法（如 HMAC、RSA、ECDSA）。 使用算法标识（如 HS256、RS256、ES256）与 Java 标准算法名称（如
 * HmacSHA256、SHA256withRSA）的双向映射。
 * </p>
 *
 * @see JWTSigner
 * @see HMacJWTSigner
 * @see RSAJWTSigner
 * @see ECDSAJWTSigner
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTSignerBuilder {

    /**
     * 正则表达式，匹配 ECDSA 算法标识（如 ES256、ES384、ES512）。
     */
    private static final Pattern ES_ALGORITHM_PATTERN = Pattern.compile("es\\d{3}", Pattern.CASE_INSENSITIVE);

    /**
     * 双向映射，存储 JWT 签名算法标识（如 HS256）与 Java 标准算法名称（如 HmacSHA256）的对应关系。
     */
    private static final BiMap<String, String> map = new BiMap<>(new HashMap<>() {
        {
            // 初始化 HMAC 算法映射
            put("HS256", Algorithm.HMACSHA256.getValue());
            put("HS384", Algorithm.HMACSHA384.getValue());
            put("HS512", Algorithm.HMACSHA512.getValue());
            put("HMD5", Algorithm.HMACMD5.getValue());
            put("HSHA1", Algorithm.HMACSHA1.getValue());
            put("SM4CMAC", Algorithm.SM4CMAC.getValue());
            // 初始化 RSA 算法映射
            put("RS256", Algorithm.SHA256WITHRSA.getValue());
            put("RS384", Algorithm.SHA384WITHRSA.getValue());
            put("RS512", Algorithm.SHA512WITHRSA.getValue());
            // 初始化 ECDSA 算法映射
            put("ES256", Algorithm.SHA256WITHECDSA.getValue());
            put("ES384", Algorithm.SHA384WITHECDSA.getValue());
            put("ES512", Algorithm.SHA512WITHECDSA.getValue());
            // 初始化 RSA-PSS 算法映射
            put("PS256", Algorithm.SHA256WITHRSA_PSS.getValue());
            put("PS384", Algorithm.SHA384WITHRSA_PSS.getValue());
            put("PS512", Algorithm.SHA512WITHRSA_PSS.getValue());
            // 初始化其他 RSA 算法映射
            put("RMD2", Algorithm.MD2WITHRSA.getValue());
            put("RMD5", Algorithm.MD5WITHRSA.getValue());
            put("RSHA1", Algorithm.SHA1WITHRSA.getValue());
            // 初始化 DSA 算法映射
            put("DNONE", Algorithm.NONEWITHDSA.getValue());
            put("DSHA1", Algorithm.SHA1WITHDSA.getValue());
            // 初始化其他 ECDSA 算法映射
            put("ENONE", Algorithm.NONEWITHECDSA.getValue());
            put("ESHA1", Algorithm.SHA1WITHECDSA.getValue());
        }
    });

    /**
     * 创建无签名器。
     *
     * @return 无签名的 {@link JWTSigner} 实例
     */
    public static JWTSigner none() {
        return NoneJWTSigner.NONE;
    }

    /**
     * 创建 HS256 (HmacSHA256) 签名器。
     *
     * @param key 密钥（字节数组）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner hs256(final byte[] key) {
        return createSigner("HS256", key);
    }

    /**
     * 创建 HS384 (HmacSHA384) 签名器。
     *
     * @param key 密钥（字节数组）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner hs384(final byte[] key) {
        return createSigner("HS384", key);
    }

    /**
     * 创建 HS512 (HmacSHA512) 签名器。
     *
     * @param key 密钥（字节数组）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner hs512(final byte[] key) {
        return createSigner("HS512", key);
    }

    /**
     * 创建 RS256 (SHA256withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rs256(final Key key) {
        return createSigner("RS256", key);
    }

    /**
     * 创建 RS384 (SHA384withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rs384(final Key key) {
        return createSigner("RS384", key);
    }

    /**
     * 创建 RS512 (SHA512withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rs512(final Key key) {
        return createSigner("RS512", key);
    }

    /**
     * 创建 ES256 (SHA256withECDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link ECDSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner es256(final Key key) {
        return createSigner("ES256", key);
    }

    /**
     * 创建 ES384 (SHA384withECDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link ECDSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner es384(final Key key) {
        return createSigner("ES384", key);
    }

    /**
     * 创建 ES512 (SHA512withECDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link ECDSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner es512(final Key key) {
        return createSigner("ES512", key);
    }

    /**
     * 创建 HMD5 (HmacMD5) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner hmd5(final Key key) {
        return createSigner("HMD5", key);
    }

    /**
     * 创建 HSHA1 (HmacSHA1) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner hsha1(final Key key) {
        return createSigner("HSHA1", key);
    }

    /**
     * 创建 SM4CMAC 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link HMacJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner sm4cmac(final Key key) {
        return createSigner("SM4CMAC", key);
    }

    /**
     * 创建 RMD2 (MD2withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rmd2(final Key key) {
        return createSigner("RMD2", key);
    }

    /**
     * 创建 RMD5 (MD5withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rmd5(final Key key) {
        return createSigner("RMD5", key);
    }

    /**
     * 创建 RSHA1 (SHA1withRSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner rsha1(final Key key) {
        return createSigner("RSHA1", key);
    }

    /**
     * 创建 DNONE (NONEwithDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner dnone(final Key key) {
        return createSigner("DNONE", key);
    }

    /**
     * 创建 DSHA1 (SHA1withDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link RSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner dsha1(final Key key) {
        return createSigner("DSHA1", key);
    }

    /**
     * 创建 ENONE (NONEwithECDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link ECDSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner enone(final Key key) {
        return createSigner("ENONE", key);
    }

    /**
     * 创建 ESHA1 (SHA1withECDSA) 签名器。
     *
     * @param key 密钥（公钥或私钥）
     * @return {@link ECDSAJWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空
     */
    public static JWTSigner esha1(final Key key) {
        return createSigner("ESHA1", key);
    }

    /**
     * 创建签名器（使用字节数组密钥）。
     * <p>
     * 根据算法 ID 创建合适的签名器实例（仅支持 HMAC 算法）。
     * </p>
     *
     * @param algorithmId 算法 ID（如 HS256、HS384、HS512）
     * @param key         密钥（字节数组）
     * @return {@link JWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空或算法 ID 无效
     */
    public static JWTSigner createSigner(final String algorithmId, final byte[] key) {
        // 验证密钥非空
        Assert.notNull(key, "Signer key must be not null!");
        // 检查是否为无签名算法
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return none();
        }
        return new HMacJWTSigner(getAlgorithm(algorithmId), key);
    }

    /**
     * 创建签名器（使用密钥对）。
     * <p>
     * 根据算法 ID 创建合适的签名器实例（支持 RSA 或 ECDSA 算法）。
     * </p>
     *
     * @param algorithmId 算法 ID（如 RS256、ES256）
     * @param keyPair     密钥对（包含公钥和私钥）
     * @return {@link JWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥对为空或算法 ID 无效
     */
    public static JWTSigner createSigner(final String algorithmId, final KeyPair keyPair) {
        // 验证密钥对非空
        Assert.notNull(keyPair, "Signer key pair must be not null!");
        // 检查是否为无签名算法
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return none();
        }
        // 获取 Java 标准算法名称
        final String algorithm = getAlgorithm(algorithmId);
        // 检查是否为 ECDSA 算法
        if (PatternKit.isMatch(ES_ALGORITHM_PATTERN, algorithmId)) {
            return new ECDSAJWTSigner(algorithm, keyPair);
        }
        return new RSAJWTSigner(algorithm, keyPair);
    }

    /**
     * 创建签名器（使用公钥或私钥）。
     * <p>
     * 根据算法 ID 和密钥类型创建合适的签名器实例（支持 HMAC、RSA 或 ECDSA 算法）。
     * </p>
     *
     * @param algorithmId 算法 ID（如 HS256、RS256、ES256）
     * @param key         密钥（公钥、私钥或对称密钥）
     * @return {@link JWTSigner} 实例
     * @throws IllegalArgumentException 如果密钥为空或算法 ID 无效
     */
    public static JWTSigner createSigner(final String algorithmId, final Key key) {
        // 验证密钥非空
        Assert.notNull(key, "Signer key must be not null!");
        // 检查是否为无签名算法
        if (null == algorithmId || NoneJWTSigner.ID_NONE.equals(algorithmId)) {
            return NoneJWTSigner.NONE;
        }
        // 获取 Java 标准算法名称
        final String algorithm = getAlgorithm(algorithmId);
        // 检查密钥类型是否为公钥或私钥
        if (key instanceof PrivateKey || key instanceof PublicKey) {
            // 检查是否为 ECDSA 算法
            if (PatternKit.isMatch(ES_ALGORITHM_PATTERN, algorithmId)) {
                return new ECDSAJWTSigner(algorithm, key);
            }
            return new RSAJWTSigner(algorithm, key);
        }
        return new HMacJWTSigner(algorithm, key);
    }

    /**
     * 获取算法名称。
     * <p>
     * 如果输入为 JWT 算法标识（如 HS256），返回对应的 Java 标准算法名称（如 HmacSHA256）； 否则返回输入值本身。
     * </p>
     *
     * @param idOrAlgorithm 算法 ID 或算法名称
     * @return 算法名称
     */
    public static String getAlgorithm(final String idOrAlgorithm) {
        return ObjectKit.defaultIfNull(getAlgorithmById(idOrAlgorithm), idOrAlgorithm);
    }

    /**
     * 获取 JWT 算法标识。
     * <p>
     * 如果输入为 Java 标准算法名称（如 HmacSHA256），返回对应的 JWT 算法标识（如 HS256）； 否则返回输入值本身。
     * </p>
     *
     * @param idOrAlgorithm 算法 ID 或算法名称
     * @return JWT 算法标识
     */
    public static String getId(final String idOrAlgorithm) {
        return ObjectKit.defaultIfNull(getIdByAlgorithm(idOrAlgorithm), idOrAlgorithm);
    }

    /**
     * 根据 JWT 算法标识获取 Java 标准算法名称。
     *
     * @param id JWT 算法标识（如 HS256）
     * @return Java 标准算法名称（如 HmacSHA256），或 null 如果不存在
     */
    private static String getAlgorithmById(final String id) {
        return map.get(id.toUpperCase());
    }

    /**
     * 根据 Java 标准算法名称获取 JWT 算法标识。
     *
     * @param algorithm Java 标准算法名称（如 HmacSHA256）
     * @return JWT 算法标识（如 HS256），或 null 如果不存在
     */
    private static String getIdByAlgorithm(final String algorithm) {
        return map.getKey(algorithm);
    }

}