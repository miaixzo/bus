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
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Model for the V3 Unified Order API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedOrder {

    /**
     * The AppID of the Official Account or Mini Program.
     */
    private String appid;
    /**
     * The AppID of the service provider.
     */
    private String sp_appid;
    /**
     * The merchant ID for a direct merchant.
     */
    private String mchid;
    /**
     * The merchant ID for a service provider.
     */
    private String sp_mchid;
    /**
     * The AppID of the sub-merchant.
     */
    private String sub_appid;
    /**
     * The ID of the sub-merchant.
     */
    private String sub_mchid;
    /**
     * A description of the goods or service.
     */
    private String description;
    /**
     * The merchant's unique order number.
     */
    private String out_trade_no;
    /**
     * The time when the transaction expires, in RFC3339 format.
     */
    private String time_expire;
    /**
     * Additional data that will be returned in the payment notification.
     */
    private String attach;
    /**
     * The URL to receive asynchronous payment notifications.
     */
    private String notify_url;
    /**
     * A tag for the order, used for promotional purposes.
     */
    private String goods_tag;
    /**
     * Settlement information, including profit sharing details.
     */
    private SettleInfo settle_info;
    /**
     * The order amount details.
     */
    private Amount amount;
    /**
     * Information about the payer.
     */
    private Payer payer;
    /**
     * Discount and promotion details.
     */
    private Detail detail;
    /**
     * Scene information, such as the client IP and store details.
     */
    private SceneInfo scene_info;
    /**
     * A flag indicating whether an electronic invoice (fapiao) is supported.
     */
    private boolean support_fapiao;

}
