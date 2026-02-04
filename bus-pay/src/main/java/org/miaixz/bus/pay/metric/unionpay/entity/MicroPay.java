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
package org.miaixz.bus.pay.metric.unionpay.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * UnionPay Cloud QuickPass - Micropay model.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MicroPay extends Voucher {

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
     * Merchant order number.
     */
    private String out_trade_no;
    /**
     * Device information.
     */
    private String device_info;
    /**
     * Body of the request.
     */
    private String body;
    /**
     * Goods detail.
     */
    private String goods_detail;
    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Attached data.
     */
    private String attach;
    /**
     * Whether a receipt is needed.
     */
    private String need_receipt;
    /**
     * Total fee.
     */
    private String total_fee;
    /**
     * Merchant creation IP.
     */
    private String mch_create_ip;
    /**
     * Authorization code.
     */
    private String auth_code;
    /**
     * Transaction start time.
     */
    private String time_start;
    /**
     * Transaction expiration time.
     */
    private String time_expire;
    /**
     * Operator ID.
     */
    private String op_user_id;
    /**
     * Operator shop ID.
     */
    private String op_shop_id;
    /**
     * Operator device ID.
     */
    private String op_device_id;
    /**
     * Goods tag.
     */
    private String goods_tag;
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
