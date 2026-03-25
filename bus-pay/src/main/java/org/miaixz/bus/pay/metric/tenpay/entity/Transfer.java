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
package org.miaixz.bus.pay.metric.tenpay.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Enterprise payment to balance model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends Voucher {

    /**
     * Character set.
     */
    private String input_charset;
    /**
     * Recipient's OpenID.
     */
    private String openid;
    /**
     * Recipient's UIN.
     */
    private String uin;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Merchant order number.
     */
    private String out_trade_no;
    /**
     * Currency type.
     */
    private String fee_type;
    /**
     * Total amount.
     */
    private String total_fee;
    /**
     * Payment memo.
     */
    private String memo;
    /**
     * Check user name option.
     */
    private String check_name;
    /**
     * Recipient's name.
     */
    private String re_user_name;
    /**
     * Whether to forcibly check the user's real name.
     */
    private String check_real_name;
    /**
     * Operator ID.
     */
    private String op_user_id;
    /**
     * Operator password.
     */
    private String op_user_passwd;
    /**
     * Terminal IP address.
     */
    private String spbill_create_ip;
    /**
     * Asynchronous notification URL.
     */
    private String notify_url;

}
