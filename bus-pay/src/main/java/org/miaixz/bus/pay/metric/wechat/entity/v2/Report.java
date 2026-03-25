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

import org.miaixz.bus.pay.magic.Voucher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Transaction guarantee model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends Voucher {

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
     * Device information.
     */
    private String device_info;
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
     * Interface URL.
     */
    private String interface_url;
    /**
     * Execution time.
     */
    private String execute_time;
    /**
     * Return code.
     */
    private String return_code;
    /**
     * Return message.
     */
    private String return_msg;
    /**
     * Result code.
     */
    private String result_code;
    /**
     * Error code.
     */
    private String err_code;
    /**
     * Error code description.
     */
    private String err_code_des;
    /**
     * Merchant order number.
     */
    private String out_trade_no;
    /**
     * User IP address.
     */
    private String user_ip;
    /**
     * Time.
     */
    private String time;

}
