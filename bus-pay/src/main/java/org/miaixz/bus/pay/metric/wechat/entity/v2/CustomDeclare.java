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
 * Model for the Customs Declaration API, used for submitting, querying, or re-pushing additional order information for
 * customs purposes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomDeclare extends Voucher {

    /**
     * Signature.
     */
    private String sign;
    /**
     * Signature type.
     */
    private String sign_type;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Merchant's order number.
     */
    private String out_trade_no;
    /**
     * WeChat's order number.
     */
    private String transaction_id;
    /**
     * Customs location (e.g., GUANGZHOU_ZS, HANGZHOU).
     */
    private String customs;
    /**
     * Merchant's customs registration number.
     */
    private String mch_customs_no;
    /**
     * Duty fee.
     */
    private String duty;
    /**
     * Action type: ADD or MODIFY.
     */
    private String action_type;
    /**
     * Merchant's customs sub-order number.
     */
    private String sub_order_no;
    /**
     * WeChat's customs sub-order ID.
     */
    private String sub_order_id;
    /**
     * Currency type.
     */
    private String fee_type;
    /**
     * Order fee.
     */
    private String order_fee;
    /**
     * Transport fee.
     */
    private String transport_fee;
    /**
     * Product fee.
     */
    private String product_fee;
    /**
     * Certificate type (e.g., ID_CARD).
     */
    private String cert_type;
    /**
     * Certificate ID number.
     */
    private String cert_id;
    /**
     * Name associated with the certificate.
     */
    private String name;

}
