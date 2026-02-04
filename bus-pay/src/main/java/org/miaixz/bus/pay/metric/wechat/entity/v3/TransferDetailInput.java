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
package org.miaixz.bus.pay.metric.wechat.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Model for an individual transfer detail within a batch transfer.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDetailInput {

    /**
     * The merchant's unique identifier for this specific detail within the batch. Can only consist of numbers and
     * uppercase/lowercase letters.
     */
    private String out_detail_no;
    /**
     * The transfer amount in cents.
     */
    private Integer transfer_amount;
    /**
     * A remark for this specific transfer, which the recipient will see. UTF-8 encoded, up to 32 characters.
     */
    private String transfer_remark;
    /**
     * The user's OpenID under the merchant's AppID.
     */
    private String openid;
    /**
     * The recipient's real name. This field must be encrypted. It is not allowed for transfers less than 0.3 yuan. It
     * is required for transfers greater than or equal to 2,000 yuan.
     */
    private String user_name;
    /**
     * The recipient's ID card number. This field is optional and must be encrypted if provided. If this field is
     * provided, the user_name must also be provided.
     */
    private String user_id_card;

}
