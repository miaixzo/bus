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
package org.miaixz.bus.auth;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.Getter;
import lombok.Setter;

/**
 * URL 构造工具类，支持 OAuth 相关功能。 提供方法以构建带查询参数的 URL、处理 OAuth 签名、生成 PKCE 校验码等功能，用于认证流程。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Builder {

    /**
     * 授权端点配置键
     */
    public static final String AUTHORIZE = "authorize";
    /**
     * 访问令牌端点配置键
     */
    public static final String ACCESSTOKEN = "accesstoken";
    /**
     * 用户信息端点配置键
     */
    public static final String USERINFO = "userinfo";
    /**
     * 刷新令牌端点配置键
     */
    public static final String REFRESH = "refresh";
    /**
     * 撤销授权端点配置键
     */
    public static final String REVOKE = "revoke";

    /**
     * 查询参数映射
     */
    private final Map<String, String> params = new LinkedHashMap<>(7);
    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 私有构造函数，防止直接实例化。
     */
    private Builder() {

    }

    /**
     * 从基础 URL 创建 Builder 实例。
     *
     * @param baseUrl 基础 URL
     * @return 新创建的 Builder 实例
     */
    public static Builder fromUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * 将字符串解析为键值对映射，字符串格式为 {@code key=value&key=value}。
     *
     * @param text 待解析的字符串
     * @return 键值对映射
     */
    public static Map<String, String> parseStringToMap(String text) {
        Map<String, String> res;
        if (text.contains(Symbol.AND)) {
            String[] fields = text.split(Symbol.AND);
            res = new HashMap<>((int) (fields.length / 0.75 + 1));
            for (String field : fields) {
                if (field.contains(Symbol.EQUAL)) {
                    String[] keyValue = field.split(Symbol.EQUAL);
                    res.put(UrlDecoder.decode(keyValue[0]),
                            keyValue.length == 2 ? UrlDecoder.decode(keyValue[1]) : null);
                }
            }
        } else {
            res = new HashMap<>(0);
        }
        return res;
    }

    /**
     * 将键值对映射转换为字符串，格式为 {@code key=value&key=value}。
     *
     * @param map    待转换的映射
     * @param encode 是否对值进行 URL 编码
     * @return 转换后的字符串，若映射为空则返回空字符串
     */
    public static String parseMapToString(Map<String, String> map, boolean encode) {
        if (null == map || map.isEmpty()) {
            return Normal.EMPTY;
        }
        List<String> paramList = new ArrayList<>();
        map.forEach((k, v) -> {
            if (null == v) {
                paramList.add(k + Symbol.EQUAL);
            } else {
                paramList.add(k + Symbol.EQUAL + (encode ? UrlEncoder.encodeAll(v) : v));
            }
        });
        return String.join(Symbol.AND, paramList);
    }

    /**
     * 生成 HMAC 签名。
     *
     * @param key       签名密钥
     * @param data      待签名数据
     * @param algorithm 签名算法（如 HMAC-SHA1）
     * @return 签名字节数组
     * @throws AuthorizedException 如果算法不支持或密钥无效
     */
    public static byte[] sign(byte[] key, byte[] data, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new AuthorizedException("Unsupported algorithm: " + algorithm, ex);
        } catch (InvalidKeyException ex) {
            throw new AuthorizedException("Invalid key: " + ArrayKit.toString(key), ex);
        }
    }

    /**
     * 生成 OAuth 2.0 PKCE 的校验码验证器（code verifier）。
     *
     * @return Base64 URL 安全的随机字符串
     */
    public static String codeVerifier() {
        return Base64.encodeUrlSafe(RandomKit.randomString(50));
    }

    /**
     * 生成 OAuth 2.0 PKCE 的校验码（code challenge）。 参考：https://tools.ietf.org/html/rfc7636#section-4.2
     *
     * @param codeChallengeMethod 校验方法（s256 或 plain）
     * @param codeVerifier        客户端生成的校验码验证器
     * @return 校验码
     */
    public static String codeChallenge(String codeChallengeMethod, String codeVerifier) {
        if (Algorithm.SHA256.getValue().equalsIgnoreCase(codeChallengeMethod)) {
            // code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
            return new String(Base64.encode(digest(codeVerifier), true), StandardCharsets.US_ASCII);
        } else {
            return codeVerifier;
        }
    }

    /**
     * 使用 SHA-256 算法对字符串进行摘要。
     *
     * @param str 待摘要的字符串
     * @return 摘要字节数组，若算法不可用则返回 null
     */
    public static byte[] digest(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(Algorithm.SHA256.getValue());
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构造 URL，不对参数值进行编码。
     *
     * @return 构造完成的 URL
     */
    public String build() {
        return this.build(false);
    }

    /**
     * 构造 URL，可选择是否对参数值进行编码。
     *
     * @param encode 是否对参数值进行 URL 编码
     * @return 构造完成的 URL
     */
    public String build(boolean encode) {
        if (MapKit.isEmpty(this.params)) {
            return this.baseUrl;
        }
        String baseUrl = StringKit.appendIfMissing(this.baseUrl, Symbol.QUESTION_MARK, Symbol.AND);
        String paramString = parseMapToString(this.params, encode);
        return baseUrl + paramString;
    }

    /**
     * 获取只读的参数映射。
     *
     * @return 不可修改的参数映射
     */
    public Map<String, Object> getReadOnlyParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 添加查询参数。
     *
     * @param key   参数名称
     * @param value 参数值
     * @return 当前 Builder 实例
     * @throws RuntimeException 如果参数名称为空
     */
    public Builder queryParam(String key, Object value) {
        if (StringKit.isEmpty(key)) {
            throw new RuntimeException("参数名称不能为空");
        }
        String valueAsString = (value != null ? value.toString() : null);
        this.params.put(key, valueAsString);
        return this;
    }

}