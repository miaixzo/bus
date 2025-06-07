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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 无需签名的 JWT 签名器。
 * <p>
 * 实现 {@link JWTSigner} 接口，用于不需签名验证的 JWT 场景（算法标识为 "none"）。 该签名器返回空签名，并验证签名是否为空。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see JWTSigner
 */
public class NoneJWTSigner implements JWTSigner {

    /**
     * 无签名算法标识，值为 "none"。
     */
    public static final String ID_NONE = Normal.NONE;

    /**
     * 单例实例，无需签名的签名器。
     */
    public static NoneJWTSigner NONE = new NoneJWTSigner();

    /**
     * 生成 JWT 签名。
     * <p>
     * 对于 "none" 算法，返回空字符串作为签名。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @return 空字符串签名
     */
    @Override
    public String sign(final String headerBase64, final String payloadBase64) {
        return Normal.EMPTY;
    }

    /**
     * 验证 JWT 签名。
     * <p>
     * 检查提供的签名是否为空字符串，表示无签名验证通过。
     * </p>
     *
     * @param headerBase64  Base64 编码的 JWT header
     * @param payloadBase64 Base64 编码的 JWT payload
     * @param signBase64    Base64 编码的签名
     * @return 是否验证通过（签名为空时返回 true）
     */
    @Override
    public boolean verify(final String headerBase64, final String payloadBase64, final String signBase64) {
        return StringKit.isEmpty(signBase64);
    }

    /**
     * 获取签名算法标识。
     *
     * @return 算法标识 "none"
     */
    @Override
    public String getAlgorithm() {
        return ID_NONE;
    }

}