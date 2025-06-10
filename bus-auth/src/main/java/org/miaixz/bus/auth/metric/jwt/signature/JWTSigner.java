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

/**
 * JWT签名接口封装，通过实现此接口，完成不同算法的签名功能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JWTSigner {

    /**
     * 签名
     *
     * @param headerBase64  JWT头的JSON字符串的Base64表示
     * @param payloadBase64 JWT载荷的JSON字符串Base64表示
     * @return 签名结果Base64，即JWT的第三部分
     */
    String sign(String headerBase64, String payloadBase64);

    /**
     * 验签
     *
     * @param headerBase64  JWT头的JSON字符串Base64表示
     * @param payloadBase64 JWT载荷的JSON字符串Base64表示
     * @param signBase64    被验证的签名Base64表示
     * @return 签名是否一致
     */
    boolean verify(String headerBase64, String payloadBase64, String signBase64);

    /**
     * 获取算法
     *
     * @return 算法
     */
    String getAlgorithm();

    /**
     * 获取算法ID，即算法的简写形式，如HS256
     *
     * @return 算法ID
     */
    default String getAlgorithmId() {
        return JWTSignerBuilder.getId(getAlgorithm());
    }

}
