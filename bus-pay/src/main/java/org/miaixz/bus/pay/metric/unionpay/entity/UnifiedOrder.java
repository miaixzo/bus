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
 * UnionPay Cloud QuickPass - Unified Order model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedOrder extends Voucher {

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
     * Application ID.
     */
    private String appid;
    /**
     * Whether to return the raw response.
     */
    private String is_raw;
    /**
     * Whether it is a mini program payment.
     */
    private String is_minipg;
    /**
     * Merchant order number.
     */
    private String out_trade_no;
    /**
     * Device information.
     */
    private String device_info;
    /**
     * Operator shop ID.
     */
    private String op_shop_id;
    /**
     * Body of the request.
     */
    private String body;
    /**
     * Sub-merchant OpenID.
     */
    private String sub_openid;
    /**
     * User ID.
     */
    private String user_id;
    /**
     * Attached data.
     */
    private String attach;
    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Total fee.
     */
    private String total_fee;
    /**
     * Whether a receipt is needed.
     */
    private String need_receipt;
    /**
     * Customer IP address.
     */
    private String customer_ip;
    /**
     * Merchant creation IP.
     */
    private String mch_create_ip;
    /**
     * Asynchronous notification URL.
     */
    private String notify_url;
    /**
     * Transaction start time.
     */
    private String time_start;
    /**
     * Transaction expiration time.
     */
    private String time_expire;
    /**
     * QR code timeout express.
     */
    private String qr_code_timeout_express;
    /**
     * Operator ID.
     */
    private String op_user_id;
    /**
     * Goods tag.
     */
    private String goods_tag;
    /**
     * Product ID.
     */
    private String product_id;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Buyer logon ID.
     */
    private String buyer_logon_id;
    /**
     * Buyer ID.
     */
    private String buyer_id;
    /**
     * Limit credit pay.
     */
    private String limit_credit_pay;
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
