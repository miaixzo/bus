/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.oauth.provider.DefaultProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构造URL
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
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
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this UrlBuilder
     */
    public Builder queryParam(String key, Object value) {
        Assert.notBlank(key, "参数名不能为空");
        String valueAsString = (value != null ? value.toString() : null);
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
        if (MapUtils.isEmpty(this.params)) {
            return this.baseUrl;
        }
        String baseUrl = DefaultProvider.appendIfNotContain(this.baseUrl, Symbol.QUESTION_MARK, Symbol.AND);
        String paramString = DefaultProvider.parseMapToString(this.params, encode);
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
        ILLEGAL_CODE("5008", "Illegal code");

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

    }

}
