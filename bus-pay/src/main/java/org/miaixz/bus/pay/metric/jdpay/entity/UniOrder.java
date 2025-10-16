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
package org.miaixz.bus.pay.metric.jdpay.entity;

import org.miaixz.bus.pay.magic.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Unified order interface model.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UniOrder extends Material {

    /**
     * Version number.
     */
    private String version;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Merchant number.
     */
    private String merchant;
    /**
     * Payment merchant number.
     */
    private String payMerchant;
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
     * Order type.
     */
    private String orderType;
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
     * Callback URL.
     */
    private String callbackUrl;
    /**
     * Asynchronous notification URL.
     */
    private String notifyUrl;
    /**
     * IP address.
     */
    private String ip;
    /**
     * Specific card number.
     */
    private String specCardNo;
    /**
     * Specific ID.
     */
    private String specId;
    /**
     * Specific name.
     */
    private String specName;
    /**
     * User ID.
     */
    private String userId;
    /**
     * Trade type.
     */
    private String tradeType;
    /**
     * Expiration time.
     */
    private String expireTime;
    /**
     * Number of goods in the order.
     */
    private String orderGoodsNum;
    /**
     * Vendor ID.
     */
    private String vendorId;
    /**
     * Goods information.
     */
    private String goodsInfo;
    /**
     * Receiver information.
     */
    private String receiverInfo;
    /**
     * Terminal information.
     */
    private String termInfo;
    /**
     * Risk information.
     */
    private String riskInfo;
    /**
     * Installment number.
     */
    private String installmentNum;
    /**
     * Pre-product information.
     */
    private String preProduct;
    /**
     * Business type.
     */
    private String bizTp;

}
