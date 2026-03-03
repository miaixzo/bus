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

/**
 * Model for the V3 Add Profit Sharing Receivers API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddReceivers extends Voucher {

    /**
     * Sub-merchant ID assigned by WeChat Pay, which is the contributing merchant for profit sharing. (Not required for
     * direct merchants, but required for service providers).
     */
    private String sub_mchid;
    /**
     * The Official Account AppID of the sub-merchant. Required when the receiver type includes PERSONAL_SUB_OPENID.
     * (Not required for direct merchants, but required for service providers).
     */
    private String sub_appid;
    /**
     * The type of the profit sharing receiver. MERCHANT_ID: Merchant ID PERSONAL_OPENID: Personal openid (converted
     * from the parent merchant's APPID) PERSONAL_SUB_OPENID: Personal sub_openid (converted from the sub-merchant's
     * APPID)
     */
    private String type;
    /**
     * The account of the profit sharing receiver. If type is MERCHANT_ID, this is the merchant ID (mch_id or
     * sub_mch_id). If type is PERSONAL_OPENID, this is the personal openid. If type is PERSONAL_SUB_OPENID, this is the
     * personal sub_openid.
     */
    private String account;
    /**
     * The full name of the profit sharing receiver. If type is MERCHANT_ID, this is the full name of the merchant
     * (required). For micro-merchants or individual businesses, it is the name of the account holder. If type is
     * PERSONAL_OPENID or PERSONAL_SUB_OPENID, this is the person's name (optional, but will be validated if provided).
     * This field must be encrypted using the public key from the WeChat Pay platform certificate with the RSAES-OAEP
     * algorithm. The Wechatpay-Serial header must be set to the certificate's serial number.
     */
    private String name;
    /**
     * The relationship type between the sub-merchant and the receiver. Enum values: SERVICE_PROVIDER, STORE, STAFF,
     * STORE_OWNER, PARTNER, HEADQUARTER, BRAND, DISTRIBUTOR, USER, SUPPLIER, CUSTOM.
     */
    private String relation_type;
    /**
     * A custom-defined relationship between the sub-merchant and the receiver. This field is required when
     * relation_type is CUSTOM and should be at most 10 characters.
     */
    private String custom_relation;

}
