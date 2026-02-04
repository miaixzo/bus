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
package org.miaixz.bus.pay.metric.wechat.entity.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.pay.magic.Voucher;

/**
 * Model for obtaining device invocation credentials for face-scanning devices.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuthInfo extends Voucher {

    /**
     * Merchant ID.
     */
    private String mch_id;
    /**
     * Sub-merchant App ID (for service providers).
     */
    private String sub_appid;
    /**
     * Sub-merchant ID (for service providers).
     */
    private String sub_mch_id;
    /**
     * Current timestamp.
     */
    private String now;
    /**
     * Version number.
     */
    private String version;
    /**
     * Signature type.
     */
    private String sign_type;
    /**
     * Random string.
     */
    private String nonce_str;
    /**
     * Store ID.
     */
    private String store_id;
    /**
     * Store name.
     */
    private String store_name;
    /**
     * Device ID.
     */
    private String device_id;
    /**
     * Raw data for face recognition initialization.
     */
    private String rawdata;
    /**
     * Additional data.
     */
    private String attach;
    /**
     * Signature.
     */
    private String sign;

}
