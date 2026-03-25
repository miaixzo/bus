/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.unionpay.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * UnionPay Cloud QuickPass - Auth Code to OpenID model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeToOpenId extends Voucher {

    /**
     * Service name.
     */
    private String service;
    /**
     * Version number.
     */
    private String version;
    /**
     * Character set.
     */
    private String charset;
    /**
     * Signature type.
     */
    private String sign_type;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Authorization code.
     */
    private String auth_code;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Agent signature number.
     */
    private String sign_agentno;
    /**
     * Group number.
     */
    private String groupno;

}
