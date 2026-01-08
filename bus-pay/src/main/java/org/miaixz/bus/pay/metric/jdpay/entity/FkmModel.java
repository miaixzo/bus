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
package org.miaixz.bus.pay.metric.jdpay.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Payment code interface model.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FkmModel extends Voucher {

    /**
     * User payment code.
     */
    private String token;
    /**
     * Version number.
     */
    private String version;
    /**
     * Merchant number.
     */
    private String merchant;
    /**
     * Device information.
     */
    private String device;
    /**
     * Transaction number.
     */
    private String tradeNum;
    /**
     * Transaction name.
     */
    private String tradeName;
    /**
     * Transaction description.
     */
    private String tradeDesc;
    /**
     * Transaction time.
     */
    private String tradeTime;
    /**
     * Transaction amount.
     */
    private String amount;
    /**
     * Industry category code.
     */
    private String industryCategoryCode;
    /**
     * Currency.
     */
    private String currency;
    /**
     * Note.
     */
    private String note;
    /**
     * Asynchronous notification URL.
     */
    private String notifyUrl;
    /**
     * Number of items in the order.
     */
    private String orderGoodsNum;
    /**
     * Vendor ID.
     */
    private String vendorId;
    /**
     * List of goods information.
     */
    private String goodsInfoList;
    /**
     * Receiver information.
     */
    private String receiverInfo;
    /**
     * Terminal information.
     */
    private String termInfo;
    /**
     * Payment merchant.
     */
    private String payMerchant;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Risk information.
     */
    private String riskInfo;
    /**
     * Business type.
     */
    private String bizTp;

}
