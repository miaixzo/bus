/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
