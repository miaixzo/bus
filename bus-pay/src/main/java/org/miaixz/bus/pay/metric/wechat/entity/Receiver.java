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
package org.miaixz.bus.pay.metric.wechat.entity;

import org.miaixz.bus.pay.magic.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Generic profit sharing receiver.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Receiver extends Material {

    /**
     * Type of profit sharing receiver. MERCHANT_ID: Merchant ID (mch_id or sub_mch_id) PERSONAL_OPENID: Personal openid
     */
    private String type;
    /**
     * Account of profit sharing receiver. When type is MERCHANT_ID, it is the merchant ID (mch_id or sub_mch_id). When
     * type is PERSONAL_OPENID, it is the personal openid.
     */
    private String account;
    /**
     * Full name of profit sharing receiver. When type is MERCHANT_ID, it is the full merchant name (required). When
     * type is PERSONAL_OPENID, it is the personal name (optional, validated if provided).
     */
    private String name;
    /**
     * Relationship type with the profit sharer. Relationship between sub-merchant and receiver. This field is an enum:
     * SERVICE_PROVIDER: Service provider STORE: Store STAFF: Employee STORE_OWNER: Store owner PARTNER: Partner
     * HEADQUARTER: Headquarters BRAND: Brand DISTRIBUTOR: Distributor USER: User SUPPLIER: Supplier CUSTOM: Custom
     */
    private String relation_type;
    /**
     * Custom profit sharing relationship. This field has a maximum of 10 characters. Required when relation_type is
     * CUSTOM. Not required when relation_type is not CUSTOM.
     */
    private String custom_relation;
    /**
     * Amount.
     */
    private int amount;
    /**
     * Description of profit sharing.
     */
    private String description;

}
