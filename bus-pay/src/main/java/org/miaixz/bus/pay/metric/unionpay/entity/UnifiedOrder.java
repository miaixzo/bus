/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
