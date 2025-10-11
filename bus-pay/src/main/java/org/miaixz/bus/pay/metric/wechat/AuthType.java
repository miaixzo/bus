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
package org.miaixz.bus.pay.metric.wechat;

/**
 * WeChat Pay v3 interface authorization and authentication types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AuthType {

    /**
     * SM2 cryptographic algorithm.
     */
    SM2("WECHATPAY2-SM2-WITH-SM3", "AEAD_SM4_GCM", "SM Algorithm"),
    /**
     * RSA cryptographic algorithm.
     */
    RSA("WECHATPAY2-SHA256-RSA2048", "AEAD_AES_256_GCM", "RSA Algorithm");

    /**
     * The authorization type code.
     */
    private final String code;

    /**
     * The certificate algorithm identifier.
     */
    private final String cert;

    /**
     * The description of the authorization type.
     */
    private final String desc;

    /**
     * Constructs a new AuthType.
     *
     * @param code The authorization type code.
     * @param cert The certificate algorithm identifier.
     * @param desc The description of the authorization type.
     */
    AuthType(String code, String cert, String desc) {
        this.code = code;
        this.cert = cert;
        this.desc = desc;
    }

    /**
     * Gets the authorization type code.
     *
     * @return The authorization type code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the detailed description of the authorization type.
     *
     * @return The description.
     */
    public String getDesc() {
        return this.desc;
    }

    /**
     * Gets the platform certificate algorithm identifier.
     *
     * @return The certificate algorithm identifier.
     */
    public String getCert() {
        return this.cert;
    }

}
