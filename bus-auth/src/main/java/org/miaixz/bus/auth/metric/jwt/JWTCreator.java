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

import java.util.Map;

import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.core.xyz.MapKit;

/**
 * JSON Web Token (JWT)工具类
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTCreator {

    /**
     * 创建HS256(HmacSHA256) JWT Token
     *
     * @param payload 荷载信息
     * @param key     HS256(HmacSHA256)密钥
     * @return JWT Token
     */
    public static String create(final Map<String, ?> payload, final byte[] key) {
        return create(MapKit.of(JWTHeader.TYPE, "JWT"), payload, key);
    }

    /**
     * 创建HS256(HmacSHA256) JWT Token
     *
     * @param headers 头信息
     * @param payload 荷载信息
     * @param key     HS256(HmacSHA256)密钥
     * @return JWT Token
     */
    public static String create(final Map<String, ?> headers, final Map<String, ?> payload, final byte[] key) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setKey(key).sign();
    }

    /**
     * 创建JWT Token
     *
     * @param payload 荷载信息
     * @param signer  签名算法
     * @return JWT Token
     */
    public static String create(final Map<String, Object> payload, final JWTSigner signer) {
        return create(null, payload, signer);
    }

    /**
     * 创建JWT Token
     *
     * @param headers 头信息
     * @param payload 荷载信息
     * @param signer  签名算法
     * @return JWT Token
     */
    public static String create(final Map<String, Object> headers, final Map<String, Object> payload,
            final JWTSigner signer) {
        return JWT.of().addHeaders(headers).addPayloads(payload).setSigner(signer).sign();
    }

}
