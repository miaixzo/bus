/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.aoju.bus.core.key.ID;
import org.aoju.bus.core.key.ObjectID;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 构造URL
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Setter
public class Builder {

    private final Map<String, Object> params = new LinkedHashMap<>(7);
    private String baseUrl;

    private Builder() {

    }

    /**
     * @param baseUrl 基础路径
     * @return the new {@code UrlBuilder}
     */
    public static Builder fromUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.setBaseUrl(baseUrl);
        return builder;
    }

    /**
     * 如果给定字符串{@code text}中不包含{@code appendStr},则在{@code text}后追加{@code appendStr}；
     * 如果已包含{@code appendStr},则在{@code text}后追加{@code otherwise}
     *
     * @param text      给定的字符串
     * @param appendStr 需要追加的内容
     * @param otherwise 当{@code appendStr}不满足时追加到{@code text}后的内容
     * @return 追加后的字符串
     */
    public static String appendIfNotContain(String text, String appendStr, String otherwise) {
        if (StringKit.isEmpty(text) || StringKit.isEmpty(appendStr)) {
            return text;
        }
        if (text.contains(appendStr)) {
            return text.concat(otherwise);
        }
        return text.concat(appendStr);
    }

    /**
     * map转字符串,转换后的字符串格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param params 待转换的map
     * @param encode 是否转码
     * @return the string
     */
    public static String parseMapToString(Map<String, Object> params, boolean encode) {
        List<String> paramList = new ArrayList<>();
        params.forEach((k, v) -> {
            if (ObjectKit.isNull(v)) {
                paramList.add(k + Symbol.EQUAL);
            } else {
                String valueString = v.toString();
                paramList.add(k + Symbol.EQUAL + (encode ? UriKit.encode(valueString) : valueString));
            }
        });
        return CollKit.join(paramList, Symbol.AND);
    }

    /**
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this UrlBuilder
     */
    public Builder queryParam(String key, Object value) {
        Assert.notBlank(key, "参数名不能为空");
        String valueAsString = (null != value ? value.toString() : null);
        this.params.put(key, valueAsString);
        return this;
    }

    /**
     * 只读的 Map, clone 内部实现也是 putAll
     * HashMap#putAll 可实现对 基本类型 和 String 类型的深度复制
     *
     * @return Map
     */
    public Map<String, Object> getReadParams() {
        return (Map<String, Object>) ((LinkedHashMap<String, Object>) params).clone();
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
        String baseUrl = appendIfNotContain(this.baseUrl, Symbol.QUESTION_MARK, Symbol.AND);
        String paramString = parseMapToString(this.params, encode);
        return baseUrl + paramString;
    }

    @Getter
    @AllArgsConstructor
    public enum ErrorCode {
        SUCCESS("0", "Success"),
        FAILURE("-1", "Failure"),
        NOT_IMPLEMENTED("5001", "Not Implemented"),
        PARAMETER_INCOMPLETE("5002", "Parameter incomplete"),
        UNSUPPORTED("5003", "Unsupported operation"),
        NO_AUTH_SOURCE("5004", "AuthDefaultSource cannot be null"),
        UNIDENTIFIED_PLATFORM("5005", "Unidentified platform"),
        ILLEGAL_REDIRECT_URI("5006", "Illegal redirect uri"),
        ILLEGAL_REQUEST("5007", "Illegal request"),
        ILLEGAL_CODE("5008", "Illegal code"),
        ILLEGAL_STATUS("5009", "Illegal state"),
        REQUIRED_REFRESH_TOKEN("5010", "The refresh token is required; it must not be null"),
        ILLEGAL_TOKEN("5011", "Invalid token");

        private String code;
        private String msg;

    }

    /**
     * 缓存类型
     */
    @Getter
    @ToString
    public enum Type {
        /**
         * 使用内置的缓存
         */
        DEFAULT,
        /**
         * 使用Redis缓存
         */
        REDIS,
        /**
         * 自定义缓存
         */
        CUSTOM
    }

    @Getter
    @Setter
    @lombok.Builder
    public static class Token {

        /**
         * token风格: uuid
         */
        public static final String TOKEN_STYLE_UUID = "uuid";

        /**
         * token风格: 简单uuid (不带下划线)
         */
        public static final String TOKEN_STYLE_SIMPLE_UUID = "simple-uuid";

        /**
         * token风格: 32位随机字符串
         */
        public static final String TOKEN_STYLE_RANDOM_32 = "random-32";

        /**
         * token风格: 64位随机字符串
         */
        public static final String TOKEN_STYLE_RANDOM_64 = "random-64";

        /**
         * token风格: 128位随机字符串
         */
        public static final String TOKEN_STYLE_RANDOM_128 = "random-128";

        /**
         * token风格: tik风格 (2_14_16)
         */
        public static final String TOKEN_STYLE_RANDOM_TIK = "tik";

        private int expireIn;
        private String accessToken;
        private String refreshToken;
        private String uid;
        private String openId;
        private String accessCode;
        private String unionId;

        /**
         * Google附带属性
         */
        private String scope;
        private String tokenType;
        private String idToken;

        /**
         * 小米附带属性
         */
        private String macAlgorithm;
        private String macKey;

        /**
         * 企业微信附带属性
         */
        private String code;

        /**
         * 根据配置的tokenStyle生成不同风格的token
         *
         * @param style token样式
         * @return token
         */
        public static String create(String style) {
            // UUID
            if (TOKEN_STYLE_UUID.equals(style)) {
                return org.aoju.bus.core.key.UUID.randomUUID().toString();
            }
            // 简单uuid (不带下划线)
            if (TOKEN_STYLE_SIMPLE_UUID.equals(style)) {
                return ID.randomUUID().replaceAll(Symbol.MINUS, Normal.EMPTY);
            }
            // 32位随机字符串
            if (TOKEN_STYLE_RANDOM_32.equals(style)) {
                return RandomKit.randomString(Normal._32);
            }
            // 64位随机字符串
            if (TOKEN_STYLE_RANDOM_64.equals(style)) {
                return RandomKit.randomString(Normal._64);
            }
            // 128位随机字符串
            if (TOKEN_STYLE_RANDOM_128.equals(style)) {
                return RandomKit.randomString(Normal._128);
            }
            // tik风格 (2_14_16)
            if (TOKEN_STYLE_RANDOM_TIK.equals(style)) {
                return RandomKit.randomString(2) + "_" + RandomKit.randomString(14) + "_" + RandomKit.randomString(Normal._16) + "__";
            }
            // 默认，还是ObjectID
            return ObjectID.id();
        }

    }

}
