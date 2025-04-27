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
package org.miaixz.bus.oauth;

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
 * 构造URL
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Builder {

    private final Map<String, String> params = new LinkedHashMap<>(7);
    private String baseUrl;

    private Builder() {

    }

    /**
     * @param baseUrl 基础路径
     * @return the new {@code Builder}
     */
    public static Builder fromUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * string字符串转map，str格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param text 待转换的字符串
     * @return the {@link Map}
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
     * map转字符串，转换后的字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param map    待转换的map
     * @param encode 是否转码
     * @return the {@link String}
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
     * 签名
     *
     * @param key       key
     * @param data      data
     * @param algorithm algorithm
     * @return byte[]
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

    public static String codeVerifier() {
        return Base64.encodeUrlSafe(RandomKit.randomString(50));
    }

    /**
     * 适用于 OAuth 2.0 PKCE 增强协议
     *
     * @param codeChallengeMethod s256 / plain
     * @param codeVerifier        客户端生产的校验码
     * @return code challenge
     */
    public static String codeChallenge(String codeChallengeMethod, String codeVerifier) {
        if (Algorithm.SHA256.getValue().equalsIgnoreCase(codeChallengeMethod)) {
            // https://tools.ietf.org/html/rfc7636#section-4.2
            // code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
            return new String(Base64.encode(digest(codeVerifier), true), StandardCharsets.US_ASCII);
        } else {
            return codeVerifier;
        }
    }

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
     * 构造url
     *
     * @return url
     */
    public String build() {
        return this.build(false);
    }

    /**
     * 构造url
     *
     * @param encode 转码
     * @return url
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
     * 只读的参数Map
     *
     * @return unmodifiable Map
     */
    public Map<String, Object> getReadOnlyParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this Builder
     */
    public Builder queryParam(String key, Object value) {
        if (StringKit.isEmpty(key)) {
            throw new RuntimeException("参数名不能为空");
        }
        String valueAsString = (value != null ? value.toString() : null);
        this.params.put(key, valueAsString);

        return this;
    }

}
