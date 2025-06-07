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

import java.io.Serial;
import java.util.Map;

/**
 * JWT头部信息
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTHeader extends Claims {

    @Serial
    private static final long serialVersionUID = 2852289258085L;

    /**
     * 加密算法，通常为HMAC SHA256（HS256）
     */
    public static String ALGORITHM = "alg";
    /**
     * 声明类型，一般为jwt
     */
    public static String TYPE = "typ";
    /**
     * 内容类型（content type）
     */
    public static String CONTENT_TYPE = "cty";
    /**
     * jwk的ID编号
     */
    public static String KEY_ID = "kid";

    /**
     * 增加“alg”头信息
     *
     * @param algorithm 算法ID，如HS265
     * @return this
     */
    public JWTHeader setAlgorithm(final String algorithm) {
        setClaim(ALGORITHM, algorithm);
        return this;
    }

    /**
     * 增加“typ”头信息
     *
     * @param type 类型，如JWT
     * @return this
     */
    public JWTHeader setType(final String type) {
        setClaim(TYPE, type);
        return this;
    }

    /**
     * 增加“cty”头信息
     *
     * @param contentType 内容类型
     * @return this
     */
    public JWTHeader setContentType(final String contentType) {
        setClaim(CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * 增加“kid”头信息
     *
     * @param keyId kid
     * @return this
     */
    public JWTHeader setKeyId(final String keyId) {
        setClaim(KEY_ID, keyId);
        return this;
    }

    /**
     * 增加自定义JWT认证头
     *
     * @param headerClaims 头信息
     * @return this
     */
    public JWTHeader addHeaders(final Map<String, ?> headerClaims) {
        putAll(headerClaims);
        return this;
    }

}
