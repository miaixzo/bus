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
package org.miaixz.bus.pay.metric.wechat.entity.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.pay.magic.Voucher;

/**
 * Model for the Send Red Packet API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SendRedPack extends Voucher {

    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Merchant's bill number.
     */
    private String mch_billno;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant ID.
     */
    private String sub_mch_id;
    /**
     * App ID of the Official Account.
     */
    private String wxappid;
    /**
     * App ID for receiving messages (for service providers).
     */
    private String msgappid;
    /**
     * Sender's name.
     */
    private String send_name;
    /**
     * Recipient's OpenID.
     */
    private String re_openid;
    /**
     * Total amount in cents.
     */
    private String total_amount;
    /**
     * Total number of red packets.
     */
    private String total_num;
    /**
     * Amount type for group red packets (ALL_RAND).
     */
    private String amt_type;
    /**
     * Wishing message for the red packet.
     */
    private String wishing;
    /**
     * Client IP address.
     */
    private String client_ip;
    /**
     * Activity name.
     */
    private String act_name;
    /**
     * Remark.
     */
    private String remark;
    /**
     * Scene ID for specific red packet scenarios.
     */
    private String scene_id;
    /**
     * Risk control information in JSON format.
     */
    private String risk_info;
    /**
     * Notification method.
     */
    private String notify_way;

}
