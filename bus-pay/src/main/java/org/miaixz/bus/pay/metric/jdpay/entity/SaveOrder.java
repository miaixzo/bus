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
package org.miaixz.bus.pay.metric.jdpay.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Online payment interface model.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SaveOrder extends Voucher {

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
     * Settlement currency.
     */
    private String settleCurrency;
    /**
     * Cross-border information.
     */
    private String kjInfo;
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
