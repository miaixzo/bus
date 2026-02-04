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
 * Model for the WeCom Pay - Send Corporate Red Packet API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SendWorkWxRedPack extends Voucher {

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
     * App ID of the WeCom application.
     */
    private String wxappid;
    /**
     * Sender's name.
     */
    private String sender_name;
    /**
     * WeCom application agent ID.
     */
    private String agentid;
    /**
     * Media ID for the sender's header image.
     */
    private String sender_header_media_id;
    /**
     * Recipient's OpenID.
     */
    private String re_openid;
    /**
     * Total amount in cents.
     */
    private String total_amount;
    /**
     * Wishing message.
     */
    private String wishing;
    /**
     * Activity name.
     */
    private String act_name;
    /**
     * Remark.
     */
    private String remark;
    /**
     * Scene ID.
     */
    private String scene_id;
    /**
     * WeCom-specific signature.
     */
    private String workwx_sign;

}
