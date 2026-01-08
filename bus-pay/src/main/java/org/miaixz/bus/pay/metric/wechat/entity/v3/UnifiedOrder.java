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
