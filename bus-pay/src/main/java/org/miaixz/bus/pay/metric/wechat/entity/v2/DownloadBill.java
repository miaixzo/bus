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
 * Model for the Download Bill API.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadBill extends Voucher {

    /**
     * Sub-merchant App ID.
     */
    private String sub_appid;
    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant ID.
     */
    private String sub_mch_id;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Signature.
     */
    private String sign;
    /**
     * Signature type.
     */
    private String sign_type;
    /**
     * The date of the bill in YYYYMMDD format.
     */
    private String bill_date;
    /**
     * The type of bill to download (e.g., ALL, SUCCESS, REFUND).
     */
    private String bill_type;
    /**
     * The compression type for the bill file (e.g., GZIP).
     */
    private String tar_type;

}
