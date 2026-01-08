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
