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
import org.miaixz.bus.pay.magic.Voucher;
import org.miaixz.bus.pay.metric.wechat.entity.Receiver;

import java.util.List;

/**
 * Model for the V3 Profit Sharing API. Supports single/multiple profit sharing requests, adding/deleting receivers, and
 * finishing profit sharing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitSharing extends Voucher {

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
