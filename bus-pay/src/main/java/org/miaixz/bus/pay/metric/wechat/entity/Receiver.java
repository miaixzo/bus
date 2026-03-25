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
package org.miaixz.bus.pay.metric.wechat.entity;

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Generic profit sharing receiver.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Receiver extends Voucher {

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
