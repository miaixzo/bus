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
 * Model for submitting a micropayment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MicroPay extends Voucher {

    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant ID.
     */
    private String sub_mch_id;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Body of the request.
     */
    private String body;
    /**
     * Attached data.
     */
    private String attach;
    /**
     * Out trade number.
     */
    private String out_trade_no;
    /**
     * Fee type.
     */
    private String fee_type;
    /**
     * Total fee.
     */
    private String total_fee;
    /**
     * IP address of the client.
     */
    private String spbill_create_ip;
    /**
     * Limit pay.
     */
    private String limit_pay;
    /**
     * Promotion tag.
     */
    private String promotion_tag;
    /**
     * Asynchronous notification URL.
     */
    private String notify_url;
    /**
     * Device information.
     */
    private String device_info;
    /**
     * Authorization code.
     */
    private String auth_code;
    /**
     * Trade type.
     */
    private String trade_type;

}
