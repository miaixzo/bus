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
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.pay.magic.Material;
import org.miaixz.bus.pay.metric.wechat.entity.Receiver;

import java.util.List;

/**
 * Model for the V3 Profit Sharing API. Supports single/multiple profit sharing requests, adding/deleting receivers, and
 * finishing profit sharing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSharing extends Material {

    /**
     * Service provider's merchant ID, assigned by WeChat Pay. Compatible with V2 API.
     */
    private String mch_id;
    /**
     * Sub-merchant ID assigned by WeChat Pay, who is the fund contributor for the profit sharing.
     */
    private String sub_mchid;
    /**
     * Sub-merchant's AppID. Required when the receiver type includes PERSONAL_SUB_OPENID.
     */
    private String sub_appid;
    /**
     * WeChat Pay's order number.
     */
    private String transaction_id;
    /**
     * The merchant's internal profit sharing order number. Must be unique within the merchant's system. Retrying with
     * the same number is treated as the same request.
     */
    private String out_order_no;
    /**
     * A list of profit sharing receivers, up to 50. The contributing merchant can also be a receiver.
     */
    private List<Receiver> receivers;
    /**
     * Whether to unfreeze any remaining unallocated funds. If true, the remaining amount is unfrozen and returned to
     * the contributing merchant. If false, the remaining amount is not unfrozen, and further profit sharing can be
     * performed on this order.
     */
    private boolean unfreeze_unsplit;

}
