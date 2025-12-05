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
package org.miaixz.bus.pay.metric.wechat.entity.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.pay.magic.Voucher;

/**
 * Model for the Unified Order API.
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
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Sub-merchant ID.
     */
    private String sub_mch_id;
    /**
     * Device information.
     */
    private String device_info;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Signature type.
     */
    private String sign_type;
    /**
     * Product description.
     */
    private String body;
    /**
     * Product details (JSON format).
     */
    private String detail;
    /**
     * Additional data.
     */
    private String attach;
    /**
     * Merchant's order number.
     */
    private String out_trade_no;
    /**
     * Currency type.
     */
    private String fee_type;
    /**
     * Total fee in cents.
     */
    private String total_fee;
    /**
     * Client IP address of the machine making the request.
     */
    private String spbill_create_ip;
    /**
     * Transaction start time in YYYYMMDDHHMMSS format.
     */
    private String time_start;
    /**
     * Transaction expiration time in YYYYMMDDHHMMSS format.
     */
    private String time_expire;
    /**
     * Goods tag for promotional purposes.
     */
    private String goods_tag;
    /**
     * The URL to receive asynchronous payment notifications.
     */
    private String notify_url;
    /**
     * Trade type (e.g., JSAPI, NATIVE, APP).
     */
    private String trade_type;
    /**
     * Product ID, required when trade_type is NATIVE.
     */
    private String product_id;
    /**
     * Specifies payment methods to limit (e.g., no_credit).
     */
    private String limit_pay;
    /**
     * User's OpenID under the Official Account App ID.
     */
    private String openid;
    /**
     * User's OpenID under the sub-merchant's App ID.
     */
    private String sub_openid;
    /**
     * Whether to issue an electronic receipt (Y/N).
     */
    private String receipt;
    /**
     * Scene information in JSON format (e.g., for store details).
     */
    private String scene_info;
    /**
     * Whether to use profit sharing (Y/N).
     */
    private String profit_sharing;

}
