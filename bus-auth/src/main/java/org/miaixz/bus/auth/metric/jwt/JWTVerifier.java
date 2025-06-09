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
package org.miaixz.bus.auth.metric.jwt;

import java.util.Date;
import java.util.Map;

import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JWT 数据校验器，用于验证 JWT 的算法、签名和时间字段。
 * <ul>
 * <li>算法是否一致</li>
 * <li>签名是否正确</li>
 * <li>时间字段是否有效（如未过期、已生效等）</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTVerifier {

    /**
     * 要验证的 JWT 对象
     */
    private final JWT jwt;

    /**
     * 构造函数，初始化验证器。
     *
     * @param jwt 要验证的 JWT 对象
     */
    public JWTVerifier(final JWT jwt) {
        this.jwt = jwt;
    }

    /**
     * 创建 JWT 验证器，从令牌字符串初始化。
     *
     * @param token JWT 令牌字符串
     * @return this
     */
    public static JWTVerifier of(final String token) {
        return new JWTVerifier(JWT.of(token));
    }

    /**
     * 创建 JWT 验证器，从现有 JWT 对象初始化。
     *
     * @param jwt JWT 对象
     * @return this
     */
    public static JWTVerifier of(final JWT jwt) {
        return new JWTVerifier(jwt);
    }

    /**
     * 验证JWT Token有效性
     *
     * @param token JWT Token
     * @param key   HS256(HmacSHA256)密钥
     * @return 是否有效
     */
    public static boolean verify(final String token, final byte[] key) {
        return JWT.of(token).setKey(key).verify();
    }

    /**
     * 验证JWT Token有效性
     *
     * @param token  JWT Token
     * @param signer 签名器
     * @return 是否有效
     */
    public static boolean verify(final String token, final JWTSigner signer) {
        return JWT.of(token).verify(signer);
    }

    /**
     * 验证 JWT 的算法和签名。
     *
     * @param jwt    JWT 对象
     * @param signer 用于验证的签名器，null 时使用 JWT 自带的签名器
     * @throws ValidateException 如果算法不匹配或签名无效
     */
    private static void validateAlgorithm(final JWT jwt, JWTSigner signer) throws ValidateException {
        final String algorithmId = jwt.getAlgorithm();
        if (null == signer) {
            signer = jwt.getSigner();
        }
        if (StringKit.isEmpty(algorithmId)) {
            if (null == signer || signer instanceof NoneJWTSigner) {
                return;
            }
            throw new ValidateException("No algorithm defined in header!");
        }
        if (null == signer) {
            throw new IllegalArgumentException("No Signer for validate algorithm!");
        }
        final String algorithmIdInSigner = signer.getAlgorithmId();
        if (!StringKit.equals(algorithmId, algorithmIdInSigner)) {
            throw new ValidateException("Algorithm [{}] defined in header doesn't match to [{}]!", algorithmId,
                    algorithmIdInSigner);
        }
        if (!jwt.verify(signer)) {
            throw new ValidateException("Signature verification failed!");
        }
    }

    /**
     * 验证 JWT 的时间字段。
     * <p>
     * 检查以下字段：
     * <ul>
     * <li>notBefore (nbf)：生效时间不能晚于当前时间</li>
     * <li>expiresAt (exp)：失效时间不能早于当前时间</li>
     * <li>issuedAt (iat)：签发时间不能晚于当前时间</li>
     * </ul>
     * 未设置的字段不检查。
     * </p>
     *
     * @param payload JWT 载荷对象
     * @param now     当前时间，null 时使用系统时间
     * @param leeway  容忍时间（秒），用于时间检查的宽松度
     * @throws ValidateException 如果时间字段无效
     */
    private static void validateDate(final JWTPayload payload, Date now, final long leeway) throws ValidateException {
        if (null == now) {
            now = DateKit.now();
            now.setTime(now.getTime() / 1000 * 1000);
        }
        final Map<String, Object> claims = payload.getClaimsJson();
        final Long notBefore = claims.get(JWTPayload.NOT_BEFORE) instanceof Long
                ? (Long) claims.get(JWTPayload.NOT_BEFORE)
                : null;
        final Long expiresAt = claims.get(JWTPayload.EXPIRES_AT) instanceof Long
                ? (Long) claims.get(JWTPayload.EXPIRES_AT)
                : null;
        final Long issueAt = claims.get(JWTPayload.ISSUED_AT) instanceof Long ? (Long) claims.get(JWTPayload.ISSUED_AT)
                : null;

        validateNotAfter(JWTPayload.NOT_BEFORE, notBefore, now, leeway);
        validateNotBefore(JWTPayload.EXPIRES_AT, expiresAt, now, leeway);
        validateNotAfter(JWTPayload.ISSUED_AT, issueAt, now, leeway);
    }

    /**
     * 验证指定时间字段是否不晚于当前时间。
     * <p>
     * 如果字段不存在，则跳过检查。
     * </p>
     *
     * @param fieldName   字段名（如 nbf, iat）
     * @param dateToCheck 时间值（秒级时间戳）
     * @param now         当前时间
     * @param leeway      容忍时间（秒），向后容忍
     * @throws ValidateException 如果时间晚于当前时间
     */
    private static void validateNotAfter(final String fieldName, final Long dateToCheck, Date now, final long leeway)
            throws ValidateException {
        if (dateToCheck == null) {
            return;
        }
        Date checkDate = new Date(dateToCheck * 1000);
        if (leeway > 0) {
            now = new Date(now.getTime() + leeway * 1000);
        }
        if (checkDate.after(now)) {
            throw new ValidateException("'{}':[{}] is after now:[{}]", fieldName, DateKit.date(checkDate),
                    DateKit.date(now));
        }
    }

    /**
     * 验证指定时间字段是否不早于当前时间。
     * <p>
     * 如果字段不存在，则跳过检查。
     * </p>
     *
     * @param fieldName   字段名（如 exp）
     * @param dateToCheck 时间值（秒级时间戳）
     * @param now         当前时间
     * @param leeway      容忍时间（秒），向前容忍
     * @throws ValidateException 如果时间早于当前时间
     */
    private static void validateNotBefore(final String fieldName, final Long dateToCheck, Date now, final long leeway)
            throws ValidateException {
        if (dateToCheck == null) {
            return;
        }
        Date checkDate = new Date(dateToCheck * 1000);
        if (leeway > 0) {
            now = new Date(now.getTime() - leeway * 1000);
        }
        if (checkDate.before(now)) {
            throw new ValidateException("'{}':[{}] is before now:[{}]", fieldName, DateKit.date(checkDate),
                    DateKit.date(now));
        }
    }

    /**
     * 验证 JWT 的算法和签名，使用 JWT 对象自带的签名器。
     *
     * @return 当前 JWTVerifier 实例
     * @throws ValidateException 如果算法不匹配或签名无效
     */
    public JWTVerifier validateAlgorithm() throws ValidateException {
        return validateAlgorithm(null);
    }

    /**
     * 验证 JWT 的算法和签名，使用指定的签名器。
     *
     * @param signer 用于验证的签名器，null 时使用 JWT 自带的签名器
     * @return 当前 JWTVerifier 实例
     * @throws ValidateException        如果算法不匹配或签名无效
     * @throws IllegalArgumentException 如果未提供签名器且 JWT 要求签名
     */
    public JWTVerifier validateAlgorithm(final JWTSigner signer) throws ValidateException {
        validateAlgorithm(this.jwt, signer);
        return this;
    }

    /**
     * 验证 JWT 的时间字段，使用当前时间。
     * <ul>
     * <li>notBefore (nbf)：生效时间不能晚于当前时间</li>
     * <li>expiresAt (exp)：失效时间不能早于当前时间</li>
     * <li>issuedAt (iat)：签发时间不能晚于当前时间</li>
     * </ul>
     * 未设置的字段不检查。
     *
     * @return 当前 JWTVerifier 实例
     * @throws ValidateException 如果时间字段无效
     */
    public JWTVerifier validateDate() throws ValidateException {
        return validateDate(DateKit.beginOfSecond(DateKit.now()));
    }

    /**
     * 验证 JWT 的时间字段，使用指定时间。
     *
     * @param dateToCheck 被检查的时间，通常为当前时间
     * @return this
     * @throws ValidateException 如果时间字段无效
     */
    public JWTVerifier validateDate(final Date dateToCheck) throws ValidateException {
        validateDate(this.jwt.getPayload(), dateToCheck, 0L);
        return this;
    }

    /**
     * 验证 JWT 的时间字段，带容忍时间。
     *
     * @param dateToCheck 被检查的时间，通常为当前时间
     * @param leeway      容忍时间（秒），用于时间检查的宽松度
     * @return this
     * @throws ValidateException 如果时间字段无效
     */
    public JWTVerifier validateDate(final Date dateToCheck, final long leeway) throws ValidateException {
        validateDate(this.jwt.getPayload(), dateToCheck, leeway);
        return this;
    }

}