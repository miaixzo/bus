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
 * @since Java 21+
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
